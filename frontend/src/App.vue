<template>
  <MainLayout v-if="!isGuestRoute">
    <router-view />
  </MainLayout>
  <router-view v-else />
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useTabStore } from '@/stores/tabs'

const route = useRoute()
const tabStore = useTabStore()
const isGuestRoute = computed(() => route.meta.guest === true)

watch(() => route.fullPath, () => {
  if (!isGuestRoute.value) {
    tabStore.addTab(route)
  }
})
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
}

[class^="ri-"],
[class*=" ri-"] {
  font-size: inherit;
  vertical-align: -0.1em;
}
</style>
