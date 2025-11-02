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
          <el-segmented
            :options="[{label:'单日',value:'single'},{label:'区间',value:'range'}]"
            v-model="filters.dateMode"
            @change="onDateModeChange"
          />
        </el-form-item>

        <template v-if="filters.dateMode === 'single'">
          <el-form-item label="计划日期">
            <el-date-picker
              v-model="filters.planDateSingle"
              type="date"
              placeholder="选择日期"
              clearable
              value-format="YYYY-MM-DD"
              style="width: 180px"
              @change="triggerSearch()"
            />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="日期区间">
            <el-date-picker
              v-model="filters.planDateRange"
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
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="username" label="用户名" width="140" align="center" />
        <el-table-column prop="planDate" label="计划日期" width="130" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.planDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="工作内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="completion" label="完成情况" min-width="200" show-overflow-tooltip />
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="170"
          align="center"
          sortable="custom"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="updateTime"
          label="更新时间"
          width="170"
          align="center"
          sortable="custom"
        >
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
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
import AddOrUpdate from './daily-plan-add-or-update.vue'
import app from '@/constants/app'
import qs from 'qs'
import { getToken } from '@/utils/cache'

/** --------- 本地常量 --------- **/
const PAGE_SIZE_KEY = 'dailyPlan.pageSize'

/** --------- UI/状态 --------- **/
const ui = reactive({
  loading: false,
  errorMsg: '',
  exportVisible: false,
  emptyText: '暂无数据'
})

const rows = ref<any[]>([])

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

/** 查询筛选（表单双向绑定与防抖输入分离） */
const filters = reactive({
  userIdInput: '' as string | number | undefined,   // 输入框值
  usernameInput: '',                                 // 输入框值
  dateMode: 'single' as 'single' | 'range',
  planDateSingle: '' as string | null,               // YYYY-MM-DD
  planDateRange: [] as string[] | []
})

/** 导出状态 */
const exportFileName = ref('工作计划表')
const exportScope = ref<'all' | 'current'>('all')

const addOrUpdateRef = ref()

/** --------- 路由同步（URL <-> 状态） --------- **/
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
  const dateMode = (q.dateMode as 'single' | 'range') || 'single'
  filters.dateMode = dateMode
  if (dateMode === 'single') {
    filters.planDateSingle = (q.planDate as string) || ''
    filters.planDateRange = []
  } else {
    const s = (q.startDate as string) || ''
    const e = (q.endDate as string) || ''
    filters.planDateRange = s && e ? [s, e] : []
    filters.planDateSingle = ''
  }
}

function syncToQuery(replace = true) {
  const query: any = {
    page: page.current,
    limit: page.size
  }
  if (sort.field) query.sortField = sort.field
  if (sort.order) query.sortOrder = sort.order

  if (filters.userIdInput !== '' && filters.userIdInput !== undefined)
    query.userId = filters.userIdInput
  if (filters.usernameInput) query.username = filters.usernameInput

  query.dateMode = filters.dateMode
  if (filters.dateMode === 'single') {
    if (filters.planDateSingle) query.planDate = filters.planDateSingle
    delete query.startDate
    delete query.endDate
  } else {
    if (Array.isArray(filters.planDateRange) && filters.planDateRange.length === 2) {
      query.startDate = filters.planDateRange[0]
      query.endDate = filters.planDateRange[1]
    }
    delete query.planDate
  }

  router[replace ? 'replace' : 'push']({ query })
}

/** --------- 工具：防抖 --------- **/
let debounceTimer: any = null
function debounce(fn: () => void, delay = 300) {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(fn, delay)
}

/** 输入框按键即触发防抖搜索 */
function onFilterInput() {
  debounce(() => {
    page.current = 1
    syncToQuery()
    fetchList()
  })
}

function onDateModeChange() {
  page.current = 1
  // 清理另一种模式下的值
  if (filters.dateMode === 'single') {
    filters.planDateRange = []
  } else {
    filters.planDateSingle = ''
  }
  syncToQuery()
  fetchList()
}

/** 点击查询按钮 */
function searchHandle() {
  page.current = 1
  syncToQuery()
  fetchList()
}

/** 重置筛选 */
function resetSearch() {
  filters.userIdInput = ''
  filters.usernameInput = ''
  filters.dateMode = 'single'
  filters.planDateSingle = ''
  filters.planDateRange = []
  page.current = 1
  syncToQuery()
  fetchList()
}

/** 排序（Element Plus -> 本组件状态） */
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

/** 将 'asc'/'desc' 转为 Element Plus 需要的 order 值 */
function elOrder(o: 'asc' | 'desc' | null) {
  if (o === 'asc') return 'ascending'
  if (o === 'desc') return 'descending'
  return null
}

/** 分页切换 */
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

/** 列表请求 */
function fetchList() {
  ui.loading = true
  ui.errorMsg = ''

  const params: any = {
    page: page.current,
    limit: page.size
  }

  // 过滤条件
  if (filters.userIdInput !== '' && filters.userIdInput !== undefined) params.userId = filters.userIdInput
  if (filters.usernameInput) params.username = filters.usernameInput

  // 日期（兼容后端：单日沿用 planDate；区间提供 startDate/endDate，需后端支持）
  if (filters.dateMode === 'single') {
    if (filters.planDateSingle) params.planDate = filters.planDateSingle
  } else if (Array.isArray(filters.planDateRange) && filters.planDateRange.length === 2) {
    params.startDate = filters.planDateRange[0]
    params.endDate = filters.planDateRange[1]
  }

  // 排序
  if (sort.field && sort.order) {
    params.sortField = sort.field
    params.sortOrder = sort.order
  }

  baseService
    .get('/attendance/dailyplan/list', params)
    .then((res: any) => {
      if (res.code === 0) {
        rows.value = res.data.list || []
        page.total = Number(res.data.total || 0)
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

/** 执行导出 */
function exportHandle() {
  const exportParams: any = {
    token: getToken(),
    fileName: exportFileName.value,
    exportScope: exportScope.value
  }

  // 同步筛选条件
  if (filters.userIdInput !== '' && filters.userIdInput !== undefined)
    exportParams.userId = filters.userIdInput
  if (filters.usernameInput) exportParams.username = filters.usernameInput

  if (filters.dateMode === 'single') {
    if (filters.planDateSingle) exportParams.planDate = filters.planDateSingle
  } else if (Array.isArray(filters.planDateRange) && filters.planDateRange.length === 2) {
    exportParams.startDate = filters.planDateRange[0]
    exportParams.endDate = filters.planDateRange[1]
  }

  // 当前页导出需要分页参数；全部导出不需要
  if (exportScope.value === 'current') {
    exportParams.page = page.current
    exportParams.limit = page.size
  }

  // 排序（导出时也可带上）
  if (sort.field && sort.order) {
    exportParams.sortField = sort.field
    exportParams.sortOrder = sort.order
  }

  // 去空
  Object.keys(exportParams).forEach((k) => {
    if (exportParams[k] === '' || exportParams[k] === undefined) {
      delete exportParams[k]
    }
  })

  // 触发下载
  window.location.href = `${app.api}/attendance/dailyplan/export?${qs.stringify(exportParams)}`
  ui.exportVisible = false
}

/** 新增/修改 */
function addOrUpdateHandle(id?: number) {
  (addOrUpdateRef.value as any).init(id)
}

/** 删除 */
function deleteHandle(id: number) {
  ElMessageBox.confirm('确定进行删除操作吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() =>
      baseService.delete(`/attendance/dailyplan/${id}`).then((res: any) => {
        if (res.code === 0) {
          ElMessage.success('删除成功')
          fetchList()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      })
    )
    .catch(() => {})
}

/** 格式化工具 */
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

/** 初始化（从URL恢复状态并加载数据） */
onMounted(() => {
  syncFromQuery()
  syncToQuery(true)
  fetchList()
})

/** 当路由查询串变化（可能来自浏览器导航） -> 同步并刷新 */
watch(
  () => route.query,
  () => {
    syncFromQuery()
    fetchList()
  }
)
</script>

<style scoped>
.main-content {
  padding: 20px;
}
.card {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
}
.operation {
  margin-bottom: 16px;
}
.operation .el-button {
  margin-right: 10px;
}
.mt-4 {
  margin-top: 16px;
}
.mb-4 {
  margin-bottom: 16px;
}
</style>
