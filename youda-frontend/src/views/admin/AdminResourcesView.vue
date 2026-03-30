<template>
  <div class="admin-resources-page">
    <div class="page-top">
      <h2 class="page-title">资料管理</h2>
      <div class="page-actions">
        <a-input-search
          v-model:value="keyword"
          placeholder="搜索资料名称..."
          style="width: 240px"
          @search="handleSearch"
          allow-clear
        />
        <a-button type="primary" @click="showUploadModal = true">
          <upload-outlined /> 上传资料
        </a-button>
      </div>
    </div>

    <!-- 资料表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="resources"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 名称列 -->
          <template v-if="column.key === 'name'">
            <div class="name-cell">
              <file-outlined class="file-icon" />
              <span>{{ record.name }}</span>
            </div>
          </template>

          <!-- 科目+年级列 -->
          <template v-else-if="column.key === 'tags'">
            <a-tag color="blue" size="small">{{ record.subjectName }}</a-tag>
            <a-tag size="small">{{ record.gradeName }}</a-tag>
          </template>

          <!-- 文件大小列 -->
          <template v-else-if="column.key === 'fileSize'">
            {{ formatSize(record.fileSize) }}
          </template>

          <!-- 上传时间列 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-button
              type="link"
              size="small"
              @click="handleDownload(record.id)"
            >
              下载
            </a-button>
            <a-divider type="vertical" />
            <a-popconfirm
              title="确定删除该资料？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 上传资料弹窗 -->
    <a-modal
      v-model:open="showUploadModal"
      title="上传学习资料"
      :footer="null"
      :destroy-on-close="true"
      width="560px"
    >
      <a-form
        :model="uploadForm"
        :rules="uploadRules"
        ref="uploadFormRef"
        layout="vertical"
        @finish="handleUpload"
      >
        <!-- 资料名称 -->
        <a-form-item label="资料名称" name="name">
          <a-input v-model:value="uploadForm.name" placeholder="请输入资料名称" />
        </a-form-item>

        <!-- 科目选择 -->
        <a-form-item label="科目" name="subjectId">
          <a-select v-model:value="uploadForm.subjectId" placeholder="请选择科目">
            <a-select-option v-for="s in subjects" :key="s.id" :value="s.id">
              {{ s.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 年级选择 -->
        <a-form-item label="年级" name="gradeId">
          <a-select v-model:value="uploadForm.gradeId" placeholder="请选择年级">
            <a-select-option v-for="g in grades" :key="g.id" :value="g.id">
              {{ g.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <!-- 资料描述 -->
        <a-form-item label="资料描述" name="description">
          <a-textarea
            v-model:value="uploadForm.description"
            placeholder="请输入资料描述（可选）"
            :rows="3"
            :maxlength="500"
          />
        </a-form-item>

        <!-- 文件选择 -->
        <a-form-item label="选择文件" name="file">
          <a-upload
            :before-upload="handleFileSelect"
            :show-upload-list="true"
            :max-count="1"
          >
            <a-button>
              <upload-outlined /> 选择文件
            </a-button>
          </a-upload>
        </a-form-item>

        <!-- 提交按钮 -->
        <div style="display: flex; justify-content: flex-end; gap: 8px">
          <a-button @click="showUploadModal = false">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="uploading">
            开始上传
          </a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getAdminResourceList,
  adminDeleteResource,
  uploadResource,
  downloadResource,
  getSubjectList,
  getGradeList
} from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { UploadOutlined, FileOutlined } from '@ant-design/icons-vue'

const resources = ref([])
const loading = ref(false)
const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: (t) => `共 ${t} 份资料`,
  showQuickJumper: true
})

const subjects = ref([])
const grades = ref([])

// 上传弹窗状态
const showUploadModal = ref(false)
const uploading = ref(false)
const uploadFormRef = ref()
const selectedFile = ref(null)

const uploadForm = reactive({
  name: '',
  subjectId: null,
  gradeId: null,
  description: '',
  file: null
})

const uploadRules = {
  name: [{ required: true, message: '请输入资料名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }]
}

const columns = [
  { title: '资料名称', key: 'name', ellipsis: true, width: '30%' },
  { title: '分类', key: 'tags', width: 160 },
  { title: '上传者', dataIndex: 'uploaderName', width: 100 },
  { title: '大小', key: 'fileSize', width: 90 },
  { title: '下载数', dataIndex: 'downloadCount', width: 80 },
  { title: '上传时间', key: 'createTime', width: 130 },
  { title: '操作', key: 'action', width: 100 }
]

/**
 * 加载资料列表
 */
async function loadResources() {
  loading.value = true
  try {
    const data = await getAdminResourceList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined
    })
    resources.value = data?.records || data || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 记录选中的文件
 */
function handleFileSelect(file) {
  selectedFile.value = file
  return false // 阻止自动上传
}

/**
 * 执行上传
 */
async function handleUpload() {
  if (!selectedFile.value) {
    message.warning('请选择要上传的文件')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('name', uploadForm.name)
    formData.append('description', uploadForm.description || '')
    formData.append('subjectId', uploadForm.subjectId)
    formData.append('gradeId', uploadForm.gradeId)
    await uploadResource(formData)
    message.success('资料上传成功')
    showUploadModal.value = false
    selectedFile.value = null
    // 重置表单
    Object.assign(uploadForm, { name: '', subjectId: null, gradeId: null, description: '' })
    loadResources()
  } finally {
    uploading.value = false
  }
}

/**
 * 下载资料
 */
function handleDownload(id) {
  downloadResource(id)
}

/**
 * 删除资料
 */
async function handleDelete(id) {
  try {
    await adminDeleteResource(id)
    message.success('资料已删除')
    loadResources()
  } catch {}
}

/**
 * 格式化文件大小
 */
function formatSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

function handleSearch() {
  pagination.current = 1
  loadResources()
}

function handleTableChange(pag) {
  pagination.current = pag.current
  loadResources()
}

/**
 * 加载科目和年级选项
 */
async function loadOptions() {
  const [subData, gradeData] = await Promise.allSettled([getSubjectList(), getGradeList()])
  if (subData.status === 'fulfilled') subjects.value = subData.value || []
  if (gradeData.status === 'fulfilled') grades.value = gradeData.value || []
}

onMounted(() => {
  loadOptions()
  loadResources()
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

.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #1677ff;
  font-size: 16px;
}
</style>
