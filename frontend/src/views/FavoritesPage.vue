<template>
  <div class="favorites-page">
    <AppHeader>
      <template #center>
        <span class="page-title">我的收藏</span>
      </template>
    </AppHeader>

    <div class="main-layout">
      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-if="!loading" class="filter-bar">
        <el-select v-model="filterType" placeholder="全部类型" size="small" style="width:140px;" @change="onFilterChange">
          <el-option label="全部类型" value="" />
          <el-option label="Markdown" value="MARKDOWN" />
          <el-option label="PPT文件" value="PPT_FILE" />
          <el-option label="外部链接" value="EXTERNAL_URL" />
        </el-select>
      </div>

      <el-empty v-else-if="list.length === 0" description="还没有收藏任何内容" />

      <div v-else class="fav-list">
        <div v-for="fav in list" :key="fav.id" class="fav-item" @click="goContent(fav.contentId)">
          <div class="fav-info">
            <span class="fav-content-id">
              <span v-if="fav.contentType" :class="['type-badge', badgeClass(fav.contentType)]">{{ typeLabel(fav.contentType) }}</span>
              {{ fav.contentTitle || '内容 #' + fav.contentId }}
            </span>
            <span class="fav-time">{{ formatTime(fav.createdAt) }}</span>
          </div>
          <el-button size="small" type="danger" plain @click.stop="handleUnfavorite(fav)">
            取消收藏
          </el-button>
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavorites, unfavoriteContent } from '@/api/favorite'
import AppHeader from '@/components/layout/AppHeader.vue'

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
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function onFilterChange() {
  page.value = 1
  fetchList()
}

function typeLabel(t) {
  return { MARKDOWN: '📝', PPT_FILE: '📊', EXTERNAL_URL: '🔗', INTERNAL_REF: '📎' }[t] || ''
}

function badgeClass(t) {
  return {
    MARKDOWN: 'type-markdown', PPT_FILE: 'type-ppt',
    EXTERNAL_URL: 'type-link', INTERNAL_REF: 'type-ref'
  }[t] || ''
}

function goContent(contentId) {
  router.push(`/content/${contentId}`)
}

async function handleUnfavorite(fav) {
  try {
    await unfavoriteContent(fav.contentId)
    ElMessage.success('已取消收藏')
    await fetchList()
  } catch { /* handled */ }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(fetchList)
</script>

<style scoped>
.favorites-page { max-width: 800px; margin: 0 auto; }
.page-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
  padding: 24px;
}
.page-title { font-size: 14px; color: #909399; }
.main-layout { max-width: 800px; margin: 0 auto; padding: 20px; }
.fav-item { display: flex; justify-content: space-between; align-items: center; padding: 16px; background: #fff; border-radius: 8px; margin-bottom: 8px; cursor: pointer; border: 1px solid #ebeef5; }
.fav-item:hover { border-color: #409eff; }
.fav-info { display: flex; flex-direction: column; gap: 4px; }
.fav-content-id { font-size: 15px; color: #303133; font-weight: 500; }
.fav-time { font-size: 12px; color: #c0c4cc; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
.loading-wrapper { padding: 40px 0; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.type-badge { display: inline-flex; align-items: center; padding: 1px 6px; border-radius: 3px; font-size: 11px; margin-right: 6px; font-weight: 500; }
.type-markdown { background: #e6f7ff; color: #1890ff; }
.type-ppt { background: #fff7e6; color: #fa8c16; }
.type-link { background: #f6ffed; color: #52c41a; }
.type-ref { background: #f9f0ff; color: #722ed1; }
</style>
