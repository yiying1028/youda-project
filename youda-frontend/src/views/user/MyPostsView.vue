<template>
  <div class="my-posts-page">
    <div class="my-posts-inner">
      <div class="page-header">
        <h1 class="page-title">我的帖子</h1>
        <a-button type="primary" @click="$router.push('/post/create')">
          <edit-outlined /> 发新帖
        </a-button>
      </div>

      <!-- 帖子列表表格 -->
      <a-card :bordered="false" class="posts-card">
        <a-table
          :columns="columns"
          :data-source="posts"
          :loading="loading"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <!-- 标题列：可点击跳转 -->
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'title'">
              <router-link
                :to="'/post/' + record.id"
                class="post-link"
              >
                <span v-if="record.isTop" class="top-mark">[置顶]</span>
                {{ record.title }}
              </router-link>
            </template>

            <!-- 分类列 -->
            <template v-else-if="column.key === 'categoryName'">
              <a-tag color="blue" size="small">{{ record.categoryName }}</a-tag>
            </template>

            <!-- 数据统计列 -->
            <template v-else-if="column.key === 'stats'">
              <span class="stat-item"><eye-outlined /> {{ record.viewCount || 0 }}</span>
              <span class="stat-item"><like-outlined /> {{ record.likeCount || 0 }}</span>
              <span class="stat-item"><message-outlined /> {{ record.commentCount || 0 }}</span>
            </template>

            <!-- 发帖时间列 -->
            <template v-else-if="column.key === 'createTime'">
              {{ formatDate(record.createTime) }}
            </template>

            <!-- 操作列 -->
            <template v-else-if="column.key === 'action'">
              <a-button
                type="link"
                size="small"
                @click="$router.push('/post/' + record.id)"
              >
                查看
              </a-button>
              <a-divider type="vertical" />
              <a-popconfirm
                title="确定删除这篇帖子？"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyPosts, deletePost } from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  EditOutlined,
  EyeOutlined,
  LikeOutlined,
  MessageOutlined
} from '@ant-design/icons-vue'

const posts = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共 ${total} 篇帖子`,
  showQuickJumper: true
})

// 表格列定义
const columns = [
  {
    title: '帖子标题',
    key: 'title',
    ellipsis: true,
    width: '40%'
  },
  {
    title: '分类',
    key: 'categoryName',
    width: 100
  },
  {
    title: '互动数据',
    key: 'stats',
    width: 160
  },
  {
    title: '发帖时间',
    key: 'createTime',
    width: 140
  },
  {
    title: '操作',
    key: 'action',
    width: 100
  }
]

/**
 * 加载我的帖子列表
 */
async function loadPosts() {
  loading.value = true
  try {
    const data = await getMyPosts({
      current: pagination.current,
      size: pagination.pageSize
    })
    posts.value = data?.records || data || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 删除帖子
 */
async function handleDelete(id) {
  try {
    await deletePost(id)
    message.success('帖子已删除')
    loadPosts()
  } catch {}
}

/**
 * 表格分页/排序变化
 */
function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadPosts()
}

/**
 * 格式化日期
 */
function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.my-posts-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
  padding-bottom: 40px;
}

.my-posts-inner {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin: 0;
}

.posts-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.post-link {
  color: #262626;
  font-weight: 500;
  text-decoration: none;
}

.post-link:hover {
  color: #1677ff;
}

.top-mark {
  color: #ff4d4f;
  font-size: 12px;
  margin-right: 4px;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-right: 12px;
  font-size: 13px;
  color: #8c8c8c;
}
</style>
