package io.renren.modules.attendance.controller;

import io.renren.modules.attendance.entity.WeeklyReportEntity;
import io.renren.modules.attendance.service.WeeklyReportService;
import io.renren.common.utils.ApiResponse;
import io.renren.modules.security.user.UserDetail;
import io.renren.modules.security.user.SecurityUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/attendance/weeklyreport")
public class WeeklyReportController {

    @Autowired
    private WeeklyReportService weeklyReportService;

    /**
     * 提交或更新周报
     */
    @PostMapping
    public ApiResponse<WeeklyReportEntity> submitWeeklyReport(@RequestBody WeeklyReportEntity weeklyReport) {
        try {
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            // 参数校验
            if (weeklyReport.getUserId() == null) {
                weeklyReport.setUserId(user.getId());
            } else if (!isAdmin && !weeklyReport.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限为其他用户提交周报");
            }

            if (weeklyReport.getWeekStartDate() == null) {
                return ApiResponse.error(400, "周开始日期不能为空");
            }

            if (weeklyReport.getWeekEndDate() == null) {
                return ApiResponse.error(400, "周结束日期不能为空");
            }

            if (StringUtils.isBlank(weeklyReport.getWeeklySummary())) {
                return ApiResponse.error(400, "本周总结不能为空");
            }

            // 检查是否已存在该用户在该周的周报
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            WeeklyReportEntity existingReport = weeklyReportService.getByUserIdAndWeek(
                    weeklyReport.getUserId(), sdf.format(weeklyReport.getWeekStartDate()));

            if (existingReport != null) {
                weeklyReport.setId(existingReport.getId());
                weeklyReport.setCreateTime(existingReport.getCreateTime());
            } else {
                weeklyReport.setCreateTime(new Date());
            }

            weeklyReport.setUpdateTime(new Date());
            weeklyReportService.saveOrUpdate(weeklyReport);

            String message = existingReport != null ? "更新成功" : "提交成功";
            return ApiResponse.success(message, weeklyReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取周报列表
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "weekStartDate", required = false) String weekStartDate) {

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

            if (weekStartDate != null && !weekStartDate.trim().isEmpty()) {
                params.put("weekStartDate", weekStartDate.trim());
            }

            Map<String, Object> data = weeklyReportService.getList(params);
            return ApiResponse.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取周报详情
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("attendance:weeklyreport:info")
    public ApiResponse<WeeklyReportEntity> getWeeklyReportInfo(@PathVariable("id") Long id) {
        try {
            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            WeeklyReportEntity weeklyReport = weeklyReportService.getById(id);
            return ApiResponse.success("查询成功", weeklyReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新周报
     */
    @PostMapping("/update")
    @RequiresPermissions("attendance:weeklyreport:update")
    public ApiResponse<WeeklyReportEntity> updateWeeklyReport(@RequestBody WeeklyReportEntity weeklyReport) {
        try {
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            if (weeklyReport.getId() == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            if (weeklyReport.getUserId() == null) {
                return ApiResponse.error(400, "用户ID不能为空");
            }

            if (!isAdmin && !weeklyReport.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限更新其他用户的周报");
            }

            if (StringUtils.isBlank(weeklyReport.getWeeklySummary())) {
                return ApiResponse.error(400, "本周总结不能为空");
            }

            weeklyReport.setUpdateTime(new Date());
            weeklyReportService.updateById(weeklyReport);
            return ApiResponse.success("更新成功", weeklyReport);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除周报
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("attendance:weeklyreport:delete")
    public ApiResponse<String> deleteWeeklyReport(@PathVariable("id") Long id) {
        try {
            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            WeeklyReportEntity report = weeklyReportService.getById(id);
            if (!isAdmin && !report.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限删除其他用户的周报");
            }

            weeklyReportService.removeById(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 导出周报数据
     */
    @PostMapping("/export")
    @RequiresPermissions("attendance:weeklyreport:export")
    public void exportWeeklyReport(HttpServletResponse response,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String weekStartDate,
                                   @RequestParam(required = false) String columns,
                                   @RequestParam(required = false) String exportScope,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer limit) {

        try {
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            Map<String, Object> params = new HashMap<>();

            // 权限控制
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

            if (weekStartDate != null && !weekStartDate.trim().isEmpty()) {
                params.put("weekStartDate", weekStartDate.trim());
            }

            // 添加导出范围参数
            if (exportScope != null) {
                params.put("exportScope", exportScope);
            }
            if (page != null) {
                params.put("page", page);
            }
            if (limit != null) {
                params.put("limit", limit);
            }
            if (columns != null) {
                params.put("columns", columns);
            }

            System.out.println("导出周报参数: " + params);
            weeklyReportService.exportWeeklyReport(response, params);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                // 清空之前的响应
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}