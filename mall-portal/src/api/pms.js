import request from '@/utils/request'

export function categoryTree() {
  return request.get('/api/pms/categories/tree')
}
export function productPage(params) {
  return request.get('/api/pms/products', { params })
}
export function productDetail(id) {
  return request.get('/api/pms/products/' + id)
}
export function search(q) {
  return request.get('/api/pms/search', { params: { q } })
}
export function brands() {
  return request.get('/api/pms/brands')
}
export function commentList(params) {
  return request.get('/api/pms/comment/list', { params })
}
export function addComment(data) {
  return request.post('/api/pms/comment', data)
}
