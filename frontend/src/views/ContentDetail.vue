<template>
  <div class="content-detail-page">
    <AppHeader />

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

    <div v-else class="detail-layout">
      <div class="detail-main">
        <div class="content-card">
          <h1 class="detail-title">{{ content.title }}</h1>

          <div class="detail-meta">
            <span :class="['type-badge', typeBadgeClass]">
              {{ typeEmoji }} {{ typeLabel }}
            </span>
            <el-tag
              v-for="tag in tags"
              :key="tag.id"
              :color="tag.color"
              size="small"
              effect="dark"
              class="detail-tag"
            >
              {{ tag.name }}
            </el-tag>
            <span class="meta-text">
              <el-icon><User /></el-icon> {{ content.createdBy }}
            </span>
            <span class="meta-text">
              <el-icon><Calendar /></el-icon> {{ formatDate(content.publishedAt || content.createdAt) }}
            </span>
          </div>

          <div class="detail-actions">
            <el-button
              :type="isFavorited ? 'warning' : 'default'"
              size="small"
              @click="toggleFavorite"
              :loading="favLoading"
            >
              <el-icon><Star /></el-icon> {{ isFavorited ? '已收藏' : '收藏' }} ({{ favCount }})
            </el-button>
            <el-button size="small" @click="handleShare">
              <el-icon><Share /></el-icon> 分享
            </el-button>
            <el-button size="small" @click="handleDownload">
              <el-icon><Download /></el-icon> 下载
            </el-button>
            <el-button
              v-if="canEdit"
              size="small"
              type="primary"
              @click="$router.push(`/content/${content.id}/edit`)"
            >
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
          </div>

          <el-divider />

          <div class="markdown-body" v-html="renderedBody" />

          <CommentSection :content-id="content.id" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Calendar, Star, Share, Download, Edit } from '@element-plus/icons-vue'
import { marked } from 'marked'
import { getContent } from '@/api/content'
import { getContentTags } from '@/api/tag'
import { checkFavorite, favoriteContent, unfavoriteContent } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import AppHeader from '@/components/layout/AppHeader.vue'
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
const favLoading = ref(false)

const typeLabels = {
  MARKDOWN: 'Markdown',
  PPT_FILE: 'PPT文件',
  EXTERNAL_URL: '外部链接',
  INTERNAL_REF: '内部引用'
}
const typeEmojis = {
  MARKDOWN: '📝',
  PPT_FILE: '📊',
  EXTERNAL_URL: '🔗',
  INTERNAL_REF: '📎'
}
const typeBadgeClasses = {
  MARKDOWN: 'type-markdown',
  PPT_FILE: 'type-ppt',
  EXTERNAL_URL: 'type-link',
  INTERNAL_REF: 'type-ref'
}

const typeLabel = computed(() => typeLabels[content.value.contentType] || 'Markdown')
const typeEmoji = computed(() => typeEmojis[content.value.contentType] || '📝')
const typeBadgeClass = computed(() => typeBadgeClasses[content.value.contentType] || 'type-markdown')
const canEdit = computed(() => {
  return userStore.info?.id === content.value.createdBy || userStore.isAdmin
})

const renderedBody = computed(() => {
  const raw = content.value.body || ''
  if (!raw) return '<p style="color:#909399;">暂无正文</p>'
  return marked(raw)
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
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
  } catch { /* handled by interceptor */ }
  finally { favLoading.value = false }
}

function handleShare() {
  const url = window.location.href
  navigator.clipboard?.writeText(url)
    .then(() => ElMessage.success('链接已复制到剪贴板'))
    .catch(() => ElMessage.info(`分享链接: ${url}`))
}

function handleDownload() {
  const blob = new Blob([content.value.body || ''], { type: 'text/markdown' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${content.value.title || 'content'}.md`
  a.click()
  URL.revokeObjectURL(a.href)
  ElMessage.success('下载成功')
}

onMounted(fetchContent)
</script>

<style scoped>
.content-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.loading-wrapper {
  max-width: 800px;
  margin: 60px auto;
  padding: 0 20px;
}
.detail-layout {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px;
}
.detail-main {
  min-width: 0;
}
.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}
.detail-title {
  font-size: 28px;
  margin: 0 0 16px;
  color: #303133;
  line-height: 1.4;
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.type-markdown { background: #e6f7ff; color: #1890ff; }
.type-ppt { background: #fff7e6; color: #fa8c16; }
.type-link { background: #f6ffed; color: #52c41a; }
.type-ref { background: #f9f0ff; color: #722ed1; }
.detail-tag {
  cursor: default;
}
.meta-text {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}
.detail-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
