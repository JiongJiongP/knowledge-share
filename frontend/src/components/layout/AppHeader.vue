<template>
  <header class="app-header">
    <div class="header-search">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="搜索知识内容...（全文检索 + 语义搜索 + 标签筛选）"
        class="search-input"
        @keyup.enter="doSearch"
      />
      <span class="search-icon" @click="doSearch">🔍</span>
    </div>

    <div class="header-actions">
      <div class="dropdown" ref="notifyRef">
        <div class="header-btn" @click="toggleNotify">
          🔔
          <span v-if="unreadCount > 0" class="dot" />
        </div>
        <div v-if="showNotifyPanel" class="notification-panel">
          <div class="notification-panel-header">
            通知消息
            <span v-if="unreadCount > 0" style="font-size:12px;color:#909399;font-weight:400;">({{ unreadCount }} 条未读)</span>
          </div>
          <template v-if="recentNotifs.length > 0">
            <div
              v-for="n in recentNotifs"
              :key="n.id"
              class="notification-item"
              :class="{ unread: !n.isRead }"
              @click="goNotification(n)"
            >
              <div class="notification-icon" :style="{ background: iconBg(n.type), color: iconColor(n.type) }">{{ iconEmoji(n.type) }}</div>
              <div class="notification-content">
                <div class="notification-title">{{ n.title }}</div>
                <div class="notification-time">{{ formatTime(n.createdAt) }}</div>
              </div>
            </div>
          </template>
          <div v-else style="padding:24px;text-align:center;color:#909399;font-size:13px;">暂无通知</div>
          <div class="notification-panel-footer">
            <a href="#" @click.prevent="$router.push('/notifications'); showNotifyPanel = false">查看全部通知</a>
          </div>
        </div>
      </div>

      <div class="dropdown" ref="userRef">
        <div class="header-avatar" @click="toggleUserMenu">{{ userInitial }}</div>
        <div v-if="showUserMenu" class="dropdown-menu">
          <div class="dropdown-item" @click="$router.push('/profile'); showUserMenu = false">👤 个人信息</div>
          <div class="dropdown-item" @click="$router.push('/settings'); showUserMenu = false">⚙️ 账号设置</div>
          <div class="dropdown-item" style="color:#F56C6C;" @click="handleLogout">🚪 退出登录</div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount, getNotifications, markRead } from '@/api/notification'

const router = useRouter()
const userStore = useUserStore()

const searchQuery = ref('')
const unreadCount = ref(0)
const recentNotifs = ref([])
const showNotifyPanel = ref(false)
const showUserMenu = ref(false)
const notifyRef = ref(null)
const userRef = ref(null)

const userInitial = computed(() => {
  const name = userStore.info?.displayName || userStore.info?.username || ''
  return name.charAt(0).toUpperCase()
})

function doSearch() {
  if (!searchQuery.value.trim()) return
  router.push({ path: '/', query: { q: searchQuery.value.trim() } })
}

function toggleNotify() {
  showNotifyPanel.value = !showNotifyPanel.value
  showUserMenu.value = false
  if (showNotifyPanel.value) fetchRecentNotifs()
}

function toggleUserMenu() {
  showUserMenu.value = !showUserMenu.value
  showNotifyPanel.value = false
}

async function fetchRecentNotifs() {
  try {
    const res = await getNotifications({ page: 1, size: 5 })
    recentNotifs.value = res.data?.records || []
  } catch { /* ignore */ }
}

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch { /* ignore */ }
}

async function goNotification(n) {
  if (!n.isRead) await markRead(n.id)
  showNotifyPanel.value = false
  router.push('/notifications')
}

function handleLogout() {
  showUserMenu.value = false
  userStore.logout()
}

function iconEmoji(type) {
  const m = { COMMENT: '💬', COMMENT_REPLY: '💬', COMMENT_MENTION: '💬', GROUP_JOIN: '👥', GROUP_APPROVE: '👥', CONTENT_AUDIT: '✅', SYSTEM: '📢' }
  return m[type] || '📢'
}
function iconBg(type) {
  return type?.startsWith('COMMENT') ? '#ecf5ff' : type?.startsWith('GROUP') ? '#fef0f0' : type === 'CONTENT_AUDIT' ? '#f0f9eb' : '#fdf6ec'
}
function iconColor(type) {
  return type?.startsWith('COMMENT') ? '#409EFF' : type?.startsWith('GROUP') ? '#F56C6C' : type === 'CONTENT_AUDIT' ? '#67C23A' : '#E6A23C'
}
function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getMonth() + 1}-${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function handleClickOutside(e) {
  if (notifyRef.value && !notifyRef.value.contains(e.target)) showNotifyPanel.value = false
  if (userRef.value && !userRef.value.contains(e.target)) showUserMenu.value = false
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnreadCount()
  }
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.app-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #E4E7ED;
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
  gap: 16px;
}

.header-search {
  flex: 1;
  max-width: 480px;
  position: relative;
}

.search-input {
  width: 100%;
  height: 36px;
  border: 1px solid #DCDFE6;
  border-radius: 18px;
  padding: 0 40px 0 16px;
  font-size: 13px;
  outline: none;
  transition: border .2s;
  background: #F5F7FA;
  font-family: inherit;
  box-sizing: border-box;
}
.search-input:focus {
  border-color: #409EFF;
  background: #fff;
}
.search-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
  cursor: pointer;
  font-size: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
}

.header-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  transition: background .2s;
  color: #606266;
  font-size: 18px;
}
.header-btn:hover {
  background: #F5F7FA;
}
.dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: #F56C6C;
  border-radius: 50%;
  border: 2px solid #fff;
}

.header-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409EFF;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
}

.dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, .12);
  min-width: 180px;
  padding: 8px 0;
  z-index: 150;
}
.dropdown-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dropdown-item:hover {
  background: #F5F7FA;
}

.notification-panel {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, .12);
  width: 360px;
  z-index: 150;
  max-height: 400px;
  overflow-y: auto;
}
.notification-panel-header {
  padding: 14px 16px;
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid #E4E7ED;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notification-item {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #E4E7ED;
  cursor: pointer;
  transition: background .2s;
}
.notification-item:hover {
  background: #F5F7FA;
}
.notification-item.unread {
  background: #ecf5ff;
}
.notification-item.unread:hover {
  background: #d9ecff;
}
.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.notification-content {
  flex: 1;
  min-width: 0;
}
.notification-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notification-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}
.notification-panel-footer {
  padding: 10px 16px;
  text-align: center;
  border-top: 1px solid #E4E7ED;
}
.notification-panel-footer a {
  color: #409EFF;
  font-size: 13px;
  text-decoration: none;
}
.notification-panel-footer a:hover {
  text-decoration: underline;
}
</style>
