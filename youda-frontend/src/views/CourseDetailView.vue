<template>
  <div class="course-detail-page">
    <div class="course-detail-inner">
      <a-button type="link" class="back-btn" @click="$router.push('/courses')">
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
                    {{ course.requiresPoints ? `${course.pointsCost} 积分解锁` : '免费课程' }}
                  </a-tag>
                </div>
                <h1 class="banner-title">{{ course.name }}</h1>
                <p class="banner-desc">{{ course.description }}</p>
                <div class="banner-meta">
                  <span><user-outlined /> 主讲：{{ course.teacherName || '优答教师' }}</span>
                  <span><play-circle-outlined /> {{ course.videoCount || 0 }} 节课</span>
                  <span><team-outlined /> {{ course.learnCount || 0 }} 人已学习</span>
                </div>
              </div>
              <img
                :src="course.cover || '/course-cover-fallback.svg'"
                class="banner-cover"
                alt="课程封面"
                @error="handleCoverError"
              />
            </div>

            <a-card :bordered="false" class="chapters-card" title="课程目录">
              <div v-if="!chapters.length" class="empty-chapters">课程内容整理中，稍后再来看。</div>
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
                  {{ course.requiresPoints ? `${course.pointsCost} 积分` : '已免费开放' }}
                </div>
                <div class="unlock-sub">
                  {{ course.canLearn ? '当前账号已具备学习权限' : '购买后可解锁本课程全部视频' }}
                </div>
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

              <div class="sidebar-stats">
                <div class="stat-row">
                  <span class="stat-label">课程章节</span>
                  <span class="stat-val">{{ chapters.length }} 章</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">视频课时</span>
                  <span class="stat-val">{{ course.videoCount || 0 }} 节</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">学习人数</span>
                  <span class="stat-val">{{ course.learnCount || 0 }} 人</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">购买状态</span>
                  <span class="stat-val">
                    {{ course.requiresPoints ? (course.purchased ? '已购买' : '未购买') : '免费开放' }}
                  </span>
                </div>
              </div>
            </a-card>
          </aside>
        </div>

        <a-result
          v-else-if="!loading"
          status="404"
          title="课程不存在"
          sub-title="该课程已下架或暂未发布。"
        >
          <template #extra>
            <a-button type="primary" @click="$router.push('/courses')">返回课程列表</a-button>
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
import {
  CheckCircleOutlined,
  FolderOpenOutlined,
  LeftOutlined,
  PlayCircleOutlined,
  PlaySquareOutlined,
  TeamOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { getCourseDetail, purchaseCourse } from '@/api/index.js'

const route = useRoute()
const router = useRouter()
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

const primaryActionText = computed(() => {
  if (!course.value) return '处理中'
  if (course.value.requiresPoints && !course.value.canLearn) {
    return `使用 ${course.value.pointsCost} 积分购买并开始学习`
  }
  return hasStarted.value ? '继续学习' : '开始学习'
})

function goToVideo(videoId) {
  if (!course.value?.id || !videoId) return
  router.push(`/course/${course.value.id}/video/${videoId}`)
}

function handleVideoClick(videoId) {
  if (course.value?.requiresPoints && !course.value?.canLearn) {
    message.warning('请先购买该课程')
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
    if (chapters.value.length > 0) {
      openChapters.value = [chapters.value[0].id]
    }
  } catch {
    course.value = null
    chapters.value = []
  } finally {
    loading.value = false
  }
}

async function handlePrimaryAction() {
  if (!firstVideoId.value) return

  actionLoading.value = true
  try {
    if (course.value?.requiresPoints && !course.value?.canLearn) {
      const result = await purchaseCourse(course.value.id)
      message.success(`购买成功，已扣除 ${result?.pointsCost || course.value.pointsCost} 积分`)
      await loadCourse()
    }
    goToVideo(firstVideoId.value)
  } catch {
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
.start-btn { height: 48px; border-radius: 14px; font-weight: 700; }
.sidebar-stats { margin-top: 18px; display: grid; gap: 14px; }
.stat-row { display: flex; justify-content: space-between; gap: 12px; font-size: 14px; }
.stat-label { color: #6b7280; }
.stat-val { color: #1f2430; font-weight: 700; }
@media (max-width: 960px) {
  .detail-layout { grid-template-columns: 1fr; }
  .course-banner { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .course-detail-inner { padding: 16px; }
}
</style>