---
title: Spring Boot Security ワークショップ 第一回
---

# Spring Boot Security ワークショップ 第一回

## ベースプロジェクト

[spring-boot-security-workshop-base.zip](https://github.com/mikoto2000/spring-boot-security-workshop/releases/download/v1.0.0/spring-boot-security-workshop-base.ziphttps://github.com/mikoto2000/spring-boot-security-workshop/releases/download/v1.0.0/spring-boot-security-workshop-base.zip) をダウンロードし、展開してください。

本ワークショップでは、このベースプロジェクトを元に Spring Security のカスタマイズを行っていきます。


## インデックスページの作成

### HTML の配置

`src/main/resources/templates/index.html` に、以下 HTML ファイルを配置します。

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>index</title>
</head>
<body>
  Hello Spring Security.
</body>
</html>
```

### コントローラーの作成

`src/main/java/dev/mikoto2000/security/controller/IndexController.java` に、以下 java ファイルを配置します。

```java
package dev.mikoto2000.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * IndexController
 */
@Controller
public class IndexController {
  @GetMapping("/")
  public String index() {
    return "index";
  }
}
```


## デフォルト時の動作確認

Spring Security の設定をしていない場合、起動時に以下のようにログインパスワードが表示されます。

```
2026-01-26T09:53:39.057Z  WARN 6610 --- [security] [  restartedMain] .s.a.UserDetailsServiceAutoConfiguration : 

Using generated security password: 5c89d5fc-d4ff-49b9-8bc0-62cb35f4f13d

This generated password is for development use only. Your security configuration must be updated before running your application in production.

```

デフォルトユーザーは `user` なので、このユーザー名・パスワードでログインできます。

`http://localhost:8080` を開くとログインフォームが表示されるので、ユーザー名: `user`, パスワード: `<起動時に表示されたパスワード>` でログインし、インデックスページが表示されれば OK です。

もちろんこれでは業務要件を満たすわけないので、これからワークショップで行うようなカスタマイズをしていくこととなります。


## ログインユーザー取得ロジックのカスタマイズ

ここでは、 Spring Security のカスタマイズポイントのひとつである「ログインユーザー取得ロジック」のカスタマイズを行います。

Spring Security では、ログイン時に「ユーザー名から認証対象ユーザーを取得する処理」を `UserDetailsService` というインタフェースに切り出しています。

この `UserDetailsService` を差し替えることで、「どこからユーザー情報を取得するか」をカスタマイズできるようになっています。

`src/main/java/dev/mikoto2000/security/configuration/UserDetailsServiceImpl.java` を作成し、次のように実装します。

```java
package dev.mikoto2000.security.configuration;

import java.util.HashMap;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * UserDetailsServiceImpl
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

  private HashMap<String, String> users = new HashMap<>();
  {
    // "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC" = "password"
    users.put("mikoto2000", "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC");
    users.put("mikoto2001", "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC");
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // ユーザーの存在チェック
    if (!users.containsKey(username)) {
      throw new UsernameNotFoundException(username);
    }

    // 見つけたユーザーの情報を返却(今回はユーザー名・パスワード以外は固定位置で返却)
    return User.withUsername(username)
      .password(users.get(username))
      .roles("ADMIN")
      .disabled(false)
      .build();
  }
}
```

こうすることで、「ユーザーを探して Spring Security の認証処理に必要なユーザー情報を返却する」という処理を自分で実装できます。
(UserDetailsService を実装したクラスを Bean として登録すると、Spring Security が自動的にこれを認証処理に利用します)

今回の実装では、メモリ上に HashMap でユーザー名とパスワードを保持し、そこからフォームから渡されたユーザー（ `loadUserByUsername` の仮引数 `username` ）を探すように実装しています。

今回は仕組み理解が目的のため、DB ではなく HashMap を使って最小構成で実装していますが、
本格的に実装するなら `loadUserByUsername` の中で DB 接続してユーザー情報を検索し、返却することになります。

これはワークショップの後半でやってみましょう。

また、パスワードについても、デフォルトで対応している `bcrypt` 形式でハッシュ化された文字列を使っています。
ここもカスタマイズポイントですので、後の方で取り上げます。


## 認可のカスタマイズ

ここからは「どのユーザーが、どのページを見られるか」を制御する「認可（Authorization）」の設定を行います。
認証（ログインできるか）と認可（アクセスできるか）は、Spring Security では別の関心事として扱われます。

`SecurityFilterChain` を Bean として定義することで、「認証方法」と「認可」のカスタマイズができます。
今回は「認証方法」は Spring Security が提供するデフォルト（ユーザー名・パスワードによるフォームログイン）のままで、「認可」のみをカスタマイズしてみましょう。

認可の条件は次の通りとします。

- `/` （インデックスページ）は誰でも表示可能
- `/private` はログイン済みユーザーしか表示できない


### 認証必須ページの作成

認証したら見えるページを作成します。

`src/main/resources/templates/private.html` を作成します。

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>認証必須ページ</title>
</head>
<body>
  これが見えているという事は、認証に成功しています。
</body>
</html>
```

### 認証必須ページのコントローラーを作成

インデックスページと同じようにコントローラーを作成します。

```java
package dev.mikoto2000.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PrivateController
 */
@Controller
public class PrivateController {
  @GetMapping("/private")
  public String privatePage() {
    return "private";
  }
}
```

### インデックスページの更新

認証必須ページへのリンクを追加します。

`src/main/resources/templates/index.html` を以下のように更新します。

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>index</title>
</head>
<body>
  Hello Spring Security.

  <!-- 追加ここから -->
  <p>
    <a href="/private">認証必須ページ</a>
  </p>
  <!-- 追加ここまで -->
</body>
</html>
```

### SecurityFilterChain の定義

`src/main/java/dev/mikoto2000/security/configuration/SecurityConfig.java` を作成し、以下のように実装します。

```java
package dev.mikoto2000.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 */
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // ログインフォームは Spring Security が提供するデフォルトを利用
    http.formLogin(Customizer.withDefaults())
    .authorizeHttpRequests(auth -> {
      auth
        // "/" は誰でも表示できる
        .requestMatchers("/").permitAll()
        // その他ページは、ログイン済みでないと表示できない
        .anyRequest().authenticated();
    });
    return http.build();
  }
}
```

SecurityFilterChain を Bean として定義すると、Spring Boot が自動設定していたデフォルトの SecurityFilterChain は無効化され、この設定が全面的に使われるようになります。


### 動作確認

`http://localhost:8080` にアクセスするとトップページが表示され、「認証必須ページ」のリンクを押下するとログイン画面に遷移。
その後ログインすると認証必須ページが表示されます。

これで、認可設定のカスタマイズが確認できました。


## ログアウトの実装

認証認可のカスタマイズができたので、今度はログアウトの実装をしましょう。

ログアウトは、 `SecurityFilterChain` にログアウトの設定を追加することで実現できます。

### ログアウト設定の追加

`src/main/java/dev/mikoto2000/security/configuration/SecurityConfig.java` を以下のように編集してください。

```java
package dev.mikoto2000.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 */
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // ログインフォームは Spring Security が提供するデフォルトを利用
    http.formLogin(Customizer.withDefaults())
    // ログアウト処理も、 Spring Security が提供するデフォルトを利用
    .logout(Customizer.withDefaults())
    .authorizeHttpRequests(auth -> {
      auth
        // "/" は誰でも表示できる
        .requestMatchers("/").permitAll()
        // その他ページは、ログイン済みでないと表示できない
        .anyRequest().authenticated();
    });
    return http.build();
  }
}
```

Spring Security が提供するデフォルトでは、 `/logout` にアクセスするとログアウト確認画面が表示され、
`Log Out` ボタンを押下するとログアウトし、 `/login` にリダイレクトされます。
(ログアウトは、 `/logout` への `POST` リクエストで実行される)

前述の通り、ログアウトは `/logout` への `POST` リクエストで実行されるため、
自作のフォームから(ログアウト画面を介さず)ログアウトさせることも可能です。
このあたりのカスタマイズはワークショップの後の方でやりましょう。


### ログアウト用リンクの追加

#### index.html

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>index</title>
</head>
<body>
  Hello Spring Security.

  <p>
    <a href="/private">認証必須ページ</a>
  </p>
  <!-- 追加ここから -->
  <p>
    <a href="/logout">ログアウト</a>
  </p>
  <!-- 追加ここまで -->
</body>
</html>
```

#### private.html

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>認証必須ページ</title>
</head>
<body>
  これが見えているという事は、認証に成功しています。
  <!-- 追加ここから -->
  <p>
    <a href="/logout">ログアウト</a>
  </p>
  <!-- 追加ここまで -->
</body>
</html>
```

### 動作確認

`http://localhost:8080` へアクセスし、private へ遷移 -> ログイン -> ログアウト -> ルート -> private というように移動してみましょう。
ログアウトできていることが確認できます。


## まとめ

ここまでで、次の作業を行ってきました。

- プロジェクトの作成
- デフォルト時の動作確認
- ログインユーザー取得ロジックのカスタマイズ
- 認可のカスタマイズ
- ログアウトの実装

これで、基本的な「ユーザー名とパスワードを用いたログイン」のカスタマイズポイントがわかってきたと思います。
次回は次の作業を行っていきます。

- ログイン・ログアウトのカスタマイズ
- DB からユーザー情報を取得するように修正
- ユーザー登録

