<template>
  <div class="courses-page">
    <div class="courses-inner">
      <div class="page-header">
        <h1 class="page-title">课程中心</h1>
        <p class="page-desc">按学科、年级和关键词筛选课程，快速找到合适的学习内容。</p>
      </div>

      <a-card :bordered="false" class="filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">科目：</span>
            <a-select
              v-model:value="filters.subjectId"
              placeholder="全部科目"
              style="width: 140px"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option v-for="s in subjects" :key="s.id" :value="s.id">
                {{ s.name }}
              </a-select-option>
            </a-select>
          </div>

          <div class="filter-item">
            <span class="filter-label">年级：</span>
            <a-select
              v-model:value="filters.gradeId"
              placeholder="全部年级"
              style="width: 140px"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option v-for="g in grades" :key="g.id" :value="g.id">
                {{ g.name }}
              </a-select-option>
            </a-select>
          </div>

          <div class="filter-item filter-item--grow">
            <a-input-search
              v-model:value="filters.keyword"
              placeholder="搜索课程名称..."
              allow-clear
              style="max-width: 360px"
              @search="handleFilterChange"
            />
          </div>
        </div>
      </a-card>

      <a-spin :spinning="loading">
        <div v-if="courses.length === 0 && !loading" class="empty-state">
          <inbox-outlined class="empty-icon" />
          <p>暂无相关课程</p>
        </div>

        <div v-else class="course-grid">
          <div
            v-for="course in courses"
            :key="course.id"
            class="course-card"
            @click="$router.push('/course/' + course.id)"
          >
            <div class="course-cover-wrap">
              <img
                :src="course.cover || '/course-cover-fallback.svg'"
                :alt="course.name"
                class="course-cover"
                @error="handleImgError"
              />
              <div class="course-cover-overlay">
                <play-circle-filled class="play-icon" />
              </div>
              <a-tag v-if="course.videoCount" class="video-count-tag">
                {{ course.videoCount }} 课时
              </a-tag>
            </div>

            <div class="course-info">
              <div class="course-name">{{ course.name }}</div>
              <div class="course-desc">{{ course.description || '暂无描述' }}</div>
              <div class="course-meta">
                <a-tag color="blue" size="small">{{ course.subjectName }}</a-tag>
                <a-tag size="small">{{ course.gradeName }}</a-tag>
              </div>
              <div class="course-footer">
                <span class="course-teacher">
                  <user-outlined /> {{ course.teacherName || '优答教师' }}
                </span>
                <span class="course-learn-count">
                  {{ course.learnCount || 0 }} 人在学
                </span>
              </div>
            </div>
          </div>
        </div>
      </a-spin>

      <div class="pagination-wrap" v-if="total > 0">
        <a-pagination
          v-model:current="currentPage"
          :total="total"
          :page-size="pageSize"
          show-quick-jumper
          :show-total="(total) => `共 ${total} 门课程`"
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCourseList, getSubjectList, getGradeList } from '@/api/index.js'
import {
  InboxOutlined,
  PlayCircleFilled,
  UserOutlined
} from '@ant-design/icons-vue'

const courses = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const subjects = ref([])
const grades = ref([])

const filters = reactive({
  subjectId: null,
  gradeId: null,
  keyword: ''
})

function handleImgError(event) {
  if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) {
    event.target.src = '/course-cover-fallback.svg'
  }
}

async function loadCourses() {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...filters
    }
    const data = await getCourseList(params)
    courses.value = data?.records || data || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadFilterOptions() {
  const [subjectData, gradeData] = await Promise.allSettled([
    getSubjectList(),
    getGradeList()
  ])
  if (subjectData.status === 'fulfilled') subjects.value = subjectData.value || []
  if (gradeData.status === 'fulfilled') grades.value = gradeData.value || []
}

function handleFilterChange() {
  currentPage.value = 1
  loadCourses()
}

function handlePageChange(page) {
  currentPage.value = page
  loadCourses()
}

onMounted(() => {
  loadFilterOptions()
  loadCourses()
})
</script>

<style scoped>
.courses-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

.courses-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 30px;
  font-weight: 700;
  color: #1f2430;
  margin-bottom: 6px;
}

.page-desc {
  color: #6b7280;
  font-size: 14px;
}

.filter-card {
  border-radius: 18px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
  margin-bottom: 22px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item--grow {
  flex: 1;
}

.filter-label {
  font-size: 14px;
  color: #4b5563;
  white-space: nowrap;
  font-weight: 600;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.course-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 6px 22px rgba(15, 23, 42, 0.06);
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.12);
}

.course-cover-wrap {
  position: relative;
  height: 178px;
  background: linear-gradient(135deg, #efe3d0 0%, #f8f3ea 100%);
}

.course-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-cover-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(10, 10, 10, 0.02), rgba(10, 10, 10, 0.2));
}

.play-icon {
  font-size: 42px;
  color: rgba(255, 255, 255, 0.88);
}

.video-count-tag {
  position: absolute;
  right: 12px;
  top: 12px;
  border-radius: 999px;
  background: rgba(17, 24, 39, 0.72);
  color: #fff;
  border: 0;
}

.course-info {
  padding: 16px;
}

.course-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2430;
  line-height: 1.4;
}

.course-desc {
  margin-top: 10px;
  min-height: 44px;
  font-size: 14px;
  line-height: 1.7;
  color: #6b7280;
}

.course-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  margin-top: 14px;
  font-size: 13px;
  color: #4b5563;
}

.empty-state {
  padding: 70px 0;
  text-align: center;
  color: #6b7280;
}

.empty-icon {
  font-size: 42px;
  margin-bottom: 14px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

@media (max-width: 768px) {
  .courses-inner {
    padding: 18px 14px 30px;
  }

  .course-grid {
    grid-template-columns: 1fr;
  }

  .course-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>