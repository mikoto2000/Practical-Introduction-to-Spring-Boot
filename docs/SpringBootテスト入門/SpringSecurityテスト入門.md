---
title: Spring Security テスト入門
---

# Spring Security テスト入門

## 講義の区切り

基本編の目安は 90 分です。終了条件は「許可・拒否・CSRFの違いを確認」です。
発展内容・次回の目安: ログイン・ログアウトの追加テスト。基本の確認後に取り組んでください。
準備と前後の章は [学習ガイド](../introduction.md) で確認できます。

## 概要

Spring Security の設定を、アクセスを許可するケースと拒否するケースの両方から検証します。
MockMvc で認証・認可・CSRF を確認し、ログインとログアウトの処理もテストします。
「画面を開けた」だけでは見つけにくい、権限設定の漏れを確認できるようにしましょう。

## 対象読者

- Spring Security の認証・認可を学んだ方
- MockMvc で Controller のテストを書ける方
- 権限設定を変更したときの影響をテストで確認したい方

## 事前準備と到達目標

[テスト入門](./テスト入門.md) と [Spring Security 第1回](../SpringSecurity入門/Vol1.md) の基本を前提にします。
ロールによる制御は [Spring Security 第2回](../SpringSecurity入門/Vol2.md) も参照してください。
この教材は Java 21、Maven Wrapper、Spring Boot 4.0 系の独立したプロジェクトで進めます。
既存の Security ハンズオンの配布プロジェクトは変更しません。

[Spring Initializr](https://start.spring.io/) で Maven / Java / Jar / YAML を選択します。
パッケージ名は `dev.mikoto2000.springboot.securitytest` とし、Spring Web と Spring Security を追加します。
DB、Thymeleaf、Lombok は使いません。レスポンスを文字列にして、アクセス制御を検証します。

完了時には、テスト用の認証済みユーザーを使うテストと、実際の認証処理を通すテストの違いを説明できることを目指します。
拒否される理由を分け、ログアウト後に保護された URL を開けないことまで確認します。

## この資料の構成

この資料は 2 部構成です。

1. 触って学ぶ Spring Security テスト: 小さなアプリにアクセス制御を設定し、許可・拒否・ログイン・ログアウトをテストするハンズオン。
2. 座学で学ぶ Spring Security テスト: 認証の用意方法、フィルター、CSRF、テスト範囲を整理する座学。

| 触って学ぶ（ハンズオン） | 座学で学ぶ（理論） |
|---|---|
| 匿名ユーザーのアクセス | 認証されていない利用者の扱い |
| USER / ADMIN のアクセス | 認証と認可を分ける |
| CSRF 付き・なしの POST | CSRF と権限不足の区別 |
| フォームログイン | テスト用ユーザーと実際の認証処理 |
| セッションを使ったログアウト | 複数リクエストと認証状態 |

## 触って学ぶ Spring Security テスト

### 依存関係の準備

`pom.xml` の `dependencies` に次の依存関係があることを確認します。
バージョンは Spring Boot の依存関係管理に任せます。

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security-test</artifactId>
  <scope>test</scope>
</dependency>
```

Spring Boot 4.0 系では、Security のテスト用自動設定も含む `spring-boot-starter-security-test` を使います。
`spring-security-test` のヘルパーは、この Starter 経由で追加されます。
[Starter の公式定義](https://github.com/spring-projects/spring-boot/blob/v4.0.7/starter/spring-boot-starter-security-test/build.gradle) も参照できます。

### 検証するアクセスルール

| URL | メソッド | 必要な認証・権限 |
|---|---|---|
| `/public` | GET | 匿名でも許可 |
| `/profile` | GET | 認証済み |
| `/admin` | GET | ADMIN ロール |
| `/admin` | POST | ADMIN ロールと有効な CSRF トークン |
| `/login` | POST | フォームログイン |
| `/logout` | POST | ログアウトと有効な CSRF トークン |

フォームログインを使うため、未ログインで保護された URL にアクセスするとログインページへリダイレクトします。
認証済みで権限が足りない場合は 403 です。

### アプリの作成

以下のファイルを `src/main/java/dev/mikoto2000/springboot/securitytest` に配置します。
Initializr が生成した `@SpringBootApplication` のクラスも同じパッケージに置きます。

```java title="PageController.java"
package dev.mikoto2000.springboot.securitytest;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PageController {
  @GetMapping("/public")
  public String publicPage() { return "public"; }

  @GetMapping("/profile")
  public String profile(Principal principal) { return principal.getName(); }

  @GetMapping("/admin")
  public String admin() { return "admin"; }

  @PostMapping("/admin")
  public String updateAdmin() { return "updated"; }
}
```

```java title="SecurityConfig.java"
package dev.mikoto2000.springboot.securitytest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public").permitAll()
            .requestMatchers("/admin").hasRole("ADMIN")
            .anyRequest().authenticated())
        .formLogin(Customizer.withDefaults())
        .logout(logout -> logout.logoutSuccessUrl("/public"))
        .build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  UserDetailsService users(PasswordEncoder encoder) {
    return new InMemoryUserDetailsManager(
        User.withUsername("alice").password(encoder.encode("password"))
            .roles("USER").build(),
        User.withUsername("admin").password(encoder.encode("password"))
            .roles("ADMIN").build());
  }
}
```

固定のユーザーとパスワードはローカル演習用です。
CSRF は無効化せず、既定の保護を使います。

### 認証と認可のテスト

次のファイルを `src/test/java/dev/mikoto2000/springboot/securitytest` に配置します。

```java title="AccessControlTest.java"
package dev.mikoto2000.springboot.securitytest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PageController.class)
@Import(SecurityConfig.class)
class AccessControlTest {
  @Autowired
  MockMvc mvc;

  @Test
  void anonymousCanReadPublicPage() throws Exception {
    mvc.perform(get("/public"))
        .andExpect(status().isOk())
        .andExpect(content().string("public"));
  }

  @Test
  void anonymousIsRedirectedToLogin() throws Exception {
    mvc.perform(get("/profile"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  @WithMockUser(username = "alice", roles = "USER")
  void authenticatedUserCanReadProfile() throws Exception {
    mvc.perform(get("/profile"))
        .andExpect(status().isOk())
        .andExpect(content().string("alice"));
  }

  @Test
  void userCannotReadAdminPage() throws Exception {
    mvc.perform(get("/admin").with(user("alice").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanReadAdminPage() throws Exception {
    mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(content().string("admin"));
  }

  @Test
  void adminCannotPostWithoutCsrf() throws Exception {
    mvc.perform(post("/admin").with(user("admin").roles("ADMIN")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCannotPostWithInvalidCsrf() throws Exception {
    mvc.perform(post("/admin").with(user("admin").roles("ADMIN"))
        .with(csrf().useInvalidToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void userCannotPostEvenWithCsrf() throws Exception {
    mvc.perform(post("/admin").with(user("alice").roles("USER")).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanPostWithCsrf() throws Exception {
    mvc.perform(post("/admin").with(user("admin").roles("ADMIN")).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string("updated"));
  }
}
```

`@Import(SecurityConfig.class)` で、検証するアクセスルールを明示的に読み込みます。
`@WebMvcTest` が用意する MockMvc では Security のフィルターも適用されます。
`addFilters = false` は指定しません。

```bash
./mvnw -Dtest=AccessControlTest test
```

PowerShell では `.\mvnw.cmd '-Dtest=AccessControlTest' test` を使います。
9 件のテストが成功することを確認してください。
権限不足の POST には有効な CSRF トークンを付けており、拒否理由を分けて検証しています。

### 実際のログインとログアウトのテスト

テスト用のユーザーを設定するだけでは、パスワード照合は検証できません。
次はフォームログインを通し、取得したセッションを後続のリクエストで使います。

```java title="LoginLogoutTest.java"
package dev.mikoto2000.springboot.securitytest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginLogoutTest {
  @Autowired
  MockMvc mvc;

  @Test
  void validPasswordAuthenticatesUser() throws Exception {
    mvc.perform(formLogin().user("alice").password("password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(authenticated().withUsername("alice"));
  }

  @Test
  void invalidPasswordDoesNotAuthenticateUser() throws Exception {
    mvc.perform(formLogin().user("alice").password("wrong"))
        .andExpect(redirectedUrl("/login?error"))
        .andExpect(unauthenticated());
  }

  @Test
  void logoutClearsAuthentication() throws Exception {
    var login = mvc.perform(formLogin().user("alice").password("password"))
        .andExpect(authenticated()).andReturn();
    MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
    assertNotNull(session);

    mvc.perform(get("/profile").session(session))
        .andExpect(status().isOk())
        .andExpect(content().string("alice"));

    mvc.perform(post("/logout").session(session).with(csrf()))
        .andExpect(redirectedUrl("/public"))
        .andExpect(unauthenticated());
    assertTrue(session.isInvalid());

    // 無効化されたセッションを再利用せず、新しいリクエストを送る。
    mvc.perform(get("/profile"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }
}
```

`formLogin()` は、フォームのパラメーターと有効な CSRF トークンを用意します。
ログアウトは通常の POST と `csrf()` を組み合わせており、設定した URL を実際に通します。

```bash
./mvnw test
```

2 クラスの合計 12 件が成功することを確認します。
この段階では DB 認証や HTML フォームの描画は検証していません。

### 練習

1. 存在しないユーザーでログインし、認証されないことを検証する。
2. ログイン済みのセッションで CSRF なしのログアウトを送り、403 になることを確認する。
3. `/admin` のルールを一時的に `permitAll()` に変更し、拒否するテストが失敗することを確認する。

設定を戻して、全テストが成功することを確認してください。

## 座学で学ぶ Spring Security テスト

### 認証と認可を分ける

認証は利用者の確認、認可は許可する操作の判断です。
ログインできることだけを確認しても、管理者ページを一般ユーザーから守れているかは分かりません。
各 URL について、匿名・一般ユーザー・管理者の期待結果を表にすると、必要なケースを整理できます。

### テスト用ユーザーと実際の認証処理

| 方法 | 用意するもの | 主に検証する対象 |
|---|---|---|
| `@WithMockUser` | テスト全体の認証済みユーザー | 認可と利用者情報の利用 |
| `user(...)` | そのリクエストの認証済みユーザー | 利用者や権限ごとのアクセス |
| `formLogin()` | ログイン用のリクエスト | ユーザー検索とパスワード照合 |

前の 2 つは、実際にユーザーを検索してパスワードを照合する処理を省きます。
ユーザー名とロールを自由に設定できるため、認可のケースを用意しやすくなります。
`roles("ADMIN")` には `ROLE_` を付けません。生成される権限には `ROLE_ADMIN` が使われます。

DB 認証の検証では、実際の `UserDetailsService` と DB を読み込み、ハッシュ化したパスワードを持つテストユーザーを登録します。
その状態で `formLogin()` を使い、成功と失敗を確認します。
独自の principal を使う場合は、`user(UserDetails)` や `@WithUserDetails` の利用も検討してください。

### フィルターを含めて検証する

認証・CSRF・URL の認可は、Controller より前のフィルターで処理されます。
Controller を直接呼ぶテストでは、この処理を通りません。
フィルターを無効化した MockMvc も、Security のアクセス制御の検証には使えません。

MockMvc を手動で組み立てる場合は、`webAppContextSetup(context).apply(springSecurity())` で連携します。
今回のように Boot が用意する MockMvc を使う場合と、手動構築を混ぜないようにしましょう。
複数のフィルターチェーンがあるアプリでは、対象 URL がどのチェーンに入るかも確認します。

### CSRF と権限不足の区別

権限不足と CSRF の失敗は、どちらも 403 になる場合があります。
USER が管理者操作を拒否されるテストでは、有効なトークンを付けます。
CSRF のテストでは、許可される ADMIN を用意してトークンだけを変えます。
他の条件を揃えることで、どの設定が拒否したかを判断できます。

Cookie によるセッション認証では、ブラウザーが自動で認証情報を送るため、CSRF への対策が必要です。
テストを通すために CSRF を無効化せず、アプリが採用する認証方式に合わせて保護とテストを設計します。

### 認証されていない利用者の扱い

フォームログインでは、未ログインの利用者をログインページへ誘導します。
API の認証方式や `AuthenticationEntryPoint` の設定によっては、401 を返す構成もあります。
「未ログインなら必ず 401」と固定せず、アプリの契約を期待値にしてください。

`@RestControllerAdvice` は、Security のフィルターで発生する拒否を共通化する場所ではありません。
API のエラー応答を揃える場合は、認証失敗やアクセス拒否に対応する Security のハンドラーを設定します。

### 複数リクエストと認証状態

MockMvc の別々のリクエストは、ブラウザーのようにセッションを自動で共有するとは限りません。
ログイン結果のセッションを明示的に渡し、認証状態の継続を確認します。
ログアウト後には、認証が消えたこととセッションの無効化を確認します。

テスト全体に `@WithMockUser` を付けると、ログアウト後のリクエストにもテスト用認証を再び設定する場合があります。
実際のログインで得たセッションを使うテストに分けると、状態の変化が分かりやすくなります。

### テスト範囲の選び方

`@WebMvcTest` は、Controller と Security 設定を中心に検証する部分テストです。
`@SpringBootTest` は、アプリの構成を読み込んで認証処理との接続を検証します。
今回のログインテストではインメモリーのユーザー管理を使うため、DB との接続は範囲外です。

ブラウザー上の CSRF トークン埋め込み、Cookie の属性、画面遷移は、ブラウザーを使うテストで確認します。
MockMvc でアクセス制御を広く確認し、ブラウザーで代表的な利用手順を確認すると、役割を分けられます。

## まとめ

- 許可するケースと拒否するケースを両方用意する。
- 認可のテストと、実際のログイン処理のテストを分ける。
- 権限不足と CSRF の失敗を区別する。
- Security のフィルターを適用した MockMvc で検証する。
- ログアウト後の認証状態まで確認する。

## 参考資料

- [MockMvc と Spring Security の設定](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/setup.html)
- [テスト用の認証ユーザー](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/authentication.html)
- [CSRF のテスト](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/csrf.html)
- [フォームログインのテスト](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/form-login.html)
- [ログアウトのテスト](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/logout.html)
