# SM4 数据字段加密整改计划

> **版本:** 2026-05-28 | **状态:** 已实施

---

## 1. 背景与目标

项目对安全性要求高，需对数据库中存储的个人信息字段进行 SM4 加密存储，防止数据泄露。

### 敏感字段识别结果

经审查全部 12 个 Flyway 迁移脚本、25 张数据库表、26 个实体类，当前数据库中没有手机号、身份证号、住址等强敏感字段。

在 `user` 表中发现以下需加密的个人信息字段：

| 优先级 | 字段 | 类型 | 原因 | WHERE 条件匹配 |
|--------|------|------|------|---------------|
| 高 | `email` | VARCHAR(128) | 电子邮箱，个人信息 | 否 |
| 高 | `username` | VARCHAR(64) | 用户名，可能含个人信息 | **是** (登录查找、去重) |
| 中 | `display_name` | VARCHAR(64) | 显示名称，可能为真实姓名 | 否 |

### 已有基础设施

- `SM4Util` (SM4/CBC/PKCS7Padding) — 加解密工具类
- `EncryptedPropertyPostProcessor` — 配置文件 `SM4(...)` 占位符解密
- MyBatis-Plus 3.5.5，已有 `JacksonTypeHandler` 使用先例
- BCrypt 密码哈希

---

## 2. 实施方案

### 2.1 密钥配置

在 `application.yml` 中新增 Spring 配置项，统一管理数据加密密钥：

```yaml
sm4:
  data-key: SM4(xxx...)   # 生产用 SM4 密文，开发可用明文
```

新建 `Sm4Config` 类，通过 `@Value("${sm4.data-key}")` 注入。生产环境密文由已有的 `EncryptedPropertyPostProcessor` 在启动阶段解密。

**与现有密钥体系的区别：**
- `SM4_KEY` 环境变量 → 用于解密 application.yml 中的 `SM4(...)` 占位符（启动引导密钥）
- `sm4.data-key` → 用于加解密数据库字段数据（数据密钥）

两者可以相同也可以不同，推荐使用不同的密钥以实现密钥分级。

### 2.2 加密策略：确定性 vs 随机 IV

SM4/CBC 默认使用随机 IV，同一明文每次加密结果不同。需要分场景处理：

| 字段 | 加密模式 | IV 策略 | 原因 |
|------|---------|---------|------|
| `username` | 确定性加密 | IV = SHA-256(plaintext)[0:16] | UNIQUE 约束 + WHERE eq 查询 |
| `email` | 确定性加密 | IV = SHA-256(plaintext)[0:16] | 预留未来查询需求 |
| `displayName` | 随机 IV | SecureRandom 生成 | 仅展示，不做查询条件 |

确定性加密：对同一明文的 IV 始终相同 → 密文相同 → UNIQUE 索引和 eq 查询依然有效。

### 2.3 TypeHandler

在 `knowledge-common` 模块新建两个 TypeHandler：

**`SM4DeterministicTypeHandler`** — 确定性加密，用于 username / email：
- IV 由 `SHA-256(plaintext)` 前 16 字节派生
- 同一明文始终产生同一密文

**`SM4EncryptTypeHandler`** — 随机 IV 加密，用于 displayName：
- 使用 `SecureRandom` 生成随机 IV
- 同一明文每次产生不同密文（更安全）

两者均从 `Sm4Config` 获取数据密钥，解密时从密文中提取 IV（IV 拼接在密文前）。

### 2.4 Entity 修改

`User.java` 敏感字段添加注解：

```java
@TableField(typeHandler = SM4DeterministicTypeHandler.class)
private String username;   // 确定性加密（UNIQUE + WHERE eq 查询）

@TableField(typeHandler = SM4DeterministicTypeHandler.class)
private String email;      // 确定性加密（预留查询需求）

@TableField(typeHandler = SM4EncryptTypeHandler.class)
private String displayName; // 随机 IV 加密（仅展示）

TypeHandler 对应用层透明：INSERT/UPDATE 自动加密，SELECT 自动解密。

### 2.5 Service 层适配

`username` 在 `AuthService` 中用于 WHERE 精确匹配，需在查询前用确定性加密处理条件值：

```java
// login() 和 createUser() 中的改动
// 使用 SM4Util.encryptDeterministic() 而非 encrypt()
String encrypted = sm4Util.encryptDeterministic(username, sm4Config.getDataKey());
new LambdaQueryWrapper<User>().eq(User::getUsername, encrypted)
```

`email` 和 `displayName` 不做 WHERE 条件，无需修改 Service 层。

**交叉模块（knowledge-content、knowledge-social）：** 仅读取 displayName/username 用于展示，解密由 TypeHandler 自动完成，**无需修改**。

### 2.6 DDL 变更

新建 Flyway 迁移 `V13__encrypt_sensitive.sql`，扩展加密后字段长度：

| 字段 | 原长度 | 新长度 | 计算依据 |
|------|--------|--------|---------|
| `username` | VARCHAR(64) | VARCHAR(256) | SM4 CBC: 16B IV + 密文(≤64B) → Base64 ≈ 172B |
| `email` | VARCHAR(128) | VARCHAR(256) | 同上 |
| `display_name` | VARCHAR(64) | VARCHAR(256) | 同上 |

### 2.7 存量数据迁移

已新建 `DataEncryptInitializer`（实现 `ApplicationRunner`），应用启动时自动检测并加密存量明文数据：

- 使用 `JdbcTemplate` 直连数据库，绕过 MyBatis TypeHandler（避免双重加密）
- 通过尝试 SM4 解密判断数据是否已加密：解密失败 → 明文 → 加密；解密成功 → 已加密 → 跳过
- 仅处理 `user` 表的 `username`、`email`、`display_name` 三个字段
- 幂等设计，多次执行安全

### 2.8 测试更新

- 更新各模块 `schema-h2.sql`：扩展字段长度
- `UserMapperTest`：适配加密后的验证逻辑
- `AuthServiceTest`：适配加密后的查询逻辑

---

## 3. 影响范围

| 模块 | 改动 |
|------|------|
| `knowledge-common` | 新增 `SM4DeterministicTypeHandler`、`SM4EncryptTypeHandler`、`Sm4Config`、`SM4Util` 扩展 |
| `knowledge-web` | `application.yml` 加 `sm4.data-key` |
| `knowledge-user-auth` | `User.java` 加注解、`AuthService.java` 改查询、Flyway V13、`DataEncryptInitializer` 存量迁移 |
| `knowledge-content` | **无需修改**（仅通过 User 实体读 displayName，TypeHandler 自动解密） |
| `knowledge-social` | **无需修改**（同上） |

---

## 4. 执行步骤

| 序号 | 模块 | 操作 |
|------|------|------|
| 1 | `knowledge-common` | `SM4Util` 添加 `encryptDeterministic()` / `decryptDeterministic()` 方法 |
| 2 | `knowledge-common` | 新建 `Sm4Config` 配置类 |
| 3 | `knowledge-common` | 新建 `SM4DeterministicTypeHandler`（确定性 IV） |
| 4 | `knowledge-common` | 新建 `SM4EncryptTypeHandler`（随机 IV） |
| 5 | `knowledge-web` | `application.yml` 添加 `sm4.data-key` 配置 |
| 6 | `knowledge-user-auth` | `User.java` 添加 `@TableField(typeHandler = ...)` |
| 7 | `knowledge-user-auth` | `AuthService.java` 修改 username 查询逻辑 |
| 8 | `knowledge-user-auth` | 新建 `V13__encrypt_sensitive.sql` 扩展字段长度 |
| 9 | `knowledge-user-auth` | 种子数据迁移（SQL → 代码初始化） |
| 10 | 各模块 | 更新 H2 测试 schema 和单元测试 |

---

## 5. 风险与注意事项

- **模糊查询失效：** 加密后 `LIKE` 查询不可用。当前 username 仅做精确匹配（`eq`），不受影响
- **UNIQUE 索引：** username 使用确定性加密（IV = SHA-256(plaintext)[0:16]），同一明文产生同一密文，UNIQUE 约束依然有效
- **IV 安全：** 确定性加密的 IV 派生自字段值本身（非全局固定值），不同明文使用不同 IV，无模式泄露风险
- **长度限制：** 确保加密后不超过扩展后的列宽
