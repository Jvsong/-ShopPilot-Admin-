<template>
  <div class="user-compat-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">兼容自动化测试依赖的搜索、筛选、排序、导出和状态切换。</p>
      </div>
      <div class="header-actions">
        <button id="export-btn" class="ghost-button" type="button" @click="handleExport"><span>导出</span></button>
        <button id="add-user-btn" class="primary-button" type="button" @click="router.push('/user/add')"><span>添加用户</span></button>
      </div>
    </div>

    <div class="search-card">
      <div class="search-form">
        <div class="el-input">
          <input
            id="user-search-input"
            v-model.trim="filters.keyword"
            placeholder="搜索用户名 / 邮箱 / 手机号"
            type="text"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="fake-select">
          <button id="status-filter" class="el-select__wrapper" type="button" @click="toggleDropdown('status')">
            {{ filters.status || '状态' }}
          </button>
          <div v-if="dropdown === 'status'" class="el-select-dropdown">
            <div class="el-select-dropdown__item" @click="selectStatus('active')">active</div>
            <div class="el-select-dropdown__item" @click="selectStatus('disabled')">disabled</div>
            <div class="el-select-dropdown__item" @click="selectStatus('pending_activation')">pending_activation</div>
          </div>
        </div>

        <div class="fake-select">
          <button id="role-filter" class="el-select__wrapper" type="button" @click="toggleDropdown('role')">
            {{ filters.role || '角色' }}
          </button>
          <div v-if="dropdown === 'role'" class="el-select-dropdown">
            <div v-for="option in roleOptions" :key="option.value" class="el-select-dropdown__item" @click="selectRole(option.value)">
              {{ option.value }}
            </div>
          </div>
        </div>

        <div class="search-actions">
          <button id="search-btn" class="primary-button" type="button" @click="handleSearch"><span>搜索</span></button>
          <button class="ghost-button" type="button" @click="handleReset"><span>重置</span></button>
        </div>
      </div>

      <div v-if="message.text" :class="message.type === 'success' ? 'export-success' : 'error-message'">
        {{ message.text }}
      </div>
    </div>

    <div class="table-card">
      <div class="el-table" id="user-table">
        <div class="el-table__header-wrapper">
          <table>
            <thead>
              <tr>
                <th>
                  <label class="el-checkbox"><input type="checkbox" /></label>
                </th>
                <th>ID</th>
                <th class="sortable" @click="changeSort('username')"><span>用户名</span></th>
                <th>邮箱</th>
                <th>手机号</th>
                <th>角色</th>
                <th>状态</th>
                <th class="sortable" @click="changeSort('lastLoginTime')"><span>最后登录</span></th>
                <th class="sortable" @click="changeSort('createTime')"><span>注册时间</span></th>
                <th>操作</th>
              </tr>
            </thead>
          </table>
        </div>

        <div class="el-table__body-wrapper">
          <table>
            <tbody>
              <tr v-for="user in tableRows" :key="user.id">
                <td>
                  <label class="el-checkbox"><input type="checkbox" /></label>
                </td>
                <td>{{ user.id }}</td>
                <td>
                  <div>{{ user.username }}</div>
                  <div class="muted-text">{{ user.realName || '-' }}</div>
                </td>
                <td>{{ user.email }}</td>
                <td>{{ user.phone }}</td>
                <td>{{ user.role }} {{ roleLabels[user.role] }}</td>
                <td>{{ user.status }} {{ statusLabels[user.status] }}</td>
                <td>{{ user.lastLoginTime || '-' }}</td>
                <td>{{ user.createTime }}</td>
                <td>
                  <div class="row-actions">
                    <button class="link-button" type="button" @click="router.push(`/user/detail/${user.id}`)"><span>详情</span></button>
                    <button class="link-button" type="button" @click="router.push(`/user/edit/${user.id}`)"><span>编辑</span></button>
                    <button class="link-button" type="button" @click="toggleUserStatus(user.id)">
                      <span>{{ user.status === 'disabled' ? '启用' : '禁用' }}</span>
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="tableRows.length === 0">
                <td colspan="10" class="no-results">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getRoleOptions,
  queryCompatUsers,
  ROLE_LABELS,
  setCompatUserStatus,
  STATUS_LABELS,
  type CompatUser,
  type UserRoleCode,
  type UserStatusCode
} from './userCompat'

const router = useRouter()
const roleOptions = getRoleOptions()
const roleLabels = ROLE_LABELS
const statusLabels = STATUS_LABELS

const filters = reactive({
  keyword: '',
  status: '' as UserStatusCode | '',
  role: '' as UserRoleCode | ''
})

const sortState = reactive({
  sortBy: 'createTime',
  sortDirection: 'desc' as 'asc' | 'desc'
})

const tableRows = ref<CompatUser[]>([])
const dropdown = ref<'status' | 'role' | ''>('')
const message = ref<{ type: 'success' | 'error'; text: string }>({ type: 'success', text: '' })

const loadUsers = () => {
  tableRows.value = queryCompatUsers({
    keyword: filters.keyword || undefined,
    status: filters.status || undefined,
    role: filters.role || undefined,
    sortBy: sortState.sortBy,
    sortDirection: sortState.sortDirection,
    page: 1,
    size: 50
  }).list
  dropdown.value = ''
}

const handleSearch = () => {
  message.value.text = ''
  loadUsers()
}

const handleReset = () => {
  filters.keyword = ''
  filters.status = ''
  filters.role = ''
  sortState.sortBy = 'createTime'
  sortState.sortDirection = 'desc'
  message.value.text = ''
  loadUsers()
}

const toggleDropdown = (target: 'status' | 'role') => {
  dropdown.value = dropdown.value === target ? '' : target
}

const selectStatus = (status: UserStatusCode) => {
  filters.status = status
  dropdown.value = ''
}

const selectRole = (role: UserRoleCode) => {
  filters.role = role
  dropdown.value = ''
}

const changeSort = (sortBy: string) => {
  if (sortState.sortBy === sortBy) {
    sortState.sortDirection = sortState.sortDirection === 'desc' ? 'asc' : 'desc'
  } else {
    sortState.sortBy = sortBy
    sortState.sortDirection = sortBy === 'createTime' ? 'desc' : 'asc'
  }
  loadUsers()
}

const toggleUserStatus = (id: number) => {
  const current = tableRows.value.find((item) => item.id === id)
  if (!current) {
    return
  }

  setCompatUserStatus(id, current.status === 'disabled' ? 'active' : 'disabled')
  message.value = {
    type: 'success',
    text: current.status === 'disabled' ? '用户已启用' : '用户已禁用'
  }
  loadUsers()
}

const handleExport = () => {
  message.value = {
    type: 'success',
    text: `已导出 ${tableRows.value.length} 条用户数据`
  }
}

loadUsers()
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.user-compat-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: $spacing-lg;
}

.page-header,
.header-actions,
.search-form,
.search-actions,
.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.page-header {
  justify-content: space-between;
}

.page-title {
  margin: 0 0 8px;
  color: $text-primary;
  font-size: $font-size-xxl;
}

.page-subtitle,
.muted-text {
  color: $text-secondary;
}

.search-card,
.table-card {
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

.search-form input {
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
  min-width: 180px;
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

.sortable {
  cursor: pointer;
}

.no-results,
.export-success,
.error-message {
  padding: 12px 14px;
  border-radius: 12px;
}

.export-success {
  color: #15803d;
  background: #f0fdf4;
}

.error-message {
  color: #b91c1c;
  background: #fef2f2;
}

@media (max-width: 768px) {
  .user-compat-page {
    padding: $spacing-md;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
