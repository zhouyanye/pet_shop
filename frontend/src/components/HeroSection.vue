<script>
export default{
  name:"HeroSection"
}
</script>
<script setup>
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay, Navigation, Pagination } from 'swiper/modules'

import 'swiper/css'
import 'swiper/css/navigation'
import 'swiper/css/pagination'

const modules = [Autoplay, Navigation, Pagination]

const bannerImages = [
  'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=1200',
  'https://images.unsplash.com/photo-1548247416-ec66f4900b2e?w=1200',
  'https://images.unsplash.com/photo-1576201836106-db1758fd1c97?w=1200',
]
</script>
<template>
  <div class="banner-wrapper">
    <swiper
        :modules="modules"
        :slides-per-view="1"
        :loop="true"
        :autoplay="{ delay: 3000, disableOnInteraction: false }"
        :navigation="{ prevEl: '.swiper-prev', nextEl: '.swiper-next' }"
        :pagination="{ el: '.swiper-pager', clickable: true }"
        class="my-swiper"
    >
      <swiper-slide v-for="(img, i) in bannerImages" :key="i">
        <div class="slide-inner">
          <img :src="img" alt="" class="banner-img" />
        </div>
      </swiper-slide>
    </swiper>

    <!-- 自定义箭头（放在 wrapper 里，不会跑到图片外面） -->
    <button class="swiper-prev"><i class="fa-solid fa-chevron-left"></i></button>
    <button class="swiper-next"><i class="fa-solid fa-chevron-right"></i></button>
    <!-- 自定义分页点 -->
    <div class="swiper-pager"></div>
  </div>
</template>
<style scoped>
/* 外层容器：定宽 + 圆角 + 阴影，作为视觉边界 */
.banner-wrapper {
  position: relative;
  width: 100%;
  max-width: 1200px;      /* 与页面其他内容对齐 */
  margin: 0 auto;
  aspect-ratio: 3 / 1;    /* 宽高比 3:1，图片不会太扁也不会太高 */
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  background: #f0f4f8;
}

.my-swiper {
  width: 100%;
  height: 100%;
}

/* 每张 slide 内层：保证图片居中等比缩放，超出就裁切 */
.slide-inner {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* 关键：cover 填满 + 主体居中，不会再有奇怪留白 */
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

/* 自定义箭头 */
.swiper-prev,
.swiper-next {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  color: #1e2b3a;
  font-size: 1rem;
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.swiper-prev { left: 16px; }
.swiper-next { right: 16px; }
.swiper-prev:hover,
.swiper-next:hover {
  background: #fff;
  transform: translateY(-50%) scale(1.05);
}

/* 自定义分页点 */
.swiper-pager {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 8px;
  z-index: 10;
}

/* 覆盖 Swiper 默认分页样式 */
.swiper-pager :deep(.swiper-pagination-bullet) {
  width: 8px;
  height: 8px;
  background: rgba(255, 255, 255, 0.6);
  opacity: 1;
  border-radius: 4px;
  transition: all 0.3s;
}

.swiper-pager :deep(.swiper-pagination-bullet-active) {
  width: 24px;
  background: #fff;
}

/* 响应式：小屏比例调高一点，避免图片太扁 */
@media (max-width: 768px) {
  .banner-wrapper {
    aspect-ratio: 2 / 1;
    border-radius: 12px;
  }
  .swiper-prev,
  .swiper-next {
    width: 36px;
    height: 36px;
  }
}
</style>