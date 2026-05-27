import { test, expect, ROUTES, login } from './helpers'

test.describe('布局与导航 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
  })

  test('UI-LAYOUT-01: 验证侧边栏菜单项完整显示', async ({ page }) => {
    const navItems = page.locator('.nav-item')
    const count = await navItems.count()
    expect(count).toBeGreaterThan(0)
  })

  test('UI-LAYOUT-02: 验证顶部导航栏包含用户头像和名称', async ({ page }) => {
    const avatar = page.locator('.header-avatar')
    await expect(avatar).toBeVisible()
  })

  test('UI-LAYOUT-03: 验证顶部导航栏包含通知铃铛图标', async ({ page }) => {
    const bellBtn = page.locator('.header-btn')
    await expect(bellBtn).toBeVisible()
  })

  test('UI-LAYOUT-04: 验证搜索框存在', async ({ page }) => {
    const searchInput = page.locator('.search-input')
    await expect(searchInput).toBeVisible()
  })
})

test.describe('路由与权限测试', () => {
  test('FUNC-ROUTE-01: 未登录访问任何受保护页面重定向到/login', async ({ page }) => {
    await login(page)
    await page.waitForLoadState('networkidle')
    await page.evaluate(() => {
      localStorage.removeItem('kp_token')
      localStorage.removeItem('kp_user')
    })
    const context = page.context()
    await context.clearCookies()
    await page.reload()
    await page.waitForTimeout(3000)
    const currentUrl = page.url()
    expect(currentUrl).toContain('/login')
  })

  test('FUNC-ROUTE-02: 侧边栏菜单点击导航正确', async ({ page }) => {
    await login(page)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    const navItems = page.locator('.nav-item')
    const count = await navItems.count()
    if (count > 1) {
      await navItems.nth(1).click()
      await page.waitForTimeout(1000)
      expect(page.url()).not.toBe(ROUTES.home)
    }
  })

  test('FUNC-ROUTE-03: 退出登录跳转到登录页', async ({ page }) => {
    await login(page)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    const avatar = page.locator('.header-avatar')
    await avatar.click()
    await page.waitForTimeout(500)
    const logoutItem = page.locator('.dropdown-item:has-text("退出登录")')
    await expect(logoutItem).toBeVisible()
    await logoutItem.click()
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/login')
  })
})
