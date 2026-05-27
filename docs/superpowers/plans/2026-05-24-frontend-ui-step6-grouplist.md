# Step 6: GroupList Enhancements Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add member count, content count, join/joined button states, and search input to GroupList page.

**Architecture:** Modify the existing GroupList.vue to add missing UI elements. Cards already show owner ID - enhance to show counts and join state.

**Tech Stack:** Vue 3 + Element Plus

---

### Task 1: Enhance GroupList

**Files:**
- Modify: `frontend/src/views/GroupList.vue`

- [ ] **Step 1: Read current GroupList.vue, then apply targeted edits**

The group card currently shows name, description, ownerId. It needs:
1. Search input in filter bar
2. Member count and content count on cards
3. Join/joined button state

Replace the card content section with enhanced version. The key changes are:
- Add `el-input` search above the grid
- Show `{{ g.memberCount || 0 }} 成员 · {{ g.contentCount || 0 }} 内容` in card meta
- Show `申请加入` or `已加入` button based on `g.joined` flag (default false if not present)
- Add `searchKeyword` ref and filter the group list client-side OR via API param

Do NOT rewrite the entire file. Make targeted edits:
1. Add search input before the grid
2. Add member/content counts to the card template
3. Add join state to the button
4. Add `searchKeyword` ref and computed `filteredGroups`
5. Add search CSS

Build and commit.

- [ ] **Step 2: Verify build**

```bash
cd frontend && npm run build
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/GroupList.vue
git commit -m "feat: add member/content counts, join state, and search to GroupList"
```
