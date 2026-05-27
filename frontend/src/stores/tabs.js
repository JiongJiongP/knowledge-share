import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const routeMetaMap = {
  Home: { label: '首页', icon: 'ri-home-5-line', closable: false },
  ContentCreate: { label: '创建内容', icon: 'ri-edit-line', closable: true },
  ContentDetail: { label: '内容详情', icon: 'ri-file-text-line', closable: true },
  ContentEdit: { label: '编辑内容', icon: 'ri-edit-line', closable: true },
  Groups: { label: '群组', icon: 'ri-team-line', closable: true },
  GroupDetail: { label: '群组详情', icon: 'ri-team-line', closable: true },
  GroupManage: { label: '群组管理', icon: 'ri-settings-3-line', closable: true },
  Favorites: { label: '我的收藏', icon: 'ri-star-line', closable: true },
  Notifications: { label: '通知消息', icon: 'ri-notification-3-line', closable: true },
  Templates: { label: '模板中心', icon: 'ri-file-list-3-line', closable: true },
  AdminTags: { label: '标签管理', icon: 'ri-price-tag-3-line', closable: true },
  AdminSensitiveWords: { label: '敏感词管理', icon: 'ri-forbid-line', closable: true },
  AdminAnalytics: { label: '数据分析', icon: 'ri-bar-chart-box-line', closable: true },
  AdminAudit: { label: '审核中心', icon: 'ri-checkbox-circle-line', closable: true },
  AdminUsers: { label: '用户管理', icon: 'ri-user-line', closable: true },
  AdminDepartments: { label: '部门管理', icon: 'ri-building-line', closable: true },
  AdminSettings: { label: '系统设置', icon: 'ri-settings-3-line', closable: true },
}

const MAX_TABS = 15

export const useTabStore = defineStore('tabs', () => {
  const tabs = ref([
    { path: '/', name: 'Home', label: '首页', icon: 'ri-home-5-line', closable: false }
  ])

  const currentPath = ref('/')

  function addTab(route) {
    if (route.meta?.guest) return
    if (route.name === 'NotFound') return

    const meta = routeMetaMap[route.name] || { label: route.name || route.path, icon: 'ri-file-line', closable: true }

    const existing = tabs.value.find(t => t.name === route.name && t.path === route.fullPath)
    if (existing) {
      currentPath.value = route.fullPath
      return
    }

    const sameNameTabs = tabs.value.filter(t => t.name === route.name)
    if (sameNameTabs.length >= 5) {
      const oldest = sameNameTabs[0]
      const idx = tabs.value.indexOf(oldest)
      tabs.value.splice(idx, 1)
    }

    tabs.value.push({
      path: route.fullPath,
      name: route.name,
      label: meta.label,
      icon: meta.icon,
      closable: meta.closable,
    })

    if (tabs.value.length > MAX_TABS) {
      const firstClosable = tabs.value.findIndex(t => t.closable)
      if (firstClosable > 0) tabs.value.splice(firstClosable, 1)
    }

    currentPath.value = route.fullPath
  }

  function closeTab(path, router) {
    const idx = tabs.value.findIndex(t => t.path === path)
    if (idx < 0) return
    const tab = tabs.value[idx]
    if (!tab.closable) return

    tabs.value.splice(idx, 1)

    if (currentPath.value === path) {
      const next = tabs.value[Math.min(idx, tabs.value.length - 1)]
      if (next) {
        currentPath.value = next.path
        router.push(next.path)
      }
    }
  }

  function closeOtherTabs(path) {
    tabs.value = tabs.value.filter(t => !t.closable || t.path === path)
  }

  return { tabs, currentPath, addTab, closeTab, closeOtherTabs }
})
