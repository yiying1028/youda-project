<template>
  <div class="resources-page">
    <div class="resources-inner">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1 class="page-title">资料库</h1>
        <p class="page-desc">海量优质学习资料，免费下载使用</p>
      </div>

      <!-- 筛选区域 -->
      <a-card :bordered="false" class="filter-card">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">科目：</span>
            <a-select
              v-model:value="filters.subjectId"
              placeholder="全部科目"
              style="width: 130px"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option
                v-for="s in subjects"
                :key="s.id"
                :value="s.id"
              >
                {{ s.name }}
              </a-select-option>
            </a-select>
          </div>

          <div class="filter-item">
            <span class="filter-label">年级：</span>
            <a-select
              v-model:value="filters.gradeId"
              placeholder="全部年级"
              style="width: 130px"
              allow-clear
              @change="handleFilterChange"
            >
              <a-select-option
                v-for="g in grades"
                :key="g.id"
                :value="g.id"
              >
                {{ g.name }}
              </a-select-option>
            </a-select>
          </div>

          <div class="filter-item" style="flex: 1">
            <a-input-search
              v-model:value="filters.keyword"
              placeholder="搜索资料名称..."
              allow-clear
              style="max-width: 320px"
              @search="handleFilterChange"
            />
          </div>
        </div>
      </a-card>

      <!-- 资料列表 -->
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
            <!-- 资料图标（根据文件类型显示不同图标颜色） -->
            <div class="resource-icon" :style="{ background: getTypeColor(item.fileType) }">
              <file-outlined />
            </div>

            <div class="resource-body">
              <div class="resource-name">{{ item.name }}</div>
              <div class="resource-desc">{{ item.description || '暂无描述' }}</div>
              <div class="resource-meta">
                <a-tag color="blue" size="small">{{ item.subjectName }}</a-tag>
                <a-tag size="small">{{ item.gradeName }}</a-tag>
                <span class="file-size">{{ formatSize(item.fileSize) }}</span>
              </div>
              <div class="resource-footer">
                <span class="upload-time">
                  <calendar-outlined /> {{ formatDate(item.createTime) }}
                </span>
                <span class="download-count">
                  <download-outlined /> {{ item.downloadCount || 0 }} 次下载
                </span>
              </div>
            </div>
          </div>
        </div>
      </a-spin>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <a-pagination
          v-model:current="currentPage"
          :total="total"
          :page-size="pageSize"
          show-quick-jumper
          :show-total="(total) => `共 ${total} 份资料`"
          @change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getResourceList, getSubjectList, getGradeList } from '@/api/index.js'
import dayjs from 'dayjs'
import {
  FileOutlined,
  InboxOutlined,
  CalendarOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'

const resources = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 科目和年级列表
const subjects = ref([])
const grades = ref([])

// 筛选条件
const filters = reactive({
  subjectId: null,
  gradeId: null,
  keyword: ''
})

/**
 * 根据文件类型获取颜色
 */
function getTypeColor(type) {
  const colorMap = {
    pdf: 'linear-gradient(135deg, #ff6b6b, #ee5a24)',
    doc: 'linear-gradient(135deg, #4facfe, #00f2fe)',
    docx: 'linear-gradient(135deg, #4facfe, #00f2fe)',
    ppt: 'linear-gradient(135deg, #f093fb, #f5576c)',
    pptx: 'linear-gradient(135deg, #f093fb, #f5576c)',
    xls: 'linear-gradient(135deg, #43e97b, #38f9d7)',
    xlsx: 'linear-gradient(135deg, #43e97b, #38f9d7)'
  }
  return colorMap[type?.toLowerCase()] || 'linear-gradient(135deg, #667eea, #764ba2)'
}

/**
 * 格式化文件大小
 */
function formatSize(bytes) {
  if (!bytes) return '未知大小'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

/**
 * 格式化日期
 */
function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

/**
 * 加载资料列表
 */
async function loadResources() {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...filters
    }
    const data = await getResourceList(params)
    resources.value = data?.records || data || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 加载筛选选项数据
 */
async function loadFilterOptions() {
  const [subjectData, gradeData] = await Promise.allSettled([
    getSubjectList(),
    getGradeList()
  ])
  if (subjectData.status === 'fulfilled') subjects.value = subjectData.value || []
  if (gradeData.status === 'fulfilled') grades.value = gradeData.value || []
}

/**
 * 筛选条件变化时重置分页并刷新
 */
function handleFilterChange() {
  currentPage.value = 1
  loadResources()
}

/**
 * 翻页
 */
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
.resources-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

.resources-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 26px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 6px;
}

.page-desc {
  color: #8c8c8c;
  font-size: 14px;
}

/* 筛选区域 */
.filter-card {
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #595959;
  white-space: nowrap;
  font-weight: 500;
}

/* 资料网格 */
.resource-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.resource-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.resource-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

/* 资料图标 */
.resource-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.resource-body {
  flex: 1;
  min-width: 0;
}

.resource-name {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.resource-desc {
  font-size: 13px;
  color: #8c8c8c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.file-size {
  font-size: 12px;
  color: #8c8c8c;
}

.resource-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #bfbfbf;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 0;
  color: #bfbfbf;
}

.empty-icon {
  font-size: 60px;
  display: block;
  margin-bottom: 12px;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .resource-grid {
    grid-template-columns: 1fr;
  }
}
</style>
