import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Category from '@/views/Category.vue'
import Register from '@/views/user/Register.vue'
import Login from '@/views/user/Login.vue'
import Search from '@/views/Search.vue'
import Cart from '@/views/Cart.vue'
import OrderConfirm from '@/views/OrderConfirm.vue'
import OrderPay from '@/views/OrderPay.vue'
import ProductDetail from '@/views/ProductDetail.vue'
import { ElMessage } from "element-plus"

// 引入后台布局
import AdminLayout from '@/views/admin/AdminLayout.vue'
import AdminLogin from '@/views/admin/AdminLogin.vue'

//普通用户布局
import UserLayout from '@/views/user/UserLayout.vue'

const routes = [
  //  公共页面
  { path: '/', name: 'Home', component: Home },
  { path: '/category/:id', name: 'Category', component: Category },
  { path: '/product/:id', name: 'ProductDetail', component: ProductDetail },
  { path: '/search', name: 'Search', component: Search },
  { path: '/cart', name: 'Cart', component: Cart, meta: { requiresAuth: true } },
  { path: '/order/confirm', name: 'OrderConfirm', component: OrderConfirm, meta: { requiresAuth: true } },
  { path: '/order/pay/:id', name: 'OrderPay', component: OrderPay, meta: { requiresAuth: true } },
  { path: '/register', name: 'Register', component: Register },
  { path: '/login', name: 'Login', component: Login },

  // 普通用户个人中心
  {
    path: '/user',
    component: UserLayout, // 使用 UserLayout 作为父组件
    redirect: '/user/profile', // 默认跳转到个人资料
    meta: { requiresAuth: true }, // 需要登录
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人资料' },
      },
      {
        path: 'orders',
        name: 'UserOrder',
        component: () => import('@/views/user/UserOrder.vue'),
        meta: { title: '我的订单' },
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/user/OrderDetail.vue'),
        meta: { title: '订单详情' },
      },
      {
        path: 'address',
        name: 'Address',
        component: () => import('@/views/user/Address.vue'),
        meta: { title: '收货地址' },
      },
      {
        path: 'comments',
        name: 'Comment',
        component: () => import('@/views/user/UserComment.vue'),
        meta: { title: '评论管理' },
      },
      {
        path: 'coupons',
        name: 'UserCoupon',
        component: () => import('@/views/Coupon.vue'),
        meta: { title: '我的优惠券' },
      },
      {
        path: 'security',
        name: 'Security',
        component: () => import('@/views/user/Security.vue'),
        meta: { title: '我的优惠券' },
      }
    ]
  },

  // 管理员后台
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
  },
  {
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { title: '数据看板' },
      },
      {
        path: 'categories',
        name: 'AdminCategory',
        component: () => import('@/views/admin/AdminCategory.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'products',
        name: 'AdminProduct',
        component: () => import('@/views/admin/AdminProduct.vue'),
        meta: { title: '商品管理' },
      },
      {
        path: 'orders',
        name: 'AdminOrder',
        component: () => import('@/views/admin/AdminOrder.vue'),
        meta: { title: '订单管理' },
      },
      {
        path: 'users',
        name: 'AdminUser',
        component: () => import('@/views/admin/AdminUser.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'comments',
        name: 'AdminComment',
        component: () => import('@/views/admin/AdminComment.vue'),
        meta: { title: '评论管理' },
      },
      {
        path: 'coupons',
        name: 'AdminCoupon',
        component: () => import('@/views/admin/AdminCoupon.vue'),
        meta: { title: '优惠券管理' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userToken = localStorage.getItem('token')
  const adminToken = localStorage.getItem('admin_token')

  //  已登录普通用户访问 /login /register → 跳首页
  if (userToken && (to.path === '/login' || to.path === '/register')) {
    next('/')
    return
  }

  // 管理员已登录访问 /admin/login → 跳后台
  if (adminToken && to.path === '/admin/login') {
    next('/admin/dashboard')
    return
  }

  //  后台页面需要 admin_token
  if (to.matched.some(r => r.meta.requiresAdmin) && !adminToken) {
    next({ path: '/admin/login', query: { redirect: to.fullPath } })
    return
  }

  // 需要普通用户登录
  if (to.matched.some(r => r.meta.requiresAuth) && !userToken) {
    //用户登录后能直接跳回刚才想去的页面
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  next()
})

export default router