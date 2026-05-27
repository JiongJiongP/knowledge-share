<template>
  <div class="group-detail-page">
    <div class="page-card">
      <el-skeleton :rows="4" animated />
    </div>

    <template v-else-if="group">
      <div class="main-layout">
        <div class="group-header">
          <h2>{{ group.name }}</h2>
          <p class="desc">{{ group.description || '暂无描述' }}</p>
          <div class="group-actions">
            <el-button v-if="!isMember" type="primary" :loading="joining" @click="handleJoin">申请加入</el-button>
            <el-button v-if="isOwner" size="small" @click="$router.push(`/group/${group.id}/manage`)">管理群组</el-button>
          </div>
        </div>

        <el-divider />

        <h3>成员列表 ({{ members.length }})</h3>
        <el-table :data="members" style="width: 100%">
          <el-table-column prop="displayName" label="姓名" width="120" />
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
        </el-table>
      </div>
    </template>

    <el-empty v-else description="群组不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGroup, joinGroup, getGroupMembers } from '@/api/group'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const group = ref(null)
const members = ref([])
const loading = ref(true)
const joining = ref(false)

const isOwner = ref(false)
const isMember = ref(false)

async function fetchGroup() {
  loading.value = true
  try {
    const [groupRes, memberRes] = await Promise.all([
      getGroup(route.params.id),
      getGroupMembers(route.params.id)
    ])
    group.value = groupRes.data
    members.value = memberRes.data

    const uid = userStore.info?.id
    const member = members.value.find(m => m.userId === uid && m.status === 'APPROVED')
    isMember.value = !!member
    isOwner.value = member?.role === 'OWNER'
  } finally {
    loading.value = false
  }
}

async function handleJoin() {
  joining.value = true
  try {
    await joinGroup(group.value.id)
    ElMessage.success('申请已提交，等待群主审批')
  } catch { /* handled by interceptor */ }
  finally { joining.value = false }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onMounted(fetchGroup)
</script>

<style scoped>
.group-detail-page { max-width: 900px; margin: 0 auto; }
.main-layout { max-width: 1200px; margin: 0 auto; padding: 20px; }
.group-header h2 { margin: 0 0 8px; font-size: 24px; }
.desc { color: #909399; margin: 0 0 16px; }
.group-actions { display: flex; gap: 10px; }
</style>
