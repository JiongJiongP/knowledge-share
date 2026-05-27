<template>
  <div class="main-layout">
    <AppSidebar />
    <div class="main-area">
      <AppHeader />
      <main class="content"><slot /></main>
    </div>
  </div>
</template>

<script setup>
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
}

.main-area {
  margin-left: 220px;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.content {
  padding: 24px;
  flex: 1;
  background: #F5F7FA;
}
</style>
