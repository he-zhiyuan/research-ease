<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listProjectsForManager } from '@/api/project'
import type { ProjectResponse } from '@/types'
import { PROJECT_STATUS_LABEL, PROJECT_STATUS_TAG } from '@/utils/dict'

const router = useRouter()
const projects = ref<ProjectResponse[]>([])
const loading = ref(false)

const filters = reactive({ status: '', keyword: '' })

const statusOptions = Object.entries(PROJECT_STATUS_LABEL).map(([value, label]) => ({ value, label }))

async function loadProjects() {
  loading.value = true
  try {
    projects.value = await listProjectsForManager({
      status: filters.status || undefined,
      keyword: filters.keyword || undefined,
    })
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/manager/projects/${id}`)
}

onMounted(loadProjects)
</script>

<template>
  <div class="pending-projects">
    <div class="toolbar">
      <h2>项目审批</h2>
      <div class="filters">
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 160px" @change="loadProjects">
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          placeholder="搜索项目名称"
          style="width: 200px"
          clearable
          @keyup.enter="loadProjects"
          @clear="loadProjects"
        />
        <el-button type="primary" @click="loadProjects">查询</el-button>
      </div>
    </div>
    <el-table v-loading="loading" :data="projects" @row-click="(row: ProjectResponse) => goDetail(row.id)">
      <el-table-column prop="projectName" label="项目名称" min-width="200" />
      <el-table-column prop="leaderName" label="负责人" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="PROJECT_STATUS_TAG[row.status]">{{ PROJECT_STATUS_LABEL[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="起始日期" width="120" />
      <el-table-column prop="endDate" label="截止日期" width="120" />
      <el-table-column prop="submittedAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="goDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.filters {
  display: flex;
  gap: 12px;
}

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
