<template>
  <div class="content-detail-page">
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="5" animated />
    </div>

    <el-result
      v-else-if="error"
      icon="error"
      title="加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>

    <template v-else>
      <div class="detail-card">
        <h1 class="detail-title">{{ content.title }}</h1>

        <div class="detail-meta">
          <span :class="['type-badge', typeBadgeClass]">{{ typeEmoji }} {{ typeLabel }}</span>
          <span v-for="tag in tags" :key="tag.id" class="tag tag-sm" :style="{ background: tag.color || '#409EFF' }">{{ tag.name }}</span>
          <span class="meta-text">👤 {{ content.createdBy }} · 📅 {{ formatDate(content.publishedAt || content.createdAt) }} · 👁️ {{ formatCount(content.viewCount) }} 阅读</span>
        </div>

        <div class="detail-actions">
          <button class="btn btn-default btn-sm" @click="toggleFavorite" :disabled="favLoading">
            ⭐ 收藏 ({{ favCount }})
          </button>
          <button class="btn btn-default btn-sm" @click="handleShare">📤 分享</button>
          <button class="btn btn-default btn-sm" @click="handleDownload">📥 下载</button>
          <button v-if="canEdit" class="btn btn-default btn-sm" @click="$router.push(`/content/${content.id}/edit`)">✏️ 编辑</button>
        </div>

        <div class="detail-divider" />

        <div class="markdown-body" v-html="renderedBody" />
      </div>

      <div class="detail-card">
        <div class="card-header-row">
          <div class="card-title-sm">评论 ({{ commentCount }})</div>
          <select class="filter-select" style="width:auto;">
            <option>按热度排序</option>
            <option>按时间排序</option>
          </select>
        </div>
        <CommentSection :content-id="content.id" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { getContent } from '@/api/content'
import { getContentTags } from '@/api/tag'
import { checkFavorite, favoriteContent, unfavoriteContent } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import CommentSection from '@/components/content/CommentSection.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const content = ref({})
const tags = ref([])
const loading = ref(true)
const error = ref('')
const isFavorited = ref(false)
const favCount = ref(0)
const commentCount = ref(0)
const favLoading = ref(false)

const typeLabels = {
  MARKDOWN: 'Markdown', PPT_FILE: 'PPT文件',
  EXTERNAL_URL: '外部链接', INTERNAL_REF: '内部引用'
}
const typeEmojis = {
  MARKDOWN: '📝', PPT_FILE: '📊',
  EXTERNAL_URL: '🔗', INTERNAL_REF: '📎'
}
const typeBadgeClasses = {
  MARKDOWN: 'type-markdown', PPT_FILE: 'type-ppt',
  EXTERNAL_URL: 'type-link', INTERNAL_REF: 'type-ref'
}

const typeLabel = computed(() => typeLabels[content.value.contentType] || 'Markdown')
const typeEmoji = computed(() => typeEmojis[content.value.contentType] || '📝')
const typeBadgeClass = computed(() => typeBadgeClasses[content.value.contentType] || 'type-markdown')
const canEdit = computed(() => {
  return userStore.info?.id === content.value.createdBy ||
    userStore.info?.username === content.value.createdBy ||
    userStore.isAdmin
})

const renderedBody = computed(() => {
  const raw = content.value.body || ''
  if (!raw) return '<p style="color:#909399;">暂无正文</p>'
  return DOMPurify.sanitize(marked(raw))
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
function formatCount(n) {
  if (!n) return '0'
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

async function fetchContent() {
  loading.value = true
  error.value = ''
  try {
    const id = route.params.id
    const [contentRes, tagRes] = await Promise.all([
      getContent(id),
      getContentTags(id).catch(() => ({ data: [] }))
    ])
    content.value = contentRes.data
    tags.value = tagRes.data || []
    commentCount.value = contentRes.data?.commentCount || 0
    try {
      const favRes = await checkFavorite(id)
      isFavorited.value = favRes.data?.favorited || false
    } catch { isFavorited.value = false }
    favCount.value = contentRes.data?.favoriteCount || 0
  } catch (e) {
    error.value = e.response?.data?.message || '内容加载失败'
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  favLoading.value = true
  try {
    if (isFavorited.value) {
      await unfavoriteContent(content.value.id)
      isFavorited.value = false
      favCount.value = Math.max(0, favCount.value - 1)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteContent(content.value.id)
      isFavorited.value = true
      favCount.value++
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  } finally { favLoading.value = false }
}

function handleShare() {
  const url = window.location.href
  if (!navigator.clipboard) { ElMessage.info(`分享链接: ${url}`); return }
  navigator.clipboard.writeText(url)
    .then(() => ElMessage.success('链接已复制到剪贴板'))
    .catch(() => ElMessage.info(`分享链接: ${url}`))
}

function handleDownload() {
  const blob = new Blob([content.value.body || ''], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${(content.value.title || 'content').replace(/[\\/:*?"<>|]/g, '_')}.md`
  a.click()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
  ElMessage.success('下载成功')
}

onMounted(fetchContent)
</script>

<style scoped>
.content-detail-page { max-width: 900px; margin: 0 auto; }
.loading-wrapper { padding: 60px 20px 0; }

.detail-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
  padding: 24px;
  margin-bottom: 16px;
}
.detail-title {
  font-size: 24px;
  margin: 0 0 12px;
  color: #303133;
  line-height: 1.4;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.type-badge {
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
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
  color: #fff;
  margin: 0;
}
.tag.sm { padding: 1px 6px; font-size: 11px; }
.meta-text { font-size: 13px; color: #909399; }
.detail-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
  flex-wrap: wrap;
}
.detail-divider {
  border-top: 1px solid #E4E7ED;
  margin: 20px 0;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all .2s;
  font-family: inherit;
  background: none;
}
.btn-primary { background: #409EFF; color: #fff; border-color: #409EFF; }
.btn-default { background: #fff; color: #303133; border-color: #DCDFE6; }
.btn-default:hover { color: #409EFF; border-color: #409EFF; }
.btn-sm { padding: 4px 10px; font-size: 12px; }
.btn:disabled { opacity: .5; cursor: not-allowed; }

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card-title-sm { font-size: 16px; font-weight: 600; margin: 0; color: #303133; }
.filter-select {
  height: 32px;
  border: 1px solid #DCDFE6;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 12px;
  outline: none;
  background: #fff;
  cursor: pointer;
}

.markdown-body {
  font-size: 15px;
  line-height: 2;
  color: #303133;
}
.markdown-body :deep(h1) { font-size: 24px; margin: 24px 0 12px; }
.markdown-body :deep(h2) { font-size: 20px; margin: 20px 0 10px; }
.markdown-body :deep(h3) { font-size: 17px; margin: 16px 0 8px; }
.markdown-body :deep(p) { margin: 0 0 12px; }
.markdown-body :deep(pre) { background: #f5f7fa; padding: 16px; border-radius: 8px; overflow-x: auto; font-size: 13px; }
.markdown-body :deep(code) { background: #f5f7fa; padding: 2px 6px; border-radius: 3px; font-size: 13px; }
.markdown-body :deep(pre code) { background: none; padding: 0; }
.markdown-body :deep(blockquote) { border-left: 4px solid #409eff; padding: 4px 16px; margin: 16px 0; background: #ecf5ff; border-radius: 0 4px 4px 0; }
.markdown-body :deep(table) { width: 100%; border-collapse: collapse; margin: 16px 0; }
.markdown-body :deep(th), .markdown-body :deep(td) { border: 1px solid #ebeef5; padding: 8px 12px; text-align: left; }
.markdown-body :deep(th) { background: #f5f7fa; font-weight: 600; }
.markdown-body :deep(img) { max-width: 100%; border-radius: 6px; }
</style>
