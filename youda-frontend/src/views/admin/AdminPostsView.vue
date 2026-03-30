<template>
  <div class="admin-posts-page">
    <div class="page-top">
      <h2 class="page-title">帖子管理</h2>
      <a-input-search
        v-model:value="keyword"
        placeholder="搜索帖子标题..."
        style="width: 260px"
        @search="handleSearch"
        allow-clear
      />
    </div>

    <a-card :bordered="false" class="table-card">
      <a-table
        :columns="columns"
        :data-source="posts"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <!-- 标题列 -->
          <template v-if="column.key === 'title'">
            <div class="title-cell">
              <a-tag v-if="record.isTop" color="red" size="small">置顶</a-tag>
              <span>{{ record.title }}</span>
            </div>
          </template>

          <!-- 发帖人列 -->
          <template v-else-if="column.key === 'authorName'">
            {{ record.authorName }}
          </template>

          <!-- 统计列 -->
          <template v-else-if="column.key === 'stats'">
            <span class="mini-stat"><eye-outlined /> {{ record.viewCount || 0 }}</span>
            <span class="mini-stat"><like-outlined /> {{ record.likeCount || 0 }}</span>
            <span class="mini-stat"><message-outlined /> {{ record.commentCount || 0 }}</span>
          </template>

          <!-- 发帖时间列 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatDate(record.createTime) }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <!-- 置顶/取消置顶 -->
            <a-tooltip :title="record.isTop ? '取消置顶' : '设为置顶'">
              <a-button
                type="link"
                size="small"
                @click="toggleTop(record)"
              >
                <vertical-align-top-outlined :style="{ color: record.isTop ? '#1677ff' : '#bfbfbf' }" />
              </a-button>
            </a-tooltip>

            <!-- 删除 -->
            <a-popconfirm title="确定删除该帖子？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger size="small">删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAdminPostList, adminDeletePost, setPostTop } from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  EyeOutlined,
  LikeOutlined,
  MessageOutlined,
  VerticalAlignTopOutlined
} from '@ant-design/icons-vue'

const posts = ref([])
const loading = ref(false)
const keyword = ref('')
const pagination = reactive({
  current: 1,
  pageSize: 15,
  total: 0,
  showTotal: (total) => `共 ${total} 篇帖子`,
  showQuickJumper: true
})

const columns = [
  { title: '帖子标题', key: 'title', ellipsis: true, width: '40%' },
  { title: '发帖人', key: 'authorName', width: 100 },
  { title: '数据', key: 'stats', width: 160 },
  { title: '发帖时间', key: 'createTime', width: 130 },
  { title: '操作', key: 'action', width: 100 }
]

async function loadPosts() {
  loading.value = true
  try {
    const data = await getAdminPostList({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined
    })
    posts.value = data?.records || data || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 切换置顶状态
 */
async function toggleTop(record) {
  const newTop = record.isTop ? 0 : 1
  try {
    await setPostTop(record.id, newTop)
    record.isTop = newTop === 1
    message.success(newTop === 1 ? '帖子已置顶' : '已取消置顶')
  } catch {}
}

/**
 * 删除帖子
 */
async function handleDelete(id) {
  try {
    await adminDeletePost(id)
    message.success('帖子已删除')
    loadPosts()
  } catch {}
}

function handleSearch() {
  pagination.current = 1
  loadPosts()
}

function handleTableChange(pag) {
  pagination.current = pag.current
  loadPosts()
}

function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  loadPosts()
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

.title-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mini-stat {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-right: 10px;
  font-size: 12px;
  color: #8c8c8c;
}
</style>
