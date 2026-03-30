<template>
  <div class="notebook-add-page">
    <div class="page-inner">
      <div class="page-header">
        <h1 class="page-title">添加错题</h1>
        <p class="page-desc">把当下不会的题记下来，后面复习时才不会丢失真正的薄弱点。</p>
      </div>

      <a-card :bordered="false" class="form-card">
        <a-form :model="formState" layout="vertical" @finish="handleSubmit">
          <a-row :gutter="16">
            <a-col :xs="24" :md="12">
              <a-form-item label="学科" name="subjectId" :rules="[{ required: true, message: '请选择学科' }]">
                <a-select v-model:value="formState.subjectId" placeholder="请选择学科">
                  <a-select-option v-for="item in subjects" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="年级" name="gradeId" :rules="[{ required: true, message: '请选择年级' }]">
                <a-select v-model:value="formState.gradeId" placeholder="请选择年级">
                  <a-select-option v-for="item in grades" :key="item.id" :value="item.id">{{ item.name }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="题目内容" name="questionContent" :rules="[{ required: true, message: '请输入题目内容' }]">
            <a-textarea v-model:value="formState.questionContent" :rows="5" placeholder="输入题干、已知条件、题目要求等" />
          </a-form-item>

          <a-form-item label="题目图片">
            <div class="upload-row">
              <a-upload :show-upload-list="false" :before-upload="handleImageUpload" accept="image/*">
                <a-button :loading="uploading">上传图片</a-button>
              </a-upload>
              <span class="upload-tip">可选，支持拍照题目后上传</span>
            </div>
            <img v-if="formState.questionImage" :src="formState.questionImage" class="preview-image" alt="题目图片" />
          </a-form-item>

          <a-row :gutter="16">
            <a-col :xs="24" :md="12">
              <a-form-item label="我的错答">
                <a-textarea v-model:value="formState.myAnswer" :rows="4" placeholder="可记录当时的错误思路或答案" />
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="正确答案与解析" name="correctAnswer" :rules="[{ required: true, message: '请输入正确答案' }]">
                <a-textarea v-model:value="formState.correctAnswer" :rows="4" placeholder="填写正确答案、关键步骤和解析" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="错误原因">
            <a-textarea v-model:value="formState.errorReason" :rows="3" placeholder="例如：概念混淆、审题不清、计算失误、知识点不熟" />
          </a-form-item>

          <div class="form-actions">
            <a-button @click="$router.push('/notebook')">返回列表</a-button>
            <a-button type="primary" html-type="submit" :loading="submitting">保存错题</a-button>
          </div>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { commonUpload, createNotebookItem, getGradeList, getSubjectList } from '@/api/index.js'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const subjects = ref([])
const grades = ref([])
const submitting = ref(false)
const uploading = ref(false)
const formState = reactive({ subjectId: null, gradeId: null, questionContent: '', questionImage: '', myAnswer: '', correctAnswer: '', errorReason: '' })

function normalizeOptions(data, key) { return (data || []).map((item) => ({ id: item.id ?? item[key], name: item.name })) }
async function loadOptions() { const [subjectRes, gradeRes] = await Promise.allSettled([getSubjectList(), getGradeList()]); if (subjectRes.status === 'fulfilled') subjects.value = normalizeOptions(subjectRes.value, 'subjectId'); if (gradeRes.status === 'fulfilled') grades.value = normalizeOptions(gradeRes.value, 'gradeId') }

async function handleImageUpload(file) {
  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', 'post')
  try {
    const data = await commonUpload(formData)
    formState.questionImage = data?.url || ''
    message.success('图片上传成功')
  } finally {
    uploading.value = false
  }
  return false
}

async function handleSubmit() {
  submitting.value = true
  try {
    await createNotebookItem(formState)
    message.success('错题保存成功')
    router.push('/notebook')
  } finally {
    submitting.value = false
  }
}

onMounted(loadOptions)
</script>

<style scoped>
.notebook-add-page { min-height: calc(100vh - 64px); background: #f5f7fa; }
.page-inner { max-width: 980px; margin: 0 auto; padding: 24px; }
.page-header { margin-bottom: 20px; }
.page-title { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.page-desc { color: #8c8c8c; }
.form-card { border-radius: 18px; box-shadow: 0 1px 8px rgba(0, 0, 0, 0.05); }
.upload-row { display: flex; align-items: center; gap: 12px; }
.upload-tip { color: #8c8c8c; font-size: 13px; }
.preview-image { margin-top: 12px; max-width: 260px; border-radius: 12px; }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; }
@media (max-width: 768px) { .page-inner { padding: 16px; } }
</style>
