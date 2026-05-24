<template>
  <div class="group-page">
    <AppHeader />

    <div class="main-layout">
      <div class="toolbar">
        <h2>群组列表</h2>
        <div style="display:flex;gap:8px;">
          <el-input v-model="searchKeyword" placeholder="搜索群组..." size="small" style="width:200px;" clearable @input="onSearch" />
          <el-button type="primary" @click="showCreate = true">创建群组</el-button>
        </div>
      </div>

      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="3" animated />
      </div>

      <el-empty v-else-if="list.length === 0" description="暂无公开群组" />

      <el-row v-else :gutter="16">
        <el-col v-for="group in list" :key="group.id" :span="12">
          <el-card class="group-card" shadow="hover">
            <div class="group-header">
              <div>
                <h3 class="group-name" @click="$router.push(`/group/${group.id}`)">{{ group.name }}</h3>
                <div class="group-meta">
                  <span>群主: {{ group.ownerId }}</span>
                  <span>{{ group.memberCount || 0 }} 成员</span>
                  <span>{{ group.contentCount || 0 }} 内容</span>
                </div>
              </div>
              <el-button
                :type="group.joined ? 'default' : 'primary'"
                size="small"
                @click.stop="handleJoin(group)"
              >{{ group.joined ? '已加入' : '申请加入' }}</el-button>
            </div>
            <p class="group-desc">{{ group.description || '暂无描述' }}</p>
          </el-card>
        </el-col>
      </el-row>

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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGroupList, createGroup, joinGroup } from '@/api/group'
import AppHeader from '@/components/layout/AppHeader.vue'

const router = useRouter()

const list = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(12)
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
    const params = { page: page.value, size: size.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await getGroupList(params)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  fetchList()
}

async function handleJoin(group) {
  try {
    await joinGroup(group.id)
    group.joined = true
    ElMessage.success('已申请加入')
  } catch { /* handled by interceptor */ }
}

async function handleCreate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    const res = await createGroup(form)
    showCreate.value = false
    ElMessage.success('群组创建成功')
    router.push(`/group/${res.data.id}`)
  } catch { /* handled by interceptor */ }
  finally { creating.value = false }
}

function resetForm() {
  formRef.value?.resetFields()
}

onMounted(fetchList)
</script>

<style scoped>
.group-page { min-height: 100vh; background: #f5f7fa; }
.main-layout { max-width: 1200px; margin: 0 auto; padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.toolbar h2 { margin: 0; font-size: 20px; }
.group-card { margin-bottom: 16px; cursor: pointer; }
.group-name { margin: 0 0 8px; font-size: 16px; color: #303133; }
.group-desc { color: #909399; font-size: 13px; margin: 0 0 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.group-meta { font-size: 12px; color: #c0c4cc; display:flex; gap:12px; margin-top:4px; }
.group-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.pagination-wrapper { display: flex; justify-content: center; margin-top: 24px; }
.loading-wrapper { padding: 40px 0; }
</style>
