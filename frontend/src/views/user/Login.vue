<script>
export default {
  name: "Login"
}
</script>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
const route = useRoute()
const router = useRouter()
// 实例化仓库
const userStore = useUserStore()
const cartStore = useCartStore()

const form = ref({
  username: sessionStorage.getItem('register_username') || '',
  password: '',
  captcha: '',
})
const loading = ref(false)

// ===== 验证码 =====
const captchaImg = ref('')
const captchaUuid = ref('')

const loadCaptcha = async () => {
  try {
    const res = await request.get('/user/captcha')
    captchaImg.value = res.data.image
    captchaUuid.value = res.data.uuid
  } catch (e) {
    ElMessage.error('验证码加载失败')
  }
}

onMounted(() => {
  loadCaptcha()
  // 用完就删，避免用户刷新登录页还自动填
  sessionStorage.removeItem('register_username')
})

// ===== 登录 =====
const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (!form.value.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }

  loading.value = true
  try {
    const res = await request.post('/user/login', {
      username: form.value.username,
      password: form.value.password,
      captcha: form.value.captcha,
      uuid: captchaUuid.value,
    })
    userStore.setLogin(res.data.token, res.data.user)
    cartStore.fetchCount()
    ElMessage.success('登录成功')

    // 处理 redirect
    let redirect = route.query.redirect || '/'
    if (redirect === '/login' || redirect === '/register') {
      redirect = '/'
    }
    router.push(redirect)
  } catch (e) {
    // 登录失败（含验证码错误）刷新验证码
    form.value.captcha = ''
    loadCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h2>登录萌宠优选</h2>
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input
              v-model="form.username"
              :prefix-icon="User"
              placeholder="请输入用户名"
              size="large"
          />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              size="large"
              @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input
                v-model="form.captcha"
                placeholder="请输入验证码"
                maxlength="4"
                size="large"
                @keyup.enter="handleLogin"
            />
            <img
                v-if="captchaImg"
                :src="captchaImg"
                @click="loadCaptcha"
                class="captcha-img"
                title="点击刷新"
                alt="验证码"
            />
          </div>
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width:100%"
            @click="handleLogin"
        >登录</el-button>
      </el-form>

      <p class="tip">还没有账号？<a @click="router.push('/register')">立即注册</a></p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: calc(100vh - 150px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F7F6;
  padding: 40px 16px;
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}
.login-card h2 {
  text-align: center;
  font-size: 24px;
  color: #3DA67E;
  margin: 0 0 24px;
}
.tip {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #888;
}
.tip a {
  color: #3DA67E;
  cursor: pointer;
}

/* 验证码行 */
.captcha-row {
  display: flex;
  gap: 8px;
  width: 100%;
  align-items: center;
}
.captcha-row :deep(.el-input) {
  flex: 1;
}
.captcha-img {
  height: 40px;
  width: 110px;
  cursor: pointer;
  border-radius: 6px;
  border: 1px solid #DCDFE6;
  object-fit: cover;
  background: #f5f5f5;
}
</style>