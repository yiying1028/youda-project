<template>
  <div class="user-center-page">
    <div class="user-center-inner">
      <div class="user-hero">
        <div class="hero-avatar">
          <a-avatar :src="userStore.userInfo?.avatar" :size="84" style="background-color: #1677ff">
            {{ userStore.userInfo?.nickname?.charAt(0) || '优' }}
          </a-avatar>
          <a-upload
            :show-upload-list="false"
            :before-upload="handleAvatarUpload"
            accept="image/*"
            class="avatar-upload"
          >
            <div class="avatar-edit-btn">
              <camera-outlined />
            </div>
          </a-upload>
        </div>

        <div class="hero-info">
          <div class="hero-name">{{ userStore.userInfo?.nickname || '用户' }}</div>
          <div class="hero-username">@{{ userStore.userInfo?.username }}</div>
          <div class="hero-bio">{{ userStore.userInfo?.bio || '记录你的学习节奏，沉淀你的错题与成长。' }}</div>
          <div class="hero-actions">
            <router-link to="/notebook"><a-button size="small">我的错题本</a-button></router-link>
            <router-link to="/checkin"><a-button size="small">签到积分</a-button></router-link>
            <router-link to="/ranking"><a-button size="small">积分排行</a-button></router-link>
          </div>
        </div>
      </div>

      <div class="stats-grid">
        <a-card :bordered="false" class="stat-card accent-blue">
          <div class="stat-label">当前积分</div>
          <div class="stat-value">{{ pointsOverview.points || 0 }}</div>
          <div class="stat-sub">今日新增 {{ pointsOverview.todayEarnedPoints || 0 }} 分</div>
        </a-card>
        <a-card :bordered="false" class="stat-card accent-orange">
          <div class="stat-label">连续签到</div>
          <div class="stat-value">{{ pointsOverview.continuousDays || 0 }}</div>
          <div class="stat-sub">{{ pointsOverview.todayCheckedIn ? '今天已签到' : '今天还未签到' }}</div>
        </a-card>
        <a-card :bordered="false" class="stat-card accent-green">
          <div class="stat-label">错题总数</div>
          <div class="stat-value">{{ notebookStats.totalCount || 0 }}</div>
          <div class="stat-sub">待复习 {{ notebookStats.reviewCount || 0 }} 题</div>
        </a-card>
        <a-card :bordered="false" class="stat-card accent-purple">
          <div class="stat-label">学习记录</div>
          <div class="stat-value">{{ learningRecords.length }}</div>
          <div class="stat-sub">最近学习轨迹</div>
        </a-card>
      </div>

      <div class="quick-grid">
        <router-link to="/user/posts" class="quick-card">
          <file-text-outlined />
          <span>我的帖子</span>
        </router-link>
        <router-link to="/user/favorites" class="quick-card">
          <star-outlined />
          <span>我的收藏</span>
        </router-link>
        <router-link to="/notebook/review" class="quick-card">
          <book-outlined />
          <span>错题复习</span>
        </router-link>
        <router-link to="/checkin" class="quick-card">
          <calendar-outlined />
          <span>每日打卡</span>
        </router-link>
      </div>

      <a-row :gutter="16" class="record-row">
        <a-col :xs="24" :lg="14">
          <a-card :bordered="false" class="panel-card" title="最近学习记录">
            <a-empty v-if="learningRecords.length === 0" description="还没有学习记录" />
            <div v-else class="record-list">
              <div v-for="item in learningRecords.slice(0, 4)" :key="item.courseId" class="record-item">
                <div>
                  <div class="record-title">{{ item.courseName }}</div>
                  <div class="record-time">最近学习：{{ formatTime(item.lastStudyTime) }}</div>
                </div>
                <a-progress :percent="item.progress || 0" size="small" style="width: 120px" />
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="10">
          <a-card :bordered="false" class="panel-card" title="学习提醒">
            <div class="reminder-item">
              <span>积分排名</span>
              <strong>第 {{ pointsOverview.currentRank || '-' }} 名</strong>
            </div>
            <div class="reminder-item">
              <span>已掌握错题</span>
              <strong>{{ notebookStats.masteredCount || 0 }} 题</strong>
            </div>
            <div class="reminder-item">
              <span>累计签到</span>
              <strong>{{ pointsOverview.totalCheckinDays || 0 }} 天</strong>
            </div>
            <a-button type="primary" block style="margin-top: 12px" @click="$router.push('/checkin')">
              去签到并查看积分
            </a-button>
          </a-card>
        </a-col>
      </a-row>

      <a-tabs v-model:activeKey="activeTab" class="user-tabs">
        <a-tab-pane key="profile" tab="基本资料">
          <a-card :bordered="false" class="tab-card">
            <a-form
              :model="profileForm"
              :rules="profileRules"
              ref="profileFormRef"
              layout="vertical"
              style="max-width: 520px"
              @finish="handleUpdateProfile"
            >
              <a-form-item label="昵称" name="nickname">
                <a-input v-model:value="profileForm.nickname" placeholder="请输入昵称" :maxlength="20" />
              </a-form-item>
              <a-form-item label="手机号" name="phone">
                <a-input v-model:value="profileForm.phone" placeholder="请输入手机号" />
              </a-form-item>
              <a-form-item label="个人简介" name="bio">
                <a-textarea
                  v-model:value="profileForm.bio"
                  placeholder="介绍一下你的学习目标、擅长科目或最近计划"
                  :rows="4"
                  :maxlength="200"
                  show-count
                />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" html-type="submit" :loading="profileLoading">保存修改</a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="password" tab="修改密码">
          <a-card :bordered="false" class="tab-card">
            <a-form
              :model="pwdForm"
              :rules="pwdRules"
              ref="pwdFormRef"
              layout="vertical"
              style="max-width: 520px"
              @finish="handleUpdatePassword"
            >
              <a-form-item label="当前密码" name="oldPassword">
                <a-input-password v-model:value="pwdForm.oldPassword" placeholder="请输入当前密码" />
              </a-form-item>
              <a-form-item label="新密码" name="newPassword">
                <a-input-password v-model:value="pwdForm.newPassword" placeholder="请输入新密码（6-20 位）" />
              </a-form-item>
              <a-form-item label="确认新密码" name="confirmPassword">
                <a-input-password v-model:value="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" html-type="submit" :loading="pwdLoading">修改密码</a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user.js'
import {
  getLearningRecords,
  getNotebookStats,
  getPointsOverview,
  updatePassword,
  updateUserInfo,
  uploadAvatar
} from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  BookOutlined,
  CalendarOutlined,
  CameraOutlined,
  FileTextOutlined,
  StarOutlined
} from '@ant-design/icons-vue'

const userStore = useUserStore()
const activeTab = ref('profile')
const profileFormRef = ref()
const pwdFormRef = ref()
const profileLoading = ref(false)
const pwdLoading = ref(false)

const profileForm = reactive({ nickname: '', phone: '', bio: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const notebookStats = ref({ totalCount: 0, masteredCount: 0, reviewCount: 0 })
const pointsOverview = ref({})
const learningRecords = ref([])

const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称最多 20 个字符', trigger: 'blur' }
  ]
}

const validateConfirmPwd = async (_, value) => {
  if (value && value !== pwdForm.newPassword) {
    return Promise.reject('两次输入的新密码不一致')
  }
  return Promise.resolve()
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

function initForm() {
  if (userStore.userInfo) {
    profileForm.nickname = userStore.userInfo.nickname || ''
    profileForm.phone = userStore.userInfo.phone || ''
    profileForm.bio = userStore.userInfo.bio || ''
  }
}

async function loadDashboard() {
  const [pointsRes, notebookRes, recordsRes] = await Promise.allSettled([
    getPointsOverview(),
    getNotebookStats(),
    getLearningRecords()
  ])
  if (pointsRes.status === 'fulfilled') pointsOverview.value = pointsRes.value || {}
  if (notebookRes.status === 'fulfilled') notebookStats.value = notebookRes.value || notebookStats.value
  if (recordsRes.status === 'fulfilled') learningRecords.value = recordsRes.value || []
}

async function handleUpdateProfile() {
  profileLoading.value = true
  try {
    await updateUserInfo(profileForm)
    Object.assign(userStore.userInfo, profileForm)
    localStorage.setItem('youda_userInfo', JSON.stringify(userStore.userInfo))
    message.success('资料已更新')
  } finally {
    profileLoading.value = false
  }
}

async function handleUpdatePassword() {
  pwdLoading.value = true
  try {
    await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    message.success('密码修改成功，请重新登录')
    userStore.logout()
    window.location.href = '/login'
  } finally {
    pwdLoading.value = false
  }
}

async function handleAvatarUpload(file) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await uploadAvatar(formData)
    const avatarUrl = data?.url || data
    const payload = {
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      bio: profileForm.bio,
      avatar: avatarUrl
    }
    await updateUserInfo(payload)
    Object.assign(userStore.userInfo, payload)
    localStorage.setItem('youda_userInfo', JSON.stringify(userStore.userInfo))
    message.success('头像更新成功')
  } catch {
    return false
  }
  return false
}

function formatTime(time) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--'
}

onMounted(() => {
  initForm()
  loadDashboard()
})
</script>

<style scoped>
.user-center-page { min-height: calc(100vh - 64px); background: #f5f7fa; padding-bottom: 40px; }
.user-center-inner { max-width: 1120px; margin: 0 auto; padding: 24px; }
.user-hero { background: linear-gradient(135deg, #1677ff, #4096ff); border-radius: 16px; padding: 28px 32px; display: flex; align-items: center; gap: 24px; margin-bottom: 24px; color: #fff; }
.hero-avatar { position: relative; flex-shrink: 0; }
.avatar-upload { position: absolute; bottom: 0; right: 0; }
.avatar-edit-btn { width: 28px; height: 28px; background: rgba(255, 255, 255, 0.92); border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #1677ff; }
.hero-info { flex: 1; }
.hero-name { font-size: 26px; font-weight: 700; }
.hero-username, .hero-bio { color: rgba(255, 255, 255, 0.82); }
.hero-bio { margin: 8px 0 14px; }
.hero-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-bottom: 16px; }
.stat-card { border-radius: 16px; }
.stat-label { color: #8c8c8c; font-size: 13px; }
.stat-value { font-size: 30px; font-weight: 700; margin: 8px 0; }
.stat-sub { color: #595959; font-size: 13px; }
.accent-blue { background: linear-gradient(180deg, #f6faff, #ffffff); }
.accent-orange { background: linear-gradient(180deg, #fff8ef, #ffffff); }
.accent-green { background: linear-gradient(180deg, #f3fff8, #ffffff); }
.accent-purple { background: linear-gradient(180deg, #f7f5ff, #ffffff); }
.quick-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-bottom: 16px; }
.quick-card { background: #fff; border-radius: 14px; padding: 18px; display: flex; align-items: center; gap: 10px; color: #262626; text-decoration: none; box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06); }
.record-row { margin-bottom: 16px; }
.panel-card, .tab-card { border-radius: 16px; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05); }
.record-list { display: flex; flex-direction: column; gap: 14px; }
.record-item, .reminder-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.record-title { font-weight: 600; margin-bottom: 4px; }
.record-time { color: #8c8c8c; font-size: 13px; }
.user-tabs { margin-top: 8px; }
@media (max-width: 960px) { .stats-grid, .quick-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 768px) { .user-center-inner { padding: 16px; } .user-hero { flex-direction: column; align-items: flex-start; } }
</style>
