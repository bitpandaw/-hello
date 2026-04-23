<template>
  <div class="page">
    <el-skeleton v-if="ld" :rows="5" />
    <template v-else-if="d.product">
      <el-carousel height="3.2rem" indicator-position="outside" class="ignore-vw">
        <el-carousel-item v-for="(x, i) in imgs" :key="i"
          ><el-image :src="x" fit="cover" class="img-lazy" style="height: 3.2rem; width: 100%" lazy
        /></el-carousel-item>
      </el-carousel>
      <h2>{{ d.product.name }}</h2>
      <div v-if="cur" class="price">￥{{ cur.price }}</div>
      <el-radio-group v-model="sku" size="small" class="skus">
        <el-radio v-for="s in d.skus" :key="s.id" :value="s.id" border>{{
          s.specJson || '规格' + s.id
        }}</el-radio>
      </el-radio-group>
      <el-button type="primary" class="add" :loading="adding" @click="add">加入购物车</el-button>
      <h4>商品详情</h4>
      <div class="html" v-html="d.product.detailHtml || '暂无'" />
    </template>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { productDetail } from '@/api/pms'
import { addCart } from '@/api/oms'
import { ElMessage } from 'element-plus'
const route = useRoute()
const d = ref({ product: null, skus: [] })
const ld = ref(true)
const sku = ref()
const adding = ref(false)
const cur = computed(() => (d.value.skus || []).find((x) => x.id === sku.value))
const imgs = computed(() => (d.value.product && d.value.product.coverImg ? [d.value.product.coverImg] : []))
onMounted(async () => {
  const t = await productDetail(route.params.id)
  d.value = t.data || { product: null, skus: [] }
  if (d.value.skus && d.value.skus[0]) {
    sku.value = d.value.skus[0].id
  }
  ld.value = false
})
async function add() {
  if (!cur.value) {
    return
  }
  adding.value = true
  try {
    await addCart(cur.value.id, 1)
    ElMessage.success('已加购')
  } finally {
    adding.value = false
  }
}
</script>
<style scoped>
.add {
  width: 100%;
  margin: 0.2rem 0 0.4rem;
}
.skus {
  display: flex;
  flex-wrap: wrap;
  margin: 0.2rem 0;
  gap: 0.1rem;
}
</style>
