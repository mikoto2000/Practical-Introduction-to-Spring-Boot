---
title: E2E テスト入門
---

# E2E テスト入門

## 講義の区切り

基本編の目安は 90 分です。終了条件は「ブラウザーでログイン・ユーザー登録・ログアウトを自動検証」です。
発展内容・次回の目安: 権限差の検証と CI での起動・実行。基本の確認後に取り組んでください。
準備と前後の章は [学習ガイド](../introduction.md) で確認できます。

この章のコード例は、Playwright の版によって動作が変わる場合があります。`npx playwright --version` で実行した版を記録し、`package-lock.json` を保存してください。版が異なる環境では公式資料と照合してください。

## 概要

ブラウザーを操作して、利用者が行う一連の手順を自動で検証します。
Spring Security 第2回で作成したアプリを使い、ログイン画面から DB 認証までを接続した状態で確認します。
テストには Playwright Test を使います。

MockMvc によるテストとの違いを体験し、E2E テストに任せる範囲を選べることを目指します。
E2E は End-to-End の略で、処理の入口から結果までを通して確認する意味です。
この教材では「ブラウザー → HTTP サーバー → Spring Security・Controller → DB」を検証範囲にします。

## 対象読者

- Spring Security でログインとアクセス制御を実装した方
- MockMvc を使ったテストとブラウザーを使うテストの違いを知りたい方
- 手動で確認している利用手順を自動化したい方

## 事前準備と到達目標

[Spring Security 第2回](../SpringSecurity入門/Vol2.md) の最終段階まで完成したプロジェクトを使います。
ユーザー登録、DB 認証、自作のログイン・ログアウトフォームが必要です。
[Spring Security テスト入門](./SpringSecurityテスト入門.md) の学習後に取り組む想定です。
[DB を含む統合テスト入門](./DBを含む統合テスト入門.md) も読むと、テスト範囲を比較できます。
ただし、注文 API のプロジェクトは今回使いません。

Java 21、Maven Wrapper、Node.js 24、npm を用意します。
Playwright が対応する OS と、Chromium をダウンロードできるネットワーク接続が必要です。
Docker は使わず、既存アプリのインメモリー H2 を利用します。

テストは TypeScript で記述します。Java のテストコードを TypeScript へ移す必要はありません。
この教材では `import`、`const`、関数と `async` / `await` を使います。
`await` は、ブラウザー操作などの非同期処理の完了を待つために付けます。

完了時には、次の状態を目指します。

- ブラウザーで利用手順を実行し、URL と画面の結果を検証できる。
- テスト間で認証状態と登録データが干渉しないようにできる。
- 失敗時の画面と操作履歴から、調査を始められる。

## この資料の構成

この資料は 2 部構成です。

1. 触って学ぶ E2E テスト: 環境を準備し、3つの利用手順をブラウザーで自動検証するハンズオン。
2. 座学で学ぶ E2E テスト: テスト範囲、待機、データ管理、失敗の調査を整理する座学。

| 触って学ぶ（ハンズオン） | 座学で学ぶ（理論） |
|---|---|
| 未ログインからのログイン | MockMvc とブラウザーの検証範囲 |
| 管理者による登録と新規ユーザーのログイン | シナリオの選び方と認証状態 |
| ログアウト後のアクセス | 状態変化を結果まで確認する |
| 画面要素の指定と結果の検証 | ロケーターと自動待機 |
| 再実行と失敗の調査 | データの独立性とトレース |

## 触って学ぶ E2E テスト

### 演習アプリの準備

まず Spring Security 第2回のアプリを、手動で操作できることを確認します。
初期管理者は `mikoto2000 / password` です。いずれも演習専用の値です。

| URL | 期待する動作 |
|---|---|
| `/` | 未ログインでも表示できる |
| `/private` | 未ログインなら `/login` へ移動する |
| `/login` | Username・Password の入力欄とログインボタンがある |
| `/signup` | ADMIN だけがユーザーを登録できる |
| `/logout`（POST） | ログアウトして `/` へ移動する |

手動確認が終わったら、アプリを Ctrl+C で停止します。
E2E 用に、`src/main/resources/application-e2e.yaml` を追加します。

```yaml
server:
  port: 18080

spring:
  datasource:
    url: jdbc:h2:mem:security-e2e;DB_CLOSE_DELAY=-1
    username: sa
    password: ""
    driver-class-name: org.h2.Driver
  sql:
    init:
      mode: always
  h2:
    console:
      enabled: false
```

第2回の `schema.sql` と `data.sql` を使い、起動時にテーブルと初期管理者を用意します。
この設定は、Spring Security 第2回の `schema.sql`・`data.sql` による初期化を前提にしています。
Flyway などを独自に追加した場合は、初期化方法が重複しないように調整してください。

Java プロジェクトの `pom.xml` があるディレクトリで起動します。

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=e2e
```

Windows PowerShell では、次のコマンドを使います。

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=e2e'
```

起動ログで `e2e` プロファイルと 18080 番ポートを確認してください。
ブラウザーで `http://localhost:18080/` を開き、トップページが表示されれば準備完了です。
このターミナルではアプリを起動したままにします。

### Playwright の準備

別のターミナルを開き、同じ Java プロジェクトのルートへ移動します。
Java プロジェクト内に `e2e` ディレクトリを作り、Playwright をインストールします。
教材サイトのリポジトリへインストールする手順ではありません。

```bash
mkdir e2e
cd e2e
npm init -y
npm install --save-dev --save-exact @playwright/test typescript @types/node
npx playwright install chromium
```

Linux でブラウザーの実行に必要な OS ライブラリが足りない場合は、公式のインストール手順を確認してください。
初回取得後は、`package.json` と `package-lock.json` を保存します。
別の環境では `npm ci` と `npx playwright install chromium` で準備します。
使用した版は `npx playwright --version` で確認できます。
使用した版を記録しておくと、環境差で失敗したときの切り分けに役立ちます。

`e2e/playwright.config.ts` を作成します。

```typescript title="playwright.config.ts"
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:18080',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
```

最初は Chromium だけで、1つずつテストを実行します。
`baseURL` は操作対象アプリの URL です。`page.goto('/private')` などの基準になります。
この設定では、Playwright はアプリを起動しません。先ほど起動したアプリに接続します。

`e2e/.gitignore` も作成します。

```text
node_modules/
playwright-report/
test-results/
```

### 未ログインからログインする

`e2e/tests` ディレクトリを作り、次のファイルを追加します。

```typescript title="tests/auth.spec.ts"
import { randomUUID } from 'node:crypto';
import { test, expect, type Page } from '@playwright/test';

async function fillLogin(page: Page, username: string, password: string) {
  await page.getByPlaceholder('Username', { exact: true }).fill(username);
  await page.getByPlaceholder('Password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'ログイン', exact: true }).click();
}

test('未ログインで保護ページを開き、ログインして表示する', async ({ page }) => {
  await page.goto('/private');
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
  await expect(page.getByRole('heading', { name: 'ログインページ' })).toBeVisible();

  await fillLogin(page, 'mikoto2000', 'password');

  await expect(page).toHaveURL(/\/private(?:\?.*)?$/);
  await expect(page.getByText('これが見えているという事は、認証に成功しています。')).toBeVisible();
});
```

`randomUUID` は、後で追加する登録テストで使います。
`fillLogin` はフォーム入力と送信をまとめた関数です。呼び出す前にログイン画面を開いておきます。
保護ページへアクセスしてからログインすることで、ログイン後に元のページへ戻る流れも確認します。
URL は、Spring Security が付けるクエリーパラメーターを許容しています。

`e2e` ディレクトリで実行します。

```bash
npx playwright test --headed
```

ブラウザーが自動で開き、ログイン後に認証必須ページを表示すれば成功です。
テスト結果に `1 passed` と表示されることを確認してください。

### 管理者が登録し、新規ユーザーでログインする

同じ `tests/auth.spec.ts` の末尾に追加します。

```typescript
test('管理者がユーザーを登録し、そのユーザーでログインできる', async ({ page }) => {
  const username = `e2e-${randomUUID()}`;
  const password = 'E2e-example-123!';

  await page.goto('/private');
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
  await fillLogin(page, 'mikoto2000', 'password');
  await expect(page).toHaveURL(/\/private(?:\?.*)?$/);

  await page.goto('/');
  await page.getByRole('link', { name: 'ユーザー登録ページ', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'サインアップページ' })).toBeVisible();
  await page.getByPlaceholder('Username', { exact: true }).fill(username);
  await page.getByPlaceholder('Password', { exact: true }).fill(password);
  await page.getByRole('button', { name: 'ユーザー作成', exact: true }).click();
  await expect(page).toHaveURL(/\/login$/);

  // 登録後も管理者の認証状態が残るため、明示的にログアウトする。
  await page.goto('/');
  await page.getByRole('button', { name: 'ログアウト', exact: true }).click();
  await expect(page).toHaveURL('http://localhost:18080/');

  await page.goto('/private');
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
  await fillLogin(page, username, password);
  await expect(page).toHaveURL(/\/private(?:\?.*)?$/);
  await expect(page.getByText('これが見えているという事は、認証に成功しています。')).toBeVisible();
});
```

登録成功後の画面遷移だけで終わらず、新しく登録した資格情報で認証できることまで確認します。
管理者のログアウト後にログイン画面へ戻ることも検証するため、認証状態の混同を避けられます。
このシナリオの途中でアプリを再起動すると、インメモリー DB の登録情報が消えます。

ユーザー名には UUID を付け、再実行しても既存ユーザーと衝突しにくくしています。
`e2e-` と UUID を合わせて40文字なので、Spring Security 第2回の `username VARCHAR(50)` に収まります。

### ログアウト後に保護ページを開く

さらに、同じファイルの末尾に追加します。

```typescript
test('ログアウト後は保護ページを表示できない', async ({ page }) => {
  await page.goto('/private');
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
  await fillLogin(page, 'mikoto2000', 'password');
  await expect(page).toHaveURL(/\/private(?:\?.*)?$/);

  await page.getByRole('button', { name: 'ログアウト', exact: true }).click();
  await expect(page).toHaveURL('http://localhost:18080/');

  await page.goto('/private');
  await expect(page).toHaveURL(/\/login(?:\?.*)?$/);
  await expect(page.getByRole('heading', { name: 'ログインページ' })).toBeVisible();
});
```

ログアウトボタンを押せたことに加え、次のアクセスが未認証として扱われることを確認します。
各テストでは、Playwright が認証状態を共有しない新しいブラウザーコンテキストを用意します。
前のテストでログインしていても、このテストは未ログインから始まります。

### 再実行して独立性を確認する

```bash
npx playwright test
npx playwright test
```

どちらも `3 passed` になることを確認します。
引数なしではブラウザーの画面を表示せずに実行します。
登録テストは実行ごとに新しいユーザー名を使うため、同じアプリへ続けて接続できます。

登録データは DB に残ります。演習終了後はアプリを Ctrl+C で停止してください。
新しい JVM で起動し直すと、初期管理者だけの状態に戻ります。
ブラウザーコンテキストの初期化と DB の初期化は、別の操作です。

### わざと失敗させて調査する

最初のテストで、管理者のパスワードを一時的に `wrong-password` に変更します。

```bash
npx playwright test --grep '未ログインで保護ページ'
npx playwright show-report
```

ログイン後の URL の検証で失敗し、実際には `/login?error` にいることを確認します。
HTML レポートから失敗したテストを開き、スクリーンショットとトレースを調べます。
トレースには操作履歴、画面の状態、ネットワーク通信が記録されます。
別ターミナルのアプリログも合わせて確認してください。

調査したらパスワードを `password` に戻し、3つのテストが成功することを確認します。
レポート表示のサーバーも、確認後に Ctrl+C で停止します。

### 発展課題

基本の終了条件を達成してから、次の内容に取り組んでください。

1. 登録テストに、一般ユーザーではトップページの登録リンクが表示されない検証を追加する。
2. 同じ一般ユーザーで `/signup` を直接開き、HTTP ステータスが 403 になることを確認する。
3. ログイン失敗のシナリオを、意図的な失敗ではなく期待どおりに成功するテストとして追加する。

2では、`const response = await page.goto('/signup')` の戻り値からステータスを取得できます。
`expect(response?.status()).toBe(403)` で、URL による拒否も確認できます。
エラーページの具体的な文面には依存させません。

## 座学で学ぶ E2E テスト

### MockMvc とブラウザーの検証範囲

| テスト構成 | 主に確認すること | 今回の使い分け |
|---|---|---|
| Service の単体テスト | 業務条件と分岐 | 多数の条件を短時間で確認する |
| MockMvc と Security | 許可・拒否、CSRF、HTTP 応答 | 権限とリクエストの組み合わせを確認する |
| MockMvc と DB | SQL、保存結果、トランザクション | 保存内容と失敗後の DB 状態を確認する |
| ブラウザーと実サーバーと DB | フォーム、画面遷移、認証状態を含む利用手順 | 代表的な利用手順を少数に絞って確認する |

MockMvc は実際の HTTP サーバーを起動せず、Servlet API のモックを使って処理を呼び出します。
Thymeleaf などの HTML 描画も検証できますが、ブラウザーによる操作や Cookie の受け渡しとは範囲が異なります。
今回のテストでは、実際に描画されたフォームをブラウザーから送信します。
CSRF トークンの埋め込みから送信、セッション Cookie による認証状態の継続も、利用手順の中で確認します。
トークンがない場合の拒否など、条件を細かく分けた検証には既存の Security テストを使います。

E2E の境界はプロジェクトによって異なります。
API が利用者向けの入口なら、HTTP クライアントから DB までを通すテストも候補になります。
`@SpringBootTest(webEnvironment = RANDOM_PORT)` は実サーバーを起動する方法ですが、それだけでブラウザー操作を含むわけではありません。
今回も、HTTPS 終端、リバースプロキシ、本番 DB、外部サービスは検証範囲に含みません。
本番相当の構成での確認が必要なら、別途その環境を用意します。

### シナリオの選び方

E2E テストでは、重要な利用手順を最後まで完了できるかを確認します。
今回の登録テストは、画面表示、認可、保存、パスワードの変換、認証の接続を通します。
単独では成功する処理でも、フォームの送信先や画面遷移が間違っていればシナリオは失敗します。

一方、入力値のすべての境界条件を、この長いシナリオで繰り返す必要はありません。
文字数の上限や入力エラーの組み合わせは、DTO や Controller のテストで詳しく確認できます。
重要度、ほかのテストで見つけられるか、実行時間、失敗時の調べやすさを基準に配置します。

ユーザー登録とそのユーザーでのログインは、1つの利用手順として同じテストにまとめました。
別のテストが登録したユーザーを使う構成にすると、単独実行や実行順の変更で失敗します。
テスト内の手順には順序がありますが、テスト同士は独立させます。

### ロケーターと自動待機

ロケーターは、操作対象の画面要素を指定する仕組みです。
`getByRole` ではボタンやリンクの役割と名前、`getByPlaceholder` では入力欄の表示を指定しました。
既存フォームにラベルがないため、今回はプレースホルダーを使っています。
フォームに関連付けたラベルがある場合は、`getByLabel` も使えます。
CSS の階層や要素の順番への依存を減らすと、見た目の変更で壊れにくくなります。

Playwright は、クリック対象が操作できる状態になるまで自動で待ちます。
`await expect(locator).toBeVisible()` や `await expect(page).toHaveURL(...)` も、条件を満たすまで再試行します。
`baseURL` を設定している場合、`toHaveURL` には `http://localhost:18080/` のような完全な URL ではなく、`/` のような相対パスも渡せます。
`await page.waitForTimeout(3000)` のような固定時間の待機を増やすと、高速な環境では無駄な待ち時間が生じます。
低速な環境では待ち時間が足りず、失敗することもあります。観測できる結果を待ちましょう。

ハンズオンの `await` は、非同期処理と検証の完了を待つために必要です。
省略すると、処理が完了する前に次の操作へ進み、失敗が分かりにくくなります。

### 認証状態とテストデータの管理

ブラウザーコンテキストは Cookie などを分離しますが、接続先アプリの DB は共有されています。
そのため、コンテキストを分けるだけでは登録ユーザー名の衝突は防げません。
今回の UUID は衝突を避ける方法であり、データ削除の代わりではありません。
短い演習では JVM の再起動で初期化し、継続運用ではテストごとの準備と削除を設計します。

DB を共有する構成では、全テーブルの削除と並列実行を組み合わせると、別テストのデータを消すおそれがあります。
並列化する場合は、実行ごとの DB やスキーマ、ユーザーを分ける方法を検討します。
`workers: 1` は演習の複雑さを抑える設定であり、データ管理を不要にする設定ではありません。

また、別プロセスのブラウザーから送るリクエストは、サーバー側で処理されます。
JUnit のテストメソッドに `@Transactional` を付けても、別の HTTP リクエストによる保存を自動で巻き戻せるわけではありません。
サーバー側のトランザクションと、テストデータの後始末を分けて考えます。

### 状態変化を結果まで確認する

登録後にログイン画面へ移動しても、管理者がログアウトしたとは限りません。
画面の URL と認証状態は別です。ハンズオンでは明示的にログアウトしてから、新規ユーザーでログインしました。
ログアウトの検証でも、トップページへの移動だけで終わらず、保護ページへの再アクセスを確認しました。

同様に、登録リンクが非表示でも、登録処理へのアクセスが拒否されるとは限りません。
発展課題の直接アクセスと、MockMvc による GET・POST の認可テストを組み合わせて確認します。

### 失敗の調査と再試行

E2E テストが失敗したら、まず失敗した操作、期待した結果、実際の URL と画面を確認します。
アプリの不具合に加え、アプリの起動忘れ、ポートの誤り、テストデータの衝突も原因になります。
トレースの通信記録とアプリログを照らし合わせ、どこで期待から外れたかを絞り込みます。

再試行で成功しても、不安定になる原因が解決したとは限りません。
この演習では `retries: 0` にし、最初の失敗を調べます。
継続運用で再試行を使う場合も、再試行で成功した結果を確認し、原因を調査します。
トレースにはフォーム入力や通信内容が含まれるため、演習用データを使い、共有範囲を確認してください。

### CI へ広げるときに決めること

基本編では、アプリを手動で起動し、テストの書き方に集中しました。
CI では、起動、準備完了の確認、テスト、終了処理まで自動化します。
Playwright の `webServer` 設定を使うと、起動コマンドと待機先 URL を指定できます。
Maven Wrapper のコマンドは実行 OS に合わせ、準備完了を固定の秒数で判断しないようにします。

CI で用意する主な処理は、次のとおりです。

1. Java・Node.js を準備し、Maven の依存取得と `e2e` での `npm ci` を実行する。
2. Chromium と OS の依存ライブラリを用意する。
3. 専用 DB を初期化してアプリを起動し、HTTP で応答できるまで待つ。
4. E2E テストを実行し、失敗時のレポートとアプリログを保存する。
5. 成否にかかわらず、起動したアプリと DB を終了する。

E2E テストは、演習アプリの CI に追加します。
教材サイトのビルドが成功しても、この章の Java アプリやテストコードが動くことの証明にはなりません。

## まとめ

- ブラウザーからフォームを操作し、実サーバーと DB を通した利用手順を検証する。
- ログイン、登録、ログアウトを、次の操作の結果まで確認する。
- 認証状態の分離と DB データの初期化を区別する。
- 重要な利用手順を E2E で確認し、細かな条件は既存のテストと分担する。

## 参考資料

- [Playwright のインストールと対応環境](https://playwright.dev/docs/intro)
- [Playwright のテスト設定](https://playwright.dev/docs/test-configuration)
- [Playwright のロケーター](https://playwright.dev/docs/locators)
- [Playwright の推奨事項](https://playwright.dev/docs/best-practices)
- [Playwright のトレースビューアー](https://playwright.dev/docs/trace-viewer-intro)
- [Playwright の Web サーバー起動設定](https://playwright.dev/docs/test-webserver)
- [Spring Framework: MockMvc と End-to-End テスト](https://docs.spring.io/spring-framework/reference/testing/mockmvc/vs-end-to-end-integration-tests.html)
