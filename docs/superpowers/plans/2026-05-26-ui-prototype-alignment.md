# UI 原型对齐修复计划

> **日期**: 2026-05-26
> **状态**: 待确认
> **目标**: 将前端 UI 完全对齐 `ui/index.html` 原型设计，采用左侧深色侧边栏 + 顶部 Header 的后台管理布局

---

## 决策记录

| 决策项 | 选择 |
|--------|------|
| 布局策略 | ✅ 完全按原型重构（侧边栏布局） |
| 修复范围 | ✅ 分批修复，先核心流程 |
| 交付方式 | ✅ 先出计划文档，确认后实施 |

---

## 总体架构变更

### 变更前（当前）

```
┌──────────────────────────────────────────────────────┐
│ AppHeader: [Logo] [首页 群组 收藏 通知 管理后台] [搜索] [用户] [退出] │
├──────────────────────────────────────────────────────┤
│                                                      │
│                   Content Area                       │
│              （各页面独立，无侧边栏）                    │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### 变更后（目标）

```
┌────────────┬──────────────────────────────────────────┐
│            │  Header: [🔍 圆角搜索框........] [🔔●] [李▼] │
│  Sidebar   ├──────────────────────────────────────────┤
│ (深色渐变) │                                          │
│            │           Content Area                   │
│ 📚知识平台  │                                          │
│            │                                          │
│ ─内容─     │                                          │
│ 🏠 首页    │                                          │
│ ✏️ 创建    │                                          │
│ 📋 模板    │                                          │
│ ─群组─     │                                          │
│ 👥 群组    │                                          │
│ ─个人─     │                                          │
│ ⭐ 收藏    │                                          │
│ 🔔 通知(5) │                                          │
│ ─管理─     │                                          │
│ ✅ 审核(3) │                                          │
│ 🏷️ 标签    │                                          │
│ 👤 用户    │                                          │
│ 🚫 敏感词  │                                          │
│ 📊 数据    │                                          │
│ ⚙️ 设置    │                                          │
└────────────┴──────────────────────────────────────────┘
```

### 新增/修改文件清单

| 操作 | 文件路径 | 说明 |
|------|----------|------|
| **新建** | `frontend/src/components/layout/AppSidebar.vue` | 全局深色侧边栏导航组件 |
| **新建** | `frontend/src/components/layout/MainLayout.vue` | 主布局容器（Sidebar + Header + content slot） |
| **重写** | `frontend/src/components/layout/AppHeader.vue` | 改为顶部搜索栏 + 通知铃铈 + 用户下拉 |
| **重写** | `frontend/src/views/HomePage.vue` | 对齐原型列表样式 |
| **重写** | `frontend/src/views/ContentDetail.vue` | 在 MainLayout 内渲染 |
| **重写** | `frontend/src/views/ContentCreate.vue` | 补充群组选择功能 |
| **修改** | `frontend/src/router/index.js` | 路由嵌套到 MainLayout 下 |
| **修改** | `frontend/src/App.vue` | 使用 MainLayout 包裹 router-view |
| **重写** | `frontend/src/components/content/ContentCard.vue` | 对齐原型列表项样式 |
| **删除/废弃** | `frontend/src/components/layout/AdminLayout.vue` | 不再需要，所有页面统一用 MainLayout |

---

## Phase 1: 全局布局重构（基础，所有页面依赖）

### Step 1.1: 创建 AppSidebar.vue — 全局深色侧边栏

**目标**: 复刻 `ui/index.html` 中的 `<aside class="sidebar">` 组件

**文件**: `frontend/src/components/layout/AppSidebar.vue`（新建）

**具体内容**:
- 固定宽度 `220px`，背景渐变 `linear-gradient(180deg, #1a1a2e 0%, #16213e 100%)`
- 顶部 Logo 区域：📚 图标 + "知享" 文字
- 导航分组：
  - **内容**: 🏠 首页(`/`)、✏️ 创建内容(`/content/create`)、📋 模板中心(`/templates`)
  - **群组**: 👥 群组列表(`/groups`)
  - **个人**: ⭐ 我的收藏(`/favorites`)、🔔 通知中心 `/notifications`（带红点 badge）
  - **管理**: ✅ 审核中心(`/admin/audit`)（badge）、🏷️ 标签管理(`/admin/tags`)、👤 用户管理(`/admin/users`)、🚫 敏感词管理(`/admin/sensitive-words`)、📊 数据分析(`/admin/analytics`)
- 当前激活项高亮：左边框 `3px solid #409EFF` + 浅色背景
- hover 效果：文字变白 + 浅色背景
- badge 样式：红色圆角胶囊，显示未读数

**验收标准**:
- [ ] 侧边栏固定在左侧，不随页面滚动
- [ ] 点击导航项正确跳转路由
- [ ] 当前路由对应的导航项高亮显示
- [ ] badge 数字可从 store 动态获取

### Step 1.2: 重写 AppHeader.vue — 顶部搜索栏

**目标**: 复刻原型 Header，去掉现有导航链接，改为搜索+通知+用户操作区

**文件**: `frontend/src/components/layout/AppHeader.vue`（重写）

**具体变更**:
- **移除**: Logo、导航链接（首页/群组/收藏/通知/管理后台）、用户名文字、退出按钮
- **保留/新增**:
  - 居中的圆角搜索框：`border-radius: 18px`，占位符 "搜索知识内容...（全文检索 + 语义搜索 + 标签筛选）"
  - 搜索图标按钮（🔍）在输入框右侧
  - 🔔 通知铃铛按钮（右上），带红点 badge（未读数 > 0 时显示）
  - 点击铃铛 → 弹出通知下拉面板（4 条最近通知 + "查看全部"链接）
  - 用户头像圆形按钮（显示用户名首字）
  - 点击头像 → 下拉菜单（👤 个人信息 / ⚙️ 账号设置 / 🚪 退出登录）

**验收标准**:
- [ ] 搜索框居中，圆角胶囊样式
- [ ] 通知铃铛带红点 badge
- [ ] 点击铃铛弹出下拉面板，显示最近通知
- [ ] 点击头像弹出下拉菜单
- [ ] 退出登录功能正常

### Step 1.3: 创建 MainLayout.vue — 主布局容器

**目标**: 组合 Sidebar + Header + 内容区的全局布局

**文件**: `frontend/src/components/layout/MainLayout.vue`（新建）

**结构**:
```vue
<template>
  <div class="main-layout">
    <AppSidebar />
    <div class="main-area">
      <AppHeader />
      <main class="content"><slot /></main>
    </div>
  </div>
</template>
```

**CSS 要点**:
- `.main-layout`: `display: flex; min-height: 100vh;`
- `.main-area`: `margin-left: 220px; flex: 1; display: flex; flex-direction: column;`
- `.content`: `padding: 24px; flex: 1; background: #F5F7FA;`

**验收标准**:
- [ ] 左侧侧边栏固定 220px 宽
- [ ] 右侧内容区自适应剩余宽度
- [ ] Header 固定在内容区顶部（sticky）
- [ ] 内容区有正确的 padding 和背景色

### Step 1.4: 修改路由和 App.vue — 接入新布局

**文件**: 
- `frontend/src/App.vue`（修改）
- `frontend/src/router/index.js`（修改）

**App.vue 变更**:
- 用 `<MainLayout><router-view /></MainLayout>` 包裹 `<router-view>`
- 移除旧的直接 `<router-view>`

**路由变更**:
- 所有需要侧边栏的页面路由保持不变（它们会自动被 MainLayout 包裹）
- Login 页面不需要侧边栏，保持独立（在 meta 中标记 `guest: true`，App.vue 中判断是否包裹 MainLayout）

**验收标准**:
- [ ] 登录页面无侧边栏
- [ ] 登录后的所有页面都有侧边栏 + 顶栏
- [ ] 页面切换时侧边栏不变（只有内容区变化）

---

## Phase 2: 核心页面修复（首页 + 详情 + 创建）

### Step 2.1: 重写 HomePage.vue — 对齐原型首页

**文件**: `frontend/src/views/HomePage.vue`（重写）

**与原型的差异修复**:

| 原型特征 | 修复动作 |
|---------|---------|
| 在 MainLayout 内容区内（无独立 sidebar） | 移除 HomePage 自带的左侧筛选 sidebar |
| 筛选栏：类型 select + 群组 select + 排序 select + 标签 chips 横排 | 保留筛选栏，改为横排在 card 顶部 |
| 内容列表项：大标题 + 2行摘要 + meta 行（类型徽章emoji/作者/日期/浏览/收藏/评论/标签） | 重写 ContentCard 或内联列表项 |
| 类型徽章：彩色背景 + emoji（📝 Markdown 蓝底 / 📊 PPT 橙底 / 🔗 绿底 / 📎 紫底） | 替换 el-tag 为自定义 type-badge |
| 分页器为小圆角按钮风格 | 可保留 el-pagination 或自定义 |

**ContentCard 组件同步修改** (`frontend/src/components/content/ContentCard.vue`):
- 从 `el-card` 改为轻量级 div 列表项样式
- 标题不再截断（或截断方式调整）
- 添加摘要描述区域（excerpt）
- meta 行完整展示：type-badge + 作者 + 日期 + 👁️浏览 + ⭐收藏 + 💬评论 + 标签tags

**验收标准**:
- [ ] 首页在侧边栏布局中正确渲染
- [ ] 筛选栏（类型/群组/排序/标签）正常工作
- [ ] 内容列表项样式与原型一致（标题/摘要/meta行/类型徽章）
- [ ] 标签 chip 点击切换选中态
- [ ] 分页正常工作

### Step 2.2: 重写 ContentDetail.vue — 对齐原型详情页

**文件**: `frontend/src/views/ContentDetail.vue`（重写）

**与原型的差异修复**:

| 原型特征 | 当前状态 | 修复动作 |
|---------|---------|---------|
| 在 MainLayout 内容区内 | ✅ 已是独立页面 | 无需大改，只需适配新布局容器 |
| 标题 24px | 28px | 改为 24px |
| meta 行：类型徽章 + 标签 + 作者·日期·阅读数 | 有但样式不同 | 调整间距和字号 |
| 操作按钮：默认 btn-default 样式 | 混用了 primary | 统一为 default 样式 |
| Markdown 正文渲染 | ✅ 已实现且更好 | 保持不变 |
| 评论区域在下方独立 card | ✅ CommentSection | 保持不变 |

**验收标准**:
- [ ] 详情页在新布局中正确渲染
- [ ] 标题/元信息/操作按钮/正文/评论 区域层次清晰
- [ ] 收藏/分享/下载/编辑功能正常
- [ ] Markdown 渲染正常（代码块/表格/引用等）

### Step 2.3: 重写 ContentCreate.vue — 补充缺失功能

**文件**: `frontend/src/views/ContentCreate.vue`（重写）

**关键修复 — 补充「发布到群组」功能**:

原型中有：
```html
<div class="form-group">
  <label>发布到群组</label>
  <label><input type="checkbox" checked> 前端技术群</label>
  <label><input type="checkbox> 后端架构群</label>
  <label><input type="checkbox> 产品设计群</label>
</div>
```

需要在表单中添加：
1. 调用 `getGroupList({ size: 100 })` 获取可用群组列表
2. 渲染为 checkbox 组（el-checkbox-group），每个选项显示群组名称
3. 选中的群组 ID 列表存入 `form.groupIds`
4. 提交时将 groupIds 传给 API

**其他微调**:
- 编辑器 textarea 高度从 rows=15 增大到 min-height: 300px
- 确保 TagSelector 正常工作

**验收标准**:
- [ ] 内容类型选择器正常（4 种类型按钮切换）
- [ ] 标题输入正常
- [ ] Markdown 编辑器工具栏 + 文本域正常
- [ ] **TagSelector 标签选择正常**
- [ ] **「发布到群组」checkbox 多选正常**
- [ ] 定时发布可选填
- [ ] 保存草稿 / 提交审核 功能正常

---

## Phase 3: 二级页面修复（群组/模板/收藏/通知/编辑）

### Step 3.1: 修复 GroupList.vue — 群组列表

**文件**: `frontend/src/views/GroupList.vue`（修改）

**修复项**:
1. **群主显示名字而非 ID**：调用用户 API 获取 ownerId 对应的 displayName
2. **卡片样式对齐原型**：2列网格、emoji 前缀名、meta 信息行、描述文字
3. **按钮文案**：「申请加入」/「已加入」状态正确
4. **搜索框**：原型中在 filter-bar 内

**验收标准**:
- [ ] 群组卡片以 2 列网格展示
- [ ] 显示群主姓名（非 ID）
- [ ] 成员数/内容数正确显示
- [ ] 加入/已加入状态切换正常

### Step 3.2: 修复 TemplatesPage.vue — 模板中心

**文件**: `frontend/src/views/TemplatesPage.vue`（修改）

**修复项**:
1. **对接后端 API**：移除硬编码数据，调用模板列表 API
2. **添加「+ 创建模板」按钮**（管理员可见）
3. **卡片样式**确认与原型一致（3 列网格）

**验收标准**:
- [ ] 模板列表从 API 获取
- [ ] 3 列卡片网格布局
- [ ] 「使用模板」按钮跳转到创建页并携带模板参数

### Step 3.3: 修复 FavoritesPage.vue — 收藏页

**文件**: `frontend/src/views/FavoritesPage.vue`（修改）

**修复项**:
1. **列表项样式**改为接近原型的 content-list-item 风格（标题 + meta 行 + 取消按钮）
2. **补充作者信息**显示
3. **类型徽章**使用彩色背景+emoji 风格

**验收标准**:
- [ ] 收藏列表展示内容标题、类型徽章、作者、收藏时间
- [ ] 取消收藏功能正常
- [ ] 类型筛选下拉正常

### Step 3.4: 修复 NotificationsPage.vue — 通知中心

**文件**: `frontend/src/views/NotificationsPage.vue`（微调）

**当前状态**: 已经比较接近原型

**微调项**:
1. 确认 Tab 样式与原型一致（底部蓝色下划线）
2. 确认通知项的 icon 圆形背景色与原型一致
3. 删除按钮样式对齐（✕ 文字按钮）

**验收标准**:
- [ ] 4 个 Tab 切换正常
- [ ] 通知列表展示正确（图标/标题/描述/时间/删除）
- [ ] 未读/已读样式区分明显
- [ ] 全部已读功能正常

### Step 3.5: 修复 ContentEdit.vue — 内容编辑

**文件**: `frontend/src/views/ContentEdit.vue`（修改）

**修复项**:
1. 复用 ContentCreate 的表单结构
2. 加载时预填充已有数据（标题/正文/类型/标签/群组）
3. 同样补充「发布到群组」功能

**验收标准**:
- [ ] 编辑页正确加载已有内容数据
- [ ] 表单各字段可编辑
- [ ] 保存/提交功能正常

---

## Phase 4: 管理后台 + 收尾

### Step 4.1: 废弃 AdminLayout，统一使用 MainLayout

**操作**: 
- 删除或标记 `AdminLayout.vue` 为废弃
- 所有 `/admin/*` 路由的组件直接在 MainLayout 的 slot 中渲染
- AppSidebar 中已有的管理菜单项会自动高亮对应路由

### Step 4.2: 管理页面逐一对齐

各管理页面的原型对齐（优先级较低，因为原型中这些页面细节较少）：

| 页面 | 主要修复项 |
|------|-----------|
| TagManage | 确认表格/表单样式 |
| UserManage | 确认用户表格 + 角色编辑 |
| SensitiveWordManage | 确认敏感词 CRUD |
| AnalyticsDashboard | 确认统计卡片 + 图表区域 |
| AuditCenter | 确认审核列表 + 操作按钮 |
| DepartmentManage | 确认树形表格 |
| SystemSettings | 确认配置表单 |

### Step 4.3: 全局视觉一致性收尾

- [ ] 所有页面的 type-badge 组件统一提取为公共组件
- [ ] 所有 tag-chip 样式统一
- [ ] 按钮、表单、表格等 Element Plus 组件主题变量确认一致
- [ ] 响应式断点测试（1200px 以下）

---

## 实施顺序总览

```
Phase 1: 全局布局重构 ────────────────────── ✅ 基础
  Step 1.1: 创建 AppSidebar.vue
  Step 1.2: 重写 AppHeader.vue  
  Step 1.3: 创建 MainLayout.vue
  Step 1.4: 修改路由 + App.vue

Phase 2: 核心页面修复 ────────────────────── ✅ 主流程
  Step 2.1: 重写 HomePage + ContentCard
  Step 2.2: 重写 ContentDetail
  Step 2.3: 重写 ContentCreate（补群组选择）

Phase 3: 二级页面修复 ────────────────────── ✅ 完整性
  Step 3.1: 修复 GroupList
  Step 3.2: 修复 TemplatesPage
  Step 3.3: 修复 FavoritesPage
  Step 3.4: 微调 NotificationsPage
  Step 3.5: 修复 ContentEdit

Phase 4: 管理后台 + 收尾 ──────────────────── ✅ 完善
  Step 4.1: 废弃 AdminLayout
  Step 4.2: 管理页面逐一对齐
  Step 4.3: 全局视觉一致性收尾
```

## 风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 布局重构影响所有页面 | 高 | Phase 1 完成后立即验证所有页面能正常打开 |
| 路由嵌套变化导致路径问题 | 中 | 仔细检查所有 `router.push` / `router-link` 路径 |
| 后端 API 缺少字段（如群主名） | 中 | 前端做兼容处理，显示 fallback |
| Element Plus 组件样式覆盖复杂 | 低 | 尽量用 scoped style + :deep() 精确控制 |

---

*请审阅此计划，确认后我将开始按 Phase 1 → 2 → 3 → 4 的顺序逐步实施。*
