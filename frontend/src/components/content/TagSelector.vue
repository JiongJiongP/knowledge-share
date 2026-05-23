<template>
  <div class="tag-selector">
    <el-select
      v-model="selectedIds"
      multiple
      filterable
      placeholder="选择标签"
      size="small"
      @change="onChange"
    >
      <el-option
        v-for="tag in tags"
        :key="tag.id"
        :label="tag.name"
        :value="tag.id"
      >
        <span class="tag-option">
          <span class="tag-dot" :style="{ background: tag.color }" />
          {{ tag.name }}
        </span>
      </el-option>
    </el-select>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTags } from '@/api/tag'

const props = defineProps({
  modelValue: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue'])

const tags = ref([])
const selectedIds = ref([...props.modelValue])

onMounted(async () => {
  try {
    const res = await getTags()
    tags.value = res.data
  } catch { /* silent */ }
})

function onChange(val) {
  emit('update:modelValue', val)
}
</script>

<style scoped>
.tag-option {
  display: flex;
  align-items: center;
  gap: 6px;
}
.tag-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}
</style>
