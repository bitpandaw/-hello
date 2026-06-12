import { createRouter, createWebHistory } from 'vue-router'
import nprogress from 'nprogress'
import { useUserStore } from '@/stores/user'

const APP_TITLE = '数码销售平台'

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
    meta: { title: '数码分类' },
  },
  {
    path: '/search',
    name: 'search',
    component: () => import('@/views/search/index.vue'),
    meta: { title: '精选设备' },
  },
  {
    path: '/product/:id',
    name: 'product',
    component: () => import('@/views/product/detail.vue'),
    meta: { title: '商品详情' },
  },
  {
    path: '/cart',
    name: 'cart',
    component: () => import('@/views/cart/index.vue'),
    meta: { title: '购物车结算', requiresAuth: true },
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
    meta: { title: '订单支付', requiresAuth: true },
  },
  {
    path: '/order/list',
    name: 'orderList',
    component: () => import('@/views/order/list.vue'),
    meta: { title: '订单中心', requiresAuth: true },
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
    meta: { title: '我的账户', requiresAuth: true },
  },
  {
    path: '/member/info',
    name: 'memberInfo',
    component: () => import('@/views/member/info.vue'),
    meta: { title: '账户资料', requiresAuth: true },
  },
  {
    path: '/member/address',
    name: 'memberAddress',
    component: () => import('@/views/member/address.vue'),
    meta: { title: '收货地址', requiresAuth: true },
  },
  {
    path: '/member/coupon',
    name: 'memberCoupon',
    component: () => import('@/views/member/coupon.vue'),
    meta: { title: '优惠权益', requiresAuth: true },
  },
  {
    path: '/auth/login',
    name: 'login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '用户登录' },
  },
  {
    path: '/auth/register',
    name: 'register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '用户注册' },
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  nprogress.start()
  document.title = `${to.meta.title || '首页'} - ${APP_TITLE}`
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      next({ path: '/auth/login', query: { redirect: to.fullPath } })
      return
    }
  }
  next()
})

router.afterEach(() => nprogress.done())

export default router
