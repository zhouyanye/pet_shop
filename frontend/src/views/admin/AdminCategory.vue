<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const list = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formRef = ref()
const submitLoading = ref(false)

// 彻底不要 sortOrder
const defaultForm = () => ({
  id: null, name: '', icon: '', description: '', status: 1
})
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/categories')
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  dialogTitle.value = '新增分类'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑分类'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (form.id) {
      await request.put(`/admin/categories/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/categories', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '警告', {
    type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    await request.delete(`/admin/categories/${row.id}`)
    ElMessage.success('已删除')
    fetchList()
  }).catch(() => {})
}

onMounted(fetchList)
</script>

<template>
  <div class="admin-category">
    <el-card shadow="never">
      <div class="toolbar">
        <span style="font-size: 16px; font-weight: 600; color: #303133;">商品分类列表</span>
        <el-button type="primary" :icon="Plus" @click="openAdd" style="margin-left:auto">
          新增分类
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="图标" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.icon">{{ row.icon }}</span>
            <span v-else style="color:#ccc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：狗狗主粮" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="可填 Emoji 🐶 或 图片URL" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
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
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
</style>