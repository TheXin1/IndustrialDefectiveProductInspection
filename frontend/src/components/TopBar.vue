<template>
  <div style="display: flex; align-items: center; gap: 12px;">
    <div class="badge">系统运行中</div>
    <div class="subtle">{{ timeText }}</div>
    <el-divider direction="vertical" />
    <div>{{ auth.user?.displayName || auth.user?.username }}</div>
    <el-button size="small" type="danger" plain @click="logout">退出</el-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const timeText = ref(new Date().toLocaleString())

let timer: number | undefined

onMounted(() => {
  timer = window.setInterval(() => {
    timeText.value = new Date().toLocaleString()
  }, 1000)
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})

const logout = () => {
  auth.logout()
  router.push('/login')
}
</script>
