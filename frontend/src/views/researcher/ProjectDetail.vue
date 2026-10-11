<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProject, listApprovalRecords, submitAcceptance, submitProject } from '@/api/project'
import { listProgressReports, submitProgressReport } from '@/api/progress'
import { listBudgetItemsWithBalance, listExpendituresForProject, submitExpenditure } from '@/api/expenditure'
import { listAchievements } from '@/api/achievement'
import type {
  AchievementRequest,
  AchievementResponse,
  ApprovalRecordResponse,
  BudgetItemBalanceResponse,
  ExpenditureResponse,
  ProgressReportResponse,
  ProjectResponse,
} from '@/types'
import {
  ACHIEVEMENT_TYPE_LABEL,
  ACHIEVEMENT_TYPE_OPTIONS,
  APPROVAL_TYPE_LABEL,
  BUDGET_CATEGORY_LABEL,
  EXPENDITURE_STATUS_LABEL,
  EXPENDITURE_STATUS_TAG,
  PROJECT_STATUS_LABEL,
  PROJECT_STATUS_TAG,
} from '@/utils/dict'
import { formatDate, formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)

const project = ref<ProjectResponse>()
const approvalRecords = ref<ApprovalRecordResponse[]>([])
const progressReports = ref<ProgressReportResponse[]>([])
const budgetItems = ref<BudgetItemBalanceResponse[]>([])
const expenditures = ref<ExpenditureResponse[]>([])
const achievements = ref<AchievementResponse[]>([])

const isExecuting = computed(() => project.value?.status === 'EXECUTING')
const isAccepting = computed(() => project.value?.status === 'ACCEPTING')
const isCompleted = computed(() => project.value?.status === 'COMPLETED')

const progressForm = reactive({ content: '', reportDate: '' })
const progressSubmitting = ref(false)

const expenditureForm = reactive({ budgetItemId: undefined as number | undefined, amount: 0, description: '' })
const expenditureSubmitting = ref(false)

const acceptanceAchievements = ref<AchievementRequest[]>([{ type: 'PAPER', title: '', detail: '', achievedDate: '' }])
const acceptanceSubmitting = ref(false)

const projectSubmitting = ref(false)

async function handleSubmitProject() {
  if (!project.value) return
  await ElMessageBox.confirm(`确认提交项目「${project.value.projectName}」申报吗？`, '提交确认', {
    type: 'warning',
  })
  projectSubmitting.value = true
  try {
    await submitProject(projectId)
    ElMessage.success('提交成功，等待管理人员审批')
    await loadAll()
  } finally {
    projectSubmitting.value = false
  }
}

async function loadAll() {
  project.value = await getProject(projectId)
  approvalRecords.value = await listApprovalRecords(projectId)
  progressReports.value = await listProgressReports(projectId)
  budgetItems.value = await listBudgetItemsWithBalance(projectId)
  expenditures.value = await listExpendituresForProject(projectId)
  if (project.value.status === 'ACCEPTING' || project.value.status === 'COMPLETED') {
    achievements.value = await listAchievements(projectId)
  }
}

async function handleSubmitProgress() {
  if (!progressForm.content || !progressForm.reportDate) {
    ElMessage.warning('请填写进度内容与日期')
    return
  }
  progressSubmitting.value = true
  try {
    await submitProgressReport(projectId, { ...progressForm })
    ElMessage.success('进度已提交')
    progressForm.content = ''
    progressForm.reportDate = ''
    progressReports.value = await listProgressReports(projectId)
  } finally {
    progressSubmitting.value = false
  }
}

async function handleSubmitExpenditure() {
  if (!expenditureForm.budgetItemId || expenditureForm.amount <= 0) {
    ElMessage.warning('请选择预算科目并填写有效金额')
    return
  }
  expenditureSubmitting.value = true
  try {
    await submitExpenditure(projectId, {
      budgetItemId: expenditureForm.budgetItemId,
      amount: expenditureForm.amount,
      description: expenditureForm.description,
    })
    ElMessage.success('经费支出申请已提交')
    expenditureForm.budgetItemId = undefined
    expenditureForm.amount = 0
    expenditureForm.description = ''
    expenditures.value = await listExpendituresForProject(projectId)
    budgetItems.value = await listBudgetItemsWithBalance(projectId)
  } finally {
    expenditureSubmitting.value = false
  }
}

function addAcceptanceAchievement() {
  acceptanceAchievements.value.push({ type: 'OTHER', title: '', detail: '', achievedDate: '' })
}

function removeAcceptanceAchievement(index: number) {
  acceptanceAchievements.value.splice(index, 1)
}

async function handleSubmitAcceptance() {
  if (acceptanceAchievements.value.some((item) => !item.title)) {
    ElMessage.warning('请完整填写成果标题')
    return
  }
  acceptanceSubmitting.value = true
  try {
    await submitAcceptance(projectId, { achievements: acceptanceAchievements.value })
    ElMessage.success('结题申请已提交，等待管理人员验收')
    await loadAll()
  } finally {
    acceptanceSubmitting.value = false
  }
}

onMounted(loadAll)
</script>

<template>
  <div v-if="project" class="project-detail">
    <div class="header">
      <h2>{{ project.projectName }}</h2>
      <el-tag :type="PROJECT_STATUS_TAG[project.status]">{{ PROJECT_STATUS_LABEL[project.status] }}</el-tag>
      <el-button
        v-if="project.status === 'DRAFT' || project.status === 'REJECTED'"
        class="edit-btn"
        type="primary"
        @click="router.push(`/researcher/projects/${projectId}/edit`)"
      >
        编辑
      </el-button>
      <el-button
        v-if="project.status === 'DRAFT' || project.status === 'REJECTED'"
        type="success"
        :loading="projectSubmitting"
        @click="handleSubmitProject"
      >
        提交
      </el-button>
      <el-button class="back-btn" @click="router.back()">返回</el-button>
    </div>

    <el-tabs>
      <el-tab-pane label="基本信息">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="负责人">{{ project.leaderName }}</el-descriptions-item>
          <el-descriptions-item label="团队成员">{{ project.teamMembers || '-' }}</el-descriptions-item>
          <el-descriptions-item label="经费来源">{{ project.fundingSource || '-' }}</el-descriptions-item>
          <el-descriptions-item label="起止日期">{{ formatDate(project.startDate) }} ~ {{ formatDate(project.endDate) }}</el-descriptions-item>
          <el-descriptions-item label="研究内容" :span="2">{{ project.researchContent || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4>预算科目</h4>
        <el-table :data="project.budgetItems">
          <el-table-column label="科目类别">
            <template #default="{ row }">{{ BUDGET_CATEGORY_LABEL[row.category] }}</template>
          </el-table-column>
          <el-table-column prop="requestedAmount" label="申请金额" />
          <el-table-column label="核定金额">
            <template #default="{ row }">{{ row.approvedAmount ?? '-' }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="审批历史">
        <el-table :data="approvalRecords">
          <el-table-column label="类型">
            <template #default="{ row }">{{ APPROVAL_TYPE_LABEL[row.type] }}</template>
          </el-table-column>
          <el-table-column label="结果">
            <template #default="{ row }">
              <el-tag :type="row.result === 'APPROVED' ? 'success' : 'danger'">
                {{ row.result === 'APPROVED' ? '通过' : '驳回' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="意见" />
          <el-table-column prop="operatorName" label="操作人" width="120" />
          <el-table-column label="时间" width="200">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="进度">
        <el-form v-if="isExecuting" inline class="inline-form">
          <el-form-item label="进度内容">
            <el-input v-model="progressForm.content" type="textarea" :rows="2" style="width: 320px" />
          </el-form-item>
          <el-form-item label="报告日期">
            <el-date-picker v-model="progressForm.reportDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="progressSubmitting" @click="handleSubmitProgress">提交进度</el-button>
          </el-form-item>
        </el-form>
        <el-table :data="progressReports">
          <el-table-column label="日期" width="150" class-name="nowrap-cell">
            <template #default="{ row }">{{ formatDate(row.reportDate) }}</template>
          </el-table-column>
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="creatorName" label="提交人" width="120" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="经费">
        <h4>预算余额</h4>
        <el-table :data="budgetItems">
          <el-table-column label="科目类别">
            <template #default="{ row }">{{ BUDGET_CATEGORY_LABEL[row.category] }}</template>
          </el-table-column>
          <el-table-column prop="approvedAmount" label="核定金额" />
          <el-table-column prop="balance" label="可用余额" />
        </el-table>

        <el-form v-if="isExecuting" inline class="inline-form">
          <el-form-item label="预算科目">
            <el-select v-model="expenditureForm.budgetItemId" placeholder="选择科目" style="width: 220px">
              <el-option
                v-for="item in budgetItems"
                :key="item.id"
                :label="`${BUDGET_CATEGORY_LABEL[item.category]}（余额 ${item.balance}）`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="金额">
            <el-input-number v-model="expenditureForm.amount" :min="0" :precision="2" />
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="expenditureForm.description" style="width: 200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="expenditureSubmitting" @click="handleSubmitExpenditure">
              提交经费支出申请
            </el-button>
          </el-form-item>
        </el-form>

        <h4>支出记录</h4>
        <el-table :data="expenditures">
          <el-table-column label="科目">
            <template #default="{ row }">{{ BUDGET_CATEGORY_LABEL[row.budgetItemCategory] }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120" />
          <el-table-column prop="description" label="说明" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="EXPENDITURE_STATUS_TAG[row.status]">{{ EXPENDITURE_STATUS_LABEL[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交时间" width="200">
            <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="结题">
        <div v-if="isExecuting">
          <p class="hint">请填写本次结题的研究成果清单：</p>
          <div v-for="(item, index) in acceptanceAchievements" :key="index" class="achievement-row">
            <el-select v-model="item.type" placeholder="成果类型" style="width: 160px">
              <el-option v-for="opt in ACHIEVEMENT_TYPE_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-input v-model="item.title" placeholder="成果标题" style="width: 220px" />
            <el-input v-model="item.detail" placeholder="详情" style="width: 220px" />
            <el-date-picker v-model="item.achievedDate" type="date" value-format="YYYY-MM-DD" placeholder="取得日期" />
            <el-button link type="danger" @click="removeAcceptanceAchievement(index)">删除</el-button>
          </div>
          <el-button type="primary" link @click="addAcceptanceAchievement">+ 添加成果</el-button>
          <div class="submit-acceptance">
            <el-button type="primary" :loading="acceptanceSubmitting" @click="handleSubmitAcceptance">
              提交结题申请
            </el-button>
          </div>
        </div>
        <el-alert v-else-if="isAccepting" title="结题申请已提交，等待管理人员验收" type="warning" :closable="false" />
        <div v-if="isAccepting || isCompleted">
          <h4>已提交成果</h4>
          <el-table :data="achievements">
            <el-table-column label="类型" width="140">
              <template #default="{ row }">{{ ACHIEVEMENT_TYPE_LABEL[row.type] }}</template>
            </el-table-column>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="detail" label="详情" />
            <el-table-column label="取得日期" width="150" class-name="nowrap-cell">
              <template #default="{ row }">{{ formatDate(row.achievedDate) }}</template>
            </el-table-column>
          </el-table>
        </div>
        <el-alert v-if="!isExecuting && !isAccepting && !isCompleted" title="项目尚未进入执行阶段" type="info" :closable="false" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.back-btn {
  margin-left: auto;
}

h4 {
  margin: 16px 0 8px;
}

.inline-form {
  margin-bottom: 16px;
}

.hint {
  color: #606266;
  margin-bottom: 12px;
}

.achievement-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.submit-acceptance {
  margin-top: 12px;
}

:deep(.nowrap-cell .cell) {
  white-space: nowrap;
}
</style>
