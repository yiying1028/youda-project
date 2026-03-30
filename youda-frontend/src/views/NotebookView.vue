<template>
  <div class="notebook-page">
    <div class="notebook-inner">
      <div class="page-header">
        <div>
          <h1 class="page-title">错题本</h1>
          <p class="page-desc">收录错题、归纳原因、持续复习，把反复出错的知识点真正消化掉。</p>
        </div>
        <div class="header-actions">
          <a-button @click="$router.push('/notebook/review')">开始复习</a-button>
          <a-button type="primary" @click="$router.push('/notebook/add')">添加错题</a-button>
        </div>
      </div>

      <div class="stats-grid">
        <a-card :bordered="false" class="stats-card"><div class="stats-label">总题数</div><div class="stats-value">{{ stats.totalCount || 0 }}</div></a-card>
        <a-card :bordered="false" class="stats-card"><div class="stats-label">待复习</div><div class="stats-value">{{ stats.reviewCount || 0 }}</div></a-card>
        <a-card :bordered="false" class="stats-card"><div class="stats-label">已掌握</div><div class="stats-value">{{ stats.masteredCount || 0 }}</div></a-card>
      </div>

      <a-card :bordered="false" class="filter-card">
        <div class="filter-row">
          <a-select v-model:value="filters.subjectId" placeholder="全部学科" allow-clear style="width: 150px" @change="handleFilterChange">
            <a-select-option v-for="item in subjects" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
          </a-select>
          <a-select v-model:value="filters.gradeId" placeholder="全部年级" allow-clear style="width: 150px" @change="handleFilterChange">
            <a-select-option v-for="item in grades" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
          </a-select>
          <a-input-search v-model:value="filters.keyword" placeholder="搜索题目内容或正确答案" allow-clear style="max-width: 320px" @search="handleFilterChange" />
        </div>
      </a-card>

      <a-spin :spinning="loading">
        <a-empty v-if="list.length === 0 && !loading" description="还没有错题记录">
          <a-button type="primary" @click="$router.push('/notebook/add')">先添加一题</a-button>
        </a-empty>
        <div v-else class="question-list">
          <a-card v-for="item in list" :key="item.id" :bordered="false" class="question-card">
            <div class="question-head">
              <div class="head-main">
                <div class="head-tags">
                  <a-tag color="blue">{{ item.subjectName || '未分类' }}</a-tag>
                  <a-tag>{{ item.gradeName || '年级未设置' }}</a-tag>
                  <a-tag :color="statusColor(item.masteryStatus)">{{ item.masteryStatusText }}</a-tag>
                </div>
                <div class="question-time">{{ formatTime(item.createTime) }}</div>
              </div>
              <div class="head-actions">
                <a-button size="small" @click="openEdit(item)">编辑</a-button>
                <a-popconfirm title="确认删除这条错题吗？" @confirm="handleDelete(item.id)">
                  <a-button size="small" danger>删除</a-button>
                </a-popconfirm>
              </div>
            </div>
            <div class="question-body">
              <div class="section-title">题目内容</div>
              <div class="question-content">{{ item.questionContent }}</div>
              <img v-if="item.questionImage" :src="item.questionImage" class="question-image" alt="题目图片" />
            </div>
            <div class="answer-grid">
              <div class="answer-item"><div class="section-title">我的错答</div><div>{{ item.myAnswer || '未填写' }}</div></div>
              <div class="answer-item"><div class="section-title">正确答案</div><div>{{ item.correctAnswer }}</div></div>
            </div>
            <div v-if="item.errorReason" class="reason-box"><div class="section-title">错误原因</div><div>{{ item.errorReason }}</div></div>
          </a-card>
        </div>
      </a-spin>

      <div class="pagination-wrap" v-if="total > 0">
        <a-pagination v-model:current="currentPage" :total="total" :page-size="pageSize" show-quick-jumper :show-total="(value) => `共 ${value} 条`" @change="handlePageChange" />
      </div>

      <a-modal v-model:open="editVisible" title="编辑错题" width="720px" @ok="handleEditSubmit">
        <a-form layout="vertical">
          <a-row :gutter="16">
            <a-col :span="12"><a-form-item label="学科"><a-select v-model:value="editForm.subjectId"><a-select-option v-for="item in subjects" :key="item.id" :value="item.id">{{ item.name }}</a-select-option></a-select></a-form-item></a-col>
            <a-col :span="12"><a-form-item label="年级"><a-select v-model:value="editForm.gradeId"><a-select-option v-for="item in grades" :key="item.id" :value="item.id">{{ item.name }}</a-select-option></a-select></a-form-item></a-col>
          </a-row>
          <a-form-item label="题目内容"><a-textarea v-model:value="editForm.questionContent" :rows="4" /></a-form-item>
          <a-form-item label="题目图片地址"><a-input v-model:value="editForm.questionImage" placeholder="保留为空则不展示图片" /></a-form-item>
          <a-form-item label="我的错答"><a-textarea v-model:value="editForm.myAnswer" :rows="2" /></a-form-item>
          <a-form-item label="正确答案"><a-textarea v-model:value="editForm.correctAnswer" :rows="3" /></a-form-item>
          <a-form-item label="错误原因"><a-textarea v-model:value="editForm.errorReason" :rows="2" /></a-form-item>
        </a-form>
      </a-modal>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { deleteNotebookItem, getGradeList, getNotebookList, getNotebookStats, getSubjectList, updateNotebookItem } from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)
const stats = ref({})
const subjects = ref([])
const grades = ref([])
const editVisible = ref(false)
const editingId = ref(null)
const filters = reactive({ subjectId: null, gradeId: null, keyword: '' })
const editForm = reactive({ subjectId: null, gradeId: null, questionContent: '', questionImage: '', myAnswer: '', correctAnswer: '', errorReason: '' })

function normalizeOptions(data, key) { return (data || []).map((item) => ({ id: item.id ?? item[key], name: item.name })) }
async function loadOptions() { const [subjectRes, gradeRes] = await Promise.allSettled([getSubjectList(), getGradeList()]); if (subjectRes.status === 'fulfilled') subjects.value = normalizeOptions(subjectRes.value, 'subjectId'); if (gradeRes.status === 'fulfilled') grades.value = normalizeOptions(gradeRes.value, 'gradeId') }
async function loadStats() { stats.value = (await getNotebookStats()) || {} }
async function loadList() { loading.value = true; try { const data = await getNotebookList({ current: currentPage.value, size: pageSize.value, ...filters }); list.value = data?.records || []; total.value = data?.total || 0 } finally { loading.value = false } }
function handleFilterChange() { currentPage.value = 1; loadList() }
function handlePageChange(page) { currentPage.value = page; loadList() }
function openEdit(item) { editingId.value = item.id; Object.assign(editForm, { subjectId: item.subjectId, gradeId: item.gradeId, questionContent: item.questionContent, questionImage: item.questionImage, myAnswer: item.myAnswer, correctAnswer: item.correctAnswer, errorReason: item.errorReason }); editVisible.value = true }
async function handleEditSubmit() { if (!editingId.value) return; await updateNotebookItem(editingId.value, editForm); message.success('错题已更新'); editVisible.value = false; await Promise.all([loadList(), loadStats()]) }
async function handleDelete(id) { await deleteNotebookItem(id); message.success('错题已删除'); await Promise.all([loadList(), loadStats()]) }
function formatTime(time) { return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '--' }
function statusColor(status) { if (status === 1) return 'green'; if (status === 2) return 'orange'; return 'blue' }
onMounted(async () => { await Promise.all([loadOptions(), loadStats(), loadList()]) })
</script>

<style scoped>
.notebook-page { min-height: calc(100vh - 64px); background: #f5f7fa; }
.notebook-inner { max-width: 1120px; margin: 0 auto; padding: 24px; }
.page-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.page-desc { color: #8c8c8c; }
.header-actions { display: flex; gap: 8px; }
.stats-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; margin-bottom: 16px; }
.stats-card, .filter-card, .question-card { border-radius: 16px; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05); }
.stats-label { color: #8c8c8c; font-size: 13px; }
.stats-value { font-size: 28px; font-weight: 700; margin-top: 8px; }
.filter-row { display: flex; gap: 12px; flex-wrap: wrap; }
.question-list { display: flex; flex-direction: column; gap: 16px; }
.question-head { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 12px; }
.head-main { display: flex; flex-direction: column; gap: 8px; }
.head-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.question-time { color: #8c8c8c; font-size: 13px; }
.head-actions { display: flex; gap: 8px; }
.section-title { font-size: 13px; color: #8c8c8c; margin-bottom: 6px; }
.question-content, .answer-item, .reason-box { white-space: pre-wrap; line-height: 1.75; }
.question-image { margin-top: 10px; max-width: 240px; border-radius: 12px; }
.answer-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; margin-top: 16px; }
.answer-item, .reason-box { background: #fafafa; border-radius: 12px; padding: 14px; }
.reason-box { margin-top: 14px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }
@media (max-width: 768px) { .notebook-inner { padding: 16px; } .page-header, .question-head { flex-direction: column; } .stats-grid, .answer-grid { grid-template-columns: 1fr; } }
</style>
