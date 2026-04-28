const comp = (key) => {
  const k = (key || '').toLowerCase()
  const m = {
    'dashboard': () => import('@/views/dashboard/index.vue'),
    'dashboard/index': () => import('@/views/dashboard/index.vue'),
    'product/list': () => import('@/views/pms/product.vue'),
    'order/list': () => import('@/views/oms/order.vue'),
    'member/list': () => import('@/views/ums/member.vue'),
    'system/role': () => import('@/views/system/index.vue'),
    'recommend/manage': () => import('@/views/recommend/manage.vue'),
    'recommend/report': () => import('@/views/recommend/report.vue'),
  }
  const f = m[k] || m[k.replace(/^\s+|\s+$/g, '')]
  return f
    ? f
    : () => import('@/views/empty/index.vue')
}

function norm(p) {
  if (!p) {
    return 'm'
  }
  return String(p).replace(/^\//, '') || 'm'
}

/** 扁平化菜单为路由（一层或两层，叶子挂组件） */
export function mapMenuToRoutes(tree) {
  const out = []
  for (const n of tree) {
    const children = n.children && n.children.length
    if (children) {
      for (const c of n.children) {
        if (!c.path && !c.component) {
          continue
        }
        out.push({
          path: norm(c.path || n.path + '/sub'),
          name: (c.code || 'm' + c.id).replace(/:/g, '_'),
          meta: { title: c.name || n.name, keep: true },
          component: comp(c.component || n.component),
        })
      }
    } else {
      out.push({
        path: norm(n.path),
        name: (n.code || 'm' + n.id).replace(/:/g, '_'),
        meta: { title: n.name, keep: true },
        component: comp(n.component),
      })
    }
  }
  if (!out.length) {
    out.push({
      path: 'dashboard',
      name: 'dashboard_f',
      meta: { title: '控制台', keep: true },
      component: comp('dashboard/index'),
    })
  }
  return { routes: out, firstPath: '/' + out[0].path }
}
