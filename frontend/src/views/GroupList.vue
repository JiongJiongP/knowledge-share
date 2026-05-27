<template>
  <PageCard title="群组列表">
    <template #header>
      <div class="header-actions">
        <el-input v-model="searchKeyword" placeholder="搜索群组..." size="small" style="width:200px;" clearable @input="onSearch" />
        <el-button type="primary" size="small" @click="showCreate = true">+ 申请创建群组</el-button>
      </div>
    </template>

    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="3" animated />
    </div>

    <el-empty v-else-if="list.length === 0" description="暂无公开群组" />

    <div v-else class="group-grid">
      <div v-for="group in list" :key="group.id" class="group-card">
        <div class="group-header">
          <div>
            <div class="group-name" @click="$router.push(`/group/${group.id}`)"><i class="ri-team-line"></i> {{ group.name }}</div>
            <div class="group-meta">群主：{{ group.ownerName || group.ownerId || '未知' }} · {{ group.memberCount || 0 }} 成员 · {{ group.contentCount || 0 }} 内容</div>
          </div>
          <el-button
            :type="group.joined ? 'default' : 'primary'"
            size="small"
            @click.stop="handleJoin(group)"
          >{{ group.joined ? '已加入' : '申请加入' }}</el-button>
        </div>
        <p class="group-desc">{{ group.description || '暂无描述' }}</p>
      </div>
    </div>

    <div v-if="total > size" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        @current-change="fetchList"
        background
        layout="prev, pager, next"
      />
    </div>

    <el-dialog v-model="showCreate" title="创建群组" width="480px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="群组名称" prop="name">
          <el-input v-model="form.name" placeholder="输入群组名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="群组描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="输入群组描述（选填）" maxlength="512" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </PageCard>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGroupList, createGroup, joinGroup } from '@/api/group'
import PageCard from '@/components/common/PageCard.vue'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const searchKeyword = ref('')
const showCreate = ref(false)
const creating = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入群组名称', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, pageSize: size.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getGroupList(params)
    const data = res.data?.records || res.data || []
    list.value = Array.isArray(data) ? data : []
    total.value = res.data?.total || list.value.length
  } catch { /* handled */ }
  finally { loading.value = false }
}

function onSearch() {
  page.value = 1
  fetchList()
}

async function handleJoin(group) {
  if (group.joined) return
  try {
    await joinGroup(group.id)
    ElMessage.success('申请已提交')
    fetchList()
  } catch { /* handled */ }
}

async function handleCreate() {
  const valid = formRef.value?.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createGroup(form)
    ElMessage.success('群组创建成功')
    showCreate.value = false
    resetForm()
    fetchList()
  } catch { /* handled */ }
  finally { creating.value = false }
}

function resetForm() {
  form.name = ''
  form.description = ''
  formRef.value?.resetFields()
}

onMounted(fetchList)
</script>

<style scoped>
.header-actions { display: flex; gap: 10px; align-items: center; }
.group-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.group-card { border: 1px solid #E4E7ED; border-radius: 8px; padding: 20px; transition: all .2s; }
.group-card:hover { border-color: #409EFF; box-shadow: 0 2px 12px rgba(64,158,255,.15); }
.group-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.group-name { font-weight: 600; font-size: 15px; margin-bottom: 4px; cursor: pointer; color: #303133; }
.group-name:hover { color: #409EFF; }
.group-meta { font-size: 12px; color: #909399; }
.group-desc { font-size: 13px; color: #606266; line-height: 1.6; margin: 0; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: center; }

@media (max-width: 1200px) {
  .group-grid { grid-template-columns: 1fr; }
}
</style>
