---
title: Spring Boot 外部 API 連携入門 — RestClient とタイムアウト
---

# Spring Boot 外部 API 連携入門 — RestClient とタイムアウト

## 講義の区切り

基本編の目安は 60〜90 分です。終了条件は、配送 API に GET を送り、取得した送料を返すことです。
さらに、配送 API が返した HTTP エラーと、応答を待ち切れなかった場合を区別して確認します。

## 概要

利用者から地域コードを受け取り、別の HTTP サーバーに送料を問い合わせる API を作ります。
Spring MVC の Controller がリクエストを受け、配送 API への問い合わせには `RestClient` を使います。

配送 API は手元で起動します。地域コードによって、正常応答・エラー・遅延を切り替えられます。

## 事前準備

- JDK 21
- Controller を使った GET エンドポイントの作成経験
- 空きポート 8080（送料照会アプリ）、8089（模擬配送 API）

[Spring Initializr](https://start.spring.io/) で新しいプロジェクトを作成します。
Maven / Java / Jar / YAML を選び、Spring Boot は 4.0 系（動作確認は 4.0.4）、Java は 21 にします。
Group は `dev.mikoto2000.springboot`、Artifact は `shipping`、Package name は `dev.mikoto2000.springboot.shipping` です。
依存関係に Spring Web を選びます。生成した `pom.xml` に `spring-boot-starter-webmvc` があることを確認し、`dependencies` に次の依存関係を追加してください。

```xml title="RestClient の依存関係"
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-restclient</artifactId>
</dependency>
```

## 触って学ぶ外部 API 連携

### 模擬配送 API を起動する

配送 API の `GET /rates/{area}` は、地域コードに応じて次の応答を返します。

| 地域コード | 配送 API の動作 |
|---|---|
| `tokyo` | すぐに 200 を返す |
| `unknown` | 対象地域が見つからず、404 を返す |
| `maintenance` | 配送 API の内部エラーとして、500 を返す |
| `slow` | 3 秒待ってから、200 を返す |

演習プロジェクトのルート（`pom.xml` と同じ場所）に `MockShippingApi.java` を作ります。
JDK に付属する HTTP サーバーを使うため、このファイルは `java` コマンドで直接実行できます。

```java title="MockShippingApi.java"
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

class MockShippingApi {

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8089), 0);
    server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    server.createContext("/rates/", MockShippingApi::handleRate);
    server.start();
    System.out.println("Mock shipping API listening on http://127.0.0.1:8089");
  }

  private static void handleRate(HttpExchange exchange) throws IOException {
    try (exchange) {
      String path = exchange.getRequestURI().getPath();
      System.out.println(exchange.getRequestMethod() + " " + path);
      if (!"GET".equals(exchange.getRequestMethod())) {
        exchange.getResponseHeaders().set("Allow", "GET");
        sendJson(exchange, 405, """
            {"error":"method not allowed"}
            """);
        return;
      }

      switch (path) {
        case "/rates/tokyo" -> sendJson(exchange, 200, """
            {"area":"tokyo","feeYen":600}
            """);
        case "/rates/maintenance" -> sendJson(exchange, 500, """
            {"error":"shipping service error"}
            """);
        case "/rates/slow" -> {
          try {
            Thread.sleep(3000);
          } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return;
          }
          sendJson(exchange, 200, """
              {"area":"slow","feeYen":800}
              """);
        }
        default -> sendJson(exchange, 404, """
            {"error":"unknown area"}
            """);
      }
    }
  }

  private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
    byte[] body = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, body.length);
    exchange.getResponseBody().write(body);
  }
}
```

1 つ目のターミナルで起動します。PowerShell でも同じコマンドを使えます。

```bash
java MockShippingApi.java
```

別のターミナルから模擬 API に直接リクエストし、200 と `{"area":"tokyo","feeYen":600}` が返ることを確認します。
以下の `curl` は、PowerShell では `curl.exe` に読み替えてください。

```bash
curl -i http://127.0.0.1:8089/rates/tokyo
```

### RestClient で送料を取得する

`src/main/resources/application.yaml` に、配送 API の接続先とタイムアウトを設定します。
比較のため、初めは模擬 API の遅延 3 秒より長い読み取りタイムアウトを設定します。

```yaml title="application.yaml"
spring:
  application:
    name: shipping
  mvc:
    problemdetails:
      enabled: true

shipping:
  base-url: http://127.0.0.1:8089
  connect-timeout: 1s
  read-timeout: 4s
```

以下の Java ファイルは `src/main/java/dev/mikoto2000/springboot/shipping` の下に配置します。
まず、配送 API の JSON を受け取る DTO を定義します。この演習では同じ DTO を送料照会 API の応答にも使います。

```java title="ShippingQuote.java"
package dev.mikoto2000.springboot.shipping;

public record ShippingQuote(String area, int feeYen) {}
```

次に、配送 API を呼び出すクラスを作ります。
Spring Boot が用意する `RestClient.Builder` を受け取り、ベース URL と HTTP 通信の設定を指定します。
演習では `SimpleClientHttpRequestFactory` を使い、接続時と読み取り時のタイムアウトを明示します。

```java title="ShippingClient.java"
package dev.mikoto2000.springboot.shipping;

import java.net.SocketTimeoutException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ShippingClient {

  private final RestClient restClient;

  public ShippingClient(
      RestClient.Builder builder,
      @Value("${shipping.base-url}") String baseUrl,
      @Value("${shipping.connect-timeout}") Duration connectTimeout,
      @Value("${shipping.read-timeout}") Duration readTimeout) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeout);
    factory.setReadTimeout(readTimeout);
    this.restClient = builder.baseUrl(baseUrl).requestFactory(factory).build();
  }

  public ShippingQuote getQuote(String area) {
    try {
      ShippingQuote quote = restClient.get()
          .uri("/rates/{area}", area)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .body(ShippingQuote.class);
      if (quote == null) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "配送 API の応答本文が空です。");
      }
      return quote;
    } catch (HttpClientErrorException.NotFound exception) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "配送対象の地域が見つかりません。", exception);
    } catch (RestClientResponseException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "配送 API がエラーを返しました。", exception);
    } catch (ResourceAccessException exception) {
      if (exception.contains(SocketTimeoutException.class)) {
        throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "配送 API の呼び出しがタイムアウトしました。", exception);
      }
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "配送 API と通信できません。", exception);
    }
  }
}
```

`uri("/rates/{area}", area)` で URL のパスを組み立てます。`retrieve().body(ShippingQuote.class)` でレスポンスを読み取り、成功時の JSON を DTO に変換します。
`retrieve()` のデフォルトの扱いでは、4xx や 5xx の応答を読み取ると、ステータスに応じた例外が発生します。

最後に、利用者からのリクエストを受ける Controller を作ります。

```java title="ShippingController.java"
package dev.mikoto2000.springboot.shipping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShippingController {

  private final ShippingClient shippingClient;

  public ShippingController(ShippingClient shippingClient) {
    this.shippingClient = shippingClient;
  }

  @GetMapping("/shipping-quotes/{area}")
  public ShippingQuote getQuote(@PathVariable("area") String area) {
    return shippingClient.getQuote(area);
  }
}
```

### HTTP 応答を確認する

模擬 API を動かしたまま、別のターミナルで送料照会アプリを起動します。PowerShell では `.\mvnw.cmd spring-boot:run` を使います。

```bash
./mvnw spring-boot:run
```

さらに別のターミナルから、今度は 8080 番の送料照会アプリにリクエストします。

```bash
curl -i http://127.0.0.1:8080/shipping-quotes/tokyo
curl -i http://127.0.0.1:8080/shipping-quotes/unknown
curl -i http://127.0.0.1:8080/shipping-quotes/maintenance
```

| 地域コード | 配送 API の応答 | 送料照会アプリの応答 |
|---|---|---|
| `tokyo` | 200 | 200 と `{"area":"tokyo","feeYen":600}` |
| `unknown` | 404 | 404 と「配送対象の地域が見つかりません。」 |
| `maintenance` | 500 | 502 と「配送 API がエラーを返しました。」 |

エラー応答では、`Content-Type` が `application/problem+json` となり、本文の `status` と `detail` で表の内容を確認できます。
模擬 API 側のターミナルには `GET /rates/tokyo` などが表示されます。受け取った地域コードを使って別の HTTP サーバーを呼び出したことが分かります。

### 読み取りタイムアウトを短くする

まず、読み取りタイムアウトが 4 秒のまま `slow` を照会します。
`curl` の出力末尾に、HTTP ステータスとリクエスト全体の所要時間が表示されます。

```bash
curl -sS -w '\nHTTP %{http_code}, %{time_total}s\n' http://127.0.0.1:8080/shipping-quotes/slow
```

模擬 API の遅延を含め、約 3 秒で 200 が返ります。
送料照会アプリを Ctrl+C で停止し、`application.yaml` の `shipping.read-timeout` を `4s` から `1s` に変更します。
送料照会アプリを再起動し、同じコマンドを実行してください。
今度は約 1 秒で 504 が返り、本文に「配送 API の呼び出しがタイムアウトしました。」と表示されます。時間は環境によって多少前後します。

続けて `tokyo` を照会し、200 が返ることも確かめます。
発展として、模擬 API を停止してからもう一度 `tokyo` を照会すると、ローカルでの接続拒否は 502 になります。

### 後片付け

送料照会アプリと模擬配送 API を、それぞれのターミナルで Ctrl+C を押して停止します。

## 座学で学ぶ HTTP クライアントとタイムアウト

### どの HTTP リクエストを計測しているか

この演習には、利用者から送料照会アプリへの HTTP 通信と、送料照会アプリから配送 API への HTTP 通信があります。
`curl` の `time_total` は前者の所要時間です。`shipping.read-timeout` は後者の読み取り待ちに適用されます。
ステータスを調べるときも、どちらの通信で観測した値かを確認します。

### 接続と読み取りで別の待ち時間を設定する

| 設定 | 待ち時間を制限する箇所 |
|---|---|
| 接続タイムアウト | 相手への接続を確立するまでの待ち時間 |
| 読み取りタイムアウト | 接続後に応答のデータを読み取るときの待ち時間 |

模擬 API の `slow` は接続を受け付けてから、応答を送る前に 3 秒待ちます。
そのため、このケースでは読み取りタイムアウトの値を変えると結果が変わります。
タイムアウトの値を決める際は、配送 API の通常の応答時間と、利用者が送料照会の結果を待てる時間を考えます。

### 呼び出し先の失敗を利用者に伝える

この演習では、配送 API の 404 を「配送対象の地域がない」という照会結果として扱います。
配送 API の 500 は、送料照会アプリが呼び出し先の異常を検知した結果として 502 にします。
待ち時間を超えた場合は、送料照会アプリが 504 を返します。このとき配送 API の HTTP ステータスは読み取れていません。
こうして、利用者は地域コードを見直す場面と、配送 API の障害や遅延を調査する場面を区別できます。

### 理解を確かめる

1. `slow` の結果を 200 から 504 に変えた設定を挙げ、どちらの HTTP 通信に適用されるか説明する。
2. `maintenance` の照会で、配送 API と送料照会アプリが返すステータスをそれぞれ答える。
3. 模擬 API を停止したときの接続拒否と、起動中の `slow` のタイムアウトを、利用者がどう区別できるか説明する。

続編の [リトライ入門](./リトライ入門.md) では、同じ模擬 API を使って、再試行の条件と回数の上限、試行間の待ち時間を確認します。

## 参考資料

- [Spring Boot 4.0 リファレンス — Calling REST Services](https://docs.spring.io/spring-boot/4.0/reference/io/rest-client.html)
- [Spring Framework 7.0 リファレンス — REST Clients](https://docs.spring.io/spring-framework/reference/7.0/integration/rest-clients.html)
