import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router/index.js'

// 创建Vue应用实例
const app = createApp(App)

// 注册Pinia状态管理
app.use(createPinia())

// 注册Vue Router路由
app.use(router)

// 注册Ant Design Vue组件库
app.use(Antd)

// 挂载应用到DOM
app.mount('#app')
