<template>
  <div class="post-create-page">
    <div class="post-create-inner">
      <div class="page-header">
        <a-button type="link" class="back-btn" @click="$router.push('/community')">
          <left-outlined /> 返回社区
        </a-button>
        <h1 class="page-title">发帖提问</h1>
        <p class="page-desc">把题目背景、卡住的位置和你已经尝试过的方法写清楚，更容易得到有效回复。</p>
      </div>

      <div class="form-container">
        <a-card :bordered="false" class="form-card">
          <a-form
            ref="formRef"
            :model="postForm"
            :rules="formRules"
            layout="vertical"
            @finish="handleSubmit"
          >
            <a-form-item label="标题" name="title">
              <a-input
                v-model:value="postForm.title"
                size="large"
                placeholder="请输入标题，简洁说明你的问题（5-100字）"
                :maxlength="100"
                show-count
              />
            </a-form-item>

            <a-form-item label="分类" name="categoryId">
              <a-select
                v-model:value="postForm.categoryId"
                size="large"
                placeholder="请选择帖子分类"
                :loading="categoryLoading"
              >
                <a-select-option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="内容" name="content">
              <a-textarea
                v-model:value="postForm.content"
                placeholder="请详细描述你的问题，例如：&#10;1. 题目背景或上下文&#10;2. 你已经尝试过的思路&#10;3. 具体卡住的位置"
                :rows="12"
                :maxlength="5000"
                show-count
                class="content-textarea"
              />
            </a-form-item>

            <a-form-item>
              <div class="form-actions">
                <a-button @click="$router.back()">取消</a-button>
                <a-button type="primary" html-type="submit" size="large" :loading="submitting">
                  发布帖子
                </a-button>
              </div>
            </a-form-item>
          </a-form>
        </a-card>

        <a-card :bordered="false" class="tips-card" title="发帖建议">
          <ul class="tips-list">
            <li>标题越具体，越容易吸引到懂这个问题的人。</li>
            <li>把已尝试的方法写出来，能减少重复沟通。</li>
            <li>选择准确分类，能让帖子更快被看到。</li>
            <li>保持表达清晰、友好，社区回复质量会更高。</li>
            <li>不要发布广告、引流或无关内容。</li>
          </ul>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryList, createPost } from '@/api/index.js'
import { message } from 'ant-design-vue'
import { LeftOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const categoryLoading = ref(false)
const categories = ref([])

const postForm = reactive({
  title: '',
  categoryId: null,
  content: ''
})

const formRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应为 5 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择帖子分类', trigger: 'change' }],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '内容长度应为 10 到 5000 个字符', trigger: 'blur' }
  ]
}

async function loadCategories() {
  categoryLoading.value = true
  try {
    const data = await getCategoryList()
    categories.value = data || []
  } finally {
    categoryLoading.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    const result = await createPost(postForm)
    message.success('帖子发布成功')
    const postId = result?.id || result?.postId || result
    if (postId) {
      router.push('/post/' + postId)
    } else {
      router.push('/community')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.post-create-page {
  min-height: calc(100vh - 64px);
  background: transparent;
  padding-bottom: 40px;
}

.post-create-inner {
  width: min(960px, calc(100% - 48px));
  margin: 0 auto;
  padding: 24px 0;
}

.page-header {
  margin-bottom: 24px;
}

.back-btn {
  padding-left: 0;
  color: var(--text-soft);
  margin-bottom: 12px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.page-title {
  margin: 0 0 8px;
  font-family: var(--font-display);
  font-size: 34px;
  color: var(--text-main);
}

.page-desc {
  margin: 0;
  color: var(--text-soft);
  font-size: 14px;
  line-height: 1.8;
}

.form-container {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
  align-items: start;
}

.form-card,
.tips-card {
  border-radius: 24px;
}

.content-textarea {
  font-size: 14px;
  line-height: 1.8;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.tips-card {
  position: sticky;
  top: 88px;
}

.tips-list {
  padding-left: 18px;
  margin: 0;
}

.tips-list li {
  font-size: 13px;
  color: var(--text-soft);
  line-height: 1.9;
  margin-bottom: 6px;
}

@media (max-width: 900px) {
  .post-create-inner {
    width: min(960px, calc(100% - 24px));
  }

  .form-container {
    grid-template-columns: 1fr;
  }

  .tips-card {
    position: static;
  }
}
</style>
