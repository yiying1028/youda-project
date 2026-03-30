<template>
  <div class="admin-courses-page">
    <div class="page-top">
      <h2 class="page-title">课程管理</h2>
      <div class="page-actions">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索课程名称..."
          style="width: 240px"
          @search="handleSearch"
          allow-clear
        />
        <a-button type="primary" @click="openAddModal">
          <plus-outlined /> 新增课程
        </a-button>
      </div>
    </div>

    <!-- 课程表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="courses"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
        :expand-row-by-click="false"
      >
        <template #bodyCell="{ column, record }">
          <!-- 课程名称 -->
          <template v-if="column.key === 'name'">
            <div class="course-cell">
              <img
                v-if="record.cover"
                :src="record.cover"
                class="course-mini-cover"
                alt="封面"
              />
              <div class="course-mini-cover placeholder-cover" v-else>
                <play-circle-outlined />
              </div>
              <span>{{ record.name }}</span>
            </div>
          </template>

          <!-- 科目+年级 -->
          <template v-else-if="column.key === 'tags'">
            <a-tag color="blue" size="small">{{ record.subjectName }}</a-tag>
            <a-tag size="small">{{ record.gradeName }}</a-tag>
          </template>

          <!-- 课时数 -->
          <template v-else-if="column.key === 'videoCount'">
            {{ record.videoCount || 0 }} 节
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <!-- 管理章节视频 -->
            <a-button
              type="link"
              size="small"
              @click="openChapterModal(record)"
            >
              章节
            </a-button>
            <a-divider type="vertical" />
            <!-- 编辑课程基本信息 -->
            <a-button type="link" size="small" @click="openEditModal(record)">
              编辑
            </a-button>
            <a-divider type="vertical" />
            <!-- 删除课程 -->
            <a-popconfirm
              title="确定删除该课程及其所有章节视频？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑课程弹窗 -->
    <a-modal
      v-model:open="showCourseModal"
      :title="editingCourse ? '编辑课程' : '新增课程'"
      :footer="null"
      width="560px"
      :destroy-on-close="true"
    >
      <a-form
        :model="courseForm"
        :rules="courseRules"
        ref="courseFormRef"
        layout="vertical"
        @finish="handleSaveCourse"
      >
        <a-form-item label="课程名称" name="name">
          <a-input v-model:value="courseForm.name" placeholder="请输入课程名称" />
        </a-form-item>

        <a-form-item label="课程描述" name="description">
          <a-textarea
            v-model:value="courseForm.description"
            placeholder="请输入课程描述"
            :rows="3"
            :maxlength="500"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="科目" name="subjectId">
              <a-select v-model:value="courseForm.subjectId" placeholder="请选择科目">
                <a-select-option v-for="s in subjects" :key="s.id" :value="s.id">
                  {{ s.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="年级" name="gradeId">
              <a-select v-model:value="courseForm.gradeId" placeholder="请选择年级">
                <a-select-option v-for="g in grades" :key="g.id" :value="g.id">
                  {{ g.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="主讲老师" name="teacherName">
          <a-input v-model:value="courseForm.teacherName" placeholder="请输入主讲老师姓名" />
        </a-form-item>

        <a-form-item label="课程封面URL" name="cover">
          <a-input v-model:value="courseForm.cover" placeholder="请输入封面图片URL（可选）" />
        </a-form-item>

        <div style="display: flex; justify-content: flex-end; gap: 8px">
          <a-button @click="showCourseModal = false">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="saving">
            {{ editingCourse ? '保存修改' : '创建课程' }}
          </a-button>
        </div>
      </a-form>
    </a-modal>

    <!-- 章节管理弹窗 -->
    <a-modal
      v-model:open="showChapterModal"
      :title="'章节管理 - ' + (selectedCourse?.name || '')"
      width="680px"
      :footer="null"
      :destroy-on-close="true"
    >
      <div class="chapter-manager">
        <!-- 新增章节 -->
        <div class="add-chapter">
          <a-input
            v-model:value="newChapterTitle"
            placeholder="输入章节标题"
            style="flex: 1"
          />
          <a-button type="primary" @click="handleAddChapter" :loading="chapterAdding">
            添加章节
          </a-button>
        </div>

        <!-- 章节列表 -->
        <div class="chapter-list">
          <div
            v-for="chapter in selectedCourse?.chapters"
            :key="chapter.id"
            class="chapter-block"
          >
            <div class="chapter-head">
              <folder-outlined />
              <span class="chapter-name">{{ chapter.title }}</span>
              <span class="chapter-video-count">{{ chapter.videos?.length || 0 }} 节</span>
              <!-- 上传视频按钮 -->
              <a-upload
                :before-upload="(file) => handleVideoUpload(chapter.id, file)"
                accept="video/*"
                :show-upload-list="false"
              >
                <a-button size="small" type="link">
                  <upload-outlined /> 上传视频
                </a-button>
              </a-upload>
            </div>

            <!-- 视频列表 -->
            <div
              v-for="video in chapter.videos"
              :key="video.id"
              class="video-row"
            >
              <play-square-outlined class="video-row-icon" />
              <span class="video-row-title">{{ video.title }}</span>
              <span class="video-row-duration" v-if="video.duration">
                {{ formatDuration(video.duration) }}
              </span>
            </div>
          </div>

          <div v-if="!selectedCourse?.chapters?.length" class="no-chapters">
            <p>暂无章节，请先添加章节</p>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getAdminCourseList,
  adminAddCourse,
  adminUpdateCourse,
  adminDeleteCourse,
  adminAddChapter,
  adminUploadVideo,
  getSubjectList,
  getGradeList
} from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  PlusOutlined,
  PlayCircleOutlined,
  PlaySquareOutlined,
  FolderOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'

const courses = ref([])
const loading = ref(false)
const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (t) => `共 ${t} 门课程`,
  showQuickJumper: true
})

const subjects = ref([])
const grades = ref([])

// 课程编辑弹窗
const showCourseModal = ref(false)
const editingCourse = ref(null)
const saving = ref(false)
const courseFormRef = ref()
const courseForm = reactive({
  name: '',
  description: '',
  subjectId: null,
  gradeId: null,
  teacherName: '',
  cover: ''
})

// 章节管理弹窗
const showChapterModal = ref(false)
const selectedCourse = ref(null)
const newChapterTitle = ref('')
const chapterAdding = ref(false)

const courseRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const columns = [
  { title: '课程', key: 'name', width: '35%' },
  { title: '分类', key: 'tags', width: 160 },
  { title: '主讲', dataIndex: 'teacherName', width: 100 },
  { title: '课时', key: 'videoCount', width: 80 },
  { title: '操作', key: 'action', width: 160 }
]

async function loadCourses() {
  loading.value = true
  try {
    const data = await getAdminCourseList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined
    })
    courses.value = data?.records || data || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 打开新增课程弹窗
 */
function openAddModal() {
  editingCourse.value = null
  Object.assign(courseForm, {
    name: '', description: '', subjectId: null,
    gradeId: null, teacherName: '', cover: ''
  })
  showCourseModal.value = true
}

/**
 * 打开编辑课程弹窗
 */
function openEditModal(record) {
  editingCourse.value = record
  Object.assign(courseForm, {
    name: record.name,
    description: record.description || '',
    subjectId: record.subjectId,
    gradeId: record.gradeId,
    teacherName: record.teacherName || '',
    cover: record.cover || ''
  })
  showCourseModal.value = true
}

/**
 * 保存课程（新增或编辑）
 */
async function handleSaveCourse() {
  saving.value = true
  try {
    if (editingCourse.value) {
      await adminUpdateCourse(editingCourse.value.id, courseForm)
      message.success('课程信息已更新')
    } else {
      await adminAddCourse(courseForm)
      message.success('课程创建成功')
    }
    showCourseModal.value = false
    loadCourses()
  } finally {
    saving.value = false
  }
}

/**
 * 删除课程
 */
async function handleDelete(id) {
  try {
    await adminDeleteCourse(id)
    message.success('课程已删除')
    loadCourses()
  } catch {}
}

/**
 * 打开章节管理弹窗
 */
function openChapterModal(record) {
  selectedCourse.value = record
  showChapterModal.value = true
}

/**
 * 添加章节
 */
async function handleAddChapter() {
  if (!newChapterTitle.value.trim()) {
    message.warning('请输入章节标题')
    return
  }
  chapterAdding.value = true
  try {
    await adminAddChapter(selectedCourse.value.id, { title: newChapterTitle.value })
    message.success('章节添加成功')
    newChapterTitle.value = ''
    // 重新加载课程详情以刷新章节列表
    loadCourses()
  } finally {
    chapterAdding.value = false
  }
}

/**
 * 上传视频（关联到章节）
 */
async function handleVideoUpload(chapterId, file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', file.name.replace(/\.[^.]+$/, ''))
  try {
    await adminUploadVideo(chapterId, formData)
    message.success('视频上传成功')
    loadCourses()
  } catch {}
  return false
}

/**
 * 格式化视频时长
 */
function formatDuration(seconds) {
  if (!seconds) return ''
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

function handleSearch() {
  pagination.current = 1
  loadCourses()
}

function handleTableChange(pag) {
  pagination.current = pag.current
  loadCourses()
}

async function loadOptions() {
  const [subData, gradeData] = await Promise.allSettled([getSubjectList(), getGradeList()])
  if (subData.status === 'fulfilled') subjects.value = subData.value || []
  if (gradeData.status === 'fulfilled') grades.value = gradeData.value || []
}

onMounted(() => {
  loadOptions()
  loadCourses()
})
</script>

<style scoped>
.page-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #262626;
  margin: 0;
}

.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 课程名称单元格 */
.course-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-mini-cover {
  width: 48px;
  height: 34px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

.placeholder-cover {
  background: linear-gradient(135deg, #e8f0fe, #c8dcff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1677ff;
  font-size: 16px;
}

/* 章节管理弹窗内容 */
.chapter-manager {
  min-height: 300px;
}

.add-chapter {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.chapter-list {
  max-height: 400px;
  overflow-y: auto;
}

.chapter-block {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 10px;
  overflow: hidden;
}

.chapter-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.chapter-name {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.chapter-video-count {
  font-size: 12px;
  color: #8c8c8c;
}

.video-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-bottom: 1px solid #f5f5f5;
}

.video-row:last-child {
  border-bottom: none;
}

.video-row-icon {
  color: #1677ff;
  font-size: 14px;
}

.video-row-title {
  flex: 1;
  font-size: 13px;
  color: #434343;
}

.video-row-duration {
  font-size: 12px;
  color: #bfbfbf;
}

.no-chapters {
  text-align: center;
  padding: 32px;
  color: #bfbfbf;
}
</style>
