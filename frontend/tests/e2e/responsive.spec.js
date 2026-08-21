import { test, expect } from '@playwright/test'

const homepage = 'http://localhost:3000/'


test.describe('Responsive Testing', () => {
  test('Landing page is usable on mobile viewport', async ({ page }) => {
    // ARRANGE
    await page.setViewportSize({ width: 393, height: 851 })

    // ACT
    await page.goto(homepage)

    // ASSERT
    await expect(page).toHaveURL(homepage)

    // Page should not have horizontal overflow.
    const documentWidth = await page.evaluate(
      () => document.documentElement.scrollWidth
    )
    const viewportWidth = await page.evaluate(() => window.innerWidth)

    expect(documentWidth).toBeLessThanOrEqual(viewportWidth)

    // Main navigation should remain visible and usable.
    await expect(page.getByRole('button', { name: 'Sign In' })).toBeVisible()
  })

   test('Landing page is usable on desktop viewport', async ({ page }) => {
    // ARRANGE
    await page.setViewportSize({ width: 1280, height: 720 })

    // ACT
    await page.goto(homepage)

    // ASSERT
    await expect(page).toHaveURL(homepage)

    // Page should not have horizontal overflow.
    const documentWidth = await page.evaluate(
      () => document.documentElement.scrollWidth
    )
    const viewportWidth = await page.evaluate(() => window.innerWidth)

    expect(documentWidth).toBeLessThanOrEqual(viewportWidth)

    // Main navigation should remain visible and usable.
    await expect(
      page.getByRole('button', { name: 'Sign In' })
    ).toBeVisible();
  })
})

 

