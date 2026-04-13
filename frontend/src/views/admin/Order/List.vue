<template>
  <div class="order-management">
    <div class="page-header">
      <div>
        <h1 class="page-title">订单管理</h1>
        <p class="page-subtitle">{{ pageSubtitle }}</p>
      </div>
    </div>

    <div class="search-card">
      <div class="search-form">
        <el-input
          v-model="searchParams.orderNo"
          placeholder="搜索订单号"
          :prefix-icon="Search"
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-if="!isUser"
          v-model="searchParams.username"
          placeholder="搜索用户名"
          :prefix-icon="User"
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="searchParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option label="待付款" :value="1" />
          <el-option label="待发货" :value="2" />
          <el-option label="已发货" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
        <el-date-picker
          v-model="searchParams.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 320px"
        />
        <div class="search-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <div v-if="!isUser" class="stats-cards">
      <div v-for="stat in orderStats" :key="stat.type" class="stat-card">
        <div class="stat-card__icon" :style="{ backgroundColor: stat.bgColor }">
          <el-icon :style="{ color: stat.color }"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ stat.value.toLocaleString() }}</div>
          <div class="stat-card__label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="orderList" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column v-if="canManageOrders" type="selection" width="55" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column v-if="!isUser" label="客户信息" width="220">
          <template #default="{ row }">
            <div class="customer-info">
              <div class="customer-name">{{ row.customerName || row.username || '-' }}</div>
              <div class="customer-contact">{{ row.customerPhone || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="金额" width="120" sortable>
          <template #default="{ row }">
            <div class="amount">￥{{ Number(row.actualAmount || row.totalAmount || 0).toFixed(2) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" sortable />
        <el-table-column label="操作" :width="isUser ? 220 : 260" fixed="right">
          <template #default="{ row }">
            <div class="order-actions">
              <el-button type="primary" link size="small" :icon="View" @click="handleViewDetail(row)">详情</el-button>

              <template v-if="!isUser">
                <el-button v-if="canManageOrders && row.status === 2" type="success" link size="small" :icon="Van" @click="handleShip(row)">
                  发货
                </el-button>
                <el-dropdown
                  v-if="canManageOrders && (row.status === 1 || row.status === 2)"
                  @command="(command) => handleOrderCommand(row, command as string)"
                >
                  <el-button type="primary" link size="small">
                    更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="cancel" :icon="Close">取消订单</el-dropdown-item>
                      <el-dropdown-item command="remark" :icon="Edit">添加备注</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>

              <template v-else>
                <el-button v-if="row.status === 1" type="danger" link size="small" @click="handleUserCancel(row)">
                  取消订单
                </el-button>
                <el-button v-if="row.status === 3" type="success" link size="small" @click="handleConfirmReceipt(row)">
                  确认收货
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <div v-if="canManageOrders && selectedOrders.length > 0" class="batch-actions">
          <span>已选择 {{ selectedOrders.length }} 个订单</span>
          <el-button type="danger" link @click="handleBatchCancel">批量取消</el-button>
          <el-button type="success" link @click="handleBatchShip">批量发货</el-button>
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

    <OrderDetailDialog v-if="detailDialogVisible" :order-id="currentOrderId" @close="detailDialogVisible = false" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Check,
  Close,
  CloseBold,
  Edit,
  Refresh,
  Search,
  ShoppingCart,
  Timer,
  User,
  Van,
  View
} from '@element-plus/icons-vue'
import OrderDetailDialog from './Detail.vue'
import { orderApi, type Order, type OrderStatistics, userOrderApi } from '@/api/order'
import { hasRole } from '@/utils/auth'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const searchParams = reactive({
  orderNo: '',
  username: '',
  status: null as number | null,
  dateRange: [] as string[]
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const orderList = ref<Order[]>([])
const loading = ref(false)
const selectedOrders = ref<Order[]>([])
const detailDialogVisible = ref(false)
const currentOrderId = ref<number | null>(null)

const isUser = computed(() => hasRole('ROLE_USER') && !hasRole('ROLE_ADMIN') && !hasRole('ROLE_MERCHANT') && !hasRole('ROLE_OPERATOR') && !hasRole('ROLE_CUSTOMER_SERVICE'))
const canManageOrders = computed(() => hasRole('ROLE_ADMIN') || hasRole('ROLE_MERCHANT') || hasRole('ROLE_OPERATOR'))
const pageSubtitle = computed(() => {
  if (hasRole('ROLE_ADMIN')) return '管理员可查看并处理全部订单'
  if (hasRole('ROLE_OPERATOR')) return '运营可查看并处理订单'
  if (hasRole('ROLE_CUSTOMER_SERVICE')) return '客服可查看订单并跟进状态'
  if (hasRole('ROLE_MERCHANT')) return '商家处理自己店铺关联订单'
  return '普通用户仅查看自己的订单'
})

const orderStatistics = ref<OrderStatistics>({
  totalOrders: 0,
  totalSales: 0,
  averageOrderValue: 0,
  pendingPaymentCount: 0,
  pendingShipmentCount: 0,
  shippedCount: 0,
  completedCount: 0,
  cancelledCount: 0
})

const orderStats = computed(() => [
  { type: 'pending', label: '待付款', value: orderStatistics.value.pendingPaymentCount, icon: Timer, color: '#FF7D00', bgColor: 'rgba(255, 125, 0, 0.1)' },
  { type: 'processing', label: '待发货', value: orderStatistics.value.pendingShipmentCount, icon: ShoppingCart, color: '#3491FA', bgColor: 'rgba(53, 145, 250, 0.1)' },
  { type: 'shipped', label: '已发货', value: orderStatistics.value.shippedCount, icon: Van, color: '#165DFF', bgColor: 'rgba(22, 93, 255, 0.1)' },
  { type: 'completed', label: '已完成', value: orderStatistics.value.completedCount, icon: Check, color: '#00B42A', bgColor: 'rgba(0, 180, 42, 0.1)' },
  { type: 'cancelled', label: '已取消', value: orderStatistics.value.cancelledCount, icon: CloseBold, color: '#F53F3F', bgColor: 'rgba(245, 63, 63, 0.1)' }
])

const getStatusTagType = (status: number): TagType => {
  const types: Record<number, TagType> = { 1: 'warning', 2: 'info', 3: 'primary', 4: 'success', 5: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 1: '待付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }
  return texts[status] || '未知状态'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const response = isUser.value
      ? await userOrderApi.getMyOrders(pagination.current, pagination.size)
      : await orderApi.getList({
          orderNo: searchParams.orderNo || undefined,
          username: searchParams.username || undefined,
          status: searchParams.status || undefined,
          startDate: searchParams.dateRange[0],
          endDate: searchParams.dateRange[1],
          page: pagination.current,
          size: pagination.size
        })

    orderList.value = response.list || []
    pagination.total = response.total || 0
    if (!isUser.value) {
      await loadOrderStatistics()
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const loadOrderStatistics = async () => {
  try {
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    orderStatistics.value = await orderApi.getStatistics(startDate, endDate)
  } catch (error) {
    console.error('加载订单统计失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadOrders()
}

const handleReset = () => {
  searchParams.orderNo = ''
  searchParams.username = ''
  searchParams.status = null
  searchParams.dateRange = []
  pagination.current = 1
  loadOrders()
}

const handleViewDetail = (order: Order) => {
  currentOrderId.value = order.id
  detailDialogVisible.value = true
}

const handleShip = async (order: Order) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入物流单号', '发货确认', {
      confirmButtonText: '确认发货',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入物流单号'
    })
    if (!value) {
      ElMessage.error('请输入物流单号')
      return
    }
    await orderApi.ship(order.id, '默认物流', value)
    ElMessage.success('发货成功')
    loadOrders()
  } catch (error) {
    console.error('发货失败:', error)
  }
}

const handleOrderCommand = async (order: Order, command: string) => {
  if (command === 'cancel') await handleCancelOrder(order)
  if (command === 'remark') await handleAddRemark(order)
}

const handleCancelOrder = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确认取消订单 ${order.orderNo} 吗？`, '取消订单', {
      confirmButtonText: '确认',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await orderApi.updateStatus(order.id, 5)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    console.error('取消订单失败:', error)
  }
}

const handleAddRemark = async (order: Order) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入备注', '添加备注', {
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入备注'
    })
    if (!value) return
    await orderApi.update(order.id, { remark: value })
    ElMessage.success('备注已保存')
    loadOrders()
  } catch (error) {
    console.error('保存备注失败:', error)
  }
}

const handleUserCancel = async (order: Order) => {
  try {
    await userOrderApi.cancelMyOrder(order.id, '用户主动取消')
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    console.error('用户取消订单失败:', error)
  }
}

const handleConfirmReceipt = async (order: Order) => {
  try {
    await userOrderApi.confirmReceipt(order.id)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch (error) {
    console.error('确认收货失败:', error)
  }
}

const handleSelectionChange = (selection: Order[]) => {
  selectedOrders.value = selection
}

const handleBatchCancel = async () => {
  if (!selectedOrders.value.length) return
  try {
    await ElMessageBox.confirm(`确认取消选中的 ${selectedOrders.value.length} 个订单吗？`, '批量取消', {
      confirmButtonText: '确认',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await Promise.all(selectedOrders.value.map((item) => orderApi.updateStatus(item.id, 5)))
    selectedOrders.value = []
    ElMessage.success('批量取消成功')
    loadOrders()
  } catch (error) {
    console.error('批量取消失败:', error)
  }
}

const handleBatchShip = async () => {
  if (!selectedOrders.value.length) return
  try {
    const { value } = await ElMessageBox.prompt('请输入物流单号', '批量发货', {
      confirmButtonText: '确认发货',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入物流单号'
    })
    if (!value) {
      ElMessage.error('请输入物流单号')
      return
    }
    await Promise.all(selectedOrders.value.map((item) => orderApi.ship(item.id, '默认物流', value)))
    selectedOrders.value = []
    ElMessage.success('批量发货成功')
    loadOrders()
  } catch (error) {
    console.error('批量发货失败:', error)
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadOrders()
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.order-management {
  padding: $spacing-lg;
}

.page-header,
.search-form,
.pagination-container,
.order-actions {
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

.page-subtitle,
.customer-contact {
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
.order-actions,
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
}

.stat-card__value {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $text-primary;
}

.customer-name,
.amount {
  font-weight: 600;
}

.amount {
  color: $danger-color;
}

@media (max-width: 768px) {
  .order-management {
    padding: $spacing-md;
  }

  .search-form,
  .pagination-container {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
