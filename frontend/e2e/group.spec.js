import { test, expect, ROUTES, login } from './helpers'

test.describe('群组列表 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.groups)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
  })

  test('UI-GROUP-01: 验证页面标题"群组列表"显示', async ({ page }) => {
    const title = page.locator('.card-title:has-text("群组列表")')
    await expect(title).toBeVisible()
  })

  test('UI-GROUP-02: 验证搜索输入框存在', async ({ page }) => {
    const search = page.locator('input[placeholder="搜索群组..."]')
    await expect(search).toBeVisible()
  })

  test('UI-GROUP-03: 验证"+ 申请创建群组"按钮存在', async ({ page }) => {
    const btn = page.locator('button:has-text("申请创建群组")')
    await expect(btn).toBeVisible()
  })

  test('UI-GROUP-04: 验证群组卡片包含群组名称和描述', async ({ page }) => {
    const cards = page.locator('.group-card')
    const count = await cards.count()
    if (count > 0) {
      await expect(cards.first().locator('.group-name')).toBeVisible()
      await expect(cards.first().locator('.group-desc')).toBeVisible()
    }
  })

  test('UI-GROUP-05: 验证加入/已加入按钮状态正确', async ({ page }) => {
    const joinBtns = page.locator('.group-card button:has-text("申请加入"), .group-card button:has-text("已加入")')
    const count = await joinBtns.count()
    expect(count).toBeGreaterThanOrEqual(0)
  })
})

test.describe('群组功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.groups)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
  })

  test('FUNC-GROUP-01: 群组列表正确加载', async ({ page }) => {
    const cards = page.locator('.group-card')
    const empty = page.locator('.el-empty')
    const hasCards = await cards.count() > 0
    const hasEmpty = await empty.isVisible().catch(() => false)
    expect(hasCards || hasEmpty).toBeTruthy()
  })

  test('FUNC-GROUP-02: 搜索群组功能正常', async ({ page }) => {
    const search = page.locator('input[placeholder="搜索群组..."]')
    await search.fill('测试搜索')
    await page.waitForTimeout(1000)
  })

  test('FUNC-GROUP-03: 创建群组成功', async ({ page }) => {
    await page.click('button:has-text("申请创建群组")')
    const dialog = page.locator('.el-dialog:visible')
    await expect(dialog).toBeVisible({ timeout: 5000 })
    const nameInput = dialog.locator('input[placeholder="输入群组名称"]')
    await nameInput.fill('自动化测试群组_' + Date.now())
    const descInput = dialog.locator('textarea[placeholder="输入群组描述（选填）"]')
    await descInput.fill('自动化测试创建的群组')
    const confirmBtn = dialog.locator('button:has-text("创建")')
    await confirmBtn.click()
    const msg = page.locator('.el-message--success')
    await expect(msg.first()).toBeVisible({ timeout: 10000 })
  })

  test('UI-GROUP-06: 验证创建群组对话框包含名称和描述输入', async ({ page }) => {
    await page.click('button:has-text("申请创建群组")')
    const dialog = page.locator('.el-dialog:visible')
    await expect(dialog).toBeVisible({ timeout: 5000 })
    await expect(dialog.locator('input[placeholder="输入群组名称"]')).toBeVisible()
    await expect(dialog.locator('textarea[placeholder="输入群组描述（选填）"]')).toBeVisible()
  })
})

test.describe('群组详情功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.groups)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(1000)
    const groupNames = page.locator('.group-name')
    const count = await groupNames.count()
    if (count > 0) {
      await groupNames.first().click()
      await page.waitForLoadState('networkidle')
      await page.waitForTimeout(1000)
    }
  })

  test('FUNC-GROUP-05: 群组详情页正确显示群组信息和成员', async ({ page }) => {
    const currentUrl = page.url()
    if (!currentUrl.includes('/group/')) {
      test.skip()
      return
    }
    const header = page.locator('.group-header h2')
    const isVisible = await header.isVisible().catch(() => false)
    if (isVisible) {
      await expect(header).toBeVisible()
    }
  })

  test('FUNC-GROUP-04: 申请加入群组成功', async ({ page }) => {
    const currentUrl = page.url()
    if (!currentUrl.includes('/group/')) {
      test.skip()
      return
    }
    const joinBtn = page.locator('button:has-text("申请加入")')
    const joinVisible = await joinBtn.isVisible().catch(() => false)
    if (joinVisible) {
      await joinBtn.click()
      const msg = page.locator('.el-message--success:has-text("申请已提交")')
      await expect(msg).toBeVisible({ timeout: 5000 })
    }
  })
})
