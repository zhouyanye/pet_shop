<script>
export default {
  name: "ProductDetail"
}
</script>
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import dayjs from "dayjs";

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const product = ref(null)
const loading = ref(false)

const specs = ref([])
const activeSpec = ref(null)

const images = ref([])
const activeImage = ref('')

const quantity = ref(1)

const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentPageSize = ref(5)
const commentLoading = ref(false)
const ratingStats = ref({ avg: 0, good: 0, total: 0 })

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/products/${route.params.id}`)
    product.value = res.data

    if (product.value.images) {
      try {
        const arr = typeof product.value.images === 'string'
            ? JSON.parse(product.value.images)
            : product.value.images
        images.value = Array.isArray(arr) ? arr : []
      } catch (e) {
        images.value = []
      }
    }
    activeImage.value = product.value.coverImage || images.value[0] || ''

    if (product.value.specs) {
      try {
        const arr = typeof product.value.specs === 'string'
            ? JSON.parse(product.value.specs)
            : product.value.specs
        specs.value = Array.isArray(arr) ? arr : []
        if (specs.value.length > 0) activeSpec.value = specs.value[0]
      } catch (e) {
        specs.value = []
      }
    }
  } catch (e) {
    ElMessage.error('商品加载失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  commentLoading.value = true
  try {
    const res = await request.get(`/products/${route.params.id}/comments`, {
      params: { pageNum: commentPage.value, pageSize: commentPageSize.value },
    })
    const records = res.data.records || []
    records.forEach(c => {
      if (c.images) {
        try {
          c.imageList = typeof c.images === 'string' ? JSON.parse(c.images) : c.images
        } catch (e) {
          c.imageList = []
        }
      } else {
        c.imageList = []
      }
    })
    comments.value = records
    commentTotal.value = res.data.total || 0

    if (records.length > 0) {
      const total = records.length
      const good = records.filter(c => c.rating >= 4).length
      const avg = records.reduce((s, c) => s + c.rating, 0) / total
      ratingStats.value = {
        total: commentTotal.value,
        good: Math.round((good / total) * 100),
        avg: avg.toFixed(1),
      }
    }
  } catch (e) {
  } finally {
    commentLoading.value = false
  }
}

const handleCommentPageChange = (p) => {
  commentPage.value = p
  fetchComments()
}

// 加入购物车
const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (product.value.stock <= 0) {
    ElMessage.warning('商品暂时缺货')
    return
  }
  if (specs.value.length > 0 && !activeSpec.value) {
    ElMessage.warning('请选择规格')
    return
  }
  try {
    await request.post('/cart', {
      productId: product.value.id,
      spec: activeSpec.value || null,
      quantity: quantity.value,
    })
    ElMessage.success('已加入购物车')
    cartStore.fetchCount()
  } catch (e) {
  }
}

// 立即购买
const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (product.value.stock <= 0) {
    ElMessage.warning('商品暂时缺货')
    return
  }
  if (specs.value.length > 0 && !activeSpec.value) {
    ElMessage.warning('请选择规格')
    return
  }
  try {
    await request.post('/cart', {
      productId: product.value.id,
      spec: activeSpec.value || null,
      quantity: quantity.value,
    })
    cartStore.fetchCount()
    router.push('/cart')
  } catch (e) {}
}

const switchImage = (img) => {
  activeImage.value = img
}

watch(() => route.params.id, (id) => {
  if (!id) return
  commentPage.value = 1
  quantity.value = 1
  activeSpec.value = null
  fetchDetail()
  fetchComments()
})

onMounted(() => {
  fetchDetail()
  fetchComments()
})
</script>
<template>
  <div v-loading="loading" class="detail-page">
    <template v-if="product">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <span @click="router.push('/')">首页</span>
        <span class="sep">/</span>
        <span @click="router.push('/category/' + product.categoryId)">商品</span>
        <span class="sep">/</span>
        <span class="current">{{ product.name }}</span>
      </div>

      <!-- 主区：图 + 信息 -->
      <div class="detail-main">
        <!-- 左侧图 -->
        <div class="detail-images">
          <div class="main-image">
            <img :src="activeImage" :alt="product.name" />
          </div>
          <div v-if="images.length > 1" class="thumb-list">
            <div
                v-for="(img, i) in images"
                :key="i"
                :class="['thumb-item', { active: img === activeImage }]"
                @click="switchImage(img)"
            >
              <img :src="img" :alt="`图${i + 1}`" />
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="detail-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p v-if="product.subtitle" class="product-subtitle">{{ product.subtitle }}</p>

          <!-- 价格块 -->
          <div class="price-block">
            <div class="price-row">
              <span class="price-now">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="price-old">
                ¥{{ product.originalPrice }}
              </span>
            </div>
            <div class="sales-row">
              <span>销量 {{ product.sales }}</span>
              <span class="divider">|</span>
              <span>库存 {{ product.stock }}</span>
              <span v-if="product.brand" class="divider">|</span>
              <span v-if="product.brand">品牌 {{ product.brand }}</span>
            </div>
          </div>

          <!-- 规格 -->
          <div v-if="specs.length > 0" class="spec-block">
            <label class="spec-label">规格</label>
            <div class="spec-options">
              <span
                  v-for="(s, i) in specs"
                  :key="i"
                  :class="['spec-item', { active: activeSpec === s }]"
                  @click="activeSpec = s"
              >{{ s }}</span>
            </div>
          </div>

          <!-- 数量 -->
          <div class="qty-block">
            <label class="spec-label">数量</label>
            <el-input-number
                v-model="quantity"
                :min="1"
                :max="product.stock || 1"
                :disabled="product.stock <= 0"
            />
          </div>

          <!-- 操作按钮 -->
          <div class="action-btns">
            <el-button
                type="primary"
                size="large"
                :icon="ShoppingCart"
                :disabled="product.stock <= 0"
                @click="addToCart"
            >加入购物车</el-button>
            <el-button
                size="large"
                :disabled="product.stock <= 0"
                @click="buyNow"
            >立即购买</el-button>
          </div>

          <p v-if="product.stock <= 0" class="out-of-stock">该商品暂时缺货</p>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="detail-block">
        <h3 class="block-title">商品详情</h3>
        <div class="block-content" v-html="product.description || '<p>暂无详细描述</p>'"></div>
      </div>

      <!-- 用户评价 -->
      <div class="detail-block">
        <h3 class="block-title">
          用户评价
          <span class="comment-summary" v-if="commentTotal > 0">
            好评率 {{ ratingStats.good }}% · 平均 {{ ratingStats.avg }} 分 · 共 {{ commentTotal }} 条
          </span>
        </h3>

        <div v-loading="commentLoading" class="comment-list">
          <div v-if="comments.length === 0 && !commentLoading" class="comment-empty">
            暂无评价，快来做第一个评价的人吧~
          </div>

          <div v-for="c in comments" :key="c.id" class="comment-item">
            <div class="comment-head">
              <el-avatar :size="36" :src="c.avatar || ''">
                {{ (c.nickname || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="comment-user">
                <span class="comment-nickname">{{ c.nickname || '匿名用户' }}</span>
                <el-rate
                    :model-value="c.rating"
                    disabled
                    size="small"
                    :colors="['#FF9900', '#FF9900', '#FF9900']"
                />
              </div>
              <span class="comment-time">{{  $dayjs.utc(c.createdAt  ).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss')}}</span>
            </div>
            <p class="comment-content">{{ c.content }}</p>
            <div v-if="c.imageList && c.imageList.length" class="comment-images">
              <img
                  v-for="(img, i) in c.imageList"
                  :key="i"
                  :src="img"
                  alt=""
              />
            </div>
          </div>

          <!-- 评价分页 -->
          <div v-if="commentTotal > commentPageSize" class="comment-pagination">
            <el-pagination
                v-model:current-page="commentPage"
                :page-size="commentPageSize"
                :total="commentTotal"
                layout="prev, pager, next"
                small
                background
                @current-change="handleCommentPageChange"
            />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px 60px;
  background: #F5F7F6;
  min-height: 60vh;
}

.breadcrumb {
  font-size: 13px;
  color: #888;
  margin-bottom: 16px;
}
.breadcrumb span {
  cursor: pointer;
}
.breadcrumb span:hover {
  color: #3DA67E;
}
.breadcrumb .sep {
  margin: 0 6px;
  color: #ccc;
  cursor: default;
}
.breadcrumb .current {
  color: #333;
  cursor: default;
}
.breadcrumb .current:hover {
  color: #333;
}

/* 主区 */
.detail-main {
  display: flex;
  gap: 32px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}

.detail-images {
  width: 420px;
  flex-shrink: 0;
}
.main-image {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #FAFAFA;
}
.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}
.thumb-item {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}
.thumb-item.active {
  border-color: #3DA67E;
}
.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  flex: 1;
  min-width: 0;
}
.product-name {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 8px;
  line-height: 1.4;
}
.product-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0 0 20px;
}

.price-block {
  background: linear-gradient(135deg, #F0F9F4, #E8F5EE);
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.price-now {
  font-size: 32px;
  font-weight: 800;
  color: #3DA67E;
}
.price-old {
  font-size: 16px;
  color: #BBB;
  text-decoration: line-through;
}
.sales-row {
  margin-top: 6px;
  font-size: 13px;
  color: #888;
}
.sales-row .divider {
  margin: 0 8px;
  color: #ddd;
}

.spec-block, .qty-block {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}
.spec-label {
  width: 50px;
  font-size: 14px;
  color: #888;
  padding-top: 6px;
  flex-shrink: 0;
}
.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.spec-item {
  padding: 6px 16px;
  border: 1px solid #DCDFE6;
  border-radius: 6px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}
.spec-item:hover {
  border-color: #3DA67E;
  color: #3DA67E;
}
.spec-item.active {
  border-color: #3DA67E;
  background: #3DA67E;
  color: #fff;
}

.action-btns {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}
.action-btns :deep(.el-button--primary) {
  background: #3DA67E;
  border-color: #3DA67E;
}

.out-of-stock {
  color: #D9774E;
  font-size: 14px;
  margin-top: 12px;
}

/* 通用区块 */
.detail-block {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}
.block-title {
  font-size: 18px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 16px;
  padding-left: 12px;
  position: relative;
}
.block-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: #3DA67E;
  border-radius: 2px;
}
.block-content {
  font-size: 14px;
  line-height: 1.8;
  color: #444;
}
.block-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

/* 评价 */
.comment-summary {
  font-size: 13px;
  font-weight: 400;
  color: #3DA67E;
  margin-left: 12px;
}
.comment-list {
  min-height: 80px;
}
.comment-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
}
.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #F0F0F0;
}
.comment-item:last-child {
  border-bottom: none;
}
.comment-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-nickname {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.comment-time {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}
.comment-content {
  font-size: 14px;
  color: #444;
  line-height: 1.6;
  margin: 0;
}
.comment-images {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.comment-images img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
}
.comment-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .detail-main {
    flex-direction: column;
    padding: 16px;
  }
  .detail-images {
    width: 100%;
  }
  .price-now {
    font-size: 26px;
  }
  .action-btns {
    flex-direction: column;
  }
  .action-btns :deep(.el-button) {
    width: 100%;
  }
}
</style>