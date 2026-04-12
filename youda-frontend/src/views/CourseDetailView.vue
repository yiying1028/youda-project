<template>
  <div class="course-detail-page">
    <div class="course-detail-inner">
      <a-button type="link" class="back-btn" @click="router.push('/courses')">
        <left-outlined /> 返回课程列表
      </a-button>

      <a-spin :spinning="loading || actionLoading">
        <div v-if="course" class="detail-layout">
          <div class="course-main">
            <div class="course-banner">
              <div class="banner-content">
                <div class="banner-tags">
                  <a-tag color="blue">{{ course.subjectName }}</a-tag>
                  <a-tag>{{ course.gradeName }}</a-tag>
                  <a-tag :color="course.requiresPoints ? 'orange' : 'green'">
                    {{ course.requiresPoints ? `${course.pointsCost} 虚拟币课程` : '免费课程' }}
                  </a-tag>
                  <a-tag v-if="course.requiresPoints && course.purchased" :color="course.canLearn ? 'green' : 'gold'">
                    {{ purchaseStatusText }}
                  </a-tag>
                </div>
                <h1 class="banner-title">{{ course.name }}</h1>
                <p class="banner-desc">{{ course.description || '课程简介暂未补充。' }}</p>
                <div class="banner-meta">
                  <span><user-outlined /> 主讲：{{ course.teacherName || '优答教师' }}</span>
                  <span><play-circle-outlined /> {{ course.videoCount || 0 }} 节课</span>
                  <span><team-outlined /> {{ course.learnCount || 0 }} 人学习</span>
                </div>
              </div>
              <img
                :src="course.cover || '/course-cover-fallback.svg'"
                class="banner-cover"
                alt="课程封面"
                @error="handleCoverError"
              />
            </div>

            <a-alert
              v-if="course.requiresPoints && !course.purchased"
              type="info"
              show-icon
              class="order-alert"
              message="购买后会生成课程订单"
              description="课程购买只扣虚拟币，不扣积分。系统先自动发货，确认收货后才会解锁观看。"
            />

            <a-alert
              v-else-if="course.pendingReceive"
              type="warning"
              show-icon
              class="order-alert"
              message="课程订单已发货，待确认收货"
              :description="pendingReceiveDescription"
            />

            <a-alert
              v-else-if="course.requiresPoints && course.canLearn"
              type="success"
              show-icon
              class="order-alert"
              message="课程已解锁"
              :description="receivedDescription"
            />

            <a-card :bordered="false" class="chapters-card" title="课程目录">
              <div v-if="!chapters.length" class="empty-chapters">课程内容整理中，稍后再来查看。</div>
              <a-collapse v-else v-model:activeKey="openChapters" :bordered="false" class="chapter-collapse">
                <a-collapse-panel v-for="chapter in chapters" :key="chapter.id" class="chapter-panel">
                  <template #header>
                    <div class="chapter-header">
                      <folder-open-outlined class="chapter-icon" />
                      <span class="chapter-title">{{ chapter.title }}</span>
                      <span class="chapter-count">{{ chapter.videos?.length || 0 }} 节</span>
                    </div>
                  </template>

                  <div
                    v-for="video in chapter.videos"
                    :key="video.id"
                    class="video-item"
                    :class="{ locked: course.requiresPoints && !course.canLearn }"
                    @click="handleVideoClick(video.id)"
                  >
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
              <div class="unlock-panel" :class="{ paid: course.requiresPoints }">
                <div class="unlock-label">学习权限</div>
                <div class="unlock-value">
                  {{ course.requiresPoints ? `${course.pointsCost} 虚拟币` : '免费开放' }}
                </div>
                <div class="unlock-sub">{{ accessDescription }}</div>
              </div>

              <div v-if="userStore.isLoggedIn && course.requiresPoints" class="balance-panel">
                <span>虚拟币余额</span>
                <strong>{{ userStore.userInfo?.virtualBalance ?? 0 }}</strong>
              </div>

              <a-button
                type="primary"
                size="large"
                block
                :disabled="!firstVideoId"
                @click="handlePrimaryAction"
                class="start-btn"
              >
                <play-circle-outlined />
                {{ primaryActionText }}
              </a-button>

              <a-button
                v-if="course.requiresPoints && course.purchased"
                block
                class="order-btn"
                @click="router.push('/user/course-orders')"
              >
                查看课程订单
              </a-button>

              <div class="sidebar-stats">
                <div class="stat-row">
                  <span class="stat-label">课程章节</span>
                  <span class="stat-val">{{ chapters.length }} 章</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">视频数量</span>
                  <span class="stat-val">{{ course.videoCount || 0 }} 节</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">学习人数</span>
                  <span class="stat-val">{{ course.learnCount || 0 }} 人</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">购买状态</span>
                  <span class="stat-val">{{ purchaseStatusText }}</span>
                </div>
                <div v-if="course.orderNo" class="stat-row">
                  <span class="stat-label">订单号</span>
                  <span class="stat-val stat-order-no">{{ course.orderNo }}</span>
                </div>
                <div v-if="course.orderDeliverTime" class="stat-row">
                  <span class="stat-label">发货时间</span>
                  <span class="stat-val">{{ formatTime(course.orderDeliverTime) }}</span>
                </div>
                <div v-if="course.orderReceiveTime" class="stat-row">
                  <span class="stat-label">收货时间</span>
                  <span class="stat-val">{{ formatTime(course.orderReceiveTime) }}</span>
                </div>
              </div>
            </a-card>
          </aside>
        </div>

        <a-result
          v-else-if="!loading"
          status="404"
          title="课程不存在"
          sub-title="该课程可能已下架，或者暂未发布。"
        >
          <template #extra>
            <a-button type="primary" @click="router.push('/courses')">返回课程列表</a-button>
          </template>
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
import {
  CheckCircleOutlined,
  FolderOpenOutlined,
  LeftOutlined,
  PlayCircleOutlined,
  PlaySquareOutlined,
  TeamOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { confirmCourseOrderReceived, getCourseDetail, purchaseCourse } from '@/api/index.js'
import { useUserStore } from '@/stores/user.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const course = ref(null)
const chapters = ref([])
const loading = ref(true)
const actionLoading = ref(false)
const openChapters = ref([])

const firstVideoId = computed(() => {
  for (const chapter of chapters.value) {
    if (chapter.videos?.length > 0) {
      return chapter.videos[0].id
    }
  }
  return null
})

const hasStarted = computed(() => {
  const progress = course.value?.progress
  return Boolean(progress && progress.completedCount > 0)
})

const accessDescription = computed(() => {
  if (!course.value) return ''
  if (!course.value.requiresPoints) return '当前账号可直接开始学习。'
  if (!course.value.purchased) return '购买后会生成订单，确认收货后才会解锁视频。'
  if (course.value.pendingReceive) return '订单已发货，请先确认收货，再开始观看课程。'
  return '当前账号已完成收货，可以观看全部视频。'
})

const purchaseStatusText = computed(() => {
  if (!course.value) return '--'
  if (!course.value.requiresPoints) return '免费开放'
  if (!course.value.purchased) return '未购买'
  return course.value.canLearn ? '已收货' : '已发货'
})

const pendingReceiveDescription = computed(() => {
  if (!course.value?.orderNo) {
    return '请先确认收货，确认后即可解锁学习。'
  }
  return `订单号 ${course.value.orderNo} 已发货，确认收货后即可开始学习。`
})

const receivedDescription = computed(() => {
  const receiveTime = course.value?.orderReceiveTime ? formatTime(course.value.orderReceiveTime) : ''
  return receiveTime ? `已于 ${receiveTime} 确认收货，现在可以正常观看课程。` : '订单已完成收货，现在可以正常观看课程。'
})

const primaryActionText = computed(() => {
  if (!course.value) return '处理中...'
  if (course.value.requiresPoints && !course.value.purchased) {
    return `使用 ${course.value.pointsCost} 虚拟币购买`
  }
  if (course.value.pendingReceive) {
    return '确认收货并解锁'
  }
  return hasStarted.value ? '继续学习' : '开始学习'
})

function goToVideo(videoId) {
  if (!course.value?.id || !videoId) return
  router.push(`/course/${course.value.id}/video/${videoId}`)
}

function handleVideoClick(videoId) {
  if (course.value?.requiresPoints && !course.value?.canLearn) {
    message.warning(course.value.pendingReceive ? '课程订单已发货，请先确认收货' : '请先购买该课程')
    return
  }
  goToVideo(videoId)
}

function formatDuration(seconds) {
  if (!seconds) return ''
  const minutes = Math.floor(seconds / 60)
  const remain = Math.floor(seconds % 60)
  return `${minutes}:${remain.toString().padStart(2, '0')}`
}

function formatTime(time) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--'
}

function handleCoverError(event) {
  if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) {
    event.target.src = '/course-cover-fallback.svg'
  }
}

async function loadCourse() {
  loading.value = true
  try {
    const data = await getCourseDetail(route.params.id)
    course.value = data
    chapters.value = data?.chapters || []
    openChapters.value = chapters.value.length > 0 ? [chapters.value[0].id] : []
  } catch {
    course.value = null
    chapters.value = []
    openChapters.value = []
  } finally {
    loading.value = false
  }
}

async function handlePrimaryAction() {
  if (!course.value) return

  actionLoading.value = true
  try {
    if (course.value.requiresPoints && !course.value.purchased) {
      const result = await purchaseCourse(course.value.id)
      if (userStore.isLoggedIn) {
        await userStore.fetchUserInfo().catch(() => {})
      }
      message.success(`购买成功，已扣除 ${result?.pointsCost || course.value.pointsCost} 虚拟币，订单已发货`)
      await loadCourse()
      return
    }

    if (course.value.pendingReceive && course.value.orderId) {
      await confirmCourseOrderReceived(course.value.orderId)
      message.success('确认收货成功，课程已解锁')
      await loadCourse()
      if (firstVideoId.value) {
        goToVideo(firstVideoId.value)
      }
      return
    }

    if (firstVideoId.value) {
      goToVideo(firstVideoId.value)
    }
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  loadCourse()
})
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
.balance-panel { margin-bottom: 14px; padding: 14px 16px; border-radius: 14px; background: #f8fafc; color: #475569; display: flex; align-items: center; justify-content: space-between; }
.balance-panel strong { color: #111827; font-size: 18px; }
.start-btn, .order-btn { height: 48px; border-radius: 14px; font-weight: 700; }
.order-btn { margin-top: 10px; }
.sidebar-stats { margin-top: 18px; display: grid; gap: 14px; }
.stat-row { display: flex; justify-content: space-between; gap: 12px; font-size: 14px; }
.stat-label { color: #6b7280; }
.stat-val { color: #1f2430; font-weight: 700; text-align: right; }
.stat-order-no { font-size: 12px; word-break: break-all; max-width: 150px; }
@media (max-width: 960px) {
  .detail-layout { grid-template-columns: 1fr; }
  .course-banner { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .course-detail-inner { padding: 16px; }
}
</style>
