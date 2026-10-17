<script>
export default {
  name: "LoginDialog"
}
</script>
<script setup>
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Message } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userStore = useUserStore()
const cartStore = useCartStore()
const props = defineProps({
  modelValue: Boolean,
  // 控制弹窗显示
  defaultTab: { type: String, default: 'login' },
})
const emit = defineEmits(['update:modelValue', 'login-success'])

const visible = ref(props.modelValue)
const activeTab = ref(props.defaultTab)

watch(() => props.modelValue, (v) => visible.value = v)
watch(() => props.defaultTab, (v) => activeTab.value = v)

// 弹窗显示/隐藏时同步，并在打开时加载验证码
watch(visible, (v) => {
  emit('update:modelValue', v)
  if (v) loadCaptcha()
})

const loginForm = ref({ username: '', password: '', captcha: '' })
const registerForm = ref({ username: '', password: '', confirm: '', phone: '', email: '' })

//验证码相关
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

//登录函数，所有登录逻辑全部放这里
const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (!loginForm.value.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }
  try {
    const res = await request.post('/user/login', {
      username: loginForm.value.username,
      password: loginForm.value.password,
      captcha: loginForm.value.captcha,
      uuid: captchaUuid.value,
    })
    // 不再手动写 localStorage，交给pinia
    userStore.setLogin(res.data.token, res.data.user)
    // 登录成功后，刷新购物车数量
    cartStore.fetchCount()

    ElMessage.success('登录成功')
    emit('login-success', res.data.user)
    visible.value = false
    loginForm.value = { username: '', password: '', captcha: '' }
  } catch (err) {
    loadCaptcha()
    loginForm.value.captcha = ''
  }
}

//注册
const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (registerForm.value.password !== registerForm.value.confirm) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  try {
    await request.post('/user/register', {
      username: registerForm.value.username,
      password: registerForm.value.password,
      phone: registerForm.value.phone,
      email: registerForm.value.email,
    })
    ElMessage.success('注册成功，请登录')
    loginForm.value.username = registerForm.value.username
    loginForm.value.password = ''
    loginForm.value.captcha = ''
    activeTab.value = 'login'
    registerForm.value = { username: '', password: '', confirm: '', phone: '', email: '' }
    // 切回登录后刷新验证码
    loadCaptcha()
  } catch (err){
  }
}

// 切换 tab 时如果切到登录，刷新验证码
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'login') {
    loginForm.value.captcha = ''
    loadCaptcha()
  }
}
</script>

<template>
  <el-dialog v-model="visible" width="420px" :show-close="true" align-center>
    <div class="auth-tabs">
      <span
          :class="['auth-tabs__item', { active: activeTab === 'login' }]"
          @click="switchTab('login')"
      >登录</span>
      <span
          :class="['auth-tabs__item', { active: activeTab === 'register' }]"
          @click="switchTab('register')"
      >注册</span>
    </div>

    <!-- 登录表单 -->
    <el-form v-if="activeTab === 'login'" label-position="top" class="auth-form">
      <el-form-item label="用户名">
        <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
        />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="Lock"
            size="large"
        />
      </el-form-item>
      <el-form-item label="验证码">
        <div class="captcha-row">
          <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              maxlength="4"
              size="large"
              class="captcha-row__input"
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
          class="auth-form__submit"
          @click="handleLogin"
      >登录</el-button>
      <p class="tip">还没有账号？<a @click="switchTab('register')">立即注册</a></p>
    </el-form>

    <!-- 注册表单 -->
    <el-form v-else label-position="top" class="auth-form">
      <el-form-item label="用户名">
        <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
        />
      </el-form-item>
      <el-form-item label="密码">
        <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置密码（至少6位）"
            show-password
            :prefix-icon="Lock"
            size="large"
        />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
            v-model="registerForm.confirm"
            type="password"
            placeholder="请再次输入密码"
            show-password
            :prefix-icon="Lock"
            size="large"
        />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input
            v-model="registerForm.phone"
            placeholder="选填"
            :prefix-icon="Iphone"
            size="large"
        />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input
            v-model="registerForm.email"
            placeholder="选填"
            :prefix-icon="Message"
            size="large"
        />
      </el-form-item>
      <el-button
          type="primary"
          size="large"
          class="auth-form__submit"
          @click="handleRegister"
      >注册</el-button>
      <p class="tip">已有账号？<a @click="switchTab('login')">返回登录</a></p>
    </el-form>
  </el-dialog>
</template>

<style scoped>
/* tab切换栏 */
.auth-tabs {
  display: flex;
  border-bottom: 2px solid #F0F0F0;
  margin-bottom: 24px;
}

.auth-tabs__item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 16px;
  cursor: pointer;
  color: #888;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
}

.auth-tabs__item.active {
  color: #3DA67E;
  border-bottom-color: #3DA67E;
  font-weight: 600;
}

.auth-form__submit {
  width: 100%;
  margin-top: 8px;
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

/* 验证码行，和登录页面保持一致 */
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
