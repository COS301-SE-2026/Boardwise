import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './tests/mobile',
  workers: 1,
  use: { baseURL: 'http://127.0.0.1:4173', trace: 'retain-on-failure' },
  webServer: { command: 'npm run preview:pwa', url: 'http://127.0.0.1:4173', reuseExistingServer: !process.env.CI },
  projects: [320, 390, 768, 1024, 1440].map(width => ({ name: `width-${width}`, use: { viewport: { width, height: 900 } } }))
})
