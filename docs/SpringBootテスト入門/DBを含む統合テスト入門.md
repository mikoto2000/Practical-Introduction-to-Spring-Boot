---
title: DB を含む統合テスト入門
---

# DB を含む統合テスト入門

## 概要

Controller、Service、Mapper、DB を接続した状態で、注文 API の結果を検証します。
HTTP 応答だけでなく、保存された行や失敗後の DB 状態まで確認します。
最初は H2 で実行し、その後 Testcontainers で PostgreSQL を起動して同じテストを実行します。

## 対象読者

- Service と Controller の部分テストを書ける方
- SQL やトランザクションを実物の DB と組み合わせて確認したい方
- テスト用 DB の用意とデータの初期化を自動化したい方

## 事前準備と到達目標

Java 21、Maven Wrapper、Spring Boot 4.0 系を使います。
[テスト入門](./テスト入門.md) と [DB アクセス・トランザクション入門](../SpringBoot入門/DBアクセス・トランザクション入門.md) を前提にします。
注文 API のプロジェクトを、トランザクションを追加した最終段階まで完成させてください。
`OrderService.createWithItem` と `delete` に `@Transactional` が付いた状態から始めます。
パッケージ名は同じ `dev.mikoto2000.springboot.database` を使います。

前半の H2 テストには Docker は不要です。
後半では Linux コンテナーを実行できる Docker 環境が必要です。
次のコマンドで、クライアントとサーバーの両方が表示されることを確認します。

```bash
docker version
docker info
```

コンテナーイメージの初回取得にはネットワーク接続とディスク容量も必要です。
Windows では Docker Desktop などの対応環境を起動し、Linux コンテナーを使ってください。

完了時には、モックで省略する範囲と実物を使う範囲を選び、DB の状態まで検証できることを目指します。
テストごとにデータを独立させ、DB の接続情報とライフサイクルを管理します。

## この資料の構成

この資料は 2 部構成です。

1. 触って学ぶ DB を含む統合テスト: H2 と PostgreSQL で注文 API の登録・取得・削除・ロールバックを検証するハンズオン。
2. 座学で学ぶ DB を含む統合テスト: テスト範囲、データ管理、トランザクション、コンテナーの使い方を整理する座学。

| 触って学ぶ（ハンズオン） | 座学で学ぶ（理論） |
|---|---|
| アプリ全体と MockMvc の起動 | 部分テストと統合テスト |
| HTTP と DB の両方の検証 | 接続した状態で見つかる不具合 |
| テスト前のデータ削除 | データの独立性と並列実行 |
| 明細の失敗とロールバック | テスト側のトランザクションの影響 |
| PostgreSQL コンテナー | 実物の DB と接続情報の管理 |

## 触って学ぶ DB を含む統合テスト

### プロジェクトの準備

注文 API には MyBatis Starter 4.0.0、Validation、H2、Spring Web が入っています。
`pom.xml` の `dependencies` に、次のテスト用依存関係を追加します。
すでにある場合は重複させません。

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webmvc-test</artifactId>
  <scope>test</scope>
</dependency>
```

`src/test/resources/application-integration.yaml` を作成します。

```yaml
spring:
  sql:
    init:
      mode: always
  h2:
    console:
      enabled: false
```

このプロファイルは `@ActiveProfiles("integration")` で読み込みます。
テーブル定義は前の教材の `src/main/resources/schema.sql` をそのまま使います。
今回はスキーマを SQL 初期化で作り、テストの前には行だけを削除します。

### DB 更新失敗の HTTP 応答を定義

前の教材では、DB 制約違反の本文を定義していませんでした。
統合テストで HTTP 応答を検証するため、今回の演習では 500 と固定したコードを返します。
次のファイルを `src/main/java/dev/mikoto2000/springboot/database` に追加します。

```java title="OrderStorageExceptionHandler.java"
package dev.mikoto2000.springboot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderStorageExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(OrderStorageExceptionHandler.class);

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> storageFailure(DataIntegrityViolationException ex) {
    log.error("Order storage failed", ex);
    ProblemDetail body = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "注文を保存できませんでした");
    body.setProperty("code", "DB_WRITE_FAILED");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
```

数量に `@Positive` は付けず、DB の CHECK 制約で失敗する演習を続けます。
通常の API では入力検証を追加し、業務上の重複などは原因を区別して 409 にする設計もできます。
このハンドラーは、この演習の DB 制約違反を 500 とする契約です。
共通エラー応答の設計は [例外処理・エラー応答入門](../SpringBoot入門/例外処理・エラー応答入門.md) を参照してください。

### 共通の API テストを作成

以下のファイルを `src/test/java/dev/mikoto2000/springboot/database` に配置します。
H2 と PostgreSQL で同じケースを実行するため、テストの本文を抽象クラスにまとめます。

```java title="OrderApiContract.java"
package dev.mikoto2000.springboot.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
abstract class OrderApiContract {
  @Autowired
  MockMvc mvc;

  @Autowired
  JdbcTemplate jdbc;

  @BeforeEach
  void clearRows() {
    jdbc.update("DELETE FROM order_item");
    jdbc.update("DELETE FROM purchase_order");
  }

  int orderCount() {
    return jdbc.queryForObject("SELECT COUNT(*) FROM purchase_order", Integer.class);
  }

  int itemCount() {
    return jdbc.queryForObject("SELECT COUNT(*) FROM order_item", Integer.class);
  }

  @Test
  void createCanBeReadAndPersistsBothRows() throws Exception {
    var response = mvc.perform(post("/orders/with-item")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"customerName":"Alice","productName":"Book","quantity":2}
            """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.customerName").value("Alice"))
        .andReturn().getResponse();

    String location = response.getHeader("Location");
    assertNotNull(location);
    mvc.perform(get(location))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerName").value("Alice"));

    assertEquals(1, orderCount());
    assertEquals(1, itemCount());
    long orderId = jdbc.queryForObject(
        "SELECT id FROM purchase_order WHERE customer_name = ?", Long.class, "Alice");
    long itemOrderId = jdbc.queryForObject("SELECT order_id FROM order_item", Long.class);
    assertEquals(orderId, itemOrderId);
    assertEquals("Book", jdbc.queryForObject("SELECT product_name FROM order_item", String.class));
    assertEquals(2, jdbc.queryForObject("SELECT quantity FROM order_item", Integer.class).intValue());
  }

  @Test
  void missingOrderReturns404() throws Exception {
    mvc.perform(get("/orders/999999"))
        .andExpect(status().isNotFound());
  }

  @Test
  void invalidInputDoesNotWriteRows() throws Exception {
    mvc.perform(post("/orders/with-item").contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"customerName":"","productName":"Book","quantity":2}
            """))
        .andExpect(status().isBadRequest());
    assertEquals(0, orderCount());
    assertEquals(0, itemCount());
  }

  @Test
  void failedItemRollsBackOnlyItsOrder() throws Exception {
    mvc.perform(post("/orders/with-item").contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"customerName":"Existing","productName":"Pen","quantity":1}
            """))
        .andExpect(status().isCreated());

    mvc.perform(post("/orders/with-item").contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"customerName":"Failure","productName":"Book","quantity":0}
            """))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.code").value("DB_WRITE_FAILED"));

    assertEquals(1, orderCount());
    assertEquals(1, itemCount());
    assertEquals("Existing", jdbc.queryForObject(
        "SELECT customer_name FROM purchase_order", String.class));
  }

  @Test
  void deleteRemovesOrderAndItem() throws Exception {
    var response = mvc.perform(post("/orders/with-item")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"customerName":"Alice","productName":"Book","quantity":2}
            """))
        .andExpect(status().isCreated()).andReturn().getResponse();
    String location = response.getHeader("Location");
    assertNotNull(location);

    mvc.perform(delete(location)).andExpect(status().isNoContent());
    mvc.perform(get(location)).andExpect(status().isNotFound());
    assertEquals(0, orderCount());
    assertEquals(0, itemCount());
  }
}
```

`@MockitoBean` は使いません。Service と Mapper も実物を読み込みます。
HTTP の取得結果と、JdbcTemplate で確認した DB の値を組み合わせています。
失敗のテストでは、先に成功した注文を用意します。
失敗した注文だけが取り消され、既存の注文が残ることを確認します。

### H2 で実行

```java title="OrderApiH2Test.java"
package dev.mikoto2000.springboot.database;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:order-api-test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class OrderApiH2Test extends OrderApiContract {}
```

```bash
./mvnw -Dtest=OrderApiH2Test test
```

PowerShell では `.\mvnw.cmd '-Dtest=OrderApiH2Test' test` を使います。
5 件のテストが成功することを確認してください。
意図的な DB 制約違反のテストでは ERROR ログが出ますが、期待した応答と DB 状態ならテストは成功します。

このテストを実行するとき、アプリを別のターミナルで起動する必要はありません。
MockMvc は実際の HTTP サーバーを起動せず、アプリのリクエスト処理を呼び出します。

### Testcontainers の依存関係

次に、`pom.xml` の `dependencies` に追加します。
Spring Boot 4.0 系が管理する Testcontainers 2 系のモジュール名と import を使います。
1 系の `postgresql` モジュールや `org.testcontainers.containers.PostgreSQLContainer` と混在させないでください。

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers-postgresql</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>test</scope>
</dependency>
```

コンテナーのモジュールと JDBC ドライバーは別の依存関係です。
この教材では JUnit のコンテナー拡張を使わず、Spring Bean として起動・停止を管理します。

### PostgreSQL で同じテストを実行

次の 2 ファイルも `src/test/java/dev/mikoto2000/springboot/database` に配置します。

```java title="PostgresTestConfiguration.java"
package dev.mikoto2000.springboot.database;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfiguration {
  @Bean
  @ServiceConnection
  PostgreSQLContainer postgres() {
    return new PostgreSQLContainer("postgres:17-alpine");
  }
}
```

```java title="OrderApiPostgresTest.java"
package dev.mikoto2000.springboot.database;

import org.springframework.context.annotation.Import;

@Import(PostgresTestConfiguration.class)
class OrderApiPostgresTest extends OrderApiContract {}
```

`@ServiceConnection` がコンテナーの接続情報を DataSource に渡します。
`localhost:5432` の固定設定や、既存 DB のユーザー名・パスワードは不要です。
H2 用クラスの `@TestPropertySource` は PostgreSQL 用クラスには継承されません。
共通プロファイルで SQL 初期化を有効にしたため、PostgreSQL にも同じ `schema.sql` が適用されます。

Docker を起動して実行します。

```bash
./mvnw -Dtest=OrderApiPostgresTest test
```

PostgreSQL のコンテナーが起動し、同じ 5 件のテストが成功することを確認します。
初回はイメージの取得があるため、H2 より時間がかかります。
テスト中のログで `jdbc:postgresql:` を含む接続先を確認し、H2 に接続していないことも確認してください。

両方のテストを実行する場合は次を使います。

```bash
./mvnw test
```

Docker が使えない環境では PostgreSQL のテストは失敗します。
前半だけ進める場合は `-Dtest=OrderApiH2Test` を指定します。
Docker の不備をテスト成功として扱わず、実行できた範囲を区別してください。

### 練習

1. 注文名を更新する API のテストを追加し、GET と DB の値が両方変わることを確認する。
2. `createWithItem` の `@Transactional` を一時的に外し、失敗後の件数確認が失敗することを確認する。
3. SQL 初期化の代わりに Flyway を使い、アプリとテストで同じマイグレーションを適用する。

設定を戻して、テストが成功することを確認してください。
Flyway を使う場合は `schema.sql` の初期化を無効化し、テーブル作成の二重実行を避けます。
[DB マイグレーション入門](../SpringBoot入門/DBマイグレーション入門.md) を参照してください。

## 座学で学ぶ DB を含む統合テスト

### 部分テストと統合テスト

| 構成 | 実物を使う主な範囲 | 主に見つける問題 |
|---|---|---|
| Service と Mapper のモック | 業務ロジック | 条件分岐や呼び出す処理の誤り |
| `@WebMvcTest` と Service のモック | HTTP の入出力 | ステータス、JSON、入力検証の誤り |
| Mapper と DB | SQL と DB | 列名、型、制約の誤り |
| `@SpringBootTest` と MockMvc と DB | Controller から DB | DI、設定、データ保存、ロールバックの誤り |

今回のテストでは、実際の HTTP サーバーやブラウザーは範囲外です。
実際のポートで通信を検証する場合は、`webEnvironment = RANDOM_PORT` でサーバーを起動し、HTTP クライアントからアクセスします。
その場合のトランザクションや通信の性質は、MockMvc と異なります。

### 接続した状態で見つかる不具合

各クラスが個別に動いても、依存関係の登録漏れや SQL の型の不一致でアプリ全体は動かないことがあります。
統合テストは、それらを接続した構成で動作確認する手段です。
HTTP が 201 でも、明細が保存されていなければ業務処理は完成していません。
件数だけでなく、関連する ID と値も確認しましょう。

### テスト側のトランザクションの影響

この教材のテストには `@Transactional` を付けていません。
Service のトランザクションで確定した結果を、テストから確認します。
テスト側でトランザクションを開始すると、Service が参加して、アプリ側の境界設定の不足を隠す場合があります。

テストの最後に自動でロールバックする方法は、データを戻す用途には便利です。
ただし、実際にコミットできるか、制約違反がいつ発生するかも検証対象に応じて確認します。
別スレッドの HTTP サーバーを使うテストでは、サーバーの更新はテストスレッドのトランザクションに参加しません。
テストに `@Transactional` があっても、サーバーが保存した行を自動で戻せるとは限りません。

### データの独立性と並列実行

この教材はテストメソッドを順番に実行し、各テスト前に子テーブルから削除します。
前のテストのデータや実行順序に依存しないよう、各ケースで必要な行を作成します。
自動採番の値は初期化していないため、ID が 1 になることは前提にしていません。

共通 DB を使うテストの並列実行では、他のケースが保存した行を削除する可能性があります。
並列化する場合は、テストごとに DB やスキーマを分けるなど、データの分離を設計してください。
テストの削除先として、開発用や本番用の DB を使わないでください。

### H2 と PostgreSQL の使い分け

H2 は環境準備が少なく、前半の動作確認に向いています。
ただし、SQL の方言、型、制約、ロックの挙動は PostgreSQL と完全には一致しません。
互換モードを使っても、実物の DB での確認が不要になるわけではありません。

本番で PostgreSQL を使うなら PostgreSQL、本番で MySQL を使うなら MySQL のコンテナーで確認します。
今回の PostgreSQL は切り替え方法を学ぶ題材です。本番と同じ DB 製品・主要バージョンを選びましょう。
イメージのタグを固定し、更新時にテストします。厳密な再現性が必要ならダイジェストも固定します。

### コンテナーの接続情報とライフサイクル

Testcontainers はテスト用コンテナーを起動し、割り当てられたポートなどを Java から取得できるようにします。
`@ServiceConnection` は、その情報を Spring Boot の接続設定に渡します。
固定ポートを指定しないことで、ローカル DB との競合を避けられます。

今回は Spring がコンテナー Bean の起動と停止を管理します。
コンテナーは ApplicationContext のライフサイクルに対応し、同じコンテキストのテスト間では共有されます。
データはテストメソッドごとに初期化し、コンテナーをケースごとに作り直しているわけではありません。

JUnit の `@Container` を使う方式もありますが、コンテキストの再利用とコンテナーの停止時期に注意が必要です。
停止済みコンテナーを指す接続設定が再利用されないよう、管理方式を揃えましょう。

### 実行環境の問題を切り分ける

| 症状 | 確認する場所 |
|---|---|
| Docker 環境を検出できない | Docker の起動状態、`docker info`、実行ユーザーの接続権限 |
| イメージを取得できない | レジストリーへの接続、認証、取得制限 |
| DB が起動しない | コンテナーのログ、メモリーやディスクの空き |
| テーブルがない | プロファイル、SQL 初期化、マイグレーションの適用 |
| データ件数が想定と異なる | 初期化処理、並列実行、別ケースの影響 |

CI にも対応するコンテナー実行環境が必要です。
環境の起動失敗とアプリのテスト失敗を区別し、スキップしたテストは未検証として扱います。

## まとめ

- Controller から DB まで実物をつなぎ、HTTP と保存結果を両方確認する。
- 失敗時は、対象の更新だけが取り消されることも検証する。
- Service の境界を確認するテストは、テスト側のトランザクションで包まない。
- データの初期化とコンテナーの起動・停止を分けて管理する。
- 本番で使う DB 製品に合わせたテストも用意する。

## 参考資料

- [Spring Boot の Testcontainers 連携](https://docs.spring.io/spring-boot/4.0/reference/testing/testcontainers.html)
- [Testcontainers の PostgreSQL モジュール](https://java.testcontainers.org/modules/databases/postgres/)
- [Testcontainers の実行環境](https://java.testcontainers.org/supported_docker_environment/)
- [Spring のテスト用トランザクション](https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/tx.html)
