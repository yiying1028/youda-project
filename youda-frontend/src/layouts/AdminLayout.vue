<template>
  <a-layout class="admin-layout">
    <!-- 左侧侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="220"
      class="sider"
    >
      <!-- 管理后台Logo -->
      <div class="sider-logo">
        <div class="logo-icon">优</div>
        <span v-if="!collapsed" class="logo-text">优答管理后台</span>
      </div>

      <!-- 侧边栏菜单 -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-menu-item key="/admin">
          <template #icon><dashboard-outlined /></template>
          控制台
        </a-menu-item>
        <a-menu-item key="/admin/users">
          <template #icon><user-outlined /></template>
          用户管理
        </a-menu-item>
        <a-menu-item key="/admin/posts">
          <template #icon><message-outlined /></template>
          帖子管理
        </a-menu-item>
        <a-menu-item key="/admin/resources">
          <template #icon><folder-outlined /></template>
          资料管理
        </a-menu-item>
        <a-menu-item key="/admin/courses">
          <template #icon><play-circle-outlined /></template>
          课程管理
        </a-menu-item>
        <a-menu-item key="/admin/announcements">
          <template #icon><notification-outlined /></template>
          公告管理
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <!-- 右侧主区域 -->
    <a-layout>
      <!-- 顶部工具栏 -->
      <a-layout-header class="admin-header">
        <!-- 折叠/展开侧边栏按钮 -->
        <menu-fold-outlined
          v-if="!collapsed"
          class="trigger"
          @click="collapsed = true"
        />
        <menu-unfold-outlined
          v-else
          class="trigger"
          @click="collapsed = false"
        />

        <!-- 面包屑导航 -->
        <div class="header-breadcrumb">
          <span class="site-name">优答管理后台</span>
          <span class="separator">/</span>
          <span class="current-page">{{ currentPageTitle }}</span>
        </div>

        <!-- 右侧用户操作区 -->
        <div class="header-right">
          <a-button size="small" @click="$router.push('/')">返回前台</a-button>
          <a-divider type="vertical" />
          <a-dropdown placement="bottomRight">
            <div class="user-wrap">
              <a-avatar :size="32" style="background-color: #1677ff">
                {{ userStore.userInfo?.nickname?.charAt(0) || '管' }}
              </a-avatar>
              <span style="margin-left: 8px; font-size: 14px">
                {{ userStore.userInfo?.nickname || '管理员' }}
              </span>
            </div>
            <template #overlay>
              <a-menu @click="handleUserMenu">
                <a-menu-item key="logout" style="color: #ff4d4f">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区域 -->
      <a-layout-content class="admin-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  UserOutlined,
  MessageOutlined,
  FolderOutlined,
  PlayCircleOutlined,
  NotificationOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const collapsed = ref(false)

// 当前选中的菜单项（与路由同步）
const selectedKeys = ref([route.path])

// 当前页面标题（根据路由计算）
const currentPageTitle = computed(() => {
  const titleMap = {
    '/admin': '控制台',
    '/admin/users': '用户管理',
    '/admin/posts': '帖子管理',
    '/admin/resources': '资料管理',
    '/admin/courses': '课程管理',
    '/admin/announcements': '公告管理'
  }
  return titleMap[route.path] || '管理后台'
})

// 监听路由变化，同步选中菜单
watch(
  () => route.path,
  (newPath) => {
    selectedKeys.value = [newPath]
  }
)

/**
 * 点击菜单项时跳转路由
 */
function handleMenuClick({ key }) {
  router.push(key)
}

/**
 * 处理用户下拉菜单
 */
function handleUserMenu({ key }) {
  if (key === 'logout') {
    userStore.logout()
    message.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
/* 整体布局高度撑满视口 */
.admin-layout {
  min-height: 100vh;
}

/* 侧边栏样式 */
.sider {
  background: #001529;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 100;
}

/* Logo区域 */
.sider-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  background: #002140;
  border-bottom: 1px solid #003566;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
}

/* 顶部工具栏 */
.admin-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 99;
  margin-left: 220px;
  height: 64px;
  line-height: 64px;
  transition: margin-left 0.2s;
}

/* 折叠按钮 */
.trigger {
  font-size: 18px;
  cursor: pointer;
  color: #595959;
  transition: color 0.2s;
}

.trigger:hover {
  color: #1677ff;
}

/* 面包屑 */
.header-breadcrumb {
  flex: 1;
}

.site-name {
  color: #8c8c8c;
  font-size: 14px;
}

.separator {
  color: #d9d9d9;
  margin: 0 8px;
}

.current-page {
  color: #262626;
  font-size: 14px;
  font-weight: 600;
}

/* 右侧工具区 */
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-wrap {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-wrap:hover {
  background: #f0f5ff;
}

/* 内容区域 */
.admin-content {
  margin-left: 220px;
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
  transition: margin-left 0.2s;
}
</style>
