import request from '@/utils/request'

// 用户相关
export const userApi = {
  login: (data) => request.post('/user/login', data),
  register: (data) => request.post('/user/register', data),
  getUserInfo: () => request.get('/user/info'),
  logout: () => request.post('/user/logout'),
}

// 商品相关
export const productApi = {
  getList: (params) => request.get('/products', { params }),
  getDetail: (id) => request.get(`/products/${id}`),
  getCategories: () => request.get('/categories'),
  getBanners: () => request.get('/banners'),
  getHot: () => request.get('/products/hot'),
  getNew: () => request.get('/products/new'),
}

// 搜索
export const searchApi = {
  search: (keyword) => request.get('/search', { params: { keyword } }),
}
