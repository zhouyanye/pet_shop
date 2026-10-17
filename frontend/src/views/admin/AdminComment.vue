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
    const res = await request.get('/admin/comments', {
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

/* ---------- 隐藏 / 恢复 ---------- */
const handleToggleStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '隐藏' : '恢复显示'
  ElMessageBox.confirm(
      `确定${action}这条评论吗？`,
      '提示',
      { type: 'warning' }
  )
      .then(async () => {
        await request.put(`/admin/comments/${row.id}/status`, null, {
          params: { status: newStatus },
        })
        ElMessage.success(`已${action}`)
        fetchList()
      })
      .catch(() => {})
}

/* ---------- 删除 ---------- */
const handleDelete = (row) => {
  ElMessageBox.confirm(
      '确定删除这条评论吗？删除后不可恢复！',
      '警告',
      { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
  )
      .then(async () => {
        await request.delete(`/admin/comments/${row.id}`)
        ElMessage.success('已删除')
        fetchList()
      })
      .catch(() => {})
}

/* ---------- 详情弹窗 ---------- */
const detailVisible = ref(false)
const detailRow = ref(null)

const openDetail = (row) => {
  detailRow.value = row
  detailVisible.value = true
}

const parseImages = (images) => {
  if (!images) return []
  try { return JSON.parse(images) } catch { return [] }
}

const fmtTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-')

onMounted(fetchList)
</script>

<template>
  <div class="admin-comment">
    <el-card shadow="never">
      <!-- 工具栏 -->
      <div class="toolbar">
        <el-input
            v-model="query.keyword"
            placeholder="搜索评论内容"
            :prefix-icon="Search"
            style="width: 260px"
            clearable
            @keyup.enter="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
          <el-option label="正常显示" :value="1" />
          <el-option label="已隐藏" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productName" label="商品" min-width="160" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column label="评分" width="130">
          <template #default="{ row }">
            <el-rate
                :model-value="row.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value} 星"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <div v-if="parseImages(row.images).length" class="img-count">
              📷 {{ parseImages(row.images).length }}
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评论时间" width="170">
          <template #default="{ row }">{{ fmtTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
                text
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '隐藏' : '恢复' }}</el-button>
            <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 👈 替换成了统一的 Pagination 组件 -->
      <Pagination
          v-model:page-num="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          @change="fetchList"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="评论详情" width="560px">
      <div v-if="detailRow" class="detail-body">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="评论ID">{{ detailRow.id }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ detailRow.productName }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ detailRow.username }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate :model-value="detailRow.rating" disabled show-score />
          </el-descriptions-item>
          <el-descriptions-item label="内容">{{ detailRow.content }}</el-descriptions-item>
          <el-descriptions-item label="点赞">{{ detailRow.likes }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailRow.status === 1 ? 'success' : 'info'" size="small">
              {{ detailRow.status === 1 ? '正常' : '已隐藏' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="时间">{{ fmtTime(detailRow.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 图片 -->
        <div v-if="parseImages(detailRow.images).length" class="detail-images">
          <div class="detail-images-title">评价图片</div>
          <div class="img-list">
            <el-image
                v-for="(img, idx) in parseImages(detailRow.images)"
                :key="idx"
                :src="img"
                :preview-src-list="parseImages(detailRow.images)"
                :initial-index="idx"
                fit="cover"
                class="img-item"
            />
          </div>
        </div>
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
.img-count {
  color: #409EFF;
  font-size: 13px;
}
.detail-body {
  padding: 4px 0;
}
.detail-images {
  margin-top: 16px;
}
.detail-images-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.img-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.img-item {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: pointer;
}
</style>