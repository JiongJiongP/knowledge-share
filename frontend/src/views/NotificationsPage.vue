<template>
  <div class="notifications-page">
    <AppHeader>
      <template #center>
        <span class="page-title">通知中心</span>
      </template>
      <template #right>
        <el-button size="small" text @click="handleMarkAllRead">全部已读</el-button>
      </template>
    </AppHeader>

    <div class="main-layout">
      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="3" animated />
      </div>

      <el-empty v-else-if="list.length === 0" description="暂无通知" />

      <div v-else class="notif-list">
        <div
          v-for="n in list" :key="n.id"
          class="notif-item"
          :class="{ unread: !n.isRead }"
          @click="handleClick(n)"
        >
          <div class="notif-dot" v-if="!n.isRead" />
          <div class="notif-body">
            <div class="notif-title">{{ n.title }}</div>
            <div class="notif-content" v-if="n.content">{{ n.content }}</div>
            <div class="notif-time">{{ formatTime(n.createdAt) }}</div>
          </div>
          <el-button size="small" text type="danger" @click.stop="handleDelete(n)">删除</el-button>
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
import { getNotifications, markRead, markAllRead, deleteNotification } from '@/api/notification'
import AppHeader from '@/components/layout/AppHeader.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(10)

async function fetchList() {
  loading.value = true
  try {
    const res = await getNotifications({ page: page.value, size: size.value })
    list.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleClick(n) {
  if (!n.isRead) {
    await markRead(n.id)
    n.isRead = 1
  }
}

async function handleMarkAllRead() {
  await markAllRead()
  await fetchList()
  ElMessage.success('已全部标记为已读')
}

async function handleDelete(n) {
  await deleteNotification(n.id)
  ElMessage.success('已删除')
  await fetchList()
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
.notifications-page { min-height: 100vh; background: #f5f7fa; }
.page-title { font-size: 14px; color: #909399; }
.main-layout { max-width: 700px; margin: 0 auto; padding: 20px; }
.notif-item { display: flex; align-items: center; gap: 12px; padding: 14px 16px; background: #fff; border-radius: 8px; margin-bottom: 6px; cursor: pointer; border: 1px solid #ebeef5; }
.notif-item.unread { background: #ecf5ff; border-color: #d9ecff; }
.notif-dot { width: 8px; height: 8px; border-radius: 50%; background: #409eff; flex-shrink: 0; }
.notif-body { flex: 1; min-width: 0; }
.notif-title { font-size: 14px; color: #303133; }
.notif-content { font-size: 12px; color: #909399; margin-top: 4px; }
.notif-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
.loading-wrapper { padding: 40px 0; }
</style>
