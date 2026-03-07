import { expect, test } from '@playwright/test'

test('user happy path from register to trends', async ({ page, request }) => {
  const username = `user${Date.now()}`

  await page.goto('/register')
  await page.locator('input').nth(0).fill(username)
  await page.locator('input').nth(1).fill('tester')
  await page.locator('input').nth(2).fill(`${username}@example.com`)
  await page.locator('input[type="password"]').fill('123456')
  await page.locator('button').last().click()

  await page.locator('input').nth(0).fill(username)
  await page.locator('input[type="password"]').fill('123456')
  await page.locator('button').last().click()
  await expect(page).toHaveURL(/dashboard/)

  await page.locator('a[href="/profile"]').click()
  await page.locator('button').filter({ hasText: /保存|save/i }).click()
  await expect(page.getByText('BMI', { exact: true })).toBeVisible()

  const token = await page.evaluate(() => localStorage.getItem('health-assistant-token'))
  expect(token).toBeTruthy()

  const mealResponse = await request.post('http://127.0.0.1:8080/api/v1/meals', {
    headers: { Authorization: `Bearer ${token}` },
    data: {
      mealType: 'lunch',
      eatenAt: '2026-03-08T12:30:00',
      description: 'manual e2e meal',
      imageUrl: '/uploads-test/mock-meal.png',
      selfRating: 4,
      emotionText: 'calm',
      items: [
        {
          foodName: 'Chicken Breast',
          amount: 150,
          unit: 'g',
          calories: 248,
          protein: 31,
          fat: 5,
          carbs: 0,
          source: 'MANUAL',
          confirmed: true
        },
        {
          foodName: 'Rice',
          amount: 180,
          unit: 'g',
          calories: 210,
          protein: 4,
          fat: 1,
          carbs: 46,
          source: 'MANUAL',
          confirmed: true
        }
      ]
    }
  })
  expect(mealResponse.ok()).toBeTruthy()

  await page.locator('a[href="/analysis/trends"]').click()
  await expect(page).toHaveURL(/analysis\/trends/)
  await expect(page.locator('div[style*="height: 320px"]').first()).toBeVisible()
})

test('admin can view audit logs', async ({ page }) => {
  await page.goto('/login')
  await page.locator('input').nth(0).fill('admin')
  await page.locator('input[type="password"]').fill('admin123')
  await page.locator('button').last().click()

  await page.locator('a[href="/admin"]').click()
  await expect(page).toHaveURL(/admin/)
  await expect(page.locator('.table-row').first()).toBeVisible()
})
