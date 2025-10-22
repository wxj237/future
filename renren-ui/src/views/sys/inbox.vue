<template>
  <div class="inbox-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>收件箱</span>
        <el-button
          style="float: right; padding: 3px 0"
          type="text"
          @click="showSendDialog = true">
          发送通知
        </el-button>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的收件箱" name="inbox">
          <div class="empty-state">
            <el-empty description="收件箱功能开发中..." />
          </div>
        </el-tab-pane>
        <el-tab-pane label="提醒" name="remind">
          <div class="empty-state">
            <el-empty description="提醒功能开发中..." />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Inbox',
  data() {
    return {
      activeTab: 'inbox',
      showSendDialog: false
    }
  },
  methods: {
    handleSendSuccess() {
      this.showSendDialog = false
    }
  }
}
</script>

<style scoped>
.inbox-container {
  padding: 20px;
}
.empty-state {
  padding: 50px 0;
  text-align: center;
}
</style>
