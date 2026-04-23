import axios from 'axios'
import { ElMessage } from 'element-plus'
import nprogress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

const base = import.meta.env.VITE_API_BASE_URL || ''
const service = axios.create({ baseURL: base, timeout: 30000 })

let refreshing = false
const waitList = []

function flush(token) {
  waitList.forEach((fn) => fn(token))
  waitList.length = 0
}

async function tryRefresh() {
  const u = useUserStore()
  if (!u.refreshToken) {
    return null
  }
  const raw = (u.refreshToken || '').replace(/^Bearer\s+/i, '')
  const { data: body } = await axios.post(base + '/api/ums/refresh', null, {
    headers: { Authorization: 'Bearer ' + raw },
  })
  if (body && (body.code === 200 || body.code === 0) && body.data) {
    u.setTokens(body.data)
    return (u.accessToken || '').replace(/^Bearer\s+/i, '')
  }
  return null
}

service.interceptors.request.use(
  (config) => {
    nprogress.start()
    const u = useUserStore()
    if (u.accessToken) {
      const raw = u.accessToken.replace(/^Bearer\s+/i, '')
      config.headers.Authorization = 'Bearer ' + raw
    }
    return config
  },
  (e) => {
    nprogress.done()
    return Promise.reject(e)
  }
)

service.interceptors.response.use(
  async (response) => {
    nprogress.done()
    const data = response.data
    if (data && (data.code === 200 || data.code === 0)) {
      return data
    }
    if (data && data.code === 401) {
      const u = useUserStore()
      if (u.accessToken) {
        const t = await tryRefresh()
        if (t) {
          return service(response.config)
        }
      }
      u.clear()
      if (!window.__loginRedirect) {
        window.__loginRedirect = true
        window.location.href = '/auth/login?redirect=' + encodeURIComponent(window.location.pathname)
      }
      return Promise.reject(new Error('unauthorized'))
    }
    const msg = (data && data.message) || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  async (err) => {
    nprogress.done()
    const res = err.response
    if (res && res.status === 401) {
      const u = useUserStore()
      if (u.refreshToken && !err.config._retry) {
        if (refreshing) {
          return new Promise((resolve) => {
            waitList.push(() => {
              err.config._retry = true
              const t = (useUserStore().accessToken || '').replace(/^Bearer\s+/i, '')
              err.config.headers.Authorization = 'Bearer ' + t
              resolve(service(err.config))
            })
          })
        }
        refreshing = true
        try {
          const t = await tryRefresh()
          if (t) {
            flush(t)
            err.config._retry = true
            const rawT = (t && t.replace) ? t.replace(/^Bearer\s+/i, '') : t
            err.config.headers.Authorization = 'Bearer ' + rawT
            return service(err.config)
          }
        } finally {
          refreshing = false
        }
      }
      u.clear()
      window.location.href = '/auth/login'
    } else {
      ElMessage.error(err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default service
export { base }
