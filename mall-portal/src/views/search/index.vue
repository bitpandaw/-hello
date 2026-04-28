<template>
  <div class="page search-page ignore-vw">
    <div class="toolbar">
      <el-input v-model="q" placeholder="搜索商品" @keyup.enter="load" clearable class="search-input">
        <template #append>
          <el-button @click="load">搜索</el-button>
        </template>
      </el-input>
      <el-radio-group v-model="sort" size="small" @change="load">
        <el-radio-button label="create_time_desc">最新</el-radio-button>
        <el-radio-button label="price_asc">价格升序</el-radio-button>
        <el-radio-button label="price_desc">价格降序</el-radio-button>
      </el-radio-group>
    </div>
    <el-skeleton v-if="ld" :rows="4" />
    <div class="list-grid" v-else>
      <div v-for="p in list" :key="p.id" class="row" @click="$router.push('/product/' + p.id)">
        <el-image :src="p.coverImg" fit="cover" lazy class="thumb" />
        <div class="content">
          <div class="name">{{ p.name }}</div>
          <div class="price">￥{{ p.minPrice }}</div>
        </div>
      </div>
    </div>
    <el-card v-if="!ld && !list.length && recs.length" class="empty-rec">
      <template #header>为你推荐</template>
      <div class="list-grid">
        <div v-for="(p, idx) in recs" :key="p.id" class="row" @click="goRec(p, idx)">
          <el-image :src="p.coverImg" fit="cover" lazy class="thumb" />
          <div class="content">
            <div class="name">{{ p.name }}</div>
            <div class="price">￥{{ p.minPrice }}</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productPage } from '@/api/pms'
import { search, guessRecommend, reportRecommendExpose, reportRecommendClick } from '@/api/pms'
const q = ref('')
const sort = ref('create_time_desc')
const list = ref([])
const recs = ref([])
const recRequestId = ref('')
const ld = ref(true)
const route = useRoute()
const router = useRouter()
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
    if (!list.value.length) {
      const r = await guessRecommend({ size: 6 })
      recRequestId.value = r.data?.requestId || ''
      recs.value = r.data?.products || []
      recs.value.forEach((x, idx) => {
        reportRecommendExpose({
          scene: 'search_empty',
          requestId: recRequestId.value,
          itemId: x.id,
          position: idx + 1,
        })
      })
    } else {
      recs.value = []
    }
  } finally {
    ld.value = false
  }
}

function goRec(p, idx) {
  if (recRequestId.value) {
    reportRecommendClick({
      scene: 'search_empty',
      requestId: recRequestId.value,
      itemId: p.id,
      position: idx + 1,
    })
  }
  router.push('/product/' + p.id)
}
</script>
<style scoped lang="scss">
.ignore-vw .toolbar {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ignore-vw .search-input {
  max-width: 520px;
}

.ignore-vw .list-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.ignore-vw .row {
  display: flex;
  gap: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
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
  color: #303133;
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

@media (max-width: 860px) {
  .ignore-vw .list-grid {
    grid-template-columns: 1fr;
  }
}

.empty-rec {
  margin-top: 12px;
}
</style>
