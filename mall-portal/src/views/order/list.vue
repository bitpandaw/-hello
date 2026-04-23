<template>
  <div class="page">
    <h3>我的订单</h3>
    <el-select v-model="st" placeholder="状态" clearable @change="load" style="width: 100%; margin-bottom: 0.2rem">
      <el-option :value="0" label="待付" />
      <el-option :value="1" label="已付/待发" />
      <el-option :value="2" label="已发" />
      <el-option :value="3" label="完成" />
    </el-select>
    <el-skeleton v-if="ld" :rows="4" />
    <el-card v-for="o in rows" :key="o.id" class="c" @click="$router.push('/order/detail/' + o.id)">
      <div>{{ o.orderNo }} ￥{{ o.payAmount }} 状态 {{ o.status }}</div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { myOrders } from '@/api/oms'
const st = ref(null)
const rows = ref([])
const ld = ref(true)
onMounted(load)
async function load() {
  ld.value = true
  const params = { p: 1, s: 20 }
  if (st.value !== null && st.value !== undefined && st.value !== '') {
    params.status = st.value
  }
  const t = await myOrders(params)
  rows.value = t.data?.records || []
  ld.value = false
}
</script>
<style scoped>
.c {
  margin-bottom: 0.16rem;
  cursor: pointer;
}
</style>
