<template>
  <SimpleLayout>
    <div class="chat-page">
      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesRef">
        <!-- 欢迎卡片 -->
        <div v-if="messages.length === 0" class="welcome-card">
          <div class="welcome-icon">🤖</div>
          <h2>你好！我是AI智能助手</h2>
          <p>基于 Spring AI Alibaba 构建，支持流式响应、工具调用</p>
          
          <div class="quick-actions">
            <button @click="sendSuggestion('北京今天天气怎么样？')" class="quick-btn">
              ☀️ 查天气
            </button>
            <button @click="sendSuggestion('帮我生成一个用户登录的代码')" class="quick-btn">
              💻 写代码
            </button>
            <button @click="sendSuggestion('解释什么是微服务架构')" class="quick-btn">
              📚 学知识
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="msg.role">
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="message-item assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-container">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact="handleSend"
            placeholder="输入消息... (Enter发送，Shift+Enter换行)"
            rows="1"
            ref="textareaRef"
          ></textarea>
          <button @click="handleSend" :disabled="!inputMessage.trim() || loading" class="send-btn">
            <span v-if="!loading">发送 ➤</span>
            <span v-else>...</span>
          </button>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import SimpleLayout from '../components/SimpleLayout.vue'
import api from '../api/agent'
import { ElMessage } from 'element-plus'

const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const messagesRef = ref(null)
const textareaRef = ref(null)
const conversationId = ref(null)

// 发送消息
const handleSend = async (event) => {
  if (event.shiftKey) return // Shift+Enter换行
  event.preventDefault()
  
  const content = inputMessage.value.trim()
  if (!content || loading.value) return
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content,
    time: new Date().toLocaleTimeString()
  })
  
  inputMessage.value = ''
  loading.value = true
  scrollToBottom()
  
  try {
    // 创建助手消息（流式填充）
    const assistantMsg = {
      role: 'assistant',
      content: '',
      time: new Date().toLocaleTimeString()
    }
    messages.value.push(assistantMsg)
    
    // 流式接收
    await api.streamChatPost(content, (chunk) => {
      assistantMsg.content += chunk
      scrollToBottom()
    })
    
  } catch (error) {
    console.error('发送失败:', error)
    ElMessage.error('发送失败，请重试')
    
    // 错误时显示提示
    const lastMsg = messages.value[messages.value.length - 1]
    if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.content) {
      lastMsg.content = '抱歉，发送失败，请重试。'
    }
  } finally {
    loading.value = false
  }
}

// 快捷发送
const sendSuggestion = (text) => {
  inputMessage.value = text
  handleSend(new Event('click'))
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* 欢迎卡片 */
.welcome-card {
  text-align: center;
  padding: 60px 20px;
}

.welcome-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.welcome-card h2 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}

.welcome-card p {
  color: #909399;
  margin: 0 0 32px 0;
}

.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.quick-btn {
  padding: 10px 20px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  color: #606266;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  background: #f5f7fa;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-item.user .message-text {
  background: #409eff;
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
  padding: 0 4px;
}

/* 打字动画 */
.typing-indicator {
  padding: 12px 16px;
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c0c4cc;
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
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* 输入区 */
.input-container {
  border-top: 1px solid #e4e7ed;
  padding: 20px 24px;
  background: #fff;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  max-height: 120px;
  line-height: 1.5;
}

textarea:focus {
  outline: none;
  border-color: #409eff;
}

.send-btn {
  padding: 12px 24px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  background: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.send-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

/* 滚动条 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>

