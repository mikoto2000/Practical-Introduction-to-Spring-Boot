---
title: Spring Session 入門 — Redis で HTTP セッションを共有する
---

# Spring Session 入門 — Redis で HTTP セッションを共有する

## 講義の区切り

基本編の目安は 60〜90 分です。終了条件は、アプリ A でセッションに保存した表示名を、同じ Cookie を使ってアプリ B から取得できることです。
アプリ B でセッションを無効化したあと、元の Cookie をアプリ A に送っても表示名を取得できないことも確認します。

## 概要

HTTP セッションを使うと、複数のリクエストにまたがって状態を保持できます。この演習では、セッションに表示名を 1 つだけ保存します。
同じ Spring Boot アプリを異なるポートで 2 つ起動し、Spring Session を使って両方を同じ Redis につなぎます。
応答にアプリの識別名を含めるので、どちらがリクエストを処理したかを確認できます。

## 事前準備

- JDK 21
- Controller を使った API の作成経験と、HTTP Cookie の基礎
- Linux コンテナーが動く Docker 環境
- 空きポート 8080（アプリ A）、8081（アプリ B）、56379（Redis）

[Spring Initializr](https://start.spring.io/) で Maven / Java / Jar / YAML を選び、新しいプロジェクトを作成します。
Spring Boot は 4.0 系、Java は 21 にします。Group は `dev.mikoto2000.springboot`、Artifact は `session`、Package name は `dev.mikoto2000.springboot.session` です。
依存関係に Spring Web を選び、生成された `pom.xml` の `dependencies` に次を追加します。

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-session-data-redis</artifactId>
</dependency>
```

## 触って学ぶ Spring Session

### 演習用の Redis を起動する

Redis のポートはローカルからだけ接続できるように公開します。次のコマンドは Bash と PowerShell のどちらでも実行できます。

```bash
docker run --rm --name spring-session-redis-lab -d -p 127.0.0.1:56379:6379 redis:7.4-alpine
docker exec spring-session-redis-lab redis-cli ping
```

`PONG` が返ることを確認します。`src/main/resources/application.yaml` を次の内容にします。

```yaml title="application.yaml"
spring:
  application:
    name: session
  data:
    redis:
      host: 127.0.0.1
      port: 56379
  session:
    timeout: 5m
    data:
      redis:
        namespace: session-lab

server:
  address: 127.0.0.1
  port: 8080

lab:
  instance: A
```

`spring.session.timeout` はセッションが操作されないまま有効でいられる時間です。
`spring.session.data.redis.namespace` を指定しておくと、Redis 上で演習のセッションキーを探しやすくなります。
Spring Boot がスターターを見て Spring Session を自動設定します。

### セッションを読み書きする API を作る

`src/main/java/dev/mikoto2000/springboot/session/SessionController.java` を作成します。

| API | 動作 |
|---|---|
| `POST /session?name=Alice` | セッションを取得または作成し、表示名を保存 |
| `GET /session` | 応答したアプリと保存済みの表示名を返す |
| `DELETE /session` | セッションがあれば無効化する |

```java title="SessionController.java"
package dev.mikoto2000.springboot.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {

  private static final String DISPLAY_NAME = "displayName";

  private final String instance;

  public SessionController(@Value("${lab.instance}") String instance) {
    this.instance = instance;
  }

  @PostMapping("/session")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@RequestParam("name") String name, HttpServletRequest request) {
    request.getSession().setAttribute(DISPLAY_NAME, name);
  }

  @GetMapping("/session")
  public SessionView read(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    String name = session == null ? null : (String) session.getAttribute(DISPLAY_NAME);
    return new SessionView(instance, name);
  }

  @DeleteMapping("/session")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void invalidate(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }

  public record SessionView(String instance, String name) {}
}
```

保存では `getSession()` を使い、セッションがなければ作成します。
参照と無効化では `getSession(false)` を使うので、セッションがないリクエストで新規作成は起きません。

### 同じアプリを 2 つ起動する

プロジェクトのルート（`pom.xml` と同じ場所）で jar を作ります。PowerShell では `.\mvnw.cmd package` を使います。

```bash
./mvnw package
```

1 つ目のターミナルでアプリ A を起動します。

```bash
java -jar target/session-0.0.1-SNAPSHOT.jar
```

2 つ目のターミナルも同じディレクトリで開き、アプリ B を起動します。
起動引数でポートと識別名を上書きします。Redis の接続先と名前空間は両方で共通です。

```bash
java -jar target/session-0.0.1-SNAPSHOT.jar --server.port=8081 --lab.instance=B
```

### Cookie を使ってアプリ B から参照する

3 つ目のターミナルから、アプリ A に表示名を保存します。`-c` はサーバーが返した Cookie をファイルに保存するオプションです。
PowerShell では、以下の `curl` を `curl.exe` に読み替えてください。

```bash
curl -i -c session.cookies -X POST 'http://127.0.0.1:8080/session?name=Alice'
```

ステータスは 204 です。ヘッダーの `Set-Cookie` に `SESSION` があることを確認します。
同じ Cookie を使い、アプリ A とアプリ B に順に GET を送ります。`-b` はファイルから読み取った Cookie を送るオプションです。

```bash
curl -i -b session.cookies http://127.0.0.1:8080/session
curl -i -b session.cookies http://127.0.0.1:8081/session
curl -i http://127.0.0.1:8081/session
```

| 呼び出し先 | Cookie | 200 の応答本文 |
|---|---|---|
| アプリ A | あり | `{"instance":"A","name":"Alice"}` |
| アプリ B | あり | `{"instance":"B","name":"Alice"}` |
| アプリ B | なし | `{"instance":"B","name":null}` |

両方とも同じホスト名 `127.0.0.1` を使います。Cookie の送信先はポートで区別されないので、同じ Cookie を 8080 と 8081 に送れます。

Redis でも、演習の名前空間にセッションが保存されていることを確認します。

```bash
docker exec spring-session-redis-lab redis-cli --scan --pattern 'session-lab:sessions:*'
```

`session-lab:sessions:` から始まるキーが見つかります。キーと Cookie にはセッションを識別する値が含まれるため、共有用のログに貼り付けないでください。

### アプリ B で無効化する

Cookie を付けてアプリ B に DELETE を送り、その後でアプリ A に同じ Cookie を送ります。
ここでは DELETE の応答で Cookie ファイルを上書きせず、無効化前の Cookie を再送して確認します。

```bash
curl -i -b session.cookies -X DELETE http://127.0.0.1:8081/session
curl -i -b session.cookies http://127.0.0.1:8080/session
```

DELETE は 204 を返します。アプリ A の GET は 200 を返し、本文は `{"instance":"A","name":null}` になります。
先ほどの `redis-cli --scan` をもう一度実行すると、初回からこの手順だけを実施した環境ではセッションのキーが見つからなくなります。

### 後片付け

アプリ A と B を Ctrl+C で停止します。Redis のコンテナーを停止し、演習で保存した Cookie ファイルを削除します。

```bash
docker stop spring-session-redis-lab
rm session.cookies
```

PowerShell では `rm session.cookies` の代わりに `Remove-Item -LiteralPath session.cookies` を使えます。

## 座学で学ぶ HTTP セッション

### Cookie と Redis が持つ情報

Cookie が運ぶのはセッションを特定するための値です。Redis は表示名をセッション属性として保存します。
Spring Session は HTTP リクエストをフィルターで処理します。Controller は `HttpSession` を使い、どのインスタンスからでも共有の保存先にアクセスできます。

### 有効期限と無効化

この演習では、最後のアクセスから設定した時間が経つとセッションは期限切れになります。`invalidate()` を呼ぶと、その場でセッションが無効になります。
セッションの内容は一時的な状態です。ログイン情報と組み合わせる場合は、セッション ID を認証に関わる情報として扱います。

発展として、アプリ A の再起動を試します。無効化とは別に、表示名をもう一度保存してから A だけを再起動し、期限内に同じ Cookie で再取得します。
Redis を動かしたままなら、再起動前の表示名が返ることを確認できます。

### 理解を確かめる

1. アプリ B が応答したと分かるフィールドと、その値を挙げる。
2. Cookie なしの GET で表示名が返らない理由を説明する。
3. アプリ B で無効化したあと、アプリ A でも表示名が得られなくなる理由を説明する。

## 参考資料

- [Spring Boot 4.0 リファレンス — Spring Session](https://docs.spring.io/spring-boot/4.0/reference/web/spring-session.html)
- [Spring Boot 4.0 設定プロパティ — Redis セッションの名前空間](https://docs.spring.io/spring-boot/4.0/appendix/application-properties/index.html#application-properties.web.spring.session.data.redis.namespace)
- [Spring Session 4.0 リファレンス — Redis Configurations](https://docs.spring.io/spring-session/reference/4.0/configuration/redis.html)
