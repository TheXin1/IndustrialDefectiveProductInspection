<template>
  <el-menu
    class="el-menu-vertical-demo"
    background-color="transparent"
    text-color="#e2e8f0"
    active-text-color="#5eead4"
    :default-active="activePath"
    router
  >
    <el-menu-item v-for="item in visibleMenus" :key="item.path" :index="item.path">
      <span>{{ item.label }}</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

interface MenuItem {
  label: string
  path: string
  permission?: string
}

const menuConfig: MenuItem[] = [
  { label: '仪表盘', path: '/dashboard', permission: 'dashboard' },
  { label: '检测', path: '/detect', permission: 'detect' },
  { label: '实时监控', path: '/realtime', permission: 'realtime' },
  { label: '检测记录', path: '/records', permission: 'records' },
  { label: '报表统计', path: '/reports', permission: 'reports' },
  { label: '模型管理', path: '/model', permission: 'model' },
  { label: '数据集管理', path: '/dataset', permission: 'dataset' },
  { label: '用户管理', path: '/access/users', permission: 'user_access' },
  { label: '角色管理', path: '/access/roles', permission: 'user_access' },
  { label: '权限管理', path: '/access/permissions', permission: 'user_access' },
  { label: '审计日志', path: '/audit', permission: 'audit' },
  { label: '系统设置', path: '/system', permission: 'system' },
  { label: '集成管理', path: '/integration', permission: 'integration' }
]

const auth = useAuthStore()
const route = useRoute()

const visibleMenus = computed(() => {
  const codes = new Set(auth.permissionCodes)
  return menuConfig.filter((item) => !item.permission || codes.has(item.permission))
})

const activePath = computed(() => route.path)
</script>
