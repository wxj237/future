import request from '@/utils/request'

/**
 * ==============================
 *   类型定义
 * ==============================
 */
export interface GeoListParams {
  page?: number
  limit?: number
  // 管理员可选过滤项
  userId?: number
  username?: string

  signDate?: string
  startDate?: string
  endDate?: string
  sortField?: string
  sortOrder?: 'asc' | 'desc' | string
}

/**
 * ==============================
 *   1️⃣ 定位签到
 * ==============================
 * 后端接口：POST /attendance/geo/signin
 * 必需参数：
 *   - userId (Long)
 *   - longitude (Double)
 *   - latitude (Double)
 *   - locationDescription? (String)
 */
export function postSign(data: { userId: number; longitude: number; latitude: number; locationDescription?: string }) {
  return request({
    url: '/attendance/geo/signin',
    method: 'post',
    data
  })
}

/**
 * ==============================
 *   2️⃣ 手动签到
 * ==============================
 * 后端接口：POST /attendance/attendance/signin/manual
 * 必需参数：
 *   - userId (Long)
 *   - location (String)
 */
export function postManualSign(data: { userId: number; location: string }) {
  return request({
    url: '/attendance/attendance/signin/manual',
    method: 'post',
    data
  })
}

/**
 * ==============================
 *   3️⃣ 签退
 * ==============================
 * 后端接口：POST /attendance/attendance/signout
 * 必需参数：
 *   - userId (Long)
 *   - location (String)
 */
export function postSignOut(data: { userId: number; location: string }) {
  return request({
    url: '/attendance/attendance/signout',
    method: 'post',
    data
  })
}

/**
 * ==============================
 *   4️⃣ 今日考勤状态
 * ==============================
 * 后端接口：GET /attendance/attendance/today?userId=xxx
 */
export function getTodayStatus(userId: number) {
  return request({
    url: '/attendance/attendance/today',
    method: 'get',
    params: { userId }
  })
}

/**
 * ==============================
 *   5️⃣ 定位签到记录（分页列表）
 * ==============================
 * 后端接口：GET /attendance/geo/list
 * - 管理员：可传 userId / username
 * - 普通用户：后端自动只返回“我”的数据
 */
export function getGeoList(params: GeoListParams = {}) {
  const defaults = { page: 1, limit: 10 }
  return request({
    url: '/attendance/geo/list',
    method: 'get',
    params: { ...defaults, ...params }
  })
}

/**
 * ==============================
 *   6️⃣ 我的签到记录（语义封装）
 * ==============================
 * 实质同 getGeoList，只是不传 userId/username
 */
export function getMyGeoList(params: Omit<GeoListParams, 'userId' | 'username'> = {}) {
  return getGeoList(params)
}

/**
 * ==============================
 *   7️⃣ 导出定位签到记录
 * ==============================
 * 后端接口：GET /attendance/geo/export
 * 需权限：attendance:geo:export
 * 参数：
 *   - exportScope: 'current' | 'all'
 *   - fileName?: string
 */
export function exportGeo(params: GeoListParams & { exportScope: 'current' | 'all'; fileName?: string }) {
  return request({
    url: '/attendance/geo/export',
    method: 'get',
    params,
    responseType: 'blob'
  }).then((blob: Blob) => {
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${params.fileName || '定位签到数据'}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
  })
}

/**
 * ==============================
 *   兼容旧命名（避免前端改动大）
 * ==============================
 */
export const getMine = getMyGeoList
export const getPage = getGeoList
export const exportCsvApi = exportGeo