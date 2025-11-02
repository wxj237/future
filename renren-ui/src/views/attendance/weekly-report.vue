<template>
  <div class="main-content">
    <div class="card">
      <!-- 查询条件 -->
      <el-form :inline="true" label-width="88px" class="mb-4">
        <el-form-item label="用户ID">
          <el-input
            v-model="filters.userIdInput"
            placeholder="请输入用户ID"
            clearable
            style="width: 200px"
            @input="onFilterInput()"
          />
        </el-form-item>

        <el-form-item label="用户名">
          <el-input
            v-model="filters.usernameInput"
            placeholder="支持模糊查询"
            clearable
            style="width: 200px"
            @input="onFilterInput()"
          />
        </el-form-item>

        <el-form-item label="日期模式">
          <!-- 若项目中无 el-segmented，可替换为 el-radio-group；此处保持不变 -->
          <el-segmented
            :options="[{label:'按周',value:'week'},{label:'区间',value:'range'}]"
            v-model="filters.dateMode"
            @change="onDateModeChange"
          />
        </el-form-item>

        <template v-if="filters.dateMode === 'week'">
          <el-form-item label="选择周">
            <el-date-picker
              v-model="filters.week"
              type="week"
              format="YYYY 第 ww 周"
              value-format="YYYY-[W]ww"
              placeholder="选择周"
              clearable
              style="width: 180px"
              @change="triggerSearch()"
            />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="日期区间">
            <el-date-picker
              v-model="filters.range"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              clearable
              unlink-panels
              value-format="YYYY-MM-DD"
              style="width: 300px"
              @change="triggerSearch()"
            />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" @click="searchHandle">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="operation">
        <el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
        <el-button type="success" @click="showExportDialog()">导出Excel</el-button>
      </div>

      <!-- 错误提示 -->
      <el-alert
        v-if="ui.errorMsg"
        :title="ui.errorMsg"
        type="error"
        show-icon
        class="mb-4"
        @close="ui.errorMsg = ''"
      />

      <!-- 表格 -->
      <el-table
        v-loading="ui.loading"
        :data="rows"
        border
        stripe
        style="width: 100%"
        :empty-text="ui.emptyText"
        :default-sort="{ prop: sort.field || 'createTime', order: elOrder(sort.order) }"
        @sort-change="onSortChange"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="46" fixed="left" />
        <el-table-column prop="id" label="ID" width="70" align="center" sortable="custom" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" sortable="custom" />
        <el-table-column prop="username" label="用户名" width="140" align="center" />

        <el-table-column prop="weekStartDate" label="周起始" width="130" align="center" sortable="custom">
          <template #default="scope">
            {{ formatDate(scope.row.weekStartDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="weekEndDate" label="周结束" width="130" align="center" sortable="custom">
          <template #default="scope">
            {{ formatDate(scope.row.weekEndDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="weeklySummary" label="本周总结" min-width="220" show-overflow-tooltip />
        <el-table-column prop="nextWeekPlan" label="下周计划" min-width="220" show-overflow-tooltip />
        <el-table-column prop="problems" label="存在问题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="suggestions" label="建议" min-width="220" show-overflow-tooltip />

        <el-table-column prop="createTime" label="创建时间" width="170" align="center" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" align="center" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="scope">
            <!-- 把整行 row 传给弹窗，避免 info 500 时无法编辑 -->
            <el-button type="primary" link @click="addOrUpdateHandle(scope.row)">修改</el-button>
            <el-button type="danger" link @click="deleteHandle(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="mt-4"
        :current-page="page.current"
        :page-sizes="page.sizes"
        :page-size="page.size"
        :total="page.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onPageSizeChange"
        @current-change="onPageChange"
      />
    </div>

    <!-- 导出对话框 -->
    <el-dialog v-model="ui.exportVisible" title="导出设置" width="500px">
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
        <!-- 保持不变 -->
        <el-button @click="ui.exportVisible = false">取消</el-button>
        <el-button type="primary" @click="exportHandle()">导出</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：新增 / 修改 -->
    <AddOrUpdate ref="addOrUpdateRef" @refreshDataList="fetchList" />
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import baseService from '@/service/baseService'
import AddOrUpdate from './weekly-report-add-or-update.vue'
import app from '@/constants/app'
import qs from 'qs'
import { getToken } from '@/utils/cache'

/** 常量 */
const PAGE_SIZE_KEY = 'weeklyPlan.pageSize'

/** UI */
const ui = reactive({
  loading: false,
  errorMsg: '',
  exportVisible: false,
  emptyText: '暂无数据'
})

/** 表格数据与选择 */
const rows = ref<any[]>([])
/** 关键：ID 改为字符串数组，避免精度问题 */
const selectedIds = ref<string[]>([])

/** 分页、排序 */
const page = reactive({
  current: 1,
  size: Number(localStorage.getItem(PAGE_SIZE_KEY) || 10),
  total: 0,
  sizes: [10, 20, 50, 100]
})
const sort = reactive<{ field: string | null; order: 'asc' | 'desc' | null }>({
  field: 'createTime',
  order: 'desc'
})

/** 查询筛选 */
const filters = reactive({
  userIdInput: '' as string | number | undefined,
  usernameInput: '',
  dateMode: 'week' as 'week' | 'range',
  week: '' as string | null,
  range: [] as string[] | []
})

/** 导出状态 */
const exportFileName = ref('周报数据')
const exportScope = ref<'all' | 'current'>('all')

const addOrUpdateRef = ref()

/** 路由同步（URL <-> 状态） */
const route = useRoute()
const router = useRouter()

function syncFromQuery() {
  const q = route.query
  page.current = Number(q.page || page.current)
  page.size = Number(q.limit || page.size)
  sort.field = (q.sortField as string) || sort.field
  sort.order = (q.sortOrder as 'asc' | 'desc' | null) || sort.order

  filters.userIdInput = (q.userId as any) ?? ''
  filters.usernameInput = (q.username as any) ?? ''
  const mode = (q.dateMode as 'week' | 'range') || 'week'
  filters.dateMode = mode
  if (mode === 'week') {
    filters.week = (q.week as string) || ''
    filters.range = []
  } else {
    const s = (q.startDate as string) || ''
    const e = (q.endDate as string) || ''
    filters.range = s && e ? [s, e] : []
    filters.week = ''
  }
}
function syncToQuery(replace = true) {
  const query: any = {
    page: page.current,
    limit: page.size
  }
  if (sort.field) query.sortField = sort.field
  if (sort.order) query.sortOrder = sort.order

  if (filters.userIdInput !== '' && filters.userIdInput !== undefined) query.userId = filters.userIdInput
  if (filters.usernameInput) query.username = filters.usernameInput

  query.dateMode = filters.dateMode
  if (filters.dateMode === 'week') {
    if (filters.week) query.week = filters.week
    delete query.startDate
    delete query.endDate
  } else {
    if (Array.isArray(filters.range) && filters.range.length === 2) {
      query.startDate = filters.range[0]
      query.endDate = filters.range[1]
    }
    delete query.week
  }

  router[replace ? 'replace' : 'push']({ query })
}

/** 防抖 */
let debounceTimer: any = null
function debounce(fn: () => void, delay = 300) {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(fn, delay)
}
function onFilterInput() {
  debounce(() => {
    page.current = 1
    syncToQuery()
    fetchList()
  })
}
function onDateModeChange() {
  page.current = 1
  if (filters.dateMode === 'week') {
    filters.range = []
  } else {
    filters.week = ''
  }
  syncToQuery()
  fetchList()
}
function triggerSearch() {
  page.current = 1
  syncToQuery()
  fetchList()
}

/** 查询/重置 */
function searchHandle() {
  page.current = 1
  syncToQuery()
  fetchList()
}
function resetSearch() {
  filters.userIdInput = ''
  filters.usernameInput = ''
  filters.dateMode = 'week'
  filters.week = ''
  filters.range = []
  page.current = 1
  syncToQuery()
  fetchList()
}

/** 排序（El -> 本地） */
function onSortChange(e: { prop: string; order: 'ascending' | 'descending' | null }) {
  if (!e.order) {
    sort.field = null
    sort.order = null
  } else {
    sort.field = e.prop
    sort.order = e.order === 'ascending' ? 'asc' : 'desc'
  }
  page.current = 1
  syncToQuery()
  fetchList()
}
function elOrder(o: 'asc' | 'desc' | null) {
  if (o === 'asc') return 'ascending'
  if (o === 'desc') return 'descending'
  return null
}

/** 分页 */
function onPageChange(cur: number) {
  page.current = cur
  syncToQuery()
  fetchList()
}
function onPageSizeChange(size: number) {
  page.size = size
  localStorage.setItem(PAGE_SIZE_KEY, String(size))
  page.current = 1
  syncToQuery()
  fetchList()
}

/** 多选（预留"勾选导出"） */
function onSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((r) => String(r.id))
}

/** 统一成功判断（兼容多种返回结构） */
function isOk(res: any) {
  return !!res && (res.code === 0 || res.code === 200 || res.success === true || res.status === 0)
}

/** 列表请求 */
function fetchList() {
  ui.loading = true
  ui.errorMsg = ''

  const params: any = {
    page: page.current,
    limit: page.size
  }
  if (filters.userIdInput !== '' && filters.userIdInput !== undefined) params.userId = filters.userIdInput
  if (filters.usernameInput) params.username = filters.usernameInput

  if (filters.dateMode === 'week') {
    if (filters.week) params.week = filters.week
  } else if (Array.isArray(filters.range) && filters.range.length === 2) {
    params.startDate = filters.range[0]
    params.endDate = filters.range[1]
  }

  if (sort.field && sort.order) {
    params.sortField = sort.field
    params.sortOrder = sort.order
  }

  baseService
    .get('/attendance/weeklyplan/list', params)
    .then((res: any) => {
      if (isOk(res)) {
        const data = res.data || {}
        rows.value = data.list || []
        page.total = Number(data.total || 0)
        ui.emptyText = rows.value.length ? ' ' : '未查询到记录'
      } else {
        ui.errorMsg = res.msg || '查询失败'
        rows.value = []
        page.total = 0
        ui.emptyText = '查询失败'
      }
    })
    .catch((err: any) => {
      console.error(err)
      ui.errorMsg = '网络异常或服务器错误'
      rows.value = []
      page.total = 0
      ui.emptyText = '加载失败'
    })
    .finally(() => {
      ui.loading = false
    })
}

/** 导出对话框 */
function showExportDialog() {
  ui.exportVisible = true
}

/** 导出 */
function exportHandle() {
  const exportParams: any = {
    token: getToken(),
    fileName: exportFileName.value,
    exportScope: exportScope.value
  }

  if (filters.userIdInput !== '' && filters.userIdInput !== undefined)
    exportParams.userId = filters.userIdInput
  if (filters.usernameInput) exportParams.username = filters.usernameInput

  if (filters.dateMode === 'week') {
    if (filters.week) exportParams.week = filters.week
  } else if (Array.isArray(filters.range) && filters.range.length === 2) {
    exportParams.startDate = filters.range[0]
    exportParams.endDate = filters.range[1]
  }

  if (exportScope.value === 'current') {
    exportParams.page = page.current
    exportParams.limit = page.size
  }

  if (sort.field && sort.order) {
    exportParams.sortField = sort.field
    exportParams.sortOrder = sort.order
  }

  Object.keys(exportParams).forEach((k) => {
    if (exportParams[k] === '' || exportParams[k] === undefined) delete exportParams[k]
  })

  window.location.href = `${app.api}/attendance/weeklyplan/export?${qs.stringify(exportParams)}`
  ui.exportVisible = false
}

/** 新增/修改/删除 */
function addOrUpdateHandle(row?: any) {
  (addOrUpdateRef.value as any).init(row)
}
function deleteHandle(id: string) {
  ElMessageBox.confirm('确定进行删除操作吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    closeOnClickModal: false,
    closeOnPressEscape: false
  })
    .then(async () => {
      console.log('开始删除，ID:', id)
      try {
        // 使用字符串 ID，避免雪花ID精度问题
        const res: any = await baseService.delete(`/attendance/weeklyplan/${String(id)}`)
        console.log('删除响应:', res)
        if (isOk(res)) {
          ElMessage.success('删除成功')
          fetchList()
        } else {
          console.log('删除失败，响应:', res)
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (e: any) {
        console.error('删除异常:', e)
        ElMessage.error('删除失败: ' + (e.message || '未知错误'))
      }
    })
    .catch(() => {
      console.log('用户取消删除')
    })
}

/** 工具：格式化 */
function formatDate(d: string | Date) {
  if (!d) return ''
  const s = typeof d === 'string' ? d : (d as Date).toString()
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s
  return s.replace('T', ' ').split(' ')[0] || ''
}
function formatDateTime(d: string | Date) {
  if (!d) return ''
  const s = typeof d === 'string' ? d : (d as Date).toISOString()
  return s.replace('T', ' ').replace('Z', '').split('.')[0]
}

/** 初始化 */
onMounted(() => {
  syncFromQuery()
  syncToQuery(true)
  fetchList()
})
watch(
  () => route.query,
  () => {
    syncFromQuery()
    fetchList()
  }
)
</script>

<style scoped>
.main-content { padding: 20px; }
.card {
  background: #fff; padding: 20px; border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.06);
}
.operation { margin-bottom: 16px; }
.operation .el-button { margin-right: 10px; }
.mt-4 { margin-top: 16px; }
.mb-4 { margin-bottom: 16px; }
</style>
