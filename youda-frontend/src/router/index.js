import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页 - 优答' }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/CommunityView.vue'),
        meta: { title: '学习社区 - 优答' }
      },
      {
        path: 'post/create',
        name: 'PostCreate',
        component: () => import('@/views/PostCreateView.vue'),
        meta: { title: '发布帖子 - 优答', requiresAuth: true }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/PostDetailView.vue'),
        meta: { title: '帖子详情 - 优答' }
      },
      {
        path: 'resources',
        name: 'Resources',
        component: () => import('@/views/ResourcesView.vue'),
        meta: { title: '资料库 - 优答' }
      },
      {
        path: 'resource/:id',
        name: 'ResourceDetail',
        component: () => import('@/views/ResourceDetailView.vue'),
        meta: { title: '资料详情 - 优答' }
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/AiChatView.vue'),
        meta: { title: 'AI 答疑 - 优答', requiresAuth: true }
      },
      {
        path: 'courses',
        name: 'Courses',
        component: () => import('@/views/CoursesView.vue'),
        meta: { title: '课程中心 - 优答' }
      },
      {
        path: 'course/:id',
        name: 'CourseDetail',
        component: () => import('@/views/CourseDetailView.vue'),
        meta: { title: '课程详情 - 优答' }
      },
      {
        path: 'course/:id/video/:vid',
        name: 'VideoPlay',
        component: () => import('@/views/VideoPlayView.vue'),
        meta: { title: '视频学习 - 优答', requiresAuth: true }
      },
      {
        path: 'notebook',
        name: 'Notebook',
        component: () => import('@/views/NotebookView.vue'),
        meta: { title: '错题本 - 优答', requiresAuth: true }
      },
      {
        path: 'notebook/add',
        name: 'NotebookAdd',
        component: () => import('@/views/NotebookAddView.vue'),
        meta: { title: '添加错题 - 优答', requiresAuth: true }
      },
      {
        path: 'notebook/review',
        name: 'NotebookReview',
        component: () => import('@/views/NotebookReviewView.vue'),
        meta: { title: '错题复习 - 优答', requiresAuth: true }
      },
      {
        path: 'checkin',
        name: 'Checkin',
        component: () => import('@/views/CheckinView.vue'),
        meta: { title: '每日打卡 - 优答', requiresAuth: true }
      },
      {
        path: 'ranking',
        name: 'Ranking',
        component: () => import('@/views/RankingView.vue'),
        meta: { title: '积分排行 - 优答', requiresAuth: true }
      },
      {
        path: 'user',
        name: 'UserCenter',
        component: () => import('@/views/user/UserCenterView.vue'),
        meta: { title: '个人中心 - 优答', requiresAuth: true }
      },
      {
        path: 'user/posts',
        name: 'MyPosts',
        component: () => import('@/views/user/MyPostsView.vue'),
        meta: { title: '我的帖子 - 优答', requiresAuth: true }
      },
      {
        path: 'user/favorites',
        name: 'MyFavorites',
        component: () => import('@/views/user/MyFavoritesView.vue'),
        meta: { title: '我的收藏 - 优答', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录 - 优答' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { title: '注册 - 优答' }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '控制台 - 优答管理后台' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsersView.vue'),
        meta: { title: '用户管理 - 优答管理后台' }
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: () => import('@/views/admin/AdminPostsView.vue'),
        meta: { title: '帖子管理 - 优答管理后台' }
      },
      {
        path: 'resources',
        name: 'AdminResources',
        component: () => import('@/views/admin/AdminResourcesView.vue'),
        meta: { title: '资料管理 - 优答管理后台' }
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/AdminCoursesView.vue'),
        meta: { title: '课程管理 - 优答管理后台' }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/AdminAnnouncementsView.vue'),
        meta: { title: '公告管理 - 优答管理后台' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  const token = localStorage.getItem('youda_token')
  const userInfoStr = localStorage.getItem('youda_userInfo')
  let userInfo = null
  try {
    userInfo = userInfoStr ? JSON.parse(userInfoStr) : null
  } catch {
    userInfo = null
  }

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && userInfo?.role !== 1) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
