<template>
  <div class="main-layout">
    <AppSidebar />
    <div class="main-area">
      <AppHeader />
      <nav class="content-tabs" v-if="tabStore.tabs.length > 0">
        <div class="tabs-scroll">
          <div class="tabs-inner">
            <div
              v-for="tab in tabStore.tabs"
              :key="tab.path"
              :class="['tab-item', { active: tab.path === tabStore.currentPath }]"
              @click="goTab(tab)"
              @contextmenu.prevent="onContextMenu($event, tab)"
            >
              <i :class="tab.icon"></i>
              <span class="tab-label">{{ tab.label }}</span>
              <span
                v-if="tab.closable"
                class="tab-close"
                @click.stop="closeTab(tab)"
              >
                <i class="ri-close-line"></i>
              </span>
            </div>
          </div>
        </div>
      </nav>
      <main class="content"><slot /></main>
    </div>

    <div
      v-if="ctxMenu.visible"
      class="tab-ctx-menu"
      :style="{ left: ctxMenu.x + 'px', top: ctxMenu.y + 'px' }"
    >
      <div class="ctx-item" @click="closeOtherTabs(ctxMenu.tab); ctxMenu.visible = false">
        <i class="ri-close-circle-line"></i> 关闭其他
      </div>
      <div class="ctx-item" v-if="ctxMenu.tab?.closable" @click="closeTab(ctxMenu.tab); ctxMenu.visible = false">
        <i class="ri-close-line"></i> 关闭当前
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AppSidebar from './AppSidebar.vue'
import AppHeader from './AppHeader.vue'
import { useTabStore } from '@/stores/tabs'

const router = useRouter()
const tabStore = useTabStore()

const ctxMenu = reactive({ visible: false, x: 0, y: 0, tab: null })

function goTab(tab) {
  if (tab.path !== tabStore.currentPath) {
    router.push(tab.path)
  }
}

function closeTab(tab) {
  tabStore.closeTab(tab.path, router)
}

function closeOtherTabs(tab) {
  tabStore.closeOtherTabs(tab.path)
  if (tab.path !== tabStore.currentPath) {
    router.push(tab.path)
  }
}

function onContextMenu(e, tab) {
  ctxMenu.visible = true
  ctxMenu.x = e.clientX
  ctxMenu.y = e.clientY
  ctxMenu.tab = tab
}

function hideCtxMenu() {
  ctxMenu.visible = false
}

onMounted(() => {
  document.addEventListener('click', hideCtxMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideCtxMenu)
})
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
}

.main-area {
  margin-left: 220px;
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.content-tabs {
  background: #fff;
  border-bottom: 1px solid #E4E7ED;
  position: sticky;
  top: 56px;
  z-index: 40;
}

.tabs-scroll {
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs-scroll::-webkit-scrollbar {
  display: none;
}

.tabs-inner {
  display: flex;
  gap: 2px;
  padding: 0 24px;
  white-space: nowrap;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: 13px;
  color: #606266;
  background: none;
  border: none;
  border-radius: 6px 6px 0 0;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  position: relative;
  flex-shrink: 0;
}

.tab-item i {
  font-size: 14px;
}

.tab-label {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tab-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-left: 4px;
  transition: all 0.15s;
}

.tab-close i {
  font-size: 12px;
}

.tab-close:hover {
  background: #C0C4CC;
  color: #fff;
}

.tab-item:hover {
  background: #F5F7FA;
  color: #303133;
}

.tab-item.active {
  background: #F5F7FA;
  color: #409EFF;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #409EFF;
  border-radius: 2px 2px 0 0;
}

.content {
  padding: 24px;
  flex: 1;
  background: #F5F7FA;
}

.tab-ctx-menu {
  position: fixed;
  z-index: 999;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  padding: 6px 0;
  min-width: 140px;
}

.ctx-item {
  padding: 8px 16px;
  font-size: 13px;
  color: #303133;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ctx-item:hover {
  background: #F5F7FA;
  color: #409EFF;
}
</style>
