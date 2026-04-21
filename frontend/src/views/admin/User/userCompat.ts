export type UserRoleCode = 'admin' | 'operator' | 'customer_service' | 'finance' | 'warehouse' | 'viewer'
export type UserStatusCode = 'active' | 'disabled' | 'locked' | 'pending_activation'

export interface CompatUser {
  id: number
  username: string
  password: string
  email: string
  phone: string
  realName: string
  role: UserRoleCode
  status: UserStatusCode
  createTime: string
  lastLoginTime: string
  loginCount: number
  description: string
}

interface QueryOptions {
  keyword?: string
  status?: UserStatusCode
  role?: UserRoleCode
  sortBy?: string
  sortDirection?: 'asc' | 'desc'
  page?: number
  size?: number
}

const STORAGE_KEY = 'shop-system-user-compat'

export const ROLE_LABELS: Record<UserRoleCode, string> = {
  admin: '管理员',
  operator: '操作员',
  customer_service: '客服',
  finance: '财务',
  warehouse: '仓库管理员',
  viewer: '查看员'
}

export const STATUS_LABELS: Record<UserStatusCode, string> = {
  active: '正常',
  disabled: '禁用',
  locked: '锁定',
  pending_activation: '待激活'
}

const DEFAULT_USERS: CompatUser[] = [
  {
    id: 1001,
    username: 'admin',
    password: 'password123',
    email: 'admin@example.com',
    phone: '13800138001',
    realName: '张三',
    role: 'admin',
    status: 'active',
    createTime: '2024-01-01 10:00:00',
    lastLoginTime: '2024-04-04 09:30:00',
    loginCount: 18,
    description: '系统管理员，拥有所有权限'
  },
  {
    id: 1002,
    username: 'operator1',
    password: 'operator123',
    email: 'operator1@example.com',
    phone: '13800138002',
    realName: '李四',
    role: 'operator',
    status: 'active',
    createTime: '2024-01-02 11:00:00',
    lastLoginTime: '2024-04-03 14:20:00',
    loginCount: 11,
    description: '商品管理操作员'
  },
  {
    id: 1003,
    username: 'service1',
    password: 'service123',
    email: 'service1@example.com',
    phone: '13800138003',
    realName: '王五',
    role: 'customer_service',
    status: 'active',
    createTime: '2024-01-03 09:30:00',
    lastLoginTime: '2024-04-02 10:15:00',
    loginCount: 7,
    description: '客服人员，负责客户咨询'
  },
  {
    id: 1004,
    username: 'disabled_user',
    password: 'password123',
    email: 'disabled@example.com',
    phone: '13800138004',
    realName: '赵六',
    role: 'viewer',
    status: 'disabled',
    createTime: '2024-01-04 14:00:00',
    lastLoginTime: '2024-02-01 09:00:00',
    loginCount: 2,
    description: '已禁用用户'
  },
  {
    id: 1005,
    username: 'pending_user',
    password: 'password123',
    email: 'pending@example.com',
    phone: '13800138005',
    realName: '钱七',
    role: 'viewer',
    status: 'pending_activation',
    createTime: '2024-04-04 08:30:00',
    lastLoginTime: '',
    loginCount: 0,
    description: '新注册用户，待激活'
  }
]

const cloneDefaultUsers = () => DEFAULT_USERS.map((item) => ({ ...item }))

const formatDateTime = (date: Date) => {
  const parts = [
    date.getFullYear(),
    `${date.getMonth() + 1}`.padStart(2, '0'),
    `${date.getDate()}`.padStart(2, '0')
  ]
  const time = [
    `${date.getHours()}`.padStart(2, '0'),
    `${date.getMinutes()}`.padStart(2, '0'),
    `${date.getSeconds()}`.padStart(2, '0')
  ]

  return `${parts.join('-')} ${time.join(':')}`
}

const persistUsers = (users: CompatUser[]) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(users))
}

export const getCompatUsers = () => {
  const cached = localStorage.getItem(STORAGE_KEY)
  if (!cached) {
    const seeded = cloneDefaultUsers()
    persistUsers(seeded)
    return seeded
  }

  try {
    const parsed = JSON.parse(cached) as CompatUser[]
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : cloneDefaultUsers()
  } catch {
    const seeded = cloneDefaultUsers()
    persistUsers(seeded)
    return seeded
  }
}

export const getCompatUser = (id: number) => getCompatUsers().find((item) => item.id === id) || null

export const getRoleOptions = () =>
  (Object.keys(ROLE_LABELS) as UserRoleCode[]).map((value) => ({
    value,
    label: ROLE_LABELS[value]
  }))

export const getStatusOptions = () =>
  (Object.keys(STATUS_LABELS) as UserStatusCode[]).map((value) => ({
    value,
    label: STATUS_LABELS[value]
  }))

const normalizeText = (value?: string | null) => (value || '').trim().toLowerCase()

const compareByField = (left: CompatUser, right: CompatUser, sortBy: string, sortDirection: 'asc' | 'desc') => {
  const factor = sortDirection === 'asc' ? 1 : -1

  if (sortBy === 'username') {
    return left.username.localeCompare(right.username) * factor
  }

  if (sortBy === 'lastLoginTime') {
    return ((Date.parse(left.lastLoginTime || '1970-01-01') || 0) - (Date.parse(right.lastLoginTime || '1970-01-01') || 0)) * factor
  }

  return ((Date.parse(left.createTime) || 0) - (Date.parse(right.createTime) || 0)) * factor
}

export const queryCompatUsers = ({
  keyword,
  status,
  role,
  sortBy = 'createTime',
  sortDirection = 'desc',
  page = 1,
  size = 10
}: QueryOptions) => {
  let users = getCompatUsers()

  if (keyword) {
    const normalizedKeyword = normalizeText(keyword)
    users = users.filter((item) =>
      [
        item.username,
        item.email,
        item.phone,
        item.realName,
        item.role,
        item.description
      ].some((field) => normalizeText(field).includes(normalizedKeyword))
    )
  }

  if (status) {
    users = users.filter((item) => item.status === status)
  }

  if (role) {
    users = users.filter((item) => item.role === role)
  }

  const sorted = [...users].sort((left, right) => compareByField(left, right, sortBy, sortDirection))
  const start = (page - 1) * size

  return {
    list: sorted.slice(start, start + size),
    total: sorted.length
  }
}

export const createCompatUser = (payload: Omit<CompatUser, 'id' | 'createTime' | 'lastLoginTime' | 'loginCount'>) => {
  const users = getCompatUsers()
  const nextId = users.reduce((maxId, item) => Math.max(maxId, item.id), 1999) + 1
  const now = formatDateTime(new Date())
  const nextUser: CompatUser = {
    id: nextId,
    username: payload.username,
    password: payload.password,
    email: payload.email,
    phone: payload.phone,
    realName: payload.realName,
    role: payload.role,
    status: payload.status,
    createTime: now,
    lastLoginTime: '',
    loginCount: 0,
    description: payload.description
  }

  persistUsers([nextUser, ...users])
  return nextUser
}

export const updateCompatUser = (id: number, payload: Partial<CompatUser>) => {
  const users = getCompatUsers()
  const nextUsers = users.map((item) => (item.id === id ? { ...item, ...payload } : item))
  persistUsers(nextUsers)
  return nextUsers.find((item) => item.id === id) || null
}

export const setCompatUserStatus = (id: number, status: UserStatusCode) =>
  updateCompatUser(id, {
    status,
    lastLoginTime: status === 'active' ? formatDateTime(new Date()) : getCompatUser(id)?.lastLoginTime || ''
  })

export const resetCompatPassword = (id: number, password: string) => updateCompatUser(id, { password })

export const deleteCompatUser = (id: number) => {
  const users = getCompatUsers().filter((item) => item.id !== id)
  persistUsers(users)
}
