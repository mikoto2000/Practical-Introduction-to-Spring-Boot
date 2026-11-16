import { defineConfig } from '@playwright/test';
import lesson from './playwright.config';

export default defineConfig(lesson, {
  webServer: {
    command:
      'java -jar ../target/security-0.0.1-SNAPSHOT.jar --spring.profiles.active=e2e',
    url: lesson.use?.baseURL,
    timeout: 60_000,
  },
});
