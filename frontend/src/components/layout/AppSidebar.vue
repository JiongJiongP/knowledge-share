<template>
  <aside class="app-sidebar">
    <div class="sidebar-logo" @click="$router.push('/')">
      <div class="logo-icon">📚</div>
      <span>知识平台</span>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-group-title">内容</div>
      <router-link to="/" class="nav-item" exact-active-class="active">🏠 首页</router-link>
      <router-link to="/content/create" class="nav-item" active-class="active">✏️ 创建内容</router-link>
      <router-link to="/templates" class="nav-item" active-class="active">📋 模板中心</router-link>

      <div class="nav-group-title">群组</div>
      <router-link to="/groups" class="nav-item" active-class="active">👥 群组列表</router-link>

      <div class="nav-group-title">个人</div>
      <router-link to="/favorites" class="nav-item" active-class="active">⭐ 我的收藏</router-link>
      <router-link to="/notifications" class="nav-item" active-class="active">
        🔔 通知中心
        <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </router-link>

      <template v-if="userStore.isAdmin">
        <div class="nav-group-title">管理</div>
        <router-link to="/admin/audit" class="nav-item" active-class="active">
          ✅ 审核中心
          <span v-if="auditPending > 0" class="badge">{{ auditPending }}</span>
        </router-link>
        <router-link to="/admin/tags" class="nav-item" active-class="active">🏷️ 标签管理</router-link>
        <router-link to="/admin/users" class="nav-item" active-class="active">👤 用户管理</router-link>
        <router-link to="/admin/sensitive-words" class="nav-item" active-class="active">🚫 敏感词管理</router-link>
        <router-link to="/admin/analytics" class="nav-item" active-class="active">📊 数据分析</router-link>
        <router-link to="/admin/departments" class="nav-item" active-class="active">🏢 部门管理</router-link>
        <router-link to="/admin/settings" class="nav-item" active-class="active">⚙️ 系统设置</router-link>
      </template>
    </nav>
  </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/notification'

const userStore = useUserStore()
const unreadCount = ref(0)
const auditPending = ref(0)
let pollTimer = null

async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 60000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.app-sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  padding: 20px 24px;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, .1);
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: #409EFF;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 0;
}

.nav-group-title {
  padding: 8px 24px;
  font-size: 11px;
  color: rgba(255, 255, 255, .4);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 24px;
  color: rgba(255, 255, 255, .7);
  cursor: pointer;
  transition: all .2s;
  font-size: 14px;
  border-left: 3px solid transparent;
  text-decoration: none;
}

.nav-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, .05);
}

.nav-item.active {
  color: #fff;
  background: rgba(255, 255, 255, .1);
  border-left-color: #409EFF;
}

.badge {
  margin-left: auto;
  background: #F56C6C;
  color: #fff;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}
</style>
