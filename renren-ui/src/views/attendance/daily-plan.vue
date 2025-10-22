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
        <el-form-item label="选择日期">
          <el-date-picker
            v-model="state.dataForm.planDate"
            type="date"
            placeholder="选择日期"
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
        <el-button type="success" @click="exportHandle()">导出Excel</el-button>
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
        <el-table-column prop="planDate" label="计划日期" width="120" align="center">
          <template #default="scope">
            {{ formatDate(scope.row.planDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="工作内容" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="completion" label="完成情况" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
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
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import baseService from '@/service/baseService'
import { useAppStore } from '@/store'
import AddOrUpdate from './daily-plan-add-or-update.vue'

const appStore = useAppStore()
const addOrUpdateRef = ref()

const state = reactive({
  dataForm: {
    userId: '' as string | number | undefined,
    username: '',
    planDate: ''
  },
  dataList: [] as any[],
  page: 1,
  limit: 10,
  total: 0,
  dataListLoading: false
})

// 获取数据列表
const getDataList = () => {
  state.dataListLoading = true

  const params = {
    page: state.page,
    limit: state.limit,
    userId: state.dataForm.userId || undefined,
    username: state.dataForm.username || undefined,
    planDate: state.dataForm.planDate || undefined
  }

  // 修正拼写错误：dailyplan -> dailyplan
  baseService.get('/attendance/dailyplan/list', params)
    .then((res) => {
      if (res.code === 0) {
        state.dataList = res.data.list || []
        state.total = res.data.total || 0
      } else {
        ElMessage.error(res.msg || '查询失败')
      }
    })
    .catch((error) => {
      console.error('查询失败:', error)
      ElMessage.error('查询失败')
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
    planDate: ''
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
    baseService.delete(`/attendance/dailyplan/${id}`)
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
        ElMessage.error('删除失败')
      })
  }).catch(() => {})
}
import axios from 'axios'

import { ElMessage } from "element-plus";

const exportHandle = async () => {
  try {
    // 从 sessionStorage 获取并验证 token
    const tokenCache = sessionStorage.getItem("v1@CacheToken");

    if (tokenCache) {
      const parsedToken = JSON.parse(tokenCache); // 解析 token 数据
      const storedExpire = parsedToken.expire;    // 获取 token 的过期时间

      // 获取当前时间戳（单位：秒）
      const currentTimestamp = Math.floor(Date.now() / 1000);

      // 判断 token 是否过期
      if (currentTimestamp > storedExpire) {
        // 如果 token 已过期，提示用户重新登录
        ElMessage.error("Token 已过期，请重新登录");
        return; // 不执行导出操作
      } else {
        console.log("Token 有效");

        // 获取有效的 token
        const token = parsedToken.token;

        // 发送导出请求时，确保请求头带上 token
        const response = await axios.get("/renren-admin/attendance/dailyplan/export", {
          params: {
            userId: state.dataForm.userId,
            username: state.dataForm.username,
            planDate: state.dataForm.planDate,
          },
          responseType: "blob", // 设置返回类型为二进制流
          headers: {
            Authorization: `Bearer ${token}`, // 在请求头中带上 token
          },
        });

        if (response.status === 200) {
          // 创建文件下载
          const blob = new Blob([response.data], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
          const link = document.createElement("a");
          link.href = URL.createObjectURL(blob);
          link.download = "日报数据.xlsx";
          link.click();
          URL.revokeObjectURL(link.href);

          ElMessage.success("导出成功");
        } else {
          ElMessage.error("导出失败，请检查登录状态或接口路径");
        }
      }
    } else {
      ElMessage.error("没有找到 token，请重新登录");
    }
  } catch (error) {
    console.error("导出失败:", error);
    ElMessage.error("导出失败，请检查登录状态或接口路径");
  }
};


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
