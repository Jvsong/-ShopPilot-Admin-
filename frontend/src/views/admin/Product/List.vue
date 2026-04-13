<template>
  <div class="product-management">
    <div class="page-header">
      <div>
        <h1 class="page-title">商品管理</h1>
        <p class="page-subtitle">{{ pageSubtitle }}</p>
      </div>
      <div class="header-actions">
        <button
          v-if="canManageProducts"
          id="add-product-btn"
          type="button"
          class="primary-button"
          @click="handleAdd"
        >
          添加商品
        </button>
      </div>
    </div>

    <div class="search-card">
      <div class="search-form">
        <input
          id="search-input"
          v-model.trim="filters.keyword"
          class="form-input"
          type="text"
          placeholder="搜索商品名称"
          @keyup.enter="handleSearch"
        />
        <select id="category-filter" v-model="filters.categoryId" class="form-select">
          <option value="">全部分类</option>
          <option v-for="category in categoryList" :key="category.id" :value="String(category.id)">
            {{ category.name }}
          </option>
        </select>
        <div id="price-filter" class="price-filter">
          <input id="price-min" v-model.trim="filters.minPrice" class="form-input" type="number" min="0" placeholder="最低价" />
          <span class="price-separator">-</span>
          <input id="price-max" v-model.trim="filters.maxPrice" class="form-input" type="number" min="0" placeholder="最高价" />
        </div>
        <select id="status-filter" v-model="filters.status" class="form-select">
          <option value="">全部状态</option>
          <option value="1">上架</option>
          <option value="0">下架</option>
        </select>
        <div class="search-actions">
          <button id="search-btn" type="button" class="primary-button" @click="handleSearch">搜索</button>
          <button id="filter-apply" type="button" class="secondary-button" @click="handleSearch">应用筛选</button>
          <button id="filter-reset" type="button" class="secondary-button" @click="handleReset">重置</button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <div v-if="actionMessage" :class="['action-message', actionMessage.type === 'success' ? 'success-message' : 'error-message']">
        {{ actionMessage.text }}
      </div>

      <div class="toolbar">
        <div class="sort-group">
          <button id="sort-by-name" type="button" class="ghost-button" @click="handleSort('name')">名称排序</button>
          <button id="sort-by-price" type="button" class="ghost-button" @click="handleSort('price')">价格排序</button>
          <button id="sort-by-stock" type="button" class="ghost-button" @click="handleSort('stock')">库存排序</button>
          <button id="sort-by-date" type="button" class="ghost-button" @click="handleSort('date')">时间排序</button>
        </div>
        <div v-if="canManageProducts" class="batch-group">
          <select id="batch-operation-select" v-model="batchOperation" class="form-select compact-select">
            <option value="">批量操作</option>
            <option value="上架">上架</option>
            <option value="下架">下架</option>
            <option value="删除">删除</option>
          </select>
          <button id="batch-operation-btn" type="button" class="secondary-button" @click="handleBatchOperation">执行</button>
          <button id="export-btn" type="button" class="secondary-button" @click="exportDialogVisible = true">导出</button>
        </div>
      </div>

      <div class="table-wrapper">
        <table id="product-table" class="product-table">
          <thead>
            <tr>
              <th v-if="canManageProducts">
                <input id="select-all" type="checkbox" :checked="allSelectableChecked" @change="handleToggleSelectAll" />
              </th>
              <th v-else>选择</th>
              <th>名称</th>
              <th>分类</th>
              <th>价格</th>
              <th>库存</th>
              <th>状态</th>
              <th>ID</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in pagedProducts" :key="product.id">
              <td>
                <input
                  class="product-checkbox"
                  type="checkbox"
                  :checked="selectedProductIds.includes(product.id)"
                  @change="handleToggleProduct(product.id)"
                />
              </td>
              <td>
                <div class="product-info">
                  <img :src="product.mainImage || '/default-product.png'" class="product-image" />
                  <div>
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-desc">{{ product.description || '-' }}</div>
                  </div>
                </div>
              </td>
              <td>{{ product.categoryName || '-' }}</td>
              <td>{{ formatPrice(product.price) }}</td>
              <td>{{ product.stock }}</td>
              <td>
                <span :class="['status-badge', product.status === 1 ? 'status-up' : 'status-down']">
                  {{ product.status === 1 ? '上架' : '下架' }}
                </span>
              </td>
              <td>{{ product.id }}</td>
              <td>{{ product.createTime || '-' }}</td>
              <td>
                <div class="product-actions">
                  <template v-if="canManageProducts">
                    <button type="button" class="link-button edit-btn" @click="handleEdit(product)">编辑</button>
                    <button type="button" class="link-button delete-btn danger" @click="handleDelete(product)">删除</button>
                    <button type="button" class="link-button" @click="handleToggleStatus(product)">
                      {{ product.status === 1 ? '下架' : '上架' }}
                    </button>
                  </template>
                  <template v-else>
                    <button type="button" class="link-button" @click="handleCreateOrder(product)">立即下单</button>
                  </template>
                </div>
              </td>
            </tr>
            <tr v-if="!pagedProducts.length">
              <td :colspan="canManageProducts ? 10 : 10" class="empty-cell">没有找到相关商品</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination pagination-container">
        <button class="page-prev ghost-button" type="button" :disabled="pagination.current <= 1" @click="handlePageChange(pagination.current - 1)">
          上一页
        </button>
        <button
          v-for="page in pageNumbers"
          :key="page"
          type="button"
          class="page-number ghost-button"
          :class="{ 'current-page': page === pagination.current }"
          @click="handlePageChange(page)"
        >
          {{ page }}
        </button>
        <button
          class="page-next ghost-button"
          type="button"
          :disabled="pagination.current >= totalPages"
          @click="handlePageChange(pagination.current + 1)"
        >
          下一页
        </button>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <ProductEditForm
        v-if="dialogVisible"
        :product-id="currentProductId"
        @success="handleDialogSuccess"
        @cancel="dialogVisible = false"
      />
    </el-dialog>

    <el-dialog v-model="exportDialogVisible" title="导出商品" width="420px" destroy-on-close>
      <div class="export-dialog">
        <label class="export-label" for="export-format">导出格式</label>
        <select id="export-format" v-model="exportFormat" class="form-select">
          <option value="excel">excel</option>
          <option value="csv">csv</option>
          <option value="pdf">pdf</option>
        </select>
      </div>
      <template #footer>
        <button type="button" class="secondary-button" @click="exportDialogVisible = false">取消</button>
        <button id="export-confirm" type="button" class="primary-button" @click="handleExport">确认导出</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ProductEditForm from './Edit.vue'
import { http } from '@/utils/request'
import { hasRole } from '@/utils/auth'

interface ProductItem {
  id: number
  name: string
  categoryId?: number
  categoryName?: string
  price: number
  stock: number
  sales: number
  status: number
  mainImage?: string
  description?: string
  createTime?: string
}

interface PageResponse<T> {
  list: T[]
  total: number
}

type ActionMessage = {
  type: 'success' | 'error'
  text: string
}

const categoryList = ref([
  { id: 1, name: '电子产品' },
  { id: 2, name: '服装' },
  { id: 3, name: '家居生活' },
  { id: 4, name: '图书音像' },
  { id: 5, name: '美妆个护' }
])

const filters = reactive({
  keyword: '',
  categoryId: '',
  minPrice: '',
  maxPrice: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const sortState = reactive({
  sortBy: '',
  sortDirection: 'asc'
})

const productList = ref<ProductItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const exportDialogVisible = ref(false)
const currentProductId = ref<number | null>(null)
const selectedProductIds = ref<number[]>([])
const batchOperation = ref('')
const exportFormat = ref('excel')
const actionMessage = ref<ActionMessage | null>(null)

const dialogTitle = computed(() => (currentProductId.value ? '编辑商品' : '添加商品'))
const isAdmin = computed(() => hasRole('ROLE_ADMIN'))
const isMerchant = computed(() => hasRole('ROLE_MERCHANT'))
const canManageProducts = computed(() => isAdmin.value || isMerchant.value)
const productApiPrefix = computed(() => (canManageProducts.value ? '/admin/products' : '/products'))
const pageSubtitle = computed(() => {
  if (isAdmin.value) return '管理员可维护全部商品'
  if (isMerchant.value) return '商家仅可维护自己创建的商品'
  return '普通用户仅可浏览已上架商品并直接下单'
})

const pagedProducts = computed(() => productList.value)
const totalPages = computed(() => Math.max(1, Math.ceil((pagination.total || 0) / pagination.size)))
const pageNumbers = computed(() => {
  const pages: number[] = []
  for (let i = 1; i <= totalPages.value; i += 1) {
    pages.push(i)
  }
  return pages
})
const allSelectableChecked = computed(
  () => !!pagedProducts.value.length && pagedProducts.value.every((item) => selectedProductIds.value.includes(item.id))
)

const setActionMessage = (type: ActionMessage['type'], text: string) => {
  actionMessage.value = { type, text }
}

const formatPrice = (price: number) => `￥${Number(price || 0).toFixed(2)}`

const loadProducts = async () => {
  loading.value = true
  try {
    const response = await http.get<PageResponse<ProductItem>>(productApiPrefix.value, {
      name: filters.keyword || undefined,
      categoryId: filters.categoryId ? Number(filters.categoryId) : undefined,
      minPrice: filters.minPrice ? Number(filters.minPrice) : undefined,
      maxPrice: filters.maxPrice ? Number(filters.maxPrice) : undefined,
      status: filters.status !== '' ? Number(filters.status) : undefined,
      sortBy: sortState.sortBy || undefined,
      sortDirection: sortState.sortDirection,
      page: pagination.current,
      size: pagination.size
    })
    productList.value = response.list || []
    pagination.total = response.total || 0
    selectedProductIds.value = selectedProductIds.value.filter((id) => productList.value.some((item) => item.id === id))
  } catch (error) {
    console.error('加载商品列表失败:', error)
    setActionMessage('error', '加载商品列表失败')
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadProducts()
}

const handleReset = () => {
  filters.keyword = ''
  filters.categoryId = ''
  filters.minPrice = ''
  filters.maxPrice = ''
  filters.status = ''
  sortState.sortBy = ''
  sortState.sortDirection = 'asc'
  pagination.current = 1
  selectedProductIds.value = []
  actionMessage.value = null
  loadProducts()
}

const handleSort = (field: 'name' | 'price' | 'stock' | 'date') => {
  const mapping: Record<typeof field, string> = {
    name: 'name',
    price: 'price',
    stock: 'stock',
    date: 'create_time'
  }
  if (sortState.sortBy === mapping[field]) {
    sortState.sortDirection = sortState.sortDirection === 'asc' ? 'desc' : 'asc'
  } else {
    sortState.sortBy = mapping[field]
    sortState.sortDirection = 'asc'
  }
  pagination.current = 1
  loadProducts()
}

const handleAdd = () => {
  if (!canManageProducts.value) return
  currentProductId.value = null
  dialogVisible.value = true
}

const handleEdit = (product: ProductItem) => {
  if (!canManageProducts.value) return
  currentProductId.value = product.id
  dialogVisible.value = true
}

const handleDelete = async (product: ProductItem) => {
  if (!canManageProducts.value) return
  try {
    await ElMessageBox.confirm(`确定要删除商品 "${product.name}" 吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await http.delete(`${productApiPrefix.value}/${product.id}`)
    setActionMessage('success', '商品删除成功')
    ElMessage.success('商品删除成功')
    await loadProducts()
  } catch (error) {
    console.error('删除商品失败:', error)
  }
}

const handleToggleStatus = async (product: ProductItem) => {
  if (!canManageProducts.value) return
  const newStatus = product.status === 1 ? 0 : 1
  try {
    await http.patch(`${productApiPrefix.value}/${product.id}/status`, null, { params: { status: newStatus } })
    setActionMessage('success', '商品状态更新成功')
    ElMessage.success('商品状态更新成功')
    await loadProducts()
  } catch (error) {
    console.error('更新商品状态失败:', error)
    setActionMessage('error', '更新商品状态失败')
    ElMessage.error('更新商品状态失败')
  }
}

const handleCreateOrder = async (product: ProductItem) => {
  try {
    const quantityPrompt = await ElMessageBox.prompt(`请输入“${product.name}”的购买数量`, '立即下单', {
      confirmButtonText: '下一步',
      cancelButtonText: '取消',
      inputValue: '1',
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入大于 0 的整数'
    })

    const addressPrompt = await ElMessageBox.prompt('请输入收货地址', '填写收货地址', {
      confirmButtonText: '提交订单',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入收货地址'
    })

    await http.post('/orders', {
      items: [
        {
          productId: product.id,
          quantity: Number(quantityPrompt.value)
        }
      ],
      paymentMethod: 1,
      shippingAddress: addressPrompt.value
    })

    setActionMessage('success', '下单成功')
    ElMessage.success('下单成功')
  } catch (error) {
    console.error('创建订单失败:', error)
  }
}

const handleToggleProduct = (productId: number) => {
  if (selectedProductIds.value.includes(productId)) {
    selectedProductIds.value = selectedProductIds.value.filter((id) => id !== productId)
    return
  }
  selectedProductIds.value = [...selectedProductIds.value, productId]
}

const handleToggleSelectAll = () => {
  if (allSelectableChecked.value) {
    selectedProductIds.value = []
    return
  }
  selectedProductIds.value = pagedProducts.value.map((item) => item.id)
}

const handleBatchOperation = async () => {
  if (!batchOperation.value || !selectedProductIds.value.length) {
    setActionMessage('error', '请先选择商品和批量操作')
    return
  }

  try {
    if (batchOperation.value === '删除') {
      await Promise.all(selectedProductIds.value.map((id) => http.delete(`/admin/products/${id}`)))
      setActionMessage('success', '批量删除成功')
    } else {
      const status = batchOperation.value === '上架' ? 1 : 0
      await Promise.all(selectedProductIds.value.map((id) => http.patch(`/admin/products/${id}/status`, null, { params: { status } })))
      setActionMessage('success', `批量${batchOperation.value}成功`)
    }
    ElMessage.success(actionMessage.value?.text || '批量操作成功')
    selectedProductIds.value = []
    batchOperation.value = ''
    await loadProducts()
  } catch (error) {
    console.error('批量操作失败:', error)
    setActionMessage('error', '批量操作失败')
    ElMessage.error('批量操作失败')
  }
}

const handleExport = () => {
  const payload = JSON.stringify(pagedProducts.value, null, 2)
  const blob = new Blob([payload], { type: 'application/json;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `products.${exportFormat.value === 'excel' ? 'xlsx' : exportFormat.value}`
  link.click()
  URL.revokeObjectURL(link.href)
  exportDialogVisible.value = false
  setActionMessage('success', `商品已按 ${exportFormat.value} 格式导出`)
  ElMessage.success(`商品已按 ${exportFormat.value} 格式导出`)
}

const handlePageChange = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  pagination.current = page
  loadProducts()
}

const handleDialogSuccess = () => {
  dialogVisible.value = false
  setActionMessage('success', '商品保存成功')
  loadProducts()
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.product-management {
  padding: $spacing-lg;
}

.page-header,
.search-form,
.search-actions,
.toolbar,
.sort-group,
.batch-group,
.pagination-container,
.product-actions {
  display: flex;
  align-items: center;
}

.page-header,
.toolbar,
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
.product-desc {
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
.sort-group,
.batch-group,
.product-actions {
  gap: $spacing-md;
  flex-wrap: wrap;
}

.toolbar {
  margin-bottom: $spacing-lg;
  gap: $spacing-lg;
  flex-wrap: wrap;
}

.form-input,
.form-select {
  min-height: 40px;
  padding: 0 12px;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  background: #fff;
}

.form-input {
  min-width: 180px;
}

.compact-select {
  min-width: 140px;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.price-separator {
  color: $text-secondary;
}

.primary-button,
.secondary-button,
.ghost-button,
.link-button {
  min-height: 38px;
  padding: 0 14px;
  border-radius: $border-radius;
  cursor: pointer;
  border: 1px solid transparent;
}

.primary-button {
  background: $primary-color;
  color: $text-white;
}

.secondary-button {
  background: #fff;
  color: $text-primary;
  border-color: $border-color;
}

.ghost-button {
  background: transparent;
  color: $text-primary;
  border-color: $border-color;
}

.link-button {
  background: transparent;
  color: $primary-color;
  padding: 0;
  min-height: auto;
}

.link-button.danger {
  color: $danger-color;
}

.table-wrapper {
  overflow-x: auto;
}

.product-table {
  width: 100%;
  border-collapse: collapse;
}

.product-table th,
.product-table td {
  padding: 12px 10px;
  border-bottom: 1px solid $border-light;
  text-align: left;
  vertical-align: middle;
}

.product-table thead th {
  background: $table-header-bg;
  color: $text-primary;
}

.product-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  min-width: 220px;
}

.product-image {
  width: 48px;
  height: 48px;
  border-radius: $border-radius;
  object-fit: cover;
  background-color: $table-header-bg;
}

.product-name {
  font-weight: 600;
}

.product-desc {
  font-size: $font-size-xs;
  margin-top: 4px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: $font-size-xs;
}

.status-up {
  background: rgba($success-color, 0.12);
  color: $success-color;
}

.status-down {
  background: rgba($warning-color, 0.12);
  color: $warning-color;
}

.empty-cell {
  text-align: center;
  color: $text-secondary;
}

.pagination-container {
  margin-top: $spacing-xl;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.current-page {
  background: rgba($primary-color, 0.12);
  color: $primary-color;
  border-color: rgba($primary-color, 0.2);
}

.action-message {
  margin-bottom: $spacing-md;
  padding: $spacing-md;
  border-radius: $border-radius;
}

.success-message {
  color: $success-color;
  background: rgba($success-color, 0.1);
}

.error-message {
  color: $danger-color;
  background: rgba($danger-color, 0.1);
}

.export-dialog {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.export-label {
  color: $text-secondary;
  font-size: $font-size-sm;
}
</style>
