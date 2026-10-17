<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Goods, User, List, Ticket, ChatDotSquare, DataLine,
  SwitchButton, HomeFilled, Fold, Expand, ArrowDown, Folder
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

/*  侧边栏折叠  */
const isCollapse = ref(false)

/*  读取管理员信息  */
const admin = ref({})
try {
  admin.value = JSON.parse(localStorage.getItem('admin') || '{}')
} catch {
  admin.value = {}
}

const isSuper = computed(() => Number(admin.value.role) === 1)
const displayName = computed(() => admin.value.nickname || admin.value.username || '管理员')
const avatarUrl = computed(() => admin.value.avatar || '../img/default_avatar.jpg')

/*  菜单配置  */
const menus = [
  { path: '/admin/dashboard', title: '数据看板', icon: DataLine },
  { path: '/admin/categories', title: '分类管理', icon: Folder },
  { path: '/admin/products', title: '商品管理', icon: Goods },
  { path: '/admin/orders', title: '订单管理', icon: List },
  { path: '/admin/users', title: '用户管理', icon: User },
  { path: '/admin/comments', title: '评论管理', icon: ChatDotSquare },
  { path: '/admin/coupons', title: '优惠券管理', icon: Ticket },

]

/*  顶部下拉菜单操作  */
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '退出',
        cancelButtonText: '取消',
      })
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin')
      ElMessage.success('已退出登录')
      router.push('/admin/login')
    } catch { /* 用户取消 */ }
  } else if (command === 'home') {
    router.push('/')
  }
}
</script>

<template>
  <el-container class="admin-layout">
    <!--侧边栏-->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="aside">
      <div class="logo">
        <i class="fa-solid fa-paw logo-icon"></i>
        <span v-show="!isCollapse" class="logo-text">萌宠优选</span>
      </div>

      <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="custom-menu"
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <template #title>{{ m.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!--右侧主体-->
    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <component :is="isCollapse ? Expand : Fold" />
          </el-icon>

          <el-breadcrumb separator-icon="ArrowRight">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '管理后台' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 管理员信息下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="32" :src="avatarUrl" class="user-avatar" />
              <div class="user-meta" v-show="!isCollapse">
                <span class="user-name">{{ displayName }}</span>
                <span class="user-role">{{ isSuper ? '超级管理员' : '普通管理员' }}</span>
              </div>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">
                  <el-icon><HomeFilled /></el-icon> 回到前台商城
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
}

/* ---------- 侧边栏 ---------- */
.aside {
  background: #fff;
  box-shadow: 2px 0 8px rgba(29, 35, 41, 0.05);
  transition: width 0.28s;
  z-index: 10;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #f0f2f5;
}
.logo-icon {
  font-size: 24px;
  color: #3da67e;
}
.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: #303133;
  letter-spacing: 1px;
}

/* 覆盖 Element Plus 默认菜单样式 */
.custom-menu {
  border-right: none;
  padding: 12px 8px;
  flex: 1;
}
.custom-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  border-radius: 8px;
  margin-bottom: 4px;
  color: #606266;
  font-weight: 500;
}
.custom-menu :deep(.el-menu-item:hover) {
  background-color: #f3f4f6;
}
.custom-menu :deep(.el-menu-item.is-active) {
  background-color: #e8f5e9;
  color: #3da67e;
  font-weight: 600;
}

/*  顶栏  */
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 9;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
}
.collapse-btn:hover {
  color: #3da67e;
}

/*  用户下拉菜单  */
.user-dropdown {
  cursor: pointer;
  outline: none;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-info:hover {
  background: #f5f7fa;
}
.user-avatar {
  border: 2px solid #e8f5e9;
}
.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}
.user-role {
  font-size: 12px;
  color: #909399;
}
.arrow-icon {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

/* ---------- 内容区 ---------- */
.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/*  路由过渡 (滑动淡入)  */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-15px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(15px);
}

/*  响应式  */
@media (max-width: 768px) {
  .user-meta {
    display: none;
  }
  .main {
    padding: 12px;
  }
}
</style>