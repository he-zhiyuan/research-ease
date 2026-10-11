<script setup lang="ts">
import { useAccessibilityStore } from '@/store/accessibility'

const accessibility = useAccessibilityStore()
</script>

<template>
  <el-button
    class="elder-mode-toggle"
    :type="accessibility.elderMode ? 'primary' : 'default'"
    round
    @click="accessibility.toggle()"
  >
    {{ accessibility.elderMode ? '退出大字模式' : '大字模式' }}
  </el-button>
</template>

<style scoped>
.elder-mode-toggle {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 2000;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}
</style>
