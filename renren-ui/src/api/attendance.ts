// src/api/attendance.ts
import http from "@/utils/http";

/** 统一返回结构 */
export type ApiResponse<T = any> = { code: number; msg: string; data: T };

/** 地理相关入参：location 必须是 "lng,lat" */
export interface GeoPayload {
  userId: number;
  location: string;   // ★ 必填，形如 "106.238545,29.998452"（lng,lat）
  address?: string;
  center?: string;    // 可选："lng,lat"
  radius?: number;    // 可选：米
  remark?: string;
}

/** 获取今日考勤状态 */
export function apiGetToday(userId: number) {
  return http({
    method: "GET",
    url: "/attendance/attendance/today",
    params: { userId },
  }) as Promise<ApiResponse>;
}

/** 手动签到（走 query params） */
export function apiSignInManual(payload: GeoPayload) {
  return http({
    method: "POST",
    url: "/attendance/attendance/signin/manual",
    params: payload,           // ★ 关键：用 params，而不是 data
  }) as Promise<ApiResponse>;
}

/** 定位签到（走 query params） */
export function apiSignInLocation(payload: GeoPayload) {
  return http({
    method: "POST",
    url: "/attendance/attendance/signin/location",
    params: payload,           // ★ 关键：用 params，而不是 data
  }) as Promise<ApiResponse>;
}

/** 签退（走 query params） */
export function apiSignOut(payload: GeoPayload) {
  return http({
    method: "POST",
    url: "/attendance/attendance/signout",
    params: payload,           // ★ 关键：用 params，而不是 data
  }) as Promise<ApiResponse>;
}
