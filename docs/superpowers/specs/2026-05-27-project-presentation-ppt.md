# Knowledge Share 项目演示 PPT 设计文档

**Date:** 2026-05-27 | **Type:** 对外技术分享 | **时长:** 30 分钟 | **页数:** 25 页

## 目标

对外技术分享：展示 AI 驱动的全栈开发实践。核心卖点 — 使用 Claude Code + Superpowers Skills 体系完成从需求到上线的完整开发流程。

## PPT 结构（4 部分，25 页）

### 第一部分：开场（3 页）

**P1 — 封面**
- 标题：AI 驱动的全栈开发实践
- 副标题：Knowledge Share 知识共享平台 — 从需求到上线的 AI 协作之旅
- 演讲者、日期

**P2 — 项目速览**
- 一句话：企业级知识管理平台，支持内容创作、协作、搜索、治理
- 关键数字：69 个 API | 20 张数据库表 | 15 个前端页面 | 9 个后端模块 | 56 次提交

**P3 — 议程**
- ① AI 如何参与全流程开发（核心）
- ② 架构设计亮点
- ③ 性能与安全实战
- ④ 总结与展望

### 第二部分：AI 参与度（10 页）

**P4 — AI 协作全景图**
- 流程图：需求分析 → 头脑风暴 → 设计文档 → 实现计划 → 编码 → 测试 → 审查 → 安全审查 → 上线
- 每个节点标注使用的 Skill 名称

**P5 — Skills 体系介绍**
- 表格展示 Superpowers 技能矩阵：
  - 流程管控：brainstorming、writing-plans、executing-plans、subagent-driven-development
  - 质量保障：code-review、security-review、verification-before-completion
  - 开发方法：test-driven-development、systematic-debugging
  - 辅助工具：wireframe-prototyping、frontend-design、gstack
- 说明 Skills 如何覆盖软件工程全生命周期

**P6 — 头脑风暴驱动设计**
- 流程：用户想法 → brainstorming skill → 澄清问题 → Visual Companion 浏览器协作 → 设计文档
- 实例：首页 Dashboard 统计大盘
  - 用户："我觉得首页太单调，想加一个资源大盘"
  - AI：探索项目→提问（位置？风格？数据？）→浏览器 Mockup→用户选择→设计文档
  - 展示方案 A/B/C 的对比截图

**P7 — 计划驱动开发**
- writing-plans skill 的输出结构
  - 每步一个操作（2-5 分钟）
  - 精确文件路径 + 完整代码
  - 预期命令和输出
  - 无占位符，零歧义
- 实际计划文档截图

**P8 — 子代理并行开发**
- 流程图：Controller → 提取任务 → 3 个 Subagent 并行
  - Subagent 1: Task 1 实现
  - Subagent 2: Task 2 实现  
  - Subagent 3: Task 3 实现
- 每个 Subagent: implement → self-review → spec reviewer → code quality reviewer → fix → merge
- 实例：Todo 待办横条功能，3 个任务并行完成

**P9 — AI 驱动的单元测试**
- 测试覆盖总览表格：5 个模块 | 22 个测试类
  | 模块 | 测试数 | 覆盖范围 |
  |------|--------|---------|
  | knowledge-common | 1 | Result 统一响应 |
  | knowledge-user-auth | 6 | AuthController、AuthService、JWT |
  | knowledge-content | 7 | ContentService、TagService、AhoCorasick |
  | knowledge-social | 6 | GroupService、CommentService、FavoriteService |
  | knowledge-search | 2 | SearchService、混合搜索 |
- TDD skill 工作流：先写失败测试→验证失败→实现代码→验证通过
- Code Review 发现并自动修复编译错误（AuthServiceTest、GroupServiceTest）

**P10 — 自动化审查流程**
- 代码审查 pipeline 流程图（5 角度并行→验证→排序输出）
- code-review skill 实际成果：发现 15 个缺陷
- security-review skill 实际成果：发现 9 个漏洞（3 CRITICAL + 4 HIGH + 2 MEDIUM）
- 修复→重审→通过 循环机制

**P11 — 数据说话**
- 统计面板：
  - 56 次 Git 提交
  - 10+ 种 Skills 被使用
  - 30+ 次 Subagent 调度
  - 22 个测试类 / 5 个模块
  - 15 个代码缺陷被发现
  - 9 个安全漏洞被发现并修复
  - 7 次设计文档 + 实现计划

### 第三部分：技术特点（10 页）

**P12 — 架构总览**
- Mermaid 架构图：前端→安全网关→控制器→服务→基础设施
- 9 模块 DDD 分层架构
- 技术栈表格

**P13 — 混合搜索**
- ES BM25 + Qdrant 向量 + RRF 融合流程图
- 中文 IK 分词 + 768 维语义向量
- 实际搜索效果展示

**P14 — 内容生命周期与通知系统**
- 内容状态机：DRAFT → PUBLISHED → 审核
- RabbitMQ 异步事件：发布→消费→持久化→前端轮询

**P15 — 数据库设计**
- 20 张表 ER 图
- 12 次 Flyway 渐进式迁移
- 从建表→索引优化→安全加固的演进过程

**P16 — 性能优化实战**
- 索引策略四步走：
  1. 分析查询模式（EXPLAIN）
  2. 删除无用索引（idx_is_deleted）
  3. 纯索引 COUNT 扫描（避免 45 万次回表）
  4. 复合索引覆盖高频查询
- 优化前后对比数据

**P17 — SM4 国密加密方案**
- 问题：硬编码密钥 → git 泄露风险
- 方案演进：明文 → 环境变量 → SM4 加密 + EnvironmentPostProcessor 自动解密
- 流程图：启动→读取 SM4_KEY→扫描 SM4(...)→解密→注入 Environment
- 效果：服务器只需 1 个密钥，所有敏感值安全存储

**P18 — 安全纵深防御**
- 三层防御模型：
  - L1: URL 模式匹配（SecurityConfig）
  - L2: 数据所有权校验（Service 层）
  - L3: 参数校验（@Valid DTOs）
- 9 项已修复漏洞展示

**P19 — 前端技术亮点**
- Vue 3 Composition API + Pinia + Element Plus
- DOMPurify 防止 Markdown XSS
- 客户端缓存策略（群组列表 3min、标签 5min）
- CLS 防抖（占位骨架）
- IME 中文输入兼容

**P20 — 首页功能展示**
- 首页最终效果截图/渲染图
- 三大区域：统计卡片条 + 待办横条 + 内容流
- 渐变卡片设计 + 响应式布局

### 第四部分：总结与展望（5 页）

**P21 — 开发全流程回顾**
- 时间线图：从空仓库 → 模块搭建 → 认证系统 → 内容管理 → 社交功能 → 搜索 → 前端页面 → 统计仪表盘 → 待办功能 → 性能优化 → 安全加固
- 标注每个阶段的 AI 参与方式

**P22 — AI 协作最佳实践**
- 什么时候用 AI：需求澄清、方案设计、代码生成、测试编写、代码审查
- 什么时候不用 AI：架构决策（需人工审核）、业务规则变更、生产环境调试
- Skill 选择策略：简单任务直接执行 / 复杂任务 brainstorming→writing-plans→subagent
- 关键教训：设计文档是 AI 协作的"契约"、每个 Skill 都有明确的触发条件

**P23 — 未来路线图**
- 短期：httpOnly Cookie、刷新令牌、ES/Qdrant 认证
- 中期：K8s 部署、CI/CD Pipeline、Grafana 监控
- 长期：BGE 中文嵌入模型替换 StubEmbedding、Federated Search

**P24 — 核心经验**
- 3 条关键 Takeaway：
  1. "AI 不是替代开发者，而是放大开发者能力"
  2. "结构化流程（Skills）比自由对话更高效"
  3. "设计文档是 AI 协作的质量底线"

**P25 — 谢幕**
- Q&A
- 项目地址：github.com/JiongJiongP/knowledge-share
- 联系方式

## 技术要求

- 输出格式：HTML 演示文稿（Reveal.js），可在浏览器中直接演示
- 支持 Mermaid 图表渲染
- 支持代码高亮
- 支持键盘翻页导航
