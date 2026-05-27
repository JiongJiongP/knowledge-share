<template>
  <PageCard title="用户管理">
    <template #header>
      <div class="header-actions">
        <el-input v-model="searchKey" placeholder="搜索用户..." size="small" style="width:200px;" clearable />
        <el-button type="primary" size="small" @click="openCreate">新建用户</el-button>
      </div>
    </template>

    <el-table :data="filteredUsers" stripe v-loading="loading" empty-text="暂无用户">
      <el-table-column prop="displayName" label="姓名" min-width="120" />
      <el-table-column prop="username" label="用户名" min-width="100" />
      <el-table-column prop="departmentName" label="部门" min-width="120" />
      <el-table-column prop="roleName" label="角色" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.roleName === '系统管理员' ? '' : 'info'" size="small">{{ row.roleName || '普通用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" min-width="140">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="openEditRole(row)">编辑角色</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建用户对话框 -->
    <el-dialog v-model="createDialogVisible" title="新建用户" width="460px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请输入密码（至少6位）" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="displayName">
          <el-input v-model="createForm.displayName" placeholder="请输入显示名称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="编辑角色" width="420px">
      <el-form label-position="top">
        <el-form-item label="用户">{{ selectedUser?.username }}</el-form-item>
        <el-form-item label="角色">
          <el-select v-model="selectedRole" placeholder="选择角色" style="width:100%">
            <el-option label="系统管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, createUser, updateUserRole } from '@/api/user'
import PageCard from '@/components/common/PageCard.vue'

const users = ref([])
const loading = ref(false)
const searchKey = ref('')
const createDialogVisible = ref(false)
const creating = ref(false)
const createFormRef = ref(null)
const createForm = ref({ username: '', password: '', displayName: '', email: '', departmentId: null })
const createRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
}
const roleDialogVisible = ref(false)
const selectedUser = ref(null)
const selectedRole = ref('USER')
const saving = ref(false)

const filteredUsers = computed(() => {
  if (!searchKey.value) return users.value
  const kw = searchKey.value.toLowerCase()
  return users.value.filter(u =>
    u.username?.toLowerCase().includes(kw) ||
    u.displayName?.toLowerCase().includes(kw) ||
    u.email?.toLowerCase().includes(kw)
  )
})

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res.data || []
  } catch { /* handled */ }
  finally { loading.value = false }
}

function openCreate() {
  createForm.value = { username: '', password: '', displayName: '', email: '', departmentId: null }
  createDialogVisible.value = true
}

async function submitCreate() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createUser(createForm.value)
    ElMessage.success('用户创建成功')
    createDialogVisible.value = false
    fetchUsers()
  } catch { /* handled */ }
  finally { creating.value = false }
}

function openEditRole(row) {
  selectedUser.value = row
  selectedRole.value = row.roleName === '系统管理员' ? 'ADMIN' : 'USER'
  roleDialogVisible.value = true
}

async function submitRole() {
  saving.value = true
  try {
    await updateUserRole(selectedUser.value.id, selectedRole.value)
    ElMessage.success('角色更新成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch { /* handled */ }
  finally { saving.value = false }
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}

onMounted(fetchUsers)
</script>

<style scoped>
.header-actions { display: flex; gap: 10px; align-items: center; }
</style>
