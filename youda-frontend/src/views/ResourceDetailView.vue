<template>
  <div class="resource-detail-page">
    <div class="resource-detail-inner">
      <a-button type="link" class="back-btn" @click="$router.push('/resources')">
        <left-outlined /> 返回资料库
      </a-button>

      <a-spin :spinning="loading">
        <div v-if="resource" class="detail-layout">
          <!-- 资料主信息 -->
          <a-card :bordered="false" class="detail-card">
            <div class="detail-header">
              <div class="detail-title-wrap">
                <h1 class="detail-title">{{ resource.name }}</h1>
                <div class="detail-tags">
                  <a-tag color="blue">{{ resource.subjectName }}</a-tag>
                  <a-tag>{{ resource.gradeName }}</a-tag>
                  <a-tag color="green">{{ resource.fileType?.toUpperCase() }}</a-tag>
                </div>
              </div>
            </div>

            <!-- 资料详情信息 -->
            <div class="detail-info">
              <div class="info-row">
                <span class="info-label">上传者：</span>
                <span>{{ resource.uploaderName || '匿名用户' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">上传时间：</span>
                <span>{{ formatDate(resource.createTime) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">文件大小：</span>
                <span>{{ formatSize(resource.fileSize) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">下载次数：</span>
                <span>{{ resource.downloadCount || 0 }} 次</span>
              </div>
            </div>

            <!-- 资料描述 -->
            <div class="detail-desc">
              <h3>资料简介</h3>
              <p>{{ resource.description || '该资料暂无描述信息' }}</p>
            </div>

            <!-- 下载按钮 -->
            <div class="download-section">
              <a-button
                type="primary"
                size="large"
                class="download-btn"
                @click="handleDownload"
              >
                <template #icon><download-outlined /></template>
                免费下载
              </a-button>
              <p class="download-tip">点击即可直接下载，完全免费</p>
            </div>
          </a-card>
        </div>

        <a-result
          v-else-if="!loading"
          status="404"
          title="资料不存在"
          sub-title="该资料已被删除或不存在"
        >
          <template #extra>
            <a-button type="primary" @click="$router.push('/resources')">返回资料库</a-button>
          </template>
        </a-result>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getResourceDetail, downloadResource } from '@/api/index.js'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { LeftOutlined, DownloadOutlined } from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const resource = ref(null)
const loading = ref(true)

function resolveResourceId(value) {
  const resourceId = Number(value)
  if (!Number.isInteger(resourceId) || resourceId <= 0) {
    return null
  }
  return resourceId
}

/**
 * 格式化文件大小
 */
function formatSize(bytes) {
  if (!bytes) return '未知大小'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

/**
 * 格式化日期
 */
function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

/**
 * 加载资料详情
 */
async function loadResource() {
  loading.value = true
  try {
    const resourceId = resolveResourceId(route.params.id)
    if (!resourceId) {
      resource.value = null
      return
    }

    const data = await getResourceDetail(resourceId)
    resource.value = data
  } catch (error) {
    resource.value = null
    if (route.params.id) {
      message.error(error.message || '资料详情加载失败')
    }
  } finally {
    loading.value = false
  }
}

/**
 * 触发文件下载（在新标签页中打开下载链接）
 */
function handleDownload() {
  if (!resource.value?.id) {
    message.error('资料不存在，无法下载')
    return
  }
  downloadResource(resource.value.id)
}

onMounted(() => {
  loadResource()
})
</script>

<style scoped>
.resource-detail-page {
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
  padding-bottom: 40px;
}

.resource-detail-inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.back-btn {
  padding-left: 0;
  color: #595959;
  margin-bottom: 20px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.detail-card {
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
}

/* 资料头部 */
.detail-header {
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-title-wrap {
  min-width: 0;
}

.detail-title {
  font-size: 22px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 10px;
  line-height: 1.4;
}

.detail-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 详情信息 */
.detail-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 20px;
  background: #f8fafb;
  border-radius: 8px;
  margin-bottom: 24px;
}

.info-row {
  font-size: 14px;
  color: #595959;
}

.info-label {
  color: #8c8c8c;
  margin-right: 4px;
}

/* 描述区域 */
.detail-desc {
  margin-bottom: 28px;
}

.detail-desc h3 {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
}

.detail-desc p {
  font-size: 14px;
  color: #595959;
  line-height: 1.7;
}

/* 下载按钮区域 */
.download-section {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f0f5ff, #e8f4fd);
  border-radius: 10px;
}

.download-btn {
  height: 48px;
  padding: 0 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
}

.download-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #8c8c8c;
}
</style>
