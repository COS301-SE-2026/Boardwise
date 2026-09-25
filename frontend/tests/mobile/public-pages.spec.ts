import { test, expect } from '@playwright/test'
for (const path of ['/', '/auth/signin', '/auth/signup', '/help']) {
  test(`${path} fits the viewport`, async ({ page }) => {
    await page.goto(path)
    await expect(page.locator('.v-application')).toBeVisible()
    await page.evaluate(() => document.fonts.ready)
    await expect.poll(() => page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  })
}
test('manifest and install icons are served', async ({ request }) => {
  const response = await request.get('/manifest.webmanifest')
  expect(response.ok()).toBeTruthy()
  const manifest = await response.json()
  expect(manifest.display).toBe('standalone')
  expect(manifest.icons.some((icon: { purpose: string }) => icon.purpose === 'maskable')).toBeTruthy()
  for (const icon of manifest.icons) expect((await request.get(icon.src)).ok()).toBeTruthy()
  expect((await request.get('/offline.html')).ok()).toBeTruthy()
})
