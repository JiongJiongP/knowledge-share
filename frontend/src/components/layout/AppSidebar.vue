<template>
  <aside class="app-sidebar">
    <div class="sidebar-brand" @click="$router.push('/')">
      <span>知享</span>
      <span class="brand-sub">Knowledge Hub</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      router
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
    >
      <el-menu-item index="/admin/tags">
        <el-icon><PriceTag /></el-icon>
        <span>标签管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/users">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/departments">
        <el-icon><OfficeBuilding /></el-icon>
        <span>部门管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/sensitive-words">
        <el-icon><WarningFilled /></el-icon>
        <span>敏感词管理</span>
      </el-menu-item>
      <el-menu-item index="/admin/analytics">
        <el-icon><DataAnalysis /></el-icon>
        <span>数据分析看板</span>
      </el-menu-item>
      <el-menu-item index="/admin/settings">
        <el-icon><Setting /></el-icon>
        <span>系统配置</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  PriceTag, User, OfficeBuilding, WarningFilled, DataAnalysis, Setting
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.app-sidebar {
  width: 200px;
  min-height: 100vh;
  background-color: #304156;
  flex-shrink: 0;
}
.sidebar-brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.brand-sub {
  font-size: 11px;
  font-weight: 400;
  color: #8492a6;
}
.el-menu {
  border-right: none;
}
</style>
