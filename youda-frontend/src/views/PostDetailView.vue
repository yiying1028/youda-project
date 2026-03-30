<template>
  <div class="post-detail-page">
    <div class="post-detail-inner">
      <a-button type="link" class="back-btn" @click="$router.push('/community')">
        <left-outlined /> 返回社区
      </a-button>

      <a-spin :spinning="loading">
        <div v-if="post" class="post-layout">
          <div class="post-main">
            <a-card :bordered="false" class="post-card">
              <div class="post-tags">
                <a-tag v-if="post.categoryName" color="blue">{{ post.categoryName }}</a-tag>
                <a-tag v-if="post.isTop" color="red">置顶</a-tag>
              </div>

              <h1 class="post-title">{{ post.title }}</h1>

              <div class="post-author-bar">
                <div class="author-wrap">
                  <a-avatar :src="post.authorAvatar" :size="42" class="author-avatar">
                    {{ post.authorName?.charAt(0) || '匿' }}
                  </a-avatar>
                  <div class="author-info">
                    <div class="author-name">{{ post.authorName || '匿名用户' }}</div>
                    <div class="post-time">发布于 {{ formatTime(post.createTime) }}</div>
                  </div>
                </div>

                <div class="post-stats">
                  <span><eye-outlined /> {{ post.viewCount || 0 }} 浏览</span>
                  <span><like-outlined /> {{ post.likeCount || 0 }} 点赞</span>
                  <span><star-outlined /> {{ post.collectCount || 0 }} 收藏</span>
                  <span><message-outlined /> {{ post.commentCount || 0 }} 评论</span>
                </div>
              </div>

              <div class="post-content">{{ post.content }}</div>

              <div class="post-actions">
                <a-button
                  :type="post.isLiked ? 'primary' : 'default'"
                  @click="handleLike"
                  :loading="likeLoading"
                >
                  <template #icon>
                    <like-filled v-if="post.isLiked" />
                    <like-outlined v-else />
                  </template>
                  {{ post.isLiked ? '已点赞' : '点赞' }} ({{ post.likeCount || 0 }})
                </a-button>

                <a-button
                  :type="post.isCollected ? 'primary' : 'default'"
                  @click="handleCollect"
                  :loading="collectLoading"
                >
                  <template #icon>
                    <star-filled v-if="post.isCollected" />
                    <star-outlined v-else />
                  </template>
                  {{ post.isCollected ? '已收藏' : '收藏' }} ({{ post.collectCount || 0 }})
                </a-button>
              </div>
            </a-card>

            <a-card :bordered="false" class="comment-card" title="评论区">
              <div v-if="userStore.isLoggedIn" class="comment-input">
                <a-avatar :size="36" class="comment-user-avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || '我' }}
                </a-avatar>
                <div class="comment-editor">
                  <a-textarea
                    v-model:value="commentContent"
                    placeholder="说说你的看法，或补充解题思路"
                    :rows="4"
                    :maxlength="500"
                    show-count
                  />
                  <div class="comment-submit">
                    <a-button type="primary" :loading="commentLoading" @click="submitComment">
                      发表评论
                    </a-button>
                  </div>
                </div>
              </div>
              <a-alert
                v-else
                message="登录后才能发表评论"
                type="info"
                show-icon
                style="margin-bottom: 16px"
              >
                <template #action>
                  <a-button size="small" type="primary" @click="$router.push('/login')">
                    去登录
                  </a-button>
                </template>
              </a-alert>

              <a-divider />

              <a-spin :spinning="commentLoading && comments.length === 0">
                <div v-if="comments.length === 0" class="empty-comments">暂无评论，来抢沙发吧。</div>
                <div v-else>
                  <div v-for="comment in comments" :key="comment.id" class="comment-item">
                    <a-avatar :src="comment.authorAvatar" :size="36" class="comment-avatar">
                      {{ comment.authorName?.charAt(0) || '评' }}
                    </a-avatar>
                    <div class="comment-body">
                      <div class="comment-header">
                        <span class="comment-author">{{ comment.authorName || '匿名用户' }}</span>
                        <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                        <a-popconfirm
                          v-if="userStore.userInfo?.id === comment.authorId"
                          title="确定删除这条评论吗？"
                          @confirm="handleDeleteComment(comment.id)"
                        >
                          <a-button type="link" danger size="small">删除</a-button>
                        </a-popconfirm>
                      </div>
                      <div class="comment-content">{{ comment.content }}</div>
                    </div>
                  </div>
                </div>
              </a-spin>

              <div class="comment-pagination" v-if="commentTotal > pageSize">
                <a-pagination
                  v-model:current="commentPage"
                  :total="commentTotal"
                  :page-size="pageSize"
                  size="small"
                  @change="loadComments"
                />
              </div>
            </a-card>
          </div>

          <div class="post-sidebar">
            <a-card :bordered="false" class="author-card" title="作者信息">
              <div class="author-profile">
                <a-avatar :src="post.authorAvatar" :size="64" class="author-avatar-lg">
                  {{ post.authorName?.charAt(0) || '匿' }}
                </a-avatar>
                <div class="author-name-big">{{ post.authorName || '匿名用户' }}</div>
                <div class="author-summary">这篇帖子发布于 {{ formatTime(post.createTime) }}</div>
              </div>
            </a-card>
          </div>
        </div>

        <a-result
          v-else-if="!loading"
          status="404"
          title="帖子不存在"
          sub-title="该帖子可能已被删除，或链接地址无效。"
        >
          <template #extra>
            <a-button type="primary" @click="$router.push('/community')">返回社区</a-button>
          </template>
        </a-result>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import {
  getPostDetail,
  getPostComments,
  togglePostLike,
  togglePostCollect,
  addComment,
  deleteComment
} from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  LeftOutlined,
  EyeOutlined,
  LikeOutlined,
  LikeFilled,
  StarOutlined,
  StarFilled,
  MessageOutlined
} from '@ant-design/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const loading = ref(true)
const comments = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const pageSize = 15
const commentContent = ref('')
const commentLoading = ref(false)
const likeLoading = ref(false)
const collectLoading = ref(false)

async function loadPost() {
  loading.value = true
  try {
    if (!route.params.id) {
      post.value = null
      return
    }
    const data = await getPostDetail(route.params.id)
    post.value = data || null
  } catch {
    post.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments(page = commentPage.value) {
  commentPage.value = page
  try {
    if (!route.params.id) {
      comments.value = []
      commentTotal.value = 0
      return
    }
    const data = await getPostComments(route.params.id, {
      current: commentPage.value,
      size: pageSize
    })
    comments.value = data?.records || data || []
    commentTotal.value = data?.total || 0
  } catch {
    comments.value = []
    commentTotal.value = 0
  }
}

async function handleLike() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  if (!post.value?.id) return

  likeLoading.value = true
  try {
    const result = await togglePostLike(post.value.id)
    post.value.isLiked = result?.isLiked ?? !post.value.isLiked
    post.value.likeCount = result?.likeCount ?? post.value.likeCount
  } finally {
    likeLoading.value = false
  }
}

async function handleCollect() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    return
  }
  if (!post.value?.id) return

  collectLoading.value = true
  try {
    const result = await togglePostCollect(post.value.id)
    post.value.isCollected = result?.isCollected ?? !post.value.isCollected
    post.value.collectCount = result?.collectCount ?? post.value.collectCount
  } finally {
    collectLoading.value = false
  }
}

async function submitComment() {
  if (!commentContent.value.trim()) {
    message.warning('评论内容不能为空')
    return
  }
  if (!post.value?.id) return

  commentLoading.value = true
  try {
    await addComment(post.value.id, { content: commentContent.value.trim() })
    message.success('评论发表成功')
    commentContent.value = ''
    commentPage.value = 1
    await loadComments(1)
    post.value.commentCount = (post.value.commentCount || 0) + 1
  } finally {
    commentLoading.value = false
  }
}

async function handleDeleteComment(id) {
  try {
    await deleteComment(id)
    message.success('评论已删除')
    await loadComments(commentPage.value)
    if (post.value) {
      post.value.commentCount = Math.max(0, (post.value.commentCount || 0) - 1)
    }
  } catch {}
}

function formatTime(time) {
  return dayjs(time).fromNow()
}

async function refreshPage() {
  await Promise.all([loadPost(), loadComments(1)])
}

watch(
  () => route.params.id,
  () => {
    refreshPage()
  }
)

onMounted(() => {
  refreshPage()
})
</script>

<style scoped>
.post-detail-page {
  min-height: calc(100vh - 64px);
  background: transparent;
  padding-bottom: 40px;
}

.post-detail-inner {
  width: min(1100px, calc(100% - 48px));
  margin: 0 auto;
  padding: 24px 0;
}

.back-btn {
  margin-bottom: 16px;
  padding-left: 0;
  color: var(--text-soft);
  font-size: 14px;
}

.post-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
  align-items: start;
}

.post-card,
.comment-card,
.author-card {
  border-radius: 24px;
}

.post-card {
  margin-bottom: 16px;
}

.post-tags {
  margin-bottom: 14px;
}

.post-title {
  margin: 0 0 22px;
  font-family: var(--font-display);
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.18;
  color: var(--text-main);
  overflow-wrap: anywhere;
}

.post-author-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(24, 32, 42, 0.08);
  margin-bottom: 20px;
}

.author-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.author-avatar,
.author-avatar-lg,
.comment-user-avatar,
.comment-avatar {
  background: linear-gradient(135deg, var(--accent), var(--accent-strong));
  flex-shrink: 0;
}

.author-info {
  min-width: 0;
}

.author-name,
.comment-author,
.author-name-big {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
}

.post-time,
.comment-time,
.author-summary {
  font-size: 12px;
  color: var(--text-soft);
}

.post-stats {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: flex-end;
  font-size: 13px;
  color: var(--text-soft);
}

.post-stats span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.post-content {
  font-size: 15px;
  line-height: 1.95;
  color: #3d4650;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: anywhere;
  padding: 20px 0;
  border-bottom: 1px solid rgba(24, 32, 42, 0.08);
  margin-bottom: 20px;
}

.post-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.comment-editor {
  flex: 1;
  min-width: 0;
}

.comment-submit {
  margin-top: 8px;
  text-align: right;
}

.empty-comments {
  text-align: center;
  color: var(--text-faint);
  padding: 32px 0;
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(24, 32, 42, 0.06);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.comment-content {
  font-size: 14px;
  color: #434343;
  line-height: 1.8;
  word-break: break-word;
  overflow-wrap: anywhere;
}

.comment-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.post-sidebar {
  position: sticky;
  top: 88px;
}

.author-profile {
  text-align: center;
}

.author-avatar-lg {
  margin-bottom: 12px;
}

.author-name-big {
  font-size: 18px;
}

.author-summary {
  margin-top: 8px;
  line-height: 1.8;
}

@media (max-width: 900px) {
  .post-detail-inner {
    width: min(1100px, calc(100% - 24px));
  }

  .post-layout {
    grid-template-columns: 1fr;
  }

  .post-sidebar {
    position: static;
  }

  .post-author-bar {
    flex-direction: column;
  }

  .post-stats {
    justify-content: flex-start;
  }

  .comment-input {
    align-items: flex-start;
  }
}
</style>
