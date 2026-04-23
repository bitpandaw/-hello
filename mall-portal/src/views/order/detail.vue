<template>
  <div class="page">
    <el-skeleton v-if="ld" :rows="5" />
    <template v-else-if="d.order">
      <h3>订单 {{ d.order.orderNo }}</h3>
      <p>金额 ￥{{ d.order.payAmount }} 状态 {{ d.order.status }}</p>
      <el-table :data="d.items" size="small" class="ignore-vw">
        <el-table-column prop="spuName" label="商品" />
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column prop="price" label="单价" />
      </el-table>
    </template>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderDetail } from '@/api/oms'
const route = useRoute()
const d = ref({ order: null, items: [] })
const ld = ref(true)
onMounted(async () => {
  const t = await orderDetail(route.params.id)
  d.value = t.data || d.value
  ld.value = false
})
</script>
