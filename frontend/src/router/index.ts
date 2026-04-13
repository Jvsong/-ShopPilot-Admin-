import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { getAuthState, getDefaultRoute, hasAnyRole } from '@/utils/auth'

const Login = () => import('@/views/admin/Login/Login.vue')
const Dashboard = () => import('@/views/admin/Dashboard/Dashboard.vue')
const ProductList = () => import('@/views/admin/Product/List.vue')
const ProductEdit = () => import('@/views/admin/Product/Edit.vue')
const OrderList = () => import('@/views/admin/Order/List.vue')
const UserList = () => import('@/views/admin/User/List.vue')
const PermissionList = () => import('@/views/admin/Permission/RoleList.vue')

const adminOnlyMeta = {
  requiresAuth: true,
  requiredRoles: ['ROLE_ADMIN']
}

const managerMeta = {
  requiresAuth: true,
  requiredRoles: ['ROLE_ADMIN', 'ROLE_MERCHANT', 'ROLE_OPERATOR']
}

const authenticatedMeta = {
  requiresAuth: true,
  requiredRoles: ['ROLE_ADMIN', 'ROLE_MERCHANT', 'ROLE_OPERATOR', 'ROLE_CUSTOMER_SERVICE', 'ROLE_USER']
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/admin/login'
  },
  {
    path: '/admin/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      hideHeader: true,
      hideSubNav: true
    }
  },
  {
    path: '/admin',
    redirect: '/admin/dashboard',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '仪表盘',
          ...adminOnlyMeta
        }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: ProductList,
        meta: {
          title: '商品管理',
          ...authenticatedMeta
        }
      },
      {
        path: 'products/edit/:id?',
        name: 'ProductEdit',
        component: ProductEdit,
        meta: {
          title: '商品编辑',
          ...managerMeta
        }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: OrderList,
        meta: {
          title: '订单管理',
          ...authenticatedMeta
        }
      },
      {
        path: 'users',
        name: 'UserList',
        component: UserList,
        meta: {
          title: '用户管理',
          ...adminOnlyMeta
        }
      },
      {
        path: 'permissions',
        name: 'PermissionList',
        component: PermissionList,
        meta: {
          title: '权限管理',
          ...adminOnlyMeta
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }

    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 电商后台管理系统` : '电商后台管理系统'

  if (to.meta.requiresAuth) {
    const authState = getAuthState()
    if (!authState?.token) {
      next('/admin/login')
      return
    }
  }

  if (!hasAnyRole(to.meta.requiredRoles as string[] | undefined)) {
    next(getDefaultRoute())
    return
  }

  next()
})

export default router
