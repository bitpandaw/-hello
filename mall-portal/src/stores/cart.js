import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useCartStore = defineStore(
  'cart',
  () => {
    const lastKeys = ref([])
    return { lastKeys }
  },
  { persist: { key: 'mall_portal_cart_meta' } }
)
