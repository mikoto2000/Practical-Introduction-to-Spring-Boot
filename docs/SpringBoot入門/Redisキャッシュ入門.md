---
title: Spring Boot Redis キャッシュ入門 — 削除と TTL
---

# Spring Boot Redis キャッシュ入門 — 削除と TTL

## 講義の区切り

基本編の目安は 60 分です。終了条件は、商品価格の更新で `@CacheEvict` が Redis のキーを削除し、次の取得で新しい価格が保存されることです。
さらに、更新していない商品のキーが TTL（キャッシュの有効期間）で期限切れになり、次の取得で作り直されることを確認します。

## 概要

[キャッシュ入門](./キャッシュ入門.md) の商品価格 API を使います。商品の保管場所は前章と同じアプリ内のデータで、キャッシュの保存先を Redis に切り替えます。
HTTP の応答とアプリの `READ`・`UPDATE` ログに加え、`redis-cli` でキーの有無と残りの有効期間を確認します。

## 事前準備

- キャッシュ入門の完成版（`@Cacheable` と `@CacheEvict` を追加した状態）
- JDK 21 と Spring Boot 4.0 系
- Linux コンテナーを実行できる Docker 環境
- 空きポート 8080（商品価格 API）、56380（Redis）

前章のアプリが起動中なら Ctrl+C で停止します。

## 触って学ぶ Redis キャッシュ

### 演習用の Redis を起動する

次のコマンドは Bash と PowerShell のどちらでも実行できます。Redis はローカルからだけ接続できるようにします。

```bash
docker run --rm --name redis-cache-lab -d -p 127.0.0.1:56380:6379 redis:7.4-alpine
docker exec redis-cache-lab redis-cli ping
```

`PONG` が返ることを確認します。

### アプリのキャッシュの保存先を切り替える

`pom.xml` の `dependencies` に次の依存関係を追加します。

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

`src/main/resources/application.yaml` の既存の `spring` の設定を次の内容にします。
ここでは TTL を 30 秒に設定します。

```yaml title="application.yaml"
spring:
  application:
    name: cache
  data:
    redis:
      host: 127.0.0.1
      port: 56380
  cache:
    type: redis
    cache-names: products
    redis:
      time-to-live: 30s
```

`spring.cache.type: redis` を指定すると、キャッシュは Redis に保存されます。
既定のキャッシュ値の変換には Java のシリアライズを使うため、`src/main/java/dev/mikoto2000/springboot/cache/Product.java` を次のようにします。

```java title="Product.java"
package dev.mikoto2000.springboot.cache;

import java.io.Serializable;

public record Product(long id, String name, int priceYen) implements Serializable {}
```

前章で付けた `@EnableCaching`、`@Cacheable`、`@CacheEvict` を使います。
プロジェクトのルートでアプリを起動してください。PowerShell では `.\mvnw.cmd spring-boot:run` を使います。

```bash
./mvnw spring-boot:run
```

### 取得した商品が Redis に入ることを確認する

別のターミナルで商品 1 を 2 回取得します。PowerShell では、以下の `curl` を `curl.exe` に読み替えてください。

```bash
curl -i http://localhost:8080/products/1
curl -i http://localhost:8080/products/1
docker exec redis-cache-lab redis-cli TTL 'products::1'
```

応答はどちらも 200 で価格は 1200 円です。アプリの `READ productId=1 priceYen=1200` は 1 行だけ出ます。
Redis のキーは、キャッシュ名 `products` と商品 ID `1` を組み合わせた `products::1` です。
`TTL` コマンドは残りの秒数を返します。キーが見つからない場合の戻り値は `-2` です。

### 価格更新でキーが削除されることを確認する

以下のコマンドを続けて実行します。最初の GET でキーの存在を確かめ、PUT の直後にキーの有無を調べます。
更新前の `TTL` が 0 秒に近い場合は、そのまま期限切れを待ち、GET からやり直します。

```bash
curl -i http://localhost:8080/products/1
docker exec redis-cache-lab redis-cli TTL 'products::1'
curl -i -X PUT 'http://localhost:8080/products/1/price?priceYen=1500'
docker exec redis-cache-lab redis-cli EXISTS 'products::1'
```

PUT は 204 を返し、アプリには `UPDATE productId=1 priceYen=1500` が出ます。
更新前に TTL が十分残っていたキーが、PUT の直後には `EXISTS` で `0` になります。これは更新メソッドの `@CacheEvict` による削除です。

```bash
curl -i http://localhost:8080/products/1
docker exec redis-cache-lab redis-cli EXISTS 'products::1'
```

GET は 1500 円を返し、`READ productId=1 priceYen=1500` が出ます。`EXISTS` が `1` に戻り、新しい価格がキャッシュされたことを確認できます。

### TTL でキーが期限切れになることを確認する

商品 2 を使います。初めに GET を送り、直後の `TTL` が 30 秒前後であることを確認します。

```bash
curl -i http://localhost:8080/products/2
docker exec redis-cache-lab redis-cli TTL 'products::2'
```

商品 2 を更新せず、最後の保存から 30 秒以上待って、もう一度 `TTL` を確認します。
以下は Bash の例です。PowerShell では `sleep 31` の代わりに `Start-Sleep -Seconds 31` を使えます。

```bash
sleep 31
docker exec redis-cache-lab redis-cli TTL 'products::2'
```

`-2` が返り、キーが期限切れになったことが分かります。続けて商品 2 をもう一度取得します。

```bash
curl -i http://localhost:8080/products/2
docker exec redis-cache-lab redis-cli TTL 'products::2'
```

価格は 800 円のままですが、`READ productId=2 priceYen=800` がもう 1 行増えます。Redis には新しい有効期間を持つキーが作られます。

### 後片付け

アプリを Ctrl+C で停止し、演習用の Redis も停止します。

```bash
docker stop redis-cache-lab
```

## 座学で学ぶ削除と期限切れ

`@CacheEvict` は更新の成功に合わせて指定したキーを削除します。この演習のように前の応答を待ってから次のリクエストを送る場合は、更新後の GET で新しい値を取り直せます。
GET と PUT が重なると、更新前の価格を読み取った GET が、削除後に古い値を Redis へ再保存する場合があります。詳しい順序は [キャッシュ入門の同時実行時の制約](./キャッシュ入門.md#同時実行時の制約) を参照してください。
この設定では再保存から 30 秒が経過すると期限切れになります。TTL は更新直後の整合性を保証しません。
TTL はキャッシュした値を保持する期間です。この演習の設定では、GET がキャッシュにヒットしても期限は延びません。
商品が更新されなくても期限は来ます。期限切れ後のアクセスでは、アプリが保管場所から商品を読み直します。

### 理解を確かめる

1. 商品 1 を更新した直後に `products::1` が消えた理由を説明する。
2. 商品 2 を更新していないのに、30 秒後の GET で `READ` が増えた理由を説明する。
3. 価格の変更を直後の取得に反映したい場合、TTL だけで運用するとどんな応答が返りうるか説明する。

## 参考資料

- [Spring Boot 4.0 リファレンス — Caching](https://docs.spring.io/spring-boot/4.0/reference/io/caching.html)
- [Spring Data Redis 4.0 リファレンス — Redis Cache](https://docs.spring.io/spring-data-redis/reference/4.0/redis/redis-cache.html)
