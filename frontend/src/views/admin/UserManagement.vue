<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { createUser, listUsers, setUserEnabled } from '@/api/user'
import type { Role, UserResponse } from '@/types'
import { ROLE_LABEL } from '@/utils/dict'

const users = ref<UserResponse[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const creating = ref(false)
const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: 'RESEARCHER' as Role,
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const roleOptions = Object.entries(ROLE_LABEL).map(([value, label]) => ({ value, label }))

async function loadUsers() {
  loading.value = true
  try {
    users.value = await listUsers()
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = 'RESEARCHER'
  dialogVisible.value = true
}

async function handleCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  creating.value = true
  try {
    await createUser({ ...form })
    ElMessage.success('账号创建成功')
    dialogVisible.value = false
    await loadUsers()
  } finally {
    creating.value = false
  }
}

async function handleToggleEnabled(row: UserResponse, enabled: boolean) {
  await setUserEnabled(row.id, enabled)
  ElMessage.success(enabled ? '账号已启用' : '账号已禁用')
  await loadUsers()
}

onMounted(loadUsers)
</script>

<template>
  <div class="user-management">
    <div class="toolbar">
      <h2>账号管理</h2>
      <el-button type="primary" @click="openCreateDialog">新建账号</el-button>
    </div>
    <el-table v-loading="loading" :data="users">
      <el-table-column prop="username" label="用户名" width="160" />
      <el-table-column prop="realName" label="姓名" width="140" />
      <el-table-column label="角色" width="140">
        <template #default="{ row }">{{ ROLE_LABEL[row.role] }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="启用状态" width="120">
        <template #default="{ row }">
          <el-switch
            :model-value="row.enabled"
            @change="(val: boolean) => handleToggleEnabled(row, val)"
          />
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新建账号" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option v-for="opt in roleOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
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
