<script>
export default {
  name: "UserOrder"
}
</script>
<script setup>
import { useCartStore } from '@/stores/cart'
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import CommentDialog from '@/components/CommentDialog.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const activeTab = ref('all')
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const loading = ref(false)

// 评价弹窗
const commentDialogVisible = ref(false)
const commentTarget = ref(null)
const commentOrderId = ref(null)

const tabs = [
  { name: 'all', label: '全部' },
  { name: '0', label: '待付款' },
  { name: '1', label: '待发货' },
  { name: '2', label: '待收货' },
  { name: '3', label: '已完成' },
]

const statusText = (s) => {
  return ['待付款', '待发货', '待收货', '已完成', '已取消', '已退款'][s] || '未知'
}
const statusType = (s) => {
  return ['warning', 'primary', 'info', 'success', 'info', 'danger'][s] || 'info'
}

const checkComments = async (rows) => {
  for (const row of rows) {
    if (row.order.status !== 3) continue
    for (const it of row.items) {
      try {
        const res = await request.get('/comment/check', {
          params: { orderId: row.order.id, productId: it.productId },
        })
        it.commented = res.data === true
      } catch (e) {
        it.commented = false
      }
    }
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeTab.value !== 'all') params.status = Number(activeTab.value)

    const res = await request.get('/order/list', { params })
    list.value = res.data.records || []
    total.value = res.data.total || 0

    await checkComments(list.value)
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pageNum.value = 1
  fetchList()
}

const handlePageChange = (p) => {
  pageNum.value = p
  fetchList()
}

const goPay = (order) => router.push(`/order/pay/${order.id}`)
const goDetail = (id) => router.push(`/user/order/${id}`)

const handleCancel = (order) => {
  ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
      .then(async () => {
        try {
          await request.put(`/order/${order.id}/cancel`)
          ElMessage.success('订单已取消')
          fetchList()
        } catch (e) {}
      })
      .catch(() => {})
}

const handleReceive = (order) => {
  ElMessageBox.confirm('确认已收到货吗？', '提示', { type: 'warning' })
      .then(async () => {
        try {
          await request.put(`/order/${order.id}/receive`)
          ElMessage.success('已确认收货')
          fetchList()
        } catch (e) {}
      })
      .catch(() => {})
}

const handleBuyAgain = async (order) => {
  const items = order.items || []
  if (items.length === 0) {
    ElMessage.warning('该订单没有商品')
    return
  }
  try {
    for (const it of items) {
      await request.post('/cart', {
        productId: it.productId,
        spec: it.spec,
        quantity: it.quantity,
      })
    }
    cartStore.fetchCount()
    ElMessage.success('已加入购物车')
    router.push('/cart')
  } catch (e) {}
}

// 打开评价弹窗
const openComment = (item, orderId) => {
  commentTarget.value = item
  commentOrderId.value = orderId
  commentDialogVisible.value = true
}

const handleCommentSuccess = () => {
  fetchList()
}

onMounted(fetchList)
watch(() => route.query.status, (s) => {
  if (s !== undefined) {
    activeTab.value = String(s)
    handleTabChange()
  }
})
</script>

<template>
  <div class="order-page">
    <h2 class="page-title">我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane
          v-for="t in tabs"
          :key="t.name"
          :label="t.label"
          :name="t.name"
      />
    </el-tabs>

    <div v-loading="loading" class="order-list">
      <el-empty v-if="!loading && list.length === 0" description="暂无订单" />

      <div
          v-for="row in list"
          :key="row.order.id"
          class="order-card"
      >
        <!-- 头部：订单号 + 状态 -->
        <div class="order-header">
          <div class="header-left">
            <span class="order-no">订单号：{{ row.order.orderNo }}</span>
            <span class="order-time">{{ $dayjs.utc(row.order.createdAt ).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss')}}</span>
          </div>
          <el-tag :type="statusType(row.order.status)" size="small">
            {{ statusText(row.order.status) }}
          </el-tag>
        </div>

        <!-- 商品列表 -->
        <div class="order-items" @click="goDetail(row.order.id)">
          <div
              v-for="it in row.items"
              :key="it.id"
              class="order-item"
          >
            <img :src="it.productImage" :alt="it.productName" />
            <div class="item-info">
              <p class="item-name">{{ it.productName }}</p>
              <p class="item-spec" v-if="it.spec">规格：{{ it.spec }}</p>
            </div>
            <div class="item-price">¥{{ it.price }}</div>
            <div class="item-qty">x{{ it.quantity }}</div>

            <!-- 商品行右侧：评价按钮 -->
            <div class="item-action" @click.stop>
              <el-button
                  v-if="row.order.status === 3 && !it.commented"
                  type="primary"
                  size="small"
                  plain
                  @click="openComment(it, row.order.id)"
              >去评价</el-button>
              <el-tag
                  v-else-if="row.order.status === 3 && it.commented"
                  type="success"
                  size="small"
              >已评价</el-tag>
            </div>
          </div>
        </div>

        <!-- 底部：金额 + 操作 -->
        <div class="order-footer">
          <div class="footer-amount">
            共 {{ row.items.reduce((s, i) => s + i.quantity, 0) }} 件商品，
            实付：<span class="total">¥{{ row.order.totalAmount }}</span>
          </div>
          <div class="footer-actions">
            <el-button
                v-if="row.order.status === 0"
                size="small"
                @click="handleCancel(row.order)"
            >取消订单</el-button>
            <el-button
                v-if="row.order.status === 0"
                type="primary"
                size="small"
                @click="goPay(row.order)"
            >去支付</el-button>
            <el-button
                v-if="row.order.status === 1"
                size="small"
                @click="handleCancel(row.order)"
            >申请取消</el-button>
            <el-button
                v-if="row.order.status === 2"
                type="primary"
                size="small"
                @click="handleReceive(row.order)"
            >确认收货</el-button>
            <el-button
                v-if="row.order.status === 3"
                size="small"
                @click="handleBuyAgain(row)"
            >再次购买</el-button>
            <el-button size="small" @click="goDetail(row.order.id)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="handlePageChange"
      />
    </div>

    <!-- 评价弹窗 -->
    <CommentDialog
        v-model="commentDialogVisible"
        :order-id="commentOrderId"
        :item="commentTarget"
        @success="handleCommentSuccess"
    />
  </div>
</template>

<style scoped>
.order-page {
  max-width: 900px;
  margin: 24px auto 40px;
  padding: 0 16px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 16px;
  padding-left: 12px;
  position: relative;
}
.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #3DA67E;
  border-radius: 2px;
}

.order-list {
  min-height: 200px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #FAFAFA;
  border-bottom: 1px solid #F0F0F0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.order-no {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}
.order-time {
  font-size: 12px;
  color: #999;
}

.order-items {
  padding: 8px 20px;
  cursor: pointer;
  transition: background 0.2s;
}
.order-items:hover {
  background: #FAFAFA;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #F5F5F5;
}
.order-item:last-child {
  border-bottom: none;
}
.order-item img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-spec {
  font-size: 12px;
  color: #999;
  margin: 0;
}
.item-price, .item-qty {
  font-size: 13px;
  color: #666;
  width: 70px;
  text-align: center;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-top: 1px solid #F0F0F0;
}
.footer-amount {
  font-size: 13px;
  color: #666;
}
.footer-amount .total {
  color: #3DA67E;
  font-size: 18px;
  font-weight: 800;
  margin-left: 4px;
}
.footer-actions {
  display: flex;
  gap: 8px;
}
.footer-actions :deep(.el-button--primary) {
  background: #3DA67E;
  border-color: #3DA67E;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
.item-action {
  width: 80px;
  text-align: right;
  flex-shrink: 0;
}
</style>