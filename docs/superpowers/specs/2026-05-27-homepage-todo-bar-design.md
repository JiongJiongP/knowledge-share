# Homepage Todo Bar Design

**Date:** 2026-05-27
**Status:** Approved

## Goal

Add a compact todo bar on the homepage showing pending item counts with quick-entry links. Users can see what needs their attention at a glance and jump directly to the relevant page.

## Decisions

| Aspect | Choice | Rationale |
|--------|--------|-----------|
| Position | Compact bar between stats cards and filter bar | Minimal space, mobile-friendly, always visible |
| Items | 4 pills: drafts, pending approvals, audit queue, unread notifications | Covers all roles (user, group owner, admin) |
| Role handling | Non-applicable items return 0, hidden from UI | Clean UX without role-specific rendering complexity |
| Click behavior | Each pill navigates to relevant page | Quick entry, not just display |
| Zero count | Pill hidden when count is 0 | Avoid showing irrelevant items |
| Data source | New `/api/todo/counts` endpoint, with existing unread-count API | Single round-trip for all counts |

## Page Layout

```
┌── Stats Cards (5 columns) ──────────────────────────┐
├── Todo Bar (pill row) ──────────────────────────────┤  ← NEW
│  📝 草稿 3    👥 入群审批 5    ✅ 审核 12    🔔 通知 8  │
├── Filter Bar (type/group/sort/tags) ────────────────┤
├── Content List ..................................... │
├── Pagination ..........................................│
└──────────────────────────────────────────────────────┘
```

## Todo Items

| Item | Data Source | Link Target | Visible To |
|------|------------|-------------|------------|
| 📝 草稿 | `content where status='DRAFT' and created_by=current_user` | `/content/drafts` or filter on homepage | All users |
| 👥 入群审批 | `group_member where status='PENDING' and group_id in (groups I own)` | `/group/{id}/manage` (first group with pending) | Group owners |
| ✅ 审核 | `audit_record where status='PENDING'` (only if admin) | `/admin/audit` | Admin |
| 🔔 通知 | `/api/notifications/unread-count` (existing API) | `/notifications` | All users |

## Backend API

```
GET /api/todo/counts

Response 200:
{
  "code": 200,
  "data": {
    "draftCount": 3,
    "pendingApprovalCount": 5,
    "pendingAuditCount": 12,
    "unreadNotificationCount": 8
  }
}
```

### Implementation

- Controller: `TodoController.java` in `knowledge-web` module
- Service: `TodoService.java` — queries ContentMapper (drafts), GroupMapper + GroupMemberMapper (pending approvals), AuditRecordMapper (audit queue)
- Input: `Authentication auth` for current user ID and roles
- Role check: use `auth.getAuthorities()` to determine if user is admin; if not, set `pendingAuditCount = 0`

## Frontend Changes

### New file

- `frontend/src/api/todo.js` — API wrapper

### Modified file

- `frontend/src/views/HomePage.vue` — add todo bar between stats-row and PageCard

### Component

Rendered inline in HomePage.vue. Each pill:
```html
<div v-if="todo.draftCount > 0" class="todo-pill" @click="$router.push('/content/drafts')">
  <span>📝</span> 草稿 <strong>{{ todo.draftCount }}</strong>
</div>
```

### Styles

- `.todo-bar`: flex row, gap 8px, padding 10px 14px, light gray background, rounded
- `.todo-pill`: inline-flex, pill shape (border-radius 14px), colored background per type, cursor pointer

## Out of Scope

- Real-time updates (refresh on page load only)
- Detailed todo list inline (just counts + links)
- Marking items as done from homepage
- Drafts list page (link target `/content/drafts` assumed to exist or handled by content list filter)
