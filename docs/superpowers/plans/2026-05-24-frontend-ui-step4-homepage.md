# Step 4: HomePage Enhancements Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add group filter dropdown, tag filter chips, and view/star/comment counts to HomePage and ContentCard.

**Architecture:** HomePage fetches groups and tags on mount for filter UI. ContentCard gains optional stat display. Filter state triggers list refresh.

**Tech Stack:** Vue 3 + Element Plus

---

## File Structure

| Action | File | Purpose |
|--------|------|---------|
| Modify | `frontend/src/views/HomePage.vue` | Add group dropdown + tag chips |
| Modify | `frontend/src/components/content/ContentCard.vue` | Add view/star/comment counts |

---

### Task 1: Enhance HomePage + ContentCard

**Files:**
- Modify: `frontend/src/views/HomePage.vue`
- Modify: `frontend/src/components/content/ContentCard.vue`

- [ ] **Step 1: Update ContentCard to show stats**

Replace the card-footer in ContentCard.vue:

```html
<div class="card-footer">
  <span class="author">{{ content.createdBy }}</span>
  <span class="time">{{ formatTime(content.publishedAt) }}</span>
</div>
```

With:

```html
<div class="card-footer">
  <div class="footer-left">
    <span class="author">{{ content.createdBy }}</span>
    <span class="time">{{ formatTime(content.publishedAt) }}</span>
  </div>
  <div class="footer-stats">
    <span class="stat-item" v-if="content.viewCount != null">👁️ {{ content.viewCount }}</span>
    <span class="stat-item" v-if="content.favoriteCount != null">⭐ {{ content.favoriteCount }}</span>
    <span class="stat-item" v-if="content.commentCount != null">💬 {{ content.commentCount }}</span>
  </div>
</div>
```

And add to the style section:

```css
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  font-size: 12px;
  color: #c0c4cc;
  flex-wrap: wrap;
  gap: 8px;
}
.footer-left {
  display: flex;
  gap: 12px;
}
.footer-stats {
  display: flex;
  gap: 12px;
}
.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: #909399;
}
```

- [ ] **Step 2: Update HomePage to add group dropdown, tag chips, and sidebar improvements**

Replace the sidebar section with:

```html
<aside class="sidebar">
  <div class="sidebar-card">
    <h4>内容类型</h4>
    <div class="type-filter">
      <el-radio-group v-model="filterType" @change="fetchList" size="small">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="MARKDOWN">文章</el-radio-button>
        <el-radio-button value="PPT_FILE">PPT</el-radio-button>
        <el-radio-button value="EXTERNAL_URL">链接</el-radio-button>
      </el-radio-group>
    </div>
  </div>
  <div class="sidebar-card">
    <h4>群组筛选</h4>
    <el-select v-model="filterGroup" placeholder="全部群组" size="small" @change="fetchList" clearable style="width:100%">
      <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
    </el-select>
  </div>
</aside>
```

Add tag filter chips above the content list in the main area, after the toolbar:

```html
<div v-if="tags.length > 0" class="tag-chips">
  <span
    v-for="tag in selectedTags"
    :key="tag.id"
    class="tag-chip active"
    :style="{ background: tag.color, color: '#fff', cursor: 'pointer' }"
    @click="toggleTag(tag)"
  >{{ tag.name }}</span>
  <span
    v-for="tag in availableTags"
    :key="tag.id"
    class="tag-chip"
    :style="{ borderColor: tag.color, color: tag.color, cursor: 'pointer' }"
    @click="toggleTag(tag)"
  >{{ tag.name }}</span>
</div>
```

Update script setup - add new imports and state:

```js
import { getGroupList } from '@/api/group'
import { getTags } from '@/api/tag'

// Add new refs
const filterGroup = ref('')
const groups = ref([])
const tags = ref([])
const selectedTagIds = ref([])

// Computed
const selectedTags = computed(() => tags.value.filter(t => selectedTagIds.value.includes(t.id)))
const availableTags = computed(() => tags.value.filter(t => !selectedTagIds.value.includes(t.id)))

function toggleTag(tag) {
  const idx = selectedTagIds.value.indexOf(tag.id)
  if (idx >= 0) {
    selectedTagIds.value.splice(idx, 1)
  } else {
    selectedTagIds.value.push(tag.id)
  }
  page.value = 1
  fetchList()
}
```

Update fetchList to include new params:

```js
async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, sort: sort.value }
    if (filterType.value) params.contentType = filterType.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterGroup.value) params.groupId = filterGroup.value
    if (selectedTagIds.value.length > 0) params.tagIds = selectedTagIds.value.join(',')
    const res = await getContentList(params)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
```

Update onMounted to also fetch groups and tags:

```js
onMounted(async () => {
  await Promise.all([fetchList(), fetchFilters()])
})

async function fetchFilters() {
  try {
    const [groupRes, tagRes] = await Promise.all([
      getGroupList({ size: 100 }),
      getTags()
    ])
    groups.value = groupRes.data?.records || groupRes.data || []
    tags.value = tagRes.data || []
  } catch { /* filters are optional */ }
}
```

Add tag chip styles to the style section:

```css
.tag-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid;
  background: transparent;
  transition: all .2s;
}
.tag-chip.active {
  color: #fff;
}
.tag-chip:hover {
  opacity: 0.8;
}
```

- [ ] **Step 3: Verify build**

```bash
cd frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/HomePage.vue frontend/src/components/content/ContentCard.vue
git commit -m "feat: add group filter, tag chips, and stat counters to HomePage"
```
