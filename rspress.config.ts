import * as path from 'node:path';
import { defineConfig } from 'rspress/config';

export default defineConfig({
  root: path.join(__dirname, 'docs'),
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
  },
  sidebar: {
    '/docs/': [
      {
        text: 'はじめに',
        items: [
          {
            text: 'はじめに',
            link: '/introduction.md',
          }
        ],
      }
    ]
  }
});
