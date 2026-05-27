<template>
  <div class="templates-page">
    <PageCard title="内容模板中心">
      <template #header>
        <button class="btn btn-primary btn-sm">+ 创建模板</button>
      </template>

      <div class="tpl-grid">
        <div
          v-for="tpl in templates"
          :key="tpl.id"
          class="tpl-card"
          @click="useTemplate(tpl)"
        >
          <div class="tpl-icon"><i :class="tpl.icon"></i></div>
          <div class="tpl-name">{{ tpl.name }}</div>
          <div class="tpl-desc">{{ tpl.desc }}</div>
          <button class="btn btn-primary btn-sm" style="width:100%">使用模板</button>
        </div>
      </div>
    </PageCard>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import PageCard from '@/components/common/PageCard.vue'

const router = useRouter()

const templates = [
  {
    id: 'tech-doc',
    icon: 'ri-file-text-line',
    name: '技术文档模板',
    desc: '系统预置 · 包含背景、方案、实现细节等结构',
    type: 'MARKDOWN'
  },
  {
    id: 'product-doc',
    icon: 'ri-file-list-3-line',
    name: '产品说明模板',
    desc: '系统预置 · 包含功能概述、使用指南、FAQ',
    type: 'MARKDOWN'
  },
  {
    id: 'meeting-notes',
    icon: 'ri-calendar-line',
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
.templates-page { max-width: 1000px; margin: 0 auto; }
.tpl-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.tpl-card {
  border: 1px solid #DCDFE6;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all .2s;
}
.tpl-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}
.tpl-icon { font-size: 28px; margin-bottom: 8px; }
.tpl-name { font-weight: 600; margin-bottom: 4px; font-size: 15px; }
.tpl-desc { font-size: 12px; color: #909399; margin-bottom: 12px; }
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all .2s;
  font-family: inherit;
  background: none;
}
.btn-primary { background: #409EFF; color: #fff; border-color: #409EFF; }
.btn-default { background: #fff; color: #303133; border-color: #DCDFE6; }
.btn-sm { padding: 4px 10px; font-size: 12px; }

@media (max-width: 768px) {
  .tpl-grid { grid-template-columns: 1fr 1fr; }
}
</style>
