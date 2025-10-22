<template>
  <div class="remind-list">
    <div class="remind-header">
      <span>未读提醒: {{ unreadCount }}</span>
      <el-button
        v-if="unreadCount > 0"
        type="text"
        @click="markAllAsRead">
        全部标记已读
      </el-button>
    </div>

    <el-table :data="tableData" style="width: 100%">
      <el-table-column prop="remindContent" label="提醒内容" />
      <el-table-column prop="remindTime" label="提醒时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.remindTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="readStatus" label="状态" width="100">
        <template slot-scope="scope">
          <el-badge is-dot :hidden="scope.row.readStatus !== 0">
            <el-tag :type="scope.row.readStatus === 0 ? 'warning' : 'info'">
              {{ scope.row.readStatus === 0 ? '未读' : '已读' }}
            </el-tag>
          </el-badge>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.readStatus === 0"
            type="text"
            @click="markAsRead(scope.row.id)">
            标记已读
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getRemindList, markRemindAsRead, markAllRemindsAsRead, getUnreadRemindCount } from '@/api/sys/inbox'

export default {
  name: 'RemindList',
  data() {
    return {
      tableData: [],
      unreadCount: 0
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [remindRes, countRes] = await Promise.all([
          getRemindList(),
          getUnreadRemindCount()
        ])
        this.tableData = remindRes.data.reminds
        this.unreadCount = countRes.data.count
      } catch (error) {
        console.error('加载提醒失败', error)
      }
    },
    async markAsRead(id) {
      try {
        await markRemindAsRead(id)
        this.$message.success('标记已读成功')
        this.loadData()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    async markAllAsRead() {
      try {
        await markAllRemindsAsRead()
        this.$message.success('全部标记已读成功')
        this.loadData()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString()
    }
  }
}
</script>

<style scoped>
.remind-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 8px;
}
</style>
