import { createRouter, createWebHistory } from 'vue-router'
import nprogress from 'nprogress'
import 'nprogress/nprogress.css'
import { useAdminStore } from '@/stores/admin'

const routes = [
  { path: '/login', name: 'login', component: () => import('@/views/login/index.vue'), meta: { title: '登录' } },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/Index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard_static',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '控制台', keep: true },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to, from, next) => {
  nprogress.start()
  document.title = to.meta.title ? to.meta.title + ' — Mall Admin' : 'Mall Admin'
  if (to.path === '/login') {
    next()
    return
  }
  const a = useAdminStore()
  if (!a.isLogin) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (!a.routesAdded) {
    try {
      await a.loadMenus()
      a.toRoutes(router)
    } catch {
      a.clear()
      next({ path: '/login' })
      return
    }
    next({ path: to.fullPath, replace: true })
    return
  }
  next()
})
router.afterEach(() => nprogress.done())

export default router
