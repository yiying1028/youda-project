<template>
  <div class="course-orders-page">
    <div class="course-orders-inner">
      <div class="page-header">
        <div>
          <h1 class="page-title">我的课程订单</h1>
          <p class="page-desc">课程购买后会先生成“已发货”订单，确认收货后才会正式解锁观看。</p>
        </div>
        <a-button @click="router.push('/user')">返回个人中心</a-button>
      </div>

      <a-spin :spinning="loading">
        <a-empty v-if="!loading && orders.length === 0" description="还没有课程订单" />

        <div v-else class="order-list">
          <a-card v-for="order in orders" :key="order.orderId" :bordered="false" class="order-card">
            <div class="order-card__body">
              <img
                :src="order.courseCoverImage || '/course-cover-fallback.svg'"
                :alt="order.courseName"
                class="order-cover"
                @error="handleCoverError"
              />

              <div class="order-main">
                <div class="order-main__top">
                  <div>
                    <div class="course-name">{{ order.courseName }}</div>
                    <div class="order-no">订单号：{{ order.orderNo || `ORDER-${order.orderId}` }}</div>
                  </div>
                  <a-tag :color="order.canLearn ? 'green' : 'gold'">{{ order.canLearn ? '已收货' : '已发货' }}</a-tag>
                </div>

                <div class="order-meta">
                  <span>虚拟币：{{ order.pointsCost }}</span>
                  <span>创建时间：{{ formatTime(order.createTime) }}</span>
                  <span>发货时间：{{ formatTime(order.deliverTime) }}</span>
                  <span>收货时间：{{ formatTime(order.receiveTime) }}</span>
                </div>

                <div class="order-actions">
                  <a-button v-if="!order.canLearn" type="primary" :loading="receivingId === order.orderId" @click="handleReceive(order)">
                    确认收货
                  </a-button>
                  <a-button v-else type="primary" ghost @click="router.push(`/course/${order.courseId}`)">
                    去学习
                  </a-button>
                  <a-button @click="router.push(`/course/${order.courseId}`)">查看课程</a-button>
                </div>
              </div>
            </div>
          </a-card>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { confirmCourseOrderReceived, getMyCourseOrders } from '@/api/index.js'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const receivingId = ref(null)

function formatTime(time) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--'
}

function handleCoverError(event) {
  if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) {
    event.target.src = '/course-cover-fallback.svg'
  }
}

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await getMyCourseOrders()
  } finally {
    loading.value = false
  }
}

async function handleReceive(order) {
  if (!order?.orderId) return
  receivingId.value = order.orderId
  try {
    const updated = await confirmCourseOrderReceived(order.orderId)
    orders.value = orders.value.map((item) => (item.orderId === order.orderId ? { ...item, ...updated } : item))
    message.success('确认收货成功，课程已解锁')
  } finally {
    receivingId.value = null
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.course-orders-page { min-height: calc(100vh - 64px); background: #f5f7fa; padding-bottom: 40px; }
.course-orders-inner { max-width: 1120px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 20px; }
.page-title { margin: 0 0 6px; font-size: 30px; font-weight: 700; color: #1f2430; }
.page-desc { margin: 0; color: #6b7280; line-height: 1.7; }
.order-list { display: grid; gap: 16px; }
.order-card { border-radius: 20px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06); }
.order-card__body { display: grid; grid-template-columns: 180px minmax(0, 1fr); gap: 18px; align-items: start; }
.order-cover { width: 100%; height: 120px; border-radius: 16px; object-fit: cover; background: #e5e7eb; }
.order-main__top { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; }
.course-name { font-size: 20px; font-weight: 700; color: #1f2430; line-height: 1.4; }
.order-no { margin-top: 8px; color: #6b7280; word-break: break-all; }
.order-meta { margin-top: 14px; display: flex; flex-wrap: wrap; gap: 14px; color: #4b5563; font-size: 14px; }
.order-actions { margin-top: 18px; display: flex; flex-wrap: wrap; gap: 10px; }
@media (max-width: 768px) {
  .course-orders-inner { padding: 16px; }
  .page-header { flex-direction: column; }
  .order-card__body { grid-template-columns: 1fr; }
  .order-cover { height: 180px; }
  .order-main__top { flex-direction: column; }
}
</style>
