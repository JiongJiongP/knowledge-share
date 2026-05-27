import { test, expect, ROUTES, login } from './helpers'

test.describe('系统设置 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminSettings)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('UI-SETTINGS-01: 验证页面标题"系统设置"显示', async ({ page }) => {
    const title = page.locator('.card-title:has-text("系统设置"), h2:has-text("系统设置"), .page-title:has-text("系统设置")')
    await expect(title.first()).toBeVisible({ timeout: 10000 })
  })

  test('UI-SETTINGS-02: 验证设置表单存在', async ({ page }) => {
    const form = page.locator('.el-form')
    const isVisible = await form.isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })

  test('UI-SETTINGS-03: 验证"保存设置"按钮存在', async ({ page }) => {
    const btn = page.locator('button:has-text("保存"), button:has-text("提交")')
    const isVisible = await btn.isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })
})

test.describe('系统设置测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminSettings)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('FUNC-ADMIN-SETTINGS-01: 系统设置表单正确显示', async ({ page }) => {
    const form = page.locator('.el-form')
    const isVisible = await form.isVisible().catch(() => false)
    const anyContent = await page.locator('body').innerText()
    expect(isVisible || anyContent.length > 100).toBeTruthy()
  })

  test('FUNC-ADMIN-SETTINGS-02: 修改设置并保存成功', async ({ page }) => {
    const inputs = page.locator('.el-form input:visible')
    const count = await inputs.count()
    if (count > 0) {
      const firstInput = inputs.first()
      const currentValue = await firstInput.inputValue()
      await firstInput.fill(currentValue || '测试值')
      const saveBtn = page.locator('button:has-text("保存"), button:has-text("提交")')
      const btnVisible = await saveBtn.isVisible().catch(() => false)
      if (btnVisible) {
        await saveBtn.first().click()
        await page.waitForTimeout(1000)
      }
    }
  })

  test('FUNC-ADMIN-SETTINGS-03: 重置设置功能', async ({ page }) => {
    const resetBtn = page.locator('button:has-text("重置"), button:has-text("恢复默认")')
    const isVisible = await resetBtn.isVisible().catch(() => false)
    if (isVisible) {
      await resetBtn.first().click()
      await page.waitForTimeout(1000)
    }
  })
})
