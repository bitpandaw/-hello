function isDemoRemoteImage(url) {
  return /picsum\.photos|via\.placeholder\.com/i.test(url)
}

function isDemoLocalImage(url) {
  return /\/demo-images\//i.test(String(url || ''))
}

const REAL_IMAGE_MAP = {
  phone: ['/real-products/phone.jpg', '/real-products/phone-alt.jpg'],
  laptop: ['/real-products/laptop.jpg'],
  tablet: ['/real-products/tablet.jpg'],
  watch: ['/real-products/watch.jpg'],
  earbuds: ['/real-products/earbuds.jpg'],
  camera: ['/real-products/camera.jpg'],
  powerbank: ['/real-products/powerbank.jpg'],
  generic: ['/real-products/laptop.jpg', '/real-products/tablet.jpg', '/real-products/phone-alt.jpg'],
}

function extractNumber(text) {
  const match = String(text || '').match(/(\d+)/)
  return match ? Number(match[1]) : 0
}

function pickFrom(list, label) {
  const index = extractNumber(label)
  return list[index % list.length]
}

function pickByLabel(label) {
  const text = String(label || '').toLowerCase()
  if (!text) {
    return REAL_IMAGE_MAP.generic[0]
  }
  if (/手机|phone|iphone|mate|nova/.test(text)) {
    return pickFrom(REAL_IMAGE_MAP.phone, text)
  }
  if (/笔记本|laptop|macbook|aerobook/.test(text)) {
    return REAL_IMAGE_MAP.laptop[0]
  }
  if (/平板|tablet|ipad|visionpad|display/.test(text)) {
    return REAL_IMAGE_MAP.tablet[0]
  }
  if (/手表|watch|腕表|pulsewatch/.test(text)) {
    return REAL_IMAGE_MAP.watch[0]
  }
  if (/耳机|earbuds|airpods|蓝牙|sonicbuds/.test(text)) {
    return REAL_IMAGE_MAP.earbuds[0]
  }
  if (/相机|camera|镜头|pixelcam/.test(text)) {
    return REAL_IMAGE_MAP.camera[0]
  }
  if (/电源|储能|powerstation|powerbank/.test(text)) {
    return REAL_IMAGE_MAP.powerbank[0]
  }
  return pickFrom(REAL_IMAGE_MAP.generic, text)
}

export function resolveProductImage(src, label) {
  if (!src || isDemoRemoteImage(src) || isDemoLocalImage(src)) {
    return pickByLabel(label)
  }
  return src
}
