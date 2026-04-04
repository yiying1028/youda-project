<template>
  <div class="resource-detail-page">
    <div class="resource-detail-inner">
      <a-button type="link" class="back-btn" @click="$router.push('/resources')">
        <left-outlined /> 返回资料库
      </a-button>

      <a-spin :spinning="loading || actionLoading">
        <div v-if="resource" class="detail-layout">
          <a-card :bordered="false" class="detail-card">
            <div class="detail-header">
              <div>
                <div class="detail-tags">
                  <a-tag color="blue">{{ resource.subjectName }}</a-tag>
                  <a-tag>{{ resource.gradeName }}</a-tag>
                  <a-tag color="green">{{ resource.fileType?.toUpperCase() }}</a-tag>
                </div>
                <h1 class="detail-title">{{ resource.name }}</h1>
                <p class="detail-desc">{{ resource.description || '该资料暂时没有补充说明。' }}</p>
              </div>
              <div class="price-panel" :class="{ paid: resource.requiresPoints }">
                <div class="price-label">获取方式</div>
                <div class="price-value">{{ resource.requiresPoints ? `${resource.pointsCost} 积分` : '免费' }}</div>
                <div class="price-sub">
                  {{ resource.canDownload ? '当前账号已解锁，可直接下载。' : '购买后可下载原始资料文件。' }}
                </div>
              </div>
            </div>

            <div class="detail-info">
              <div class="info-item">
                <span class="info-label">上传时间</span>
                <strong>{{ formatDate(resource.createTime) }}</strong>
              </div>
              <div class="info-item">
                <span class="info-label">文件大小</span>
                <strong>{{ formatSize(resource.fileSize) }}</strong>
              </div>
              <div class="info-item">
                <span class="info-label">下载次数</span>
                <strong>{{ resource.downloadCount || 0 }} 次</strong>
              </div>
              <div class="info-item">
                <span class="info-label">购买状态</span>
                <strong>{{ resource.requiresPoints ? (resource.purchased ? '已购买' : '未购买') : '免费开放' }}</strong>
              </div>
            </div>

            <div class="action-section">
              <a-button type="primary" size="large" class="action-btn" @click="handlePrimaryAction">
                <template #icon><download-outlined /></template>
                {{ actionText }}
              </a-button>
              <p class="action-tip">
                <template v-if="resource.canDownload">下载成功后会自动保存在浏览器默认下载目录。</template>
                <template v-else>将先扣除积分完成购买，再自动下载资料。</template>
              </p>
            </div>
          </a-card>
        </div>

        <a-result
          v-else-if="!loading"
          status="404"
          title="资料不存在"
          sub-title="该资料已被删除或暂未发布。"
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
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { DownloadOutlined, LeftOutlined } from '@ant-design/icons-vue'
import { downloadResource, getResourceDetail, purchaseResource } from '@/api/index.js'
import { useUserStore } from '@/stores/user.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const resource = ref(null)
const loading = ref(true)
const actionLoading = ref(false)

// 主按钮文案跟着已购买/未购买动态切换。
const actionText = computed(() => {
  if (!resource.value) return '处理中...'
  if (resource.value.canDownload) return '立即下载'
  return `使用 ${resource.value.pointsCost} 积分购买并下载`
})

// 路由参数兜底转换成合法的资料 ID。
function resolveResourceId(value) {
  const resourceId = Number(value)
  return Number.isInteger(resourceId) && resourceId > 0 ? resourceId : null
}

function formatSize(bytes) {
  if (!bytes) return '未知大小'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function formatDate(value) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--'
}

// 加载资料详情，顺便拿到当前账号的购买状态。
async function loadResource() {
  loading.value = true
  try {
    const resourceId = resolveResourceId(route.params.id)
    if (!resourceId) {
      resource.value = null
      return
    }
    resource.value = await getResourceDetail(resourceId)
  } catch {
    resource.value = null
  } finally {
    loading.value = false
  }
}

// 实际下载动作单独拆出来，便于购买成功后自动下载复用。
async function downloadCurrentResource() {
  if (!resource.value?.id) return false
  try {
    await downloadResource(resource.value.id)
    await loadResource()
    return true
  } catch (error) {
    message.error(error.message || '下载失败')
    return false
  }
}

// 主操作：已解锁时直接下载，未解锁时先购买再自动下载。
async function handlePrimaryAction() {
  if (!resource.value?.id) return

  actionLoading.value = true
  try {
    if (resource.value.canDownload) {
      await downloadCurrentResource()
      return
    }

    const result = await purchaseResource(resource.value.id)
    if (userStore.isLoggedIn) {
      await userStore.fetchUserInfo().catch(() => {})
    }
    message.success(`购买成功，已扣除 ${result?.pointsCost || resource.value.pointsCost} 积分`)
    await loadResource()
    await downloadCurrentResource()
  } catch {
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  loadResource()
})
</script>

<style scoped>
.resource-detail-page { min-height: calc(100vh - 64px); background: #f5f7fa; padding-bottom: 40px; }
.resource-detail-inner { max-width: 960px; margin: 0 auto; padding: 24px; }
.back-btn { padding-left: 0; color: #6b7280; margin-bottom: 20px; }
.detail-card { border-radius: 22px; box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08); }
.detail-header { display: grid; grid-template-columns: minmax(0, 1fr) 240px; gap: 24px; padding-bottom: 24px; border-bottom: 1px solid #f0f0f0; }
.detail-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }
.detail-title { margin: 0 0 12px; font-size: 30px; line-height: 1.2; color: #1f2937; }
.detail-desc { margin: 0; color: #6b7280; line-height: 1.8; }
.price-panel { border-radius: 18px; padding: 20px; background: linear-gradient(180deg, #edf7ed, #ffffff); }
.price-panel.paid { background: linear-gradient(180deg, #fff3e8, #ffffff); }
.price-label { color: #8c8c8c; font-size: 13px; }
.price-value { margin-top: 8px; font-size: 30px; font-weight: 700; color: #1f2937; }
.price-sub { margin-top: 10px; color: #6b7280; line-height: 1.6; font-size: 13px; }
.detail-info { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-top: 24px; }
.info-item { padding: 16px; border-radius: 16px; background: #f8fafc; }
.info-label { display: block; color: #8c8c8c; font-size: 13px; margin-bottom: 6px; }
.action-section { margin-top: 28px; padding: 24px; text-align: center; border-radius: 18px; background: linear-gradient(135deg, #eef4ff, #f8fbff); }
.action-btn { min-width: 260px; height: 48px; border-radius: 14px; font-weight: 700; }
.action-tip { margin: 12px 0 0; color: #8c8c8c; font-size: 13px; }
@media (max-width: 768px) {
  .resource-detail-inner { padding: 16px; }
  .detail-header { grid-template-columns: 1fr; }
  .detail-info { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .action-btn { width: 100%; min-width: 0; }
}
</style>
