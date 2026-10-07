# 社内勉強会資料レビュー

レビュー日: 2026-10-07（Asia/Tokyo）
対象コミット: `a27be331b8ece628eaea291698325ceb857ba4bd`
レビュー用ブランチ: `codex/review-study-materials`
worktree: `C:\Users\mikoto\.codex\worktrees\study-materials-review\Practical-Introduction-to-Spring-Boot`

## 1. Executive Summary

基本概念、実装、動作確認、理論の復習までをつなぐ教材として、土台は良好です。各章の前提知識・到達目標、演習と本番の違い、テストの限界、失敗時の確認が明記されています。特に DB トランザクションと Security テストは、成功だけでなく失敗の理由を切り分けて学べる構成を維持すべきです。

ただし、受講者が本文どおり進めると止まる問題が3件あります。バリデーションテストの import 漏れ、Security 配布 ZIP の Java 25 指定と本文の Java 21 の不一致、テスト入門で後の節にある Mapper を先に参照する順序です。完成状態のサンプルが動くことと、掲載順に進められることは別に確認する必要があります。

| Severity | 件数 | 判断 |
|---|---:|---|
| Critical | 0 | 教材の中心概念を覆す誤り、重大な危険操作は確認できなかった |
| High | 3 | コンパイル・起動を妨げる問題を再現した |
| Medium | 13 | 設定の前提、エラー処理、境界条件、教育構成、実務への接続の改善 |
| Low | 4 | バージョン呼称、サイトメタ情報、コマンド表現、Markdown の改善 |
| 合計 | 20 | 横断指摘の再掲は重複計上していない |

build、ESLint、textlint は成功しました。元の15ページから生成した HTML の内部リンク1,048件、画像参照24件に欠落はありません。本文の外部リンク44種類も取得時にはすべて HTTP 2xx でした。Java の完成サンプル4構成で28件のテストが成功した一方、2種類のコンパイル失敗と配布 ZIP の JDK 不一致を再現しました。PostgreSQL とブラウザーの画面確認は未実施です。

**最優先 Top 5**:

1. H-01: バリデーションテストの `StandardCharsets` import 漏れを解消する。
2. H-02: Security 配布 ZIP と本文の JDK 前提を一致させる。
3. H-03: Service テスト前に Mapper の型を用意する。
4. M-01: ロギングの独自 XML と YAML 設定例の適用条件を明示する。
5. M-02: 「例外ログの良い例」で例外を捕捉した後の失敗伝達を示す。

今回は教材本文、設定、依存関係、サイドバー、配布サンプルを変更していません。この報告書のみを成果物とします。

## 2. 対象範囲

### サイト構造と実装

Markdown の個別レビュー前に、`rspress.config.ts`、`package.json`、`package-lock.json`、README、DEVELOPMENT、サイドバー、`_meta.json`、`manifest.json`、GitHub Actions、devcontainer、画像と図の配置を確認しました。適用される AGENTS.md はリポジトリ内に見つかりませんでした。

- ドキュメント root: `docs`。ホーム `index.md` と導入 `introduction.md`、Spring Boot 基礎6ページ、テスト5ページ、Security 2ページの計15ページ。MDX は0件。
- サイドバーは `/SpringBoot入門`、`/SpringBootテスト入門`、`/SpringSecurity入門` の3グループ。グローバルな学習順序は導入に記載されている。
- 画像: テスト対象を示す PNG 6枚、同値分割・境界値分析の SVG 2枚、`docs/diagrams` の SVG / draw JSON、公開ロゴなど。図の source と rendered を対応させる manifest もある。
- 独自 React コンポーネント、サイト用 Java ソース、実行可能な Java プロジェクトはこのリポジトリにはない。Java は Markdown のコードブロックと外部 ZIP で提供される。
- npm / `package-lock.json` を使用。開発 `npm run dev`、build `npm run build`、preview `npm run preview`。Node.js 24 の CI / devcontainer。
- `rspress` は宣言 `^1.40.2`、lockfile / インストール実体 `1.47.0`。ただしルートに `@rspress/core 2.0.1` もあり、今回のクリーンインストール後の `npm run build` は **Rspress v2.0.1** を起動した（M-06）。
- 設定プラグイン: sitemap 2.0.1、rst-directives 0.2.0、PlantUML 0.1.0。builder に Google Analytics。本文では frontmatter、admonition、コードタイトル、相対リンクを使用。PlantUML / RST の本文記法は現在の公開15ページでは確認できない。
- 出力先は `doc_build`。base は `/Practical-Introduction-to-Spring-Boot/`。sitemap のホスト設定は実際の Pages 用として要確認（L-02）。

### 対象と除外の数え方

公開対象 **15 Markdown / 0 MDX を全文通読**し、通読完了後に横断レビューを行いました。README と DEVELOPMENT の2件も、前提・運用手順を確認する補助資料として読んでいます。

| 範囲 | Markdown / MDX 数 | 扱い |
|---|---:|---|
| `docs` の既存教材 | 15 | 全文レビュー対象 |
| ルートの README / DEVELOPMENT | 2 | 公開教材の集計から除外、補助確認 |
| worktree の `node_modules` | 1,572 | third-party / vendor 文書として除外 |
| `doc_build` | 0 | 生成物として対象外。HTML はリンク検証に利用 |
| tracked の自動生成 API 文書 | 0 | 該当なし |
| 除外 Markdown 合計 | **1,574** | クリーンインストール後の worktree で物理集計 |

Git 管理された Markdown に限ると17件中、公開対象15件、対象外の補助資料2件です。依存文書数はインストール内容に依存し、教材量とは関係ありません。`.git`、`dist`、`build`、生成物、外部配布 ZIP は公開 Markdown の分母に含めません。新規の本報告書もレビュー対象15件には含めません。

### 想定受講順序・前提・到達像

導入ページが指定する順序は、プロジェクト作成 → バリデーション → テスト実装・設計 → バリデーションテスト → ログ・マイグレーション → DB / トランザクション → 例外処理 → Security 1・2 → Security テスト → DB 統合テストです。

Java のクラス・メソッド・コンストラクタ・例外、HTTP / JSON の基本が入口の前提です。その後、Controller / Service / DI、SQL、HTML フォーム、最後に Docker が加わります。題材は原則別プロジェクトで、Security 2 は Security 1 の続き、DB 統合テストは注文 API の続きです。

明示された目的は、機能を使う理由を説明でき、実装・レビューが楽になることです。全体として「API の入力、処理、保存、認証、観測、失敗を実装し、それを適切な範囲のテストで確かめる」が最終到達像と推定します。勉強会の総時間、社内アーキテクチャ、受講者の実際の経験、実施回数は不明です。以下の時間案は予定の断定ではありません。

## 3. 総合評価

| 評価軸 | Score | 主な理由 |
|---|---:|---|
| 技術的正確性 | 4/5 | 主軸は正しく、主要完成サンプル28テストが成功。実行を止める3件とログ設定等の前提不足が残る |
| 教育設計 | 3/5 | 到達目標と確認点は良い。途中状態の成立、章間ナビ、長いページの講義単位に改善が必要 |
| 文章・説明品質 | 4/5 | 具体的な入力・期待結果・注意書きが豊富。復習の重複、断片例の識別、長いコードは整理余地がある |
| サイト全体の一貫性 | 3/5 | 前提の案内はあるが JDK と配布物、JUnit 呼称、実際の Rspress 起動版、学習順とサイドバーが揃わない |
| 実務への適合性 | 4/5 | SQL バインド、ロールバック、内部情報保護、CSRF、実 DB テストの限界を説明。失敗伝達・ログ入力・本番認証の補足が必要 |

単純平均は **3.6/5**。集計上の参考値であり、High の解消を点数より優先します。

### 技術的正確性

Bean と DTO、MVC 入力検証と JSON 読み取り、プロキシを通るトランザクション、MockMvc と実サーバー、本番 DB と H2 の違いを正しく切り分けています。Security 7 の相対ログインリダイレクトを考慮した `/login` の期待値も、実行で確認できました。主な不足は完成コードの整合性と途中段階の再現性です。未実行サンプルまで「動く」とは判定していません。

### 教育設計

「なぜ必要か → 実装 → 成功・失敗の観察 → 仕組みの整理」の設計は有効です。基礎と発展を分ける注記も良いです。一方、テストの順序で未定義の型を使う、発展的なテスト構文が長い一括コードになる、導入の受講順とサイドバーの継続順が異なる点は講師の補助を必要とします。

### 文章・説明品質

期待する HTTP ステータス、本文、DB 件数、ログが具体的で、読者が成功を判断できます。文章 lint は成功し、見出しの階層飛びもありません。文章そのものの細かな修正より、断片・完成形・演習用の区別と、画面で説明する部分の絞り込みが効果的です。

### サイト全体の一貫性

プロジェクトを分けること、8080 の競合、Bash の前提、DB の寿命は導入と各章で整合しています。エラー応答の3形式は別プロジェクトであり、例外処理章でも互換性に言及しているため、単純な矛盾ではありません。一方、JDK 前提と ZIP は明確な不一致です。JUnit とサイト実行版の名称・バージョンも整理が必要です。

### 実務への適合性

トランザクション外の副作用、自己呼び出し、テストのトランザクションが設定漏れを隠すこと、CSRF と権限不足の分離は実務に直結します。ログの「良い例」で失敗をどう伝えるか、入力をどう安全に記録するか、認証サンプルの本番化前に確認することを近くに補足すると、模倣のリスクを減らせます。

## 4. Critical Issues

**0件**。確認範囲で、中心となる学習内容を誤らせる重大な誤り、破壊的コマンド、重大なセキュリティ事故に直結する無説明の手順は見つかりませんでした。これは全コード・本番環境の安全性を保証するものではありません。サンプルのコンパイル失敗は指定された基準の High に分類しています。

## 5. High Priority Issues

### H-01: Advice テストの import 漏れで全テストがコンパイルできない

- Severity: **High**
- File: `docs/SpringBootテスト入門/バリデーションテスト.md`
- Section / Heading: Advice のテスト
- Line: **579–603、652**
- Category: A 技術的正確性 / E コードサンプル
- Issue: 完成クラス `ApiExceptionHandlerTest` が `StandardCharsets.UTF_8` を使用しているのに、`java.nio.charset.StandardCharsets` の import がない。Controller テストの方には487行で import がある。
- なぜ問題なのか: Java は別ファイルの import を共有しない。本文の `./mvnw test` はテスト全体をコンパイルするため、このクラスを配置すると実行前に停止する。Java 21 / Boot 4.0.7 の抽出プロジェクトで `StandardCharsets` のシンボル解決失敗を再現した。
- Suggested improvement: 当該完成コードに import を追加する。本文の指定版で、DTO・Controller・Advice をまとめて `test` する確認を追加する。既存 Controller テストの import をコピーして済ませず、各掲載クラスを独立に確認する。
- 根拠: 本文・コンパイル結果。[Java 21 StandardCharsets](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/charset/StandardCharsets.html)

### H-02: Java 21 の準備では Security 配布プロジェクトを起動できない

- Severity: **High**
- File: `docs/SpringSecurity入門/Vol1.md`（Vol2 も継続使用）
- Section / Heading: 前提知識と到達目標 / ベースプロジェクト
- Line: **20、64–68**（Vol2: **19–23**）
- Category: A 技術的正確性 / B 前提 / E 依存関係 / G バージョン整合
- Issue: 本文は Java 21 を用意すると案内するが、リンク先 v1.0.0 ZIP 内の `pom.xml` は `<java.version>25</java.version>`。Boot は4.0.2、MyBatis は4.0.1であり、JDK 前提だけが本文と一致しない。
- なぜ問題なのか: Java 21 のコンパイラーは release 25 を扱えない。配布 ZIP を変更せず JDK 21.0.9 で Maven compile し、`release version 25 not supported` を再現した。第1回の開始時点で止まり、第2回にも進めない。
- Suggested improvement: 教材がサポートする JDK を決め、本文と配布物を一致させる。Java 21 を維持する場合は対応する配布版を用意するか、必要な pom の読み替えを明示する。JDK 25 を使う場合は導入にも例外として記載し、`java -version` / Maven が実際に使う JDK の確認を載せる。今回のレビューでは ZIP / pom を修正しない。
- 根拠: [教材が指定する配布 ZIP](https://github.com/mikoto2000/spring-boot-security-workshop/releases/download/v1.0.0/spring-boot-security-workshop-base.zip) の実内容と実ビルド結果。

### H-03: Service テストの時点では UserMapper がまだ定義されていない

- Severity: **High**
- File: `docs/SpringBootテスト入門/テスト入門.md`
- Section / Heading: Service のテスト → Repository のテスト
- Line: **254、268、318、372、383–412**
- Category: B 学習順序 / E 実行可能性
- Issue: `UserService` とテストが `UserMapper` を参照し、372行で実行を指示するが、その型の作成は後の383行から。共通クラスの節には `User` と `DateTimeUtil` しかない。
- なぜ問題なのか: Mockito でモックにする型もコンパイル時に存在する必要がある。`-Dtest=UserServiceTest` は実行対象の絞り込みであり、未定義の production クラスを無視しない。掲載順の Service 段階を抽出して、repository package / `UserMapper` のシンボル不足を再現した。全クラスを配置した完成状態では5件が成功するため、完成形の検証だけでは見逃す。
- Suggested improvement: 共通準備に Mapper のインターフェースを先に置くか、Service 節の冒頭に後の定義を先に作る手順を明記する。テストを学ぶ順序は維持しても、コンパイルに必要な型の準備順は分ける。各途中チェックポイントで実行可能か確認する。

## 6. Medium Priority Issues

### M-01: 独自 Logback XML を残したままでは後半のファイル設定例が成立しない

- Severity: **Medium**
- File: `docs/SpringBoot入門/ロギング入門.md`
- Section / Heading: MDC を表示するように Logback を設定 / Spring Boot でのロギング設定
- Line: **509–526、826–869**
- Category: A 設定の適用条件 / E サンプル整合性 / G 前後の前提
- Issue: 前半の `logback-spring.xml` は CONSOLE のみを root に接続する。後半では `logging.file.name` と rollingpolicy を示すが、独自 XML を退避する指示は JSON 出力の1042行までない。また、日次ローテーションには XML の直接編集が必要と読めるが、Boot の既定ファイル設定も日付とサイズを使う。
- なぜ問題なのか: YAML の file 名を設定しても、前半の XML がファイルアペンダーを生成・接続するわけではない。受講者が同じアプリに追記すると `logs/app.log` が作られず、設定の仕組みを誤解する。日次とサイズを排他的な方式と捉えるおそれもある。
- Suggested improvement: 後半を「標準ログ設定へ戻した場合の例」と明記し、退避 → YAML 設定 → ファイル生成 / 回転確認の手順を示す。独自 XML を使い続ける例なら file-appender の接続も説明する。既定は時間・サイズの組み合わせであり、追加要件に XML が必要と説明する。
- 根拠: 掲載 XML。[Spring Boot Logging](https://docs.spring.io/spring-boot/4.0/reference/features/logging.html)

### M-02: 「例外ログの良い例」が例外を握りつぶす形で終わる

- Severity: **Medium**
- File: `docs/SpringBoot入門/ロギング入門.md`
- Section / Heading: 実践的なロギングパターン / 例外ログ
- Line: **878–891**
- Category: A 前提 / E コード例 / G 例外・トランザクション章との整合 / H 実務
- Issue: catch でログを記録するだけで終了し、再送出・失敗応答・回復の判断がない。「良い例」とだけ示すため、処理の失敗を正常終了へ変える使い方も推奨しているように見える。
- なぜ問題なのか: トランザクション章は失敗を外に伝える重要性を説明している。同じ形を transactional な Service に持ち込むと、期待するロールバックやエラー応答を妨げる場合がある。単なるログ API の断片としてなら成立するが、前提が必要。
- Suggested improvement: 「ログ呼び出しの断片」と明記し、再送出する場合、回復して継続する場合、最終境界で応答へ変換する場合を区別する。例外処理章へのリンクと二重ログの注意を添える。

### M-03: 許可された商品名が取得用パスとして成立しない

- Severity: **Medium**
- File: `docs/SpringBoot入門/例外処理・エラー応答入門.md`
- Section / Heading: DTO / Service と Controller の作成
- Line: **77–78、167–174**
- Category: A 境界条件 / E API サンプル / H 実務
- Issue: 商品名は空白拒否と50文字以内だけで、`A/B` も登録可能。しかし展開後の `.encode()` はパスの `/` を区切りとして残すため、Location は `/products/A/B` になる。GET の `{name}` は単一セグメントである。
- なぜ問題なのか: 掲載の通常例 `Book` は通るが、入力検証を通る名前で作成→取得の契約が成立しない。Boot 4.0.7 の URI 生成テストで `/products/A/B` を確認した。実サーバーでの encoded slash 対応まで検証したわけではない。
- Suggested improvement: 演習用の商品名の許可文字を明示して制約を合わせる、あるいは URL に安定した ID を使う。URI 変数の encoding を説明する場合は展開前の builder encode と展開後の component encode の違いを示し、予約文字を含む値のテストを追加する。単に `%2F` にすればすべてのサーバーで解決するとは説明しない。
- 根拠: 本文・URI 生成の実測。[Spring URI Encoding](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-uri-building.html)

### M-04: 長い完成コードと長大なページが画面共有の説明単位を大きくする

- Severity: **Medium**
- File: `docs/SpringBootテスト入門/バリデーションテスト.md`、`docs/SpringBootテスト入門/DBを含む統合テスト入門.md`、`docs/SpringBoot入門/例外処理・エラー応答入門.md`、`docs/SpringBoot入門/ロギング入門.md` など
- Section / Heading: DTO のバリデーションテスト / 共通 API テスト / 共通エラー応答 / 座学
- Line: **273–442、132–262、218–317、653–1085**（各ファイル順）
- Category: B 粒度 / C 画面共有 / I 情報量
- Issue: 最長コードは168行、DB 共通テスト129行、例外ハンドラー98行。ロギングは1,085行、テスト入門923行、Security 2は880行ある。
- なぜ問題なのか: コード全体は自習に有用だが、説明対象と周辺の準備が同じ画面に収まらず、講師・受講者が現在位置を合わせにくい。ブラウザーでの実際の視認性は未検証であり、ソース量と生成 HTML の構造からの判断。
- Suggested improvement: 到達点ごとに「今説明する10～25行の抜粋 → 小さい動作確認 → 完成コード参照」を用意する。完成コードは保持し、重いテストの共通化、AOP、詳細用語集を発展参照に回す。章冒頭の対応表は維持する。

### M-05: 導入の学習順と一覧・サイドバーの継続順が一致しない

- Severity: **Medium**
- File: `docs/introduction.md`、`rspress.config.ts`
- Section / Heading: 学習の進め方 / コンテンツ一覧 / sidebar
- Line: **25–40、64–77**（導入）、**34–49**（設定）
- Category: B ページ順序 / F ナビゲーション / G 横断整合
- Issue: 学習手順では Security 1・2 の後に Security テスト、DB アクセスの後に DB 統合テスト。しかし一覧とテストグループではこれらの発展テストが基礎テストと連続して並ぶ。
- なぜ問題なのか: 各ページに前提リンクはあるため致命的ではないが、サイドバーや前後移動をそのまま使うと未学習の Security / DB へ進む。3グループを横断する道筋が本文の手順に依存している。
- Suggested improvement: 受講経路を「基本コース」と「関連前提を終えた発展テスト」に分けて案内し、各終端に次に読むページを明記する。後続の修正でナビを検討する際は、テーマ分類と受講順序を混同しない。今回サイドバーは変更しない。

### M-06: Rspress の宣言版と実際に起動する CLI が一致しない

- Severity: **Medium**
- File: `DEVELOPMENT.md`、`package.json`、`package-lock.json`
- Section / Heading: セットアップ / build
- Line: **15–19、32–38**（DEVELOPMENT）、**6、13、18**（package.json）
- Category: A バージョン依存 / F Rspress / G 再現性
- Issue: `rspress 1.47.0` と core 2.0.1 が同居し、`npm ci` 後の `.bin/rspress.cmd` は `@rspress/core/bin/rspress.js` を指す。build の表示は v2.0.1。元 checkout の既存依存を参照して v1 CLI を直接起動した試行は、ESM 専用 rst-directives の読み込みで失敗した。
- なぜ問題なのか: 設定・Markdown の対応を「Rspress 1 のサイト」として説明すると検証対象を取り違える。今回のクリーン build 成功は、v1 でも成功する根拠にはならない。手元と CI で依存インストール方法も npm ci / npm install と異なる。
- Suggested improvement: サポートする Rspress メジャーとプラグイン互換性を決め、起動版とインストール手順を記録する。更新・依存整理は別作業とし、今回の報告には実際の起動版を明記する。CI で clean install → version → build を確認する。

### M-07: 業務ログに利用者入力をそのまま書く例の対策が近くにない

- Severity: **Medium**
- File: `docs/SpringBoot入門/ロギング入門.md`
- Section / Heading: 業務ログを完成させる
- Line: **616–632、727、1027–1028**
- Category: A セキュリティ前提 / H 運用
- Issue: 無制約の `name` をテキストログへ出力する。後半に機密情報・改行の一般的な注意はあるが、実装例と結び付いていない。
- なぜ問題なのか: URL エンコードされた改行などを含む入力で複数行のように見えるログを作れて、イベント境界や調査を混乱させる。SLF4J の `{}` は文字列連結を避ける機能であり、入力の改行や機密性を自動で制御する機能ではない。
- Suggested improvement: 演習入力の長さ・文字の制限、制御文字の扱い、名前の代わりに内部 ID を記録する判断をこの節で説明する。構造化ログは値の escaping に役立つが、機密情報の除外・記録方針は別途必要と示す。

### M-08: 理論側の Java / XML 断片が実行可能コードに見える

- Severity: **Medium**
- File: `docs/SpringBoot入門/ロギング入門.md`、`docs/SpringSecurity入門/Vol1.md`
- Section / Heading: SLF4J の使用例 / MDC の使用例 / フィルターでの MDC 利用 / UserDetailsService の役割
- Line: **786–792、945–958、977–999**（ログ）、**499–506**（Vol1）
- Category: D 読者の次の行動 / E 完成形と抜粋
- Issue: `RequestIdFilter.java` のタイトル付きクラスには import がなく、SLF4J 例はクラス外の文、XML は pattern の断片。Vol1 の UserDetailsService の復習例は戻り値がない。テスト入門46行のような包括的な断片説明がこれらにはない。
- なぜ問題なのか: ハンズオンでは完全なクラスをコピーする形式のため、後半でも同じように試す読者がコンパイルや XML 配置で止まる。抜粋そのものは誤りではないが、利用方法の明示が必要。
- Suggested improvement: 各例に「既存クラス内の抜粋」「概念図に相当する擬似コード」「XML の要素だけ」を付ける。試すための実装は前半の完成クラスへリンクし、未定義変数を自分で補う必要があると案内する。

### M-09: 削除のトランザクション境界は成功ケースだけでは検証できない

- Severity: **Medium**
- File: `docs/SpringBootテスト入門/DBを含む統合テスト入門.md`
- Section / Heading: 共通の API テスト / 練習
- Line: **245–260、380–382**
- Category: E テストの検証範囲 / H 実務
- Issue: `delete` にも `@Transactional` を付ける前提だが、削除テストは両 SQL が成功するケースのみ。設定を外して失敗を観察する練習は `createWithItem` のみ。
- なぜ問題なのか: 削除はトランザクションなしでも正常系が成功するため、このテストが通っても削除途中の原子性は確認できない。「最終 API のトランザクションをすべて確認した」とは言えない。
- Suggested improvement: 本文に削除途中の失敗は範囲外と明記するか、テスト用に2件目が失敗する条件を用意し、明細も元に戻ることを検証する発展課題を置く。正常 CRUD と境界設定のテストを区別する。

### M-10: 並行開発で小さいバージョンが後から届く場合の挙動が不足する

- Severity: **Medium**
- File: `docs/SpringBoot入門/DBマイグレーション入門.md`
- Section / Heading: 並行開発に向けたマイグレーション運用例
- Line: **193–228**
- Category: A 条件 / H 運用 / I 発展の説明
- Issue: 日付・連番と draft 運用、重複と適用順の確認はあるが、すでに適用された最大バージョンより小さい SQL が後でマージされる具体例がない。
- なぜ問題なのか: 名前が衝突しなくても、既定の outOfOrder=false では後から追加した低い版を通常順に適用できず、履歴・検証上の問題になる。draft から移す方式だけでこの問題は解消しない。
- Suggested improvement: 「V2 適用済み環境へ V1.5 が追加される」例を示す。確定時の採番、共有環境の適用順、CI での履歴検証のルールを説明する。outOfOrder / repair を安易な回避策として推奨せず、共有環境の運用判断と位置付ける。
- 根拠: [Flyway Out Of Order](https://documentation.red-gate.com/fd/flyway-out-of-order-setting-277579015.html)

### M-11: 受講単位と基本・発展の所要量が一覧から判断しにくい

- Severity: **Medium**
- File: `docs/introduction.md`、長いハンズオン各章
- Section / Heading: 学習の進め方
- Line: **25–45**（導入）
- Category: B 教育設計 / C 講師の説明 / I 時間配分
- Issue: 学習順と発展項目の案内はあるが、1回の範囲、事前準備、講義と実装の区切りはない。全体は7,982行で、個別プロジェクトのセットアップも複数回必要。
- なぜ問題なのか: 全サイトを1回で進めるか複数回で進めるかを読者が判断できない。内容の価値より、セットアップやスクロールに時間を使うおそれがある。予定時間は不明なので不足・過剰を断定しない。
- Suggested improvement: 「1回の基本到達点」「事前準備」「残りを自習に回す場合」を章ごとに設定し、後述の60 / 90 / 120分案を講師用の目安として検討する。全文削減より、講義で扱う範囲を明示する。

### M-12: 認証サンプルを実務へ移す際の最低限の確認が一か所にない

- Severity: **Medium**
- File: `docs/SpringSecurity入門/Vol1.md`、`docs/SpringSecurity入門/Vol2.md`
- Section / Heading: ユーザー情報 / ユーザー登録 / まとめ
- Line: **169–175、518–536**（Vol1）、**285–300、752–753、868–874**（Vol2）
- Category: H tutorial / production の区別
- Issue: 固定管理者・共通の演習パスワード、メモリー DB、入力検証省略を使う。省略や DB 寿命は説明されているが、「実務でよく見る構成のベース」というまとめの近くに、公開前に必要な確認がまとまっていない。
- なぜ問題なのか: 認証・認可の学習としては適切な最小例でも、パスワードハッシュだけで公開可能と受け取られない補足が必要。固定認証情報の除去、TLS、ログイン試行への対策、セッション・Cookie 設定、入力・パスワード方針はアプリ要件との接点になる。
- Suggested improvement: 数項目の「この演習から本番へ進む前の確認」をまとめに置き、固定ユーザーを配布例のまま公開しないことを明記する。各実装詳細の全面追加は不要。ハッシュは推測攻撃を完全に防がないこと、漏えい時の対応も一文補う。

### M-13: ペアワイズの保証範囲が説明されていない

- Severity: **Medium**
- File: `docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md`
- Section / Heading: その他テストケース設計（発展）
- Line: **244–250**
- Category: A 過度な単純化 / B 学習 / D 用語
- Issue: 「複数パラメータの組み合わせを効率的に網羅する」とだけあり、2因子の値の組を対象にする意味や、3因子以上の相互作用をすべて保証しないことがない。
- なぜ問題なのか: 「全組み合わせの代わりに同じ範囲を保証できる」と読み違える余地がある。権限×プラン×機能フラグの例では、まさに3条件以上の業務ルールが重要な場合がある。
- Suggested improvement: 2因子の組をカバーする方式であることを1文で定義し、重要な多条件の業務ルールは別のケースとして足すと補足する。小さな組み合わせ表を発展参照に置く。

## 7. Low Priority Issues

### L-01: Boot 4.0 の依存関係に対して JUnit 5 の呼称が残る

- Severity: **Low**
- File: `docs/SpringBootテスト入門/テスト入門.md`
- Section / Heading: 概要 / JUnit 5 とアサーション / 用語集
- Line: **9、691–720、904、921**
- Category: G バージョン表記 / D 用語
- Issue: Boot 4.0 系は JUnit 6 を使う一方、本文・見出し・参考資料が JUnit 5 に固定されている。695行では pom に従う注記があり、API は Jupiter のため掲載コードは動く。
- なぜ問題なのか: 読者が依存ツリーと説明の名称を照合しにくい。Java の import が `org.junit.jupiter` のままでもメジャーバージョンは同じとは限らない。
- Suggested improvement: 見出しを JUnit Jupiter とし、検証した Boot / JUnit の対応を注記する。JUnit 5 固有の説明として残すなら対象版を明示する。
- 根拠: 実行環境の `junit-platform-launcher-6.0.3`。[Boot 4.0 Testing](https://docs.spring.io/spring-boot/4.0/reference/testing/index.html)

### L-02: sitemap が教材ページではなく GitHub の repository ホストを指す

- Severity: **Low**
- File: `rspress.config.ts`
- Section / Heading: pluginSitemap
- Line: **10–12**
- Category: F 外部参照 / サイトメタ情報
- Issue: siteUrl が `https://github.com/mikoto2000/Practical-Introduction-to-Spring-Boot/`。生成 sitemap には、この後ろに教材のページ名が付いた URL が15件並ぶ。
- なぜ問題なのか: GitHub repository の URL は教材の公開ページの URL ではない。通常の本文リンクは正常でも、検索・サイト登録では教材の参照先が誤る。
- Suggested improvement: 公開先を確認し、GitHub Pages 運用なら対応する Pages ホストに合わせる。生成 sitemap の loc と公開ページの一致を検証する。今回、公開サイトの全ルート疎通は未検証。

### L-03: 実行コマンドと期待出力が同じ Bash fence に入る

- Severity: **Low**
- File: `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md`
- Section / Heading: プロジェクトの実行 / ビルドとデプロイ
- Line: **283–286、398–401**
- Category: E コピペ可能性 / F code fence
- Issue: curl と `=> {"age":36}` が同じ bash ブロックにある。
- なぜ問題なのか: まとめてコピーすると出力説明もシェルへ渡される。年齢が実行日に依存する注記は正しく、そこは維持したい。
- Suggested improvement: 実行用 bash と出力用 json / text を分離するか、出力行をシェルコメントにする。

### L-04: 非コード fence の言語とテスト図の代替テキストを補える

- Severity: **Low**
- File: `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md`、`docs/SpringBoot入門/DBマイグレーション入門.md`、`docs/SpringBoot入門/ロギング入門.md`、`docs/SpringSecurity入門/Vol1.md`、`docs/SpringBootテスト入門/テスト入門.md`
- Section / Heading: ディレクトリ構成 / ログ出力 / 題材とテスト対象図
- Line: **75、331、366**（プロジェクト）、**130、201**（Flyway）、**262、387、414、540**（ログ）、**123**（Vol1）、**95、104、147、245、379、492**（テスト）
- Category: F Markdown / D アクセシビリティ
- Issue: 無言語 fence が合計10箇所あり、テスト対象 PNG は alt が空。これらは Java の未指定 fence ではなく、tree・ログ・図式などである。
- なぜ問題なのか: レンダリングは成立しているが、text 指定で意味が分かり、図の alt があると読み上げや画像が見えない環境でも対象層を理解できる。
- Suggested improvement: 非コードには text を使い、図には「Controller → Service → Mapper → DB、Service は日付と年齢計算 Util も利用」など短い説明を付ける。詳細な図をすべて文章に重複させる必要はない。

## 8. ファイル別レビュー

以下は全公開15ファイルの確認記録です。「追加指摘なし」は全文通読後の判断であり、実行確認済みを意味するものではありません。指摘件数は5〜7節で一度だけ数えます。

### docs/index.md

ホーム frontmatter と CTA の参照先は正常。15行で目的と入口が明確。追加指摘なし。API 作成から入力検証・テスト・ログ・認証へ進む短い紹介は維持したい。DB とエラー処理は導入一覧で補われるため、ホームへ全項目を詰め込む必要はない。

### docs/introduction.md

77行。Java / HTTP / SQL / Docker、別プロジェクト、Shell、8080、再起動の扱いを確認。前提説明はサイト全体の強み。M-05 / M-11 により、受講順とナビ・実施単位を合わせるとよい。4.0系の指定版と最新安定版を区別する注記は維持し、Security の Java 25 例外を解消する。

### docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md

426行。DTO / Service / Controller、DI、日付変換、profile group、環境変数、jar 起動を通読。未来日が現段階では500、年齢が実行日に依存、DB profile はグループに追加が必要という補足は有用。L-03 / L-04。jar のローカル起動とサーバー常駐化を区別しているため、タイトルの「デプロイ」を本番運用の完了とは誤認させにくい。Java 実行は未確認。

### docs/SpringBoot入門/バリデーション入門.md

675行。単純引数 / DTO、null 方針、AssertTrue の getter 制約、Integer 用 RangeEx、i18n、3種類の例外を確認。Controller にクラスレベル @Validated を付けない理由、JSON 読み取りと制約違反を区別する説明は維持したい。独自制約とメッセージは発展へ回す方針が明確。80行の Advice は M-04 に準じて主要ハンドラーごとに説明するとよい。戻り値違反を一律400にしない注意はあるが、サンプル自体は入力専用なので、再利用時の適用範囲を守る。Java 実行は未確認。

### docs/SpringBoot入門/ロギング入門.md

1,085行。Filter / AOP / MDC / Service、レベル、SLF4J / Logback、ファイル・JSON、監査・例外ログを確認。M-01 / M-02 / M-04 / M-07 / M-08、L-04。Filter が記録する時点の200と最終500を区別し、success を HTTP 成否と分ける注記は正しい。UUID は分散 trace ではない、MDC は別スレッドへ自動伝播しない説明も維持。ハンズオン同期処理の範囲を外れる話は Appendix に適する。Java 実行は未確認。

### docs/SpringBoot入門/DBマイグレーション入門.md

243行。H2 file / Flyway / naming / history / ALTER / draft を確認。target 配下の DB が clean で消える注意、適用済み SQL を編集しない原則、expand / contract の導入は実務向き。M-10、L-04。基本演習は短く、Flyway の履歴を再起動前後で見る構成がよい。「DB を壊さず」は破壊的 DDL の安全性まで保証する意味ではないことを講師が補足するとよい。起動・DDL の実行は未確認。

### docs/SpringBoot入門/DBアクセス・トランザクション入門.md

538行。DTO / Mapper / CRUD / 生成 ID / CHECK 制約 / 二段階保存 / @Transactional / proxy / rollback / concurrency を確認。完成 Service と Mapper は H2 統合テストに接続して5件成功。入力で quantity を制限しない理由と後の @Positive 練習が明確。SQL 値バインド、ID 欠番、catch 後の DB 状態、外部 API の副作用の注意は維持。M-09 に関連して、削除途中失敗まで確認したわけではない。独立した追加 High なし。

### docs/SpringBoot入門/例外処理・エラー応答入門.md

619行。memory 商品 API / concurrency / exceptions / Advice / ProblemDetail / headers / logging / MockMvc を確認。掲載6件の契約テストが成功。M-03 / M-04。405 の Allow、固定した利用者向けメッセージ、調査 ID、Security Filter が Advice の外にある説明は良い。単なる Exception.class の全面捕捉で終わらず MVC 例外のステータスを維持する設計を保持したい。

### docs/SpringBootテスト入門/テスト入門.md

923行。Util → Service → Mapper → Controller と後半理論を通読。完成形は5テスト成功、Service 段階は型不足で失敗（H-03）。M-04、L-01 / L-04。日付を DI で固定し、Spring を使わないテストと slice を段階的に分ける例は教材として良い。単体の依存は「必要に応じて」モックとする表現を、後半の表・説明でも揃えると、モックを必須と誤解しにくい。

### docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md

267行。実施者 / テストレベル / 狙い / ブラック・ホワイト / AAA / 同値・境界 / 状態・ペアワイズを確認。M-13。図は代表値と境界、null・型変換の別対象を示し、本文とも一致する。「ホワイトで不足を見つけ、仕様の振る舞いで期待結果を書く」「assert が1個という意味ではない」は維持すべき。設計の基礎を先に読みたい経路を導入が認めていることも良い。

### docs/SpringBootテスト入門/バリデーションテスト.md

706行。DTO / mock Controller / Advice / direct Validator / parameterized / MVC / standalone を確認。H-01 / M-04。DTO・HTTP・応答形式の責務分離と、username の空文字で2件、順序固定しない説明が良い。正常・異常・境界の大きな完成テストを小分けで説明すると講義に使いやすい。元の掲載コードは testCompile 失敗なので成功と判定していない。

### docs/SpringBootテスト入門/SpringSecurityテスト入門.md

439行。SecurityConfig / anonymous / roles / CSRF / form login / session / logout を通読し、掲載12件がすべて成功。独立した機能不良の指摘なし。USER の拒否には有効な CSRF、CSRF 失敗には ADMIN を使う分離は特に良い。@WithMockUser と実際のパスワード照合、セッションの明示的受け渡し、HTML / Cookie の検証は範囲外という説明も維持。前提章とのナビは M-05。

### docs/SpringBootテスト入門/DBを含む統合テスト入門.md

480行。H2 / HTTP と DB / 初期化 / rollback / container / service connection / lifecycle を通読。H25件は成功。M-04 / M-09。PostgreSQL テストは Docker がないため未実行。H2と同じ結果を本番 DB とみなさない、テスト @Transactional が境界漏れを隠す、ID を1に固定しない注意は良い。PostgreSQL の module / package と Testcontainers 2系の区別もある。抽象テストの継承は初読では発展として説明するとよい。

### docs/SpringSecurity入門/Vol1.md

609行。ZIP、generated user、独自 UserDetailsService、認可、logout、password / filter chain を通読。H-02、M-08 / M-12、L-04。固定ユーザー例そのものを本番推奨と断定はしない。ログイン状態による再確認方法、Controller と RestController の違い、GET logout 確認と POST 実行の区別は良い。配布 ZIP の未変更ビルドは Java 21 で失敗し、ハンズオン全体を成功とは判定していない。

### docs/SpringSecurity入門/Vol2.md

880行。Vol1からの変更、自作HTML / th:action / DB users / UserDetails 変換 / encoder / signup / rolesを通読。H-02を継承、M-04 / M-12。View 非表示だけでは認可できない、直接 URL で403を確認する、管理者登録後はログアウトして新規ユーザーで確認する手順は良い。固定 USER ロールで受け取り、利用者入力で ADMIN を付与しない構成は保持。入力検証省略と Service への移行先も示されている。ブラウザーでの自作フォーム確認は未実施。

### 補助資料: README.md / DEVELOPMENT.md

README の対象者と導入リンクは本文と整合。DEVELOPMENT は npm ci / build / textlint / preview と、Java は別途確認が必要であることを明記している。build に対する M-06 が関連する。npm run check / format は書き込みを伴うのでレビューでは実行していない。Node の前提バージョンは CI と合わせて明記できる。

## 9. ページ横断の問題

全文通読後に、プロジェクト名・package・port・dependency・エラー形式・用語・順序を再比較しました。

### 用語・表記

- JUnit 5 / Jupiter / 実体 JUnit 6 は L-01。MockMvc の package と @MockitoBean は Boot 4系に合い、主要実行サンプルでも確認した。
- テスト入門は結合を広い統合として扱い、設計編は両者を分けるが、それぞれ定義に断りがあるので矛盾ではない。講師は最初にこのコースの用語対応を示すとよい。
- Spring Bean と Bean Validation の DTO はバリデーション章62行で区別される。用語を一律に置換する必要はない。
- `traceId` / `requestId`、`user` / `userId`、local / dev、application.yaml / yml、Vol1 / 第1回は各例の文脈で成立するが、断片例を移植する際はキーを合わせる。独立した重大不整合ではない。

### 重複

ロギングのレベル、Security の認証・認可、テストのレベル・重要性は前半と後半に意図的に再登場する。ハンズオンと復習の対応表は維持し、定義全文を繰り返すより「先ほど見た現象をこの概念で説明する」問いへ置き換えると講義時間を抑えられる。テスト入門後半の同値・境界の深掘りは設計編へ参照を渡せる。

### 矛盾

- 真の実行上の不一致: Security の JDK 21 vs ZIP 25（H-02）。サイトの宣言と CLI 実体（M-06）。JUnit 呼称（L-01）。
- 例外・DB 章で失敗を伝える原則と、ロギングの catch-only 例は読み方を補足する必要がある（M-02）。
- ValidationErrors / ApiErrorResponse / ProblemDetail は別演習であり、単純な同一 API の矛盾ではない。例外章553–554行の互換性説明を保持する。
- 4.0.2 / 4.0.3 / 4.0系、MyBatis 4.0.0 / 配布版4.0.1は章ごとに題材が異なる。更新漏れと断定せず、検証環境表で区別する。
- port 8080は共通で導入に停止の説明がある。DB の file / memory の差も本文で説明される。異なる package / project名を統一する必要はない。

### 前提知識

SQL の基礎、HTML、Docker は必要な章に案内がある。ただし、Mapper の型は実行指示より後（H-03）。@Nested / MethodSource / abstract superclassは文中である程度説明されるが、初心者向けの小さい例を先に置くと理解しやすい（M-04）。Security ZIPは生成プロジェクトと異なるので前提表を別に置く。

### ページ順序

受講順と分類順は M-05。テスト設計は任意で先に読めるため、単純に順序を逆転する必要はない。DB 統合・Security テストの最初にある前提リンクは維持し、テスト入門を終えた直後の分岐先を明示する。

## 10. 不足している説明

優先して補うのは次の点です。

- コピー後の「この時点で存在するファイル」と、次の実行を可能にする最小型（H-03）。
- 章別の実測環境: Boot / JDK / MyBatis / Jackson / JUnit、配布 ZIP の版。最新版という意味ではなく検証条件を記録する。
- ログ設定の標準・独自 XML の切り替え条件、日付とサイズの回転、例外捕捉後の失敗伝達（M-01 / M-02）。
- 入力で許可する値と URL で表現できる値の整合（M-03）。
- ログに利用者の入力を記録するときの制御文字・機密性・長さの判断（M-07）。
- マイグレーションの非衝突と順序適合は別問題（M-10）。
- Securityを本番へ進める際の固定ユーザー除去と要件確認（M-12）。
- 各テストが何を確認していないか。特に削除途中失敗、実HTTP / browser / Cookie、PostgreSQL未実行の扱い（M-09）。

追加する項目をすべて基礎講義へ入れる必要はありません。短い注意と参照先にとどめ、実行を止める前提だけは本文で完結させるべきです。

## 11. 図・コード例・デモを追加するとよい箇所

| 対象 | 追加するとよいもの | 受講者に確認させること |
|---|---|---|
| プロジェクト / DI | Controller → Service → DTO、Spring container の小図 | 自分で new する対象と Spring に渡される対象の違い |
| バリデーション → 例外 | JSON parsing → DTO validation → Service → Advice | 壊れた JSON と制約違反は段階も例外も違う |
| ロギング | 同じ request ID のログ3行、エラー dispatch の時系列 | Filter 時点の status と最終応答、MDC の寿命 |
| DB / トランザクション | 2 INSERT の成功・途中失敗の図、proxy の入口 | @Transactional有無でどの行が残るか |
| 例外応答 | Service rollback → Advice → ProblemDetail の流れ | ロールバックと HTTP 変換は別の責務 |
| テスト設計 | 少数の入力表→ParameterizedTestの10行 | 固定期待値と boundary の対応 |
| Security 1・2 | Filter → provider → UserDetailsService → PasswordEncoder | 取得元の差し替えとパスワード照合を区別する |
| Security テスト | anonymous / USER / ADMIN × method × CSRF の結果表 | 同じ403でも拒否理由が違う |
| DB 統合 | API の HTTP 結果と DB 行を並べるデモ | 応答成功だけでは保存・原子性を保証しない |

既存のテスト対象層の6枚の図、同値分割・境界値図は適切です。新しい図を増やす前に、これらを説明の節目として使い、コード全体のスクロールを減らすのがよいです。

## 12. 削減または Appendix 化を推奨する内容

基本の実行コードは削除せず、講義の本筋と参照を分けます。

- ロギング: Pointcut の派生式、Log4j2 選択の説明、監査・構造化ログの詳細、後半用語集。基本はアクセス / 業務 / MDC の観測まで。
- バリデーション: RangeEx の全アノテーション・Validator、多言語メッセージ一覧。基本は @Valid、必須・サイズ、共通400応答。
- テスト入門: 後半の目的・レベルの繰り返しと設計技法の詳細は設計編へ参照。Mockito reset の表は必須操作と受け取られない補足を添える。
- バリデーションテスト: @Nested と詳細コメントの多い168行の完成版は参照へ。最初は正常1件と境界の最小セットを説明する。
- DB 統合: abstract test class による共通化と PostgreSQL / Testcontainers は H2 で成功・失敗を確認した後の発展。
- Flyway: draft / 並行開発は基本の履歴確認後。運用条件は省略せず、別セッションへ回せる。
- Security: 各段階のHTML全文は参照として維持し、講義では変更点と SecurityConfig・処理の流れを中心にする。

### 予定時間が不明な場合の実施案

以下は**1回のテーマ別勉強会の目安**であり、全15ページをこの時間内に完了する案ではありません。PC・依存関係・Docker等の準備時間は別に確保する想定です。

| 時間 | テーマ例と配分 | 自習 / 次回へ回す範囲 |
|---|---|---|
| 60分 | バリデーション: 目的・前提10、単純値 / DTO 演習25、400共通応答15、振り返り10 | RangeEx、i18n、テストの詳細 |
| 90分 | テスト基礎: 目的10、Util20、Service20、Mapper / Controller25、ケース選択と振り返り15 | 設計編の発展、Security / DB統合 |
| 120分 | DB / トランザクション: 設定15、CRUD25、途中失敗25、proxy / rollback15、H2テスト25、振り返り15 | PostgreSQL、並行更新、マイグレーション運用 |

Security は第1回・第2回に分かれた構成を維持し、環境確認・フォーム・DB・権限確認の終了条件で講師が範囲を決めます。社内の所要時間が確定したら、デモ中心か受講者実装中心かに応じて再配分してください。

## 13. 推奨する修正順序

### Phase 1: Critical / High の実行問題

- `docs/SpringBootテスト入門/バリデーションテスト.md`: H-01。
- `docs/SpringSecurity入門/Vol1.md`、`Vol2.md`、`docs/introduction.md`、外部配布物: H-02。
- `docs/SpringBootテスト入門/テスト入門.md`: H-03。

完了条件: 掲載クラス全体をコンパイルでき、受講途中のコマンドも実行でき、指定 JDK と配布物で起動できること。本文を直しただけで終えず、配布物とチェックポイントを再確認する。

### Phase 2: 学習順序・構成

- `docs/introduction.md` と設定・各章の案内: M-05 / M-11。
- ロギング、テスト、バリデーションテスト、例外、DB 統合、Security 2: M-04。

完了条件: 1回の到達点、次に読むページ、発展へ回せる部分が明確。受講者が図・小さいコード・実行結果の順に追える。

### Phase 3: 前提・説明不足・実務の具体例

- ロギング: M-01 / M-02 / M-07 / M-08。
- 例外: M-03。
- DB 統合: M-09。
- Flyway: M-10。
- Security 1・2: M-08 / M-12。
- テスト設計: M-13。
- DEVELOPMENT とサイト実行環境: M-06（依存整理は別途スコープを決める）。

完了条件: 設定が反映される前提、失敗伝達、境界値、テスト保証範囲を説明できる。追加した例は自動テストまたは手順で確認する。

### Phase 4: 表記・文章・Markdown・メタ情報

- テスト入門: L-01 / L-04。
- サイト設定: L-02。
- プロジェクト、Flyway、ロギング、Security 1: L-03 / L-04。

完了条件: バージョン名・実行ブロック・alt・text fence が明確、sitemapが公開先を指し、textlint / build / link check が成功する。

## 14. Build / Link / Markdown 検証結果

### 環境と変更管理

- 元 checkout は作業開始時 clean、branch main。HEAD から専用 managed worktree を作り、`codex/review-study-materials` を作成。最新remoteへの追従ではなく、ユーザーが示したローカル HEAD のレビューとして固定した。fetch は行っていない。
- Windows PowerShell、Node.js **24.11.1**、npm **11.6.2**。
- worktree で `npm ci --ignore-scripts --no-audit --no-fund` を実行。package / lockfile は変更なし。依存更新はしていない。
- `npm run check` は biome `--write`、`npm run format` は prettier `--write` のため未実行。修正を伴わない lint / textlint のみ実行。

### サイト・文章・リンク

| 検証 | 結果 | 詳細 / 限界 |
|---|---|---|
| npm ci | 成功 / exit 0 | peer override、glob deprecated の警告。脆弱性監査は今回のレビュー対象外 |
| npm run build | 成功 / exit 0 | 実行表示 Rspress **2.0.1**。15教材 + 404 の HTML と sitemap 15ページを生成 |
| build warning | あり | MODULE_TYPELESS_PACKAGE_JSON: config TS を ESM として再解釈する警告。致命的エラーなし |
| npm run lint | 成功 / exit 0 | ESLint。Markdown の Java コード検証ではない |
| npm run textlint | 成功 / exit 0 | 報告書作成前の既存 `docs` 15ページで実行 |
| 生成 HTML 内部リンク | 欠落0 | href1,048件。baseを正規化し対象ファイルとfragmentのid存在を比較。ナビ・目次の重複も含む |
| 生成 HTML 画像参照 | 欠落0 | src24件。logoの反復等も含み、異なる画像の数ではない |
| 本文外部リンク | 到達44 / 44 | fenced code外のMarkdownリンクを抽出、GETとredirect後の2xxを確認。リンク先の内容が教材に適切かを全件全文精査した結果ではない |
| code fence | 閉じ忘れ0 | Java / JSON / YAML 等のフェンスを点検。言語なし10件は L-04 |
| 見出し | 階層飛び0 | 1段超の飛びを検出しなかった。homeはfrontmatter主体でH1なし |
| frontmatter / MDX | build上エラーなし | MDX0件、home frontmatterと各タイトルを確認 |
| 図 | 参照先あり | テスト対象PNGの代表画像を目視、境界図SVGの構成も確認。全ページのbrowser描画は未確認 |
| sitemap | 生成成功 / URL設定要改善 | locのホストはL-02。通常の内部リンク検査とは別 |

最初の予備試行では元 checkout の既存 node_modules を NODE_PATH で参照し、v1 CLIを直接起動したところ、rst-directives の `ERR_PACKAGE_PATH_NOT_EXPORTED` で config 読み込みに失敗しました。これを最終のクリーン build 結果とは区別しています。worktree に lockfile通りインストールしてからの通常の `npm run build` は成功しました。v1を動かすための設定修正は行っていません。

### Java サンプルの検証

一時ディレクトリに Markdown の package / class を持つコードブロックを抽出し、最小の起動クラスと必要な POM / SQL を付けて確認しました。本文ソースは修正していません。抽出テストの環境は **Java 21.0.9、Maven 3.9.16、Boot 4.0.7**。MyBatis は本文と同じ4.0.0。この検証は本文のすべての指定パッチ版を保証しません。

| 教材 / 状態 | 結果 |
|---|---|
| テスト入門・全クラス完成形 | Util2、Service1、Mapper1、Controller1、計5件成功 |
| 例外処理・エラー応答の掲載テスト | 6件成功。意図した想定外例外のERRORと405のWARNは教材の挙動 |
| Spring Security テスト | AccessControl9、LoginLogout3、計12件成功 |
| DBアクセス最終Service + DB統合H2 | 5件成功。@Transactionalを本文の最終段階通り追加。意図したCHECK違反のERRORは正常なテスト経路 |
| 掲載テスト成功合計 | **28件 / failures0 / errors0 / skipped0** |
| バリデーションテスト・掲載完成形 | testCompile失敗: StandardCharsets不足（H-01）。修正して成功したとは扱っていない |
| テスト入門・掲載順Service段階 | compile失敗: 未定義UserMapper（H-03） |
| Security配布ZIP未変更 + Java21 | compile失敗: release25非対応（H-02）。配布ZIPのBootは4.0.2 |
| 追加したレビュー用URI生成テスト | 1件成功し、商品名A/BがLocationで2セグメントになることを確認（M-03）。掲載28件とは別集計 |
| PostgreSQL / Testcontainers | 未実行: Dockerコマンド / 実行環境なし |
| Security 1・2のbrowserフロー | 未実行: 配布ZIPのJDK不一致を確認する範囲まで |
| プロジェクト作成・バリデーション基礎・ログ・Flyway全手順 | 静的レビュー。起動・curl・SQL・ログの全手順は実行していない |

Mockito の動的 agent attach、JVM class sharing などの警告がありました。これらを掲載コードのコンパイル失敗とは区別しています。最初の Maven offline 試行は repository ID の解決条件で失敗しましたが、通常モードでの実行では解決し、上の結果を取得しています。

### 再実行・確認事項

今回の28テストは最小の抽出プロジェクトを使った検証です。次の修正時は、実際に受講者へ配布するプロジェクト、本文の指定パッチ版、掲載順の途中状態を使って再確認してください。リンクが200でも、Javaサンプルが動くことや図が画面で読みやすいことの根拠にはなりません。

報告書は `review/2026-10-07-review-study-materials.md` に配置しています。ユーザーの指定により、Rspress のドキュメント用ディレクトリ `docs` から移動し、ファイル名にレビュー日を付けました。教材の件数・初回build・リンクの集計は報告書追加前のbaselineです。移動前の `docs` 配下に報告書を追加した段階でも `npm run build` は exit 0 でしたが、その際の sitemap 16ページには報告書が含まれていました。現在の報告書は Rspress の root 外にあり、公開教材15ページには含まれません。

## 15. 良い点

- **前提・到達目標・実行結果が具体的**。HTTP statusだけでなく本文、DB行、ログを確認させる構成は維持すべき。
- **独立プロジェクトと継続プロジェクトを導入で明記**。packageやportの違いを無理に統一する必要がなく、題材が混ざりにくい。
- **日付依存をテストで固定する例**。DateTimeUtilをコンストラクタへ渡すServiceは、DIの学習とテストの決定性が自然につながる。
- **バリデーションのnullと制約を分ける説明**。RangeExはnullを許容して@NotNullへ任せる。必須・形式・JSON変換を区別できる。
- **テスト責務の切り分けが良い**。DTOの制約、ControllerのHTTP、Adviceの形式を分ける方針は維持したい。
- **DBの途中失敗を実際の保存結果で観察する構成**。先に成功した注文は残し、失敗した注文だけ戻ることを検証する例は教育効果が高い。
- **トランザクションの落とし穴を明記**。自己呼び出し、newによる生成、チェック例外、catch、同時更新、外部API、テスト側transactionまで関係付けている。
- **Securityテストが拒否理由を分離**。権限不足とCSRFの403を条件で区別し、パスワード照合とテスト用principalを分けている。
- **画面非表示と認可を区別**。ユーザー登録リンクを隠すだけでなく、直接URLで403を確認する手順は変更しない方がよい。
- **例外応答で内部情報を出さない設計**。調査IDとログ、405のAllow保持、Security Filterの失敗はAdviceの範囲外という説明は実務に適する。
- **H2を本番DBと同一視しない**。Testcontainers、DB製品・版、ライフサイクル、データ初期化の限界を説明している。
- **ロギングの観測時点を説明**。Filterログの200が最終200とは限らず、UUIDが分散traceではないことも明示されている。
- **設計編はテストの保守性に踏み込む**。仕様を期待値の基準にし、実装を漏れの分析に使う方針、1振る舞いと1assertを混同しない説明は維持すべき。
- **サイト・文章の検証が成立**。clean install後のbuild、lint、textlint、生成HTMLのリンクが通り、見出し・画像・内部参照の大きな破損はない。
