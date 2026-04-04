<template>
  <div class="video-play-page">
    <div class="play-header">
      <a-button type="link" class="back-btn" @click="$router.push('/course/' + courseId)">
        <left-outlined /> 返回课程
      </a-button>
      <div class="play-title">{{ videoInfo?.title || '视频加载中...' }}</div>
    </div>

    <div class="play-layout">
      <div class="player-section">
        <a-spin :spinning="videoLoading">
          <video
            v-if="videoInfo?.url"
            ref="videoRef"
            :src="videoInfo.url"
            class="video-player"
            controls
            controlsList="nodownload"
            @timeupdate="handleTimeUpdate"
            @loadedmetadata="handleVideoLoaded"
            @ended="handleVideoEnded"
          >
            您的浏览器不支持视频播放。
          </video>
          <div v-else class="video-placeholder">
            <loading-outlined class="placeholder-icon" />
            <span>视频加载中...</span>
          </div>
        </a-spin>

        <div v-if="videoInfo" class="video-info-bar">
          <div class="video-meta">
            <h2 class="vinfo-title">{{ videoInfo.title }}</h2>
            <p class="vinfo-desc">{{ videoInfo.description || '跟着课程节奏完成本节学习。' }}</p>
          </div>
          <div class="nav-btns">
            <a-button :disabled="!prevVideoId" @click="goToVideo(prevVideoId)">
              <left-outlined /> 上一节
            </a-button>
            <a-button :disabled="!nextVideoId" type="primary" @click="goToVideo(nextVideoId)">
              下一节 <right-outlined />
            </a-button>
          </div>
        </div>
      </div>

      <aside class="playlist-section">
        <div class="playlist-header">课程目录</div>
        <div class="playlist-body">
          <a-skeleton :loading="courseLoading" active>
            <template v-for="chapter in chapters" :key="chapter.id">
              <div class="chapter-group">
                <div class="chapter-name">{{ chapter.title }}</div>
                <div
                  v-for="video in chapter.videos"
                  :key="video.id"
                  class="playlist-item"
                  :class="{ active: currentVideoId === video.id, finished: video.isFinished }"
                  @click="goToVideo(video.id)"
                >
                  <play-circle-outlined class="item-icon" />
                  <span class="item-title">{{ video.title }}</span>
                  <check-circle-outlined v-if="video.isFinished" class="item-check" />
                </div>
              </div>
            </template>
          </a-skeleton>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getVideoInfo, getCourseDetail, updateVideoProgress } from '@/api/index.js'
import {
  LeftOutlined,
  RightOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  LoadingOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()

const courseId = computed(() => Number(route.params.id))
const currentVideoId = computed(() => Number(route.params.vid))

const videoRef = ref(null)
const videoInfo = ref(null)
const videoLoading = ref(true)
const chapters = ref([])
const courseLoading = ref(true)
const activeVideoId = ref(null)

let progressTimer = null

const flatVideos = computed(() => {
  const list = []
  for (const chapter of chapters.value) {
    for (const video of chapter.videos || []) {
      list.push(video)
    }
  }
  return list
})

const currentIndex = computed(() => flatVideos.value.findIndex((video) => video.id === currentVideoId.value))

const prevVideoId = computed(() => {
  if (currentIndex.value > 0) {
    return flatVideos.value[currentIndex.value - 1].id
  }
  return null
})

const nextVideoId = computed(() => {
  if (currentIndex.value >= 0 && currentIndex.value < flatVideos.value.length - 1) {
    return flatVideos.value[currentIndex.value + 1].id
  }
  return null
})

function isValidId(value) {
  return Number.isFinite(value) && value > 0
}

function goToVideo(videoId) {
  if (!isValidId(videoId) || !isValidId(courseId.value)) return
  saveProgress()
  router.push(`/course/${courseId.value}/video/${videoId}`)
}

function handleVideoLoaded() {
  if (videoInfo.value?.progress && videoRef.value) {
    videoRef.value.currentTime = videoInfo.value.progress
  }
}

function handleTimeUpdate() {
}

function handleVideoEnded() {
  saveProgress(true)
  if (nextVideoId.value) {
    setTimeout(() => goToVideo(nextVideoId.value), 1500)
  }
}

async function saveProgress(finished = false) {
  const videoId = isValidId(activeVideoId.value) ? activeVideoId.value : currentVideoId.value
  if (!videoRef.value || !videoInfo.value || !isValidId(videoId)) return
  try {
    await updateVideoProgress(videoId, {
      position: finished
        ? Math.floor(videoRef.value.duration || videoRef.value.currentTime || 0)
        : Math.floor(videoRef.value.currentTime || 0),
      duration: Math.floor(videoRef.value.duration || 0)
    })
  } catch {
  }
}

async function loadVideo() {
  if (!isValidId(currentVideoId.value)) {
    activeVideoId.value = null
    videoInfo.value = null
    videoLoading.value = false
    return
  }

  videoLoading.value = true
  videoInfo.value = null
  try {
    const data = await getVideoInfo(currentVideoId.value)
    activeVideoId.value = currentVideoId.value
    videoInfo.value = data
  } catch {
    videoInfo.value = null
    message.warning('当前课程尚未解锁，已返回课程详情页')
    if (isValidId(courseId.value)) {
      router.replace(`/course/${courseId.value}`)
    }
  } finally {
    videoLoading.value = false
  }
}

async function loadCourse() {
  if (!isValidId(courseId.value)) {
    chapters.value = []
    courseLoading.value = false
    return
  }

  courseLoading.value = true
  try {
    const data = await getCourseDetail(courseId.value)
    chapters.value = data?.chapters || []
  } finally {
    courseLoading.value = false
  }
}

onMounted(() => {
  loadVideo()
  loadCourse()
  progressTimer = setInterval(() => saveProgress(), 30000)
})

watch(currentVideoId, (newId, oldId) => {
  if (!isValidId(newId) || newId === oldId) return
  loadVideo()
})

watch(courseId, (newId, oldId) => {
  if (!isValidId(newId) || newId === oldId) return
  loadCourse()
})

onBeforeUnmount(() => {
  saveProgress()
  clearInterval(progressTimer)
})
</script>

<style scoped>
.video-play-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1a1a2e;
  overflow: hidden;
}

.play-header {
  height: 52px;
  background: #0d1117;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 16px;
  flex-shrink: 0;
}

.back-btn {
  color: #8c8c8c;
  font-size: 13px;
  padding: 0;
}

.play-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.play-layout {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 300px;
  overflow: hidden;
}

.player-section {
  display: flex;
  flex-direction: column;
  background: #000;
  overflow: hidden;
}

.video-player {
  width: 100%;
  flex: 1;
  background: #000;
  display: block;
  max-height: calc(100vh - 52px - 100px);
}

.video-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c8c8c;
  gap: 12px;
  min-height: 400px;
}

.placeholder-icon {
  font-size: 40px;
  color: #595959;
}

.video-info-bar {
  background: #0d1117;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  border-top: 1px solid #1a1a2e;
}

.video-meta {
  flex: 1;
  min-width: 0;
}

.vinfo-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vinfo-desc {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-btns {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.playlist-section {
  background: #0d1117;
  border-left: 1px solid #1a1a2e;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.playlist-header {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #8c8c8c;
  border-bottom: 1px solid #1a1a2e;
  flex-shrink: 0;
}

.playlist-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.chapter-group {
  margin-bottom: 4px;
}

.chapter-name {
  font-size: 12px;
  color: #595959;
  padding: 10px 16px 6px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.playlist-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.playlist-item:hover {
  background: #1a1a2e;
}

.playlist-item.active {
  background: rgb(22 119 255 / 0.12);
}

.item-icon {
  color: #595959;
  font-size: 14px;
  flex-shrink: 0;
}

.playlist-item.active .item-icon {
  color: #1677ff;
}

.item-title {
  flex: 1;
  font-size: 13px;
  color: #8c8c8c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-item.active .item-title {
  color: #1677ff;
  font-weight: 500;
}

.item-check {
  color: #52c41a;
  font-size: 14px;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .play-layout {
    grid-template-columns: 1fr;
  }

  .playlist-section {
    display: none;
  }
}
</style>
