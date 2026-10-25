# docs Markdown 全16ページ レビュー最終報告（2026-10-24）

- 対象: `docs` 配下の Markdown 16ファイル
- 状態: 全16ページの本文評価・横断確認・確定指摘の修正・修正後検証を完了
- 履歴: 作業中の部分記録は `review/2026-10-24-review-partial.md` に保持。本ファイルが最終結論の参照先。

## 1. 全16ファイルの最終状態

| # | ファイル | 行数 | 本文評価 | 確定指摘 |
|---|---|---|---|---|
| 1 | docs/index.md | 15 | 完了 | なし |
| 2 | docs/introduction.md | 102 | 完了 | なし |
| 3 | docs/SpringSecurity入門/Vol1.md | 638 | 完了 | なし |
| 4 | docs/SpringSecurity入門/Vol2.md | 915 | 完了 | なし |
| 5 | docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md | 442 | 完了 | なし |
| 6 | docs/SpringBoot入門/DBアクセス・トランザクション入門.md | 546 | 完了 | なし |
| 7 | docs/SpringBoot入門/DBマイグレーション入門.md | 269 | 完了 | なし |
| 8 | docs/SpringBoot入門/バリデーション入門.md | 683 | 完了 | なし |
| 9 | docs/SpringBoot入門/ロギング入門.md | 1148（修正後） | 完了 | P2 1件（修正済み） |
| 10 | docs/SpringBoot入門/例外処理・エラー応答入門.md | 674 | 完了 | なし |
| 11 | docs/SpringBootテスト入門/テスト入門.md | 1127 | 完了 | なし |
| 12 | docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md | 357 | 完了 | なし |
| 13 | docs/SpringBootテスト入門/バリデーションテスト.md | 756 | 完了 | なし |
| 14 | docs/SpringBootテスト入門/DBを含む統合テスト入門.md | 526 | 完了 | なし |
| 15 | docs/SpringBootテスト入門/SpringSecurityテスト入門.md | 445 | 完了 | なし |
| 16 | docs/SpringBootテスト入門/E2Eテスト入門.md | 450 | 完了 | なし |

変更されたファイルはロギング入門の1件のみ。残り15ファイルは変更前後で SHA-256 が一致（補助担当の比較）。
ロギング入門の最終 SHA-256: `3ef25554d05f918fd5bd5cf19bc1686488543b7b51ba0bd386654c330eb2ee09`

## 2. 確定指摘と修正（P2 1件）

### 対象
`docs/SpringBoot入門/ロギング入門.md` の「MDC のパターン設定」節

### 問題
章内で設定ファイル名が揺れていた。ハンズオン（526行付近）は `src/main/resources/logback-spring.xml` を作成させ、章末（1103行付近）も `logback-spring.xml` を退避する手順なのに、MDC 節だけ `logback.xml` を指していた。学習者が別ファイルを作る原因になる説明整合性の問題。

### 修正内容（最小）
1. 本文の `logback.xml` を `logback-spring.xml` に統一。
2. コードフェンスの title を `logback-spring.xml` に変更。
3. 当該 `<pattern>` が「既存の encoder に組み込む断片」であること、ハンズオンでは `<property name="CONSOLE_LOG_PATTERN" value="..."/>` の value 内の出力パターンに組み込むことを明示。
4. MDC キー名はハンズオンのフィルターが設定する `traceId` と `user` に合わせるよう明記（座学の `requestId`/`userId` 例をそのまま置換させないため）。

### 変更しなかったもの
- 845行の対応ファイル一覧「`logback.xml` / `logback-spring.xml`」は正しい記述なので維持。
- 座学の `requestId`/`userId` のコード例、ハンズオンの実装コードは変更なし。

### 修正後の再読
1034〜1046行を再読し、本文・フェンス title・キー名の指示が整合していることを確認。

## 3. 撤回済みの候補（現在の不具合として扱わない）

- **NotBlank の日本語メッセージ欠落**: 撤回。補助担当が隔離環境で Spring 管理の Validator を実行し、`LocaleContextHolder=ja` で「空白は許可されていません」、`en` で「must not be blank」となることを確認（NotBlankFallbackTest 1件成功）。Hibernate Validator 内蔵の日本語バンドルへ正常にフォールバックするため、キー追加の修正は不要。
- **`thymeleaf-extras-springsecurity6` の不足**: 撤回。公開配布 ZIP v1.0.0（SHA-256 `88c5c01339fde0d0bbbc35bacd614cc934277704e540530b225f9d0b9baf178a`）の `pom.xml` 54〜57行付近に既に含まれることを確認。依存追加の修正は不要。
- **`logback.xml` では Boot の既定設定を一切取り込めない／必ず既存設定が失われる**: 撤回。公式資料では `logback.xml` 自体もサポートされ、Spring Boot 拡張には `logback-spring.xml` または `logging.config` の指定が必要という整理が正しい。参照: https://docs.spring.io/spring-boot/4.0/reference/features/logging.html#features.logging.custom-log-configuration および https://docs.spring.io/spring-boot/4.0/reference/features/logging.html#features.logging.logback-extensions
- **Spring Boot 版の不統一（`v4.0.7` 参照）**: 撤回。`v4.0.7` は Starter 公式定義への参照 URL で実行版指定ではない。補助担当が当該タグの定義実在と security / test / security-test 依存を確認済み。参照: https://github.com/spring-projects/spring-boot/blob/v4.0.7/starter/spring-boot-starter-security-test/build.gradle
- **ポート番号の衝突**: 撤回。8080 は各章のアプリ、18080 は E2E 用 `application-e2e.yaml`、5432 は DB統合テストの接続例で用途が異なる。introduction がアプリ切り替え時の停止と 8080 競合回避を明記済み。
- **「次の章の動作確認」が DB 章への移行を指す**: 撤回。プロジェクト作成・デプロイ入門の同一文書内の後続節「動作確認」を指す。
- **ロギングの依存名未確認**: 解消。本文の `spring-boot-starter-aspectj` で依存解決・起動・AOP ログを確認済み。

## 4. 横断評価の結論

### 4.1 プロジェクトの引き継ぎ
`docs/introduction.md` 19〜24行の原文に基づく正しい整理:

- 原則として教材ごとに別の Java プロジェクトを使う。
- 例外1: Spring Security 第2回（Vol2）は第1回（Vol1）のプロジェクトを継続。
- 例外2: DB を含む統合テスト入門は DB アクセス・トランザクション入門の最終段階のプロジェクトを使う。
- 例外3: E2E テスト入門は Spring Security 第2回の最終段階のプロジェクトを使う。
- Spring Security テスト入門は独立したプロジェクト。

各章の前提記述はこの方針と矛盾しない。

### 4.2 バージョン
`4.0.2` を明示する Initializr リンク（プロジェクト作成54行、DBマイグレーション26行、バリデーションテスト40行、ロギング75行）の間で一致。MyBatis Starter の `4.0.0`（テスト入門43/48行、DBアクセス80行、DB統合67行）は別ライブラリの版であり混同ではない。「4.0 系」とのみ述べる章の指定版を 4.0.2 と断定はしない。

### 4.3 用語（単体／結合／統合／E2E）
各章の説明は同文ではないが相互に矛盾しない。

- テスト入門 95行: この資料では結合テストも広い意味で「統合テスト」と呼ぶと明記。
- 開発者テスト入門 64〜66行: プロジェクトやチームによる定義差、結合と統合をまとめる資料もあると補足。348行: 用語はチームで定義し、実物と接続している範囲を明確にする。
- DBを含む統合テスト: 部分テストと、実物を接続する統合テストの範囲を表で説明。

### 4.4 リンク・アセット
- 開発者テスト章の `equivalence-partitioning.svg`（3039 bytes）と `boundary-value-analysis.svg`（4148 bytes）は所定ディレクトリに存在し、SVG XML として解析成功。
- ビルド済み17 HTML で内部リンク1352件・フラグメント1009件を照合し欠落0。

## 5. 検証結果（出所を区別）

### 5.1 Rei 自身が実行した検証
- `npm run textlint`: 終了コード 0、タイムアウトなし（call_5b20f36ab496fe599737b517）。
- `npm run build`: 終了コード 0、タイムアウトなし、16ページ生成（call_3833b323437cfb826d7d5ac8）。
- `node .tmp-html-links.mjs`: `broken=0`。
- `node .tmp-anchor-check.mjs`: `bad=0`。

### 5.2 補助担当が実行した検証（Rei 自身の実行ではない）
隔離ディレクトリで教材から抽出したコードを用いた実行。Java は JDK 25、コンパイル release 21、Spring Boot 4.0.2。

| 対象 | 結果 | 制限 |
|---|---|---|
| プロジェクト作成・デプロイ入門 | DTO/Service/Controller と YAML を抽出し、local/prod プロファイル起動、ログレベル、age 計算、400/500 応答を確認。テスト1件成功 | Java 21 ランタイム・Initializr 操作・jar 配置の実行保証ではない |
| DBマイグレーション入門 | 3本の SQL を Flyway/H2 で適用。再実行0件、履歴2件、outOfOrder の例外と true 時の適用を確認。テスト1件成功 | H2 コンソール GUI の実操作ではない |
| DBアクセス・トランザクション入門 | 完成コードに `@Transactional` を追加し、H2 契約テスト6件成功。加えて、本文の H2 Console 設定（`spring-boot-h2console` / `spring.h2.console.enabled`）を使った別の1テストで `/h2-console/` の HTTP 200 と H2 Console の画面本文を確認 | H2 Console の GUI ログイン操作は未実施。PostgreSQL コンテナーはこの章の検証対象ではない |
| バリデーション入門 | 全完成コードをコンパイル、HTTP 応答・RangeEx・CalcInfo の相関を4件成功 | Java 21 ランタイム未検証 |
| ロギング入門 | 最終 Java と MDC 入り Logback XML で3件成功。標準テキスト形式・logstash JSON 形式も別々に起動し HTTP 200 と traceId を確認 | 10MB ローテーション・30日保持の経過試験は未実行 |
| 例外処理・エラー応答入門 | ApiExceptionHandlerTest 8件成功（404/409/400/500、内部情報非公開、Location、405 の Allow） | 独立したメモリー内サンプルで DB を使用しない |
| テスト入門 | UserControllerTest/UserMapperTest/UserServiceTest/UserUtilTest/UserUtilDesignTest 計11件成功 | 追加課題の未掲載解答は未実行 |
| 開発者テスト入門 | AgeValidator とテスト2件成功 | — |
| バリデーションテスト | ApiExceptionHandlerTest/UserControllerTest/UserCreateRequestTest 計10件成功 | — |
| DBを含む統合テスト入門 | H2 で OrderApiH2Test 6件成功（CRUD・ロールバック含む） | Docker/Podman 不在のため PostgreSQL/Testcontainers 版は未実行 |
| SpringSecurityテスト入門 | AccessControlTest 9件 + LoginLogoutTest 3件 = 12件成功 | DB認証・HTML描画・実ブラウザーは範囲外 |
| E2Eテスト入門 | 公開 v1.0.0 ZIP + Vol1/Vol2 最終実装の隔離アプリで TypeScript 3シナリオを Chromium + Playwright 1.64.1 で実行。初回3件成功、同一 JVM 再利用で再実行3件成功。意図的失敗で1件が期待どおり失敗し trace 保存を確認、戻して3件再成功 | 実受講者による90分授業は未実施 |

同名サンプルのコピー間で重複するテストがあるため、上記を単一の独立テスト件数として合算しない。

### 5.3 未検証範囲（明示）
- PostgreSQL / Testcontainers の実行（Docker 不在）。DBを含む統合テスト入門の後半が該当。
- Java 21 ランタイム上での実行（JDK 25 + release 21 のみ）。
- 実ブラウザーでの画面描画・Cookie 属性・CSRF トークン埋め込みの目視確認（E2E の Playwright 実行を除く）。
- ロギングの 10MB ローテーションと30日保持の経過試験。
- 実受講者による授業時間（90分・60分）の妥当性。
- 画像の見た目としての評価（SVG の存在と解析成功のみ確認）。

## 6. 受け入れ条件の達成状況

| 条件 | 状態 | 根拠 |
|---|---|---|
| docs 配下の全 Markdown 16ページを実際に読み、章単位の指摘を根拠付きで記録 | 達成 | 各章の到達範囲・評価を partial 記録に保存。全16章の章末まで取得済み |
| 横断レビュー（用語・パス・ポート・バージョン・リンク・章間前提） | 達成 | 本報告 4章。事実誤認の訂正も反映済み |
| 指摘を P0/P1/P2 に分類し、確定分を applyTextDiff で修正 | 達成 | P0/P1 なし。P2 1件を修正 |
| 修正後に該当箇所を再読して整合を確認 | 達成 | ロギング 1034〜1046行を再読 |
| レビューレポートを作成して報告 | 達成 | 本ファイル |

## 7. 残件・阻害要因

教材本文に関する未修正の確定指摘はない。残る未検証範囲と制限は 5.3 のとおり。うち PostgreSQL/Testcontainers の未実行は Docker 不在、Java 21 ランタイム上の実行は環境不在による。一方、画像の目視評価、ロギングの長期保持・ローテーションの経過試験、実受講者による授業時間の妥当性は、環境不在とは別の未実施範囲である。
