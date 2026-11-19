# Spring Boot 実践入門 website

この資料は教材サイトを編集する方向けです。
教材内の Java アプリケーションを動かす手順は、各ハンズオンを参照してください。

## 事前準備

Node.js 24・npm と Python 3.10 以上が使える環境で、リポジトリのルート（このファイルと同じ場所）を作業ディレクトリにします。
まず `node --version`、`npm --version`、`python3 --version` で、コマンドが実行できることを確認してください。WindowsでPythonのコマンドが `python` の場合は、以下の `python3` も読み替えます。

サイトは `@rspress/core 2.1.3` を使います。`npm ci` 後に `npx --no-install rspress --version` で 2.1.3 と表示されることを確認してください。
[Rspress 2 の移行ガイド](https://www.rspress.dev/guide/migration/rspress-1-x) に沿って依存と設定の import を統一しています。

textlint 16.1.1 が指定する Glob 13 系には既知脆弱性があるため、同ツールの依存に限って Glob 14.0.6 以上へ上書きしています。
textlintを更新する際は、この上書きが引き続き必要か確認し、文章検査も実行してください。

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
npm run typecheck
npm run check:chapters
python3 -m unittest discover -s .github/scripts/tests
npm run build
```

チェックに加え、プレビューで見出し、コードブロック、画像、リンクを確認してください。
教材のコード例はサイトのビルドだけでは検証されないため、実行手順を変えた場合は Java プロジェクトでも確認します。

## 教材コードの実行検証

PR と main への push では、全22章の主要なコード例を検証します。公開ワークフローから演習検証を呼び出し、サイトのビルドと演習検証の両方が成功してから GitHub Pages へデプロイします。
同じブランチの検証と公開はワークフロー単位で順番に実行します。公開直前にmainの最新SHAを照合し、古いコミットの実行結果は公開しません。
CI は教材のコードブロックから一時ディレクトリにプロジェクトを組み立てます。コード例を更新するときは、対応するコードブロックの `title` を変更した場合も生成スクリプトを更新してください。
追加した章の完全な Java クラスは、同じクラスの置き換え例がある場合、最後の定義を使います。クラスの一覧も照合し、抽出対象の不足をエラーにします。
専用のテスト用ファイルを使う章は、その欠落も生成時にエラーにします。CIのMaven検証では `-DfailIfNoTests=true` を指定し、テスト0件を成功として扱いません。

Java 21、Maven、Python 3.10 以上、Docker が利用できる環境では、リポジトリのルートから注文 API を検証できます。

```bash
orders_dir=$(mktemp -d)
python3 .github/scripts/prepare-exercises.py "$orders_dir"
mvn -f "$orders_dir/pom.xml" test
```

Security と Playwright のプロジェクト生成および実行コマンドは [教材コードの検証ワークフロー](.github/workflows/exercises.yml) を参照してください。

RFC 9457 は、同時完了が1件だけ成功することに加え、本文の MockMvc テストで HTTP ステータス・メディアタイプ・問題種別・入力違反を検証します。Docker なしで実行できます。

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
Redis の検証では、本文の `RedisCacheConfig` も抽出します。Spring Data Redis 4.0 の非同期書き込み・削除に対し、演習では `immediateWrites()` で完了を待ってから結果を確認します。

これらのキャッシュテストは逐次リクエストの動作を検証します。同時更新や複数アプリ間の強い整合性は保証しません。
教材コードの依存関係やアノテーションを変えた場合は、名前付きコードブロックと生成スクリプトの対応も確認してください。

### その他の章の検証範囲

同じ生成コマンドに次の `--lesson` を指定できます。Maven の実行方法は上記と共通です。

| `--lesson` | 確認する動作 | 追加環境 |
|---|---|---|
| `project-create` | 年齢計算 API、日付の変換、未来日付・null の拒否 | なし |
| `validation` | パラメーターの既定値、形式・長さ制約、DTO の必須制約、不正 JSON | なし |
| `testing` | 本文の Util・Service・MyBatis・Controller テストと境界値演習 | なし（H2 使用） |
| `test-design` | 本文の年齢の同値分割・境界値テスト | なし |
| `validation-tests` | 本文の DTO・Controller・Advice テスト | なし |
| `logging` | アクセス・AOP・業務ログ、入力の無害化、失敗時の記録、MDC の解放 | なし |
| `migration` | 本文の2本の SQL の適用、履歴、再実行時のデータ保持 | なし（H2 使用） |
| `errors` | 本文の8件の HTTP 契約テスト（発展の共通処理を含む） | なし |
| `scheduling` | 実際のスケジュール起動と複数回の START・END ログ | なし |
| `session` | 異なるポートの2アプリ間での Cookie による共有と無効化 | Redis |
| `actuator` | PostgreSQL の接続拒否・復旧時のヘルス、API 応答、ステータス別メトリクス | Docker |

`session` のローカル既定ポートは本文と同じ56379です。CI は専用 Redis の割当ポートを `-Dspring.data.redis.port` で渡します。
`actuator` は Testcontainers で PostgreSQL を起動するので、本文の手動 Docker コマンドは不要です。
障害は接続の拒否と既存接続の切断で再現し、コンテナーの再起動による割当ポートの変更を避けます。
Security 第1回・第2回・E2E は、既存の配布 ZIP から完成版を生成し、Chromium で検証します。生成した E2E 設定とテストにも `npm run typecheck` を実行します。

全章に検証対象がありますが、すべての演習手順を自動化しているわけではありません。jar の手動起動、プロファイル切替、定期実行の間隔比較、Flyway の並行開発演習などは各章の手順でも確認してください。
`npm run check:chapters` は教材ファイルと、学習ガイド・サイドバーの章一覧と順序を照合します。新しい章を追加するときは両方の一覧を更新してください。
