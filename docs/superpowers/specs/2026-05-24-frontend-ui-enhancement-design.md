# Frontend UI Enhancement Design

> 2026-05-24 | Status: Design Approved

## Goal

Bring the Vue 3 frontend to full parity with the UI prototype at `ui/index.html`.

## Architecture

### Tech Stack (unchanged)

- Vue 3 Composition API (`<script setup>`)
- Element Plus component library
- Pinia state management
- Axios HTTP (existing `utils/request.js` with JWT interceptor)

### Component Strategy

Existing reusable components remain:
- `CommentSection.vue` — full-featured, needs wiring into ContentDetail
- `TagSelector.vue` — needs wiring into ContentCreate
- `ContentCard.vue` — needs extra fields for view/star/comment counts
- `SortBar.vue` — works, needs no changes
- `AppSidebar.vue` — add missing menu items + routes
- `AppHeader.vue` — add notification bell badge + user dropdown

New components for extracted patterns:
- `MarkdownEditor.vue` — toolbar + textarea, reused by ContentCreate and ContentEdit
- `StatCard.vue` — reused in AnalyticsDashboard
- `RankingList.vue` — reused for hot keywords, popular content, group activity

### Layout Strategy

Two existing layouts, no new ones needed:
- **Default layout** (AppHeader) — non-admin pages
- **AdminLayout** (AppSidebar + AppHeader + `<slot>`) — admin pages

### Data Flow

All pages follow the existing pattern:
```
View.vue → api/*.js → Axios (request.js with JWT) → Spring Boot API
```

No new Pinia stores needed. Each view manages its own reactive state via `ref()`/`reactive()`.

## Implementation Order (9 steps)

### Tier 1: Core Content Flow

| Step | Page | Key Changes |
|------|------|-------------|
| 1 | ContentDetail | Replace placeholder. Add: title, type badge, tags, meta (author/date/views), action buttons (fav/share/download/edit), markdown body, CommentSection integration |
| 2 | ContentCreate | Replace placeholder. Add: type selector buttons, title input, MarkdownEditor (toolbar + textarea), TagSelector, group checkboxes, datetime-local input, save draft + submit buttons |
| 3 | ContentEdit | Replace placeholder. Reuse ContentCreate form structure with pre-filled data from API |
| 4 | HomePage | Add: group filter dropdown, tag filter chips (toggleable, from API tags), view/star/comment counts on ContentCard |

### Tier 2: Social & Interaction

| Step | Page | Key Changes |
|------|------|-------------|
| 5 | Templates | New page + route. 3-column grid of template cards, each with name/description/icon + "use template" button |
| 6 | GroupList | Add: member count, content count, join/joined button states, search input |
| 7 | Favorites + Notifications | Favorites: type filter dropdown, display title + type badge. Notifications: 4 tabs (all/reply/group/system), colored type icons |

### Tier 3: Admin Pages

| Step | Page | Key Changes |
|------|------|-------------|
| 8 | Audit + Tag + SensitiveWord + Analytics | Audit: type filter, missing columns. Tag: usage count column. SensitiveWord: filter bar. Analytics: 4 stat cards, group ranking |
| 9 | Users + Departments + Settings | 3 new pages with routes, tables, and basic CRUD. UserManagement: user table + role edit. DepartmentManagement: tree table. Settings: basic config form |

## Error Handling

- Each view handles loading state (v-loading or skeleton)
- Empty states for lists (el-empty)
- API errors surfaced via ElMessage.error
- Form validation via Element Plus form rules

## Testing

- Manual verification via `gstack` browser testing
- Verify each page against `ui/index.html` design
- Test responsive layout at 1200px breakpoint
