<template>
  <div class="create-page">
    <div class="create-card">
      <div class="card-header">
        <div class="card-title">创建新内容</div>
        <div class="btn-group">
          <button class="btn btn-default btn-sm" @click="handleSaveDraft" :disabled="saving">💾 保存草稿</button>
          <button class="btn btn-primary btn-sm" @click="handleSubmit" :disabled="submitting">📤 提交审核</button>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">内容类型</label>
        <div class="type-selector">
          <button
            v-for="t in contentTypes"
            :key="t.value"
            :class="['btn', form.contentType === t.value ? 'btn-primary' : 'btn-default', 'btn-sm']"
            @click="form.contentType = t.value"
          >{{ t.emoji }} {{ t.label }}</button>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">标题</label>
        <input class="form-input" v-model="form.title" placeholder="请输入内容标题" maxlength="256" />
      </div>

      <div class="form-group">
        <label class="form-label">正文（Markdown 编辑器）</label>
        <div class="editor-wrapper">
          <div class="editor-toolbar">
            <button class="btn btn-default btn-sm" @click="insertMarkdown('**', '**')"><b>B</b></button>
            <button class="btn btn-default btn-sm" @click="insertMarkdown('*', '*')"><i>I</i></button>
            <button class="btn btn-default btn-sm" @click="insertMarkdown('\n## ', '')">H</button>
            <button class="btn btn-default btn-sm" @click="insertMarkdown('`', '`')">&lt;/&gt;</button>
            <button class="btn btn-default btn-sm" @click="insertMarkdown('[', '](url)')">🔗</button>
            <button class="btn btn-default btn-sm" @click="insertMarkdown('![alt](', ')')">🖼️</button>
            <button class="btn btn-default btn-sm" @click="insertMarkdown('\n| Col1 | Col2 |\n|-----|-----|\n| ', ' |')">📊</button>
            <button class="btn btn-default btn-sm" @click="insertMarkdown('\n> ', '')">❝</button>
          </div>
          <textarea
            v-model="form.body"
            class="editor-textarea"
            placeholder="使用 Markdown 语法编写内容...&#10;&#10;## 概述&#10;在这里编写你的知识内容...&#10;&#10;支持：图片插入、代码块高亮、表格、视频嵌入"
            style="min-height:300px;"
          ></textarea>
        </div>
        <p class="form-hint">支持 Markdown 语法：标题、粗体、斜体、代码块、表格、图片、视频嵌入</p>
      </div>

      <div class="form-group">
        <label class="form-label">选择标签</label>
        <TagSelector v-model="form.tagIds" />
      </div>

      <div class="form-group">
        <label class="form-label">发布到群组</label>
        <div v-if="groups.length > 0" class="group-checkboxes">
          <label v-for="g in groups" :key="g.id" class="group-check">
            <input type="checkbox" :value="g.id" v-model="form.groupIds" /> {{ g.name }}
          </label>
        </div>
        <p v-else class="no-groups">暂无可用群组</p>
      </div>

      <div class="form-group">
        <label class="form-label">定时发布（可选）</label>
        <input class="form-input" type="datetime-local" style="width:260px;" v-model="form.scheduledAt" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createContent, publishContent, schedulePublish } from '@/api/content'
import { getGroupList } from '@/api/group'
import TagSelector from '@/components/content/TagSelector.vue'
import { setContentTags } from '@/api/tag'

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

const saving = ref(false)
const submitting = ref(false)
const groups = ref([])

function insertMarkdown(before, after) {
  const el = document.querySelector('.editor-textarea')
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = form.body.substring(start, end)
  const replacement = before + selected + after
  form.body = form.body.substring(0, start) + replacement + form.body.substring(end)
  nextTick(() => {
    el.focus()
    el.setSelectionRange(start + replacement.length, start + replacement.length)
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
      contentType: form.contentType,
      groupIds: form.groupIds
    }
    const res = await createContent(payload)
    const id = res.data?.id
    if (id) {
      if (form.tagIds.length > 0) await setContentTags(id, form.tagIds).catch(() => {})
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
      contentType: form.contentType,
      groupIds: form.groupIds
    }
    const res = await createContent(payload)
    const id = res.data?.id
    if (id) {
      if (form.tagIds.length > 0) await setContentTags(id, form.tagIds).catch(() => {})
      await publishContent(id).catch(() => ElMessage.warning('发布失败'))
      if (form.scheduledAt) await schedulePublish(id, form.scheduledAt).catch(() => ElMessage.warning('定时发布设置失败'))
      ElMessage.success('内容已提交审核')
      router.push(`/content/${id}`)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '提交失败')
  } finally { submitting.value = false }
}

onMounted(async () => {
  try {
    const res = await getGroupList({ size: 100 })
    groups.value = res.data?.records || res.data || []
  } catch { /* optional */ }
})
</script>

<style scoped>
.create-page { max-width: 900px; margin: 0 auto; }

.create-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,.08);
  padding: 24px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}
.btn-group { display: flex; gap: 8px; }

.form-group { margin-bottom: 18px; }
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #303133;
}
.form-hint { font-size: 12px; color: #909399; margin-top: 4px; }
.form-input {
  height: 36px;
  border: 1px solid #DCDFE6;
  border-radius: 6px;
  padding: 0 12px;
  font-size: 13px;
  outline: none;
  width: 100%;
  transition: border .2s;
  font-family: inherit;
  box-sizing: border-box;
}
.form-input:focus { border-color: #409EFF; }

.type-selector { display: flex; gap: 8px; flex-wrap: wrap; }

.editor-wrapper {
  border: 1px solid #DCDFE6;
  border-radius: 8px;
  overflow: hidden;
}
.editor-wrapper:focus-within { border-color: #409EFF; }
.editor-toolbar {
  display: flex;
  gap: 4px;
  padding: 8px 12px;
  background: #F5F7FA;
  border-bottom: 1px solid #E4E7ED;
  flex-wrap: wrap;
}
.editor-textarea {
  width: 100%;
  min-height: 300px;
  border: none;
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.7;
  resize: vertical;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
}

.group-checkboxes { display: flex; gap: 16px; flex-wrap: wrap; }
.group-check {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #606266;
}
.no-groups { font-size: 13px; color: #909399; }

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all .2s;
  font-family: inherit;
  background: none;
}
.btn-primary { background: #409EFF; color: #fff; border-color: #409EFF; }
.btn-primary:hover { background: #66b1ff; }
.btn-default { background: #fff; color: #303133; border-color: #DCDFE6; }
.btn-default:hover { color: #409EFF; border-color: #409EFF; }
.btn-sm { padding: 4px 10px; font-size: 12px; }
.btn:disabled { opacity: .5; cursor: not-allowed; }
</style>
