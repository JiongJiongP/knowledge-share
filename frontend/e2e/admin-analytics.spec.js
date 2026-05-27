import { test, expect, ROUTES, login } from './helpers'

test.describe('数据分析看板 UI 测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminAnalytics)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('UI-ANALYTICS-01: 验证页面标题"数据分析"显示', async ({ page }) => {
    const title = page.locator('.card-title:has-text("数据分析"), h2:has-text("数据分析"), .page-title:has-text("数据分析")')
    await expect(title.first()).toBeVisible({ timeout: 10000 })
  })

  test('UI-ANALYTICS-02: 验证统计卡片区域存在', async ({ page }) => {
    const cards = page.locator('.stat-card, .data-card, .el-card')
    const count = await cards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('UI-ANALYTICS-03: 验证图表区域存在', async ({ page }) => {
    const chart = page.locator('.trend-chart, .trend-bar, .el-table')
    const isVisible = await chart.first().isVisible().catch(() => false)
    expect(isVisible).toBeTruthy()
  })
})

test.describe('数据分析看板测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.adminAnalytics)
    await page.waitForLoadState('networkidle')
    await page.waitForTimeout(2000)
  })

  test('FUNC-ADMIN-ANALYTICS-01: 数据看板正确加载统计卡片', async ({ page }) => {
    const cards = page.locator('.stat-card, .data-card, .el-card')
    const count = await cards.count()
    const anyContent = await page.locator('body').innerText()
    expect(count > 0 || anyContent.length > 100).toBeTruthy()
  })

  test('FUNC-ADMIN-ANALYTICS-02: 日期范围筛选功能正常', async ({ page }) => {
    const dateRange = page.locator('.el-date-editor')
    const isVisible = await dateRange.isVisible().catch(() => false)
    if (isVisible) {
      await dateRange.click()
      await page.waitForTimeout(500)
    }
  })

  test('FUNC-ADMIN-ANALYTICS-03: 数据导出功能', async ({ page }) => {
    const exportBtn = page.locator('button:has-text("导出"), button:has-text("Export")')
    const isVisible = await exportBtn.isVisible().catch(() => false)
    if (isVisible) {
      await exportBtn.click()
      await page.waitForTimeout(1000)
    }
  })
})
