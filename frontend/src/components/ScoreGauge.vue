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
      title: { show: false },
      detail: { show: false },
      data: [{ value: props.score }],
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
  <div class="score-gauge" role="img" :aria-label="`市场机会评分 ${score} 分`">
    <div ref="chartElement" class="score-gauge__chart" aria-hidden="true"></div>
    <div class="score-gauge__value" aria-hidden="true">
      <strong>{{ score }}</strong>
      <span>市场机会 / 100</span>
    </div>
  </div>
</template>

<style scoped>
.score-gauge {
  position: relative;
  width: 100%;
  height: 230px;
}

.score-gauge__chart {
  width: 100%;
  height: 100%;
}

.score-gauge__value {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  padding-top: 8px;
  pointer-events: none;
}

.score-gauge__value strong {
  color: #f8fafc;
  font-size: clamp(40px, 4vw, 48px);
  font-weight: 750;
  line-height: .95;
  letter-spacing: -.045em;
}

.score-gauge__value span {
  color: #94a3b8;
  font-size: 12px;
  line-height: 1.4;
}
</style>
