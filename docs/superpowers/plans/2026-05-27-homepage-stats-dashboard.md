# Homepage Stats Dashboard Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 5 gradient stat cards above the homepage content list showing total content, users, groups, today's content, and total comments.

**Architecture:** New `StatsController` in `knowledge-web` queries existing MyBatis-Plus mappers from sub-modules via constructor injection. New `stats.js` API wrapper fetches the endpoint. `HomePage.vue` renders a flex row of 5 gradient-background cards above the filter bar, each with an emoji icon, formatted number, and label.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3 Composition API, Element Plus

---

### Task 1: Backend — StatsController

**Files:**
- Create: `knowledge-web/src/main/java/com/company/web/controller/StatsController.java`

- [ ] **Step 1: Create StatsController**

Write `knowledge-web/src/main/java/com/company/web/controller/StatsController.java`:

```java
package com.company.web.controller;

import com.company.common.result.Result;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.social.infrastructure.mapper.CommentMapper;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.userauth.infrastructure.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final GroupMapper groupMapper;
    private final CommentMapper commentMapper;

    public StatsController(ContentMapper contentMapper, UserMapper userMapper,
                           GroupMapper groupMapper, CommentMapper commentMapper) {
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.groupMapper = groupMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        long totalContents = contentMapper.selectCount(null);
        long totalUsers = userMapper.selectCount(null);
        long totalGroups = groupMapper.selectCount(null);
        long totalComments = commentMapper.selectCount(null);
        long todayContents = contentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                        com.company.content.domain.model.KnowledgeContent>()
                        .ge(com.company.content.domain.model.KnowledgeContent::getCreatedAt,
                                LocalDate.now().atStartOfDay())
        );

        return Result.ok(Map.of(
                "totalContents", totalContents,
                "totalUsers", totalUsers,
                "totalGroups", totalGroups,
                "todayContents", todayContents,
                "totalComments", totalComments
        ));
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
mvn compile -pl knowledge-web -q
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add knowledge-web/src/main/java/com/company/web/controller/StatsController.java
git commit -m "feat: add StatsController with GET /api/stats/overview endpoint"
```

---

### Task 2: Frontend — API wrapper

**Files:**
- Create: `frontend/src/api/stats.js`

- [ ] **Step 1: Create stats API wrapper**

Write `frontend/src/api/stats.js`:

```js
import request from '@/utils/request'

export function getStatsOverview() {
  return request.get('/stats/overview')
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/stats.js
git commit -m "feat: add stats API wrapper for homepage dashboard"
```

---

### Task 3: Frontend — HomePage.vue stat cards

**Files:**
- Modify: `frontend/src/views/HomePage.vue`

- [ ] **Step 1: Add template section for stat cards**

Insert the stat cards row between the opening `<div class="home-page">` and `<PageCard>`. Locate line 2:

```html
  <div class="home-page">
```

Insert after it:

```html
    <div v-if="stats" class="stats-row">
      <div class="stat-card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
        <div class="stat-icon">📄</div>
        <div class="stat-num">{{ formatLargeNum(stats.totalContents) }}</div>
        <div class="stat-label">总内容</div>
      </div>
      <div class="stat-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
        <div class="stat-icon">👥</div>
        <div class="stat-num">{{ formatLargeNum(stats.totalUsers) }}</div>
        <div class="stat-label">总用户</div>
      </div>
      <div class="stat-card" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
        <div class="stat-icon">👥</div>
        <div class="stat-num">{{ formatLargeNum(stats.totalGroups) }}</div>
        <div class="stat-label">群组数</div>
      </div>
      <div class="stat-card" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
        <div class="stat-icon">🔥</div>
        <div class="stat-num">{{ formatLargeNum(stats.todayContents) }}</div>
        <div class="stat-label">今日发布</div>
      </div>
      <div class="stat-card" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
        <div class="stat-icon">💬</div>
        <div class="stat-num">{{ formatLargeNum(stats.totalComments) }}</div>
        <div class="stat-label">总评论</div>
      </div>
    </div>
```

- [ ] **Step 2: Add script imports and logic**

Add the stats API import (insert after line 99):

```js
import { getStatsOverview } from '@/api/stats'
```

Add the stats ref and formatter (insert after line 112 `const route = useRoute()`):

```js
const stats = ref(null)

function formatLargeNum(n) {
  if (n == null) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}
```

Add `fetchStats` to `onMounted` (modify line 185-187):

```js
onMounted(async () => {
  await Promise.all([fetchList(), fetchFilters(), fetchStats()])
})
```

Add `fetchStats` function (insert before `onMounted`):

```js
async function fetchStats() {
  try {
    const res = await getStatsOverview()
    stats.value = res.data
  } catch { /* stats are optional, don't block page */ }
}
```

- [ ] **Step 3: Add styles**

Insert before the closing `</style>` tag:

```css
.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.stat-card {
  flex: 1;
  border-radius: 10px;
  padding: 18px 12px;
  text-align: center;
  color: #fff;
  min-width: 0;
}
.stat-icon {
  font-size: 22px;
  margin-bottom: 6px;
}
.stat-num {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
}
.stat-label {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .stats-row {
    flex-wrap: wrap;
  }
  .stat-card {
    flex: 1 1 calc(50% - 6px);
    min-width: calc(50% - 6px);
  }
}
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/HomePage.vue
git commit -m "feat: add 5 gradient stat cards to homepage"
```
