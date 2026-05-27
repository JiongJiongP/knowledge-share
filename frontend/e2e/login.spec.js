import { test, expect, ROUTES, login } from './helpers'

test.describe('登录页面 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(ROUTES.login)
  })

  test('UI-LOGIN-01: 验证登录页面标题显示"◆ 知享 Knowledge Hub"', async ({ page }) => {
    const title = page.locator('h2')
    await expect(title).toContainText('知享 Knowledge Hub')
  })

  test('UI-LOGIN-02: 验证副标题显示"企业内部知识分享平台"', async ({ page }) => {
    const subtitle = page.locator('.subtitle')
    await expect(subtitle).toContainText('企业内部知识分享平台')
  })

  test('UI-LOGIN-03: 验证用户名输入框存在且placeholder为"用户名 / 工号"', async ({ page }) => {
    const input = page.locator('input[placeholder="用户名 / 工号"]')
    await expect(input).toBeVisible()
  })

  test('UI-LOGIN-04: 验证密码输入框存在且placeholder为"密码"', async ({ page }) => {
    const input = page.locator('input[placeholder="密码"]')
    await expect(input).toBeVisible()
  })

  test('UI-LOGIN-05: 验证登录按钮存在且文字为"登 录"', async ({ page }) => {
    const btn = page.locator('button:has-text("登 录")')
    await expect(btn).toBeVisible()
  })

  test('UI-LOGIN-06: 验证SSO登录按钮存在', async ({ page }) => {
    const btn = page.locator('button:has-text("企业 SSO 登录")')
    await expect(btn).toBeVisible()
  })

  test('UI-LOGIN-07: 验证LDAP登录按钮存在', async ({ page }) => {
    const btn = page.locator('button:has-text("LDAP 域账号登录")')
    await expect(btn).toBeVisible()
  })

  test('UI-LOGIN-08: 验证登录页面背景渐变样式', async ({ page }) => {
    const container = page.locator('.login-container')
    const bg = await container.evaluate(el => getComputedStyle(el).background)
    expect(bg).toBeTruthy()
  })
})

test.describe('登录功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(ROUTES.login)
  })

  test('FUNC-LOGIN-01: 使用正确用户名和密码登录成功，跳转到首页', async ({ page }) => {
    await login(page)
    await expect(page).toHaveURL(ROUTES.home)
  })

  test('FUNC-LOGIN-02: 用户名为空时显示验证错误"请输入用户名"', async ({ page }) => {
    await page.fill('input[placeholder="密码"]', 'admin123')
    await page.click('input[placeholder="用户名 / 工号"]')
    await page.click('input[placeholder="密码"]')
    const validationMsg = page.locator('.el-form-item__error:has-text("请输入用户名")')
    await expect(validationMsg).toBeVisible({ timeout: 5000 })
  })

  test('FUNC-LOGIN-03: 密码为空时显示验证错误"请输入密码"', async ({ page }) => {
    await page.fill('input[placeholder="用户名 / 工号"]', 'admin')
    await page.click('input[placeholder="密码"]')
    await page.click('input[placeholder="用户名 / 工号"]')
    const validationMsg = page.locator('.el-form-item__error:has-text("请输入密码")')
    await expect(validationMsg).toBeVisible({ timeout: 5000 })
  })

  test('FUNC-LOGIN-04: 登录失败时显示错误提示', async ({ page }) => {
    await page.fill('input[placeholder="用户名 / 工号"]', 'wronguser')
    await page.fill('input[placeholder="密码"]', 'wrongpass')
    await page.click('button:has-text("登 录")')
    const errorMsg = page.locator('.el-message--error').first()
    await expect(errorMsg).toBeVisible({ timeout: 10000 })
  })

  test('FUNC-LOGIN-06: 未登录用户访问受保护页面跳转到/login', async ({ page }) => {
    await page.context().clearCookies()
    await page.goto(ROUTES.home)
    await expect(page).toHaveURL(/\/login/, { timeout: 10000 })
  })
})
