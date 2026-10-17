<script >
export default {
  name: "Home"
}
</script>
<script setup>
import { ref, onMounted } from 'vue'
import HeroSection from '@/components/HeroSection.vue'
import ProductSection from '@/components/ProductSection.vue'
import request from '@/utils/request'

// 三类商品数据
const hotProducts = ref([])
const newProducts = ref([])
const recommendProducts = ref([])
const loading = ref(false)

// 统一拉取商品，按标签/排序分类
const fetchProducts = async () => {
  loading.value = true
  try {
    // 热卖：按销量排序（后端 PageQuery 支持的话）
    const hotRes = await request.get('/products', {
      params: { pageNum: 1, pageSize: 10, sortBy: 'sales', order: 'desc' }
    })
    hotProducts.value = hotRes.data.records || []
    console.log("热卖",hotRes.data.records)


    // 新品：按创建时间排序
    const newRes = await request.get('/products', {
      params: { pageNum: 1, pageSize: 10, sortBy: 'created_at', order: 'desc' }
    })
    newProducts.value = newRes.data.records || []
    console.log("新品", newRes.data.records)


    // 推荐：可以按 tag=推荐 或随机，这里用 tag 筛选
    const recRes = await request.get('/products', {
      params: { pageNum: 1, pageSize: 10, tag: '推荐' }
    })
    recommendProducts.value = recRes.data.records || []
    console.log("推荐",recRes.data.records)

  } catch (err) {
    console.error('获取商品失败', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProducts()
})

</script>

<template>
  <div class="home">
    <!-- 轮播图 + 分类侧栏 + 公告 -->
    <HeroSection />

    <!-- 热卖商品 -->
    <ProductSection
        :products="hotProducts"
        title="热卖排行"
        subtitle="最受欢迎的宠物好物，大家都在买"
        theme="green"
    />

    <!-- 促销横幅 -->
    <div class="promo-banner">
      <div class="promo-banner__inner">
        <div class="promo-banner__text">
          <h2>会员日专享 · 每月8号</h2>
          <p>领取 95 折专享券，叠加满减更划算</p>
          <el-button round size="large" type="warning" @click="router.push('/coupon')">
            立即领券
          </el-button>
        </div>
        <el-button round size="large" type="warning">了解会员权益</el-button>
      </div>
    </div>

    <!-- 新品上市 -->
    <ProductSection
        :products="newProducts"
        title="新品上市"
        subtitle="新鲜上架，抢先体验"
        theme="blue"
    />

    <!-- 好物推荐 -->
    <ProductSection
        :products="recommendProducts"
        title="好物推荐"
        subtitle="为你精心挑选的优质宠物用品"
        theme="orange"
    />
  </div>
</template>

<style scoped>
.home {
  background: #F5F7F6;
}

.promo-banner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}
.promo-banner__inner {
  background: linear-gradient(135deg, #3DA67E, #2B8B68);
  border-radius: 16px;
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.promo-banner__text h2 {
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 8px;
}
.promo-banner__text p {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}
.promo-banner__inner :deep(.el-button) {
  font-weight: 600;
}

@media (max-width: 768px) {
  .promo-banner__inner {
    flex-direction: column;
    gap: 16px;
    text-align: center;
    padding: 24px;
  }
}
</style>