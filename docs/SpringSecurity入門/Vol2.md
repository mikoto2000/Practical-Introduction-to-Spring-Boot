---
title: Spring Boot Security ハンズオン 第二回
---

# Spring Boot Security 入門 第二回 - ログイン・ログアウトのカスタマイズとDB認証

## 概要

Java / Spring Boot における Spring Security の基本を学ぶ勉強会の第二回です。ログイン・ログアウトのカスタマイズ、DB からのユーザー情報取得、ユーザー登録、ロールを用いたアクセス制御（RBAC の触り）を扱います。

## 対象読者

- Java / Spring Boot でアプリケーションを開発しているが、Spring Security を「なんとなく」使っている方
- ログインページやログアウトを自作したい方
- ユーザー情報を DB から取得する構成を学びたい方

## この資料の構成

この資料は 2 部構成 になっています。

1. 触って学ぶ Spring Security: サンプルアプリを実際に作りながら、ログイン・ログアウトのカスタマイズ、DB 認証、ユーザー登録を実装していくハンズオン
2. 座学で学ぶ Spring Security: ログインページのカスタマイズ、ログアウトと CSRF、DB 認証、パスワードハッシュ、ロールと認可などの理論を整理する座学

「触って学ぶ」で実装した内容を、「座学で学ぶ」で概念として整理・復習する流れです。対応関係は次の表の通りです。

| 触って学ぶ（ハンズオン） | 座学で学ぶ（理論） |
|---|---|
| [ログイン・ログアウトのカスタマイズ](#ログインログアウトのカスタマイズ) | [ログインページのカスタマイズの仕組み](#ログインページのカスタマイズの仕組み) |
| [ログイン・ログアウトのカスタマイズ](#ログインログアウトのカスタマイズ) | [ログアウトの仕組みと CSRF](#ログアウトの仕組みと-csrf) |
| [DB からユーザー情報を取得するように修正](#db-からユーザー情報を取得するように修正) | [DB 認証の仕組み](#db-認証の仕組みuserdetailsservice-と-db) |
| [ユーザー登録](#ユーザー登録) | [パスワードハッシュと PasswordEncoder](#パスワードハッシュと-passwordencoder) |
| [ユーザー登録](#ユーザー登録) | [ロールと認可（RBAC の触り）](#ロールと認可rbac-の触り) |

## 触って学ぶ Spring Security

### はじめに

前回は、次の作業を進めてきました。

- プロジェクトの作成
- デフォルト時の動作確認
- ログインユーザー取得ロジックのカスタマイズ
- 認可のカスタマイズ
- ログアウトの実装

これで、基本的な「ユーザー名とパスワードを用いたログイン」のカスタマイズポイントがわかってきたはずです。
今回は、次の作業を進めます。

- ログイン・ログアウトのカスタマイズ
- DB からユーザー情報を取得するように修正
- ユーザー登録


## ログイン・ログアウトのカスタマイズ

これまでは Spring Security デフォルトのログイン・ログアウト画面を利用していましたが、今回はこれをカスタマイズしましょう。

### SecurityConfig の修正

まずは、 SecurityConfig の `formLogin` と `logout` を修正します。

`src/main/java/dev/mikoto2000/security/configuration/SecurityConfig.java`:

```java
package dev.mikoto2000.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 */
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    /* 修正ここから */
    // ログインフォームを自作し、ログイン関連 URL は誰でもアクセスできるよう指定
    // ログイン失敗時には "/login?error" へリダイレクトする
    http.formLogin(login -> {
      login
        .loginPage("/login")
        .permitAll()
        .failureUrl("/login?error");
    })
    // ログアウトは、画面の自作はせず
    // POST する URL とログアウト成功後のリダイレクト URL を指定する
    .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/"))
    /* 修正ここまで */
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

### ログインページ用のコントローラーを作成

今回はログインページを自作するので、ログインページ用のコントローラーも作成します。

`src/main/java/dev/mikoto2000/security/controller/LoginController.java`:

```java
package dev.mikoto2000.security.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * LoginController
 */
@Controller
public class LoginController {

  @GetMapping("/login")
  public String login() {
    return "login";
  }

}
```

### ログイン画面の作成

ログイン画面を用意します。

今回は、ユーザー名とパスワードを入れるテキストエリアとログインボタンがあるだけのシンプルなページにします。

`src/main/resources/templates/login.html`:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>LOGIN</title>
    <meta charset="UTF-8">
  </head>
  <body>
    <h1>ログインページ</h1>
    <div th:if="${param.error}">
      ユーザー名かパスワードが違います。
    </div>
    <form th:action="@{/login}" method="post">
      <div>
        <input type="text" name="username" placeholder="Username"/>
      </div>
      <div>
        <input type="password" name="password" placeholder="Password"/>
      </div>
      <input type="submit" value="ログイン" />
    </form>
  </body>
</html>
```

### ログアウトの仕組みを修正

今回は、ログアウト確認画面を表示せず、ログアウトボタンを押下したらすぐにログアウトするようにします。

第1回でも述べた通り、 Spring Security のデフォルトでは `/logout` に `POST` リクエストを送ることでログアウトします。

CSRF 対策のため、 Thymeleaf の機能を用いて `form` を構築します。
(`th:action` を使用すると、 form に自動で CSRF 対策のパラメーターが挿入される)

`src/main/resources/templates/index.html`:

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
  <!-- 変更ここから -->
  <form method="post" th:action="@{/logout}">
    <button type="submit">ログアウト</button>
  </form>
  <!-- 変更ここまで -->
</body>
</html>
```

`src/main/resources/templates/private.html`:

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8" />
  <title>認証必須ページ</title>
</head>
<body>
  これが見えているという事は、認証に成功しています。
  <!-- 変更ここから -->
  <form method="post" th:action="@{/logout}">
    <button type="submit">ログアウト</button>
  </form>
  <!-- 変更ここまで -->
</body>
</html>
```

ここまでの変更で、「自作ログインページでのログイン」と「ボタン押下で即ログアウト」が実現できます。

ログイン画面の自作はしていますが、ログイン処理は依然として Spring Security デフォルトのままです。
(今回は、 Spring Security に「ユーザー名とパスワード」を渡すところまでをカスタマイズしたということ)


## DB からユーザー情報を取得するように修正

さて、これまでは簡単のためにユーザー情報を HashMap で保持していましたが、ここで DB から取得するように修正しましょう。

今回は、インメモリの H2 DB と MyBatis の組み合わせを使います。

### DB・テーブル定義について

本プロジェクトには、既に H2 データベースの起動・接続・初期化の設定がされています。
これらについてはハンズオンの本題とはずれるので、テーブル定義の説明のみを行います。

#### テーブル定義

<!-- textlint-disable -->

ユーザー情報を格納するテーブルは、 `USERS` テーブルとして定義しています。
CREATE 文は `src/main/resources/schema.sql`, データは `src/main/resources/data.sql` で確認できます。

<!-- textlint-enable -->

role カラムを用意していますが、本格的な RBAC（ロール設計・権限設計）までは扱いません。

本ハンズオンでは、次の 2 点までをゴールとします。

- ロール情報をログインユーザーに持たせる
- URL / View レベルで制御できることを確認する

`src/main/resources/schema.sql`:

```sql
CREATE TABLE USERS (
  username VARCHAR(50) PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL,
  role VARCHAR(50) NOT NULL
);
```

`src/main/resources/data.sql`:

```sql
INSERT INTO USERS (username, password, enabled, role)
VALUES
(
  'mikoto2000',
  -- "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC" = "password"
  '{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC',
  true,
  'ADMIN'
),
(
  'mikoto2001',
  -- "{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC" = "password"
  '{bcrypt}$2a$10$0OsB8/8crrUzT9O8VNJF.uF2sB1c7tpvqJ/COY0Hm9qtoCETRa1cC',
  true,
  'ADMIN'
);
```

### エンティティの作成

テーブルから取得した値を格納するためのクラスを作成します。

`src/main/java/dev/mikoto2000/security/entity/User.java`:

```java
package dev.mikoto2000.security.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * User
 */
@AllArgsConstructor
@Data
public class User {
  private String username;
  private String password;
  private Boolean enabled;
  private String role;
}
```

### マッパーの作成

テーブルから情報を取得する Mapper インターフェースを作成します。

`username` を基にユーザー情報を取得する IF を定義します。

`src/main/java/dev/mikoto2000/security/repository/UsersMapper.java`:

```java
package dev.mikoto2000.security.repository;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import dev.mikoto2000.security.entity.User;

/**
 * UsersMapper
 */
@Mapper
public interface UsersMapper {
  @Select("""
          SELECT
            username,
            password,
            enabled,
            role
          FROM
            USERS
          WHERE
            USERS.username = #{username}
          """)
  Optional<User> findByUsername(String username);
}
```

### userDetailsServiceImpl の修正

これまでに作ったエンティティとマッパーを利用して、 DB からユーザー情報を取得するように修正します。

`src/main/java/dev/mikoto2000/security/configuration/UserDetailsServiceImpl.java`:

```java
package dev.mikoto2000.security.configuration;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import dev.mikoto2000.security.repository.UsersMapper;
import lombok.RequiredArgsConstructor;

/**
 * UserDetailsServiceImpl
 */
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UsersMapper usersMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // ユーザーの存在チェック
    var userOpt = usersMapper.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new UsernameNotFoundException("User not found.");
    }
    var user = userOpt.get();

    // 見つけたユーザーの情報を返却
    return User.withUsername(user.getUsername())
      .password(user.getPassword())
      .roles(user.getRole())
      .disabled(!user.getEnabled())
      .build();
  }
}
```

`loadUserByUsername` メソッド内で、 DI した `UsersMapper` を利用し、ユーザー情報を取得し、
`User.withUsername` で Spring Security に返却するユーザー情報を組み立てます。


## ユーザー登録

それでは、 DB にユーザー情報を登録してみましょう。

HashMap や DB のデータ定義を見た方は気付いたはずですが、Spring Security ではパスワードをハッシュ化して保持しています。

ここではユーザー情報を作成し、パスワードをハッシュ化したうえで USERS テーブルに入れるようにコードを修正していきます。

### Spring Security 設定の変更・追加

SecurityConfig に、以下の修正を加えます。

- `/signup` ページに誰でもアクセスできるようにする
- ユーザー作成時に使用する `PasswordEncoder` を Bean 定義する

`src/main/java/dev/mikoto2000/security/configuration/SecurityConfig.java`:

```java
package dev.mikoto2000.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig
 */
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // ログインフォームを自作し、ログイン関連 URL は誰でもアクセスできるよう指定
    // ログイン失敗時には "/login?error" へリダイレクトする
    http.formLogin(login -> {
      login
        .loginPage("/login")
        .permitAll()
        .failureUrl("/login?error");
    })
    // ログアウトは、画面の自作はせず
    // POST する URL とログアウト成功後のリダイレクト URL を指定する
    .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/"))
    .authorizeHttpRequests(auth -> {
      auth
        /* 修正ここから */
        // "/" は誰でも表示できる
        .requestMatchers("/").permitAll()
        // "/signup", "/admin/**" は ADMIN しか表示できない
        .requestMatchers("/signup").hasRole("ADMIN")
        .requestMatchers("/admin/**").hasRole("ADMIN")
        /* 修正ここまで */
        // その他ページは、ログイン済みでないと表示できない
        .anyRequest().authenticated();
    });
    return http.build();
  }

  /* 修正ここから(PasswordEncoder Bean 定義を追加) */
  /**
   * Spring Security で使用する PasswordEncoder を定義。
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
  /* 修正ここまで */
}
```

`PasswordEncoder` を Bean 定義することで、 Spring Security がその `PasswordEncoder` を使用します。
さらに、アプリケーションで DI することで、 Spring Security が使用する `PasswordEncoder` と同じものをアプリケーションが使えるようになります。

また、ユーザー登録は管理者のみが行うこととして、 `/signup` は `ADMIN` のみがアクセスできるようにしました。
初期管理者ユーザーは `data.sql` であらかじめ作成しています。


### UsersMapper にインサート用メソッドを追加

`insert` メソッドを追加し、テーブルにレコードを追加できるようにします。

`src/main/java/dev/mikoto2000/security/repository/UsersMapper.java`:

```java
package dev.mikoto2000.security.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import dev.mikoto2000.security.entity.User;

/**
 * UsersMapper
 */
@Mapper
public interface UsersMapper {
  @Select("""
          SELECT
            username,
            password,
            enabled,
            role
          FROM
            USERS
          WHERE
            USERS.username = #{username}
          """)
  Optional<User> findByUsername(String username);

  /* 追加ここから */
  @Insert("""
          INSERT INTO USERS
          (
            username,
            password,
            enabled,
            role
          )
            VALUES
          (
            #{username},
            #{password},
            #{enabled},
            #{role}
          )
          """)
  int insert(User user);
  /* 追加ここまで */
}
```

ここは一般的な MyBatis の insert ですね。


### コントローラーの追加

`/signup` 用のコントローラーを定義します。

`src/main/java/dev/mikoto2000/security/controller/SignupController.java`:

```java
package dev.mikoto2000.security.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.mikoto2000.security.entity.User;
import dev.mikoto2000.security.repository.UsersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SignupController
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SignupController {

  private final UsersMapper usersMapper;
  private final PasswordEncoder passwordEncoder;

  @GetMapping("/signup")
  public String signupPage() {
    return "signup";
  }

  @PostMapping("/signup")
  public String signup(
      @RequestParam String username,
      @RequestParam String password
      ) {

    // パスワードのハッシュ化
    var hashedPassword = passwordEncoder.encode(password);

    try {
      // ユーザーをテーブルへインサート
      User user = new User(username, hashedPassword, true, "USER");
      usersMapper.insert(user);
    } catch (RuntimeException e) {
      log.error("ユーザー登録で例外が発生しました", e);
      return "redirect:/signup?error";
    }

    // ログイン画面へリダイレクト
    return "redirect:/login";
  }

}
```

GET リクエストでサインアップページを表示し、そこから POST リクエストを受け取ることでユーザー登録します。

ユーザー登録では、 DI した `PasswordEncoder` を利用しパスワードをハッシュ化することで、
Spring Security が読み込めるハッシュ形式のパスワードを生成します。

### View の追加

裏側の仕組みが整ったので、 View の作成に入っていきます。

#### インデックス画面

インデックス画面に、 `ADMIN` ロールを持つユーザーにのみ見えるサインアップ画面へのリンクを追加します。

`src/main/resources/templates/index.html`:

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
  <!-- 変更ここから -->
  <!-- ADMIN ロールを持つ人にのみ /signup へのリンクを表示-->
  <a sec:authorize="hasRole('ADMIN')" href="/signup">ユーザー登録ページ</a>
  <!-- 変更ここまで -->
  <form method="post" th:action="@{/logout}">
    <button type="submit">ログアウト</button>
  </form>
</body>
</html>
```

Thymeleaf で認可情報を扱うために、 `thymeleaf-extras-springsecurity6` を利用し、
`sec:authorize` で「ロールごとに表示非表示を切り替える」を実現しています。


#### ログイン画面

ログイン画面にも、 `ADMIN` ロールを持つユーザーにのみ見えるサインアップ画面へのリンクを追加します。

`src/main/resources/templates/login.html`:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>LOGIN</title>
    <meta charset="UTF-8">
  </head>
  <body>
    <h1>ログインページ</h1>
    <div th:if="${param.error}">
      ユーザー名かパスワードが違います。
    </div>
    <form th:action="@{/login}" method="post">
      <div>
        <input type="text" name="username" placeholder="Username"/>
      </div>
      <div>
        <input type="password" name="password" placeholder="Password"/>
      </div>
      <input type="submit" value="ログイン" />
    </form>
    <!-- 追加ここから -->
    <!-- ADMIN ロールを持つ人にのみ /signup へのリンクを表示-->
    <a sec:authorize="hasRole('ADMIN')" href="/signup">ユーザー登録ページ</a>
    <a href="/">インデックスページ</a>
    <!-- 追加ここまで -->
  </body>
</html>
```

#### サインアップ画面

ログインページ同様、ユーザー名とパスワードを入力する画面を追加します。

```html
<!DOCTYPE html>
<html>
  <head>
    <title>SIGNUP</title>
    <meta charset="UTF-8">
  </head>
  <body>
    <h1>サインアップページ</h1>
    <div th:if="${param.error}">
      エラーが発生しました。
    </div>
    <form th:action="@{/signup}" method="post">
      <div>
        <input type="text" name="username" placeholder="Username"/>
      </div>
      <div>
        <input type="password" name="password" placeholder="Password"/>
      </div>
      <input type="submit" value="ユーザー作成" />
    </form>
    <a href="/login">ログインページ</a>
    <a href="/">インデックスページ</a>
  </body>
</html>
```

### 動作確認

ユーザー登録し、登録したユーザーでログインができることを確認しましょう。


## 座学で学ぶ Spring Security

### ログインページのカスタマイズの仕組み

#### ログインページを自作する理由

Spring Security のデフォルトログインページは、動作確認には十分ですが、実際のアプリケーションでは自社のデザインや要件に合わせた画面が必要になります。ハンズオンでは `loginPage("/login")` を指定し、自作のログインページへ遷移するようにしました。

#### loginPage と permitAll の関係

`loginPage("/login")` は「ログインページの URL は `/login` にする」という指定です。同時に `permitAll()` を付けることで、ログインしていないユーザーでもログインページ自体にはアクセスできるようにしています。もし `permitAll()` を付け忘れると、ログインページにアクセスする時点で認証が必要になり、ログイン画面が表示されないという矛盾が起きます。

#### ログイン失敗時のリダイレクト

`failureUrl("/login?error")` は、認証に失敗したときに `/login?error` へリダイレクトする指定です。ハンズオンのログインページでは `th:if="${param.error}"` で `error` パラメータの有無を判定し、「ユーザー名かパスワードが違います」と表示しています。このように、失敗時の遷移先を指定することで、エラー表示を自作の画面で制御できます。

#### ログイン処理はデフォルトのまま

ログインページを自作しても、ログイン処理そのものは Spring Security が担当します。フォームから送信された `username` と `password` を受け取り、`UserDetailsService` で取得したユーザー情報と照合するのは、あくまで Spring Security の処理です。ハンズオンでは「画面を自作し、Spring Security にユーザー名とパスワードを渡すところまでをカスタマイズした」というのが正確な理解です。

### ログアウトの仕組みと CSRF

#### ログアウトは状態を変更する操作

ログアウトはセッションの認証情報を破棄する、状態を変更する操作です。状態を変更する操作は `GET` ではなく `POST` で実行するのが安全です。ハンズオンでは `logoutUrl("/logout")` を指定し、`/logout` への `POST` でログアウトするようにしました。

#### なぜ Thymeleaf の th:action を使うのか

ログアウトを `POST` で実行するとき、CSRF（クロスサイト・リクエスト・フォージェリ）対策が関わります。CSRF は、ユーザーが意図しないリクエストを送信させられる攻撃です。Spring Security はデフォルトで CSRF 対策を有効にしており、`POST` などの状態変更リクエストにはトークンが必要になります。

ハンズオンでは `th:action="@{/logout}"` を使いました。Thymeleaf の `th:action` は、フォームに自動で CSRF トークンを挿入します。そのため、手動でトークンを扱わずに安全なログアウトフォームを作れます。

#### ログアウト成功後のリダイレクト

`logoutSuccessUrl("/")` は、ログアウト成功後に `/` へリダイレクトする指定です。ログアウト後は認証されていない状態に戻るため、トップページなど誰でも見られるページへ遷移させるのが一般的です。

### DB 認証の仕組み（UserDetailsService と DB）

#### なぜ DB から取得するのか

第1回では、ユーザー情報を HashMap で保持していました。これは仕組みの理解を目的とした最小構成ですが、実際のアプリケーションではユーザー情報は DB に保存されていることが一般的です。`UserDetailsService` の `loadUserByUsername` の中で DB を検索すれば、取得元を DB に切り替えられます。

#### 認証の流れと DB 検索の位置

```text
ユーザーがユーザー名・パスワードを送信
↓
UserDetailsService がユーザー名からユーザー情報を取得（ここで DB 検索）
↓
取得したパスワードと入力されたパスワードを照合
↓
一致すれば認証成功、認可の判断へ進む
```

ハンズオンでは、`UsersMapper.findByUsername(username)` で DB からユーザー情報を取得し、`User.withUsername` で Spring Security に返すユーザー情報を組み立てました。第1回の HashMap 実装と比べると、取得元が DB に変わっただけで、`UserDetailsService` の役割は同じです。

#### ユーザーが見つからない場合

`loadUserByUsername` は、ユーザーが見つからない場合に `UsernameNotFoundException` を投げます。ハンズオンでは `userOpt.isEmpty()` のときに例外を投げ、見つかった場合のみユーザー情報を返すようにしました。この例外は Spring Security の認証処理で「認証失敗」として扱われます。

### パスワードハッシュと PasswordEncoder

#### パスワードをそのまま保存しない理由

パスワードを平文のまま保存すると、データが漏れたときにパスワードがそのまま相手に渡ってしまいます。そのため、保存時にはハッシュ化して、元のパスワードをそのまま持たないようにします。ハンズオンの `data.sql` では、パスワードが `{bcrypt}` プレフィックス付きのハッシュ文字列として保存されています。

#### PasswordEncoder を Bean 定義する理由

ハンズオンでは `PasswordEncoder` を Bean として定義しました。これを Bean 定義すると、Spring Security がその `PasswordEncoder` を認証時のパスワード照合に使用します。さらに、アプリケーション側でも DI することで、Spring Security と同じ `PasswordEncoder` を使えます。

#### ユーザー登録時のハッシュ化

ユーザー登録では、`passwordEncoder.encode(password)` で入力されたパスワードをハッシュ化してから DB に保存しました。このとき、`PasswordEncoderFactories.createDelegatingPasswordEncoder()` が生成する `DelegatingPasswordEncoder` を使うと、`{bcrypt}` プレフィックス付きの形式でハッシュ化されます。これにより、保存したハッシュを Spring Security がそのまま認証に使えます。

### ロールと認可（RBAC の触り）

#### ロールとは何か

ロール（役割）は、ユーザーに付与する権限のグループです。ハンズオンでは `ADMIN` と `USER` というロールをユーザーに持たせました。認可のルールは「どのロールならどのページを見られるか」という形で指定します。

#### hasRole による URL レベルの制御

ハンズオンでは、次のようにロールごとにアクセスを制御しました。

```java
.requestMatchers("/signup").hasRole("ADMIN")
.requestMatchers("/admin/**").hasRole("ADMIN")
```

`hasRole("ADMIN")` は「`ADMIN` ロールを持つユーザーだけアクセスを許可する」という指定です。これにより、ユーザー登録ページは管理者だけが見られるようになります。

#### View レベルでの制御（sec:authorize）

URL レベルだけでなく、View レベルでもロールによる表示制御ができます。ハンズオンでは `thymeleaf-extras-springsecurity6` を利用し、`sec:authorize="hasRole('ADMIN')"` で「`ADMIN` ロールのユーザーにのみリンクを表示する」を実現しました。URL レベルは「アクセスを拒否する」制御で、View レベルは「表示しない」制御です。両方を組み合わせることで、より細かい画面制御ができます。

#### RBAC の触りと本格的なロール設計

ハンズオンでは、ロール情報をログインユーザーに持たせ、URL / View レベルで制御できることを確認しました。これが RBAC（ロールベースのアクセス制御）の触りです。本格的な RBAC では、ロールと権限（Authority）の設計、メソッドレベルの認可など、より細かい制御を扱います。これらは次回以降の話題です。

## まとめ

Vol.2 では、次のことをやりました。

- ログイン・ログアウトのカスタマイズ
- DB からユーザー情報を取得するように修正
- ユーザー登録
- ロールを用いた URL レベルのアクセス制御（RBAC の触り）

これにより、「実務でよく見る Spring Security 構成」のベースとなる形が完成しました。

これ以降は次のような話題になりますが、
本ハンズオンで学んだ構成を土台にすれば、公式ドキュメントを読み進められる状態になっています。

- メソッドレベルの認可
- 権限（Authority）設計


## 参考資料

- CSRF
    - [安全なウェブサイトの作り方 - 1.6 CSRF（クロスサイト・リクエスト・フォージェリ） | 情報セキュリティ | IPA 独立行政法人 情報処理推進機構](https://www.ipa.go.jp/security/vuln/websecurity/csrf.html)
