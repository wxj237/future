<template>
  <div class="mod-attendance__dailyplan-submit">
    <el-form :model="dataForm" ref="dataFormRef" label-width="120px" @keyup.enter="submitHandle()">
      <el-form-item label="计划日期" prop="planDate">
        <el-date-picker
          v-model="dataForm.planDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD">
        </el-date-picker>
      </el-form-item>
      
      <el-form-item label="工作内容" prop="content">
        <el-input
          v-model="dataForm.content"
          type="textarea"
          :rows="5"
          placeholder="请输入工作内容">
        </el-input>
      </el-form-item>
      
      <el-form-item label="完成情况" prop="completion">
        <el-input
          v-model="dataForm.completion"
          type="textarea"
          :rows="3"
          placeholder="请输入完成情况">
        </el-input>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitHandle()">提交</el-button>
        <el-button @click="resetHandle()">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import baseService from '@/service/baseService';
import { useAppStore } from '@/store';

// 获取当前登录用户信息
const appStore = useAppStore();
const currentUser = computed(() => appStore.state.user);

// 响应式数据
const dataForm = reactive({
  userId: "", // 添加userId字段
  planDate: "",
  content: "",
  completion: ""
});

// 表单验证规则
const rules = reactive({
  planDate: [
    { required: true, message: "计划日期不能为空", trigger: "blur" }
  ],
  content: [
    { required: true, message: "工作内容不能为空", trigger: "blur" }
  ]
});

// 表单引用
const dataFormRef = ref();

// 初始化时设置当前用户ID
dataForm.userId = currentUser.value.userId;

// 提交处理
const submitHandle = () => {
  dataFormRef.value.validate((valid: boolean) => {
    if (!valid) {
      return false;
    }
    
    // 发送请求到后端
    baseService.post("/attendance/dailyplan", dataForm).then((res) => {
      ElMessage.success("提交成功！");
      resetHandle();
    }).catch(() => {
      ElMessage.error("提交失败！");
    });
  });
};

// 重置表单
const resetHandle = () => {
  dataFormRef.value.resetFields();
  // 重置时仍设置为当前用户ID
  dataForm.userId = currentUser.value.userId;
};
</script>

<style scoped>
.mod-attendance__dailyplan-submit {
  padding: 20px;
}
</style>