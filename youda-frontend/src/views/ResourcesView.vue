<template>
  <div class="resources-page">
    <div class="resources-inner">
      <div class="page-header">
        <h1 class="page-title">资料库</h1>
        <p class="page-desc">支持免费下载与积分购买，按学科、年级快速筛选可用学习资料。</p>
      </div>

      <a-card :bordered="false" class="filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">科目</span>
            <a-select
              v-model:value="filters.subjectId"
              placeholder="全部科目"
              style="width: 130px"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option v-for="item in subjects" :key="item.id" :value="item.id">
                {{ item.name }}
              </a-select-option>
            </a-select>
          </div>

          <div class="filter-item">
            <span class="filter-label">年级</span>
            <a-select
              v-model:value="filters.gradeId"
              placeholder="全部年级"
              style="width: 130px"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option v-for="item in grades" :key="item.id" :value="item.id">
                {{ item.name }}
              </a-select-option>
            </a-select>
          </div>

          <div class="filter-item filter-item--grow">
            <a-input-search
              v-model:value="filters.keyword"
              placeholder="搜索资料名称"
              allow-clear
              style="max-width: 320px"
              @search="handleFilterChange"
            />
          </div>
        </div>
      </a-card>

      <a-spin :spinning="loading">
        <div v-if="resources.length === 0 && !loading" class="empty-state">
          <inbox-outlined class="empty-icon" />
          <p>暂无相关资料</p>
        </div>

        <div v-else class="resource-grid">
          <div
            v-for="item in resources"
            :key="item.id"
            class="resource-card"
            @click="$router.push('/resource/' + item.id)"
          >
            <div class="resource-head">
              <div class="resource-name">{{ item.name }}</div>
              <div class="resource-price" :class="{ paid: item.requiresPoints }">
                <template v-if="item.requiresPoints">
                  <span>{{ item.pointsCost }} 积分</span>
                  <span v-if="item.purchased" class="purchased-flag">已购</span>
                </template>
                <template v-else>免费</template>
              </div>
            </div>
            <div class="resource-desc">{{ item.description || '暂无描述' }}</div>
            <div class="resource-meta">
              <a-tag color="blue">{{ item.subjectName }}</a-tag>
              <a-tag>{{ item.gradeName }}</a-tag>
              <span class="file-size">{{ formatSize(item.fileSize) }}</span>
            </div>
            <div class="resource-footer">
              <span><calendar-outlined /> {{ formatDate(item.createTime) }}</span>
              <span><download-outlined /> {{ item.downloadCount || 0 }} 次下载</span>
            </div>
          </div>
        </div>
      </a-spin>

      <div v-if="total > 0" class="pagination-wrap">
        <a-pagination
          v-model:current="currentPage"
          :total="total"
          :page-size="pageSize"
          show-quick-jumper
          :show-total="(value) => `共 ${value} 份资料`"
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { CalendarOutlined, DownloadOutlined, InboxOutlined } from '@ant-design/icons-vue'
import { getGradeList, getResourceList, getSubjectList } from '@/api/index.js'

const resources = ref([])
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

function formatSize(bytes) {
  if (!bytes) return '未知大小'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function formatDate(value) {
  return value ? dayjs(value).format('YYYY-MM-DD') : '--'
}

async function loadResources() {
  loading.value = true
  try {
    const data = await getResourceList({
      current: currentPage.value,
      size: pageSize.value,
      ...filters
    })
    resources.value = data?.records || data || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadFilterOptions() {
  const [subjectRes, gradeRes] = await Promise.allSettled([getSubjectList(), getGradeList()])
  if (subjectRes.status === 'fulfilled') subjects.value = subjectRes.value || []
  if (gradeRes.status === 'fulfilled') grades.value = gradeRes.value || []
}

function handleFilterChange() {
  currentPage.value = 1
  loadResources()
}

function handlePageChange(page) {
  currentPage.value = page
  loadResources()
}

onMounted(() => {
  loadFilterOptions()
  loadResources()
})
</script>

<style scoped>
.resources-page { min-height: calc(100vh - 64px); background: #f5f7fa; }
.resources-inner { max-width: 1200px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: 700; color: #262626; margin-bottom: 6px; }
.page-desc { color: #8c8c8c; font-size: 14px; }
.filter-card { border-radius: 12px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06); margin-bottom: 20px; }
.filter-row { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.filter-item { display: flex; align-items: center; gap: 8px; }
.filter-item--grow { flex: 1; }
.filter-label { font-size: 14px; color: #595959; }
.resource-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 18px; }
.resource-card {
  background: #fff;
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.resource-card:hover { transform: translateY(-4px); box-shadow: 0 14px 28px rgba(15, 23, 42, 0.1); }
.resource-head { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; }
.resource-name { font-size: 18px; font-weight: 700; color: #1f2937; line-height: 1.4; }
.resource-price {
  flex-shrink: 0;
  padding: 6px 10px;
  border-radius: 999px;
  background: #edf7ed;
  color: #1e7a34;
  font-size: 12px;
  font-weight: 700;
}
.resource-price.paid { background: #fff3e8; color: #d46b08; }
.purchased-flag { margin-left: 6px; color: #1677ff; }
.resource-desc { margin-top: 12px; min-height: 44px; color: #6b7280; line-height: 1.6; font-size: 14px; }
.resource-meta { margin-top: 14px; display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.file-size { color: #6b7280; font-size: 13px; }
.resource-footer { margin-top: 16px; display: flex; justify-content: space-between; gap: 12px; color: #8c8c8c; font-size: 13px; }
.pagination-wrap { margin-top: 24px; display: flex; justify-content: center; }
.empty-state { padding: 56px 0; text-align: center; color: #8c8c8c; }
.empty-icon { font-size: 40px; margin-bottom: 12px; }
@media (max-width: 768px) {
  .resources-inner { padding: 16px; }
  .resource-footer { flex-direction: column; }
}
</style>