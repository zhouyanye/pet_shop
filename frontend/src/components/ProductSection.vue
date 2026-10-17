<script>
export default{
  name:"ProductSection"
}
</script>
<script setup>
import { useRouter } from 'vue-router'
import { ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  products: { type: Array, default: () => [] },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  theme: { type: String, default: 'green' },
})

const router = useRouter()

const themeColors = {
  green: { primary: '#3DA67E', light: '#F0F9F4', tagBg: '#3DA67E' },
  orange: { primary: '#D9774E', light: '#FFF6F0', tagBg: '#D9774E' },
  blue: { primary: '#2D7CB0', light: '#F0F7FB', tagBg: '#2D7CB0' },
}
const theme = themeColors[props.theme] || themeColors.green

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const handleCartIconClick = (product) => {
  router.push(`/product/${product.id}`)
}

const formatSales = (n) => {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toString()
}
</script>

<template>
  <section class="product-section">
    <div class="product-section__header">
      <div>
        <h3 :style="{ borderLeftColor: theme.primary }">{{ title }}</h3>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <!--      <el-button text @click="router.push('/category/' + products[0]?.categoryId)">查看更多</el-button>-->
    </div>

    <div class="product-grid">
      <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="goToDetail(product.id)"
      >
        <div class="product-card__image">
          <img :src="product.coverImage" :alt="product.name" />
          <span v-if="product.tag" class="product-card__tag" :style="{backgroundColor: theme.tagBg}">{{ product.tag }}</span>
        </div>
        <div class="product-card__body">
          <h4 class="product-card__name">{{ product.name }}</h4>
          <div class="product-card__price">
            <span class="product-card__price-now" :style="{color: theme.primary}">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="product-card__price-old">¥{{ product.originalPrice }}</span>
          </div>
          <div class="product-card__footer">
            <span class="product-card__sales">已售 {{ formatSales(product.sales) }}· 库存 {{ product.stock }}</span>
            <el-button
                type="primary"
                size="small"
                circle
                :icon="ShoppingCart"
                :style="{backgroundColor: theme.primary, borderColor: theme.primary}"
                @click.stop="handleCartIconClick(product)"
            />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.product-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 20px;
  /* 板块背景默认白色 */
  background: #fff;
}
.product-section__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 30px;
}
.product-section__header h3 {
  font-size: 26px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0;
  position: relative;
  padding-left: 16px;
  border-left: 5px solid;
  border-radius: 3px;
}
.product-section__header p {
  font-size: 14px;
  color: #888;
  margin: 6px 0 0 16px;
}
.product-section__header :deep(.el-button) {
  color: inherit;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.product-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.28s ease;
  border: 1px solid #f2f2f2;
}
.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 14px 36px rgba(0, 0, 0, 0.12);
  border-color: #e8e8e8;
}

.product-card__image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #fafafa;
}
.product-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}
.product-card:hover .product-card__image img {
  transform: scale(1.06);
}
.product-card__tag {
  position: absolute;
  top: 10px;
  left: 10px;
  color: #fff;
  font-size: 11px;
  padding: 3px 9px;
  border-radius: 5px;
  font-weight: 600;
}

.product-card__body {
  padding: 14px;
}
.product-card__name {
  font-size: 14px;
  font-weight: 500;
  color: #222;
  margin: 0 0 10px;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 41px;
}
.product-card__price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}
.product-card__price-now {
  font-size: 22px;
  font-weight: 800;
}
.product-card__price-old {
  font-size: 13px;
  color: #bbbbbb;
  text-decoration: line-through;
}
.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.product-card__sales {
  font-size: 12px;
  color: #888;
}
.product-card__footer :deep(.el-button):hover {
  opacity: 0.82;
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .product-section__header h3 {
    font-size: 22px;
  }
  .product-section {
    padding: 32px 14px;
  }
}
</style>
