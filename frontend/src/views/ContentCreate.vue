<template>
  <div class="create-page">
    <AppHeader />

    <div class="create-layout">
      <div class="create-card">
        <div class="card-header">
          <h2 class="card-title">创建新内容</h2>
          <div class="header-actions">
            <el-button @click="handleSaveDraft" :loading="saving">💾 保存草稿</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">📤 提交审核</el-button>
          </div>
        </div>

        <!-- Content type selector -->
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

        <!-- Title -->
        <div class="form-group">
          <label class="form-label">标题</label>
          <el-input v-model="form.title" placeholder="请输入内容标题" maxlength="256" show-word-limit />
        </div>

        <!-- Markdown editor -->
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
              ref="editorRef"
              v-model="form.body"
              type="textarea"
              :rows="15"
              placeholder="使用 Markdown 语法编写内容...&#10;&#10;## 概述&#10;在这里编写你的知识内容..."
              class="editor-textarea"
            />
          </div>
          <p class="form-hint">支持 Markdown 语法：标题、粗体、斜体、代码块、表格、图片、视频嵌入</p>
        </div>

        <!-- Tag selector -->
        <div class="form-group">
          <label class="form-label">选择标签</label>
          <TagSelector v-model="form.tagIds" />
        </div>

        <!-- Publish to groups -->
        <div class="form-group">
          <label class="form-label">发布到群组</label>
          <div class="group-checkboxes">
            <el-checkbox
              v-for="g in groups"
              :key="g.id"
              v-model="form.groupIds"
              :label="g.id"
              :value="g.id"
            >{{ g.name }}</el-checkbox>
            <span v-if="groups.length === 0" class="no-groups">暂无可用群组</span>
          </div>
        </div>

        <!-- Scheduled publish -->
        <div class="form-group">
          <label class="form-label">定时发布（可选）</label>
          <el-input v-model="form.scheduledAt" type="datetime-local" style="width:260px;" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createContent, saveDraft, publishContent } from '@/api/content'
import { getGroupList } from '@/api/group'
import AppHeader from '@/components/layout/AppHeader.vue'
import TagSelector from '@/components/content/TagSelector.vue'

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
  groupIds: [],
  scheduledAt: ''
})

const groups = ref([])
const saving = ref(false)
const submitting = ref(false)

onMounted(async () => {
  try {
    const res = await getGroupList({ size: 100 })
    groups.value = res.data?.records || res.data || []
  } catch { groups.value = [] }
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

async function handleSaveDraft() {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  saving.value = true
  try {
    const payload = {
      title: form.title,
      body: form.body,
      contentType: form.contentType
    }
    const res = await createContent(payload)
    const id = res.data?.id
    if (id) {
      await saveDraft(id, payload).catch(() => {})
      ElMessage.success('草稿已保存')
      router.push(`/content/${id}`)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '保存失败')
  } finally { saving.value = false }
}

async function handleSubmit() {
  if (!validate()) return
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      body: form.body,
      contentType: form.contentType
    }
    const res = await createContent(payload)
    const id = res.data?.id
    if (id) {
      await publishContent(id).catch(() => {})
      ElMessage.success('内容已提交')
      router.push(`/content/${id}`)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.create-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.create-layout {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px;
}
.create-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
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
.group-checkboxes {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.no-groups {
  font-size: 13px;
  color: #909399;
}
</style>
