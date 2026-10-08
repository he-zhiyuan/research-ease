<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createProject, getProject, updateProject } from '@/api/project'
import type { BudgetCategory, ProjectCreateRequest } from '@/types'
import { BUDGET_CATEGORY_OPTIONS } from '@/utils/dict'

const route = useRoute()
const router = useRouter()

const projectId = computed(() => (route.params.id ? Number(route.params.id) : null))
const isEdit = computed(() => projectId.value !== null)

const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = reactive<ProjectCreateRequest>({
  projectName: '',
  researchContent: '',
  teamMembers: '',
  fundingSource: '',
  startDate: '',
  endDate: '',
  budgetItems: [{ category: 'EQUIPMENT' as BudgetCategory, requestedAmount: 0 }],
})

const rules: FormRules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择起始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
}

function addBudgetItem() {
  form.budgetItems.push({ category: 'OTHER', requestedAmount: 0 })
}

function removeBudgetItem(index: number) {
  form.budgetItems.splice(index, 1)
}

async function loadForEdit() {
  if (!projectId.value) return
  const project = await getProject(projectId.value)
  form.projectName = project.projectName
  form.researchContent = project.researchContent ?? ''
  form.teamMembers = project.teamMembers ?? ''
  form.fundingSource = project.fundingSource ?? ''
  form.startDate = project.startDate
  form.endDate = project.endDate
  form.budgetItems = project.budgetItems.map((item) => ({
    category: item.category,
    requestedAmount: item.requestedAmount,
  }))
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.budgetItems.length === 0) {
    ElMessage.warning('请至少添加一条预算科目')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && projectId.value) {
      await updateProject(projectId.value, form)
      ElMessage.success('保存成功')
      router.push(`/researcher/projects/${projectId.value}`)
    } else {
      const created = await createProject(form)
      ElMessage.success('创建成功')
      router.push(`/researcher/projects/${created.id}`)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadForEdit()
  }
})
</script>

<template>
  <div class="project-form">
    <h2>{{ isEdit ? '编辑项目申报' : '新建项目申报' }}</h2>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="form.projectName" />
      </el-form-item>
      <el-form-item label="研究内容">
        <el-input v-model="form.researchContent" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="团队成员">
        <el-input v-model="form.teamMembers" placeholder="多个成员用逗号分隔" />
      </el-form-item>
      <el-form-item label="经费来源">
        <el-input v-model="form.fundingSource" />
      </el-form-item>
      <el-form-item label="起始日期" prop="startDate">
        <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="截止日期" prop="endDate">
        <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="预算科目">
        <div class="budget-items">
          <div v-for="(item, index) in form.budgetItems" :key="index" class="budget-item-row">
            <el-select v-model="item.category" placeholder="科目类别" style="width: 200px">
              <el-option v-for="opt in BUDGET_CATEGORY_OPTIONS" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
            <el-input-number v-model="item.requestedAmount" :min="0" :precision="2" style="width: 180px" />
            <el-button link type="danger" @click="removeBudgetItem(index)">删除</el-button>
          </div>
          <el-button type="primary" link @click="addBudgetItem">+ 添加预算科目</el-button>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.project-form {
  max-width: 720px;
}

.budget-item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
</style>
