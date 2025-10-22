<template>
  <div class="send-notification">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="通知类型" prop="receiverType">
        <el-radio-group v-model="form.receiverType" @change="handleReceiverTypeChange">
          <el-radio label="group">指定群组</el-radio>
          <el-radio label="user">指定用户</el-radio>
          <el-radio label="all">所有用户</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.receiverType === 'group'" label="选择群组" prop="groupId">
        <el-select v-model="form.groupId" placeholder="请选择群组" style="width: 100%">
          <el-option
            v-for="group in groupList"
            :key="group.id"
            :label="group.groupName"
            :value="group.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item v-if="form.receiverType === 'user'" label="选择用户" prop="userIds">
        <el-select v-model="form.userIds" multiple placeholder="请选择用户" style="width: 100%">
          <el-option
            v-for="user in userList"
            :key="user.userId"
            :label="user.username"
            :value="user.userId">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入通知标题" />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="请输入通知内容" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">发送</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { sendToGroup, sendToUsers, sendToAll } from '@/api/sys/inbox'
import { getGroupList } from '@/api/sys/group'
import { getUserList } from '@/api/sys/user'

export default {
  name: 'SendNotification',
  data() {
    return {
      form: {
        receiverType: 'group',
        groupId: null,
        userIds: [],
        title: '',
        content: ''
      },
      rules: {
        receiverType: [
          { required: true, message: '请选择接收类型', trigger: 'change' }
        ],
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入内容', trigger: 'blur' }
        ]
      },
      groupList: [],
      userList: [],
      loading: false
    }
  },
  mounted() {
    this.loadGroupList()
    this.loadUserList()
  },
  methods: {
    handleReceiverTypeChange(value) {
      this.form.groupId = null
      this.form.userIds = []
    },
    async handleSubmit() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return

        this.loading = true
        try {
          let apiCall
          const params = {
            title: this.form.title,
            content: this.form.content
          }

          switch (this.form.receiverType) {
            case 'group':
              apiCall = sendToGroup({ ...params, groupId: this.form.groupId })
              break
            case 'user':
              apiCall = sendToUsers({ ...params, receiverIds: this.form.userIds.join(',') })
              break
            case 'all':
              apiCall = sendToAll(params)
              break
          }

          if (apiCall) {
            await apiCall
            this.$message.success('发送成功')
            this.$emit('success')
          }
        } catch (error) {
          console.error('发送失败', error)
          this.$message.error('发送失败')
        } finally {
          this.loading = false
        }
      })
    },
    handleCancel() {
      this.$emit('success')
    },
    async loadGroupList() {
      try {
        const res = await getGroupList()
        this.groupList = res.data.list
      } catch (error) {
        console.error('加载群组列表失败', error)
      }
    },
    async loadUserList() {
      try {
        const res = await getUserList()
        this.userList = res.data.list
      } catch (error) {
        console.error('加载用户列表失败', error)
      }
    }
  }
}
</script>
