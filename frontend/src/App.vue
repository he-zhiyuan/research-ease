<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { useUserStore } from '@/store/user'
import { usePendingCountsStore } from '@/store/pendingCounts'
import { useNotificationStore } from '@/store/notifications'
import NotificationDialog from '@/components/NotificationDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const pendingCounts = usePendingCountsStore()
const notificationStore = useNotificationStore()
const notificationDialogVisible = ref(false)

const showNav = computed(() => !route.meta.public)

const menuItems = computed(() => {
  switch (userStore.role) {
    case 'RESEARCHER':
      return [{ path: '/researcher/projects', label: '我的项目' }]
    case 'MANAGER':
      return [
        { path: '/manager/projects', label: '项目审批', badge: pendingCounts.projectCount },
        { path: '/manager/expenditures', label: '经费审核', badge: pendingCounts.expenditureCount },
        { path: '/manager/dashboard', label: '数据看板' },
      ]
    case 'ADMIN':
      return [{ path: '/admin/users', label: '账号管理' }]
    default:
      return []
  }
})

watch(
  () => userStore.role,
  async (role) => {
    if (role === 'MANAGER') {
      pendingCounts.refresh()
      await notificationStore.refresh()
      if (notificationStore.items.length > 0) {
        notificationDialogVisible.value = true
      }
    } else if (role === 'RESEARCHER') {
      await notificationStore.refresh()
      if (notificationStore.items.length > 0) {
        notificationDialogVisible.value = true
      }
    } else {
      pendingCounts.reset()
      notificationStore.reset()
    }
  },
  { immediate: true },
)

router.afterEach((to) => {
  if (userStore.role === 'MANAGER' && to.path.startsWith('/manager')) {
    pendingCounts.refresh()
    notificationStore.refresh()
  } else if (userStore.role === 'RESEARCHER' && to.path.startsWith('/researcher')) {
    notificationStore.refresh()
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-config-provider :locale="zhCn">
    <div v-if="showNav" class="layout">
      <el-menu mode="horizontal" :default-active="route.path" router class="nav-menu" :ellipsis="false">
        <div class="brand">ResearchEase</div>
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-badge v-if="item.badge" :value="item.badge" class="menu-badge">{{ item.label }}</el-badge>
          <template v-else>{{ item.label }}</template>
        </el-menu-item>
        <div class="nav-spacer" />
        <el-badge
          v-if="(userStore.role === 'MANAGER' || userStore.role === 'RESEARCHER') && notificationStore.items.length > 0"
          :value="notificationStore.items.length"
          class="bell-badge"
        >
          <el-button link type="primary" @click="notificationDialogVisible = true">待办提醒</el-button>
        </el-badge>
        <span class="user-info">{{ userStore.realName }}（{{ userStore.role }}）</span>
        <el-button link type="primary" @click="handleLogout">退出登录</el-button>
      </el-menu>
      <div class="main-content">
        <router-view />
      </div>
      <NotificationDialog v-model:visible="notificationDialogVisible" />
    </div>
    <router-view v-else />
  </el-config-provider>
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

.menu-badge :deep(.el-badge__content) {
  top: 4px;
}

.bell-badge {
  margin-right: 16px;
}
</style>
