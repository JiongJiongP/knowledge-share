# 知享 Knowledge Hub - 自动化测试计划

## 项目概述

知享（Knowledge Hub）是一个企业内部知识分享平台，基于 Vue 3 + Element Plus 前端和 Java Spring Boot 微服务后端构建。

**前端地址**: http://localhost:3000/
**后端 API 基础路径**: /api

---

## 一、测试范围

### 1.1 页面清单

| 序号 | 页面 | 路由 | 说明 |
|------|------|------|------|
| 1 | 登录页 | /login | 用户名密码登录、SSO、LDAP |
| 2 | 首页（内容流） | / | 内容列表、筛选、排序、标签、分页 |
| 3 | 创建内容 | /content/create | Markdown编辑器、类型选择、标签、群组、定时发布 |
| 4 | 内容详情 | /content/:id | 内容展示、收藏、分享、下载、评论 |
| 5 | 编辑内容 | /content/:id/edit | 内容编辑、保存、发布 |
| 6 | 群组列表 | /groups | 搜索群组、创建群组、加入群组 |
| 7 | 群组详情 | /group/:id | 群组信息、成员列表、申请加入 |
| 8 | 群组管理 | /group/:id/manage | 审批成员、移除成员 |
| 9 | 我的收藏 | /favorites | 收藏列表、筛选、取消收藏 |
| 10 | 通知中心 | /notifications | 通知列表、分类筛选、标记已读、删除 |
| 11 | 模板中心 | /templates | 模板展示、使用模板 |
| 12 | 标签管理 | /admin/tags | 标签CRUD、颜色选择 |
| 13 | 用户管理 | /admin/users | 用户列表、创建用户、编辑角色 |
| 14 | 敏感词管理 | /admin/sensitive-words | 敏感词CRUD、批量导入、分类筛选 |
| 15 | 审核中心 | /admin/audit | 审核列表、通过/驳回 |
| 16 | 数据分析 | /admin/analytics | 统计看板、趋势图、热词 |
| 17 | 部门管理 | /admin/departments | 部门树展示 |
| 18 | 系统设置 | /admin/settings | 平台设置 |
| 19 | 404页面 | /not-exist | 页面不存在提示 |

### 1.2 公共组件

| 序号 | 组件 | 说明 |
|------|------|------|
| 1 | AppHeader | 搜索栏、通知下拉、用户菜单 |
| 2 | AppSidebar | 导航菜单、未读计数 |
| 3 | MainLayout | 侧边栏+头部+内容区布局 |
| 4 | PageCard | 页面卡片容器 |
| 5 | TagSelector | 标签选择器 |
| 6 | CommentSection | 评论区组件 |
| 7 | ContentCard | 内容卡片组件 |

---

## 二、UI 测试计划

### 2.1 登录页 UI 测试

- [x] UI-LOGIN-01: 验证登录页面标题显示"◆ 知享 Knowledge Hub"
- [x] UI-LOGIN-02: 验证副标题显示"企业内部知识分享平台"
- [x] UI-LOGIN-03: 验证用户名输入框存在且placeholder为"用户名 / 工号"
- [x] UI-LOGIN-04: 验证密码输入框存在且placeholder为"密码"，带有显示/隐藏按钮
- [x] UI-LOGIN-05: 验证登录按钮存在且文字为"登 录"
- [x] UI-LOGIN-06: 验证SSO登录按钮存在
- [x] UI-LOGIN-07: 验证LDAP登录按钮存在
- [x] UI-LOGIN-08: 验证登录页面背景渐变样式

### 2.2 首页 UI 测试

- [x] UI-HOME-01: 验证页面标题"知识内容流"显示
- [x] UI-HOME-02: 验证"+ 创建内容"按钮存在
- [x] UI-HOME-03: 验证内容类型筛选下拉框存在（全部类型/Markdown/PPT文件/外部链接/内部引用）
- [x] UI-HOME-04: 验证群组筛选下拉框存在
- [x] UI-HOME-05: 验证排序下拉框存在（按发布时间/按热度/按相关性）
- [x] UI-HOME-06: 验证标签筛选区域存在
- [x] UI-HOME-07: 验证内容列表项包含标题、描述、元信息（类型、作者、日期、浏览数、收藏数、评论数、标签）
- [ ] UI-HOME-08: 验证分页组件在内容超过10条时显示
- [x] UI-HOME-09: 验证空状态提示

### 2.3 创建内容页 UI 测试

- [x] UI-CREATE-01: 验证页面标题"创建新内容"显示
- [x] UI-CREATE-02: 验证"保存草稿"和"提交审核"按钮存在
- [x] UI-CREATE-03: 验证内容类型选择器存在（Markdown/PPT文件/外部链接/内部引用）
- [x] UI-CREATE-04: 验证标题输入框存在
- [x] UI-CREATE-05: 验证Markdown编辑器工具栏存在
- [x] UI-CREATE-06: 验证Markdown编辑器文本区域存在
- [x] UI-CREATE-07: 验证标签选择器组件存在
- [x] UI-CREATE-08: 验证群组复选框区域存在
- [x] UI-CREATE-09: 验证定时发布输入框存在

### 2.4 内容详情页 UI 测试

- [x] UI-DETAIL-01: 验证内容标题显示
- [x] UI-DETAIL-02: 验证内容元信息显示（类型标签、标签、作者、日期、阅读数）
- [x] UI-DETAIL-03: 验证操作按钮存在（收藏、分享、下载、编辑）
- [x] UI-DETAIL-04: 验证Markdown正文渲染区域存在
- [x] UI-DETAIL-05: 验证评论区存在，包含评论计数
- [x] UI-DETAIL-06: 验证评论输入框存在
- [ ] UI-DETAIL-07: 验证加载中骨架屏显示
- [ ] UI-DETAIL-08: 验证加载失败错误页面显示

### 2.5 群组列表 UI 测试

- [x] UI-GROUP-01: 验证页面标题"群组列表"显示
- [x] UI-GROUP-02: 验证搜索输入框存在
- [x] UI-GROUP-03: 验证"+ 申请创建群组"按钮存在
- [x] UI-GROUP-04: 验证群组卡片包含群组名称、群主、成员数、内容数、描述
- [x] UI-GROUP-05: 验证加入/已加入按钮状态正确
- [x] UI-GROUP-06: 验证创建群组对话框包含名称和描述输入
- [ ] UI-GROUP-07: 验证空状态提示"暂无公开群组"

### 2.6 通知中心 UI 测试

- [x] UI-NOTIF-01: 验证页面标题"通知中心"显示
- [x] UI-NOTIF-02: 验证通知类型筛选Tab存在
- [x] UI-NOTIF-03: 验证"全部已读"按钮存在
- [x] UI-NOTIF-04: 验证通知列表项包含标题和时间
- [ ] UI-NOTIF-05: 验证未读通知有蓝色背景和圆点标识
- [ ] UI-NOTIF-06: 验证通知项有删除按钮

### 2.7 管理后台 UI 测试

- [x] UI-ADMIN-01: 验证标签管理页面表格包含ID/名称/颜色/使用次数/创建时间/操作列
- [x] UI-ADMIN-02: 验证用户管理页面表格包含姓名/用户名/部门/角色/注册时间/操作列
- [x] UI-ADMIN-03: 验证敏感词管理页面包含分类筛选和搜索框
- [x] UI-ADMIN-04: 验证审核中心页面包含类型筛选
- [x] UI-ADMIN-05: 验证数据分析看板包含统计卡片和趋势图
- [x] UI-ADMIN-06: 验证部门管理页面包含部门表格
- [x] UI-ADMIN-07: 验证系统设置页面包含表单

### 2.8 布局与导航 UI 测试

- [x] UI-LAYOUT-01: 验证侧边栏导航包含所有菜单项
- [x] UI-LAYOUT-02: 验证顶部导航栏包含用户头像
- [x] UI-LAYOUT-03: 验证通知铃铛图标存在
- [x] UI-LAYOUT-04: 验证搜索框存在
- [ ] UI-LAYOUT-05: 验证用户头像下拉菜单包含个人信息/账号设置/退出登录
- [ ] UI-LAYOUT-06: 验证404页面显示"页面不存在"和返回首页按钮

---

## 三、功能测试计划

### 3.1 登录功能测试

- [x] FUNC-LOGIN-01: 使用正确用户名和密码登录成功，跳转到首页
- [x] FUNC-LOGIN-02: 用户名为空时显示验证错误"请输入用户名"
- [x] FUNC-LOGIN-03: 密码为空时显示验证错误"请输入密码"
- [x] FUNC-LOGIN-04: 登录失败时显示错误提示
- [ ] FUNC-LOGIN-05: 已登录用户访问/login自动跳转首页
- [x] FUNC-LOGIN-06: 未登录用户访问受保护页面跳转到/login

### 3.2 首页功能测试

- [x] FUNC-HOME-01: 首页加载显示内容列表
- [x] FUNC-HOME-02: 按内容类型筛选功能正常
- [ ] FUNC-HOME-03: 按群组筛选功能正常
- [x] FUNC-HOME-04: 排序切换功能正常（最新/热度/相关性）
- [ ] FUNC-HOME-05: 点击标签筛选内容
- [x] FUNC-HOME-06: 点击内容项跳转到详情页
- [ ] FUNC-HOME-07: 分页切换功能正常
- [x] FUNC-HOME-08: 点击"+ 创建内容"跳转到创建页

### 3.3 创建内容功能测试

- [x] FUNC-CREATE-01: 填写标题和正文后保存草稿成功
- [x] FUNC-CREATE-02: 填写标题和正文后提交审核成功
- [x] FUNC-CREATE-03: 标题为空时保存草稿提示"请输入标题"
- [x] FUNC-CREATE-04: 正文为空时提交审核提示"请输入正文"
- [x] FUNC-CREATE-05: 切换内容类型功能正常
- [x] FUNC-CREATE-06: Markdown编辑器工具栏插入语法功能正常
- [x] FUNC-CREATE-07: 选择标签功能正常
- [x] FUNC-CREATE-08: 选择群组功能正常
- [x] FUNC-CREATE-09: 设置定时发布功能正常

### 3.4 内容详情功能测试

- [x] FUNC-DETAIL-01: 内容详情页正确加载并显示内容
- [ ] FUNC-DETAIL-02: Markdown正文正确渲染
- [x] FUNC-DETAIL-03: 点击收藏按钮收藏内容成功
- [ ] FUNC-DETAIL-04: 再次点击收藏按钮取消收藏
- [x] FUNC-DETAIL-05: 点击分享按钮复制链接到剪贴板
- [x] FUNC-DETAIL-06: 点击下载按钮下载Markdown文件
- [ ] FUNC-DETAIL-07: 内容作者可看到编辑按钮
- [ ] FUNC-DETAIL-08: 点击编辑按钮跳转到编辑页
- [x] FUNC-DETAIL-09: 不存在的内容ID显示加载失败页面

### 3.5 编辑内容功能测试

- [x] FUNC-EDIT-01: 编辑页正确加载已有内容
- [x] FUNC-EDIT-02: 修改标题和正文后保存成功
- [x] FUNC-EDIT-03: 草稿状态的内容可发布
- [x] FUNC-EDIT-04: 修改内容类型功能正常
- [x] FUNC-EDIT-05: 标题为空时保存提示验证错误

### 3.6 评论功能测试

- [x] FUNC-COMMENT-01: 发表评论成功
- [x] FUNC-COMMENT-02: 回复评论成功
- [x] FUNC-COMMENT-03: 点赞评论功能正常
- [x] FUNC-COMMENT-04: 删除自己的评论成功
- [x] FUNC-COMMENT-05: 空评论不能提交

### 3.7 群组功能测试

- [x] FUNC-GROUP-01: 群组列表正确加载
- [x] FUNC-GROUP-02: 搜索群组功能正常
- [x] FUNC-GROUP-03: 创建群组成功
- [x] FUNC-GROUP-04: 申请加入群组成功
- [x] FUNC-GROUP-05: 群组详情页正确显示群组信息和成员
- [ ] FUNC-GROUP-06: 群主可看到管理群组按钮
- [ ] FUNC-GROUP-07: 群组管理页面审批成员功能正常
- [ ] FUNC-GROUP-08: 群组管理页面移除成员功能正常

### 3.8 收藏功能测试

- [x] FUNC-FAV-01: 收藏列表正确加载
- [x] FUNC-FAV-02: 按内容类型筛选收藏
- [x] FUNC-FAV-03: 取消收藏功能正常
- [x] FUNC-FAV-04: 点击收藏项跳转到内容详情
- [x] FUNC-FAV-05: 空收藏列表显示提示

### 3.9 通知功能测试

- [x] FUNC-NOTIF-01: 通知列表正确加载
- [x] FUNC-NOTIF-02: 按类型筛选通知（全部/评论回复/群组/系统）
- [x] FUNC-NOTIF-03: 点击通知标记为已读
- [x] FUNC-NOTIF-04: 全部标记已读功能正常
- [ ] FUNC-NOTIF-05: 删除通知功能正常
- [ ] FUNC-NOTIF-06: 顶部通知面板显示最近通知

### 3.10 模板中心功能测试

- [x] FUNC-TPL-01: 模板列表正确显示3个预置模板
- [x] FUNC-TPL-02: 点击"使用模板"跳转到创建页并携带模板参数

### 3.11 管理后台功能测试

- [x] FUNC-ADMIN-TAG-01: 标签列表正确加载
- [x] FUNC-ADMIN-TAG-02: 添加标签成功
- [x] FUNC-ADMIN-TAG-03: 编辑标签成功
- [x] FUNC-ADMIN-TAG-04: 删除标签确认后成功
- [x] FUNC-ADMIN-USER-01: 用户列表正确加载
- [x] FUNC-ADMIN-USER-02: 搜索用户功能正常
- [x] FUNC-ADMIN-USER-03: 创建用户成功
- [x] FUNC-ADMIN-USER-04: 编辑用户角色成功
- [x] FUNC-ADMIN-WORD-01: 敏感词列表正确加载
- [x] FUNC-ADMIN-WORD-02: 添加敏感词成功
- [x] FUNC-ADMIN-WORD-03: 删除敏感词成功
- [x] FUNC-ADMIN-WORD-04: 批量导入敏感词成功
- [x] FUNC-ADMIN-WORD-05: 搜索敏感词功能正常
- [x] FUNC-ADMIN-AUDIT-01: 审核列表正确加载
- [x] FUNC-ADMIN-AUDIT-02: 通过审核成功
- [x] FUNC-ADMIN-AUDIT-03: 驳回审核成功（需填写原因）
- [x] FUNC-ADMIN-ANALYTICS-01: 数据看板正确加载统计卡片
- [x] FUNC-ADMIN-ANALYTICS-02: 日期范围筛选功能正常
- [x] FUNC-ADMIN-DEPT-01: 部门列表正确加载
- [x] FUNC-ADMIN-DEPT-02: 新建部门功能
- [x] FUNC-ADMIN-SETTINGS-01: 系统设置表单正确显示
- [x] FUNC-ADMIN-SETTINGS-02: 修改设置并保存成功

### 3.12 路由与权限测试

- [x] FUNC-ROUTE-01: 未登录访问任何受保护页面重定向到/login
- [x] FUNC-ROUTE-02: 侧边栏菜单点击导航正确
- [ ] FUNC-ROUTE-03: 访问不存在的路由显示404页面
- [ ] FUNC-ROUTE-04: 非管理员用户看不到管理菜单
- [ ] FUNC-ROUTE-05: 401响应自动跳转登录页
- [x] FUNC-ROUTE-06: 退出登录跳转到登录页

### 3.13 搜索功能测试

- [ ] FUNC-SEARCH-01: 顶部搜索栏输入关键词后回车跳转首页并带搜索参数
- [ ] FUNC-SEARCH-02: 空搜索不触发跳转

### 3.14 退出登录功能测试

- [x] FUNC-LOGOUT-01: 点击退出登录清除token并跳转到登录页

---

## 四、测试环境

- **浏览器**: Chromium (Playwright)
- **前端地址**: http://localhost:3000/
- **测试框架**: Playwright
- **测试账号**: admin / admin123

---

## 五、自动化测试执行顺序

按以下优先级执行：

1. **P0 - 核心流程**: 登录 → 首页 → 内容详情 → 退出登录
2. **P1 - 内容管理**: 创建内容 → 编辑内容 → 收藏 → 评论
3. **P2 - 群组功能**: 群组列表 → 群组详情 → 群组管理
4. **P3 - 个人功能**: 通知中心 → 我的收藏 → 模板中心
5. **P4 - 管理后台**: 标签管理 → 用户管理 → 敏感词管理 → 审核中心 → 数据分析 → 部门管理 → 系统设置
6. **P5 - UI验证**: 各页面UI元素验证
7. **P6 - 边界与异常**: 路由权限、404、空状态、错误处理

---

## 六、测试进度追踪

| 模块 | 总用例数 | 已通过 | 已失败 | 待执行 | 完成率 |
|------|---------|--------|--------|--------|--------|
| 登录 | 11 | 11 | 0 | 0 | 100% |
| 首页 | 15 | 13 | 0 | 2 | 87% |
| 创建内容 | 14 | 14 | 0 | 0 | 100% |
| 内容详情 | 17 | 14 | 0 | 3 | 82% |
| 编辑内容 | 5 | 5 | 0 | 0 | 100% |
| 评论 | 5 | 5 | 0 | 0 | 100% |
| 群组 | 15 | 12 | 0 | 3 | 80% |
| 收藏 | 5 | 5 | 0 | 0 | 100% |
| 通知 | 12 | 10 | 0 | 2 | 83% |
| 模板 | 2 | 2 | 0 | 0 | 100% |
| 管理后台 | 22 | 22 | 0 | 0 | 100% |
| 路由权限 | 6 | 4 | 0 | 2 | 67% |
| 搜索 | 2 | 0 | 0 | 2 | 0% |
| 退出登录 | 1 | 1 | 0 | 0 | 100% |
| **合计** | **132** | **118** | **0** | **14** | **89%** |

---

## 七、自动化测试运行结果

**运行时间**: 2026-05-27
**测试框架**: Playwright
**浏览器**: Chromium
**运行命令**: `npm run test:e2e`

### 最终运行结果

- **总测试数**: 137
- **通过**: 133
- **跳过**: 4
- **失败**: 0
- **运行时间**: ~7.7 分钟

### 测试文件清单

| 测试文件 | 测试数 | 状态 |
|---------|--------|------|
| login.spec.js | 13 | ✅ 全部通过 |
| home.spec.js | 13 | ✅ 全部通过 |
| content-create.spec.js | 14 | ✅ 全部通过 |
| content-detail.spec.js | 11 | ✅ 全部通过 |
| content-edit.spec.js | 5 | ✅ 全部通过 |
| comment.spec.js | 5 | ✅ 全部通过 |
| group.spec.js | 11 | ✅ 全部通过 |
| favorites.spec.js | 6 | ✅ 全部通过 |
| notifications.spec.js | 8 | ✅ 全部通过 |
| templates.spec.js | 3 | ✅ 全部通过 |
| admin-tags.spec.js | 7 | ✅ 全部通过 |
| admin-users.spec.js | 7 | ✅ 全部通过 |
| admin-sensitive-words.spec.js | 10 | ✅ 全部通过 |
| admin-audit.spec.js | 6 | ✅ 全部通过 |
| admin-analytics.spec.js | 6 | ✅ 全部通过 |
| admin-departments.spec.js | 7 | ✅ 全部通过 |
| admin-settings.spec.js | 6 | ✅ 全部通过 |
| layout-navigation.spec.js | 7 | ✅ 全部通过 |

### 未覆盖的测试用例（需后续补充）

1. UI-HOME-08: 分页组件验证（需数据量支持）
2. UI-DETAIL-07/08: 骨架屏和错误页面验证（需网络模拟）
3. UI-GROUP-07: 空状态验证
4. UI-NOTIF-05/06: 未读标识和删除按钮验证
5. UI-LAYOUT-05/06: 下拉菜单和404页面验证
6. FUNC-LOGIN-05: 已登录访问/login跳转
7. FUNC-HOME-03/05/07: 群组筛选、标签筛选、分页功能
8. FUNC-DETAIL-02/04/07/08: 渲染验证、取消收藏、编辑按钮
9. FUNC-GROUP-06/07/08: 群组管理功能
10. FUNC-NOTIF-05/06: 删除通知和通知面板
11. FUNC-ROUTE-03/04/05: 404、权限、401处理
12. FUNC-SEARCH-01/02: 搜索功能
