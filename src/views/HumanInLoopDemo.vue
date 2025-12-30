<template>
  <div class="page-container">
    <div class="page-header">
      <h1><el-icon><UserFilled /></el-icon> Human-in-the-Loop</h1>
      <p>人工介入 - AI方案需要人类审批后执行</p>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：创建工作流 -->
      <el-col :span="12">
        <el-card class="demo-card">
          <template #header>
            <div class="card-header">
              <span>📝 创建待审批工作流</span>
            </div>
          </template>

          <el-input
            v-model="task"
            type="textarea"
            :rows="6"
            placeholder="输入需要AI制定方案的任务..."
          />

          <el-button 
            type="primary" 
            @click="createWorkflow"
            :loading="createLoading"
            style="margin-top: 16px;"
          >
            <el-icon><Document /></el-icon> 生成方案并提交审批
          </el-button>

          <div v-if="createdWorkflow" class="created-section">
            <el-alert type="success" :closable="false">
              <h4>✅ 方案已生成，等待审批</h4>
              <div><strong>工作流ID:</strong> {{ createdWorkflow.workflowId }}</div>
            </el-alert>

            <el-card class="proposal-card">
              <h5>📋 AI生成的方案</h5>
              <pre>{{ createdWorkflow.proposal }}</pre>
            </el-card>

            <div class="approval-actions">
              <el-input
                v-model="feedback"
                type="textarea"
                :rows="2"
                placeholder="审批意见..."
              />
              <div style="display: flex; gap: 12px; margin-top: 12px;">
                <el-button 
                  type="success" 
                  @click="approveWorkflow(createdWorkflow.workflowId, true)"
                  :loading="approvalLoading"
                >
                  <el-icon><Check /></el-icon> 批准执行
                </el-button>
                <el-button 
                  type="danger" 
                  @click="approveWorkflow(createdWorkflow.workflowId, false)"
                  :loading="approvalLoading"
                >
                  <el-icon><Close /></el-icon> 拒绝
                </el-button>
              </div>
            </div>

            <div v-if="approvalResult" class="approval-result">
              <el-alert 
                :type="approvalResult.status === 'completed' ? 'success' : 'warning'"
                :closable="false"
              >
                {{ approvalResult.message }}
              </el-alert>

              <el-card v-if="approvalResult.executionResult" class="execution-card">
                <h5>🎯 执行结果</h5>
                <pre>{{ approvalResult.executionResult }}</pre>
              </el-card>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：待审批列表 -->
      <el-col :span="12">
        <el-card class="demo-card">
          <template #header>
            <div class="card-header">
              <span>📋 待审批工作流</span>
              <el-button @click="loadPending" text size="small">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
            </div>
          </template>

          <div v-if="pendingWorkflows.length === 0" class="empty-state">
            暂无待审批工作流
          </div>

          <el-card 
            v-for="workflow in pendingWorkflows" 
            :key="workflow.workflowId"
            class="pending-card"
          >
            <div class="pending-header">
              <span class="workflow-id">{{ workflow.workflowId.substring(0, 8) }}...</span>
              <span class="workflow-time">{{ formatTime(workflow.createdAt) }}</span>
            </div>

            <div class="workflow-task">
              <strong>任务:</strong> {{ workflow.task }}
            </div>

            <el-button 
              size="small" 
              @click="viewDetail(workflow.workflowId)"
              text
            >
              查看详情
            </el-button>
          </el-card>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="工作流详情" width="70%">
      <div v-if="workflowDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工作流ID">
            {{ workflowDetail.workflowId }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(workflowDetail.status)">
              {{ workflowDetail.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ workflowDetail.createdAt }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>任务</h4>
          <p>{{ workflowDetail.task }}</p>
        </div>

        <div class="detail-section">
          <h4>AI方案</h4>
          <pre>{{ workflowDetail.proposal }}</pre>
        </div>

        <div v-if="workflowDetail.feedback" class="detail-section">
          <h4>审批意见</h4>
          <p>{{ workflowDetail.feedback }}</p>
        </div>

        <div v-if="workflowDetail.executionResult" class="detail-section">
          <h4>执行结果</h4>
          <pre>{{ workflowDetail.executionResult }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

const task = ref('')
const createLoading = ref(false)
const createdWorkflow = ref(null)

const feedback = ref('')
const approvalLoading = ref(false)
const approvalResult = ref(null)

const pendingWorkflows = ref([])
const detailVisible = ref(false)
const workflowDetail = ref(null)

onMounted(() => {
  loadPending()
})

const createWorkflow = async () => {
  if (!task.value) {
    ElMessage.warning('请输入任务')
    return
  }

  createLoading.value = true
  createdWorkflow.value = null
  approvalResult.value = null

  try {
    const res = await api.post('/human-in-loop/create', { task: task.value })
    createdWorkflow.value = res.data
    ElMessage.success('方案已生成')
    loadPending()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    createLoading.value = false
  }
}

const approveWorkflow = async (workflowId, approved) => {
  approvalLoading.value = true
  approvalResult.value = null

  try {
    const res = await api.post(`/human-in-loop/approve/${workflowId}`, {
      approved,
      feedback: feedback.value
    })
    approvalResult.value = res.data
    ElMessage.success(approved ? '已批准' : '已拒绝')
    loadPending()
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    approvalLoading.value = false
  }
}

const loadPending = async () => {
  try {
    const res = await api.get('/human-in-loop/pending')
    if (res.data.success) {
      pendingWorkflows.value = res.data.data
    }
  } catch (error) {
    console.error('加载待审批列表失败', error)
  }
}

const viewDetail = async (workflowId) => {
  try {
    const res = await api.get(`/human-in-loop/detail/${workflowId}`)
    workflowDetail.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

const formatTime = (time) => {
  return new Date(time).toLocaleString()
}

const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    completed: 'success',
    failed: 'danger'
  }
  return types[status] || 'info'
}
</script>

<style scoped>
.page-container {
  padding: 40px;
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
  margin-bottom: 24px;
}

.demo-card {
  background: rgba(138, 43, 226, 0.05);
  border: 1px solid rgba(138, 43, 226, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.created-section {
  margin-top: 24px;
}

.proposal-card {
  background: rgba(0, 0, 0, 0.2);
  margin: 16px 0;
}

.proposal-card h5 {
  color: #ba55d3;
  margin-bottom: 12px;
}

.proposal-card pre {
  white-space: pre-wrap;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.approval-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(138, 43, 226, 0.2);
}

.approval-result {
  margin-top: 16px;
}

.execution-card {
  margin-top: 16px;
  background: rgba(0, 0, 0, 0.2);
}

.execution-card h5 {
  color: #ba55d3;
  margin-bottom: 12px;
}

.execution-card pre {
  white-space: pre-wrap;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  padding: 32px;
}

.pending-card {
  background: rgba(138, 43, 226, 0.05);
  margin-bottom: 12px;
}

.pending-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.workflow-id {
  color: #ba55d3;
  font-weight: bold;
}

.workflow-time {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.workflow-task {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 12px;
}

.detail-section {
  margin: 24px 0;
}

.detail-section h4 {
  color: #ba55d3;
  margin-bottom: 12px;
}

.detail-section pre {
  background: rgba(0, 0, 0, 0.2);
  padding: 12px;
  border-radius: 6px;
  white-space: pre-wrap;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
}
</style>



