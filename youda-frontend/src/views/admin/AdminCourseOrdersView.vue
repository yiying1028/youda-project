<template>
  <div class="admin-course-orders-page">
    <div class="page-top">
      <h2 class="page-title">课程订单管理</h2>
      <div class="page-actions">
        <a-select v-model:value="filters.status" style="width: 160px" allow-clear placeholder="全部状态" @change="handleSearch">
          <a-select-option :value="0">待支付</a-select-option>
          <a-select-option :value="1">已支付</a-select-option>
          <a-select-option :value="2">已完成</a-select-option>
        </a-select>
        <a-input-search v-model:value="filters.keyword" placeholder="搜索订单号、用户或课程" style="width: 260px" allow-clear @search="handleSearch" />
      </div>
    </div>

    <a-card :bordered="false" class="table-card">
      <a-table :columns="columns" :data-source="orders" :loading="loading" :pagination="pagination" row-key="orderId" @change="handleTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'"><div class="user-cell"><div class="user-name">{{ record.nickname || '未命名用户' }}</div><div class="user-sub">@{{ record.username || '--' }}</div></div></template>
          <template v-else-if="column.key === 'course'"><div class="course-cell"><img :src="record.courseCoverImage || '/course-cover-fallback.svg'" class="course-cover" alt="课程封面" @error="handleCoverError" /><div class="course-main"><div class="course-name">{{ record.courseName }}</div><div class="course-sub">订单号：{{ record.orderNo }}</div></div></div></template>
          <template v-else-if="column.key === 'status'"><a-tag :color="statusColor(record.orderStatus)">{{ record.orderStatusLabel || localStatusLabel(record.orderStatus) }}</a-tag></template>
          <template v-else-if="column.key === 'amount'">{{ formatPrice(record.paymentAmount) }}</template>
          <template v-else-if="column.key === 'time'"><div class="time-cell"><div>下单：{{ formatTime(record.createTime) }}</div><div>支付：{{ formatTime(record.paidTime) }}</div><div>完成：{{ formatTime(record.completedTime) }}</div></div></template>
          <template v-else-if="column.key === 'action'"><div class="action-row"><a-button v-if="record.canPay" type="primary" size="small" :loading="payingId === record.orderId" @click="handlePay(record)">标记支付</a-button><a-button v-if="record.canComplete" size="small" :loading="completingId === record.orderId" @click="handleComplete(record)">完成订单</a-button><a-button size="small" @click="goCourse(record.courseId)">查看课程</a-button></div></template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { adminCompleteCourseOrder, adminPayCourseOrder, getAdminCourseOrders } from '@/api/index.js'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const payingId = ref(null)
const completingId = ref(null)
const filters = reactive({ status: undefined, keyword: '' })
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showQuickJumper: true, showTotal: (total) => `共 ${total} 条` })
const columns = [
  { title: '下单用户', key: 'user', width: 180 },
  { title: '课程信息', key: 'course', width: 320 },
  { title: '订单状态', key: 'status', width: 120 },
  { title: '订单金额', key: 'amount', width: 120 },
  { title: '时间信息', key: 'time', width: 260 },
  { title: '操作', key: 'action', width: 220 }
]

function formatPrice(value) { return `￥${Number(value || 0).toFixed(2)}` }
function formatTime(value) { return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--' }
function statusColor(status) { if (status === 0) return 'orange'; if (status === 1) return 'blue'; if (status === 2) return 'green'; return 'default' }
function localStatusLabel(status) { if (status === 0) return '待支付'; if (status === 1) return '已支付'; if (status === 2) return '已完成'; return '--' }
function handleCoverError(event) { if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) event.target.src = '/course-cover-fallback.svg' }
function goCourse(courseId) { if (courseId) router.push(`/course/${courseId}`) }

async function loadOrders() {
  loading.value = true
  try {
    const data = await getAdminCourseOrders({ current: pagination.current, size: pagination.pageSize, status: filters.status, keyword: filters.keyword || undefined })
    orders.value = data?.records || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.current = 1; loadOrders() }
function handleTableChange(pag) { pagination.current = pag.current; loadOrders() }
async function handlePay(record) {
  if (!record?.orderId) return
  payingId.value = record.orderId
  try {
    const updated = await adminPayCourseOrder(record.orderId)
    orders.value = orders.value.map((item) => (item.orderId === record.orderId ? { ...item, ...updated } : item))
    message.success('订单已支付')
  } finally { payingId.value = null }
}
async function handleComplete(record) {
  if (!record?.orderId) return
  completingId.value = record.orderId
  try {
    const updated = await adminCompleteCourseOrder(record.orderId)
    orders.value = orders.value.map((item) => (item.orderId === record.orderId ? { ...item, ...updated } : item))
    message.success('订单已完成')
  } finally { completingId.value = null }
}

onMounted(() => { loadOrders() })
</script>

<style scoped>
.admin-course-orders-page { display: flex; flex-direction: column; gap: 16px; }
.page-top { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.page-title { margin: 0; color: #1f2937; font-size: 22px; font-weight: 700; }
.page-actions { display: flex; align-items: center; gap: 12px; }
.table-card { border-radius: 18px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06); }
.user-name { color: #111827; font-weight: 600; }
.user-sub { margin-top: 4px; color: #6b7280; font-size: 12px; }
.course-cell { display: flex; align-items: center; gap: 12px; }
.course-cover { width: 72px; height: 48px; border-radius: 10px; object-fit: cover; background: #f3f4f6; flex-shrink: 0; }
.course-name { color: #111827; font-weight: 600; }
.course-sub { margin-top: 4px; color: #6b7280; font-size: 12px; word-break: break-all; }
.time-cell { display: grid; gap: 4px; color: #4b5563; font-size: 12px; }
.action-row { display: flex; flex-wrap: wrap; gap: 8px; }
@media (max-width: 768px) { .page-top, .page-actions { flex-direction: column; align-items: stretch; } }
</style>
