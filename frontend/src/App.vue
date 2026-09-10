<script setup>
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import {
  ElAlert,
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElProgress,
  ElTag,
} from 'element-plus'
import { createAnalysis, getAnalysis, getHealth } from './api/analysis'

const ScoreGauge = defineAsyncComponent(() => import('./components/ScoreGauge.vue'))

const agentDefinitions = [
  { type: 'MARKET_TREND', name: '市场趋势分析', description: '识别需求增长和市场窗口' },
  { type: 'COMPETITOR', name: '竞品格局分析', description: '评估价格带与差异化空间' },
  { type: 'CUSTOMER_INSIGHT', name: '用户评论洞察', description: '提取痛点、偏好和购买动机' },
  { type: 'STRATEGY', name: '商业策略生成', description: '综合生成市场进入方案' },
]

const formRef = ref()
const form = reactive({
  product: 'Portable Blender',
  market: 'USA Amazon',
})
const rules = {
  product: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  market: [{ required: true, message: '请输入目标市场', trigger: 'blur' }],
}

const view = ref('home')
const submitting = ref(false)
const analysisId = ref('')
const analysis = ref(null)
const workflowError = ref('')
const aiRuntime = ref(null)
let pollTimer
let consecutivePollErrors = 0

const displayTasks = computed(() => agentDefinitions.map((definition) => {
  const liveTask = analysis.value?.agentTasks?.find((task) => task.agentType === definition.type)
  return { ...definition, status: liveTask?.status || 'PENDING', result: liveTask?.result || null }
}))

const progressPercent = computed(() => {
  const completed = displayTasks.value.filter((task) => task.status === 'COMPLETED').length
  const running = displayTasks.value.some((task) => task.status === 'RUNNING') ? 0.5 : 0
  return Math.round(((completed + running) / agentDefinitions.length) * 100)
})

const currentAgent = computed(() => (
  displayTasks.value.find((task) => task.status === 'RUNNING')
  || displayTasks.value.find((task) => task.status === 'PENDING')
))

async function startAnalysis() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  clearPollTimer()
  submitting.value = true
  workflowError.value = ''
  analysis.value = null
  try {
    const task = await createAnalysis({
      product: form.product.trim(),
      market: form.market.trim(),
    })
    analysisId.value = task.analysisId
    view.value = 'analyzing'
    await pollAnalysis()
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '无法创建分析任务，请确认后端服务已启动'))
  } finally {
    submitting.value = false
  }
}

async function pollAnalysis() {
  if (!analysisId.value || view.value !== 'analyzing') return
  try {
    analysis.value = await getAnalysis(analysisId.value)
    consecutivePollErrors = 0
    if (analysis.value.status === 'COMPLETED') {
      pollTimer = window.setTimeout(() => {
        view.value = 'report'
      }, 450)
      return
    }
    if (analysis.value.status === 'FAILED') {
      workflowError.value = 'Agent 分析执行失败，请返回首页重试或检查后端日志。'
      return
    }
  } catch (error) {
    consecutivePollErrors += 1
    if (consecutivePollErrors >= 4) {
      workflowError.value = resolveErrorMessage(error, '暂时无法获取分析进度')
      return
    }
  }
  pollTimer = window.setTimeout(pollAnalysis, 600)
}

function resetAnalysis() {
  clearPollTimer()
  analysisId.value = ''
  analysis.value = null
  workflowError.value = ''
  consecutivePollErrors = 0
  view.value = 'home'
}

function resumePolling() {
  workflowError.value = ''
  consecutivePollErrors = 0
  pollAnalysis()
}

function exportPdf() {
  window.print()
}

function clearPollTimer() {
  if (pollTimer) window.clearTimeout(pollTimer)
  pollTimer = undefined
}

function resolveErrorMessage(error, fallback) {
  return error?.response?.data?.message || error?.message || fallback
}

function taskStatusLabel(status) {
  return {
    PENDING: '等待中',
    RUNNING: '分析中',
    COMPLETED: '已完成',
    FAILED: '失败',
  }[status] || status
}

function taskStatusClass(status) {
  return status.toLowerCase()
}

function taskResultSummary(task) {
  const result = task.result
  if (!result) return ''
  return result.summary || result.strategy || result.marketingSuggestion || ''
}

async function loadRuntime() {
  try {
    aiRuntime.value = (await getHealth()).ai
  } catch {
    aiRuntime.value = null
  }
}

onMounted(loadRuntime)
onBeforeUnmount(clearPollTimer)
</script>

<template>
  <div class="app-shell">
    <header class="topbar no-print">
      <button class="brand" type="button" aria-label="返回 CrossMind AI 首页" @click="resetAnalysis">
        <span class="brand-mark">CM</span>
        <span class="brand-copy">
          <strong>CrossMind AI</strong>
          <small>Cross-border intelligence</small>
        </span>
      </button>
      <div class="topbar-meta">
        <span class="live-indicator"><i></i>
          {{ aiRuntime?.live ? `${aiRuntime.model} · REAL AI` : 'DEMO SAFE MODE' }}
        </span>
        <ElTag effect="dark" round>Hackathon MVP</ElTag>
      </div>
    </header>

    <main v-if="view === 'home'" class="home-view">
      <section class="hero-copy">
        <div class="eyebrow"><span>AI AGENT WORKFLOW</span><i></i><span>MARKET INTELLIGENCE</span></div>
        <h1>把跨境选品，<br /><span>变成可解释的决策。</span></h1>
        <p class="hero-description">
          输入商品和目标市场，由四个专业 Agent 协作分析趋势、竞品、评论与商业策略，
          在几秒内生成一份可执行的市场进入报告。
        </p>

        <div class="capabilities" aria-label="核心能力">
          <div><strong>4</strong><span>专业 Agent</span></div>
          <div><strong>7</strong><span>决策维度</span></div>
          <div><strong>1</strong><span>进入策略</span></div>
        </div>
      </section>

      <ElCard class="analysis-form-card glass-card" shadow="never">
        <div class="form-heading">
          <span class="section-index">01</span>
          <div>
            <h2>创建市场分析</h2>
            <p>当前 Demo 已准备 Portable Blender 测试数据</p>
          </div>
        </div>

        <ElForm ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent="startAnalysis">
          <ElFormItem label="商品名称" prop="product">
            <ElInput v-model="form.product" placeholder="例如：Portable Blender" clearable />
          </ElFormItem>
          <ElFormItem label="目标市场" prop="market">
            <ElInput v-model="form.market" placeholder="例如：USA Amazon" clearable />
          </ElFormItem>
          <ElButton class="start-button" type="primary" native-type="submit" :loading="submitting">
            <span>{{ submitting ? '正在创建任务' : '开始 AI 分析' }}</span>
            <span v-if="!submitting" aria-hidden="true">→</span>
          </ElButton>
        </ElForm>

        <div class="form-footnote">
          <span>无需 API Key 即可运行</span>
          <span>·</span>
          <span>支持阿里云百炼</span>
        </div>
      </ElCard>
    </main>

    <main v-else-if="view === 'analyzing'" class="analysis-view">
      <section class="analysis-heading">
        <p class="eyebrow-simple">MULTI-AGENT ANALYSIS</p>
        <h1>AI 正在构建市场进入策略</h1>
        <p>{{ form.product }} · {{ form.market }}</p>
        <div class="task-id">TASK {{ analysisId.slice(0, 8).toUpperCase() }}</div>
      </section>

      <section class="workflow-layout">
        <ElCard class="workflow-card glass-card" shadow="never">
          <div class="workflow-progress">
            <div>
              <span>整体进度</span>
              <strong>{{ progressPercent }}%</strong>
            </div>
            <ElProgress :percentage="progressPercent" :show-text="false" :stroke-width="8" />
          </div>

          <div class="agent-list">
            <article v-for="(task, index) in displayTasks" :key="task.type" class="agent-task" :class="taskStatusClass(task.status)">
              <div class="task-number">0{{ index + 1 }}</div>
              <div class="task-status-icon" aria-hidden="true">
                <span v-if="task.status === 'COMPLETED'">✓</span>
                <span v-else-if="task.status === 'FAILED'">!</span>
                <i v-else-if="task.status === 'RUNNING'"></i>
                <span v-else>·</span>
              </div>
              <div class="task-copy">
                <strong>{{ task.name }} Agent</strong>
                <span>{{ task.description }}</span>
                <small v-if="taskResultSummary(task)" class="task-result">{{ taskResultSummary(task) }}</small>
              </div>
              <span class="task-status">{{ taskStatusLabel(task.status) }}</span>
            </article>
          </div>
        </ElCard>

        <aside class="agent-console">
          <div class="console-glow"></div>
          <p>ACTIVE AGENT</p>
          <template v-if="currentAgent">
            <div class="orb"><span></span><i></i></div>
            <h2>{{ currentAgent.name }}</h2>
            <span>{{ currentAgent.description }}</span>
          </template>
          <template v-else>
            <div class="orb complete"><span>✓</span></div>
            <h2>报告生成完成</h2>
            <span>正在整理可视化结果</span>
          </template>
          <div class="signal-lines"><i></i><i></i><i></i><i></i><i></i></div>
        </aside>
      </section>

      <ElAlert v-if="workflowError" class="workflow-error" type="error" :title="workflowError" show-icon :closable="false">
        <template #default>
          <div class="error-actions">
            <ElButton size="small" @click="resumePolling">重新连接</ElButton>
            <ElButton size="small" text @click="resetAnalysis">返回首页</ElButton>
          </div>
        </template>
      </ElAlert>
    </main>

    <main v-else class="report-view">
      <section class="report-header">
        <div>
          <p class="eyebrow-simple">MARKET ENTRY REPORT</p>
          <h1>{{ analysis.product }}</h1>
          <p>{{ analysis.market }} · AI 商品市场进入分析报告</p>
        </div>
        <div class="report-actions no-print">
          <ElButton @click="resetAnalysis">重新分析</ElButton>
          <ElButton type="primary" @click="exportPdf">导出 PDF</ElButton>
        </div>
      </section>

      <section class="report-grid report-summary">
        <ElCard class="score-card glass-card" shadow="never">
          <ScoreGauge :score="analysis.score" />
          <ElTag type="success" effect="dark" round>建议差异化进入</ElTag>
        </ElCard>

        <div class="summary-stack">
          <ElCard class="metric-card glass-card" shadow="never">
            <span>市场趋势</span>
            <strong>{{ analysis.trend }}</strong>
            <small>搜索需求与市场规模保持正向增长</small>
          </ElCard>
          <ElCard class="metric-card glass-card" shadow="never">
            <span>竞争程度</span>
            <strong>{{ analysis.competition }}</strong>
            <small>成熟品牌存在，功能差异化仍有窗口</small>
          </ElCard>
        </div>

        <ElCard class="strategy-card glass-card" shadow="never">
          <span class="section-label">核心进入策略</span>
          <blockquote>“{{ analysis.strategy }}”</blockquote>
          <div class="strategy-meta">
            <span>AI 综合建议</span>
            <span>{{ analysis.score }}/100 CONFIDENCE</span>
          </div>
        </ElCard>
      </section>

      <section class="report-section">
        <div class="section-heading">
          <span class="section-index">02</span>
          <div><h2>用户痛点</h2><p>来自评论语义与高频问题识别</p></div>
        </div>
        <div class="pain-point-grid">
          <article v-for="(painPoint, index) in analysis.painPoints" :key="painPoint">
            <span>0{{ index + 1 }}</span>
            <p>{{ painPoint }}</p>
          </article>
        </div>
      </section>

      <section class="report-grid recommendation-grid">
        <ElCard class="recommendation-card glass-card" shadow="never">
          <span class="card-kicker">PRODUCT</span>
          <h2>产品改进建议</h2>
          <ul>
            <li v-for="suggestion in analysis.productSuggestions" :key="suggestion">{{ suggestion }}</li>
          </ul>
        </ElCard>
        <ElCard class="recommendation-card pricing-card glass-card" shadow="never">
          <span class="card-kicker">PRICING</span>
          <h2>定价建议</h2>
          <strong>{{ analysis.pricingSuggestion }}</strong>
          <p>定位中端功能升级款，避免陷入低价竞争。</p>
        </ElCard>
        <ElCard class="recommendation-card marketing-card glass-card" shadow="never">
          <span class="card-kicker">MARKETING</span>
          <h2>营销建议</h2>
          <p>{{ analysis.marketingSuggestion }}</p>
          <div class="channel-tags"><span>TRAVEL</span><span>FITNESS</span><span>OFFICE</span></div>
        </ElCard>
      </section>

      <footer class="report-footer">
        <span>CrossMind AI · Agent-generated market intelligence</span>
        <span>Analysis {{ analysis.analysisId.slice(0, 8).toUpperCase() }}</span>
      </footer>
    </main>
  </div>
</template>

<style scoped>
.app-shell { width: min(1440px, 100%); min-height: 100vh; margin: 0 auto; padding: 0 5vw; }
.topbar { height: 84px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border); }
.brand { display: flex; align-items: center; gap: 12px; padding: 0; color: inherit; background: none; border: 0; cursor: pointer; text-align: left; }
.brand-mark { display: grid; place-items: center; width: 40px; height: 40px; border: 1px solid rgba(129, 140, 248, .42); border-radius: 13px; color: white; font-size: 12px; font-weight: 800; background: linear-gradient(145deg, #7c3aed, #2563eb); box-shadow: 0 8px 30px rgba(99, 102, 241, .32); }
.brand-copy { display: grid; gap: 2px; }.brand-copy strong { font-size: 15px; }.brand-copy small { color: var(--muted); font-size: 10px; letter-spacing: .08em; text-transform: uppercase; }
.topbar-meta, .live-indicator { display: flex; align-items: center; gap: 18px; }.live-indicator { gap: 8px; color: #94a3b8; font-size: 11px; letter-spacing: .08em; }.live-indicator i { width: 7px; height: 7px; border-radius: 50%; background: #34d399; box-shadow: 0 0 12px #34d399; }
.home-view { min-height: calc(100vh - 85px); display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(390px, .72fr); align-items: center; gap: clamp(48px, 8vw, 120px); padding: 70px 0; }
.eyebrow { display: flex; align-items: center; gap: 12px; color: #818cf8; font-size: 10px; font-weight: 700; letter-spacing: .18em; }.eyebrow i { width: 28px; height: 1px; background: #475569; }
.hero-copy h1 { margin: 24px 0; font-size: clamp(46px, 5.2vw, 78px); line-height: 1.08; letter-spacing: -.055em; }.hero-copy h1 span { color: transparent; background: linear-gradient(90deg, #a78bfa 0%, #60a5fa 52%, #22d3ee 100%); background-clip: text; }
.hero-description { max-width: 700px; color: var(--muted); font-size: 17px; line-height: 1.9; }
.capabilities { display: flex; gap: clamp(28px, 4vw, 64px); margin-top: 46px; }.capabilities div { display: grid; gap: 4px; }.capabilities strong { font-size: 25px; }.capabilities span { color: var(--muted); font-size: 11px; letter-spacing: .06em; }
.glass-card { border: 1px solid var(--border); border-radius: 22px; background: linear-gradient(145deg, rgba(15, 23, 42, .86), rgba(9, 14, 28, .74)); box-shadow: 0 28px 90px rgba(2, 6, 23, .32); backdrop-filter: blur(20px); }
.analysis-form-card { position: relative; overflow: visible; }.analysis-form-card::before { content: ''; position: absolute; inset: -1px; z-index: -1; border-radius: 23px; background: linear-gradient(135deg, rgba(139, 92, 246, .45), transparent 36%, rgba(34, 211, 238, .2)); filter: blur(9px); opacity: .65; }
.form-heading, .section-heading { display: flex; gap: 16px; margin-bottom: 28px; }.section-index { flex: none; color: #818cf8; font: 12px/1.5 ui-monospace, monospace; }.form-heading h2, .section-heading h2 { margin: 0 0 5px; font-size: 20px; }.form-heading p, .section-heading p { margin: 0; color: var(--muted); font-size: 12px; }
.start-button { width: 100%; height: 50px; margin-top: 4px; border: 0; background: linear-gradient(100deg, #7c3aed, #2563eb 65%, #0891b2); box-shadow: 0 12px 34px rgba(79, 70, 229, .26); }.start-button span { display: flex; justify-content: space-between; width: 100%; }
.form-footnote { display: flex; justify-content: center; gap: 9px; margin-top: 22px; color: #64748b; font-size: 10px; letter-spacing: .04em; }
.analysis-view { min-height: calc(100vh - 85px); padding: 64px 0 90px; }.analysis-heading { text-align: center; }.eyebrow-simple { margin: 0 0 13px; color: #818cf8 !important; font-size: 10px !important; font-weight: 750; letter-spacing: .2em; }.analysis-heading h1, .report-header h1 { margin: 0; font-size: clamp(34px, 4vw, 54px); letter-spacing: -.035em; }.analysis-heading > p { color: var(--muted); font-size: 14px; }.task-id { display: inline-block; margin-top: 8px; padding: 7px 12px; border: 1px solid var(--border); border-radius: 99px; color: #64748b; font: 10px ui-monospace, monospace; letter-spacing: .12em; }
.workflow-layout { display: grid; grid-template-columns: minmax(0, 1.25fr) minmax(300px, .6fr); gap: 24px; max-width: 1050px; margin: 46px auto 0; }.workflow-progress > div { display: flex; justify-content: space-between; margin-bottom: 12px; color: var(--muted); font-size: 12px; }.workflow-progress strong { color: #c4b5fd; font: 13px ui-monospace, monospace; }
.agent-list { margin-top: 24px; }.agent-task { display: grid; grid-template-columns: 34px 34px 1fr auto; gap: 13px; align-items: center; min-height: 78px; padding: 13px 4px; border-top: 1px solid rgba(148, 163, 184, .09); opacity: .48; transition: .3s ease; }.agent-task.running, .agent-task.completed { opacity: 1; }.task-number { color: #475569; font: 10px ui-monospace, monospace; }.task-status-icon { display: grid; place-items: center; width: 30px; height: 30px; border: 1px solid #334155; border-radius: 10px; color: #64748b; }.task-status-icon i { width: 9px; height: 9px; border-radius: 50%; background: #818cf8; box-shadow: 0 0 15px #6366f1; animation: pulse 1.2s infinite; }.completed .task-status-icon { color: #34d399; border-color: rgba(52, 211, 153, .35); background: rgba(16, 185, 129, .08); }.failed .task-status-icon { color: #fb7185; border-color: rgba(251, 113, 133, .35); }.task-copy { display: grid; gap: 5px; min-width: 0; }.task-copy strong { font-size: 14px; }.task-copy span { color: var(--muted); font-size: 11px; }.task-result { overflow: hidden; color: #c4b5fd; font-size: 11px; line-height: 1.5; text-overflow: ellipsis; white-space: nowrap; }.task-status { color: #64748b; font-size: 10px; }.running .task-status { color: #a78bfa; }.completed .task-status { color: #34d399; }
.agent-console { position: relative; display: flex; min-height: 410px; flex-direction: column; align-items: center; justify-content: center; overflow: hidden; border: 1px solid rgba(99, 102, 241, .18); border-radius: 22px; background: rgba(7, 11, 23, .76); text-align: center; }.console-glow { position: absolute; width: 240px; height: 240px; border-radius: 50%; background: rgba(79, 70, 229, .14); filter: blur(54px); }.agent-console > p { color: #64748b; font-size: 9px; letter-spacing: .2em; }.agent-console h2 { margin: 22px 0 8px; font-size: 18px; }.agent-console > span { max-width: 220px; color: var(--muted); font-size: 11px; line-height: 1.6; }.orb { position: relative; display: grid; place-items: center; width: 102px; height: 102px; margin-top: 20px; border: 1px solid rgba(129, 140, 248, .26); border-radius: 50%; background: radial-gradient(circle, rgba(99, 102, 241, .35), rgba(30, 41, 59, .08) 65%); box-shadow: 0 0 48px rgba(99, 102, 241, .24); }.orb::before, .orb::after { content: ''; position: absolute; inset: -11px; border: 1px solid rgba(129, 140, 248, .2); border-radius: 50%; animation: spin 7s linear infinite; border-top-color: #818cf8; }.orb::after { inset: 13px; animation-direction: reverse; animation-duration: 4s; }.orb > span { width: 20px; height: 20px; border-radius: 50%; background: #818cf8; box-shadow: 0 0 28px #6366f1; }.orb.complete > span { display: grid; place-items: center; width: 38px; height: 38px; color: white; background: #10b981; }.signal-lines { display: flex; align-items: center; gap: 4px; height: 30px; margin-top: 24px; }.signal-lines i { width: 2px; height: 8px; background: #6366f1; animation: signal .8s ease-in-out infinite alternate; }.signal-lines i:nth-child(2), .signal-lines i:nth-child(4) { animation-delay: .2s; }.signal-lines i:nth-child(3) { animation-delay: .4s; }
.workflow-error { max-width: 1050px; margin: 20px auto 0; }.error-actions { margin-top: 10px; }
.report-view { padding: 54px 0 70px; }.report-header { display: flex; align-items: end; justify-content: space-between; gap: 30px; margin-bottom: 36px; }.report-header > div:first-child { min-width: 0; }.report-header h1 { overflow-wrap: anywhere; }.report-header p:last-child { color: var(--muted); }.report-actions { display: flex; gap: 10px; }.report-grid { display: grid; gap: 18px; }.report-summary { grid-template-columns: .8fr .8fr 1.4fr; }.report-summary > * { min-width: 0; }.score-card :deep(.el-card__body) { display: flex; height: 100%; flex-direction: column; align-items: center; justify-content: center; gap: 4px; }.summary-stack { display: grid; gap: 18px; }.metric-card :deep(.el-card__body) { display: flex; height: 100%; flex-direction: column; }.metric-card span, .section-label { color: var(--muted); font-size: 10px; letter-spacing: .12em; text-transform: uppercase; }.metric-card strong { margin: auto 0 8px; font-size: 30px; }.metric-card small { color: #64748b; line-height: 1.5; }.strategy-card :deep(.el-card__body) { display: flex; height: 100%; flex-direction: column; }.strategy-card blockquote { margin: auto 0; overflow-wrap: anywhere; font-size: clamp(21px, 2.2vw, 30px); font-weight: 650; line-height: 1.55; letter-spacing: -.02em; }.strategy-meta { display: flex; justify-content: space-between; padding-top: 20px; border-top: 1px solid var(--border); color: #64748b; font: 9px ui-monospace, monospace; letter-spacing: .1em; }
.report-section { margin-top: 58px; }.pain-point-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }.pain-point-grid article { min-height: 130px; padding: 20px; border: 1px solid var(--border); border-radius: 17px; background: rgba(15, 23, 42, .52); }.pain-point-grid span { color: #f472b6; font: 10px ui-monospace, monospace; }.pain-point-grid p { margin: 26px 0 0; font-size: 14px; line-height: 1.55; }
.recommendation-grid { grid-template-columns: 1.05fr .8fr 1.15fr; margin-top: 20px; }.recommendation-card { min-height: 260px; }.card-kicker { color: #818cf8; font: 9px ui-monospace, monospace; letter-spacing: .18em; }.recommendation-card h2 { margin: 12px 0 24px; font-size: 20px; }.recommendation-card ul { display: grid; gap: 14px; padding: 0; list-style: none; }.recommendation-card li { position: relative; padding-left: 20px; color: #cbd5e1; font-size: 13px; line-height: 1.6; }.recommendation-card li::before { content: '→'; position: absolute; left: 0; color: #818cf8; }.pricing-card strong { display: block; color: #67e8f9; font-size: 23px; }.recommendation-card p { color: var(--muted); font-size: 13px; line-height: 1.7; }.channel-tags { display: flex; flex-wrap: wrap; gap: 7px; margin-top: 18px; }.channel-tags span { padding: 6px 8px; border: 1px solid rgba(34, 211, 238, .2); border-radius: 6px; color: #67e8f9; font: 8px ui-monospace, monospace; letter-spacing: .1em; }
.report-footer { display: flex; justify-content: space-between; margin-top: 42px; padding-top: 20px; border-top: 1px solid var(--border); color: #475569; font: 9px ui-monospace, monospace; letter-spacing: .09em; }
@keyframes pulse { 50% { transform: scale(.65); opacity: .55; } }@keyframes spin { to { transform: rotate(360deg); } }@keyframes signal { to { height: 24px; opacity: .45; } }
@media (max-width: 980px) { .home-view { grid-template-columns: 1fr; }.analysis-form-card { max-width: 620px; }.workflow-layout, .report-summary { grid-template-columns: 1fr; }.agent-console { min-height: 330px; }.summary-stack { grid-template-columns: 1fr 1fr; }.recommendation-grid { grid-template-columns: 1fr 1fr; }.marketing-card { grid-column: 1 / -1; }.pain-point-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 640px) { .app-shell { padding: 0 20px; }.topbar { height: 72px; }.live-indicator { display: none; }.home-view { min-height: auto; padding: 56px 0; }.hero-copy h1 { font-size: 42px; }.hero-description { font-size: 15px; }.capabilities { justify-content: space-between; gap: 12px; }.workflow-layout { margin-top: 30px; }.workflow-card :deep(.el-card__body) { padding: 14px; }.agent-task { grid-template-columns: 26px 30px 1fr; }.task-status { display: none; }.report-header { align-items: flex-start; flex-direction: column; }.summary-stack, .recommendation-grid, .pain-point-grid { grid-template-columns: 1fr; }.marketing-card { grid-column: auto; }.strategy-meta, .report-footer { align-items: flex-start; flex-direction: column; gap: 8px; }.report-actions { width: 100%; }.report-actions .el-button { flex: 1; } }
</style>
