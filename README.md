# 知识分享平台 (Knowledge Share Platform)

企业内部知识分享、检索、评论平台，支持多种内容形式、全文检索与语义搜索。

## 技术栈

| 组件 | 方案 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Element Plus + Pinia + Axios | - |
| 后端 | Spring Boot + MyBatis-Plus + DDD | 3.2.0 |
| 数据库 | MySQL | 8.0 |
| 全文检索 | Elasticsearch + IK Analyzer | 8.11.0 |
| 向量检索 | Qdrant + BGE-base-zh | 1.9.1 |
| 缓存 | Redis | 6.0+ |
| 消息队列 | RabbitMQ | 3.x |
| 文件存储 | MinIO | - |
| 构建 | Maven + Vite | - |

## 快速开始

### 1. 启动基础设施服务

```bash
docker-compose up -d
```

启动 MySQL、Redis、Elasticsearch、Qdrant、RabbitMQ、MinIO 共 6 个服务。

### 2. 启动后端

```bash
cd knowledge-web
mvn spring-boot:run
```

首次启动时 Flyway 会自动执行数据库迁移并插入种子数据。后端运行在 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`。

### 4. 登录

默认测试账号：
- 管理员：`admin` / `admin123`
- 普通用户：`lisan` / `user123`

---

## 项目结构

```
knowledge-platform/
├── knowledge-common/          # 公共基础设施（BaseEntity、Result、异常处理）
├── knowledge-user-auth/       # 用户 + 部门 + 角色权限 + JWT 认证
├── knowledge-content/         # 内容发布 + 标签体系
├── knowledge-social/          # 评论 + 群组
├── knowledge-search/          # 全文检索 + 语义搜索 + RRF 混合检索
├── knowledge-notification/    # 通知（待实现）
├── knowledge-file/            # 文件服务（待实现）
├── knowledge-analytics/       # 数据分析（待实现）
├── knowledge-web/             # Spring Boot 启动入口
└── frontend/                  # Vue 3 前端 SPA
```

---

## 配置参数

### 🔴 必须修改（生产环境上线前）

#### 数据库

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/knowledge_platform` | `knowledge-web/.../application.yml` | 生产数据库地址 |
| `spring.datasource.username` | `root` | 同上 | 数据库用户名 |
| `spring.datasource.password` | `root123` | 同上 | 数据库密码 |
| `MYSQL_ROOT_PASSWORD` | `root123` | `docker-compose.yml` | Docker 数据库 root 密码 |

#### JWT 认证

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `jwt.secret` | `knowledge-platform-secret-key-min-256-bits!!` | `knowledge-web/.../application.yml` | **必须更换**为随机 256-bit 密钥 |
| `jwt.expiration` | `86400000` (24小时) | `JwtUtil.java` (@Value 默认值) | Token 过期时间(ms)，按需调整 |

#### CORS 跨域

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| CORS allowed origins | `http://localhost:3000` | `SecurityConfig.java` 第 51 行 | **硬编码**，生产环境需改为前端域名 |

#### Redis

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `spring.data.redis.host` | `localhost` | `knowledge-web/.../application.yml` | Redis 地址 |

#### RabbitMQ

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `spring.rabbitmq.host` | `localhost` | `knowledge-web/.../application.yml` | RabbitMQ 地址 |
| `spring.rabbitmq.username` | `admin` | 同上 | RabbitMQ 用户名 |
| `spring.rabbitmq.password` | `admin123` | 同上 | **必须更换** |
| `RABBITMQ_DEFAULT_USER` | `admin` | `docker-compose.yml` | Docker 默认用户 |
| `RABBITMQ_DEFAULT_PASS` | `admin123` | `docker-compose.yml` | **必须更换** |

#### Elasticsearch

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `spring.elasticsearch.uris` | `http://localhost:9200` | `knowledge-web/.../application.yml` | ES 集群地址 |
| `spring.elasticsearch.uris` | `http://localhost:9200` | `ElasticsearchConfig.java` (@Value 默认值) | Java 代码级回退值 |
| `elasticsearch.enabled` | `true` | `ElasticsearchConfig.java` (@ConditionalOnProperty) | 设为 `false` 可禁用 ES |

#### Qdrant（向量数据库）

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `qdrant.host` | `localhost` | `knowledge-web/.../application.yml` | Qdrant 地址 |
| `qdrant.port` | `6334` | 同上 | Qdrant gRPC 端口 |
| `qdrant.host` | `localhost` | `QdrantConfig.java` (@Value 默认值) | Java 代码级回退值 |
| `qdrant.enabled` | `true` | `QdrantConfig.java` (@ConditionalOnProperty) | 设为 `false` 可禁用 Qdrant |

#### MinIO（文件存储）

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `minio.endpoint` | `http://localhost:9000` | `knowledge-web/.../application.yml` | MinIO 地址 |
| `minio.access-key` | `admin` | 同上 | **必须更换** |
| `minio.secret-key` | `admin123456` | 同上 | **必须更换** |
| `minio.bucket` | `knowledge-files` | 同上 | 存储桶名称 |
| `MINIO_ROOT_USER` | `admin` | `docker-compose.yml` | Docker 默认用户 |
| `MINIO_ROOT_PASSWORD` | `admin123456` | `docker-compose.yml` | **必须更换** |

#### 种子数据账号

| 用户名 | 密码 | 角色 | 所在文件 |
|--------|------|------|----------|
| `admin` | `admin123` | 系统管理员 | `V1__user_auth.sql` |
| `lisan` | `user123` | 普通用户 | `V1__user_auth.sql` |

> ⚠️ 密码明文在 SQL 注释中暴露，生产环境需移除注释并更换哈希值。

---

### 🟡 建议检查（按需调整）

| 参数 | 默认值 | 所在文件 | 说明 |
|------|--------|----------|------|
| `server.port` | `8080` | `knowledge-web/.../application.yml` | 后端端口 |
| `mybatis-plus.configuration.log-impl` | `StdOutImpl` | 同上 | **生产环境建议**改为 `Slf4jImpl` 或移除，避免 SQL 泄露 |
| `spring.servlet.multipart.max-file-size` | `50MB` | 同上 | 文件上传大小限制 |
| `spring.servlet.multipart.max-request-size` | `100MB` | 同上 | 请求总大小限制 |
| `xpack.security.enabled` | `false` | `docker-compose.yml` | **生产环境应启用** ES 安全 |
| Docker 端口暴露 | 3306/6379/9200/5672/15672/9000/9001/6333/6334 | `docker-compose.yml` | 生产环境不应暴露数据库和中间件端口 |

---

### 🟢 可选项

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `elasticsearch.enabled` | `true` | 设为 `false` 完全禁用 ES 客户端 |
| `qdrant.enabled` | `true` | 设为 `false` 完全禁用 Qdrant 客户端 |

---

## Docker Compose 服务端口

| 服务 | 内部端口 | 映射端口 | 管理界面 |
|------|---------|---------|---------|
| MySQL | 3306 | 3306 | - |
| Redis | 6379 | 6379 | - |
| Elasticsearch | 9200 | 9200 | - |
| Qdrant | 6333/6334 | 6333/6334 | - |
| RabbitMQ | 5672 | 5672 | `http://localhost:15672` |
| MinIO | 9000 | 9000 | `http://localhost:9001` |

---

## API 端点概览

### 认证 (`/api/auth`)
- `POST /api/auth/login` — 账号密码登录
- `GET /api/auth/me` — 获取当前用户信息

### 内容 (`/api/contents`)
- `GET /api/contents` — 内容列表（支持筛选、搜索、排序）
- `GET /api/contents/{id}` — 内容详情
- `POST /api/contents` — 创建内容（草稿）
- `PUT /api/contents/{id}` — 更新内容
- `POST /api/contents/{id}/publish` — 发布内容
- `POST /api/contents/{id}/draft` — 保存草稿
- `DELETE /api/contents/{id}` — 软删除内容

### 标签 (`/api/tags`, `/api/admin/tags`)
- `GET /api/tags` — 获取所有标签
- `POST /api/admin/tags` — 创建标签 [管理员]
- `PUT /api/admin/tags/{id}` — 更新标签 [管理员]
- `DELETE /api/admin/tags/{id}` — 删除标签 [管理员]
- `GET /api/contents/{id}/tags` — 获取内容标签
- `PUT /api/contents/{id}/tags` — 设置内容标签 [作者]

### 群组 (`/api/groups`)
- `GET /api/groups` — 公开群组列表
- `GET /api/groups/{id}` — 群组详情
- `POST /api/groups` — 创建群组
- `POST /api/groups/{id}/join` — 申请加入
- `GET /api/groups/{id}/members` — 成员列表
- `GET /api/groups/{id}/members/pending` — 待审批列表 [群主]
- `PUT /api/groups/{id}/members/{userId}` — 审批成员 [群主]
- `DELETE /api/groups/{id}/members/{userId}` — 移除成员 [群主]

### 评论 (`/api/contents/{id}/comments`, `/api/comments`)
- `GET /api/contents/{id}/comments` — 评论列表
- `GET /api/comments/{id}/replies` — 回复列表
- `POST /api/contents/{id}/comments` — 发表评论
- `POST /api/comments/{id}/like` — 点赞
- `DELETE /api/comments/{id}/like` — 取消点赞
- `DELETE /api/comments/{id}` — 删除评论 [作者]

### 搜索 (`/api/search`)
- `GET /api/search?keyword=&page=&size=&sort=` — 全文检索
- `GET /api/search/vector?q=&topK=` — 向量语义搜索
- `GET /api/search/hybrid?keyword=&page=&size=` — RRF 混合检索

---

## 开发进度

| 子计划 | 进度 | 测试 |
|--------|------|------|
| A. 基础设施 + 内容核心 | 9/9 ✅ | 67 通过 |
| B. 搜索 + 互动体验 | 5/10 | 24 通过 |
| C. 工具链 + 管理后台 | 0/9 | - |
| **总计** | **14/28** | **91 通过** |
