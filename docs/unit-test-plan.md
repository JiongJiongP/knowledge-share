# 后端单元测试编写计划

## 目标
- 覆盖率 >= 80%
- 通过率 100%
- 使用 JUnit 5 + Mockito 进行单元测试
- 使用 H2 内存数据库进行集成测试

## 项目模块概览

| 模块 | 说明 | 测试数 | 状态 |
|------|------|--------|------|
| knowledge-common | 通用工具/异常/结果类 | 33 | ✅ 全部通过 |
| knowledge-user-auth | 用户认证授权 | 31 | ✅ 全部通过 |
| knowledge-content | 内容管理 | 77 | ✅ 全部通过 |
| knowledge-social | 社交互动 | 40 | ✅ 全部通过 |
| knowledge-search | 搜索服务 | 14 | ✅ 全部通过 |
| knowledge-web | Web聚合层 | 8 | ✅ 全部通过 |
| knowledge-analytics | 数据分析 | - | 无Java源码，无需测试 |
| knowledge-file | 文件服务 | - | 无Java源码，无需测试 |
| knowledge-notification | 通知服务 | - | 无Java源码，无需测试 |

**总计: 203 个测试，0 失败，0 错误，通过率 100%**

## 已完成的测试文件

### knowledge-common (33 tests)
- BizExceptionTest - 异常构造、静态工厂方法
- GlobalExceptionHandlerTest - 各类异常处理（BizException、校验异常、格式异常、未知异常等）
- SM4UtilTest - SM4加密解密、密钥生成
- MyBatisPlusConfigTest - 自动填充逻辑
- PageResultTest - 分页结果
- ResultTest - 统一返回结果

### knowledge-user-auth (31 tests)
- AuthServiceTest - 认证服务核心逻辑
- AuthServiceExtendedTest - 补充listUsers、createUser、updateUserRole测试
- UserControllerTest - 用户管理API
- AuthControllerTest - 认证API
- UserMapperTest - 用户Mapper集成测试（含SM4加密字段解密验证）
- JwtUtilTest - JWT工具类
- JwtAuthFilterTest - JWT过滤器

### knowledge-content (77 tests)
- ContentServiceTest - 内容服务核心逻辑（含UserMapper mock）
- ProductionToolchainServiceTest - 版本管理、审核流程、模板管理、定时发布
- AnalyticsServiceTest - 数据分析服务
- TagServiceTest - 标签服务
- SensitiveWordServiceTest - 敏感词服务
- AhoCorasickAutomatonTest - AC自动机算法
- ContentRepositoryTest - 内容仓储集成测试
- TagRepositoryTest - 标签仓储集成测试
- ContentControllerTest - 内容管理API

### knowledge-social (40 tests)
- CommentServiceTest - 评论服务
- FavoriteServiceTest - 收藏服务
- GroupServiceTest - 群组服务（含UserMapper、EventPublisher mock）
- NotificationServiceTest - 通知服务（含markRead/delete权限校验）
- GroupRepositoryTest - 群组仓储集成测试

### knowledge-search (14 tests)
- SearchServiceTest - 搜索服务（ES mock）
- VectorSearchServiceTest - 向量搜索服务（null QdrantClient处理）
- SearchControllerTest - 搜索API

### knowledge-web (8 tests)
- StatsServiceTest - 统计服务
- TodoServiceTest - 待办事项服务
- StatsControllerTest - 统计API
- TodoControllerTest - 待办事项API

## 测试策略

### 单元测试（Mockito）
- Service层：使用@Mock模拟依赖，@InjectMocks注入被测对象
- Controller层：使用standalone MockMvc + GlobalExceptionHandler
- 工具类：直接实例化测试

### 集成测试（H2内存数据库）
- Repository层：使用@SpringBootTest + H2内存数据库
- Mapper层：使用@SpringBootTest + H2内存数据库

### 关键技术决策
1. **跨模块依赖处理**：对于依赖其他模块Mapper的服务（如ContentService依赖UserMapper），在单元测试中通过@Mock注入
2. **集成测试排除策略**：TestConfig中排除依赖跨模块Mapper的Service和Controller，避免ApplicationContext加载失败
3. **Flyway排除**：测试环境排除FlywayAutoConfiguration，使用schema.sql初始化H2数据库
4. **SM4加密字段**：集成测试中数据库存储的是加密值，断言时需要先解密再比较
5. **Optional依赖注入**：GroupService的Optional<EventPublisher>参数，使用手动构造而非@InjectMocks

### 覆盖率工具
- 使用JaCoCo Maven插件生成覆盖率报告
- 命令: `mvn test jacoco:report`
