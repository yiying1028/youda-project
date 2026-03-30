<template>
  <div class="community-page">
    <div class="community-inner">
      <div class="page-header">
        <h1 class="page-title">学习社区</h1>
        <p class="page-desc">提问、追问、经验分享都在这里发生。</p>
      </div>

      <div class="community-layout">
        <div class="content-main">
          <div class="toolbar">
            <a-input-search
              v-model:value="searchKeyword"
              class="search-box"
              placeholder="搜索帖子标题或内容"
              allow-clear
              @search="handleSearch"
            />
            <a-button type="primary" @click="goCreatePost" class="create-btn">
              <template #icon><edit-outlined /></template>
              发帖提问
            </a-button>
          </div>

          <div class="filter-bar">
            <div class="filter-section">
              <span class="filter-label">分类</span>
              <a-tag
                :class="{ active: !selectedCategory }"
                class="filter-tag"
                @click="selectCategory(null)"
              >
                全部
              </a-tag>
              <a-tag
                v-for="cat in categories"
                :key="cat.id"
                :class="{ active: selectedCategory === cat.id }"
                class="filter-tag"
                @click="selectCategory(cat.id)"
              >
                {{ cat.name }}
              </a-tag>
            </div>
            <div class="filter-section">
              <span class="filter-label">排序</span>
              <a-tag
                :class="{ active: sortBy === 'latest' }"
                class="filter-tag"
                @click="changeSortBy('latest')"
              >
                最新
              </a-tag>
              <a-tag
                :class="{ active: sortBy === 'hot' }"
                class="filter-tag"
                @click="changeSortBy('hot')"
              >
                最热
              </a-tag>
            </div>
          </div>

          <a-spin :spinning="loading">
            <div v-if="posts.length === 0 && !loading" class="empty-state">
              <inbox-outlined class="empty-icon" />
              <p>暂无帖子，来发第一帖吧。</p>
              <a-button type="primary" @click="goCreatePost">立即发帖</a-button>
            </div>

            <div v-else class="post-list">
              <div
                v-for="post in posts"
                :key="post.id"
                class="post-card"
                @click="$router.push('/post/' + post.id)"
              >
                <div v-if="post.isTop" class="top-badge">置顶</div>

                <div class="post-card-inner">
                  <a-avatar
                    :src="post.authorAvatar"
                    :size="42"
                    class="post-avatar"
                  >
                    {{ post.authorName?.charAt(0) || '匿' }}
                  </a-avatar>

                  <div class="post-body">
                    <div class="post-top">
                      <span class="post-title">{{ post.title }}</span>
                      <a-tag v-if="post.categoryName" color="blue" size="small">
                        {{ post.categoryName }}
                      </a-tag>
                    </div>

                    <div class="post-summary">{{ post.summary || post.content }}</div>

                    <div class="post-meta">
                      <span class="post-author">{{ post.authorName || '匿名用户' }}</span>
                      <span class="meta-dot">·</span>
                      <span class="post-time">{{ formatTime(post.createTime) }}</span>
                      <span class="meta-dot">·</span>
                      <eye-outlined />
                      <span>{{ post.viewCount || 0 }}</span>
                      <span class="meta-dot">·</span>
                      <like-outlined />
                      <span>{{ post.likeCount || 0 }}</span>
                      <span class="meta-dot">·</span>
                      <message-outlined />
                      <span>{{ post.commentCount || 0 }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </a-spin>

          <div class="pagination-wrap" v-if="total > 0">
            <a-pagination
              v-model:current="currentPage"
              :total="total"
              :page-size="pageSize"
              show-quick-jumper
              :show-total="(count) => `共 ${count} 篇帖子`"
              @change="handlePageChange"
            />
          </div>
        </div>

        <div class="content-sidebar">
          <a-card class="sidebar-card" :bordered="false">
            <div class="sidebar-post-guide">
              <img :src="communityGuide" alt="发帖引导" class="guide-img" />
              <p class="guide-text">把题目背景、卡住的位置和已尝试的方法写清楚，更容易得到有效回复。</p>
              <a-button type="primary" block @click="goCreatePost">
                <edit-outlined /> 发帖提问
              </a-button>
            </div>
          </a-card>

          <a-card class="sidebar-card" :bordered="false" style="margin-top: 16px" title="分类导航">
            <div class="category-nav">
              <div
                v-for="cat in categories"
                :key="cat.id"
                class="cat-nav-item"
                @click="selectCategory(cat.id)"
              >
                <span class="cat-name">{{ cat.name }}</span>
                <right-outlined class="cat-arrow" />
              </div>
            </div>
          </a-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { getPostList, getCategoryList } from '@/api/index.js'
import communityGuide from '@/assets/community-guide.svg'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  EditOutlined,
  EyeOutlined,
  LikeOutlined,
  MessageOutlined,
  InboxOutlined,
  RightOutlined
} from '@ant-design/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

const posts = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const searchKeyword = ref('')
const selectedCategory = ref(null)
const sortBy = ref('latest')
const categories = ref([])

async function loadPosts() {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value
    }
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const data = await getPostList(params)
    posts.value = data?.records || data || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const data = await getCategoryList()
    categories.value = data || []
  } catch {
    categories.value = []
  }
}

function selectCategory(id) {
  selectedCategory.value = id
  currentPage.value = 1
  loadPosts()
}

function changeSortBy(sort) {
  sortBy.value = sort
  currentPage.value = 1
  loadPosts()
}

function handleSearch() {
  currentPage.value = 1
  loadPosts()
}

function handlePageChange(page) {
  currentPage.value = page
  loadPosts()
}

function goCreatePost() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后再发帖')
    router.push({ name: 'Login', query: { redirect: '/post/create' } })
    return
  }
  router.push('/post/create')
}

function formatTime(time) {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadCategories()
  loadPosts()
})
</script>

<style scoped>
.community-page {
  min-height: calc(100vh - 64px);
  background: transparent;
}

.community-inner {
  width: min(1200px, calc(100% - 48px));
  margin: 0 auto;
  padding: 24px 0 32px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 6px;
  font-family: var(--font-display);
  font-size: 32px;
  color: var(--text-main);
}

.page-desc {
  margin: 0;
  color: var(--text-soft);
  font-size: 14px;
}

.community-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.search-box {
  width: min(420px, 100%);
}

.create-btn {
  height: 42px;
  font-weight: 600;
}

.filter-bar {
  background: rgba(255, 250, 243, 0.82);
  border: 1px solid rgba(24, 32, 42, 0.08);
  border-radius: 20px;
  padding: 16px 18px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 14px;
  color: var(--text-soft);
  font-weight: 600;
  flex-shrink: 0;
}

.filter-tag {
  cursor: pointer;
  border-radius: 999px;
  transition: all 0.2s;
  user-select: none;
}

.filter-tag.active {
  background: var(--accent);
  color: #fff;
  border-color: var(--accent);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  background: rgba(255, 250, 243, 0.86);
  border: 1px solid rgba(24, 32, 42, 0.08);
  border-radius: 22px;
  padding: 0;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  overflow: hidden;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 40px rgba(24, 32, 42, 0.1);
}

.top-badge {
  position: absolute;
  top: 0;
  left: 0;
  background: var(--accent);
  color: #fff;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 0 0 10px 0;
}

.post-card-inner {
  display: flex;
  gap: 14px;
  padding: 18px;
}

.post-avatar {
  background: linear-gradient(135deg, var(--accent), var(--accent-strong));
  flex-shrink: 0;
}

.post-body {
  flex: 1;
  min-width: 0;
}

.post-top {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.post-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  flex: 1;
  min-width: 0;
  overflow-wrap: anywhere;
}

.post-summary {
  font-size: 14px;
  color: var(--text-soft);
  margin-bottom: 10px;
  line-height: 1.8;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--text-faint);
}

.post-author {
  color: var(--text-soft);
  font-weight: 600;
}

.meta-dot {
  color: rgba(24, 32, 42, 0.22);
}

.pagination-wrap {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: var(--text-faint);
}

.empty-icon {
  font-size: 56px;
  display: block;
  margin-bottom: 12px;
}

.sidebar-card {
  border-radius: 20px;
}

.sidebar-post-guide {
  text-align: center;
}

.guide-img {
  width: 88px;
  height: 88px;
  margin: 0 auto 14px;
  opacity: 0.72;
}

.guide-text {
  margin-bottom: 14px;
  color: var(--text-soft);
  line-height: 1.8;
}

.category-nav {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cat-nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.cat-nav-item:hover {
  background: rgba(255, 245, 235, 0.9);
  color: var(--accent);
}

.cat-name {
  font-size: 14px;
}

.cat-arrow {
  font-size: 12px;
  color: var(--text-faint);
}

@media (max-width: 900px) {
  .community-inner {
    width: min(1200px, calc(100% - 24px));
  }

  .community-layout {
    grid-template-columns: 1fr;
  }

  .content-sidebar {
    display: none;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    width: 100%;
  }
}
</style>
