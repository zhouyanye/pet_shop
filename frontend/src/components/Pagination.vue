<script setup>
const props = defineProps({
  pageNum: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  total: { type: Number, required: true },
})
const emit = defineEmits(['update:pageNum', 'update:pageSize', 'change'])

const onCurrent = (p) => {
  emit('update:pageNum', p)
  emit('change')
}
const onSize = (s) => {
  emit('update:pageSize', s)
  emit('update:pageNum', 1)
  emit('change')
}
</script>

<template>
  <div class="pagination-wrap">
    <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="onCurrent"
        @size-change="onSize"
    />
  </div>
</template>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>