<template>
  <PageCard title="系统设置">

    <el-form label-position="top" style="max-width:500px;">
      <el-form-item label="平台名称">
        <el-input v-model="settings.platformName" placeholder="知识分享平台" />
      </el-form-item>
      <el-form-item label="内容审核">
        <el-switch v-model="settings.contentAudit" active-text="开启" inactive-text="关闭" />
      </el-form-item>
      <el-form-item label="用户注册">
        <el-switch v-model="settings.openRegistration" active-text="开放" inactive-text="仅管理员创建" />
      </el-form-item>
      <el-form-item label="单文件上传限制 (MB)">
        <el-input-number v-model="settings.maxFileSize" :min="1" :max="500" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存设置</el-button>
      </el-form-item>
    </el-form>
  </PageCard>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import PageCard from '@/components/common/PageCard.vue'

const settings = reactive({
  platformName: '知识分享平台',
  contentAudit: true,
  openRegistration: false,
  maxFileSize: 50
})

function handleSave() {
  ElMessage.success('设置已保存（功能开发中）')
}
</script>

<style scoped>
</style>
