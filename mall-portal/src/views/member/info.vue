<template>
  <div class="page ignore-vw">
    <section class="section-shell">
      <div class="section-head">
        <div>
          <h1 class="section-title">账户资料</h1>
          <div class="section-copy">展示当前用户在数码销售平台中的基础账户信息。</div>
        </div>
      </div>
      <div class="info-grid">
        <div class="info-cell"><span>用户名</span><strong>{{ d?.username || '--' }}</strong></div>
        <div class="info-cell"><span>手机号</span><strong>{{ d?.phone || '--' }}</strong></div>
        <div class="info-cell"><span>邮箱</span><strong>{{ d?.email || '--' }}</strong></div>
        <div class="info-cell"><span>账户状态</span><strong>{{ d?.status === 1 ? '正常' : '未启用' }}</strong></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getMe } from '@/api/ums'

const d = ref()
onMounted(async () => {
  d.value = (await getMe()).data
})
</script>

<style scoped lang="scss">
.ignore-vw .info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .info-cell {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid $brand-border;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
}

.ignore-vw .info-cell span {
  display: block;
  color: $text-soft;
  font-size: 13px;
}

.ignore-vw .info-cell strong {
  display: block;
  margin-top: 10px;
  color: $brand-ink;
  font-size: 20px;
}

@media (max-width: 760px) {
  .ignore-vw .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
