<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { User, Goods, List, Money, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const stats = ref({
  totalUsers: 0, todayUsers: 0,
  totalProducts: 0, onSaleProducts: 0,
  totalOrders: 0, todayOrders: 0, pendingOrders: 0,
  totalAmount: 0, todayAmount: 0,
  trend: [],
})

const chartRef = ref(null)
let chartInstance = null

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/dashboard/stats')
    stats.value = res.data || stats.value
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  const trend = stats.value.trend || []
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'] },
    grid: { left: 50, right: 50, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      data: trend.map(t => dayjs(t.date).format('MM-DD')),
    },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '销售额' },
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: trend.map(t => t.orders),
        itemStyle: { color: '#3DA67E' },
        barMaxWidth: 30,
      },
      {
        name: '销售额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: trend.map(t => Number(t.amount)),
        itemStyle: { color: '#E6A23C' },
        areaStyle: { opacity: 0.1 },
      },
    ],
  })
}

const handleResize = () => chartInstance?.resize()

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<template>
  <div class="admin-dashboard" v-loading="loading">
    <el-row :gutter="16">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#E8F5E9;color:#3DA67E">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">总用户数</div>
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-sub">今日 +{{ stats.todayUsers }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#E3F2FD;color:#409EFF">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">商品总数</div>
            <div class="stat-value">{{ stats.totalProducts }}</div>
            <div class="stat-sub">在售 {{ stats.onSaleProducts }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#FFF3E0;color:#E6A23C">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">总订单数</div>
            <div class="stat-value">{{ stats.totalOrders }}</div>
            <div class="stat-sub">今日 +{{ stats.todayOrders }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#FCE4EC;color:#F56C6C">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">总销售额</div>
            <div class="stat-value">¥{{ Number(stats.totalAmount).toFixed(2) }}</div>
            <div class="stat-sub">今日 ¥{{ Number(stats.todayAmount).toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">
      <el-col :span="24">
        <el-card shadow="never">
          <div class="pending-row">
            <div class="pending-item">
              <el-icon class="pending-icon" style="color:#E6A23C"><Clock /></el-icon>
              <div>
                <div class="pending-label">待发货订单</div>
                <div class="pending-value">{{ stats.pendingOrders }}</div>
              </div>
            </div>
            <el-button type="primary" text @click="$router.push('/admin/orders')">
              去处理 →
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt-16">
      <template #header>
        <div class="card-header">
          <span>最近 7 天销售趋势</span>
        </div>
      </template>
      <div ref="chartRef" class="chart"></div>
    </el-card>
  </div>
</template>

<style scoped>
.admin-dashboard { min-height: 200px; }

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}
.stat-icon {
  width: 56px; height: 56px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; flex-shrink: 0;
}
.stat-info { flex: 1; min-width: 0; }
.stat-label { font-size: 13px; color: #909399; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; line-height: 1.3; }
.stat-sub { font-size: 12px; color: #909399; }

.mt-16 { margin-top: 16px; }

.pending-row {
  display: flex; align-items: center; justify-content: space-between;
}
.pending-item { display: flex; align-items: center; gap: 12px; }
.pending-icon { font-size: 28px; }
.pending-label { font-size: 13px; color: #909399; }
.pending-value { font-size: 20px; font-weight: 600; color: #303133; }

.card-header { font-size: 15px; font-weight: 600; color: #303133; }

.chart { height: 340px; width: 100%; }
</style>