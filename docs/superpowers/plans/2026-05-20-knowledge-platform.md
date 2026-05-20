# 企业内部知识分享平台 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个企业内部知识分享、检索、评论平台，支持 Markdown/PPT/链接/引用四种内容形式，含全文+语义混合检索、DDD 模块化后端、数据分析看板。

**Architecture:** Vue 3 + Element Plus 前端 SPA，Spring Boot DDD 模块化单体后端（7 个 Maven 模块 + 1 个启动模块），通过 RabbitMQ 异步解耦模块间通信，Elasticsearch + Qdrant 混合检索。

**Tech Stack:** Vue 3, Element Plus, Vite, Pinia, Vue Router | Spring Boot 3, MyBatis-Plus, Spring Security, JWT | MySQL 8.0, Elasticsearch 8.x, Qdrant, Redis 6.x, RabbitMQ 3.x, MinIO

---

## 文件结构

```
knowledge-platform/
├── frontend/                          # Vue 3 前端
│   ├── src/
│   │   ├── api/                       # API 请求封装
│   │   │   ├── auth.js
│   │   │   ├── content.js
│   │   │   ├── group.js
│   │   │   ├── comment.js
│   │   │   ├── search.js
│   │   │   ├── favorite.js
│   │   │   ├── notification.js
│   │   │   ├── tag.js
│   │   │   ├── audit.js
│   │   │   ├── template.js
│   │   │   ├── analytics.js
│   │   │   └── admin.js
│   │   ├── stores/                    # Pinia 状态管理
│   │   │   ├── user.js
│   │   │   └── notification.js
│   │   ├── router/
│   │   │   └── index.js
│   │   ├── components/                # 公共组件
│   │   │   ├── layout/
│   │   │   │   ├── AppHeader.vue
│   │   │   │   ├── AppSidebar.vue
│   │   │   │   └── AdminLayout.vue
│   │   │   ├── content/
│   │   │   │   ├── ContentCard.vue
│   │   │   │   ├── MarkdownEditor.vue
│   │   │   │   ├── TagSelector.vue
│   │   │   │   └── FileUploader.vue
│   │   │   ├── comment/
│   │   │   │   ├── CommentSection.vue
│   │   │   │   ├── CommentItem.vue
│   │   │   │   └── CommentInput.vue
│   │   │   ├── common/
│   │   │   │   ├── Pagination.vue
│   │   │   │   ├── SortBar.vue
│   │   │   │   └── SearchBox.vue
│   │   │   ├── notification/
│   │   │   │   └── NotificationBell.vue
│   │   │   ├── version/
│   │   │   │   └── VersionDiff.vue
│   │   │   └── analytics/
│   │   │       └── AnalyticsChart.vue
│   │   ├── views/                     # 页面组件
│   │   │   ├── HomePage.vue
│   │   │   ├── ContentDetail.vue
│   │   │   ├── ContentEdit.vue
│   │   │   ├── ContentCreate.vue
│   │   │   ├── VersionHistory.vue
│   │   │   ├── TemplateCenter.vue
│   │   │   ├── GroupList.vue
│   │   │   ├── GroupDetail.vue
│   │   │   ├── GroupManage.vue
│   │   │   ├── LoginPage.vue
│   │   │   ├── Favorites.vue
│   │   │   ├── Notifications.vue
│   │   │   ├── AuditCenter.vue
│   │   │   └── admin/
│   │   │       ├── Dashboard.vue
│   │   │       ├── TagManage.vue
│   │   │       ├── UserManage.vue
│   │   │       ├── DeptManage.vue
│   │   │       ├── SensitiveWords.vue
│   │   │       └── Settings.vue
│   │   ├── utils/
│   │   │   ├── request.js             # Axios 封装 + 拦截器
│   │   │   └── auth.js                # Token 管理
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── knowledge-common/                  # 公共基础设施
├── knowledge-user-auth/               # 用户 + 权限
├── knowledge-content/                 # 内容发布 + 版本 + 审核 + 标签
├── knowledge-social/                  # 评论 + 群组 + 收藏
├── knowledge-notification/            # 通知
├── knowledge-search/                  # 搜索
├── knowledge-file/                    # 文件服务
├── knowledge-analytics/               # 数据分析
└── knowledge-web/                     # 启动入口 + Controller
```

---

## Phase 1: 项目脚手架与基础设施

### Task 1: 初始化前端项目

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/router/index.js`
- Create: `frontend/src/utils/request.js`
- Create: `frontend/src/utils/auth.js`

- [ ] **Step 1: 使用 Vite 创建 Vue 3 项目**

```bash
cd D:\work\code\ai_competiton\UI
npm create vite@latest frontend -- --template vue
cd frontend
npm install
```

- [ ] **Step 2: 安装依赖**

```bash
npm install element-plus @element-plus/icons-vue vue-router@4 pinia axios
npm install -D sass @vitejs/plugin-vue
```

- [ ] **Step 3: 配置 `vite.config.js`**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
```

- [ ] **Step 4: 配置路由 `src/router/index.js`**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/HomePage.vue') },
  { path: '/login', name: 'Login', component: () => import('@/views/LoginPage.vue') },
  { path: '/content/create', name: 'ContentCreate', component: () => import('@/views/ContentCreate.vue') },
  { path: '/content/:id', name: 'ContentDetail', component: () => import('@/views/ContentDetail.vue') },
  { path: '/content/:id/edit', name: 'ContentEdit', component: () => import('@/views/ContentEdit.vue') },
  { path: '/content/:id/versions', name: 'VersionHistory', component: () => import('@/views/VersionHistory.vue') },
  { path: '/templates', name: 'Templates', component: () => import('@/views/TemplateCenter.vue') },
  { path: '/groups', name: 'Groups', component: () => import('@/views/GroupList.vue') },
  { path: '/group/:id', name: 'GroupDetail', component: () => import('@/views/GroupDetail.vue') },
  { path: '/group/:id/manage', name: 'GroupManage', component: () => import('@/views/GroupManage.vue') },
  { path: '/favorites', name: 'Favorites', component: () => import('@/views/Favorites.vue') },
  { path: '/notifications', name: 'Notifications', component: () => import('@/views/Notifications.vue') },
  { path: '/audit', name: 'Audit', component: () => import('@/views/AuditCenter.vue') },
  { path: '/admin', component: () => import('@/components/layout/AdminLayout.vue'), children: [
    { path: '', redirect: '/admin/dashboard' },
    { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
    { path: 'tags', name: 'TagManage', component: () => import('@/views/admin/TagManage.vue') },
    { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue') },
    { path: 'departments', name: 'DeptManage', component: () => import('@/views/admin/DeptManage.vue') },
    { path: 'sensitive-words', name: 'SensitiveWords', component: () => import('@/views/admin/SensitiveWords.vue') },
    { path: 'settings', name: 'Settings', component: () => import('@/views/admin/Settings.vue') }
  ]}
]

export default createRouter({
  history: createWebHistory(),
  routes
})
```

- [ ] **Step 5: 封装 Axios `src/utils/request.js`**

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(config => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  response => response.data,
  error => {
    const { status } = error.response || {}
    if (status === 401) { removeToken(); window.location.href = '/login' }
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 6: Token 管理 `src/utils/auth.js`**

```javascript
const TOKEN_KEY = 'kp_token'

export function getToken() { return localStorage.getItem(TOKEN_KEY) }
export function setToken(token) { localStorage.setItem(TOKEN_KEY, token) }
export function removeToken() { localStorage.removeItem(TOKEN_KEY) }
```

- [ ] **Step 7: 配置 `main.js`**

```javascript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(ElementPlus)
app.use(createPinia())
app.use(router)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.mount('#app')
```

- [ ] **Step 8: 验证项目启动**

```bash
cd frontend && npm run dev
```
Expected: 访问 `http://localhost:3000`，显示空页面，无控制台报错

- [ ] **Step 9: Commit**

```bash
git add frontend/
git commit -m "feat: scaffold Vue 3 + Element Plus frontend project"
```

---

### Task 2: 初始化后端 Maven 多模块项目

**Files:**
- Create: `pom.xml` (root parent POM)
- Create: `knowledge-common/pom.xml`
- Create: `knowledge-user-auth/pom.xml`
- Create: `knowledge-content/pom.xml`
- Create: `knowledge-social/pom.xml`
- Create: `knowledge-notification/pom.xml`
- Create: `knowledge-search/pom.xml`
- Create: `knowledge-file/pom.xml`
- Create: `knowledge-analytics/pom.xml`
- Create: `knowledge-web/pom.xml`

- [ ] **Step 1: 创建根 `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
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

- [ ] **Step 2: 创建各子模块 `pom.xml`**

每个子模块 `pom.xml` 依赖 `knowledge-common`，并声明对应基础设施依赖：

- `knowledge-common`: spring-boot-starter-web, mybatis-plus-spring-boot3-starter, lombok, validation
- `knowledge-user-auth`: spring-boot-starter-security, jjwt, spring-boot-starter-data-redis
- `knowledge-content`: 依赖 `knowledge-file`
- `knowledge-social`: 依赖 `knowledge-content`, `knowledge-user-auth`
- `knowledge-notification`: spring-boot-starter-amqp
- `knowledge-search`: elasticsearch-java, qdrant-client
- `knowledge-file`: minio
- `knowledge-analytics`: 依赖 `knowledge-content`, `knowledge-user-auth`
- `knowledge-web`: 依赖所有业务模块

- [ ] **Step 3: 验证编译**

```bash
mvn clean compile
```
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add pom.xml knowledge-*/pom.xml
git commit -m "feat: scaffold Spring Boot DDD multi-module project"
```

---

### Task 3: 基础设施 Docker Compose 编排

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: 编写 `docker-compose.yml`**

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

  redis:
    image: redis:6-alpine
    ports: ["6379:6379"]

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

- [ ] **Step 2: 启动基础设施**

```bash
docker-compose up -d
```
Expected: 6 个容器全部 running (`docker-compose ps`)

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml
git commit -m "feat: add Docker Compose infrastructure services"
```

---

## Phase 2: 公共基础 + 用户权限模块

### Task 4: knowledge-common 公共模块

**Files:**
- Create: `knowledge-common/src/main/java/com/company/common/base/BaseEntity.java`
- Create: `knowledge-common/src/main/java/com/company/common/result/Result.java`
- Create: `knowledge-common/src/main/java/com/company/common/result/PageResult.java`
- Create: `knowledge-common/src/main/java/com/company/common/exception/BizException.java`
- Create: `knowledge-common/src/main/java/com/company/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 编写 `BaseEntity.java`**

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

- [ ] **Step 2: 编写统一响应体 `Result.java`**

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

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.code = 500; r.message = msg;
        return r;
    }
}
```

- [ ] **Step 3: 编写分页结果 `PageResult.java`**

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
}
```

- [ ] **Step 4: 编写 `BizException.java` 和 `GlobalExceptionHandler.java`**

```java
// BizException.java
package com.company.common.exception;

public class BizException extends RuntimeException {
    private int code;
    public BizException(String message) { super(message); this.code = 500; }
    public BizException(int code, String message) { super(message); this.code = code; }
    public int getCode() { return code; }
}

// GlobalExceptionHandler.java
package com.company.common.exception;

import com.company.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result<?> handleBiz(BizException e) {
        return Result.fail(e.getMessage());
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add knowledge-common/
git commit -m "feat: add common module with base entity, result wrapper, exception handling"
```

---

### Task 5: 用户认证 — 数据库表 + 实体

**Files:**
- Create: `knowledge-user-auth/src/main/resources/db/migration/V1__user_auth.sql`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/domain/model/User.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/domain/model/Role.java`

- [ ] **Step 1: 编写数据库初始化 SQL**

```sql
CREATE TABLE `user` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(256) NOT NULL,
  display_name VARCHAR(64),
  email VARCHAR(128),
  sso_id VARCHAR(128),
  department_id BIGINT,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `department` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `role` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `permission` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL
);

CREATE TABLE `user_role` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id)
);

CREATE TABLE `role_permission` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_permission (role_id, permission_id)
);

-- 初始化数据
INSERT INTO role (code, name) VALUES ('ADMIN', '系统管理员'), ('USER', '普通用户');
INSERT INTO permission (code, name) VALUES
  ('content:view', '查看内容'), ('content:create', '创建内容'),
  ('content:audit', '审核内容'), ('tag:manage', '管理标签'),
  ('user:manage', '管理用户'), ('group:approve', '审批群组'),
  ('analytics:view', '查看数据'), ('system:config', '系统配置');
```

- [ ] **Step 2: 编写实体类 `User.java`**

```java
package com.company.userauth.domain.model;

import com.company.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {
    private String username;
    private String password;
    private String displayName;
    private String email;
    private String ssoId;
    private Long departmentId;
    private String status;
}
```

- [ ] **Step 3: 编写 `Role.java`**

```java
package com.company.userauth.domain.model;

import com.company.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {
    private String code;
    private String name;
}
```

- [ ] **Step 4: Commit**

```bash
git add knowledge-user-auth/
git commit -m "feat: add user auth domain models and database schema"
```

---

### Task 6: JWT 认证 + Spring Security 配置

**Files:**
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/security/JwtUtil.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/security/JwtAuthFilter.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/security/SecurityConfig.java`

- [ ] **Step 1: 编写 `JwtUtil.java`**

```java
package com.company.userauth.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long expiration = 86400000L; // 24h

    public String generate(Long userId, String username) {
        return Jwts.builder()
            .setSubject(String.valueOf(userId))
            .claim("username", username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).getBody();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }
}
```

- [ ] **Step 2: 编写 `JwtAuthFilter.java`**

```java
package com.company.userauth.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                Long userId = jwtUtil.getUserId(token);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException ignored) {}
        }
        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 3: 编写 `SecurityConfig.java`**

```java
package com.company.userauth.infrastructure.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

- [ ] **Step 4: Commit**

```bash
git add knowledge-user-auth/
git commit -m "feat: add JWT authentication and Spring Security config"
```

---

### Task 7: 登录注册 API + 前端登录页

**Files:**
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/interfaces/controller/AuthController.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/application/service/AuthService.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/application/dto/LoginRequest.java`
- Create: `frontend/src/api/auth.js`
- Create: `frontend/src/views/LoginPage.vue`

- [ ] **Step 1: 编写登录 DTO**

```java
// LoginRequest.java
package com.company.userauth.application.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
```

- [ ] **Step 2: 编写 `AuthService.java`**

```java
package com.company.userauth.application.service;

import com.company.common.exception.BizException;
import com.company.userauth.domain.model.User;
import com.company.userauth.domain.repository.UserRepository;
import com.company.userauth.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        return jwtUtil.generate(user.getId(), user.getUsername());
    }
}
```

- [ ] **Step 3: 编写 `AuthController.java`**

```java
package com.company.userauth.interfaces.controller;

import com.company.common.result.Result;
import com.company.userauth.application.dto.LoginRequest;
import com.company.userauth.application.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        return Result.ok(Map.of("token", token));
    }
}
```

- [ ] **Step 4: 编写前端 API `src/api/auth.js`**

```javascript
import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}
```

- [ ] **Step 5: 编写前端 `LoginPage.vue`**

```vue
<template>
  <div class="login-container">
    <div class="login-card">
      <h2>&#9670; 知享 Knowledge Hub</h2>
      <p class="login-sub">企业内部知识分享平台</p>
      <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="用户名 / 工号" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" size="large" style="width:100%">
          登 录
        </el-button>
      </el-form>
      <el-divider>或</el-divider>
      <el-button size="large" style="width:100%;margin-bottom:8px" @click="ssoLogin">企业 SSO 登录 (OAuth2)</el-button>
      <el-button size="large" style="width:100%" @click="ldapLogin">LDAP 域账号登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { setToken } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  loading.value = true
  try {
    const res = await login(form)
    setToken(res.data.token)
    ElMessage.success('登录成功')
    router.push('/')
  } catch { /* 拦截器已处理 */ }
  finally { loading.value = false }
}

function ssoLogin() { window.location.href = '/api/auth/sso/redirect' }
function ldapLogin() { window.location.href = '/api/auth/ldap/redirect' }
</script>

<style scoped>
.login-container {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f2f5 100%);
}
.login-card { background: #fff; border-radius: 12px; padding: 40px; width: 400px; box-shadow: 0 4px 24px rgba(0,0,0,.06); }
.login-card h2 { font-size: 22px; text-align: center; margin: 0 0 8px; color: #303133; }
.login-sub { text-align: center; font-size: 13px; color: #909399; margin-bottom: 28px; }
</style>
```

- [ ] **Step 6: Commit**

```bash
git add knowledge-user-auth/ frontend/
git commit -m "feat: implement login API and login page"
```

---

### Task 8: 用户管理（CRUD + 角色分配）API

**Files:**
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/interfaces/controller/UserController.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/application/service/UserService.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/domain/repository/UserRepository.java`
- Create: `knowledge-user-auth/src/main/java/com/company/userauth/infrastructure/repository/UserRepositoryImpl.java`

- [ ] **Step 1: 编写 Repository 接口与实现**

```java
// domain/repository/UserRepository.java
package com.company.userauth.domain.repository;

import com.company.userauth.domain.model.User;
import java.util.List;

public interface UserRepository {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll(int page, int size);
    long count();
    void save(User user);
    void update(User user);
}

// infrastructure/repository/UserRepositoryImpl.java
package com.company.userauth.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.userauth.domain.model.User;
import com.company.userauth.domain.repository.UserRepository;
import com.company.userauth.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) { this.userMapper = userMapper; }

    @Override
    public User findById(Long id) { return userMapper.selectById(id); }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public List<User> findAll(int page, int size) {
        return userMapper.selectPage(new Page<>(page, size), null).getRecords();
    }

    @Override
    public long count() { return userMapper.selectCount(null); }

    @Override
    public void save(User user) { userMapper.insert(user); }

    @Override
    public void update(User user) { userMapper.updateById(user); }
}
```

- [ ] **Step 2: 编写 `UserService.java`**

```java
package com.company.userauth.application.service;

import com.company.common.result.PageResult;
import com.company.userauth.domain.model.User;
import com.company.userauth.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }

    public PageResult<User> list(int page, int size) {
        List<User> users = userRepository.findAll(page, size);
        PageResult<User> result = new PageResult<>();
        result.setRecords(users);
        result.setTotal(userRepository.count());
        result.setPage(page); result.setSize(size);
        return result;
    }

    public User getById(Long id) { return userRepository.findById(id); }
}
```

- [ ] **Step 3: 编写 `UserController.java`**

```java
package com.company.userauth.interfaces.controller;

import com.company.common.result.PageResult;
import com.company.common.result.Result;
import com.company.userauth.application.service.UserService;
import com.company.userauth.domain.model.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public Result<PageResult<User>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.list(page, size));
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add knowledge-user-auth/
git commit -m "feat: add user management CRUD API"
```

---

## Phase 3: 内容发布模块

### Task 9: 内容表结构 + 领域模型

**Files:**
- Create: `knowledge-content/src/main/resources/db/migration/V2__content.sql`
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/KnowledgeContent.java`
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/enums/ContentType.java`
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/enums/PublishStatus.java`

- [ ] **Step 1: 编写建表 SQL**

```sql
CREATE TABLE `knowledge_content` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(256) NOT NULL,
  body LONGTEXT,
  content_type VARCHAR(16) NOT NULL DEFAULT 'MARKDOWN',
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT NOT NULL,
  published_at DATETIME,
  is_deleted TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_created_by (created_by),
  INDEX idx_status (status),
  INDEX idx_published_at (published_at)
);

CREATE TABLE `content_group_relation` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_content_group (content_id, group_id)
);

CREATE TABLE `content_tag_relation` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  content_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_content_tag (content_id, tag_id)
);
```

- [ ] **Step 2: 编写枚举类**

```java
// ContentType.java
package com.company.content.domain.model.enums;

public enum ContentType {
    MARKDOWN, PPT_FILE, EXTERNAL_URL, INTERNAL_REF
}

// PublishStatus.java
package com.company.content.domain.model.enums;

public enum PublishStatus {
    DRAFT, PUBLISHED, DELETED, PENDING_AUDIT
}
```

- [ ] **Step 3: 编写 `KnowledgeContent.java`**

```java
package com.company.content.domain.model;

import com.company.common.base.BaseEntity;
import com.company.content.domain.model.enums.*;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_content")
public class KnowledgeContent extends BaseEntity {
    private String title;
    private String body;
    private ContentType contentType;
    private PublishStatus status;
    private Long createdBy;
    private LocalDateTime publishedAt;
    private Integer isDeleted;
}
```

- [ ] **Step 4: Commit**

```bash
git add knowledge-content/
git commit -m "feat: add content domain model and database schema"
```

---

### Task 10: 内容 CRUD + 发布 API

**Files:**
- Create: `knowledge-content/src/main/java/com/company/content/domain/repository/ContentRepository.java`
- Create: `knowledge-content/src/main/java/com/company/content/infrastructure/repository/ContentRepositoryImpl.java`
- Create: `knowledge-content/src/main/java/com/company/content/application/service/ContentService.java`
- Create: `knowledge-content/src/main/java/com/company/content/application/dto/CreateContentRequest.java`
- Create: `knowledge-content/src/main/java/com/company/content/interfaces/controller/ContentController.java`
- Create: `frontend/src/api/content.js`

- [ ] **Step 1: 编写 Repository**

```java
// ContentRepository.java
package com.company.content.domain.repository;

import com.company.content.domain.model.KnowledgeContent;
import java.util.List;

public interface ContentRepository {
    KnowledgeContent findById(Long id);
    List<KnowledgeContent> findPublished(int page, int size, String sort);
    List<KnowledgeContent> findByCreatedBy(Long userId, int page, int size);
    long countPublished();
    void save(KnowledgeContent content);
    void update(KnowledgeContent content);
    void softDelete(Long id);
}

// ContentRepositoryImpl.java
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
    private final ContentMapper contentMapper;

    public ContentRepositoryImpl(ContentMapper contentMapper) { this.contentMapper = contentMapper; }

    @Override
    public KnowledgeContent findById(Long id) { return contentMapper.selectById(id); }

    @Override
    public List<KnowledgeContent> findPublished(int page, int size, String sort) {
        LambdaQueryWrapper<KnowledgeContent> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeContent::getStatus, PublishStatus.PUBLISHED)
          .eq(KnowledgeContent::getIsDeleted, 0);
        if ("hot".equals(sort)) qw.orderByDesc(KnowledgeContent::getUpdatedAt); // 后续加阅读量
        else qw.orderByDesc(KnowledgeContent::getPublishedAt);
        return contentMapper.selectPage(new Page<>(page, size), qw).getRecords();
    }

    @Override
    public long countPublished() {
        return contentMapper.selectCount(new LambdaQueryWrapper<KnowledgeContent>()
            .eq(KnowledgeContent::getStatus, PublishStatus.PUBLISHED)
            .eq(KnowledgeContent::getIsDeleted, 0));
    }

    @Override
    public void save(KnowledgeContent content) { contentMapper.insert(content); }

    @Override
    public void update(KnowledgeContent content) { contentMapper.updateById(content); }

    @Override
    public void softDelete(Long id) {
        KnowledgeContent c = new KnowledgeContent();
        c.setId(id); c.setIsDeleted(1);
        contentMapper.updateById(c);
    }

    @Override
    public List<KnowledgeContent> findByCreatedBy(Long userId, int page, int size) { return List.of(); }
}
```

- [ ] **Step 2: 编写 `ContentService.java`**

```java
package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.*;
import com.company.content.domain.repository.ContentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ContentService {
    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public KnowledgeContent create(Long userId, CreateContentRequest req) {
        KnowledgeContent c = new KnowledgeContent();
        c.setTitle(req.getTitle()); c.setBody(req.getBody());
        c.setContentType(req.getContentType()); c.setStatus(PublishStatus.DRAFT);
        c.setCreatedBy(userId);
        contentRepository.save(c);
        return c;
    }

    public PageResult<KnowledgeContent> list(int page, int size, String sort) {
        PageResult<KnowledgeContent> r = new PageResult<>();
        r.setRecords(contentRepository.findPublished(page, size, sort));
        r.setTotal(contentRepository.countPublished());
        r.setPage(page); r.setSize(size);
        return r;
    }

    public KnowledgeContent getById(Long id) {
        KnowledgeContent c = contentRepository.findById(id);
        if (c == null || c.getIsDeleted() == 1) throw new BizException(404, "内容不存在");
        return c;
    }

    public void update(Long id, Long userId, CreateContentRequest req) {
        KnowledgeContent c = getById(id);
        if (!c.getCreatedBy().equals(userId)) throw new BizException(403, "无权编辑");
        c.setTitle(req.getTitle()); c.setBody(req.getBody());
        contentRepository.update(c);
    }

    public void publish(Long id, Long userId) {
        KnowledgeContent c = getById(id);
        if (!c.getCreatedBy().equals(userId)) throw new BizException(403, "无权操作");
        c.setStatus(PublishStatus.PUBLISHED);
        c.setPublishedAt(LocalDateTime.now());
        contentRepository.update(c);
    }

    public void delete(Long id, Long userId) {
        KnowledgeContent c = getById(id);
        if (!c.getCreatedBy().equals(userId)) throw new BizException(403, "无权删除");
        contentRepository.softDelete(id);
    }
}
```

- [ ] **Step 3: 编写 `ContentController.java`**

```java
package com.company.content.interfaces.controller;

import com.company.common.result.*;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.application.service.ContentService;
import com.company.content.domain.model.KnowledgeContent;
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
        return Result.ok(contentService.list(page, size, sort));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeContent> get(@PathVariable Long id) {
        return Result.ok(contentService.getById(id));
    }

    @PostMapping
    public Result<KnowledgeContent> create(@RequestBody CreateContentRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(contentService.create(userId, req));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CreateContentRequest req, Authentication auth) {
        contentService.update(id, (Long) auth.getPrincipal(), req);
        return Result.ok(null);
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, Authentication auth) {
        contentService.publish(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        contentService.delete(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }
}
```

- [ ] **Step 4: 编写前端 API `src/api/content.js`**

```javascript
import request from '@/utils/request'

export function getContentList(params) { return request.get('/contents', { params }) }
export function getContent(id) { return request.get(`/contents/${id}`) }
export function createContent(data) { return request.post('/contents', data) }
export function updateContent(id, data) { return request.put(`/contents/${id}`, data) }
export function deleteContent(id) { return request.delete(`/contents/${id}`) }
export function publishContent(id) { return request.post(`/contents/${id}/publish`) }
```

- [ ] **Step 5: Commit**

```bash
git add knowledge-content/ frontend/
git commit -m "feat: add content CRUD and publish API with frontend integration"
```

---

### Task 11: 首页内容列表 + 内容卡片组件

**Files:**
- Create: `frontend/src/components/content/ContentCard.vue`
- Create: `frontend/src/components/common/SortBar.vue`
- Create: `frontend/src/views/HomePage.vue`

- [ ] **Step 1: 编写 `ContentCard.vue`**

```vue
<template>
  <div class="content-card" @click="$router.push(`/content/${content.id}`)">
    <div class="card-header">
      <span class="type-icon" :class="typeClass">{{ typeLabel }}</span>
      <div>
        <h3 class="card-title">{{ content.title }}</h3>
        <div class="card-tags">
          <el-tag v-for="tag in content.tags" :key="tag.id" size="small" type="info">{{ tag.name }}</el-tag>
        </div>
      </div>
    </div>
    <p class="card-desc">{{ content.body?.replace(/[#*`]/g,'').substring(0, 150) }}...</p>
    <div class="card-meta">
      <span>{{ content.author }} · {{ content.department }}</span>
      <span>{{ formatTime(content.publishedAt) }}</span>
      <span>{{ content.viewCount || 0 }} 阅读</span>
      <span>{{ content.favoriteCount || 0 }} 收藏</span>
      <span>{{ content.commentCount || 0 }} 评论</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ content: Object })

const typeMap = { MARKDOWN: ['MD','type-md'], PPT_FILE: ['PPT','type-ppt'],
  EXTERNAL_URL: ['URL','type-link'], INTERNAL_REF: ['REF','type-ref'] }

const [typeLabel, typeClass] = computed(() => typeMap[props.content.contentType] || ['','']).value

function formatTime(t) { /* 格式化时间 */ return t || '' }
</script>

<style scoped>
.content-card { background:#fff;border-radius:8px;padding:20px 24px;border:1px solid #ebeef5;cursor:pointer;transition:box-shadow .2s;margin-bottom:12px }
.content-card:hover { box-shadow:0 2px 12px rgba(0,0,0,.06) }
.card-header { display:flex;align-items:flex-start;gap:10px;margin-bottom:10px }
.type-icon { width:32px;height:32px;border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:600;flex-shrink:0 }
.type-md { background:#ecf5ff;color:#409eff } .type-ppt { background:#fef0f0;color:#f56c6c }
.type-link { background:#f0f9eb;color:#67c23a } .type-ref { background:#fdf6ec;color:#e6a23c }
.card-title { font-size:16px;font-weight:600;color:#303133;margin:0 }
.card-tags { margin-top:6px;display:flex;gap:4px }
.card-desc { font-size:13px;color:#909399;line-height:1.5;margin:10px 0;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden }
.card-meta { font-size:12px;color:#c0c4cc;display:flex;gap:14px }
</style>
```

- [ ] **Step 2: 编写 `HomePage.vue`**

```vue
<template>
  <div class="page">
    <AppHeader />
    <div class="main">
      <aside class="sidebar">
        <h4>标签筛选</h4>
        <el-checkbox-group v-model="selectedTags" @change="fetchList">
          <el-checkbox v-for="tag in tags" :key="tag.id" :label="tag.id">{{ tag.name }} ({{ tag.count }})</el-checkbox>
        </el-checkbox-group>
      </aside>
      <div class="content-area">
        <div class="toolbar">
          <SortBar v-model="sort" @change="fetchList" />
          <span class="result-count">共 {{ total }} 条内容</span>
        </div>
        <ContentCard v-for="item in list" :key="item.id" :content="item" />
        <el-pagination v-model:current-page="page" :total="total" :page-size="size" @change="fetchList" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getContentList } from '@/api/content'
import AppHeader from '@/components/layout/AppHeader.vue'
import SortBar from '@/components/common/SortBar.vue'
import ContentCard from '@/components/content/ContentCard.vue'

const list = ref([]), total = ref(0), page = ref(1), size = ref(10), sort = ref('latest'), selectedTags = ref([]), tags = ref([])

async function fetchList() {
  const res = await getContentList({ page: page.value, size: size.value, sort: sort.value })
  list.value = res.data.records; total.value = res.data.total
}

onMounted(fetchList)
</script>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/
git commit -m "feat: add homepage content list with card component and tag filter"
```

---

## Phase 4: 群组管理模块

### Task 12: 群组表结构 + CRUD API

**Files:**
- Create: `knowledge-social/src/main/resources/db/migration/V3__group.sql`
- Create: `knowledge-social/src/main/java/com/company/social/domain/model/Group.java`
- Create: `knowledge-social/src/main/java/com/company/social/domain/repository/GroupRepository.java`
- Create: `knowledge-social/src/main/java/com/company/social/infrastructure/repository/GroupRepositoryImpl.java`
- Create: `knowledge-social/src/main/java/com/company/social/application/service/GroupService.java`
- Create: `knowledge-social/src/main/java/com/company/social/interfaces/controller/GroupController.java`

- [ ] **Step 1: 建表 SQL**

```sql
CREATE TABLE `group_info` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(512),
  owner_id BIGINT NOT NULL,
  visibility VARCHAR(16) DEFAULT 'PUBLIC',
  status VARCHAR(16) DEFAULT 'APPROVED',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `group_member` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(16) DEFAULT 'MEMBER',
  status VARCHAR(16) DEFAULT 'PENDING',
  joined_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_group_user (group_id, user_id)
);
```

- [ ] **Step 2: 编写 `GroupService.java` 核心方法**

```java
package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.domain.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    public Group create(Long userId, String name, String description) {
        Group g = new Group(); g.setName(name); g.setDescription(description);
        g.setOwnerId(userId);
        groupRepository.save(g);
        // 创建者自动成为群主
        GroupMember gm = new GroupMember(); gm.setGroupId(g.getId());
        gm.setUserId(userId); gm.setRole("OWNER"); gm.setStatus("APPROVED");
        memberRepository.save(gm);
        return g;
    }

    public List<Group> list(int page, int size) {
        return groupRepository.findAll(page, size);
    }

    public void join(Long groupId, Long userId) {
        if (memberRepository.exists(groupId, userId)) throw new BizException("已申请或已加入");
        GroupMember gm = new GroupMember();
        gm.setGroupId(groupId); gm.setUserId(userId); gm.setStatus("PENDING");
        memberRepository.save(gm);
    }

    public void approve(Long groupId, Long memberId, Long ownerId) {
        Group g = groupRepository.findById(groupId);
        if (!g.getOwnerId().equals(ownerId)) throw new BizException(403, "仅群主可审批");
        memberRepository.updateStatus(memberId, "APPROVED");
    }
}
```

- [ ] **Step 3: 编写 `GroupController.java`**

```java
@RestController
@RequestMapping("/api/groups")
public class GroupController {
    // GET /api/groups — 群组列表
    // POST /api/groups — 创建群组
    // GET /api/groups/{id} — 群组详情
    // POST /api/groups/{id}/join — 申请加入
    // PUT /api/groups/{id}/members/{userId} — 审批成员
    // DELETE /api/groups/{id}/members/{userId} — 移除成员
}
```

- [ ] **Step 4: Commit**

```bash
git add knowledge-social/
git commit -m "feat: add group management domain, API, and database"
```

---

### Task 13: 群组前端页面（列表 + 详情 + 管理）

**Files:**
- Create: `frontend/src/api/group.js`
- Create: `frontend/src/views/GroupList.vue`
- Create: `frontend/src/views/GroupDetail.vue`
- Create: `frontend/src/views/GroupManage.vue`

- [ ] **Step 1: 编写 `GroupList.vue`** — 群组卡片网格页，含搜索、创建入口、加入状态标签

- [ ] **Step 2: 编写 `GroupDetail.vue`** — 群组头部信息 + Tab(内容/成员/关于) + 内容列表 + 成员侧栏

- [ ] **Step 3: 编写 `GroupManage.vue`** — 左侧管理菜单 + 待审批表格 + 通过/拒绝操作

- [ ] **Step 4: Commit**

---

## Phase 5: 标签体系 + 搜索服务模块

### Task 14: 标签管理（管理员 CRUD + 内容打标）

**Files:**
- Create: `knowledge-content/src/main/resources/db/migration/V4__tag.sql`
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/Tag.java`
- Create: `knowledge-content/src/main/java/com/company/content/interfaces/controller/TagController.java`
- Create: `frontend/src/views/admin/TagManage.vue`

TagController 提供标准 CRUD，管理员权限校验。前端 TagManage.vue 实现彩色标签卡片网格管理。

### Task 15: Elasticsearch 全文检索 + Qdrant 向量检索 + 混合搜索

**Files:**
- Create: `knowledge-search/src/main/java/com/company/search/infrastructure/es/EsClient.java`
- Create: `knowledge-search/src/main/java/com/company/search/infrastructure/qdrant/QdrantClient.java`
- Create: `knowledge-search/src/main/java/com/company/search/application/service/SearchService.java`
- Create: `knowledge-search/src/main/java/com/company/search/interfaces/controller/SearchController.java`

- [ ] **Step 1: `SearchService` 混合检索核心逻辑**

```java
public List<Long> hybridSearch(String keyword) {
    // 1. ES 关键词检索 → List<Long> esResult
    List<Long> esResult = esClient.search(keyword);
    // 2. BGE 向量化 → Qdrant 语义检索 → List<Long> qdrantResult
    float[] vector = embeddingService.embed(keyword);
    List<Long> qdrantResult = qdrantClient.search(vector, 50);
    // 3. RRF 融合: RRF_score = Σ 1/(k+rank_i), k=60
    Map<Long, Double> scores = new HashMap<>();
    for (int i = 0; i < esResult.size(); i++) scores.merge(esResult.get(i), 1.0/(60+i+1), Double::sum);
    for (int i = 0; i < qdrantResult.size(); i++) scores.merge(qdrantResult.get(i), 1.0/(60+i+1), Double::sum);
    // 4. 按综合得分降序
    return scores.entrySet().stream()
        .sorted(Map.Entry.<Long,Double>comparingByValue().reversed())
        .map(Map.Entry::getKey).collect(Collectors.toList());
}
```

- [ ] **Step 2: `SearchController`**

```java
@GetMapping("/api/search")
public Result<List<KnowledgeContent>> search(
    @RequestParam String keyword,
    @RequestParam(required = false) List<Long> tags,
    @RequestParam(defaultValue = "relevance") String sort) {
    return Result.ok(searchService.search(keyword, tags, sort));
}
```

---

## Phase 6: 评论互动 + 收藏 + 通知模块

### Task 16: 评论系统（发布/回复/@提及/点赞/敏感词过滤）

**Files:**
- Create: `knowledge-social/src/main/resources/db/migration/V5__comment.sql`
- Create: `knowledge-social/src/main/java/com/company/social/domain/model/Comment.java`
- Create: `knowledge-social/src/main/java/com/company/social/application/service/CommentService.java`
- Create: `knowledge-social/src/main/java/com/company/social/infrastructure/sensitive/SensitiveWordFilter.java`
- Create: `knowledge-social/src/main/java/com/company/social/interfaces/controller/CommentController.java`
- Create: `frontend/src/components/comment/CommentSection.vue`
- Create: `frontend/src/components/comment/CommentItem.vue`
- Create: `frontend/src/components/comment/CommentInput.vue`

核心实现要点：
- CommentService 发布时调用 SensitiveWordFilter 检测，AC 自动机实现
- 评论结构限定二级（parentId + replyToId）
- @提及解析后发送 MQ 事件通知被 @ 用户
- CommentSection 组件支持嵌套回复、点赞、举报、排序切换

### Task 17: 收藏功能 API + 前端页面

**Files:**
- Create: `knowledge-social/src/main/java/com/company/social/domain/model/Favorite.java`
- Create: `knowledge-social/src/main/java/com/company/social/interfaces/controller/FavoriteController.java`
- Create: `frontend/src/views/Favorites.vue`

FavoriteController: POST/DELETE 收藏/取消，GET 列表 + 类型筛选，GET check 检查收藏状态

### Task 18: 通知系统（MQ 消费 + API + 前端通知中心）

**Files:**
- Create: `knowledge-notification/src/main/java/com/company/notification/domain/model/Notification.java`
- Create: `knowledge-notification/src/main/java/com/company/notification/infrastructure/mq/NotificationConsumer.java`
- Create: `knowledge-notification/src/main/java/com/company/notification/interfaces/controller/NotificationController.java`
- Create: `frontend/src/components/notification/NotificationBell.vue`
- Create: `frontend/src/views/Notifications.vue`

MQ Consumer 监听 content.event、comment.event、group.event Topics，生成对应类型 Notification。NotificationController 提供列表、未读数、标记已读、删除 API。NotificationBell 轮询未读数显示 Badge。

---

## Phase 7: 内容生产工具链模块

### Task 19: 历史版本管理（版本快照 + 差异对比 + 回滚）

**Files:**
- Create: `knowledge-content/src/main/resources/db/migration/V6__version.sql`
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/ContentVersion.java`
- Create: `knowledge-content/src/main/java/com/company/content/application/service/VersionService.java`
- Create: `knowledge-content/src/main/java/com/company/content/interfaces/controller/VersionController.java`
- Create: `frontend/src/views/VersionHistory.vue`
- Create: `frontend/src/components/version/VersionDiff.vue`

每次更新内容时自动创建版本快照。Diff 使用 diff-match-patch 库实现前端双栏对比。回滚时创建新版本记录操作。

### Task 20: 审核工作流（内容审核 + 评论审核）

**Files:**
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/AuditRecord.java`
- Create: `knowledge-content/src/main/java/com/company/content/application/service/AuditService.java`
- Create: `knowledge-content/src/main/java/com/company/content/interfaces/controller/AuditController.java`
- Create: `frontend/src/views/AuditCenter.vue`

状态流转：PENDING → APPROVED / REJECTED。驳回时必填原因。审核通过后发布 MQ 事件触发通知和索引更新。

### Task 21: 内容模板 + 定时发布

**Files:**
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/ContentTemplate.java`
- Create: `knowledge-content/src/main/java/com/company/content/domain/model/ScheduledPublish.java`
- Create: `knowledge-content/src/main/java/com/company/content/application/service/TemplateService.java`
- Create: `knowledge-content/src/main/java/com/company/content/application/service/ScheduleService.java`
- Create: `frontend/src/views/TemplateCenter.vue`

---

## Phase 8: 数据分析 + 管理后台

### Task 22: 数据分析看板（API + ECharts 图表）

**Files:**
- Create: `knowledge-analytics/src/main/resources/db/migration/V7__analytics.sql`
- Create: `knowledge-analytics/src/main/java/com/company/analytics/domain/model/UserActionLog.java`
- Create: `knowledge-analytics/src/main/java/com/company/analytics/domain/model/ContentStats.java`
- Create: `knowledge-analytics/src/main/java/com/company/analytics/application/service/AnalyticsService.java`
- Create: `knowledge-analytics/src/main/java/com/company/analytics/interfaces/controller/AnalyticsController.java`
- Create: `frontend/src/views/admin/Dashboard.vue`
- Create: `frontend/src/components/analytics/AnalyticsChart.vue`

API 提供 overview、content-trend、hot-content、hot-keywords、group-activity 五个接口。前端使用 ECharts 渲染折线图、柱状图。

### Task 23: 管理后台页面（用户/部门/敏感词/系统配置）

**Files:**
- Create: `frontend/src/views/admin/UserManage.vue`
- Create: `frontend/src/views/admin/DeptManage.vue`
- Create: `frontend/src/views/admin/SensitiveWords.vue`
- Create: `frontend/src/views/admin/Settings.vue`

---

## Phase 9: RabbitMQ 集成 + 事件驱动

### Task 24: MQ 配置 + 事件发布/消费完整链路

**Files:**
- Modify: `knowledge-common` — 添加 RabbitMQ 配置
- Modify: `knowledge-content` — 发布 ContentPublishedEvent
- Modify: `knowledge-social` — 发布 CommentCreatedEvent、GroupEvent
- Modify: `knowledge-notification` — 消费所有事件生成通知
- Modify: `knowledge-analytics` — 消费 UserActionEvent 写行为日志
- Modify: `knowledge-search` — 消费 ContentEvent 更新索引

定义 6 个 Topic（content.event、comment.event、group.event、audit.event、user.action、file.process），确认生产者/消费者映射，实现幂等消费和死信队列。

---

## Phase 10: 文件服务 + 集成测试

### Task 25: 文件上传/下载/MinIO 集成

**Files:**
- Create: `knowledge-file/src/main/java/com/company/file/application/service/FileService.java`
- Create: `knowledge-file/src/main/java/com/company/file/interfaces/controller/FileController.java`
- Create: `frontend/src/components/content/FileUploader.vue`

### Task 26: 端到端集成测试

编写前后端全链路测试用例，覆盖：
- 用户登录 → 创建内容 → 发布 → 搜索命中
- 群组创建申请 → 管理员审批 → 群主审批入群
- 评论发布 → 敏感词拦截 → 审核通过 → 通知到达
- 收藏 → 收藏列表 → 取消收藏
- 数据分析数据采集与聚合

---

## 实施顺序依赖图

```
Phase 1 (脚手架) ──── 阻塞所有后续 Phase
    │
Phase 2 (用户权限) ── 阻塞 Phase 3-8 (所有业务模块需要认证)
    │
├── Phase 3 (内容发布) ── 阻塞 Phase 5 (搜索依赖内容数据)
│       │
│   Phase 5 (搜索)
│
├── Phase 4 (群组) ── 可与 Phase 3 并行
│
├── Phase 6 (评论/收藏/通知) ── 可与 Phase 3-5 并行
│
├── Phase 7 (工具链) ── 依赖 Phase 3 (内容)、Phase 6 (评论)
│
├── Phase 8 (数据分析) ── 依赖 Phase 3 (内容)、Phase 2 (用户)
│
├── Phase 9 (MQ) ── 依赖 Phase 2-8 (各模块事件)
│
└── Phase 10 (文件/测试) ── 依赖所有 Phase
```

**建议并行组：**
- Phase 3 + Phase 4 可并行开发
- Phase 5 + Phase 6 可并行开发
- Phase 7 + Phase 8 可并行开发

---

> **总计 26 个开发任务，预计 10 个 Sprint（每个 Sprint 1-2 周），5-6 人团队约 4 个月完成全部 P0 交付。**
