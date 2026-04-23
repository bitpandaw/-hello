import { createRouter, createWebHistory } from 'vue-router'
import nprogress from 'nprogress'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/', redirect: '/home' },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/category',
    name: 'category',
    component: () => import('@/views/category/index.vue'),
    meta: { title: '分类' },
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/views/search/index.vue'),
    meta: { title: '搜索' },
  },
  {
    path: '/product/:id',
    name: 'product',
    component: () => import('@/views/product/detail.vue'),
    meta: { title: '商品' },
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('@/views/cart/index.vue'),
    meta: { title: '购物车', requiresAuth: true },
  },
  {
    path: '/order/confirm',
    name: 'orderConfirm',
    component: () => import('@/views/order/confirm.vue'),
    meta: { title: '确认订单', requiresAuth: true },
  },
  {
    path: '/order/pay/:id',
    name: 'orderPay',
    component: () => import('@/views/order/pay.vue'),
    meta: { title: '支付', requiresAuth: true },
  },
  {
    path: '/order/list',
    name: 'orderList',
    component: () => import('@/views/order/list.vue'),
    meta: { title: '我的订单', requiresAuth: true },
  },
  {
    path: '/order/detail/:id',
    name: 'orderDetail',
    component: () => import('@/views/order/detail.vue'),
    meta: { title: '订单详情', requiresAuth: true },
  },
  {
    path: '/member',
    name: 'member',
    component: () => import('@/views/member/index.vue'),
    meta: { title: '个人中心', requiresAuth: true },
  },
  {
    path: '/member/info',
    name: 'memberInfo',
    component: () => import('@/views/member/info.vue'),
    meta: { title: '资料', requiresAuth: true },
  },
  {
    path: '/member/address',
    name: 'memberAddress',
    component: () => import('@/views/member/address.vue'),
    meta: { title: '地址', requiresAuth: true },
  },
  {
    path: '/member/coupon',
    name: 'memberCoupon',
    component: () => import('@/views/member/coupon.vue'),
    meta: { title: '优惠券', requiresAuth: true },
  },
  {
    path: '/auth/login',
    name: 'login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/auth/register',
    name: 'register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册' },
  },
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to, from, next) => {
  nprogress.start()
  document.title = (to.meta.title || 'Mall') + ' - Mall'
  if (to.meta.requiresAuth) {
    const u = useUserStore()
    if (!u.isLogin) {
      next({ path: '/auth/login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})
router.afterEach(() => nprogress.done())

export default router
