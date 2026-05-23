<template>
  <header class="app-header">
    <div class="header-inner">
      <span class="logo" @click="$router.push('/')">知享 Knowledge Hub</span>
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
