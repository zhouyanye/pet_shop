<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/admin/login', form.value)
    localStorage.setItem('admin_token', res.data.token)
    localStorage.setItem('admin', JSON.stringify(res.data.admin))
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch (e) {
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-login">
    <div class="login-card">
      <h1>萌宠优选 · 后台管理</h1>
      <el-form label-position="top">
        <el-form-item label="管理员账号">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
              v-model="form.password"
              type="password"
              show-password
              size="large"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width:100%"
            @click="handleLogin"
        >登录</el-button>
      </el-form>
      <p class="tip">默认账号：admin / ohhh</p>
    </div>
  </div>
</template>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3DA67E, #2B8B68);
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}
.login-card h1 {
  text-align: center;
  font-size: 22px;
  color: #3DA67E;
  margin: 0 0 28px;
}
.tip {
  text-align: center;
  color: #999;
  font-size: 13px;
  margin-top: 16px;
}
</style>