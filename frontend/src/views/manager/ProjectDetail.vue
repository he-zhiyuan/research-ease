<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  approveAcceptance,
  approveProject,
  getProject,
  listApprovalRecords,
  rejectAcceptance,
  rejectProject,
} from '@/api/project'
import { listProgressReports } from '@/api/progress'
import { listBudgetItemsWithBalance, listExpendituresForProject } from '@/api/expenditure'
import { listAchievements } from '@/api/achievement'
import { usePendingCountsStore } from '@/store/pendingCounts'
import { useNotificationStore } from '@/store/notifications'
import type {
  AchievementResponse,
  ApprovalRecordResponse,
  BudgetApprovalItem,
  BudgetItemBalanceResponse,
  ExpenditureResponse,
  ProgressReportResponse,
  ProjectResponse,
} from '@/types'
import {
  ACHIEVEMENT_TYPE_LABEL,
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
const pendingCounts = usePendingCountsStore()
const notificationStore = useNotificationStore()
const projectId = Number(route.params.id)

const project = ref<ProjectResponse>()
const approvalRecords = ref<ApprovalRecordResponse[]>([])
const progressReports = ref<ProgressReportResponse[]>([])
const budgetItems = ref<BudgetItemBalanceResponse[]>([])
const expenditures = ref<ExpenditureResponse[]>([])
const achievements = ref<AchievementResponse[]>([])

const isSubmitted = computed(() => project.value?.status === 'SUBMITTED')
const isAccepting = computed(() => project.value?.status === 'ACCEPTING')

const approvalAmounts = reactive<Record<number, number>>({})
const approving = ref(false)

const rejectDialog = reactive({ visible: false, comment: '', target: '' as 'project' | 'acceptance' })

async function loadAll() {
  project.value = await getProject(projectId)
  approvalRecords.value = await listApprovalRecords(projectId)
  progressReports.value = await listProgressReports(projectId)
  budgetItems.value = await listBudgetItemsWithBalance(projectId)
  expenditures.value = await listExpendituresForProject(projectId)
  if (project.value.status === 'ACCEPTING' || project.value.status === 'COMPLETED') {
    achievements.value = await listAchievements(projectId)
  }
  project.value.budgetItems.forEach((item) => {
    approvalAmounts[item.id] = item.approvedAmount ?? item.requestedAmount
  })
}

async function handleApproveProject() {
  approving.value = true
  try {
    const items: BudgetApprovalItem[] = Object.entries(approvalAmounts).map(([budgetItemId, approvedAmount]) => ({
      budgetItemId: Number(budgetItemId),
      approvedAmount,
    }))
    await approveProject(projectId, { budgetItems: items })
    ElMessage.success('审批通过，项目进入执行阶段')
    await loadAll()
    pendingCounts.refresh()
    notificationStore.refresh()
  } finally {
    approving.value = false
  }
}

function openReject(target: 'project' | 'acceptance') {
  rejectDialog.target = target
  rejectDialog.comment = ''
  rejectDialog.visible = true
}

async function confirmReject() {
  if (!rejectDialog.comment) {
    ElMessage.warning('请填写驳回意见')
    return
  }
  if (rejectDialog.target === 'project') {
    await rejectProject(projectId, { comment: rejectDialog.comment })
    ElMessage.success('已驳回项目申报')
  } else {
    await rejectAcceptance(projectId, { comment: rejectDialog.comment })
    ElMessage.success('已驳回结题申请，项目回到执行阶段')
  }
  rejectDialog.visible = false
  await loadAll()
  pendingCounts.refresh()
  notificationStore.refresh()
}

async function handleApproveAcceptance() {
  await approveAcceptance(projectId)
  ElMessage.success('验收通过，项目已结题')
  await loadAll()
  pendingCounts.refresh()
  notificationStore.refresh()
}

onMounted(loadAll)
</script>

<template>
  <div v-if="project" class="project-detail">
    <div class="header">
      <h2>{{ project.projectName }}</h2>
      <el-tag :type="PROJECT_STATUS_TAG[project.status]">{{ PROJECT_STATUS_LABEL[project.status] }}</el-tag>
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
          <el-table-column label="核定金额" width="200">
            <template #default="{ row }">
              <el-input-number
                v-if="isSubmitted"
                v-model="approvalAmounts[row.id]"
                :min="0"
                :precision="2"
                size="small"
              />
              <span v-else>{{ row.approvedAmount ?? '-' }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="isSubmitted" class="actions">
          <el-button type="primary" :loading="approving" @click="handleApproveProject">审批通过</el-button>
          <el-button type="danger" @click="openReject('project')">驳回</el-button>
        </div>
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
        <p class="hint">经费审核请前往"经费审核"页面处理。</p>
      </el-tab-pane>

      <el-tab-pane label="结题">
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
        <div v-if="isAccepting" class="actions">
          <el-button type="primary" @click="handleApproveAcceptance">验收通过</el-button>
          <el-button type="danger" @click="openReject('acceptance')">验收不通过</el-button>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="rejectDialog.visible" title="驳回意见" width="420px">
      <el-input v-model="rejectDialog.comment" type="textarea" :rows="3" placeholder="请填写驳回理由" />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
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

.actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.hint {
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
}

:deep(.nowrap-cell .cell) {
  white-space: nowrap;
}
</style>
