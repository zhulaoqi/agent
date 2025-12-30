<template>
  <div class="chat-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="32"><ChatDotRound /></el-icon>
        <h2>AI Agent</h2>
      </div>
      
      <nav class="nav-menu">
        <router-link to="/chat" class="nav-item active">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能对话</span>
        </router-link>
        <router-link to="/workflow" class="nav-item">
          <el-icon><Connection /></el-icon>
          <span>工作流</span>
        </router-link>
        <router-link to="/rag" class="nav-item">
          <el-icon><Folder /></el-icon>
          <span>RAG知识库</span>
        </router-link>
        <router-link to="/multi-agent" class="nav-item">
          <el-icon><Connection /></el-icon>
          <span>Multi-Agent</span>
        </router-link>
        <router-link to="/human-in-loop" class="nav-item">
          <el-icon><UserFilled /></el-icon>
          <span>人工介入</span>
        </router-link>
        <router-link to="/naming" class="nav-item">
          <el-icon><Edit /></el-icon>
          <span>命名助手</span>
        </router-link>
        <router-link to="/script" class="nav-item">
          <el-icon><Document /></el-icon>
          <span>脚本生成</span>
        </router-link>
        <router-link to="/image-gen" class="nav-item">
          <el-icon><Picture /></el-icon>
          <span>图片生成</span>
        </router-link>
        <router-link to="/history" class="nav-item">
          <el-icon><Clock /></el-icon>
          <span>历史记录</span>
        </router-link>
      </nav>
      
      <div class="user-info">
        <div class="user-avatar">{{ userInitial }}</div>
        <div class="user-details">
          <div class="user-name">{{ userName }}</div>
          <div class="user-status" :class="{ connected }">
            {{ connected ? '在线' : '离线' }}
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn" title="登出">
          <el-icon><SwitchButton /></el-icon>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 快捷功能卡片 -->
      <div v-if="messages.length === 0" class="welcome-cards">
        <div class="welcome-header">
          <el-icon :size="64" color="#8a2be2"><Robot /></el-icon>
          <h2>你好！我是AI智能助手</h2>
          <p>基于 Spring AI Alibaba 构建，支持流式响应、工具调用、工作流编排</p>
        </div>

        <div class="feature-grid">
          <div class="feature-card" @click="sendSuggestion('北京今天天气怎么样？')">
            <el-icon :size="32" color="#409EFF"><Sunny /></el-icon>
            <h3>天气查询</h3>
            <p>实时天气信息</p>
          </div>

          <div class="feature-card" @click="goTo('/naming')">
            <el-icon :size="32" color="#67C23A"><Edit /></el-icon>
            <h3>命名助手</h3>
            <p>智能变量命名</p>
          </div>

          <div class="feature-card" @click="goTo('/script')">
            <el-icon :size="32" color="#E6A23C"><Document /></el-icon>
            <h3>脚本生成</h3>
            <p>自动生成代码</p>
          </div>

          <div class="feature-card" @click="goTo('/image-gen')">
            <el-icon :size="32" color="#F56C6C"><Picture /></el-icon>
            <h3>图片生成</h3>
            <p>AI绘图创作</p>
          </div>

          <div class="feature-card" @click="goTo('/workflow')">
            <el-icon :size="32" color="#ba55d3"><Connection /></el-icon>
            <h3>工作流</h3>
            <p>复杂任务编排</p>
          </div>

          <div class="feature-card" @click="sendSuggestion('帮我分析一下这段文本的情感倾向')">
            <el-icon :size="32" color="#909399"><DataAnalysis /></el-icon>
            <h3>文本分析</h3>
            <p>结构化分析</p>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="messages" ref="messagesRef">
        <div v-for="msg in messages" :key="msg.id" 
             :class="['message', msg.role]">
          <div class="message-avatar">
            <el-avatar v-if="msg.role === 'user'" :size="32">{{ userInitial }}</el-avatar>
            <el-icon v-else :size="32" color="#ba55d3"><Robot /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(msg.content)"></div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <div v-if="loading" class="message assistant">
          <div class="message-avatar">
            <el-icon :size="32" color="#ba55d3"><Robot /></el-icon>
          </div>
          <div class="message-content">
            <div class="message-text">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <el-input
          v-model="userInput"
          type="textarea"
          :rows="3"
          placeholder="输入消息... (Enter发送, Shift+Enter换行)"
          @keydown.enter.exact.prevent="sendMessage"
          @keydown.shift.enter="handleShiftEnter"
          :disabled="loading"
        />
        <div class="input-actions">
          <el-button @click="clearMessages" size="small" text>
            <el-icon><Delete /></el-icon> 清空
          </el-button>
          <el-button 
            type="primary" 
            @click="sendMessage"
            :loading="loading"
            :disabled="!userInput.trim()"
          >
            <el-icon><Promotion /></el-icon>
            发送 (Enter)
          </el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api/agent'

const router = useRouter()
const messages = ref([])
const userInput = ref('')
const loading = ref(false)
const connected = ref(false)
const messagesRef = ref(null)
let currentEventSource = null

const user = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : { username: 'Guest', nickname: '访客' }
})

const userName = computed(() => user.value.nickname || user.value.username)
const userInitial = computed(() => (userName.value[0] || 'U').toUpperCase())

// 检查连接状态
onMounted(async () => {
  try {
    await api.health()
    connected.value = true
    ElMessage.success('已连接到AI服务，支持流式响应')
  } catch (error) {
    connected.value = false
    ElMessage.error('无法连接到AI服务')
  }
})

// 流式发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || loading.value) return

  const userMsg = {
    id: Date.now(),
    role: 'user',
    content: userInput.value,
    time: new Date().toLocaleTimeString()
  }
  messages.value.push(userMsg)
  
  const messageContent = userInput.value
  userInput.value = ''
  loading.value = true

  // 创建助手消息（流式填充）
  const assistantMsg = {
    id: Date.now() + 1,
    role: 'assistant',
    content: '',
    time: new Date().toLocaleTimeString()
  }
  messages.value.push(assistantMsg)

  scrollToBottom()

  try {
    // 使用流式API
    await api.streamChatPost(messageContent, (chunk) => {
      assistantMsg.content += chunk
      scrollToBottom()
    })

    loading.value = false
    
  } catch (error) {
    console.error('发送失败:', error)
    assistantMsg.content = '抱歉，消息发送失败，请重试。'
    loading.value = false
    ElMessage.error('消息发送失败')
  }
}

// 处理Shift+Enter（换行）
const handleShiftEnter = (e) => {
  // 允许默认行为（换行）
}

// 发送建议
const sendSuggestion = (text) => {
  userInput.value = text
  nextTick(() => {
    sendMessage()
  })
}

// 跳转页面
const goTo = (path) => {
  router.push(path)
}

// 清空消息
const clearMessages = () => {
  messages.value = []
  ElMessage.success('消息已清空')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 格式化消息
const formatMessage = (content) => {
  if (!content) return ''
  
  // 代码块格式化
  content = content.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    return `<pre><code class="language-${lang || 'text'}">${escapeHtml(code)}</code></pre>`
  })
  
  // 行内代码
  content = content.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 换行
  content = content.replace(/\n/g, '<br>')
  
  return content
}

const escapeHtml = (text) => {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 登出
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
  ElMessage.success('已登出')
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background: #0d0221;
}

.sidebar {
  width: 260px;
  background: rgba(13, 2, 33, 0.8);
  border-right: 1px solid rgba(138, 43, 226, 0.2);
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  color: #ba55d3;
}

.logo h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}

.nav-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: all 0.3s;
}

.nav-item:hover {
  background: rgba(138, 43, 226, 0.1);
  color: #ba55d3;
}

.nav-item.active {
  background: rgba(138, 43, 226, 0.2);
  color: #ba55d3;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border: 1px solid rgba(138, 43, 226, 0.2);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8a2be2, #da70d6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
}

.user-details {
  flex: 1;
}

.user-name {
  color: #fff;
  font-weight: 500;
  font-size: 14px;
}

.user-status {
  font-size: 12px;
  color: #999;
}

.user-status.connected {
  color: #67C23A;
}

.logout-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 82, 82, 0.1);
  color: #ff5252;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #1a1a2e;
}

.welcome-cards {
  padding: 40px;
  overflow-y: auto;
}

.welcome-header {
  text-align: center;
  margin-bottom: 40px;
}

.welcome-header h2 {
  color: #fff;
  margin: 20px 0 10px;
  font-size: 28px;
}

.welcome-header p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  background: rgba(138, 43, 226, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.2);
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.feature-card:hover {
  background: rgba(138, 43, 226, 0.1);
  border-color: #ba55d3;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(138, 43, 226, 0.3);
}

.feature-card h3 {
  color: #fff;
  margin: 12px 0 8px;
  font-size: 16px;
}

.feature-card p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  margin: 0;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.2);
  border-radius: 12px;
  padding: 12px 16px;
}

.message.user .message-content {
  background: linear-gradient(135deg, #8a2be2, #da70d6);
  border: none;
}

.message-text {
  color: #fff;
  line-height: 1.6;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 8px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ba55d3;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  30% {
    opacity: 1;
    transform: translateY(-10px);
  }
}

.input-area {
  padding: 20px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.input-area :deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.3);
  color: #fff;
  border-radius: 8px;
}

.input-area :deep(.el-textarea__inner):focus {
  border-color: #ba55d3;
  box-shadow: 0 0 0 2px rgba(186, 85, 211, 0.2);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
}

:deep(pre) {
  background: rgba(0, 0, 0, 0.3);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

:deep(code) {
  background: rgba(138, 43, 226, 0.2);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

:deep(a) {
  color: #ba55d3;
  text-decoration: none;
}

:deep(a:hover) {
  text-decoration: underline;
}
</style>
