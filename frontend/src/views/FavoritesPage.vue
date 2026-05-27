<template>
  <PageCard title="我的收藏">
    <template #header>
      <select class="filter-select" v-model="filterType" @change="onFilterChange">
        <option value="">全部类型</option>
        <option value="MARKDOWN">Markdown</option>
        <option value="PPT_FILE">PPT文件</option>
        <option value="EXTERNAL_URL">外部链接</option>
        <option value="INTERNAL_REF">内部引用</option>
      </select>
    </template>

    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="3" animated />
    </div>

    <el-empty v-else-if="list.length === 0" description="还没有收藏任何内容" />

    <div v-else class="fav-list">
      <div v-for="fav in list" :key="fav.id" class="content-list-item" @click="goContent(fav.contentId)">
        <div class="content-item-main">
          <div class="content-item-title">{{ fav.contentTitle || '内容 #' + fav.contentId }}</div>
          <div class="content-item-meta">
            <span v-if="fav.contentType" :class="['content-type-badge', typeBadgeClass(fav.contentType)]"><i :class="typeEmoji(fav.contentType)"></i> {{ typeLabel(fav.contentType) }}</span>
            <span><i class="ri-user-line"></i> {{ fav.authorName || '未知' }}</span>
            <span><i class="ri-star-line"></i> 收藏于 {{ formatDate(fav.createdAt) }}</span>
          </div>
        </div>
        <button class="btn btn-text" style="color:#F56C6C;" @click.stop="handleUnfavorite(fav)">取消收藏</button>
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavorites, unfavoriteContent } from '@/api/favorite'
import PageCard from '@/components/common/PageCard.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const filterType = ref('')

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterType.value) params.contentType = filterType.value
    const res = await getFavorites(params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { /* handled */ }
  finally { loading.value = false }
}

function onFilterChange() {
  page.value = 1
  fetchList()
}

function goContent(contentId) {
  router.push(`/content/${contentId}`)
}

async function handleUnfavorite(fav) {
  try {
    await unfavoriteContent(fav.id)
    ElMessage.success('已取消收藏')
    fetchList()
  } catch { /* handled */ }
}

function typeBadgeClass(type) {
  const map = { MARKDOWN: 'type-markdown', PPT_FILE: 'type-ppt', EXTERNAL_URL: 'type-link', INTERNAL_REF: 'type-ref' }
  return map[type] || ''
}

function typeEmoji(type) {
  const map = { MARKDOWN: 'ri-file-text-line', PPT_FILE: 'ri-presentation-line', EXTERNAL_URL: 'ri-link', INTERNAL_REF: 'ri-attachment-line' }
  return map[type] || 'ri-file-text-line'
}

function typeLabel(type) {
  const map = { MARKDOWN: 'Markdown', PPT_FILE: 'PPT文件', EXTERNAL_URL: '外部链接', INTERNAL_REF: '内部引用' }
  return map[type] || type
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}

onMounted(fetchList)
</script>

<style scoped>
.filter-select { height: 36px; border: 1px solid #DCDFE6; border-radius: 6px; padding: 0 12px; font-size: 13px; outline: none; cursor: pointer; }
.content-list-item { display: flex; gap: 16px; padding: 20px 0; border-bottom: 1px solid #E4E7ED; cursor: pointer; transition: background .2s; align-items: center; }
.content-list-item:hover { background: #fafbfc; margin: 0 -24px; padding: 20px 24px; }
.content-list-item:last-child { border-bottom: none; }
.content-item-main { flex: 1; min-width: 0; }
.content-item-title { font-size: 16px; font-weight: 600; margin-bottom: 6px; color: #303133; }
.content-item-title:hover { color: #409EFF; }
.content-item-meta { display: flex; align-items: center; gap: 16px; font-size: 12px; color: #909399; flex-wrap: wrap; }
.content-type-badge { display: inline-flex; align-items: center; gap: 4px; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 500; }
.type-markdown { background: #e6f7ff; color: #1890ff; }
.type-ppt { background: #fff7e6; color: #fa8c16; }
.type-link { background: #f6ffed; color: #52c41a; }
.type-ref { background: #f9f0ff; color: #722ed1; }
.btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: 6px; font-size: 13px; cursor: pointer; border: 1px solid transparent; transition: all .2s; font-family: inherit; }
.btn-text { background: transparent; border: none; color: #409EFF; padding: 4px 8px; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; }
.loading-wrapper { padding: 20px 0; }
</style>
