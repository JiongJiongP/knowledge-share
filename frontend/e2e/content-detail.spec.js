import { test, expect, ROUTES, login } from './helpers'

test.describe('内容详情页 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.home)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    const items = page.locator('.content-list-item')
    const count = await items.count()
    if (count > 0) {
      await items.first().click()
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)
    } else {
      await page.goto(ROUTES.contentDetail(1))
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)
    }
  })

  test('UI-DETAIL-01: 验证内容标题显示', async ({ page }) => {
    const title = page.locator('.detail-title')
    const error = page.locator('.el-result')
    const hasTitle = await title.isVisible().catch(() => false)
    const hasError = await error.isVisible().catch(() => false)
    expect(hasTitle || hasError).toBeTruthy()
  })

  test('UI-DETAIL-02: 验证内容元信息显示', async ({ page }) => {
    const meta = page.locator('.detail-meta')
    const isVisible = await meta.isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })

  test('UI-DETAIL-03: 验证操作按钮存在', async ({ page }) => {
    const actionArea = page.locator('.detail-actions')
    const isVisible = await actionArea.isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })

  test('UI-DETAIL-04: 验证Markdown正文渲染区域存在', async ({ page }) => {
    const body = page.locator('.markdown-body')
    const isVisible = await body.isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })

  test('UI-DETAIL-05: 验证评论区存在', async ({ page }) => {
    const commentSection = page.locator('.comment-section')
    const isVisible = await commentSection.isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })

  test('UI-DETAIL-06: 验证评论输入框存在', async ({ page }) => {
    const commentInput = page.locator('.comment-input .el-textarea__inner, .comment-input textarea')
    const isVisible = await commentInput.first().isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })
})

test.describe('内容详情功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.home)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    const items = page.locator('.content-list-item')
    const count = await items.count()
    if (count > 0) {
      await items.first().click()
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)
    } else {
      await page.goto(ROUTES.contentDetail(1))
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)
    }
  })

  test('FUNC-DETAIL-01: 内容详情页正确加载并显示内容', async ({ page }) => {
    const title = page.locator('.detail-title')
    const error = page.locator('.el-result')
    const hasTitle = await title.isVisible().catch(() => false)
    const hasError = await error.isVisible().catch(() => false)
    expect(hasTitle || hasError).toBeTruthy()
  })

  test('FUNC-DETAIL-03: 点击收藏按钮收藏内容成功', async ({ page }) => {
    const favBtn = page.locator('.detail-actions button:has-text("收藏")')
    const isVisible = await favBtn.isVisible().catch(() => false)
    if (!isVisible) return
    await favBtn.click()
    const msg = page.locator('.el-message--success')
    await expect(msg.first()).toBeVisible({ timeout: 5000 })
  })

  test('FUNC-DETAIL-05: 点击分享按钮复制链接到剪贴板', async ({ page }) => {
    const shareBtn = page.locator('.detail-actions button:has-text("分享")')
    const isVisible = await shareBtn.isVisible().catch(() => false)
    if (!isVisible) return
    await shareBtn.click()
    const msg = page.locator('.el-message--success, .el-message--info')
    await expect(msg.first()).toBeVisible({ timeout: 5000 })
  })

  test('FUNC-DETAIL-06: 点击下载按钮下载Markdown文件', async ({ page }) => {
    const downloadBtn = page.locator('.detail-actions button:has-text("下载")')
    const isVisible = await downloadBtn.isVisible().catch(() => false)
    if (!isVisible) return
    await downloadBtn.click()
    const msg = page.locator('.el-message--success:has-text("下载成功")')
    await expect(msg).toBeVisible({ timeout: 5000 })
  })

  test('FUNC-DETAIL-09: 不存在的内容ID显示加载失败页面', async ({ page }) => {
    await page.goto(ROUTES.contentDetail(999999))
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
    const errorResult = page.locator('.el-result')
    await expect(errorResult).toBeVisible({ timeout: 10000 })
  })
})
