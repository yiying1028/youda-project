<template>
  <div class="admin-announcements-page">
    <div class="page-top">
      <h2 class="page-title">公告管理</h2>
      <a-button type="primary" @click="openAddModal">
        <plus-outlined /> 发布公告
      </a-button>
    </div>

    <!-- 公告表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="announcements"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 标题列 -->
          <template v-if="column.key === 'title'">
            <span class="ann-title">{{ record.title }}</span>
          </template>

          <!-- 内容摘要列 -->
          <template v-else-if="column.key === 'content'">
            <span class="ann-content">{{ record.content }}</span>
          </template>

          <!-- 发布时间列 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openEditModal(record)">
              编辑
            </a-button>
            <a-divider type="vertical" />
            <a-popconfirm
              title="确定删除该公告？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑公告弹窗 -->
    <a-modal
      v-model:open="showModal"
      :title="editingId ? '编辑公告' : '发布新公告'"
      :footer="null"
      width="580px"
      :destroy-on-close="true"
    >
      <a-form
        :model="annForm"
        :rules="annRules"
        ref="annFormRef"
        layout="vertical"
        @finish="handleSave"
      >
        <!-- 公告标题 -->
        <a-form-item label="公告标题" name="title">
          <a-input
            v-model:value="annForm.title"
            placeholder="请输入公告标题"
            :maxlength="100"
            show-count
          />
        </a-form-item>

        <!-- 公告内容 -->
        <a-form-item label="公告内容" name="content">
          <a-textarea
            v-model:value="annForm.content"
            placeholder="请输入公告内容..."
            :rows="6"
            :maxlength="2000"
            show-count
          />
        </a-form-item>

        <div style="display: flex; justify-content: flex-end; gap: 8px">
          <a-button @click="showModal = false">取消</a-button>
          <a-button type="primary" html-type="submit" :loading="saving">
            {{ editingId ? '保存修改' : '发布公告' }}
          </a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getAdminAnnouncementList,
  adminAddAnnouncement,
  adminUpdateAnnouncement,
  adminDeleteAnnouncement
} from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PlusOutlined } from '@ant-design/icons-vue'

const announcements = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (t) => `共 ${t} 条公告`,
  showQuickJumper: true
})

// 弹窗状态
const showModal = ref(false)
const editingId = ref(null)
const saving = ref(false)
const annFormRef = ref()
const annForm = reactive({
  title: '',
  content: ''
})

// 表单校验规则
const annRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 100, message: '标题最多100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ]
}

// 表格列定义
const columns = [
  { title: '公告标题', key: 'title', width: '35%' },
  { title: '内容摘要', key: 'content', ellipsis: true },
  { title: '发布时间', key: 'createTime', width: 140 },
  { title: '操作', key: 'action', width: 100 }
]

/**
 * 加载公告列表
 */
async function loadAnnouncements() {
  loading.value = true
  try {
    const data = await getAdminAnnouncementList({
      current: pagination.current,
      size: pagination.pageSize
    })
    announcements.value = data?.records || data || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 打开新增公告弹窗
 */
function openAddModal() {
  editingId.value = null
  annForm.title = ''
  annForm.content = ''
  showModal.value = true
}

/**
 * 打开编辑公告弹窗
 */
function openEditModal(record) {
  editingId.value = record.id
  annForm.title = record.title
  annForm.content = record.content
  showModal.value = true
}

/**
 * 保存公告（新增或编辑）
 */
async function handleSave() {
  saving.value = true
  try {
    if (editingId.value) {
      await adminUpdateAnnouncement(editingId.value, annForm)
      message.success('公告已更新')
    } else {
      await adminAddAnnouncement(annForm)
      message.success('公告已发布')
    }
    showModal.value = false
    loadAnnouncements()
  } finally {
    saving.value = false
  }
}

/**
 * 删除公告
 */
async function handleDelete(id) {
  try {
    await adminDeleteAnnouncement(id)
    message.success('公告已删除')
    loadAnnouncements()
  } catch {}
}

/**
 * 格式化日期
 */
function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

/**
 * 分页变化
 */
function handleTableChange(pag) {
  pagination.current = pag.current
  loadAnnouncements()
}

onMounted(() => {
  loadAnnouncements()
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

.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.ann-title {
  font-weight: 500;
  color: #262626;
}

.ann-content {
  color: #8c8c8c;
  font-size: 13px;
}
</style>
