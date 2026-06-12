<template>
  <div>
    <el-form inline @submit.prevent="load">
      <el-form-item label="订单号">
        <el-input v-model="orderNo" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="st" clearable placeholder="全部" style="width: 120px" @change="load">
          <el-option :value="0" label="待付款" />
          <el-option :value="1" label="待发货" />
          <el-option :value="2" label="已发货" />
          <el-option :value="3" label="已完成" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
    </el-form>
    <el-table v-loading="ld" :data="rows" border style="width: 100%">
      <el-table-column prop="id" width="80" label="ID" />
      <el-table-column prop="orderNo" min-width="180" label="订单号" />
      <el-table-column prop="payAmount" width="100" label="金额" />
      <el-table-column prop="status" width="80" label="状态" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="primary" text @click="open(row)">发货</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="p"
      v-model:page-size="s"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
    <el-dialog
      v-model="show"
      title="发货"
      width="400px"
      @close="() => { ship.deliveryCompany = ''; ship.deliverySn = '' }"
    >
      <el-form>
        <el-form-item label="公司">
          <el-input v-model="ship.deliveryCompany" />
        </el-form-item>
        <el-form-item label="单号">
          <el-input v-model="ship.deliverySn" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="show = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doShip">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageOms, shipOrder } from '@/api/admin'

const st = ref()
const orderNo = ref('')
const p = ref(1)
const s = ref(10)
const total = ref(0)
const rows = ref([])
const ld = ref(true)
const show = ref(false)
const cur = ref()
const ship = reactive({ deliveryCompany: '', deliverySn: '' })
const saving = ref(false)

async function load() {
  ld.value = true
  const t = await pageOms(p.value, s.value, st.value, orderNo.value || undefined)
  rows.value = t.data?.records || []
  total.value = t.data?.total || 0
  ld.value = false
}

function open(row) {
  cur.value = row
  show.value = true
}

async function doShip() {
  saving.value = true
  try {
    await shipOrder(cur.value.id, ship)
    ElMessage.success('已发货')
    show.value = false
    load()
  } finally {
    saving.value = false
  }
}

load()
</script>

<script>
export default { name: 'OmsOrder' }
</script>
