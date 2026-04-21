export type OrderStatusCode =
  | 'pending_payment'
  | 'paid'
  | 'pending_shipment'
  | 'shipped'
  | 'delivered'
  | 'completed'
  | 'cancelled'
  | 'refunded'

export interface CompatOrderItem {
  id: number
  productName: string
  quantity: number
  price: number
  subtotal: number
}

export interface CompatOrder {
  id: number
  orderNo: string
  userId: number
  username: string
  customerName: string
  customerPhone: string
  email: string
  shippingAddress: string
  actualAmount: number
  status: OrderStatusCode
  paymentMethod: string
  paymentStatus: string
  createTime: string
  shipmentTime?: string
  deliveryTime?: string
  completionTime?: string
  trackingNumber?: string
  courier?: string
  notes: string[]
  items: CompatOrderItem[]
}

interface OrderQuery {
  keyword?: string
  username?: string
  status?: OrderStatusCode
  startDate?: string
  endDate?: string
  sortBy?: 'createTime' | 'actualAmount'
  sortDirection?: 'asc' | 'desc'
}

const STORAGE_KEY = 'shop-system-order-compat'

export const ORDER_STATUS_LABELS: Record<OrderStatusCode, string> = {
  pending_payment: '待付款',
  paid: '已付款',
  pending_shipment: '待发货',
  shipped: '已发货',
  delivered: '已送达',
  completed: '已完成',
  cancelled: '已取消',
  refunded: '已退款'
}

const DEFAULT_ORDERS: CompatOrder[] = [
  {
    id: 1,
    orderNo: 'ORDER-20240404-001',
    userId: 1001,
    username: 'customer1',
    customerName: 'customer1',
    customerPhone: '13800138011',
    email: 'customer1@example.com',
    shippingAddress: '上海市浦东新区测试路 1 号',
    actualAmount: 2999.99,
    status: 'pending_payment',
    paymentMethod: '支付宝',
    paymentStatus: '待支付',
    createTime: '2024-04-04 10:30:00',
    notes: [],
    items: [
      {
        id: 1,
        productName: '智能手机',
        quantity: 1,
        price: 2999.99,
        subtotal: 2999.99
      }
    ]
  },
  {
    id: 2,
    orderNo: 'ORDER-20240404-002',
    userId: 1002,
    username: 'customer2',
    customerName: 'customer2',
    customerPhone: '13800138012',
    email: 'customer2@example.com',
    shippingAddress: '杭州市西湖区测试路 2 号',
    actualAmount: 89.99,
    status: 'pending_shipment',
    paymentMethod: '微信支付',
    paymentStatus: '已支付',
    createTime: '2024-04-04 11:15:00',
    notes: [],
    items: [
      {
        id: 2,
        productName: '男士T恤',
        quantity: 1,
        price: 89.99,
        subtotal: 89.99
      }
    ]
  },
  {
    id: 3,
    orderNo: 'ORDER-20240403-001',
    userId: 1003,
    username: 'customer3',
    customerName: 'customer3',
    customerPhone: '13800138013',
    email: 'customer3@example.com',
    shippingAddress: '深圳市南山区测试路 3 号',
    actualAmount: 5999.99,
    status: 'shipped',
    paymentMethod: '银行转账',
    paymentStatus: '已支付',
    createTime: '2024-04-03 14:20:00',
    shipmentTime: '2024-04-03 16:30:00',
    trackingNumber: 'SF1234567890',
    courier: '顺丰速运',
    notes: [],
    items: [
      {
        id: 3,
        productName: '笔记本电脑',
        quantity: 1,
        price: 5999.99,
        subtotal: 5999.99
      }
    ]
  },
  {
    id: 4,
    orderNo: 'ORDER-20240401-001',
    userId: 1001,
    username: 'customer1',
    customerName: 'customer1',
    customerPhone: '13800138011',
    email: 'customer1@example.com',
    shippingAddress: '上海市浦东新区测试路 1 号',
    actualAmount: 3089.98,
    status: 'completed',
    paymentMethod: '支付宝',
    paymentStatus: '已支付',
    createTime: '2024-04-01 09:45:00',
    shipmentTime: '2024-04-01 14:30:00',
    deliveryTime: '2024-04-02 10:15:00',
    completionTime: '2024-04-03 08:00:00',
    notes: [],
    items: [
      {
        id: 4,
        productName: '智能手机',
        quantity: 1,
        price: 2999.99,
        subtotal: 2999.99
      },
      {
        id: 5,
        productName: '男士T恤',
        quantity: 1,
        price: 89.99,
        subtotal: 89.99
      }
    ]
  }
]

const cloneDefaultOrders = () =>
  DEFAULT_ORDERS.map((order) => ({
    ...order,
    notes: [...order.notes],
    items: order.items.map((item) => ({ ...item }))
  }))

const persistOrders = (orders: CompatOrder[]) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(orders))
}

export const getCompatOrders = () => {
  const cached = localStorage.getItem(STORAGE_KEY)
  if (!cached) {
    const seeded = cloneDefaultOrders()
    persistOrders(seeded)
    return seeded
  }

  try {
    const parsed = JSON.parse(cached) as CompatOrder[]
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : cloneDefaultOrders()
  } catch {
    const seeded = cloneDefaultOrders()
    persistOrders(seeded)
    return seeded
  }
}

export const getCompatOrder = (id: number) => getCompatOrders().find((item) => item.id === id) || null

export const getCompatOrderByNo = (orderNo: string) => getCompatOrders().find((item) => item.orderNo === orderNo) || null

const normalize = (value?: string | null) => (value || '').trim().toLowerCase()

const compareOrders = (
  left: CompatOrder,
  right: CompatOrder,
  sortBy: 'createTime' | 'actualAmount',
  sortDirection: 'asc' | 'desc'
) => {
  const factor = sortDirection === 'asc' ? 1 : -1

  if (sortBy === 'actualAmount') {
    return (left.actualAmount - right.actualAmount) * factor
  }

  return ((Date.parse(left.createTime) || 0) - (Date.parse(right.createTime) || 0)) * factor
}

export const queryCompatOrders = ({
  keyword,
  username,
  status,
  startDate,
  endDate,
  sortBy = 'createTime',
  sortDirection = 'desc'
}: OrderQuery) => {
  let orders = getCompatOrders()

  if (keyword) {
    const target = normalize(keyword)
    orders = orders.filter((item) => {
      const productNames = item.items.map((product) => product.productName).join(' ')
      return [item.orderNo, productNames].some((field) => normalize(field).includes(target))
    })
  }

  if (username) {
    const target = normalize(username)
    orders = orders.filter((item) => normalize(item.username).includes(target))
  }

  if (status) {
    orders = orders.filter((item) => item.status === status)
  }

  if (startDate) {
    orders = orders.filter((item) => item.createTime.slice(0, 10) >= startDate)
  }

  if (endDate) {
    orders = orders.filter((item) => item.createTime.slice(0, 10) <= endDate)
  }

  return [...orders].sort((left, right) => compareOrders(left, right, sortBy, sortDirection))
}

export const updateCompatOrder = (id: number, payload: Partial<CompatOrder>) => {
  const orders = getCompatOrders()
  const nextOrders = orders.map((item) => (item.id === id ? { ...item, ...payload } : item))
  persistOrders(nextOrders)
  return nextOrders.find((item) => item.id === id) || null
}

export const setCompatOrderStatus = (id: number, status: OrderStatusCode) => {
  const nextPaymentStatus =
    status === 'cancelled' ? '已关闭' : status === 'shipped' || status === 'completed' ? '已支付' : undefined

  return updateCompatOrder(id, {
    status,
    paymentStatus: nextPaymentStatus
  })
}

export const addCompatOrderNote = (id: number, note: string) => {
  const order = getCompatOrder(id)
  if (!order) {
    return null
  }

  return updateCompatOrder(id, {
    notes: [...order.notes, note]
  })
}

export const shipCompatOrder = (id: number, courier: string, trackingNumber: string) =>
  updateCompatOrder(id, {
    status: 'shipped',
    courier,
    trackingNumber,
    shipmentTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    paymentStatus: '已支付'
  })
