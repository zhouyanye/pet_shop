<script>
export default {
  name: "OrderDetail"
}
</script>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const items = ref([])
const address = ref(null)
const loading = ref(false)


const statusText = (s) => {
  return ['待付款', '待发货', '待收货', '已完成', '已取消', '已退款'][s] || '未知'
}
const statusType = (s) => {
  return ['warning', 'primary', 'info', 'success', 'info', 'danger'][s] || 'info'
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/order/${route.params.id}`)
    order.value = res.data.order
    items.value = res.data.items

    // 拉地址
    if (order.value.addressId) {
      try {
        const addrRes = await request.get('/address')
        address.value = (addrRes.data || []).find(a => a.id === order.value.addressId)
      } catch (e) {}
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const goPay = () => {
  router.push(`/order/pay/${order.value.id}`)
}

const handleCancel = () => {
  ElMessageBox.confirm('确定取消该订单吗？', '提示', { type: 'warning' })
      .then(async () => {
        try {
          await request.put(`/order/${order.value.id}/cancel`)
          ElMessage.success('订单已取消')
          fetchDetail()
        } catch (e) {}
      })
      .catch(() => {})
}

const handleReceive = () => {
  ElMessageBox.confirm('确认已收到货吗？', '提示', { type: 'warning' })
      .then(async () => {
        try {
          await request.put(`/order/${order.value.id}/receive`)
          ElMessage.success('已确认收货')
          fetchDetail()
        } catch (e) {}
      })
      .catch(() => {})
}



onMounted(fetchDetail)
</script>

<template>
  <div v-loading="loading" class="order-detail">
    <template v-if="order">
      <!-- 状态条 -->
      <div class="status-banner">
        <span class="status-text">{{ statusText(order.status) }}</span>
        <span v-if="order.status === 0" class="status-tip">请尽快完成支付</span>
        <span v-else-if="order.status === 1" class="status-tip">商家正在备货</span>
        <span v-else-if="order.status === 2" class="status-tip">商品已发出，请注意查收</span>
        <span v-else-if="order.status === 3" class="status-tip">感谢您的购买，期待您的评价</span>
        <span v-else-if="order.status === 4" class="status-tip">订单已取消</span>
      </div>

      <!-- 收货地址 -->
      <div v-if="address" class="card">
        <div class="card-header">
          <el-icon><Location /></el-icon>
          <span>收货信息</span>
        </div>
        <div class="addr">
          <div class="addr-line1">
            <span class="addr-name">{{ address.receiver }}</span>
            <span class="addr-phone">{{ address.phone }}</span>
          </div>
          <p class="addr-line2">
            {{ address.province }} {{ address.city }}
            {{ address.district }} {{ address.detail }}
          </p>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="card">
        <div class="card-header">商品清单</div>
        <div v-for="it in items" :key="it.id" class="item">
          <img :src="it.productImage" :alt="it.productName" />
          <div class="item-info">
            <p class="item-name">{{ it.productName }}</p>
            <p class="item-spec" v-if="it.spec">规格：{{ it.spec }}</p>
          </div>
          <div class="item-price">¥{{ it.price }}</div>
          <div class="item-qty">x{{ it.quantity }}</div>
          <div class="item-subtotal">¥{{ it.subtotal }}</div>


        </div>
      </div>

      <!-- 订单信息 -->
      <div class="card">
        <div class="card-header">订单信息</div>
        <div class="info-row"><span class="label">订单号</span><span>{{ order.orderNo }}</span></div>
        <div class="info-row">
          <span class="label">下单时间</span>
          <span>{{ $dayjs.utc(order?.createdAt).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }}</span>
        </div>
        <div v-if="order.paidAt" class="info-row">
          <span class="label">支付时间</span>
          <span>{{ $dayjs.utc(order?.paidAt).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }}</span>
        </div>

        <div v-if="order.remark" class="info-row"><span class="label">备注</span><span>{{ order.remark }}</span></div>
      </div>

      <!-- 金额 -->
      <div class="card">
        <div class="amount-row"><span>商品金额</span><span>¥{{ order.productAmount }}</span></div>
        <div class="amount-row"><span>运费</span><span>{{ order.shippingFee > 0 ? '¥' + order.shippingFee : '免运费' }}</span></div>
        <div v-if="order.discountAmount > 0" class="amount-row"><span>优惠</span><span class="discount">-¥{{ order.discountAmount }}</span></div>
        <div class="amount-row total-row"><span>实付金额</span><span class="total">¥{{ order.totalAmount }}</span></div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button v-if="order.status === 0" @click="handleCancel">取消订单</el-button>
        <el-button v-if="order.status === 0" type="primary" @click="goPay">去支付</el-button>
        <el-button v-if="order.status === 2" type="primary" @click="handleReceive">确认收货</el-button>
        <el-button @click="router.push('/user/orders')">返回订单列表</el-button>
      </div>
    </template>

  </div>
</template>

<style scoped>
.order-detail {
  max-width: 900px;
  margin: 24px auto 40px;
  padding: 0 16px;
}

.status-banner {
  background: linear-gradient(135deg, #3DA67E, #2B8B68);
  color: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}
.status-text {
  font-size: 22px;
  font-weight: 700;
  display: block;
  margin-bottom: 6px;
}
.status-tip {
  font-size: 14px;
  opacity: 0.9;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.addr-line1 {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 4px;
}
.addr-name {
  font-weight: 600;
  color: #333;
}
.addr-phone {
  font-size: 13px;
  color: #666;
}
.addr-line2 {
  font-size: 13px;
  color: #888;
  margin: 0;
}

.item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #F5F5F5;
}
.item:last-child {
  border-bottom: none;
}
.item img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px;
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
.item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #3DA67E;
  width: 90px;
  text-align: right;
}


.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 6px 0;
}
.info-row .label {
  color: #999;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
  padding: 6px 0;
}
.amount-row .discount {
  color: #3DA67E;
}
.total-row {
  padding-top: 12px;
  border-top: 1px dashed #E5E5E5;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.total-row .total {
  color: #3DA67E;
  font-size: 22px;
  font-weight: 800;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
.actions :deep(.el-button--primary) {
  background: #3DA67E;
  border-color: #3DA67E;
}
</style>