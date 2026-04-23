<template>
  <div class="page">
    <h3>个人资料</h3>
    <p v-if="d">手机 {{ d.phone || '-' }} 邮箱 {{ d.email || '-' }}</p>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getMe } from '@/api/ums'
const d = ref()
onMounted(async () => (d.value = (await getMe()).data))
</script>
