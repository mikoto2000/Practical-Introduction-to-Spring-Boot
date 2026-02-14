---
title: Spring Boot と Flyway で実現する DB マイグレーション - DB を壊さず開発から運用まで行う方法
---

# Spring Boot と Flyway で実現する DB マイグレーション - DB を壊さず開発から運用まで行う方法

## Spring Initializr

次のリンクから Spring Initializr を開き、 Spring Boot プロジェクトを作成してください。

[Spring Initializr](https://start.spring.io/#!type=maven-project&language=java&platformVersion=4.0.2&packaging=jar&configurationFileFormat=yaml&jvmVersion=21&groupId=dev.mikoto2000.springboot&artifactId=flyway&name=flyway&description=Demo%20project%20for%20Spring%20Boot%20and%20Flyway&packageName=dev.mikoto2000.springboot.flyway&dependencies=devtools,lombok,web,data-jdbc,h2,flyway)


## DB 接続情報の作成

`application.yaml` に、データソース定義を追加します。

マイグレーションを体験するためには、 Spring Boot アプリケーションの起動ごとに DB の初期化がされてしまうと困ります。
そのため今回は、 H2 DB を利用し、 `./target/workshopdb.mv.db` という名前の DB ファイルを作成し、そこでデータを管理します。

```yaml
spring:
  application:
    name: flyway

  # 追加ここから
  datasource:
    url: ${DATASOURCE_URL:jdbc:h2:file:./target/workshopdb}
    username: ${DATASOURCE_USERNAME:sa}
    password: ${DATASOURCE_PASSWORD:}
    driver-class-name: org.h2.Driver
  # 追加ここまで
```

H2 DB は、デフォルトユーザー名 `sa`, パスワード無しでログインできるため、そのように設定しています。


## 起動確認

この時点で正常に起動できるか確認しましょう。

次のコマンドを実行し、 Spring Boot アプリケーションが正常に起動することを確認してください。

```sh
./mvnw spring-boot:run
```


## H2 コンソールの確認

Spring Boot アプリケーションを起動した状態で `https://localhost:8080/h2-console` に接続すると、 H2 コンソールを確認できます。

H2 コンソールでは、 H2 が管理している DB の状態が確認できます。
今回は、 DB マイグレーションの結果をこの H2 コンソールを用いて確認していきます。

H2 コンソールのログイン画面が開くので、 `application.yaml` に記載したものと同じ接続情報を入力し、 `Connect` ボタンを押してください。


## 初期スキーマの作成

`src/main/resources/db/migration` に DDL を格納することで、 Spring Boot アプリケーション起動時に Flyway が走り、マイグレーションを行ってくれます。
(`src/main/resources/db/migration` は Flyway のデフォルト探索パス)

今回は `ACCOUNT` テーブルを作ってみましょう。

`src/main/resources/db/migration/V20260203.01__add_account_table.sql`:

```sql
CREATE TABLE ACCOUNT (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(50),
  password VARCHAR(255)
);
```

これだけで、マイグレーションの準備は完了です。

:::note
今回は、可読性を重視して「日付.連番」形式を採用しています。
命名規則については、「タイムスタンプ」や「セマンティックバージョニング」など
色々ありますので、プロジェクトの特性や運用条件に応じて選ぶようにしましょう。
※ チーム内でルールを統一することが重要です。
:::


## Spring Boot アプリケーションの起動とスキーマ確認

それではもう一度 Spring Boot アプリケーションを実行してみましょう。

```sh
./mvnw spring-boot:run
```

起動ログに、次のようにマイグレーションの結果が表示されます。

```
2026-02-04T10:45:41.784Z  INFO 170450 --- [flyway] [  restartedMain] o.f.c.i.s.JdbcTableSchemaHistory         : Schema history table "PUBLIC"."flyway_schema_history" does not exist yet
2026-02-04T10:45:41.786Z  INFO 170450 --- [flyway] [  restartedMain] o.f.core.internal.command.DbValidate     : Successfully validated 1 migration (execution time 00:00.006s)
2026-02-04T10:45:41.789Z  INFO 170450 --- [flyway] [  restartedMain] o.f.c.i.s.JdbcTableSchemaHistory         : Creating Schema History table "PUBLIC"."flyway_schema_history" ...
2026-02-04T10:45:41.808Z  INFO 170450 --- [flyway] [  restartedMain] o.f.core.internal.command.DbMigrate      : Current version of schema "PUBLIC": << Empty Schema >>
2026-02-04T10:45:41.811Z  INFO 170450 --- [flyway] [  restartedMain] o.f.core.internal.command.DbMigrate      : Migrating schema "PUBLIC" to version "20260203.01 - add account table"
2026-02-04T10:45:41.821Z  INFO 170450 --- [flyway] [  restartedMain] o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema "PUBLIC", now at version v20260203.01 (execution time 00:00.003s)
2026-02-04T10:45:41.914Z  INFO 170450 --- [flyway] [  restartedMain] o.s.b.h.a.H2ConsoleAutoConfiguration     : H2 console available at '/h2-console'. Database available at 'jdbc:h2:file:./
```

ログ上では成功しているので、 `http://localhost:8080/h2-console` にアクセスし、テーブル定義が存在するか確認してみましょう。

テーブル一覧にテーブル `ACCOUNT` が存在していることを確認してください。

また、テーブル `flyway_schema_history` が存在していることに気付いた方もいるかもしれません。
このテーブルは、 Flyway が行ったマイグレーションの履歴を管理するためのテーブルです。
消したり手動で変更したりしないようにしましょう。

このように、適用したマイグレーションファイルを Flyway が管理するため、
マイグレーションが完了したら、作成したマイグレーションファイルを編集しないようにしましょう。

間違いや仕様変更でスキーマが変更されたら、新しいマイグレーションファイルを追加して対応していきます。


## スキーマの更新ファイル作成

それではさっそく、「仕様変更でテーブル `ACCOUNT` にカラム `ROLE` が必要になった」という想定で、
新しいマイグレーションファイルを作成してみましょう。

`src/main/resources/db/migration/V20260203.02__add_role_column_to_account_table.sql`:

```sql
ALTER TABLE ACCOUNT ADD COLUMN role VARCHAR(50);
```

ALTER 文でカラム `ROLE` を追加するマイグレーションファイルを新規作成しました。

「expand / contract」という考え方の元、 `NOT NULL` や `DEFAULT` をつけずにカラム追加のみを行っています。
`NOT NULL` や `DEFAULT` は、次以降のステップで実行することとなります。


## Spring Boot アプリケーションの起動と更新したスキーマの確認

それではもう一度 Spring Boot アプリケーションを実行し、
H2 コンソールでカラムが追加されているか確認してみましょう。

Spring Boot 起動時に Flyway が自動実行されるため、新しいマイグレーションが適用されます。

カラムが追加されていることが確認できれば OK です。


## 並行開発に向けたマイグレーション運用例紹介

スキーマに厳密なプロジェクトでは、
「実装中は draft ディレクトリにマイグレーションファイルを作り、
レビュー後に migration ディレクトリに移動させてマージするという運用する」
という運用がされています。

```
feature branch を作成
↓
draft ディレクトリ内で SQL を書く
↓
PR（draft状態）
↓
レビューOK
↓
migration へ移動（Vxxxに変換）
↓
PR更新
↓
最終レビュー
↓
Pull Request をマージ
```

ただし、この運用をするには次の条件を満たす必要があります。

- Git の高度な機能が使える
- 機能追加毎に DB をリセットできる環境が整っている

ですので、このあたりの運用は、自分のプロジェクトの特性に合わせ、自分で考えフィットさせていくようにしましょう。


## まとめ

今回は、 Spring Boot と Flyway を組み合わせて使った場合の基本的な挙動について学びました。

どうやれば自分のプロジェクトに取り入れられるか、皆さんも考えてみてください。


## 参考資料

- expand / contract
    - [Expand and Contract パターンによる破壊的変更の段階的適用](https://zenn.dev/babyjob/articles/4bfe61d366c6c0)
    - [スキーマの変更をデプロイする方法 - そこに仁義はあるのか(仮)](https://syobochim.hatenablog.com/entry/2020/05/20/084736)
    - [データベースを止めずにテーブル設計を変更したい](https://zenn.dev/praha/articles/abceef00258275)


