<template>
  <div class="home-page">
    <PageCard title="知识内容流">
      <template #header>
        <el-button type="primary" size="small" @click="$router.push('/content/create')">
          + 创建内容
        </el-button>
      </template>

      <div class="filter-bar">
        <select class="filter-select" v-model="filterType" @change="onFilterChange">
          <option value="">全部类型</option>
          <option value="MARKDOWN">Markdown</option>
          <option value="PPT_FILE">PPT文件</option>
          <option value="EXTERNAL_URL">外部链接</option>
          <option value="INTERNAL_REF">内部引用</option>
        </select>
        <select class="filter-select" v-model="filterGroup" @change="onFilterChange">
          <option value="">全部群组</option>
          <option v-for="g in groups" :key="g.id" :value="g.id">{{ g.name }}</option>
        </select>
        <select class="filter-select" v-model="sort" @change="fetchList">
          <option value="latest">按发布时间</option>
          <option value="hot">按热度</option>
          <option value="relevance">按相关性</option>
        </select>
        <span
          v-for="tag in selectedTagList"
          :key="tag.id"
          class="tag tag-sm active"
          :style="{ background: tag.color }"
          @click="toggleTag(tag)"
        >{{ tag.name }}</span>
        <span
          v-for="tag in availableTags.slice(0, 6)"
          :key="tag.id"
          class="tag tag-sm outline"
          :style="{ color: tag.color, borderColor: tag.color }"
          @click="toggleTag(tag)"
        >{{ tag.name }}</span>
      </div>

      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="3" animated />
      </div>

      <el-empty v-else-if="list.length === 0" description="暂无内容，去写第一篇文章吧" />

      <div v-else class="content-list">
        <div v-for="item in list" :key="item.id" class="content-list-item" @click="$router.push(`/content/${item.id}`)">
          <div class="content-item-main">
            <div class="content-item-title">{{ item.title }}</div>
            <div class="content-item-desc">{{ item.summary || item.title }}</div>
            <div class="content-item-meta">
              <span :class="['content-type-badge', typeBadgeClass(item.contentType)]">{{ typeEmoji(item.contentType) }} {{ typeLabel(item.contentType) }}</span>
              <el-tooltip content="作者" placement="top">
                <span>👤 {{ item.createdBy || '匿名' }}</span>
              </el-tooltip>
              <el-tooltip :content="formatFullDate(item.publishedAt || item.createdAt)" placement="top">
                <span>📅 {{ formatDate(item.publishedAt || item.createdAt) }}</span>
              </el-tooltip>
              <el-tooltip :content="String(item.viewCount || 0) + ' 次阅读'" placement="top">
                <span>👁️ {{ formatCount(item.viewCount) }}</span>
              </el-tooltip>
              <el-tooltip :content="String(item.favoriteCount || 0) + ' 人收藏'" placement="top">
                <span>⭐ {{ item.favoriteCount || 0 }}</span>
              </el-tooltip>
              <el-tooltip :content="String(item.commentCount || 0) + ' 条评论'" placement="top">
                <span>💬 {{ item.commentCount || 0 }}</span>
              </el-tooltip>
              <el-tooltip v-for="t in (item.tags || []).slice(0, 2)" :key="t.id" :content="t.name" placement="top">
                <span class="tag tag-sm" :style="{ background: t.color || '#409EFF' }">{{ t.name }}</span>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > size" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :total="total"
          :page-size="size"
          @current-change="fetchList"
          background
          layout="prev, pager, next"
        />
      </div>
    </PageCard>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getContentList } from '@/api/content'
import { getGroupList } from '@/api/group'
import PageCard from '@/components/common/PageCard.vue'
import { getTags } from '@/api/tag'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const sort = ref('latest')
const filterType = ref('')
const filterGroup = ref('')
const groups = ref([])
const tags = ref([])
const selectedTagIds = ref([])
const route = useRoute()

const selectedTagList = computed(() => tags.value.filter(t => selectedTagIds.value.includes(t.id)))
const availableTags = computed(() => tags.value.filter(t => !selectedTagIds.value.includes(t.id)))

function typeLabel(type) {
  return { MARKDOWN: 'Markdown', PPT_FILE: 'PPT文件', EXTERNAL_URL: '外部链接', INTERNAL_REF: '内部引用' }[type] || type
}
function typeEmoji(type) {
  return { MARKDOWN: '📝', PPT_FILE: '📊', EXTERNAL_URL: '🔗', INTERNAL_REF: '📎' }[type] || '📝'
}
function typeBadgeClass(type) {
  return { MARKDOWN: 'type-markdown', PPT_FILE: 'type-ppt', EXTERNAL_URL: 'type-link', INTERNAL_REF: 'type-ref' }[type] || 'type-markdown'
}
function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
function formatFullDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
function formatCount(n) {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, sort: sort.value }
    if (filterType.value) params.contentType = filterType.value
    if (filterGroup.value) params.groupId = filterGroup.value
    if (selectedTagIds.value.length > 0) params.tagIds = selectedTagIds.value.join(',')
    if (route.query.q) params.keyword = route.query.q
    const res = await getContentList(params)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onFilterChange() {
  page.value = 1
  fetchList()
}

function toggleTag(tag) {
  const idx = selectedTagIds.value.indexOf(tag.id)
  if (idx >= 0) selectedTagIds.value.splice(idx, 1)
  else selectedTagIds.value.push(tag.id)
  page.value = 1
  fetchList()
}

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

onMounted(async () => {
  await Promise.all([fetchList(), fetchFilters()])
})

watch(() => route.query.q, () => {
  page.value = 1
  fetchList()
})
</script>

<style scoped>
.home-page { max-width: 960px; margin: 0 auto; }
.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter-select {
  height: 36px;
  border: 1px solid #DCDFE6;
  border-radius: 6px;
  padding: 0 12px;
  font-size: 13px;
  outline: none;
  background: #fff;
  cursor: pointer;
}
.filter-select:focus { border-color: #409EFF; }

.content-list-item {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid #E4E7ED;
  cursor: pointer;
  transition: background .2s;
}
.content-list-item:hover { background: #fafbfc; margin: 0 -24px; padding: 20px 24px; }
.content-list-item:last-child { border-bottom: none; }
.content-item-main { flex: 1; min-width: 0; }
.content-item-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #303133;
}
.content-item-title:hover { color: #409EFF; }
.content-item-desc {
  color: #606266;
  font-size: 13px;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.content-item-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}
.content-item-meta .el-tooltip {
  cursor: help;
}
.content-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}
.type-markdown { background: #e6f7ff; color: #1890ff; }
.type-ppt { background: #fff7e6; color: #fa8c16; }
.type-link { background: #f6ffed; color: #52c41a; }
.type-ref { background: #f9f0ff; color: #722ed1; }
.tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
  margin: 0;
}
.tag.outline { background: transparent; border: 1px solid; }
.tag.sm { padding: 1px 6px; font-size: 11px; }
.loading-wrapper { padding: 40px 0; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
</style>
