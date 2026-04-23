import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore(
  'user',
  () => {
    const accessToken = ref('')
    const refreshToken = ref('')
    const user = ref(null)

    const isLogin = computed(() => !!accessToken.value)

    function setTokens(d) {
      accessToken.value = d.access || d.accessToken || ''
      refreshToken.value = d.refresh || d.refreshToken || ''
    }
    function clear() {
      accessToken.value = ''
      refreshToken.value = ''
      user.value = null
    }
    return { accessToken, refreshToken, user, isLogin, setTokens, clear }
  },
  { persist: { key: 'mall_portal_user', pick: ['accessToken', 'refreshToken'] } }
)
