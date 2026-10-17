<script>
export default {
  name: "Category"
}
</script>
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const categoryId = ref(route.params.id)
const categoryName = ref('')
const products = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

/** 查商品 */
const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/products', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        categoryId: categoryId.value,
        status: 1,
      },
    })
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('商品加载失败', e)
  } finally {
    loading.value = false
  }
}

/** 查分类名（用于标题） */
const fetchCategoryName = async () => {
  try {
    const res = await request.get('/categories')
    const cat = (res.data || []).find(c => String(c.id) === String(categoryId.value))
    if (cat) {
      categoryName.value = cat.name
      document.title = `${cat.name} - 萌宠优选`
    }
  } catch (e) {
    console.error('分类加载失败', e)
  }
}

const handlePageChange = (p) => {
  pageNum.value = p
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goDetail = (id) => router.push(`/product/${id}`)

// 同一个组件复用时（比如点导航切到另一个分类），参数会变，要重新查
watch(() => route.params.id, (id) => {
  if (!id) return
  categoryId.value = id
  pageNum.value = 1
  fetchCategoryName()
  fetchProducts()
})

onMounted(() => {
  fetchCategoryName()
  fetchProducts()
})
</script>

<template>
  <div class="category-page">
    <!-- 标题 -->
    <div class="category-header">
      <h2>{{ categoryName || '商品列表' }}</h2>
    </div>

    <!-- 商品网格 -->
    <div v-loading="loading" class="product-grid">
      <div
          v-for="p in products"
          :key="p.id"
          class="product-card"
          @click="goDetail(p.id)"
      >
        <div class="product-card__image">
          <img :src="p.coverImage" :alt="p.name" />
          <span v-if="p.tag" class="product-card__tag">{{ p.tag }}</span>
        </div>
        <div class="product-card__body">
          <h4 class="product-card__name">{{ p.name }}</h4>
          <div class="product-card__price">
            <span class="product-card__price-now">¥{{ p.price }}</span>
            <span v-if="p.originalPrice" class="product-card__price-old">
              ¥{{ p.originalPrice }}
            </span>
          </div>
          <div class="product-card__footer">
            <span class="product-card__sales">
              已售 {{ p.sales }} · 库存 {{ p.stock }}
            </span>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && products.length === 0" description="该分类下暂无商品" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.category-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 60px;
  background: #F5F7F6;
  min-height: 60vh;
}

.category-header {
  margin-bottom: 20px;
}
.category-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0;
  position: relative;
  padding-left: 14px;
}
.category-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 5px;
  height: 24px;
  background: #3DA67E;
  border-radius: 3px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  min-height: 200px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #F0F0F0;
}
.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #3DA67E;
}
.product-card__image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #FAFAFA;
}
.product-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.product-card:hover .product-card__image img {
  transform: scale(1.08);
}
.product-card__tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #3DA67E;
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
}
.product-card__body {
  padding: 12px;
}
.product-card__name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}
.product-card__price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}
.product-card__price-now {
  font-size: 20px;
  font-weight: 800;
  color: #3DA67E;
}
.product-card__price-old {
  font-size: 13px;
  color: #BBB;
  text-decoration: line-through;
}
.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.product-card__sales {
  font-size: 12px;
  color: #999;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}
</style>