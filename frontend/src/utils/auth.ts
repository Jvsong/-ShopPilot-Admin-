export interface UserInfo {
  id: number
  username: string
  email?: string
  phone?: string
  avatar?: string
  status?: number
  userType?: number
  createTime?: string
  lastLoginTime?: string
  roles?: string[]
}

export interface AuthState {
  token: string
  refreshToken?: string
  userInfo?: UserInfo
}

const AUTH_STORAGE_KEY = 'auth_state'

const ROLE_HOME_ROUTE: Record<string, string> = {
  ROLE_ADMIN: '/admin/dashboard',
  ROLE_OPERATOR: '/admin/orders',
  ROLE_CUSTOMER_SERVICE: '/admin/orders',
  ROLE_MERCHANT: '/admin/products',
  ROLE_USER: '/admin/orders'
}

const normalizeRoles = (roles?: string[]) =>
  Array.from(
    new Set(
      (roles || [])
        .filter(Boolean)
        .map((role) => (role.startsWith('ROLE_') ? role : `ROLE_${role}`))
    )
  )

export const getAuthState = (): AuthState | null => {
  const token = localStorage.getItem('token')
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)

  if (!token && !raw) {
    return null
  }

  if (!raw) {
    return token ? { token } : null
  }

  try {
    const parsed = JSON.parse(raw) as AuthState
    if (parsed.userInfo) {
      parsed.userInfo.roles = normalizeRoles(parsed.userInfo.roles)
    }
    parsed.token = parsed.token || token || ''
    return parsed
  } catch {
    return token ? { token } : null
  }
}

export const saveAuthState = (state: AuthState) => {
  const nextState: AuthState = {
    ...state,
    userInfo: state.userInfo
      ? {
          ...state.userInfo,
          roles: normalizeRoles(state.userInfo.roles)
        }
      : undefined
  }

  localStorage.setItem('token', nextState.token)
  if (nextState.refreshToken) {
    localStorage.setItem('refreshToken', nextState.refreshToken)
  } else {
    localStorage.removeItem('refreshToken')
  }
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextState))
}

export const clearAuthState = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export const getCurrentUser = () => getAuthState()?.userInfo

export const getCurrentRoles = () => normalizeRoles(getCurrentUser()?.roles)

export const hasRole = (role: string) => getCurrentRoles().includes(role)

export const hasAnyRole = (requiredRoles?: string[]) => {
  if (!requiredRoles || requiredRoles.length === 0) {
    return true
  }

  const roles = getCurrentRoles()
  return requiredRoles.some((role) => roles.includes(role))
}

export const getDefaultRoute = (roles?: string[]) => {
  const currentRoles = normalizeRoles(roles?.length ? roles : getCurrentRoles())

  for (const role of ['ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_CUSTOMER_SERVICE', 'ROLE_MERCHANT', 'ROLE_USER']) {
    if (currentRoles.includes(role)) {
      return ROLE_HOME_ROUTE[role]
    }
  }

  return '/admin/products'
}
