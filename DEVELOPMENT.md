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
