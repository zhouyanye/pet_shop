<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'

const route = useRoute()

// 定义不需要商城大 Header 的路由前缀（后台 + 用户中心）
const consolePrefixes = ['/admin', '/user']

// 计算属性：是否是控制台/中心页面
const isConsolePage = computed(() => {
  return consolePrefixes.some(prefix => route.path.startsWith(prefix))
})
</script>

<template>
  <div class="app-layout">
    <!-- 只有在普通商城页面，才显示大 Header -->
    <AppHeader v-if="!isConsolePage" />

    <main class="app-main">
      <router-view />
    </main>

    <!-- 普通商城页面才显示大 Footer -->
    <AppFooter v-if="!isConsolePage" />
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  /* 如果个人中心页面是灰色背景，建议在这里或者具体页面里加 */
  /* background-color: #f5f7fa; */
}
.app-main {
  flex: 1;
}
</style>