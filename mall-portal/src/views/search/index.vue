<template>
  <div class="page">
    <el-input v-model="q" placeholder="搜索" @keyup.enter="load" clearable>
      <template #append
        ><el-button @click="load">搜</el-button></template
      >
    </el-input>
    <el-radio-group v-model="sort" size="small" @change="load" style="margin: 0.2rem 0">
      <el-radio-button label="create_time_desc">新</el-radio-button>
      <el-radio-button label="price_asc">价低</el-radio-button>
      <el-radio-button label="price_desc">价高</el-radio-button>
    </el-radio-group>
    <el-skeleton v-if="ld" :rows="4" />
    <div v-for="p in list" :key="p.id" class="row" v-else @click="$router.push('/product/' + p.id)">
      <el-image :src="p.coverImg" style="width: 1.2rem" fit="cover" lazy />
      <div>
        <div>{{ p.name }}</div>
        <div class="price">￥{{ p.minPrice }}</div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { productPage } from '@/api/pms'
import { search } from '@/api/pms'
const q = ref('')
const sort = ref('create_time_desc')
const list = ref([])
const ld = ref(true)
const route = useRoute()
onMounted(() => {
  if (route.query.q) {
    q.value = String(route.query.q)
  }
  load()
})
async function load() {
  ld.value = true
  try {
    if (q.value) {
      const t = await search(q.value)
      list.value = t.data || []
    } else {
      const cat = route.query.cat ? Number(route.query.cat) : undefined
      const g = await productPage({ p: 1, s: 20, categoryId: cat, sort: sort.value, q: undefined })
      list.value = g.data?.records || []
    }
  } finally {
    ld.value = false
  }
}
</script>
<style scoped>
.row {
  display: flex;
  gap: 0.2rem;
  background: #fff;
  margin-bottom: 0.2rem;
  padding: 0.16rem;
}
.price {
  color: #e74c3c;
}
</style>
