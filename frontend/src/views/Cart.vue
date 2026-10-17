<script>
export default {
  name: "Cart"
}
</script>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, ShoppingCart } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const list = ref([])
const loading = ref(false)

// ===== 拉购物车 =====
const fetchCart = async () => {
  loading.value = true
  try {
    const res = await request.get('/cart')
    list.value = (res.data || []).map(item => ({
      ...item,
      selected: item.selected === 1,   // 转成 boolean 方便 el-checkbox
    }))
    cartStore.fetchCount()
  } catch (e) {
  } finally {
    loading.value = false
  }
}

// ===== 全选状态 =====
const allSelected = computed({
  get() {
    const valid = list.value.filter(i => !i.offline)
    return valid.length > 0 && valid.every(i => i.selected)
  },
  set(val) {
    list.value.forEach(i => {
      if (!i.offline) i.selected = val
    })
    // 批量更新后端
    batchUpdateSelected()
  },
})

// 批量更新勾选状态
const batchUpdateSelected = async () => {
  const updates = list.value
      .filter(i => !i.offline)
      .map(i => request.put(`/cart/${i.id}`, { selected: i.selected ? 1 : 0 }))
  try {
    await Promise.all(updates)
  } catch (e) {
  }
}

// 单个勾选
const handleSelectOne = async (item) => {
  try {
    await request.put(`/cart/${item.id}`, { selected: item.selected ? 1 : 0 })
  } catch (e) {
    item.selected = !item.selected  // 失败回滚
  }
}

// ===== 改数量 =====
const handleQuantityChange = async (item, val) => {
  if (val <= 0) return
  try {
    await request.put(`/cart/${item.id}`, { quantity: val })
  } catch (e) {
    // 失败回滚到原值
    item.quantity = item._oldQty ?? item.quantity
  }
}

// 记录改前值（el-input-number change 前）
const handleQuantityBeforeChange = (item) => {
  item._oldQty = item.quantity
}

// ===== 删除单项 =====
const handleDelete = (item) => {
  ElMessageBox.confirm(`确定移除「${item.name}」吗？`, '提示', {
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/cart/${item.id}`)
      ElMessage.success('已移除')
      fetchCart()
    } catch (e) {
    }
  }).catch(() => {})
}

// ===== 删除选中 =====
const handleDeleteSelected = () => {
  const ids = list.value.filter(i => i.selected).map(i => i.id)
  if (ids.length === 0) {
    ElMessage.warning('请先勾选商品')
    return
  }
  ElMessageBox.confirm(`确定移除选中的 ${ids.length} 件商品吗？`, '提示', {
    type: 'warning',
  }).then(async () => {
    try {
      await Promise.all(ids.map(id => request.delete(`/cart/${id}`)))
      ElMessage.success('已移除')
      fetchCart()
    } catch (e) {
    }
  }).catch(() => {})
}

// ===== 合计 =====
const totalAmount = computed(() => {
  return list.value
      .filter(i => i.selected && !i.offline)
      .reduce((sum, i) => sum + Number(i.price) * i.quantity, 0)
      .toFixed(2)
})

const totalCount = computed(() => {
  return list.value
      .filter(i => i.selected && !i.offline)
      .reduce((sum, i) => sum + i.quantity, 0)
})

// ===== 结算 =====
const handleCheckout = () => {
  const selected = list.value.filter(i => i.selected && !i.offline)
  if (selected.length === 0) {
    ElMessage.warning('请先勾选商品')
    return
  }
  // ElMessage.success(`准备结算 ${selected.length} 件商品，金额 ¥${totalAmount.value}`)
  router.push('/order/confirm')
}

const goDetail = (id) => router.push(`/product/${id}`)
const goHome = () => router.push('/')

onMounted(fetchCart)
</script>

<template>
  <div class="cart-page">
    <h2 class="cart-title">我的购物车</h2>

    <div v-loading="loading" class="cart-body">
      <!-- 空购物车 -->
      <div v-if="!loading && list.length === 0" class="cart-empty">
        <el-empty description="购物车还是空的">
          <el-button type="primary" @click="goHome">去逛逛</el-button>
        </el-empty>
      </div>

      <!-- 有商品 -->
      <template v-else>
        <!-- 表头 -->
        <div class="cart-header">
          <el-checkbox v-model="allSelected">全选</el-checkbox>
          <span class="col-info">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-subtotal">小计</span>
          <span class="col-op">操作</span>
        </div>

        <!-- 商品行 -->
        <div
            v-for="item in list"
            :key="item.id"
            :class="['cart-item', { offline: item.offline }]"
        >
          <el-checkbox
              v-model="item.selected"
              :disabled="item.offline"
              @change="handleSelectOne(item)"
          />

          <div class="item-info" @click="goDetail(item.productId)">
            <img :src="item.coverImage" :alt="item.name" />
            <div class="item-detail">
              <h4 class="item-name">{{ item.name }}</h4>
              <p v-if="item.spec" class="item-spec">规格：{{ item.spec }}</p>
              <p v-if="item.offline" class="item-offline">
                {{ item.status === 0 ? '已下架' : '库存不足' }}
              </p>
            </div>
          </div>

          <div class="col-price">¥{{ item.price }}</div>

          <div class="col-qty">
            <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.stock"
                :disabled="item.offline"
                size="small"
                @focus="handleQuantityBeforeChange(item)"
                @change="(val) => handleQuantityChange(item, val)"
            />
          </div>

          <div class="col-subtotal">
            <span class="subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>

          <div class="col-op">
            <el-button
                text
                type="danger"
                :icon="Delete"
                @click="handleDelete(item)"
            >删除</el-button>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部结算栏 -->
    <div v-if="list.length > 0" class="cart-footer">
      <div class="footer-left">
        <el-checkbox v-model="allSelected">全选</el-checkbox>
        <el-button text type="danger" @click="handleDeleteSelected">删除选中</el-button>
      </div>
      <div class="footer-right">
        <span class="selected-count">
          已选 <b>{{ totalCount }}</b> 件
        </span>
        <span class="total-label">合计：</span>
        <span class="total-amount">¥{{ totalAmount }}</span>
        <el-button
            type="primary"
            size="large"
            :icon="ShoppingCart"
            :disabled="totalCount === 0"
            @click="handleCheckout"
        >去结算</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 80px;
  background: #F5F7F6;
  min-height: 60vh;
}

.cart-title {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 20px;
  padding-left: 12px;
  position: relative;
}
.cart-title::before {
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

.cart-body {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  min-height: 300px;
  margin-bottom: 80px;
}

.cart-empty {
  padding: 80px 0;
}

/* 表头 */
.cart-header {
  display: grid;
  grid-template-columns: 40px 1fr 100px 140px 100px 80px;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #F0F0F0;
  color: #888;
  font-size: 13px;
}
.col-info { text-align: left; }
.col-price, .col-qty, .col-subtotal, .col-op { text-align: center; }

/* 商品行 */
.cart-item {
  display: grid;
  grid-template-columns: 40px 1fr 100px 140px 100px 80px;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
  border-bottom: 1px solid #F5F5F5;
}
.cart-item:last-child {
  border-bottom: none;
}
.cart-item.offline {
  opacity: 0.6;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}
.item-info img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background: #FAFAFA;
}
.item-detail {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.item-spec {
  font-size: 12px;
  color: #999;
  margin: 0;
  background: #F5F7F6;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
}
.item-offline {
  font-size: 12px;
  color: #D9774E;
  margin: 4px 0 0;
}

.col-price {
  color: #333;
  font-size: 14px;
}
.col-subtotal .subtotal {
  color: #3DA67E;
  font-size: 16px;
  font-weight: 700;
}
.col-op :deep(.el-button) {
  padding: 4px 8px;
}

/* 底部栏 */
.cart-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  padding: 16px 0;
  z-index: 90;
}
.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 16px;
  padding-right: 16px;
}
.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.footer-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.selected-count {
  font-size: 14px;
  color: #666;
}
.selected-count b {
  color: #3DA67E;
}
.total-label {
  font-size: 14px;
  color: #666;
}
.total-amount {
  font-size: 24px;
  font-weight: 800;
  color: #3DA67E;
}
.footer-right :deep(.el-button--primary) {
  background: #3DA67E;
  border-color: #3DA67E;
}

@media (max-width: 768px) {
  .cart-header {
    display: none;
  }
  .cart-item {
    grid-template-columns: 30px 1fr;
    grid-template-rows: auto auto auto;
    gap: 8px;
  }
  .cart-item .col-price,
  .cart-item .col-qty,
  .cart-item .col-subtotal,
  .cart-item .col-op {
    grid-column: 2;
    text-align: left;
  }
  .cart-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  .footer-right {
    justify-content: space-between;
  }
  .cart-body {
    margin-bottom: 140px;
  }
}
</style>