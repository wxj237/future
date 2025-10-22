<template>
  <el-dialog
    v-model="dialogVisible"
    :title="!state.dataForm.id ? '新增' : '修改'"
    width="600px"
    :close-on-click-modal="false"
    @close="closeDialog"
  >
    <el-form
      :model="state.dataForm"
      :rules="state.rules"
      ref="dataFormRef"
      label-width="100px"
    >
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="state.dataForm.userId" placeholder="自动获取用户ID" readonly></el-input>
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="state.dataForm.username" placeholder="自动获取用户名" readonly></el-input>
      </el-form-item>
      <el-form-item label="计划日期" prop="planDate">
        <el-date-picker
          v-model="state.dataForm.planDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          style="width: 100%">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="工作内容" prop="content">
        <el-input
          v-model="state.dataForm.content"
          type="textarea"
          :rows="5"
          placeholder="请输入工作内容"></el-input>
      </el-form-item>
      <el-form-item label="完成情况" prop="completion">
        <el-input
          v-model="state.dataForm.completion"
          type="textarea"
          :rows="5"
          placeholder="请输入完成情况"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="state.submitHandle()" :loading="submitButtonLoading">提交</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { reactive, ref, computed, nextTick } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import baseService from '@/service/baseService';
import { useAppStore } from '@/store';

const dialogVisible = ref(false);
const submitButtonLoading = ref(false);
const dataFormRef = ref<FormInstance>();

const emit = defineEmits(["refreshDataList", "close"]);

// 获取当前登录用户信息
const appStore = useAppStore();
const currentUser = computed(() => appStore.state.user);

// 调试：打印用户信息
console.log('当前用户信息:', currentUser.value);

const state = reactive({
  dataForm: {
    id: null,
    userId: null as number | null,
    username: '',
    planDate: null,
    content: '',
    completion: ''
  },

  rules: {
    planDate: [
      { required: true, message: '请选择计划日期', trigger: 'change' }
    ],
    content: [
      { required: true, message: '请输入工作内容', trigger: 'blur' }
    ]
  } as FormRules,

  submitHandle: async () => {
    if (!dataFormRef.value) return;

    const valid = await dataFormRef.value.validate().catch(() => false);
    if (!valid) {
      ElMessage.warning('请完善表单信息');
      return;
    }

    submitButtonLoading.value = true;

    try {
      const submitData = {
        ...state.dataForm,
        planDate: state.dataForm.planDate
      };

      console.log('提交数据:', JSON.stringify(submitData, null, 2));

      const url = state.dataForm.id ? "/attendance/dailyplan/update" : "/attendance/dailyplan";
      const response = await baseService.post(url, submitData);

      console.log('完整响应:', response);

      if (response && response.code === 0) {
        ElMessage.success(state.dataForm.id ? "修改成功!" : "添加成功!");
        closeDialog();
      } else {
        const errorMsg = response?.msg || response?.message || "操作失败";
        ElMessage.error(errorMsg);
      }
    } catch (error: any) {
      console.error("保存失败:", error);
      let errorMsg = "服务器内部异常";
      if (error.response) {
        const responseData = error.response.data;
        errorMsg = responseData?.msg || responseData?.message || error.response.statusText;
      } else if (error.message) {
        errorMsg = error.message;
      }
      ElMessage.error(`保存失败: ${errorMsg}`);
    } finally {
      submitButtonLoading.value = false;
    }
  }
});

// 初始化数据 - 修复版本
const init = (id?: number) => {
  console.log('初始化弹窗，ID:', id);
  console.log('当前用户:', currentUser.value);

  try {
    if (id) {
      // 编辑模式：获取现有数据
      baseService.get(`/attendance/dailyplan/info/${id}`)
        .then((res) => {
          if (res.code === 0) {
            state.dataForm = {
              ...res.data,
              planDate: res.data.planDate || null
            };
            dialogVisible.value = true;
          } else {
            ElMessage.error(res.msg || "获取数据失败");
          }
        })
        .catch((error) => {
          console.error("获取数据失败:", error);
          ElMessage.error("获取数据失败: " + error.message);
        });
    } else {
      // 新增模式：自动设置用户信息
      // 安全地获取用户ID和用户名
      const userId = currentUser.value?.id || currentUser.value?.userId;
      const username = currentUser.value?.username || currentUser.value?.name || '未知用户';

      console.log('设置用户ID:', userId, '用户名:', username);

      state.dataForm = {
        id: null,
        userId: userId, // 使用数字ID
        username: username,
        planDate: new Date(),
        content: '',
        completion: ''
      };

      nextTick(() => {
        if (dataFormRef.value) {
          dataFormRef.value.clearValidate();
        }
        dialogVisible.value = true;
      });
    }
  } catch (error) {
    console.error('初始化失败:', error);
    ElMessage.error('初始化失败');
  }
};

const closeDialog = () => {
  dialogVisible.value = false;
  emit("refreshDataList");
  emit("close");
};

defineExpose({
  init
});
</script>
