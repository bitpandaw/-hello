<template>
  <div class="page category-page ignore-vw">
    <div class="wrap">
      <aside class="left">
        <div class="title">商品分类</div>
        <el-menu :default-active="String(sel || '')">
          <el-menu-item v-for="c in tree" :key="c.id" :index="String(c.id)" @click="sel = c.id">{{ c.name }}</el-menu-item>
        </el-menu>
      </aside>
      <section class="right">
        <el-skeleton v-if="ld" :rows="5" />
        <div class="cards" v-else>
          <div v-for="p in list" :key="p.id" class="row" @click="$router.push('/product/' + p.id)">
            <el-image :src="p.coverImg" class="thumb" fit="cover" lazy />
            <div class="content">
              <div class="name">{{ p.name }}</div>
              <div class="price">￥{{ p.minPrice }}</div>
            </div>
          </div>
        </div>
      </section>
    </div>
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
<style scoped lang="scss">
.ignore-vw.category-page {
  padding-bottom: 0.4rem;
}

.ignore-vw .wrap {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 14px;
}

.ignore-vw .left {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}

.ignore-vw .title {
  font-size: 16px;
  font-weight: 700;
  padding: 14px 14px 0;
}

.ignore-vw .right {
  min-width: 0;
}

.ignore-vw .cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.ignore-vw .row {
  display: flex;
  gap: 12px;
  background: #fff;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
}

.ignore-vw .thumb {
  width: 120px;
  height: 120px;
  border-radius: 6px;
}

.ignore-vw .content {
  min-width: 0;
}

.ignore-vw .name {
  line-height: 22px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ignore-vw .price {
  color: #ff4d4f;
  margin-top: 10px;
  font-weight: 700;
  font-size: 20px;
}

@media (max-width: 980px) {
  .ignore-vw .wrap {
    grid-template-columns: 1fr;
  }

  .ignore-vw .cards {
    grid-template-columns: 1fr;
  }
}
</style>
