import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export async function createAnalysis(payload) {
  const { data } = await apiClient.post('/api/analysis', payload)
  return data
}

export async function getAnalysis(analysisId) {
  const { data } = await apiClient.get(`/api/analysis/${analysisId}`)
  return data
}

export async function getHealth() {
  const { data } = await apiClient.get('/api/health')
  return data
}
