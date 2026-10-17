<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import request from '@/utils/request'
import Pagination from '@/components/Pagination.vue'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const query = reactive({ keyword: '', status: null })

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/users', {
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

const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '封禁' : '解封'
  ElMessageBox.confirm(
      `确定${action}用户「${row.username}」吗？`,
      '提示',
      { type: 'warning' }
  )
      .then(async () => {
        await request.put(`/admin/users/${row.id}/status`, null, {
          params: { status: newStatus },
        })
        ElMessage.success(`已${action}`)
        fetchList()
      })
      .catch(() => {})
}

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({ user: {}, orderCount: 0, totalSpent: 0 })

const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await request.get(`/admin/users/${row.id}`)
    detail.value = res.data
  } finally {
    detailLoading.value = false
  }
}

const fmtTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-')

onMounted(fetchList)
</script>

<template>
  <div class="admin-user">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
            v-model="query.keyword"
            placeholder="用户名 / 昵称 / 手机号 / 邮箱"
            :prefix-icon="Search"
            style="width: 260px"
            clearable
            @keyup.enter="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="正常" :value="1" />
          <el-option label="已封禁" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar || ''">
              {{ (row.nickname || row.username || 'U').charAt(0).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="point" label="积分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
                text
                :type="row.status === 1 ? 'danger' : 'success'"
                @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '封禁' : '解封' }}</el-button>
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

    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用户ID">{{ detail.user.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ detail.user.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detail.user.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.user.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detail.user.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ detail.user.gender === 1 ? '男' : detail.user.gender === 2 ? '女' : '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="积分">{{ detail.user.point || 0 }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detail.user.status === 1 ? 'success' : 'danger'" size="small">
              {{ detail.user.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单数">{{ detail.orderCount }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">
            <span style="color:#F56C6C;font-weight:600">
              ¥{{ Number(detail.totalSpent || 0).toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">
            {{ fmtTime(detail.user.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>