<template>
  <PageCard title="审核中心">
    <div class="filter-bar">
      <el-select v-model="filterType" placeholder="全部" @change="onFilterChange" style="width:160px">
        <el-option label="全部" value="" />
        <el-option label="内容审核" value="CONTENT" />
        <el-option label="评论审核" value="COMMENT" />
      </el-select>
    </div>
    <el-table :data="audits" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="targetTypeName" label="类型" width="90"/>
      <el-table-column prop="targetTitle" label="目标内容" min-width="180">
        <template #default="{ row }">
          <router-link v-if="row.targetType === 'CONTENT'" :to="`/content/${row.targetId}`" style="color:#409EFF;">
            {{ row.targetTitle }}
          </router-link>
          <span v-else>{{ row.targetTitle }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="submitterName" label="提交人" width="100"/>
      <el-table-column prop="statusName" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'danger'" size="small">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" width="160"/>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
            <el-button size="small" type="danger" @click="showReject(row)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="rejectVisible" title="驳回原因" width="400px">
      <el-input v-model="rejectReason" placeholder="填写驳回原因" type="textarea" :rows="3"/>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/common/PageCard.vue'

const audits = ref([])
const loading = ref(false)
const filterType = ref('')
const rejectVisible = ref(false)
const rejectReason = ref('')
const currentId = ref(null)

async function fetch() {
  loading.value = true
  try {
    const params = {}
    if (filterType.value) params.type = filterType.value
    const res = await request.get('/admin/audit/pending', { params })
    audits.value = res.data
  } catch { /* handled */ }
  finally { loading.value = false }
}

function onFilterChange() { fetch() }

async function handleApprove(row) {
  await request.post(`/admin/audit/${row.id}/approve`)
  ElMessage.success('已通过')
  fetch()
}

function showReject(row) { currentId.value = row.id; rejectVisible.value = true; rejectReason.value = '' }
async function handleReject() {
  await request.post(`/admin/audit/${currentId.value}/reject`, { reason: rejectReason.value })
  ElMessage.success('已驳回')
  rejectVisible.value = false
  fetch()
}

onMounted(fetch)
</script>

<style scoped>
.filter-bar { display: flex; margin-bottom: 16px; }
</style>
