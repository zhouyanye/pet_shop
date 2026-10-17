<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const query = reactive({ status: null, keyword: '' })

// 订单状态映射
const STATUS_MAP = {
  0: { text: '待付款', type: 'info' },
  1: { text: '待发货', type: 'warning' },
  2: { text: '待收货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
  5: { text: '已退款', type: 'danger' },
}

const PAY_METHOD_MAP = {
  1: '微信',
  2: '支付宝',
  3: '货到付款',
}

const statusText = (s) => STATUS_MAP[s]?.text || '未知'
const statusType = (s) => STATUS_MAP[s]?.type || 'info'
const payText = (m) => PAY_METHOD_MAP[m] || '-'
const fmtTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/orders', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        status: query.status ?? undefined,
        keyword: query.keyword || undefined,
      },
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; fetchList() }
const handleReset = () => {
  query.status = null
  query.keyword = ''
  pageNum.value = 1
  fetchList()
}

/* ---------- 详情弹窗 ---------- */
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({ order: {}, user: {}, items: [] })

const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await request.get(`/admin/orders/${row.id}`)
    detail.value = res.data
  } finally {
    detailLoading.value = false
  }
}

/* ---------- 发货 ---------- */
const handleShip = (row) => {
  ElMessageBox.confirm(`确定对订单「${row.orderNo}」发货吗？`, '提示', { type: 'warning' })
      .then(async () => {
        await request.put(`/admin/orders/${row.id}/ship`)
        ElMessage.success('已发货')
        fetchList()
      })
      .catch(() => {})
}

/* ---------- 取消 ---------- */
const handleCancel = (row) => {
  ElMessageBox.confirm(`确定取消订单「${row.orderNo}」吗？`, '提示', { type: 'warning' })
      .then(async () => {
        await request.put(`/admin/orders/${row.id}/cancel`)
        ElMessage.success('已取消')
        fetchList()
      })
      .catch(() => {})
}

onMounted(fetchList)
</script>

<template>
  <div class="admin-order">
    <el-card shadow="never">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-input
            v-model="query.keyword"
            placeholder="订单号 / 用户名"
            :prefix-icon="Search"
            style="width: 240px"
            clearable
            @keyup.enter="handleSearch"
        />
        <el-select
            v-model="query.status"
            placeholder="订单状态"
            clearable
            style="width: 140px"
        >
          <el-option
              v-for="(v, k) in STATUS_MAP"
              :key="k"
              :label="v.text"
              :value="Number(k)"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="username" label="下单用户" width="140" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span style="color:#F56C6C;font-weight:600">
              ¥{{ Number(row.totalAmount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="100">
          <template #default="{ row }">{{ payText(row.payMethod) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ fmtTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
                v-if="row.status === 1"
                text
                type="success"
                @click="handleShip(row)"
            >发货</el-button>
            <el-button
                v-if="row.status === 0 || row.status === 1"
                text
                type="danger"
                @click="handleCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 👈 统一的分页组件 -->
      <Pagination
          v-model:page-num="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          @change="fetchList"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <div v-loading="detailLoading">
        <!-- 订单基础信息 -->
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.order.status)" size="small">
              {{ statusText(detail.order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单用户">
            {{ detail.user.nickname || detail.user.username }}
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">
            {{ payText(detail.order.payMethod) }}
          </el-descriptions-item>
          <el-descriptions-item label="商品金额">
            ¥{{ Number(detail.order.productAmount || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="运费">
            ¥{{ Number(detail.order.shippingFee || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="优惠">
            ¥{{ Number(detail.order.discountAmount || 0).toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="实付">
            <span style="color:#F56C6C;font-weight:600">
              ¥{{ Number(detail.order.totalAmount || 0).toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">
            {{ fmtTime(detail.order.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="支付时间">
            {{ fmtTime(detail.order.paidAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ detail.order.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 商品明细 -->
        <div class="items-title">商品明细</div>
        <el-table :data="detail.items" size="small" border>
          <el-table-column label="图片" width="70">
            <template #default="{ row }">
              <img
                  :src="row.productImage"
                  style="width:50px;height:50px;object-fit:cover;border-radius:4px"
              />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品" min-width="180" />
          <el-table-column prop="spec" label="规格" width="120" />
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column label="小计" width="110">
            <template #default="{ row }">
              <span style="color:#F56C6C">¥{{ Number(row.subtotal).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.items-title {
  margin: 20px 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
</style>