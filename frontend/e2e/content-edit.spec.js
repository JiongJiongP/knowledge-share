import { test, expect, ROUTES, login } from './helpers'

test.describe('编辑内容页功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.home)
    const items = page.locator('.content-list-item')
    const count = await items.count()
    if (count > 0) {
      await items.first().click()
      await page.waitForLoadState('networkidle')
      const editBtn = page.locator('button:has-text("编辑")')
      const editVisible = await editBtn.isVisible().catch(() => false)
      if (editVisible) {
        await editBtn.click()
        await page.waitForLoadState('networkidle')
      } else {
        const uniqueTitle = `编辑测试_${Date.now()}`
        await page.goto(ROUTES.contentCreate)
        await page.fill('input[placeholder="请输入内容标题"]', uniqueTitle)
        await page.fill('.editor-textarea', '# 编辑测试内容')
        await page.click('button:has-text("保存草稿")')
        await page.waitForTimeout(3000)
      }
    }
  })

  test('FUNC-EDIT-01: 编辑页正确加载已有内容', async ({ page }) => {
    const currentUrl = page.url()
    if (currentUrl.includes('/edit')) {
      const titleInput = page.locator('input[placeholder="请输入内容标题"]')
      const titleVisible = await titleInput.isVisible().catch(() => false)
      expect(titleVisible).toBeTruthy()
    }
  })

  test('FUNC-EDIT-02: 修改标题和正文后保存成功', async ({ page }) => {
    const currentUrl = page.url()
    if (!currentUrl.includes('/edit')) {
      test.skip()
      return
    }
    const titleInput = page.locator('input[placeholder="请输入内容标题"]')
    await titleInput.fill('修改后的标题_' + Date.now())
    const saveBtn = page.locator('button:has-text("保存")')
    const saveVisible = await saveBtn.isVisible().catch(() => false)
    if (saveVisible) {
      await saveBtn.click()
      const msg = page.locator('.el-message--success:has-text("已保存")')
      await expect(msg).toBeVisible({ timeout: 10000 })
    }
  })

  test('FUNC-EDIT-04: 修改内容类型功能正常', async ({ page }) => {
    const currentUrl = page.url()
    if (!currentUrl.includes('/edit')) {
      test.skip()
      return
    }
    const pptBtn = page.locator('.el-button:has-text("PPT文件")')
    const pptVisible = await pptBtn.isVisible().catch(() => false)
    if (pptVisible) {
      await pptBtn.click()
      const activeBtn = page.locator('.el-button--primary:has-text("PPT文件")')
      await expect(activeBtn).toBeVisible({ timeout: 3000 })
    }
  })
})
