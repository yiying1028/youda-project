<template>
  <div class="home-page">
    <section class="hero-card">
      <div class="hero-copy">
        <div class="hero-kicker">YUDA LEARNING FLOW</div>
        <h1 class="hero-title">{{ hero.title }}</h1>
        <p class="hero-desc">{{ hero.desc }}</p>
        <div class="hero-actions">
          <a-button type="primary" size="large" class="hero-btn" @click="$router.push('/community')">
            {{ hero.primaryAction }}
          </a-button>
          <a-button size="large" class="hero-btn hero-btn-secondary" @click="$router.push('/ai-chat')">
            {{ hero.secondaryAction }}
          </a-button>
        </div>
      </div>

      <div class="hero-side">
        <div class="hero-side-title">{{ heroSideTitle }}</div>
        <div class="feature-grid">
          <button
            v-for="feature in features"
            :key="feature.path"
            class="feature-card"
            type="button"
            @click="$router.push(feature.path)"
          >
            <div class="feature-icon" :style="{ background: feature.color }">
              <component :is="feature.icon" class="feature-icon-svg" />
            </div>
            <div class="feature-name">{{ feature.name }}</div>
            <div class="feature-desc">{{ feature.desc }}</div>
          </button>
        </div>
      </div>
    </section>

    <section class="panel-grid">
      <section class="info-panel">
        <div class="panel-head">
          <div>
            <div class="panel-kicker">Broadcast</div>
            <h2 class="panel-title">{{ labels.announcements }}</h2>
          </div>
        </div>
        <a-skeleton :loading="announcementLoading" active>
          <div v-if="announcements.length === 0" class="empty-tip">{{ labels.emptyAnnouncement }}</div>
          <div v-else class="announcement-list">
            <div v-for="item in announcements" :key="item.id" class="announcement-item">
              <span class="announcement-dot"></span>
              <div class="announcement-body">
                <div class="announcement-title">{{ item.title }}</div>
                <div class="announcement-date">{{ labels.publishAt }} {{ formatDate(item.createTime) }}</div>
              </div>
            </div>
          </div>
        </a-skeleton>
      </section>

      <section class="info-panel">
        <div class="panel-head panel-head--split">
          <div>
            <div class="panel-kicker">Courses</div>
            <h2 class="panel-title">{{ labels.courses }}</h2>
          </div>
          <a-button type="link" @click="$router.push('/courses')">{{ labels.viewAll }}</a-button>
        </div>
        <a-skeleton :loading="courseLoading" active>
          <div v-if="featuredCourses.length === 0" class="empty-tip">{{ labels.emptyCourse }}</div>
          <div v-else class="course-list">
            <div
              v-for="course in featuredCourses"
              :key="course.id"
              class="course-item"
              @click="$router.push('/course/' + course.id)"
            >
              <img
                :src="course.cover || '/course-cover-fallback.svg'"
                class="course-cover"
                :alt="labels.courseCoverAlt"
                @error="handleCourseCoverError"
              />
              <div class="course-detail">
                <div class="course-name">{{ course.name }}</div>
                <div class="course-sub">
                  <a-tag color="blue">{{ course.subjectName || labels.subjectFallback }}</a-tag>
                  <a-tag>{{ course.gradeName || labels.gradeFallback }}</a-tag>
                </div>
              </div>
            </div>
          </div>
        </a-skeleton>
      </section>

      <section class="info-panel">
        <div class="panel-head panel-head--split">
          <div>
            <div class="panel-kicker">Community Pulse</div>
            <h2 class="panel-title">{{ labels.hotPosts }}</h2>
          </div>
          <a-button type="link" @click="$router.push('/community')">{{ labels.enterCommunity }}</a-button>
        </div>
        <a-skeleton :loading="postLoading" active>
          <div v-if="hotPosts.length === 0" class="empty-tip">{{ labels.emptyPost }}</div>
          <div v-else class="post-list">
            <div
              v-for="(post, index) in hotPosts"
              :key="post.id"
              class="post-item"
              @click="$router.push('/post/' + post.id)"
            >
              <div class="post-rank">{{ index + 1 }}</div>
              <div class="post-info">
                <div class="post-title">{{ post.title }}</div>
                <div class="post-meta">
                  <span>{{ post.authorName || labels.anonymous }}</span>
                  <span class="dot">·</span>
                  <like-outlined />
                  <span>{{ post.likeCount || 0 }}</span>
                  <span class="dot">·</span>
                  <message-outlined />
                  <span>{{ post.commentCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </a-skeleton>
      </section>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import {
  LikeOutlined,
  MessageOutlined,
  BookOutlined,
  RobotOutlined,
  TeamOutlined,
  ReadOutlined
} from '@ant-design/icons-vue'
import { getAnnouncementList, getPostList, getCourseList } from '@/api/index.js'

const hero = {
  title: '\u8BA9\u63D0\u95EE\u3001\u5B66\u4E60\u4E0E\u590D\u76D8\uFF0C\u56DE\u5230\u4E00\u6761\u6E05\u6670\u7684\u8282\u594F\u91CC\u3002',
  desc: '\u4ECE\u793E\u533A\u63D0\u95EE\u5230\u8BFE\u7A0B\u8DDF\u5B66\uFF0C\u518D\u5230\u8D44\u6599\u67E5\u627E\u4E0E AI \u8F85\u52A9\uFF0C\u4F18\u7B54\u628A\u5E38\u7528\u5B66\u4E60\u73AF\u8282\u96C6\u4E2D\u5728\u4E00\u8D77\uFF0C\u51CF\u5C11\u5207\u6362\u6210\u672C\uFF0C\u4E5F\u66F4\u65B9\u4FBF\u6301\u7EED\u8DDF\u8FDB\u3002',
  primaryAction: '\u8FDB\u5165\u5B66\u4E60\u793E\u533A',
  secondaryAction: '\u7ACB\u5373\u4F53\u9A8C AI \u7B54\u7591'
}

const heroSideTitle = '\u56DB\u4E2A\u6838\u5FC3\u5B66\u4E60\u5165\u53E3'

const labels = {
  announcements: '\u5E73\u53F0\u516C\u544A',
  courses: '\u7CBE\u9009\u8BFE\u7A0B',
  hotPosts: '\u70ED\u95E8\u8BA8\u8BBA',
  emptyAnnouncement: '\u6682\u65E0\u516C\u544A',
  emptyCourse: '\u6682\u65E0\u8BFE\u7A0B',
  emptyPost: '\u6682\u65E0\u5E16\u5B50',
  publishAt: '\u53D1\u5E03\u65F6\u95F4',
  viewAll: '\u67E5\u770B\u5168\u90E8',
  enterCommunity: '\u8FDB\u5165\u793E\u533A',
  anonymous: '\u533F\u540D\u7528\u6237',
  courseCoverAlt: '\u8BFE\u7A0B\u5C01\u9762',
  subjectFallback: '\u5B66\u79D1\u5206\u7C7B',
  gradeFallback: '\u9002\u7528\u5E74\u7EA7'
}

const features = [
  {
    path: '/community',
    name: '\u5B66\u4E60\u793E\u533A',
    desc: '\u63D0\u95EE\u3001\u8FFD\u95EE\u548C\u4E92\u52A9\u8BA8\u8BBA\u96C6\u4E2D\u53D1\u751F\u3002',
    color: 'linear-gradient(135deg, #d4653e 0%, #b94721 100%)',
    icon: TeamOutlined
  },
  {
    path: '/resources',
    name: '\u8D44\u6599\u5E93',
    desc: '\u6559\u8F85\u3001\u8BB2\u4E49\u4E0E\u6A21\u677F\u53EF\u4EE5\u7EDF\u4E00\u67E5\u627E\u4E0B\u8F7D\u3002',
    color: 'linear-gradient(135deg, #1e546d 0%, #2d728f 100%)',
    icon: BookOutlined
  },
  {
    path: '/ai-chat',
    name: 'AI \u7B54\u7591',
    desc: '\u5148\u5FEB\u901F\u62C6\u9898\uFF0C\u518D\u6DF1\u5165\u8FFD\u95EE\u5173\u952E\u6B65\u9AA4\u3002',
    color: 'linear-gradient(135deg, #c79a43 0%, #e0b962 100%)',
    icon: RobotOutlined
  },
  {
    path: '/courses',
    name: '\u8BFE\u7A0B\u4E2D\u5FC3',
    desc: '\u7CBE\u9009\u8BFE\u7A0B\u6309\u7167\u5B66\u4E60\u8282\u594F\u987A\u5E8F\u7F16\u6392\u3002',
    color: 'linear-gradient(135deg, #6e7e67 0%, #8b9878 100%)',
    icon: ReadOutlined
  }
]

const announcements = ref([])
const announcementLoading = ref(true)
const hotPosts = ref([])
const postLoading = ref(true)
const featuredCourses = ref([])
const courseLoading = ref(true)

function formatDate(date) {
  return dayjs(date).format('MM-DD')
}

function handleCourseCoverError(event) {
  if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) {
    event.target.src = '/course-cover-fallback.svg'
  }
}

async function loadData() {
  try {
    const [annData, postData, courseData] = await Promise.allSettled([
      getAnnouncementList({ current: 1, size: 5 }),
      getPostList({ current: 1, size: 8, sortBy: 'hot' }),
      getCourseList({ current: 1, size: 4 })
    ])

    if (annData.status === 'fulfilled') {
      announcements.value = annData.value?.records || annData.value || []
    }
    if (postData.status === 'fulfilled') {
      hotPosts.value = postData.value?.records || postData.value || []
    }
    if (courseData.status === 'fulfilled') {
      featuredCourses.value = courseData.value?.records || courseData.value || []
    }
  } finally {
    announcementLoading.value = false
    postLoading.value = false
    courseLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-page {
  padding: 28px 24px 40px;
  background: linear-gradient(180deg, #f8f3ea 0%, #f6f7f8 100%);
}

.hero-card {
  width: min(1200px, 100%);
  margin: 0 auto 28px;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 24px;
}

.hero-copy,
.hero-side,
.info-panel {
  background: rgba(255, 250, 243, 0.84);
  border: 1px solid rgba(24, 32, 42, 0.08);
  border-radius: 28px;
  box-shadow: 0 18px 40px rgba(49, 39, 28, 0.08);
}

.hero-copy,
.hero-side,
.info-panel {
  padding: 28px;
}

.hero-kicker,
.panel-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  color: #9c593c;
}

.hero-title {
  max-width: 640px;
  margin: 14px 0 16px;
  font-size: clamp(34px, 4vw, 52px);
  line-height: 1.1;
  letter-spacing: -0.025em;
  color: #1f2430;
}

.hero-desc {
  max-width: 620px;
  margin: 0;
  font-size: 16px;
  line-height: 1.85;
  color: #5d6876;
}

.hero-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-top: 30px;
}

.hero-btn {
  min-height: 48px;
  padding-inline: 24px;
}

.hero-btn-secondary {
  background: rgba(255, 250, 243, 0.72);
  border: 1px solid rgba(24, 32, 42, 0.14);
  color: #1f2430;
}

.hero-side-title,
.panel-title {
  font-size: 28px;
  color: #1f2430;
  margin: 10px 0 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.feature-card {
  border: 0;
  text-align: left;
  padding: 16px;
  border-radius: 20px;
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(31, 36, 48, 0.12);
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.feature-icon-svg {
  font-size: 20px;
}

.feature-name {
  margin-top: 14px;
  font-weight: 700;
  color: #1f2430;
}

.feature-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: #6b7280;
}

.panel-grid {
  width: min(1200px, 100%);
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.panel-head--split {
  align-items: center;
}

.empty-tip {
  color: #6b7280;
  padding: 24px 0 8px;
}

.announcement-list,
.post-list,
.course-list {
  margin-top: 18px;
  display: grid;
  gap: 14px;
}

.announcement-item {
  display: grid;
  grid-template-columns: 10px 1fr;
  gap: 12px;
  align-items: flex-start;
}

.announcement-dot {
  width: 10px;
  height: 10px;
  margin-top: 8px;
  border-radius: 50%;
  background: #d4653e;
}

.announcement-title,
.post-title,
.course-name {
  font-weight: 700;
  color: #1f2430;
}

.announcement-date,
.post-meta {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.post-item {
  display: grid;
  grid-template-columns: 36px 1fr;
  gap: 12px;
  align-items: flex-start;
  cursor: pointer;
}

.post-rank {
  width: 36px;
  height: 36px;
  border-radius: 14px;
  background: rgba(212, 101, 62, 0.14);
  color: #9c593c;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot {
  margin: 0 6px;
}

.course-item {
  display: grid;
  grid-template-columns: 108px 1fr;
  gap: 12px;
  align-items: center;
  cursor: pointer;
}

.course-cover {
  width: 108px;
  height: 76px;
  border-radius: 16px;
  object-fit: cover;
  background: #f2eadf;
}

.course-sub {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 1100px) {
  .hero-card,
  .panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .home-page {
    padding: 18px 14px 32px;
  }

  .hero-copy,
  .hero-side,
  .info-panel {
    padding: 20px;
    border-radius: 22px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .course-item {
    grid-template-columns: 88px 1fr;
  }

  .course-cover {
    width: 88px;
    height: 68px;
  }
}
</style>