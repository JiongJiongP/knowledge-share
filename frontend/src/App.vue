<template>
  <MainLayout v-if="!isGuestRoute">
    <router-view />
  </MainLayout>
  <router-view v-else />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/components/layout/MainLayout.vue'

const route = useRoute()
const isGuestRoute = computed(() => route.meta.guest === true)
</script>
