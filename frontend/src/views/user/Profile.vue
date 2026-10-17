<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Phone, Message } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 个人资料表单
const profileForm = reactive({
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  gender: 0,
  point: 0,
})
const profileLoading = ref(false)

const fetchProfile = async () => {
  profileLoading.value = true
  try {
    const res = await request.get('/user/profile')
    const u = res.data
    profileForm.nickname = u.nickname || ''
    profileForm.avatar = u.avatar || ''
    profileForm.phone = u.phone || ''
    profileForm.email = u.email || ''
    profileForm.gender = u.gender ?? 0
    profileForm.point = u.point || 0
  } catch (err) {
    // 拦截器已弹错误
  } finally {
    profileLoading.value = false
  }
}

// 上传前校验
const beforeAvatarUpload = (file) => {
  const allowTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG / PNG / WEBP 格式')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片不能超过 5MB')
    return false
  }
  return true
}

// 自定义上传
const handleUploadAvatar = async ({ file }) => {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    profileForm.avatar = res.data
    ElMessage.success('头像上传成功，记得点保存')
  } catch (err) {}
}

const handleUpdateProfile = async () => {
  try {
    await request.put('/user/profile', {
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
      phone: profileForm.phone,
      email: profileForm.email,
      gender: profileForm.gender,
    })
    ElMessage.success('资料已更新')
    userStore.setUser({
      ...userStore.user,
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
      phone: profileForm.phone,
      email: profileForm.email,
      gender: profileForm.gender,
    })
  } catch (err) {}
}

onMounted(() => {
  fetchProfile()
})
</script>

<template>
  <div class="profile-page">
    <!-- 如果放在 UserLayout 的 el-main 里，其实不需要这个最大宽度限制，直接铺满即可 -->

    <!-- 页面头部：标题和积分 -->
    <div class="page-header">
      <h2>个人资料</h2>
      <div class="point-box">
        当前积分：<span class="point-text">{{ profileForm.point || 0 }}</span>
      </div>
    </div>

    <el-form
        :model="profileForm"
        label-width="100px"
        class="profile-form"
        v-loading="profileLoading"
    >
      <!-- 头像（改成更现代的样式） -->
      <el-form-item label="头像">
        <div class="avatar-section">
          <el-avatar
              class="avatar-circle"
              :size="100"
              :src="profileForm.avatar || ''"
              fit="cover"
          >
            {{ (profileForm.nickname || 'U').charAt(0).toUpperCase() }}
          </el-avatar>

          <div class="avatar-upload-wrapper">
            <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleUploadAvatar"
                accept="image/*"
            >
              <el-button type="primary" plain>点击更换头像</el-button>
            </el-upload>
            <p class="upload-tip">支持 JPG、PNG、WEBP，不超过 5MB</p>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="昵称">
        <el-input v-model="profileForm.nickname" placeholder="请输入昵称" :prefix-icon="User" />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input v-model="profileForm.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
      </el-form-item>

      <el-form-item label="邮箱">
        <el-input v-model="profileForm.email" placeholder="请输入邮箱" :prefix-icon="Message" />
      </el-form-item>

      <el-form-item label="性别">
        <el-radio-group v-model="profileForm.gender">
          <el-radio :label="0">保密</el-radio>
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" size="large" @click="handleUpdateProfile" style="width: 120px;">
          保存修改
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
/* 因为外面有 el-main 了，这里不需要再设 max-width 和居中 */
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0;
}

.point-box {
  font-size: 14px;
  color: #606266;
  background: #e8f5e9;
  padding: 6px 16px;
  border-radius: 20px;
}
.point-text {
  font-size: 18px;
  font-weight: 700;
  color: #3DA67E;
  margin-left: 4px;
}

.profile-form {
  margin-top: 20px;
  /* 让表单不要那么宽，更好看 */
  max-width: 600px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-circle {
  border: 3px solid #e8f5e9;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* 修复头像拉伸 */
.avatar-circle :deep(img) {
  object-fit: cover !important;
}

.avatar-upload-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .avatar-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>