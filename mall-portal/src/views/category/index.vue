<template>
  <div class="page category-page ignore-vw">
    <section class="section-shell intro-shell">
      <div>
        <h1 class="section-title">数码分类导购</h1>
        <div class="section-copy">按设备品类查看当前平台中的数码商品，适合作为毕业设计答辩中的商品分类展示页面。</div>
      </div>
    </section>

    <div class="wrap">
      <aside class="section-shell left">
        <div class="menu-copy">
          <h2>分类导航</h2>
          <span>设备品类入口</span>
        </div>
        <el-menu :default-active="String(sel || '')" class="menu">
          <el-menu-item v-for="c in tree" :key="c.id" :index="String(c.id)" @click="sel = c.id">{{ c.name }}</el-menu-item>
        </el-menu>
      </aside>

      <section class="section-shell right">
        <div class="section-head">
          <div>
            <h2 class="section-title">{{ activeCategoryName }}</h2>
            <div class="section-copy">基于当前分类筛选出的数码设备列表，突出图片、标题与价格信息。</div>
          </div>
        </div>

        <el-skeleton v-if="ld" :rows="5" />

        <div v-else class="cards">
          <article v-for="p in list" :key="p.id" class="row" @click="$router.push('/product/' + p.id)">
            <el-image :src="resolveProductImage(p.coverImg, p.name)" class="thumb" fit="cover" lazy />
            <div class="content">
              <div class="top">
                <span class="device-chip">{{ deviceType(p.name) }}</span>
                <span class="hint">分类结果</span>
              </div>
              <div class="name">{{ p.name }}</div>
              <div class="desc">{{ productPitch(p.name) }}</div>
              <div class="price">￥{{ formatMoney(p.minPrice) }}</div>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { categoryTree, productPage } from '@/api/pms'
import { resolveProductImage } from '@/utils/image'

const tree = ref([])
const sel = ref(null)
const list = ref([])
const ld = ref(true)

const activeCategoryName = computed(() => tree.value.find((item) => item.id === sel.value)?.name || '设备分类')

onMounted(async () => {
  const treeRes = await categoryTree()
  tree.value = treeRes.data || []
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
  const result = await productPage({ p: 1, s: 20, categoryId: sel.value })
  list.value = result.data?.records || []
  ld.value = false
}

watch(sel, (id) => {
  if (id) {
    loadList()
  }
})

function formatMoney(value) {
  const n = Number(value)
  return Number.isNaN(n) ? String(value ?? '--') : n.toFixed(2)
}

function deviceType(name) {
  const text = String(name || '').toLowerCase()
  if (text.includes('phone')) return '智能手机'
  if (text.includes('book')) return '轻薄笔记本'
  if (text.includes('pad') || text.includes('display')) return '平板设备'
  if (text.includes('watch')) return '智能穿戴'
  if (text.includes('buds')) return '音频设备'
  if (text.includes('cam')) return '影像设备'
  if (text.includes('power')) return '数码配件'
  return '数码设备'
}

function productPitch(name) {
  const type = deviceType(name)
  const map = {
    智能手机: '聚焦移动通讯、性能与影像体验。',
    轻薄笔记本: '适合课程设计开发、学习办公与项目答辩演示。',
    平板设备: '兼顾大屏浏览、轻办公与阅读记录。',
    智能穿戴: '强调健康监测与设备互联体验。',
    音频设备: '突出降噪、连接稳定与沉浸听感。',
    影像设备: '适合拍摄创作与内容记录展示。',
    数码配件: '满足补能与外设扩展需求。',
  }
  return map[type] || '数码平台的设备展示信息。'
}
</script>

<style scoped lang="scss">
.ignore-vw.category-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .wrap {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 18px;
}

.ignore-vw .menu-copy h2 {
  margin: 0;
  color: $brand-ink;
  font-size: 22px;
}

.ignore-vw .menu-copy span {
  display: block;
  margin-top: 8px;
  color: $text-soft;
  font-size: 13px;
}

.ignore-vw .menu {
  margin-top: 16px;
  border-right: none;
}

.ignore-vw .cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .row {
  border: 1px solid $brand-border;
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  padding: 16px;
  display: flex;
  gap: 14px;
  cursor: pointer;
}

.ignore-vw .thumb {
  width: 160px;
  height: 160px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eef4fa, #dfeaf6);
}

.ignore-vw .content {
  min-width: 0;
  flex: 1;
}

.ignore-vw .top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.ignore-vw .hint {
  color: $text-soft;
  font-size: 12px;
}

.ignore-vw .name {
  margin-top: 12px;
  color: $brand-ink;
  font-size: 20px;
  font-weight: 800;
  line-height: 1.35;
}

.ignore-vw .desc {
  margin-top: 10px;
  color: $text-sub;
  font-size: 14px;
  line-height: 1.75;
}

.ignore-vw .price {
  margin-top: 14px;
  color: #ff5c43;
  font-size: 26px;
  font-weight: 800;
}

@media (max-width: 980px) {
  .ignore-vw .wrap {
    grid-template-columns: 1fr;
  }

  .ignore-vw .cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .ignore-vw .row {
    flex-direction: column;
  }

  .ignore-vw .thumb {
    width: 100%;
    height: 220px;
  }
}
</style>
