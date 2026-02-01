import * as path from 'node:path';
import { defineConfig } from 'rspress/config';
import { pluginGoogleAnalytics } from 'rsbuild-plugin-google-analytics';

export default defineConfig({
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
  },
  builderPlugins: [
    pluginGoogleAnalytics({
      id: 'G-RTY3TRXY5T',
    }),
  ],
});
