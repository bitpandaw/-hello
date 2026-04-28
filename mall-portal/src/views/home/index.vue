<template>
  <div class="page home ignore-vw">
    <el-skeleton v-if="load" :rows="6" animated class="skeleton" />
    <template v-else>
      <div class="hero">
        <swiper
          v-if="banners.length"
          :modules="[Pagination, Autoplay]"
          :pagination="{ clickable: true }"
          :autoplay="{ delay: 3500 }"
          class="banner"
        >
          <swiper-slide v-for="(b, i) in banners" :key="i"
            ><div class="bn">{{ b }}</div></swiper-slide
          >
        </swiper>
        <div class="quick-panel">
          <div class="qp-title">热门分类</div>
          <div class="quick-list">
            <el-button
              v-for="c in cats.slice(0, 8)"
              :key="c.id"
              class="quick-btn"
              @click="$router.push('/search?cat=' + c.id)"
            >
              {{ c.name }}
            </el-button>
          </div>
        </div>
      </div>
      <div class="section-title">
        <h3>猜你喜欢</h3>
        <el-link type="primary" @click="$router.push('/search')">更多商品</el-link>
      </div>
      <div class="product-grid">
        <el-card
          v-for="(p, idx) in products"
          :key="p.id"
          shadow="hover"
          class="goods-card"
          @click="goDetail(p, idx)"
        >
          <el-image :src="p.coverImg || 'https://via.placeholder.com/200'" fit="cover" lazy class="goods-img" />
          <div class="pname">{{ p.name }}</div>
          <div class="meta">
            <div class="price">￥{{ p.minPrice }}</div>
            <span class="tag">包邮</span>
          </div>
        </el-card>
      </div>
    </template>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Pagination, Autoplay } from 'swiper/modules'
import { categoryTree, productPage, guessRecommend, reportRecommendExpose, reportRecommendClick } from '@/api/pms'
import 'swiper/css'
import 'swiper/css/pagination'

const load = ref(true)
const router = useRouter()
const cats = ref([])
const products = ref([])
const recRequestId = ref('')
const banners = ref(['限时秒杀 低至 3 折', '爆款上新 满 199 减 30', '品质家电 以旧换新'])
onMounted(async () => {
  try {
    const t = await categoryTree()
    cats.value = t.data || []
    const g = await guessRecommend({ size: 8 })
    recRequestId.value = g.data?.requestId || ''
    products.value = g.data?.products || []
    if (!products.value.length) {
      const fallback = await productPage({ p: 1, s: 8 })
      products.value = fallback.data?.records || []
    } else {
      products.value.forEach((x, idx) => {
        reportRecommendExpose({
          scene: 'home_guess',
          requestId: recRequestId.value,
          itemId: x.id,
          position: idx + 1,
        })
      })
    }
  } finally {
    load.value = false
  }
})

function goDetail(p, idx) {
  if (recRequestId.value) {
    reportRecommendClick({
      scene: 'home_guess',
      requestId: recRequestId.value,
      itemId: p.id,
      position: idx + 1,
    })
  }
  router.push('/product/' + p.id)
}
</script>
<style scoped lang="scss">
.ignore-vw.home {
  padding-bottom: 0.4rem;
}

.ignore-vw .hero {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 14px;
  margin-bottom: 18px;
}

.ignore-vw .banner {
  height: 280px;
  border-radius: 10px;
  overflow: hidden;
}

.ignore-vw .bn {
  background: linear-gradient(135deg, #ff8a00, #ff4d4f);
  color: #fff;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
}

.ignore-vw .quick-panel {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.ignore-vw .qp-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 12px;
}

.ignore-vw .quick-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.ignore-vw .quick-btn {
  margin: 0;
}

.ignore-vw .section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 10px 0 14px;
}

.ignore-vw .product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .goods-card {
  cursor: pointer;
}

.ignore-vw .goods-img {
  width: 100%;
  height: 190px;
  border-radius: 6px;
}

.ignore-vw .pname {
  margin-top: 10px;
  min-height: 44px;
  line-height: 22px;
  font-size: 14px;
  color: #333;
}

.ignore-vw .meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ignore-vw .price {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: 700;
}

.ignore-vw .tag {
  font-size: 12px;
  color: #ff6a00;
  border: 1px solid #ffc9ab;
  border-radius: 4px;
  padding: 2px 6px;
}

@media (max-width: 1100px) {
  .ignore-vw .product-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .ignore-vw .hero {
    grid-template-columns: 1fr;
  }

  .ignore-vw .banner,
  .ignore-vw .bn {
    height: 220px;
  }

  .ignore-vw .bn {
    font-size: 22px;
  }

  .ignore-vw .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
