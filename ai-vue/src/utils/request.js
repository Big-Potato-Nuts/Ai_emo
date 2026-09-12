import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 请求的前缀
  timeout: 30000, // 请求的超时时间
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    const { data, config } = response
    // 处理业务状态码
    if (data.code === '200') {
        return data.data
    }

    // 判断是否是登录过期 / token失效
    const isAuthError = data.code === '401'
      || data.code === 'A0230'
      || data.code === 'A0301'
      || (data.code === '-1' && /登录|过期|未登录|token|失效|登录状态/i.test(data.msg || data.message || ''))

    if (isAuthError) {
      // 登录/注册接口的 -1 属于业务失败，只提示不跳转
      if (config.url?.includes('/login') || config.url?.includes('/user/add')) {
        ElMessage.error(data.msg || data.message || '操作失败')
        return Promise.reject(new Error(data.msg || data.message || '操作失败'))
      }
      ElMessage.error(data.msg || data.message || '登录过期，请重新登录')

      // 清除登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/auth/login'
      return Promise.reject(new Error(data.msg || data.message || '登录过期，请重新登录'))
    }

    // 其他业务失败：提示错误，并返回 rejected，避免把失败响应当成功数据处理
    ElMessage.error(data.msg || data.message || '操作失败')
    return Promise.reject(new Error(data.msg || data.message || '操作失败'))
  },
  (error) => {
    // 对响应错误做点什么
    const status = error.response?.status
    if (status === 403) {
      ElMessage.error('暂无权限访问该资源')
    } else if (status === 401) {
      ElMessage.error('登录过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/auth/login'
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default service
