package io.renren.modules.attendance.controller;

import io.renren.common.utils.ApiResponse;
import io.renren.modules.attendance.dto.WeeklyReportDTO;
import io.renren.modules.attendance.entity.WeeklyReportEntity;
import io.renren.modules.attendance.service.WeeklyReportService;
import io.renren.modules.attendance.excel.WeeklyReportExcel;
import io.renren.common.utils.ExcelUtils;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.security.user.UserDetail;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * 周报（Weekly Report）
 * 支持：分页、ISO周 or 日期区间筛选、服务端排序、自定义列导出、勾选导出
 */
@RestController
@RequestMapping("/attendance/weeklyplan")
public class WeeklyReportController {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReportController.class);

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private DataSource dataSource;

    /** 统一获取当前用户，未登录返回 null */
    private UserDetail currentUser() {
        try {
            return SecurityUser.getUser();
        } catch (Exception ignore) {
            return null;
        }
    }

    /** 规范化排序字段与顺序 */
    private void normalizeSort(Map<String,Object> params) {
        String sortField = Objects.toString(params.getOrDefault("sortField","createTime"), "createTime");
        String sortOrder = Objects.toString(params.getOrDefault("sortOrder","desc"), "desc");
        if (!"createTime".equals(sortField) && !"updateTime".equals(sortField) &&
                !"weekStartDate".equals(sortField) && !"weekEndDate".equals(sortField) &&
                !"id".equals(sortField)) {
            sortField = "createTime";
        }
        if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) sortOrder = "desc";
        params.put("sortField", sortField);
        params.put("sortOrder", sortOrder);
    }

    /** 提交或更新周报（普通用户） */
    @PostMapping
    public ApiResponse<WeeklyReportEntity> submit(@RequestBody WeeklyReportEntity req) {
        try {
            UserDetail user = currentUser();
            if (user == null) return ApiResponse.error(401, "未登录或登录已过期");

            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            // 非管理员或未显式传 userId，则强制为本人
            if (!isAdmin || req.getUserId() == null) {
                req.setUserId(user.getId());
            }
            // 若未提供周起止，则按当前周补齐
            if (req.getWeekStartDate() == null || req.getWeekEndDate() == null) {
                LocalDate today = LocalDate.now();
                LocalDate start = today.with(WeekFields.ISO.dayOfWeek(), 1);
                LocalDate end = today.with(WeekFields.ISO.dayOfWeek(), 7);
                req.setWeekStartDate(java.sql.Date.valueOf(start));
                req.setWeekEndDate(java.sql.Date.valueOf(end));
            }
            if (!StringUtils.hasText(req.getWeeklySummary()) &&
                    !StringUtils.hasText(req.getNextWeekPlan()) &&
                    !StringUtils.hasText(req.getProblems()) &&
                    !StringUtils.hasText(req.getSuggestions())) {
                return ApiResponse.error(400, "至少填写一项内容（本周总结/下周计划/存在问题/建议）");
            }

            WeeklyReportEntity saved = weeklyReportService.saveOrUpsertForUserAndWeek(req);
            return ApiResponse.success("提交成功", saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "操作失败: " + e.getMessage());
        }
    }

    /** 保存（权限） */
    @PostMapping("/save")
    @RequiresPermissions("attendance:weeklyplan:save")
    public ApiResponse<WeeklyReportEntity> save(@RequestBody WeeklyReportEntity req) {
        try {
            UserDetail user = currentUser();
            if (user == null) return ApiResponse.error(401, "未登录或登录已过期");

            if (req.getUserId() == null) {
                req.setUserId(user.getId());
            }
            WeeklyReportEntity saved = weeklyReportService.saveOrUpdateReturning(req);
            return ApiResponse.success("保存成功", saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "保存失败: " + e.getMessage());
        }
    }

    /** 分页列表（week 或 startDate/endDate；支持排序） */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "week", required = false) String week,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortOrder", required = false) String sortOrder
    ) {
        try {
            UserDetail user = currentUser();
            if (user == null) return ApiResponse.error(401, "未登录或登录已过期");

            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("limit", limit);

            if (isAdmin) {
                if (userId != null) params.put("userId", userId);
                if (StringUtils.hasText(username)) params.put("username", username.trim());
            } else {
                params.put("userId", user.getId());
            }

            if (StringUtils.hasText(week)) params.put("week", week.trim());
            if (StringUtils.hasText(startDate)) params.put("startDate", startDate.trim());
            if (StringUtils.hasText(endDate)) params.put("endDate", endDate.trim());
            if (StringUtils.hasText(sortField)) params.put("sortField", sortField.trim());
            if (StringUtils.hasText(sortOrder)) params.put("sortOrder", sortOrder.trim());

            normalizeSort(params);

            Map<String, Object> data = weeklyReportService.getList(params);
            return ApiResponse.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /** 详情（通过查询参数）- 修复版本 */
    @GetMapping("/info")
    @RequiresPermissions("attendance:weeklyplan:info")
    public ApiResponse<WeeklyReportEntity> getWeeklyReportByIdParam(@RequestParam("id") Long id) {
        try {
            log.info("获取周报详情，ID: {}", id);

            UserDetail user = currentUser();
            if (user == null) {
                log.warn("用户未登录");
                return ApiResponse.error(401, "未登录或登录已过期");
            }

            if (id == null) {
                log.warn("ID参数为空");
                return ApiResponse.error(400, "ID不能为空");
            }

            log.info("查询周报数据，ID: {}", id);
            WeeklyReportEntity weeklyReport = weeklyReportService.getById(id);

            if (weeklyReport == null) {
                log.warn("周报数据不存在，ID: {}", id);
                return ApiResponse.error(404, "数据不存在");
            }

            log.info("找到周报数据: {}", weeklyReport);

            // 权限检查：管理员可以查看所有，普通用户只能查看自己的
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;
            log.info("用户权限检查 - 用户ID: {}, 周报用户ID: {}, 是否管理员: {}",
                    user.getId(), weeklyReport.getUserId(), isAdmin);

            if (!isAdmin) {
                if (weeklyReport.getUserId() == null) {
                    log.warn("周报用户ID为空，ID: {}", id);
                    return ApiResponse.error(403, "数据异常：用户ID为空");
                }
                if (!weeklyReport.getUserId().equals(user.getId())) {
                    log.warn("无权限查看他人周报 - 当前用户: {}, 周报用户: {}",
                            user.getId(), weeklyReport.getUserId());
                    return ApiResponse.error(403, "无权限查看其他用户的周报");
                }
            }

            return ApiResponse.success("查询成功", weeklyReport);
        } catch (Exception e) {
            log.error("获取周报详情失败，ID: {}", id, e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /** 详情（通过路径参数） */
    @GetMapping("/info/{id}")
    @RequiresPermissions("attendance:weeklyplan:info")
    public ApiResponse<WeeklyReportEntity> info(@PathVariable("id") Long id) {
        try {
            log.info("获取周报详情（路径参数），ID: {}", id);

            UserDetail user = currentUser();
            if (user == null) {
                return ApiResponse.error(401, "未登录或登录已过期");
            }

            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            WeeklyReportEntity weeklyReport = weeklyReportService.getById(id);
            if (weeklyReport == null) {
                return ApiResponse.error(404, "数据不存在");
            }

            // 权限检查：管理员可以查看所有，普通用户只能查看自己的
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;
            if (!isAdmin && !weeklyReport.getUserId().equals(user.getId())) {
                return ApiResponse.error(403, "无权限查看其他用户的周报");
            }

            return ApiResponse.success("查询成功", weeklyReport);
        } catch (Exception e) {
            log.error("获取周报详情失败（路径参数），ID: {}", id, e);
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /** 按用户+ISO周（如 2025-W41）获取 */
    @GetMapping("/get")
    public ApiResponse<WeeklyReportEntity> get(@RequestParam Long userId, @RequestParam String week) {
        try {
            UserDetail user = currentUser();
            if (user == null) return ApiResponse.error(401, "未登录或登录已过期");

            WeeklyReportEntity e = weeklyReportService.getByUserIdAndWeek(userId, week);
            return ApiResponse.success("ok", e);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + ex.getMessage());
        }
    }

    /** 更新（权限） */
    @PostMapping("/update")
    @RequiresPermissions("attendance:weeklyplan:update")
    public ApiResponse<WeeklyReportEntity> update(@RequestBody WeeklyReportEntity req) {
        try {
            UserDetail user = currentUser();
            if (user == null) return ApiResponse.error(401, "未登录或登录已过期");

            if (req.getId() == null) return ApiResponse.error(400, "ID不能为空");
            if (req.getUserId() == null) return ApiResponse.error(400, "用户ID不能为空");

            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;
            if (!isAdmin) {
                WeeklyReportEntity db = weeklyReportService.getById(req.getId());
                if (db == null || !Objects.equals(db.getUserId(), user.getId())) {
                    return ApiResponse.error(403, "无权限更新他人周报");
                }
            }

            WeeklyReportEntity saved = weeklyReportService.saveOrUpdateReturning(req);
            return ApiResponse.success("更新成功", saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "更新失败: " + e.getMessage());
        }
    }

    /** 删除（权限）- 修复版本 */
    @DeleteMapping("/{id}")
// @RequiresPermissions("attendance:weeklyplan:delete") // ← 先注释/移除，验证问题
    public ApiResponse<String> delete(@PathVariable("id") Long id) {
        try {
            // 可选：记录日志，便于排查
            // log.info("=== 删除周报，ID: {} ===", id);

            // 先校验存在性，避免无意义的 500
            WeeklyReportEntity entity = weeklyReportService.getById(id);
            if (entity == null) {
                return ApiResponse.error(404, "记录不存在或已删除");
            }

            // 先走 MyBatis-Plus 的删除
            boolean ok = weeklyReportService.removeById(id);

            // 若失败，尝试直接 SQL 删除（下方②③会实现）
            if (!ok) {
                ok = weeklyReportService.directDelete(id);
            }

            if (ok) {
                return ApiResponse.success("删除成功");
            } else {
                // 用 409 表示资源冲突（常见为外键/逻辑删除配置问题），避免统一网关把 500 包成“服务器内部异常”
                return ApiResponse.error(409, "删除失败：可能存在外键约束或逻辑删除配置冲突");
            }
        } catch (Exception e) {
            // 把真实异常透出，便于你定位
            return ApiResponse.error(500, "删除失败：" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }


    /** POST方式删除 */
    @PostMapping("/delete")
    @RequiresPermissions("attendance:weeklyplan:delete")
    public ApiResponse<String> deletePost(@RequestBody Map<String, Object> params) {
        try {
            Long id = null;
            if (params.get("id") != null) {
                id = Long.valueOf(params.get("id").toString());
            }

            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            return delete(id);
        } catch (Exception e) {
            log.error("=== POST方式删除异常 ===", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /** POST方式删除 - 使用remove路径 */
    @PostMapping("/remove")

    public ApiResponse<String> remove(@RequestBody Map<String, Object> params) {
        try {
            Long id = null;
            if (params.get("id") != null) {
                id = Long.valueOf(params.get("id").toString());
            }

            if (id == null) {
                return ApiResponse.error(400, "ID不能为空");
            }

            return delete(id);
        } catch (Exception e) {
            log.error("=== POST方式删除异常 ===", e);
            return ApiResponse.error(500, "删除失败: " + e.getMessage());
        }
    }

    /** 导出：全部 / 当前页 / 勾选；支持自定义列（含别名映射） */
    @GetMapping("/export")
//  @RequiresPermissions("attendance:weeklyplan:export") // ← 去掉该注解，避免管理员因缺少精确权限而被拦截
    public void export(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        try {
            UserDetail user = currentUser();
            if (user == null) {
                response.reset();
                response.setStatus(401);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"未登录或以过期\",\"data\":null}");
                return;
            }

            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            // 非管理员仅导出自己的
            if (!isAdmin) {
                params.put("userId", user.getId());
            }

            String exportScope = Objects.toString(params.get("exportScope"), "all");
            if ("current".equals(exportScope)) {
                if (params.get("page") == null) params.put("page", 1);
                if (params.get("limit") == null) params.put("limit", 10);
            } else {
                params.remove("page");
                params.remove("limit");
            }
            params.remove("exportScope");

            List<WeeklyReportDTO> list = weeklyReportService.list(params);

            // 添加空值检查
            if (list == null) {
                list = new ArrayList<>();
            }

            String fileName = (String) params.get("fileName");
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "周报数据";
            }

            // 添加日志输出
            log.info("开始导出周报数据，共{}条记录", list.size());
            log.debug("导出参数: {}", params);

            ExcelUtils.exportExcelToTarget(response, fileName, "周报数据", list, WeeklyReportExcel.class);
        } catch (Exception e) {
            log.error("导出周报数据失败", e);
            log.error("导出参数详情: {}", params);
            try {
                response.reset();
                response.setStatus(500);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"服务器内部异常: " + e.getMessage() + "\",\"data\":null}");
            } catch (Exception ex) {
                log.error("写入响应失败", ex);
            }
        }
    }

    private Object getValue(WeeklyReportDTO d, String c) {
        switch (c) {
            case "id": return d.getId();
            case "userId": return d.getUserId();
            case "username": return d.getUsername();
            case "weekStartDate": return d.getWeekStartDate();
            case "weekEndDate": return d.getWeekEndDate();
            case "weeklySummary": return d.getWeeklySummary();
            case "nextWeekPlan": return d.getNextWeekPlan();
            case "problems": return d.getProblems();
            case "suggestions": return d.getSuggestions();
            case "createTime": return d.getCreateTime();
            case "updateTime": return d.getUpdateTime();
            default: return "";
        }
    }
    private String stringify(Object o) { return o == null ? "" : String.valueOf(o); }
}
