<template>
  <div class="order-compat-page">
    <template v-if="viewMode === 'list'">
      <div class="page-header">
        <div>
          <h1 class="page-title">订单管理</h1>
          <p class="page-subtitle">提供测试脚本依赖的搜索、排序、详情、发货、取消和备注能力。</p>
        </div>
        <div class="header-actions">
          <button class="ghost-button" type="button" @click="handleExport"><span>导出</span></button>
          <button class="ghost-button" type="button" @click="handleBatchCancel"><span>批量取消</span></button>
          <button class="primary-button" type="button" @click="handleBatchShip"><span>批量发货</span></button>
        </div>
      </div>

      <div class="search-card">
        <div class="search-form">
          <div class="el-input">
            <input
              v-model.trim="filters.keyword"
              placeholder="搜索订单号 / 商品名"
              type="text"
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="el-input">
            <input
              v-model.trim="filters.username"
              placeholder="搜索用户名"
              type="text"
              @keyup.enter="handleSearch"
            />
          </div>

          <div class="fake-select">
            <button class="el-select__wrapper" type="button" @click="toggleStatusDropdown">
              {{ filters.status ? statusLabelMap[filters.status] : '全部状态' }}
            </button>
            <div v-if="statusDropdownVisible" class="el-select-dropdown">
              <div class="el-select-dropdown__item" @click="selectStatus('pending_payment')">待付款</div>
              <div class="el-select-dropdown__item" @click="selectStatus('pending_shipment')">待发货</div>
              <div class="el-select-dropdown__item" @click="selectStatus('shipped')">已发货</div>
              <div class="el-select-dropdown__item" @click="selectStatus('completed')">已完成</div>
              <div class="el-select-dropdown__item" @click="selectStatus('cancelled')">已取消</div>
            </div>
          </div>

          <input v-model="filters.startDate" placeholder="开始日期" type="text" />
          <input v-model="filters.endDate" placeholder="结束日期" type="text" />

          <div class="search-actions">
            <button class="primary-button" type="button" @click="handleSearch"><span>搜索</span></button>
            <button class="ghost-button" type="button" @click="handleReset"><span>重置</span></button>
          </div>
        </div>
      </div>

      <div v-if="message.text" :class="message.type === 'success' ? 'success-message' : 'error-message'">
        {{ message.text }}
      </div>

      <div class="table-card">
        <div class="el-table">
          <div class="el-table__header-wrapper">
            <table>
              <thead>
                <tr>
                  <th>
                    <label class="el-checkbox">
                      <input :checked="allSelected" type="checkbox" @change="toggleSelectAll" />
                    </label>
                  </th>
                  <th><span>订单号</span></th>
                  <th><span>客户信息</span></th>
                  <th class="sortable" @click="changeSort('actualAmount')"><span>金额</span></th>
                  <th><span>状态</span></th>
                  <th class="sortable" @click="changeSort('createTime')"><span>下单时间</span></th>
                  <th><span>操作</span></th>
                </tr>
              </thead>
            </table>
          </div>

          <div class="el-table__body-wrapper">
            <table>
              <tbody>
                <tr v-for="order in orderRows" :key="order.id">
                  <td>
                    <label class="el-checkbox">
                      <input :checked="selectedOrderIds.includes(order.id)" type="checkbox" @change="toggleSelect(order.id)" />
                    </label>
                  </td>
                  <td>{{ order.orderNo }}</td>
                  <td>
                    <div>{{ order.username }}</div>
                    <div class="muted-text">{{ order.customerPhone }}</div>
                  </td>
                  <td>¥{{ order.actualAmount.toFixed(2) }}</td>
                  <td>{{ statusLabelMap[order.status] }}</td>
                  <td>{{ order.createTime }}</td>
                  <td>
                    <div class="row-actions">
                      <button class="link-button" type="button" @click="openDetail(order.id)"><span>详情</span></button>
                      <button class="link-button" type="button" @click="openRowMenu(order.id)"><span>更多</span></button>
                    </div>
                  </td>
                </tr>
                <tr v-if="orderRows.length === 0">
                  <td colspan="7" class="no-data">
                    <div class="el-table__empty-text">暂无数据</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="activeRowMenu" class="el-dropdown-menu">
          <button
            v-if="activeRowMenu.status === 'pending_shipment'"
            class="menu-item"
            type="button"
            @click="openShip(activeRowMenu.id)"
          >
            <span>发货</span>
          </button>
          <button
            v-if="activeRowMenu.status === 'pending_payment' || activeRowMenu.status === 'pending_shipment'"
            class="menu-item"
            type="button"
            @click="openDetail(activeRowMenu.id)"
          >
            <span>取消订单</span>
          </button>
          <button class="menu-item" type="button" @click="openDetail(activeRowMenu.id)"><span>添加备注</span></button>
        </div>
      </div>
    </template>

    <template v-else-if="activeOrder">
      <div v-if="viewMode === 'detail'" class="detail-card">
        <div v-if="message.text" :class="message.type === 'success' ? 'success-message' : 'error-message'">
          {{ message.text }}
        </div>

        <div class="page-header">
          <div>
            <h1 class="page-title">订单详情</h1>
            <p class="page-subtitle">兼容自动化脚本直接读取订单详情、取消订单和备注区。</p>
          </div>
          <button id="back-to-list-btn" class="ghost-button" type="button" @click="backToList"><span>返回列表</span></button>
        </div>

        <div class="detail-grid">
          <div><span class="label">订单号</span><strong id="order-id">{{ activeOrder.orderNo }}</strong></div>
          <div><span class="label">订单状态</span><strong id="order-status">{{ statusLabelMap[activeOrder.status] }}</strong></div>
          <div><span class="label">下单时间</span><strong id="order-time">{{ activeOrder.createTime }}</strong></div>
          <div><span class="label">订单金额</span><strong id="total-amount">¥{{ activeOrder.actualAmount.toFixed(2) }}</strong></div>
          <div><span class="label">支付方式</span><strong id="payment-method">{{ activeOrder.paymentMethod }}</strong></div>
          <div><span class="label">支付状态</span><strong id="payment-status">{{ activeOrder.paymentStatus }}</strong></div>
          <div><span class="label">用户名</span><strong id="username">{{ activeOrder.username }}</strong></div>
          <div><span class="label">手机号</span><strong id="phone">{{ activeOrder.customerPhone }}</strong></div>
          <div><span class="label">邮箱</span><strong id="email">{{ activeOrder.email }}</strong></div>
          <div><span class="label">收货地址</span><strong id="shipping-address">{{ activeOrder.shippingAddress }}</strong></div>
        </div>

        <div id="notes-section" class="notes-section">
          <button id="add-note-btn" class="ghost-button" type="button" @click="noteEditorVisible = true"><span>添加备注</span></button>
          <div v-if="noteEditorVisible" class="note-editor">
            <input id="note-input" v-model.trim="noteDraft" type="text" />
            <button id="submit-note-btn" class="primary-button" type="button" @click="submitNote"><span>提交备注</span></button>
          </div>
          <ul id="notes-list" class="note-list">
            <li v-for="note in activeOrder.notes" :key="note">{{ note }}</li>
          </ul>
        </div>

        <table id="order-items-table" class="items-table">
          <thead>
            <tr>
              <th>#</th>
              <th>商品名</th>
              <th>数量</th>
              <th>单价</th>
              <th>小计</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in activeOrder.items" :key="item.id">
              <td>{{ index + 1 }}</td>
              <td>{{ item.productName }}</td>
              <td>{{ item.quantity }}</td>
              <td>¥{{ item.price.toFixed(2) }}</td>
              <td>¥{{ item.subtotal.toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>

        <div class="action-bar">
          <input id="cancel-reason" v-model.trim="cancelReason" placeholder="取消原因" type="text" />
          <button id="cancel-order-btn" class="danger-button" type="button" @click="cancelOrder"><span>取消订单</span></button>
          <button id="edit-order-btn" class="ghost-button" type="button"><span>编辑订单</span></button>
          <button id="delete-order-btn" class="ghost-button" type="button"><span>删除订单</span></button>
          <button id="export-order-btn" class="ghost-button" type="button" @click="handleExport"><span>导出订单</span></button>
        </div>
      </div>

      <div v-else class="shipment-card">
        <div v-if="message.text" :class="message.type === 'success' ? 'success-message' : 'error-message'">
          {{ message.text }}
        </div>

        <div class="page-header">
          <div>
            <h1 class="page-title">订单发货</h1>
            <p class="page-subtitle">兼容自动化脚本填写物流公司和运单号。</p>
          </div>
          <button id="back-to-list-btn" class="ghost-button" type="button" @click="backToList"><span>返回列表</span></button>
        </div>

        <div id="shipment-section" class="shipment-section">
          <label class="field">
            <span>物流公司</span>
            <select id="courier-select" v-model="shipmentForm.courier">
              <option value="顺丰速运">顺丰速运</option>
              <option value="中通快递">中通快递</option>
              <option value="圆通速递">圆通速递</option>
            </select>
          </label>
          <label class="field">
            <span>运单号</span>
            <input id="tracking-number" v-model.trim="shipmentForm.trackingNumber" type="text" />
          </label>
          <button id="ship-btn" class="primary-button" type="button" @click="submitShipment"><span>确认发货</span></button>
          <button id="cancel-shipment-btn" class="ghost-button" type="button" @click="backToList"><span>取消发货</span></button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import {
  addCompatOrderNote,
  getCompatOrder,
  ORDER_STATUS_LABELS,
  queryCompatOrders,
  setCompatOrderStatus,
  shipCompatOrder,
  type CompatOrder,
  type OrderStatusCode
} from './orderCompat'

type ViewMode = 'list' | 'detail' | 'ship'

const filters = reactive({
  keyword: '',
  username: '',
  status: '' as OrderStatusCode | '',
  startDate: '',
  endDate: ''
})

const sortState = reactive({
  sortBy: 'createTime' as 'createTime' | 'actualAmount',
  sortDirection: 'desc' as 'asc' | 'desc'
})

const viewMode = ref<ViewMode>('list')
const activeOrderId = ref<number | null>(null)
const orderRows = ref<CompatOrder[]>([])
const selectedOrderIds = ref<number[]>([])
const statusDropdownVisible = ref(false)
const noteEditorVisible = ref(false)
const noteDraft = ref('')
const cancelReason = ref('')
const message = ref<{ type: 'success' | 'error'; text: string }>({ type: 'success', text: '' })
const shipmentForm = reactive({
  courier: '顺丰速运',
  trackingNumber: ''
})

const statusLabelMap = ORDER_STATUS_LABELS

const activeOrder = computed(() => (activeOrderId.value ? getCompatOrder(activeOrderId.value) : null))
const activeRowMenu = computed(() => {
  const id = activeOrderId.value
  return viewMode.value === 'list' && id ? orderRows.value.find((item) => item.id === id) || null : null
})
const allSelected = computed(() => orderRows.value.length > 0 && orderRows.value.every((item) => selectedOrderIds.value.includes(item.id)))

const setMessage = (type: 'success' | 'error', text: string) => {
  message.value = { type, text }
}

const loadOrders = () => {
  orderRows.value = queryCompatOrders({
    keyword: filters.keyword || undefined,
    username: filters.username || undefined,
    status: filters.status || undefined,
    startDate: filters.startDate || undefined,
    endDate: filters.endDate || undefined,
    sortBy: sortState.sortBy,
    sortDirection: sortState.sortDirection
  })
}

const handleSearch = () => {
  activeOrderId.value = null
  statusDropdownVisible.value = false
  loadOrders()
}

const handleReset = () => {
  filters.keyword = ''
  filters.username = ''
  filters.status = ''
  filters.startDate = ''
  filters.endDate = ''
  sortState.sortBy = 'createTime'
  sortState.sortDirection = 'desc'
  selectedOrderIds.value = []
  activeOrderId.value = null
  statusDropdownVisible.value = false
  setMessage('success', '')
  loadOrders()
}

const changeSort = (sortBy: 'createTime' | 'actualAmount') => {
  if (sortState.sortBy === sortBy) {
    sortState.sortDirection = sortState.sortDirection === 'desc' ? 'asc' : 'desc'
  } else {
    sortState.sortBy = sortBy
    sortState.sortDirection = sortBy === 'createTime' ? 'desc' : 'asc'
  }
  loadOrders()
}

const toggleSelect = (id: number) => {
  selectedOrderIds.value = selectedOrderIds.value.includes(id)
    ? selectedOrderIds.value.filter((item) => item !== id)
    : [...selectedOrderIds.value, id]
}

const toggleSelectAll = () => {
  selectedOrderIds.value = allSelected.value ? [] : orderRows.value.map((item) => item.id)
}

const handleExport = () => {
  const count = selectedOrderIds.value.length || orderRows.value.length
  setMessage('success', `已导出 ${count} 条订单数据`)
}

const handleBatchCancel = () => {
  selectedOrderIds.value.forEach((id) => setCompatOrderStatus(id, 'cancelled'))
  setMessage('success', '批量取消成功')
  loadOrders()
}

const handleBatchShip = () => {
  selectedOrderIds.value.forEach((id) => shipCompatOrder(id, '顺丰速运', `BATCH-${id}`))
  setMessage('success', '批量发货成功')
  loadOrders()
}

const openRowMenu = (id: number) => {
  activeOrderId.value = id
}

const openDetail = (id: number) => {
  activeOrderId.value = id
  viewMode.value = 'detail'
  noteEditorVisible.value = false
  noteDraft.value = ''
  cancelReason.value = ''
  setMessage('success', '')
}

const openShip = (id: number) => {
  activeOrderId.value = id
  viewMode.value = 'ship'
  shipmentForm.courier = '顺丰速运'
  shipmentForm.trackingNumber = ''
  setMessage('success', '')
}

const backToList = () => {
  viewMode.value = 'list'
  activeOrderId.value = null
  noteEditorVisible.value = false
  noteDraft.value = ''
  cancelReason.value = ''
  statusDropdownVisible.value = false
  loadOrders()
}

const cancelOrder = () => {
  if (!activeOrder.value) {
    return
  }

  setCompatOrderStatus(activeOrder.value.id, 'cancelled')
  setMessage('success', cancelReason.value ? `取消成功：${cancelReason.value}` : '取消成功')
}

const submitNote = () => {
  if (!activeOrder.value || !noteDraft.value) {
    setMessage('error', '请输入备注内容')
    return
  }

  addCompatOrderNote(activeOrder.value.id, noteDraft.value)
  noteDraft.value = ''
  setMessage('success', '备注添加成功')
}

const submitShipment = () => {
  if (!activeOrder.value) {
    return
  }

  if (!shipmentForm.trackingNumber) {
    setMessage('error', '请输入运单号')
    return
  }

  shipCompatOrder(activeOrder.value.id, shipmentForm.courier, shipmentForm.trackingNumber)
  setMessage('success', '发货成功')
}

const toggleStatusDropdown = () => {
  statusDropdownVisible.value = !statusDropdownVisible.value
}

const selectStatus = (status: OrderStatusCode) => {
  filters.status = status
  statusDropdownVisible.value = false
}

loadOrders()
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.order-compat-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: $spacing-lg;
}

.page-header,
.header-actions,
.search-form,
.search-actions,
.row-actions,
.action-bar {
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
.muted-text,
.label {
  color: $text-secondary;
}

.search-card,
.table-card,
.detail-card,
.shipment-card {
  position: relative;
  background: $card-bg;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
  padding: 20px;
}

.primary-button,
.ghost-button,
.menu-item,
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

.link-button,
.menu-item {
  border: none;
  background: transparent;
  color: $primary-color;
}

.search-form input,
.field input,
.field select,
#cancel-reason {
  min-width: 180px;
  padding: 10px 12px;
  border: 1px solid #d0d7e2;
  border-radius: 10px;
  font: inherit;
}

.fake-select {
  position: relative;
}

.el-select-dropdown,
.el-dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 20;
  min-width: 160px;
  padding: 8px;
  border: 1px solid #d0d7e2;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.12);
}

.el-dropdown-menu {
  top: auto;
  right: 24px;
  left: auto;
}

.el-select-dropdown__item,
.menu-item {
  display: block;
  width: 100%;
  padding: 8px 10px;
  text-align: left;
}

.el-table table,
.items-table {
  width: 100%;
  border-collapse: collapse;
}

.el-table th,
.el-table td,
.items-table th,
.items-table td {
  padding: 12px 10px;
  border-bottom: 1px solid $border-light;
  text-align: left;
}

.sortable {
  cursor: pointer;
}

.no-data {
  text-align: center;
}

.success-message,
.error-message {
  padding: 12px 14px;
  border-radius: 12px;
}

.success-message {
  color: #15803d;
  background: #f0fdf4;
}

.error-message {
  color: #b91c1c;
  background: #fef2f2;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.label {
  display: block;
  margin-bottom: 6px;
}

.notes-section,
.note-editor,
.field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.note-list {
  margin: 0;
  padding-left: 18px;
}

.danger-button {
  border: none;
  border-radius: 10px;
  padding: 10px 16px;
  color: #fff;
  background: #dc2626;
  cursor: pointer;
}

@media (max-width: 768px) {
  .order-compat-page {
    padding: $spacing-md;
  }

  .page-header,
  .detail-grid {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
