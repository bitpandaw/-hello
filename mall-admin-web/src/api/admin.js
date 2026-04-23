import request from '@/utils/request'

export function getMenus() {
  return request.get('/admin/menus')
}
export function login(data) {
  return request.post('/admin/auth/login', data)
}
export function getStats() {
  return request.get('/admin/stats')
}
export function getCharts() {
  return request.get('/admin/stats/charts')
}
export function pageProducts(p, s, name) {
  return request.get('/admin/pms/products', { params: { p, s, name } })
}
export function saveProduct(p) {
  return request.post('/admin/pms/products', p)
}
export function pageCategory(p, s) {
  return request.get('/admin/pms/categories', { params: { p, s } })
}
export function saveCategory(c) {
  return request.post('/admin/pms/categories', c)
}
export function pageBrand(p, s) {
  return request.get('/admin/pms/brands', { params: { p, s } })
}
export function saveBrand(b) {
  return request.post('/admin/pms/brands', b)
}
export function pageOms(p, s, status) {
  return request.get('/admin/oms/orders', { params: { p, s, status } })
}
export function shipOrder(id, d) {
  return request.post('/admin/oms/orders/' + id + '/ship', d)
}
export function pageUms(p, s, username) {
  return request.get('/admin/ums/members', { params: { p, s, username } })
}
export function setUmsStatus(id, status) {
  return request.post('/admin/ums/members/' + id + '/status', { status })
}
export function uploadPms(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/pms/file', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}
