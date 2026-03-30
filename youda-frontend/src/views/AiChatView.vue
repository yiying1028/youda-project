<template>
  <div class="ai-chat-page">
    <div class="session-panel">
      <div class="session-header">
        <h3 class="session-title">AI答疑</h3>
        <a-button type="primary" size="small" @click="handleNewSession" :loading="createLoading">
          <template #icon><plus-outlined /></template>
          新对话
        </a-button>
      </div>

      <div class="session-list" ref="sessionListRef">
        <a-spin :spinning="sessionLoading">
          <div
            v-for="session in sessions"
            :key="session.chatId"
            class="session-item"
            :class="{ active: currentSessionId === session.chatId }"
            @click="selectSession(session)"
          >
            <robot-outlined class="session-icon" />
            <div class="session-info">
              <div class="session-name">{{ session.title || '新对话' }}</div>
              <div class="session-time">{{ formatTime(session.updateTime) }}</div>
            </div>
            <a-popconfirm title="确定删除这段对话吗？" @confirm.stop="handleDeleteSession(session.chatId)">
              <delete-outlined class="session-delete" @click.stop />
            </a-popconfirm>
          </div>

          <div v-if="sessions.length === 0 && !sessionLoading" class="no-sessions">
            <robot-outlined style="font-size: 32px; color: #d9d9d9" />
            <p>还没有对话记录</p>
            <a-button type="primary" size="small" @click="handleNewSession">开始提问</a-button>
          </div>
        </a-spin>
      </div>
    </div>

    <div class="chat-panel">
      <div v-if="!currentSessionId" class="welcome-screen">
        <div class="welcome-content">
          <div class="welcome-icon">
            <robot-outlined />
          </div>
          <h2 class="welcome-title">优答 AI 学习助手</h2>
          <p class="welcome-desc">
            我可以帮助你解答中小学常见学习问题，适合做概念讲解、解题思路梳理和复习提纲整理。
          </p>
          <div class="welcome-examples">
            <div
              v-for="example in exampleQuestions"
              :key="example"
              class="example-item"
              @click="startWithExample(example)"
            >
              {{ example }}
            </div>
          </div>
          <a-button type="primary" size="large" @click="handleNewSession" :loading="createLoading">
            <plus-outlined /> 开始新对话
          </a-button>
        </div>
      </div>

      <template v-else>
        <div class="chat-header">
          <robot-outlined />
          <span>{{ currentSession?.title || 'AI答疑对话' }}</span>
        </div>

        <div class="message-list" ref="messageListRef">
          <a-spin :spinning="messageLoading">
            <div class="message ai-message">
              <div class="msg-avatar ai-avatar">AI</div>
              <div class="msg-bubble ai-bubble">
                你好，我是优答 AI 助手。把题目、知识点或你的卡点发给我，我会尽量用适合学生理解的方式来回答。
              </div>
            </div>

            <div
              v-for="msg in messages"
              :key="msg.messageId || msg.id"
              class="message"
              :class="msg.role === 'user' ? 'user-message' : 'ai-message'"
            >
              <div v-if="msg.role === 'assistant'" class="msg-avatar ai-avatar">AI</div>

              <div class="msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
                <img v-if="msg.image" :src="msg.image" alt="用户上传图片" class="msg-image" />
                <div class="msg-content">{{ msg.content }}</div>
              </div>

              <div v-if="msg.role === 'user'" class="msg-avatar user-avatar">我</div>
            </div>

            <div v-if="aiTyping" class="message ai-message">
              <div class="msg-avatar ai-avatar">AI</div>
              <div class="msg-bubble ai-bubble typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </a-spin>
        </div>

        <div class="input-area">
          <div v-if="uploadedImage" class="image-preview">
            <img :src="uploadedImage" alt="待发送图片" />
            <close-circle-outlined class="remove-image" @click="uploadedImage = ''" />
          </div>

          <div class="input-row">
            <a-upload :show-upload-list="false" :before-upload="handleImageUpload" accept="image/*">
              <a-button class="upload-btn" :disabled="sending">
                <picture-outlined />
              </a-button>
            </a-upload>

            <a-textarea
              v-model:value="inputContent"
              placeholder="输入问题，按 Ctrl+Enter 发送..."
              :rows="3"
              :maxlength="2000"
              :disabled="sending"
              @keydown.ctrl.enter="handleSend"
              class="message-input"
            />

            <a-button
              type="primary"
              :loading="sending"
              :disabled="!inputContent.trim() && !uploadedImage"
              @click="handleSend"
              class="send-btn"
            >
              发送
            </a-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import {
  getChatList,
  getChatDetail,
  createChatSession,
  sendMessage,
  deleteChat,
  uploadChatImage
} from '@/api/index.js'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  PlusOutlined,
  RobotOutlined,
  DeleteOutlined,
  PictureOutlined,
  CloseCircleOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const sessions = ref([])
const sessionLoading = ref(false)
const createLoading = ref(false)

const currentSessionId = ref(null)
const currentSession = ref(null)

const messages = ref([])
const messageLoading = ref(false)
const sending = ref(false)
const aiTyping = ref(false)

const inputContent = ref('')
const uploadedImage = ref('')

const sessionListRef = ref(null)
const messageListRef = ref(null)

const exampleQuestions = [
  '如何解一元二次方程？',
  '请帮我分析这道语文阅读理解题。',
  '英语时态怎么区分和使用？',
  '化学方程式怎么配平？',
  '牛顿三定律分别是什么意思？'
]

function extractChatId(value, seen = new WeakSet()) {
  if (!value) return ''
  if (typeof value === 'string') return value
  if (typeof value !== 'object') return ''
  if (seen.has(value)) return ''
  seen.add(value)
  if (typeof value.chatId === 'string') return value.chatId
  if (typeof value.id === 'string' && value.id.startsWith('chat_')) return value.id
  if (value.data) {
    const nestedChatId = extractChatId(value.data, seen)
    if (nestedChatId) return nestedChatId
  }
  for (const nested of Object.values(value)) {
    const nestedChatId = extractChatId(nested, seen)
    if (nestedChatId) return nestedChatId
  }
  return ''
}

async function scrollToBottom() {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function loadSessions() {
  sessionLoading.value = true
  try {
    const data = await getChatList({ current: 1, size: 50 })
    sessions.value = (data?.records || []).map((session) => ({
      ...session,
      chatId: extractChatId(session?.chatId) || extractChatId(session)
    }))
  } finally {
    sessionLoading.value = false
  }
}

async function selectSession(session) {
  const chatId = extractChatId(session?.chatId) || extractChatId(session)
  if (!chatId) {
    message.error('会话ID无效，请刷新后重试')
    return
  }
  currentSessionId.value = chatId
  currentSession.value = session
  messages.value = []
  messageLoading.value = true
  try {
    const data = await getChatDetail(chatId)
    messages.value = data?.messages || []
    await scrollToBottom()
  } finally {
    messageLoading.value = false
  }
}

async function handleNewSession() {
  if (!userStore.isLoggedIn) {
    message.warning('请先登录后使用 AI 答疑')
    router.push('/login')
    return
  }

  createLoading.value = true
  try {
    const data = await createChatSession()
    const chatId = extractChatId(data)
    if (!chatId) {
      throw new Error('创建会话失败：未返回有效 chatId')
    }
    const newSession = {
      chatId,
      title: '新对话',
      updateTime: new Date()
    }
    sessions.value.unshift(newSession)
    await selectSession(newSession)
  } finally {
    createLoading.value = false
  }
}

async function handleDeleteSession(chatId) {
  try {
    await deleteChat(chatId)
    sessions.value = sessions.value.filter((session) => session.chatId !== chatId)
    if (currentSessionId.value === chatId) {
      currentSessionId.value = null
      currentSession.value = null
      messages.value = []
    }
    message.success('对话已删除')
  } catch {}
}

async function startWithExample(question) {
  await handleNewSession()
  inputContent.value = question
}

async function handleImageUpload(file) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const data = await uploadChatImage(formData)
    uploadedImage.value = data?.url || data
  } catch {}
  return false
}

async function handleSend() {
  const content = inputContent.value.trim()
  if (!content && !uploadedImage.value) return
  if (sending.value) return
  const chatId = extractChatId(currentSessionId.value)
  if (!chatId) {
    message.error('会话ID无效，请重新创建对话')
    return
  }

  const userMsg = {
    id: Date.now(),
    role: 'user',
    content,
    image: uploadedImage.value,
    createTime: new Date()
  }
  messages.value.push(userMsg)
  await scrollToBottom()

  const sentContent = content
  const sentImage = uploadedImage.value
  inputContent.value = ''
  uploadedImage.value = ''

  sending.value = true
  aiTyping.value = true

  try {
    const data = await sendMessage(chatId, {
      content: sentContent,
      image: sentImage || undefined
    })

    aiTyping.value = false
    messages.value.push({
      messageId: data?.messageId || `msg_${Date.now() + 1}`,
      role: data?.role || 'assistant',
      content: data?.content || '',
      image: data?.image,
      createTime: data?.createTime || new Date()
    })
    await scrollToBottom()

    if (currentSession.value && !currentSession.value.title?.trim()) {
      currentSession.value.title = sentContent.substring(0, 30) || '新对话'
    }

    const index = sessions.value.findIndex((session) => session.chatId === chatId)
    if (index !== -1) {
      sessions.value[index] = {
        ...sessions.value[index],
        title: currentSession.value?.title || sentContent.substring(0, 30) || '新对话',
        updateTime: new Date()
      }
    }
  } catch {
    aiTyping.value = false
    messages.value = messages.value.filter((msg) => msg.id !== userMsg.id)
  } finally {
    sending.value = false
  }
}

function formatTime(time) {
  return time ? dayjs(time).format('MM-DD HH:mm') : ''
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadSessions()
  }
})
</script>

<style scoped>
.ai-chat-page {
  height: calc(100vh - 64px);
  display: flex;
  overflow: hidden;
  background: #f5f7fa;
}

.session-panel {
  width: 260px;
  background: #fff;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.session-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.session-title {
  font-size: 16px;
  font-weight: 700;
  color: #262626;
  margin: 0;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
  position: relative;
}

.session-item:hover {
  background: #f5f8ff;
}

.session-item.active {
  background: #e8f0fe;
}

.session-item:hover .session-delete {
  opacity: 1;
}

.session-icon {
  color: #1677ff;
  font-size: 18px;
  flex-shrink: 0;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 13px;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 11px;
  color: #bfbfbf;
  margin-top: 2px;
}

.session-delete {
  opacity: 0;
  color: #ff4d4f;
  font-size: 14px;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.no-sessions {
  text-align: center;
  padding: 40px 16px;
  color: #bfbfbf;
}

.no-sessions p {
  margin: 8px 0 12px;
  font-size: 13px;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f8fafb;
}

.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.welcome-content {
  text-align: center;
  max-width: 640px;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  font-size: 36px;
  color: #1677ff;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.12), rgba(64, 150, 255, 0.2));
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: #262626;
  margin-bottom: 12px;
}

.welcome-desc {
  font-size: 15px;
  color: #8c8c8c;
  line-height: 1.7;
  margin-bottom: 28px;
}

.welcome-examples {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 28px;
}

.example-item {
  padding: 10px 16px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #595959;
  text-align: left;
  transition: all 0.2s;
}

.example-item:hover {
  border-color: #1677ff;
  color: #1677ff;
  background: #f0f5ff;
}

.chat-header {
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.user-message {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.ai-avatar {
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
}

.user-avatar {
  background: linear-gradient(135deg, #52c41a, #389e0d);
  color: #fff;
}

.msg-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.ai-bubble {
  background: #fff;
  color: #262626;
  border: 1px solid #f0f0f0;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.user-bubble {
  background: linear-gradient(135deg, #1677ff, #4096ff);
  color: #fff;
  border-top-right-radius: 4px;
}

.msg-image {
  max-width: 100%;
  max-height: 220px;
  border-radius: 6px;
  margin-bottom: 8px;
  display: block;
}

.msg-content {
  white-space: pre-wrap;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1677ff;
  animation: typing-bounce 1.2s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

.input-area {
  background: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.image-preview {
  position: relative;
  display: inline-block;
  margin-bottom: 10px;
}

.image-preview img {
  height: 60px;
  border-radius: 6px;
}

.remove-image {
  position: absolute;
  top: -6px;
  right: -6px;
  font-size: 18px;
  color: #ff4d4f;
  cursor: pointer;
  background: #fff;
  border-radius: 50%;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.upload-btn {
  height: 80px;
  flex-shrink: 0;
}

.message-input {
  flex: 1;
  resize: none;
  font-size: 14px;
}

.send-btn {
  height: 80px;
  padding: 0 20px;
  font-weight: 600;
  font-size: 15px;
}

@media (max-width: 900px) {
  .ai-chat-page {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 64px);
  }

  .session-panel {
    width: 100%;
    max-height: 240px;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }

  .msg-bubble {
    max-width: 85%;
  }
}
</style>



