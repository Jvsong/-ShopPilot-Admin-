<template>
  <div class="dashboard-container">
    <section class="hero-panel">
      <div class="hero-copy">
        <p class="hero-eyebrow">AI Operating Console</p>
        <h1 class="page-title">智能运营仪表盘</h1>
        <p class="page-subtitle">
          把今日订单、热销商品、补货风险和异常提醒聚合到一个工作台里。
        </p>
      </div>
      <div class="hero-actions">
        <el-select v-model="selectedRange" class="range-select" size="large">
          <el-option label="最近 7 天" value="7d" />
          <el-option label="最近 15 天" value="15d" />
          <el-option label="最近 30 天" value="30d" />
        </el-select>
        <el-button class="hero-button" type="primary" size="large" :loading="dashboardLoading" @click="refreshDashboard">
          刷新建议
        </el-button>
        <el-button class="hero-button secondary" size="large" @click="openAssistant('DAILY_SUMMARY')">
          打开智能问讯
        </el-button>
      </div>
    </section>

    <section class="stats-grid">
      <article v-for="item in statsCards" :key="item.key" class="stat-card">
        <div class="stat-card__icon" :style="{ background: item.bgColor, color: item.color }">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ item.value }}</div>
          <div class="stat-card__label">{{ item.label }}</div>
        </div>
      </article>
    </section>

    <section class="content-grid">
      <article class="summary-card card-surface">
        <div class="section-header">
          <div>
            <p class="section-eyebrow">Today Brief</p>
            <h2>今日运营摘要</h2>
          </div>
          <el-tag type="info" effect="plain">{{ rangeLabel }}</el-tag>
        </div>

        <div v-loading="summaryLoading" class="summary-body">
          <div class="summary-main">
            <p class="summary-text">{{ dailySummary.summary || '暂无摘要数据' }}</p>
            <div class="summary-metrics">
              <div v-for="item in summaryMetricItems" :key="item.label" class="summary-metric">
                <span class="summary-metric__label">{{ item.label }}</span>
                <strong class="summary-metric__value">{{ item.value }}</strong>
              </div>
            </div>
          </div>

          <div class="quick-actions">
            <p class="quick-actions__title">快捷提问</p>
            <div class="quick-actions__list">
              <button
                v-for="item in quickQuestions"
                :key="item.type"
                type="button"
                class="quick-chip"
                @click="openAssistant(item.type)"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.label }}</span>
              </button>
            </div>
          </div>
        </div>
      </article>

      <article class="assistant-teaser card-surface">
        <div class="section-header">
          <div>
            <p class="section-eyebrow">Live Signals</p>
            <h2>重点建议</h2>
          </div>
          <el-button type="primary" link @click="openAssistant('DAILY_SUMMARY')">查看全部</el-button>
        </div>
        <div v-loading="summaryLoading" class="signal-list">
          <div
            v-for="item in highlightedSuggestions"
            :key="item.title"
            class="signal-item"
            :class="`signal-item--${item.level.toLowerCase()}`"
          >
            <div class="signal-item__head">
              <span class="signal-badge">{{ levelLabelMap[item.level] }}</span>
              <strong>{{ item.title }}</strong>
            </div>
            <p>{{ item.detail }}</p>
            <button type="button" class="signal-link" @click="handleSuggestionAction(item)">
              {{ item.action || '查看详情' }}
            </button>
          </div>
        </div>
      </article>
    </section>

    <section class="lower-grid">
      <article class="card-surface">
        <div class="section-header">
          <div>
            <p class="section-eyebrow">Hot Products</p>
            <h2>热销商品排行</h2>
          </div>
          <el-button type="primary" link @click="openAssistant('HOT_PRODUCTS')">查看 AI 解读</el-button>
        </div>
        <el-table v-loading="dashboardLoading" :data="hotProducts" class="dashboard-table">
          <el-table-column label="排名" width="88">
            <template #default="{ $index }">
              <span class="rank-badge">{{ $index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" min-width="200" />
          <el-table-column prop="salesQuantity" label="销量" width="120" />
          <el-table-column label="销售额" width="160">
            <template #default="{ row }">
              {{ formatCurrency(row.salesAmount) }}
            </template>
          </el-table-column>
        </el-table>
      </article>

      <article class="card-surface">
        <div class="section-header">
          <div>
            <p class="section-eyebrow">Latest Orders</p>
            <h2>最近订单</h2>
          </div>
          <el-button type="primary" link @click="router.push('/admin/orders')">查看全部</el-button>
        </div>
        <el-table v-loading="dashboardLoading" :data="recentOrders" class="dashboard-table">
          <el-table-column prop="orderNo" label="订单号" min-width="180" />
          <el-table-column prop="customerName" label="客户" width="120">
            <template #default="{ row }">
              {{ row.customerName || row.username || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="140">
            <template #default="{ row }">
              {{ formatCurrency(row.actualAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTypeMap[row.status] || 'info'">
                {{ statusLabelMap[row.status] || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="180" />
        </el-table>
      </article>
    </section>

    <el-drawer
      v-model="assistantVisible"
      class="assistant-drawer"
      size="520px"
      :with-header="false"
      destroy-on-close
    >
      <div class="assistant-panel">
        <div class="assistant-panel__header">
          <div>
            <p class="section-eyebrow">AI Assistant</p>
            <h2>智能问讯窗口</h2>
            <p class="assistant-panel__desc">{{ currentQuestionLabel }}</p>
          </div>
          <el-button circle @click="assistantVisible = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <div class="assistant-toolbar">
          <el-select v-model="assistantRange" class="assistant-toolbar__select" @change="handleAssistantRangeChange">
            <el-option label="最近 7 天" value="7d" />
            <el-option label="最近 15 天" value="15d" />
            <el-option label="最近 30 天" value="30d" />
          </el-select>
          <div class="assistant-toolbar__chips">
            <button
              v-for="item in quickQuestions"
              :key="item.type"
              type="button"
              class="mini-chip"
              :class="{ 'mini-chip--active': assistantQuestionType === item.type }"
              @click="runAssistantQuery(item.type)"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <div v-loading="assistantLoading" class="assistant-result">
          <div class="assistant-summary">
            <span class="assistant-summary__tag">{{ rangeLabelMap[assistantRange] }}</span>
            <p>{{ assistantResult.summary || '请选择一个问题开始问讯。' }}</p>
          </div>

          <div class="assistant-metrics">
            <div v-for="item in assistantMetricEntries" :key="item.label" class="assistant-metric">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </div>
          </div>

          <div class="assistant-suggestions">
            <article
              v-for="item in assistantResult.suggestions || []"
              :key="item.title"
              class="assistant-suggestion"
              :class="`assistant-suggestion--${item.level.toLowerCase()}`"
            >
              <div class="assistant-suggestion__head">
                <strong>{{ item.title }}</strong>
                <span class="signal-badge">{{ levelLabelMap[item.level] }}</span>
              </div>
              <p class="assistant-suggestion__detail">{{ item.detail }}</p>
              <ul class="assistant-suggestion__evidence">
                <li v-for="evidence in item.evidence" :key="evidence">{{ evidence }}</li>
              </ul>
              <button type="button" class="signal-link" @click="handleSuggestionAction(item)">
                {{ item.action || '查看详情' }}
              </button>
            </article>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Close,
  Goods,
  Histogram,
  MagicStick,
  Money,
  Promotion,
  ShoppingCart,
  Star,
  Van,
  Warning
} from '@element-plus/icons-vue'
import { aiAssistantApi, type AiAssistantResponse, type AiQuestionType, orderApi, type Order, type OrderStatistics } from '@/api'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

type ProductStatistic = NonNullable<OrderStatistics['topProducts']>[number]

const router = useRouter()

const selectedRange = ref<'7d' | '15d' | '30d'>('7d')
const assistantRange = ref<'7d' | '15d' | '30d'>('7d')
const assistantQuestionType = ref<AiQuestionType>('DAILY_SUMMARY')
const assistantVisible = ref(false)
const dashboardLoading = ref(false)
const summaryLoading = ref(false)
const assistantLoading = ref(false)

const todayStats = ref<Record<string, number>>({})
const statistics = ref<OrderStatistics>({
  totalOrders: 0,
  totalSales: 0,
  averageOrderValue: 0,
  pendingPaymentCount: 0,
  pendingShipmentCount: 0,
  shippedCount: 0,
  completedCount: 0,
  cancelledCount: 0,
  topProducts: []
})
const recentOrders = ref<Order[]>([])
const dailySummary = ref<AiAssistantResponse>({
  questionType: 'DAILY_SUMMARY',
  startDate: '',
  endDate: '',
  summary: '',
  suggestions: [],
  metrics: {}
})
const assistantResult = ref<AiAssistantResponse>({
  questionType: 'DAILY_SUMMARY',
  startDate: '',
  endDate: '',
  summary: '',
  suggestions: [],
  metrics: {}
})

const quickQuestions: Array<{ label: string; type: AiQuestionType; icon: any }> = [
  { label: '今日运营建议', type: 'DAILY_SUMMARY', icon: MagicStick },
  { label: '热销商品', type: 'HOT_PRODUCTS', icon: Star },
  { label: '补货建议', type: 'RESTOCK_SUGGESTION', icon: Promotion },
  { label: '异常提醒', type: 'ORDER_ALERT', icon: Warning }
]

const rangeLabelMap: Record<'7d' | '15d' | '30d', string> = {
  '7d': '最近 7 天',
  '15d': '最近 15 天',
  '30d': '最近 30 天'
}

const statusLabelMap: Record<number, string> = {
  1: '待付款',
  2: '待发货',
  3: '已发货',
  4: '已完成',
  5: '已取消'
}

const statusTypeMap: Record<number, TagType> = {
  1: 'warning',
  2: 'info',
  3: 'primary',
  4: 'success',
  5: 'danger'
}

const levelLabelMap: Record<string, string> = {
  HIGH: '高优先级',
  MEDIUM: '中优先级',
  LOW: '低优先级'
}

const statsCards = computed(() => [
  {
    key: 'today-orders',
    label: '今日订单',
    value: formatNumber(todayStats.value.totalOrders || 0),
    icon: ShoppingCart,
    color: '#165dff',
    bgColor: 'rgba(22, 93, 255, 0.12)'
  },
  {
    key: 'today-sales',
    label: '今日销售额',
    value: formatCurrency(todayStats.value.totalSales || 0),
    icon: Money,
    color: '#00b42a',
    bgColor: 'rgba(0, 180, 42, 0.12)'
  },
  {
    key: 'pending-shipment',
    label: '待发货订单',
    value: formatNumber(todayStats.value.pendingShipment || 0),
    icon: Van,
    color: '#ff7d00',
    bgColor: 'rgba(255, 125, 0, 0.12)'
  },
  {
    key: 'completed-orders',
    label: '已完成订单',
    value: formatNumber(statistics.value.completedCount || 0),
    icon: Histogram,
    color: '#722ed1',
    bgColor: 'rgba(114, 46, 209, 0.12)'
  }
])

const hotProducts = computed<ProductStatistic[]>(() => statistics.value.topProducts || [])
const rangeLabel = computed(() => rangeLabelMap[selectedRange.value])
const currentQuestionLabel = computed(() => quickQuestions.find((item) => item.type === assistantQuestionType.value)?.label || '智能问讯')
const highlightedSuggestions = computed(() => (dailySummary.value.suggestions || []).slice(0, 3))

const summaryMetricItems = computed(() => [
  { label: '订单总数', value: formatNumber(Number(dailySummary.value.metrics?.totalOrders || 0)) },
  { label: '销售额', value: formatCurrency(Number(dailySummary.value.metrics?.totalSales || 0)) },
  { label: '高风险补货', value: formatNumber(Number(dailySummary.value.metrics?.highRiskRestockCount || 0)) }
])

const assistantMetricEntries = computed(() =>
  Object.entries(assistantResult.value.metrics || {}).slice(0, 6).map(([key, value]) => ({
    label: formatMetricLabel(key),
    value: typeof value === 'number'
      ? key.toLowerCase().includes('sales') || key.toLowerCase().includes('amount')
        ? formatCurrency(value)
        : formatNumber(value)
      : String(value)
  }))
)

const fetchDashboardData = async () => {
  dashboardLoading.value = true
  try {
    const { startDate, endDate } = resolveDateRange(selectedRange.value)
    const [today, stats, orders] = await Promise.all([
      orderApi.getTodayStatistics(),
      orderApi.getStatistics(startDate, endDate),
      orderApi.getList({ page: 1, size: 5 })
    ])

    todayStats.value = today || {}
    statistics.value = stats
    recentOrders.value = orders.list || []
  } finally {
    dashboardLoading.value = false
  }
}

const fetchDailySummary = async () => {
  summaryLoading.value = true
  try {
    dailySummary.value = await aiAssistantApi.ask({
      questionType: 'DAILY_SUMMARY',
      rangeType: selectedRange.value
    })
  } finally {
    summaryLoading.value = false
  }
}

const refreshDashboard = async () => {
  await Promise.all([fetchDashboardData(), fetchDailySummary()])
}

const openAssistant = async (questionType: AiQuestionType) => {
  assistantVisible.value = true
  assistantRange.value = selectedRange.value
  await runAssistantQuery(questionType)
}

const runAssistantQuery = async (questionType: AiQuestionType) => {
  assistantQuestionType.value = questionType
  assistantLoading.value = true
  try {
    assistantResult.value = await aiAssistantApi.ask({
      questionType,
      rangeType: assistantRange.value
    })
  } finally {
    assistantLoading.value = false
  }
}

const handleAssistantRangeChange = async () => {
  await runAssistantQuery(assistantQuestionType.value)
}

const handleSuggestionAction = (item: AiAssistantResponse['suggestions'][number]) => {
  if (!item.actionTarget) {
    return
  }
  router.push(item.actionTarget)
}

const resolveDateRange = (range: '7d' | '15d' | '30d') => {
  const endDate = new Date()
  const days = range === '30d' ? 30 : range === '15d' ? 15 : 7
  const startDate = new Date(endDate)
  startDate.setDate(endDate.getDate() - (days - 1))

  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate)
  }
}

const formatDate = (date: Date) => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatNumber = (value: number) => new Intl.NumberFormat('zh-CN').format(Number.isFinite(value) ? value : 0)

const formatCurrency = (value: number | string | undefined) => {
  const amount = Number(value || 0)
  return `¥${new Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(amount)}`
}

const formatMetricLabel = (key: string) => {
  const map: Record<string, string> = {
    totalOrders: '订单总数',
    totalSales: '销售额',
    completedCount: '已完成订单',
    highRiskRestockCount: '高风险补货',
    mediumRiskRestockCount: '中风险补货',
    pendingPaymentCount: '待付款订单',
    pendingShipmentCount: '待发货订单',
    cancelledCount: '已取消订单',
    cancelledRate: '取消率',
    analysisDays: '分析天数',
    highRiskCount: '高风险商品',
    mediumRiskCount: '中风险商品',
    lowRiskCount: '低风险商品',
    topProductCount: '热销商品数'
  }

  if (map[key]) {
    return map[key]
  }

  return key.replace(/([A-Z])/g, ' $1').replace(/^./, (text) => text.toUpperCase())
}

onMounted(async () => {
  try {
    await refreshDashboard()
  } catch (error) {
    ElMessage.error('仪表盘数据加载失败，请稍后重试')
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.dashboard-container {
  padding: $spacing-lg;
  background:
    radial-gradient(circle at top left, rgba($primary-color, 0.08), transparent 32%),
    linear-gradient(180deg, #f7fbff 0%, #f3f6fb 100%);
  min-height: 100%;
}

.hero-panel,
.card-surface {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba($primary-color, 0.08);
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(22, 93, 255, 0.08);
  backdrop-filter: blur(16px);
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  gap: $spacing-xl;
  padding: 28px;
  margin-bottom: $spacing-xl;
}

.hero-copy {
  max-width: 720px;
}

.hero-eyebrow,
.section-eyebrow {
  margin: 0 0 10px;
  color: $primary-color;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.page-title {
  margin: 0;
  font-size: 38px;
  line-height: 1.1;
  color: $text-primary;
}

.page-subtitle {
  margin: 14px 0 0;
  color: $text-secondary;
  font-size: 17px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 14px;
  min-width: 220px;
}

.range-select {
  width: 100%;
}

.hero-button.secondary {
  margin-left: 0;
}

.stats-grid,
.content-grid,
.lower-grid {
  display: grid;
  gap: $spacing-xl;
  margin-bottom: $spacing-xl;
}

.stats-grid {
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.content-grid {
  grid-template-columns: minmax(0, 1.65fr) minmax(320px, 0.95fr);
}

.lower-grid {
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
}

.stat-card {
  padding: 22px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(255, 255, 255, 0.75);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.05);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-card__icon {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.stat-card__value {
  font-size: 30px;
  font-weight: 700;
  color: $text-primary;
}

.stat-card__label {
  margin-top: 4px;
  color: $text-secondary;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 24px 24px 0;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: $text-primary;
}

.summary-card {
  padding-bottom: 24px;
}

.summary-body {
  padding: 18px 24px 0;
}

.summary-main {
  padding: 22px;
  border-radius: 20px;
  background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 52%, #60a5fa 100%);
  color: $text-white;
}

.summary-text {
  margin: 0;
  font-size: 18px;
  line-height: 1.8;
}

.summary-metrics {
  margin-top: 20px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.summary-metric {
  padding: 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
}

.summary-metric__label {
  display: block;
  font-size: 12px;
  opacity: 0.82;
}

.summary-metric__value {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  font-weight: 700;
}

.quick-actions {
  margin-top: 20px;
}

.quick-actions__title {
  margin: 0 0 12px;
  font-size: 14px;
  color: $text-secondary;
}

.quick-actions__list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-chip,
.mini-chip {
  border: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.quick-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 999px;
  background: #eef4ff;
  color: #12356d;
  box-shadow: inset 0 0 0 1px rgba($primary-color, 0.08);
}

.quick-chip:hover,
.mini-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(22, 93, 255, 0.14);
}

.assistant-teaser {
  padding-bottom: 24px;
}

.signal-list {
  padding: 18px 24px 0;
  display: grid;
  gap: 14px;
}

.signal-item,
.assistant-suggestion {
  border-radius: 20px;
  padding: 18px;
  border: 1px solid $border-light;
  background: #fff;
}

.signal-item--high,
.assistant-suggestion--high {
  background: linear-gradient(180deg, rgba(245, 63, 63, 0.06), rgba(255, 255, 255, 0.98));
  border-color: rgba($danger-color, 0.18);
}

.signal-item--medium,
.assistant-suggestion--medium {
  background: linear-gradient(180deg, rgba(255, 125, 0, 0.08), rgba(255, 255, 255, 0.98));
  border-color: rgba($warning-color, 0.22);
}

.signal-item--low,
.assistant-suggestion--low {
  background: linear-gradient(180deg, rgba(0, 180, 42, 0.06), rgba(255, 255, 255, 0.98));
  border-color: rgba($success-color, 0.16);
}

.signal-item__head,
.assistant-suggestion__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.signal-item p,
.assistant-suggestion__detail {
  margin: 12px 0 0;
  color: $text-secondary;
  line-height: 1.7;
}

.signal-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: $primary-color;
  background: rgba($primary-color, 0.1);
}

.signal-link {
  margin-top: 14px;
  padding: 0;
  border: none;
  background: none;
  color: $primary-color;
  cursor: pointer;
  font-weight: 600;
}

.dashboard-table {
  padding: 16px 24px 24px;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba($primary-color, 0.12);
  color: $primary-color;
  font-weight: 700;
}

:deep(.assistant-drawer) {
  background: linear-gradient(180deg, #f8fbff 0%, #eef4fb 100%);
}

.assistant-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.assistant-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.assistant-panel__header h2 {
  margin: 0;
  font-size: 28px;
}

.assistant-panel__desc {
  margin: 10px 0 0;
  color: $text-secondary;
}

.assistant-toolbar {
  display: grid;
  gap: 14px;
}

.assistant-toolbar__select {
  width: 180px;
}

.assistant-toolbar__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.mini-chip {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  color: $text-secondary;
}

.mini-chip--active {
  background: $primary-color;
  color: $text-white;
}

.assistant-result {
  margin-top: 20px;
  overflow-y: auto;
  padding-right: 4px;
}

.assistant-summary {
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba($primary-color, 0.12), rgba($info-color, 0.08));
}

.assistant-summary__tag {
  display: inline-flex;
  margin-bottom: 10px;
  color: $primary-color;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.assistant-summary p {
  margin: 0;
  color: $text-primary;
  line-height: 1.8;
}

.assistant-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 18px 0;
}

.assistant-metric {
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba($primary-color, 0.08);
}

.assistant-metric span {
  display: block;
  color: $text-secondary;
  font-size: 13px;
}

.assistant-metric strong {
  display: block;
  margin-top: 8px;
  color: $text-primary;
  font-size: 20px;
}

.assistant-suggestions {
  display: grid;
  gap: 14px;
}

.assistant-suggestion__evidence {
  margin: 14px 0 0;
  padding-left: 18px;
  color: $text-secondary;
}

.assistant-suggestion__evidence li + li {
  margin-top: 6px;
}

@media (max-width: 1200px) {
  .content-grid,
  .lower-grid {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    flex-direction: column;
  }

  .hero-actions {
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: $spacing-md;
  }

  .page-title {
    font-size: 30px;
  }

  .summary-metrics,
  .assistant-metrics {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
