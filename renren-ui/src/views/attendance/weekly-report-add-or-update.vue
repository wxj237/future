<template>
  <el-dialog
    v-model="dialogVisible"
    :title="!state.dataForm.id ? '新增' : '修改'"
    width="700px"
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
      <el-form-item label="周开始日期" prop="weekStartDate">
        <el-date-picker
          v-model="state.dataForm.weekStartDate"
          type="date"
          placeholder="选择周开始日期"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          style="width: 100%"
          @change="calculateWeekEndDate">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="周结束日期" prop="weekEndDate">
        <el-date-picker
          v-model="state.dataForm.weekEndDate"
          type="date"
          placeholder="自动计算周结束日期"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          style="width: 100%"
          readonly>
        </el-date-picker>
      </el-form-item>
      <el-form-item label="本周总结" prop="weeklySummary">
        <el-input
          v-model="state.dataForm.weeklySummary"
          type="textarea"
          :rows="4"
          placeholder="请输入本周工作总结"
          maxlength="1000"
          show-word-limit></el-input>
      </el-form-item>
      <el-form-item label="下周计划" prop="nextWeekPlan">
        <el-input
          v-model="state.dataForm.nextWeekPlan"
          type="textarea"
          :rows="4"
          placeholder="请输入下周工作计划"
          maxlength="1000"
          show-word-limit></el-input>
      </el-form-item>
      <el-form-item label="遇到的问题" prop="problems">
        <el-input
          v-model="state.dataForm.problems"
          type="textarea"
          :rows="3"
          placeholder="请输入本周遇到的问题"
          maxlength="500"
          show-word-limit></el-input>
      </el-form-item>
      <el-form-item label="建议" prop="suggestions">
        <el-input
          v-model="state.dataForm.suggestions"
          type="textarea"
          :rows="3"
          placeholder="请输入工作建议"
          maxlength="500"
          show-word-limit></el-input>
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

const state = reactive({
  dataForm: {
    id: null,
    userId: null as number | null,
    username: '',
    weekStartDate: null,
    weekEndDate: null,
    weeklySummary: '',
    nextWeekPlan: '',
    problems: '',
    suggestions: ''
  },

  rules: {
    weekStartDate: [
      { required: true, message: '请选择周开始日期', trigger: 'change' }
    ],
    weekEndDate: [
      { required: true, message: '周结束日期不能为空', trigger: 'change' }
    ],
    weeklySummary: [
      { required: true, message: '请输入本周总结', trigger: 'blur' }
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
        ...state.dataForm
      };

      console.log('提交周报数据:', JSON.stringify(submitData, null, 2));

      const url = state.dataForm.id ? "/attendance/weeklyreport/update" : "/attendance/weeklyreport";
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

// 计算周结束日期（周开始日期 + 6天）
const calculateWeekEndDate = (startDate: string) => {
  if (!startDate) return;

  const start = new Date(startDate);
  const end = new Date(start);
  end.setDate(start.getDate() + 6);

  const year = end.getFullYear();
  const month = String(end.getMonth() + 1).padStart(2, '0');
  const day = String(end.getDate()).padStart(2, '0');

  state.dataForm.weekEndDate = `${year}-${month}-${day}`;
};

// 初始化数据
const init = (id?: number) => {
  console.log('初始化周报弹窗，ID:', id);
  console.log('当前用户:', currentUser.value);

  try {
    if (id) {
      // 编辑模式：获取现有数据
      baseService.get(`/attendance/weeklyreport/info/${id}`)
        .then((res) => {
          if (res.code === 0) {
            state.dataForm = {
              ...res.data,
              weekStartDate: res.data.weekStartDate || null,
              weekEndDate: res.data.weekEndDate || null
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
      // 新增模式：自动设置用户信息和默认日期
      const userId = currentUser.value?.id || currentUser.value?.userId;
      const username = currentUser.value?.username || currentUser.value?.name || '未知用户';

      // 设置默认日期为本周一和本周日
      const today = new Date();
      const dayOfWeek = today.getDay();
      const diffToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
      const monday = new Date(today);
      monday.setDate(today.getDate() + diffToMonday);
      const sunday = new Date(monday);
      sunday.setDate(monday.getDate() + 6);

      const formatDate = (date: Date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
      };

      state.dataForm = {
        id: null,
        userId: userId,
        username: username,
        weekStartDate: formatDate(monday),
        weekEndDate: formatDate(sunday),
        weeklySummary: '',
        nextWeekPlan: '',
        problems: '',
        suggestions: ''
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
