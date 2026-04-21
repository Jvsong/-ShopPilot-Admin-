import { getCompatUsers } from '../User/userCompat'

export type RoleStatusCode = 'active' | 'disabled'
export type PermissionTabCode = 'system' | 'product' | 'order' | 'user' | 'finance' | 'report'

export interface CompatPermission {
  id: number
  name: string
  code: string
  tab: PermissionTabCode
}

export interface CompatRole {
  id: number
  name: string
  code: string
  description: string
  status: RoleStatusCode
  createTime: string
  permissionCodes: string[]
  usernames: string[]
}

const STORAGE_KEY = 'shop-system-permission-compat'

export const ROLE_STATUS_LABELS: Record<RoleStatusCode, string> = {
  active: 'active',
  disabled: 'disabled'
}

export const PERMISSION_TABS: PermissionTabCode[] = ['system', 'product', 'order', 'user', 'finance', 'report']

export const DEFAULT_PERMISSIONS: CompatPermission[] = [
  { id: 1, name: '系统首页', code: 'system_view', tab: 'system' },
  { id: 2, name: '商品查看', code: 'product_view', tab: 'product' },
  { id: 3, name: '商品编辑', code: 'product_edit', tab: 'product' },
  { id: 4, name: '订单查看', code: 'order_view', tab: 'order' },
  { id: 5, name: '订单发货', code: 'order_ship', tab: 'order' },
  { id: 6, name: '用户查看', code: 'user_view', tab: 'user' },
  { id: 7, name: '用户编辑', code: 'user_edit', tab: 'user' },
  { id: 8, name: '财务报表', code: 'finance_report', tab: 'finance' },
  { id: 9, name: '运营报表', code: 'report_view', tab: 'report' }
]

const DEFAULT_ROLES: CompatRole[] = [
  {
    id: 1,
    name: '管理员',
    code: 'ROLE_ADMIN',
    description: '系统管理员，拥有全部权限',
    status: 'active',
    createTime: '2024-01-01 09:00:00',
    permissionCodes: DEFAULT_PERMISSIONS.map((item) => item.code),
    usernames: ['admin']
  },
  {
    id: 2,
    name: '操作员',
    code: 'ROLE_OPERATOR',
    description: '负责商品和订单日常处理',
    status: 'active',
    createTime: '2024-01-02 10:00:00',
    permissionCodes: ['product_view', 'product_edit', 'order_view', 'order_ship'],
    usernames: ['operator1']
  },
  {
    id: 3,
    name: '客服',
    code: 'ROLE_CUSTOMER_SERVICE',
    description: '负责订单跟进和客户支持',
    status: 'active',
    createTime: '2024-01-03 11:00:00',
    permissionCodes: ['order_view', 'user_view'],
    usernames: ['service1']
  }
]

const cloneDefaultRoles = () => DEFAULT_ROLES.map((item) => ({ ...item, permissionCodes: [...item.permissionCodes], usernames: [...item.usernames] }))

const persistRoles = (roles: CompatRole[]) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(roles))
}

export const getCompatRoles = () => {
  const cached = localStorage.getItem(STORAGE_KEY)
  if (!cached) {
    const seeded = cloneDefaultRoles()
    persistRoles(seeded)
    return seeded
  }

  try {
    const parsed = JSON.parse(cached) as CompatRole[]
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : cloneDefaultRoles()
  } catch {
    const seeded = cloneDefaultRoles()
    persistRoles(seeded)
    return seeded
  }
}

export const getCompatRole = (id: number) => getCompatRoles().find((item) => item.id === id) || null

const normalize = (value?: string | null) => (value || '').trim().toLowerCase()

export const queryCompatRoles = (keyword = '', status?: RoleStatusCode) => {
  let roles = getCompatRoles()
  if (keyword.trim()) {
    const target = normalize(keyword)
    roles = roles.filter((item) => [item.name, item.code, item.description].some((field) => normalize(field).includes(target)))
  }
  if (status) {
    roles = roles.filter((item) => item.status === status)
  }
  return roles
}

export const createCompatRole = (payload: Pick<CompatRole, 'name' | 'code' | 'description' | 'status'>) => {
  const roles = getCompatRoles()
  const nextId = roles.reduce((maxId, item) => Math.max(maxId, item.id), 0) + 1
  const role: CompatRole = {
    id: nextId,
    name: payload.name,
    code: payload.code,
    description: payload.description,
    status: payload.status,
    createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    permissionCodes: [],
    usernames: []
  }
  persistRoles([...roles, role])
  return role
}

export const updateCompatRole = (id: number, payload: Partial<CompatRole>) => {
  const roles = getCompatRoles()
  const nextRoles = roles.map((item) => (item.id === id ? { ...item, ...payload } : item))
  persistRoles(nextRoles)
  return nextRoles.find((item) => item.id === id) || null
}

export const deleteCompatRole = (id: number) => {
  const nextRoles = getCompatRoles().filter((item) => item.id !== id)
  persistRoles(nextRoles)
}

export const assignCompatRolePermissions = (id: number, permissionCodes: string[]) =>
  updateCompatRole(id, {
    permissionCodes: Array.from(new Set(permissionCodes))
  })

export const assignCompatRoleUsers = (id: number, usernames: string[]) =>
  updateCompatRole(id, {
    usernames: Array.from(new Set(usernames.filter(Boolean)))
  })

export const getPermissionsByTab = (tab: PermissionTabCode) => DEFAULT_PERMISSIONS.filter((item) => item.tab === tab)

export const getUserOptions = () => getCompatUsers().map((item) => item.username)
