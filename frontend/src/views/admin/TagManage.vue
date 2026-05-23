<template>
  <AdminLayout>
    <div class="tag-manage">
      <div class="toolbar">
        <h2>标签管理</h2>
        <el-button type="primary" @click="showAdd">添加标签</el-button>
      </div>

      <el-table :data="tags" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" width="200" />
        <el-table-column prop="color" label="颜色" width="120">
          <template #default="{ row }">
            <span class="color-block" :style="{ background: row.color }" /> {{ row.color }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="showEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" :title="editingId ? '编辑标签' : '添加标签'" width="400px" @closed="resetForm">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="标签名称" prop="name">
            <el-input v-model="form.name" placeholder="输入标签名称" maxlength="64" />
          </el-form-item>
          <el-form-item label="颜色" prop="color">
            <el-color-picker v-model="form.color" show-alpha />
            <span style="margin-left:8px;font-size:12px;color:#909399">{{ form.color }}</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTags, createTag, updateTag, deleteTag } from '@/api/tag'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const tags = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', color: '#409EFF' })
const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

async function fetchTags() {
  try {
    const res = await getTags()
    tags.value = res.data
  } catch { /* handled */ }
}

function showAdd() {
  editingId.value = null
  form.name = ''
  form.color = '#409EFF'
  dialogVisible.value = true
}

function showEdit(row) {
  editingId.value = row.id
  form.name = row.name
  form.color = row.color
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateTag(editingId.value, { name: form.name, color: form.color })
      ElMessage.success('标签已更新')
    } else {
      await createTag({ name: form.name, color: form.color })
      ElMessage.success('标签已创建')
    }
    dialogVisible.value = false
    await fetchTags()
  } catch { /* handled by interceptor */ }
  finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除标签"${row.name}"吗？关联的内容标签也会移除。`, '确认删除', { type: 'warning' })
    await deleteTag(row.id)
    ElMessage.success('标签已删除')
    await fetchTags()
  } catch { /* cancelled or error */ }
}

function resetForm() {
  formRef.value?.resetFields()
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(fetchTags)
</script>

<style scoped>
.tag-manage { background: #fff; border-radius: 8px; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.toolbar h2 { margin: 0; font-size: 18px; }
.color-block { display: inline-block; width: 16px; height: 16px; border-radius: 4px; vertical-align: middle; margin-right: 4px; }
</style>
