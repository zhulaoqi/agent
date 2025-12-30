import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// ===== SSE辅助函数 =====
/**
 * 处理SSE数据行
 * 支持格式：
 * - data: content
 * - data:content
 * - 多行data
 */
function processLine(line, onChunk) {
  const lines = line.split('\n')
  
  for (const l of lines) {
    const trimmed = l.trim()
    
    if (trimmed.startsWith('data:')) {
      // 提取data后的内容，支持 "data: " 和 "data:"
      let content = trimmed.substring(5).trim()
      
      // 去掉可能的引号
      if (content.startsWith('"') && content.endsWith('"')) {
        content = content.slice(1, -1)
      }
      
      // 忽略控制标记
      if (content && content !== '[DONE]' && content !== '[ERROR]' && content !== 'null') {
        onChunk(content)
      }
    }
  }
}

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default {
  // ===== 认证相关 =====
  login(data) {
    return api.post('/auth/login', data)
  },
  register(data) {
    return api.post('/auth/register', data)
  },
  logout() {
    return api.post('/auth/logout')
  },
  getCurrentUser() {
    return api.get('/auth/user')
  },

  // ===== 流式聊天 =====
  streamChat(message, onMessage, onComplete, onError) {
    const token = localStorage.getItem('token')
    const eventSource = new EventSource(
      `/api/stream/chat?message=${encodeURIComponent(message)}${token ? '&token=' + token : ''}`
    )

    eventSource.onmessage = (event) => {
      if (event.data) {
        const text = event.data.replace(/^data:\s*/, '')
        if (text && text !== '[DONE]') {
          onMessage(text)
        }
      }
    }

    eventSource.onerror = (error) => {
      eventSource.close()
      onError && onError(error)
    }

    // 监听完成事件
    setTimeout(() => {
      eventSource.close()
      onComplete && onComplete()
    }, 60000) // 60秒超时

    return eventSource
  },

  // 流式对话（POST版本） - 优化SSE解析
  async streamChatPost(message, onChunk) {
    const response = await fetch('/api/stream/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ message })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          // 处理最后的buffer
          if (buffer.trim()) {
            processLine(buffer, onChunk)
          }
          break
        }

        // 解码并追加到buffer
        buffer += decoder.decode(value, { stream: true })
        
        // 按 \n\n 分割SSE事件（标准SSE格式）
        const events = buffer.split('\n\n')
        
        // 保留最后一个不完整的事件
        buffer = events.pop() || ''
        
        // 处理完整的事件
        for (const event of events) {
          if (event.trim()) {
            processLine(event, onChunk)
          }
        }
      }
    } catch (error) {
      console.error('Stream reading error:', error)
      throw error
    }
  },

  // ===== 聊天相关 =====
  // 普通对话（非流式）
  chat(message) {
    return api.post('/agent/chat', { message })
  },

  // ===== 工具相关 =====
  // 天气查询
  queryWeather(city) {
    return api.post('/tool/weather', { city })
  },

  // 命名助手
  generateNaming(description) {
    return api.post('/tool/naming', { description })
  },

  // 脚本生成
  generateScript(requirements, language) {
    return api.post('/tool/script', { requirements, language })
  },

  // 脚本生成（流式）- 优化SSE解析
  async streamScriptGen(requirements, language, onChunk) {
    const response = await fetch('/api/stream/code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ requirement: requirements, language })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          if (buffer.trim()) {
            processLine(buffer, onChunk)
          }
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const events = buffer.split('\n\n')
        buffer = events.pop() || ''
        
        for (const event of events) {
          if (event.trim()) {
            processLine(event, onChunk)
          }
        }
      }
    } catch (error) {
      console.error('Stream reading error:', error)
      throw error
    }
  },

  // ===== 生成功能 =====
  // 图片生成
  generateImage(prompt) {
    return api.post('/image/generate', { prompt })
  },

  // PPT生成
  generatePpt(data) {
    return api.post('/ppt/generate', data, {
      responseType: 'blob'
    })
  },

  // ===== 工作流相关 =====
  // 开发工作流
  executeDevWorkflow(requirement) {
    return api.post('/workflow/dev', { requirement })
  },

  // 自定义工作流
  executeCustomWorkflow(data) {
    return api.post('/workflow/custom', data)
  },

  // ===== 演示功能 =====
  // 结构化天气分析
  demoStructuredWeather(city, weatherData) {
    return api.post('/demo/structured/weather', { city, weatherData })
  },

  // 结构化文本分析
  demoStructuredAnalysis(text) {
    return api.post('/demo/structured/analysis', { text })
  },

  // 上下文管理演示
  demoContext(conversationId) {
    return api.get(`/demo/context/${conversationId}`)
  },

  // 工作流演示
  demoWorkflow(requirement) {
    return api.post('/demo/workflow/dev', { requirement })
  },

  // Token限流演示
  demoTokenHook(userId) {
    return api.get(`/demo/hook/token/${userId}`)
  },

  // 安全检查演示
  demoSecurityHook(input) {
    return api.post('/demo/hook/security', { input })
  },

  // 功能清单
  getFeatures() {
    return api.get('/demo/features')
  },

  // ===== 其他 =====
  health() {
    return api.get('/test/health')
  },

  toolTest() {
    return api.get('/test/tool-test')
  }
}
