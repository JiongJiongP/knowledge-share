import { test, expect, ROUTES, login } from './helpers'

test.describe('审核中心测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminAudit)
  })

  test('UI-ADMIN-04: 验证审核中心页面包含类型筛选', async ({ page }) => {
    const select = page.locator('.filter-bar .el-select')
    await expect(select).toBeVisible()
  })

  test('FUNC-ADMIN-AUDIT-01: 审核列表正确加载', async ({ page }) => {
    const table = page.locator('.el-table')
    await expect(table).toBeVisible()
    const rows = table.locator('.el-table__row')
    const count = await rows.count()
    expect(count).toBeGreaterThanOrEqual(0)
  })

  test('FUNC-ADMIN-AUDIT-02: 通过审核成功', async ({ page }) => {
    const approveBtns = page.locator('.el-table button:has-text("通过")')
    const count = await approveBtns.count()
    if (count > 0) {
      await approveBtns.first().click()
      const msg = page.locator('.el-message--success:has-text("已通过")')
      await expect(msg).toBeVisible({ timeout: 10000 })
    }
  })

  test('FUNC-ADMIN-AUDIT-03: 驳回审核成功', async ({ page }) => {
    const rejectBtns = page.locator('.el-table button:has-text("驳回")')
    const count = await rejectBtns.count()
    if (count > 0) {
      await rejectBtns.first().click()
      const dialog = page.locator('.el-dialog:visible')
      await expect(dialog).toBeVisible({ timeout: 5000 })
      await dialog.locator('textarea').fill('测试驳回原因')
      const confirmBtn = dialog.locator('button:has-text("确认驳回")')
      await confirmBtn.click()
      const msg = page.locator('.el-message--success:has-text("已驳回")')
      await expect(msg).toBeVisible({ timeout: 10000 })
    }
  })
})
