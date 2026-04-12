<template>
  <div class="dashboard-page">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2 class="welcome-title">欢迎回来，{{ userStore.userInfo?.nickname || '管理员' }}</h2>
        <p class="welcome-sub">今天是 {{ today }}，祝你工作顺利。</p>
      </div>
    </div>

    <a-row :gutter="20" class="stat-cards">
      <a-col :xs="24" :sm="12" :xl="6" v-for="stat in statCards" :key="stat.key">
        <a-card :bordered="false" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: stat.color }"><component :is="stat.icon" /></div>
            <div class="stat-info">
              <div class="stat-value"><a-skeleton-element v-if="loading" type="button" size="large" /><span v-else>{{ stat.value }}</span></div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
          <div class="stat-footer">
            <span class="stat-change" :class="stat.trend > 0 ? 'up' : 'down'">
              <arrow-up-outlined v-if="stat.trend > 0" />
              <arrow-down-outlined v-else />
              较上周 {{ Math.abs(stat.trend || 0) }}%
            </span>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="20" style="margin-top: 20px">
      <a-col :span="24">
        <a-card :bordered="false" title="快捷管理入口" class="quick-card">
          <div class="quick-actions">
            <div v-for="action in quickActions" :key="action.path" class="quick-action" @click="$router.push(action.path)">
              <div class="qa-icon" :style="{ background: action.color }"><component :is="action.icon" /></div>
              <span class="qa-label">{{ action.label }}</span>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { getAdminStatistics } from '@/api/index.js'
import dayjs from 'dayjs'
import { UserOutlined, MessageOutlined, FolderOutlined, PlayCircleOutlined, ArrowUpOutlined, ArrowDownOutlined, NotificationOutlined, ShoppingOutlined } from '@ant-design/icons-vue'

const userStore = useUserStore()
const loading = ref(true)
const today = dayjs().format('YYYY年MM月DD日 dddd')

const statCards = reactive([
  { key: 'users', label: '注册用户', value: 0, trend: 5, color: 'linear-gradient(135deg, #1677ff, #4096ff)', icon: UserOutlined },
  { key: 'posts', label: '社区帖子', value: 0, trend: 12, color: 'linear-gradient(135deg, #f5222d, #ff7875)', icon: MessageOutlined },
  { key: 'resources', label: '学习资料', value: 0, trend: 8, color: 'linear-gradient(135deg, #52c41a, #95de64)', icon: FolderOutlined },
  { key: 'courses', label: '课程数量', value: 0, trend: 3, color: 'linear-gradient(135deg, #fa8c16, #ffd591)', icon: PlayCircleOutlined }
])

const quickActions = [
  { path: '/admin/users', label: '用户管理', color: 'linear-gradient(135deg, #1677ff, #4096ff)', icon: UserOutlined },
  { path: '/admin/posts', label: '帖子管理', color: 'linear-gradient(135deg, #f5222d, #ff7875)', icon: MessageOutlined },
  { path: '/admin/resources', label: '资料管理', color: 'linear-gradient(135deg, #52c41a, #95de64)', icon: FolderOutlined },
  { path: '/admin/courses', label: '课程管理', color: 'linear-gradient(135deg, #fa8c16, #ffd591)', icon: PlayCircleOutlined },
  { path: '/admin/course-orders', label: '课程订单', color: 'linear-gradient(135deg, #13c2c2, #36cfc9)', icon: ShoppingOutlined },
  { path: '/admin/announcements', label: '公告管理', color: 'linear-gradient(135deg, #722ed1, #b37feb)', icon: NotificationOutlined }
]

async function loadStatistics() {
  loading.value = true
  try {
    const data = await getAdminStatistics()
    if (data) {
      statCards.find((s) => s.key === 'users').value = data.userCount || 0
      statCards.find((s) => s.key === 'posts').value = data.postCount || 0
      statCards.find((s) => s.key === 'resources').value = data.resourceCount || 0
      statCards.find((s) => s.key === 'courses').value = data.courseCount || 0
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadStatistics() })
</script>

<style scoped>
.welcome-banner { background: linear-gradient(135deg, #1677ff, #4096ff); border-radius: 12px; padding: 24px 28px; margin-bottom: 20px; color: #fff; }
.welcome-title { font-size: 22px; font-weight: 700; color: #fff; margin-bottom: 6px; }
.welcome-sub { color: rgba(255, 255, 255, 0.85); font-size: 14px; margin: 0; }
.stat-card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); height: 100%; transition: box-shadow 0.2s; }
.stat-card:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12); }
.stat-inner { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; flex-shrink: 0; }
.stat-value { font-size: 28px; font-weight: 700; color: #262626; line-height: 1; margin-bottom: 4px; }
.stat-label { font-size: 13px; color: #8c8c8c; }
.stat-footer { padding-top: 12px; border-top: 1px solid #f0f0f0; }
.stat-change { font-size: 12px; display: flex; align-items: center; gap: 4px; }
.stat-change.up { color: #52c41a; }
.stat-change.down { color: #ff4d4f; }
.quick-card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); }
.quick-actions { display: flex; gap: 16px; flex-wrap: wrap; }
.quick-action { display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; padding: 16px 20px; border-radius: 10px; transition: background 0.2s; min-width: 80px; }
.quick-action:hover { background: #f5f8ff; }
.qa-icon { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px; color: #fff; }
.qa-label { font-size: 13px; color: #595959; font-weight: 500; }
</style>
