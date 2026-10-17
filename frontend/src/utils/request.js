import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
const request = axios.create({
    baseURL: '/api',
    timeout: 15000,
})

request.interceptors.request.use(
    (config) => {
        // 后台接口用 admin_token
        if (config.url && config.url.startsWith('/admin')) {
            const adminToken = localStorage.getItem('admin_token')
            if (adminToken) {
                config.headers.Authorization = `Bearer ${adminToken}`
            }
        } else {
            const token = localStorage.getItem('token')
            if (token) {
                config.headers.Authorization = `Bearer ${token}`
            }
        }
        return config
    },
    (error) => Promise.reject(error)
)

request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.success === false) {
            // 兜底：HTTP 200 但 code=401
            if (res.code === 401) {
                handleUnauthorized(res.message)
                return Promise.reject(new Error(res.message || '未登录'))
            }
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    },
    (error) => {
        const status = error.response?.status
        const msg = error.response?.data?.message
        if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
            ElMessage.error('请求超时，请检查网络后重试')
            return Promise.reject(error)
        }
        if (status === 401) {
            handleUnauthorized(msg)
        } else if (status === 400) {
            ElMessage.error(msg || '请求参数有误')
        } else if (status === 403) {
            ElMessage.error('没有权限访问')
        } else if (status === 404) {
            ElMessage.error('请求的资源不存在')
        } else if (status >= 500) {
            ElMessage.error('服务器异常，请稍后再试')
        } else {
            ElMessage.error(msg || error.message || '请求失败')
        }

        return Promise.reject(error)
    }
)

// 统一的 401 处理

function handleUnauthorized(msg) {
    const userStore = useUserStore()
    const cartStore = useCartStore()

    const current = router.currentRoute.value.fullPath

    //  按管理员处理
    if (current.startsWith('/admin')) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin')
        ElMessage.warning(msg || '管理员登录已过期')
        if (current !== '/admin/login') {
            router.push('/admin/login')
        }
        return
    }

    // 用户端 401
    userStore.logout()
    cartStore.reset()
    ElMessage.warning(msg || '请先登录')
    if (current !== '/login') {
        router.push({ path: '/login', query: { redirect: current } })
    }
}

export default request