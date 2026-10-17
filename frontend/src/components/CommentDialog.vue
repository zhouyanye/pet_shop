<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  modelValue: Boolean,
  orderId: Number,
  item: Object,
})
const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
watch(() => props.modelValue, v => visible.value = v)
watch(visible, v => emit('update:modelValue', v))

const rating = ref(5)
const content = ref('')
const submitting = ref(false)

const handleSubmit = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  submitting.value = true
  try {
    await request.post('/comment', {
      orderId: props.orderId,
      productId: props.item.productId,
      rating: rating.value,
      content: content.value,
    })
    ElMessage.success('评价成功')
    visible.value = false
    content.value = ''
    rating.value = 5
    emit('success')
  } catch (e) {
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" title="发表评价" width="500px">
    <div v-if="item" class="comment-form">
      <div class="product-row">
        <img :src="item.productImage" />
        <span>{{ item.productName }}</span>
      </div>

      <div class="rate-row">
        <span>评分</span>
        <el-rate v-model="rating" />
      </div>

      <el-input
          v-model="content"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="分享您的使用体验吧~"
      />
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.comment-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.product-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #FAFAFA;
  border-radius: 8px;
}
.product-row img {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
}
.rate-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>