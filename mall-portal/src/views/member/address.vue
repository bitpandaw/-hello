<template>
  <div class="page ignore-vw">
    <section class="section-shell">
      <div class="section-head">
        <div>
          <h1 class="section-title">收货地址</h1>
          <div class="section-copy">管理配送地址信息，服务于确认订单与支付流程展示。</div>
        </div>
      </div>

      <el-skeleton v-if="ld" :rows="3" />
      <div v-else class="addr-grid">
        <article v-for="a in list" :key="a.id" class="addr-card">
          <strong>{{ a.name }} {{ a.phone }}</strong>
          <p>{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}</p>
        </article>
      </div>
    </section>

    <section class="section-shell">
      <div class="section-head">
        <div>
          <h2 class="section-title">新增地址</h2>
          <div class="section-copy">补充新的配送地址，用于下单流程演示。</div>
        </div>
      </div>
      <el-form :model="form" label-width="88px" class="form-grid">
        <el-form-item label="省份"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="区县"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="详细地址" class="wide"><el-input v-model="form.detail" type="textarea" /></el-form-item>
      </el-form>
      <el-button type="primary" :loading="sav" @click="save">保存新地址</el-button>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listAddress, saveAddress } from '@/api/ums'

const list = ref([])
const ld = ref(true)
const sav = ref(false)
const form = reactive({
  province: '',
  city: '',
  district: '',
  detail: '',
  name: '',
  phone: '',
})

onMounted(async () => {
  list.value = (await listAddress()).data || []
  ld.value = false
})

async function save() {
  sav.value = true
  try {
    await saveAddress(form)
    ElMessage.success('地址已保存')
    list.value = (await listAddress()).data || []
  } finally {
    sav.value = false
  }
}
</script>

<style scoped lang="scss">
.ignore-vw .addr-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ignore-vw .addr-card {
  padding: 18px;
  border-radius: 18px;
  border: 1px solid $brand-border;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
}

.ignore-vw .addr-card strong {
  display: block;
  color: $brand-ink;
  font-size: 17px;
}

.ignore-vw .addr-card p {
  margin: 10px 0 0;
  color: $text-sub;
  font-size: 14px;
  line-height: 1.8;
}

.ignore-vw .form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 18px;
}

.ignore-vw .wide {
  grid-column: 1 / -1;
}

@media (max-width: 760px) {
  .ignore-vw .addr-grid,
  .ignore-vw .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
