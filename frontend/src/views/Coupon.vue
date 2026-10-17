<script>
export default {
  name: "Coupon"
}
</script>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

const activeTab = ref('center')

// 领券中心
const centerList = ref([])
const centerLoading = ref(false)
const receiving = ref({})   // { [id]: true }

// 我的卡券
const myList = ref([])
const myLoading = ref(false)
const myTab = ref(0)   // 0未使用 1已使用 2已过期

const fetchCenter = async () => {
  centerLoading.value = true
  try {
    const res = await request.get('/coupon/list')
    centerList.value = res.data || []
  } catch (e) {
  } finally {
    centerLoading.value = false
  }
}

const fetchMy = async () => {
  myLoading.value = true
  try {
    const res = await request.get('/user-coupon/my', {
      params: { status: myTab.value },
    })
    myList.value = res.data || []
  } catch (e) {
  } finally {
    myLoading.value = false
  }
}

const handleReceive = async (item) => {
  if (item.received) {
    ElMessage.info('您已领取过该优惠券')
    return
  }
  if (!item.canReceive) {
    ElMessage.warning('该优惠券已被抢完')
    return
  }
  receiving.value[item.id] = true
  try {
    await request.post(`/coupon/receive/${item.id}`)
    ElMessage.success('领取成功')
    item.received = true
    item.canReceive = false
  } catch (e) {
  } finally {
    receiving.value[item.id] = false
  }
}

const handleTabChange = (name) => {
  if (name === 'center') fetchCenter()
  else fetchMy()
}

const handleMyTabChange = () => {
  fetchMy()
}

const goUse = () => {
  router.push('/')
}

// 计算券的显示值
const couponValueText = (item) => {
  if (item.type === 2) {
    return (item.discountValue * 10).toFixed(1) + '折'
  }
  return '¥' + item.discountValue
}
const couponCondText = (item) => {
  return item.minAmount > 0 ? `满${item.minAmount}可用` : '无门槛'
}

onMounted(() => {
  fetchCenter()
})
</script>

<template>
  <div class="coupon-page">
    <h2 class="page-title">红包卡券</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="领券中心" name="center" />
      <el-tab-pane label="我的卡券" name="my" />
    </el-tabs>

    <!-- 领券中心 -->
    <div v-if="activeTab === 'center'" v-loading="centerLoading" class="coupon-list">
      <el-empty v-if="!centerLoading && centerList.length === 0" description="暂无可领取的优惠券" />

      <div
          v-for="c in centerList"
          :key="c.id"
          :class="['coupon-card', { disabled: c.received || !c.canReceive }]"
      >
        <div class="coupon-card__left">
          <div class="coupon-card__value">
            {{ couponValueText(c) }}
          </div>
          <div class="coupon-card__cond">{{ couponCondText(c) }}</div>
        </div>

        <div class="coupon-card__right">
          <h4 class="coupon-card__name">{{ c.name }}</h4>
          <p class="coupon-card__time">
            有效期：{{ $dayjs.utc(c.startTime).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }} 至{{ $dayjs.utc( c.endTime ).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }}
          </p>

          <div class="coupon-card__progress">
            <el-progress
                :percentage="c.remainPercent"
                :stroke-width="6"
                :show-text="false"
                color="#3DA67E"
            />
            <span class="coupon-card__remain">剩余 {{ c.remainPercent }}%</span>
          </div>
        </div>

        <div class="coupon-card__action">
          <el-button
              v-if="c.received"
              disabled
              round
          >已领取</el-button>
          <el-button
              v-else-if="!c.canReceive"
              disabled
              round
          >已抢完</el-button>
          <el-button
              v-else
              type="primary"
              round
              :loading="receiving[c.id]"
              @click="handleReceive(c)"
          >立即领取</el-button>
        </div>
      </div>
    </div>

    <!-- 我的卡券 -->
    <div v-else>
      <el-tabs v-model="myTab" @tab-change="handleMyTabChange">
        <el-tab-pane label="未使用" :name="0" />
        <el-tab-pane label="已使用" :name="1" />
        <el-tab-pane label="已过期" :name="2" />
      </el-tabs>

      <div v-loading="myLoading" class="coupon-list">
        <el-empty v-if="!myLoading && myList.length === 0" description="暂无卡券" />

        <div
            v-for="c in myList"
            :key="c.userCouponId"
            :class="['coupon-card', { gray: c.status !== 0 }]"
        >
          <div class="coupon-card__left">
            <div class="coupon-card__value">
              {{ couponValueText(c) }}
            </div>
            <div class="coupon-card__cond">{{ couponCondText(c) }}</div>
          </div>

          <div class="coupon-card__right">
            <h4 class="coupon-card__name">{{ c.name }}</h4>
            <p class="coupon-card__time">
              有效期：{{ $dayjs.utc(c.startTime).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }} 至{{ $dayjs.utc( c.endTime ).tz('Asia/Shanghai').format('YYYY-MM-DD HH:mm:ss') }}
            </p>
            <p v-if="c.status === 1" class="coupon-card__used">
              使用时间：{{ c.usedAt }}
            </p>
          </div>

          <div class="coupon-card__action">
            <el-button
                v-if="c.status === 0"
                type="primary"
                round
                @click="goUse"
            >去使用</el-button>
            <el-button v-else disabled round>{{ c.tip }}</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.coupon-page {
  max-width: 900px;
  margin: 24px auto 40px;
  padding: 0 16px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A1A;
  margin: 0 0 16px;
  padding-left: 12px;
  position: relative;
}
.page-title::before {
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

.coupon-list {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}
.coupon-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
.coupon-card.disabled,
.coupon-card.gray {
  filter: grayscale(0.6);
  opacity: 0.75;
}

.coupon-card__left {
  background: linear-gradient(135deg, red, red);
  color: #fff;
  width: 140px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}
.coupon-card.gray .coupon-card__left {
  background: linear-gradient(135deg, #AAA, #888);
}
.coupon-card.disabled .coupon-card__left {
  background: linear-gradient(135deg, #AAA, #888);
}
.coupon-card__value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1;
}
.coupon-card__cond {
  font-size: 12px;
  margin-top: 6px;
  opacity: 0.9;
}

.coupon-card__right {
  flex: 1;
  padding: 16px 20px;
  min-width: 0;
}
.coupon-card__name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}
.coupon-card__time,
.coupon-card__used {
  font-size: 12px;
  color: #999;
  margin: 0 0 4px;
}
.coupon-card__progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}
.coupon-card__progress :deep(.el-progress) {
  flex: 1;
}
.coupon-card__remain {
  font-size: 11px;
  color: #3DA67E;
  flex-shrink: 0;
}

.coupon-card__action {
  display: flex;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
}
.coupon-card__action :deep(.el-button--primary) {
  background: #3DA67E;
  border-color: #3DA67E;
}

@media (max-width: 600px) {
  .coupon-card__left {
    width: 110px;
    padding: 12px;
  }
  .coupon-card__value {
    font-size: 22px;
  }
}
</style>