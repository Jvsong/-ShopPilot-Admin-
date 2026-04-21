<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>电商后台管理系统</h1>
        <p>请输入账号密码登录后台。</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <label class="field">
          <span>用户名</span>
          <input id="username" v-model.trim="loginForm.username" autocomplete="username" type="text" />
        </label>

        <label class="field">
          <span>密码</span>
          <input
            id="password"
            v-model="loginForm.password"
            autocomplete="current-password"
            type="password"
            @keyup.enter="handleLogin"
          />
        </label>

        <div class="login-options">
          <label class="remember-me-label" for="remember-me">
            <input id="remember-me" v-model="loginForm.rememberMe" type="checkbox" @change="syncRememberMe" />
            <span>记住我</span>
          </label>
          <button id="forgot-password" class="link-button" type="button" @click="openForgotPasswordDialog">
            <span>忘记密码</span>
          </button>
        </div>

        <button id="login-btn" class="login-button" :disabled="loading" type="submit">
          <span>{{ loading ? '登录中...' : '登录' }}</span>
        </button>

        <div v-if="errorMessage" id="error-msg" class="error-msg">
          {{ errorMessage }}
        </div>
      </form>
    </div>

    <div v-if="forgotDialogVisible" class="dialog-mask" @click.self="closeForgotPasswordDialog">
      <div class="el-dialog forgot-dialog" role="dialog" aria-modal="true">
        <div class="dialog-header">
          <h2>忘记密码</h2>
          <button class="dialog-close" type="button" @click="closeForgotPasswordDialog">×</button>
        </div>

        <div class="dialog-body">
          <label class="field">
            <span>邮箱</span>
            <input v-model.trim="forgotPasswordForm.email" type="email" />
          </label>
          <label class="field">
            <span>验证码</span>
            <input v-model.trim="forgotPasswordForm.code" type="text" />
          </label>
          <label class="field">
            <span>新密码</span>
            <input v-model="forgotPasswordForm.newPassword" type="password" />
          </label>
        </div>

        <div class="dialog-footer">
          <button class="ghost-button" type="button" @click="handleSendResetCode">发送验证码</button>
          <button class="primary-button" type="button" @click="handleResetPasswordByCode">重置密码</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '@/utils/request'
import { getDefaultRoute, saveAuthState } from '@/utils/auth'

interface LoginUserInfo {
  id: number
  username: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  userType?: number
  lastLoginTime?: string
  createTime?: string
  roles?: string[]
}

interface LoginResponse {
  token: string
  refreshToken?: string
  userInfo?: LoginUserInfo
}

const router = useRouter()
const loading = ref(false)
const forgotDialogVisible = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const forgotPasswordForm = reactive({
  email: '',
  code: '',
  newPassword: ''
})

const setForgotPasswordUrl = (opened: boolean) => {
  const nextPath = opened ? '/admin/login/forgot-password' : '/admin/login'
  window.history.replaceState({}, '', nextPath)
}

const openForgotPasswordDialog = () => {
  forgotDialogVisible.value = true
  setForgotPasswordUrl(true)
}

const closeForgotPasswordDialog = () => {
  forgotDialogVisible.value = false
  setForgotPasswordUrl(false)
}

const validateLoginForm = () => {
  const username = loginForm.username.trim()
  const password = loginForm.password

  if (!username) {
    return '请输入用户名'
  }

  if (!password) {
    return '请输入密码'
  }

  if (username.length < 3 || username.length > 20) {
    return '用户名长度在 3 到 20 个字符之间'
  }

  if (password.length > 20) {
    return '密码长度在 6 到 20 个字符之间'
  }

  return ''
}

const resolveLoginError = (error: any) => {
  const status = error?.response?.status
  const serverMessage = error?.response?.data?.message || error?.message || ''

  if (status === 401) {
    return '401 用户名或密码错误'
  }

  if (typeof serverMessage === 'string' && serverMessage.trim()) {
    return serverMessage.trim()
  }

  return '登录失败，请检查用户名和密码'
}

const syncRememberMe = () => {
  if (loginForm.rememberMe) {
    localStorage.setItem('rememberMe', 'true')
    sessionStorage.setItem('rememberMe', 'true')
    return
  }

  localStorage.removeItem('rememberMe')
  sessionStorage.removeItem('rememberMe')
}

const handleLogin = async () => {
  const validationMessage = validateLoginForm()
  if (validationMessage) {
    errorMessage.value = validationMessage
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await http.post<LoginResponse>('/auth/login', {
      username: loginForm.username.trim(),
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    })

    saveAuthState({
      token: response.token,
      refreshToken: response.refreshToken,
      userInfo: response.userInfo
    })

    syncRememberMe()
    ElMessage.success('登录成功')
    router.push(getDefaultRoute(response.userInfo?.roles))
  } catch (error: any) {
    errorMessage.value = resolveLoginError(error)
  } finally {
    loading.value = false
  }
}

const handleSendResetCode = async () => {
  if (!forgotPasswordForm.email.trim()) {
    ElMessage.error('请输入注册邮箱')
    return
  }

  try {
    await http.post('/auth/send-reset-code', {
      email: forgotPasswordForm.email.trim()
    })
    ElMessage.success('验证码已发送，请检查邮箱')
  } catch (error) {
    console.error('发送重置验证码失败:', error)
  }
}

const handleResetPasswordByCode = async () => {
  if (!forgotPasswordForm.email.trim()) {
    ElMessage.error('请输入注册邮箱')
    return
  }

  if (!forgotPasswordForm.code.trim()) {
    ElMessage.error('请输入验证码')
    return
  }

  if (forgotPasswordForm.newPassword.length < 6 || forgotPasswordForm.newPassword.length > 20) {
    ElMessage.error('密码长度在 6 到 20 个字符之间')
    return
  }

  try {
    await http.post('/auth/reset-password-by-code', {
      email: forgotPasswordForm.email.trim(),
      code: forgotPasswordForm.code.trim(),
      newPassword: forgotPasswordForm.newPassword
    })
    ElMessage.success('密码重置成功，请重新登录')
    forgotPasswordForm.code = ''
    forgotPasswordForm.newPassword = ''
    closeForgotPasswordDialog()
  } catch (error) {
    console.error('重置密码失败:', error)
  }
}

loginForm.rememberMe = localStorage.getItem('rememberMe') === 'true' || sessionStorage.getItem('rememberMe') === 'true'
if (window.location.pathname.includes('forgot-password')) {
  forgotDialogVisible.value = true
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(22, 93, 255, 0.12), transparent 28%),
    radial-gradient(circle at bottom right, rgba(0, 180, 42, 0.12), transparent 24%),
    linear-gradient(135deg, #f8fbff, #eef5ff);
}

.login-card {
  width: min(100%, 420px);
  padding: 32px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 60px rgba(15, 35, 95, 0.12);
}

.login-header h1 {
  margin: 0;
  font-size: 28px;
  color: $text-primary;
}

.login-header p {
  margin: 8px 0 0;
  color: $text-secondary;
}

.login-form {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: $text-primary;
  font-weight: 600;
}

.field input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #d7e0ef;
  border-radius: 12px;
  font: inherit;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.remember-me-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: $text-secondary;
  cursor: pointer;
}

.link-button,
.dialog-close {
  border: none;
  background: transparent;
  color: $primary-color;
  cursor: pointer;
}

.login-button,
.primary-button,
.ghost-button {
  border-radius: 12px;
  padding: 12px 16px;
  font: inherit;
  cursor: pointer;
}

.login-button,
.primary-button {
  border: none;
  color: #fff;
  background: linear-gradient(135deg, #165dff, #0f7bff);
}

.ghost-button {
  border: 1px solid #d7e0ef;
  background: #fff;
  color: $text-primary;
}

.error-msg {
  border: 1px solid #fecaca;
  border-radius: 12px;
  padding: 12px 14px;
  color: #dc2626;
  background: #fff5f5;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background: rgba(15, 23, 42, 0.4);
}

.el-dialog {
  width: min(100%, 420px);
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.2);
}

.dialog-header,
.dialog-body,
.dialog-footer {
  padding: 20px 24px;
}

.dialog-header,
.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
