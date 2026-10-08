<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showNav = computed(() => !route.meta.public)

const menuItems = computed(() => {
  switch (userStore.role) {
    case 'RESEARCHER':
      return [{ path: '/researcher/projects', label: '我的项目' }]
    case 'MANAGER':
      return [
        { path: '/manager/projects', label: '项目审批' },
        { path: '/manager/expenditures', label: '经费审核' },
        { path: '/manager/dashboard', label: '数据看板' },
      ]
    case 'ADMIN':
      return [{ path: '/admin/users', label: '账号管理' }]
    default:
      return []
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div v-if="showNav" class="layout">
    <el-menu mode="horizontal" :default-active="route.path" router class="nav-menu" :ellipsis="false">
      <div class="brand">ResearchEase</div>
      <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
        {{ item.label }}
      </el-menu-item>
      <div class="nav-spacer" />
      <span class="user-info">{{ userStore.realName }}（{{ userStore.role }}）</span>
      <el-button link type="primary" @click="handleLogout">退出登录</el-button>
    </el-menu>
    <div class="main-content">
      <router-view />
    </div>
  </div>
  <router-view v-else />
</template>

<style scoped>
.layout {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.nav-menu {
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.brand {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  margin-right: 24px;
}

.nav-spacer {
  flex: 1;
}

.user-info {
  margin-right: 16px;
  color: #606266;
}

.main-content {
  flex: 1;
  overflow: auto;
  padding: 20px 24px;
}
</style>
