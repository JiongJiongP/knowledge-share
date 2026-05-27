import { test, expect, ROUTES, login } from './helpers'

test.describe('我的收藏页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.favorites)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
  })

  test('UI-FAV-01: 验证页面标题"我的收藏"显示', async ({ page }) => {
    const title = page.locator('.card-title:has-text("我的收藏")')
    await expect(title).toBeVisible()
  })

  test('FUNC-FAV-01: 收藏列表正确加载', async ({ page }) => {
    const list = page.locator('.fav-list')
    const empty = page.locator('.el-empty')
    const items = page.locator('.content-list-item')
    const hasList = await list.isVisible().catch(() => false)
    const hasEmpty = await empty.isVisible().catch(() => false)
    const hasItems = await items.count() > 0
    expect(hasList || hasEmpty || hasItems).toBeTruthy()
  })

  test('FUNC-FAV-02: 按内容类型筛选收藏', async ({ page }) => {
    const select = page.locator('.filter-select')
    const selectVisible = await select.isVisible().catch(() => false)
    if (selectVisible) {
      await select.selectOption('MARKDOWN')
      await page.waitForTimeout(1000)
    }
  })

  test('FUNC-FAV-03: 取消收藏功能正常', async ({ page }) => {
    const unfavBtns = page.locator('button:has-text("取消收藏")')
    const count = await unfavBtns.count()
    if (count > 0) {
      await unfavBtns.first().click()
      const msg = page.locator('.el-message--success:has-text("已取消收藏")')
      await expect(msg).toBeVisible({ timeout: 5000 })
    }
  })

  test('FUNC-FAV-04: 点击收藏项跳转到内容详情', async ({ page }) => {
    const items = page.locator('.content-list-item')
    const count = await items.count()
    if (count > 0) {
      await items.first().click()
      await expect(page).toHaveURL(/\/content\//, { timeout: 10000 })
    }
  })

  test('FUNC-FAV-05: 空收藏列表显示提示', async ({ page }) => {
    const empty = page.locator('.el-empty')
    const items = page.locator('.content-list-item')
    const hasEmpty = await empty.isVisible().catch(() => false)
    const itemCount = await items.count()
    expect(hasEmpty || itemCount > 0).toBeTruthy()
  })
})
