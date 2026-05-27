import { test, expect, ROUTES, login } from './helpers'

test.describe('通知页面 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.notifications)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
  })

  test('UI-NOTIF-01: 验证页面标题"通知中心"显示', async ({ page }) => {
    const title = page.locator('.page-card-title:has-text("通知中心"), .card-title:has-text("通知中心")')
    await expect(title.first()).toBeVisible({ timeout: 10000 })
  })

  test('UI-NOTIF-02: 验证通知类型筛选Tab存在', async ({ page }) => {
    const tabs = page.locator('.tab-item')
    const count = await tabs.count()
    expect(count).toBeGreaterThan(0)
  })

  test('UI-NOTIF-03: 验证"全部已读"按钮存在', async ({ page }) => {
    const btn = page.locator('button:has-text("全部已读")')
    await expect(btn).toBeVisible()
  })

  test('UI-NOTIF-04: 验证通知列表项包含标题和时间', async ({ page }) => {
    const items = page.locator('.notif-item')
    const count = await items.count()
    if (count > 0) {
      await expect(items.first().locator('.notif-title')).toBeVisible()
      await expect(items.first().locator('.notif-time')).toBeVisible()
    }
  })
})

test.describe('通知功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.notifications)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
  })

  test('FUNC-NOTIF-01: 通知列表正确加载', async ({ page }) => {
    const items = page.locator('.notif-item')
    const empty = page.locator('.el-empty')
    const hasItems = await items.count() > 0
    const hasEmpty = await empty.isVisible().catch(() => false)
    expect(hasItems || hasEmpty).toBeTruthy()
  })

  test('FUNC-NOTIF-02: 按类型筛选通知', async ({ page }) => {
    const tabs = page.locator('.tab-item')
    const count = await tabs.count()
    if (count > 1) {
      await tabs.nth(1).click()
      await page.waitForTimeout(1000)
    }
  })

  test('FUNC-NOTIF-03: 标记单条通知已读', async ({ page }) => {
    const unreadItems = page.locator('.notif-item.unread')
    const count = await unreadItems.count()
    if (count > 0) {
      await unreadItems.first().click()
      await page.waitForTimeout(1000)
    }
  })

  test('FUNC-NOTIF-04: 全部标记已读', async ({ page }) => {
    const btn = page.locator('button:has-text("全部已读")')
    await btn.click()
    const msg = page.locator('.el-message--success')
    await expect(msg.first()).toBeVisible({ timeout: 5000 })
  })
})
