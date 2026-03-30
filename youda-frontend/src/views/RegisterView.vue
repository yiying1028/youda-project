<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 顶部Logo -->
      <router-link to="/" class="reg-logo">
        <div class="logo-icon">优</div>
        <span class="logo-text">优答·中小学答疑网</span>
      </router-link>

      <div class="register-box">
        <h2 class="reg-title">创建您的账号</h2>
        <p class="reg-sub">加入优答，开启高效学习之旅</p>

        <!-- 注册表单 -->
        <a-form
          :model="registerForm"
          :rules="formRules"
          ref="formRef"
          layout="vertical"
          @finish="handleRegister"
        >
          <!-- 用户名 -->
          <a-form-item label="用户名" name="username">
            <a-input
              v-model:value="registerForm.username"
              size="large"
              placeholder="请输入用户名（4-20个字符）"
              allow-clear
            >
              <template #prefix><user-outlined style="color: #bfbfbf" /></template>
            </a-input>
          </a-form-item>

          <!-- 昵称 -->
          <a-form-item label="昵称" name="nickname">
            <a-input
              v-model:value="registerForm.nickname"
              size="large"
              placeholder="请输入昵称（显示给其他用户）"
              allow-clear
            >
              <template #prefix><smile-outlined style="color: #bfbfbf" /></template>
            </a-input>
          </a-form-item>

          <!-- 手机号（可选） -->
          <a-form-item label="手机号（选填）" name="phone">
            <a-input
              v-model:value="registerForm.phone"
              size="large"
              placeholder="请输入手机号"
              allow-clear
            >
              <template #prefix><phone-outlined style="color: #bfbfbf" /></template>
            </a-input>
          </a-form-item>

          <!-- 密码 -->
          <a-form-item label="密码" name="password">
            <a-input-password
              v-model:value="registerForm.password"
              size="large"
              placeholder="请输入密码（6-20个字符）"
            >
              <template #prefix><lock-outlined style="color: #bfbfbf" /></template>
            </a-input-password>
          </a-form-item>

          <!-- 确认密码 -->
          <a-form-item label="确认密码" name="confirmPassword">
            <a-input-password
              v-model:value="registerForm.confirmPassword"
              size="large"
              placeholder="请再次输入密码"
            >
              <template #prefix><lock-outlined style="color: #bfbfbf" /></template>
            </a-input-password>
          </a-form-item>

          <!-- 协议勾选 -->
          <a-form-item name="agree">
            <a-checkbox v-model:checked="registerForm.agree">
              我已阅读并同意
              <a style="color: #1677ff">《用户服务协议》</a>
              和
              <a style="color: #1677ff">《隐私政策》</a>
            </a-checkbox>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
              class="reg-btn"
            >
              立即注册
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 登录入口 -->
        <div class="login-link">
          已有账号？
          <router-link to="/login" style="color: #1677ff; font-weight: 600">
            立即登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/index.js'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  SmileOutlined,
  PhoneOutlined,
  LockOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

// 注册表单数据
const registerForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agree: false
})

/**
 * 自定义校验：确认密码
 */
const validateConfirmPassword = async (rule, value) => {
  if (value && value !== registerForm.password) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

/**
 * 自定义校验：同意协议
 */
const validateAgree = async (rule, value) => {
  if (!value) {
    return Promise.reject('请阅读并同意用户协议')
  }
  return Promise.resolve()
}

// 表单校验规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度为4-20个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称最多20个字符', trigger: 'blur' }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agree: [{ validator: validateAgree, trigger: 'change' }]
}

/**
 * 执行注册操作
 */
async function handleRegister() {
  loading.value = true
  try {
    // 构建注册请求数据（排除确认密码和同意协议字段）
    const { confirmPassword, agree, ...submitData } = registerForm
    await userRegister(submitData)
    message.success('注册成功！请登录您的账号')
    router.push('/login')
  } catch {
    // 错误由request拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f0fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.register-container {
  width: 100%;
  max-width: 480px;
}

/* Logo区域 */
.reg-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  justify-content: center;
  margin-bottom: 24px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1677ff;
}

/* 注册表单卡片 */
.register-box {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 24px rgba(22, 119, 255, 0.12);
}

.reg-title {
  font-size: 24px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 6px;
  text-align: center;
}

.reg-sub {
  color: #8c8c8c;
  font-size: 14px;
  text-align: center;
  margin-bottom: 28px;
}

.reg-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #595959;
}
</style>
