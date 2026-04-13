import { http } from '@/utils/request'

// 订单状态枚举
export enum OrderStatus {
  PENDING_PAYMENT = 1,    // 待付款
  PENDING_SHIPMENT = 2,   // 待发货
  SHIPPED = 3,            // 已发货
  COMPLETED = 4,          // 已完成
  CANCELLED = 5           // 已取消
}

// 支付方式枚举
export enum PaymentMethod {
  ALIPAY = 1,      // 支付宝
  WECHAT = 2,      // 微信支付
  BANK_CARD = 3,   // 银行卡
  CASH = 4         // 现金
}

// 订单相关类型定义
export interface Order {
  id: number
  orderNo: string
  userId: number
  username?: string
  totalAmount: number
  discountAmount: number
  shippingFee: number
  actualAmount: number
  status: number
  paymentMethod?: number
  paymentTime?: string
  shippingTime?: string
  receiveTime?: string
  cancelTime?: string
  cancelReason?: string
  shippingAddress?: string
  shippingCompany?: string
  trackingNumber?: string
  remark?: string
  createTime: string
  updateTime?: string
  isNew?: boolean
  customerName?: string
  customerPhone?: string
  items?: OrderItem[]
}

export interface OrderItem {
  id?: number
  orderId?: number
  productId: number
  productName: string
  productImage?: string
  price: number
  quantity: number
  totalPrice: number
}

export interface OrderDetailResponse extends Order {
  items: OrderItem[]
}

export interface OrderQueryParams {
  orderNo?: string
  userId?: number
  username?: string
  status?: number
  paymentMethod?: number
  minAmount?: number
  maxAmount?: number
  startDate?: string
  endDate?: string
  sortBy?: string
  sortDirection?: string
  includeDeleted?: boolean
  page?: number
  size?: number
}

export interface OrderCreateRequest {
  userId: number
  items: Array<{
    productId: number
    quantity: number
    price?: number
    productName?: string
    productImage?: string
  }>
  shippingAddress?: string
  paymentMethod?: number
  discountAmount?: number
  shippingFee?: number
  remark?: string
}

export interface OrderUpdateRequest {
  shippingAddress?: string
  paymentMethod?: number
  discountAmount?: number
  shippingFee?: number
  remark?: string
  shippingCompany?: string
  trackingNumber?: string
}

export interface OrderStatistics {
  totalOrders: number
  totalSales: number
  averageOrderValue: number
  pendingPaymentCount: number
  pendingShipmentCount: number
  shippedCount: number
  completedCount: number
  cancelledCount: number
  dailyStatistics?: Array<{
    date: string
    orderCount: number
    salesAmount: number
  }>
  statusDistribution?: Record<number, number>
  paymentMethodDistribution?: Record<number, number>
  topProducts?: Array<{
    productId: number
    productName: string
    salesQuantity: number
    salesAmount: number
  }>
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  pages: number
}

// 订单API
export const orderApi = {
  /**
   * 获取订单列表
   */
  getList(params: OrderQueryParams) {
    return http.get<PageResponse<Order>>('/admin/orders', params)
  },

  /**
   * 获取订单详情
   */
  getDetail(id: number) {
    return http.get<OrderDetailResponse>(`/admin/orders/${id}`)
  },

  /**
   * 创建订单（后台手动创建）
   */
  create(data: OrderCreateRequest) {
    return http.post<number>('/admin/orders', data)
  },

  /**
   * 更新订单信息
   */
  update(id: number, data: OrderUpdateRequest) {
    return http.put(`/admin/orders/${id}`, data)
  },

  /**
   * 删除订单（软删除）
   */
  delete(id: number) {
    return http.delete(`/admin/orders/${id}`)
  },

  /**
   * 更新订单状态
   */
  updateStatus(id: number, status: number, remark?: string) {
    return http.patch(`/admin/orders/${id}/status`, null, {
      params: { status, remark }
    })
  },

  /**
   * 批量更新订单状态
   */
  batchUpdateStatus(ids: number[], status: number, remark?: string) {
    return http.post('/admin/orders/batch-status', { ids, status, remark })
  },

  /**
   * 订单发货
   */
  ship(id: number, shippingCompany: string, trackingNumber: string) {
    return http.post(`/admin/orders/${id}/ship`, null, {
      params: { shippingCompany, trackingNumber }
    })
  },

  /**
   * 订单完成（确认收货）
   */
  complete(id: number) {
    return http.post(`/admin/orders/${id}/complete`)
  },

  /**
   * 订单取消
   */
  cancel(id: number, cancelReason?: string) {
    return http.post(`/admin/orders/${id}/cancel`, null, {
      params: { cancelReason }
    })
  },

  /**
   * 订单退款
   */
  refund(id: number, refundAmount: number, refundReason: string) {
    return http.post(`/admin/orders/${id}/refund`, null, {
      params: { refundAmount, refundReason }
    })
  },

  /**
   * 获取订单统计信息
   */
  getStatistics(startDate: string, endDate: string) {
    return http.get<OrderStatistics>('/admin/orders/statistics', {
      startDate,
      endDate
    })
  },

  /**
   * 获取今日订单统计
   */
  getTodayStatistics() {
    return http.get('/admin/orders/today-statistics')
  },

  /**
   * 导出订单数据
   */
  export(params: OrderQueryParams) {
    return http.get<Order[]>('/admin/orders/export', params)
  },

  /**
   * 获取用户订单列表
   */
  getUserOrders(userId: number, page = 1, size = 10) {
    return http.get<PageResponse<Order>>(`/admin/orders/user/${userId}`, {
      page,
      size
    })
  },

  /**
   * 计算订单金额
   */
  calculateAmount(id: number) {
    return http.get<number>(`/admin/orders/${id}/amount`)
  },

  /**
   * 检查订单归属
   */
  checkOwnership(id: number, userId: number) {
    return http.get<boolean>(`/admin/orders/${id}/ownership/${userId}`)
  },

  /**
   * 生成订单号
   */
  generateOrderNo() {
    return http.get<string>('/admin/orders/generate-order-no')
  }
}

// 前台商城订单相关API（用户端）
export const userOrderApi = {
  /**
   * 获取当前用户的订单列表
   */
  getMyOrders(page = 1, size = 10) {
    return http.get<PageResponse<Order>>('/orders/my', { page, size })
  },

  /**
   * 创建订单（前台用户下单）
   */
  createOrder(data: OrderCreateRequest) {
    return http.post<number>('/orders', data)
  },

  /**
   * 获取订单详情（用户端）
   */
  getMyOrderDetail(id: number) {
    return http.get<OrderDetailResponse>(`/orders/${id}`)
  },

  /**
   * 取消订单（用户端）
   */
  cancelMyOrder(id: number, cancelReason?: string) {
    return http.post(`/orders/${id}/cancel`, undefined, {
      params: { cancelReason }
    })
  },

  /**
   * 确认收货
   */
  confirmReceipt(id: number) {
    return http.post(`/orders/${id}/confirm-receipt`)
  }
}

export default orderApi
