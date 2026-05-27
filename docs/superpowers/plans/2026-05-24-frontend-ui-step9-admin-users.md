# Step 9: User Management, Department Management, System Settings

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create 3 new admin pages (UserManagement, DepartmentManagement, SystemSettings) with routes and AdminLayout wrapper.

**Architecture:** Each page uses AdminLayout (sidebar + header). User page shows user table with role editing. Department page shows tree table. Settings page shows basic config form. All use placeholder/demo data where no backend API exists.

**Tech Stack:** Vue 3 + Element Plus

---

### Task 1: Create all 3 admin pages + routes

**Files:**
- Create: `frontend/src/views/admin/UserManage.vue`
- Create: `frontend/src/views/admin/DepartmentManage.vue`
- Create: `frontend/src/views/admin/SystemSettings.vue`
- Modify: `frontend/src/router/index.js`

#### Page 1: UserManage.vue

User table showing username, department, role, groups, registration date, with role edit action.

Full code:

```vue
<template>
  <AdminLayout>
    <div class="page-card">
      <div class="card-header">
        <h2 class="card-title">用户管理</h2>
        <el-input v-model="searchKey" placeholder="搜索用户..." size="small" style="width:200px;" clearable />
      </div>

      <el-table :data="users" stripe v-loading="loading" empty-text="暂无用户">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="displayName" label="姓名" min-width="100" />
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
            <el-button size="small" text type="primary" @click="editRole(row)">编辑角色</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

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
        <el-button type="primary" @click="roleDialogVisible = false; ElMessage.success('角色已更新')">保存</el-button>
      </template>
    </el-dialog>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const users = ref([])
const loading = ref(false)
const searchKey = ref('')
const roleDialogVisible = ref(false)
const selectedUser = ref(null)
const selectedRole = ref('USER')

onMounted(async () => {
  loading.value = true
  try {
    // Placeholder data - replace with real API when available
    users.value = [
      { id: 1, username: 'admin', displayName: '管理员', departmentName: '技术管理部', roleName: '系统管理员', createdAt: '2026-03-01T10:00:00' },
      { id: 2, username: 'lisan', displayName: '李三', departmentName: '技术中心', roleName: '普通用户', createdAt: '2026-03-05T10:00:00' }
    ]
  } finally { loading.value = false }
})

function editRole(user) {
  selectedUser.value = user
  selectedRole.value = user.roleName === '系统管理员' ? 'ADMIN' : 'USER'
  roleDialogVisible.value = true
}

function formatDate(d) {
  if (!d) return ''
  return d.substring(0, 10)
}
</script>

<style scoped>
.page-card { background: #fff; border-radius: 8px; padding: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-title { font-size: 16px; font-weight: 600; margin: 0; }
</style>
```

#### Page 2: DepartmentManage.vue

```vue
<template>
  <AdminLayout>
    <div class="page-card">
      <div class="card-header">
        <h2 class="card-title">部门管理</h2>
        <el-button type="primary" size="small" @click="ElMessage.info('功能开发中')">+ 新建部门</el-button>
      </div>

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
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/components/layout/AdminLayout.vue'

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
.page-card { background: #fff; border-radius: 8px; padding: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-title { font-size: 16px; font-weight: 600; margin: 0; }
</style>
```

#### Page 3: SystemSettings.vue

```vue
<template>
  <AdminLayout>
    <div class="page-card">
      <div class="card-header">
        <h2 class="card-title">系统设置</h2>
      </div>

      <el-form label-position="top" style="max-width:500px;">
        <el-form-item label="平台名称">
          <el-input v-model="settings.platformName" placeholder="知识分享平台" />
        </el-form-item>
        <el-form-item label="内容审核">
          <el-switch v-model="settings.contentAudit" active-text="开启" inactive-text="关闭" />
        </el-form-item>
        <el-form-item label="用户注册">
          <el-switch v-model="settings.openRegistration" active-text="开放" inactive-text="仅管理员创建" />
        </el-form-item>
        <el-form-item label="单文件上传限制 (MB)">
          <el-input-number v-model="settings.maxFileSize" :min="1" :max="500" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存设置</el-button>
        </el-form>
      </el-form>
    </div>
  </AdminLayout>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const settings = reactive({
  platformName: '知识分享平台',
  contentAudit: true,
  openRegistration: false,
  maxFileSize: 50
})

function handleSave() {
  ElMessage.success('设置已保存（功能开发中）')
}
</script>

<style scoped>
.page-card { background: #fff; border-radius: 8px; padding: 24px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-title { font-size: 16px; font-weight: 600; margin: 0; }
</style>
```

#### Route additions

Add these 3 routes to `frontend/src/router/index.js`, before the NotFound route:

```js
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/UserManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/departments',
    name: 'AdminDepartments',
    component: () => import('@/views/admin/DepartmentManage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/settings',
    name: 'AdminSettings',
    component: () => import('@/views/admin/SystemSettings.vue'),
    meta: { requiresAuth: true },
  },
```

## Verify

```bash
cd frontend && npm run build
```

Must succeed with no errors.

## Commit

```bash
git add frontend/src/views/admin/ frontend/src/router/index.js
git commit -m "feat: add User, Department, and System Settings admin pages with routes"
```
