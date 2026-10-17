<script>
export default {
  name:"Register"
}
</script>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Message } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const form = ref({
  username: '',
  password: '',
  confirm: '',
  phone: '',
  email: '',
})
const loading = ref(false)

const handleRegister = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (form.value.password !== form.value.confirm) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }

  loading.value = true
  try {
    await request.post('/user/register', {
      username: form.value.username,
      password: form.value.password,
      phone: form.value.phone || null,
      email: form.value.email || null,
    })
    ElMessage.success('注册成功，请登录')
    sessionStorage.setItem('register_username', form.value.username)
    router.push('/login')
  } catch (e) {
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <h2>注册萌宠优选</h2>
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
              :prefix-icon="Lock"
              placeholder="请设置密码（至少6位）"
              size="large"
          />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input
              v-model="form.confirm"
              type="password"
              show-password
              :prefix-icon="Lock"
              placeholder="请再次输入密码"
              size="large"
          />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input
              v-model="form.phone"
              :prefix-icon="Iphone"
              placeholder="选填"
              size="large"
          />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input
              v-model="form.email"
              :prefix-icon="Message"
              placeholder="选填"
              size="large"
          />
        </el-form-item>

        <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width:100%"
            @click="handleRegister"
        >注册</el-button>
      </el-form>

      <p class="tip">
        已有账号？<a @click="router.push('/login')">返回登录</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: calc(100vh - 150px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F7F6;
  padding: 40px 16px;
}
.register-card {
  width: 440px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
}
.register-card h2 {
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
</style>