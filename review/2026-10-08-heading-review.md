# Markdown 見出し階層の横断確認

確認日: 2026-10-08（Asia/Tokyo）
対象: Git 管理された Markdown 全19件（公開教材15件、ルート文書2件、既存レビュー2件）。
生成物・依存パッケージの Markdown は対象外です。この確認記録は対象19件に追加しています。

## 判断基準

H1 はページタイトル、H2 は主要な章、H3 は章内の節、H4 以降はその手順や詳細とします。
「触って学ぶ」と「座学で学ぶ」を H2 に置く教材では、個々の学習対象は H3 以下へ揃えます。
見出しの数字が連続していても、内容が本来の親節の外へ出ていれば修正します。
トップページ docs/index.md は frontmatter の home レイアウトを使うため、Markdown の H1 がないことを許容します。

## 全ファイルの比較

見出し数は修正後の H1 / H2 / H3 / H4 / H5 / H6 の順です。

| ファイル | 各レベルの見出し数 | 確認結果・修正内容 |
|---|---|---|
| [DEVELOPMENT.md](../DEVELOPMENT.md) | 1 / 4 / 0 / 0 / 0 / 0 | 階層の修正なし |
| [README.md](../README.md) | 1 / 4 / 0 / 0 / 0 / 0 | 階層の修正なし |
| [docs/SpringBootテスト入門/DBを含む統合テスト入門.md](../docs/SpringBootテスト入門/DBを含む統合テスト入門.md) | 1 / 9 / 14 / 0 / 0 / 0 | 階層の修正なし |
| [docs/SpringBootテスト入門/SpringSecurityテスト入門.md](../docs/SpringBootテスト入門/SpringSecurityテスト入門.md) | 1 / 9 / 13 / 0 / 0 / 0 | 階層の修正なし |
| [docs/SpringBootテスト入門/テスト入門.md](../docs/SpringBootテスト入門/テスト入門.md) | 1 / 10 / 15 / 37 / 0 / 0 | Util・Service・Repository・Controller を H3、各実装・テストを H4 に変更。ハンズオンの配下へ戻した。 |
| [docs/SpringBootテスト入門/バリデーションテスト.md](../docs/SpringBootテスト入門/バリデーションテスト.md) | 1 / 7 / 9 / 4 / 0 / 0 | 階層の修正なし |
| [docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md](../docs/SpringBootテスト入門/開発者テスト入門_テストとは編.md) | 1 / 10 / 12 / 2 / 1 / 0 | Pairwise の詳細を H4 としてその他テストケース設計の配下へ移動。参考資料の後から本文へ戻した。 |
| [docs/SpringBoot入門/DBアクセス・トランザクション入門.md](../docs/SpringBoot入門/DBアクセス・トランザクション入門.md) | 1 / 9 / 16 / 0 / 0 / 0 | 階層の修正なし |
| [docs/SpringBoot入門/DBマイグレーション入門.md](../docs/SpringBoot入門/DBマイグレーション入門.md) | 1 / 13 / 1 / 0 / 0 / 0 | 採番の説明を H3 として並行開発の運用例へ移動。参考資料の後から本文へ戻した。 |
| [docs/SpringBoot入門/バリデーション入門.md](../docs/SpringBoot入門/バリデーション入門.md) | 1 / 9 / 10 / 5 / 2 / 0 | RequestBody の動作確認と Advice の定義・確認を H4 に変更し、それぞれの親節へ揃えた。 |
| [docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md](../docs/SpringBoot入門/プロジェクト作成・デプロイ入門.md) | 1 / 9 / 13 / 3 / 4 / 0 | 階層の修正なし |
| [docs/SpringBoot入門/ロギング入門.md](../docs/SpringBoot入門/ロギング入門.md) | 1 / 9 / 16 / 38 / 1 / 2 | AOP の動作確認を H4 に変更し、Controller の開始・終了ログ追加の配下へ戻した。 |
| [docs/SpringBoot入門/例外処理・エラー応答入門.md](../docs/SpringBoot入門/例外処理・エラー応答入門.md) | 1 / 9 / 17 / 0 / 0 / 0 | 階層の修正なし |
| [docs/SpringSecurity入門/Vol1.md](../docs/SpringSecurity入門/Vol1.md) | 1 / 10 / 14 / 25 / 2 / 0 | ハンズオンのまとめを H3 に変更。本番向けの確認を H2 として座学の後へ移動。 |
| [docs/SpringSecurity入門/Vol2.md](../docs/SpringSecurity入門/Vol2.md) | 1 / 10 / 9 / 32 / 4 / 0 | ハンズオンの3段階を H3、その手順を H4、テーブル・画面の詳細を H5 に変更。 |
| [docs/index.md](../docs/index.md) | 0 / 0 / 0 / 0 / 0 / 0 | 階層の修正なし |
| [docs/introduction.md](../docs/introduction.md) | 1 / 6 / 0 / 0 / 0 / 0 | 階層の修正なし |
| [review/2026-10-07-review-fixes.md](../review/2026-10-07-review-fixes.md) | 1 / 1 / 0 / 0 / 0 / 0 | 階層の修正なし |
| [review/2026-10-07-review-study-materials.md](../review/2026-10-07-review-study-materials.md) | 1 / 15 / 58 / 0 / 0 / 0 | 階層の修正なし |

## 検証

- 7教材を修正。残り12件も見出しの順序と内容上の親子関係を確認。
- コードフェンス内を除いて、見出しレベルの飛び越し0件。各通常ページの H1 は1件。
- 修正前後のコードブロックを比較し、掲載コードの変更0件。
- 見出しの文言を維持し、生成 HTML の既存アンカーと内部リンクを確認。
- npm run textlint、npm run build、git diff --check を実行。
- 生成 HTML の内部リンク1243件・画像参照24件を確認し、欠落0件。
