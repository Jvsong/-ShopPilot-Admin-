<template>
  <div class="role-management">
    <div class="page-header">
      <div>
        <h1 class="page-title">权限管理</h1>
        <p class="page-subtitle">支持角色新增、编辑、删除和权限分配。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增角色</el-button>
    </div>

    <div class="search-card">
      <div class="search-form">
        <el-input
          v-model="searchParams.keyword"
          placeholder="搜索角色名称或编码"
          :prefix-icon="Search"
          @keyup.enter="loadRoles"
          style="width: 300px"
        />
        <el-select v-model="searchParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <div class="search-actions">
          <el-button type="primary" :icon="Search" @click="loadRoles">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="roleList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="180" />
        <el-table-column prop="code" label="角色编码" width="180" />
        <el-table-column prop="description" label="角色描述" min-width="220" />
        <el-table-column label="权限数" width="100">
          <template #default="{ row }">
            {{ row.permissionIds?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="success" @click="openPermissionDialog(row)">分配权限</el-button>
              <el-button link type="info" @click="openUserDialog(row)">分配用户</el-button>
              <el-button link :type="row.status === 1 ? 'warning' : 'primary'" @click="toggleRoleStatus(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
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

    <el-dialog v-model="roleDialogVisible" :title="editingRoleId ? '编辑角色' : '新增角色'" width="560px" destroy-on-close>
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="88px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" :disabled="!!editingRoleId" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="680px" destroy-on-close>
      <div class="permission-toolbar">
        <el-button link type="primary" @click="checkAllPermissions">全选</el-button>
        <el-button link @click="clearAllPermissions">清空</el-button>
      </div>
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        node-key="id"
        show-checkbox
        default-expand-all
        :props="{ label: 'label', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPermissions" @click="savePermissions">保存权限</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="userDialogVisible" title="分配用户" width="620px" destroy-on-close>
      <el-select
        v-model="selectedUserIds"
        multiple
        filterable
        collapse-tags
        collapse-tags-tooltip
        placeholder="选择需要关联到该角色的用户"
        style="width: 100%"
      >
        <el-option
          v-for="user in userOptions"
          :key="user.id"
          :label="`${user.username} (${user.email || user.phone || '无联系方式'})`"
          :value="user.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingUsers" @click="saveRoleUsers">保存用户</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules, ElTree } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { http } from '@/utils/request'

interface PermissionItem {
  id: number
  name: string
  code: string
  parentId: number
  type: number
  status: number
}

interface RoleItem {
  id: number
  name: string
  code: string
  description?: string
  status: number
  createTime?: string
  permissionIds?: number[]
}

interface UserOption {
  id: number
  username: string
  email?: string
  phone?: string
  status: number
}

interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  pages: number
}

interface PermissionTreeNode {
  id: number
  label: string
  children?: PermissionTreeNode[]
}

const loading = ref(false)
const submitting = ref(false)
const savingPermissions = ref(false)
const savingUsers = ref(false)
const roleDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const userDialogVisible = ref(false)
const editingRoleId = ref<number | null>(null)
const currentPermissionRoleId = ref<number | null>(null)
const currentUserRoleId = ref<number | null>(null)
const roleFormRef = ref<FormInstance>()
const permissionTreeRef = ref<InstanceType<typeof ElTree>>()

const searchParams = reactive({
  keyword: '',
  status: null as number | null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const roleList = ref<RoleItem[]>([])
const permissionList = ref<PermissionItem[]>([])
const userOptions = ref<UserOption[]>([])
const selectedUserIds = ref<number[]>([])

const roleForm = reactive({
  name: '',
  code: '',
  description: '',
  status: 1
})

const roleRules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z_]+$/, message: '角色编码需以 ROLE_ 开头且使用大写字母/下划线', trigger: 'blur' }
  ]
}

const permissionTree = computed<PermissionTreeNode[]>(() => {
  const map = new Map<number, PermissionTreeNode>()
  const roots: PermissionTreeNode[] = []

  permissionList.value.forEach((item) => {
    map.set(item.id, { id: item.id, label: `${item.name} (${item.code})`, children: [] })
  })

  permissionList.value.forEach((item) => {
    const node = map.get(item.id)!
    if (item.parentId && map.has(item.parentId)) {
      map.get(item.parentId)!.children!.push(node)
    } else {
      roots.push(node)
    }
  })

  return roots
})

const loadRoles = async () => {
  loading.value = true
  try {
    const response = await http.get<PageResponse<RoleItem>>('/admin/roles', {
      page: pagination.current,
      size: pagination.size,
      name: searchParams.keyword || undefined,
      code: searchParams.keyword || undefined,
      status: searchParams.status ?? undefined
    })

    const roles = response.list || []
    const details = await Promise.all(roles.map((role) => http.get<RoleItem>(`/admin/roles/${role.id}`)))
    roleList.value = details
    pagination.total = response.total || 0
  } catch (error) {
    console.error('加载角色失败:', error)
    ElMessage.error('加载角色失败')
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  try {
    permissionList.value = await http.get<PermissionItem[]>('/admin/permissions')
  } catch (error) {
    console.error('加载权限失败:', error)
    ElMessage.error('加载权限失败')
  }
}

const loadUsers = async () => {
  try {
    const response = await http.get<PageResponse<UserOption>>('/admin/users', {
      page: 1,
      size: 200
    })
    userOptions.value = response.list || []
  } catch (error) {
    console.error('加载用户失败:', error)
    ElMessage.error('加载用户失败')
  }
}

const resetRoleForm = () => {
  editingRoleId.value = null
  roleForm.name = ''
  roleForm.code = ''
  roleForm.description = ''
  roleForm.status = 1
}

const openCreateDialog = () => {
  resetRoleForm()
  roleDialogVisible.value = true
}

const openEditDialog = (role: RoleItem) => {
  editingRoleId.value = role.id
  roleForm.name = role.name
  roleForm.code = role.code
  roleForm.description = role.description || ''
  roleForm.status = role.status
  roleDialogVisible.value = true
}

const submitRole = async () => {
  if (!roleFormRef.value) return
  await roleFormRef.value.validate()

  submitting.value = true
  try {
    const payload = {
      name: roleForm.name,
      code: roleForm.code,
      description: roleForm.description || undefined,
      status: roleForm.status
    }

    if (editingRoleId.value) {
      await http.put(`/admin/roles/${editingRoleId.value}`, payload)
      ElMessage.success('角色已更新')
    } else {
      await http.post('/admin/roles', payload)
      ElMessage.success('角色已创建')
    }
    roleDialogVisible.value = false
    loadRoles()
  } catch (error) {
    console.error('保存角色失败:', error)
  } finally {
    submitting.value = false
  }
}

const openPermissionDialog = async (role: RoleItem) => {
  currentPermissionRoleId.value = role.id
  permissionDialogVisible.value = true
  await nextTick()
  permissionTreeRef.value?.setCheckedKeys(role.permissionIds || [])
}

const openUserDialog = async (role: RoleItem) => {
  currentUserRoleId.value = role.id
  userDialogVisible.value = true
  await loadUsers()
  const users = await http.get<UserOption[]>(`/admin/roles/${role.id}/users`)
  selectedUserIds.value = users.map((item) => item.id)
}

const savePermissions = async () => {
  if (!currentPermissionRoleId.value) return
  savingPermissions.value = true
  try {
    const checked = permissionTreeRef.value?.getCheckedKeys(false) || []
    const halfChecked = permissionTreeRef.value?.getHalfCheckedKeys() || []
    const permissionIds = Array.from(new Set([...checked, ...halfChecked])) as number[]
    await http.put(`/admin/roles/${currentPermissionRoleId.value}/permissions`, permissionIds)
    ElMessage.success('权限已保存')
    permissionDialogVisible.value = false
    loadRoles()
  } catch (error) {
    console.error('保存权限失败:', error)
  } finally {
    savingPermissions.value = false
  }
}

const saveRoleUsers = async () => {
  if (!currentUserRoleId.value) return
  savingUsers.value = true
  try {
    await http.put(`/admin/roles/${currentUserRoleId.value}/users`, selectedUserIds.value)
    ElMessage.success('角色用户已保存')
    userDialogVisible.value = false
  } catch (error) {
    console.error('保存角色用户失败:', error)
  } finally {
    savingUsers.value = false
  }
}

const toggleRoleStatus = async (role: RoleItem) => {
  try {
    await http.patch(`/admin/roles/${role.id}/status`, null, {
      params: { status: role.status === 1 ? 0 : 1 }
    })
    ElMessage.success('状态已更新')
    loadRoles()
  } catch (error) {
    console.error('更新角色状态失败:', error)
  }
}

const handleDelete = async (role: RoleItem) => {
  try {
    await ElMessageBox.confirm(`确认删除角色 ${role.name} 吗？`, '删除角色', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await http.delete(`/admin/roles/${role.id}`)
    ElMessage.success('角色已删除')
    loadRoles()
  } catch (error) {
    console.error('删除角色失败:', error)
  }
}

const handleReset = () => {
  searchParams.keyword = ''
  searchParams.status = null
  pagination.current = 1
  loadRoles()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadRoles()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadRoles()
}

const checkAllPermissions = () => {
  permissionTreeRef.value?.setCheckedKeys(permissionList.value.map((item) => item.id))
}

const clearAllPermissions = () => {
  permissionTreeRef.value?.setCheckedKeys([])
}

onMounted(async () => {
  await Promise.all([loadPermissions(), loadRoles(), loadUsers()])
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.role-management {
  padding: $spacing-lg;
}

.page-header,
.search-form,
.search-actions,
.pagination-container,
.table-actions,
.permission-toolbar {
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
.table-card {
  background: $card-bg;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
}

.search-form,
.search-actions,
.table-actions {
  gap: $spacing-md;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: $spacing-xl;
}

.permission-toolbar {
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}
</style>
