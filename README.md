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

## 数据库表结构 (DDL)

> 以下 DDL 由 Flyway 在首次启动时自动执行（`knowledge-web/src/main/resources/db/migration/` 下的各模块迁移文件）。
> 也可手动执行以下完整 DDL 初始化数据库。

### 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS knowledge_platform
  DEFAULT CHARSET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
```

### V1 — 用户认证 (`user`, `department`, `role`, `permission`, `user_role`, `role_permission`)

```sql
CREATE TABLE IF NOT EXISTS `user` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(256) NOT NULL,
  display_name VARCHAR(64) NOT NULL,
  email VARCHAR(128),
  sso_id VARCHAR(128),
  department_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `department` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `role` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `permission` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_role` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `role_permission` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V2 — 知识内容 (`knowledge_content`, `content_group_relation`)

```sql
CREATE TABLE IF NOT EXISTS `knowledge_content` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(256) NOT NULL,
  body LONGTEXT,
  content_type VARCHAR(16) NOT NULL DEFAULT 'MARKDOWN',
  status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT NOT NULL,
  published_at DATETIME,
  is_deleted TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_created_by (created_by),
  INDEX idx_status_published (status, published_at),
  INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_group_relation` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_content_group (content_id, group_id),
  INDEX idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V3 — 群组 (`group_info`, `group_member`)

```sql
CREATE TABLE IF NOT EXISTS `group_info` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512),
  owner_id BIGINT NOT NULL,
  visibility VARCHAR(16) NOT NULL DEFAULT 'PUBLIC',
  status VARCHAR(16) NOT NULL DEFAULT 'APPROVED',
  is_deleted TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_owner (owner_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `group_member` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  joined_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_group_user (group_id, user_id),
  INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V4 — 标签 (`tag`, `content_tag_relation`)

```sql
CREATE TABLE IF NOT EXISTS `tag` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  color VARCHAR(7) NOT NULL DEFAULT '#409EFF',
  created_by BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_tag_relation` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_content_tag (content_id, tag_id),
  INDEX idx_content_id (content_id),
  INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V5 — 评论 (`comment`, `comment_like`, `comment_mention`)

```sql
CREATE TABLE IF NOT EXISTS `comment` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  parent_id BIGINT,
  reply_to_id BIGINT,
  reply_to_user_id BIGINT,
  user_id BIGINT NOT NULL,
  body TEXT NOT NULL,
  like_count INT DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED',
  audit_status VARCHAR(16) NOT NULL DEFAULT 'APPROVED',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_content_id (content_id),
  INDEX idx_parent_id (parent_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment_like` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comment_user (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment_mention` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  comment_id BIGINT NOT NULL,
  mentioned_user_id BIGINT NOT NULL,
  INDEX idx_comment_id (comment_id),
  INDEX idx_mentioned_user (mentioned_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V6 — 敏感词 (`sensitive_word`)

```sql
CREATE TABLE IF NOT EXISTS `sensitive_word` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  word VARCHAR(64) NOT NULL,
  category VARCHAR(32) NOT NULL DEFAULT 'GENERAL',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V7 — 收藏 (`favorite`)

```sql
CREATE TABLE IF NOT EXISTS `favorite` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  content_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_content (user_id, content_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V8 — 通知 (`notification`)

```sql
CREATE TABLE IF NOT EXISTS `notification` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  title VARCHAR(256) NOT NULL,
  content VARCHAR(512),
  related_id BIGINT,
  related_type VARCHAR(32),
  is_read TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_read (user_id, is_read),
  INDEX idx_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V9 — 内容工具链 (`content_version`, `audit_record`, `content_template`, `scheduled_publish`)

```sql
CREATE TABLE IF NOT EXISTS `content_version` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  version_number INT NOT NULL,
  title VARCHAR(256) NOT NULL,
  body LONGTEXT,
  change_summary VARCHAR(512),
  created_by BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_content_id (content_id),
  UNIQUE KEY uk_content_version (content_id, version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_record` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  submitter_id BIGINT NOT NULL,
  reviewer_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  reject_reason VARCHAR(512),
  submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  reviewed_at DATETIME,
  INDEX idx_status (status),
  INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_template` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512),
  content_type VARCHAR(16) NOT NULL DEFAULT 'MARKDOWN',
  body LONGTEXT,
  is_system TINYINT DEFAULT 0,
  created_by BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `scheduled_publish` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  scheduled_at DATETIME NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_scheduled (status, scheduled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### V10 — 数据分析 (`user_action_log`, `content_stats`, `search_hot_keyword`)

```sql
CREATE TABLE IF NOT EXISTS `user_action_log` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  action_type VARCHAR(32) NOT NULL,
  target_type VARCHAR(32),
  target_id BIGINT,
  extra_data JSON,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_created (created_at DESC),
  INDEX idx_action (action_type, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `content_stats` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  view_count BIGINT DEFAULT 0,
  favorite_count BIGINT DEFAULT 0,
  comment_count BIGINT DEFAULT 0,
  download_count BIGINT DEFAULT 0,
  stat_date DATE NOT NULL,
  UNIQUE KEY uk_content_date (content_id, stat_date),
  INDEX idx_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `search_hot_keyword` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  keyword VARCHAR(128) NOT NULL,
  search_count INT DEFAULT 1,
  stat_date DATE NOT NULL,
  UNIQUE KEY uk_keyword_date (keyword, stat_date),
  INDEX idx_count (stat_date, search_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 种子数据

```sql
-- 部门
INSERT INTO department (id, name, parent_id, sort_order) VALUES
  (1, '技术管理部', 0, 1),
  (2, '技术中心', 0, 2),
  (3, '前端技术组', 2, 1),
  (4, '基础架构组', 2, 2),
  (5, '产品中心', 0, 3);

-- 角色
INSERT INTO role (code, name) VALUES ('ADMIN', '系统管理员');
INSERT INTO role (code, name) VALUES ('USER', '普通用户');

-- 权限
INSERT INTO permission (code, name) VALUES
  ('content:view', '查看内容'),
  ('content:create', '创建内容'),
  ('content:edit', '编辑内容'),
  ('content:delete', '删除内容'),
  ('content:audit', '审核内容'),
  ('tag:manage', '管理标签'),
  ('user:manage', '管理用户'),
  ('group:approve', '审批群组'),
  ('analytics:view', '查看数据分析'),
  ('system:config', '系统配置'),
  ('sensitive:manage', '管理敏感词');

-- 管理员拥有所有权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'ADMIN'), id FROM permission;

-- 普通用户拥有基础权限
INSERT INTO role_permission (role_id, permission_id)
SELECT (SELECT id FROM role WHERE code = 'USER'), id FROM permission
WHERE code IN ('content:view', 'content:create', 'content:edit', 'content:delete');

-- 默认用户 (密码: admin123 / user123)
INSERT INTO user (username, password, display_name, department_id) VALUES
  ('admin', '$2a$10$KkrV5Vq1WZ.06JZGEar3Jec2SHzZ70tFyldyY1wZWh4cJZmzPW4cu', '管理员', 1),
  ('lisan', '$2a$10$/mibGlql.jsDNe5C39yVL.wy5eunnU/.gtk7LKwoYEuJdb1JwDxPW', '李三', 2);

-- 用户-角色关联
INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user u, role r
WHERE u.username = 'admin' AND r.code = 'ADMIN';

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM user u, role r
WHERE u.username = 'lisan' AND r.code = 'USER';
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
