<template>
  <div class="p-4">
    <el-card>
      <div class="mb-3 flex items-center">
        <el-button type="primary" @click="doSign" :loading="loading">一键签到</el-button>
        <el-button class="ml-2" @click="locate" :loading="locLoading">重新定位</el-button>
        <span class="ml-3">范围：{{ radius }} 米（中心：{{ center }}）</span>
      </div>
      <div id="map" style="height: 420px;"></div>
      <div class="mt-3 text-sm">
        定位：{{ pos.lng }}, {{ pos.lat }} ｜ 距中心：{{ distance }} m ｜ {{ inRange ? '在范围' : '超出范围' }}
      </div>
      <!-- 新增：显示当前位置地址 -->
      <div class="mt-2 text-sm" v-if="currentAddress">
        <strong>当前位置：</strong>{{ currentAddress }}
      </div>
    </el-card>

    <el-card class="mt-4" header="我的签到记录">
      <el-table :data="mine" size="small" border>
        <el-table-column prop="signTime" label="签到时间" width="180" />
        <el-table-column prop="reason" label="地址" />
        <el-table-column prop="longitude" label="经度" width="120" />
        <el-table-column prop="latitude" label="纬度" width="120" />
        <el-table-column prop="inRange" label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.inRange === 1 ? 'success' : 'warning'">
              {{ row.inRange === 1 ? '成功' : '超出范围' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMine, postSign } from '@/service/attendanceGeo'
import { useAppStore } from '@/store'

const store = useAppStore()

const key = import.meta.env.VITE_AMAP_KEY
const security = import.meta.env.VITE_AMAP_SECURITY
const center = import.meta.env.VITE_GEO_CENTER
const radius = Number(import.meta.env.VITE_GEO_RADIUS)

const pos = reactive({ lng: 0, lat: 0 })
const distance = ref(0)
const inRange = ref(false)
const loading = ref(false)
const locLoading = ref(false)
const mine = ref<any[]>([])
const currentAddress = ref('') // 新增：当前位置地址

let map: any = null, fence: any = null, centerMarker: any = null, myMarker: any = null
let geolocation: any = null, geocoder: any = null // 新增：地理编码器
const [lng0, lat0] = (center || '0,0').split(',').map(Number)

function ensureEnv() {
  if (!key || !security || !center || Number.isNaN(radius)) {
    ElMessage.error('地图或围栏配置缺失：请检查 VITE_AMAP_KEY / VITE_AMAP_SECURITY / VITE_GEO_CENTER / VITE_GEO_RADIUS')
    return false
  }
  return true
}

function loadMap() {
  if (!ensureEnv()) return
  // @ts-ignore
  if (window.AMap) { init(); return }
  // @ts-ignore
  window._AMapSecurityConfig = { securityJsCode: security }

  const exists = Array.from(document.getElementsByTagName('script')).some(s => s.src.includes('webapi.amap.com/maps'))
  if (exists) {
    const tryInit = () => {
      // @ts-ignore
      if (window.AMap) init(); else setTimeout(tryInit, 150)
    }
    tryInit()
    return
  }
  const script = document.createElement('script')
  script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}`
  script.onload = init
  script.onerror = () => ElMessage.error('高德地图脚本加载失败')
  document.head.appendChild(script)
}

function init() {
  // @ts-ignore
  map = new AMap.Map('map', { zoom: 16, center: [lng0, lat0] })
  // @ts-ignore
  fence = new AMap.Circle({ center: [lng0, lat0], radius, strokeStyle: 'dashed' })
  map.add(fence)
  // @ts-ignore
  centerMarker = new AMap.Marker({ position: [lng0, lat0], label: { content: '中心点' } })
  map.add(centerMarker)

  // 显式加载插件，新增地理编码插件
  // @ts-ignore
  AMap.plugin(['AMap.GeometryUtil', 'AMap.Geolocation', 'AMap.Geocoder'], () => {
    // @ts-ignore
    geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      useNative: true,
      showCircle: false,
      showButton: false
    })

    // 新增：初始化地理编码器
    // @ts-ignore
    geocoder = new AMap.Geocoder({
      city: "全国", // 城市设为全国，默认："全国"
      radius: 1000 // 范围，默认：500
    })

    map.addControl(geolocation)
    locate()
    refreshMine()
  })
}

/** 兜底：haversine */
function haversine(lng1: number, lat1: number, lng2: number, lat2: number) {
  const toRad = (d: number) => d * Math.PI / 180
  const R = 6378137
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat/2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng/2) ** 2
  return Math.round(R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)))
}

function computeDistance(lng: number, lat: number) {
  // @ts-ignore
  const GU = (window as any).AMap?.GeometryUtil
  if (GU?.distance) {
    // @ts-ignore
    return Math.round(GU.distance([lng, lat], [lng0, lat0]))
  }
  return haversine(lng, lat, lng0, lat0)
}

function updateFenceState() {
  const d = computeDistance(pos.lng, pos.lat)
  distance.value = d
  inRange.value = d <= radius
}

/** 新增：根据经纬度获取地址 */
function getAddressByLngLat(lng: number, lat: number): Promise<string> {
  return new Promise((resolve, reject) => {
    if (!geocoder) {
      reject(new Error('地理编码器未初始化'))
      return
    }

    geocoder.getAddress([lng, lat], (status: string, result: any) => {
      if (status === 'complete' && result.info === 'OK') {
        // 获取地址成功
        const address = result.regeocode.formattedAddress
        resolve(address)
      } else {
        // 获取地址失败
        reject(new Error('获取地址失败: ' + result.info))
      }
    })
  })
}

/** 浏览器原生定位（WGS-84） */
function useBrowserGeoFallback() {
  if (!navigator.geolocation) return Promise.reject(new Error('no browser geo'))
  return new Promise<{lng:number,lat:number}>((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (r) => resolve({ lng: r.coords.longitude, lat: r.coords.latitude }),
      (err) => reject(err),
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 3000 }
    )
  })
}

/** 将 WGS-84 尽量转换为 GCJ-02（在中国大陆范围内会生效） */
async function toGCJ(lng:number, lat:number): Promise<{lng:number,lat:number}> {
  // @ts-ignore
  const A = (window as any).AMap
  if (!A?.convertFrom) return { lng, lat }
  return await new Promise(resolve => {
    A.convertFrom([lng, lat], 'gps', (status:string, result:any) => {
      if (status === 'complete' && result?.locations?.length) {
        const pt = result.locations[0]
        resolve({ lng: pt.lng, lat: pt.lat })
      } else {
        resolve({ lng, lat })
      }
    })
  })
}

async function locate() {
  locLoading.value = true
  currentAddress.value = '' // 清空地址
  try {
    let lng = 0, lat = 0
    let alreadyGCJ = false

    if (geolocation) {
      const p = await new Promise<any>((resolve, reject) => {
        geolocation.getCurrentPosition((status: string, result: any) => {
          if (status === 'complete' && result?.position) resolve(result)
          else reject(result)
        })
      })
      lng = p.position.lng
      lat = p.position.lat
      alreadyGCJ = true // 高德返回 GCJ-02
    } else {
      const p = await useBrowserGeoFallback()     // WGS-84
      lng = p.lng; lat = p.lat
    }

    if (!alreadyGCJ) {
      const gcj = await toGCJ(lng, lat)
      lng = gcj.lng; lat = gcj.lat
    }

    pos.lng = Number(lng)
    pos.lat = Number(lat)
    updateFenceState()

    // 新增：获取当前位置地址
    try {
      const address = await getAddressByLngLat(pos.lng, pos.lat)
      currentAddress.value = address
    } catch (error) {
      console.warn('获取地址失败:', error)
      currentAddress.value = '无法获取详细地址'
    }

    // 自己的位置点
    // @ts-ignore
    if (myMarker) map.remove(myMarker)
    // @ts-ignore
    myMarker = new AMap.Marker({ position: [pos.lng, pos.lat] })
    map.add(myMarker)
    map.setFitView([myMarker, fence])
  } catch (err) {
    console.warn('定位失败：', err)
    ElMessage.warning('尚未获取到定位，请检查浏览器定位权限或点击"重新定位"重试（建议使用 HTTPS 或 localhost）')
  } finally {
    locLoading.value = false
  }
}

async function doSign() {
  if (!pos.lng || !pos.lat) {
    ElMessage.warning('尚未获取到定位，请先点击"重新定位"')
    return
  }

  // ✅ 签到前检查：先计算距离，超出范围直接提示，不发起请求
  const dist = computeDistance(pos.lng, pos.lat)
  const inRangeFlag = dist <= radius

  if (!inRangeFlag) {
    ElMessage.warning(`当前位置距离签到点 ${dist} 米，超出 ${radius} 米范围，请重新定位到有效区域`)
    return
  }

  loading.value = true
  try {
    const res: any = await postSign({
      userId: store?.state?.user?.id,
      longitude: pos.lng,
      latitude: pos.lat,
      coordType: 'gcj02',
      address: currentAddress.value || '未知位置',
      locationDescription: currentAddress.value || '未知位置'
    })

    console.log('🎯 完整响应结构:', res)

    // ✅ 修复：深入探索响应结构，找到真正的数据
    let resp = res

    // 尝试不同的响应结构层级
    if (res?.data?.code !== undefined) {
      // 结构可能是: { data: { code, msg, success, data } }
      resp = res.data
    } else if (res?.data?.data?.code !== undefined) {
      // 结构可能是: { data: { data: { code, msg, success } } }
      resp = res.data.data
    } else if (res?.code !== undefined) {
      // 结构可能是: { code, msg, success, data }
      resp = res
    } else {
      // 如果都不匹配，使用原始响应
      resp = res
    }

    console.log('🎯 探索后的响应:', resp)
    console.log('🎯 resp.code:', resp?.code)
    console.log('🎯 resp.msg:', resp?.msg)
    console.log('🎯 resp.success:', resp?.success)

    const code = resp?.code
    const msg = resp?.msg
    const success = resp?.success

    console.log('🎯 最终解析 - code:', code, 'msg:', msg, 'success:', success)

    // ✅ 修复：主要根据 code 和 success 判断
    if (code === 0 || success === true) {
      console.log('✅ 进入成功分支')
      ElMessage.success('签到成功！')
      refreshMine()

      // ✅ 更新前端状态显示
      distance.value = Math.round(dist)
      inRange.value = true
    } else {
      console.log('❌ 进入失败分支')
      ElMessage.error(msg || '签到失败，请重试')
    }
  } catch (e: any) {
    console.error('签到请求异常:', e)
    ElMessage.error('网络请求失败: ' + (e.message || '请检查网络连接'))
  } finally {
    loading.value = false
  }
}

/** 统一历史记录坐标系到 GCJ-02 再判定有效性 */
async function refreshMine() {
  try {
    const res: any = await getMine({ page: 1, limit: 10 })
    const payload = res?.data ?? res
    const { list = [] } = payload?.data ?? {}

    const normalized:any[] = []
    for (const r of list) {
      const signTime = r.checkTime || r.createTime || r.updateTime
      let lng = Number(r.longitude)
      let lat = Number(r.latitude)

      if (lng && lat) {
        const gcj = await toGCJ(lng, lat)
        lng = gcj.lng; lat = gcj.lat
      }

      let valid = r.inRange
      if (typeof valid === 'undefined' && lng && lat) {
        const d = computeDistance(lng, lat)
        valid = d <= radius ? 1 : 0
      }

      normalized.push({ ...r, longitude: lng, latitude: lat, signTime, inRange: valid })
    }
    mine.value = normalized
  } catch (e) {
    console.error(e)
    mine.value = []
  }
}

onMounted(loadMap)
</script>

<style scoped>
.text-sm { font-size: 12px; color: var(--el-text-color-regular); }
</style>
