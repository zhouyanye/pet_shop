<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Message } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的 Tab，默认在修改密码
const activeTab = ref('password')

/* ================= 修改密码逻辑 ================= */
const pwdFormRef = ref()

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度 6~20 位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && value === pwdForm.oldPassword) {
          callback(new Error('新密码不能与原密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const pwdLoading = ref(false)

const handleChangePassword = async () => {
  try {
    await pwdFormRef.value.validate()
  } catch {
    return
  }
  pwdLoading.value = true
  try {
    await request.put('/user/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdFormRef.value.resetFields()
    userStore.logout()
    await router.push('/login')
  } catch (err) {
    pwdFormRef.value.resetFields()
  } finally {
    pwdLoading.value = false
  }
}

/* ================= 找回密码逻辑 ================= */
const resetFormRef = ref()

const resetForm = reactive({
  contact: '',
  newPassword: '',
  confirmPassword: '',
})

const resetRules = {
  contact: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const isPhone = /^1[3-9]\d{9}$/.test(value)
        const isEmail = /^[\w.-]+@[\w-]+\.[\w.-]+$/.test(value)
        if (!isPhone && !isEmail) {
          callback(new Error('请输入正确的手机号或邮箱'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度 6~20 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const resetLoading = ref(false)

const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
  } catch {
    return
  }
  resetLoading.value = true
  try {
    const isPhone = /^1[3-9]\d{9}$/.test(resetForm.contact)
    await request.post('/user/reset-password', {
      phone: isPhone ? resetForm.contact : null,
      email: isPhone ? null : resetForm.contact,
      newPassword: resetForm.newPassword,
    })
    ElMessage.success('密码已重置，请用新密码登录')
    resetFormRef.value.resetFields()
  } catch (err) {
  } finally {
    resetLoading.value = false
  }
}
</script>

<template>
  <div class="security-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>安全设置</h2>
      <p class="sub-title">保护您的账号安全，请定期修改密码或找回密码</p>
    </div>

    <!-- 核心内容卡片 -->
    <div class="security-card">
      <el-tabs v-model="activeTab" class="custom-tabs">

        <!-- 修改密码 Tab -->
        <el-tab-pane label="修改密码" name="password">
          <div class="tab-content">
            <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="100px"
                class="setting-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    placeholder="请输入原密码"
                    show-password
                    :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    placeholder="请输入新密码（至少6位）"
                    show-password
                    :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                    :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pwdLoading" @click="handleChangePassword">
                  确认修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 找回密码 Tab -->
        <el-tab-pane label="找回密码" name="reset">
          <div class="tab-content">
            <el-alert
                title="输入注册时使用的手机号或邮箱，直接重置密码"
                type="info"
                :closable="false"
                style="margin-bottom: 20px"
            />
            <el-form
                ref="resetFormRef"
                :model="resetForm"
                :rules="resetRules"
                label-width="100px"
                class="setting-form"
            >
              <el-form-item label="手机/邮箱" prop="contact">
                <el-input
                    v-model="resetForm.contact"
                    placeholder="请输入手机号或邮箱"
                    :prefix-icon="Message"
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                    v-model="resetForm.newPassword"
                    type="password"
                    placeholder="请输入新密码（至少6位）"
                    show-password
                    :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                    v-model="resetForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                    :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="warning" plain :loading="resetLoading" @click="handleResetPassword">
                  重置密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.security-page {
  max-width: 900px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.sub-title {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 包裹 Tab 的卡片 */
.security-card {
  background: #fff;
  border-radius: 8px;
  padding: 10px 24px 30px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.05);
  border: 1px solid #f0f2f5;
}

/* 覆盖 Element Plus Tab 默认的头部样式，让它更清爽 */
.custom-tabs :deep(.el-tabs__header) {
  margin-bottom: 24px;
}
.custom-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  height: 48px;
  line-height: 48px;
}
.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #3da67e; /* 品牌绿 */
  font-weight: 600;
}
.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #3da67e;
  height: 3px;
  border-radius: 2px;
}

/* 表单内容区 */
.tab-content {
  padding-top: 10px;
}

/* 限制表单最大宽度并居中 */
.setting-form {
  max-width: 480px;
  margin: 0 auto;
}

/* 调整表单间距 */
.setting-form :deep(.el-form-item) {
  margin-bottom: 22px;
}
</style>