<template>
  <div class="group-manage-page">
    <div class="page-card">
      <el-skeleton :rows="4" animated />
    </div>

    <template v-else-if="group">
      <div class="main-layout">
        <h2>管理群组: {{ group.name }}</h2>

        <el-divider />

        <h3>待审批申请</h3>
        <el-table :data="pendingMembersList" style="width: 100%">
          <el-table-column prop="userName" label="用户名" width="120" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="createdAt" label="申请时间">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="success" size="small" :loading="approvingId === row.id" @click="handleApprove(row, 'APPROVED')">通过</el-button>
              <el-button type="danger" size="small" :loading="approvingId === row.id" @click="handleApprove(row, 'REJECTED')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="pendingMembersList.length === 0" description="暂无待审批申请" />

        <el-divider />

        <h3>已加入成员</h3>
        <el-table :data="approvedMembers" style="width: 100%">
          <el-table-column prop="userName" label="用户名" width="120" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="role" label="角色" width="120">
            <template #default="{ row }">
              <el-tag :type="row.role === 'OWNER' ? 'warning' : 'info'" size="small">
                {{ row.role === 'OWNER' ? '群主' : '成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="joinedAt" label="加入时间">
            <template #default="{ row }">{{ formatTime(row.joinedAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                v-if="row.role !== 'OWNER'"
                type="danger"
                size="small"
                plain
                @click="handleRemove(row)"
              >移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGroup, getGroupMembers, getPendingMembers, approveMember, removeMember } from '@/api/group'

const route = useRoute()

const group = ref(null)
const approvedMembers = ref([])
const pendingMembersList = ref([])
const loading = ref(true)
const approvingId = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const [groupRes, memberRes, pendingRes] = await Promise.all([
      getGroup(route.params.id),
      getGroupMembers(route.params.id),
      getPendingMembers(route.params.id)
    ])
    group.value = groupRes.data
    approvedMembers.value = memberRes.data
    pendingMembersList.value = pendingRes.data
  } finally {
    loading.value = false
  }
}

async function handleApprove(row, action) {
  approvingId.value = row.id
  try {
    await approveMember(group.value.id, row.userId, action)
    ElMessage.success(action === 'APPROVED' ? '已通过' : '已拒绝')
    await fetchData()
  } catch { /* handled by interceptor */ }
  finally { approvingId.value = null }
}

async function handleRemove(row) {
  try {
    await ElMessageBox.confirm(`确定要移除用户 ${row.userId} 吗？`, '确认移除', { type: 'warning' })
    await removeMember(group.value.id, row.userId)
    ElMessage.success('已移除')
    await fetchData()
  } catch { /* cancelled or error */ }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onMounted(fetchData)
</script>

<style scoped>
.group-manage-page { max-width: 900px; margin: 0 auto; }
.main-layout { max-width: 1200px; margin: 0 auto; padding: 20px; }
</style>
