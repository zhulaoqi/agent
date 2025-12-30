<template>
  <div class="login-container">
    <!-- 粒子连线背景 -->
    <canvas ref="canvas" class="particle-canvas"></canvas>
    
    <div class="login-card">
      <div class="card-header">
        <div class="logo">
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <h1>AI Agent</h1>
        </div>
        <p class="subtitle">企业级智能办公助手</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <input 
            v-model="form.username" 
            type="text" 
            placeholder="用户名"
            required
            class="form-control"
          />
        </div>

        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <input 
            v-model="form.password" 
            type="password" 
            placeholder="密码"
            required
            class="form-control"
          />
        </div>

        <button type="submit" :disabled="loading" class="btn-primary">
          <span v-if="!loading">登录</span>
          <span v-else class="loading-spinner"></span>
        </button>

        <div v-if="error" class="error-alert">
          <svg class="alert-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          {{ error }}
        </div>
      </form>

      <div class="demo-accounts">
        <div class="demo-title">测试账号</div>
        <div class="demo-list">
          <span @click="quickLogin('admin', 'admin123')" class="demo-account">
            👨‍💼 admin / admin123
          </span>
          <span @click="quickLogin('user', 'user123')" class="demo-account">
            👤 user / user123
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api/agent'

const router = useRouter()
const canvas = ref(null)
const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)
const error = ref('')

let particles = []
let animationId = null
let mouse = { x: null, y: null, radius: 150 }

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  
  try {
    const response = await api.login(form.value)
    
    if (response.success) {
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      router.push('/chat')
    } else {
      error.value = response.message || '登录失败'
    }
  } catch (err) {
    error.value = err.response?.data?.message || err.message || '网络错误'
  } finally {
    loading.value = false
  }
}

const quickLogin = (username, password) => {
  form.value.username = username
  form.value.password = password
  handleLogin()
}

// 粒子系统
const initParticles = () => {
  if (!canvas.value) return
  
  const ctx = canvas.value.getContext('2d')
  canvas.value.width = window.innerWidth
  canvas.value.height = window.innerHeight
  
  // 创建粒子
  particles = []
  const particleCount = 80
  
  for (let i = 0; i < particleCount; i++) {
    particles.push({
      x: Math.random() * canvas.value.width,
      y: Math.random() * canvas.value.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      radius: Math.random() * 2 + 1
    })
  }
  
  animate(ctx)
}

const animate = (ctx) => {
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  // 更新粒子位置
  particles.forEach(particle => {
    // 鼠标吸附效果
    if (mouse.x != null && mouse.y != null) {
      const dx = mouse.x - particle.x
      const dy = mouse.y - particle.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < mouse.radius) {
        const forceDirectionX = dx / distance
        const forceDirectionY = dy / distance
        const force = (mouse.radius - distance) / mouse.radius
        const directionX = forceDirectionX * force * 0.6
        const directionY = forceDirectionY * force * 0.6
        
        particle.x += directionX
        particle.y += directionY
      }
    }
    
    particle.x += particle.vx
    particle.y += particle.vy
    
    if (particle.x < 0 || particle.x > canvas.value.width) particle.vx *= -1
    if (particle.y < 0 || particle.y > canvas.value.height) particle.vy *= -1
    
    // 绘制粒子
    ctx.beginPath()
    ctx.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(138, 43, 226, 0.6)'
    ctx.shadowBlur = 10
    ctx.shadowColor = 'rgba(138, 43, 226, 0.5)'
    ctx.fill()
    ctx.shadowBlur = 0
  })
  
  // 连接近距离的粒子
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < 120) {
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        const opacity = (1 - distance / 120) * 0.4
        ctx.strokeStyle = `rgba(138, 43, 226, ${opacity})`
        ctx.lineWidth = 0.8
        ctx.stroke()
      }
    }
  }
  
  // 鼠标连线
  if (mouse.x != null && mouse.y != null) {
    particles.forEach(particle => {
      const dx = mouse.x - particle.x
      const dy = mouse.y - particle.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < mouse.radius) {
        ctx.beginPath()
        ctx.moveTo(particle.x, particle.y)
        ctx.lineTo(mouse.x, mouse.y)
        const opacity = (1 - distance / mouse.radius) * 0.5
        ctx.strokeStyle = `rgba(255, 20, 147, ${opacity})`
        ctx.lineWidth = 1
        ctx.stroke()
      }
    })
  }
  
  animationId = requestAnimationFrame(() => animate(ctx))
}

const handleResize = () => {
  if (canvas.value) {
    canvas.value.width = window.innerWidth
    canvas.value.height = window.innerHeight
  }
}

const handleMouseMove = (e) => {
  mouse.x = e.clientX
  mouse.y = e.clientY
}

const handleMouseLeave = () => {
  mouse.x = null
  mouse.y = null
}

onMounted(() => {
  initParticles()
  window.addEventListener('resize', handleResize)
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseleave', handleMouseLeave)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseleave', handleMouseLeave)
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0d0221;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(13, 2, 33, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(138, 43, 226, 0.2);
  border-radius: 20px;
  padding: 48px 40px;
  box-shadow: 0 8px 32px rgba(138, 43, 226, 0.2), 0 0 60px rgba(138, 43, 226, 0.1);
  position: relative;
  z-index: 1;
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  color: #ba55d3;
  filter: drop-shadow(0 0 10px rgba(186, 85, 211, 0.8));
}

.logo h1 {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  text-shadow: 0 0 20px rgba(138, 43, 226, 0.6);
}

.subtitle {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 32px;
}

.input-group {
  position: relative;
  margin-bottom: 20px;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: rgba(186, 85, 211, 0.6);
  pointer-events: none;
}

.input-icon svg {
  width: 100%;
  height: 100%;
}

.form-control {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 1px solid rgba(138, 43, 226, 0.3);
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.03);
  color: #fff;
}

.form-control::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.form-control:focus {
  outline: none;
  border-color: #ba55d3;
  background: rgba(255, 255, 255, 0.06);
  box-shadow: 0 0 20px rgba(186, 85, 211, 0.3);
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #8a2be2 0%, #da70d6 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 24px;
  box-shadow: 0 0 20px rgba(138, 43, 226, 0.4);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 0 35px rgba(186, 85, 211, 0.7);
  background: linear-gradient(135deg, #9a3bf2 0%, #ea80e6 100%);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-alert {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 82, 82, 0.1);
  color: #ff5252;
  border: 1px solid rgba(255, 82, 82, 0.3);
  border-radius: 8px;
  font-size: 14px;
  margin-top: 16px;
}

.alert-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.demo-accounts {
  padding-top: 24px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.demo-title {
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  margin-bottom: 12px;
}

.demo-list {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.demo-account {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(138, 43, 226, 0.3);
  border-radius: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s;
}

.demo-account:hover {
  background: rgba(138, 43, 226, 0.15);
  border-color: #ba55d3;
  color: #ba55d3;
  transform: translateY(-2px);
  box-shadow: 0 0 15px rgba(138, 43, 226, 0.4);
}
</style>

