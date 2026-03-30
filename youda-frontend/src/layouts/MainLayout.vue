<template>
  <a-layout class="main-layout">
    <a-layout-header class="header">
      <div class="header-inner">
        <div class="header-left">
          <router-link to="/" class="logo">
            <div class="logo-text">
              <span class="logo-main">优答</span>
              <span class="logo-sub">中小学成长学习平台</span>
            </div>
          </router-link>

          <nav class="nav-links" aria-label="主导航">
            <router-link
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="nav-link"
              :class="{ active: isActive(item.path) }"
            >
              {{ item.label }}
            </router-link>
          </nav>
        </div>

        <div class="header-right">
          <div class="header-tagline">课程、社区、资料与 AI 答疑串成一条更清楚的学习路径</div>

          <template v-if="!userStore.isLoggedIn">
            <a-button class="header-action" @click="$router.push('/login')">登录</a-button>
            <a-button type="primary" class="header-action" @click="$router.push('/register')">
              免费注册
            </a-button>
          </template>

          <template v-else>
            <a-button
              v-if="userStore.isAdmin"
              size="small"
              class="header-admin"
              @click="$router.push('/admin')"
            >
              管理后台
            </a-button>

            <a-dropdown placement="bottomRight">
              <div class="user-avatar-wrap">
                <a-avatar :src="userStore.userInfo?.avatar" :size="38" class="user-avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || '优' }}
                </a-avatar>
                <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
              </div>
              <template #overlay>
                <a-menu @click="handleUserMenu">
                  <a-menu-item key="user">个人中心</a-menu-item>
                  <a-menu-item key="posts">我的帖子</a-menu-item>
                  <a-menu-item key="favorites">我的收藏</a-menu-item>
                  <a-menu-item key="notebook">我的错题本</a-menu-item>
                  <a-menu-item key="checkin">签到积分</a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout" style="color: #b94721">退出登录</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </div>
      </div>
    </a-layout-header>

    <a-layout-content class="content">
      <div class="content-shell">
        <router-view />
      </div>
    </a-layout-content>

    <a-layout-footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-brand-title">优答</div>
          <div class="footer-brand-sub">把学习的每一步排成更顺手的节奏</div>
          <p class="footer-copy">
            © {{ currentYear }} 优答 · 中小学成长学习平台。让提问、练习、查资料和复盘在同一个场域内完成。
          </p>
        </div>

        <div class="footer-links">
          <router-link v-for="item in footerLinks" :key="item.path" :to="item.path">
            {{ item.label }}
          </router-link>
        </div>
      </div>
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { message } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentYear = new Date().getFullYear()

const navItems = [
  { path: '/', label: '首页' },
  { path: '/community', label: '学习社区' },
  { path: '/resources', label: '资料库' },
  { path: '/ai-chat', label: 'AI 答疑' },
  { path: '/courses', label: '课程中心' },
  { path: '/notebook', label: '错题本' },
  { path: '/checkin', label: '签到积分' }
]

const footerLinks = [
  { path: '/', label: '首页' },
  { path: '/community', label: '社区讨论' },
  { path: '/resources', label: '资料下载' },
  { path: '/courses', label: '精选课程' },
  { path: '/ai-chat', label: 'AI 辅导' },
  { path: '/checkin', label: '成长打卡' }
]

function isActive(path) {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

function handleUserMenu({ key }) {
  if (key === 'user') {
    router.push('/user')
  } else if (key === 'posts') {
    router.push('/user/posts')
  } else if (key === 'favorites') {
    router.push('/user/favorites')
  } else if (key === 'notebook') {
    router.push('/notebook')
  } else if (key === 'checkin') {
    router.push('/checkin')
  } else if (key === 'logout') {
    userStore.logout()
    message.success('已退出登录')
    router.push('/')
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  position: relative;
}

.main-layout::before,
.main-layout::after {
  content: '';
  position: fixed;
  inset: auto;
  pointer-events: none;
  z-index: 0;
}

.main-layout::before {
  top: 96px;
  right: -80px;
  width: 320px;
  height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(199, 154, 67, 0.18), transparent 68%);
}

.main-layout::after {
  left: -100px;
  bottom: 120px;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(30, 84, 109, 0.12), transparent 70%);
}

.header {
  position: sticky;
  top: 0;
  z-index: 120;
  height: auto;
  line-height: normal;
  padding: 14px 0;
  background: rgba(255, 248, 239, 0.78);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(24, 32, 42, 0.08);
  box-shadow: 0 8px 30px rgba(24, 32, 42, 0.06);
}

.header-inner,
.footer-inner {
  width: min(1280px, calc(100% - 48px));
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.header-left {
  min-width: 0;
}

.logo {
  position: relative;
  flex-shrink: 0;
  padding-left: 14px;
}

.logo::before {
  content: '';
  position: absolute;
  top: 4px;
  bottom: 4px;
  left: 0;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--accent), var(--accent-gold));
}

.logo-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.logo-main,
.footer-brand-title {
  font-family: var(--font-display);
  font-size: 28px;
  line-height: 1;
  letter-spacing: 0.06em;
  color: var(--text-main);
}

.logo-sub,
.footer-brand-sub,
.header-tagline,
.footer-copy {
  color: var(--text-soft);
  font-size: 13px;
  line-height: 1.6;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-link {
  padding: 10px 16px;
  border-radius: 999px;
  border: 1px solid transparent;
  color: var(--text-soft);
  font-size: 14px;
  transition: all 0.22s ease;
}

.nav-link:hover,
.nav-link.active {
  color: var(--text-main);
  border-color: rgba(24, 32, 42, 0.1);
  background: rgba(255, 250, 243, 0.78);
  box-shadow: 0 10px 24px rgba(24, 32, 42, 0.08);
}

.header-tagline {
  max-width: 280px;
  text-align: right;
}

.header-action,
.header-admin {
  min-height: 40px;
}

.user-avatar-wrap {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px 6px 6px;
  border-radius: 999px;
  border: 1px solid rgba(24, 32, 42, 0.08);
  background: rgba(255, 250, 243, 0.8);
  cursor: pointer;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.user-avatar-wrap:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(24, 32, 42, 0.08);
}

.user-avatar {
  background: linear-gradient(135deg, var(--accent) 0%, var(--accent-strong) 100%);
}

.user-name {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-main);
  font-size: 14px;
}

.content {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 196px);
}

.content-shell {
  width: 100%;
}

.footer {
  position: relative;
  z-index: 1;
  padding: 42px 0 36px;
  margin-top: 56px;
  background:
    linear-gradient(135deg, rgba(31, 42, 54, 0.98) 0%, rgba(21, 36, 46, 0.96) 55%, rgba(30, 84, 109, 0.92) 100%);
  overflow: hidden;
}

.footer::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at top right, rgba(199, 154, 67, 0.18), transparent 28%),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: auto, 28px 28px, 28px 28px;
  opacity: 0.55;
  pointer-events: none;
}

.footer-inner {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 32px;
}

.footer-brand {
  max-width: 560px;
}

.footer-brand-sub,
.footer-copy,
.footer-links a {
  color: rgba(255, 250, 243, 0.84);
}

.footer-brand-sub,
.footer-copy {
  opacity: 0.76;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, minmax(88px, 1fr));
  gap: 12px 18px;
  min-width: min(360px, 100%);
}

.footer-links a {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(255, 250, 243, 0.08);
  border: 1px solid rgba(255, 250, 243, 0.08);
  transition: all 0.22s ease;
}

.footer-links a:hover {
  background: rgba(255, 250, 243, 0.14);
  transform: translateY(-1px);
}

@media (max-width: 1120px) {
  .header-inner,
  .footer-inner {
    width: min(1280px, calc(100% - 32px));
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .header-tagline {
    max-width: none;
    text-align: left;
  }

  .footer-links {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .header {
    padding: 12px 0;
  }

  .header-left,
  .header-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .nav-links {
    width: 100%;
  }

  .nav-link {
    padding: 8px 12px;
    font-size: 13px;
  }

  .header-tagline {
    display: none;
  }

  .footer-links {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
