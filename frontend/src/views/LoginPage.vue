<template>
  <div class="login-container">
    <div class="login-card">
      <h2>&#9670; 知享 Knowledge Hub</h2>
      <p class="subtitle">企业内部知识分享平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名 / 工号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" size="large" style="width:100%">
          登 录
        </el-button>
      </el-form>
      <el-divider>或</el-divider>
      <el-button size="large" style="width:100%;margin-bottom:8px">企业 SSO 登录 (OAuth2)</el-button>
      <el-button size="large" style="width:100%">LDAP 域账号登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    router.push('/')
  } catch {
    // 拦截器已处理 toast
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #e8f4fd 0%, #f0f2f5 100%);
}
.login-card { background: #fff; border-radius: 12px; padding: 40px; width: 400px; box-shadow: 0 4px 24px rgba(0,0,0,.06); }
h2 { font-size: 22px; text-align: center; margin: 0 0 4px; color: #303133; }
.subtitle { text-align: center; font-size: 13px; color: #909399; margin-bottom: 28px; }
</style>
