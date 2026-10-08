<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function homePathFor(role: string) {
  switch (role) {
    case 'RESEARCHER':
      return '/researcher/projects'
    case 'MANAGER':
      return '/manager/projects'
    case 'ADMIN':
      return '/admin/users'
    default:
      return '/login'
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const result = await login(form)
    userStore.setLoginInfo(result)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect as string | undefined
    router.push(redirect || homePathFor(result.role))
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">ResearchEase</h2>
      <p class="subtitle">科研项目全生命周期管理平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleSubmit">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f1ff 0%, #f5f7fa 100%);
}

.login-card {
  width: 360px;
  padding: 16px;
}

.title {
  text-align: center;
  color: #409eff;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin: 8px 0 24px;
  font-size: 13px;
}

.submit-btn {
  width: 100%;
}
</style>
