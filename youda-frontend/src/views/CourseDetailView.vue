<template>
  <div class="course-detail-page">
    <div class="course-detail-inner">
      <a-button type="link" class="back-btn" @click="router.push('/courses')">
        <left-outlined /> 返回课程中心
      </a-button>
      <a-spin :spinning="loading || actionLoading">
        <div v-if="course" class="detail-layout">
          <div class="course-main">
            <div class="course-banner">
              <div class="banner-content">
                <div class="banner-tags">
                  <a-tag color="blue">{{ course.subjectName }}</a-tag>
                  <a-tag>{{ course.gradeName }}</a-tag>
                  <a-tag :color="course.requiresPurchase ? 'orange' : 'green'">{{ course.requiresPurchase ? `${formatPrice(course.priceAmount)} 订单课` : '免费课程' }}</a-tag>
                  <a-tag v-if="course.requiresPurchase && course.hasOrder" :color="statusColor(course.orderStatus)">{{ purchaseStatusText }}</a-tag>
                </div>
                <h1 class="banner-title">{{ course.name }}</h1>
                <p class="banner-desc">{{ course.description || '课程简介待补充。' }}</p>
                <div class="banner-meta">
                  <span><user-outlined /> 讲师：{{ course.teacherName || '暂无讲师' }}</span>
                  <span><play-circle-outlined /> {{ course.videoCount || 0 }} 个视频</span>
                  <span><team-outlined /> {{ course.learnCount || 0 }} 人学习</span>
                </div>
              </div>
              <img :src="course.cover || '/course-cover-fallback.svg'" class="banner-cover" alt="课程封面" @error="handleCoverError" />
            </div>
            <a-alert v-if="course.requiresPurchase && !course.hasOrder" type="info" show-icon class="order-alert" message="该课程通过订单购买" description="先创建课程订单，再完成支付即可立即解锁课程，课程购买不扣积分。" />
            <a-alert v-else-if="course.requiresPurchase && course.canPay" type="warning" show-icon class="order-alert" message="订单待支付" :description="pendingPaymentDescription" />
            <a-alert v-else-if="course.requiresPurchase && course.canLearn" type="success" show-icon class="order-alert" message="课程已解锁" :description="paidDescription" />
            <a-card :bordered="false" class="chapters-card" title="课程内容">
              <div v-if="!chapters.length" class="empty-chapters">暂无章节内容。</div>
              <a-collapse v-else v-model:activeKey="openChapters" :bordered="false" class="chapter-collapse">
                <a-collapse-panel v-for="chapter in chapters" :key="chapter.id" class="chapter-panel">
                  <template #header>
                    <div class="chapter-header">
                      <folder-open-outlined class="chapter-icon" />
                      <span class="chapter-title">{{ chapter.title }}</span>
                      <span class="chapter-count">{{ chapter.videos?.length || 0 }} 个视频</span>
                    </div>
                  </template>
                  <div v-for="video in chapter.videos" :key="video.id" class="video-item" :class="{ locked: course.requiresPurchase && !course.canLearn }" @click="handleVideoClick(video.id)">
                    <play-square-outlined class="video-icon" />
                    <span class="video-title">{{ video.title }}</span>
                    <span v-if="video.duration" class="video-duration">{{ formatDuration(video.duration) }}</span>
                    <check-circle-outlined v-if="video.isFinished" class="finished-icon" />
                  </div>
                </a-collapse-panel>
              </a-collapse>
            </a-card>
          </div>
          <aside class="course-sidebar">
            <a-card :bordered="false" class="sidebar-card">
              <div class="unlock-panel" :class="{ paid: course.requiresPurchase }">
                <div class="unlock-label">课程价格</div>
                <div class="unlock-value">{{ course.requiresPurchase ? formatPrice(course.priceAmount) : '免费' }}</div>
                <div class="unlock-sub">{{ accessDescription }}</div>
              </div>
              <a-button type="primary" size="large" block :disabled="course.canLearn && !firstVideoId" @click="handlePrimaryAction" class="start-btn">
                <play-circle-outlined /> {{ primaryActionText }}
              </a-button>
              <a-button v-if="course.requiresPurchase && course.hasOrder" block class="order-btn" @click="router.push('/user/course-orders')">查看课程订单</a-button>
              <div class="sidebar-stats">
                <div class="stat-row"><span class="stat-label">章节数</span><span class="stat-val">{{ chapters.length }}</span></div>
                <div class="stat-row"><span class="stat-label">视频数</span><span class="stat-val">{{ course.videoCount || 0 }}</span></div>
                <div class="stat-row"><span class="stat-label">学习人数</span><span class="stat-val">{{ course.learnCount || 0 }}</span></div>
                <div class="stat-row"><span class="stat-label">订单状态</span><span class="stat-val">{{ purchaseStatusText }}</span></div>
                <div v-if="course.orderNo" class="stat-row"><span class="stat-label">订单号</span><span class="stat-val stat-order-no">{{ course.orderNo }}</span></div>
                <div v-if="course.orderCreateTime" class="stat-row"><span class="stat-label">下单时间</span><span class="stat-val">{{ formatTime(course.orderCreateTime) }}</span></div>
                <div v-if="course.orderPaidTime" class="stat-row"><span class="stat-label">支付时间</span><span class="stat-val">{{ formatTime(course.orderPaidTime) }}</span></div>
                <div v-if="course.orderCompletedTime" class="stat-row"><span class="stat-label">完成时间</span><span class="stat-val">{{ formatTime(course.orderCompletedTime) }}</span></div>
              </div>
            </a-card>
          </aside>
        </div>
        <a-result v-else-if="!loading" status="404" title="课程不存在" sub-title="课程可能已下线，或暂未发布。">
          <template #extra><a-button type="primary" @click="router.push('/courses')">返回课程中心</a-button></template>
        </a-result>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { CheckCircleOutlined, FolderOpenOutlined, LeftOutlined, PlayCircleOutlined, PlaySquareOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue'
import { completeCourseOrder, getCourseDetail, payCourseOrder, purchaseCourse } from '@/api/index.js'
const route = useRoute()
const router = useRouter()
const course = ref(null)
const chapters = ref([])
const loading = ref(true)
const actionLoading = ref(false)
const openChapters = ref([])
const firstVideoId = computed(() => { for (const chapter of chapters.value) { if (chapter.videos?.length > 0) return chapter.videos[0].id } return null })
const hasStarted = computed(() => Boolean(course.value?.progress && course.value.progress.completedCount > 0))
const accessDescription = computed(() => {
  if (!course.value) return ''
  if (!course.value.requiresPurchase) return '当前可直接开始学习。'
  if (!course.value.hasOrder) return '先创建订单并支付后即可解锁全部视频。'
  if (course.value.canPay) return '订单已创建，完成支付后即可解锁课程。'
  if (course.value.canComplete) return '订单已支付，课程已开放学习。'
  return '订单已完成，课程保持解锁状态。'
})
const purchaseStatusText = computed(() => {
  if (!course.value) return '--'
  if (!course.value.requiresPurchase) return '免费'
  if (!course.value.hasOrder) return '未下单'
  if (course.value.orderStatus === 0) return '待支付'
  if (course.value.orderStatus === 1) return '已支付'
  if (course.value.orderStatus === 2) return '已完成'
  return course.value.orderStatusLabel || '--'
})
const pendingPaymentDescription = computed(() => course.value?.orderNo ? `订单 ${course.value.orderNo} 待支付，支付后即可立即学习。` : '请先完成支付以解锁课程。')
const paidDescription = computed(() => course.value?.orderPaidTime ? `已于 ${formatTime(course.value.orderPaidTime)} 完成支付。` : '订单支付成功，课程已解锁。')
const primaryActionText = computed(() => {
  if (!course.value) return '处理中...'
  if (course.value.requiresPurchase && !course.value.hasOrder) return `立即下单 ${formatPrice(course.value.priceAmount)}`
  if (course.value.canPay) return '立即支付'
  return hasStarted.value ? '继续学习' : '开始学习'
})
function formatPrice(value) { return `￥${Number(value || 0).toFixed(2)}` }
function statusColor(status) { if (status === 0) return 'orange'; if (status === 1) return 'blue'; if (status === 2) return 'green'; return 'default' }
function goToVideo(videoId) { if (!course.value?.id || !videoId) return; router.push(`/course/${course.value.id}/video/${videoId}`) }
function handleVideoClick(videoId) { if (course.value?.requiresPurchase && !course.value?.canLearn) { message.warning(course.value.canPay ? '请先完成支付' : '请先下单并支付课程订单'); return } goToVideo(videoId) }
function formatDuration(seconds) { if (!seconds) return ''; const minutes = Math.floor(seconds / 60); const remain = Math.floor(seconds % 60); return `${minutes}:${remain.toString().padStart(2, '0')}` }
function formatTime(time) { return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--' }
function handleCoverError(event) { if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) event.target.src = '/course-cover-fallback.svg' }
async function loadCourse() { loading.value = true; try { const data = await getCourseDetail(route.params.id); course.value = data; chapters.value = data?.chapters || []; openChapters.value = chapters.value.length > 0 ? [chapters.value[0].id] : [] } catch { course.value = null; chapters.value = []; openChapters.value = [] } finally { loading.value = false } }
async function handlePrimaryAction() {
  if (!course.value) return
  actionLoading.value = true
  try {
    if (course.value.requiresPurchase && !course.value.hasOrder) {
      const result = await purchaseCourse(course.value.id)
      message.success(`课程订单已创建：${result?.orderNo || ''}`)
      await loadCourse()
      return
    }
    if (course.value.canPay && course.value.orderId) {
      await payCourseOrder(course.value.orderId)
      message.success('支付成功')
      await loadCourse()
      if (firstVideoId.value) goToVideo(firstVideoId.value)
      return
    }
    if (course.value.canComplete && course.value.orderId) await completeCourseOrder(course.value.orderId).catch(() => null)
    if (firstVideoId.value) goToVideo(firstVideoId.value)
  } finally { actionLoading.value = false }
}
onMounted(() => { loadCourse() })
</script>

<style scoped>
.course-detail-page { min-height: calc(100vh - 64px); background: #f5f7fa; padding-bottom: 40px; }
.course-detail-inner { max-width: 1120px; margin: 0 auto; padding: 24px; }
.back-btn { padding-left: 0; color: #6b7280; margin-bottom: 20px; }
.detail-layout { display: grid; grid-template-columns: minmax(0, 1fr) 320px; gap: 20px; align-items: start; }
.course-banner, .chapters-card, .sidebar-card { background: #fff; border-radius: 22px; box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08); }
.course-banner { padding: 28px; display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 24px; margin-bottom: 20px; }
.banner-title { margin: 14px 0 12px; font-size: clamp(28px, 3vw, 40px); line-height: 1.15; color: #1f2430; }
.banner-desc { margin: 0; color: #5d6876; line-height: 1.8; }
.banner-meta { margin-top: 16px; display: flex; flex-wrap: wrap; gap: 14px; color: #4b5563; }
.banner-cover { width: 100%; height: 220px; border-radius: 18px; object-fit: cover; background: #efe3d0; }
.order-alert { margin-bottom: 20px; border-radius: 16px; }
.empty-chapters { color: #6b7280; padding: 12px 0 4px; }
.chapter-header { display: flex; align-items: center; gap: 10px; }
.chapter-title { flex: 1; font-weight: 700; color: #1f2430; }
.chapter-count { color: #6b7280; font-size: 13px; }
.video-item { display: flex; align-items: center; gap: 10px; padding: 12px 4px; border-bottom: 1px solid #f1f5f9; cursor: pointer; }
.video-item:last-child { border-bottom: 0; }
.video-item:hover { color: #1677ff; }
.video-item.locked { opacity: 0.65; }
.video-title { flex: 1; }
.video-duration, .finished-icon { color: #6b7280; font-size: 13px; }
.sidebar-card { padding: 8px; }
.unlock-panel { border-radius: 18px; padding: 18px; background: linear-gradient(180deg, #edf7ed, #ffffff); margin-bottom: 16px; }
.unlock-panel.paid { background: linear-gradient(180deg, #fff3e8, #ffffff); }
.unlock-label { color: #8c8c8c; font-size: 13px; }
.unlock-value { margin-top: 8px; font-size: 28px; font-weight: 700; color: #1f2430; }
.unlock-sub { margin-top: 8px; color: #6b7280; font-size: 13px; line-height: 1.6; }
.start-btn, .order-btn { height: 48px; border-radius: 14px; font-weight: 700; }
.order-btn { margin-top: 10px; }
.sidebar-stats { margin-top: 18px; display: grid; gap: 14px; }
.stat-row { display: flex; justify-content: space-between; gap: 12px; font-size: 14px; }
.stat-label { color: #6b7280; }
.stat-val { color: #1f2430; font-weight: 700; text-align: right; }
.stat-order-no { font-size: 12px; word-break: break-all; max-width: 150px; }
@media (max-width: 960px) { .detail-layout { grid-template-columns: 1fr; } .course-banner { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .course-detail-inner { padding: 16px; } }
</style>
