<template>
  <el-dialog v-model="visible" :title="title" width="720px" :close-on-click-modal="false">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
      <el-form-item label="用户ID" prop="userId" v-if="!!form.userId">
        <el-input v-model="form.userId" placeholder="仅管理员编辑他人数据时使用" />
      </el-form-item>

      <el-form-item label="周起止" required>
        <el-date-picker
          v-model="weekRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          unlink-panels
          style="width: 320px"
        />
      </el-form-item>

      <el-form-item label="本周总结" prop="weeklySummary">
        <el-input v-model="form.weeklySummary" type="textarea" :rows="4" placeholder="本周工作总结" />
      </el-form-item>

      <el-form-item label="下周计划" prop="nextWeekPlan">
        <el-input v-model="form.nextWeekPlan" type="textarea" :rows="4" placeholder="下周计划" />
      </el-form-item>

      <el-form-item label="存在问题" prop="problems">
        <el-input v-model="form.problems" type="textarea" :rows="3" placeholder="遇到的问题" />
      </el-form-item>

      <el-form-item label="建议" prop="suggestions">
        <el-input v-model="form.suggestions" type="textarea" :rows="3" placeholder="改进建议" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="submit">保 存</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import baseService from '@/service/baseService'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const title = ref('新增周报')
const formRef = ref()

const form = reactive<any>({
  id: undefined,
  userId: undefined,
  weekStartDate: '',
  weekEndDate: '',
  weeklySummary: '',
  nextWeekPlan: '',
  problems: '',
  suggestions: ''
})

const weekRange = ref<string[]>([])
const rules = {}

/** 统一成功判断 */
function isOk(res: any) {
  return !!res && (res.code === 0 || res.code === 200 || res.success === true || res.status === 0)
}

/** 用行数据直接回填（避免 info 500） */
function fillFromRow(row: any) {
  form.id = row.id
  form.userId = row.userId
  form.weekStartDate = fmtDate(row.weekStartDate)
  form.weekEndDate = fmtDate(row.weekEndDate)
  form.weeklySummary = row.weeklySummary || ''
  form.nextWeekPlan = row.nextWeekPlan || ''
  form.problems = row.problems || ''
  form.suggestions = row.suggestions || ''
  weekRange.value = [form.weekStartDate, form.weekEndDate]
}

/** 对外： open（支持 row 或 id） */
function init(payload?: number | Record<string, any>) {
  reset()
  if (!payload) {
    title.value = '新增周报'
    visible.value = true
    return
  }
  if (typeof payload === 'object') {
    title.value = '修改周报'
    fillFromRow(payload)
    visible.value = true
    return
  }
  title.value = '修改周报'
  const id = payload
  baseService.get('/attendance/weeklyplan/info', { id }).then((res: any) => {
    if (isOk(res) && res.data) {
      fillFromRow(res.data)
      visible.value = true
    } else {
      ElMessage.error(res.msg || '获取周报详情失败')
    }
  }).catch((e: any) => {
    console.error(e)
    ElMessage.error('网络异常或服务器错误')
  })
}

/** 提交（新增或更新，后端 upsert） */
function submit() {
  if (!weekRange.value || weekRange.value.length !== 2) {
    ElMessage.warning('请选择周起止日期')
    return
  }
  form.weekStartDate = weekRange.value[0]
  form.weekEndDate = weekRange.value[1]
  baseService.post('/attendance/weeklyplan', { ...form }).then((res: any) => {
    if (isOk(res)) {
      ElMessage.success('保存成功')
      visible.value = false
      emit('refreshDataList')
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  }).catch((e: any) => {
    console.error(e)
    ElMessage.error('网络异常或服务器错误')
  })
}

/** 工具 */
function reset() {
  form.id = undefined
  form.userId = undefined
  form.weekStartDate = ''
  form.weekEndDate = ''
  form.weeklySummary = ''
  form.nextWeekPlan = ''
  form.problems = ''
  form.suggestions = ''
  weekRange.value = []
}
function fmtDate(d: string | Date) {
  if (!d) return ''
  const s = typeof d === 'string' ? d : (d as Date).toString()
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s
  return s.replace('T', ' ').split(' ')[0] || ''
}

defineExpose({ init })
</script>

<style scoped>
/* 可按需补充样式 */
</style>
