import { test, expect, ROUTES, login } from './helpers'

test.describe('模板中心测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.templates)
  })

  test('UI-TPL-01: 验证页面标题"内容模板中心"显示', async ({ page }) => {
    const title = page.locator('.card-title:has-text("内容模板中心")')
    await expect(title).toBeVisible()
  })

  test('FUNC-TPL-01: 模板列表正确显示3个预置模板', async ({ page }) => {
    const cards = page.locator('.tpl-card')
    await expect(cards).toHaveCount(3)
  })

  test('FUNC-TPL-02: 点击"使用模板"跳转到创建页并携带模板参数', async ({ page }) => {
    const useBtn = page.locator('.tpl-card button:has-text("使用模板")').first()
    await useBtn.click()
    await expect(page).toHaveURL(/\/content\/create/, { timeout: 10000 })
    const url = page.url()
    expect(url).toContain('template=')
  })
})
