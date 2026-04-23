import request from '@/utils/request'

export function coupons() {
  return request.get('/api/sms/coupons')
}
export function take(id) {
  return request.post('/api/sms/coupons/' + id + '/take')
}
