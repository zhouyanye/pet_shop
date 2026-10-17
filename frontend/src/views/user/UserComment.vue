<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(5)
const loading = ref(false)

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 解析图片 JSON 数组
const parseImages = (images) => {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/comment/my', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
      },
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const goProduct = (productId) => {
  if (productId) {
    router.push(`/product/${productId}`)
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="my-comment-page">
    <div class="header">
      <div class="header-left">

        <h2 class="page-title">我的评价</h2>
      </div>
    </div>

    <div v-loading="loading" class="comment-list">
      <el-empty v-if="!loading && list.length === 0" description="您还没有发表过评价" />

      <div v-for="item in list" :key="item.id" class="comment-card">
        <!-- 左侧：商品信息 -->
        <div class="product-info" @click="goProduct(item.productId)">
          <img :src="item.productImage || 'https://picsum.photos/100/100'" alt="商品图" class="product-img" />
          <div class="product-name">{{ item.productName }}</div>
        </div>

        <!-- 右侧：评价内容 -->
        <div class="comment-content">
          <div class="comment-header">
            <el-rate :model-value="item.rating" disabled show-score text-color="#ff9900" />
            <span class="time">{{ formatTime(item.createdAt) }}</span>
          </div>

          <p class="content-text">{{ item.content }}</p>

          <!-- 评价图片 -->
          <div v-if="parseImages(item.images).length > 0" class="image-list">
            <el-image
                v-for="(img, idx) in parseImages(item.images)"
                :key="idx"
                :src="img"
                :preview-src-list="parseImages(item.images)"
                :initial-index="idx"
                fit="cover"
                class="comment-img"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="fetchList"
      />
    </div>
  </div>
</template>

<style scoped>
.my-comment-page {
  max-width: 900px;
  margin: 32px auto;
  padding: 0 16px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.header-left {
  display: flex;
  align-items: center;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0;
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

.comment-list {
  min-height: 200px;
}

.comment-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  gap: 24px;
  border: 1px solid #F0F0F0;
  transition: all 0.2s;
}
.comment-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

/* 左侧商品区 */
.product-info {
  width: 120px;
  flex-shrink: 0;
  cursor: pointer;
  text-align: center;
}
.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}
.product-name {
  font-size: 13px;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.product-info:hover .product-name {
  color: #3DA67E;
}

/* 右侧评价区 */
.comment-content {
  flex: 1;
  min-width: 0;
}
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.time {
  font-size: 13px;
  color: #999;
}
.content-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0 0 12px;
  word-break: break-all;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.comment-img {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  cursor: pointer;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 600px) {
  .comment-card {
    flex-direction: column;
    gap: 12px;
  }
  .product-info {
    display: flex;
    align-items: center;
    width: 100%;
    text-align: left;
    gap: 12px;
  }
  .product-img {
    margin-bottom: 0;
    width: 60px;
    height: 60px;
  }
}
</style>