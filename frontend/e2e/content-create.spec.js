import { test, expect, ROUTES, login } from './helpers'

test.describe('创建内容页 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.contentCreate)
  })

  test('UI-CREATE-01: 验证页面标题"创建新内容"显示', async ({ page }) => {
    const title = page.locator('.card-title:has-text("创建新内容"), h2:has-text("创建新内容")')
    await expect(title).toBeVisible()
  })

  test('UI-CREATE-02: 验证"保存草稿"和"提交审核"按钮存在', async ({ page }) => {
    await expect(page.locator('button:has-text("保存草稿")')).toBeVisible()
    await expect(page.locator('button:has-text("提交审核")')).toBeVisible()
  })

  test('UI-CREATE-03: 验证内容类型选择器存在', async ({ page }) => {
    const typeBtns = page.locator('.type-selector .btn')
    await expect(typeBtns).toHaveCount(4)
  })

  test('UI-CREATE-04: 验证标题输入框存在', async ({ page }) => {
    const input = page.locator('input[placeholder="请输入内容标题"]')
    await expect(input).toBeVisible()
  })

  test('UI-CREATE-05: 验证Markdown编辑器工具栏存在', async ({ page }) => {
    const toolbar = page.locator('.editor-toolbar')
    await expect(toolbar).toBeVisible()
  })

  test('UI-CREATE-06: 验证Markdown编辑器文本区域存在', async ({ page }) => {
    const textarea = page.locator('.editor-textarea')
    await expect(textarea).toBeVisible()
  })

  test('UI-CREATE-07: 验证标签选择器组件存在', async ({ page }) => {
    const tagSelector = page.locator('.tag-selector')
    await expect(tagSelector).toBeVisible()
  })

  test('UI-CREATE-08: 验证群组复选框区域存在', async ({ page }) => {
    const groupArea = page.locator('.group-checkboxes, .no-groups')
    await expect(groupArea.first()).toBeVisible()
  })

  test('UI-CREATE-09: 验证定时发布输入框存在', async ({ page }) => {
    const input = page.locator('input[type="datetime-local"]')
    await expect(input).toBeVisible()
  })
})

test.describe('创建内容功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.contentCreate)
  })

  test('FUNC-CREATE-03: 标题为空时保存草稿提示"请输入标题"', async ({ page }) => {
    await page.click('button:has-text("保存草稿")')
    const msg = page.locator('.el-message:has-text("请输入标题")')
    await expect(msg).toBeVisible({ timeout: 5000 })
  })

  test('FUNC-CREATE-05: 切换内容类型功能正常', async ({ page }) => {
    const pptBtn = page.locator('.type-selector .btn:has-text("PPT文件")')
    await pptBtn.click()
    const activeBtn = page.locator('.type-selector .btn-primary')
    await expect(activeBtn).toContainText('PPT文件')
  })

  test('FUNC-CREATE-06: Markdown编辑器工具栏插入语法功能正常', async ({ page }) => {
    const boldBtn = page.locator('.editor-toolbar .btn:has-text("B")')
    await boldBtn.click()
    const textarea = page.locator('.editor-textarea')
    const value = await textarea.inputValue()
    expect(value).toContain('****')
  })

  test('FUNC-CREATE-01: 填写标题和正文后保存草稿成功', async ({ page }) => {
    const uniqueTitle = `测试草稿_${Date.now()}`
    await page.fill('input[placeholder="请输入内容标题"]', uniqueTitle)
    await page.fill('.editor-textarea', '# 测试内容\n\n这是一段测试正文。')
    await page.click('button:has-text("保存草稿")')
    const msg = page.locator('.el-message--success:has-text("草稿已保存")')
    await expect(msg).toBeVisible({ timeout: 10000 })
    await expect(page).toHaveURL(/\/content\//, { timeout: 10000 })
  })

  test('FUNC-CREATE-02: 填写标题和正文后提交审核成功', async ({ page }) => {
    const uniqueTitle = `测试提交_${Date.now()}`
    await page.fill('input[placeholder="请输入内容标题"]', uniqueTitle)
    await page.fill('.editor-textarea', '# 测试提交\n\n提交审核测试正文。')
    await page.click('button:has-text("提交审核")')
    const msg = page.locator('.el-message--success')
    await expect(msg).toBeVisible({ timeout: 15000 })
  })

  test('FUNC-CREATE-04: 正文为空时提交审核提示"请输入正文"', async ({ page }) => {
    await page.fill('input[placeholder="请输入内容标题"]', '测试标题')
    await page.click('button:has-text("提交审核")')
    const msg = page.locator('.el-message:has-text("请输入正文")')
    await expect(msg).toBeVisible({ timeout: 5000 })
  })
})
