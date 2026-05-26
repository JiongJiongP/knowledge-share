<template>
  <div class="edit-page">
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="8" animated />
    </div>

    <el-result
      v-else-if="error"
      icon="error"
      title="加载失败"
      :sub-title="error"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>

    <div v-else class="page-card">
      <div class="card-header">
        <h2 class="card-title">编辑内容</h2>
        <div class="header-actions">
          <el-button @click="handleUpdate" :loading="saving">💾 保存</el-button>
          <el-button type="success" @click="handlePublish" :loading="publishing" v-if="form.status === 'DRAFT'">
            📤 发布
          </el-button>
        </div>
      </div>

        <div class="form-group">
          <label class="form-label">内容类型</label>
          <div class="type-selector">
            <el-button
              v-for="t in contentTypes"
              :key="t.value"
              :type="form.contentType === t.value ? 'primary' : 'default'"
              size="small"
              @click="form.contentType = t.value"
            >{{ t.emoji }} {{ t.label }}</el-button>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">标题</label>
          <el-input v-model="form.title" placeholder="请输入内容标题" maxlength="256" show-word-limit />
        </div>

        <div class="form-group">
          <label class="form-label">正文（Markdown 编辑器）</label>
          <div class="editor-wrapper">
            <div class="editor-toolbar">
              <el-button size="small" text @click="insertMarkdown('**', '**')"><b>B</b></el-button>
              <el-button size="small" text @click="insertMarkdown('*', '*')"><i>I</i></el-button>
              <el-button size="small" text @click="insertMarkdown('\n## ', '')">H</el-button>
              <el-button size="small" text @click="insertMarkdown('`', '`')">&lt;/&gt;</el-button>
              <el-button size="small" text @click="insertMarkdown('[', '](url)')">🔗</el-button>
              <el-button size="small" text @click="insertMarkdown('![alt](', ')')">🖼️</el-button>
              <el-button size="small" text @click="insertMarkdown('\n| Col1 | Col2 |\n|-----|-----|\n| ', ' |')">📊</el-button>
              <el-button size="small" text @click="insertMarkdown('\n> ', '')">❝</el-button>
            </div>
            <el-input
              v-model="form.body"
              type="textarea"
              :rows="15"
              placeholder="使用 Markdown 语法编写内容..."
              class="editor-textarea"
            />
          </div>
          <p class="form-hint">支持 Markdown 语法：标题、粗体、斜体、代码块、表格、图片、视频嵌入</p>
        </div>

        <div class="form-group">
          <label class="form-label">选择标签</label>
          <TagSelector v-model="form.tagIds" />
        </div>

        <div class="form-group">
          <label class="form-label">定时发布（可选）</label>
          <el-input v-model="form.scheduledAt" type="datetime-local" style="width:260px;" />
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContent, updateContent, publishContent, schedulePublish } from '@/api/content'
import { getContentTags, setContentTags } from '@/api/tag'
import TagSelector from '@/components/content/TagSelector.vue'

const route = useRoute()
const router = useRouter()

const contentTypes = [
  { value: 'MARKDOWN', label: 'Markdown', emoji: '📝' },
  { value: 'PPT_FILE', label: 'PPT文件', emoji: '📊' },
  { value: 'EXTERNAL_URL', label: '外部链接', emoji: '🔗' },
  { value: 'INTERNAL_REF', label: '内部引用', emoji: '📎' }
]

const form = reactive({
  title: '',
  body: '',
  contentType: 'MARKDOWN',
  tagIds: [],
  scheduledAt: '',
  status: 'DRAFT'
})

const loading = ref(true)
const error = ref('')
const saving = ref(false)
const publishing = ref(false)

onMounted(async () => {
  try {
    const id = route.params.id
    const [contentRes, tagRes] = await Promise.all([
      getContent(id),
      getContentTags(id).catch(() => ({ data: [] }))
    ])
    const c = contentRes.data
    form.title = c.title || ''
    form.body = c.body || ''
    form.contentType = c.contentType || 'MARKDOWN'
    form.status = c.status || 'DRAFT'
    form.tagIds = (tagRes.data || []).map(t => t.id)
  } catch (e) {
    error.value = e.response?.data?.message || '内容加载失败'
  } finally {
    loading.value = false
  }
})

function insertMarkdown(before, after) {
  const el = document.querySelector('.editor-textarea textarea, .editor-textarea .el-textarea__inner')
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = form.body.substring(start, end)
  const replacement = before + selected + after
  form.body = form.body.substring(0, start) + replacement + form.body.substring(end)
  nextTick(() => {
    el.focus()
    const cursor = start + replacement.length
    el.setSelectionRange(cursor, cursor)
  })
}

function validate() {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return false }
  if (!form.body.trim()) { ElMessage.warning('请输入正文'); return false }
  return true
}

async function handleUpdate() {
  if (!validate()) return
  saving.value = true
  try {
    const id = route.params.id
    const payload = {
      title: form.title,
      body: form.body,
      contentType: form.contentType
    }
    await updateContent(id, payload)
    if (form.tagIds.length > 0) {
      await setContentTags(id, form.tagIds).catch(() => ElMessage.warning('标签保存失败'))
    }
    ElMessage.success('内容已保存')
    router.push(`/content/${id}`)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally { saving.value = false }
}

async function handlePublish() {
  if (!validate()) return
  publishing.value = true
  try {
    const id = route.params.id
    const payload = {
      title: form.title,
      body: form.body,
      contentType: form.contentType
    }
    await updateContent(id, payload)
    if (form.tagIds.length > 0) {
      await setContentTags(id, form.tagIds).catch(() => ElMessage.warning('标签保存失败'))
    }
    await publishContent(id).catch(() => ElMessage.warning('发布失败'))
    if (form.scheduledAt) {
      await schedulePublish(id, form.scheduledAt).catch(() => ElMessage.warning('定时发布设置失败'))
    }
    ElMessage.success('内容已发布')
    router.push(`/content/${id}`)
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发布失败')
  } finally { publishing.value = false }
}
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 24px 20px;
}
.loading-wrapper {
  max-width: 800px;
  margin: 60px auto;
  padding: 0 20px;
}
.page-card {
  max-width: 900px;
  margin: 0 auto;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.form-group {
  margin-bottom: 20px;
}
.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #303133;
}
.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}
.type-selector {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color .2s;
}
.editor-wrapper:focus-within {
  border-color: #409eff;
}
.editor-toolbar {
  display: flex;
  gap: 2px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  flex-wrap: wrap;
}
.editor-textarea :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  box-shadow: none;
  resize: vertical;
}
.editor-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}
</style>
