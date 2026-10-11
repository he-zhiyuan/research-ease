<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/store/notifications'
import type { NotificationItem } from '@/store/notifications'

const visible = defineModel<boolean>('visible', { default: false })

const router = useRouter()
const notificationStore = useNotificationStore()

function handleGo(item: NotificationItem) {
  visible.value = false
  router.push(item.route)
}

function handleAck(key: string) {
  notificationStore.dismiss(key)
  if (notificationStore.items.length === 0) {
    visible.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" title="待办提醒" width="480px">
    <div v-if="notificationStore.items.length === 0" class="empty">暂无新的待办提醒</div>
    <div v-for="item in notificationStore.items" :key="item.key" class="notification-item">
      <p class="text">{{ item.text }}</p>
      <div class="actions">
        <el-button size="small" @click="handleAck(item.key)">知道了</el-button>
        <el-button size="small" type="primary" @click="handleGo(item)">去处理</el-button>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">全部关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.notification-item:last-child {
  border-bottom: none;
}

.text {
  margin: 0;
  color: #303133;
  font-size: 14px;
}

.actions {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
}

.empty {
  color: #909399;
  text-align: center;
  padding: 24px 0;
}
</style>
