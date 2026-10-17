<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Ticket } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const submitting = ref(false)

// 地址
const addresses = ref([])
const selectedAddressId = ref(null)
const addressDialogVisible = ref(false) // 控制选择地址弹窗

// 优惠券
const userCoupons = ref([])
const selectedCouponId = ref(null)
const couponDialogVisible = ref(false)

// 商品和金额
const items = ref([])
const productAmount = ref(0)
const shippingFee = ref(0)
const discountAmount = ref(0)
const totalAmount = ref(0)

const remark = ref('')

// 当前选中的地址对象
const selectedAddress = computed(() =>
    addresses.value.find(a => a.id === selectedAddressId.value)
)
const selectedCoupon = computed(() =>
    userCoupons.value.find(c => c.id === selectedCouponId.value)
)

// 拉地址
const fetchAddresses = async () => {
  try {
    const res = await request.get('/address')
    addresses.value = res.data || []
    // 优先选中默认地址，没有默认地址则自动选第一个
    const def = addresses.value.find(a => a.isDefault === 1)
    selectedAddressId.value = def ? def.id : (addresses.value[0]?.id ?? null)
  } catch (e) {
    addresses.value = []
  }
}

// 弹窗中选中地址
const handleSelectAddress = (id) => {
  selectedAddressId.value = id
  addressDialogVisible.value = false
}

// 拉用户优惠券（未使用）
const fetchCoupons = async () => {
  try {
    const res = await request.get('/user-coupon/available')
    userCoupons.value = res.data || []
  } catch (e) {
    userCoupons.value = []
  }
}

// 预览金额（选券时重算）
const previewAmount = async () => {
  loading.value = true
  try {
    const res = await request.post('/order/preview', {
      userCouponId: selectedCouponId.value,
    })
    items.value = res.data.items
    productAmount.value = res.data.productAmount
    shippingFee.value = res.data.shippingFee
    discountAmount.value = res.data.discountAmount
    totalAmount.value = res.data.totalAmount
  } catch (e) {
    // 优惠券失效 → 清掉
    if (selectedCouponId.value) {
      selectedCouponId.value = null
      previewAmount()
    }
  } finally {
    loading.value = false
  }
}

// 选券
const handleSelectCoupon = (id) => {
  selectedCouponId.value = id
  couponDialogVisible.value = false
  previewAmount()
}

const clearCoupon = () => {
  selectedCouponId.value = null
  previewAmount()
}

// 提交订单
const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  submitting.value = true
  try {
    const res = await request.post('/order/create', {
      addressId: selectedAddressId.value,
      userCouponId: selectedCouponId.value,
      remark: remark.value,
    })
    ElMessage.success('订单已创建')
    cartStore.fetchCount()
    router.push(`/order/pay/${res.data.orderId}`)
  } catch (e) {
    // 错误已统一提示
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await fetchAddresses()
  await fetchCoupons()
  await previewAmount()
})
</script>

<template>
  <div class="order-confirm">
    <h2 class="page-title">确认订单</h2>

    <!-- 收货地址卡片 -->
    <div class="card">
      <div class="card-header">
        <span class="header-title"><el-icon><Location /></el-icon> 收货地址</span>
        <el-button text type="primary" @click="router.push('/user/address')">管理地址</el-button>
      </div>

      <!-- 暂无地址 -->
      <div v-if="addresses.length === 0" class="no-address">
        <p>还没有收货地址</p>
        <el-button type="primary" @click="router.push('/user/address')">添加地址</el-button>
      </div>

      <!-- 当前选中地址卡片 (点击任意地方或提示处均可呼出弹窗更换) -->
      <div v-else-if="selectedAddress" class="selected-address-card" @click="addressDialogVisible = true">
        <div class="addr-main-info">
          <div class="addr-line1">
            <span class="addr-receiver">{{ selectedAddress.receiver }}</span>
            <span class="addr-phone">{{ selectedAddress.phone }}</span>
            <el-tag v-if="selectedAddress.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
          <p class="addr-line2">{{ selectedAddress.province }} {{ selectedAddress.city }} {{ selectedAddress.district }} {{ selectedAddress.detail }}</p>
        </div>
        <span class="change-tip">更换地址 &gt;</span>
      </div>
    </div>

    <!-- 商品清单 -->
    <div class="card">
      <div class="card-header">
        <span class="header-title">商品清单</span>
      </div>
      <div class="item-list">
        <div v-for="it in items" :key="it.cartId" class="order-item">
          <img :src="it.coverImage" :alt="it.name" />
          <div class="item-info">
            <p class="item-name">{{ it.name }}</p>
            <p class="item-spec" v-if="it.spec">规格：{{ it.spec }}</p>
          </div>
          <div class="item-price">¥{{ it.price }}</div>
          <div class="item-qty">x{{ it.quantity }}</div>
          <div class="item-subtotal">¥{{ it.subtotal }}</div>
        </div>
      </div>
    </div>

    <!-- 优惠券 -->
    <div class="card">
      <div class="card-header">
        <span class="header-title"><el-icon><Ticket /></el-icon> 优惠券</span>
      </div>
      <div class="coupon-row">
        <span v-if="selectedCoupon" class="coupon-selected">
          {{ selectedCoupon.couponName }} -¥{{ discountAmount }}
          <el-button text type="danger" size="small" @click="clearCoupon">取消使用</el-button>
        </span>
        <el-button
            v-else
            text
            type="primary"
            @click="couponDialogVisible = true"
        >选择优惠券 ({{ userCoupons.length }} 张可用)</el-button>
      </div>
    </div>

    <!-- 备注 -->
    <div class="card">
      <div class="card-header"><span class="header-title">订单备注</span></div>
      <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
          placeholder="选填，请先与客服协商一致"
      />
    </div>

    <!-- 金额明细 -->
    <div class="card amount-card">
      <div class="amount-row">
        <span>商品总额</span>
        <span>¥{{ productAmount }}</span>
      </div>
      <div class="amount-row">
        <span>运费</span>
        <span>{{ shippingFee > 0 ? '¥' + shippingFee : '免运费' }}</span>
      </div>
      <div class="amount-row" v-if="discountAmount > 0">
        <span>优惠券</span>
        <span class="discount">-¥{{ discountAmount }}</span>
      </div>
      <div class="amount-row total-row">
        <span>应付金额</span>
        <span class="total">¥{{ totalAmount }}</span>
      </div>
    </div>

    <!-- 底部提交固定栏 (合并金额与按钮，视觉更紧凑) -->
    <div class="submit-bar">
      <div class="submit-content">
        <div class="submit-left">
          <span>应付金额：</span>
          <span class="submit-total">¥{{ totalAmount }}</span>
        </div>
        <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
        >提交订单</el-button>
      </div>
    </div>

    <!-- 选择地址弹窗 -->
    <el-dialog v-model="addressDialogVisible" title="选择收货地址" width="550px">
      <div v-if="addresses.length === 0" class="no-address">暂无可用收货地址</div>
      <div v-else class="address-dialog-list">
        <div
            v-for="a in addresses"
            :key="a.id"
            :class="['address-dialog-card', { active: selectedAddressId === a.id }]"
            @click="handleSelectAddress(a.id)"
        >
          <div class="addr-line1">
            <span class="addr-receiver">{{ a.receiver }}</span>
            <span class="addr-phone">{{ a.phone }}</span>
            <el-tag v-if="a.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
          <p class="addr-line2">{{ a.province }} {{ a.city }} {{ a.district }} {{ a.detail }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 优惠券弹窗 -->
    <el-dialog v-model="couponDialogVisible" title="选择优惠券" width="500px">
      <div v-if="userCoupons.length === 0" class="no-coupon">暂无可用优惠券</div>
      <div v-else class="coupon-list">
        <div
            v-for="c in userCoupons"
            :key="c.id"
            :class="['coupon-item', { disabled: c.usable === false }]"
            @click="c.usable !== false && handleSelectCoupon(c.id)"
        >
          <div class="coupon-left">
            <div class="coupon-value">
              <template v-if="c.type === 2">
                {{ (c.discountValue * 10).toFixed(1) }}折
              </template>
              <template v-else>
                ¥{{ c.discountValue }}
              </template>
            </div>
            <div class="coupon-cond">
              {{ c.minAmount > 0 ? '满' + c.minAmount + '可用' : '无门槛' }}
            </div>
          </div>
          <div class="coupon-right">
            <p class="coupon-name">{{ c.couponName }}</p>
            <span class="coupon-time">
              {{ $dayjs.utc(c.startTime).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }}
              - {{ $dayjs.utc(c.endTime).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }}
            </span>
            <p v-if="c.usable === false" class="coupon-tip">{{ c.reason }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.order-confirm {
  max-width: 900px;
  margin: 24px auto 100px;
  padding: 0 16px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 20px;
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

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.no-address {
  text-align: center;
  padding: 20px;
  color: #999;
}

/* 当前选中的地址展示卡片 */
.selected-address-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #3DA67E;
  background: #F8FDFA;
  border-radius: 8px;
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.selected-address-card:hover {
  box-shadow: 0 2px 8px rgba(61, 166, 126, 0.15);
}
.addr-main-info {
  flex: 1;
}
.change-tip {
  font-size: 13px;
  color: #3DA67E;
  font-weight: 600;
  margin-left: 16px;
  white-space: nowrap;
}

/* 通用地址行样式 */
.addr-line1 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.addr-receiver {
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
  line-height: 1.5;
}

/* 弹窗中的地址列表 */
.address-dialog-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 4px;
}
.address-dialog-card {
  border: 1px solid #E5E5E5;
  border-radius: 8px;
  padding: 12px 14px;
  cursor: pointer;
  transition: all 0.2s;
}
.address-dialog-card:hover {
  border-color: #3DA67E;
}
.address-dialog-card.active {
  border-color: #3DA67E;
  background: #F8FDFA;
  box-shadow: 0 0 0 2px rgba(61, 166, 126, 0.1);
}

/* 商品清单 */
.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #FAFAFA;
  border-radius: 8px;
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

/* 优惠券行 */
.coupon-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.coupon-selected {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #3DA67E;
  font-weight: 600;
}

/* 金额明细 */
.amount-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.amount-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
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

/* 底部提交栏整体优化：合并紧凑 */
.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  padding: 12px 24px;
  z-index: 90;
  display: flex;
  justify-content: flex-end; /* 靠右对齐更符合习惯 */
}
.submit-content {
  display: flex;
  align-items: center;
  gap: 20px;
}
.submit-left {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-size: 14px;
  color: #666;
}
.submit-total {
  font-size: 26px;
  font-weight: 800;
  color: #3DA67E;
}

/* 优惠券弹窗 */
.no-coupon {
  text-align: center;
  padding: 40px;
  color: #999;
}
.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
}
.coupon-item {
  display: flex;
  border: 1px solid #3DA67E;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}
.coupon-item:hover {
  box-shadow: 0 4px 12px rgba(61, 166, 126, 0.15);
}
.coupon-item.disabled {
  border-color: #ccc;
  opacity: 0.5;
  cursor: not-allowed;
}
.coupon-left {
  background: red;
  color: #fff;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 110px;
}
.coupon-value {
  font-size: 22px;
  font-weight: 800;
}
.coupon-cond {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 4px;
}
.coupon-right {
  flex: 1;
  padding: 12px 16px;
}
.coupon-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px;
}
.coupon-time {
  font-size: 12px;
  color: #999;
  margin: 0;
}
.coupon-tip {
  font-size: 12px;
  color: #D9774E;
  margin: 4px 0 0;
}

@media (max-width: 600px) {
  .submit-bar {
    justify-content: center;
    padding: 12px 16px;
  }
  .submit-content {
    width: 100%;
    justify-content: space-between;
  }
  .order-item {
    flex-wrap: wrap;
  }
}
</style>