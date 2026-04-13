<template>
  <div id="app">
    <header v-if="showHeader" class="top-navbar">
      <div class="navbar-brand">
        <div class="logo">EC</div>
        <span>电商后台管理系统</span>
      </div>
      <div class="navbar-right">
        <div class="user-info">
          <div class="avatar">
            <el-icon><User /></el-icon>
          </div>
          <span>{{ currentUser?.username || '访客' }}</span>
          <span class="role-badge" :class="roleBadgeClass">
            {{ currentRoleLabel }}
          </span>
          <el-icon><ArrowDown /></el-icon>
        </div>
      </div>
    </header>

    <nav v-if="showSubNav && visibleMenuItems.length > 0" class="sub-navbar">
      <div class="nav-menu">
        <div v-for="item in visibleMenuItems" :key="item.path" class="nav-item">
          <router-link :to="item.path" :class="{ active: $route.path.startsWith(item.path) }">
            <el-icon v-if="item.icon" style="margin-right: 4px">
              <component :is="item.icon" />
            </el-icon>
            {{ item.title }}
          </router-link>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowDown, Goods, HomeFilled, Setting, ShoppingCart, User, UserFilled } from '@element-plus/icons-vue'
import { getCurrentUser, getCurrentRoles, hasAnyRole } from '@/utils/auth'

const route = useRoute()

const showHeader = computed(() => !route.meta.hideHeader)
const showSubNav = computed(() => !route.meta.hideSubNav)
const currentUser = computed(() => {
  route.fullPath
  return getCurrentUser()
})

const roleLabelMap: Record<string, string> = {
  ROLE_ADMIN: '系统管理员',
  ROLE_OPERATOR: '运营',
  ROLE_CUSTOMER_SERVICE: '客服',
  ROLE_MERCHANT: '商家',
  ROLE_USER: '普通用户'
}

const menuItems = ref([
  { path: '/admin/dashboard', title: '仪表盘', icon: HomeFilled, requiredRoles: ['ROLE_ADMIN'] },
  { path: '/admin/products', title: '商品管理', icon: Goods, requiredRoles: ['ROLE_ADMIN', 'ROLE_MERCHANT', 'ROLE_OPERATOR'] },
  { path: '/admin/orders', title: '订单管理', icon: ShoppingCart, requiredRoles: ['ROLE_ADMIN', 'ROLE_MERCHANT', 'ROLE_OPERATOR', 'ROLE_CUSTOMER_SERVICE', 'ROLE_USER'] },
  { path: '/admin/users', title: '用户管理', icon: UserFilled, requiredRoles: ['ROLE_ADMIN'] },
  { path: '/admin/permissions', title: '权限管理', icon: Setting, requiredRoles: ['ROLE_ADMIN'] }
])

const visibleMenuItems = computed(() => {
  route.fullPath
  return menuItems.value.filter((item) => hasAnyRole(item.requiredRoles))
})

const currentRole = computed(() => {
  const roles = getCurrentRoles()
  return ['ROLE_ADMIN', 'ROLE_OPERATOR', 'ROLE_CUSTOMER_SERVICE', 'ROLE_MERCHANT', 'ROLE_USER'].find((item) => roles.includes(item))
})

const currentRoleLabel = computed(() => currentRole.value ? roleLabelMap[currentRole.value] : '访客')

const roleBadgeClass = computed(() => ({
  merchant: currentRole.value === 'ROLE_MERCHANT',
  user: currentRole.value === 'ROLE_USER',
  support: currentRole.value === 'ROLE_OPERATOR' || currentRole.value === 'ROLE_CUSTOMER_SERVICE'
}))
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.main-content {
  flex: 1;
  overflow: auto;
  background-color: $page-bg;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-badge {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(22, 93, 255, 0.1);
  color: #165dff;
  font-size: 12px;

  &.merchant {
    background: rgba(255, 125, 0, 0.12);
    color: #ff7d00;
  }

  &.user {
    background: rgba(0, 180, 42, 0.12);
    color: #00b42a;
  }

  &.support {
    background: rgba(52, 145, 250, 0.12);
    color: #3491fa;
  }
}
</style>
