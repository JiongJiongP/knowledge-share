<template>
  <div class="admin-layout">
    <AppSidebar />
    <div class="admin-main">
      <AppHeader>
        <template #center>
          <span class="admin-title">管理后台</span>
        </template>
      </AppHeader>
      <div class="admin-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}
.admin-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.admin-title {
  font-size: 14px;
  color: #909399;
}
.admin-content {
  padding: 20px;
  flex: 1;
}
</style>
