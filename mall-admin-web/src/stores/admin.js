import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMenus } from '@/api/admin'
import { mapMenuToRoutes } from '@/utils/permission'

export const useAdminStore = defineStore(
  'admin',
  () => {
    const accessToken = ref('')
    const refreshToken = ref('')
    const menuTree = ref([])
    const routesAdded = ref(false)
    const isLogin = computed(() => !!accessToken.value)
    function setTokens(t) {
      if (!t) {
        return
      }
      const acc = t.access || t.accessToken || ''
      const refT = t.refresh || t.refreshToken || ''
      accessToken.value = acc
      refreshToken.value = refT
    }
    function clear() {
      accessToken.value = ''
      refreshToken.value = ''
      menuTree.value = []
      routesAdded.value = false
    }
    async function loadMenus() {
      const { data } = await getMenus()
      menuTree.value = data || []
    }
    function toRoutes(router) {
      const { routes: list, firstPath } = mapMenuToRoutes(menuTree.value)
      list.forEach((r) => router.addRoute('Layout', r))
      const fp = (firstPath || '/dashboard').replace(/\/+/, '/')
      router.addRoute('Layout', { path: '', name: 'rootR' + Date.now(), redirect: fp })
      routesAdded.value = true
    }
    return { accessToken, refreshToken, menuTree, routesAdded, isLogin, setTokens, clear, loadMenus, toRoutes }
  },
  { persist: { key: 'mall_admin', pick: ['accessToken', 'refreshToken'] } }
)
