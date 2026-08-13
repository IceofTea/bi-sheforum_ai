import axios from 'axios'
import { ElNotification } from 'element-plus'

axios.defaults.baseURL = import.meta.env.VITE_SERVER

axios.interceptors.request.use(function (config) {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, function (error) {
  return Promise.reject(error)
})

axios.interceptors.response.use(function (response) {
  return response.data
}, function (error) {
  const { config: { url } = {}, response: { status } = {} } = error
  if (!status) {
    ElNotification.error({ title: '网络错误', message: '无法连接到服务器' })
    return Promise.reject(error)
  }
  const messages = {
    401: '登录已过期，请重新登录',
    404: `API接口 ${url} 不存在`,
    500: `服务器错误: ${url}`
  }
  ElNotification.error({
    title: `错误 ${status}`,
    message: messages[status] || `请求 ${url} 失败`
  })
  if (status === 401) {
    sessionStorage.clear()
    const { fullPath } = { fullPath: '/' }
    window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
  }
  return Promise.reject(error)
})
