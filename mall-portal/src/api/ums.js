import request from '@/utils/request'

export function getCaptcha() {
  return request.get('/api/ums/captcha')
}
export function register(data) {
  return request.post('/api/ums/register', data)
}
export function login(data) {
  return request.post('/api/ums/login', data)
}
export function getMe() {
  return request.get('/api/ums/me')
}
export function listAddress() {
  return request.get('/api/ums/address')
}
export function saveAddress(data) {
  return request.post('/api/ums/address', data)
}
export function delAddress(id) {
  return request.delete('/api/ums/address/' + id)
}
