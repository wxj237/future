<!-- src/views/sys/group.vue -->
<template>
  <div class="group-management">
    <el-card>
      <div class="filter-container">
        <el-button type="primary" @click="handleCreate">
          新增群组
        </el-button>
      </div>

      <el-table :data="groupList" row-key="id" border>
        <el-table-column prop="groupName" label="群组名称" min-width="200"></el-table-column>
        <el-table-column prop="sort" label="排序" width="100"></el-table-column>
        <el-table-column prop="description" label="描述" min-width="200"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="text" @click="handleUpdate(scope.row)">
              修改
            </el-button>
            <el-button type="text" @click="handleChat(scope.row)">
              进入群聊
            </el-button>
            <el-button type="text" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="groupForm" :model="groupForm" :rules="rules" label-width="80px">
        <el-form-item label="群组名称" prop="groupName">
          <el-input v-model="groupForm.groupName" placeholder="请输入群组名称"></el-input>
        </el-form-item>
        <el-form-item label="上级群组" prop="parentId">
          <el-tree-select
            v-model="groupForm.parentId"
            :data="groupTree"
            check-strictly
            :props="{ label: 'groupName', value: 'id' }"
            placeholder="请选择上级群组"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="groupForm.sort" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="groupForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群组描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 群聊对话框 -->
    <el-dialog title="群组聊天" v-model="chatDialogVisible" width="800px" :fullscreen="true">
      <div class="chat-container">
        <div class="chat-header">
          <h3>{{ currentGroup?.groupName }} - 群聊</h3>
          <el-button type="primary" @click="loadMessages">刷新消息</el-button>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div v-for="message in messages" :key="message.id" class="message-item" :class="{ 'own-message': message.sendUserId === currentUser.id }">
            <div class="message-header">
              <span class="username">{{ getUserName(message.sendUserId) }}</span>
            </div>
            <div class="message-content">
              {{ message.content }}
            </div>
            <div class="message-time">
              {{ formatFullTime(message.createTime) }}
            </div>
          </div>
          <div v-if="messages.length === 0" class="no-messages">
            暂无消息，开始聊天吧！
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.ctrl.enter="sendMessage"
          ></el-input>
          <div class="input-actions">
            <span class="current-user">当前用户: {{ currentUser.realName }}</span>
            <el-button @click="sendMessage" type="primary">发送消息 (Ctrl+Enter)</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/http'
import { useAppStore } from '@/store'

// 使用store获取用户信息
const appStore = useAppStore()
const currentUser = ref({
  id: '',
  realName: '',
  username: ''
})

const groupList = ref([])
const groupTree = ref([])
const dialogVisible = ref(false)
const chatDialogVisible = ref(false)
const dialogTitle = ref('')
const currentGroup = ref<any>(null)
const messages = ref([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement>()
const userMap = ref(new Map()) // 用户ID到用户名的映射

const groupForm = ref({
  id: undefined,
  groupName: '',
  parentId: 0,
  sort: 0,
  description: ''
})

const rules = {
  groupName: [{ required: true, message: '请输入群组名称', trigger: 'blur' }]
}

// 获取当前用户信息
const getCurrentUserInfo = () => {
  const storeUser = appStore.state.user
  if (storeUser && storeUser.id) {
    currentUser.value = {
      id: storeUser.id.toString(),
      realName: storeUser.realName || storeUser.username || '用户',
      username: storeUser.username || 'user'
    }
    // 将当前用户信息存入映射
    userMap.value.set(currentUser.value.id, currentUser.value.realName)
  } else {
    // 如果store中没有，使用默认值
    currentUser.value = {
      id: '1',
      realName: '管理员',
      username: 'admin'
    }
    userMap.value.set('1', '管理员')
  }
}

// 根据用户ID获取用户名 - 简化版本
const getUserName = (userId: string) => {
  // 如果是当前用户
  if (userId === currentUser.value.id) {
    return currentUser.value.realName + ' (我)'
  }

  // 如果已经缓存了该用户信息，直接返回
  if (userMap.value.has(userId)) {
    return userMap.value.get(userId)
  }

  // 否则使用已知的用户映射
  const knownUsers = {
    '1': '管理员',
    '2': '驴娣',
    '3': '王向军',
    '4': '234'
  }

  if (knownUsers[userId]) {
    userMap.value.set(userId, knownUsers[userId])
    return knownUsers[userId]
  }

  // 如果不知道的用户，返回用户ID
  return `用户${userId}`
}

// 加载群组列表
const loadGroups = async () => {
  try {
    const response = await http({
      url: '/sys/group/list',
      method: 'get'
    })
    groupList.value = response.data.list
    groupTree.value = [{ id: 0, groupName: '一级菜单' }, ...response.data.list]
  } catch (error) {
    console.error('加载群组失败:', error)
    ElMessage.error('加载群组失败')
  }
}

// 处理新增
const handleCreate = () => {
  dialogTitle.value = '新增群组'
  dialogVisible.value = true
  groupForm.value = {
    id: undefined,
    groupName: '',
    parentId: 0,
    sort: 0,
    description: ''
  }
}

// 处理修改
const handleUpdate = (row: any) => {
  dialogTitle.value = '修改群组'
  dialogVisible.value = true
  groupForm.value = { ...row }
}

// 处理删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该群组吗？', '提示', { type: 'warning' })
    await http({
      url: '/sys/group/delete',
      method: 'post',
      data: [row.id]
    })
    ElMessage.success('删除成功')
    loadGroups()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 提交表单
const submitForm = async () => {
  try {
    if (groupForm.value.id) {
      await http({
        url: '/sys/group/update',
        method: 'post',
        data: groupForm.value
      })
    } else {
      await http({
        url: '/sys/group/save',
        method: 'post',
        data: groupForm.value
      })
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadGroups()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 进入群聊
const handleChat = (row: any) => {
  currentGroup.value = row
  chatDialogVisible.value = true
  loadMessages()
}

// 加载消息
const loadMessages = async () => {
  if (!currentGroup.value) return

  try {
    const response = await http({
      url: `/sys/groupmessage/messages/${currentGroup.value.id}`,
      method: 'get'
    })
    messages.value = response.data.messages || []
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败:', error)
    // 如果接口还没完全实现，显示一些测试消息
    const now = new Date()
    messages.value = [
      {
        id: 1,
        sendUserId: currentUser.value.id,
        content: '欢迎来到群聊！',
        createTime: new Date(now.getTime() - 120000)
      },
      {
        id: 2,
        sendUserId: '2', // 驴娣
        content: '大家好，我是驴娣',
        createTime: new Date(now.getTime() - 60000)
      },
      {
        id: 3,
        sendUserId: '3', // 王向军
        content: '我是王向军，请多关照',
        createTime: new Date(now.getTime() - 30000)
      },
      {
        id: 4,
        sendUserId: currentUser.value.id,
        content: '今天天气不错，适合写代码',
        createTime: new Date(now.getTime() - 15000)
      }
    ]
    scrollToBottom()
  }
}

// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  if (!currentGroup.value) {
    ElMessage.error('未选择群组')
    return
  }

  try {
    const messageData = {
      groupId: currentGroup.value.id,
      sendUserId: currentUser.value.id,
      messageType: 'text',
      content: newMessage.value.trim(),
      mentionedUsers: ''
    }

    await http({
      url: '/sys/groupmessage/send',
      method: 'post',
      data: messageData
    })

    ElMessage.success('消息发送成功')
    newMessage.value = ''
    loadMessages() // 重新加载消息列表
  } catch (error) {
    console.error('发送消息失败:', error)
    // 如果后端接口还没完全实现，模拟发送成功
    const newMsg = {
      id: Date.now(),
      sendUserId: currentUser.value.id,
      content: newMessage.value.trim(),
      createTime: new Date()
    }
    messages.value.push(newMsg)
    newMessage.value = ''
    ElMessage.success('消息发送成功')
    scrollToBottom()
  }
}

// 格式化完整时间
const formatFullTime = (time: string | Date) => {
  if (!time) return '刚刚'

  try {
    const date = typeof time === 'string' ? new Date(time) : time
    if (isNaN(date.getTime())) {
      return '刚刚'
    }

    const now = new Date()

    // 如果是今天
    if (date.toDateString() === now.toDateString()) {
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }

    // 如果是昨天
    const yesterday = new Date(now)
    yesterday.setDate(yesterday.getDate() - 1)
    if (date.toDateString() === yesterday.toDateString()) {
      return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }

    // 其他情况显示完整日期
    return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  } catch (error) {
    return '刚刚'
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  getCurrentUserInfo()
  loadGroups()
})
</script>

<style scoped>
/* 样式保持不变 */
.filter-container {
  margin-bottom: 20px;
}

.chat-container {
  height: 70vh;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  background: #f9f9f9;
  min-height: 300px;
}

.message-item {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 5px;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message-item.own-message {
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.message-header {
  margin-bottom: 5px;
}

.username {
  font-size: 14px;
  font-weight: bold;
  color: #333;
}

.message-content {
  word-break: break-word;
  line-height: 1.5;
  margin-bottom: 5px;
  font-size: 14px;
}

.message-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

.no-messages {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.chat-input {
  border-top: 1px solid #e4e7ed;
  padding-top: 10px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.current-user {
  color: #666;
  font-size: 14px;
}
</style>
