<template>
  <div class="page">
    <h3>收货地址</h3>
    <el-skeleton v-if="ld" :rows="2" />
    <el-card v-else v-for="a in list" :key="a.id" class="c">
      <div>{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}</div>
      <div>{{ a.name }} {{ a.phone }}</div>
    </el-card>
    <el-form :model="form" label-width="1.6rem" class="ignore-vw" style="margin-top: 0.24rem">
      <el-form-item label="省"><el-input v-model="form.province" /></el-form-item>
      <el-form-item label="市"><el-input v-model="form.city" /></el-form-item>
      <el-form-item label="区"><el-input v-model="form.district" /></el-form-item>
      <el-form-item label="详细"><el-input v-model="form.detail" type="textarea" /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
      <el-button type="primary" :loading="sav" @click="save">保存新地址</el-button>
    </el-form>
  </div>
</template>
<script setup>
import { reactive, ref, onMounted } from 'vue'
import { listAddress, saveAddress } from '@/api/ums'
import { ElMessage } from 'element-plus'
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
    ElMessage.success('已保存')
    list.value = (await listAddress()).data || []
  } finally {
    sav.value = false
  }
}
</script>
<style scoped>
.c {
  margin-bottom: 0.16rem;
}
</style>
