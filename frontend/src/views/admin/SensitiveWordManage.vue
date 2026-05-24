<template>
  <AdminLayout>
    <div class="sw-manage">
      <div class="toolbar">
        <h2>敏感词管理</h2>
        <div class="toolbar-right">
          <el-button @click="showBatch = true">批量导入</el-button>
          <el-button type="primary" @click="showAdd = true">添加敏感词</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-select v-model="filterCategory" placeholder="全部分类" @change="onFilterChange" style="width:160px">
          <el-option label="全部分类" value="" />
          <el-option label="通用敏感词" value="GENERAL" />
          <el-option label="政治敏感" value="POLITICAL" />
          <el-option label="广告推广" value="ADVERTISING" />
        </el-select>
        <el-input v-model="searchKeyword" placeholder="搜索敏感词" clearable @input="onFilterChange" style="width:220px" />
      </div>
      <el-table :data="words" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" width="200" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="categoryType(row.category)" size="small">{{ categoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="添加时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Add dialog -->
      <el-dialog v-model="showAdd" title="添加敏感词" width="400px" @closed="resetAddForm">
        <el-form ref="addFormRef" :model="addForm" :rules="rules" label-position="top">
          <el-form-item label="敏感词" prop="word">
            <el-input v-model="addForm.word" placeholder="输入敏感词" maxlength="64" />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-select v-model="addForm.category" style="width:100%">
              <el-option label="通用" value="GENERAL" />
              <el-option label="政治" value="POLITICAL" />
              <el-option label="广告" value="ADVERTISING" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAdd = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleAdd">添加</el-button>
        </template>
      </el-dialog>

      <!-- Batch import dialog -->
      <el-dialog v-model="showBatch" title="批量导入敏感词" width="500px">
        <el-form label-position="top">
          <el-form-item label="每行一个敏感词">
            <el-input v-model="batchText" type="textarea" :rows="8" placeholder="每行一个敏感词" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="batchCategory" style="width:100%">
              <el-option label="通用" value="GENERAL" />
              <el-option label="政治" value="POLITICAL" />
              <el-option label="广告" value="ADVERTISING" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showBatch = false">取消</el-button>
          <el-button type="primary" :loading="importing" @click="handleBatchImport">导入</el-button>
        </template>
      </el-dialog>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSensitiveWords, addSensitiveWord, deleteSensitiveWord, batchImportSensitiveWords } from '@/api/sensitive-word'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const words = ref([])
const loading = ref(false)
const saving = ref(false)
const importing = ref(false)
const filterCategory = ref('')
const searchKeyword = ref('')

const showAdd = ref(false)
const addFormRef = ref(null)
const addForm = reactive({ word: '', category: 'GENERAL' })
const rules = { word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }] }

const showBatch = ref(false)
const batchText = ref('')
const batchCategory = ref('GENERAL')

function categoryType(cat) {
  return { GENERAL: '', POLITICAL: 'danger', ADVERTISING: 'warning' }[cat] || ''
}
function categoryLabel(cat) {
  return { GENERAL: '通用', POLITICAL: '政治', ADVERTISING: '广告' }[cat] || cat
}

async function fetchWords() {
  loading.value = true
  try {
    const params = {}
    if (filterCategory.value) params.category = filterCategory.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getSensitiveWords(params)
    words.value = res.data || []
  } finally { loading.value = false }
}

function onFilterChange() { fetchWords() }

async function handleAdd() {
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await addSensitiveWord({ word: addForm.word, category: addForm.category })
    ElMessage.success('已添加')
    showAdd.value = false
    await fetchWords()
  } catch { /* handled */ }
  finally { saving.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除"${row.word}"吗？`, '确认', { type: 'warning' })
    await deleteSensitiveWord(row.id)
    ElMessage.success('已删除')
    await fetchWords()
  } catch { /* cancelled or error */ }
}

async function handleBatchImport() {
  const lines = batchText.value.split('\n').map(s => s.trim()).filter(Boolean)
  if (!lines.length) return ElMessage.warning('请输入敏感词')
  importing.value = true
  try {
    const res = await batchImportSensitiveWords(lines, batchCategory.value)
    ElMessage.success(`成功导入 ${res.data.imported} 个敏感词`)
    showBatch.value = false
    batchText.value = ''
    await fetchWords()
  } catch { /* handled */ }
  finally { importing.value = false }
}

function resetAddForm() { addFormRef.value?.resetFields() }

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(fetchWords)
</script>

<style scoped>
.sw-manage { background: #fff; border-radius: 8px; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.toolbar h2 { margin: 0; font-size: 18px; }
.toolbar-right { display: flex; gap: 10px; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
