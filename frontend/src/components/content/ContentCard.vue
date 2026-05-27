<template>
  <el-card class="content-card" shadow="hover" @click="goDetail">
    <template #header>
      <div class="card-header">
        <h3 class="title">{{ content.title }}</h3>
        <el-tag :type="typeTagColor" size="small">{{ typeLabel }}</el-tag>
      </div>
    </template>
    <p class="excerpt">{{ excerpt }}</p>
    <div class="card-footer">
      <div class="footer-left">
        <span class="author">{{ content.createdByName || content.createdBy }}</span>
        <span class="time">{{ formatTime(content.publishedAt) }}</span>
      </div>
      <div class="footer-stats">
        <span class="stat-item" v-if="content.viewCount != null"><i class="ri-eye-line"></i> {{ content.viewCount }}</span>
        <span class="stat-item" v-if="content.favoriteCount != null"><i class="ri-star-line"></i> {{ content.favoriteCount }}</span>
        <span class="stat-item" v-if="content.commentCount != null"><i class="ri-chat-3-line"></i> {{ content.commentCount }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  content: { type: Object, required: true }
})

const router = useRouter()

const typeLabels = {
  MARKDOWN: '文章',
  PPT_FILE: 'PPT',
  EXTERNAL_URL: '链接',
  INTERNAL_REF: '引用'
}

const typeLabel = computed(() => typeLabels[props.content.contentType] || '文章')
const typeTagColor = computed(() => {
  const colors = { MARKDOWN: '', PPT_FILE: 'warning', EXTERNAL_URL: 'success', INTERNAL_REF: 'info' }
  return colors[props.content.contentType] || ''
})

const excerpt = computed(() => {
  const body = props.content.body || ''
  return body.replace(/[#*`>!\[\]()]/g, '').substring(0, 150) + (body.length > 150 ? '...' : '')
})

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function goDetail() {
  router.push(`/content/${props.content.id}`)
}
</script>

<style scoped>
.content-card {
  margin-bottom: 12px;
  cursor: pointer;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.excerpt {
  color: #909399;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
}
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
</style>
