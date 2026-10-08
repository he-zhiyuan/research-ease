import { createRouter, createWebHistory } from 'vue-router'
import type { Role } from '@/types'
import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/Login.vue'),
      meta: { public: true },
    },
    {
      path: '/researcher/projects',
      name: 'researcher-projects',
      component: () => import('@/views/researcher/MyProjects.vue'),
      meta: { roles: ['RESEARCHER'] },
    },
    {
      path: '/researcher/projects/new',
      name: 'researcher-project-new',
      component: () => import('@/views/researcher/ProjectForm.vue'),
      meta: { roles: ['RESEARCHER'] },
    },
    {
      path: '/researcher/projects/:id/edit',
      name: 'researcher-project-edit',
      component: () => import('@/views/researcher/ProjectForm.vue'),
      meta: { roles: ['RESEARCHER'] },
    },
    {
      path: '/researcher/projects/:id',
      name: 'researcher-project-detail',
      component: () => import('@/views/researcher/ProjectDetail.vue'),
      meta: { roles: ['RESEARCHER'] },
    },
    {
      path: '/manager/projects',
      name: 'manager-projects',
      component: () => import('@/views/manager/PendingProjects.vue'),
      meta: { roles: ['MANAGER'] },
    },
    {
      path: '/manager/projects/:id',
      name: 'manager-project-detail',
      component: () => import('@/views/manager/ProjectDetail.vue'),
      meta: { roles: ['MANAGER'] },
    },
    {
      path: '/manager/expenditures',
      name: 'manager-expenditures',
      component: () => import('@/views/manager/PendingExpenditures.vue'),
      meta: { roles: ['MANAGER'] },
    },
    {
      path: '/manager/dashboard',
      name: 'manager-dashboard',
      component: () => import('@/views/manager/Dashboard.vue'),
      meta: { roles: ['MANAGER'] },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/admin/UserManagement.vue'),
      meta: { roles: ['ADMIN'] },
    },
    {
      path: '/',
      redirect: () => homeRedirect(),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFound.vue'),
      meta: { public: true },
    },
  ],
})

function homeRedirect() {
  const userStore = useUserStore()
  switch (userStore.role) {
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

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    if (to.name === 'login' && userStore.isLoggedIn) {
      return homeRedirect()
    }
    return true
  }
  if (!userStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  const allowedRoles = to.meta.roles as Role[] | undefined
  if (allowedRoles && !allowedRoles.includes(userStore.role as Role)) {
    return homeRedirect()
  }
  return true
})

export default router
