package io.renren.modules.attendance.controller;

import io.renren.common.utils.ApiResponse;
import io.renren.common.utils.ExcelUtils;
import io.renren.modules.attendance.dto.AttendanceGeoDTO;
import io.renren.modules.attendance.entity.AttendanceGeoEntity;
import io.renren.modules.attendance.excel.AttendanceGeoExcel;
import io.renren.modules.attendance.service.AttendanceGeoService;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.security.user.UserDetail;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@RequestMapping("/attendance/geo")
public class AttendanceGeoController {

    private final AttendanceGeoService attendanceGeoService;

    public AttendanceGeoController(AttendanceGeoService attendanceGeoService) {
        this.attendanceGeoService = attendanceGeoService;
    }

    /**
     * 定位签到接口 - 修改为允许一天内多次签到
     */
    @PostMapping("/signin")
    public ApiResponse<AttendanceGeoEntity> locationSignIn(@RequestBody(required = false) Map<String, Object> params) {
        try {
            // 检查参数是否存在
            if (params == null) {
                return ApiResponse.error(400, "请求体不能为空");
            }

            // 检查userId参数
            if (!params.containsKey("userId") || params.get("userId") == null) {
                return ApiResponse.error(400, "缺少必要参数userId");
            }

            String userIdStr = params.get("userId").toString().trim();
            if (StringUtils.isBlank(userIdStr)) {
                return ApiResponse.error(400, "用户ID不能为空");
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return ApiResponse.error(400, "用户ID格式不正确");
            }

            // 检查位置信息参数
            if (!params.containsKey("longitude") || params.get("longitude") == null) {
                return ApiResponse.error(400, "缺少必要参数longitude");
            }

            if (!params.containsKey("latitude") || params.get("latitude") == null) {
                return ApiResponse.error(400, "缺少必要参数latitude");
            }

            Double longitude;
            Double latitude;
            try {
                longitude = Double.parseDouble(params.get("longitude").toString());
                latitude = Double.parseDouble(params.get("latitude").toString());
            } catch (NumberFormatException e) {
                return ApiResponse.error(400, "经纬度格式不正确");
            }

            // 获取地址信息 - 修复：优先使用address参数
            String address = (String) params.get("address");
            if (StringUtils.isBlank(address)) {
                address = (String) params.get("locationDescription");
            }
            if (StringUtils.isBlank(address)) {
                address = "未知位置";
            }

            // 创建新的签到记录（允许一天内多次签到）
            AttendanceGeoEntity attendance = new AttendanceGeoEntity();
            attendance.setUserId(userId);
            attendance.setLongitude(longitude);
            attendance.setLatitude(latitude);
            attendance.setSignTime(new Date());
            // 设置默认值
            attendance.setResult(1); // 默认签到成功
            attendance.setReason(address); // 保存地址信息到reason字段（兼容历史）
            attendance.setAddress(address); // ✅ 新增：同时保存到address字段
            attendance.setPointId(null); // 设置 point_id 为 null（允许为空）

            attendanceGeoService.save(attendance);

            return ApiResponse.success("签到成功", attendance);
        } catch (Exception e) {
            return ApiResponse.error(500, "签到失败: " + e.getMessage());
        }
    }

    /**
     * 分页列表（与日报风格一致）
     * GET /attendance/geo/list?page=1&limit=10&userId=&username=&signDate=&startDate=&endDate=&sortField=&sortOrder=
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "signDate", required = false) String signDate,
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
                if (userId != null) params.put("userId", userId);
                if (username != null && !username.trim().isEmpty()) {
                    params.put("username", username.trim());
                }
            } else {
                params.put("userId", user.getId());
            }

            if (signDate != null && !signDate.trim().isEmpty()) params.put("signDate", signDate.trim());
            if (startDate != null && !startDate.trim().isEmpty()) params.put("startDate", startDate.trim());
            if (endDate != null && !endDate.trim().isEmpty()) params.put("endDate", endDate.trim());
            if (sortField != null && !sortField.trim().isEmpty()) params.put("sortField", sortField.trim());
            if (sortOrder != null && !sortOrder.trim().isEmpty()) params.put("sortOrder", sortOrder.trim());

            Map<String, Object> data = attendanceGeoService.getList(params);
            return ApiResponse.success("查询成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 导出（支持 exportScope=current/all，与日报一致）
     * GET /attendance/geo/export?exportScope=current&page=1&limit=10&...（或 exportScope=all）
     */
    @GetMapping("/export")
    @RequiresPermissions("attendance:geo:export")
    public void export(@RequestParam Map<String, Object> params, HttpServletResponse response) {
        try {
            UserDetail user = SecurityUser.getUser();
            boolean isAdmin = user.getSuperAdmin() == 1 || user.getId() == 1L;

            // 非管理员只能导出自己的数据
            if (!isAdmin) {
                params.put("userId", user.getId());
            }

            Object exportScope = params.get("exportScope");
            if (exportScope != null && "current".equals(exportScope.toString())) {
                if (params.get("page") == null) params.put("page", 1);
                if (params.get("limit") == null) params.put("limit", 10);
            } else {
                params.remove("page");
                params.remove("limit");
            }
            params.remove("exportScope");

            List<AttendanceGeoDTO> list = attendanceGeoService.list(params);
            if (list == null) {
                list = Collections.emptyList();
            }

            String fileName = (String) params.get("fileName");
            if (StringUtils.isBlank(fileName)) {
                fileName = "定位签到数据";
            }
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

            // 正确的下载响应头（xlsx）
            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded + ".xlsx");

            // 写出 Excel
            ExcelUtils.exportExcelToTarget(response, null, fileName, list, AttendanceGeoExcel.class);
        } catch (Exception e) {
            // 避免与已写入文件流冲突，不再写 JSON，仅设置状态码
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}