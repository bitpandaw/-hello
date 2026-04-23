<template>
  <div v-loading="ld">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-statistic title="今日订单" :value="s.todayOrderCount" />
      </el-col>
      <el-col :span="8">
        <el-statistic title="今日 GMV" :value="Number(s.todayGmv || 0)" :precision="2" />
      </el-col>
      <el-col :span="8">
        <el-statistic title="今日新用户" :value="s.todayNewUser" />
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 24px">
      <el-col :span="14">
        <div ref="gmvRef" class="chart" />
      </el-col>
      <el-col :span="10">
        <div ref="topRef" class="chart" />
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { onMounted, onBeforeUnmount, ref, reactive, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { getStats, getCharts } from '@/api/admin'
const s = reactive({ todayOrderCount: 0, todayGmv: 0, todayNewUser: 0 })
const ld = ref(true)
const gmvRef = ref()
const topRef = ref()
const gChart = shallowRef()
const tChart = shallowRef()
let ro
onMounted(async () => {
  const [a, c] = await Promise.all([getStats(), getCharts()])
  Object.assign(s, a.data || {})
  const gmv = c.data?.gmvLast7Days || []
  const top = c.data?.top10Sku || []
  gChart.value = echarts.init(gmvRef.value)
  tChart.value = echarts.init(topRef.value)
  gChart.value.setOption({
    title: { text: '近7日 GMV' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: gmv.map((x) => x.day) },
    yAxis: { type: 'value' },
    series: [{ data: gmv.map((x) => x.gmv), type: 'line', smooth: true }],
  })
  tChart.value.setOption({
    title: { text: 'SPU 销量 TOP10' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: top.map((x) => x.name), inverse: true },
    series: [{ type: 'bar', data: top.map((x) => x.qty) }],
  })
  ro = new ResizeObserver(() => {
    gChart.value && gChart.value.resize()
    tChart.value && tChart.value.resize()
  })
  ro.observe(gmvRef.value)
  ro.observe(topRef.value)
  ld.value = false
})
onBeforeUnmount(() => {
  ro && ro.disconnect()
  gChart.value && gChart.value.dispose()
  tChart.value && tChart.value.dispose()
})
</script>
<style scoped>
.chart {
  height: 360px;
  width: 100%;
  min-width: 0;
}
</style>
