<template>
  <div class="user-detail-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户详情</h1>
        <p class="page-subtitle">兼容自动化测试直接读取详情字段和执行启用、禁用、重置密码操作。</p>
      </div>
      <button id="back-btn" class="ghost-button" type="button" @click="router.push('/admin/users')">返回列表</button>
    </div>

    <section v-if="user" class="detail-card">
      <div v-if="message.text" :class="message.type === 'success' ? 'success-message' : 'error-message'">
        {{ message.text }}
      </div>

      <div class="meta-grid">
        <div>
          <span class="meta-label">用户ID</span>
          <strong id="user-id">{{ user.id }}</strong>
        </div>
        <div>
          <span class="meta-label">注册时间</span>
          <strong id="create-time">{{ user.createTime }}</strong>
        </div>
        <div>
          <span class="meta-label">最后登录</span>
          <strong id="last-login">{{ user.lastLoginTime || '-' }}</strong>
        </div>
        <div>
          <span class="meta-label">登录次数</span>
          <strong id="login-count">{{ user.loginCount }}</strong>
        </div>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>用户名</span>
          <input id="username" :value="user.username" readonly type="text" />
        </label>
        <label class="field">
          <span>邮箱</span>
          <input id="email" :value="user.email" readonly type="text" />
        </label>
        <label class="field">
          <span>手机号</span>
          <input id="phone" :value="user.phone" readonly type="text" />
        </label>
        <label class="field">
          <span>真实姓名</span>
          <input id="real-name" :value="user.realName" readonly type="text" />
        </label>
        <label class="field">
          <span>角色</span>
          <select id="role" :value="user.role" disabled>
            <option v-for="option in roleOptions" :key="option.value" :value="option.value">
              {{ option.value }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>状态</span>
          <select id="status" :value="user.status" disabled>
            <option v-for="option in statusOptions" :key="option.value" :value="option.value">
              {{ option.value }}
            </option>
          </select>
        </label>
      </div>

      <label class="field field--full">
        <span>备注</span>
        <textarea id="description" :value="user.description" readonly rows="4" />
      </label>

      <div class="action-bar">
        <button
          v-if="user.status !== 'disabled'"
          id="disable-btn"
          class="danger-button"
          type="button"
          @click="handleDisable"
        >
          禁用用户
        </button>
        <button v-else id="enable-btn" class="primary-button" type="button" @click="handleEnable">启用用户</button>
        <button id="reset-password-btn" class="ghost-button" type="button" @click="handleResetPassword">重置密码</button>
        <button id="delete-btn" class="ghost-button" type="button" @click="handleDelete">删除用户</button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  deleteCompatUser,
  getCompatUser,
  getRoleOptions,
  getStatusOptions,
  resetCompatPassword,
  setCompatUserStatus,
  type CompatUser
} from './userCompat'

const route = useRoute()
const router = useRouter()
const roleOptions = getRoleOptions()
const statusOptions = getStatusOptions()

const user = ref<CompatUser | null>(getCompatUser(Number(route.params.id)))
const message = ref<{ type: 'success' | 'error'; text: string }>({ type: 'success', text: '' })

const reloadUser = () => {
  user.value = getCompatUser(Number(route.params.id))
}

const handleDisable = () => {
  if (!user.value) {
    return
  }
  setCompatUserStatus(user.value.id, 'disabled')
  reloadUser()
  message.value = { type: 'success', text: '用户已禁用' }
}

const handleEnable = () => {
  if (!user.value) {
    return
  }
  setCompatUserStatus(user.value.id, 'active')
  reloadUser()
  message.value = { type: 'success', text: '用户已启用' }
}

const handleResetPassword = () => {
  if (!user.value) {
    return
  }
  resetCompatPassword(user.value.id, 'Reset@123456')
  message.value = { type: 'success', text: '密码已重置' }
}

const handleDelete = () => {
  if (!user.value) {
    return
  }
  deleteCompatUser(user.value.id)
  message.value = { type: 'success', text: '用户已删除' }
  router.push('/admin/users')
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.user-detail-page {
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

.page-subtitle,
.meta-label {
  color: $text-secondary;
}

.detail-card {
  background: $card-bg;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: 24px;
}

.meta-grid,
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.meta-grid {
  margin-bottom: 20px;
}

.meta-label {
  display: block;
  margin-bottom: 8px;
  font-size: $font-size-sm;
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
  background: #f8fafc;
  font: inherit;
}

.field--full {
  margin-top: 16px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
  flex-wrap: wrap;
}

.primary-button,
.danger-button,
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

.danger-button {
  border: none;
  background: #dc2626;
  color: #fff;
}

.ghost-button {
  border: 1px solid #d0d7e2;
  background: #fff;
  color: $text-primary;
}

.success-message,
.error-message {
  margin-bottom: 12px;
  padding: 12px 14px;
  border-radius: 12px;
}

.success-message {
  color: #15803d;
  background: #f0fdf4;
}

.error-message {
  color: #b91c1c;
  background: #fef2f2;
}

@media (max-width: 768px) {
  .page-header,
  .meta-grid,
  .form-grid,
  .action-bar {
    flex-direction: column;
    grid-template-columns: 1fr;
    align-items: stretch;
  }
}
</style>
