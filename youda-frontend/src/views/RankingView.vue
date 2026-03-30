<template>
  <div class="ranking-page">
    <div class="ranking-inner">
      <div class="page-header">
        <h1 class="page-title">积分排行榜</h1>
        <p class="page-desc">看看自己的学习投入在全站处于什么位置，保持节奏，慢慢把排名打上去。</p>
      </div>

      <a-card :bordered="false" class="self-card">
        <div><div class="self-label">我的当前排名</div><div class="self-rank">第 {{ ranking.currentUserRank || '-' }} 名</div></div>
        <div class="self-points">{{ ranking.currentUserPoints || 0 }} 积分</div>
      </a-card>

      <div class="podium" v-if="topThree.length > 0">
        <div v-for="item in topThree" :key="item.userId" class="podium-card" :class="`rank-${item.rank}`">
          <div class="podium-rank">TOP {{ item.rank }}</div>
          <a-avatar :src="item.avatar" :size="64" style="background-color: #1677ff">{{ item.nickname?.charAt(0) || '优' }}</a-avatar>
          <div class="podium-name">{{ item.nickname }}</div>
          <div class="podium-points">{{ item.points }} 分</div>
        </div>
      </div>

      <a-card :bordered="false" class="list-card" title="前 20 名">
        <div class="rank-list">
          <div v-for="item in rankingList" :key="item.userId" class="rank-item" :class="{ current: item.currentUser }">
            <div class="rank-left">
              <div class="rank-no">{{ item.rank }}</div>
              <a-avatar :src="item.avatar" :size="44" style="background-color: #1677ff">{{ item.nickname?.charAt(0) || '优' }}</a-avatar>
              <div><div class="rank-name">{{ item.nickname }}</div><div class="rank-meta">{{ item.currentUser ? '你自己' : '学习者' }}</div></div>
            </div>
            <div class="rank-points">{{ item.points }} 分</div>
          </div>
        </div>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getPointsRanking } from '@/api/index.js'

const ranking = ref({})
const rankingList = computed(() => ranking.value?.rankingList || [])
const topThree = computed(() => rankingList.value.slice(0, 3))
async function loadRanking() { ranking.value = (await getPointsRanking()) || {} }
onMounted(loadRanking)
</script>

<style scoped>
.ranking-page { min-height: calc(100vh - 64px); background: #f5f7fa; }
.ranking-inner { max-width: 1000px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.page-desc { color: #8c8c8c; }
.self-card, .list-card { border-radius: 18px; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05); }
.self-card { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.self-label { color: #8c8c8c; }
.self-rank { font-size: 30px; font-weight: 700; margin-top: 8px; }
.self-points { font-size: 24px; font-weight: 700; color: #1677ff; }
.podium { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 18px; }
.podium-card { background: #fff; border-radius: 18px; padding: 20px; text-align: center; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05); }
.rank-1 { background: linear-gradient(180deg, #fff8dd, #ffffff); }
.rank-2 { background: linear-gradient(180deg, #f4f7fb, #ffffff); }
.rank-3 { background: linear-gradient(180deg, #fff1e6, #ffffff); }
.podium-rank { color: #8c8c8c; font-size: 12px; margin-bottom: 12px; }
.podium-name { font-weight: 700; margin: 12px 0 6px; }
.podium-points { color: #1677ff; font-size: 20px; font-weight: 700; }
.rank-list { display: flex; flex-direction: column; }
.rank-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 16px 0; border-bottom: 1px solid #f0f0f0; }
.rank-item.current { background: linear-gradient(90deg, rgba(22, 119, 255, 0.06), transparent); border-radius: 12px; padding-left: 12px; padding-right: 12px; }
.rank-left { display: flex; align-items: center; gap: 12px; }
.rank-no { width: 28px; font-size: 18px; font-weight: 700; color: #1677ff; text-align: center; }
.rank-name { font-weight: 600; }
.rank-meta { color: #8c8c8c; font-size: 13px; }
.rank-points { font-size: 20px; font-weight: 700; color: #1677ff; }
@media (max-width: 768px) { .ranking-inner { padding: 16px; } .self-card { flex-direction: column; gap: 12px; } .podium { grid-template-columns: 1fr; } }
</style>
