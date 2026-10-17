<script>
export default {
  name: "AppHeader"
}
</script>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import LoginDialog from '@/components/LoginDialog.vue'
import {
  Search, ArrowDown, UserFilled, Setting, List, Location,Ticket
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showLoginDialog = ref(false)
const defaultTab = ref('login')

const categories = ref([])
const fetchCategories = async () => {
  try {
    const res = await request.get('/categories')
    categories.value = res.data || []
  } catch (e) {}
}

const openLogin = () => {
  defaultTab.value = 'login'
  showLoginDialog.value = true
}
const openRegister = () => {
  defaultTab.value = 'register'
  showLoginDialog.value = true
}

const handleLoginSuccess = () => {
  // 弹窗登录后 Pinia 已经更新，这里只需要拉购物车数量
  cartStore.fetchCount()
}

const handleCartClick = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: '/cart' } })
    return
  }
  router.push('/cart')
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  router.push({ path: '/search', query: { q: searchKeyword.value } })
}

const handleLogout = () => {
  userStore.logout()
  cartStore.reset()
  ElMessage.success('已退出登录')
}

const handleCommand = (command) => {
  if (command === 'logout') handleLogout()
  else if (command === 'profile') router.push('/user/profile')
  else if (command === 'orders') router.push('/user/orders')
  else if (command === 'address') router.push('/user/address')
  else if (command === 'coupon') router.push('/coupon')
  // else if (command === 'settings') router.push('/user/settings')
}

onMounted(() => {
  fetchCategories()
  cartStore.fetchCount()
})
</script>

<template>
  <header class="app-header">
    <!-- 顶部窄条 -->
    <div class="top-bar">
      <div class="top-bar__inner">
        <span class="top-bar__welcome">欢迎来到萌宠优选 · 让每一只毛孩子都幸福</span>
        <div class="top-bar__links">
          <span><el-icon><Location/></el-icon>配送至：全国</span>
          <a>我的订单</a>
          <a @click="router.push('/chat')">AI 客服</a>
        </div>
      </div>
    </div>

    <!-- 主导航 -->
    <div class="nav-main">
      <div class="nav-main__inner">
        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <i class="fa-solid fa-paw logo__icon"></i>
          <span class="logo__text">萌宠优选</span>
        </div>

        <!-- 搜索栏 -->
        <div class="search-box">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索狗粮、猫粮、宠物玩具..."
              size="large"
              @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" type="primary">搜索</el-button>
            </template>
          </el-input>
        </div>

        <!-- 右侧操作区 -->
        <div class="nav-actions">
          <!-- 购物车 -->
          <div v-if="userStore.isLoggedIn" class="cart-btn" @click="handleCartClick">
            <el-badge :value="cartStore.count" :max="99" :hidden="cartStore.count === 0">
              <i class="fa-solid fa-cart-shopping" style="font-size: 22px"></i>
            </el-badge>
            <span>购物车</span>
          </div>

          <!-- 未登录 -->
          <template v-if="!userStore.isLoggedIn">
            <el-button type="primary" @click="openLogin">登录</el-button>
            <el-button @click="openRegister">注册</el-button>
          </template>

          <!-- 已登录 -->
          <el-dropdown v-else @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.user?.avatar || ''"/>
              <span class="user-info__name">
                  {{ userStore.user?.nickname || userStore.user?.username }}
                </span>
              <el-icon>
                <ArrowDown/>
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="UserFilled">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders" :icon="List">我的订单</el-dropdown-item>
                <el-dropdown-item command="address" :icon="Location">收货地址</el-dropdown-item>
                <el-dropdown-item command="coupon" :icon="Ticket">红包卡券</el-dropdown-item>
<!--                <el-dropdown-item command="settings" :icon="Setting">账号设置</el-dropdown-item>-->
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 分类菜单栏 -->
    <nav class="nav-menu">
      <div class="nav-menu__inner">
        <a
            class="nav-menu__item"
            :class="{ active: route.path === '/' }"
            @click="router.push('/')"
        >首页</a>

        <a
            v-for="cat in categories"
            :key="cat.id"
            class="nav-menu__item"
            :class="{ active: route.path === '/category/' + cat.id }"
            @click="router.push('/category/' + cat.id)"
        >{{ cat.name }}</a>

        <a
            class="nav-menu__item"
            :class="{ active: route.path === '/hot' }"
            @click="router.push('/hot')"
        >热卖排行</a>

        <a
            class="nav-menu__item"
            :class="{ active: route.path === '/new' }"
            @click="router.push('/new')"
        >新品上市</a>
      </div>
    </nav>

    <!-- 登录/注册弹窗 -->
    <LoginDialog
        v-model="showLoginDialog"
        :default-tab="defaultTab"
        @login-success="handleLoginSuccess"
    />
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.top-bar {
  background: #F7F8FA;
  border-bottom: 1px solid #E8E8E8;
}

.top-bar__inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #888;
  padding: 0 16px;
}

.top-bar__links {
  display: flex;
  gap: 16px;
  align-items: center;
}

.top-bar__links a {
  cursor: pointer;
}

.top-bar__links a:hover {
  color: #3DA67E;
}

.top-bar__links span {
  display: flex;
  align-items: center;
  gap: 2px;
}

.nav-main {
  background: #fff;
}

.nav-main__inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 72px;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo__icon {
  font-size: 28px;
  color: #3DA67E;
}

.logo__text {
  font-size: 24px;
  font-weight: 700;
  color: #3DA67E;
  white-space: nowrap;
}

.search-box {
  flex: 1;
  /*max-width: 560px;*/
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 8px 0 0 8px;
}

.search-box :deep(.el-input-group__append) {
  padding: 0;
}

.search-box :deep(.el-input-group__append .el-button) {
  border-radius: 0 8px 8px 0;
  margin: 0;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  margin-left: auto;
}

.cart-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #555;
  font-size: 14px;
  transition: color 0.2s;
}

.cart-btn:hover {
  background: transparent;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: transparent;
}

.user-info__name {
  font-size: 14px;
  color: #333;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-menu {
  background: #fff;
  border-top: 1px solid #F0F0F0;
}

.nav-menu__inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 44px;
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0 16px;
}

.nav-menu__item {
  padding: 0 20px;
  height: 44px;
  line-height: 44px;
  font-size: 15px;
  color: #333;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}

.nav-menu__item:hover {
  color: #3DA67E;
}

.nav-menu__item.active {
  font-weight: 600;
  color: #3DA67E;
}

@media (max-width: 768px) {
  .top-bar__links a:nth-child(2) {
    display: none;
  }

  .nav-main__inner {
    gap: 12px;
  }

  .logo__text {
    display: none;
  }

  .nav-menu__inner {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .nav-menu__item {
    padding: 0 14px;
    white-space: nowrap;
    font-size: 14px;
  }

  .cart-btn span {
    display: none;
  }
}

.user-info:focus,
.user-info:focus-visible,
.user-info:focus-within {
  outline: none;
}
</style>