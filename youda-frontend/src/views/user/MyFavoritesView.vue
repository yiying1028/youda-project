<template>
  <div class="my-favorites-page">
    <div class="my-favorites-inner">
      <div class="page-header">
        <h1 class="page-title">我的收藏</h1>
      </div>

      <!-- 收藏列表 -->
      <a-card :bordered="false" class="favorites-card">
        <a-table
          :columns="columns"
          :data-source="favorites"
          :loading="loading"
          :pagination="pagination"
          row-key="id"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <!-- 标题列 -->
            <template v-if="column.key === 'title'">
              <router-link
                :to="'/post/' + record.postId"
                class="post-link"
              >
                {{ record.postTitle || record.title }}
              </router-link>
            </template>

            <!-- 原作者列 -->
            <template v-else-if="column.key === 'authorName'">
              <span>{{ record.authorName }}</span>
            </template>

            <!-- 分类列 -->
            <template v-else-if="column.key === 'categoryName'">
              <a-tag color="blue" size="small" v-if="record.categoryName">
                {{ record.categoryName }}
              </a-tag>
            </template>

            <!-- 收藏时间列 -->
            <template v-else-if="column.key === 'collectTime'">
              {{ formatDate(record.collectTime || record.createTime) }}
            </template>

            <!-- 操作列 -->
            <template v-else-if="column.key === 'action'">
              <a-button
                type="link"
                size="small"
                @click="$router.push('/post/' + (record.postId || record.id))"
              >
                查看
              </a-button>
            </template>
          </template>

          <!-- 空状态 -->
          <template #emptyText>
            <div class="empty-state">
              <star-outlined class="empty-icon" />
              <p>还没有收藏任何帖子</p>
              <a-button type="primary" @click="$router.push('/community')">
                去社区浏览
              </a-button>
            </div>
          </template>
        </a-table>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyFavorites } from '@/api/index.js'
import dayjs from 'dayjs'
import { StarOutlined } from '@ant-design/icons-vue'

const favorites = ref([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total) => `共收藏 ${total} 篇帖子`,
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
    title: '原作者',
    key: 'authorName',
    width: 120
  },
  {
    title: '分类',
    key: 'categoryName',
    width: 100
  },
  {
    title: '收藏时间',
    key: 'collectTime',
    width: 140
  },
  {
    title: '操作',
    key: 'action',
    width: 80
  }
]

/**
 * 加载我的收藏列表
 */
async function loadFavorites() {
  loading.value = true
  try {
    const data = await getMyFavorites({
      current: pagination.current,
      size: pagination.pageSize
    })
    favorites.value = data?.records || data || []
    pagination.total = data?.total || 0
  } finally {
    loading.value = false
  }
}

/**
 * 表格分页变化
 */
function handleTableChange(pag) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadFavorites()
}

/**
 * 格式化日期
 */
function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.my-favorites-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
  padding-bottom: 40px;
}

.my-favorites-inner {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin: 0;
}

.favorites-card {
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

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 48px 0;
  color: #bfbfbf;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.empty-state p {
  margin-bottom: 16px;
  font-size: 14px;
}
</style>
