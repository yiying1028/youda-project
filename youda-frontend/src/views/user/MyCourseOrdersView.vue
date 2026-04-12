<template>
  <div class="course-orders-page">
    <div class="course-orders-inner">
      <div class="page-header">
        <div>
          <h1 class="page-title">课程订单</h1>
          <p class="page-desc">在这里查看课程订单、完成支付，并管理课程学习权限。</p>
        </div>
        <a-button @click="router.push('/user')">返回用户中心</a-button>
      </div>
      <a-spin :spinning="loading">
        <a-empty v-if="!loading && orders.length === 0" description="暂无课程订单" />
        <div v-else class="order-list">
          <a-card v-for="order in orders" :key="order.orderId" :bordered="false" class="order-card">
            <div class="order-card__body">
              <img :src="order.courseCoverImage || '/course-cover-fallback.svg'" :alt="order.courseName" class="order-cover" @error="handleCoverError" />
              <div class="order-main">
                <div class="order-main__top">
                  <div>
                    <div class="course-name">{{ order.courseName }}</div>
                    <div class="order-no">订单号：{{ order.orderNo || `ORDER-${order.orderId}` }}</div>
                  </div>
                  <a-tag :color="statusColor(order.orderStatus)">{{ localStatusLabel(order) }}</a-tag>
                </div>
                <div class="order-meta">
                  <span>订单金额：{{ formatPrice(order.paymentAmount) }}</span>
                  <span>下单时间：{{ formatTime(order.createTime) }}</span>
                  <span>支付时间：{{ formatTime(order.paidTime) }}</span>
                  <span>完成时间：{{ formatTime(order.completedTime) }}</span>
                </div>
                <div class="order-actions">
                  <a-button v-if="order.canPay" type="primary" :loading="payingId === order.orderId" @click="handlePay(order)">立即支付</a-button>
                  <a-button v-if="order.canLearn" type="primary" ghost @click="router.push(`/course/${order.courseId}`)">去学习</a-button>
                  <a-button v-if="order.canComplete" :loading="completingId === order.orderId" @click="handleComplete(order)">完成订单</a-button>
                  <a-button @click="router.push(`/course/${order.courseId}`)">查看课程</a-button>
                </div>
              </div>
            </div>
          </a-card>
        </div>
      </a-spin>
      <a-modal
        v-model:open="paymentSuccessVisible"
        title="支付成功"
        :footer="null"
        centered
        width="420px"
      >
        <div class="payment-success-modal">
          <div class="payment-success-modal__title">订单支付成功，可以开始学习了</div>
          <div class="payment-success-modal__desc">
            <span v-if="paymentSuccessOrderNo">订单号：{{ paymentSuccessOrderNo }}</span>
            <span v-if="paymentSuccessCourseName">课程：{{ paymentSuccessCourseName }}</span>
          </div>
          <div class="payment-success-modal__actions">
            <a-button @click="paymentSuccessVisible = false">稍后学习</a-button>
            <a-button type="primary" :disabled="!paymentSuccessCourseId" @click="handleGoLearnFromSuccess">立即学习</a-button>
          </div>
        </div>
      </a-modal>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { completeCourseOrder, getMyCourseOrders, payCourseOrder } from '@/api/index.js'
const router = useRouter()
const orders = ref([])
const loading = ref(false)
const payingId = ref(null)
const completingId = ref(null)
const paymentSuccessVisible = ref(false)
const paymentSuccessOrderNo = ref('')
const paymentSuccessCourseName = ref('')
const paymentSuccessCourseId = ref(null)
function formatTime(time) { return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--' }
function formatPrice(value) { return `￥${Number(value || 0).toFixed(2)}` }
function statusColor(status) { if (status === 0) return 'orange'; if (status === 1) return 'blue'; if (status === 2) return 'green'; return 'default' }
function localStatusLabel(order) { if (order.orderStatus === 0) return '待支付'; if (order.orderStatus === 1) return '已支付'; if (order.orderStatus === 2) return '已完成'; return order.orderStatusLabel || '--' }
function handleCoverError(event) { if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) event.target.src = '/course-cover-fallback.svg' }
function handleGoLearnFromSuccess() { paymentSuccessVisible.value = false; if (paymentSuccessCourseId.value) router.push(`/course/${paymentSuccessCourseId.value}`) }
async function loadOrders() { loading.value = true; try { orders.value = await getMyCourseOrders() } finally { loading.value = false } }
async function handlePay(order) { if (!order?.orderId) return; payingId.value = order.orderId; try { const updated = await payCourseOrder(order.orderId); orders.value = orders.value.map((item) => item.orderId === order.orderId ? { ...item, ...updated } : item); paymentSuccessOrderNo.value = updated?.orderNo || order.orderNo || ''; paymentSuccessCourseName.value = order.courseName || ''; paymentSuccessCourseId.value = order.courseId || updated?.courseId || null; message.success('支付成功'); paymentSuccessVisible.value = true } finally { payingId.value = null } }
async function handleComplete(order) { if (!order?.orderId) return; completingId.value = order.orderId; try { const updated = await completeCourseOrder(order.orderId); orders.value = orders.value.map((item) => item.orderId === order.orderId ? { ...item, ...updated } : item); message.success('订单已完成') } finally { completingId.value = null } }
onMounted(() => { loadOrders() })
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
.payment-success-modal__title { font-size: 18px; font-weight: 700; color: #1f2430; }
.payment-success-modal__desc { margin-top: 12px; display: grid; gap: 6px; color: #6b7280; line-height: 1.6; }
.payment-success-modal__actions { margin-top: 24px; display: flex; justify-content: flex-end; gap: 10px; }
@media (max-width: 768px) { .course-orders-inner { padding: 16px; } .page-header { flex-direction: column; } .order-card__body { grid-template-columns: 1fr; } .order-cover { height: 180px; } .order-main__top { flex-direction: column; } }
</style>
