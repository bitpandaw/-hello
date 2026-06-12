<template>
  <div class="page member-page ignore-vw">
    <section class="hero-card section-shell">
      <div class="avatar">{{ initials }}</div>
      <div class="hero-copy">
        <h1 class="section-title">我的账户</h1>
        <div class="section-copy">用于展示会员信息、订单入口与个人服务模块。</div>
        <strong v-if="me">{{ me.username }}</strong>
      </div>
    </section>

    <div class="grid">
      <section class="section-shell account-card">
        <div class="section-head">
          <div>
            <h2 class="section-title">账户概览</h2>
            <div class="section-copy">展示当前登录用户在数码销售平台中的基础信息。</div>
          </div>
        </div>
        <div class="info-list">
          <div class="info-row"><span>用户名</span><strong>{{ me?.username || '--' }}</strong></div>
          <div class="info-row"><span>手机号</span><strong>{{ me?.phone || '--' }}</strong></div>
          <div class="info-row"><span>邮箱</span><strong>{{ me?.email || '--' }}</strong></div>
        </div>
      </section>

      <section class="section-shell link-card">
        <div class="section-head">
          <div>
            <h2 class="section-title">常用入口</h2>
            <div class="section-copy">统一展示个人中心相关的功能入口。</div>
          </div>
        </div>
        <div class="link-grid">
          <button v-for="item in links" :key="item.label" class="link-item" @click="$router.push(item.to)">
            <strong>{{ item.label }}</strong>
            <span>{{ item.copy }}</span>
          </button>
        </div>
        <el-button type="danger" plain class="logout" @click="logout">退出登录</el-button>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getMe } from '@/api/ums'
import { useUserStore } from '@/stores/user'

const me = ref(null)
const userStore = useUserStore()
const router = useRouter()

const links = [
  { label: '账户资料', copy: '查看手机号与邮箱信息', to: '/member/info' },
  { label: '收货地址', copy: '管理收货地址与配送信息', to: '/member/address' },
  { label: '优惠权益', copy: '查看可领取优惠券和活动权益', to: '/member/coupon' },
  { label: '订单中心', copy: '跟踪订单状态与支付结果', to: '/order/list' },
]

const initials = computed(() => (me.value?.username || 'DSP').slice(0, 2).toUpperCase())

onMounted(async () => {
  const result = await getMe()
  me.value = result.data
})

function logout() {
  userStore.clear()
  router.push('/auth/login')
}
</script>

<style scoped lang="scss">
.ignore-vw.member-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ignore-vw .hero-card {
  display: flex;
  align-items: center;
  gap: 18px;
}

.ignore-vw .avatar {
  width: 86px;
  height: 86px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, $brand-primary, $brand-accent);
  color: #fff;
  font-size: 28px;
  font-weight: 800;
}

.ignore-vw .hero-copy strong {
  display: block;
  margin-top: 10px;
  color: $brand-ink;
  font-size: 22px;
}

.ignore-vw .grid {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 18px;
}

.ignore-vw .info-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ignore-vw .info-row {
  display: flex;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid $brand-border;
  color: $text-sub;
}

.ignore-vw .info-row strong {
  color: $brand-ink;
}

.ignore-vw .link-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.ignore-vw .link-item {
  text-align: left;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid $brand-border;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  cursor: pointer;
}

.ignore-vw .link-item strong {
  display: block;
  color: $brand-ink;
  font-size: 18px;
}

.ignore-vw .link-item span {
  display: block;
  margin-top: 10px;
  color: $text-sub;
  font-size: 13px;
  line-height: 1.7;
}

.ignore-vw .logout {
  margin-top: 16px;
}

@media (max-width: 980px) {
  .ignore-vw .grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .ignore-vw .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .ignore-vw .link-grid {
    grid-template-columns: 1fr;
  }
}
</style>
