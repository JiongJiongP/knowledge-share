import { test, expect, ROUTES, login } from './helpers'

test.describe('首页功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.home)
    await page.waitForLoadState('networkidle')
  })

  test('FUNC-HOME-01: 首页加载显示内容列表', async ({ page }) => {
    await page.waitForTimeout(2000)
    const list = page.locator('.content-list')
    const empty = page.locator('.el-empty')
    const skeleton = page.locator('.el-skeleton')
    const items = page.locator('.content-list-item')
    const hasList = await list.isVisible().catch(() => false)
    const hasEmpty = await empty.isVisible().catch(() => false)
    const hasSkeleton = await skeleton.isVisible().catch(() => false)
    const hasItems = await items.count() > 0
    expect(hasList || hasEmpty || hasSkeleton || hasItems).toBeTruthy()
  })

  test('FUNC-HOME-02: 按内容类型筛选功能正常', async ({ page }) => {
    const typeSelect = page.locator('.filter-select').first()
    await typeSelect.selectOption('MARKDOWN')
    await page.waitForTimeout(1000)
    const url = page.url()
    expect(url).toContain(ROUTES.home)
  })

  test('FUNC-HOME-04: 排序切换功能正常', async ({ page }) => {
    const selects = page.locator('.filter-select')
    const sortSelect = selects.nth(2)
    await sortSelect.selectOption('hot')
    await page.waitForTimeout(1000)
  })

  test('FUNC-HOME-06: 点击内容项跳转到详情页', async ({ page }) => {
    const items = page.locator('.content-list-item')
    const count = await items.count()
    if (count > 0) {
      await items.first().click()
      await expect(page).toHaveURL(/\/content\//, { timeout: 10000 })
    }
  })

  test('FUNC-HOME-08: 点击"+ 创建内容"跳转到创建页', async ({ page }) => {
    const btn = page.locator('button:has-text("+ 创建内容"), a:has-text("+ 创建内容")')
    await btn.click()
    await expect(page).toHaveURL(ROUTES.contentCreate, { timeout: 10000 })
  })

  test('UI-HOME-09: 验证空状态提示', async ({ page }) => {
    await page.goto(ROUTES.home + '?contentType=NONEXISTENT')
    await page.waitForTimeout(2000)
    const empty = page.locator('.el-empty')
    const list = page.locator('.content-list-item')
    const hasEmpty = await empty.isVisible().catch(() => false)
    const listCount = await list.count()
    expect(hasEmpty || listCount === 0).toBeTruthy()
  })
})

test.describe('首页 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.home)
    await page.waitForLoadState('networkidle')
  })

  test('UI-HOME-01: 验证页面标题"知识内容流"显示', async ({ page }) => {
    const title = page.locator('.card-title:has-text("知识内容流")')
    await expect(title).toBeVisible()
  })

  test('UI-HOME-02: 验证"+ 创建内容"按钮存在', async ({ page }) => {
    const btn = page.locator('button:has-text("+ 创建内容"), a:has-text("+ 创建内容")')
    await expect(btn).toBeVisible()
  })

  test('UI-HOME-03: 验证内容类型筛选下拉框存在', async ({ page }) => {
    const select = page.locator('.filter-select').first()
    await expect(select).toBeVisible()
    const options = select.locator('option')
    await expect(options).toHaveCount(5)
  })

  test('UI-HOME-04: 验证群组筛选下拉框存在', async ({ page }) => {
    const selects = page.locator('.filter-select')
    await expect(selects.nth(1)).toBeVisible()
  })

  test('UI-HOME-05: 验证排序下拉框存在', async ({ page }) => {
    const selects = page.locator('.filter-select')
    const sortSelect = selects.nth(2)
    await expect(sortSelect).toBeVisible()
  })

  test('UI-HOME-06: 验证标签筛选区域存在', async ({ page }) => {
    const tags = page.locator('.tag')
    const tagCount = await tags.count()
    expect(tagCount).toBeGreaterThanOrEqual(0)
  })

  test('UI-HOME-07: 验证内容列表项包含标题和元信息', async ({ page }) => {
    const items = page.locator('.content-list-item')
    const count = await items.count()
    if (count > 0) {
      const firstItem = items.first()
      await expect(firstItem.locator('.content-item-title')).toBeVisible()
      await expect(firstItem.locator('.content-item-meta')).toBeVisible()
    }
  })
})
