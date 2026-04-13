<template>
  <div class="login-container">
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <div class="logo-icon">EC</div>
            <h1>电商后台管理系统</h1>
          </div>
          <p class="welcome-text">欢迎回来，请登录您的账号</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              id="username"
              v-model="loginForm.username"
              placeholder="请输入用户名"
              autocomplete="username"
              size="large"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="login-options">
            <el-checkbox id="remember-me" v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-button id="forgot-password" link type="primary" @click="forgotDialogVisible = true">
              忘记密码
            </el-button>
          </div>

          <el-form-item>
            <el-button
              id="login-btn"
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>

          <div v-if="errorMessage" id="error-msg" class="error-msg">
            <el-icon><Warning /></el-icon>
            <span>{{ errorMessage }}</span>
          </div>
        </el-form>
      </div>
    </div>

    <el-dialog v-model="forgotDialogVisible" title="忘记密码" width="420px" destroy-on-close>
      <el-form
        ref="forgotPasswordFormRef"
        :model="forgotPasswordForm"
        :rules="forgotPasswordRules"
        label-position="top"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="forgotPasswordForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="dialog-inline-field">
            <el-input v-model="forgotPasswordForm.code" placeholder="请输入验证码" />
            <el-button :loading="sendingCode" @click="handleSendResetCode">发送验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resettingPassword" @click="handleResetPasswordByCode">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Lock, User, Warning } from '@element-plus/icons-vue'
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

interface ForgotPasswordForm {
  email: string
  code: string
  newPassword: string
}

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const forgotPasswordFormRef = ref<FormInstance>()
const loading = ref(false)
const sendingCode = ref(false)
const resettingPassword = ref(false)
const forgotDialogVisible = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const forgotPasswordForm = reactive<ForgotPasswordForm>({
  email: '',
  code: '',
  newPassword: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6 到 20 个字符之间', trigger: 'blur' }
  ]
}

const forgotPasswordRules: FormRules = {
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6 到 20 个字符之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) {
    return
  }

  try {
    await loginFormRef.value.validate()
    loading.value = true
    errorMessage.value = ''

    const response = await http.post<LoginResponse>('/auth/login', {
      username: loginForm.username,
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    })

    saveAuthState({
      token: response.token,
      refreshToken: response.refreshToken,
      userInfo: response.userInfo
    })

    if (loginForm.rememberMe) {
      localStorage.setItem('rememberMe', 'true')
    } else {
      localStorage.removeItem('rememberMe')
    }

    ElMessage.success('登录成功')
    router.push(getDefaultRoute(response.userInfo?.roles))
  } catch (error: any) {
    console.error('登录失败:', error)
    errorMessage.value = error.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}

const handleSendResetCode = async () => {
  if (!forgotPasswordFormRef.value) {
    return
  }

  try {
    await forgotPasswordFormRef.value.validateField('email')
    sendingCode.value = true
    await http.post('/auth/send-reset-code', {
      email: forgotPasswordForm.email
    })
    ElMessage.success('验证码已发送，请检查邮箱')
  } catch (error) {
    console.error('发送重置密码验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

const handleResetPasswordByCode = async () => {
  if (!forgotPasswordFormRef.value) {
    return
  }

  try {
    await forgotPasswordFormRef.value.validate()
    resettingPassword.value = true
    await http.post('/auth/reset-password-by-code', {
      email: forgotPasswordForm.email,
      code: forgotPasswordForm.code,
      newPassword: forgotPasswordForm.newPassword
    })
    ElMessage.success('密码重置成功，请重新登录')
    forgotDialogVisible.value = false
    forgotPasswordForm.code = ''
    forgotPasswordForm.newPassword = ''
  } catch (error) {
    console.error('重置密码失败:', error)
  } finally {
    resettingPassword.value = false
  }
}

const checkRememberMe = () => {
  loginForm.rememberMe = localStorage.getItem('rememberMe') === 'true'
}

checkRememberMe()
</script>

<style scoped lang="scss">
@use 'sass:color';
@use '@/styles/variables.scss' as *;

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4ff 0%, #e6f7ff 100%);
  padding: $spacing-xl;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 8px;
    background: linear-gradient(90deg, $primary-color, color.adjust($primary-color, $lightness: 20%));
  }

  .login-wrapper {
    width: 100%;
    max-width: 480px;
    position: relative;
    z-index: 1;
  }

  .login-card {
    width: 100%;
    margin: 0 auto;
    padding: $spacing-xxl;
    background: $card-bg;
    border: 1px solid $border-light;
    border-radius: $border-radius;
    box-shadow: $box-shadow-card;
  }

  .login-header {
    margin-bottom: $spacing-xxl;
    text-align: center;

    .logo {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: $spacing-lg;

      .logo-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 80px;
        height: 80px;
        color: $text-white;
        font-size: $font-size-xxl;
        font-weight: bold;
        background: $primary-color;
        border-radius: 20px;
        box-shadow: 0 8px 24px rgba($primary-color, 0.3);
      }

      h1 {
        margin: 0;
        color: $text-primary;
        font-size: $font-size-xxl;
        font-weight: 700;
        line-height: 1.3;
      }
    }

    .welcome-text {
      margin-top: $spacing-lg;
      color: $text-secondary;
      font-size: $font-size-lg;
      font-weight: 500;
    }
  }

  .login-form {
    .el-form-item {
      margin-bottom: $spacing-xl;
    }

    .el-input {
      :deep(.el-input__wrapper) {
        min-height: 56px;
        padding: 0 $spacing-lg;
        font-size: $font-size-lg;
        border-radius: $border-radius;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

        &:hover,
        &:focus {
          box-shadow: 0 4px 16px rgba($primary-color, 0.15);
        }
      }

      :deep(.el-input__prefix .el-icon) {
        color: $text-secondary;
        font-size: $font-size-xl;
      }
    }
  }

  .login-options {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xxl;
    padding: 0 $spacing-xs;

    :deep(.el-checkbox__label) {
      color: $text-secondary;
      font-size: $font-size-md;
      font-weight: 500;
    }

    :deep(.el-checkbox__inner) {
      width: 20px;
      height: 20px;
    }
  }

  .login-button {
    width: 100%;
    min-height: 56px;
    margin-top: $spacing-lg;
    font-size: $font-size-lg;
    font-weight: 600;
    border-radius: $border-radius;
    box-shadow: 0 4px 16px rgba($primary-color, 0.3);
    transition: all 0.3s;

    &:hover,
    &:focus {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba($primary-color, 0.4);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 8px rgba($primary-color, 0.3);
    }
  }

  .error-msg {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-md;
    margin-top: $spacing-xl;
    padding: $spacing-lg;
    color: $danger-color;
    font-size: $font-size-md;
    font-weight: 500;
    background-color: rgba($danger-color, 0.08);
    border: 1px solid rgba($danger-color, 0.2);
    border-radius: $border-radius;

    .el-icon {
      font-size: $font-size-lg;
    }
  }
}

.dialog-inline-field {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: $spacing-md;
}
</style>
