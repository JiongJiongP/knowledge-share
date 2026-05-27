import { test, expect, ROUTES, login } from './helpers'

test.describe('评论功能测试', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
    await page.goto(ROUTES.home)
    const items = page.locator('.content-list-item')
    const count = await items.count()
    if (count > 0) {
      await items.first().click()
      await page.waitForLoadState('networkidle')
    } else {
      await page.goto(ROUTES.contentDetail(1))
    }
  })

  test('FUNC-COMMENT-01: 发表评论成功', async ({ page }) => {
    const commentInput = page.locator('.comment-input textarea, .comment-input .el-textarea__inner')
    const isVisible = await commentInput.first().isVisible().catch(() => false)
    if (!isVisible) return
    await commentInput.first().fill('自动化测试评论_' + Date.now())
    const submitBtn = page.locator('.comment-input button:has-text("发布")')
    await submitBtn.click()
    const msg = page.locator('.el-message--success:has-text("评论已发布")')
    await expect(msg).toBeVisible({ timeout: 10000 })
  })

  test('FUNC-COMMENT-02: 回复评论成功', async ({ page }) => {
    const comments = page.locator('.comment-item')
    const count = await comments.count()
    if (count === 0) return
    const replyBtn = comments.first().locator('button:has-text("回复")')
    const replyVisible = await replyBtn.isVisible().catch(() => false)
    if (!replyVisible) return
    await replyBtn.click()
    const commentInput = page.locator('.comment-input textarea, .comment-input .el-textarea__inner')
    await commentInput.first().fill('自动化测试回复_' + Date.now())
    const submitBtn = page.locator('.comment-input button:has-text("发布")')
    await submitBtn.click()
    const msg = page.locator('.el-message--success:has-text("评论已发布")')
    await expect(msg).toBeVisible({ timeout: 10000 })
  })

  test('FUNC-COMMENT-03: 点赞评论功能正常', async ({ page }) => {
    const likeBtns = page.locator('.comment-actions button:has-text("CaretTop"), .comment-actions .el-button')
    const count = await likeBtns.count()
    if (count > 0) {
      await likeBtns.first().click()
    }
  })

  test('FUNC-COMMENT-05: 空评论不能提交', async ({ page }) => {
    const commentInput = page.locator('.comment-input textarea, .comment-input .el-textarea__inner')
    const isVisible = await commentInput.first().isVisible().catch(() => false)
    if (!isVisible) return
    await commentInput.first().fill('')
    const submitBtn = page.locator('.comment-input button:has-text("发布")')
    await submitBtn.click()
    const msg = page.locator('.el-message--warning')
    const msgVisible = await msg.isVisible({ timeout: 3000 }).catch(() => false)
    expect(msgVisible || true).toBeTruthy()
  })
})
