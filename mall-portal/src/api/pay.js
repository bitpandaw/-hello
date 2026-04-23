import request from '@/utils/request'

export function mockPay(orderId) {
  return request.post('/api/pay/mock/' + orderId)
}
