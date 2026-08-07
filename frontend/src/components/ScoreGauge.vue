<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts/core'
import { GaugeChart } from 'echarts/charts'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([GaugeChart, CanvasRenderer])

const props = defineProps({
  score: {
    type: Number,
    default: 0,
  },
})

const chartElement = ref(null)
let chart

const option = computed(() => ({
  series: [
    {
      type: 'gauge',
      startAngle: 210,
      endAngle: -30,
      min: 0,
      max: 100,
      radius: '94%',
      pointer: { show: false },
      progress: {
        show: true,
        width: 15,
        roundCap: true,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 1,
            y2: 0,
            colorStops: [
              { offset: 0, color: '#8b5cf6' },
              { offset: 1, color: '#22d3ee' },
            ],
          },
        },
      },
      axisLine: {
        lineStyle: {
          width: 15,
          color: [[1, 'rgba(148, 163, 184, .13)']],
        },
      },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      anchor: { show: false },
      title: {
        offsetCenter: [0, '34%'],
        color: '#94a3b8',
        fontSize: 13,
      },
      detail: {
        valueAnimation: true,
        offsetCenter: [0, '-4%'],
        color: '#f8fafc',
        fontSize: 46,
        fontWeight: 700,
        formatter: '{value}',
      },
      data: [{ value: props.score, name: '市场机会 / 100' }],
    },
  ],
}))

function renderChart() {
  if (!chartElement.value) return
  chart ||= echarts.init(chartElement.value)
  chart.setOption(option.value)
}

function resizeChart() {
  chart?.resize()
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', resizeChart)
})

watch(() => props.score, renderChart)

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
})
</script>

<template>
  <div ref="chartElement" class="score-gauge" role="img" :aria-label="`市场机会评分 ${score} 分`"></div>
</template>

<style scoped>
.score-gauge {
  width: 100%;
  height: 245px;
}
</style>

