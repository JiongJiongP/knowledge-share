<template>
  <div class="home-page">
    <AppHeader>
      <template #center>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索知识内容..."
          :prefix-icon="Search"
          size="small"
          class="search-input"
          clearable
          @keyup.enter="onSearch"
        />
      </template>
    </AppHeader>

    <div class="main-layout">
      <aside class="sidebar">
        <div class="sidebar-card">
          <h4>内容类型</h4>
          <el-radio-group v-model="filterType" @change="fetchList" size="small">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="MARKDOWN">文章</el-radio-button>
            <el-radio-button value="PPT_FILE">PPT</el-radio-button>
            <el-radio-button value="EXTERNAL_URL">链接</el-radio-button>
          </el-radio-group>
        </div>
        <div class="sidebar-card">
          <h4>群组筛选</h4>
          <el-select v-model="filterGroup" placeholder="全部群组" size="small" @change="fetchList" clearable style="width:100%">
            <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </div>
      </aside>

      <main class="content-area">
        <div class="toolbar">
          <SortBar v-model="sort" @change="fetchList" />
          <el-button type="primary" size="small" @click="$router.push('/content/create')">
            <el-icon><Plus /></el-icon> 写文章
          </el-button>
        </div>

        <div v-if="loading" class="loading-wrapper">
          <el-skeleton :rows="3" animated />
        </div>

        <el-empty v-else-if="list.length === 0" description="暂无内容，去写第一篇文章吧" />

        <div v-if="tags.length > 0" class="tag-chips">
          <span
            v-for="tag in selectedTagList"
            :key="tag.id"
            class="tag-chip active"
            :style="{ background: tag.color, color: '#fff' }"
            @click="toggleTag(tag)"
          >{{ tag.name }}</span>
          <span
            v-for="tag in availableTags"
            :key="tag.id"
            class="tag-chip"
            :style="{ borderColor: tag.color, color: tag.color }"
            @click="toggleTag(tag)"
          >{{ tag.name }}</span>
        </div>

        <ContentCard v-for="item in list" :key="item.id" :content="item" />

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
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { getContentList } from '@/api/content'
import { getGroupList } from '@/api/group'
import { getTags } from '@/api/tag'
import AppHeader from '@/components/layout/AppHeader.vue'
import SortBar from '@/components/common/SortBar.vue'
import ContentCard from '@/components/content/ContentCard.vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const sort = ref('latest')
const filterType = ref('')
const searchKeyword = ref('')
const filterGroup = ref('')
const groups = ref([])
const tags = ref([])
const selectedTagIds = ref([])

const selectedTagList = computed(() => tags.value.filter(t => selectedTagIds.value.includes(t.id)))
const availableTags = computed(() => tags.value.filter(t => !selectedTagIds.value.includes(t.id)))

async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value, sort: sort.value }
    if (filterType.value) params.contentType = filterType.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (filterGroup.value) params.groupId = filterGroup.value
    if (selectedTagIds.value.length > 0) params.tagIds = selectedTagIds.value.join(',')
    const res = await getContentList(params)
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

function toggleTag(tag) {
  const idx = selectedTagIds.value.indexOf(tag.id)
  if (idx >= 0) {
    selectedTagIds.value.splice(idx, 1)
  } else {
    selectedTagIds.value.push(tag.id)
  }
  page.value = 1
  fetchList()
}

async function fetchFilters() {
  try {
    const [groupRes, tagRes] = await Promise.all([
      getGroupList({ size: 100 }),
      getTags()
    ])
    groups.value = groupRes.data?.records || groupRes.data || []
    tags.value = tagRes.data || []
  } catch { /* filters are optional */ }
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchFilters()])
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f7fa;
}
.search-input {
  max-width: 320px;
  width: 100%;
}
.main-layout {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  gap: 20px;
}
.sidebar {
  width: 180px;
  flex-shrink: 0;
}
.sidebar-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #ebeef5;
  position: sticky;
  top: 80px;
}
.sidebar-card h4 {
  font-size: 14px;
  margin: 0 0 12px;
  color: #303133;
}
.type-filter {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.content-area {
  flex: 1;
  min-width: 0;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.tag-chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid;
  background: transparent;
  transition: all .2s;
  cursor: pointer;
}
.tag-chip.active {
  color: #fff;
}
.tag-chip:hover {
  opacity: 0.8;
}
.loading-wrapper {
  padding: 40px 0;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
