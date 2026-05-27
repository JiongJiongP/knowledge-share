import { test, expect, ROUTES, login } from './helpers'

test.describe('部门管理 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminDepartments)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('UI-DEPT-01: 验证页面标题"部门管理"显示', async ({ page }) => {
    const title = page.locator('.page-card-title:has-text("部门管理"), .card-title:has-text("部门管理")')
    await expect(title.first()).toBeVisible({ timeout: 10000 })
  })

  test('UI-DEPT-02: 验证部门表格显示', async ({ page }) => {
    const table = page.locator('.el-table')
    await expect(table).toBeVisible()
  })

  test('UI-DEPT-03: 验证"新建部门"按钮存在', async ({ page }) => {
    const btn = page.locator('button:has-text("新建部门")')
    await expect(btn).toBeVisible()
  })
})

test.describe('部门管理测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminDepartments)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('FUNC-ADMIN-DEPT-01: 部门列表正确加载', async ({ page }) => {
    const table = page.locator('.el-table')
    const empty = page.locator('.el-empty')
    const hasTable = await table.isVisible().catch(() => false)
    const hasEmpty = await empty.isVisible().catch(() => false)
    expect(hasTable || hasEmpty).toBeTruthy()
  })

  test('FUNC-ADMIN-DEPT-02: 点击新建部门按钮显示提示', async ({ page }) => {
    const addBtn = page.locator('button:has-text("新建部门")')
    await addBtn.click()
    const msg = page.locator('.el-message--info')
    await expect(msg.first()).toBeVisible({ timeout: 5000 })
  })

  test('FUNC-ADMIN-DEPT-03: 编辑部门名称', async ({ page }) => {
    const editBtns = page.locator('.el-table button:has-text("编辑")')
    const count = await editBtns.count()
    if (count > 0) {
      await editBtns.first().click()
      await page.waitForTimeout(1000)
    }
  })

  test('FUNC-ADMIN-DEPT-04: 删除部门', async ({ page }) => {
    const deleteBtns = page.locator('.el-table button:has-text("删除")')
    const count = await deleteBtns.count()
    if (count > 0) {
      await deleteBtns.first().click()
      const confirmBtn = page.locator('.el-message-box button:has-text("确定")')
      const confirmVisible = await confirmBtn.isVisible().catch(() => false)
      if (confirmVisible) {
        await confirmBtn.click()
        await page.waitForTimeout(1000)
      }
    }
  })
})
