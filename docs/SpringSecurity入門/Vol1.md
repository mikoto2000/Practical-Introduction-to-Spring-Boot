---
title: Spring Boot Security 入門 第一回 - 認証・認可・ログアウト
---

# Spring Boot Security 入門 第一回 - 認証・認可・ログアウト

## 概要

Java / Spring Boot における Spring Security の基本を学ぶ勉強会です。認証（ログインできるか）と認可（アクセスできるか）の違い、ログインユーザー取得ロジックのカスタマイズ、認可の設定、ログアウトの実装を扱います。

## 対象読者

- Java / Spring Boot でアプリケーションを開発しているが、Spring Security を「なんとなく」使っている方
- 認証と認可の違いを整理したい方
- ログイン・ログアウトのカスタマイズポイントを学びたい方

## この資料の構成

この資料は 2 部構成 になっています。

1. 触って学ぶ Spring Security: サンプルアプリを実際に作りながら、認証・認可・ログアウトを実装していくハンズオン
2. 座学で学ぶ Spring Security: 認証と認可の違い、フィルタチェーン、パスワードハッシュなどの理論を整理する座学

「触って学ぶ」で実装した内容を、「座学で学ぶ」で概念として整理・復習する流れです。対応関係は次の表の通りです。

| 触って学ぶ（ハンズオン） | 座学で学ぶ（理論） |
|---|---|
| [デフォルト時の動作確認](#デフォルト時の動作確認) | [認証と認可の違い](#認証と認可の違い) |
| [ログインユーザー取得ロジックのカスタマイズ](#ログインユーザー取得ロジックのカスタマイズ) | [認証の仕組み（UserDetailsService）](#認証の仕組みuserdetailsservice) |
| [ログインユーザー取得ロジックのカスタマイズ](#ログインユーザー取得ロジックのカスタマイズ) | [パスワードハッシュ（bcrypt / DelegatingPasswordEncoder）](#パスワードハッシュbcrypt--delegatingpasswordencoder) |
| [認可のカスタマイズ](#認可のカスタマイズ) | [認可の仕組み（SecurityFilterChain / requestMatchers）](#認可の仕組みsecurityfilterchain--requestmatchers) |
| [ログアウトの実装](#ログアウトの実装) | [ログアウトの仕組み](#ログアウトの仕組み) |

## 触って学ぶ Spring Security

### ベースプロジェクト

[spring-boot-security-workshop-base.zip](https://github.com/mikoto2000/spring-boot-security-workshop/releases/download/v1.0.0/spring-boot-security-workshop-base.zip) をダウンロードし、展開してください。

本ハンズオンでは、このベースプロジェクトを元に Spring Security のカスタマイズを行っていきます。


### インデックスページの作成

#### HTML の配置

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

#### コントローラーの作成

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


### デフォルト時の動作確認

Spring Security の設定をしていない場合、起動時に以下のようにログインパスワードが表示されます。

```
2026-01-26T09:53:39.057Z  WARN 6610 --- [security] [  restartedMain] .s.a.UserDetailsServiceAutoConfiguration : 

Using generated security password: 5c89d5fc-d4ff-49b9-8bc0-62cb35f4f13d

This generated password is for development use only. Your security configuration must be updated before running your application in production.

```

デフォルトユーザーは `user` なので、このユーザー名・パスワードでログインできます。

`http://localhost:8080` を開くとログインフォームが表示されます。
ユーザー名: `user`, パスワード: `<起動時に表示されたパスワード>` でログインし、インデックスページが表示されれば OK です。

もちろんこれでは業務要件を満たすわけないので、これからハンズオンで行うようなカスタマイズをしていくこととなります。


### ログインユーザー取得ロジックのカスタマイズ

ここでは、 Spring Security のカスタマイズポイントのひとつである「ログインユーザー取得ロジック」のカスタマイズを行います。

> ここから先の動作確認では、デフォルトの生成パスワードは使われなくなり、**`mikoto2000` / `password`**（または `mikoto2001` / `password`）でログインします。

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

  public UserDetailsServiceImpl() {
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

今回の実装では、メモリ上に HashMap でユーザー名とパスワードを保持し、そこからフォームで渡されたユーザー（ `loadUserByUsername` の仮引数 `username` ）を探すように実装しています。

今回は仕組み理解が目的のため、DB ではなく HashMap を使って最小構成で実装していますが、
本格的に実装するなら `loadUserByUsername` の中で DB 接続してユーザー情報を検索し、返却することになります。

これはハンズオンの後半でやってみましょう。

また、パスワードについても、デフォルトで対応している `bcrypt` 形式でハッシュ化された文字列を使っています。
`{bcrypt}` プレフィックスは「この文字列は bcrypt でハッシュ化されたパスワードだ」ということを Spring Security に伝えるための目印です。
ここもカスタマイズポイントですので、後の方で取り上げます。


### 認可のカスタマイズ

ここからは「どのユーザーが、どのページを見られるか」を制御する「認可（Authorization）」の設定をします。
認証（ログインできるか）と認可（アクセスできるか）は、Spring Security では別の関心事として扱われます。

`SecurityFilterChain` を Bean として定義することで、「認証方法」と「認可」のカスタマイズができます。
今回は「認証方法」は Spring Security が提供するデフォルト（ユーザー名・パスワードによるフォームログイン）のままで、「認可」のみをカスタマイズしてみましょう。

認可の条件は次の通りとします。

- `/` （インデックスページ）は誰でも表示可能
- ルート以外のページはすべてログイン必須

`/private` は「ルート以外のページ」の代表例として、認証必須ページを作成します。


#### 認証必須ページの作成

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

#### 認証必須ページのコントローラーを作成

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

#### インデックスページの更新

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

#### SecurityFilterChain の定義

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

#### 動作確認

`http://localhost:8080` にアクセスするとトップページが表示され、「認証必須ページ」のリンクを押下するとログイン画面に遷移。
その後ログインすると認証必須ページが表示されます。

ログインには、前述の `UserDetailsServiceImpl` で定義した **`mikoto2000` / `password`** を使います。

これで、認可設定のカスタマイズが確認できました。


### ログアウトの実装

認証認可のカスタマイズができたので、今度はログアウトの実装をしましょう。

ログアウトは、 `SecurityFilterChain` にログアウトの設定を追加することで実現できます。

#### ログアウト設定の追加

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

Spring Security が提供するデフォルトでは、 `/logout` にアクセスするとログアウト確認画面が表示されます。
`Log Out` ボタンを押下するとログアウトし、 `/login` にリダイレクトされます。
(ログアウトは、 `/logout` への `POST` リクエストで実行される)

前述の通り、ログアウトは `/logout` への `POST` リクエストで実行されるため、
自作のフォームから(ログアウト画面を介さず)ログアウトさせることも可能です。
このあたりのカスタマイズはハンズオンの後の方でやりましょう。


#### ログアウト用リンクの追加

##### index.html

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

##### private.html

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

#### 動作確認

`http://localhost:8080` へアクセスし、private へ遷移 -> ログイン -> ログアウト -> ルート -> private というように移動してみましょう。
ログアウトできていることが確認できます。


### まとめ

ここまでで、次の作業を進めてきました。

- プロジェクトの作成
- デフォルト時の動作確認
- ログインユーザー取得ロジックのカスタマイズ
- 認可のカスタマイズ
- ログアウトの実装

これで、基本的な「ユーザー名とパスワードを用いたログイン」のカスタマイズポイントがわかってきたはずです。
次回は次の作業を進めます。

- ログイン・ログアウトのカスタマイズ
- DB からユーザー情報を取得するように修正
- ユーザー登録

## 座学で学ぶ Spring Security

### 認証と認可の違い

#### 認証（Authentication）と認可（Authorization）は別の関心事

Spring Security を理解するうえで、最初に押さえるべきは「認証」と「認可」が別の概念だということです。

- 認証（Authentication）: 「誰であるか」を確認する。ログインできるかどうか
- 認可（Authorization）: 「何を許可するか」を決める。アクセスできるかどうか

認証は「その人が本当にその人か」を確かめる処理で、認可は「その人に何を許すか」を決める処理です。たとえば、ログインに成功したユーザーが、必ずしもすべてのページを見られるわけではありません。ログインできた（認証）としても、管理者専用ページを見られるかは別の判断（認可）になります。

ハンズオンでは、この 2 つを別のタイミングで扱いました。まず `UserDetailsServiceImpl` で「誰としてログインするか」をカスタマイズし、その後 `SecurityFilterChain` で「どのページを見られるか」を設定しました。この順番は、認証が先で認可が後、という処理の流れに対応しています。

#### なぜ分けて考えるのか

認証と認可を分けて考えると、それぞれの関心事を独立して設定できます。「認証方法は変えずに、認可のルールだけ変える」といった変更が、互いに影響を与えずに行えます。ハンズオンでも、認証方法はデフォルトのままにして認可だけをカスタマイズしました。これは、2 つの関心事が独立しているからこそできる切り分けです。

### 認証の仕組み（UserDetailsService）

#### 認証の流れ

Spring Security のフォームログインでは、次のような流れで認証が行われます。

```text
ユーザーがユーザー名・パスワードを送信
↓
UserDetailsService がユーザー名からユーザー情報を取得
↓
取得したパスワードと入力されたパスワードを照合
↓
一致すれば認証成功、認可の判断へ進む
```

この流れの中で、アプリケーションがカスタマイズする代表的なポイントが `UserDetailsService` です。

#### UserDetailsService の役割

`UserDetailsService` は「ユーザー名から認証対象ユーザーを取得する処理」を切り出したインタフェースです。ハンズオンでは、このインタフェースを実装して「どこからユーザー情報を取得するか」を自分で決めました。

```java
public class UserDetailsServiceImpl implements UserDetailsService {
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // ここでユーザーを探して返す
  }
}
```

ハンズオンでは HashMap から探しましたが、ここを DB 検索に置き換えれば「DB からユーザー情報を取得する」実装になります。`UserDetailsService` は「ユーザー情報の取得元」を差し替えるための窓口であり、認証の仕組みそのものは Spring Security が担当します。

#### なぜ UserDetailsService に切り出すのか

ユーザー情報の取得元は、アプリケーションごとに異なります。メモリ上のマップ、DB、外部の認証サービスなど、取得元はさまざまです。Spring Security はこの部分をインタフェースに切り出し、アプリケーション側で実装を差し替えられるようにしています。これにより、認証の仕組みは共通のまま、ユーザー情報の取得方法だけを自由に変えられます。

### パスワードハッシュ（bcrypt / DelegatingPasswordEncoder）

#### パスワードをそのまま保存しない理由

パスワードを平文のまま保存すると、データが漏れたときにパスワードがそのまま相手に渡ってしまいます。そのため、保存時にはハッシュ化して、元のパスワードをそのまま持たないようにします。ハッシュ化された値から元のパスワードを復元するのは困難で、たとえデータが漏れてもパスワード自体は守られやすくなります。

#### bcrypt とは

bcrypt はパスワードのハッシュ化に使われるアルゴリズムのひとつです。特徴として、計算にわざと時間がかかる設計になっており、総当たり攻撃への耐性を高めています。Spring Security は bcrypt をデフォルトでサポートしています。

ハンズオンでは、次のように `{bcrypt}` プレフィックス付きのハッシュ文字列をパスワードとして設定しました。

```java
users.put("mikoto2000", "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC");
```

#### `{bcrypt}` プレフィックスの意味

`{bcrypt}` は「この文字列は bcrypt でハッシュ化されたパスワードだ」ということを Spring Security に伝える目印です。このプレフィックスは `DelegatingPasswordEncoder` という仕組みで解釈されます。

#### DelegatingPasswordEncoder とは

`DelegatingPasswordEncoder` は、複数のパスワードエンコーダーをプレフィックスで使い分けるための仕組みです。`{bcrypt}` なら bcrypt、`{noop}` なら平文、といったように、プレフィックスを見て適切なエンコーダーを選びます。これにより、既存のパスワードを移行しながら新しいアルゴリズムを導入する際も、形式を混在させられます。

ハンズオンでは bcrypt を使いましたが、この仕組みがあることで「どのアルゴリズムでハッシュ化したか」を文字列の先頭で管理できます。

### 認可の仕組み（SecurityFilterChain / requestMatchers）

#### SecurityFilterChain とは

Spring Security は、リクエストを一連のフィルタで処理します。このフィルタの並びを定義するのが `SecurityFilterChain` です。認証や認可の処理は、このフィルタチェーンの中で実行されます。

ハンズオンでは、`SecurityFilterChain` を Bean として定義しました。これを定義すると、Spring Boot が自動設定していたデフォルトのチェーンは無効化され、自分で定義した設定が全面的に使われます。

#### requestMatchers と anyRequest の違い

認可のルールは `authorizeHttpRequests` の中で指定します。

```java
auth
  .requestMatchers("/").permitAll()          // "/" は誰でも表示できる
  .anyRequest().authenticated();             // その他はログイン必須
```

- `requestMatchers("/")`: 指定したパスにだけルールを適用する
- `permitAll()`: 認証なしでアクセスを許可する
- `anyRequest()`: 上で指定しなかった残りのすべてのリクエストにルールを適用する
- `authenticated()`: ログイン済みのユーザーだけ許可する

この設定では「`/` は誰でも見られるが、それ以外はログイン必須」というルールになっています。ルールは上から順に評価されるため、先に `requestMatchers("/").permitAll()` で `/` を許可し、残りを `anyRequest().authenticated()` でログイン必須にしています。

#### 認可の判断はどのタイミングで行われるか

認可は、認証が成功した後に実行されます。フィルタチェーンの中で、まず認証フィルタがユーザーを特定し、その後で認可フィルタが「このユーザーはこのパスにアクセスしてよいか」を判断します。ハンズオンで「ログインしていないと `/private` にアクセスするとログイン画面に遷移する」という挙動は、この認可フィルタによるものです。

### ログアウトの仕組み

#### ログアウトは POST で実行される

ログアウトは、`/logout` への `POST` リクエストで実行されます。ハンズオンでは `logout(Customizer.withDefaults())` を追加し、Spring Security が提供するデフォルトのログアウト処理を利用しました。

デフォルトでは、`/logout` に `GET` でアクセスするとログアウト確認画面が表示され、`Log Out` ボタンが押されると `POST` でログアウトが実行されます。

#### なぜ GET ではなく POST なのか

ログアウトは状態を変更する操作です。状態を変更する操作を `GET` で実行すると、リンクを踏んだだけでログアウトが実行されてしまいます。たとえば、ブラウザのキャッシュやリンクの共有によって意図しないログアウトが発生する可能性があります。そのため、ログアウトのような状態変更は `POST` で実行するのが安全です。

ハンズオンで追加した `<a href="/logout">` は `GET` なので、確認画面を経由してから `POST` でログアウトする、という流れになっています。

#### ログアウト後の状態

ログアウトが実行されると、セッションに保持されていた認証情報が破棄され、以降のリクエストは認証されていない状態として扱われます。ハンズオンでは、ログアウト後にルートへ戻り、再度 `/private` へアクセスするとログイン画面に遷移することを確認しました。

## 用語集

| 用語 | 説明 |
|---|---|
| 認証（Authentication） | 「誰であるか」を確認する処理。ログインできるかどうか |
| 認可（Authorization） | 「何を許可するか」を決める処理。アクセスできるかどうか |
| UserDetailsService | ユーザー名から認証対象ユーザーを取得する処理を切り出したインタフェース |
| UserDetails | 認証に必要なユーザー情報（ユーザー名・パスワード・権限など）を表すオブジェクト |
| SecurityFilterChain | リクエストを処理するフィルタの並びを定義する仕組み |
| フィルタチェーン | 認証・認可などの処理を順番に実行する一連のフィルタの並び |
| requestMatchers | 認可ルールを適用するパスを指定するメソッド |
| permitAll | 認証なしでアクセスを許可する設定 |
| authenticated | ログイン済みのユーザーだけアクセスを許可する設定 |
| bcrypt | パスワードのハッシュ化に使われるアルゴリズムのひとつ |
| DelegatingPasswordEncoder | プレフィックスを見て適切なパスワードエンコーダーを選ぶ仕組み |
| ハッシュ化 | 元の値をそのまま持たず、計算によって変換した値で保存する仕組み |
| ログアウト | セッションの認証情報を破棄し、認証されていない状態に戻す操作 |

## 参考資料

- [Spring Security :: Spring リファレンス](https://spring.pleiades.io/spring-security/reference/index.html)
- [Password Storage :: Spring Security リファレンス](https://spring.pleiades.io/spring-security/reference/features/authentication/password-storage.html)
- [Servlet アプリケーションのアーキテクチャ :: Spring Security リファレンス](https://spring.pleiades.io/spring-security/reference/servlet/architecture.html)
