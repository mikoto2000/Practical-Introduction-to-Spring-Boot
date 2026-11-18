import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '../..');
const read = (path) => readFileSync(resolve(root, path), 'utf8');
const inventory = [
  'SpringBoot入門',
  'SpringBootテスト入門',
  'SpringSecurity入門',
].flatMap((directory) =>
  readdirSync(resolve(root, 'docs', directory))
    .filter((name) => name.endsWith('.md'))
    .map((name) => `${directory}/${name.slice(0, -3)}`),
);
const guide = read('docs/introduction.md').split('## コンテンツ一覧')[1];
assert.ok(guide, '学習ガイドのコンテンツ一覧がありません');
const chapters = [...guide.matchAll(/^\d+\. \[[^\]]+\]\(\.\/(.+)\.md\)/gm)].map(
  (match) => match[1],
);
const sidebar = [
  ...read('rspress.config.ts').matchAll(/link:\s*['"]\/([^'"]+)['"]/g),
]
  .map((match) => match[1])
  .filter((path) => path !== 'introduction');

assert.equal(new Set(chapters).size, chapters.length, '章一覧に重複があります');
assert.deepEqual(
  [...chapters].sort(),
  [...inventory].sort(),
  '学習ガイドの章一覧と教材ファイルが一致しません',
);
assert.deepEqual(
  sidebar,
  chapters,
  'サイドバーと学習ガイドの章・順序が一致しません',
);
console.log(`${chapters.length}章のファイル・章一覧・サイドバーが一致しました`);
