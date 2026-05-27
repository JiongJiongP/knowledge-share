<template>
  <div class="page-card">
    <div v-if="title || $slots.header" class="card-header">
      <h2 v-if="title" class="card-title">{{ title }}</h2>
      <slot name="header" />
    </div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}
</style>
