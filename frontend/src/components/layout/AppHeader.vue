<template>
  <header class="app-header">
    <div class="header-inner">
      <span class="logo" @click="$router.push('/')">知享 Knowledge Hub</span>
      <nav class="header-nav">
        <router-link to="/" class="nav-item" exact-active-class="nav-active">首页</router-link>
        <router-link to="/groups" class="nav-item" active-class="nav-active">群组</router-link>
        <router-link to="/favorites" class="nav-item" active-class="nav-active">收藏</router-link>
        <router-link to="/notifications" class="nav-item" active-class="nav-active">通知</router-link>
        <router-link v-if="userStore.isAdmin" to="/admin/users" class="nav-item nav-admin" active-class="nav-active">管理后台</router-link>
      </nav>
      <slot name="center" />
      <div class="header-right">
        <slot name="right" />
        <span class="username">{{ userStore.info?.displayName }}</span>
        <el-button type="danger" size="small" plain @click="userStore.logout">退出</el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
</script>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  display: flex;
  align-items: center;
  height: 56px;
  max-width: 1200px;
  margin: 0 auto;
  gap: 20px;
}
.logo {
  font-weight: 700;
  font-size: 16px;
  color: #303133;
  cursor: pointer;
  white-space: nowrap;
}
.header-nav {
  display: flex;
  gap: 4px;
}
.nav-item {
  padding: 6px 14px;
  font-size: 14px;
  color: #606266;
  text-decoration: none;
  border-radius: 6px;
  transition: background 0.2s, color 0.2s;
}
.nav-item:hover {
  background: #f0f2f5;
  color: #303133;
}
.nav-active {
  color: #409eff;
  background: #ecf5ff;
}
.nav-admin {
  border: 1px dashed #c0c4cc;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}
.username {
  font-size: 13px;
  color: #606266;
}
</style>
