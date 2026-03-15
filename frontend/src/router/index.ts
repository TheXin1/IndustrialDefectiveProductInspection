import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import AppLayout from '../layouts/AppLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import DetectView from '../views/DetectView.vue'
import RealtimeView from '../views/RealtimeView.vue'
import RecordsView from '../views/RecordsView.vue'
import ReportsView from '../views/ReportsView.vue'
import SettingsView from '../views/SettingsView.vue'
import UsersView from '../views/UsersView.vue'
import RolesView from '../views/RolesView.vue'
import PermissionsView from '../views/PermissionsView.vue'
import AuditView from '../views/AuditView.vue'
import PlaceholderView from '../views/PlaceholderView.vue'

const routes: RouteRecordRaw[] = [
  { path: '/login', component: LoginView, meta: { public: true } },
  { path: '/register', component: RegisterView, meta: { public: true } },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: DashboardView },
      { path: 'detect', component: DetectView },
      { path: 'realtime', component: RealtimeView, meta: { title: '实时监控' } },
      { path: 'records', component: RecordsView, meta: { title: '检测记录' } },
      { path: 'reports', component: ReportsView, meta: { title: '报表统计' } },
      { path: 'settings', component: SettingsView, meta: { title: '个人设置' } },
      { path: 'access/users', component: UsersView, meta: { title: '用户管理' } },
      { path: 'access/roles', component: RolesView, meta: { title: '角色管理' } },
      { path: 'access/permissions', component: PermissionsView, meta: { title: '权限管理' } },
      { path: 'model', component: PlaceholderView, meta: { title: '模型管理' } },
      { path: 'dataset', component: PlaceholderView, meta: { title: '数据集管理' } },
      { path: 'audit', component: AuditView, meta: { title: '审计日志' } },
      { path: 'system', component: PlaceholderView, meta: { title: '系统设置' } },
      { path: 'integration', component: PlaceholderView, meta: { title: '集成管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!auth.user) {
    auth.hydrate()
  }
  if (to.meta.public) return true
  if (!auth.isLoggedIn) {
    return '/login'
  }
  return true
})

export default router
