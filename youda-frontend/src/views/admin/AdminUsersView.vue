<template>
  <div class="admin-users-page">
    <!-- 页面标题 + 搜索 -->
    <div class="page-top">
      <h2 class="page-title">用户管理</h2>
      <a-input-search
        v-model:value="keyword"
        placeholder="搜索用户名/昵称..."
        style="width: 260px"
        @search="handleSearch"
        allow-clear
      />
    </div>

    <!-- 用户数据表格 -->
    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="users"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 用户信息列（头像+昵称） -->
          <template v-if="column.key === 'user'">
            <div class="user-cell">
              <a-avatar :src="record.avatar" :size="36" style="background-color: #1677ff">
                {{ record.nickname?.charAt(0) || '用' }}
              </a-avatar>
              <div>
                <div class="user-name">{{ record.nickname }}</div>
                <div class="user-uname">@{{ record.username }}</div>
              </div>
            </div>
          </template>

          <!-- 角色列 -->
          <template v-else-if="column.key === 'role'">
            <a-tag :color="record.role === 'admin' ? 'red' : 'blue'">
              {{ record.role === 'admin' ? '管理员' : '学生' }}
            </a-tag>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? '正常' : '已禁用'"
            />
          </template>

          <!-- 注册时间列 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-popconfirm
              :title="record.status === 1 ? '确定禁用该用户？' : '确定启用该用户？'"
              @confirm="toggleUserStatus(record)"
            >
              <a-button
                :type="record.status === 1 ? 'default' : 'primary'"
                size="small"
                :danger="record.status === 1"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminUserList, updateUserStatus } from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const users = ref([])
const loading = ref(false)
const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: (total) => `共 ${total} 名用户`,
  showQuickJumper: true
})

// 表格列定义
const columns = [
  { title: '用户', key: 'user', width: '30%' },
  { title: '角色', key: 'role', width: 80 },
  { title: '手机号', dataIndex: 'phone', width: 130 },
  { title: '状态', key: 'status', width: 90 },
  { title: '注册时间', key: 'createTime', width: 140 },
  { title: '操作', key: 'action', width: 80 }
]

/**
 * 加载用户列表
 */
async function loadUsers() {
  loading.value = true
  try {
    const data = await getAdminUserList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined
    })
    users.value = data?.records || data || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 切换用户启用/禁用状态
 */
async function toggleUserStatus(record) {
  const newStatus = record.status === 1 ? 0 : 1
  try {
    await updateUserStatus(record.id, newStatus)
    record.status = newStatus
    message.success(newStatus === 1 ? '用户已启用' : '用户已禁用')
  } catch {}
}

/**
 * 搜索
 */
function handleSearch() {
  pagination.current = 1
  loadUsers()
}

/**
 * 表格分页/排序变化
 */
function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadUsers()
}

/**
 * 格式化日期
 */
function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-users-page {
  /* 用户管理页面 */
}

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

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.user-uname {
  font-size: 12px;
  color: #8c8c8c;
}
</style>
