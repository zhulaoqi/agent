<template>
  <div class="page-container">
    <div class="page-header">
      <h1><el-icon><Connection /></el-icon> 工作流编排</h1>
      <p>基于Graph Core的复杂任务流程</p>
    </div>

    <el-tabs v-model="activeTab" class="workflow-tabs">
      <!-- 开发工作流 -->
      <el-tab-pane label="开发工作流" name="dev">
        <div class="tab-content">
          <el-input
            v-model="devRequirement"
            type="textarea"
            :rows="6"
            placeholder="输入开发需求，AI将自动完成：需求分析 → 方案设计 → 代码生成 → 代码审查"
          />
          <el-button 
            type="primary" 
            @click="executeDevWorkflow"
            :loading="devLoading"
            size="large"
          >
            <el-icon><CaretRight /></el-icon> 执行工作流
          </el-button>

          <div v-if="devResult" class="result-section">
            <h3>执行结果</h3>
            
            <el-steps :active="devStepActive" finish-status="success" class="workflow-steps">
              <el-step title="需求分析" />
              <el-step title="方案设计" />
              <el-step title="代码生成" />
              <el-step title="代码审查" />
            </el-steps>

            <div class="result-card" v-if="devResult.analysis">
              <h4>📋 需求分析</h4>
              <pre>{{ devResult.analysis }}</pre>
            </div>

            <div class="result-card" v-if="devResult.design">
              <h4>🏗️ 方案设计</h4>
              <pre>{{ devResult.design }}</pre>
            </div>

            <div class="result-card" v-if="devResult.code">
              <h4>💻 代码生成</h4>
              <pre><code>{{ devResult.code }}</code></pre>
            </div>

            <div class="result-card" v-if="devResult.review">
              <h4>✅ 代码审查 (评分: {{ devResult.quality_score }})</h4>
              <pre>{{ devResult.review }}</pre>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- PPT生成工作流 -->
      <el-tab-pane label="PPT生成" name="ppt">
        <div class="tab-content">
          <el-form :model="pptForm" label-width="100px">
            <el-form-item label="PPT主题">
              <el-input v-model="pptForm.theme" placeholder="例如：人工智能发展趋势" />
            </el-form-item>
            <el-form-item label="内容描述">
              <el-input 
                v-model="pptForm.content" 
                type="textarea"
                :rows="8"
                placeholder="描述PPT的主要内容，AI会自动生成大纲和幻灯片"
              />
            </el-form-item>
          </el-form>

          <el-button 
            type="primary" 
            @click="generatePpt"
            :loading="pptLoading"
            size="large"
          >
            <el-icon><Document /></el-icon> 生成PPT
          </el-button>

          <el-alert 
            v-if="pptSuccess" 
            type="success" 
            title="PPT生成成功！文件已自动下载"
            :closable="false"
            style="margin-top: 16px;"
          />
        </div>
      </el-tab-pane>

      <!-- 文本分析工作流 -->
      <el-tab-pane label="文本分析" name="analysis">
        <div class="tab-content">
          <el-input
            v-model="analysisText"
            type="textarea"
            :rows="8"
            placeholder="输入要分析的文本，AI将提取情感、关键词、主题等结构化信息"
          />
          <el-button 
            type="primary" 
            @click="analyzeText"
            :loading="analysisLoading"
            size="large"
          >
            <el-icon><DataAnalysis /></el-icon> 开始分析
          </el-button>

          <div v-if="analysisResult" class="result-section">
            <h3>分析结果</h3>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="情感倾向">
                <el-tag :type="getSentimentType(analysisResult.sentiment)">
                  {{ analysisResult.sentiment }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="情感得分">
                {{ analysisResult.sentimentScore }}
              </el-descriptions-item>
              <el-descriptions-item label="置信度">
                <el-progress :percentage="analysisResult.confidence" />
              </el-descriptions-item>
            </el-descriptions>

            <div class="result-card">
              <h4>📝 内容摘要</h4>
              <p>{{ analysisResult.summary }}</p>
            </div>

            <div class="result-card">
              <h4>🏷️ 关键词</h4>
              <el-tag 
                v-for="keyword in analysisResult.keywords" 
                :key="keyword"
                style="margin-right: 8px;"
              >
                {{ keyword }}
              </el-tag>
            </div>

            <div class="result-card">
              <h4>🎯 主要主题</h4>
              <ul>
                <li v-for="topic in analysisResult.topics" :key="topic">{{ topic }}</li>
              </ul>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api/agent'

const activeTab = ref('dev')

// 开发工作流
const devRequirement = ref('')
const devLoading = ref(false)
const devResult = ref(null)
const devStepActive = ref(0)

const executeDevWorkflow = async () => {
  if (!devRequirement.value.trim()) return

  devLoading.value = true
  devResult.value = null
  devStepActive.value = 0

  try {
    const result = await api.executeDevWorkflow(devRequirement.value)
    if (result.success) {
      devResult.value = result.data
      devStepActive.value = 4
      ElMessage.success('工作流执行成功')
    }
  } catch (error) {
    ElMessage.error('工作流执行失败')
  } finally {
    devLoading.value = false
  }
}

// PPT生成
const pptForm = ref({
  theme: '',
  content: ''
})
const pptLoading = ref(false)
const pptSuccess = ref(false)

const generatePpt = async () => {
  if (!pptForm.value.theme || !pptForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  pptLoading.value = true
  pptSuccess.value = false

  try {
    const blob = await api.generatePpt(pptForm.value)
    
    // 下载文件
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${pptForm.value.theme}.pptx`
    link.click()
    window.URL.revokeObjectURL(url)

    pptSuccess.value = true
    ElMessage.success('PPT生成成功')
  } catch (error) {
    ElMessage.error('PPT生成失败')
  } finally {
    pptLoading.value = false
  }
}

// 文本分析
const analysisText = ref('')
const analysisLoading = ref(false)
const analysisResult = ref(null)

const analyzeText = async () => {
  if (!analysisText.value.trim()) return

  analysisLoading.value = true
  analysisResult.value = null

  try {
    const result = await api.demoStructuredAnalysis(analysisText.value)
    if (result.success) {
      analysisResult.value = result.data
      ElMessage.success('分析完成')
    }
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analysisLoading.value = false
  }
}

const getSentimentType = (sentiment) => {
  if (sentiment === 'positive') return 'success'
  if (sentiment === 'negative') return 'danger'
  return 'info'
}
</script>

<style scoped>
.page-container {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  color: #fff;
  font-size: 32px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.workflow-tabs {
  background: rgba(138, 43, 226, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.2);
  border-radius: 12px;
  padding: 24px;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0;
}

.result-section {
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.result-section h3 {
  color: #fff;
  margin-bottom: 24px;
  font-size: 20px;
}

.workflow-steps {
  margin-bottom: 32px;
}

.result-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(138, 43, 226, 0.2);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.result-card h4 {
  color: #ba55d3;
  margin: 0 0 12px 0;
  font-size: 16px;
}

.result-card pre {
  background: rgba(0, 0, 0, 0.3);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  color: #fff;
  margin: 0;
}

.result-card ul {
  margin: 0;
  padding-left: 20px;
  color: rgba(255, 255, 255, 0.8);
}
</style>
