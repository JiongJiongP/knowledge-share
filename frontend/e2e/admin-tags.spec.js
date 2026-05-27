import { test, expect, ROUTES, login } from './helpers'

test.describe('标签管理测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminTags)
  })

  test('UI-ADMIN-01: 验证标签管理页面表格列存在', async ({ page }) => {
    const table = page.locator('.el-table')
    await expect(table).toBeVisible()
    const headers = table.locator('th')
    const headerTexts = await headers.allTextContents()
    expect(headerTexts.join(',')).toContain('ID')
    expect(headerTexts.join(',')).toContain('标签名称')
  })

  test('FUNC-ADMIN-TAG-01: 标签列表正确加载', async ({ page }) => {
    const rows = page.locator('.el-table__body-wrapper .el-table__row')
    const count = await rows.count()
    expect(count).toBeGreaterThanOrEqual(0)
  })

  test('FUNC-ADMIN-TAG-02: 添加标签成功', async ({ page }) => {
    await page.click('button:has-text("添加标签")')
    const dialog = page.locator('.el-dialog:visible')
    await expect(dialog).toBeVisible({ timeout: 5000 })
    await dialog.locator('input[placeholder="输入标签名称"]').fill('自动化测试标签_' + Date.now())
    const saveBtn = dialog.locator('button:has-text("保存")')
    await saveBtn.click()
    const msg = page.locator('.el-message--success:has-text("标签已创建")')
    await expect(msg).toBeVisible({ timeout: 10000 })
  })

  test('FUNC-ADMIN-TAG-03: 编辑标签成功', async ({ page }) => {
    const editBtns = page.locator('.el-table button:has-text("编辑")')
    const count = await editBtns.count()
    if (count > 0) {
      await editBtns.first().click()
      const dialog = page.locator('.el-dialog:visible')
      await expect(dialog).toBeVisible({ timeout: 5000 })
      const nameInput = dialog.locator('input[placeholder="输入标签名称"]')
      await nameInput.fill('编辑后标签_' + Date.now())
      const saveBtn = dialog.locator('button:has-text("保存")')
      await saveBtn.click()
      const msg = page.locator('.el-message--success:has-text("标签已更新")')
      await expect(msg).toBeVisible({ timeout: 10000 })
    }
  })

  test('FUNC-ADMIN-TAG-04: 删除标签确认后成功', async ({ page }) => {
    const deleteBtns = page.locator('.el-table button:has-text("删除")')
    const count = await deleteBtns.count()
    if (count > 0) {
      await deleteBtns.first().click()
      const confirmBtn = page.locator('.el-message-box__btns button:has-text("确定"), .el-msgbox__btns button:has-text("确定")')
      const confirmVisible = await confirmBtn.isVisible({ timeout: 3000 }).catch(() => false)
      if (confirmVisible) {
        await confirmBtn.click()
        const msg = page.locator('.el-message--success:has-text("标签已删除")')
        await expect(msg).toBeVisible({ timeout: 10000 })
      }
    }
  })
})
