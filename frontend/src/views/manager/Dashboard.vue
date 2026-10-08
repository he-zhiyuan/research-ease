<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardOverview } from '@/api/dashboard'
import { listOverdueProjects } from '@/api/progress'
import type { DashboardOverviewResponse, OverdueProjectResponse } from '@/types'
import { PROJECT_STATUS_LABEL } from '@/utils/dict'

const router = useRouter()
const overview = ref<DashboardOverviewResponse>()
const overdueProjects = ref<OverdueProjectResponse[]>([])
const loading = ref(false)

async function loadDashboard() {
  loading.value = true
  try {
    overview.value = await getDashboardOverview()
    overdueProjects.value = await listOverdueProjects()
  } finally {
    loading.value = false
  }
}

function goProject(projectId: number) {
  router.push(`/manager/projects/${projectId}`)
}

onMounted(loadDashboard)
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <h2>数据看板</h2>
    <div v-if="overview" class="stat-cards">
      <el-card class="stat-card">
        <div class="stat-label">项目总数</div>
        <div class="stat-value">{{ overview.totalProjects }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">申请总额</div>
        <div class="stat-value">{{ overview.totalRequestedBudget }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">核定总额</div>
        <div class="stat-value">{{ overview.totalApprovedBudget }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">已花费</div>
        <div class="stat-value">{{ overview.totalSpent }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">审批中占用</div>
        <div class="stat-value">{{ overview.totalPending }}</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-label">结余</div>
        <div class="stat-value">{{ overview.totalBalance }}</div>
      </el-card>
    </div>

    <h3>项目状态分布</h3>
    <el-table v-if="overview" :data="Object.entries(overview.statusCounts).map(([status, count]) => ({ status, count }))">
      <el-table-column label="状态">
        <template #default="{ row }">{{ PROJECT_STATUS_LABEL[row.status] ?? row.status }}</template>
      </el-table-column>
      <el-table-column prop="count" label="数量" />
    </el-table>

    <h3>进度预警（超期未报）</h3>
    <el-table :data="overdueProjects">
      <el-table-column label="项目名称">
        <template #default="{ row }">
          <el-button link type="primary" @click="goProject(row.projectId)">{{ row.projectName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="leaderName" label="负责人" width="120" />
      <el-table-column prop="lastReportDate" label="最近进度日期" width="140" />
      <el-table-column prop="daysSinceLastReport" label="超期天数" width="120" />
    </el-table>
  </div>
</template>

<style scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin: 16px 0 24px;
}

.stat-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

h3 {
  margin: 20px 0 8px;
}
</style>
