<template>
  <div class="user-management">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">保留可用的用户维护能力，移除未落地入口</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">添加用户</el-button>
      </div>
    </div>

    <div class="search-card">
      <div class="search-form">
        <el-input
          v-model="searchParams.keyword"
          placeholder="搜索用户名"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
          style="width: 300px"
        />
        <el-select v-model="searchParams.status" placeholder="全部状态" clearable style="width: 120px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-select v-model="searchParams.userType" placeholder="用户类型" clearable style="width: 140px">
          <el-option label="普通用户" :value="1" />
          <el-option label="管理员" :value="2" />
          <el-option label="商家" :value="3" />
        </el-select>
        <div class="search-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-card__icon primary"><el-icon><UserFilled /></el-icon></div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ totalUsers }}</div>
          <div class="stat-card__label">总用户数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon success"><el-icon><User /></el-icon></div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ activeUsers }}</div>
          <div class="stat-card__label">活跃用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon warning"><el-icon><User /></el-icon></div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ adminUsers }}</div>
          <div class="stat-card__label">管理员</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon info"><el-icon><Timer /></el-icon></div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ newUsersToday }}</div>
          <div class="stat-card__label">今日新增</div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="userList" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="160">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.avatar" class="user-avatar">
                {{ row.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <div class="user-detail">
                <div class="username">{{ row.username }}</div>
                <div class="user-id">ID: {{ row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.userType === 2 ? 'warning' : row.userType === 3 ? 'success' : 'info'" size="small">
              {{ row.userType === 2 ? '管理员' : row.userType === 3 ? '商家' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="user-actions">
              <el-button type="primary" link size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button
                :type="row.status === 1 ? 'danger' : 'success'"
                link
                size="small"
                :icon="row.status === 1 ? Lock : Unlock"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button type="primary" link size="small" :icon="Key" @click="handleResetPassword(row)">重置密码</el-button>
              <el-button type="danger" link size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <div class="batch-actions" v-if="selectedUsers.length > 0">
          <span>已选择 {{ selectedUsers.length }} 个用户</span>
          <el-button type="danger" link @click="handleBatchDisable">批量禁用</el-button>
          <el-button type="success" link @click="handleBatchEnable">批量启用</el-button>
          <el-button type="danger" link @click="handleBatchDelete">批量删除</el-button>
        </div>
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <UserEditForm
        v-if="dialogVisible"
        :user-id="currentUserId"
        @success="handleDialogSuccess"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Delete,
  Edit,
  Key,
  Lock,
  Plus,
  Refresh,
  Search,
  Timer,
  Unlock,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import UserEditForm from './Edit.vue'
import { http } from '@/utils/request'

interface UserItem {
  id: number
  username: string
  email?: string
  phone?: string
  avatar?: string
  userType: number
  status: number
  lastLoginTime?: string
  createTime?: string
}

interface PageResponse<T> {
  list: T[]
  total: number
}

const searchParams = reactive({
  keyword: '',
  status: null as number | null,
  userType: null as number | null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const userList = ref<UserItem[]>([])
const selectedUsers = ref<UserItem[]>([])
const loading = ref(false)
const totalUsers = ref(0)
const activeUsers = ref(0)
const adminUsers = ref(0)
const newUsersToday = ref(0)
const dialogVisible = ref(false)
const currentUserId = ref<number | null>(null)
const dialogTitle = computed(() => (currentUserId.value ? '编辑用户' : '添加用户'))

const updateStatistics = (users: UserItem[], total: number) => {
  totalUsers.value = total
  activeUsers.value = users.filter((item) => item.status === 1).length
  adminUsers.value = users.filter((item) => item.userType === 2 || item.userType === 3).length
  const today = new Date().toISOString().slice(0, 10)
  newUsersToday.value = users.filter((item) => item.createTime?.startsWith(today)).length
}

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await http.get<PageResponse<UserItem>>('/admin/users', {
      username: searchParams.keyword || undefined,
      status: searchParams.status ?? undefined,
      userType: searchParams.userType ?? undefined,
      page: pagination.current,
      size: pagination.size
    })
    userList.value = response.list || []
    pagination.total = response.total || 0
    updateStatistics(userList.value, pagination.total)
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadUsers()
}

const handleReset = () => {
  searchParams.keyword = ''
  searchParams.status = null
  searchParams.userType = null
  pagination.current = 1
  loadUsers()
}

const handleAdd = () => {
  currentUserId.value = null
  dialogVisible.value = true
}

const handleEdit = (user: UserItem) => {
  currentUserId.value = user.id
  dialogVisible.value = true
}

const handleToggleStatus = async (user: UserItem) => {
  const newStatus = user.status === 1 ? 0 : 1
  try {
    await http.patch(`/admin/users/${user.id}/status`, { status: newStatus })
    user.status = newStatus
    ElMessage.success('用户状态更新成功')
  } catch (error) {
    console.error('更新用户状态失败:', error)
    ElMessage.error('更新用户状态失败')
  }
}

const handleResetPassword = async (user: UserItem) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入新密码',
      inputType: 'password'
    })
    if (!value) return
    await http.post(`/admin/users/${user.id}/reset-password`, { password: value })
    ElMessage.success('密码重置成功')
  } catch (error) {
    console.error('重置密码失败:', error)
  }
}

const handleDelete = async (user: UserItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await http.delete(`/admin/users/${user.id}`)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (error) {
    console.error('删除用户失败:', error)
  }
}

const handleSelectionChange = (selection: UserItem[]) => {
  selectedUsers.value = selection
}

const handleBatchDisable = async () => {
  if (!selectedUsers.value.length) return
  await http.post('/admin/users/batch-disable', { ids: selectedUsers.value.map((item) => item.id) })
  ElMessage.success('批量禁用成功')
  selectedUsers.value = []
  loadUsers()
}

const handleBatchEnable = async () => {
  if (!selectedUsers.value.length) return
  await http.post('/admin/users/batch-enable', { ids: selectedUsers.value.map((item) => item.id) })
  ElMessage.success('批量启用成功')
  selectedUsers.value = []
  loadUsers()
}

const handleBatchDelete = async () => {
  if (!selectedUsers.value.length) return
  await http.post('/admin/users/batch-delete', { ids: selectedUsers.value.map((item) => item.id) })
  ElMessage.success('批量删除成功')
  selectedUsers.value = []
  loadUsers()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadUsers()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadUsers()
}

const handleDialogSuccess = () => {
  dialogVisible.value = false
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.user-management {
  padding: $spacing-lg;
}

.page-header,
.search-form,
.search-actions,
.pagination-container,
.user-actions {
  display: flex;
  align-items: center;
}

.page-header,
.pagination-container {
  justify-content: space-between;
}

.page-header {
  margin-bottom: $spacing-xl;
}

.page-title {
  font-size: $font-size-xxl;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-sm;
}

.page-subtitle {
  color: $text-secondary;
}

.search-card,
.table-card,
.stat-card {
  background: $card-bg;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
}

.search-card,
.table-card {
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.search-form,
.search-actions,
.user-actions,
.batch-actions {
  gap: $spacing-md;
  flex-wrap: wrap;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  padding: $spacing-lg;
}

.stat-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.primary { background: rgba(22, 93, 255, 0.1); color: var(--primary-color); }
  &.success { background: rgba(0, 180, 42, 0.1); color: var(--success-color); }
  &.warning { background: rgba(255, 125, 0, 0.1); color: var(--warning-color); }
  &.info { background: rgba(53, 145, 250, 0.1); color: var(--info-color); }
}

.stat-card__value {
  font-size: $font-size-xl;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.user-avatar {
  background-color: $primary-color;
  color: #fff;
}

.username {
  font-weight: 500;
}

.user-id {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.pagination-container {
  margin-top: $spacing-xl;
}
</style>
