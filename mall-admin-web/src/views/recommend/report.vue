<template>
  <div v-loading="loading">
    <el-card shadow="never">
      <template #header>
        <div class="hd">
          <span>推荐效果趋势</span>
          <el-button size="small" @click="load">刷新</el-button>
        </div>
      </template>
      <div ref="chartRef" class="chart" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, shallowRef } from 'vue'
import * as echarts from 'echarts'
import { recommendMetrics } from '@/api/admin'

const chartRef = ref()
const chart = shallowRef()
const loading = ref(false)
let ro

async function load() {
  loading.value = true
  try {
    const t = await recommendMetrics(7)
    const daily = t.data?.daily || []
    chart.value = chart.value || echarts.init(chartRef.value)
    chart.value.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['曝光', '点击', 'CTR(%)'] },
      xAxis: { type: 'category', data: daily.map((x) => x.day) },
      yAxis: [{ type: 'value' }, { type: 'value', axisLabel: { formatter: '{value}%' } }],
      series: [
        { name: '曝光', type: 'bar', data: daily.map((x) => x.expose) },
        { name: '点击', type: 'bar', data: daily.map((x) => x.click) },
        { name: 'CTR(%)', type: 'line', yAxisIndex: 1, smooth: true, data: daily.map((x) => Number((x.ctr || 0) * 100).toFixed(2)) },
      ],
    })
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await load()
  ro = new ResizeObserver(() => chart.value && chart.value.resize())
  ro.observe(chartRef.value)
})

onBeforeUnmount(() => {
  ro && ro.disconnect()
  chart.value && chart.value.dispose()
})
</script>

<style scoped>
.chart {
  height: 380px;
}
.hd {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
