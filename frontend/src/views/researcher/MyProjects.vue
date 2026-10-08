<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMyProjects, submitProject } from '@/api/project'
import type { ProjectResponse } from '@/types'
import { PROJECT_STATUS_LABEL, PROJECT_STATUS_TAG } from '@/utils/dict'

const router = useRouter()
const projects = ref<ProjectResponse[]>([])
const loading = ref(false)

async function loadProjects() {
  loading.value = true
  try {
    projects.value = await listMyProjects()
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/researcher/projects/new')
}

function goDetail(id: number) {
  router.push(`/researcher/projects/${id}`)
}

function goEdit(id: number) {
  router.push(`/researcher/projects/${id}/edit`)
}

async function handleSubmit(project: ProjectResponse) {
  await ElMessageBox.confirm(`确认提交项目「${project.projectName}」申报吗？`, '提交确认', {
    type: 'warning',
  })
  await submitProject(project.id)
  ElMessage.success('提交成功，等待管理人员审批')
  await loadProjects()
}

onMounted(loadProjects)
</script>

<template>
  <div class="my-projects">
    <div class="toolbar">
      <h2>我的项目</h2>
      <el-button type="primary" @click="goCreate">新建项目申报</el-button>
    </div>
    <el-table v-loading="loading" :data="projects" @row-click="(row: ProjectResponse) => goDetail(row.id)">
      <el-table-column prop="projectName" label="项目名称" min-width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="PROJECT_STATUS_TAG[row.status]">{{ PROJECT_STATUS_LABEL[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="起始日期" width="120" />
      <el-table-column prop="endDate" label="截止日期" width="120" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="goDetail(row.id)">详情</el-button>
          <el-button
            v-if="row.status === 'DRAFT' || row.status === 'REJECTED'"
            link
            type="primary"
            @click.stop="goEdit(row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 'DRAFT' || row.status === 'REJECTED'"
            link
            type="success"
            @click.stop="handleSubmit(row)"
          >
            提交
          </el-button>
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

:deep(.el-table__row) {
  cursor: pointer;
}
</style>
