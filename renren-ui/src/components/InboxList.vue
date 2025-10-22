<template>
  <div class="inbox-list">
    <el-table :data="tableData" style="width: 100%">
      <el-table-column prop="title" label="标题" width="180" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="createTime" label="时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="readStatus" label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.readStatus === 0 ? 'danger' : 'success'">
            {{ scope.row.readStatus === 0 ? '未读' : '已读' }}
          </el-tag>
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

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total">
    </el-pagination>
  </div>
</template>

<script>
import { getInboxList, markInboxAsRead } from '@/api/sys/inbox'

export default {
  name: 'InboxList',
  data() {
    return {
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const params = {
          page: this.currentPage,
          limit: this.pageSize
        }
        const res = await getInboxList(params)
        this.tableData = res.data.page.list
        this.total = res.data.page.totalCount
      } catch (error) {
        console.error('加载收件箱失败', error)
      }
    },
    async markAsRead(id) {
      try {
        await markInboxAsRead(id)
        this.$message.success('标记已读成功')
        this.loadData()
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadData()
    },
    formatTime(time) {
      return new Date(time).toLocaleString()
    }
  }
}
</script>
