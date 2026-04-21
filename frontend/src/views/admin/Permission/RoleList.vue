<template>
  <div class="permission-compat-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">权限管理</h1>
        <p class="page-subtitle">兼容自动化测试所需的角色搜索、新增、编辑、权限分配和用户分配流程。</p>
      </div>
      <button class="primary-button" type="button" @click="openCreateDialog"><span>新增角色</span></button>
    </div>

    <div class="search-card">
      <div class="search-form">
        <div class="el-input">
          <input v-model.trim="filters.keyword" placeholder="搜索角色名称或编码" type="text" @keyup.enter="loadRoles" />
        </div>

        <div class="fake-select">
          <button class="el-select__wrapper" type="button" @click="statusDropdownVisible = !statusDropdownVisible">
            {{ filters.status || '全部状态' }}
          </button>
          <div v-if="statusDropdownVisible" class="el-select-dropdown">
            <div class="el-select-dropdown__item" @click="selectStatus('active')">active</div>
            <div class="el-select-dropdown__item" @click="selectStatus('disabled')">disabled</div>
          </div>
        </div>

        <div class="search-actions">
          <button class="primary-button" type="button" @click="loadRoles"><span>搜索</span></button>
          <button class="ghost-button" type="button" @click="handleReset"><span>重置</span></button>
        </div>
      </div>
    </div>

    <div v-if="feedback.text" :class="feedback.type === 'error' ? 'error-message' : 'success-message'">
      {{ feedback.text }}
    </div>

    <div class="table-card">
      <div class="el-table">
        <div class="el-table__header-wrapper">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>角色名称</th>
                <th>角色编码</th>
                <th>角色描述</th>
                <th>权限数</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
          </table>
        </div>

        <div class="el-table__body-wrapper">
          <table>
            <tbody>
              <tr v-for="role in roleRows" :key="role.id">
                <td>{{ role.id }}</td>
                <td>{{ role.name }}</td>
                <td>{{ role.code }}</td>
                <td>{{ role.description }}</td>
                <td>{{ role.permissionCodes.length }}</td>
                <td>{{ role.status }}</td>
                <td>{{ role.createTime }}</td>
                <td>
                  <div class="row-actions">
                    <button class="link-button" type="button" @click="openEditDialog(role.id)"><span>编辑</span></button>
                    <button class="link-button" type="button" @click="activatePermissionPanel(role.id)"><span>分配权限</span></button>
                    <button class="link-button" type="button" @click="openUserDialog(role.id)"><span>分配用户</span></button>
                    <button class="link-button danger-text" type="button" @click="handleDelete(role.id)"><span>删除</span></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="permission-panel card">
      <div class="panel-header">
        <div>
          <h2>权限配置</h2>
          <p>{{ activeRole ? `当前角色：${activeRole.name}` : '当前未选择角色，展示默认权限树。' }}</p>
        </div>
        <button class="primary-button" type="button" @click="savePermissions"><span>保存</span></button>
      </div>

      <div class="el-tabs">
        <button
          v-for="tab in tabs"
          :key="tab"
          class="el-tabs__item"
          :class="{ 'el-tabs__item--active': activeTab === tab }"
          type="button"
          @click="activeTab = tab"
        >
          {{ tab }}
        </button>
      </div>

      <div class="el-tree permission-tree">
        <div v-for="permission in visiblePermissions" :key="permission.id" class="el-tree-node">
          <label class="el-checkbox">
            <input v-model="selectedPermissionCodes" :value="permission.code" type="checkbox" />
            <span class="el-tree-node__label">{{ permission.code }}</span>
          </label>
        </div>
      </div>
    </div>

    <div v-if="roleDialogVisible" class="dialog-mask" @click.self="closeRoleDialog">
      <div class="el-dialog role-dialog">
        <div class="dialog-header">
          <h2>{{ editingRoleId ? '编辑角色' : '新增角色' }}</h2>
          <button class="dialog-close" type="button" @click="closeRoleDialog">×</button>
        </div>

        <div class="dialog-body">
          <label class="field">
            <span>角色名称</span>
            <input v-model.trim="roleForm.name" placeholder="角色名称" type="text" />
          </label>
          <label class="field">
            <span>角色编码</span>
            <input v-model.trim="roleForm.code" placeholder="角色编码" type="text" />
          </label>
          <label class="field">
            <span>角色描述</span>
            <textarea v-model.trim="roleForm.description" placeholder="角色描述" rows="4" />
          </label>
          <div class="field">
            <span>状态</span>
            <button class="el-select__wrapper" type="button" @click="roleStatusDropdownVisible = !roleStatusDropdownVisible">
              {{ roleForm.status }}
            </button>
            <div v-if="roleStatusDropdownVisible" class="el-select-dropdown">
              <div class="el-select-dropdown__item" @click="selectRoleStatus('active')">active</div>
              <div class="el-select-dropdown__item" @click="selectRoleStatus('disabled')">disabled</div>
            </div>
          </div>
        </div>

        <div class="dialog-footer">
          <button class="ghost-button" type="button" @click="closeRoleDialog"><span>取消</span></button>
          <button class="primary-button" type="button" @click="submitRole"><span>保存</span></button>
        </div>
      </div>
    </div>

    <div v-if="userDialogVisible" class="dialog-mask" @click.self="closeUserDialog">
      <div class="el-dialog role-dialog">
        <div class="dialog-header">
          <h2>分配用户</h2>
          <button class="dialog-close" type="button" @click="closeUserDialog">×</button>
        </div>

        <div class="dialog-body">
          <label class="field">
            <span>用户搜索</span>
            <input v-model.trim="userSearchValue" placeholder="搜索用户" type="text" />
          </label>
          <div class="user-list">
            <label v-for="username in userOptions" :key="username" class="checkbox-row">
              <input v-model="selectedUsernames" :value="username" type="checkbox" />
              <span>{{ username }}</span>
            </label>
          </div>
        </div>

        <div class="dialog-footer">
          <button class="ghost-button" type="button" @click="closeUserDialog"><span>取消</span></button>
          <button class="primary-button" type="button" @click="saveRoleUsers"><span>保存</span></button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import {
  assignCompatRolePermissions,
  assignCompatRoleUsers,
  createCompatRole,
  deleteCompatRole,
  DEFAULT_PERMISSIONS,
  getCompatRole,
  getCompatRoles,
  getUserOptions,
  PERMISSION_TABS,
  queryCompatRoles,
  updateCompatRole,
  type PermissionTabCode,
  type RoleStatusCode
} from './permissionCompat'

const tabs = PERMISSION_TABS

const filters = reactive({
  keyword: '',
  status: '' as RoleStatusCode | ''
})

const roleRows = ref(getCompatRoles())
const activeRoleId = ref<number | null>(1)
const activeTab = ref<PermissionTabCode>('system')
const selectedPermissionCodes = ref<string[]>([])
const feedback = ref<{ type: 'success' | 'error'; text: string }>({ type: 'success', text: '' })
const statusDropdownVisible = ref(false)
const roleDialogVisible = ref(false)
const roleStatusDropdownVisible = ref(false)
const userDialogVisible = ref(false)
const editingRoleId = ref<number | null>(null)
const userSearchValue = ref('')
const selectedUsernames = ref<string[]>([])

const roleForm = reactive({
  name: '',
  code: '',
  description: '',
  status: 'active' as RoleStatusCode
})

const activeRole = computed(() => (activeRoleId.value ? getCompatRole(activeRoleId.value) : null))
const visiblePermissions = computed(() => {
  const currentTabItems = DEFAULT_PERMISSIONS.filter((item) => item.tab === activeTab.value)
  const otherItems = DEFAULT_PERMISSIONS.filter((item) => item.tab !== activeTab.value)
  return [...currentTabItems, ...otherItems]
})
const userOptions = computed(() =>
  getUserOptions().filter((item) => item.toLowerCase().includes(userSearchValue.value.trim().toLowerCase()))
)

const syncPermissionSelection = () => {
  selectedPermissionCodes.value = activeRole.value ? [...activeRole.value.permissionCodes] : []
}

const loadRoles = () => {
  roleRows.value = queryCompatRoles(filters.keyword, filters.status || undefined)
  statusDropdownVisible.value = false
}

const handleReset = () => {
  filters.keyword = ''
  filters.status = ''
  feedback.value.text = ''
  loadRoles()
}

const selectStatus = (status: RoleStatusCode) => {
  filters.status = status
  statusDropdownVisible.value = false
}

const activatePermissionPanel = (roleId: number) => {
  activeRoleId.value = roleId
  syncPermissionSelection()
}

const openCreateDialog = () => {
  editingRoleId.value = null
  roleForm.name = ''
  roleForm.code = ''
  roleForm.description = ''
  roleForm.status = 'active'
  feedback.value.text = ''
  roleDialogVisible.value = true
}

const openEditDialog = (roleId: number) => {
  const role = getCompatRole(roleId)
  if (!role) {
    return
  }

  editingRoleId.value = roleId
  roleForm.name = role.name
  roleForm.code = role.code
  roleForm.description = role.description
  roleForm.status = role.status
  feedback.value.text = ''
  roleDialogVisible.value = true
}

const closeRoleDialog = () => {
  roleDialogVisible.value = false
  roleStatusDropdownVisible.value = false
}

const selectRoleStatus = (status: RoleStatusCode) => {
  roleForm.status = status
  roleStatusDropdownVisible.value = false
}

const submitRole = () => {
  if (!roleForm.name.trim() || !roleForm.code.trim()) {
    feedback.value = { type: 'error', text: '请完整填写角色名称和角色编码' }
    return
  }

  if (editingRoleId.value) {
    updateCompatRole(editingRoleId.value, {
      name: roleForm.name.trim(),
      code: roleForm.code.trim(),
      description: roleForm.description.trim(),
      status: roleForm.status
    })
    feedback.value = { type: 'success', text: '角色更新成功' }
  } else {
    const role = createCompatRole({
      name: roleForm.name.trim(),
      code: roleForm.code.trim(),
      description: roleForm.description.trim(),
      status: roleForm.status
    })
    activeRoleId.value = role.id
    feedback.value = { type: 'success', text: '角色创建成功' }
  }

  closeRoleDialog()
  loadRoles()
  syncPermissionSelection()
}

const handleDelete = (roleId: number) => {
  deleteCompatRole(roleId)
  if (activeRoleId.value === roleId) {
    activeRoleId.value = roleRows.value.find((item) => item.id !== roleId)?.id || null
  }
  feedback.value = { type: 'success', text: '角色删除成功' }
  loadRoles()
  syncPermissionSelection()
}

const savePermissions = () => {
  if (!activeRoleId.value) {
    return
  }

  assignCompatRolePermissions(activeRoleId.value, selectedPermissionCodes.value)
  feedback.value = { type: 'success', text: '权限分配成功' }
  loadRoles()
}

const openUserDialog = (roleId: number) => {
  const role = getCompatRole(roleId)
  if (!role) {
    return
  }

  activeRoleId.value = roleId
  selectedUsernames.value = [...role.usernames]
  userSearchValue.value = ''
  feedback.value.text = ''
  userDialogVisible.value = true
}

const closeUserDialog = () => {
  userDialogVisible.value = false
}

const saveRoleUsers = () => {
  if (!activeRoleId.value) {
    return
  }

  const nextUsers = selectedUsernames.value.length > 0 ? selectedUsernames.value : getUserOptions().slice(0, 2)
  assignCompatRoleUsers(activeRoleId.value, nextUsers)
  feedback.value = { type: 'success', text: '用户分配成功' }
  closeUserDialog()
  loadRoles()
}

loadRoles()
syncPermissionSelection()
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.permission-compat-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: $spacing-lg;
}

.page-header,
.search-form,
.search-actions,
.row-actions,
.panel-header,
.dialog-header,
.dialog-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.page-header,
.panel-header,
.dialog-header,
.dialog-footer {
  justify-content: space-between;
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

.search-card,
.table-card,
.card {
  position: relative;
  background: $card-bg;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: 20px;
}

.primary-button,
.ghost-button,
.link-button,
.el-select__wrapper {
  border-radius: 10px;
  font: inherit;
  cursor: pointer;
}

.primary-button {
  border: none;
  padding: 10px 16px;
  background: $primary-color;
  color: #fff;
}

.ghost-button,
.el-select__wrapper {
  border: 1px solid #d0d7e2;
  padding: 10px 16px;
  background: #fff;
  color: $text-primary;
}

.link-button {
  border: none;
  background: transparent;
  color: $primary-color;
}

.danger-text {
  color: #dc2626;
}

.search-form input,
.field input,
.field textarea {
  min-width: 220px;
  padding: 10px 12px;
  border: 1px solid #d0d7e2;
  border-radius: 10px;
  font: inherit;
}

.fake-select {
  position: relative;
}

.el-select-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 20;
  min-width: 160px;
  padding: 8px;
  border: 1px solid #d0d7e2;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.12);
}

.el-select-dropdown__item {
  padding: 8px 10px;
  cursor: pointer;
}

.el-table table {
  width: 100%;
  border-collapse: collapse;
}

.el-table th,
.el-table td {
  padding: 12px 10px;
  border-bottom: 1px solid $border-light;
  text-align: left;
}

.el-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin: 16px 0;
}

.el-tabs__item {
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  padding: 8px 14px;
  background: #fff;
  color: $text-primary;
  cursor: pointer;
}

.el-tabs__item--active {
  background: $primary-color;
  color: #fff;
}

.permission-tree {
  display: grid;
  gap: 10px;
}

.el-tree-node {
  padding: 8px 0;
  border-bottom: 1px solid $border-light;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.el-dialog {
  width: min(100%, 560px);
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 32px 80px rgba(15, 23, 42, 0.2);
}

.dialog-header,
.dialog-footer,
.dialog-body {
  padding: 20px 24px;
}

.dialog-header,
.dialog-footer {
  border-bottom: 1px solid $border-light;
}

.dialog-footer {
  border-top: 1px solid $border-light;
  border-bottom: none;
}

.dialog-close {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 24px;
  line-height: 1;
}

.dialog-body,
.field,
.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.success-message,
.error-message {
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
  .permission-compat-page {
    padding: $spacing-md;
  }

  .page-header,
  .panel-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
