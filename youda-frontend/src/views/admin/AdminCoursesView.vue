<template>
  <div class="admin-courses-page">
    <div class="page-top">
      <h2 class="page-title">课程管理</h2>
      <div class="page-actions">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索课程名称"
          style="width: 240px"
          allow-clear
          @search="handleSearch"
        />
        <a-button type="primary" @click="openAddModal">
          <plus-outlined /> 新增课程
        </a-button>
      </div>
    </div>

    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="courses"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="course-cell">
              <img
                :src="record.coverImage || '/course-cover-fallback.svg'"
                class="course-mini-cover"
                alt="课程封面"
                @error="handleCourseCoverError"
              />
              <div class="course-main-info">
                <div class="course-name">{{ record.name }}</div>
                <div class="course-desc">{{ record.description || '暂无课程简介' }}</div>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'tags'">
            <a-tag color="blue">{{ getSubjectName(record.subjectId) }}</a-tag>
            <a-tag>{{ getGradeName(record.gradeId) }}</a-tag>
          </template>

          <template v-else-if="column.key === 'pricing'">
            <a-tag :color="isPaidCourse(record) ? 'orange' : 'green'">
              {{ isPaidCourse(record) ? `${record.pointsCost} 积分` : '免费' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'chapterCount'">
            {{ record.chapterCount || 0 }} 章
          </template>

          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openChapterModal(record)">
              章节与视频
            </a-button>
            <a-divider type="vertical" />
            <a-button type="link" size="small" @click="openEditModal(record)">
              编辑
            </a-button>
            <a-divider type="vertical" />
            <a-popconfirm title="确定删除该课程及其章节和视频吗？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="showCourseModal"
      :title="editingCourse ? '编辑课程' : '新增课程'"
      :footer="null"
      width="680px"
      :destroy-on-close="true"
      @cancel="closeCourseModal"
    >
      <a-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        layout="vertical"
        @finish="handleSaveCourse"
      >
        <a-form-item label="课程名称" name="name">
          <a-input v-model:value="courseForm.name" placeholder="请输入课程名称" />
        </a-form-item>

        <a-form-item label="课程描述" name="description">
          <a-textarea
            v-model:value="courseForm.description"
            :rows="4"
            :maxlength="500"
            placeholder="请输入课程简介"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="科目" name="subjectId">
              <a-select v-model:value="courseForm.subjectId" placeholder="请选择科目">
                <a-select-option v-for="item in subjects" :key="item.id" :value="item.id">
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="年级" name="gradeId">
              <a-select v-model:value="courseForm.gradeId" placeholder="请选择年级">
                <a-select-option v-for="item in grades" :key="item.id" :value="item.id">
                  {{ item.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="主讲老师" name="teacherName">
          <a-input v-model:value="courseForm.teacherName" placeholder="请输入主讲老师姓名" />
        </a-form-item>

        <a-form-item label="课程封面">
          <div class="cover-uploader">
            <div class="cover-preview-box">
              <img
                v-if="courseForm.coverImage"
                :src="courseForm.coverImage"
                alt="课程封面预览"
                class="cover-preview"
                @error="handleCourseCoverError"
              />
              <div v-else class="cover-preview-empty">
                <picture-outlined />
                <span>暂无封面</span>
              </div>
            </div>
            <div class="cover-upload-actions">
              <a-upload
                :show-upload-list="false"
                :before-upload="handleCoverUpload"
                accept="image/*"
              >
                <a-button :loading="coverUploading">
                  <upload-outlined /> 选择本地图片
                </a-button>
              </a-upload>
              <div class="cover-upload-tip">点击按钮后会打开本地文件夹，上传成功后自动回填封面。</div>
              <a-button v-if="courseForm.coverImage" type="link" danger @click="courseForm.coverImage = ''">
                清除封面
              </a-button>
            </div>
          </div>
        </a-form-item>

        <a-form-item label="课程类型">
          <a-radio-group v-model:value="courseForm.requiresPoints">
            <a-radio :value="0">免费</a-radio>
            <a-radio :value="1">付费</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item v-if="courseForm.requiresPoints === 1" label="购买积分" name="pointsCost">
          <a-input-number
            v-model:value="courseForm.pointsCost"
            :min="1"
            :precision="0"
            style="width: 100%"
            placeholder="请输入解锁课程需要的积分"
          />
        </a-form-item>

        <div class="modal-actions">
          <a-button @click="closeCourseModal">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="saving">
            {{ editingCourse ? '保存修改' : '创建课程' }}
          </a-button>
        </div>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="showChapterModal"
      :title="selectedCourse ? `章节管理 - ${selectedCourse.name}` : '章节管理'"
      :footer="null"
      width="760px"
      :destroy-on-close="true"
      @cancel="closeChapterModal"
    >
      <a-spin :spinning="chapterLoading">
        <div v-if="selectedCourse" class="chapter-manager">
          <div class="chapter-manager-head">
            <div>
              <div class="chapter-manager-title">{{ selectedCourse.name }}</div>
              <div class="chapter-manager-subtitle">先新增章节，再把课程视频上传到对应章节。</div>
            </div>
            <a-tag color="blue">{{ selectedCourse.chapters.length }} 个章节</a-tag>
          </div>

          <div class="add-chapter">
            <a-input
              v-model:value="newChapterTitle"
              placeholder="输入章节标题，例如：第一章 课程导读"
              @pressEnter="handleAddChapter"
            />
            <a-button type="primary" :loading="chapterAdding" @click="handleAddChapter">
              添加章节
            </a-button>
          </div>

          <div v-if="selectedCourse.chapters.length" class="chapter-list">
            <div v-for="chapter in selectedCourse.chapters" :key="chapter.chapterId" class="chapter-card">
              <div class="chapter-head">
                <div class="chapter-head-left">
                  <folder-outlined class="chapter-icon" />
                  <div>
                    <div class="chapter-name">{{ chapter.title }}</div>
                    <div class="chapter-meta">{{ chapter.videos.length }} 个视频</div>
                  </div>
                </div>
                <a-upload
                  :show-upload-list="false"
                  :before-upload="(file) => handleVideoUpload(chapter, file)"
                  accept="video/*"
                >
                  <a-button size="small" :loading="uploadingChapterId === chapter.chapterId">
                    <upload-outlined /> 上传视频
                  </a-button>
                </a-upload>
              </div>

              <div v-if="chapter.videos.length" class="video-list">
                <div v-for="video in chapter.videos" :key="video.videoId" class="video-row">
                  <div class="video-info">
                    <play-square-outlined class="video-icon" />
                    <span class="video-title">{{ video.title }}</span>
                  </div>
                  <span class="video-duration">{{ formatDuration(video.duration) }}</span>
                </div>
              </div>
              <div v-else class="empty-videos">当前章节还没有视频，点击右侧按钮上传。</div>
            </div>
          </div>

          <div v-else class="no-chapters">
            还没有章节。先添加章节，随后就能看到对应的“上传视频”按钮。
          </div>
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  adminAddChapter,
  adminAddCourse,
  adminDeleteCourse,
  adminUpdateCourse,
  adminUploadCourseCover,
  adminUploadVideo,
  getAdminCourseDetail,
  getAdminCourseList,
  getGradeList,
  getSubjectList
} from '@/api/index.js'
import {
  FolderOutlined,
  PictureOutlined,
  PlayCircleOutlined,
  PlaySquareOutlined,
  PlusOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'

const columns = [
  { title: '课程', key: 'name', width: '34%' },
  { title: '分类', key: 'tags', width: 180 },
  { title: '费用', key: 'pricing', width: 120 },
  { title: '主讲', dataIndex: 'teacherName', width: 120 },
  { title: '章节数', key: 'chapterCount', width: 100 },
  { title: '操作', key: 'action', width: 220 }
]

const courses = ref([])
const loading = ref(false)
const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 门课程`
})

const subjects = ref([])
const grades = ref([])
const subjectNameMap = ref({})
const gradeNameMap = ref({})

const showCourseModal = ref(false)
const editingCourse = ref(null)
const saving = ref(false)
const coverUploading = ref(false)
const courseFormRef = ref()

const showChapterModal = ref(false)
const selectedCourse = ref(null)
const chapterLoading = ref(false)
const chapterAdding = ref(false)
const uploadingChapterId = ref(null)
const newChapterTitle = ref('')

const createEmptyCourseForm = () => ({
  name: '',
  description: '',
  subjectId: undefined,
  gradeId: undefined,
  teacherName: '',
  coverImage: '',
  requiresPoints: 0,
  pointsCost: 0
})

const courseForm = reactive(createEmptyCourseForm())

const courseRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }],
  pointsCost: [{
    validator: (_, value) => {
      if (courseForm.requiresPoints !== 1 || Number(value) > 0) {
        return Promise.resolve()
      }
      return Promise.reject(new Error('付费课程的积分必须大于 0'))
    },
    trigger: 'change'
  }]
}

function resetCourseForm() {
  Object.assign(courseForm, createEmptyCourseForm())
}

function normalizeCourseRecord(record = {}) {
  return {
    ...record,
    id: record.id ?? record.courseId ?? null,
    coverImage: record.coverImage ?? '',
    chapterCount: record.chapterCount ?? 0,
    pointsCost: Number(record.pointsCost ?? 0),
    requiresPoints: Number(record.requiresPoints ?? 0)
  }
}

function normalizeCourseDetail(course = {}) {
  return {
    ...course,
    id: course.id ?? course.courseId ?? null,
    courseId: course.courseId ?? course.id ?? null,
    coverImage: course.coverImage ?? '',
    chapters: Array.isArray(course.chapters)
      ? course.chapters.map((chapter) => ({
          ...chapter,
          chapterId: chapter.chapterId ?? chapter.id ?? null,
          videos: Array.isArray(chapter.videos)
            ? chapter.videos.map((video) => ({
                ...video,
                videoId: video.videoId ?? video.id ?? null,
                duration: Number(video.duration ?? 0)
              }))
            : []
        }))
      : []
  }
}

function isPaidCourse(record) {
  return Number(record?.requiresPoints) === 1 && Number(record?.pointsCost) > 0
}

function getSubjectName(subjectId) {
  return subjectNameMap.value[subjectId] || '未分类'
}

function getGradeName(gradeId) {
  return gradeNameMap.value[gradeId] || '未分级'
}

function handleCourseCoverError(event) {
  if (event?.target && !event.target.src.endsWith('/course-cover-fallback.svg')) {
    event.target.src = '/course-cover-fallback.svg'
  }
}

function closeCourseModal() {
  showCourseModal.value = false
  editingCourse.value = null
  resetCourseForm()
}

function closeChapterModal() {
  showChapterModal.value = false
  selectedCourse.value = null
  newChapterTitle.value = ''
  uploadingChapterId.value = null
}

async function loadCourses() {
  loading.value = true
  try {
    const data = await getAdminCourseList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined
    })
    const records = data?.records || data || []
    courses.value = records.map(normalizeCourseRecord)
    pagination.total = data?.total || records.length || 0
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  const [subjectRes, gradeRes] = await Promise.allSettled([getSubjectList(), getGradeList()])
  if (subjectRes.status === 'fulfilled') {
    subjects.value = subjectRes.value || []
    subjectNameMap.value = subjects.value.reduce((map, item) => {
      map[item.id] = item.name
      return map
    }, {})
  }
  if (gradeRes.status === 'fulfilled') {
    grades.value = gradeRes.value || []
    gradeNameMap.value = grades.value.reduce((map, item) => {
      map[item.id] = item.name
      return map
    }, {})
  }
}

function openAddModal() {
  editingCourse.value = null
  resetCourseForm()
  showCourseModal.value = true
}

function openEditModal(record) {
  editingCourse.value = record
  Object.assign(courseForm, {
    name: record.name || '',
    description: record.description || '',
    subjectId: record.subjectId,
    gradeId: record.gradeId,
    teacherName: record.teacherName || '',
    coverImage: record.coverImage || '',
    requiresPoints: isPaidCourse(record) ? 1 : 0,
    pointsCost: isPaidCourse(record) ? Number(record.pointsCost) : 0
  })
  showCourseModal.value = true
}

async function handleCoverUpload(file) {
  coverUploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await adminUploadCourseCover(formData)
    courseForm.coverImage = data?.url || ''
    message.success('课程封面上传成功')
  } finally {
    coverUploading.value = false
  }
  return false
}

async function handleSaveCourse() {
  if (courseForm.requiresPoints === 1 && Number(courseForm.pointsCost) <= 0) {
    message.warning('付费课程必须设置大于 0 的积分')
    return
  }

  saving.value = true
  try {
    const payload = {
      name: courseForm.name.trim(),
      description: courseForm.description?.trim() || '',
      subjectId: courseForm.subjectId,
      gradeId: courseForm.gradeId,
      teacherName: courseForm.teacherName?.trim() || '',
      coverImage: courseForm.coverImage || '',
      requiresPoints: courseForm.requiresPoints,
      pointsCost: courseForm.requiresPoints === 1 ? Number(courseForm.pointsCost) : 0
    }

    if (editingCourse.value) {
      await adminUpdateCourse(editingCourse.value.id, payload)
      message.success('课程已更新')
    } else {
      await adminAddCourse(payload)
      message.success('课程已创建')
    }

    closeCourseModal()
    await loadCourses()
  } finally {
    saving.value = false
  }
}

async function handleDelete(courseId) {
  await adminDeleteCourse(courseId)
  message.success('课程已删除')
  await loadCourses()
}

async function reloadSelectedCourse(courseId = selectedCourse.value?.id) {
  if (!courseId) {
    return
  }
  chapterLoading.value = true
  try {
    const data = await getAdminCourseDetail(courseId)
    selectedCourse.value = normalizeCourseDetail(data)
  } finally {
    chapterLoading.value = false
  }
}

async function openChapterModal(record) {
  selectedCourse.value = normalizeCourseDetail({
    id: record.id,
    courseId: record.id,
    name: record.name,
    chapters: []
  })
  newChapterTitle.value = ''
  showChapterModal.value = true
  await reloadSelectedCourse(record.id)
}

async function handleAddChapter() {
  const title = newChapterTitle.value.trim()
  if (!title) {
    message.warning('请输入章节标题')
    return
  }
  if (!selectedCourse.value?.id) {
    return
  }

  chapterAdding.value = true
  try {
    await adminAddChapter(selectedCourse.value.id, {
      title,
      sort: (selectedCourse.value.chapters?.length || 0) + 1
    })
    message.success('章节添加成功')
    newChapterTitle.value = ''
    await Promise.all([reloadSelectedCourse(), loadCourses()])
  } finally {
    chapterAdding.value = false
  }
}

async function handleVideoUpload(chapter, file) {
  const chapterId = chapter.chapterId
  if (!chapterId) {
    return false
  }

  uploadingChapterId.value = chapterId
  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', file.name.replace(/\.[^.]+$/, '') || '未命名视频')
  formData.append('sort', String((chapter.videos?.length || 0) + 1))

  try {
    await adminUploadVideo(chapterId, formData)
    message.success('课程视频上传成功')
    await Promise.all([reloadSelectedCourse(), loadCourses()])
  } finally {
    uploadingChapterId.value = null
  }

  return false
}

function formatDuration(seconds) {
  if (!seconds) {
    return '时长待解析'
  }
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = Math.floor(seconds % 60)
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

function handleSearch() {
  pagination.current = 1
  loadCourses()
}

function handleTableChange(pag) {
  pagination.current = pag.current
  loadCourses()
}

onMounted(() => {
  loadOptions()
  loadCourses()
})
</script>

<style scoped>
.admin-courses-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  color: #1f2937;
  font-size: 22px;
  font-weight: 700;
}

.page-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-card {
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.course-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-mini-cover {
  width: 64px;
  height: 46px;
  border-radius: 10px;
  object-fit: cover;
  background: #f3f4f6;
  flex-shrink: 0;
}

.course-main-info {
  min-width: 0;
}

.course-name {
  color: #111827;
  font-weight: 600;
}

.course-desc {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  line-height: 1.5;
}

.cover-uploader {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cover-preview-box {
  width: 220px;
  height: 132px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5efe2 0%, #ece7dd 100%);
  border: 1px solid rgba(148, 163, 184, 0.28);
  flex-shrink: 0;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-preview-empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
}

.cover-preview-empty :deep(svg) {
  font-size: 28px;
}

.cover-upload-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.cover-upload-tip {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.chapter-manager {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 260px;
}

.chapter-manager-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.chapter-manager-title {
  color: #111827;
  font-size: 18px;
  font-weight: 700;
}

.chapter-manager-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.add-chapter {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chapter-card {
  border: 1px solid rgba(226, 232, 240, 0.95);
  border-radius: 16px;
  background: #fbfdff;
  padding: 16px;
}

.chapter-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.chapter-head-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chapter-icon {
  color: #2563eb;
  font-size: 18px;
}

.chapter-name {
  color: #111827;
  font-size: 15px;
  font-weight: 600;
}

.chapter-meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.video-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.video-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
}

.video-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.video-icon {
  color: #0f766e;
}

.video-title {
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.video-duration {
  color: #64748b;
  font-size: 12px;
  flex-shrink: 0;
}

.empty-videos,
.no-chapters {
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.empty-videos {
  margin-top: 14px;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
}

.no-chapters {
  padding: 28px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .page-top,
  .page-actions,
  .cover-uploader,
  .chapter-head,
  .chapter-manager-head {
    flex-direction: column;
    align-items: stretch;
  }

  .add-chapter {
    grid-template-columns: 1fr;
  }

  .cover-preview-box {
    width: 100%;
    height: 180px;
  }

  .video-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

