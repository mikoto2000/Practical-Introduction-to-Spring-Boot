---
title: Spring Boot Actuator 入門 — ヘルスチェックとメトリクス
---

# Spring Boot Actuator 入門 — ヘルスチェックとメトリクス

## 講義の区切り

基本編の目安は 60〜90 分です。終了条件は、PostgreSQL の停止と復旧をヘルスチェックで検知し、注文取得 API の応答件数と処理時間を HTTP ステータス別に確認することです。
最後に、管理用エンドポイントの公開範囲を設定します。準備と前後の章は [学習ガイド](../introduction.md) で確認できます。

## 概要

注文 API を動かしたまま PostgreSQL を止めて、利用者のリクエストと運用者のヘルスチェックに何が起きるかを確認します。
次に、注文取得の成功と失敗を発生させ、Spring Boot Actuator のメトリクスで影響を受けた操作を絞ります。

## 事前準備

- [DB アクセス・トランザクション入門](./DBアクセス・トランザクション入門.md) の注文 API 完成版
- Java 21、Maven Wrapper、Spring Boot 4.0 系
- Linux コンテナーを実行できる Docker 環境
- 空きポート 8080（アプリ）、8081（管理用）、55432（PostgreSQL）

`docker info` を実行して、サーバーの情報が表示されることを確認します。
コマンドはすべて注文 API のプロジェクトルートで実行します。Windows では Git Bash または Docker と Java を利用できる WSL の Bash を使ってください。

## 触って学ぶ Spring Boot Actuator

### 注文 API を PostgreSQL につなぐ

`pom.xml` の `dependencies` に次を追加します。バージョンは Spring Boot の依存関係管理に従います。

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <scope>runtime</scope>
</dependency>
```

演習用の PostgreSQL を起動します。接続先はローカルに限定し、パスワードは演習専用の値を使います。

```bash
docker run -d --name orders-actuator-db \
  -p 127.0.0.1:55432:5432 \
  -e POSTGRES_DB=orders \
  -e POSTGRES_USER=orders \
  -e POSTGRES_PASSWORD=orders-local \
  postgres:17-alpine

docker exec orders-actuator-db pg_isready -h 127.0.0.1 -U orders -d orders
```

`accepting connections` が表示されるまで `pg_isready` を繰り返します。
次に、注文 API にある `schema.sql` を使って、演習用の空の DB にテーブルを一度だけ作成します。実行結果に `CREATE TABLE` が 2 回表示されます。

```bash
docker exec -i orders-actuator-db psql -v ON_ERROR_STOP=1 -U orders -d orders \
  < src/main/resources/schema.sql
```

`src/main/resources/application-actuator-lab.yaml` を作成します。
このプロファイルでは接続先を PostgreSQL に切り替え、テーブル作成済みの DB に接続します。アプリの再起動時にも同じ DB を使います。

```yaml title="application-actuator-lab.yaml"
server:
  address: 127.0.0.1
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:55432/orders
    username: orders
    password: orders-local
    hikari:
      connection-timeout: 3000
  sql:
    init:
      mode: never
  h2:
    console:
      enabled: false
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

デフォルトで HTTP に公開される管理用エンドポイントは `health` です。このプロファイルでは `metrics` も公開します。

演習用プロファイルを指定して起動します。以降の HTTP リクエストと Docker コマンドは、別のターミナルで実行します。

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=actuator-lab
```

### ヘルスチェックで停止と復旧を見る

まず、ヘルスチェックと注文登録を実行します。

```bash
curl -i http://127.0.0.1:8080/actuator/health
curl -i http://127.0.0.1:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerName":"Alice"}'
```

ヘルスチェックは HTTP 200 を返し、JSON の `status` は `UP` になります。環境によっては、`groups` などの項目も返ります。
注文登録は HTTP 201 を返します。
登録時の `Location` ヘッダーを控えてください。以降の `/orders/1` は、取得したパスに読み替えます。

アプリを起動したまま PostgreSQL を停止し、同じ注文を取得します。

```bash
docker stop orders-actuator-db
curl -i http://127.0.0.1:8080/actuator/health
curl -i http://127.0.0.1:8080/orders/1
```

ヘルスチェックは HTTP 503 を返し、JSON の `status` は `DOWN` になります。
注文の取得は DB に接続できず、HTTP 500 になります。アプリを起動したターミナルで、DB の接続取得に失敗した時刻と例外を確認してください。

```bash
docker start orders-actuator-db
docker exec orders-actuator-db pg_isready -h 127.0.0.1 -U orders -d orders
```

`accepting connections` が表示されるまで `pg_isready` を繰り返します。接続を受け付ける状態になったら、ヘルスチェックと同じ注文の取得を再実行します。

```bash
curl -i http://127.0.0.1:8080/actuator/health
curl -i http://127.0.0.1:8080/orders/1
```

アプリを再起動せずに `UP` と HTTP 200 に戻り、停止前に登録した注文を取得できます。接続が回復するまで、数秒おいて再確認してください。

### HTTP メトリクスで件数と応答時間を確かめる

アプリを起動したまま、今度は存在しない注文を取得して HTTP 404 を発生させます。

```bash
curl -i http://127.0.0.1:8080/orders/0
curl -i http://127.0.0.1:8080/actuator/metrics/http.server.requests
```

メトリクスの `availableTags` で、`method` の `GET`、`uri` の `/orders/{id}`、`status` の `200`・`404`・`500` を確認します。
`uri` には、実際の注文 ID を埋める前のパスが使われます。

次のコマンドで注文取得だけを対象にして、ステータス別に比較します。
`--data-urlencode` は `{id}` などをクエリーパラメーター用に符号化します。

```bash
curl -G http://127.0.0.1:8080/actuator/metrics/http.server.requests \
  --data-urlencode 'tag=method:GET' \
  --data-urlencode 'tag=uri:/orders/{id}' \
  --data-urlencode 'tag=status:200'

curl -G http://127.0.0.1:8080/actuator/metrics/http.server.requests \
  --data-urlencode 'tag=method:GET' \
  --data-urlencode 'tag=uri:/orders/{id}' \
  --data-urlencode 'tag=status:404'

curl -G http://127.0.0.1:8080/actuator/metrics/http.server.requests \
  --data-urlencode 'tag=method:GET' \
  --data-urlencode 'tag=uri:/orders/{id}' \
  --data-urlencode 'tag=status:500'
```

| `measurements` の項目 | 読み方 |
|---|---|
| `COUNT` | 条件に一致するリクエストの件数 |
| `TOTAL_TIME` | 条件に一致するリクエストの処理時間の合計。単位は `baseUnit` で確認 |
| `MAX` | 計測に使う時間窓での最大値 |

例の注文取得を各 1 回だけ送った場合は、どのステータスでも `COUNT` は 1 です。
存在しない注文を再取得し、404 の `COUNT` だけが増えることを確認します。平均の処理時間は `TOTAL_TIME` を `COUNT` で割って求められます。
500 の件数は、PostgreSQL が復旧した後も記録に残ります。今回のようにプロセス内で記録する場合、アプリを再起動すると集計がリセットされます。

### 管理用エンドポイントをローカルの別ポートに分ける

`application-actuator-lab.yaml` の既存の `management` に `server` を追加します。設定全体は次のようになります。

```yaml
management:
  server:
    port: 8081
    address: 127.0.0.1
  endpoints:
    web:
      exposure:
        include: health,metrics
```

同じプロファイルで再起動し、次の応答を比較します。

```bash
curl -i http://127.0.0.1:8080/orders/1
curl -i http://127.0.0.1:8080/actuator/health
curl -i http://127.0.0.1:8081/actuator/health
curl -i http://127.0.0.1:8081/actuator/metrics
curl -i http://127.0.0.1:8081/actuator/env
```

順に HTTP 200・404・200・200・404 を確認します。
`exposure.include` は HTTP に載せるエンドポイントを選ぶ設定です。管理ポートは `127.0.0.1` で待ち受けるため、同じホストの利用者やプロセスから接続できます。
利用者ごとに閲覧権限を分ける場合は、Spring Security などで認証と認可を設定します。

### 後片付け

アプリを Ctrl+C で停止します。次のコマンドで演習用コンテナーと、そこに保存した注文データを削除します。

```bash
docker stop orders-actuator-db
docker rm -v orders-actuator-db
```

## 座学で学ぶヘルスチェックとメトリクス

| 調べたいこと | 確認先 |
|---|---|
| 今、DB を含むアプリの状態はどうか | ヘルスチェックの HTTP ステータスと `status` |
| どの操作でエラーが増え、どれくらい時間がかかったか | HTTP メトリクスの `uri`・`method`・`status` と計測値 |
| どの時刻に、どんな例外が発生したか | アプリのログ |

この演習では、DB への接続確認に失敗すると、標準の DB ヘルスチェックが `DOWN` を報告します。ヘルスが `UP` のときでも、存在しない注文を取得すれば HTTP 404 が返ります。
障害調査では、メトリクスで件数が増えた操作を特定し、同じ時間帯のログを確認します。
[ロギング入門](./ロギング入門.md) のリクエスト ID を使うと、同じリクエストのログを追跡できます。

## 参考資料

- [Spring Boot 4.0 — Actuator エンドポイント](https://docs.spring.io/spring-boot/4.0/reference/actuator/endpoints.html)
- [Spring Boot 4.0 — メトリクス](https://docs.spring.io/spring-boot/4.0/reference/actuator/metrics.html)
- [Spring Boot 4.0 — Metrics API](https://docs.spring.io/spring-boot/4.0/api/rest/actuator/metrics.html)
- [Spring Boot 4.0 — HTTP による監視と管理](https://docs.spring.io/spring-boot/4.0/reference/actuator/monitoring.html)
