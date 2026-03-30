<template>
  <div class="review-page">
    <div class="review-inner">
      <div class="page-header">
        <div>
          <h1 class="page-title">错题复习</h1>
          <p class="page-desc">随机抽题复盘，掌握后不再反复出现，仍不会的题继续保留。</p>
        </div>
        <a-select v-model:value="subjectId" allow-clear placeholder="按学科筛选" style="width: 180px" @change="loadQuestion">
          <a-select-option v-for="item in subjects" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
        </a-select>
      </div>

      <a-spin :spinning="loading">
        <a-empty v-if="!question && !loading" description="当前筛选条件下没有待复习的错题">
          <a-button type="primary" @click="$router.push('/notebook/add')">去添加错题</a-button>
        </a-empty>
        <a-card v-else-if="question" :bordered="false" class="review-card">
          <div class="review-tags">
            <a-tag color="blue">{{ question.subjectName }}</a-tag>
            <a-tag>{{ question.gradeName }}</a-tag>
            <a-tag :color="statusColor(question.masteryStatus)">{{ question.masteryStatusText }}</a-tag>
          </div>
          <div class="content-section">
            <div class="section-title">题目</div>
            <div class="section-content">{{ question.questionContent }}</div>
            <img v-if="question.questionImage" :src="question.questionImage" class="question-image" alt="题目图片" />
          </div>
          <div class="grid-two">
            <div class="content-box"><div class="section-title">我的错答</div><div class="section-content">{{ question.myAnswer || '未记录' }}</div></div>
            <div class="content-box"><div class="section-title">正确答案</div><div class="section-content">{{ question.correctAnswer }}</div></div>
          </div>
          <div v-if="question.errorReason" class="content-box"><div class="section-title">错误原因</div><div class="section-content">{{ question.errorReason }}</div></div>
          <div class="review-actions">
            <a-button @click="loadQuestion">换一题</a-button>
            <a-button @click="markStatus(2)">仍不会</a-button>
            <a-button type="primary" @click="markStatus(1)">已掌握</a-button>
          </div>
        </a-card>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getNotebookReviewRandom, getSubjectList, updateNotebookMasteryStatus } from '@/api/index.js'
import { message } from 'ant-design-vue'

const loading = ref(false)
const question = ref(null)
const subjectId = ref(null)
const subjects = ref([])

function normalizeOptions(data) { return (data || []).map((item) => ({ id: item.id ?? item.subjectId, name: item.name })) }
async function loadSubjects() { subjects.value = normalizeOptions(await getSubjectList()) }
async function loadQuestion() { loading.value = true; try { question.value = await getNotebookReviewRandom(subjectId.value ? { subjectId: subjectId.value } : {}) } finally { loading.value = false } }
async function markStatus(status) { if (!question.value) return; await updateNotebookMasteryStatus(question.value.id, status); message.success(status === 1 ? '已标记为已掌握' : '已保留到下次复习'); await loadQuestion() }
function statusColor(status) { if (status === 1) return 'green'; if (status === 2) return 'orange'; return 'blue' }
onMounted(async () => { await loadSubjects(); await loadQuestion() })
</script>

<style scoped>
.review-page { min-height: calc(100vh - 64px); background: #f5f7fa; }
.review-inner { max-width: 960px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.page-desc { color: #8c8c8c; }
.review-card { border-radius: 18px; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05); }
.review-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.content-section, .content-box { background: #fafafa; border-radius: 14px; padding: 16px; margin-bottom: 16px; }
.section-title { color: #8c8c8c; font-size: 13px; margin-bottom: 8px; }
.section-content { line-height: 1.8; white-space: pre-wrap; }
.grid-two { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.question-image { margin-top: 12px; max-width: 280px; border-radius: 12px; }
.review-actions { display: flex; justify-content: flex-end; gap: 12px; }
@media (max-width: 768px) { .review-inner { padding: 16px; } .page-header, .review-actions { flex-direction: column; } .grid-two { grid-template-columns: 1fr; } }
</style>
