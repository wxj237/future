<template>
  <div class="p-4">
    <el-card>
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="模糊搜索" clearable />
        </el-form-item>

        <!-- 日期筛选：默认显示全部，可选"指定日期 / 日期范围" -->
        <el-form-item label="日期">
          <el-radio-group v-model="query.dateMode" @change="onDateModeChange">
            <el-radio label="all">全部</el-radio>
            <el-radio label="single">指定日期</el-radio>
            <el-radio label="range">日期范围</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="query.dateMode==='single'">
          <el-date-picker
            v-model="query.signDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            clearable
          />
        </el-form-item>

        <el-form-item v-if="query.dateMode==='range'">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            range-separator="至"
            clearable
          />
        </el-form-item>

        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <!-- 按当前筛选与分页导出 Excel -->
        <el-button type="success" @click="showExportDialog">导出Excel</el-button>
      </el-form>

      <el-table :data="rows" border class="mt-3">
        <el-table-column prop="username" label="用户名" width="120"/>
        <el-table-column prop="deptName" label="部门" width="150"/> <!-- ✅ 新增：部门列 -->

        <!-- 经度（兼容: longitude/lng/lon/x） -->
        <el-table-column label="经度" width="120">
          <template #default="{ row }">
            {{ pick(row, ['longitude','lng','lon','x']) ?? '-' }}
          </template>
        </el-table-column>

        <!-- 纬度（兼容: latitude/lat/y） -->
        <el-table-column label="纬度" width="120">
          <template #default="{ row }">
            {{ pick(row, ['latitude','lat','y']) ?? '-' }}
          </template>
        </el-table-column>

        <!-- 地址（兼容: reason/address/addr/location/poiName/place/formattedAddress） -->
        <el-table-column label="地址">
          <template #default="{ row }">
            {{ pick(row, ['reason','address','addr','location','poiName','place','formattedAddress']) ?? '-' }}
          </template>
        </el-table-column>

        <!-- 签到时间（兼容: signTime/sign_time/createdAt/createTime/gmtCreate/time/timestamp） -->
        <el-table-column label="签到时间" width="200">
          <template #default="{ row }">
            {{ formatTimeLike(pick(row, ['signTime','sign_time','createdAt','createTime','gmtCreate','time','timestamp'])) }}
          </template>
        </el-table-column>

        <!-- ✅ 修改：将"有效"改为"签到状态"，显示"成功"或"超出范围" -->
        <el-table-column prop="inRange" label="签到状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.inRange===1?'success':'warning'">
              {{ row.inRange===1 ? '成功' : '超出范围' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="mt-3"
        background
        layout="prev, pager, next, total"
        :total="total" :page-size="query.limit" :current-page="query.page"
        @current-change="p=>{query.page=p;load()}"
      />
    </el-card>

    <!-- 导出对话框 -->
    <el-dialog v-model="exportVisible" title="导出设置" width="500px">
      <el-form label-width="90px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportScope">
            <el-radio label="all">全部数据</el-radio>
            <el-radio label="current">当前页数据</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="exportFileName" placeholder="请输入文件名">
            <template #append>.xlsx</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportVisible = false">取消</el-button>
        <el-button type="primary" @click="exportCsv">导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPage } from '@/service/attendanceGeo'
import { getToken } from '@/utils/cache'
import app from '@/constants/app'
import qs from 'qs'

type DateMode = 'all' | 'single' | 'range'

const query = reactive<{
  page: number
  limit: number
  username: string
  dateMode: DateMode
  signDate: string | null
  dateRange: [string, string] | null
}>({
  page: 1,
  limit: 10,
  username: '',
  dateMode: 'all',
  signDate: null,
  dateRange: null
})

const rows = ref<any[]>([])
const total = ref(0)

// 导出相关状态
const exportVisible = ref(false)
const exportFileName = ref('定位签到数据')
const exportScope = ref<'all' | 'current'>('current')

/** 将当前查询条件转换为后端需要的参数 */
function buildQueryParams() {
  const params: Record<string, any> = {
    page: query.page,
    limit: query.limit,
    username: query.username
  }
  if (query.dateMode === 'single' && query.signDate) {
    params.signDate = query.signDate
  } else if (query.dateMode === 'range' && query.dateRange && query.dateRange.length === 2) {
    params.startDate = query.dateRange[0]
    params.endDate   = query.dateRange[1]
  }
  return params
}

/** 兼容不同后端返回结构，安全提取分页数据 */
function extractPage(payload: any) {
  const candidate =
    payload?.page ??
    payload?.data?.page ??
    payload?.data?.data?.page ??
    payload?.data?.data ??
    payload?.data ??
    payload

  const list =
    candidate?.list ??
    candidate?.records ??
    candidate?.rows ??
    candidate?.data ??
    []

  const totalLike =
    candidate?.totalCount ??
    candidate?.total ??
    candidate?.count ??
    candidate?.totalElements ??
    0

  return {
    list: Array.isArray(list) ? list : [],
    total: Number(totalLike) || 0,
  }
}

/** 从对象里按优先级取第一个非空字段 */
function pick(obj: any, keys: string[]) {
  for (const k of keys) {
    const v = obj?.[k]
    if (v !== undefined && v !== null && v !== '') return v
  }
  return undefined
}

/** 把各种时间格式/时间戳尽量转成人类可读的 'YYYY-MM-DD HH:mm:ss' */
function formatTimeLike(v: any) {
  if (v === undefined || v === null || v === '') return '-'
  let d: Date
  if (typeof v === 'number') {
    d = new Date(v > 1e12 ? v : v * 1000)
  } else {
    d = new Date(String(v).replace(/-/g, '/'))
  }
  if (isNaN(d.getTime())) return String(v)
  const pad = (n: number) => (n < 10 ? '0' + n : '' + n)
  const Y = d.getFullYear()
  const M = pad(d.getMonth() + 1)
  const D = pad(d.getDate())
  const h = pad(d.getHours())
  const m = pad(d.getMinutes())
  const s = pad(d.getSeconds())
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}

async function load(){
  try {
    const res = await getPage(buildQueryParams())
    const { list, total: t } = extractPage(res)

    // ✅ 修复：在前端重新计算距离和范围状态
    const centerLng = 106.238545; // 你的中心点经度
    const centerLat = 29.998452;  // 你的中心点纬度
    const radius = 800; // 你的范围半径

    list.forEach(item => {
      if (item.longitude && item.latitude) {
        // 计算距离
        const distance = calculateDistance(item.longitude, item.latitude, centerLng, centerLat);
        // 判断是否在范围内
        item.inRange = distance <= radius ? 1 : 0;
      } else {
        item.inRange = 0;
      }
    });

    rows.value = list
    total.value = t

    // ✅ 调试：打印第一条记录，确认是否有部门和地址信息
    if (list.length > 0) {
      console.log('第一条签到记录:', list[0])
      console.log('用户名:', list[0].username)
      console.log('部门:', list[0].deptName) // ✅ 新增：部门信息
      console.log('reason字段:', list[0].reason)
      console.log('address字段:', list[0].address)
      console.log('计算的距离:', calculateDistance(list[0].longitude, list[0].latitude, centerLng, centerLat))
      console.log('范围状态:', list[0].inRange)
    }
  } catch (e:any) {
    console.error(e)
    rows.value = []
    total.value = 0
    ElMessage.error(e?.message || '签到记录加载失败')
  }
}

// ✅ 新增：距离计算方法
function calculateDistance(lng1: number, lat1: number, lng2: number, lat2: number): number {
  const R = 6378137; // 地球半径，单位米
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLng = (lng2 - lng1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
          Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
          Math.sin(dLng/2) * Math.sin(dLng/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function reset(){
  query.page = 1
  query.limit = 10
  query.username = ''
  query.dateMode = 'all'
  query.signDate = null
  query.dateRange = null
  load()
}

function onDateModeChange() {
  if (query.dateMode === 'all') {
    query.signDate = null
    query.dateRange = null
  } else if (query.dateMode === 'single') {
    query.dateRange = null
  } else if (query.dateMode === 'range') {
    query.signDate = null
  }
}

/** 显示导出对话框 */
function showExportDialog() {
  if (!total.value) {
    ElMessage.warning('暂无可导出的数据')
    return
  }
  exportVisible.value = true
}

/** 执行导出 - 参考日报导出的方式 */
function exportCsv() {
  const exportParams: any = {
    token: getToken(),
    fileName: exportFileName.value,
    exportScope: exportScope.value
  }

  // 同步筛选条件
  if (query.username) exportParams.username = query.username

  // 日期条件
  if (query.dateMode === 'single' && query.signDate) {
    exportParams.signDate = query.signDate
  } else if (query.dateMode === 'range' && query.dateRange && query.dateRange.length === 2) {
    exportParams.startDate = query.dateRange[0]
    exportParams.endDate = query.dateRange[1]
  }

  // 当前页导出需要分页参数；全部导出不需要
  if (exportScope.value === 'current') {
    exportParams.page = query.page
    exportParams.limit = query.limit
  }

  // 去空
  Object.keys(exportParams).forEach((k) => {
    if (exportParams[k] === '' || exportParams[k] === undefined || exportParams[k] === null) {
      delete exportParams[k]
    }
  })

  console.log('导出参数:', exportParams)

  // 触发下载 - 使用 window.location.href 方式，参考日报导出
  const base = app.api || (import.meta as any).env?.VITE_BASE_API || '/renren-admin'
  const url = `${base}/attendance/geo/export?${qs.stringify(exportParams)}`

  console.log('导出URL:', url)

  window.location.href = url
  exportVisible.value = false

  ElMessage.success('导出请求已发送，请稍候...')
}

onMounted(load)
</script>
