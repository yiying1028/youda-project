<template>
  <div class="checkin-page">
    <div class="checkin-inner">
      <div class="hero-card">
        <div>
          <div class="hero-title">每日打卡与积分</div>
          <div class="hero-desc">
            签到、学习、发帖和整理错题都会转化为积分，持续学习就能稳定累计。
          </div>
        </div>
        <div class="hero-actions">
          <a-button size="large" @click="$router.push('/ranking')">查看排行榜</a-button>
          <a-button
            type="primary"
            size="large"
            :loading="checkinLoading"
            :disabled="overview.todayCheckedIn"
            @click="handleCheckin"
          >
            {{ overview.todayCheckedIn ? '今日已签到' : '立即签到' }}
          </a-button>
        </div>
      </div>

      <div class="stats-grid">
        <a-card :bordered="false" class="stat-card">
          <div class="stat-label">总积分</div>
          <div class="stat-value">{{ overview.points || 0 }}</div>
        </a-card>
        <a-card :bordered="false" class="stat-card">
          <div class="stat-label">今日获得</div>
          <div class="stat-value">{{ overview.todayEarnedPoints || 0 }}</div>
        </a-card>
        <a-card :bordered="false" class="stat-card">
          <div class="stat-label">连续签到</div>
          <div class="stat-value">{{ overview.continuousDays || 0 }}</div>
        </a-card>
        <a-card :bordered="false" class="stat-card">
          <div class="stat-label">当前排名</div>
          <div class="stat-value">{{ overview.currentRank || '-' }}</div>
        </a-card>
      </div>

      <a-row :gutter="16">
        <a-col :xs="24" :lg="16">
          <a-card :bordered="false" class="panel-card" title="积分明细">
            <a-empty v-if="records.length === 0" description="暂无积分记录" />
            <div v-else class="record-list">
              <div
                v-for="item in records"
                :key="`${item.actionType}-${item.createTime}`"
                class="record-item"
              >
                <div>
                  <div class="record-title">{{ item.actionLabel }}</div>
                  <div class="record-time">
                    {{ formatTime(item.createTime) }} {{ item.remark || '' }}
                  </div>
                </div>
                <div class="record-points" :class="{ negative: item.points < 0 }">
                  {{ item.points > 0 ? '+' : '' }}{{ item.points }}
                </div>
              </div>
            </div>
            <div v-if="total > 0" class="pagination-wrap">
              <a-pagination
                v-model:current="currentPage"
                :total="total"
                :page-size="pageSize"
                :show-total="(value) => `共 ${value} 条`"
                @change="loadRecords"
              />
            </div>
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-card :bordered="false" class="panel-card" title="积分规则">
            <div class="rule-item"><span>每日签到</span><strong>+5</strong></div>
            <div class="rule-item"><span>连续签到 7 天</span><strong>+20</strong></div>
            <div class="rule-item"><span>发布帖子</span><strong>+3 / 每日最多 3 次</strong></div>
            <div class="rule-item"><span>发表评论</span><strong>+1 / 每日最多 5 次</strong></div>
            <div class="rule-item"><span>完成课程章节</span><strong>+10</strong></div>
            <div class="rule-item"><span>添加错题</span><strong>+2 / 每日最多 5 次</strong></div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { dailyCheckIn, getPointsOverview, getPointsRecords } from '@/api/index.js'

const overview = ref({})
const records = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const checkinLoading = ref(false)

async function loadOverview() {
  overview.value = (await getPointsOverview()) || {}
}

async function loadRecords(page = currentPage.value) {
  currentPage.value = page
  const data = await getPointsRecords({ current: currentPage.value, size: pageSize.value })
  records.value = data?.records || []
  total.value = data?.total || 0
}

async function handleCheckin() {
  if (checkinLoading.value || overview.value?.todayCheckedIn) return

  checkinLoading.value = true
  try {
    const data = await dailyCheckIn()
    if (data?.overview) {
      overview.value = data.overview
    }
    message.success(
      data?.bonusPoints
        ? `签到成功，获得 5 积分 + 连续签到奖励 ${data.bonusPoints} 积分`
        : '签到成功，获得 5 积分'
    )
    await Promise.all([loadOverview(), loadRecords(1)])
  } catch {
  } finally {
    checkinLoading.value = false
  }
}

function formatTime(time) {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--'
}

onMounted(async () => {
  try {
    await Promise.all([loadOverview(), loadRecords(1)])
  } catch {
  }
})
</script>

<style scoped>
.checkin-page { min-height: calc(100vh - 64px); background: #f5f7fa; }
.checkin-inner { max-width: 1120px; margin: 0 auto; padding: 24px; }
.hero-card {
  background: linear-gradient(135deg, #133c7a, #1677ff);
  border-radius: 18px;
  color: #fff;
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}
.hero-title { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.hero-desc { max-width: 640px; color: rgba(255, 255, 255, 0.82); }
.hero-actions { display: flex; align-items: flex-start; gap: 10px; }
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card, .panel-card { border-radius: 16px; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05); }
.stat-label { color: #8c8c8c; font-size: 13px; }
.stat-value { font-size: 30px; font-weight: 700; margin-top: 8px; }
.record-list { display: flex; flex-direction: column; gap: 12px; }
.record-item, .rule-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.record-title { font-weight: 600; margin-bottom: 4px; }
.record-time { color: #8c8c8c; font-size: 13px; }
.record-points { color: #1677ff; font-size: 18px; font-weight: 700; }
.record-points.negative { color: #cf1322; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: center; }
@media (max-width: 768px) {
  .checkin-inner { padding: 16px; }
  .hero-card, .hero-actions { flex-direction: column; }
  .stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>