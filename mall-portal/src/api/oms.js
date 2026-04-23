import request from '@/utils/request'

export function getCart() {
  return request.get('/api/oms/cart')
}
export function addCart(skuId, quantity) {
  return request.post('/api/oms/cart/' + skuId, null, { params: { quantity } })
}
export function setQty(skuId, quantity) {
  return request.put('/api/oms/cart/' + skuId, null, { params: { quantity } })
}
export function delCart(skuId) {
  return request.delete('/api/oms/cart/' + skuId)
}
export function selectCart(skuId, on) {
  return request.post('/api/oms/cart/' + skuId + '/select', null, { params: { on } })
}
export function previewOrder(data) {
  return request.post('/api/oms/orders/preview', data)
}
export function createOrder(data) {
  return request.post('/api/oms/orders', data)
}
export function myOrders(params) {
  return request.get('/api/oms/orders', { params })
}
export function orderDetail(id) {
  return request.get('/api/oms/orders/' + id)
}
