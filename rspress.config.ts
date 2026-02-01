import * as path from 'node:path';
import { defineConfig } from 'rspress/config';
import { pluginSitemap } from '@rspress/plugin-sitemap';
import { pluginGoogleAnalytics } from 'rsbuild-plugin-google-analytics';

export default defineConfig({
  plugins: [
    pluginSitemap({
      siteUrl: 'https://github.com/mikoto2000/Practical-Introduction-to-Spring-Boot/',
    }),
  ],
  root: path.join(__dirname, 'docs'),
  base: '/Practical-Introduction-to-Spring-Boot/',
  title: 'Spring Boot 実践入門',
  icon: '/spring.svg',
  logo: {
    light: '/rspress-light-logo.png',
    dark: '/rspress-dark-logo.png',
  },
  logo: '/spring-2.svg',
  logoText: 'Spring Boot 実践入門',
  themeConfig: {
    socialLinks: [
      {
        icon: 'github',
        mode: 'link',
        content: 'https://github.com/mikoto2000/Practical-Introduction-to-Spring-Boot',
      },
    ],
    sidebar: {
      '/シリーズ:SpringBoot入門': [
        { text: 'プロジェクト作成・デプロイ入門', link: '/シリーズ:SpringBoot入門/プロジェクト作成・デプロイ入門' },
        { text: 'バリデーション入門', link: '/シリーズ:SpringBoot入門/バリデーション入門' },
        { text: 'テスト入門', link: '/シリーズ:SpringBoot入門/テスト入門' },
      ],
      '/シリーズ:SpringSecurity入門': [
        { text: 'Vol1', link: '/シリーズ:SpringSecurity入門/Vol1' },
        { text: 'Vol2', link: '/シリーズ:SpringSecurity入門/Vol2' },
      ],
    },
  },
  builderConfig: {
    plugins: [
      pluginGoogleAnalytics({
        id: 'G-RTY3TRXY5T',
      }),
    ],
  },
});
