<template>
  <div class="sort-bar">
    <el-radio-group v-model="model" @change="onChange" size="small">
      <el-radio-button value="latest">最新发布</el-radio-button>
      <el-radio-button value="popular">最受欢迎</el-radio-button>
    </el-radio-group>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: 'latest' }
})

const emit = defineEmits(['update:modelValue', 'change'])

const model = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

function onChange(val) {
  emit('change', val)
}
</script>

<style scoped>
.sort-bar {
  display: inline-flex;
}
</style>
