package io.renren.modules.attendance.controller;

import io.renren.modules.attendance.entity.DailyPlanEntity;
import io.renren.modules.attendance.service.DailyPlanService;
import io.renren.common.utils.ApiResponse;
import io.renren.modules.attendance.dto.DailyPlanDTO;
import io.renren.modules.attendance.excel.DailyPlanExcel;
import io.renren.common.utils.ExcelUtils;
import io.renren.modules.security.user.UserDetail;
import io.renren.modules.security.user.SecurityUser;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/attendance/dailyplan")
public class DailyPlanController {

    @Autowired
    private DailyPlanService dailyPlanService;

    /**
     * 提交或更新每日工作计划 (供普通用户使用)
     */
    @PostMapping
    public ApiResponse<DailyPlanEntity> submitDailyPlan(@RequestBody DailyPlanEntity dailyPlan) {
        try {
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            if (dailyPlan.getUserId() == null) {
                dailyPlan.setUserId(user.getId());
            } else if (!isAdmin && !dailyPlan.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限为其他用户提交日报");
            }

            if (dailyPlan.getPlanDate() == null) {
                return ApiResponse.error(400, "计划日期不能为空");
            }

            if (dailyPlan.getContent() == null || dailyPlan.getContent().trim().isEmpty()) {
                return ApiResponse.error(400, "工作内容不能为空");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            DailyPlanEntity existingPlan = dailyPlanService.getByUserIdAndDate(dailyPlan.getUserId(),
                    sdf.format(dailyPlan.getPlanDate()));

            if (existingPlan != null) {
                dailyPlan.setId(existingPlan.getId());
                dailyPlan.setCreateTime(existingPlan.getCreateTime());
            } else {
                dailyPlan.setCreateTime(new Date());
            }

            dailyPlan.setUpdateTime(new Date());
            dailyPlanService.saveOrUpdate(dailyPlan);

            String message = existingPlan != null ? "更新成功" : "提交成功";
            return ApiResponse.success(message, dailyPlan);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 创建或更新每日工作计划
     */
    @PostMapping("/save")
    @RequiresPermissions("attendance:dailyplan:save")
    public ApiResponse<DailyPlanEntity> saveDailyPlan(@RequestBody DailyPlanEntity dailyPlan) {
        try {
            UserDetail user = SecurityUser.getUser();
            dailyPlan.setUserId(user.getId());

            if (dailyPlan.getContent() == null || dailyPlan.getContent().trim().isEmpty()) {
                return ApiResponse.error(400, "工作内容不能为空");
            }

            if (dailyPlan.getId() == null) {
                dailyPlan.setCreateTime(new Date());
            }
            dailyPlan.setUpdateTime(new Date());

            dailyPlanService.saveOrUpdate(dailyPlan);
            return ApiResponse.success("保存成功", dailyPlan);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取工作计划列表（支持：分页、单日、日期区间、排序）
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "planDate", required = false) String planDate,
            // 新增支持：日期区间 + 排序
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortOrder", required = false) String sortOrder
    ) {
        try {
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("limit", limit);

            if (isAdmin) {
                if (userId != null) {
                    params.put("userId", userId);
                }
                if (username != null && !username.trim().isEmpty()) {
                    params.put("username", username.trim());
                }
            } else {
                params.put("userId", user.getId());
            }

            if (planDate != null && !planDate.trim().isEmpty()) {
                params.put("planDate", planDate.trim());
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                params.put("startDate", startDate.trim());
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                params.put("endDate", endDate.trim());
            }
            if (sortField != null && !sortField.trim().isEmpty()) {
                params.put("sortField", sortField.trim());
            }
            if (sortOrder != null && !sortOrder.trim().isEmpty()) {
                params.put("sortOrder", sortOrder.trim());
            }

            Map<String, Object> data = dailyPlanService.getList(params);
            return ApiResponse.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID和日期获取工作计划
     */
    @GetMapping("/get")
    public ApiResponse<DailyPlanEntity> getDailyPlanByUserIdAndDate(
            @RequestParam Long userId,
            @RequestParam String planDate) {
        try {
            if (userId == null) {
                return ApiResponse.error(400, "用户ID不能为空");
            }
            if (planDate == null || planDate.trim().isEmpty()) {
                return ApiResponse.error(400, "计划日期不能为空");
            }
            DailyPlanEntity dailyPlan = dailyPlanService.getByUserIdAndDate(userId, planDate);
            if (dailyPlan != null) {
                return ApiResponse.success("查询成功", dailyPlan);
            } else {
                return ApiResponse.success("未找到指定日期的工作计划", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取工作计划信息（通过路径参数）
     */
    @GetMapping("/info/{id}")
    public ApiResponse<DailyPlanEntity> getDailyPlanById(@PathVariable("id") Long id) {
        try {
            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            DailyPlanEntity dailyPlan = dailyPlanService.getById(id);
            if (dailyPlan == null) {
                return ApiResponse.error(404, "数据不存在");
            }

            // 权限检查：管理员可以查看所有，普通用户只能查看自己的
            if (!isAdmin && !dailyPlan.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限查看其他用户的日报");
            }

            return ApiResponse.success("查询成功", dailyPlan);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取工作计划信息（通过查询参数）- 新增接口
     */
    @GetMapping("/info")
    public ApiResponse<DailyPlanEntity> getDailyPlanByIdParam(@RequestParam("id") Long id) {
        try {
            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            DailyPlanEntity dailyPlan = dailyPlanService.getById(id);
            if (dailyPlan == null) {
                return ApiResponse.error(404, "数据不存在");
            }

            // 权限检查：管理员可以查看所有，普通用户只能查看自己的
            if (!isAdmin && !dailyPlan.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限查看其他用户的日报");
            }

            return ApiResponse.success("查询成功", dailyPlan);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新工作计划
     */
    @PostMapping("/update")
    @RequiresPermissions("attendance:dailyplan:update")
    public ApiResponse<DailyPlanEntity> updateDailyPlan(@RequestBody DailyPlanEntity dailyPlan) {
        try {
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            if (dailyPlan.getId() == null) {
                return ApiResponse.error(400, "ID不能为空");
            }
            if (dailyPlan.getUserId() == null) {
                return ApiResponse.error(400, "用户ID不能为空");
            }
            if (!isAdmin && !dailyPlan.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限更新其他用户的日报");
            }
            if (dailyPlan.getContent() == null || dailyPlan.getContent().trim().isEmpty()) {
                return ApiResponse.error(400, "工作内容不能为空");
            }

            dailyPlan.setUpdateTime(new Date());
            dailyPlanService.updateById(dailyPlan);
            return ApiResponse.success("更新成功", dailyPlan);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除工作计划
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("attendance:dailyplan:delete")
    public ApiResponse<String> deleteDailyPlan(@PathVariable("id") Long id) {
        try {
            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            DailyPlanEntity plan = dailyPlanService.getById(id);
            if (!isAdmin && !plan.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限删除其他用户的日报");
            }

            dailyPlanService.removeById(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 导出日报数据（标准/全部/当前页）
     */
    @GetMapping("/export")
    @RequiresPermissions("attendance:dailyplan:export")
    public void export(@RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        UserDetail user = SecurityUser.getUser();
        boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

        // 非管理员只能导出自己的数据
        if (!isAdmin) {
            params.put("userId", user.getId());
        }

        String exportScope = (String) params.get("exportScope");
        if ("current".equals(exportScope)) {
            if (params.get("page") == null) params.put("page", 1);
            if (params.get("limit") == null) params.put("limit", 10);
        } else {
            params.remove("page");
            params.remove("limit");
        }
        params.remove("exportScope");

        List<DailyPlanDTO> list = dailyPlanService.list(params);

        String fileName = (String) params.get("fileName");
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "日报数据";
        }

        ExcelUtils.exportExcelToTarget(response, null, fileName, list, DailyPlanExcel.class);
    }
}