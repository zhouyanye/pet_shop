<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Wallet } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const items = ref([])
const loading = ref(false)
const paying = ref(false)
const payMethod = ref(1)

const timeLeft = ref(0)   // 剩余秒数
let timer = null

// 拉订单详情
const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await request.get(`/order/${route.params.id}`)
    order.value = res.data.order
    items.value = res.data.items

    // 如果已支付 → 跳详情
    if (order.value.status !== 0) {
      ElMessage.info('该订单已支付或已取消')
      router.replace(`/user/order/${order.value.id}`)
      return
    }

    // 计算剩余时间
    const created = new Date(order.value.createdAt).getTime()
    const passed = Math.floor((Date.now() - created) / 1000)
    timeLeft.value = Math.max(0, 600 - passed)   // 10 分钟 = 600 秒

    if (timeLeft.value > 0) startCountdown()
    else handleTimeout()
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const startCountdown = () => {
  stopCountdown()
  timer = setInterval(() => {
    timeLeft.value--
    if (timeLeft.value <= 0) {
      handleTimeout()
    }
  }, 1000)
}

const stopCountdown = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const handleTimeout = async () => {
  stopCountdown()
  try {
    await request.put(`/order/${order.value.id}/cancel`)
  } catch (e) {}
  ElMessage.warning('订单已超时，请重新下单')
  router.replace('/user/orders')
}

// 格式化时间 mm:ss
const formatTime = (s) => {
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
}

// 支付
const handlePay = async () => {
  paying.value = true
  try {
    const res = await request.post('/payment/pay', {
      orderId: order.value.id,
      payMethod: payMethod.value,
    })
    ElMessage.success('支付成功')
    stopCountdown()
    router.replace(`/user/order/${order.value.id}`)
  } catch (e) {
  } finally {
    paying.value = false
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    await request.put(`/order/${order.value.id}/cancel`)
    ElMessage.success('订单已取消')
    router.replace('/user/orders')
  } catch (e) {
  }
}

onMounted(fetchOrder)
onUnmounted(stopCountdown)
</script>

<template>
  <div v-loading="loading" class="order-pay">
    <template v-if="order">
      <!-- 倒计时提示 -->
      <div class="countdown-bar">
        <el-icon><Wallet /></el-icon>
        <span>订单提交成功，请在</span>
        <span class="time">{{ formatTime(timeLeft) }}</span>
        <span>内完成支付，超时订单自动取消</span>
      </div>

      <!-- 订单信息 -->
      <div class="card">
        <h3 class="card-title">订单信息</h3>
        <div class="info-row">
          <span class="label">订单号</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">下单时间</span>
          <span class="value">{{ $dayjs.utc(order.createdAt).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }}
          </span>
        </div>
      </div>

      <!-- 商品清单 -->
      <div class="card">
        <h3 class="card-title">商品清单</h3>
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

      <!-- 支付方式 -->
      <div class="card">
        <h3 class="card-title">支付方式</h3>
        <el-radio-group v-model="payMethod" class="pay-methods">
          <el-radio :value="1" border>微信支付</el-radio>
          <el-radio :value="2" border>支付宝</el-radio>
          <el-radio :value="3" border>货到付款</el-radio>
        </el-radio-group>
      </div>

      <!-- 金额 -->
      <div class="card">
        <div class="amount-row">
          <span>商品金额</span>
          <span>¥{{ order.productAmount }}</span>
        </div>
        <div class="amount-row">
          <span>运费</span>
          <span>{{ order.shippingFee > 0 ? '¥' + order.shippingFee : '免运费' }}</span>
        </div>
        <div v-if="order.discountAmount > 0" class="amount-row">
          <span>优惠</span>
          <span class="discount">-¥{{ order.discountAmount }}</span>
        </div>
        <div class="amount-row total-row">
          <span>应付金额</span>
          <span class="total">¥{{ order.totalAmount }}</span>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="pay-bar">
        <el-button size="large" @click="handleCancel">取消订单</el-button>
        <el-button
            type="primary"
            size="large"
            :loading="paying"
            @click="handlePay"
        >确认支付 ¥{{ order.totalAmount }}</el-button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.order-pay {
  max-width: 900px;
  margin: 24px auto 100px;
  padding: 0 16px;
}

.countdown-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #FFF6F0, #FFEDE0);
  color: #D9774E;
  border-radius: 10px;
  padding: 14px 20px;
  margin-bottom: 16px;
  font-size: 14px;
}
.countdown-bar .time {
  font-weight: 800;
  font-size: 18px;
  font-family: monospace;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
  padding: 6px 0;
}
.info-row .label {
  color: #999;
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
  width: 80px;
  text-align: center;
}
.item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #3DA67E;
  width: 90px;
  text-align: right;
}

.pay-methods {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.pay-methods :deep(.el-radio) {
  margin-right: 0;
  height: 44px;
  padding: 0 20px;
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

.pay-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
.pay-bar :deep(.el-button--primary) {
  background: #3DA67E;
  border-color: #3DA67E;
  padding: 12px 32px;
}
</style>