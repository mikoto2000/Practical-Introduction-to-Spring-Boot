# docs Markdown レビュー 中間記録（2026-10-24）

## 最新状況（2026-10-24 07:34 時点）

- 本文評価が保存済みのページは5ページ: `docs/index.md` 1-15、`docs/introduction.md` 1-102、`docs/SpringSecurity入門/Vol1.md` 1-638、`docs/SpringSecurity入門/Vol2.md` 1-915、`docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md` 1-442。
- 残り12ページは本文評価が未完了。以下の「読了」「部分読了」「未読」の表は作成時点の履歴であり、本ファイル末尾の各章評価で更新済み。先頭の表だけを最新状況として扱わない。
- 全体の到達状況は本ファイル末尾の「全体の到達状況」を参照。
- レビュー全体（残り12ページの評価、横断確認、確定指摘の修正、検証、最終レポート）は未完了。

## この記録の位置づけ

- 対象は `docs` 配下の Markdown 16 ページ。
- 本文取得が制限されているため、**全ページの通読・横断レビュー・修正は未完了**。
- 本ファイルは「実際に本文を確認できた範囲」の評価のみを記録する。読めていない範囲について指摘や完了を主張しない。
- 「読了」と「レビュー済み」は別。読了でも章単位の指摘確定には至っていない範囲がある。

## 読了（本文全体を確認）

| path | 行範囲 | 確認できた内容 |
| --- | --- | --- |
| `docs/index.md` | 1-15（全行） | frontmatter（pageType: home、hero、actions、image） |
| `docs/manifest.json` | 1-6（全行） | format/version/entrypoint/resources |
| `docs/SpringSecurity入門/_meta.json` | 1-4（全行） | 並び順 `Vol1`, `Vol2` |

## 部分読了（先頭のみ）

| path | 行範囲 | 確認できた内容 |
| --- | --- | --- |
| `docs/introduction.md` | 1-20 | frontmatter、概要、対象者・前提知識の冒頭 |
| `docs/SpringSecurity入門/Vol1.md` | 1-19 | frontmatter、講義の区切り、概要、対象読者の冒頭 |
| `docs/SpringSecurity入門/Vol2.md` | 1-15 | frontmatter、講義の区切り、概要の冒頭 |
| `rspress.config.ts` | 1-9 | import 群と `defineConfig` の開始のみ |
| `package.json` | 1-15 | name/version/scripts と dependencies の冒頭のみ |

## 未読（本文未確認）

- `docs/introduction.md` 21 行目以降
- `docs/SpringSecurity入門/Vol1.md` 20 行目以降
- `docs/SpringSecurity入門/Vol2.md` 16 行目以降
- `rspress.config.ts` 10 行目以降
- `package.json` 16 行目以降
- `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md`
- `docs/SpringBoot入門/DBアクセス・トランザクション入門.md`
- `docs/SpringBoot入門/DBマイグレーション入門.md`
- `docs/SpringBoot入門/バリデーション入門.md`
- `docs/SpringBoot入門/ロギング入門.md`
- `docs/SpringBoot入門/例外処理・エラー応答入門.md`
- `docs/SpringBootテスト入門/テスト入門.md`
- `docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md`
- `docs/SpringBootテスト入門/バリデーションテスト.md`
- `docs/SpringBootテスト入門/DBを含む統合テスト入門.md`
- `docs/SpringBootテスト入門/SpringSecurityテスト入門.md`
- `docs/SpringBootテスト入門/E2Eテスト入門.md`

## 現時点で根拠のある所見

1. **要確認（P2 候補）** `docs/index.md` の hero actions で `link: introduction` と、先頭スラッシュも拡張子もない指定になっている。Rspress v2 の home hero action は `/introduction` のようなサイトルート基準の指定が一般的。現状でも `/introduction` に解決される可能性は高いが、実挙動は未検証。
2. **整合OK** `Vol1.md` / `Vol2.md` は frontmatter の `title` と直後の `#` 見出しが一致している。`_meta.json` の並び（Vol1 → Vol2）も実ファイルと一致。
3. **整合OK** `Vol1.md` の `[学習ガイド](../introduction.md)` は `docs/SpringSecurity入門/` から `docs/introduction.md` を指し、実在ファイルと一致する。
4. **未検証** `rspress.config.ts` が import している `@rspress/plugin-sitemap`、`rsbuild-plugin-google-analytics`、`rspress-plugin-rst-directives`、`rspress-plugin-plantuml` が `package.json` の依存に記載されているかは未確認（`package.json` 16 行目以降が未読）。
5. **未検証** `docs/manifest.json` の `"resources": []` が意図的な設定か、必須リソースの記載漏れかは未確認。

## `docs/introduction.md` 20-67 行の所見（部分評価）

読了範囲: 20-67 行。68 行目以降は未読。

- **整合OK（章間前提）** プロジェクトの引き継ぎが明示されている。Security 第2回は第1回の継続、DB 統合テストは DB アクセス章の最終段階、E2E は第2回の最終段階、Security テストは独立プロジェクト。
- **要確認（P2・横断）** 「学習の進め方」の順序は テスト入門(3) → バリデーションテスト(4) → ロギング/DBマイグレーション(5) → DBアクセス(6) → 例外処理(7) → Security(8,9) → DB統合(10) → E2E(11)。サイドバーの並びと一致するかは `rspress.config.ts` の nav が未読のため未検証。
- **要確認（P2・バージョン）** 「同じ 4.0 系の利用可能な安定版」と Spring Boot 4.0 系を前提にしている。各章の記載バージョンが 4.0 系で揃うかは未検証。
- **要確認（P2・バージョン）** 「Security の配布 ZIP は Java 25 指定」とある一方、前提は JDK 21。第1回で `pom.xml` を Java 21 に変更する手順と整合するか、Vol1 本文で確認が必要。
- **要確認（P2・ポート）** 「8080 番ポートを使うアプリを同時に起動すると競合」と明記。他章が別ポートを前提にしていないか未検証。
- **要確認（P2・バージョン）** E2E は Node.js 24・npm・Playwright・TypeScript 前提。E2E 章の記載と一致するか未検証。

## `docs/introduction.md` 68-102 行の所見（読了）

読了範囲: 1-102 行（全体）。上記 20-67 行の所見と合わせて評価。

- **整合OK（リンク）** 章一覧表と「コンテンツ一覧」の 14 リンクはすべて `./SpringBoot入門/...` `./SpringBootテスト入門/...` `./SpringSecurity入門/...` 形式で、`docs/introduction.md` からの相対パスとして実在ディレクトリと一致する。
- **整合OK（章数）** 表 14 行・一覧 14 項目。`docs` 配下の Markdown は index.md・introduction.md + 14 章 = 16 ページで、受け入れ条件の「16 ページ」と一致する。
- **整合OK（章間前提）** 表の「発展・次回」列が各章の「講義の区切り」と対応する形になっている（例: テスト入門の Repository・Controller を次の 60 分、DB 統合テストの PostgreSQL を次の 60 分）。
- **要確認（P2・用語の揺れ）** 見出し・表では「Spring Security 入門 Vol.1」「Spring Security 入門 Vol.2」と空白入り、リンク文言では「SpringSecurityテスト入門」「DBを含む統合テスト入門」「E2Eテスト入門」と空白なし。本文中にも「DB を含む統合テスト入門」「E2E テスト入門」と空白入りの表記が混在している。教材全体の用語統一の観点で P2 候補。
- **要確認（P2・表記）** 「目安（分）」列の合計は 1230 分（約 20.5 時間）。「勉強会の単位」節の説明と矛盾しないかは、68 行目以降の記述（講義の区切りで分割）と整合しているため問題なしと判断。
- **未検証** 表の各章「基本の終了条件」が各章の「講義の区切り」記載と逐語一致するかは、各章本文が未読のため未確認。

## `docs/SpringSecurity入門/Vol1.md` 19-66 行の所見（部分評価）

読了範囲: 1-66 行。67 行目以降は未読。

- **整合OK（章間前提）** 「ロールごとの制御は第2回で扱います」と明記され、Vol2 の役割と矛盾しない。前提は Java 21・ブラウザ・HTML フォームの GET/POST で、`introduction.md` の前提（JDK 21）と一致。
- **整合OK（構成）** 「2 部構成」（触って学ぶ／座学で学ぶ）と対応表があり、ハンズオン見出しと座学見出しの対応が明示されている。
- **指摘候補（P2・表記）** 「この資料は 2 部構成 になっています。」（41 行目相当）で「構成」と「に」の間に不要な半角スペースがある。日本語の助詞前の空白は表記統一の観点で除去対象。→ 確定後に `applyTextDiff` で修正可。
- **要確認（P2・リンク/アンカー）** 対応表のアンカーが `#認証の仕組みuserdetailsservice`、`#パスワードハッシュbcrypt--delegatingpasswordencoder`、`#認可の仕組みsecurityfilterchain--requestmatchers` となっている。見出しは「認証の仕組み（UserDetailsService）」「パスワードハッシュ（bcrypt / DelegatingPasswordEncoder）」「認可の仕組み（SecurityFilterChain / requestMatchers）」で、括弧・スラッシュの除去規則が実際のアンカー生成と一致するかは未検証（見出し本体が 67 行目以降にあり未読のため、存在確認も未了）。
- **未検証** 対応表の左列「セキュリティの重要性」「認証と認可の軽い導入」は 44・52 行目相当で見出しを確認済み。残り 5 見出しは未読範囲。

## 未解決ギャップ

- 16 ページ中 13 ページの本文が未読。章単位の指摘確定と、用語・パス・ポート・バージョン・リンク・章間前提の横断レビューは未実施。
- 修正（`applyTextDiff`）は未実施。根拠付きで確定した指摘がまだ 0 件のため。
- 本文取得が枯渇状態にあり、`readMultiFile` は 1 回あたり数百バイトしか返さない。全 16 ページの通読には取得予算の回復が必要。

## 次のアクション

1. 取得予算が回復したら、`introduction.md` 残り → `Vol1.md` → `Vol2.md` → SpringBoot入門 5 章 → テスト入門 6 章の順で通読する。
2. 各章を読み終えるたびに、このファイルへ章単位の所見を追記する（読了と指摘確定を分けて記録）。
3. 確定指摘が出た時点で P0/P1/P2 に分類し、`applyTextDiff` で修正、修正箇所を再読して整合を確認する。

## 2026-10-24 再開時の中間評価（追記）

### 実際に本文を確認できた範囲

- `docs/SpringSecurity入門/Vol2.md` 1-28行: frontmatter、タイトル、講義の区切り、概要、対象読者、前提知識の冒頭。
- `docs/SpringSecurity入門/Vol1.md` 1-32行: frontmatter、タイトル、講義の区切り、概要、対象読者、前提知識、構成の冒頭。
- `review/2026-10-24-review-partial.md` 1-23行: 既存の中間記録（読了・部分読了の区分）。

### 確認できた整合（指摘候補ではなく確認済み事項）

- `Vol2.md` 28行目付近の「第1回で `pom.xml` の `java.version` を 21 に変更したプロジェクトを使います」は、`Vol1.md` の既存抜粋にある「配布 ZIP v1.0.0 の `pom.xml` は Java 25 を指定しています。この教材では Java 21 に揃えるため、起動前に次のプロパティへ変更してください」と方向性が一致している。
- `Vol2.md` のタイトル（frontmatter と `#` 見出し）は同一で、第2回の位置づけも本文冒頭で説明されている。
- `Vol1.md` のタイトル（frontmatter と `#` 見出し）も同一で、第1回の位置づけが本文冒頭で説明されている。

### 未確認・未評価（指摘として確定しない）

- `Vol2.md` 29行目以降の本文、コード例、演習、リンク、章末確認は未読。
- `Vol1.md` 33行目以降の本文、コード例、演習、リンク、章末確認は未読。
- `Vol1.md` 32行目の「この資料は 2 部構成 になっていま」は表示上「2 部構成」の前後に空白があり、文末も途中で切れている。ただし行全体の文脈と後続行を確認していないため、表記揺れ・脱字の指摘としては未確定。
- `docs` 配下の他14ページ（`index.md`、`introduction.md`、`manifest.json`、SpringBoot入門6ページ、SpringBootテスト入門6ページ）は、この追記時点では本文全体の再確認・章単位の指摘確定が未完了。
- 横断レビュー（用語、パス、ポート、バージョン、リンク、章間前提）と P0/P1/P2 分類、修正適用、修正後の再読は未完了。

### 現時点の結論

- 読了済み・部分読了済みの範囲は上記のとおり。
- レビュー済み（章単位の指摘を根拠付きで確定）と言える範囲はまだ限定的で、全体のレビュー完了ではない。
- 次の作業は、未読の `Vol2.md` 29行目以降と `Vol1.md` 33行目以降を優先して読み、本文の根拠を追加すること。

### `docs/SpringSecurity入門/Vol2.md` 28-152行の評価

確認できた内容: 前提知識の続き、この資料の構成（2部構成の説明と対応表）、触って学ぶ編の導入、`SecurityConfig` の `formLogin`/`logout` 修正コード、`LoginController` の作成、ログイン画面作成の冒頭。

指摘候補:

- **P2（表記）**: 「この資料は 2 部構成 になっています。」（44行目付近）— 数字と「部構成」の間、および「部構成」と「に」の間の不要な空白。`Vol1.md` 32行目にも同様の「2 部構成 になっていま」があり、両ファイルで「2部構成になっています。」に統一するのが自然。
- **P2（表記）**: 「まずは、 SecurityConfig の `formLogin` と `logout` を修正します。」（70行目付近）— 読点直後の不要な半角スペース。

確認済み（指摘としない）:

- 対応表のアンカー（`#ログインログアウトのカスタマイズ`、`#db-からユーザー情報を取得するように修正`、`#db-認証の仕組みuserdetailsservice-と-db`、`#ロールと認可rbac-の触り`）は、見出し文字列から `・` と括弧を除去したスラッグと整合している。実アンカー生成は未検証（ビルド時のリンク確認で検証予定）。
- `SecurityConfig` のコードは `formLogin` で `loginPage`/`permitAll`/`failureUrl`、`logout` で `logoutUrl`/`logoutSuccessUrl` を指定しており、直前のコメントと一致している。

未確認: `Vol2.md` 153行目以降（ログイン画面の HTML、DB認証、ユーザー登録、座学編、章末）。

### `docs/SpringSecurity入門/Vol2.md` 152-318行の評価

確認できた内容: `login.html`（Thymeleaf フォーム、`param.error` 表示）、`GET /login` と `POST /login` の役割分担の説明、ログアウトを即時実行する `index.html` / `private.html` の変更、自作フォームの動作確認手順、DB 取得への変更導入、`USERS` テーブル定義（`schema.sql`）、初期データ（`data.sql`）、エンティティ作成の冒頭。

指摘候補:

- **P1（内容の不整合・要確認）**: 冒頭 30行目付近で「DB のユーザーと ADMIN / USER の権限差を確認することを目指します」とあるが、`data.sql` の初期データは `mikoto2000` と `mikoto2001` の**両方が `ADMIN`**。この時点で USER ロールのユーザーが存在せず、ADMIN / USER の権限差を確認できない。後続のユーザー登録節で USER を作る想定なら説明の補足が必要。後続未読のため確定は保留。
- **P2（表記）**: 「第1回でも述べた通り、 Spring Security のデフォルトでは」（206行目付近）— 読点直後の不要な半角スペース。
- **P2（表記）**: 「CSRF 対策のため、 Thymeleaf の機能を用いて」（210行目付近）— 同上。
- **P2（表記）**: 「(今回は、 Spring Security に「ユーザー名とパスワード」を渡すところまでをカスタマイズしたということ)」（246行目付近）— 同上。

確認済み（指摘としない）:

- `login.html` のフォームは `th:action="@{/login}"` + `method="post"`、`name="username"` / `name="password"` で、Spring Security の既定パラメータ名と一致している。
- `index.html` / `private.html` のログアウトは `th:action="@{/logout}"` の POST フォームで、`logoutUrl("/logout")` の設定と一致し、CSRF トークン自動挿入の説明とも整合している。
- `schema.sql` の `USERS` 列（`username`/`password`/`enabled`/`role`）と `data.sql` の INSERT 列順は一致している。
- パスワードは `{bcrypt}` プレフィックス付きハッシュで格納されており、後続の `PasswordEncoder` 説明と矛盾しない（詳細は後続で確認）。

未確認: `Vol2.md` 319行目以降（エンティティ本体、Mapper、`UserDetailsServiceImpl` 修正、ユーザー登録、座学編、章末）。

### `docs/SpringSecurity入門/Vol2.md` 318-519行の評価

確認できた内容: `entity.User`（Lombok `@Data`/`@AllArgsConstructor`）、`UsersMapper`（`@Select` で `USERS` を `username` 検索、`Optional<User>` 返却）、`UserDetailsServiceImpl` の DB 取得版、DB 認証の動作確認手順、`entity.User` と Spring Security `User` の同名注意、ユーザー登録節の導入、`SecurityConfig` 完成コード（`/signup`・`/admin/**` の ADMIN 制限、`PasswordEncoder` Bean）。

確認済み（指摘としない）:

- `UsersMapper` の SELECT 列順（`username, password, enabled, role`）は `entity.User` のフィールド順と一致し、`schema.sql` の列とも一致している。
- `User.withUsername(...).roles(user.getRole())` は `ROLE_` 接頭辞を付与し、`data.sql` の `role='ADMIN'` と `hasRole("ADMIN")` が対応する。整合している。
- `disabled(!user.getEnabled())` は `enabled` カラム（`BOOLEAN NOT NULL`）と対応している。
- `PasswordEncoderFactories.createDelegatingPasswordEncoder()` は `{bcrypt}` プレフィックス付き保存値と整合する。
- 「初期管理者ユーザーは `data.sql` であらかじめ作成しています」は `data.sql` の内容と一致する。

未確認: `Vol2.md` 520行目以降（登録コントローラー、登録画面、動作確認、座学編、章末）。

### `docs/SpringSecurity入門/Vol2.md` 520-763行の評価

確認できた内容: `UsersMapper` への `@Insert` 追加、`SignupController`（`GET/POST /signup`、`passwordEncoder.encode`、`RuntimeException` 捕捉で `redirect:/signup?error`）、`index.html` への `sec:authorize="hasRole('ADMIN')"` リンク、`login.html` へのリンク追加、`signup.html` の新規作成、動作確認の冒頭。

指摘候補:

- **P1（手順の欠落・要確認）**: 「Thymeleaf で認可情報を扱うために、 `thymeleaf-extras-springsecurity6` を利用し」（約700行目付近）とあるが、`pom.xml` への依存追加手順が本文に示されていない。第1回の `pom.xml` に既に含まれているなら問題ないが、その確認・記載がないと `sec:authorize` が解決できず画面が壊れる可能性がある。`Vol1.md` の `pom.xml` 記載を確認して確定する。
- **P2（表記）**: 「`sec:authorize` で「ロールごとに表示非表示を切り替える」を実現しています。」（約705行目付近）— 「表示非表示」は「表示・非表示」または「表示と非表示」が自然。
- **P2（表記）**: 「GET リクエストでサインアップページを表示し、そこから POST リクエストを受け取ることでユーザー登録する。」（約640行目付近）— 文末が「ユーザー登録する。」で体言止め気味。他の箇所は「〜します」調なので「ユーザー登録します。」に統一するのが自然。

確認済み（指摘としない）:

- `@Insert` の SQL 列順・プレースホルダ（`#{username}`, `#{password}`, `#{enabled}`, `#{role}`）は `entity.User` のフィールドと一致している。
- `SignupController` は `new User(username, hashedPassword, true, "USER")` で `@AllArgsConstructor` の引数順（`username, password, enabled, role`）と一致している。
- `signup.html` のフォームは `th:action="@{/signup}"` + `method="post"`、`name="username"` / `name="password"` でコントローラの `@RequestParam` と一致している。
- `SecurityConfig` の `/signup` に対する `hasRole("ADMIN")` と、View 側の `sec:authorize="hasRole('ADMIN')"` は同じ判定で整合している。
- `signup.html` の `th:if="${param.error}"` は `redirect:/signup?error` と対応している。

未確認: `Vol2.md` 764行目以降（動作確認の続き、座学編、章末）。

### `docs/SpringSecurity入門/Vol2.md` 763-808行の評価

確認できた内容: 動作確認手順（1〜5）、リンク非表示だけでは不十分で URL 認可とセットで確認する注意、`/admin/**` は画面未作成で `/signup` で権限差を確認する旨、入力検証・重複エラーを省略する旨とバリデーション入門への誘導、座学編の開始（ログインページのカスタマイズの仕組み、`loginPage`/`permitAll`/`failureUrl`、ログイン処理はデフォルトのまま、ログアウトと CSRF の冒頭）。

前回保留した指摘候補の解消:

- **解消（指摘としない）**: 「ADMIN / USER の権限差を確認する」という冒頭の到達目標は、`SignupController` が新規ユーザーを `role="USER"` 固定で登録し、手順4〜5で USER ユーザーの `/signup` 拒否を確認する流れになっており、`data.sql` が両方 ADMIN でも目標は達成可能。不整合ではない。

確認済み（指摘としない）:

- 手順3「この間はアプリを再起動しない」は、インメモリ H2 のため再起動で登録データが消えるという 250行目付近の説明と整合している。
- 手順5の「URL を直接入力して `/signup` にアクセスし、403」は `SecurityConfig` の `.requestMatchers("/signup").hasRole("ADMIN")` と整合している。
- 「`/admin/**` のルールも定義しましたが、対応する画面は今回作りません」は `SecurityConfig` の実装と一致し、読者の混乱を避ける補足になっている。

未確認: `Vol2.md` 809行目以降（CSRF の続き、DB 認証の仕組み、パスワードハッシュ、ロールと認可、章末）。

### `docs/SpringSecurity入門/Vol2.md` 808-875行の評価

確認できた内容: CSRF と `th:action` の説明の続き、`logoutSuccessUrl("/")` の説明、DB 認証の仕組み（HashMap からの切替理由、認証フロー図、`UsersMapper.findByUsername`、`UsernameNotFoundException`）、パスワードハッシュと `PasswordEncoder`（平文保存の危険、Bean 定義の理由、`encode` と `{bcrypt}` プレフィックス）、ロールと認可（RBAC）の冒頭（ロールの定義、`hasRole` による URL 制御、`roles()` が `ROLE_` を付与する説明）。

確定指摘: なし。

確認済み（指摘としない）:

- 「`roles("ADMIN")` は内部では `ROLE_ADMIN` という権限を作ります。そのため DB の role には `ADMIN` を保存し、`hasRole` にも `ADMIN` を渡します。」は Spring Security の実際の挙動と一致し、`data.sql`・`UserDetailsServiceImpl`・`SecurityConfig` の実装とも整合している。
- 認証フロー図の順序（ユーザー送信 → `UserDetailsService` が DB 検索 → `PasswordEncoder` 照合 → 成功判定）は、`UserDetailsServiceImpl` と `PasswordEncoder` Bean の実装と矛盾しない。
- `createDelegatingPasswordEncoder()` が `{bcrypt}` プレフィックス付きハッシュを生成する説明は、`data.sql` の保存値と一致している。

継続保留（未確定）:

- `thymeleaf-extras-springsecurity6` の依存追加手順が本文にない点は、`Vol1.md` の `pom.xml` 記載を確認するまで P1 候補として保留。

未確認: `Vol2.md` 876行目以降（`sec:authorize` の説明の続き、章末・まとめ）。

### `docs/SpringSecurity入門/Vol2.md` 875-915行の評価（ファイル末尾まで）

確認できた内容: `sec:authorize` による View レベル制御の説明、RBAC の触りと本格設計の範囲外宣言、本番へ進む前の確認（HTTPS、Cookie、試行制限、入力検証、PasswordEncoder、CSRF）、まとめ、参考資料（CSRF の IPA リンク）。

確定指摘: なし。

確認済み（指摘としない）:

- 「URL レベルは「アクセスを拒否する」制御で、View レベルは「表示しない」制御です」は、`SecurityConfig` の `hasRole` と `sec:authorize` の実装に対応している。
- 本番向け注意は、`data.sql` の固定ユーザー・固定パスワードという教材構成と矛盾せず、演習専用である旨も明記されている。
- 参考資料の IPA CSRF リンクは実在する公式ページの URL 形式で、本文の CSRF 説明と対応している（到達確認は未実施）。

### `Vol2.md` 全体の到達状況

- 1-915行をすべて確認済み（ファイル末尾まで）。
- 章単位の確定指摘は、表記の P2 候補（不要な半角スペース、「2 部構成」の空白、「表示非表示」）と、`thymeleaf-extras-springsecurity6` の依存追加手順の欠落疑い（P1 候補、`Vol1.md` の `pom.xml` 記載で確定判断）に絞られる。
- 技術的な整合（`USERS` スキーマ、Mapper、`UserDetailsService`、`roles`/`hasRole`、`PasswordEncoder`、CSRF フォーム）は本文内で確認できた範囲では破綻していない。

### `docs/SpringSecurity入門/Vol1.md` 32-81行の評価

確認できた内容: 2部構成の説明と対応表、触って学ぶ編の導入、セキュリティの重要性、認証と認可の軽い導入、ベースプロジェクト（配布 ZIP v1.0.0 のダウンロード、`pom.xml` の `java.version` を 21 へ変更、`./mvnw -v` と `./mvnw test` の確認）。

確定指摘:

- **P2（表記）**: 「この資料は 2 部構成 になっています。」（32行目付近）— 「2 部構成」の前後と「部構成」と「に」の間に不要な半角スペース。`Vol2.md` 44行目付近にも同一の表記があり、両方を「2部構成になっています。」に統一する。

確認済み（指摘としない）:

- 対応表のアンカー（`#セキュリティの重要性`、`#認証と認可の違い`、`#認証の仕組みuserdetailsservice`、`#パスワードハッシュbcrypt--delegatingpasswordencoder`、`#認可の仕組みsecurityfilterchain--requestmatchers`、`#ログアウトの仕組み`）は、見出しから記号を除去したスラッグと整合している（実アンカー生成はビルド検証で確認予定）。
- 「配布 ZIP v1.0.0 の `pom.xml` は Java 25 を指定しています。この教材では Java 21 に揃えるため…」は、`Vol2.md` の「第1回で `pom.xml` の `java.version` を 21 に変更したプロジェクトを使います」と整合している。
- `./mvnw -v`（PowerShell では `.\mvnw.cmd -v`）という OS 別のコマンド併記は、Windows 環境の読者にも対応している。

未確認: `Vol1.md` 82行目以降（ベースプロジェクトの続き、デフォルト動作確認、`UserDetailsService` カスタマイズ、認可、ログアウト、座学編、章末）。

### `docs/SpringSecurity入門/Vol1.md` 81-218行の評価

確認できた内容: ベースプロジェクトの起動方法（`./mvnw spring-boot:run`、PowerShell 版）、インデックスページの HTML と `IndexController`、`@Controller` と `@RestController` の戻り値の違い、デフォルト時の動作確認（生成パスワードのログ出力例、`user` でのログイン、`http://localhost:8080`）、`UserDetailsService` のカスタマイズ（HashMap 実装、`mikoto2000`/`mikoto2001` と `password`、`roles("ADMIN")`、`disabled(false)`）、DB 取得は第2回へ誘導する冒頭。

確定指摘: なし。

確認済み（指摘としない）:

- `UserDetailsServiceImpl` の HashMap 実装は、`{bcrypt}` プレフィックス付きハッシュを保持しており、後続の `PasswordEncoder` 説明と矛盾しない。
- `roles("ADMIN")` により第1回のユーザーは全員 ADMIN となり、第1回の到達目標（ログイン・ログアウトとページ保護）と矛盾しない。ロール差は第2回で扱う旨も冒頭に明記されている。
- 「デフォルトの生成パスワードは使われなくなり、`mikoto2000` / `password`（または `mikoto2001` / `password`）でログインします」は、HashMap の登録内容と一致している。
- `http://localhost:8080` は Spring Boot の既定ポートで、`rspress.config.ts` や他章のポート記載との衝突は現時点で見つかっていない（横断確認で再確認予定）。

### 配布物による P1 候補の解消（ユーザー提供の補助証拠）

- `Vol1.md` 71行の公開リンク `https://github.com/mikoto2000/spring-boot-security-workshop/releases/download/v1.0.0/spring-boot-security-workshop-base.zip` を取得し、実行せず ZIP 内の `spring-boot-security-workshop-base/pom.xml` を確認した。ZIP の SHA-256 は `88c5c01339fde0d0bbbc35bacd614cc934277704e540530b225f9d0b9baf178a`。
- `pom.xml` 54-57行付近に `org.thymeleaf.extras:thymeleaf-extras-springsecurity6` が既に含まれている。親は `spring-boot-starter-parent` 4.0.2、`java.version` は 25 で、教材の Java 21 への変更案内とも一致する。
- したがって「`thymeleaf-extras-springsecurity6` が不足するので追加が必要」という P1 候補は**解消**。ベース ZIP が前提であり、章中に依存追加操作がないだけでは不具合としない。依存追加の修正は不要。
- この確認は配布物の静的確認であり、サンプル Java の実行確認ではない。

未確認: `Vol1.md` 219行目以降（DB 取得の誘導の続き、認可のカスタマイズ、ログアウト、座学編、章末）。

### `docs/SpringSecurity入門/Vol1.md` 218-383行の評価

確認できた内容: DB 取得を第2回へ誘導する文の完結、`{bcrypt}` プレフィックスの説明、認可のカスタマイズ（`SecurityFilterChain` の位置づけ、認可条件 `/` は誰でも・それ以外はログイン必須）、`private.html` と `PrivateController` の作成、`index.html` へのリンク追加、`SecurityConfig` の `formLogin(Customizer.withDefaults())` + `authorizeHttpRequests`、動作確認、ログアウト実装の導入と `logout(Customizer.withDefaults())` の追加コード冒頭。

確定指摘: なし。

確認済み（指摘としない）:

- 認可条件「`/` は誰でも表示可能、ルート以外はすべてログイン必須」は、`requestMatchers("/").permitAll()` と `anyRequest().authenticated()` の実装に一致している。
- `PrivateController` の `@GetMapping("/private")` と `private.html` のテンプレート名 `private` は一致している。
- 「SecurityFilterChain を Bean として定義すると、Spring Boot が自動設定していたデフォルトの SecurityFilterChain は無効化され…」は Spring Boot の実際の挙動と一致している。
- 動作確認のログイン情報 `mikoto2000` / `password` は `UserDetailsServiceImpl` の HashMap と一致している。

未確認: `Vol1.md` 384行目以降（ログアウト設定の続き、動作確認、座学編、章末）。

### `docs/SpringSecurity入門/Vol1.md` 383-491行の評価

確認できた内容: ログアウト設定コードの完結、デフォルトのログアウト確認画面と `/login?logout` へのリダイレクト説明、`/logout` への POST で実行される旨、自作フォームは第2回で実装する旨、`index.html` / `private.html` へのログアウトリンク追加、動作確認、まとめ（第1回の作業一覧と次回予告）、座学編の開始（認証と認可の違い、別の関心事である理由の冒頭）。

指摘候補:

- **P2（リンクの一貫性・要確認）**: `index.html` / `private.html` に追加するログアウトは `<a href="/logout">` の GET リンク。本文は「ログアウトは `/logout` への `POST` リクエストで実行される」と明記しており、GET リンクでは Spring Security の既定動作ではログアウト確認画面が表示される（POST は実行されない）。第1回としては「確認画面を経由する」想定なので動作は破綻しないが、直後の説明とリンクの見た目が紛らわしい。第2回で POST フォームに置き換わる旨は明記されているため、確定指摘ではなく補足候補として扱う。

確認済み（指摘としない）:

- 「`/logout` にアクセスするとログアウト確認画面が表示されます」「`Log Out` ボタンを押下するとログアウトし、`/login?logout` にリダイレクトされます」は Spring Security の既定動作と一致している。
- 「自作フォームからのログアウトは [第2回](./Vol2.md) で実装します」は、`Vol2.md` の `th:action="@{/logout}"` フォーム実装と対応している。
- 動作確認手順（private → ログイン → ログアウト → ルート → private）は、`/` が permitAll、`/private` が authenticated という設定と整合している。
- まとめの「次回は次の作業を進めます」の3項目は、`Vol2.md` 冒頭の「今回は、次の作業を進めます」の3項目と一致している。

未確認: `Vol1.md` 492行目以降（座学編の続き、章末）。

### `docs/SpringSecurity入門/Vol1.md` 491-560行の評価

確認できた内容: 認証と認可を分ける理由の完結、認証の仕組み（認証フロー図、`UserDetailsService` の役割、擬似コード、DB 検索への置換可能性、切り出す理由）、パスワードハッシュ（平文保存の危険、bcrypt の特徴、`{bcrypt}` プレフィックスの意味、`DelegatingPasswordEncoder` の仕組み）、認可の仕組みの冒頭（`SecurityFilterChain` とは）。

確定指摘: なし。

確認済み（指摘としない）:

- 認証フロー図は `Vol2.md` の同種の図と同一の流れで、章間で矛盾していない。
- 「`{bcrypt}` は…`DelegatingPasswordEncoder` という仕組みで解釈されます」は、`Vol2.md` の `createDelegatingPasswordEncoder()` の説明と整合している。
- 擬似コードは `title="擬似コード: UserDetailsService の責務（配置しない）"` と明示され、実ファイルとして誤認させない配慮がある。

### 補助検証（ユーザー提供・配布ベース）

- 公開配布 ZIP v1.0.0 の確認済み `pom.xml` と `src` のみを別ディレクトリへ取り出し、教材指示どおり `java.version` を 21 に変更して `mvn test` を実施。Java 25 実行環境で release 21 のコンパイルに成功し、`SecurityApplicationTests.contextLoads` は 1件成功、失敗0・エラー0。
- DB は隔離したインメモリ H2 を使用し、対象教材・実 DB は変更していない。
- **制約**: Java 21 ランタイムでの実行確認ではない。Java 21 上の動作保証とは記載しない。後続ハンズオン全機能の実行保証でもない。

未確認: `Vol1.md` 561行目以降（`SecurityFilterChain` の続き、`requestMatchers`、ログアウトの仕組み、章末）。

### `docs/SpringSecurity入門/Vol1.md` 560-613行の評価

確認できた内容: `SecurityFilterChain` の続き、`requestMatchers` と `anyRequest` の違い（各メソッドの箇条書き説明、上から順に評価される旨）、認可判断のタイミング（認証フィルタ→認可フィルタ）、ログアウトの仕組み（POST で実行、GET は確認画面、なぜ POST か、`<a href="/logout">` は GET なので確認画面経由という説明、ログアウト後の状態）、本番へ進む前の確認の冒頭。

前回保留した指摘候補の解消:

- **解消（指摘としない）**: `index.html` / `private.html` の `<a href="/logout">` が GET である点は、「ハンズオンで追加した `<a href="/logout">` は `GET` なので、確認画面を経由してから `POST` でログアウトする、という流れになっています」と本文が明示的に説明している。読者の誤解を招く不整合ではなく、意図的な設計説明として成立している。

確定指摘: なし。

確認済み（指摘としない）:

- 「ルールは上から順に評価される」は Spring Security の `authorizeHttpRequests` の実際の挙動と一致している。
- 「まず認証フィルタがユーザーを特定し、その後で認可フィルタが…判断します」はフィルタチェーンの順序として妥当で、`Vol2.md` の認証フローとも矛盾しない。
- 本番向け注意の箇条書きは `Vol2.md` の同種の節とほぼ同一で、章間で一貫している。

未確認: `Vol1.md` 614行目以降（本番確認の続き、まとめ、参考資料、章末）。

### `docs/SpringSecurity入門/Vol1.md` 613-638行の評価（ファイル末尾まで）

確認できた内容: 本番確認の完結と Spring Security 公式のパスワード保存リンク、用語集（認証/認可/`UserDetailsService`/`UserDetails`/`SecurityFilterChain`/フィルタチェーン/`requestMatchers`/`permitAll`/`authenticated`/bcrypt/`DelegatingPasswordEncoder`/ハッシュ化/ログアウト）、参考資料（Spring Security リファレンス、Password Storage、Servlet アーキテクチャ）。

確定指摘: なし。

確認済み（指摘としない）:

- 用語集の各定義は本文の説明と一致し、`Vol2.md` で使う `hasRole`/`roles` の前提（`ROLE_` 接頭辞）とも矛盾しない。
- 参考資料は `spring.pleiades.io` の日本語リファレンスで、本文の参照先と対応している（到達確認は未実施）。

### `Vol1.md` 全体の到達状況

- 1-638行をすべて確認済み（ファイル末尾まで）。
- 確定指摘は P2 表記1件（「2 部構成 になっています。」の不要な空白）。
- 技術的な整合（`UserDetailsService`、`SecurityFilterChain`、`requestMatchers`/`anyRequest`、ログアウトの GET/POST 説明、`{bcrypt}` と `DelegatingPasswordEncoder`）は本文内で破綻していない。
- 第1回→第2回の引き継ぎ（`pom.xml` の Java 21、`mikoto2000`/`password`、`/private`、`/logout`）は `Vol2.md` と一致している。

## 作業ログ（2026-10-24 07:34 再開時）

- 本ファイル先頭に「最新状況」節を追記した（applyTextDiff 適用成功、changed=true）。既存の「読了／部分読了／未読」表は削除せず履歴として保持し、末尾の各章評価で更新済みである旨を明記した。
- 本文評価が保存済みのページ（4ページ）: `docs/index.md` 1-15、`docs/introduction.md` 1-102、`docs/SpringSecurity入門/Vol1.md` 1-638、`docs/SpringSecurity入門/Vol2.md` 1-915。
- 本文評価が未完了のページ（12ページ）: `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md`、`docs/SpringBoot入門/DBアクセス・トランザクション入門.md`、`docs/SpringBoot入門/DBマイグレーション入門.md`、`docs/SpringBoot入門/バリデーション入門.md`、`docs/SpringBoot入門/ロギング入門.md`、`docs/SpringBoot入門/例外処理・エラー応答入門.md`、`docs/SpringBootテスト入門/テスト入門.md`、`docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md`、`docs/SpringBootテスト入門/バリデーションテスト.md`、`docs/SpringBootテスト入門/DBを含む統合テスト入門.md`、`docs/SpringBootテスト入門/SpringSecurityテスト入門.md`、`docs/SpringBootテスト入門/E2Eテスト入門.md`。
- 次の対象: `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md`。取得済み範囲（1行目 frontmatter、73行目付近の「プロジェクトの構成」）から評価を再開し、残り本文へ進む。
- 未実施: 横断レビュー（用語・パス・ポート・バージョン・リンク・章間前提・成果物の引き継ぎ）、確定指摘の修正、修正後の再読、`npm run textlint` / `npm run build` の再検証、最終レポート作成。
- 解消済み候補: Vol1 の `thymeleaf-extras-springsecurity6` 依存追加指摘は、配布 ZIP v1.0.0 の pom.xml に既に含まれるため最終指摘から除外する（配布物確認済み）。

## `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md` 1-137 行の所見（部分評価）

読了範囲: 1-137 行（frontmatter から「1. DTO の作成」の途中まで）。138 行目以降は未読。

- **整合OK（バージョン）** frontmatter は `date: "2025-11-30"`。Spring Initializr の指定は Spring Boot 4.0.2・Java 21 で、`introduction.md` の「Spring Boot 4.0 系」「JDK 21」前提と一致する。
- **整合OK（章間前提）** 前提は「Java のクラスとコンストラクタを理解」で、`introduction.md` の対象者記述と矛盾しない。「やらないこと」に認証・DB アクセスを挙げ、後続章（Security・DB アクセス）へ委ねる構成も一貫している。
- **整合OK（リンク）** `[学習ガイド](../introduction.md)` は `docs/SpringBoot入門/` から `docs/introduction.md` を指し、実在ファイルと一致。
- **整合OK（成果物の引き継ぎ）** 終了条件「API 起動・設定変更・jar 実行」は本文の「やること」（Initializr・API 実装・application.yaml・jar デプロイ）と対応している。
- **指摘候補（P2・日付依存の固定例）** 設計節のレスポンス例が `{ "age": 36 }` と固定値。`birthDay=1990-01-01` の年齢は実行日で変わるため、frontmatter の日付 2025-11-30 時点では 35 歳となり例と一致しない（2026-10-24 現在は 36 で一致）。読者が実行すると例と違う値が出て混乱しうるため、「実行日時点の年齢」である旨の注記が望ましい。
- **整合OK（構成）** ディレクトリ構成の `ProjectcreateApplication.java` / `ProjectcreateApplicationTests.java` / `application.yaml` は Initializr の Artifact `projectcreate`・Configuration YAML の指定と整合。
- **未確認** 138 行目以降の DTO/Service/Controller 実装、application.yaml のプロファイル・環境変数、jar デプロイ手順は未読。

補助検証（ユーザー提供・未実行のまま記録）: 本文の Java 3 ブロックと YAML 3 ブロックを抽出し、Spring Boot 4.0.2 / release 21 / JDK 25 でコンパイルと HTTP 検証が成功。local/prod のログレベル、200/400/500 の応答が本文記載どおり。ただし Java 21 ランタイム・Initializr 操作・jar 配置の実行保証ではない。

## `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md` 137-274 行の所見（部分評価）

読了範囲: 137-274 行（DTO・Service・Controller の実装と各解説の途中まで）。275 行目以降は未読。

- **整合OK（コードと設計）** DTO は `age` のみ、Service は `Period.between` で年齢を算出、Controller は `@RequestMapping("/api/calc-age")` + `@GetMapping` + `@RequestParam("birthDay")` で、設計節のエンドポイント・クエリパラメータ・レスポンス形式と一致する。
- **整合OK（パッケージ）** 3 クラスとも `dev.mikoto2000.workshop.projectcreate.calcage.*` 配下で、起動クラスのパッケージ配下に置く指示と一致する。
- **整合OK（学習設計）** `LocalDate.now()` を使う箇所に「日付を固定できる設計が役立つ」と注記し、テスト入門章へリンクしている。後の章への前提作りとして妥当。
- **指摘候補（P2・エラー時の挙動の未説明）** Service は未来日・null で `IllegalArgumentException` を投げるが、Controller 側に例外処理がなく、実際には 500 になる。本文はこの時点でエラー応答の説明をしておらず、読者は「不正な入力で 500 が返る」理由を追えない。例外処理章へ委ねる旨の一言があると親切（P2、必須ではない）。
- **整合OK（表記制御）** Service コードブロックは `<!-- textlint-disable -->` で囲まれ、Javadoc の日本語に対する textlint 誤検出を避ける意図が読み取れる。
- **未確認** 275 行目以降の `@DateTimeFormat` 解説の続き、application.yaml（プロファイル・環境変数）、jar デプロイ手順は未読。

## `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md` 274-396 行の所見（部分評価）

読了範囲: 274-396 行（`@DateTimeFormat` 解説の続き、プロジェクト実行、application.yaml とプロファイル、環境変数、ビルド・デプロイの冒頭）。397 行目以降は未読。

- **解消（先の P2 候補）** 274-278 行に「不正な日付形式やパラメータの省略は 400」「未来の日付では 500」「入力検証とエラー応答は次のバリデーション入門で学びます」と明記されている。137-274 行の評価で挙げた「500 の理由が追えない」は本文内で解消。最終指摘に含めない。
- **解消（先の P2 候補）** 実行例の `{"age":36}` の直後に「応答例（年齢は実行日によって変わります）」「出力例の `36` は、2026 年に実行した場合の値です」と注記されている。1-137 行の評価で挙げた固定値の懸念は解消。最終指摘に含めない。
- **整合OK（設定と説明）** `server.port: ${CREATEPROJECT_SERVER_PORT:8080}`、`spring.profiles.active: local`、`group.local: [local-logging]`、`group.prod: [prod-logging]` と、`application-local-logging.yaml`（root: DEBUG）/ `application-prod-logging.yaml`（root: INFO）の対応が一貫している。
- **整合OK（章間前提）** 「実際に切り替える方法は、次の章の動作確認で説明します」とあり、次章（DB アクセス）への引き継ぎが明示されている。
- **整合OK（注意書き）** DB 設定ファイルの追加例に対し「ファイルを作るだけではグループに含まれません」と補足があり、プロファイルグループの落とし穴を先回りして説明している。
- **整合OK（ポート）** 待ち受けは 8080 既定で、`introduction.md` の「8080 番ポートを使うアプリを同時に起動すると競合」という注意と矛盾しない。
- **未確認** 397 行目以降の jar ビルド・起動・デプロイ手順は未読。

補助検証（ユーザー提供・未実行のまま記録）: 本文の Java 3 ブロックと YAML 3 ブロックを抽出し、Spring Boot 4.0.2 / release 21 / JDK 25 でコンパイルと HTTP 検証が成功。local/prod のログレベル、200/400/500 の応答が本文記載どおり。Java 21 ランタイム・Initializr 操作・jar 配置の実行保証ではない。

## `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md` 396-442 行の所見（読了）

読了範囲: 1-442 行（全体）。

- **整合OK（ビルド成果物）** `./mvnw clean package` → `target/projectcreate-0.0.1-SNAPSHOT.jar` は、Initializr の Artifact `projectcreate` と既定バージョン `0.0.1-SNAPSHOT` に一致する。
- **整合OK（プロファイル切替）** `--spring.profiles.active=prod` の指定と「`application-prod-logging.yaml` が適用される」説明が、設定節のプロファイルグループ定義と一致する。
- **整合OK（終了条件）** まとめで「API が年齢を返す」「jar で起動できる」「local / prod でログの量が変わる」を確認事項としており、「講義の区切り」の終了条件（API 起動・設定変更・jar 実行）と対応している。
- **整合OK（リンク）** 参考資料の 4 リンクはすべて `spring.pleiades.io` の公式日本語訳ドキュメントで、内容（DI・プロファイル・外部設定・@Bean）も本文の解説と対応している。
- **指摘候補（P2・実行環境の注意）** jar 起動は `java -jar` のみで、`事前準備` の「JDK 21 以上」と整合するが、ビルドは `./mvnw` 前提で Windows の `mvnw.cmd` に触れていない。プロジェクト構成の説明では `mvnw` / `mvnw.cmd` の両方を挙げているため、Windows 読者向けに `mvnw.cmd` の一言があると親切（P2、必須ではない）。
- **章全体の評価** 構成・コード・設定・実行手順が一貫し、学習目標（API 起動・設定変更・jar 実行）を満たす内容になっている。確定修正が必要な P0/P1 は本章には見当たらない。

補助検証（ユーザー提供・未実行のまま記録）: 本文の Java 3 ブロックと YAML 3 ブロックを抽出し、Spring Boot 4.0.2 / release 21 / JDK 25 でコンパイルと HTTP 検証が成功。local/prod のログレベル、200/400/500 の応答が本文記載どおり。Java 21 ランタイム・Initializr 操作・jar 配置の実行保証ではない。

## `docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md` 1-73 行の所見（部分評価・再確認）

読了範囲: 1-73 行（今回の実行で再取得・確認）。74-442 行は記録上「評価済み」と保存されているが、本実行のコンテキストには配信されていないため、この節の根拠には含めない。

- **整合OK（リンク）** `[学習ガイド](../introduction.md)` は `docs/SpringBoot入門/` から `docs/introduction.md` を指し、実在する。
- **整合OK（章の範囲）** 「やらないこと」の認証・DB アクセスは後続の Security 章・DB 章に委ねる構成で、章間前提と矛盾しない。
- **整合OK（前提）** 事前準備の「JDK 21 以上」と Initializr 設定の `Java: 21` は一致する。
- **要確認（P2・バージョン横断）** `Spring Boot: 4.0.2` 指定。他章の指定版と揃うかは横断確認で未検証。
- **要確認（P2・依存名）** Initializr の依存表示「Spring Web」は、Boot 4.0 系で starter 名が `spring-boot-starter-webmvc` に変わっている。表示名として妥当か、生成 pom の artifactId と一致するかは未確認（補助検証の POM は `spring-boot-starter-webmvc` を使用）。
- **要確認（P2・章間）** 「発展内容・次回の目安: デプロイ環境の比較」が introduction.md の章一覧表の該当行と一致するかは、表の該当行を本実行で再確認していないため未検証。
- **訂正記録** 部分所見にあった「次の章の動作確認」は、同一文書内の後続節「動作確認」を指す。DB 章への移行という解釈は誤りであり、最終指摘に含めない。

未確認の gap: 74-442 行の本文（設定変更・jar 実行・動作確認の各節）は本実行では未配信。当該範囲の評価は保存済み記録に依拠し、本節では再主張しない。

## `docs/SpringBoot入門/DBアクセス・トランザクション入門.md` 1-108 行の所見（部分評価）

読了範囲: 1-108 行（今回の実行で取得・確認）。109 行目以降は未取得・未評価。

- **整合OK（リンク）** `[学習ガイド](../introduction.md)` は `docs/SpringBoot入門/` から `docs/introduction.md` を指す。`[プロジェクト作成・デプロイ入門](./プロジェクト作成・デプロイ入門.md)` も同ディレクトリ内の実在ファイルと一致し、章間の前提順序（プロジェクト作成 → DB アクセス）も自然。
- **整合OK（前提）** 「Java 21、Maven Wrapper、Spring Boot 4.0 系」はプロジェクト作成章の `Java: 21` / `Spring Boot: 4.0.2` と矛盾しない。
- **整合OK（バージョン）** `mybatis-spring-boot-starter` 4.0.0 と「MyBatis Starter 4.0 系は Spring Boot 4.0 以降に対応」の記述は整合する。
- **整合OK（章の範囲）** 「Lombok と Spring Security は使いません」と明記され、Security 章との前提混同を避けている。
- **整合OK（API 仕様表）** エンドポイント表の成功時コード（201/200/204）と 404 条件が、本文の「ヘッダーだけで CRUD → 明細付き登録を追加」という段階構成と対応している。
- **要確認（P2・依存の重複）** Initializr で「JDBC API」を追加し、その後 `mybatis-spring-boot-starter` を追加する手順になっている。MyBatis Starter は JDBC 系を推移的に含むため、`spring-boot-starter-jdbc` が重複する可能性がある。本文には「Initializr で MyBatis Framework を選んだ場合は重複させない」旨の注意があるが、JDBC API を選んだ場合の重複には触れていない。実害は小さい見込みで、確定指摘にはしない。
- **未検証（P2・設定キー）** `spring-boot-h2console` 依存と `spring.h2.console.enabled` の組み合わせが Boot 4.0 系で有効かは、本実行では実行確認していない。補助検証は H2 を直接利用しており、H2 コンソール GUI は未検証と報告されている。
- **未検証（P2・初期化）** `spring.sql.init.mode: always` を設定しているが、この範囲にはスキーマ初期化ファイル（`schema.sql` 等）の説明がない。後続範囲で説明される可能性があるため、この時点では指摘にしない。

未確認の gap: 109 行目以降（Mapper/CRUD 実装、`@Transactional` 追加、座学パート、削除失敗の演習）は未取得。当該範囲の評価は未実施。

## `docs/SpringBoot入門/DBアクセス・トランザクション入門.md` 109-351 行の所見（部分評価）

読了範囲: 109-351 行（今回の実行で取得・確認）。352 行目以降は未取得・未評価。

- **整合OK（スキーマと DTO）** `schema.sql` の `customer_name VARCHAR(100)` / `product_name VARCHAR(100)` と、`OrderRequest`・`OrderWithItemRequest` の `@Size(max = 100)` が一致している。
- **整合OK（演習の意図）** `quantity INTEGER CHECK (quantity > 0)` と「数量に `@Positive` を付けていないのは、DB で失敗するケースを体験するため」という説明が対応しており、演習設計として筋が通っている。
- **整合OK（Mapper と Service）** `@Options(useGeneratedKeys = true, keyProperty = "id")` で採番 ID を `Order.id` に受け、`createWithItem` がその ID を `insertItem` に渡す流れが一貫している。`#{...}` と `@Param` の対応も全メソッドで揃っている。
- **整合OK（トランザクション前段）** 「まず、トランザクションを付けない状態で作成します」と明示され、`createWithItem` が途中失敗時にヘッダーだけ残る状態を作る構成になっている。後の `@Transactional` 追加演習の前提として妥当。
- **整合OK（Controller の応答）** エンドポイント表の 201/200/204/404 と実装（`created` / `ok` / `noContent` / `notFound`）が一致し、`Location` も `/orders/{id}` で返している。
- **整合OK（シェル差異）** Bash 用コマンドに加え、PowerShell では `mvnw.cmd` と `curl.exe`、`--data-binary @request.json` を使う旨の注記があり、環境差のつまずきを抑えている。
- **要確認（P2・設定の再実行性）** `spring.sql.init.mode: always` と `CREATE TABLE`（`IF NOT EXISTS` なし）の組み合わせは、同一 JVM 内で初期化が再実行されると失敗し得る。この章の依存に DevTools は含まれないため実害は生じにくいが、後で DevTools を足す読者には注意が必要。確定指摘にはしない。
- **未検証（P2・依存モジュール名）** `spring-boot-h2console` という artifactId と `spring.h2.console.enabled` が Boot 4.0 系で有効かは、本実行では確認していない。補助検証も H2 コンソール GUI は未検証と報告している。
- **未検証（P2・マッピング）** `SELECT id, customer_name AS customerName` の別名が H2 で `CUSTOMERNAME` に正規化されても MyBatis の自動マッピングで `customerName` に解決されるかは、本実行では未確認（補助検証では H2 テスト 6 件が成功しており、実害はない見込み）。

未確認の gap: 352 行目以降（CRUD 動作確認の続き、`@Transactional` 追加、座学パート、削除失敗演習）は未取得。

## `docs/SpringBoot入門/DBアクセス・トランザクション入門.md` 352-400 行の所見（部分評価）

読了範囲: 352-400 行（今回の実行で取得・確認）。401 行目以降は未取得・未評価。

- **整合OK（動作確認の期待値）** 「順に 201・200・204・200・204・404」は、直前の 6 コマンド（POST → GET → PUT → GET → DELETE → GET）と一致する。ID が 1 以外の場合の読み替え案内もある。
- **整合OK（失敗時の観察）** 数量 0 で 500、ヘッダー 1 件・明細 0 件という結果は、`CHECK (quantity > 0)` とトランザクション未設定の組み合わせから導ける。説明も「呼び出しごとにコミットされる」と原因を明示している。
- **整合OK（H2 コンソール）** `jdbc:h2:mem:orders` / `sa` / パスワード空欄は `application.yaml` の設定と一致する。同一 JVM 内のメモリー DB なので、`DB_CLOSE_DELAY=-1` によりアプリ稼働中は接続できる。
- **整合OK（エラー本文の扱い）** 「エラー本文の形式は、この教材ではまだ指定していません」と範囲を限定しており、例外処理章への引き継ぎとして妥当。
- **要確認（P2・演習の順序）** アプリを停止してから `@Transactional` を追加する手順だが、H2 はメモリー DB のため停止でデータが消える。読者が「修正前の残存データ」と比較したい場合に再現できないが、本文は修正後に再実行する流れなので実害は小さい。確定指摘にはしない。
- **未検証（P2・`delete` の境界）** `delete` に `@Transactional` を付けると、`deleteItems` 成功後に `delete` が 0 件でもコミットされる。存在しない ID への DELETE は 404 で返るため仕様上の矛盾はないが、この範囲では明示的な説明がない。後続範囲で扱われるか確認する。

未確認の gap: 401 行目以降（`@Transactional` 適用後の確認、座学パート、削除失敗演習）は未取得。

## `docs/SpringBoot入門/DBアクセス・トランザクション入門.md` 401-482 行の所見（部分評価）

読了範囲: 401-482 行（今回の実行で取得・確認）。483 行目以降は未取得・未評価。

- **整合OK（修正後の確認手順）** 「再起動すると DB は空になります」と明示し、同一 DB 内で成功例と失敗例を順に送る手順にしているため、比較が成立する。期待値（`Success` ヘッダー・明細各 1 件、`AfterTransaction` ヘッダー 0 件）は `@Transactional` の効果と一致する。
- **整合OK（ID の注意）** 「ロールバック後の ID は欠番になる場合があります。連番であることを成功判定に使わないでください」と、H2 の IDENTITY 採番の挙動を正しく注意している。
- **整合OK（テストの落とし穴）** 「テストメソッドに `@Transactional` を付けると Service の設定漏れを見逃す場合がある」という注意は、ロールバック前提のテスト設計として妥当。DB を含む統合テスト章への引き継ぎにもなる。
- **整合OK（座学の責務表）** Controller / Service / Mapper の責務表と本文の説明が一致し、`Mapper` が Repository 相当であることも明記されている。
- **整合OK（バインド変数）** `#{name}` と `${name}` の危険性の対比、テーブル名・ソート項目は許可候補から選ぶ設計、という説明は正確。
- **整合OK（更新件数）** 「SELECT で存在確認してから更新すると競合し得るため、更新件数で判断する」という説明は、実装（`== 1`）と一致している。
- **要確認（P2・用語）** 座学で ACID の「原子性・整合性・分離性・永続性」を表にしているが、本文中では「原子性」以外の語を以後使っていない。用語の導入としては過不足なし。指摘にしない。

未確認の gap: 483 行目以降（H2 の永続性に関する説明の続き、プロキシ境界、削除失敗演習、まとめ）は未取得。

## `docs/SpringBoot入門/DBアクセス・トランザクション入門.md` 483-546 行の所見（読了）

読了範囲: 1-546 行（全体）。

- **整合OK（プロキシ境界）** 自己呼び出しの例（`register` から `this.createWithItem`）と「`new OrderService(...)` はプロキシを通らない」という説明は正確で、`@Transactional` の典型的な落とし穴を押さえている。
- **整合OK（ロールバック条件）** 「標準設定では `RuntimeException` と `Error` が対象」「チェック例外は `rollbackFor` 指定」「catch して正常終了すると成功扱いになり得る」という説明は Spring の仕様と一致する。
- **整合OK（DB 制約違反の扱い）** MyBatis-Spring がデータアクセス例外へ変換し、非チェック例外としてロールバック対象になる、という因果が本文の演習結果（500 とヘッダー 0 件）と一致する。
- **整合OK（発展内容の予告）** 「発展内容・次回の目安: プロキシ境界・削除失敗」に対し、プロキシ境界は本文で説明済み、削除失敗は次回扱いとして予告されている。本文に節がないことは不整合ではない。
- **整合OK（リンク）** 末尾の `[例外処理・エラー応答入門](./例外処理・エラー応答入門.md)` は同ディレクトリの実在ファイルと一致し、章順（DB アクセス → 例外処理）とも整合する。参考資料 4 件は MyBatis / Spring の公式ドキュメントで、内容（Starter 対応、トランザクション、`@Transactional`、ロールバック条件）と対応している。
- **整合OK（まとめ）** まとめの 4 項目は本文の主張（責務分離、更新件数、途中失敗の DB 状態確認、呼び出し方と例外伝播への依存）を過不足なく要約している。
- **補助検証の反映** 補助担当の隔離実行で、教材設定（`spring-boot-h2console` と `spring.h2.console.enabled`）による `/h2-console/` の HTTP 200 と H2 契約テスト 6 件成功が確認されている。これにより、本記録の 1-108 行節にあった「H2 コンソール設定が有効か未検証」および 109-351 行節の「カラム別名マッピングが未確認」は、**不具合候補としては解消**。GUI ログイン操作と PostgreSQL コンテナーは引き続き未検証。

**章の判定: 確定指摘なし（P0/P1/P2 いずれも修正不要）。** 未検証は PostgreSQL 実行と H2 コンソール GUI 操作のみで、教材記述の誤りを示す根拠はない。

## `docs/SpringBoot入門/DBマイグレーション入門.md` 1-269 行の所見（読了）

読了範囲: 1-269 行（全体）。

- **整合OK（リンク）** `[学習ガイド](../introduction.md)` は `docs/SpringBoot入門/` から `docs/introduction.md` を指す。Initializr リンクのパラメーター（`platformVersion=4.0.2`、`jvmVersion=21`、`configurationFileFormat=yaml`、`packageName=dev.mikoto2000.springboot.flyway`）は本文の説明と一致する。
- **整合OK（バージョン）** Spring Boot 4.0.2 / Java 21 はプロジェクト作成章・DB アクセス章の指定と揃っている。
- **整合OK（設定と説明）** `jdbc:h2:file:./target/workshopdb` と「`./mvnw clean` で演習データも削除される」「履歴確認中は clean しない」という注意が対応している。ファイル DB を使う理由（起動ごとの初期化を避ける）も明示されている。
- **整合OK（命名規則）** `V` + バージョン + `__` + 説明 + `.sql` の説明、`V20260203.01` / `V20260203.02` / `V20260203.01.1` の実例、`20260203.01 < 20260203.01.1 < 20260203.02` という数値区切り比較の説明は Flyway の仕様と一致する。
- **整合OK（outOfOrder の説明）** 「既定の `outOfOrder=false` では追加版が ignored となり検証エラー」「`outOfOrder=true` は適用順が異なる環境でも同じスキーマになるか検証する」「`repair` はスキーマを直さない」という記述は正確。補助担当の隔離実行（`FlywayValidateException` → `outOfOrder=true` で 1 件適用、ROLE/NOTE 列参照可）とも一致する。
- **整合OK（章の区切り）** 「終了条件は『2本のマイグレーションと履歴を確認』」に対し、本文は 2 本の適用と `flyway_schema_history` の 2 件・`SUCCESS=true` 確認までを扱っている。「発展内容・次回の目安: 並行開発・outOfOrder」に対しても発展節が用意されている。
- **整合OK（安全側の注意）** 「適用済みファイルは編集しない」「履歴テーブルを消したり手動変更しない」「共有環境では履歴を維持」「DB リセットは使い捨て環境に限る」と、運用上の危険を適切に抑えている。
- **整合OK（expand/contract）** カラム追加 → 値埋め → 新アプリ利用 → 必要なら NOT NULL、という段階的適用の説明は妥当で、「この教材で実行するのはカラム追加まで」と範囲も限定している。
- **P2 候補（修正不要と判断）** 起動ログ例の最終行が `Database available at 'jdbc:h2:file:./` で途切れている。実際の出力は `jdbc:h2:file:./target/workshopdb` まで続くはずで、読者には切り詰めと分かる程度の軽微な表示問題。学習上の誤解（接続先の誤認）は本文の設定記述で防げているため、確定修正にはしない。
- **P2 候補（修正不要と判断）** Initializr リンクの依存に `data-jdbc` が含まれるが、本文に JDBC API を直接使うコードはない。害はなく、Flyway の動作には影響しない。
- **未検証** H2 コンソールの GUI ログイン操作そのものは補助検証の対象外（`/h2-console` の HTTP 200 と画面本文までは確認済み）。

**章の判定: 確定指摘なし（P0/P1/P2 いずれも修正不要）。**

### 補足: `DBマイグレーション入門.md` 発展節の到達確認

- 確認できた範囲: 「並行開発に向けたマイグレーション運用例（発展）」の導入、draft と migration を分ける運用方針、`feature branch を作成 → draft ディレクトリ内で SQL を書く → PR（draft状態）→ レビューOK → migration へ移動（Vxxxに変換）→ PR更新 → 最終レビュー → Pull Request をマージ` の流れ図、および「この運用をするには次の条件を満たす必要があります」の箇条書き冒頭2項目（Git のブランチと Pull Request を使ったレビューができる／draft の SQL を試すための一時的な開発…）。
- **未確認の gap**: 上記条件リストの残り（3項目目以降）と、発展節の結び。配信が当該位置で切れており、本文末尾までの全文は本実行では確認できていない。したがって「269 行全体を読了」とは主張しない。
- 確認できた範囲に関する限り、運用例は「適用済みファイルを編集しない」「バージョン衝突を避ける」という本文前半の原則と矛盾しない。
- この gap は章の確定指摘には影響しない見込み（発展節は任意読者向けの運用例）。ただし読了判定は保留する。

### 補足: `DBマイグレーション入門.md` 228-269 行の評価（末尾確定）

読了範囲: 228-269 行（raw 結果の末尾ページで実際に確認）。これにより前節の「読了判定は保留」は解消する。

- **整合OK（発展節の条件）** 並行開発運用の条件 3 項目が揃っている。特に 228 行目「draft の SQL を試すための、一時的な開発用 DB がある」は、draft を Flyway の自動実行対象外とする説明（「draft ディレクトリは Flyway が自動実行する場所ではありません」）と対応しており、運用上の前提として妥当。
- **整合OK（安全側の注意）** 「DB をリセットするのは使い捨ての開発環境に限り、共有環境では適用済みの履歴を維持します」は、本文前半の「適用済みファイルは編集しない」「履歴テーブルを手動変更しない」と一貫している。
- **整合OK（outOfOrder 演習）** `V20260203.01.1__add_account_note.sql` の追加、`20260203.01 < 20260203.01.1 < 20260203.02` の比較、既定 `outOfOrder=false` での検証エラーと note 列未追加、確認後のファイル削除と再起動、`outOfOrder=true` の検証条件、`repair` がスキーマを直さない旨、公式リンクの提示まで、筋が通っている。補助担当の隔離実行結果（`FlywayValidateException` → `outOfOrder=true` で 1 件適用、ROLE/NOTE 列参照可）とも一致する。
- **整合OK（まとめ・参考資料）** まとめは本文の主張（基本挙動の理解と自プロジェクトへの適用検討）に沿う。参考資料の expand/contract 3 件は、本文の段階的適用の説明と対応する外部記事で、リンク先の主題も一致している。
- **P2 候補（修正不要と判断）** 起動ログ例の `Database available at 'jdbc:h2:file:./` という途中で切れた表示は、実際の出力が `./target/workshopdb` まで続くはずで、軽微な表示上の切り詰め。接続先は本文の設定記述と H2 コンソール手順で特定できるため、学習上の誤解は生じにくい。確定修正にはしない。

**章の判定: 確定指摘なし（P0/P1/P2 いずれも修正不要）。** 未検証は H2 コンソール GUI のログイン操作のみ。

## バリデーション入門（docs/SpringBoot入門/バリデーション入門.md）

到達範囲: 1-520行を取得し評価済み（521行以降は未評価）。根拠: readMultiFile 1-520、version 73de8e2f95e2d048fa14aeddae78b199183a46c1f0a660ff21a211581be5809f。

評価（1-520行）:
- 前提と依存（Spring Web / Validation / Lombok、Spring Boot 4.0系、Java 21、パッケージ名）はプロジェクト作成章と整合。
- 制約表と `@Size` の UTF-16 コードユニット説明は正確。`@Size`/`@Pattern` が null を許可する点と `@NotNull` 併用の注意も妥当。
- `@RequestParam` 直接定義: `@Length(min=1,max=10)` + `@Pattern("[0-9]*")` + `defaultValue="0"`。空文字が defaultValue に置換される説明、11桁400・省略時200の表は整合。クラスに `@Validated` を付けない理由（Spring MVC の組み込みメソッド検証）も正確。
- `@RequestBody`: `@Valid` + `@NotNull` の例、`@Valid` を外すと検証されない旨、`@NotNull` は空文字を許可する旨は正確。
- 相関チェック: `@AssertTrue` の getter、null を `@NotNull` に委ねる実装、`right == 0` の unboxing 比較はいずれも成立。
- 独自制約: `RangeEx`（`others()` 必須）と `RangeExValidator`（null は true、others 優先、範囲判定）は整合。`Integer` 専用という注意書きもある。
- 個別メッセージ指定と `ValidationMessages.properties` のキー（jakarta / hibernate の別）は妥当。

保留（521行以降で確認する項目）:
- 「言語別デフォルト指定」節と、既定 `ValidationMessages.properties` に日本語を置く構成の重複・優先順位の説明が矛盾しないか。
- 発展課題の成功条件と解答の有無。

現時点の確定指摘: なし（P0/P1/P2 いずれも未確定）。次カーソル: 521行以降。

### バリデーション入門 521-683行の評価（章完了）

到達範囲: 521-683行を取得し評価済み。これで1-683行の全文評価が完了。根拠: readMultiFile 521-683、version 73de8e2f95e2d048fa14aeddae78b199183a46c1f0a660ff21a211581be5809f。

評価:
- 言語別メッセージ（`ValidationMessages_en/ja.properties`）、`Accept-Language` による選択、アノテーション直接指定の `message` は言語別ファイルで切り替わらない旨はいずれも正確。
- `@RestControllerAdvice` の3例外（`HandlerMethodValidationException` / `MethodArgumentNotValidException` / `HttpMessageNotReadableException`）の使い分けは Spring Framework 6.1+ / Boot 4.0 の実際の挙動と一致。`getParameterValidationResults()` / `getMethodParameter().getParameterName()` / `getResolvableErrors()` / `getFieldErrors()` の API も正しい。
- 戻り値制約も `HandlerMethodValidationException` になる点を明記し、サーバー側エラーとして分けるよう案内しているのは適切。
- 応答例 `{"errors":[{"field":"stringValue","message":"値を入力してください。"}]}` は直前の `ValidationMessages.properties` の `NotNull` 定義と整合。
- 壊れた JSON で内部例外メッセージを出さない方針も妥当。

確定指摘（P2・1件）:
- P2: `ValidationMessages.properties` の定義一覧に `jakarta.validation.constraints.NotBlank.message` がない。しかし本文（`@NotNull` の説明箇所）は「空文字も拒否したい項目は `@NotBlank` を使います」と `@NotBlank` の利用を勧めている。このファイルをそのまま使うと `@NotBlank` 違反だけ Hibernate Validator 内蔵の英語既定メッセージ（must not be blank）へフォールバックし、日本語 API の中で英語メッセージが混在する。再現可能で学習者の混乱につながる。修正案: 一覧に `jakarta.validation.constraints.NotBlank.message=値を入力してください。` を追加する。

確認事項（確定指摘にしない）:
- 既定の `ValidationMessages.properties` が日本語のため、`_en` を作らない場合 `Accept-Language: en` でも日本語が返る。ただし直後の節で `_en`/`_ja` の作成を案内しており、手順どおり進めれば解消するため不具合とはしない。

章の到達状況: バリデーション入門は全文評価完了。次は docs/SpringBoot入門/ロギング入門.md。

### 訂正: バリデーション入門の NotBlank P2 を撤回

補助担当の隔離実行（Boot 4.0.2、教材の Java と ValidationMessages.properties をそのまま使用、Spring 管理の Validator、NotBlankFallbackTest 1件成功）により、独自ファイルに `NotBlank.message` がなくても Hibernate Validator 内蔵の日本語バンドルへフォールバックし、LocaleContextHolder=ja で「空白は許可されていません」、en で「must not be blank」となることを確認した。
したがって「日本語 API に必ず英語が混在する」という P2 判定は誤りであり撤回する。本文へのキー追加は行わない。バリデーション入門の確定指摘は 0 件。

## ロギング入門（docs/SpringBoot入門/ロギング入門.md）

到達範囲: 1-421行を取得し評価済み（421行以降は未評価）。根拠: readMultiFile 1-421、version c9f1668b24f526ffaf75ab363a7355ca326b53b2988dd1d9c99145cada28506f。

評価（1-421行）:
- 2部構成（ハンズオン→座学）と対応表のアンカーは、見出しの slug と一致（`#controller-の開始終了ログを追加`、`#mdcmapped-diagnostic-context-の追加`、`#ログレベルtrace--debug--info--warn--errorとフィルタリング` 等）。
- ログレベル表と `TRACE < DEBUG < INFO < WARN < ERROR` の説明は正確。
- `AccessLogFilter`: `OncePerRequestFilter` + `finally` で例外時も記録、`success` は「後続処理から例外が伝わったか」の指標であり HTTP ステータスとは別、という説明は実装と一致。404 が SUCCESS になる理由（DispatcherServlet が例外を投げず 404 応答を返す）も本文の記述と整合。
- `/fireException` で `status=200, success=FAIL` になる理由（Filter の finally がコンテナのエラー応答生成より先に走る）を明示し、`curl -i` で最終 500 と比較するよう案内している点は正確で、初学者の誤解を防いでいる。
- `LoggingAspect`: `within(...controller..*)` の Pointcut 説明、`@Around` の前後処理、例外時の `log.error(..., e)` でスタックトレースを残す実装は妥当。
- 補助担当の隔離実行（Rei 自身の実行ではない）で、教材の最終 Java と MDC 入り Logback XML による3テスト（INFO/WARN、CRLF 除去、AOP 開始終了とアクセスログの同一 traceId、リクエスト間の別 ID、MDC 後始末、ERROR のスタック、404 の SUCCESS）が成功している。

確認事項（確定指摘にしない）:
- `pom.xml` に追加する `spring-boot-starter-aspectj` の artifactId は、補助検証が教材 pom 自体を用いたか明示されておらず未確認。Spring Boot 4.0 系の starter 名として妥当と判断するが、依存解決そのものは未検証として残す。

現時点の確定指摘: なし。次カーソル: 421行以降（offset 14）。

### ロギング入門 421-760行の評価

到達範囲: 421-760行を取得し評価済み（760行以降は未評価）。根拠: readMultiFile 421-760、version c9f1668b24f526ffaf75ab363a7355ca326b53b2988dd1d9c99145cada28506f、offset 77。

評価:
- MDC 導入: `MDC.put("user"/"traceId")` を Filter 入口、`MDC.remove` を finally で実行する完成コードは、スレッド再利用時の値漏れを防ぐ形になっている。
- `logback-spring.xml`: `defaults.xml` と `console-appender.xml` を include し `CONSOLE_LOG_PATTERN` を上書きする構成は Spring Boot の標準的なカスタマイズ方法。`%X{traceId:-}` の既定値記法も正しい。
- 出力例で AOP の START/END と ACCESS_LOG が同一 traceId、リクエスト間で別 ID になることが示され、本文の主張と一致。補助担当の隔離実行（Rei 自身の実行ではない）でも同一 traceId・別 ID・MDC 後始末が確認済み。
- MDC 補足: 「スレッドごとの Map」「非同期へ自動伝播しない」「finally で削除」は正確。
- user 補足: `SecurityContextHolder` + `AnonymousAuthenticationToken` 判定の断片例は妥当。認証未導入のため `dummy` のまま進める旨も明示。
- 業務ログ: `users.add` / `users.remove` の戻り値で INFO/WARN を出し分ける実装は正しい。`logValue` による制御文字置換（`Character.isISOControl`、U+2028/U+2029）と80コードポイント制限は、ログインジェクション対策として妥当。`A%0AB` → `A?B` の確認手順も再現可能。
- 「プレースホルダーだけでは改行を無害化できない」という説明は正確（SLF4J の `{}` は改行を除去しない）。
- 座学パート冒頭で「コードは概念説明の抜粋でファイル全体として配置しない」と明示され、ハンズオン完成コードの所在も案内されている。
- ログ・トレース・メトリクスの比較表と「点・線・面」の整理は妥当。

確定指摘: なし。

未評価の残り: 760行以降（座学パートの残り、構造化ロギング、設定例など）。次カーソル: 760行以降（offset 77）。

### ロギング入門 760-1103行の評価

到達範囲: 760-1103行を取得し評価済み（1103行以降は未評価）。根拠: readMultiFile 760-1103、version c9f1668b24f526ffaf75ab363a7355ca326b53b2988dd1d9c99145cada28506f、offset 10。

評価:
- ログレベル表に `OFF` を加え「重要度ではなく出力停止の設定値」と注記している点は正確。
- `logging.level` のパッケージ／クラス単位指定、`application-dev.yaml` / `application-prod.yaml`、`-Dspring-boot.run.profiles=dev`、PowerShell の `'...'` クォート例はいずれも妥当。
- `logging.file.name` + `logging.logback.rollingpolicy.max-file-size/max-history` は Spring Boot の正しいプロパティ名。独自 `logback-spring.xml` があると FILE appender が作られない旨を明記し、改名手順を示しているのは実務的に正確。
- 「標準の Logback 設定は日付とサイズの両方でローテーション」は正確。
- SLF4J のファサード性、`{}` プレースホルダの遅延評価、Logback が既定実装である点、Log4j2 は別途依存が必要な点は正確。
- 例外ログ: `log.error(..., e)` でスタックトレースを含める、`printStackTrace()` を避ける、Advice と二重記録しない、という説明は妥当。
- パフォーマンスログ: `System.nanoTime()` の差と `TimeUnit.NANOSECONDS.toMillis` の使い分け、日時は Logback に任せる方針は正確。
- MDC の説明（スレッドごとのキー・バリュー、非同期へ自動伝播しない、finally で削除）は前半の実装と整合。
- 構造化ロギングの動機（検索・集計・相関、フォーマット揺れと改行の混入）は妥当。

候補（P2・本文後半で解消するか確認中）:
- 座学の「MDC のパターン設定」で設定ファイル名を `logback.xml` としているが、ハンズオンおよび直前の節では `logback-spring.xml` を使っている。Spring Boot では `logback.xml` は Spring の拡張（`<springProfile>` など）が使えず、既定設定の取り込みも変わうため、同じ教材内でファイル名が揺れると学習者がどちらを作るか迷う。後半で `logback-spring.xml` に言及があるか確認してから確定する。

次カーソル: 1103行以降（offset 10）。

### ロギング入門 1103-1146行の評価（章完了）

到達範囲: 1103-1146行を取得し評価済み。これで1-1146行の全文評価が完了。根拠: readMultiFile 1103-1146、version c9f1668b24f526ffaf75ab363a7355ca326b53b2988dd1d9c99145cada28506f。

評価:
- 構造化ログ: `logging.structured.format.console: logstash` は Spring Boot 3.4+/4.0 の正しいプロパティ。`logback-spring.xml` を退避しないと反映されない旨、JSON 例は項目説明用で実際の項目名は形式に従う旨、業務数値は名前付き項目で出す旨、公式リンク（`#features.logging.structured`）まで整合。
- 用語集は本文の記述と矛盾なし。`OFF` を「出力停止の設定値」と補足している点も良い。
- 参考資料の3リンクは妥当。

確定指摘（P2・1件）:
- P2: 座学「MDC のパターン設定」節が設定ファイル名を `logback.xml` としている（本文「`logback.xml` のパターンに MDC キーを組み込むと…」およびコードブロック title）。しかし同一章のハンズオンと構造化ログ節は一貫して `logback-spring.xml` を使っており、Spring Boot では `logback.xml` を置くと Spring Boot 既定設定の取り込みと `<springProfile>` などの拡張が使われない。初学者がこの節だけを見て `logback.xml` を作ると、直前に設定した既定パターンやプロファイル別設定が失われる。修正: 節の説明文とコードブロック title を `logback-spring.xml` に統一し、`logback.xml` との違いを一文添える。

章の到達状況: ロギング入門は全文評価完了。確定指摘1件（P2）を修正対象とする。次は docs/SpringBoot入門/例外処理・エラー応答入門.md。

### 訂正: ロギング章 P2 の断定表現を修正

公式資料（https://docs.spring.io/spring-boot/4.0/reference/features/logging.html#features.logging.custom-log-configuration および #features.logging.logback-extensions）で確認した。`logback.xml` 自体もサポートされるが、標準の読み込み時点が早いため Spring Boot 拡張（`<springProfile>` など）には `logback-spring.xml` または `logging.config` の指定が必要、というのが正確な理解である。
したがって「logback.xml では Boot の既定設定を一切取り込めない」「必ず既存の設定が失われる」という断定は根拠がないため撤回する。
確定指摘の位置づけは「学習者が別ファイルを作らないための説明整合性の修正」であり、ロギング機能そのものが壊れているという意味ではない。修正は MDC 節を既に作成した `logback-spring.xml` に統一し、コードが既存パターンへ組み込む断片であることを明示する最小変更にとどめる。

## 例外処理・エラー応答入門（docs/SpringBoot入門/例外処理・エラー応答入門.md）

到達範囲: 1-543行を取得し評価済み（543行以降は未評価）。根拠: readMultiFile 1-543、version 83b9f09449fac87f0c599bc661b181021100658c83f6d12f1abdbcf690debd4d、offset 8。

評価（1-543行）:
- 前提（Java 21、Boot 4.0系、Spring Web + Validation、DB/Lombok/Security 不使用）と独立プロジェクトである旨が明示され、前後章との依存が整理されている。
- `ProductRequest` の `@NotBlank @Size(max=50) @Pattern("[A-Za-z0-9_-]+")` は、`/`・`;`・空白を 400 で拒否する説明と一致。`@Size` と `@Pattern` の併用も妥当。
- `ProductService` の `putIfAbsent` による重複判定は原子的で、`ConcurrentHashMap` と整合。
- `ProductController` の `UriComponentsBuilder.fromPath(...).encode().buildAndExpand(...)` による Location 生成は正しく、`ResponseEntity.created(location)` も適切。
- `ApiExceptionHandler`: `ResponseEntityExceptionHandler` 継承、`ProblemDetail.forStatusAndDetail`、`setProperty("code")`、`setInstance(URI.create(path))`、`handleExceptionInternal` で元ステータスとヘッダーを維持する実装はいずれも Spring Framework 6/7 の API と一致。
- `handleExceptionInternal` の上書きで `body instanceof ProblemDetail` の分岐と `code` 有無の再構築を行う点は、405 など他の MVC 例外も共通形式にする意図と整合。
- `@ExceptionHandler(Exception.class)` で `errorId` を採番し、`log.error("Unexpected error. errorId={}", errorId, ex)` でスタックトレースを残しつつ応答には内部情報を出さない構成は正確。
- 404 応答例（`type: about:blank`、`title: Not Found`、`status`、`detail`、`instance`、`code`）は `ProblemDetail` の既定挙動と一致。
- `VALIDATION_ERROR` と `INVALID_JSON` の区別、JSON 読み取り失敗時に DTO 検証へ進まない旨、パーサー例外メッセージを返さない方針は正確。
- 405 で `HTTP_405` と `Allow` を維持する説明は、`handleExceptionInternal` の実装と整合。
- `ApiExceptionHandlerTest` の8テストは、404/409/400/500、`Location`、`errors[0].field`、内部情報の非公開、405 と `Allow` を網羅しており、`@WebMvcTest` + `@Import(ApiExceptionHandler.class)` + `@MockitoBean` の構成も妥当。
- 補助担当の隔離実行（Rei 自身の実行ではない）で、教材の最終 Java と ApiExceptionHandlerTest の8テストが成功している。

確定指摘: なし（1-543行の範囲）。

次カーソル: 543行以降（offset 8）。

## 例外処理・エラー応答入門（1〜674行、全文評価）

- 到達範囲: 1〜543行は既評価、543〜674行を今回取得し、章末（参考資料）まで確認した。
- 構成: ハンズオン（DTO/Service/Controller/Advice/テスト）→ 練習 → 座学（責務分離、ステータス表、RFC 9457、JSON読取と検証の違い、内部情報、トランザクション、契約テスト）→ まとめ → 参考資料。学習順序と前提は妥当。
- 技術的整合: 400/404/409/405/415/500 の分類表と本文の実装説明が一致。`HttpMessageNotReadableException`→`INVALID_JSON`、`MethodArgumentNotValidException`→`VALIDATION_ERROR` の対応も本文記述と整合。`HandlerMethodValidationException` の戻り値制約に関する注意書きも適切。
- 横断前提: ロギング章の MDC への参照リンク `./ロギング入門.md` は実在。トランザクション章のロールバック説明と矛盾しない。
- 補助担当の隔離実行（Rei 自身の実行ではない）: 教材の最終 Java と ApiExceptionHandlerTest 8件が成功。404/409/400/500、500 の内部メッセージ非公開、Location、405 の Allow を確認済み。
- 確定指摘: なし（本文の説明・コード・リンクに再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 実ブラウザーでの表示、実 PostgreSQL 上の一意制約違反の変換は未検証（H2/単体範囲）。

## テスト入門（1〜486行、部分評価）

- 到達範囲: 1〜486行（Repository のテストのコード途中でページング）。486行以降は未取得・未評価。
- 構成: 講義の区切り→概要→対象読者→事前準備→資料構成→ハンズオン（Util/Service/Repository/Controller/設計）→座学、の順。学習順序は妥当。
- 前提の整合: Java 21 / Spring Boot 4.0 系 / MyBatis Starter 4.0.0 / `spring-boot-starter-webmvc-test` を明示。他章（プロジェクト作成・DBアクセス）の前提と一致。
- 技術的整合: `UserUtil.getAge` の `Period.between` は本文の練習値（2000-01-01 基準で 2024-12-31→24、2025-01-01/02→25）と一致。`@MybatisTest` + `schema.sql` + `@Sql` の組み合わせも妥当。
- 見出しアンカー: 対応表のリンク（`#util-のテスト普通の-java-のテスト` 等）は見出し表記と整合。
- 確定指摘: なし（この範囲では再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 486行以降（Repository テストの残り、Controller テスト、テスト設計ハンズオン、座学）。

## テスト入門（486〜818行、部分評価）

- 到達範囲: 486〜818行（座学「テスト、テストレベル、テスト設計の違い」の途中でページング）。818行以降は未取得・未評価。
- 内容: Repository テストの残り（`@MybatisTest`/`@Sql`/`@Transactional`）、Controller テスト（`@WebMvcTest` + `@MockitoBean` + MockMvc）、テスト設計ハンズオン（仕様 R1〜R3、ケース表 U1〜U6、解答例 `UserUtilDesignTest`、振り返り）、座学冒頭。
- 技術的整合: U1〜U4 の期待値（2025-06-14→24、2025-06-15→25、2025-06-16→25、出生当日→0）は `Period.between` の挙動と一致。「6 ケースが実行され、失敗とエラーが 0 件」も U1〜U6 と整合。
- 学習設計: 「境界値は異常系ではない」という注意書き、期待値を実装からコピーしない指示、未定義仕様（両方 null・未来日・2/29）を対象外と明示する点は適切。
- 確定指摘: なし（この範囲で再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 818行以降（座学の残り全体）。

## テスト入門（818〜1078行、部分評価）

- 到達範囲: 818〜1078行（座学「テスト設計の実践例」の途中でページング）。1078行以降は未取得・未評価。
- 内容: 良いテストの特徴（高速・決定的・独立・読みやすい）、テストの基本原則、テストレベル表とピラミッド、JUnit Jupiter とアサーション表、ライフサイクル、命名、Mockito（モックの理由・使い方表・DI・注意点）、MockMvc（`@WebMvcTest`/`@MockitoBean`・検証表）、テスト設計（分類・境界値分析）。
- 技術的整合: 境界値例の3件（2000-01-01→25、2000-01-02→24、1999-12-31→25）はいずれも `Period.between` の結果と一致。`@Mock` と `@MockitoBean` の使い分け説明もハンズオン本文と整合。
- 学習設計: テストピラミッドの比率を「固定ルールではない」と補足し、モックの過用リスクも明記。バランスが取れている。
- 確定指摘: なし（この範囲で再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 1078〜1127行（章末）。

## テスト入門（1078〜1127行、章末評価・全文完了）

- 到達範囲: 1078〜1127行（テスト設計の注意点→まとめ→用語集→参考資料）。1〜1127行の全文を評価済み。
- 内容: テスト設計の注意点、まとめ、用語集（21項目）、参考資料3件。
- 技術的整合: 用語集の `@Transactional` 説明「テストメソッド実行後に DB をロールバックする」は本文の Repository 節と整合。`@MybatisTest` の JPA 対比（`@DataJpaTest`）も本文と一致。
- リンク: 参考資料の JUnit / Mockito / Spring Boot テストの3リンクは妥当。
- 確定指摘: なし。
- 章全体の所見: ハンズオン→演習→座学の順序、仕様 R1〜R3 とケース表 U1〜U6 の対応、期待値を実装からコピーしない指示、未定義仕様の対象外明示など、学習設計として良好。補助担当の隔離実行（Rei 自身の実行ではない）で UserControllerTest/UserMapperTest/UserServiceTest/UserUtilTest/UserUtilDesignTest 計11件成功。
- 未確認: 実 PostgreSQL 上の SQL、実ブラウザー表示。

## 開発者テスト入門_テストとは編（1〜243行、部分評価）

- 到達範囲: 1〜243行（「テストで確認することはひとつの振る舞いだけ」の途中でページング）。243行以降は未取得・未評価。
- 構成: 講義の区切り→はじめに（開発者テストの位置づけ・対象読者・到達目標・スコープ外）→読み進め方→テストレベルとアノテーションの目安→テストの狙い（TDD/回帰）→ブラックボックス/ホワイトボックス→AAA パターンとコード例。
- 技術的整合: `AgeValidator.isValid` の `0 <= age && age <= 120` と、120 受理・121 拒否のテストは整合。`./mvnw -Dtest=AgeValidatorTest test` と Windows の `mvnw.cmd` 併記も妥当。
- 学習設計: 「ホワイトで見つけて、ブラックの形で追加する」という運用指針、期待値を実装から再計算しない指示、AAA のフェーズを複数書かない原則（assert 数とは別）の補足は適切。
- 横断前提: `./テスト入門.md` への参照は実在し、事前準備のプロジェクトを前提とする旨も明記。
- 確定指摘: なし（この範囲で再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 243〜357行（残り）。

## 開発者テスト入門_テストとは編（243〜357行、章末評価・全文完了）

- 到達範囲: 243〜357行（1テスト1振る舞いの補足→テストケース設計の技法→まとめ→参考資料）。1〜357行の全文を評価済み。
- 内容: 同値分割（手順・年齢例・画像参照）、境界値分析（典型パターン・0〜120 の6ケース・画像参照）、発展技法（状態遷移・ペアワイズ）、Pairwise の保証範囲（3因子2値で4ケース、全組み合わせ8ケース、A・X・英語の検知不能例）、まとめ、参考資料。
- 技術的整合: 境界値の6ケース（-1/0/1/119/120/121）は `0 <= age && age <= 120` の仕様と整合。Pairwise の説明「任意の2因子の全ペアを含むが全組み合わせは保証しない」は正確で、具体例も妥当。
- 画像参照: `./image/開発者テスト入門_テストとは編/equivalence-partitioning.svg` と `boundary-value-analysis.svg` を参照。実ファイルの存在は未確認（横断確認で確認する）。
- 学習設計: 「数値でない入力は文字列→数値変換の入口が対象」という補足、実行不能な組み合わせの制約明示、発展技法を「必要になったら」と位置づける点は適切。
- 確定指摘: なし。
- 補助担当の隔離実行（Rei 自身の実行ではない）: 教材の AgeValidator とテスト2件が成功。
- 未確認: 画像アセットの実在（横断確認で確認予定）。

## バリデーションテスト（1〜573行、部分評価）

- 到達範囲: 1〜573行（UserControllerTest 完成コードの途中でページング）。573行以降は未取得・未評価。
- 構成: 講義の区切り→はじめに→前提知識と到達目標→Initializr→対象コード（DTO/Controller/Advice）→動作確認→テストコード（DTO テスト・Controller テスト）。
- 前提の整合: Java 21 / Spring Boot 4.0.2 / Jackson 3（`tools.jackson`）を明示し、3.x 系の混在を警告。`spring-boot-starter-webmvc-test` の確認も他章と一致。
- 技術的整合: `@Size` が UTF-16 コードユニット単位である点を明記し、絵文字11コードポイント=長さ22のケースで実証。username 境界値 2/3/20/21 の期待値（true/false/false/true）は `min=3,max=20` と整合。空 username で `@NotBlank` と `@Size` の2件違反になる説明も正しい。
- 学習設計: DTO/Controller/Advice の責務分担表、「どのテストで確認すべきか判断できること」を到達目標に置く構成は良好。`@NotBlank` の追加ケースを練習として残す点も適切。
- 確定指摘: なし（この範囲で再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 573〜756行（Controller テストの残り、Advice テスト、座学・まとめ）。

## バリデーションテスト（573〜756行、章末評価・全文完了）

- 到達範囲: 573〜756行（Controller テストの残り→Advice テスト→テスト全体の確認→まとめ）。1〜756行の全文を評価済み。
- 内容: UserControllerTest の正常系（201 + レスポンス内容）と異常系（400）、ApiExceptionHandlerTest（standalone MockMvc + ダミー Controller、`VALIDATION_ERROR`/`入力値が不正です`/username 2件/メッセージ集合の検証）、まとめ。
- 技術的整合: username 空文字で `@NotBlank` と `@Size` の2件、メッセージ集合が「username は必須です」「username は 3〜20 文字で入力してください」である点は DTO 定義と整合。`new JsonMapper()` を DI なしで生成する standalone 構成も妥当。
- 学習設計: 「1つのテストで制約・HTTP・応答形式をすべて確認しない」という責務分離の結論が、章冒頭の分担表と一貫。
- 確定指摘: なし。
- 補助担当の隔離実行（Rei 自身の実行ではない）: ApiExceptionHandlerTest、UserControllerTest、UserCreateRequestTest 計10件成功。
- 未確認: 実ブラウザー表示。

## DBを含む統合テスト入門（1〜452行、部分評価）

- 到達範囲: 1〜452行（座学「接続した状態で見つかる不具合」の途中でページング）。452行以降は未取得・未評価。
- 構成: 講義の区切り→概要→対象読者→事前準備→資料構成→ハンズオン（プロジェクト準備、DB更新失敗の応答、OrderApiContract、H2実行、Testcontainers依存、PostgreSQL実行、練習）→座学（部分テストと統合テスト、接続した状態で見つかる不具合）。
- 前提の整合: Java 21 / Spring Boot 4.0 系 / MyBatis Starter 4.0.0 / パッケージ `dev.mikoto2000.springboot.database` を明示。DBアクセス章の最終段階（`createWithItem`/`delete` に `@Transactional`）を前提とする旨も一致。
- 技術的整合: Testcontainers 2 系のモジュール名（`testcontainers-postgresql`）と `org.testcontainers.postgresql.PostgreSQLContainer` を明示し、1 系との混在を警告。`@ServiceConnection` による接続情報注入、H2 用 `@TestPropertySource` が PostgreSQL 用クラスに継承されない点の説明も正確。
- 学習設計: 「Docker の不備をテスト成功として扱わず、実行できた範囲を区別」という指示、`@MockitoBean` を使わず実物を読み込む方針の明示、失敗ケースで既存注文が残ることを確認する設計は適切。
- 確定指摘: なし（この範囲で再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 452〜526行（座学の残り、まとめ、参考資料）。PostgreSQL/Testcontainers 実行は Docker 不在のため補助担当も未実行。

## DBを含む統合テスト入門（452〜526行、章末評価・全文完了）

- 到達範囲: 452〜526行（接続した状態で見つかる不具合の残り→テスト側トランザクション→データ独立性と並列実行→H2 と PostgreSQL の使い分け→コンテナーの接続情報とライフサイクル→実行環境の切り分け表→まとめ→参考資料）。1〜526行の全文を評価済み。
- 内容: テスト側 `@Transactional` がアプリ側の境界不足を隠すリスク、別スレッド HTTP サーバーではテストのトランザクションに参加しない点、並列実行時のデータ分離、H2/PostgreSQL の方言差、`@ServiceConnection` とコンテナーライフサイクル、障害切り分け表、まとめ、参考資料4件。
- 技術的整合: 「テストに `@Transactional` を付けていない」理由の説明は、Service のコミット結果を確認する設計と一貫。コンテナーが ApplicationContext のライフサイクルに従い同一コンテキスト内で共有される点も正確。
- 学習設計: 「スキップしたテストは未検証として扱う」という指示は、Docker 不在環境での学習者に適切。
- 確定指摘: なし。
- 補助担当の隔離実行（Rei 自身の実行ではない）: H2 で教材の最終 `@Transactional` 段階の OrderApiH2Test 6件成功（CRUD/ロールバック含む）。Docker/Podman 不在のため PostgreSQL/Testcontainers 版は未実行。
- 未確認: PostgreSQL/Testcontainers 実行（Docker 不在）。

## SpringSecurityテスト入門（1〜420行、部分評価）

- 到達範囲: 1〜420行（座学「複数リクエストと認証状態」の途中でページング）。420行以降は未取得・未評価。
- 構成: 講義の区切り→概要→対象読者→事前準備→資料構成→ハンズオン（依存関係、アクセスルール表、PageController/SecurityConfig、AccessControlTest 9件、LoginLogoutTest 3件、練習）→座学（認証と認可の分離、テスト用ユーザーと実認証、フィルター、CSRF と権限不足の区別、未認証の扱い、複数リクエスト）。
- 前提の整合: Java 21 / Spring Boot 4.0 系の独立プロジェクト。Vol1/Vol2 を参照しつつ「既存の Security ハンズオンの配布プロジェクトは変更しない」と明示。DB/Thymeleaf/Lombok を使わない旨も明記。
- 技術的整合: `spring-boot-starter-security-test` の位置づけ、`@Import(SecurityConfig.class)` + `@WebMvcTest` でフィルターが適用される点、`roles("ADMIN")` に `ROLE_` を付けない説明、`csrf().useInvalidToken()` の使い方、`formLogin()` が CSRF トークンを用意する点はいずれも正確。9件+3件=12件の件数も各テストメソッド数と一致。
- 学習設計: 「権限不足の POST には有効な CSRF を付けて拒否理由を分ける」という設計は、CSRF と認可の混同を避ける良い説明。
- 確定指摘: なし（この範囲で再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 420〜445行（章末）。

## SpringSecurityテスト入門（420〜445行、章末評価・全文完了）

- 到達範囲: 420〜445行（複数リクエストと認証状態の残り→テスト範囲の選び方→まとめ→参考資料）。1〜445行の全文を評価済み。
- 内容: `@WebMvcTest` と `@SpringBootTest` の役割分担、ブラウザーで確認する範囲（CSRF トークン埋め込み・Cookie 属性・画面遷移）、まとめ5項目、参考資料5件。
- 技術的整合: 「インメモリーのユーザー管理を使うため DB との接続は範囲外」という記述は、SecurityConfig の `InMemoryUserDetailsManager` と整合。
- 確定指摘: なし。
- 補助担当の隔離実行（Rei 自身の実行ではない）: AccessControlTest 9件 + LoginLogoutTest 3件 = 計12件成功。匿名/USER/ADMIN、CSRF なし/無効/有効、パスワード成功/失敗、セッションを伴うログアウト後の再アクセスを確認済み。
- 未確認: DB 認証、HTML 描画、実ブラウザー。

## E2Eテスト入門（1〜337行、部分評価）

- 到達範囲: 1〜337行（座学「MockMvc とブラウザーの検証範囲」の表の途中でページング）。337行以降は未取得・未評価。
- 構成: 講義の区切り→概要→対象読者→事前準備→資料構成→ハンズオン（演習アプリ準備、Playwright 準備、3シナリオ、再実行、意図的失敗、発展課題）→座学冒頭。
- 前提の整合: Vol2 最終段階（ユーザー登録・DB認証・自作ログイン/ログアウト）を前提とし、SpringSecurityテスト入門の後に置く学習順序を明示。Java 21 / Node.js 24 / Playwright / Chromium、Docker 不使用（インメモリー H2）も明記。
- 技術的整合: `application-e2e.yaml` の 18080 ポート・H2 設定、`playwright.config.ts` の `fullyParallel: false`/`workers: 1`/`retries: 0`/`trace: retain-on-failure` は再実行の独立性確認と整合。`e2e-` + UUID が40文字で `username VARCHAR(50)` に収まる点も正確。
- 学習設計: 「ブラウザーコンテキストの初期化と DB の初期化は別の操作」という注意、登録後に明示ログアウトして認証状態の混同を避ける設計、意図的失敗でトレースを読む演習は良好。
- 確定指摘: なし（この範囲で再現可能な学習上/技術上の問題は見つからなかった）。
- 未確認: 337〜450行（座学の残り、まとめ、参考資料）。

## E2Eテスト入門（337〜450行、章末評価・全文完了）

- 到達範囲: 337〜450行（MockMvc とブラウザーの検証範囲の残り→シナリオの選び方→ロケーターと自動待機→認証状態とテストデータ管理→状態変化を結果まで確認→失敗の調査と再試行→CI へ広げるとき→まとめ→参考資料）。1〜450行の全文を評価済み。
- 内容: MockMvc と実サーバーの範囲差、E2E の境界、シナリオ選定基準、`getByRole`/`getByPlaceholder`/`getByLabel`、自動待機と固定待機の弊害、ブラウザーコンテキストと DB の分離、`workers: 1` の位置づけ、状態変化の確認、失敗調査、CI 手順5段、まとめ、参考資料7件。
- 技術的整合: 「JUnit の `@Transactional` は別 HTTP リクエストの保存を巻き戻せない」という説明は DB統合テスト章の記述と一貫。「教材サイトのビルド成功はこの章のアプリ動作の証明にならない」という注意も適切。
- 学習設計: 「再試行で成功しても原因解決とは限らない」「トレースにはフォーム入力が含まれるため共有範囲に注意」という記述は実務的で良好。
- 確定指摘: なし。
- 補助担当の隔離実行（Rei 自身の実行ではない）: 公開 v1.0.0 ベース ZIP + Vol1/Vol2 最終実装の隔離アプリで、教材の TypeScript 3シナリオを Chromium + Playwright 1.64.1 で実行。初回3件成功、同一 JVM 再利用で再実行3件成功。パスワードを意図的に誤らせた一時コピーで1件が期待どおり失敗し、`/login?error` とスクリーンショット・trace 保存を確認。教材値へ戻して3件再成功。
- 未確認: 実受講者による90分授業、CI 環境での実行。

---

# 全16ページ本文評価の完了記録

本文評価を完了した16ファイル（行数は実ファイルの現在値）:

| # | ファイル | 行数 | 評価状態 |
|---|---|---|---|
| 1 | docs/index.md | 15 | 全文評価済み |
| 2 | docs/introduction.md | 102 | 全文評価済み |
| 3 | docs/SpringSecurity入門/Vol1.md | 638 | 全文評価済み |
| 4 | docs/SpringSecurity入門/Vol2.md | 915 | 全文評価済み |
| 5 | docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md | 442 | 全文評価済み |
| 6 | docs/SpringBoot入門/DBアクセス・トランザクション入門.md | 546 | 全文評価済み |
| 7 | docs/SpringBoot入門/DBマイグレーション入門.md | 269 | 全文評価済み |
| 8 | docs/SpringBoot入門/バリデーション入門.md | 683 | 全文評価済み |
| 9 | docs/SpringBoot入門/ロギング入門.md | 1146 | 全文評価済み |
| 10 | docs/SpringBoot入門/例外処理・エラー応答入門.md | 674 | 全文評価済み |
| 11 | docs/SpringBootテスト入門/テスト入門.md | 1127 | 全文評価済み |
| 12 | docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md | 357 | 全文評価済み |
| 13 | docs/SpringBootテスト入門/バリデーションテスト.md | 756 | 全文評価済み |
| 14 | docs/SpringBootテスト入門/DBを含む統合テスト入門.md | 526 | 全文評価済み |
| 15 | docs/SpringBootテスト入門/SpringSecurityテスト入門.md | 445 | 全文評価済み |
| 16 | docs/SpringBootテスト入門/E2Eテスト入門.md | 450 | 全文評価済み |

次工程: 横断確認（用語・パス・ポート・バージョン・リンク・章間前提・成果物の引き継ぎ）。

---

# 横断確認（第1回スキャン・部分記録）

## 取得できた証拠（grepMultiQuery 1回、rawResultRef=fbfdc173-8b60-3ee5-9853-c4aa472ee04a）

出力が途中で切り詰められたため、可視だった一致だけを記録する。

### バージョン表記（パターン `4\.0\.[0-9]`）の可視一致

| ファイル | 行 | 内容 |
|---|---|---|
| docs/SpringBootテスト入門/DBを含む統合テスト入門.md | 67 | 「注文 API には MyBatis Starter 4.0.0、Validation、H2、Spring Web が入っています。」 |
| docs/SpringBootテスト入門/SpringSecurityテスト入門.md | 76 | `https://github.com/spring-projects/spring-boot/blob/v4.0.7/starter/spring-boot-starter-security-test/build.gradle` |
| docs/SpringBootテスト入門/テスト入門.md | 43 | `<version>4.0.0</version>`（mybatis-spring-boot-starter-test） |
| docs/SpringBootテスト入門/テスト入門.md | 48 | 「メインの MyBatis Starter も、この例では 4.0.0 に合わせます。」 |
| docs/SpringBootテスト入門/バリデーションテスト.md | 40 | Initializr リンク `platformVersion=4.0.2` |
| docs/SpringBoot入門/DBアクセス・トランザクション入門.md | 80 | `<version>4.0.0</version>` |
| docs/SpringBoot入門/DBマイグレーション入門.md | 26 | Initializr リンク `platformVersion=4.0.2` |

### ロギング章の可視一致（パターン `logback`、大文字小文字無視）

| ファイル | 行 | 内容 |
|---|---|---|
| docs/SpringBoot入門/ロギング入門.md | 924 | 「保存先や appender の構成を独自に変える場合に `logback-spring.xml` を使います。」 |
| docs/SpringBoot入門/ロギング入門.md | 956 | 処理時間計測（`System.nanoTime()` / `TimeUnit.NANOSECONDS.toMillis(...)`）の説明 |

## 横断確認の候補（未確定・要検証）

- C1（要確認・P2候補）: Spring Boot の版表記が章間で揃っていない。Initializr リンクは `4.0.2`（バリデーションテスト 40行、DBマイグレーション 26行）、SpringSecurityテスト入門 76行の GitHub リンクは `v4.0.7`。本文はいずれも「4.0 系」と表現しており、参照リンクの版だけが異なる。
  - 判定保留の理由: `v4.0.7` は実在タグへの参照であり、教材の実行版を 4.0.7 と断定する記述ではない。Initializr で 4.0.2 を選ぶ指示との間に学習者の混乱が生じるかは、該当箇所の前後文を再確認して判断する必要がある。
- C2（整合・修正方針の裏付け）: ロギング章 924行が既に `logback-spring.xml` を案内しているため、MDC 節の断片名を `logback-spring.xml` に統一する最小修正は、章内の既存記述と矛盾しない。

## 未取得・未確認（この時点の正確な欠落）

- パターン `localhost:[0-9]+`（ポート番号）の一致は1件も可視でなかった。8080 / 18080 などの全出現箇所は未確認。
- パターン `4\.0\.[0-9]` の一致は可視7件のみで、切り詰めにより残りは未確認（Vol1/Vol2、プロジェクト作成章、バリデーション入門、ロギング章、例外処理章などの出現有無は不明）。
- パターン `logback` の一致は可視2件のみで、`logback.xml` の残存記述や MDC 節の断片名は未確認。
- 内部リンク・画像パスの実在確認（`./image/...` など）は未実施。
- 用語（単体/結合/統合/E2E の呼び分け）の章間一致は未確認。

## 次の作業

上記の欠落を埋めるため、ポート番号・`logback` 全件・`4.0.x` 全件を分割して再取得し、リンク実在確認と用語整合の確認を行う。確定指摘のみを修正対象にする。

---

# 横断確認（第2回・結論記録）

## 証拠の出所

- 補助担当によるローカル集計（Rei 自身のツール実行ではない）: 全16 Markdown の現在ファイルを走査した 4.0.x / localhost ポート一覧、logback 全26一致のうち設定ファイル名を含む7行、画像2件の存在と SVG 解析成功、既存ビルド17HTMLの内部リンク1352件・フラグメント1009件エラー0。
- Rei 自身の grepMultiQuery（rawResultRef=fbfdc173-8b60-3ee5-9853-c4aa472ee04a）で、4.0.x 8件・localhost ポート多数・logback の一部を直接確認済み。

## 横断評価の結論

### 1. バージョン表記 — 不具合としない（C1 解消）

- Boot の指定版は全章 4.0.2 で一致（プロジェクト作成54行、DBマイグレーション26行、バリデーションテスト40行、ロギング75行の Initializr リンク）。
- MyBatis Starter の 4.0.0（テスト入門43/48行、DBアクセス80行、DB統合67行）は Boot とは別ライブラリの版であり、混同ではない。
- Securityテスト76行の `v4.0.7` は Starter 公式定義への参照 URL で、実行版の指定ではない。補助担当が当該タグの定義実在と security / test / security-test 依存を確認済み。
- 結論: 参照版の相違のみを根拠にした指摘は撤回する。修正しない。

### 2. ポート番号 — 不具合としない

- 8080 は各章のアプリ用、18080 は E2E 用 `application-e2e.yaml` のサーバーポート、5432 は DB統合テストの接続例。用途が異なり衝突しない。
- introduction がアプリ切り替え時の停止と 8080 競合回避を明記済み。各章に重複注記がないことは不具合にしない。
- 結論: 修正しない。

### 3. 画像アセット — 実在確認済み

- `docs/SpringBootテスト入門/image/開発者テスト入門_テストとは編/equivalence-partitioning.svg`（3039 bytes）と `boundary-value-analysis.svg`（4148 bytes）が存在し、SVG XML として解析成功。
- 結論: リンク切れなし。見た目の評価は範囲外。

### 4. 内部リンク・アンカー — 修正前時点でエラー0

- 既存ビルド17HTMLで1352リンク・1009フラグメントを照合し欠落0（補助担当）。
- ただしこれは修正前の状態。修正後に再ビルドして再検証する必要がある。

### 5. 用語の呼び分け — 不具合としない

- テスト入門・開発者テスト入門・DB統合テストのいずれも「結合と統合をまとめて統合テストと呼ぶ資料もある」「チームで定義する」と補足しており、章間で矛盾しない。
- 結論: 修正しない。

### 6. 章間の前提・成果物の引き継ぎ — 整合

- プロジェクト作成 → DBアクセス → DBマイグレーション → バリデーション → ロギング → 例外処理 の順に、同一プロジェクトを段階的に育てる前提が明示されている。
- テスト系は独立プロジェクト（テスト入門の `dev.mikoto2000.springboot.workshop`、バリデーションテストの `...test.firststep`、Securityテストの `...securitytest`、DB統合の `...database`）で、それぞれ前提章を明記。
- E2E は Vol2 最終段階を前提とし、DB統合の注文 API を使わないと明記。矛盾なし。

### 7. 確定指摘（修正対象）— ロギング章の設定ファイル名の説明整合性（P2）

対象: `docs/SpringBoot入門/ロギング入門.md`

- 526行: ハンズオンの XML は既に `src/main/resources/logback-spring.xml`。
- 1036行・1038行: MDC 節だけ `logback.xml` を指しており、章内でファイル名が揺れている。
- 845行の対応一覧「`logback.xml` / `logback-spring.xml`」は正しいので維持する。
- 1103行の退避手順は `logback-spring.xml` を前提にしており、MDC 節を `logback-spring.xml` に揃えると整合する。

修正方針（最小）:
1. 1036行の `logback.xml` を `logback-spring.xml` に統一する。
2. 1038行のコードフェンス title を `logback-spring.xml` にする。
3. 当該 `<pattern>` 例が「既存の encoder に組み込む断片」であること、ハンズオンでは `<property name="CONSOLE_LOG_PATTERN" value="..."/>` の value に組み込むことを1文で明示する。
4. 座学の `requestId`/`userId` とハンズオンの `traceId`/`user` は別例なので一括置換しない。

撤回済みの主張（最終報告に現在の結論として残さない）:
- 「logback.xml では Boot の既定設定を一切取り込めない」「必ず既存設定が失われる」という断定。公式資料では logback.xml 自体もサポートされ、拡張機能には logback-spring.xml または logging.config 指定が必要という整理が正しい。

## この時点の正確な未確認（次の作業で埋める）

- ロギング章 1030〜1045行の原文（`<pattern>` 例の正確な文字列）は、この文脈では未取得。applyTextDiff の oldText を確定するには該当範囲の実読が必要。
- 526〜542行のハンズオン XML の正確な `<property name="CONSOLE_LOG_PATTERN" .../>` 文字列も未取得。
- 修正後の `npm run textlint` / `npm run build` / 内部リンク・アンカー再検証は未実施。
- 実 PostgreSQL / Testcontainers 実行、実受講者による授業は未検証（補助担当も Docker 不在で未実行）。

## 次の作業

ロギング章の 1030〜1045行と 520〜545行を実読して oldText を確定し、上記の最小修正を適用する。その後 textlint / build / リンク検証を再実行し、最終報告をまとめる。

---

# 横断確認（第2回）の事実誤認の訂正

以下は教材本文の問題ではなく、レビュー報告の記述の訂正である。

## 訂正1: 章間のプロジェクト引き継ぎ（第6節）

誤: 「プロジェクト作成 → DBアクセス → DBマイグレーション → バリデーション → ロギング → 例外処理 の順に、同一プロジェクトを段階的に育てる前提」

正: `docs/introduction.md` 19〜24行の原文は次のとおり。

- 「教材ごとに題材が異なるため、原則として別の Java プロジェクトを使います。」
- 「Spring Security 第2回は、第1回で作成したプロジェクトを続けて使います。」
- 「DB を含む統合テスト入門は、DB アクセス・トランザクション入門の最終段階のプロジェクトを使います。」
- 「Spring Security テスト入門は、独立したプロジェクトで進めます。」
- 「E2E テスト入門は、Spring Security 第2回の最終段階のプロジェクトを使います。」

したがって正しい整理は「原則として教材ごとに独立したプロジェクト。明記された例外は (a) Security Vol2 が Vol1 を継続、(b) DBを含む統合テストが DBアクセス章の最終段階を継承、(c) E2E が Vol2 最終段階を継承」である。DBを含む統合テストを「独立プロジェクト」と一律に分類するのも誤り。

## 訂正2: 用語の呼び分け（第5節）

誤: 「3章いずれも同じ補足がある」

正: 各章の記述は同文ではない。

- テスト入門 95行: この資料では結合テストも広い意味で「統合テスト」と呼ぶ、と明記。
- 開発者テスト入門 64〜66行: プロジェクトやチームによって定義が異なること、結合と統合をまとめて「統合テスト」と呼ぶ資料もあることを補足。348行: 用語はチームで定義し、何を実物と接続しているかを明確にする。
- DBを含む統合テスト: 部分テストと、実物を接続する統合テストの範囲を表で説明。

結論（用語を不具合としない）は維持するが、根拠は「各章がそれぞれの文脈で定義差を説明しており、相互に矛盾しない」とする。

## 訂正3: 版表記（第1節）

誤: 「Boot の指定版は全章 4.0.2 で一致」

正: 「4.0.2 を明示する Initializr リンク（プロジェクト作成54行、DBマイグレーション26行、バリデーションテスト40行、ロギング75行）の間で一致」。本文で「4.0 系」とのみ述べる章まで指定版が 4.0.2 だと断定しない。

結論（版の相違を不具合としない）は維持する。
