# Homepage Todo Bar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a compact todo pill bar between stats cards and filter bar on the homepage, showing draft count, pending approvals, audit queue, and unread notifications.

**Architecture:** New `TodoController` + `TodoService` in `knowledge-web` module aggregate counts from content/social sub-module mappers. New `todo.js` API wrapper fetches counts. `HomePage.vue` renders a flex row of colored pills that link to relevant pages.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue 3 Composition API

---

### Task 1: Backend — TodoController + TodoService

**Files:**
- Create: `knowledge-web/src/main/java/com/company/web/service/TodoService.java`
- Create: `knowledge-web/src/main/java/com/company/web/controller/TodoController.java`

- [ ] **Step 1: Create TodoService**

Write `knowledge-web/src/main/java/com/company/web/service/TodoService.java`:

```java
package com.company.web.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.infrastructure.mapper.AuditRecordMapper;
import com.company.content.infrastructure.mapper.ContentMapper;
import com.company.content.domain.model.AuditRecord;
import com.company.social.domain.model.Group;
import com.company.social.domain.model.GroupMember;
import com.company.social.infrastructure.mapper.GroupMapper;
import com.company.social.infrastructure.mapper.GroupMemberMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TodoService {

    private final ContentMapper contentMapper;
    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final AuditRecordMapper auditRecordMapper;

    public TodoService(ContentMapper contentMapper, GroupMapper groupMapper,
                       GroupMemberMapper groupMemberMapper, AuditRecordMapper auditRecordMapper) {
        this.contentMapper = contentMapper;
        this.groupMapper = groupMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.auditRecordMapper = auditRecordMapper;
    }

    public Map<String, Object> getCounts(Long userId, boolean isAdmin) {
        // 1. My drafts
        long draftCount = contentMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeContent>()
                        .eq(KnowledgeContent::getCreatedBy, userId)
                        .eq(KnowledgeContent::getStatus, PublishStatus.DRAFT)
        );

        // 2. Pending group join approvals (groups I own)
        long pendingApprovalCount = 0;
        var myGroups = groupMapper.selectList(
                new LambdaQueryWrapper<Group>()
                        .eq(Group::getOwnerId, userId)
        );
        if (!myGroups.isEmpty()) {
            var groupIds = myGroups.stream().map(Group::getId).toList();
            pendingApprovalCount = groupMemberMapper.selectCount(
                    new LambdaQueryWrapper<GroupMember>()
                            .in(GroupMember::getGroupId, groupIds)
                            .eq(GroupMember::getStatus, "PENDING")
            );
        }

        // 3. Pending audit (admin only)
        long pendingAuditCount = 0;
        if (isAdmin) {
            pendingAuditCount = auditRecordMapper.selectCount(
                    new LambdaQueryWrapper<AuditRecord>()
                            .eq(AuditRecord::getStatus, "PENDING")
            );
        }

        return Map.of(
                "draftCount", draftCount,
                "pendingApprovalCount", pendingApprovalCount,
                "pendingAuditCount", pendingAuditCount
        );
    }
}
```

- [ ] **Step 2: Create TodoController**

Write `knowledge-web/src/main/java/com/company/web/controller/TodoController.java`:

```java
package com.company.web.controller;

import com.company.common.result.Result;
import com.company.web.service.TodoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/counts")
    public Result<Map<String, Object>> counts(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return Result.ok(todoService.getCounts(userId, isAdmin));
    }
}
```

- [ ] **Step 3: Verify entity has PublishStatus enum accessible**

The plan uses `KnowledgeContent.PublishStatus.DRAFT`. Verify this enum is public. The `PublishStatus` enum at `com.company.content.domain.model.enums.PublishStatus` has values `DRAFT` and `PUBLISHED`. In `KnowledgeContent`, the status field type is `PublishStatus`. If the field is stored as `PublishStatus` enum, the LambdaQueryWrapper should compare with `KnowledgeContent::getStatus` directly and use `.eq(KnowledgeContent::getStatus, PublishStatus.DRAFT)`.

Adjust TodoService if needed to import `com.company.content.domain.model.enums.PublishStatus` and use `PublishStatus.DRAFT` directly.

- [ ] **Step 4: Verify compilation**

```bash
mvn compile -pl knowledge-web -q
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add knowledge-web/src/main/java/com/company/web/service/TodoService.java knowledge-web/src/main/java/com/company/web/controller/TodoController.java
git commit -m "feat: add TodoController + TodoService with GET /api/todo/counts"
```

---

### Task 2: Frontend — API wrapper

**Files:**
- Create: `frontend/src/api/todo.js`

- [ ] **Step 1: Create todo API wrapper**

Write `frontend/src/api/todo.js`:

```js
import request from '@/utils/request'

export function getTodoCounts() {
  return request.get('/todo/counts')
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/api/todo.js
git commit -m "feat: add todo API wrapper for homepage"
```

---

### Task 3: Frontend — HomePage.vue todo bar

**Files:**
- Modify: `frontend/src/views/HomePage.vue`

- [ ] **Step 1: Add todo bar template between stats placeholder and PageCard**

Read HomePage.vue to find the exact location. The todo bar goes after the stats placeholder div (the `v-else` block ending around line 36) and before `<PageCard>` (line 37).

Insert:

```html
    <div v-if="todo" class="todo-bar">
      <div v-if="todo.draftCount > 0" class="todo-pill todo-pill-draft" @click="$router.push('/content/create')">
        <span>📝</span> 草稿 <strong>{{ todo.draftCount }}</strong>
      </div>
      <div v-if="todo.pendingApprovalCount > 0" class="todo-pill todo-pill-approval" @click="$router.push('/group/1/manage')">
        <span>👥</span> 入群审批 <strong>{{ todo.pendingApprovalCount }}</strong>
      </div>
      <div v-if="todo.pendingAuditCount > 0" class="todo-pill todo-pill-audit" @click="$router.push('/admin/audit')">
        <span>✅</span> 审核 <strong>{{ todo.pendingAuditCount }}</strong>
      </div>
      <div v-if="todo.unreadCount > 0" class="todo-pill todo-pill-notify" @click="$router.push('/notifications')">
        <span>🔔</span> 通知 <strong>{{ todo.unreadCount }}</strong>
      </div>
    </div>
```

- [ ] **Step 2: Add script imports and logic**

Add the import (next to other API imports):
```js
import { getTodoCounts } from '@/api/todo'
```

Add the todo ref (after stats ref):
```js
const todo = ref(null)
```

Note: also import `getUnreadCount` if not already imported. Check if it's imported from `@/api/notification`. If not, add:
```js
import { getUnreadCount } from '@/api/notification'
```

Add `fetchTodo` function (before `onMounted`):
```js
async function fetchTodo() {
  try {
    const [todoRes, notifyRes] = await Promise.all([
      getTodoCounts(),
      getUnreadCount()
    ])
    todo.value = {
      ...todoRes.data,
      unreadCount: notifyRes.data?.count || 0
    }
  } catch { /* todo is optional */ }
}
```

Update `onMounted` to include `fetchTodo`:
```js
onMounted(async () => {
  await Promise.all([fetchList(), fetchFilters(), fetchStats(), fetchTodo()])
})
```

- [ ] **Step 3: Add styles**

Insert before `</style>`:

```css
.todo-bar {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #E4E7ED;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.todo-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 14px;
  font-size: 12px;
  cursor: pointer;
  transition: opacity .2s;
  white-space: nowrap;
}
.todo-pill:hover { opacity: 0.8; }
.todo-pill strong { font-size: 13px; }
.todo-pill-draft { background: #fff7e6; color: #E6A23C; }
.todo-pill-approval { background: #ecf5ff; color: #409EFF; }
.todo-pill-audit { background: #fef0f0; color: #F56C6C; }
.todo-pill-notify { background: #f0f9eb; color: #67C23A; }
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/HomePage.vue
git commit -m "feat: add todo pill bar to homepage"
```
