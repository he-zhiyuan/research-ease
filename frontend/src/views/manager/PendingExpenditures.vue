<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { approveExpenditure, listExpendituresByStatus, rejectExpenditure } from '@/api/expenditure'
import type { ExpenditureResponse } from '@/types'
import { BUDGET_CATEGORY_LABEL, EXPENDITURE_STATUS_LABEL, EXPENDITURE_STATUS_TAG } from '@/utils/dict'
import { formatDateTime } from '@/utils/format'
import { usePendingCountsStore } from '@/store/pendingCounts'

const router = useRouter()
const pendingCounts = usePendingCountsStore()
const expenditures = ref<ExpenditureResponse[]>([])
const loading = ref(false)
const statusFilter = ref('PENDING')

const statusOptions = Object.entries(EXPENDITURE_STATUS_LABEL).map(([value, label]) => ({ value, label }))

const rejectDialog = reactive({ visible: false, comment: '', targetId: 0 })

async function loadExpenditures() {
  loading.value = true
  try {
    expenditures.value = await listExpendituresByStatus(statusFilter.value || undefined)
  } finally {
    loading.value = false
  }
}

async function handleApprove(row: ExpenditureResponse) {
  await approveExpenditure(row.id)
  ElMessage.success('已批准该笔经费支出')
  await loadExpenditures()
  pendingCounts.refresh()
}

function openReject(row: ExpenditureResponse) {
  rejectDialog.targetId = row.id
  rejectDialog.comment = ''
  rejectDialog.visible = true
}

async function confirmReject() {
  if (!rejectDialog.comment) {
    ElMessage.warning('请填写驳回理由')
    return
  }
  await rejectExpenditure(rejectDialog.targetId, { comment: rejectDialog.comment })
  ElMessage.success('已驳回该笔经费支出')
  rejectDialog.visible = false
  await loadExpenditures()
  pendingCounts.refresh()
}

function goProject(projectId: number) {
  router.push(`/manager/projects/${projectId}`)
}

onMounted(loadExpenditures)
</script>

<template>
  <div class="pending-expenditures">
    <div class="toolbar">
      <h2>经费审核</h2>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 160px" @change="loadExpenditures">
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
    </div>
    <el-table v-loading="loading" :data="expenditures">
      <el-table-column label="所属项目" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="goProject(row.projectId)">项目#{{ row.projectId }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="科目">
        <template #default="{ row }">{{ BUDGET_CATEGORY_LABEL[row.budgetItemCategory] }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="120" />
      <el-table-column prop="description" label="说明" />
      <el-table-column prop="creatorName" label="申请人" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="EXPENDITURE_STATUS_TAG[row.status]">{{ EXPENDITURE_STATUS_LABEL[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" width="200">
        <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button link type="primary" @click="handleApprove(row as ExpenditureResponse)">批准</el-button>
            <el-button link type="danger" @click="openReject(row as ExpenditureResponse)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="rejectDialog.visible" title="驳回理由" width="420px">
      <el-input v-model="rejectDialog.comment" type="textarea" :rows="3" placeholder="请填写驳回理由" />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
</style>
