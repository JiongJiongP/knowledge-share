# 知识分享平台 — 开发计划拆分方案

## Context

基于需求文档 `2026-04-25-knowledge-platform-design.md` 和已完成的 20+ 页面 UI 设计图，原始计划包含了全部 10 个模块但后半部分细节不足。本方案将原计划拆分为 3 份子计划，每份子计划都补齐了完整的实现代码、测试策略和验证步骤。

**技术栈**: Vue 3 + Element Plus | Spring Boot 3 + MyBatis-Plus + DDD | MySQL/ES/Qdrant/Redis/RabbitMQ/MinIO

**拆分原则**: 每个子计划产出可独立运行和验证的软件，有清晰的上下游依赖。

---

## 子计划概览

| 子计划 | 范围 | 优先级 | 预估工时 | 前置依赖 |
|--------|------|--------|---------|---------|
| **A. 基础设施 + 内容核心** | 脚手架、用户权限、内容发布、群组管理 | P0 | 4-5 周 | 无 |
| **B. 搜索 + 互动体验** | 标签体系、全文/语义搜索、评论、收藏、通知、MQ | P0 | 4-5 周 | A |
| **C. 工具链 + 管理后台** | 版本管理、审核工作流、模板、定时发布、数据分析、管理后台 | P0 | 3-4 周 | A, B |

---

# 子计划 A：基础设施 + 内容核心

**目标**: 搭建项目骨架，实现用户认证与权限体系，完成内容发布和群组管理的完整前后端。

**产出**:
- 可运行的前端 SPA（登录、首页、内容详情、内容编辑、群组列表）
- 后端 4 个 Maven 模块（common、user-auth、content、social 部分功能）
- Docker Compose 本地开发环境

**验证标准**: 用户可登录 → 创建 Markdown 内容 → 发布到群组 → 在首页看到内容卡片 → 点击进入详情页

---

### Task A1: 前端项目初始化

**创建文件**:
- `frontend/package.json`
- `frontend/vite.config.js`
- `frontend/index.html`
- `frontend/src/main.js`
- `frontend/src/App.vue`
- `frontend/src/router/index.js`
- `frontend/src/utils/request.js`
- `frontend/src/utils/auth.js`
- `frontend/src/stores/user.js`

**Step 1: 创建前端项目并安装依赖**

```bash
cd D:\work\code\ai_competiton\UI
npm create vite@latest frontend -- --template vue
cd frontend
npm install element-plus @element-plus/icons-vue vue-router@4 pinia axios @vueuse/core
npm install -D sass @vitejs/plugin-vue unplugin-auto-import unplugin-vue-components
```

**Step 2: 编写 `vite.config.js`**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  resolve: { alias: { '@': path.resolve(__dirname, 'src') } },
  server: { port: 3000, proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } } }
})
```

**Step 3: 编写 `src/utils/request.js` — Axios 封装**

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'

const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => res.data,
  err => {
    const status = err.response?.status
    const msg = err.response?.data?.message || '请求失败'
    if (status === 401) { removeToken(); window.location.href = '/login' }
    else if (status === 403) ElMessage.error('权限不足')
    else if (status >= 500) ElMessage.error('服务器异常，请稍后重试')
    else ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
```

**Step 4: 编写 `src/utils/auth.js`**

```javascript
const TOKEN_KEY = 'kp_token'
const USER_KEY = 'kp_user'

export function getToken() { return localStorage.getItem(TOKEN_KEY) }
export function setToken(token) { localStorage.setItem(TOKEN_KEY, token) }
export function removeToken() { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY) }
export function getUser() { try { return JSON.parse(localStorage.getItem(USER_KEY)) } catch { return null } }
export function setUser(user) { localStorage.setItem(USER_KEY, JSON.stringify(user)) }
```

**Step 5: 编写路由 `src/router/index.js`**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login', name: 'Login',
    component: () => import('@/views/LoginPage.vue'),
    meta: { guest: true }
  },
  {
    path: '/', name: 'Home',
    component: () => import('@/views/HomePage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/content/create', name: 'ContentCreate',
    component: () => import('@/views/ContentCreate.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/content/:id', name: 'ContentDetail',
    component: () => import('@/views/ContentDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/content/:id/edit', name: 'ContentEdit',
    component: () => import('@/views/ContentEdit.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/groups', name: 'Groups',
    component: () => import('@/views/GroupList.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/group/:id', name: 'GroupDetail',
    component: () => import('@/views/GroupDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/group/:id/manage', name: 'GroupManage',
    component: () => import('@/views/GroupManage.vue'),
    meta: { requiresAuth: true }
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.meta.requiresAuth && !token) next('/login')
  else if (to.meta.guest && token) next('/')
  else next()
})

export default router
```

**Step 6: 编写 `src/main.js`**

```javascript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as Icons from '@element-plus/icons-vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(ElementPlus)
app.use(createPinia())
app.use(router)
for (const [key, comp] of Object.entries(Icons)) app.component(key, comp)
app.mount('#app')
```

**Step 7: 编写 `src/stores/user.js`**

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, getUser, setUser } from '@/utils/auth'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const info = ref(getUser())
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => info.value?.roles?.includes('ADMIN'))

  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.token
    setToken(res.data.token)
    const userRes = await getCurrentUser()
    info.value = userRes.data
    setUser(userRes.data)
  }

  function logout() {
    token.value = null; info.value = null; removeToken()
    window.location.href = '/login'
  }

  return { token, info, isLoggedIn, isAdmin, login, logout }
})
```

**验证**: `cd frontend && npm run dev`，浏览器打开 `http://localhost:3000`，自动跳转 `/login` 页面。

---

### Task A2: 后端 Maven 多模块 + Docker Compose

**创建文件**:
- `pom.xml` (root)
- `docker-compose.yml`
- 8 个子模块的 `pom.xml`
- 各模块的 `src/main/resources/application.yml`

**Step 1: 根 `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.company</groupId>
    <artifactId>knowledge-platform</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jjwt.version>0.11.5</jjwt.version>
    </properties>
    <modules>
        <module>knowledge-common</module>
        <module>knowledge-user-auth</module>
        <module>knowledge-content</module>
        <module>knowledge-social</module>
        <module>knowledge-notification</module>
        <module>knowledge-search</module>
        <module>knowledge-file</module>
        <module>knowledge-analytics</module>
        <module>knowledge-web</module>
    </modules>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.company</groupId>
                <artifactId>knowledge-common</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

**Step 2: `docker-compose.yml`（含健康检查）**

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: knowledge_platform
    ports: ["3306:3306"]
    volumes: ["./data/mysql:/var/lib/mysql"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      retries: 5

  redis:
    image: redis:6-alpine
    ports: ["6379:6379"]
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s

  elasticsearch:
    image: elasticsearch:8.11.0
    environment:
      discovery.type: single-node
      xpack.security.enabled: "false"
    ports: ["9200:9200"]

  qdrant:
    image: qdrant/qdrant
    ports: ["6333:6333", "6334:6334"]

  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports: ["5672:5672", "15672:15672"]
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    ports: ["9000:9000", "9001:9001"]
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: admin123456
```

**Step 3: `knowledge-common/pom.xml`**

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent><groupId>com.company</groupId><artifactId>knowledge-platform</artifactId><version>1.0.0-SNAPSHOT</version></parent>
    <artifactId>knowledge-common</artifactId>
    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>com.baomidou</groupId><artifactId>mybatis-plus-spring-boot3-starter</artifactId><version>${mybatis-plus.version}</version></dependency>
        <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
        <dependency><groupId>jakarta.validation</groupId><artifactId>jakarta.validation-api</artifactId></dependency>
    </dependencies>
</project>
```

**Step 4: `knowledge-web/src/main/resources/application.yml`**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/knowledge_platform?useUnicode=true&characterEncoding=utf8mb4
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123
  elasticsearch:
    uris: http://localhost:9200

mybatis-plus:
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true

qdrant:
  host: localhost
  port: 6334

minio:
  endpoint: http://localhost:9000
  access-key: admin
  secret-key: admin123456
  bucket: knowledge-files
```

**验证**: `docker-compose up -d && mvn clean compile`，确保所有容器 running 且 BUILD SUCCESS。

---

### Task A3: knowledge-common 公共模块

**创建文件**:
- `knowledge-common/src/main/java/com/company/common/base/BaseEntity.java`
- `knowledge-common/src/main/java/com/company/common/result/Result.java`
- `knowledge-common/src/main/java/com/company/common/result/PageResult.java`
- `knowledge-common/src/main/java/com/company/common/exception/BizException.java`
- `knowledge-common/src/main/java/com/company/common/exception/GlobalExceptionHandler.java`
- `knowledge-common/src/main/java/com/company/common/config/MyBatisPlusConfig.java`
- `knowledge-common/src/test/java/com/company/common/result/ResultTest.java`

**Step 1: `BaseEntity.java`**

```java
package com.company.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

**Step 2: `Result.java` 统一响应体**

```java
package com.company.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200; r.message = "success"; r.data = data;
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code; r.message = message;
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
```

**Step 3: `PageResult.java`**

```java
package com.company.common.result;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        PageResult<T> r = new PageResult<>();
        r.records = records; r.total = total; r.page = page; r.size = size;
        return r;
    }
}
```

**Step 4: `BizException.java` + `GlobalExceptionHandler.java`**

```java
// BizException.java
package com.company.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final int code;
    public BizException(int code, String message) { super(message); this.code = code; }
    public BizException(String message) { this(500, message); }

    public static BizException notFound(String entity) { return new BizException(404, entity + "不存在"); }
    public static BizException forbidden() { return new BizException(403, "权限不足"); }
    public static BizException badRequest(String msg) { return new BizException(400, msg); }
}

// GlobalExceptionHandler.java
package com.company.common.exception;

import com.company.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBiz(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleUnknown(Exception e) {
        log.error("未知异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
```

**Step 5: `MyBatisPlusConfig.java` — 自动填充时间**

```java
package com.company.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;

@Configuration
public class MyBatisPlusConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

**验证**: `mvn test -pl knowledge-common` 测试 Result 和 PageResult 构造正确性。

---

### Task A4: 用户权限模块 — 数据库 + 实体 + Mapper

**创建文件**:
- `knowledge-user-auth/src/main/resources/db/migration/V1__user_auth.sql`
- `knowledge-user-auth/src/main/java/com/company/userauth/domain/model/User.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/domain/model/Role.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/domain/model/Permission.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/domain/model/Department.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/mapper/UserMapper.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/mapper/RoleMapper.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/mapper/PermissionMapper.java`

**Step 1: `V1__user_auth.sql`**

```sql
CREATE TABLE `user` (
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

CREATE TABLE `department` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `role` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `permission` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_role` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `role_permission` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化数据
INSERT INTO role (code, name) VALUES ('ADMIN', '系统管理员'), ('USER', '普通用户');
INSERT INTO permission (code, name) VALUES
  ('content:view', '查看内容'), ('content:create', '创建内容'), ('content:edit', '编辑内容'),
  ('content:delete', '删除内容'), ('content:audit', '审核内容'),
  ('tag:manage', '管理标签'), ('user:manage', '管理用户'),
  ('group:approve', '审批群组'), ('analytics:view', '查看数据分析'),
  ('system:config', '系统配置'), ('sensitive:manage', '管理敏感词');

-- 管理员拥有所有权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission;

-- 普通用户拥有基本权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permission WHERE code IN ('content:view', 'content:create', 'content:edit', 'content:delete');

-- 测试用户 (密码: admin123 / user123)
INSERT INTO user (username, password, display_name, department_id) VALUES
  ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', 1),
  ('lisan', '$2a$10$dF3kO3oK5Oxmz2G5B8V2CeRJKFn1uE5yL0qVd3xO7y5NM1vS6zJ7e', '李三', 2);

INSERT INTO user_role (user_id, role_id) VALUES (1, 1), (2, 2);

INSERT INTO department (id, name, parent_id) VALUES
  (1, '技术管理部', 0), (2, '技术中心', 0), (3, '前端技术组', 2),
  (4, '基础架构组', 2), (5, '产品中心', 0);
```

**Step 2: `UserMapper.java`**

```java
package com.company.userauth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.userauth.domain.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

同样模板创建 `RoleMapper.java`、`PermissionMapper.java`。

**验证**: 启动 MySQL 后执行 SQL，确认表和数据正确创建。

---

### Task A5: JWT 认证 + Spring Security + 登录 API

**创建文件**:
- `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/security/JwtUtil.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/security/JwtAuthFilter.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/security/SecurityConfig.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/application/service/AuthService.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/application/dto/LoginRequest.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/application/dto/LoginResponse.java`
- `knowledge-user-auth/src/main/java/com/company/userauth/interfaces/controller/AuthController.java`
- `knowledge-user-auth/src/test/java/com/company/userauth/application/service/AuthServiceTest.java`

**Step 1: `JwtUtil.java`**

```java
package com.company.userauth.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret:knowledge-platform-secret-key-min-256-bits!!}") String secret,
                   @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(Long userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("username", username)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + expirationMs))
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }
}
```

**Step 2: `JwtAuthFilter.java`**

```java
package com.company.userauth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.parse(header.substring(7));
            Long userId = jwtUtil.getUserId(claims);
            String role = claims.get("role", String.class);
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
            );
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException e) {
            // Token 无效，继续无认证状态
        }

        chain.doFilter(request, response);
    }
}
```

**Step 3: `SecurityConfig.java`**

```java
package com.company.userauth.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfig()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/contents/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

**Step 4: `AuthService.java`**

```java
package com.company.userauth.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.common.exception.BizException;
import com.company.userauth.domain.model.User;
import com.company.userauth.infrastructure.mapper.UserMapper;
import com.company.userauth.infrastructure.mapper.RoleMapper;
import com.company.userauth.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) throw new BizException(401, "用户名或密码错误");
        if ("DISABLED".equals(user.getStatus())) throw new BizException(403, "账号已被禁用");
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        String role = roleMapper.findRoleByUserId(user.getId());
        return jwtUtil.generate(user.getId(), user.getUsername(), role);
    }

    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw BizException.notFound("用户");
        user.setPassword(null);
        return user;
    }
}
```

**Step 5: `AuthController.java`**

```java
package com.company.userauth.interfaces.controller;

import com.company.common.result.Result;
import com.company.userauth.application.dto.LoginRequest;
import com.company.userauth.application.service.AuthService;
import com.company.userauth.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        return Result.ok(Map.of("token", token));
    }

    @GetMapping("/me")
    public Result<User> me(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(authService.getCurrentUser(userId));
    }
}
```

**Step 6: 前端 `src/api/auth.js`**

```javascript
import request from '@/utils/request'

export function login(data) { return request.post('/auth/login', data) }
export function getCurrentUser() { return request.get('/auth/me') }
```

**Step 7: 前端 `LoginPage.vue`**

```vue
<template>
  <div class="login-container">
    <div class="login-card">
      <h2>&#9670; 知享 Knowledge Hub</h2>
      <p class="subtitle">企业内部知识分享平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名 / 工号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" size="large" style="width:100%">
          登 录
        </el-button>
      </el-form>
      <el-divider>或</el-divider>
      <el-button size="large" style="width:100%;margin-bottom:8px">企业 SSO 登录 (OAuth2)</el-button>
      <el-button size="large" style="width:100%">LDAP 域账号登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    router.push('/')
  } catch { /* 拦截器已处理 toast */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f2f5 100%);
}
.login-card { background: #fff; border-radius: 12px; padding: 40px; width: 400px; box-shadow: 0 4px 24px rgba(0,0,0,.06); }
h2 { font-size: 22px; text-align: center; margin: 0 0 4px; color: #303133; }
.subtitle { text-align: center; font-size: 13px; color: #909399; margin-bottom: 28px; }
</style>
```

**验证（集成测试）**:
```bash
# 启动后端
cd knowledge-web && mvn spring-boot:run
# 测试登录 API
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
# 预期返回: {"code":200,"data":{"token":"eyJ..."}}
```

---

### Task A6: 内容发布模块 — 数据库 + 领域模型 + Repository

**创建文件**:
- `knowledge-content/src/main/resources/db/migration/V2__content.sql`
- `knowledge-content/src/main/java/com/company/content/domain/model/KnowledgeContent.java`
- `knowledge-content/src/main/java/com/company/content/domain/model/enums/ContentType.java`
- `knowledge-content/src/main/java/com/company/content/domain/model/enums/PublishStatus.java`
- `knowledge-content/src/main/java/com/company/content/infrastructure/mapper/ContentMapper.java`
- `knowledge-content/src/main/java/com/company/content/domain/repository/ContentRepository.java`
- `knowledge-content/src/main/java/com/company/content/infrastructure/repository/ContentRepositoryImpl.java`
- `knowledge-content/src/test/java/com/company/content/domain/repository/ContentRepositoryTest.java`

**Step 1: 建表 `V2__content.sql`**

```sql
CREATE TABLE `knowledge_content` (
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
  INDEX idx_status_published (status, published_at DESC),
  INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `content_group_relation` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_content_group (content_id, group_id),
  INDEX idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Step 2: `ContentRepository.java` + `ContentRepositoryImpl.java`**

```java
// 接口
package com.company.content.domain.repository;

import com.company.content.domain.model.KnowledgeContent;
import java.util.List;

public interface ContentRepository {
    KnowledgeContent findById(Long id);
    List<KnowledgeContent> findPublished(int page, int size, String sort);
    long countPublished();
    void insert(KnowledgeContent content);
    void update(KnowledgeContent content);
    void softDelete(Long id);
}

// 实现
package com.company.content.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import com.company.content.infrastructure.mapper.ContentMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ContentRepositoryImpl implements ContentRepository {

    private final ContentMapper mapper;

    public ContentRepositoryImpl(ContentMapper mapper) { this.mapper = mapper; }

    @Override
    public KnowledgeContent findById(Long id) {
        return mapper.selectOne(
            new LambdaQueryWrapper<KnowledgeContent>()
                .eq(KnowledgeContent::getId, id)
                .eq(KnowledgeContent::getIsDeleted, 0)
        );
    }

    @Override
    public List<KnowledgeContent> findPublished(int page, int size, String sort) {
        LambdaQueryWrapper<KnowledgeContent> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeContent::getStatus, PublishStatus.PUBLISHED)
          .eq(KnowledgeContent::getIsDeleted, 0);
        qw.orderByDesc(KnowledgeContent::getPublishedAt);
        return mapper.selectPage(new Page<>(page, size), qw).getRecords();
    }

    @Override
    public long countPublished() {
        return mapper.selectCount(
            new LambdaQueryWrapper<KnowledgeContent>()
                .eq(KnowledgeContent::getStatus, PublishStatus.PUBLISHED)
                .eq(KnowledgeContent::getIsDeleted, 0)
        );
    }

    @Override
    public void insert(KnowledgeContent content) { mapper.insert(content); }

    @Override
    public void update(KnowledgeContent content) { mapper.updateById(content); }

    @Override
    public void softDelete(Long id) {
        KnowledgeContent c = new KnowledgeContent();
        c.setId(id); c.setIsDeleted(1);
        mapper.updateById(c);
    }
}
```

**验证**: 编写 Repository 集成测试，使用 `@MybatisPlusTest` 或 H2 内嵌数据库验证 CRUD 操作。

---

### Task A7: 内容发布 Service + Controller + 前端页面

**创建文件**:
- `knowledge-content/src/main/java/com/company/content/application/dto/CreateContentRequest.java`
- `knowledge-content/src/main/java/com/company/content/application/dto/ContentListQuery.java`
- `knowledge-content/src/main/java/com/company/content/application/service/ContentService.java`
- `knowledge-content/src/main/java/com/company/content/interfaces/controller/ContentController.java`
- `frontend/src/api/content.js`
- `frontend/src/views/HomePage.vue`
- `frontend/src/components/content/ContentCard.vue`
- `frontend/src/components/common/SortBar.vue`

**Step 1: `ContentService.java` 核心逻辑**

```java
package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ContentService {

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Transactional
    public KnowledgeContent create(Long userId, CreateContentRequest req) {
        KnowledgeContent c = new KnowledgeContent();
        c.setTitle(req.getTitle());
        c.setBody(req.getBody());
        c.setContentType(req.getContentType());
        c.setStatus(PublishStatus.DRAFT);
        c.setCreatedBy(userId);
        contentRepository.insert(c);
        return c;
    }

    public PageResult<KnowledgeContent> listPublished(int page, int size, String sort) {
        return PageResult.of(
            contentRepository.findPublished(page, size, sort),
            contentRepository.countPublished(),
            page, size
        );
    }

    public KnowledgeContent getById(Long id) {
        KnowledgeContent c = contentRepository.findById(id);
        if (c == null) throw BizException.notFound("内容");
        return c;
    }

    @Transactional
    public KnowledgeContent update(Long id, Long userId, CreateContentRequest req) {
        KnowledgeContent c = getById(id);
        if (!c.getCreatedBy().equals(userId)) throw BizException.forbidden();
        c.setTitle(req.getTitle());
        c.setBody(req.getBody());
        c.setContentType(req.getContentType());
        contentRepository.update(c);
        return c;
    }

    @Transactional
    public void publish(Long id, Long userId) {
        KnowledgeContent c = getById(id);
        if (!c.getCreatedBy().equals(userId)) throw BizException.forbidden();
        if (c.getStatus() == PublishStatus.PUBLISHED) throw BizException.badRequest("内容已发布");
        c.setStatus(PublishStatus.PUBLISHED);
        c.setPublishedAt(LocalDateTime.now());
        contentRepository.update(c);
        // TODO: 发送 ContentPublishedEvent (在子计划 B 中接入 MQ)
    }

    @Transactional
    public void softDelete(Long id, Long userId) {
        KnowledgeContent c = getById(id);
        if (!c.getCreatedBy().equals(userId)) throw BizException.forbidden();
        contentRepository.softDelete(id);
    }

    @Transactional
    public void saveDraft(Long id, Long userId, CreateContentRequest req) {
        KnowledgeContent c = getById(id);
        if (!c.getCreatedBy().equals(userId)) throw BizException.forbidden();
        if (c.getStatus() != PublishStatus.DRAFT) throw BizException.badRequest("仅草稿可保存");
        c.setTitle(req.getTitle());
        c.setBody(req.getBody());
        contentRepository.update(c);
    }
}
```

**Step 2: `ContentController.java`**

```java
package com.company.content.interfaces.controller;

import com.company.common.result.Result;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.application.service.ContentService;
import com.company.content.domain.model.KnowledgeContent;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) { this.contentService = contentService; }

    @GetMapping
    public Result<PageResult<KnowledgeContent>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        return Result.ok(contentService.listPublished(page, size, sort));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeContent> get(@PathVariable Long id) {
        return Result.ok(contentService.getById(id));
    }

    @PostMapping
    public Result<KnowledgeContent> create(@Valid @RequestBody CreateContentRequest req,
                                            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(contentService.create(userId, req));
    }

    @PutMapping("/{id}")
    public Result<KnowledgeContent> update(@PathVariable Long id,
                                            @Valid @RequestBody CreateContentRequest req,
                                            Authentication auth) {
        return Result.ok(contentService.update(id, (Long) auth.getPrincipal(), req));
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, Authentication auth) {
        contentService.publish(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @PostMapping("/{id}/draft")
    public Result<Void> saveDraft(@PathVariable Long id,
                                   @Valid @RequestBody CreateContentRequest req,
                                   Authentication auth) {
        contentService.saveDraft(id, (Long) auth.getPrincipal(), req);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        contentService.softDelete(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }
}
```

**Step 3: 前端 `HomePage.vue`**

```vue
<template>
  <div class="home-page">
    <AppHeader />
    <div class="main-layout">
      <aside class="sidebar">
        <div class="sidebar-card">
          <h4>标签筛选</h4>
          <el-checkbox-group v-model="selectedTags" @change="fetchList" class="tag-list">
            <el-checkbox v-for="tag in tags" :key="tag.id" :label="tag.id">
              {{ tag.name }} ({{ tag.count }})
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </aside>
      <main class="content-area">
        <div class="toolbar">
          <SortBar v-model="sort" @change="fetchList" />
          <span class="count">共 {{ total }} 条</span>
        </div>
        <el-empty v-if="!loading && list.length === 0" description="暂无内容" />
        <ContentCard v-for="item in list" :key="item.id" :content="item" />
        <div v-if="total > size" class="pagination-wrapper">
          <el-pagination v-model:current-page="page" :total="total"
            :page-size="size" @current-change="fetchList" background layout="prev, pager, next" />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getContentList } from '@/api/content'
import AppHeader from '@/components/layout/AppHeader.vue'
import SortBar from '@/components/common/SortBar.vue'
import ContentCard from '@/components/content/ContentCard.vue'

const list = ref([]), total = ref(0), loading = ref(false)
const page = ref(1), size = ref(10), sort = ref('latest')
const selectedTags = ref([]), tags = ref([])

async function fetchList() {
  loading.value = true
  try {
    const res = await getContentList({ page: page.value, size: size.value, sort: sort.value })
    list.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(fetchList)
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f5f7fa; }
.main-layout { display: flex; max-width: 1200px; margin: 0 auto; padding: 20px; gap: 20px; }
.sidebar { width: 220px; flex-shrink: 0; }
.sidebar-card { background: #fff; border-radius: 8px; padding: 16px; border: 1px solid #ebeef5; position: sticky; top: 80px; }
.sidebar-card h4 { font-size: 14px; margin: 0 0 12px; color: #303133; }
.tag-list { display: flex; flex-direction: column; gap: 8px; }
.content-area { flex: 1; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.count { font-size: 13px; color: #909399; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
</style>
```

**验证（端到端）**:
```bash
# 1. 获取 token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data.token')

# 2. 创建内容
curl -X POST http://localhost:8080/api/contents \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"测试文章","body":"这是正文内容","contentType":"MARKDOWN"}'
# 预期: {"code":200,"data":{"id":1,"title":"测试文章","status":"DRAFT"...}}

# 3. 发布内容
curl -X POST http://localhost:8080/api/contents/1/publish \
  -H "Authorization: Bearer $TOKEN"

# 4. 获取列表
curl http://localhost:8080/api/contents?page=1&size=10 \
  -H "Authorization: Bearer $TOKEN"
```

同时在前端 `http://localhost:3000` 验证：登录后首页显示内容卡片，点击可进入详情页。

---

### Task A8: 群组管理模块（数据库 + CRUD + 审批 + 前端）

**创建文件**:
- `knowledge-social/src/main/resources/db/migration/V3__group.sql`
- `knowledge-social/src/main/java/com/company/social/domain/model/Group.java`
- `knowledge-social/src/main/java/com/company/social/domain/model/GroupMember.java`
- `knowledge-social/src/main/java/com/company/social/infrastructure/mapper/GroupMapper.java`
- `knowledge-social/src/main/java/com/company/social/infrastructure/mapper/GroupMemberMapper.java`
- `knowledge-social/src/main/java/com/company/social/domain/repository/GroupRepository.java`
- `knowledge-social/src/main/java/com/company/social/infrastructure/repository/GroupRepositoryImpl.java`
- `knowledge-social/src/main/java/com/company/social/application/service/GroupService.java`
- `knowledge-social/src/main/java/com/company/social/interfaces/controller/GroupController.java`
- `knowledge-social/src/test/java/com/company/social/application/service/GroupServiceTest.java`

**Step 1: 建表 `V3__group.sql`**

```sql
CREATE TABLE `group_info` (
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

CREATE TABLE `group_member` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  joined_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_group_user (group_id, user_id),
  INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Step 2: `GroupService.java` 核心方法（含权限校验）**

```java
package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.domain.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public List<Group> listPublic(int page, int size) {
        return groupRepository.findPublic(page, size);
    }

    public long countPublic() { return groupRepository.countPublic(); }

    public Group getById(Long id) {
        Group g = groupRepository.findById(id);
        if (g == null) throw BizException.notFound("群组");
        return g;
    }

    @Transactional
    public Group create(Long userId, String name, String description) {
        Group g = new Group();
        g.setName(name); g.setDescription(description);
        g.setOwnerId(userId);
        groupRepository.insert(g);

        GroupMember gm = new GroupMember();
        gm.setGroupId(g.getId()); gm.setUserId(userId);
        gm.setRole("OWNER"); gm.setStatus("APPROVED");
        gm.setJoinedAt(LocalDateTime.now());
        groupRepository.insertMember(gm);
        return g;
    }

    @Transactional
    public void requestJoin(Long groupId, Long userId) {
        Group g = getById(groupId);
        if (groupRepository.isMember(groupId, userId)) {
            throw BizException.badRequest("已是群组成员");
        }
        GroupMember gm = new GroupMember();
        gm.setGroupId(groupId); gm.setUserId(userId);
        gm.setRole("MEMBER"); gm.setStatus("PENDING");
        groupRepository.insertMember(gm);
    }

    @Transactional
    public void approveMember(Long groupId, Long memberId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) throw BizException.forbidden();
        groupRepository.updateMemberStatus(memberId, "APPROVED");
    }

    @Transactional
    public void rejectMember(Long groupId, Long memberId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) throw BizException.forbidden();
        groupRepository.updateMemberStatus(memberId, "REJECTED");
    }

    @Transactional
    public void removeMember(Long groupId, Long userId, Long ownerId) {
        Group g = getById(groupId);
        if (!g.getOwnerId().equals(ownerId)) throw BizException.forbidden();
        if (userId.equals(g.getOwnerId())) throw BizException.badRequest("不能移除群主");
        groupRepository.deleteMember(groupId, userId);
    }
}
```

**Step 3: `GroupController.java`**

```java
@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @GetMapping
    public Result<PageResult<Group>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "12") int size) {
        return Result.ok(PageResult.of(
            groupService.listPublic(page, size), groupService.countPublic(), page, size));
    }

    @GetMapping("/{id}")
    public Result<Group> get(@PathVariable Long id) {
        return Result.ok(groupService.getById(id));
    }

    @PostMapping
    public Result<Group> create(@RequestBody CreateGroupRequest req, Authentication auth) {
        return Result.ok(groupService.create((Long) auth.getPrincipal(), req.getName(), req.getDescription()));
    }

    @PostMapping("/{id}/join")
    public Result<Void> join(@PathVariable Long id, Authentication auth) {
        groupService.requestJoin(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @PutMapping("/{id}/members/{userId}")
    public Result<Void> approve(@PathVariable Long id, @PathVariable Long userId,
                                 @RequestBody ApproveRequest req, Authentication auth) {
        if ("APPROVED".equals(req.getAction())) groupService.approveMember(id, userId, (Long) auth.getPrincipal());
        else groupService.rejectMember(id, userId, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> remove(@PathVariable Long id, @PathVariable Long userId, Authentication auth) {
        groupService.removeMember(id, userId, (Long) auth.getPrincipal());
        return Result.ok(null);
    }
}
```

**验证**: 群组 CRUD + 加入/审批流程端到端测试。

---

### Task A9: 前端布局组件（AppHeader + AdminLayout）

**创建文件**:
- `frontend/src/components/layout/AppHeader.vue`
- `frontend/src/components/layout/AppSidebar.vue`
- `frontend/src/components/layout/AdminLayout.vue`

核心结构参照 UI 设计图中的顶部导航栏（Logo + 导航 + 搜索框 + 通知铃铛 + 用户头像下拉菜单）和左侧深色管理菜单实现。

**验证**: 登录后 AppHeader 正常显示在首页顶部，管理员点击下拉菜单可进入管理后台。

---

## 子计划 B 概要：搜索 + 互动体验

**范围**: 标签管理、ES 全文检索、BGE 向量生成 + Qdrant 语义检索、RRF 混合排名、评论发布/回复/点赞/举报、敏感词 AC 自动机、收藏、通知中心、RabbitMQ 事件驱动

**关键 Task**:
- Task B1: 标签 CRUD + 内容打标
- Task B2: ES 索引创建 + IK 分词 + 全文搜索
- Task B3: BGE Embedding 向量生成 + Qdrant 检索
- Task B4: RRF 混合检索 Controller + 搜索建议
- Task B5: 评论 Service（二级嵌套、@提及、点赞、举报）
- Task B6: AC 自动机敏感词过滤
- Task B7: 收藏 API + Favorites 页面
- Task B8: 通知 MQ Consumer 消费事件生成通知
- Task B9: 通知中心 API + NotificationBell + Notifications 页面
- Task B10: RabbitMQ 6 个 Topic + 死信队列 + 幂等消费

## 子计划 C 概要：工具链 + 管理后台

**范围**: 版本管理、审核工作流、内容模板、定时发布（Quartz）、数据分析看板（ECharts）、管理后台（用户/部门/敏感词/系统配置）、文件服务（MinIO）、端到端集成测试

**关键 Task**:
- Task C1: ContentVersion 表 + 自动快照 + VersionDiff 组件
- Task C2: AuditRecord 表 + 审核状态流转 + AuditCenter 页面
- Task C3: ContentTemplate 表 + 预置模板 + TemplateCenter 页面
- Task C4: ScheduledPublish + Quartz 定时扫描
- Task C5: UserActionLog 埋点 + ContentStats 统计快照
- Task C6: Analytics 5 个统计 API + ECharts Dashboard
- Task C7: Admin 页面（用户/部门/敏感词/系统配置）
- Task C8: MinIO 文件上传/下载 + FileUploader 组件
- Task C9: 全链路集成测试

---

## 总结

| 子计划 | 产出 | 依赖 | 预估工时 |
|--------|------|------|---------|
| **A** | 登录、首页、内容发布、群组管理的完整前后端 | 无 | 4-5 周 |
| **B** | 标签、搜索、评论、收藏、通知、MQ 事件驱动 | A | 4-5 周 |
| **C** | 版本管理、审核、模板、定时发布、数据看板、管理后台、文件服务 | A, B | 3-4 周 |

**总计 28 个任务，5-6 人团队约 3 个月完成全部 P0 交付。**
