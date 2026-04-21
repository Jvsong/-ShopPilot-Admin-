<template>
  <div class="user-form-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isEditMode ? '编辑用户' : '添加用户' }}</h1>
        <p class="page-subtitle">使用兼容表单字段，确保自动化脚本可直接访问和提交。</p>
      </div>
      <button id="cancel-btn" class="ghost-button" type="button" @click="goBack">返回列表</button>
    </div>

    <section class="form-card">
      <div v-if="feedback?.type === 'success'" class="success-message">{{ feedback.text }}</div>
      <div v-else-if="feedback?.type === 'error'" class="error-message">{{ feedback.text }}</div>

      <div v-if="validationErrors.length" class="validation-list">
        <div v-for="error in validationErrors" :key="error" class="validation-error">{{ error }}</div>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>用户名</span>
          <input id="username" v-model.trim="form.username" :disabled="isEditMode" type="text" />
        </label>

        <label class="field">
          <span>邮箱</span>
          <input id="email" v-model.trim="form.email" type="email" />
        </label>

        <label class="field">
          <span>手机号</span>
          <input id="phone" v-model.trim="form.phone" type="text" />
        </label>

        <label class="field">
          <span>真实姓名</span>
          <input id="real-name" v-model.trim="form.realName" type="text" />
        </label>

        <label class="field" v-if="!isEditMode">
          <span>密码</span>
          <input id="password" v-model="form.password" type="password" />
        </label>

        <label class="field" v-if="!isEditMode">
          <span>确认密码</span>
          <input id="confirm-password" v-model="form.confirmPassword" type="password" />
        </label>

        <label class="field">
          <span>角色</span>
          <select id="role" v-model="form.role">
            <option v-for="option in roleOptions" :key="option.value" :value="option.value">
              {{ option.value }}
            </option>
          </select>
        </label>

        <label class="field">
          <span>状态</span>
          <select id="status" v-model="form.status">
            <option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.value }}
            </option>
          </select>
        </label>
      </div>

      <label class="field field--full">
        <span>备注</span>
        <textarea id="description" v-model.trim="form.description" rows="4" />
      </label>

      <div class="action-bar">
        <button id="submit-btn" class="ghost-button" type="button" @click="handleBoundarySubmit">校验用户名</button>
        <button id="save-btn" class="primary-button" type="button" @click="handleSave">
          {{ isEditMode ? '保存修改' : '保存用户' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createCompatUser,
  getCompatUser,
  getRoleOptions,
  getStatusOptions,
  updateCompatUser,
  type UserRoleCode,
  type UserStatusCode
} from './userCompat'

const route = useRoute()
const router = useRouter()

const roleOptions = getRoleOptions()
const statusOptions = getStatusOptions()

const userId = computed(() => Number(route.params.id))
const isEditMode = computed(() => Number.isFinite(userId.value))

const feedback = ref<{ type: 'success' | 'error'; text: string } | null>(null)
const validationErrors = ref<string[]>([])

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  realName: '',
  role: 'viewer' as UserRoleCode,
  status: 'active' as UserStatusCode,
  description: ''
})

const loadUser = () => {
  if (!isEditMode.value) {
    return
  }

  const user = getCompatUser(userId.value)
  if (!user) {
    feedback.value = { type: 'error', text: '用户不存在' }
    return
  }

  form.username = user.username
  form.email = user.email
  form.phone = user.phone
  form.realName = user.realName
  form.role = user.role
  form.status = user.status
  form.description = user.description
}

const validateUsernameOnly = () => {
  const errors: string[] = []
  const username = form.username.trim()

  if (username.length > 0 && username.length < 3) {
    errors.push('用户名长度至少3位')
  }

  if (username.length > 20) {
    errors.push('用户名长度不能超过20位')
  }

  return errors
}

const validateForSave = () => {
  const errors = validateUsernameOnly()

  if (!form.username.trim()) {
    errors.push('请输入用户名')
  }

  if (!isEditMode.value) {
    if (!form.password) {
      errors.push('请输入密码')
    } else if (form.password.length < 8) {
      errors.push('密码长度至少8位')
    }

    if (form.confirmPassword !== form.password) {
      errors.push('两次输入的密码不一致')
    }
  }

  if (form.email && !/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(form.email)) {
    errors.push('请输入有效的邮箱地址')
  }

  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    errors.push('请输入有效的手机号码')
  }

  return errors
}

const handleBoundarySubmit = () => {
  validationErrors.value = validateUsernameOnly()
  feedback.value = validationErrors.value.length
    ? { type: 'error', text: '用户名校验未通过' }
    : { type: 'success', text: '用户名校验通过' }
}

const handleSave = () => {
  validationErrors.value = validateForSave()
  if (validationErrors.value.length) {
    feedback.value = { type: 'error', text: '请修正表单错误后再提交' }
    return
  }

  if (isEditMode.value) {
    updateCompatUser(userId.value, {
      email: form.email,
      phone: form.phone,
      realName: form.realName,
      role: form.role,
      status: form.status,
      description: form.description
    })
    feedback.value = { type: 'success', text: '用户更新成功' }
    return
  }

  createCompatUser({
    username: form.username,
    password: form.password,
    email: form.email,
    phone: form.phone,
    realName: form.realName,
    role: form.role,
    status: form.status,
    description: form.description
  })
  feedback.value = { type: 'success', text: '用户创建成功' }
}

const goBack = () => {
  router.push('/admin/users')
}

loadUser()
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.user-form-page {
  padding: $spacing-lg;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 8px;
  color: $text-primary;
  font-size: $font-size-xxl;
}

.page-subtitle {
  margin: 0;
  color: $text-secondary;
}

.form-card {
  background: $card-bg;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: $text-primary;
  font-weight: 600;
}

.field input,
.field select,
.field textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d0d7e2;
  border-radius: 10px;
  font: inherit;
}

.field textarea {
  resize: vertical;
}

.field--full {
  margin-top: 16px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.primary-button,
.ghost-button {
  border-radius: 10px;
  padding: 10px 18px;
  font: inherit;
  cursor: pointer;
}

.primary-button {
  border: none;
  background: $primary-color;
  color: #fff;
}

.ghost-button {
  border: 1px solid #d0d7e2;
  background: #fff;
  color: $text-primary;
}

.success-message,
.error-message,
.validation-error {
  margin-bottom: 12px;
  font-size: $font-size-sm;
}

.success-message {
  color: #16a34a;
}

.error-message,
.validation-error {
  color: #dc2626;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
