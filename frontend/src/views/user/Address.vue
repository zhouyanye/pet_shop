<script>
export default {
  name: "Address"
}
</script>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Location,ArrowLeft  } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const list = ref([])
const loading = ref(false)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增地址')
const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  id: null,
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0,
})

const rules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

// 拉列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/address')
    list.value = res.data || []
  } catch (e) {
  } finally {
    loading.value = false
  }
}

// 打开新增
const openAdd = () => {
  dialogTitle.value = '新增地址'
  Object.assign(form, {
    id: null,
    receiver: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0,
  })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

// 打开编辑
const openEdit = (item) => {
  dialogTitle.value = '编辑地址'
  Object.assign(form, {
    id: item.id,
    receiver: item.receiver,
    phone: item.phone,
    province: item.province,
    city: item.city,
    district: item.district,
    detail: item.detail,
    isDefault: item.isDefault,
  })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const payload = {
      receiver: form.receiver,
      phone: form.phone,
      province: form.province,
      city: form.city,
      district: form.district,
      detail: form.detail,
      isDefault: form.isDefault,
    }
    if (form.id) {
      await request.put(`/address/${form.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      await request.post('/address', payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = (item) => {
  ElMessageBox.confirm(`确定删除「${item.receiver}」的地址吗？`, '提示', {
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/address/${item.id}`)
      ElMessage.success('已删除')
      fetchList()
    } catch (e) {
    }
  }).catch(() => {})
}

// 设为默认
const handleSetDefault = async (item) => {
  if (item.isDefault === 1) return
  try {
    await request.put(`/address/${item.id}/default`)
    ElMessage.success('已设为默认')
    fetchList()
  } catch (e) {
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="address-page">
    <div class="address-header">
      <div style="display: flex; align-items: center;">
        <el-button link @click="$router.back()" style="margin-right: 12px;">
          <el-icon><ArrowLeft /></el-icon> 返回
        </el-button>
        <h2>收货地址</h2>
      </div>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增地址</el-button>
    </div>

    <div v-loading="loading" class="address-list">
      <el-empty v-if="!loading && list.length === 0" description="还没有收货地址">
        <el-button type="primary" @click="openAdd">添加第一个地址</el-button>
      </el-empty>

      <div
          v-for="item in list"
          :key="item.id"
          :class="['address-item', { default: item.isDefault === 1 }]"
      >
        <div class="address-info">
          <div class="address-top">
            <span class="receiver">{{ item.receiver }}</span>
            <span class="phone">{{ item.phone }}</span>
            <el-tag v-if="item.isDefault === 1" type="success" size="small">默认</el-tag>
          </div>
          <p class="address-detail">
            {{ item.province }} {{ item.city }} {{ item.district }} {{ item.detail }}
          </p>
        </div>

        <div class="address-actions">
          <el-button text :icon="Location" @click="handleSetDefault(item)" v-if="item.isDefault !== 1">
            设为默认
          </el-button>
          <el-button text :icon="Edit" @click="openEdit(item)">编辑</el-button>
          <el-button text type="danger" :icon="Delete" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="如：湖南省" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="如：湘潭市" />
        </el-form-item>
        <el-form-item label="区/县" prop="district">
          <el-input v-model="form.district" placeholder="如：雨湖区" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input
              v-model="form.detail"
              type="textarea"
              :rows="2"
              placeholder="街道、门牌号等"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.address-page {
  max-width: 900px;
  margin: 32px auto;
  padding: 0 16px;
}

.address-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.address-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0;
  padding-left: 12px;
  position: relative;
}
.address-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #3DA67E;
  border-radius: 2px;
}

.address-list {
  min-height: 200px;
}

.address-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 12px;
  border: 1px solid #F0F0F0;
  transition: all 0.2s;
}
.address-item.default {
  border-color: #3DA67E;
  background: #F8FDFA;
}
.address-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.address-info {
  flex: 1;
  min-width: 0;
}
.address-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}
.receiver {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.phone {
  font-size: 14px;
  color: #666;
}
.address-detail {
  font-size: 13px;
  color: #888;
  margin: 0;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

@media (max-width: 600px) {
  .address-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>