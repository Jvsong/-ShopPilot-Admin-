<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">仪表盘</h1>
      <p class="page-subtitle">系统概览与关键业务指标</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-card__icon" style="background-color: rgba(22, 93, 255, 0.1)">
          <el-icon style="color: var(--primary-color)"><ShoppingCart /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">1,245</div>
          <div class="stat-card__label">今日订单</div>
        </div>
        <div class="stat-card__trend trend-up">
          <el-icon><Top /></el-icon>
          <span>12%</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon" style="background-color: rgba(0, 180, 42, 0.1)">
          <el-icon style="color: var(--success-color)"><Goods /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">3,567</div>
          <div class="stat-card__label">商品总数</div>
        </div>
        <div class="stat-card__trend trend-up">
          <el-icon><Top /></el-icon>
          <span>5%</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon" style="background-color: rgba(255, 125, 0, 0.1)">
          <el-icon style="color: var(--warning-color)"><UserFilled /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">8,912</div>
          <div class="stat-card__label">注册用户</div>
        </div>
        <div class="stat-card__trend trend-down">
          <el-icon><Bottom /></el-icon>
          <span>2%</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-card__icon" style="background-color: rgba(53, 145, 250, 0.1)">
          <el-icon style="color: var(--info-color)"><Money /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">￥245,680</div>
          <div class="stat-card__label">今日销售额</div>
        </div>
        <div class="stat-card__trend trend-up">
          <el-icon><Top /></el-icon>
          <span>18%</span>
        </div>
      </div>
    </div>

    <div class="charts-section">
      <div class="chart-card">
        <div class="card-header">
          <div class="card-title">订单趋势</div>
          <el-select v-model="chartDateRange" size="small" style="width: 120px">
            <el-option label="最近7天" value="7days" />
            <el-option label="最近30天" value="30days" />
            <el-option label="最近90天" value="90days" />
          </el-select>
        </div>
        <div class="card-body">
          <div class="chart-placeholder">
            <el-icon><DataAnalysis /></el-icon>
            <p>订单趋势图区域</p>
          </div>
        </div>
      </div>

      <div class="table-card">
        <div class="card-header">
          <div class="card-title">热销商品排行</div>
        </div>
        <div class="card-body">
          <el-table :data="hotProducts" style="width: 100%">
            <el-table-column prop="rank" label="排名" width="80">
              <template #default="{ row }">
                <span class="rank-badge" :class="`rank-${row.rank}`">{{ row.rank }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="sales" label="销量" width="100" />
            <el-table-column prop="growth" label="增长" width="100">
              <template #default="{ row }">
                <span :class="row.growth >= 0 ? 'trend-up' : 'trend-down'">
                  {{ row.growth >= 0 ? '+' : '' }}{{ row.growth }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <div class="recent-orders">
      <div class="card">
        <div class="card-header">
          <div class="card-title">最近订单</div>
          <div class="card-actions">
            <el-button type="primary" link @click="viewAllOrders">查看全部</el-button>
          </div>
        </div>
        <div class="card-body">
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="customer" label="客户" width="120" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">￥{{ row.amount.toLocaleString() }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="180" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewOrderDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Bottom, DataAnalysis, Goods, Money, ShoppingCart, Top, UserFilled } from '@element-plus/icons-vue'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const router = useRouter()
const chartDateRange = ref('7days')

const hotProducts = ref([
  { rank: 1, name: 'iPhone 15 Pro Max', category: '手机', sales: 12560, growth: 15 },
  { rank: 2, name: 'MacBook Pro 14', category: '电脑', sales: 8920, growth: 8 },
  { rank: 3, name: 'PlayStation 5', category: '游戏', sales: 7560, growth: 22 },
  { rank: 4, name: '戴森 V12', category: '家电', sales: 6320, growth: 5 },
  { rank: 5, name: 'Nike Air Max 270', category: '鞋类', sales: 5120, growth: -3 }
])

const recentOrders = ref([
  { orderNo: 'EC202604040001', customer: '张三', amount: 12999, status: 3, createTime: '2026-04-04 10:30:22' },
  { orderNo: 'EC202604040002', customer: '李四', amount: 8999, status: 2, createTime: '2026-04-04 09:15:45' },
  { orderNo: 'EC202604040003', customer: '王五', amount: 2599, status: 1, createTime: '2026-04-04 08:45:12' },
  { orderNo: 'EC202604040004', customer: '赵六', amount: 15999, status: 4, createTime: '2026-04-03 22:10:33' },
  { orderNo: 'EC202604040005', customer: '孙七', amount: 3299, status: 5, createTime: '2026-04-03 20:25:18' }
])

const getStatusTagType = (status: number): TagType => {
  const types: Record<number, TagType> = { 1: 'warning', 2: 'info', 3: 'primary', 4: 'success', 5: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 1: '待付款', 2: '待发货', 3: '已发货', 4: '已完成', 5: '已取消' }
  return texts[status] || '未知状态'
}

const viewAllOrders = () => {
  router.push('/admin/orders')
}

const viewOrderDetail = (order: unknown) => {
  console.log('查看订单详情:', order)
}
</script>

<style scoped lang="scss">
@use '@/styles/variables.scss' as *;

.dashboard-container {
  padding: $spacing-lg;
}

.page-header {
  margin-bottom: $spacing-xxl;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: $text-primary;
}

.page-subtitle {
  color: $text-secondary;
  font-size: $font-size-lg;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: $spacing-xl;
  margin-bottom: $spacing-xxl;
}

.stat-card,
.chart-card,
.table-card,
.card {
  background: $card-bg;
  border-radius: $border-radius;
  box-shadow: $box-shadow;
}

.stat-card {
  padding: $spacing-xl;
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.stat-card__icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-card__value {
  font-size: $font-size-xxl;
  font-weight: 700;
}

.stat-card__trend {
  margin-left: auto;
}

.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: $spacing-xl;
  margin-bottom: $spacing-xl;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-xl;
  border-bottom: 1px solid $border-light;
}

.card-body {
  padding: $spacing-xl;
}

.chart-placeholder {
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: $text-secondary;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba($primary-color, 0.1);
}

.trend-up {
  color: $success-color;
}

.trend-down {
  color: $danger-color;
}

@media (max-width: 1200px) {
  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style>
