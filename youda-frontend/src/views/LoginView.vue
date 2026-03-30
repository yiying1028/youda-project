<template>
  <div class="login-page">
    <div class="login-left">
      <!-- 左侧装饰区域 -->
      <div class="login-brand">
        <div class="brand-logo">优</div>
        <h1 class="brand-name">优答·中小学答疑网</h1>
        <p class="brand-slogan">专业 · 高效 · 有趣的学习社区</p>
        <div class="brand-features">
          <div class="bf-item">
            <check-circle-outlined />
            AI智能答疑，秒速解题
          </div>
          <div class="bf-item">
            <check-circle-outlined />
            海量优质资料免费下载
          </div>
          <div class="bf-item">
            <check-circle-outlined />
            名师视频课程系统学习
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <!-- 头部标题 -->
        <div class="login-header">
          <router-link to="/" class="back-home">
            <left-outlined /> 返回首页
          </router-link>
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-sub">登录您的账号继续学习之旅</p>
        </div>

        <!-- 登录表单 -->
        <a-form
          :model="loginForm"
          :rules="formRules"
          ref="formRef"
          layout="vertical"
          @finish="handleLogin"
        >
          <a-form-item label="账号" name="username">
            <a-input
              v-model:value="loginForm.username"
              size="large"
              placeholder="请输入用户名或手机号"
              allow-clear
            >
              <template #prefix>
                <user-outlined style="color: #bfbfbf" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="密码" name="password">
            <a-input-password
              v-model:value="loginForm.password"
              size="large"
              placeholder="请输入密码"
            >
              <template #prefix>
                <lock-outlined style="color: #bfbfbf" />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 记住我 + 忘记密码 -->
          <div class="form-extra">
            <a-checkbox v-model:checked="rememberMe">记住我</a-checkbox>
            <a style="color: #1677ff">忘记密码？</a>
          </div>

          <a-form-item style="margin-top: 8px">
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
              class="login-btn"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 注册入口 -->
        <div class="register-link">
          还没有账号？
          <router-link to="/register" style="color: #1677ff; font-weight: 600">
            立即免费注册
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { userLogin } from '@/api/index.js'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined,
  CheckCircleOutlined,
  LeftOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 记住我选项
const rememberMe = ref(false)

// 按钮加载状态
const loading = ref(false)

// 表单校验规则
const formRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/**
 * 执行登录操作
 */
async function handleLogin() {
  loading.value = true
  try {
    const result = await userLogin(loginForm)
    // 登录成功，保存token和用户信息到store
    userStore.login(result.token, result.userInfo)
    message.success('登录成功，欢迎回来！')

    // 跳转到登录前页面或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch {
    // 错误由request拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}

/* 左侧装饰区 */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 60%, #003eb3 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-brand {
  text-align: center;
  color: #fff;
}

.brand-logo {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 40px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  backdrop-filter: blur(10px);
}

.brand-name {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 8px;
}

.brand-slogan {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 40px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}

.bf-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
}

/* 右侧登录表单区 */
.login-right {
  width: 480px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 360px;
}

.login-header {
  margin-bottom: 32px;
}

.back-home {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #8c8c8c;
  font-size: 13px;
  text-decoration: none;
  margin-bottom: 24px;
  transition: color 0.2s;
}

.back-home:hover {
  color: #1677ff;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 6px;
}

.login-sub {
  color: #8c8c8c;
  font-size: 14px;
}

.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
}

.login-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #595959;
}

/* 响应式：小屏隐藏左侧装饰区 */
@media (max-width: 768px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}
</style>
