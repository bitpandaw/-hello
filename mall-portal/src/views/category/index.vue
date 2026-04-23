<template>
  <div class="page">
    <el-row :gutter="8">
      <el-col :span="8" class="left">
        <el-menu>
          <el-menu-item v-for="c in tree" :key="c.id" @click="sel = c.id">{{ c.name }}</el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="16">
        <el-skeleton v-if="ld" :rows="4" />
        <div v-else v-for="p in list" :key="p.id" class="row" @click="$router.push('/product/' + p.id)">
          <el-image :src="p.coverImg" style="width: 1.2rem; height: 1.2rem" fit="cover" lazy />
          <div>
            <div>{{ p.name }}</div>
            <div class="price">￥{{ p.minPrice }}</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, watch, onMounted } from 'vue'
import { categoryTree, productPage } from '@/api/pms'
const tree = ref([])
const sel = ref(null)
const list = ref([])
const ld = ref(true)
onMounted(async () => {
  const t = await categoryTree()
  tree.value = t.data || []
  if (tree.value[0]) {
    sel.value = tree.value[0].id
    await loadList()
  }
  ld.value = false
})
async function loadList() {
  if (!sel.value) {
    return
  }
  ld.value = true
  const g = await productPage({ p: 1, s: 20, categoryId: sel.value })
  list.value = g.data?.records || []
  ld.value = false
}
watch(sel, (id) => {
  if (id) {
    loadList()
  }
})
</script>
<style scoped>
.row {
  display: flex;
  gap: 0.2rem;
  margin-bottom: 0.2rem;
  background: #fff;
  padding: 0.16rem;
}
.price {
  color: #e74c3c;
  margin-top: 0.12rem;
}
</style>
