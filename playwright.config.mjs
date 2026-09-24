import { defineConfig } from '@playwright/test';
if (!process.env.TASK_E2E_URL) throw new Error('Use npm run test:e2e for an isolated database.');
export default defineConfig({
  testDir: './e2e', workers: 1, fullyParallel: false, forbidOnly: true, retries: 0,
  timeout: 60000, reporter: [['list'], ['html', { open: 'never' }]],
  use: { baseURL: process.env.TASK_E2E_URL, screenshot: 'only-on-failure' },
  projects: [
    { name: 'chromium-mobile', use: { browserName: 'chromium', viewport: { width: 375, height: 812 }, isMobile: true, hasTouch: true } },
    { name: 'chromium-small', use: { browserName: 'chromium', viewport: { width: 320, height: 740 }, isMobile: true, hasTouch: true } },
    { name: 'chromium-desktop', use: { browserName: 'chromium', viewport: { width: 1280, height: 900 } } },
    { name: 'webkit-mobile', use: { browserName: 'webkit', viewport: { width: 375, height: 812 }, isMobile: true, hasTouch: true } },
    { name: 'webkit-desktop', use: { browserName: 'webkit', viewport: { width: 1280, height: 900 } } },
  ],
});
