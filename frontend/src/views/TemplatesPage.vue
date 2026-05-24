<template>
  <div class="templates-page">
    <AppHeader />

    <div class="templates-layout">
      <div class="templates-card">
        <div class="card-header">
          <h2 class="card-title">内容模板中心</h2>
        </div>

        <div class="tpl-grid">
          <div
            v-for="tpl in templates"
            :key="tpl.id"
            class="tpl-card"
            @click="useTemplate(tpl)"
          >
            <div class="tpl-icon">{{ tpl.icon }}</div>
            <div class="tpl-name">{{ tpl.name }}</div>
            <div class="tpl-desc">{{ tpl.desc }}</div>
            <el-button type="primary" size="small" style="width:100%">使用模板</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'

const router = useRouter()

const templates = [
  {
    id: 'tech-doc',
    icon: '📝',
    name: '技术文档模板',
    desc: '系统预置 · 包含背景、方案、实现细节等结构',
    type: 'MARKDOWN'
  },
  {
    id: 'product-doc',
    icon: '📋',
    name: '产品说明模板',
    desc: '系统预置 · 包含功能概述、使用指南、FAQ',
    type: 'MARKDOWN'
  },
  {
    id: 'meeting-notes',
    icon: '📅',
    name: '会议纪要模板',
    desc: '系统预置 · 包含议题、决议、待办事项',
    type: 'MARKDOWN'
  }
]

function useTemplate(tpl) {
  router.push({ path: '/content/create', query: { template: tpl.id, type: tpl.type } })
}
</script>

<style scoped>
.templates-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.templates-layout {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 20px;
}
.templates-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}
.tpl-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.tpl-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all .2s;
}
.tpl-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}
.tpl-icon {
  font-size: 32px;
  margin-bottom: 10px;
}
.tpl-name {
  font-weight: 600;
  margin-bottom: 6px;
  font-size: 15px;
}
.tpl-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 16px;
}
@media (max-width: 768px) {
  .tpl-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
