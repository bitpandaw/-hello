import axios from 'axios'
import { ElMessage } from 'element-plus'
import nprogress from 'nprogress'
import 'nprogress/nprogress.css'
import { useAdminStore } from '@/stores/admin'

const base = import.meta.env.VITE_API_BASE_URL || ''
const service = axios.create({ baseURL: base, timeout: 30000 })
let refreshing = false
const queue = []
function runQueue(t) {
  queue.forEach((f) => f(t))
  queue.length = 0
}
async function tryRefresh() {
  const a = useAdminStore()
  if (!a.refreshToken) {
    return null
  }
  const rawRef = (a.refreshToken || '').replace(/^Bearer\s+/i, '')
  const { data: body } = await axios.post(base + '/admin/auth/refresh', null, {
    headers: { Authorization: 'Bearer ' + rawRef },
  })
  if (body && (body.code === 200 || body.code === 0) && body.data) {
    a.setTokens(body.data)
    return (a.accessToken || '').replace(/^Bearer\s+/i, '')
  }
  return null
}
service.interceptors.request.use(
  (config) => {
    nprogress.start()
    const a = useAdminStore()
    if (a.accessToken) {
      const raw = a.accessToken.replace(/^Bearer\s+/i, '')
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
    const msg = (data && data.message) || '请求失败'
    if (data && data.code === 401) {
      const a = useAdminStore()
      if (a.refreshToken && !response.config._retry) {
        if (refreshing) {
          return new Promise((resolve) => {
            queue.push((token) => {
              response.config._retry = true
              const raw = (token && token.replace) ? token.replace(/^Bearer\s+/i, '') : token
            response.config.headers.Authorization = 'Bearer ' + raw
              resolve(service(response.config))
            })
          })
        }
        refreshing = true
        try {
          const t = await tryRefresh()
          if (t) {
            runQueue(t)
            response.config._retry = true
            response.config.headers.Authorization = 'Bearer ' + t
            return service(response.config)
          }
        } finally {
          refreshing = false
        }
      }
      a.clear()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      return Promise.reject(new Error('unauthorized'))
    }
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (err) => {
    nprogress.done()
    const res = err.response
    if (res && res.status === 401) {
      const a = useAdminStore()
      a.clear()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else {
      ElMessage.error(err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)
export default service
export { base }
