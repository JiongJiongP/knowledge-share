<template>
  <PageCard title="部门管理">
    <template #header>
      <el-button type="primary" size="small" @click="ElMessage.info('功能开发中')">+ 新建部门</el-button>
    </template>

    <el-table :data="departments" stripe row-key="id" default-expand-all v-loading="loading" empty-text="暂无部门">
      <el-table-column prop="name" label="部门名称" min-width="160" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="createdAt" label="创建时间" min-width="140">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default>
          <el-button size="small" text type="primary">编辑</el-button>
          <el-button size="small" text type="danger">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </PageCard>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/common/PageCard.vue'

const departments = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    departments.value = [
      { id: 1, name: '技术管理部', parentId: 0, sortOrder: 1, createdAt: '2026-03-01T10:00:00' },
      { id: 2, name: '技术中心', parentId: 0, sortOrder: 2, createdAt: '2026-03-01T10:00:00' },
      { id: 3, name: '前端技术组', parentId: 2, sortOrder: 1, createdAt: '2026-03-01T10:00:00' },
      { id: 4, name: '基础架构组', parentId: 2, sortOrder: 2, createdAt: '2026-03-01T10:00:00' },
      { id: 5, name: '产品中心', parentId: 0, sortOrder: 3, createdAt: '2026-03-01T10:00:00' }
    ]
  } finally { loading.value = false }
})

function formatDate(d) {
  if (!d) return ''
  return d.substring(0, 10)
}
</script>

<style scoped>
</style>
