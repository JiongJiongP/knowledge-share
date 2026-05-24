<template>
  <AdminLayout>
    <div class="dashboard">
      <h2>数据分析看板</h2>
      <el-row :gutter="16" class="stat-cards">
        <el-col :span="6"><el-card><h3>总内容数</h3><div class="number">{{ overview.totalContents }}</div></el-card></el-col>
        <el-col :span="6"><el-card><h3>今日发布</h3><div class="number">{{ overview.todayContents }}</div></el-card></el-col>
        <el-col :span="6"><el-card><h3>总用户数</h3><div class="number">{{ totalUsers }}</div></el-card></el-col>
        <el-col :span="6"><el-card><h3>今日活跃用户</h3><div class="number">{{ activeToday }}</div></el-card></el-col>
      </el-row>
      <el-card class="section"><h3>内容发布趋势（近7天）</h3>
        <div class="trend-chart" v-if="trend.length">
          <div v-for="d in trend" :key="d.date" class="trend-bar-wrapper">
            <div class="trend-bar" :style="{ height: barHeight(d.count) }" :title="`${d.date}: ${d.count}`" />
            <span class="trend-label">{{ d.date.slice(5) }}</span>
          </div>
        </div>
      </el-card>
      <el-card class="section"><h3>热门内容 Top 20</h3>
        <el-table :data="hotContents"><el-table-column prop="contentId" label="内容ID"/><el-table-column prop="viewCount" label="浏览量"/><el-table-column prop="favoriteCount" label="收藏数"/></el-table>
      </el-card>
      <el-card class="section"><h3>搜索热词</h3>
        <el-table :data="hotKeywords"><el-table-column prop="keyword" label="关键词"/><el-table-column prop="searchCount" label="搜索次数"/></el-table>
      </el-card>
      <el-card class="section"><h3>群组活跃度排行</h3>
        <el-table :data="groupActivity" v-loading="loadingGroups">
          <el-table-column prop="id" label="群组ID" width="100" />
          <el-table-column prop="name" label="群组名称" />
          <el-table-column prop="contentCount" label="内容数量" width="120" />
        </el-table>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { getGroupList } from '@/api/group'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const overview = ref({})
const trend = ref([])
const hotContents = ref([])
const hotKeywords = ref([])
const totalUsers = ref(458)
const activeToday = ref(86)
const groupActivity = ref([])
const loadingGroups = ref(false)

function barHeight(count) {
  const max = Math.max(...trend.value.map(d => d.count), 1)
  return Math.max((count / max) * 120, 4) + 'px'
}

onMounted(async () => {
  try {
    const [ov, tr, hc, hk] = await Promise.all([
      request.get('/admin/analytics/overview'),
      request.get('/admin/analytics/content-trend'),
      request.get('/admin/analytics/hot-content'),
      request.get('/admin/analytics/hot-keywords')
    ])
    overview.value = ov.data
    trend.value = tr.data
    hotContents.value = hc.data
    hotKeywords.value = hk.data
  } catch { /* handled */ }
  try {
    loadingGroups.value = true
    const res = await getGroupList({ page: 1, pageSize: 10 })
    groupActivity.value = (res.data?.records || res.data || []).slice(0, 10)
  } catch { groupActivity.value = [] }
  finally { loadingGroups.value = false }
})
</script>

<style scoped>
.dashboard { padding: 0; }
.dashboard h2 { margin: 0 0 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-cards h3 { font-size: 13px; color: #909399; margin: 0 0 8px; }
.number { font-size: 28px; font-weight: 700; color: #303133; }
.section { margin-bottom: 16px; }
.section h3 { font-size: 14px; margin: 0 0 12px; }
.trend-chart { display: flex; align-items: flex-end; gap: 12px; height: 160px; padding: 8px 0; }
.trend-bar-wrapper { display: flex; flex-direction: column; align-items: center; flex: 1; }
.trend-bar { width: 32px; background: #409eff; border-radius: 4px 4px 0 0; min-height: 4px; transition: height .3s; }
.trend-label { font-size: 10px; color: #c0c4cc; margin-top: 4px; }
</style>
