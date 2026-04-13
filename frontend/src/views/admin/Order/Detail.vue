<template>
  <el-dialog
    v-model="visible"
    :title="`订单详情 - ${orderData.orderNo || ''}`"
    width="900px"
    destroy-on-close
    @close="handleClose"
  >
    <div v-loading="loading" class="order-detail">
      <div class="info-section">
        <h3 class="section-title">订单信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">订单号：</span>
            <span class="info-value">{{ orderData.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">订单状态：</span>
            <el-tag :type="getStatusTagType(orderData.status)" size="small">{{ getStatusText(orderData.status) }}</el-tag>
          </div>
          <div class="info-item">
            <span class="info-label">下单时间：</span>
            <span class="info-value">{{ orderData.createTime || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付方式：</span>
            <span class="info-value">{{ getPaymentMethodText(orderData.paymentMethod) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">支付时间：</span>
            <span class="info-value">{{ orderData.paymentTime || '未支付' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">发货时间：</span>
            <span class="info-value">{{ orderData.shippingTime || '未发货' }}</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="section-title">客户信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">客户：</span>
            <span class="info-value">{{ orderData.customerName || orderData.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系电话：</span>
            <span class="info-value">{{ orderData.customerPhone || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">收货地址：</span>
            <span class="info-value">{{ orderData.shippingAddress || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="info-section">
        <h3 class="section-title">商品信息</h3>
        <el-table :data="orderData.items || []" style="width: 100%" border>
          <el-table-column prop="productName" label="商品名称" min-width="200" />
          <el-table-column prop="productId" label="商品ID" width="120" />
          <el-table-column label="单价" width="120">
            <template #default="{ row }">￥{{ Number(row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">￥{{ Number(row.totalPrice || 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <div class="info-section">
        <h3 class="section-title">订单金额</h3>
        <div class="amount-list">
          <div class="amount-item">
            <span class="amount-label">商品总价：</span>
            <span class="amount-value">￥{{ Number(orderData.totalAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-item">
            <span class="amount-label">运费：</span>
            <span class="amount-value">￥{{ Number(orderData.shippingFee || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-item">
            <span class="amount-label">优惠：</span>
            <span class="amount-value text-success">-￥{{ Number(orderData.discountAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-item total">
            <span class="amount-label">实付：</span>
            <span class="amount-value">￥{{ Number(orderData.actualAmount || 0).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <div v-if="orderData.remark" class="info-section">
        <h3 class="section-title">订单备注</h3>
        <div class="remark-content">{{ orderData.remark }}</div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button v-if="!isUser && orderData.status === 2" type="primary" @click="handleShip">发货</el-button>
        <el-button v-if="orderData.status === 1 || (!isUser && orderData.status === 2)" type="danger" @click="handleCancel">
          取消订单
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, type OrderDetailResponse, userOrderApi } from '@/api/order'
import { hasRole } from '@/utils/auth'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const props = defineProps<{
  orderId: number | null
}>()

const emit = defineEmits<{
  close: []
}>()

const visible = ref(true)
const loading = ref(false)
const orderData = ref<Partial<OrderDetailResponse>>({})
const isUser = hasRole('ROLE_USER') && !hasRole('ROLE_ADMIN') && !hasRole('ROLE_MERCHANT')

const getStatusTagType = (status?: number): TagType => {
  const types: Record<number, TagType> = { 1: 'warning', 2: 'info', 3: 'primary', 4: 'success', 5: 'danger' }
  return status ? types[status] || 'info' : 'info'
}

const getStatusText = (status?: number) => {
  const texts: Record<number, string> = { 1: '待付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }
  return status ? texts[status] || '未知状态' : '未知状态'
}

const getPaymentMethodText = (method?: number) => {
  const methods: Record<number, string> = { 1: '支付宝', 2: '微信支付', 3: '银行卡', 4: '现金' }
  return method ? methods[method] || '未知' : '未知'
}

const loadOrderDetail = async () => {
  if (!props.orderId) return
  loading.value = true
  try {
    orderData.value = isUser ? await userOrderApi.getMyOrderDetail(props.orderId) : await orderApi.getDetail(props.orderId)
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const handleShip = async () => {
  if (!props.orderId) return
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
    await orderApi.ship(props.orderId, '默认物流', value)
    ElMessage.success('发货成功')
    loadOrderDetail()
  } catch (error) {
    console.error('发货失败:', error)
  }
}

const handleCancel = async () => {
  if (!props.orderId) return
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '取消订单确认', {
      confirmButtonText: '确定取消',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (isUser) {
      await userOrderApi.cancelMyOrder(props.orderId, '用户主动取消')
    } else {
      await orderApi.updateStatus(props.orderId, 5)
    }
    ElMessage.success('订单已取消')
    loadOrderDetail()
  } catch (error) {
    console.error('取消订单失败:', error)
  }
}

const handleClose = () => {
  visible.value = false
  emit('close')
}

watch(
  () => props.orderId,
  (newVal) => {
    if (newVal) {
      loadOrderDetail()
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.info-section {
  margin-bottom: $spacing-xl;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-light;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: $spacing-lg;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  min-width: 88px;
  color: $text-secondary;
}

.info-value {
  color: $text-primary;
  font-weight: 500;
}

.amount-list {
  max-width: 420px;
  margin-left: auto;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: $spacing-md;
}

.amount-item.total {
  margin-top: $spacing-lg;
  padding-top: $spacing-lg;
  border-top: 2px solid $border-color;
  font-weight: 600;
}

.text-success {
  color: $success-color;
}

.remark-content {
  background-color: $table-header-bg;
  border-radius: $border-radius;
  padding: $spacing-lg;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
}
</style>
