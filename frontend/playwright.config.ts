import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  expect: { timeout: 10_000 },
  use: {
    baseURL: 'http://127.0.0.1:4173',
    trace: 'retain-on-failure'
  },
  webServer: [
    {
      command: 'mvn spring-boot:run "-Dspring-boot.run.profiles=test"',
      cwd: '../backend',
      port: 8080,
      reuseExistingServer: true,
      timeout: 120_000
    },
    {
      command: 'cmd /c "npm run build && npm run preview -- --host 127.0.0.1 --port 4173"',
      cwd: '.',
      port: 4173,
      reuseExistingServer: true,
      timeout: 120_000
    }
  ],
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
})
