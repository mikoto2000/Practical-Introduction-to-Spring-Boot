# docs 配下 Markdown 教材レビュー（2026-11-01）

## 対象と確認状態

- 対象: docs 配下 Markdown 16 件（index.md、introduction.md、SpringBoot入門 7 件、SpringBootテスト入門 6 件、SpringSecurity入門 2 件）
- 台帳: reviewJob `c0083ac8-2b08-4b58-839c-4c63d441bc0b`、status COMPLETE、reviewPending 空
- 確認済み: 16 ファイルの required range をカバー（台帳上）
- 部分確認: 台帳上なし
- 未確認: 実行検証（ビルド・テスト・Playwright）、依存 starter の実在、Rspress ビルド、`_meta.json` の扱い

## 確定指摘（P2）

| ID | ファイル | 行 | 受講者への影響 | 根拠 | 最小修正案 |
|---|---|---|---|---|---|
| VAL-01 | docs/SpringBoot入門/バリデーション入門.md | 106 | `@Size` を代表例として説明した直後に Hibernate 固有の `@Length` を説明なしで使い、標準 API との使い分けを判断できない | `@Length(min = 1, max = 10)` | `@Size(min = 1, max = 10)` に置換、または `@Length` が Hibernate Validator 固有で `@Size` と同様に長さを検証すると 1 文補足 |
| LOG-01/02/03（同一原因） | docs/SpringBoot入門/ロギング入門.md | 1038, 1041, 1054 | 指示は `traceId`/`user` に合わせるよう求めるが、直後のパターン例と抜粋は `requestId`/`userId`。ハンズオンの `AccessLogFilter` は `traceId`/`user` を設定するため、そのまま適用すると MDC 値がログに現れず動作確認でつまずく | `MDC.put("requestId", requestId);`、`%X{requestId}`、`MDC キー名は…traceId と user に合わせてください` | パターン例を `%X{traceId}`/`%X{user}`、抜粋を `MDC.put("traceId", ...)`/`MDC.put("user", ...)` に揃える。または独立例であると明記 |
| TEST-01 | docs/SpringBootテスト入門/テスト入門.md | 977 | `reset(...)` が基本機能と同列に並び、`MockitoExtension` がテストごとにモックを用意する説明と衝突。受講者が毎回 reset を呼び、テスト間の独立性を壊す実装に進みやすい | `| \`reset(...)\` | モックをリセットする | \`reset(mapper)\` |` | 「通常は `@ExtendWith(MockitoExtension.class)` がテストごとに初期化するため不要。共有モックを意図的に再利用する場合に限る」と注記、または行削除 |
| E2E-01 | docs/SpringBootテスト入門/E2Eテスト入門.md | 13 | 教材自ら「たたき台」でありコード例と所要時間が未検証と明記。手順どおり進めても再現しない可能性 | `この資料はたたき台です。公開前に、完成した演習アプリでコード例と所要時間を確認してください。` | 検証済みのコード例・実測所要時間に更新し、注記を削除または「検証済み（確認日: YYYY-MM-DD）」へ置換 |

## 棄却した指摘

- SEC1-01（docs/SpringSecurity入門/Vol1.md 399 行）: 「GET リンクではログアウトできない」という指摘。同一ページ 390〜391 行に「`/logout` にアクセスするとログアウト確認画面が表示されます」「`Log Out` ボタンを押下するとログアウトし、`/login?logout` にリダイレクトされます」と明記されており、CSRF 有効時の既定確認ページを経由してログアウトできる。指摘の前提と原文が矛盾するため棄却。

## 追加観察（未確定・P2 候補）

- docs/SpringBoot入門/ロギング入門.md 230〜300 行: `/invalidEndpoint` は `status=404, success=SUCCESS`。`success` の定義（例外伝播時のみ FAIL）とは整合するが、本文の「404 で失敗しているものもちゃんとログに記録されている」という表現は `success=SUCCESS` と語義が衝突する。修正案: 「404 で応答したものも記録されている」に変更。
- docs/SpringSecurity入門/_meta.json: Rspress 2 では sidebar を `rspress.config.ts` で定義しており、`_meta.json` は不要。残存すると順序や設定の混乱の元。修正案: 削除、または Rspress 2 移行ガイドに従って整理。

## 横断レビュー（静的確認）

- リンク: introduction.md の 14 章リンク、各章の前提リンクはすべて実在パスに解決。
- バージョン: Java 21、Spring Boot 4.0 系で統一。Security ZIP の Java 25 指定は Vol1 で Java 21 に変更する手順があり、introduction でも言及。
- ポート: 基本 8080、E2E は 18080 で別プロファイル。役割分離が明示されている。
- 章順: introduction の表・一覧と各章の前提が対応。開発者テスト入門を先に読む代替経路も明示。
- 用語: 「テスト入門」の title は「Spring Boot テスト実践入門」だが、リンクテキストは短縮名。許容範囲。

## 除外

- docs/manifest.json: `format: mdpkg` で Rspress ナビゲーションとは別系統。教材本文外。
- review/ ディレクトリ: 既存のレビュー記録。今回の対象外。
- README.md、DEVELOPMENT.md: リポジトリ運用向け。教材本文外。

## 未確認・検証上の限界

- `spring-boot-starter-webmvc-test`、`spring-boot-starter-aspectj`、`spring-boot-h2console`、`mybatis-spring-boot-starter-test` の Spring Boot 4.0 系での実在・解決性は未確認。
- Rspress 2.0.1 でのビルド、リンク解決、画像表示は未実行。
- Playwright の実行、Docker の実行は未実施。
- これらは依頼で実行許可が明示されていないため、静的レビューに留めた。

## 良い点

- introduction.md が学習順序・前提知識・環境要件・章ごとの終了条件を明示している。
- 各章が「触って学ぶ」「座学で学ぶ」を分け、発展内容を基本から分離している。
- Security ZIP の Java 25 指定というバージョン差異を明示し、変更手順を提示している。
- E2E のポートを 18080 に分離し、既存アプリとの競合を回避している。
