<template>
  <SimpleLayout>
    <div class="framework-center">
      <h1 class="page-title">🎯 Spring AI Alibaba 框架能力中心</h1>
      <p class="page-desc">完整展示框架的所有核心功能</p>

      <!-- 功能导航卡片 -->
      <div class="feature-grid">
        <!-- Hooks -->
        <div class="feature-card" @click="activeTab = 'hooks'">
          <div class="card-icon">🎣</div>
          <h3>Hooks生态</h3>
          <p>审计、安全、Token限流</p>
        </div>

        <!-- Interceptors -->
        <div class="feature-card" @click="activeTab = 'interceptors'">
          <div class="card-icon">🛡️</div>
          <h3>Interceptors</h3>
          <p>性能监控、安全拦截</p>
        </div>

        <!-- Multi-Agent -->
        <div class="feature-card" @click="activeTab = 'multi-agent'">
          <div class="card-icon">👥</div>
          <h3>多Agent协作</h3>
          <p>监督者、并行、通信</p>
        </div>

        <!-- Human-in-Loop -->
        <div class="feature-card" @click="activeTab = 'human-in-loop'">
          <div class="card-icon">✋</div>
          <h3>人工介入</h3>
          <p>审批流程、工作流暂停</p>
        </div>

        <!-- RAG -->
        <div class="feature-card" @click="activeTab = 'rag'">
          <div class="card-icon">📚</div>
          <h3>RAG知识库</h3>
          <p>文档检索、智能问答</p>
        </div>

        <!-- 图片生成 -->
        <div class="feature-card" @click="activeTab = 'image'">
          <div class="card-icon">🎨</div>
          <h3>图片生成</h3>
          <p>AI绘图、通义万相</p>
        </div>
      </div>

      <!-- 详情展示区 -->
      <div v-if="activeTab" class="detail-section">
        <!-- Hooks监控 -->
        <div v-if="activeTab === 'hooks'" class="detail-content">
          <h2>🎣 Hooks监控</h2>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="stat-card">
                <h4>审计日志Hook</h4>
                <p class="stat-value">{{ auditStats.count || 0 }}</p>
                <p class="stat-label">记录数</p>
                <el-button @click="loadAuditLogs" size="small">查看日志</el-button>
              </el-card>
            </el-col>

            <el-col :span="8">
              <el-card class="stat-card">
                <h4>Token限流Hook</h4>
                <p class="stat-value">{{ tokenStats.remaining || 0 }}</p>
                <p class="stat-label">剩余配额</p>
                <el-button @click="checkToken" size="small">检查配额</el-button>
              </el-card>
            </el-col>

            <el-col :span="8">
              <el-card class="stat-card">
                <h4>安全检查Hook</h4>
                <div class="test-input">
                  <el-input v-model="securityInput" placeholder="输入测试文本" />
                  <el-button @click="testSecurity" type="primary" size="small">安全测试</el-button>
                </div>
                <div v-if="securityResult" class="result-text">
                  {{ securityResult.message }}
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- Interceptors监控 -->
        <div v-if="activeTab === 'interceptors'" class="detail-content">
          <h2>🛡️ Interceptors监控</h2>
          
          <el-tabs>
            <el-tab-pane label="性能监控">
              <div v-if="performanceStats" class="stats-grid">
                <div class="stat-item">
                  <span class="stat-label">总调用次数</span>
                  <span class="stat-value">{{ performanceStats.totalCalls }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">平均耗时</span>
                  <span class="stat-value">{{ performanceStats.avgTime }}ms</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">最大耗时</span>
                  <span class="stat-value">{{ performanceStats.maxTime }}ms</span>
                </div>
              </div>
              <el-button @click="loadPerformanceStats">刷新数据</el-button>
            </el-tab-pane>

            <el-tab-pane label="安全拦截">
              <div v-if="safetyStats" class="stats-grid">
                <div class="stat-item">
                  <span class="stat-label">检查次数</span>
                  <span class="stat-value">{{ safetyStats.totalChecks }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">拦截次数</span>
                  <span class="stat-value">{{ safetyStats.blockedCount }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">通过率</span>
                  <span class="stat-value">{{ safetyStats.passRate }}%</span>
                </div>
              </div>
              <el-button @click="loadSafetyStats">刷新数据</el-button>
            </el-tab-pane>

            <el-tab-pane label="工具监控">
              <div v-if="toolStats" class="stats-grid">
                <div class="stat-item">
                  <span class="stat-label">总调用</span>
                  <span class="stat-value">{{ toolStats.totalCalls }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">成功率</span>
                  <span class="stat-value">{{ toolStats.successRate }}%</span>
                </div>
              </div>
              <el-button @click="loadToolStats">刷新数据</el-button>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 多Agent -->
        <div v-if="activeTab === 'multi-agent'" class="detail-content">
          <h2>👥 多Agent协作</h2>
          
          <el-tabs>
            <el-tab-pane label="监督者模式">
              <p>任务分解、分配给多个专家Agent、汇总结果</p>
              <el-input v-model="supervisorTask" placeholder="输入复杂任务" />
              <el-button @click="runSupervisor" type="primary" :loading="supervisorLoading">
                执行监督者模式
              </el-button>
              <div v-if="supervisorResult" class="result-box">
                <pre>{{ JSON.stringify(supervisorResult, null, 2) }}</pre>
              </div>
            </el-tab-pane>

            <el-tab-pane label="并行执行">
              <p>多个Agent同时执行不同任务</p>
              <el-input v-model="parallelTasks" type="textarea" :rows="3" placeholder="输入多个任务，每行一个" />
              <el-button @click="runParallel" type="primary" :loading="parallelLoading">
                并行执行
              </el-button>
              <div v-if="parallelResult" class="result-box">
                <pre>{{ JSON.stringify(parallelResult, null, 2) }}</pre>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- Human-in-Loop -->
        <div v-if="activeTab === 'human-in-loop'" class="detail-content">
          <h2>✋ 人工介入</h2>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <h3>创建审批工作流</h3>
              <el-input v-model="approvalTask" placeholder="输入任务" />
              <el-button @click="createApproval" type="primary">创建</el-button>
            </el-col>

            <el-col :span="12">
              <h3>待审批列表</h3>
              <div v-for="item in pendingApprovals" :key="item.id" class="approval-item">
                <div>{{ item.task }}</div>
                <el-button-group>
                  <el-button @click="approveItem(item.id, true)" size="small" type="success">批准</el-button>
                  <el-button @click="approveItem(item.id, false)" size="small" type="danger">拒绝</el-button>
                </el-button-group>
              </div>
              <el-button @click="loadPendingApprovals" size="small">刷新列表</el-button>
            </el-col>
          </el-row>
        </div>

        <!-- RAG -->
        <div v-if="activeTab === 'rag'" class="detail-content">
          <h2>📚 RAG知识库</h2>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <h3>添加文档</h3>
              <el-input v-model="ragDoc.title" placeholder="文档标题" />
              <el-input v-model="ragDoc.content" type="textarea" :rows="6" placeholder="文档内容" />
              <el-button @click="addDocument" type="primary">添加</el-button>
            </el-col>

            <el-col :span="12">
              <h3>智能问答</h3>
              <el-input v-model="ragQuestion" placeholder="输入问题" />
              <el-button @click="queryRag" type="primary" :loading="ragLoading">提问</el-button>
              <div v-if="ragAnswer" class="answer-box">
                {{ ragAnswer }}
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 图片生成 -->
        <div v-if="activeTab === 'image'" class="detail-content">
          <h2>🎨 图片生成</h2>
          
          <el-input v-model="imagePrompt" type="textarea" :rows="3" placeholder="输入图片描述..." />
          <el-button @click="generateImage" type="primary" :loading="imageLoading" size="large">
            生成图片
          </el-button>

          <div v-if="generatedImage" class="image-result">
            <img :src="generatedImage" alt="生成的图片" />
          </div>
        </div>
      </div>
    </div>
  </SimpleLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import SimpleLayout from '../components/SimpleLayout.vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({ baseURL: '/api' })

const activeTab = ref('')

// Hooks数据
const auditStats = ref({})
const tokenStats = ref({})
const securityInput = ref('')
const securityResult = ref(null)

// Interceptors数据
const performanceStats = ref(null)
const safetyStats = ref(null)
const toolStats = ref(null)

// Multi-Agent数据
const supervisorTask = ref('')
const supervisorLoading = ref(false)
const supervisorResult = ref(null)
const parallelTasks = ref('')
const parallelLoading = ref(false)
const parallelResult = ref(null)

// Human-in-Loop数据
const approvalTask = ref('')
const pendingApprovals = ref([])

// RAG数据
const ragDoc = ref({ title: '', content: '' })
const ragQuestion = ref('')
const ragLoading = ref(false)
const ragAnswer = ref('')

// 图片生成
const imagePrompt = ref('')
const imageLoading = ref(false)
const generatedImage = ref('')

// Hooks功能
const loadAuditLogs = async () => {
  try {
    const res = await api.get('/monitor/audit')
    auditStats.value = { count: res.data.data?.length || 0 }
    ElMessage.success('加载成功')
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const checkToken = async () => {
  try {
    const res = await api.get('/monitor/token/1')
    tokenStats.value = { remaining: res.data.remaining }
    ElMessage.success('查询成功')
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

const testSecurity = async () => {
  try {
    const res = await api.post('/monitor/security/check', { input: securityInput.value })
    securityResult.value = res.data
    ElMessage.success(res.data.safe ? '安全✅' : '不安全⚠️')
  } catch (error) {
    ElMessage.error('检查失败')
  }
}

// Interceptors功能
const loadPerformanceStats = async () => {
  try {
    const res = await api.get('/monitor/performance')
    performanceStats.value = res.data.data
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const loadSafetyStats = async () => {
  try {
    const res = await api.get('/monitor/safety')
    safetyStats.value = res.data.data
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const loadToolStats = async () => {
  try {
    const res = await api.get('/monitor/tools')
    toolStats.value = res.data.data
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

// Multi-Agent功能
const runSupervisor = async () => {
  supervisorLoading.value = true
  try {
    const res = await api.post('/multi-agent/supervisor', { task: supervisorTask.value })
    supervisorResult.value = res.data
    ElMessage.success('执行成功')
  } catch (error) {
    ElMessage.error('执行失败')
  } finally {
    supervisorLoading.value = false
  }
}

const runParallel = async () => {
  parallelLoading.value = true
  try {
    const tasks = parallelTasks.value.split('\n').filter(t => t.trim())
    const res = await api.post('/multi-agent/parallel', { tasks })
    parallelResult.value = res.data
    ElMessage.success('执行成功')
  } catch (error) {
    ElMessage.error('执行失败')
  } finally {
    parallelLoading.value = false
  }
}

// Human-in-Loop功能
const createApproval = async () => {
  try {
    await api.post('/human-in-loop/create', { task: approvalTask.value })
    ElMessage.success('创建成功')
    approvalTask.value = ''
    loadPendingApprovals()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const loadPendingApprovals = async () => {
  try {
    const res = await api.get('/human-in-loop/pending')
    pendingApprovals.value = res.data.data || []
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

const approveItem = async (id, approved) => {
  try {
    await api.post(`/human-in-loop/approve/${id}`, { approved, feedback: '' })
    ElMessage.success(approved ? '已批准' : '已拒绝')
    loadPendingApprovals()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// RAG功能
const addDocument = async () => {
  try {
    await api.post('/rag/document/add', {
      knowledgeBaseId: 'default',
      ...ragDoc.value
    })
    ElMessage.success('添加成功')
    ragDoc.value = { title: '', content: '' }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const queryRag = async () => {
  ragLoading.value = true
  try {
    const res = await api.post('/rag/query', {
      knowledgeBaseId: 'default',
      question: ragQuestion.value
    })
    ragAnswer.value = res.data.answer
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    ragLoading.value = false
  }
}

// 图片生成
const generateImage = async () => {
  imageLoading.value = true
  try {
    const res = await api.post('/image/generate', { prompt: imagePrompt.value })
    generatedImage.value = res.data.imageUrl
    ElMessage.success('生成成功')
  } catch (error) {
    ElMessage.error('生成失败：' + (error.response?.data?.message || error.message))
  } finally {
    imageLoading.value = false
  }
}

onMounted(() => {
  // 默认加载概览数据
})
</script>

<style scoped>
.framework-center {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  margin-bottom: 8px;
}

.page-desc {
  color: #666;
  margin-bottom: 32px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.feature-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid #e8e8e8;
  text-align: center;
}

.feature-card:hover {
  border-color: #409eff;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
}

.card-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.feature-card h3 {
  font-size: 18px;
  margin: 8px 0;
}

.feature-card p {
  color: #909399;
  font-size: 14px;
}

.detail-section {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
}

.detail-content h2 {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin: 16px 0;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.test-input {
  margin-top: 16px;
}

.result-text, .result-box, .answer-box {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}

.approval-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.image-result {
  margin-top: 24px;
  text-align: center;
}

.image-result img {
  max-width: 100%;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}
</style>


