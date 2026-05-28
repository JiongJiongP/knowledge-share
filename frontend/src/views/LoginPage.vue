<template>
  <div class="login-container">
    <div class="bg-orb bg-orb-1"></div>
    <div class="bg-orb bg-orb-2"></div>
    <div class="bg-orb bg-orb-3"></div>

    <div class="login-card">
      <div class="login-header">
        <div class="logo-circle">
          <i class="ri-book-open-fill"></i>
        </div>
        <h2>知享 Knowledge Hub</h2>
        <p class="subtitle">企业内部知识分享平台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名 / 工号" size="large">
            <template #prefix><i class="ri-user-line"></i></template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password>
            <template #prefix><i class="ri-lock-line"></i></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" size="large" class="login-btn">
          登 录
        </el-button>
      </el-form>

      <div class="divider">
        <span>或</span>
      </div>

      <div class="alt-login">
        <button class="alt-btn" type="button">
          <i class="ri-shield-keyhole-line"></i> 企业 SSO 登录
        </button>
        <button class="alt-btn" type="button">
          <i class="ri-server-line"></i> LDAP 域账号登录
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
    ElMessage.error('登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  position: relative;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}
.bg-orb-1 {
  width: 400px;
  height: 400px;
  background: rgba(102, 126, 234, 0.6);
  top: -10%;
  left: -5%;
  animation-delay: 0s;
}
.bg-orb-2 {
  width: 350px;
  height: 350px;
  background: rgba(240, 147, 251, 0.5);
  bottom: -8%;
  right: -3%;
  animation-delay: -3s;
}
.bg-orb-3 {
  width: 250px;
  height: 250px;
  background: rgba(118, 75, 162, 0.4);
  top: 50%;
  left: 60%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 48px 40px 36px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-circle {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.logo-circle i {
  font-size: 28px;
  color: #fff;
}

h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px;
  color: #fff;
  letter-spacing: 0.5px;
}

.subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.login-card :deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.login-card :deep(.el-input__wrapper) {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 10px;
  box-shadow: none;
  transition: all 0.3s;
}

.login-card :deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.45);
}

.login-card :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.1);
}

.login-card :deep(.el-input__inner) {
  color: #fff;
  -webkit-box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0.01) inset;
  box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0.01) inset;
  -webkit-transition: background-color 9999999s ease-in-out 0s;
  transition: background-color 9999999s ease-in-out 0s;
}

.login-card :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.45);
}

.login-card :deep(.el-input__prefix .ri-user-line),
.login-card :deep(.el-input__prefix .ri-lock-line) {
  color: rgba(255, 255, 255, 0.6);
  font-size: 18px;
  line-height: 1;
}

.login-card :deep(.el-input__suffix .el-input__icon) {
  color: rgba(255, 255, 255, 0.5);
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

.divider {
  display: flex;
  align-items: center;
  margin: 24px 0 20px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.2);
}

.divider span {
  padding: 0 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.alt-login {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alt-btn {
  width: 100%;
  height: 42px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
  font-family: inherit;
  backdrop-filter: blur(4px);
}

.alt-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

.alt-btn i {
  font-size: 18px;
}
</style>

<style>
.login-card input:-webkit-autofill,
.login-card input:-webkit-autofill:hover,
.login-card input:-webkit-autofill:focus {
  -webkit-text-fill-color: #fff !important;
  caret-color: #fff !important;
  -webkit-box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0.01) inset !important;
}

.login-card input:-internal-autofill-previewed,
.login-card input:-internal-autofill-selected {
  -webkit-text-fill-color: #fff !important;
  caret-color: #fff !important;
  -webkit-box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0.01) inset !important;
}
</style>
