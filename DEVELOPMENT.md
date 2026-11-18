# Spring Boot 実践入門 website

この資料は教材サイトを編集する方向けです。
教材内の Java アプリケーションを動かす手順は、各ハンズオンを参照してください。

## 事前準備

Node.js 24 と npm が使える環境で、リポジトリのルート（このファイルと同じ場所）を作業ディレクトリにします。
まず `node --version` と `npm --version` で、コマンドが実行できることを確認してください。

サイトは `@rspress/core 2.0.1` を使います。`npm ci` 後に `npx --no-install rspress --version` で 2.0.1 と表示されることを確認してください。
[Rspress 2 の移行ガイド](https://www.rspress.dev/guide/migration/rspress-1-x) に沿って依存と設定の import を統一しています。

## セットアップ

依存関係をインストールします。

```bash
npm ci
```

## はじめ方

開発サーバーを起動します。ターミナルに表示された URL をブラウザで開いてください。
終了するときは、起動したターミナルで Ctrl+C を押します。

```bash
npm run dev
```

本番用にサイトをビルドします。

```bash
npm run build
```

ビルドが成功した後、本番ビルドをローカルでプレビューします。

```bash
npm run preview
```

アクセス解析を使う場合は、ビルド時の環境変数 `GA_ID` に Google Analytics の測定 ID を設定します。
GitHub Pages のビルドには、GitHub Actions のリポジトリ変数 `GA_ID` が渡されます。空の場合は計測タグを出力しません。

## 変更の確認

変更を確認するには、CI と同じ順序で次のコマンドを実行します。

```bash
npm run textlint
npm run lint
npm run check
npm run build
```

チェックに加え、プレビューで見出し、コードブロック、画像、リンクを確認してください。
教材のコード例はサイトのビルドだけでは検証されないため、実行手順を変えた場合は Java プロジェクトでも確認します。

## 教材コードの実行検証

PR では、注文 API の H2・PostgreSQL 統合テストと、Security のログイン・登録・ログアウト・一般ユーザーの登録拒否を Chromium で実行します。CI は教材のコードブロックから一時ディレクトリにプロジェクトを組み立てます。コード例を更新するときは、対応するコードブロックの `title` を変更した場合も生成スクリプトを更新してください。

Java 21、Maven、Python 3、Docker が利用できる環境では、リポジトリのルートから注文 API を検証できます。

```bash
orders_dir=$(mktemp -d)
python3 .github/scripts/prepare-exercises.py "$orders_dir"
mvn -f "$orders_dir/pom.xml" test
```

Security と Playwright のプロジェクト生成および実行コマンドは [教材コードの検証ワークフロー](.github/workflows/exercises.yml) を参照してください。

RFC 9457 のタスク完了処理は、同時リクエストで成功が 1 件だけになることも CI で検証します。Docker なしで実行できます。

```bash
task_dir=$(mktemp -d)
python3 .github/scripts/prepare-exercises.py "$task_dir" --lesson rfc9457
mvn -f "$task_dir/pom.xml" test
```

### 外部 API・リトライ・キャッシュ・Security の検証

次の章も、教材から生成したプロジェクトで CI の回帰テストを実行します。

| `--lesson` | 確認する動作 |
|---|---|
| `shipping` | 送料の取得、404・500・空の応答本文の変換、読み取りタイムアウト |
| `retry` | 上記に加え、503 の再試行回数と待ち時間、対象外の失敗では再試行しないこと |
| `cache` | キャッシュヒット、価格更新後の再取得、別商品のキャッシュ保持、不正な更新の拒否 |
| `redis-cache` | 実物の Redis を使ったキャッシュの動作と TTL による期限切れ |
| `security-tests` | 教材の 12 ケースで認証・認可・CSRF・ログイン・ログアウトを検証 |

たとえば、リトライ入門を検証するには次を実行します。模擬配送 API はテストが空きポートで起動・停止します。

```bash
lesson_dir=$(mktemp -d)
python3 .github/scripts/prepare-exercises.py "$lesson_dir" --lesson retry
mvn -f "$lesson_dir/pom.xml" test
```

Redis の検証では、[Redis キャッシュ入門](docs/SpringBoot入門/Redisキャッシュ入門.md) の手順で演習用 Redis を起動してから、`--lesson redis-cache` で生成します。
既定のポートは 56380 です。別のポートを使う場合は Maven に `-Dspring.data.redis.port=ポート番号` を渡します。
CI は専用の Redis サービスを起動し、割り当てられたポートを渡します。テストは `products` キャッシュを削除するため、共有環境の Redis へ接続しないでください。

これらのキャッシュテストは逐次リクエストの動作を検証します。同時更新や複数アプリ間の強い整合性は保証しません。
教材コードの依存関係やアノテーションを変えた場合は、名前付きコードブロックと生成スクリプトの対応も確認してください。
