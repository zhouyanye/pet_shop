<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const query = reactive({ keyword: '', status: null, categoryId: null })
const categories = ref([])

// 编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增商品')
const formRef = ref()
const submitLoading = ref(false)

const defaultForm = () => ({
  id: null, categoryId: null, name: '', subtitle: '', brand: '',
  price: 0, originalPrice: 0, coverImage: '', description: '',
  stock: 0, status: 1,
})

const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请输入封面图 URL', trigger: 'blur' }],
}

const fetchCategories = async () => {
  const res = await request.get('/categories')
  categories.value = res.data || []
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/products', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: query.keyword || undefined,
        status: query.status ?? undefined,
        categoryId: query.categoryId ?? undefined,
      },
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; fetchList() }
const handleReset = () => {
  query.keyword = ''
  query.status = null
  query.categoryId = null
  pageNum.value = 1
  fetchList()
}

const openAdd = () => {
  dialogTitle.value = '新增商品'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑商品'
  Object.assign(form, {
    id: row.id, categoryId: row.categoryId, name: row.name,
    subtitle: row.subtitle, brand: row.brand, price: Number(row.price),
    originalPrice: Number(row.originalPrice || 0), coverImage: row.coverImage,
    description: row.description, stock: row.stock, status: row.status,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (form.id) {
      await request.put(`/admin/products/${form.id}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/products', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？删除后不可恢复！`, '警告', {
    type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger',
  })
      .then(async () => {
        await request.delete(`/admin/products/${row.id}`) // 👈 修改了这里
        ElMessage.success('已删除')
        fetchList()
      })
      .catch(() => {})
}

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确定${action}「${row.name}」吗？`, '提示', { type: 'warning' })
      .then(async () => {
        await request.put(`/admin/products/${row.id}/status`, null, {
          params: { status: newStatus },
        })
        ElMessage.success(`已${action}`)
        fetchList()
      })
      .catch(() => {})
}

onMounted(() => {
  fetchCategories()
  fetchList()
})
</script>

<template>
  <div class="admin-product">
    <el-card shadow="never">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-input
            v-model="query.keyword"
            placeholder="搜索商品名称 / 品牌"
            :prefix-icon="Search"
            style="width: 240px"
            clearable
            @keyup.enter="handleSearch"
        />
        <el-select v-model="query.categoryId" placeholder="分类" clearable style="width: 140px">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="已上架" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button type="primary" :icon="Plus" @click="openAdd" style="margin-left:auto">
          新增商品
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <img :src="row.coverImage" class="cover-img" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span style="color:#F56C6C;font-weight:600">¥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button text :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 👈 统一的分页组件 -->
      <Pagination
          v-model:page-num="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          @change="fetchList"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="图片 URL" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
.cover-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
}
</style>