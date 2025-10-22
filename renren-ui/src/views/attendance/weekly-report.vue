<template>
  <div class="main-content">
    <div class="card">
      <!-- 查询条件 -->
      <el-form :inline="true" :model="state.dataForm" @submit.prevent="getDataList()">
        <el-form-item label="用户ID">
          <el-input
            v-model="state.dataForm.userId"
            placeholder="用户ID"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="state.dataForm.username"
            placeholder="用户名"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="周开始日期">
          <el-date-picker
            v-model="state.dataForm.weekStartDate"
            type="date"
            placeholder="选择周开始日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getDataList()">查询</el-button>
          <el-button @click="resetSearch()">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="operation">
        <el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
        <el-button type="success" @click="showExportDialog = true">导出Excel</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="state.dataListLoading"
        :data="state.dataList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100" align="center"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120" align="center"></el-table-column>
        <el-table-column prop="weekStartDate" label="周开始日期" width="120" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.weekStartDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="weekEndDate" label="周结束日期" width="120" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.weekEndDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="weeklySummary" label="本周总结" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="nextWeekPlan" label="下周计划" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="problems" label="遇到的问题" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="suggestions" label="建议" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="addOrUpdateHandle(scope.row.id)">修改</el-button>
            <el-button type="danger" link @click="deleteHandle(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        :current-page="state.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="state.limit"
        :total="state.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="sizeChangeHandle"
        @current-change="currentChangeHandle"
      ></el-pagination>
    </div>

    <!-- 弹窗 -->
    <add-or-update ref="addOrUpdateRef" @refreshDataList="getDataList"></add-or-update>

    <!-- 导出设置对话框 -->
    <el-dialog v-model="showExportDialog" title="导出设置" width="500px">
      <el-form :model="exportForm" label-width="80px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.exportScope">
            <el-radio label="all">全部数据</el-radio>
            <el-radio label="current">当前页数据</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="导出列">
          <el-checkbox-group v-model="exportForm.selectedColumns">
            <el-checkbox label="id">ID</el-checkbox>
            <el-checkbox label="userId">用户ID</el-checkbox>
            <el-checkbox label="username">用户名</el-checkbox>
            <el-checkbox label="weekStartDate">周开始日期</el-checkbox>
            <el-checkbox label="weekEndDate">周结束日期</el-checkbox>
            <el-checkbox label="weeklySummary">本周总结</el-checkbox>
            <el-checkbox label="nextWeekPlan">下周计划</el-checkbox>
            <el-checkbox label="problems">遇到的问题</el-checkbox>
            <el-checkbox label="suggestions">建议</el-checkbox>
            <el-checkbox label="createTime">创建时间</el-checkbox>
            <el-checkbox label="updateTime">更新时间</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="文件名称">
          <el-input v-model="exportForm.fileName" placeholder="请输入文件名称"></el-input>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showExportDialog = false">取消</el-button>
        <el-button type="primary" @click="handleExport" :loading="exportLoading">确认导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import baseService from '@/service/baseService'
import { useAppStore } from '@/store'
import AddOrUpdate from './weekly-report-add-or-update.vue'

const appStore = useAppStore()
const addOrUpdateRef = ref()

const state = reactive({
  dataForm: {
    userId: '' as string | number | undefined,
    username: '',
    weekStartDate: ''
  },
  dataList: [] as any[],
  page: 1,
  limit: 10,
  total: 0,
  dataListLoading: false
})

// 导出相关状态
const showExportDialog = ref(false)
const exportLoading = ref(false)
const exportForm = reactive({
  exportScope: 'all',
  selectedColumns: ['username', 'weekStartDate', 'weekEndDate', 'weeklySummary', 'nextWeekPlan'],
  fileName: `周报数据_${new Date().getTime()}`
})

// 获取数据列表
const getDataList = () => {
  state.dataListLoading = true

  const params = {
    page: state.page,
    limit: state.limit,
    userId: state.dataForm.userId || undefined,
    username: state.dataForm.username || undefined,
    weekStartDate: state.dataForm.weekStartDate || undefined
  }

  console.log('请求参数:', params)

  baseService.get('/attendance/weeklyreport/list', params)
    .then((res) => {
      console.log('响应数据:', res)
      if (res.code === 0) {
        state.dataList = res.data.list || []
        state.total = res.data.total || 0
      } else {
        ElMessage.error(res.msg || '查询失败')
      }
    })
    .catch((error) => {
      console.error('查询失败:', error)
      ElMessage.error('查询失败: ' + (error.message || '未知错误'))
    })
    .finally(() => {
      state.dataListLoading = false
    })
}

// 重置查询
const resetSearch = () => {
  state.dataForm = {
    userId: '',
    username: '',
    weekStartDate: ''
  }
  state.page = 1
  getDataList()
}

// 新增 / 修改
const addOrUpdateHandle = (id?: number) => {
  addOrUpdateRef.value.init(id)
}

// 删除
const deleteHandle = (id: number) => {
  ElMessageBox.confirm('确定进行删除操作吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    baseService.delete(`/attendance/weeklyreport/${id}`)
      .then((res) => {
        if (res.code === 0) {
          ElMessage.success('删除成功')
          getDataList()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      })
      .catch((error) => {
        console.error('删除失败:', error)
        ElMessage.error('删除失败: ' + (error.message || '未知错误'))
      })
  }).catch(() => {})
}

// 处理导出
const handleExport = () => {
  if (exportForm.selectedColumns.length === 0) {
    ElMessage.warning('请至少选择一列进行导出')
    return
  }

  if (!exportForm.fileName.trim()) {
    ElMessage.warning('请输入文件名称')
    return
  }

  exportLoading.value = true

  // 构建导出参数
  const params: any = {
    columns: exportForm.selectedColumns.join(','),
    exportScope: exportForm.exportScope
  }

  // 添加查询条件
  if (state.dataForm.userId) {
    params.userId = state.dataForm.userId
  }
  if (state.dataForm.username) {
    params.username = state.dataForm.username
  }
  if (state.dataForm.weekStartDate) {
    params.weekStartDate = state.dataForm.weekStartDate
  }

  // 如果是当前页，添加分页参数
  if (exportForm.exportScope === 'current') {
    params.page = state.page
    params.limit = state.limit
  }

  console.log('导出周报参数:', params)

  baseService.download('/attendance/weeklyreport/export', params, `${exportForm.fileName}.xlsx`, { responseType: 'blob' })
    .then(() => {
      ElMessage.success('导出成功')
      showExportDialog.value = false
    })
    .catch(error => {
      console.error('导出失败:', error)
      ElMessage.error('导出失败: ' + (error.message || '未知错误'))
    })
    .finally(() => {
      exportLoading.value = false
    })
}

// 分页大小变化
const sizeChangeHandle = (val: number) => {
  state.limit = val
  state.page = 1
  getDataList()
}

// 当前页码变化
const currentChangeHandle = (val: number) => {
  state.page = val
  getDataList()
}

// 日期格式化
const formatDate = (date: string) => {
  if (!date) return ''
  return date.split(' ')[0]
}

const formatDateTime = (date: string) => {
  if (!date) return ''
  return date.replace('T', ' ').split('.')[0]
}

onMounted(() => {
  getDataList()
})
</script>

<style scoped>
.main-content {
  padding: 20px;
}
.card {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
.operation {
  margin-bottom: 20px;
}
.operation .el-button {
  margin-right: 10px;
}
</style>
