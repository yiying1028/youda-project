<template>
  <div class="courses-page">
    <div class="courses-inner">
      <div class="page-header">
        <h1 class="page-title">课程中心</h1>
        <p class="page-desc">免费课程可直接学习，付费课程需先下单并完成支付后解锁。</p>
      </div>

      <a-card :bordered="false" class="filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">学科</span>
            <a-select v-model:value="filters.subjectId" placeholder="全部学科" style="width: 140px" allow-clear @change="handleFilterChange">
              <a-select-option v-for="item in subjects" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
            </a-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">年级</span>
            <a-select v-model:value="filters.gradeId" placeholder="全部年级" style="width: 140px" allow-clear @change="handleFilterChange">
              <a-select-option v-for="item in grades" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
            </a-select>
          </div>
          <div class="filter-item filter-item--grow">
            <a-input-search v-model:value="filters.keyword" placeholder="搜索课程名称或简介" allow-clear style="max-width: 360px" @search="handleFilterChange" />
          </div>
        </div>
      </a-card>

      <a-spin :spinning="loading">
        <div v-if="courses.length === 0 && !loading" class="empty-state">
          <inbox-outlined class="empty-icon" />
          <p>暂无课程</p>
        </div>
        <div v-else class="course-grid">
          <div v-for="course in courses" :key="course.id" class="course-card" @click="router.push('/course/' + course.id)">
            <div class="course-cover-wrap">
              <img :src="course.cover || '/course-cover-fallback.svg'" :alt="course.name" class="course-cover" @error="handleImgError" />
              <div class="course-price" :class="{ paid: course.requiresPurchase }">
                <template v-if="course.requiresPurchase">
                  {{ formatPrice(course.priceAmount) }}
                  <span v-if="course.hasOrder" class="status-tag">{{ course.orderStatusLabel }}</span>
                </template>
                <template v-else>免费</template>
              </div>
            </div>
            <div class="course-info">
              <div class="course-name">{{ course.name }}</div>
              <div class="course-desc">{{ course.description || '课程简介待补充' }}</div>
              <div class="course-meta">
                <a-tag color="blue">{{ course.subjectName }}</a-tag>
                <a-tag>{{ course.gradeName }}</a-tag>
              </div>
              <div class="course-footer">
                <span><user-outlined /> {{ course.teacherName || '暂无讲师' }}</span>
                <span>{{ course.learnCount || 0 }} 人学习</span>
              </div>
            </div>
          </div>
        </div>
      </a-spin>

      <div v-if="total > 0" class="pagination-wrap">
        <a-pagination v-model:current="currentPage" :total="total" :page-size="pageSize" show-quick-jumper :show-total="(value) => `共 ${value} 条`" @change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { InboxOutlined, UserOutlined } from '@ant-design/icons-vue'
import { getCourseList, getGradeList, getSubjectList } from '@/api/index.js'

const router = useRouter()
const courses = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const subjects = ref([])
const grades = ref([])
const filters = reactive({ subjectId: null, gradeId: null, keyword: '' })

function handleImgError(event) {
  if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) event.target.src = '/course-cover-fallback.svg'
}
function formatPrice(value) { return `￥${Number(value || 0).toFixed(2)}` }
async function loadCourses() {
  loading.value = true
  try {
    const data = await getCourseList({ current: currentPage.value, size: pageSize.value, ...filters })
    courses.value = data?.records || data || []
    total.value = data?.total || 0
  } finally { loading.value = false }
}
async function loadFilterOptions() {
  const [subjectRes, gradeRes] = await Promise.allSettled([getSubjectList(), getGradeList()])
  if (subjectRes.status === 'fulfilled') subjects.value = subjectRes.value || []
  if (gradeRes.status === 'fulfilled') grades.value = gradeRes.value || []
}
function handleFilterChange() { currentPage.value = 1; loadCourses() }
function handlePageChange(page) { currentPage.value = page; loadCourses() }
onMounted(() => { loadFilterOptions(); loadCourses() })
</script>

<style scoped>
.courses-page { min-height: calc(100vh - 64px); background: #f5f7fa; }
.courses-inner { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 30px; font-weight: 700; color: #1f2430; margin-bottom: 6px; }
.page-desc { color: #6b7280; font-size: 14px; }
.filter-card { border-radius: 18px; box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06); margin-bottom: 22px; }
.filter-row { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.filter-item { display: flex; align-items: center; gap: 8px; }
.filter-item--grow { flex: 1; }
.filter-label { font-size: 14px; color: #4b5563; font-weight: 600; }
.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 20px; }
.course-card { background: #fff; border-radius: 18px; overflow: hidden; cursor: pointer; transition: transform 0.2s ease, box-shadow 0.2s ease; box-shadow: 0 6px 22px rgba(15, 23, 42, 0.06); }
.course-card:hover { transform: translateY(-4px); box-shadow: 0 14px 30px rgba(15, 23, 42, 0.12); }
.course-cover-wrap { position: relative; height: 168px; background: #e5e7eb; }
.course-cover { width: 100%; height: 100%; object-fit: cover; }
.course-price { position: absolute; top: 14px; right: 14px; padding: 6px 10px; border-radius: 999px; background: rgba(15, 118, 110, 0.88); color: #fff; font-size: 12px; font-weight: 700; }
.course-price.paid { background: rgba(214, 107, 8, 0.9); }
.status-tag { margin-left: 6px; color: #ffe7ba; }
.course-info { padding: 18px; }
.course-name { font-size: 18px; font-weight: 700; color: #1f2937; line-height: 1.4; }
.course-desc { margin-top: 10px; min-height: 44px; color: #6b7280; line-height: 1.6; font-size: 14px; }
.course-meta { margin-top: 14px; display: flex; flex-wrap: wrap; gap: 8px; }
.course-footer { margin-top: 16px; display: flex; justify-content: space-between; gap: 10px; color: #6b7280; font-size: 13px; }
.pagination-wrap { margin-top: 24px; display: flex; justify-content: center; }
.empty-state { padding: 56px 0; text-align: center; color: #8c8c8c; }
.empty-icon { font-size: 40px; margin-bottom: 12px; }
@media (max-width: 768px) { .courses-inner { padding: 16px; } .course-footer { flex-direction: column; } }
</style>
