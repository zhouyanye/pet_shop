<script>
export default {
  name:"Search"
}
</script>
<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search as SearchIcon } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 查询条件
const query = ref({
  keyword: route.query.q || '',
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  sortBy: '',       // sales / price / createdAt
  order: 'desc',
  pageNum: 1,
  pageSize: 12,
})

const products = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])

// 加载分类（筛选用）
const fetchCategories = async () => {
  try {
    const res = await request.get('/categories')
    categories.value = res.data || []
  } catch (e) {}
}

// 加载商品
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: query.value.pageNum,
      pageSize: query.value.pageSize,
      status: 1,
    }
    if (query.value.keyword) params.keyword = query.value.keyword
    if (query.value.categoryId !== null) params.categoryId = query.value.categoryId
    if (query.value.minPrice !== null) params.minPrice = query.value.minPrice
    if (query.value.maxPrice !== null) params.maxPrice = query.value.maxPrice
    if (query.value.sortBy) {
      params.sortBy = query.value.sortBy
      params.order = query.value.order
    }

    const res = await request.get('/products', { params })
    products.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
  } finally {
    loading.value = false
  }
}



// 点分类
const handleCategory = (id) => {
  query.value.categoryId = id
  query.value.pageNum = 1
  fetchProducts()
}

// 排序
const handleSort = (sortBy, order) => {
  query.value.sortBy = sortBy
  query.value.order = order
  query.value.pageNum = 1
  fetchProducts()
}

// 价格筛选
const handlePriceFilter = () => {
  query.value.pageNum = 1
  fetchProducts()
}

// 清空筛选
const resetFilter = () => {
  query.value.categoryId = null
  query.value.minPrice = null
  query.value.maxPrice = null
  query.value.sortBy = ''
  query.value.order = 'desc'
  query.value.pageNum = 1
  fetchProducts()
}

// 翻页
const handlePageChange = (p) => {
  query.value.pageNum = p
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goDetail = (id) => router.push(`/product/${id}`)

// 顶部 header 里搜索框改关键词时，URL 变 → 重新查
watch(() => route.query.q, (q) => {
  if (route.path !== '/search') return
  query.value.keyword = q || ''
  query.value.pageNum = 1
  fetchProducts()
})

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<template>
  <div class="search-page">


    <div class="search-body">
      <!-- 左侧筛选 -->
      <aside class="filters">
        <div class="filter-block">
          <h4>分类</h4>
          <div class="filter-list">
            <span
                :class="['filter-item', { active: query.categoryId === null }]"
                @click="handleCategory(null)"
            >全部</span>
            <span
                v-for="c in categories"
                :key="c.id"
                :class="['filter-item', { active: query.categoryId === c.id }]"
                @click="handleCategory(c.id)"
            >{{ c.name }}</span>
          </div>
        </div>

        <div class="filter-block">
          <h4>价格区间</h4>
          <div class="price-row">
            <el-input-number
                v-model="query.minPrice"
                :min="0"
                :controls="false"
                placeholder="最低"
                style="width: 90px"
            />
            <span class="sep">-</span>
            <el-input-number
                v-model="query.maxPrice"
                :min="0"
                :controls="false"
                placeholder="最高"
                style="width: 90px"
            />
          </div>
          <div class="price-actions">
            <el-button size="small" type="primary" plain @click="handlePriceFilter">确定</el-button>
            <el-button size="small" text @click="resetFilter">重置</el-button>
          </div>
        </div>
      </aside>

      <!-- 右侧结果 -->
      <main class="results">
        <!-- 排序条 -->
        <div class="sort-bar">
          <span class="sort-bar__label">排序：</span>
          <span
              :class="['sort-item', { active: query.sortBy === '' }]"
              @click="handleSort('', '')"
          >综合</span>
          <span
              :class="['sort-item', { active: query.sortBy === 'sales' }]"
              @click="handleSort('sales', 'desc')"
          >销量</span>
          <span
              :class="['sort-item', { active: query.sortBy === 'price' && query.order === 'asc' }]"
              @click="handleSort('price', 'asc')"
          >价格 ↑</span>
          <span
              :class="['sort-item', { active: query.sortBy === 'price' && query.order === 'desc' }]"
              @click="handleSort('price', 'desc')"
          >价格 ↓</span>
          <span
              :class="['sort-item', { active: query.sortBy === 'createdAt' }]"
              @click="handleSort('createdAt', 'desc')"
          >最新</span>

          <span class="sort-bar__total">共 {{ total }} 件</span>
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

          <el-empty
              v-if="!loading && products.length === 0"
              description="没有找到相关商品"
              style="grid-column: 1 / -1"
          />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap">
          <el-pagination
              v-model:current-page="query.pageNum"
              :page-size="query.pageSize"
              :total="total"
              layout="prev, pager, next, total"
              background
              @current-change="handlePageChange"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px 60px;
  background: #F5F7F6;
  min-height: 60vh;
}

.search-bar {
  margin-bottom: 20px;
}

.search-body {
  display: flex;
  gap: 16px;
}

/* 左侧筛选 */
.filters {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  height: fit-content;
}
.filter-block + .filter-block {
  margin-top: 20px;
}
.filter-block h4 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px;
}
.filter-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.filter-item {
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 13px;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-item:hover {
  background: #F0F9F4;
  color: #3DA67E;
}
.filter-item.active {
  background: #3DA67E;
  color: #fff;
  font-weight: 600;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 6px;
}
.sep {
  color: #999;
}
.price-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

/* 右侧结果 */
.results {
  flex: 1;
  min-width: 0;
}

.sort-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  font-size: 14px;
}
.sort-bar__label {
  color: #888;
}
.sort-item {
  cursor: pointer;
  color: #555;
  transition: color 0.2s;
}
.sort-item:hover {
  color: #3DA67E;
}
.sort-item.active {
  color: #3DA67E;
  font-weight: 600;
}
.sort-bar__total {
  margin-left: auto;
  color: #999;
  font-size: 13px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
@media (max-width: 768px) {
  .search-body {
    flex-direction: column;
  }
  .filters {
    width: 100%;
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}
</style>