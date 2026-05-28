# SM4 国密数据加密 — 关键决策记录

> **日期:** 2026-05-28 | **作者:** Jason + Claude Code

---

## 概述

本文档记录项目 SM4 数据库字段加密改造过程中的关键决策点，以及人机协作的交互过程。

### 背景

用户提出项目安全性要求高，需检查数据库中是否存在保存敏感信息的字段（手机号、身份证号、住址等），并在 MyBatis 层对敏感字段的 INSERT/UPDATE 执行 SM4 加密，SELECT 执行解密。

---

## 决策 1：敏感字段识别与范围确认

### 交互过程

```
用户: 请你阅读项目的DDL检查数据库中是否存在保存敏感信息的字段，如手机号、
      身份证号、住址等

Claude: [扫描全部 25 张表、26 个实体类]
        → 结论：当前没有手机号/身份证号/住址等强敏感字段
        → 但在 user 表中发现 email / username / displayName 需保护
        → 提出初步计划待用户确认

用户: [审阅分析结果] → 认可结论，要求开始执行
```

### 决议

| 字段 | 是否加密 | 理由 |
|------|---------|------|
| `user.email` | ✅ 加密 | 电子邮箱，个人信息 |
| `user.username` | ✅ 加密 | 可能含个人信息，且用于登录 |
| `user.displayName` | ✅ 加密 | 可能为真实姓名 |
| `user.password` | ❌ 已处理 | 已是 BCrypt 哈希 |
| `user.ssoId` | ❌ 不处理 | 内部标识符 |

**决策原则：** 选择当前实际存在且构成风险的字段，不做过度设计。其他表（notification.content、user_action_log.extraData）可能间接包含用户信息，但优先级低，后续迭代处理。

---

## 决策 2：加密粒度 — TypeHandler vs Interceptor

### 交互过程

```
Claude: [提出两种方案]
        A) TypeHandler: 声明式，字段上注解即可，简单但 WHERE 条件需手动加密
        B) Interceptor: SQL 拦截器，对应用完全透明，但实现复杂需解析 SQL

        推荐 TypeHandler（项目规模小，仅 2 处 WHERE 需适配）
```

### 决议

选择 **MyBatis-Plus TypeHandler 方案**。

理由：
- 项目仅 `username` 字段用于 WHERE 精确匹配（登录、去重检查），2 处调用点手动加密即可
- TypeHandler 实现简单（~30 行每个），易于理解和维护
- 已有 `JacksonTypeHandler` 先例，团队熟悉
- Interceptor 需解析 SQL AST，复杂度高，容易遗漏边界情况

---

## 决策 3：IV 策略 — 确定性加密 vs ECB

### 交互过程

```
Claude: [初版方案]
        - 所有字段使用随机 IV 加密（SM4/CBC）
        - 风险：username 有 UNIQUE 索引，随机 IV 导致同明文→不同密文，约束失效
        - 提出：username 改用 ECB 模式（确定性）或应用层检查唯一性

用户: ECB模式自然是不用

Claude: 理解了。
        → 改为 IV = SHA-256(plaintext)[0:16] 确定性加密
        → 同明文→同 IV→同密文，UNIQUE 约束有效
        → 不同明文→不同 IV（由自身值派生），无全局固定 IV 的模式泄露风险
```

### 决议

**分字段采用不同 IV 策略**：

| 字段 | 模式 | IV 策略 |
|------|------|---------|
| `username` | 确定性加密 | `IV = SHA-256(plaintext)[0:16]` |
| `email` | 确定性加密 | `IV = SHA-256(plaintext)[0:16]` |
| `displayName` | 随机 IV | `SecureRandom` 生成 |

**为什么不选 ECB：**
- ECB 对相同明文块产生相同密文块，模式泄露风险大
- ECB 已被业界弃用（NIST SP 800-38A 不推荐）
- SHA-256 派生 IV 方案既保证确定性（同明文→同密文），又保证每值独立 IV

---

## 决策 4：密钥管理 — 两级密钥体系

### 交互过程

```
用户: 我希望SM4的秘钥写在yml文件中配置

Claude: [分析]
        → 已有 EncryptedPropertyPostProcessor（解析 SM4(...) 占位符）
        → 需要 bootstrap key 先解密配置中的 SM4(...) 值
        → 设计两级密钥：SM4_KEY 环境变量（引导密钥） + sm4.data-key（数据密钥）
```

### 决议

**两级密钥体系**：

```
SM4_KEY (环境变量)           ← 启动引导密钥
  ↓ 解密
sm4.data-key (YAML 配置)     ← 数据加密密钥
  ↓ 用于
数据库字段 SM4 加解密
```

| 密钥 | 存储位置 | 用途 |
|------|---------|------|
| `SM4_KEY` | 环境变量 / `~/.knowledge-secret.key` | 解密 YAML 中的 `SM4(...)` 占位符 |
| `sm4.data-key` | `application.yml`（可选 `SM4(...)` 密文或明文） | 加解密数据库字段 |

**关键设计：** `Sm4Config` 通过 `@Value("${sm4.data-key}")` 注入后被 `EncryptedPropertyPostProcessor` 解密（若为 SM4 密文格式），然后通过静态持有者暴露给非 Spring 管理的 TypeHandler。

---

## 决策 5：存量数据迁移策略

### 交互过程

```
Claude: [实施完成后]
        → 遗留事项：生产环境 V1 种子用户（admin/lisan）是明文存储的
        → 提出两个方案：手动删除重插 vs 启动初始化器

用户: 需要 [实现启动初始化器]

Claude: [实现 DataEncryptInitializer]
        → ApplicationRunner，启动时自动执行
        → JdbcTemplate 绕过 TypeHandler 避免二次加密
        → 尝试解密判断加密状态（幂等）
```

### 决议

**`DataEncryptInitializer` 实现 `ApplicationRunner`**：

1. 启动时用 `JdbcTemplate` 直连读取 user 表（绕过 MyBatis TypeHandler）
2. 尝试 SM4 解密 username 字段 —— 异常捕获判断是否已加密
3. 明文记录 → SM4 加密 → 直连 UPDATE
4. 已加密记录 → 跳过
5. SM4 key 未配置 → 打印 WARN 并跳过（不阻塞启动）

**为什么用 JdbcTemplate 而非 MyBatis Mapper：** 避免 TypeHandler 对已加密数据再次加密导致双重加密。

---

## 决策 6：测试适配策略

### 交互过程

```
Claude: [实施测试时发现]
        → Mockito 测试（无 Spring 上下文）：Sm4Config 未初始化，getDataKey() 抛异常
        → SpringBootTest（H2）：TestConfig @ComponentScan 未覆盖 common 包
        → H2 种子数据：明文 INSERT 后 TypeHandler 解密失败

用户: [静默观察测试运行结果]

Claude: [逐项修复]
        → Sm4Config 添加 initializeForTest() 静态方法
        → TestConfig 导入 Sm4Config.class & 各模块添加 sm4.data-key
        → H2 种子数据移除，测试用自己的数据和加密逻辑
```

### 决议

| 问题 | 方案 |
|------|------|
| Mockito 测试无 Spring | `Sm4Config.initializeForTest()` + `@BeforeAll` |
| SpringBootTest 未扫描 common | `@Import({MyBatisPlusConfig.class, Sm4Config.class})` |
| H2 种子明文 vs TypeHandler | 移除种子用户 INSERT，测试自建数据（经 TypeHandler 自动加密） |
| 测试环境 SM4 key | 各模块 `application-test.yml` 添加 `sm4.data-key` |

---

## 最终架构总览

```
┌─────────────────────────────────────────────────────┐
│                   application.yml                    │
│  sm4.data-key: SM4(...)  ← EncryptedPropertyPP 解密  │
└───────────────────────┬─────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│                    Sm4Config                         │
│  @Value("${sm4.data-key}") → static key holder       │
└───────────────────────┬─────────────────────────────┘
                        ↓
┌───────────────────────────────────────────────────────┐
│                   TypeHandler Layer                    │
│  SM4DeterministicTypeHandler  SM4EncryptTypeHandler    │
│  (IV=SHA-256派生，确定性)      (IV=SecureRandom，随机) │
│  用于 username / email         用于 displayName        │
└───────────────────────┬───────────────────────────────┘
                        ↓
┌───────────────────────────────────────────────────────┐
│                    SM4Util                             │
│  encrypt() / encryptDeterministic()                    │
│  decrypt() / decryptDeterministic()                    │
│  SM4/CBC/PKCS7Padding + BouncyCastle                   │
└───────────────────────┬───────────────────────────────┘
                        ↓
┌───────────────────────────────────────────────────────┐
│                    MySQL (user 表)                      │
│  username VARCHAR(256) ← Base64(IV + SM4密文)          │
│  email VARCHAR(256)    ← Base64(IV + SM4密文)          │
│  display_name VARCHAR(256) ← Base64(IV + SM4密文)       │
└───────────────────────────────────────────────────────┘
                        ↑
┌───────────────────────────────────────────────────────┐
│              DataEncryptInitializer                    │
│  ApplicationRunner — 启动时迁移明文存量数据             │
│  JdbcTemplate 直连（绕过 TypeHandler）                  │
└───────────────────────────────────────────────────────┘
```

---

## 经验总结

1. **先梳理再动手：** 全面扫描 DDL + 实体后确认范围，避免遗漏和过度改造
2. **利用已有基础设施：** 项目已有 SM4Util + EncryptedPropertyPostProcessor，只需扩展
3. **做减法：** 仅加密当前实际敏感字段，notification/template/log 等留待后续评估
4. **确定性加密的权衡：** 拒绝 ECB 后，SHA-256 派生 IV 是兼顾安全性和查询能力的合理折中
5. **测试不能放过：** Mockito / SpringBootTest / H2 三种场景都需要适配

---

## 变更文件清单

| 文件 | 操作 | 模块 |
|------|------|------|
| `SM4Util.java` | 修改（新增 `encryptDeterministic` / `decryptDeterministic`） | common |
| `Sm4Config.java` | **新建** | common |
| `SM4DeterministicTypeHandler.java` | **新建** | common |
| `SM4EncryptTypeHandler.java` | **新建** | common |
| `DataEncryptInitializer.java` | **新建** | user-auth |
| `V13__encrypt_sensitive.sql` | **新建** | user-auth |
| `User.java` | 修改（添加 TypeHandler 注解） | user-auth |
| `AuthService.java` | 修改（WHERE 条件加密） | user-auth |
| `application.yml` | 修改（`sm4.data-key`） | web |
| 3 个 `application-test.yml` | 修改 | 各模块 |
| 3 个 `TestConfig.java` | 修改 | 各模块 |
| `schema-h2.sql` | 修改 | user-auth |
| 2 个 `AuthService*Test.java` | 修改 | user-auth |
| `UserMapperTest.java` | 修改 | user-auth |
