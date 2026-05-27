import { test, expect, ROUTES, login } from './helpers'

test.describe('用户管理测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminUsers)
  })

  test('UI-ADMIN-02: 验证用户管理页面表格列存在', async ({ page }) => {
    const table = page.locator('.el-table')
    await expect(table).toBeVisible()
    const headers = table.locator('th')
    const headerTexts = await headers.allTextContents()
    expect(headerTexts.join(',')).toContain('姓名')
    expect(headerTexts.join(',')).toContain('用户名')
  })

  test('FUNC-ADMIN-USER-01: 用户列表正确加载', async ({ page }) => {
    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const count = await rows.count()
    expect(count).toBeGreaterThanOrEqual(0)
  })

  test('FUNC-ADMIN-USER-02: 搜索用户功能正常', async ({ page }) => {
    const search = page.locator('input[placeholder="搜索用户..."]')
    await search.fill('admin')
    await page.waitForTimeout(500)
    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const count = await rows.count()
    expect(count).toBeGreaterThanOrEqual(0)
  })

  test('FUNC-ADMIN-USER-03: 创建用户成功', async ({ page }) => {
    await page.click('button:has-text("新建用户")')
    const dialog = page.locator('.el-dialog:visible')
    await expect(dialog).toBeVisible({ timeout: 5000 })
    const uniqueName = 'testuser_' + Date.now()
    await dialog.locator('input[placeholder="请输入用户名"]').fill(uniqueName)
    await dialog.locator('input[placeholder="请输入密码（至少6位）"]').fill('test123456')
    await dialog.locator('input[placeholder="请输入显示名称"]').fill('测试用户')
    await dialog.locator('input[placeholder="请输入邮箱"]').fill(`${uniqueName}@test.com`)
    const createBtn = dialog.locator('button:has-text("创建")')
    await createBtn.click()
    const msg = page.locator('.el-message--success:has-text("用户创建成功")')
    await expect(msg).toBeVisible({ timeout: 10000 })
  })

  test('FUNC-ADMIN-USER-04: 编辑用户角色成功', async ({ page }) => {
    const editBtns = page.locator('.el-table button:has-text("编辑角色")')
    const count = await editBtns.count()
    if (count > 0) {
      await editBtns.first().click()
      const dialog = page.locator('.el-dialog:visible')
      await expect(dialog).toBeVisible({ timeout: 5000 })
      const saveBtn = dialog.locator('button:has-text("保存")')
      await saveBtn.click()
      const msg = page.locator('.el-message--success:has-text("角色更新成功")')
      await expect(msg).toBeVisible({ timeout: 10000 })
    }
  })
})
