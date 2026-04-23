<template>
  <div class="page">
    <el-skeleton v-if="ld" :rows="4" />
    <el-empty v-else-if="!Object.keys(cart).length" description="购物车是空的" />
    <el-card v-else v-for="(v, k) in cart" :key="k" class="c">
      <el-checkbox
        :model-value="v.selected"
        @change="(on) => onSel(k, on)"
        >{{ v.specJson || 'SKU' + k }} x{{ v.quantity }} — ￥{{ v.price }} / 件</el-checkbox
      >
      <el-button size="small" type="danger" text @click="rem(k)">删</el-button>
    </el-card>
    <el-button
      v-if="Object.keys(cart).length"
      type="primary"
      @click="$router.push('/order/confirm')"
      style="width: 100%; margin-top: 0.2rem"
      >去结算</el-button
    >
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { getCart, delCart, selectCart } from '@/api/oms'
const cart = ref({})
const ld = ref(true)
onMounted(async () => {
  await load()
  ld.value = false
})
async function load() {
  const t = await getCart()
  cart.value = t.data || {}
}
async function rem(k) {
  await delCart(k)
  await load()
}
async function onSel(skuId, on) {
  await selectCart(skuId, !!on)
  await load()
}
</script>
<style scoped>
.c {
  margin-bottom: 0.16rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
