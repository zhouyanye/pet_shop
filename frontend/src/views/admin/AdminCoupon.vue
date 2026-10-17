<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const query = reactive({ keyword: '', status: null })

/* ---------- 类型映射 ---------- */
const TYPE_MAP = {
  1: '满减',
  2: '折扣',
  3: '无门槛',
}

const fmtType = (t) => TYPE_MAP[t] || '未知'
const fmtDiscount = (row) => {
  if (row.type === 2) {
    // 折扣类型：0.95 → 9.5折
    return `${(Number(row.discountValue) * 10).toFixed(1)} 折`
  }
  return `¥${Number(row.discountValue).toFixed(2)}`
}
const fmtTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-')

/* ---------- 列表 ---------- */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/coupons', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        keyword: query.keyword || undefined,
        status: query.status ?? undefined,
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
  pageNum.value = 1
  fetchList()
}

/* ---------- 新增 / 编辑 ---------- */
const dialogVisible = ref(false)
const dialogTitle = ref('新增优惠券')
const formRef = ref()
const submitLoading = ref(false)

const defaultForm = () => ({
  id: null,
  name: '',
  type: 1,
  discountValue: 10,
  minAmount: 100,
  totalCount: 100,
  startTime: '',
  endTime: '',
  status: 1,
})

const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

const openAdd = () => {
  dialogTitle.value = '新增优惠券'
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑优惠券'
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type,
    discountValue: Number(row.discountValue),
    minAmount: Number(row.minAmount),
    totalCount: row.totalCount,
    startTime: row.startTime,
    endTime: row.endTime,
    status: row.status,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    const payload = {
      name: form.name,
      type: form.type,
      discountValue: form.discountValue,
      minAmount: form.minAmount,
      totalCount: form.totalCount,
      startTime: form.startTime,
      endTime: form.endTime,
      status: form.status,
    }
    if (form.id) {
      await request.put(`/admin/coupons/${form.id}`, payload)
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/coupons', payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

/* ---------- 上下架 ---------- */
const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '停用' : '启用'
  ElMessageBox.confirm(`确定${action}「${row.name}」吗？`, '提示', { type: 'warning' })
      .then(async () => {
        await request.put(`/admin/coupons/${row.id}/status`, null, {
          params: { status: newStatus },
        })
        ElMessage.success(`已${action}`)
        fetchList()
      })
      .catch(() => {})
}

/* ---------- 删除 ---------- */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '警告', {
    type: 'warning',
    confirmButtonText: '删除',
    confirmButtonClass: 'el-button--danger',
  })
      .then(async () => {
        await request.delete(`/admin/coupons/${row.id}`)
        ElMessage.success('已删除')
        fetchList()
      })
      .catch(() => {})
}

/* ---------- 类型切换时调整默认值 ---------- */
const handleTypeChange = (t) => {
  if (t === 1) {
    form.discountValue = 10
    form.minAmount = 100
  } else if (t === 2) {
    form.discountValue = 0.95
    form.minAmount = 0
  } else {
    form.discountValue = 5
    form.minAmount = 0
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="admin-coupon">
    <el-card shadow="never">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-input
            v-model="query.keyword"
            placeholder="搜索优惠券名称"
            :prefix-icon="Search"
            style="width: 240px"
            clearable
            @keyup.enter="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button type="primary" :icon="Plus" @click="openAdd" style="margin-left:auto">
          新增优惠券
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ fmtType(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠值" width="110">
          <template #default="{ row }">
            <span style="color:#F56C6C;font-weight:600">{{ fmtDiscount(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="门槛" width="110">
          <template #default="{ row }">
            {{ Number(row.minAmount) > 0 ? `满 ¥${Number(row.minAmount).toFixed(0)}` : '无门槛' }}
          </template>
        </el-table-column>
        <el-table-column label="发放/领取" width="130">
          <template #default="{ row }">
            {{ row.totalCount === -1 ? '不限量' : row.totalCount }} / {{ row.receivedCount }}
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="300">
          <template #default="{ row }">
            <div class="date-range">
              <span>{{ fmtTime(row.startTime) }}</span>
              <span>~</span>
              <span>{{ fmtTime(row.endTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
                text
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
          v-model:page-num="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          @change="fetchList"
      />
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：新人立减20" />
        </el-form-item>

        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio :label="1">满减</el-radio>
            <el-radio :label="2">折扣</el-radio>
            <el-radio :label="3">无门槛</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="优惠值" prop="discountValue">
          <el-input-number
              v-model="form.discountValue"
              :min="0"
              :max="form.type === 2 ? 1 : 99999"
              :precision="form.type === 2 ? 2 : 2"
              :step="form.type === 2 ? 0.05 : 1"
              style="width: 100%"
          />
          <span class="tip">
            {{ form.type === 2 ? '折扣率，如 0.95 表示 95 折' : '减免金额（元）' }}
          </span>
        </el-form-item>

        <el-form-item v-if="form.type !== 3" label="使用门槛">
          <el-input-number
              v-model="form.minAmount"
              :min="0"
              :precision="2"
              style="width: 100%"
          />
          <span class="tip">满多少可用（0 表示无门槛）</span>
        </el-form-item>

        <el-form-item label="发放总量">
          <el-input-number
              v-model="form.totalCount"
              :min="-1"
              style="width: 100%"
          />
          <span class="tip">-1 表示不限量</span>
        </el-form-item>

        <el-form-item label="生效时间" prop="startTime">
          <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择开始时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="失效时间" prop="endTime">
          <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
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
.date-range {
  display: flex;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.tip {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>