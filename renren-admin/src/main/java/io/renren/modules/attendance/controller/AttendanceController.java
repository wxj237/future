// io/renren/modules/attendance/controller/AttendanceController.java
package io.renren.modules.attendance.controller;

import io.renren.modules.attendance.entity.AttendanceEntity;
import io.renren.modules.attendance.service.AttendanceService;
import io.renren.common.utils.ApiResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@RestController
@RequestMapping("/attendance/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 测试接口 - 直接在浏览器访问
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "考勤模块测试成功！");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 手动签到
     */
    @PostMapping("/signin/manual")
    public ApiResponse<AttendanceEntity> manualSignIn(@RequestBody(required = false) Map<String, Object> params) {
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
            
            // 检查位置信息
            String location = (String) params.get("location");
            if (StringUtils.isBlank(location)) {
                return ApiResponse.error(400, "位置信息不能为空");
            }

            // 检查是否已经签到
            QueryWrapper<AttendanceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("status", 0); // 未签退
            queryWrapper.apply("DATE(create_time) = CURDATE()");
            
            AttendanceEntity existingRecord = attendanceService.getOne(queryWrapper);
            if (existingRecord != null) {
                return ApiResponse.error(400, "今日已签到，请勿重复签到");
            }

            AttendanceEntity attendance = new AttendanceEntity();
            attendance.setUserId(userId);
            attendance.setSignInTime(new Date());
            attendance.setSignInType("manual");
            attendance.setSignInLocation(location); // 添加位置信息
            attendance.setStatus(0); // 未签退
            attendance.setCreateTime(new Date());

            attendanceService.save(attendance);

            return ApiResponse.success("手动签到成功", attendance);
        } catch (Exception e) {
            return ApiResponse.error(500, "签到失败: " + e.getMessage());
        }
    }

    /**
     * 定位签到
     */
    @PostMapping("/signin/location")
    public ApiResponse<AttendanceEntity> locationSignIn(@RequestBody(required = false) Map<String, Object> params) {
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
            
            // 检查location参数
            if (!params.containsKey("location") || params.get("location") == null) {
                return ApiResponse.error(400, "缺少必要参数location");
            }
            
            String location = params.get("location").toString().trim();
            if (StringUtils.isBlank(location)) {
                return ApiResponse.error(400, "位置信息不能为空");
            }

            // 检查是否已经签到
            QueryWrapper<AttendanceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("status", 0); // 未签退
            queryWrapper.apply("DATE(create_time) = CURDATE()");
            
            AttendanceEntity existingRecord = attendanceService.getOne(queryWrapper);
            if (existingRecord != null) {
                return ApiResponse.error(400, "今日已签到，请勿重复签到");
            }

            AttendanceEntity attendance = new AttendanceEntity();
            attendance.setUserId(userId);
            attendance.setSignInTime(new Date());
            attendance.setSignInType("location");
            attendance.setSignInLocation(location);
            attendance.setStatus(0); // 未签退
            attendance.setCreateTime(new Date());

            attendanceService.save(attendance);

            return ApiResponse.success("定位签到成功", attendance);
        } catch (Exception e) {
            return ApiResponse.error(500, "签到失败: " + e.getMessage());
        }
    }

    /**
     * 签退
     */
    @PostMapping("/signout")
    public ApiResponse<AttendanceEntity> signOut(@RequestBody(required = false) Map<String, Object> params) {
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
            
            // 检查location参数
            if (!params.containsKey("location") || params.get("location") == null) {
                return ApiResponse.error(400, "缺少必要参数location");
            }
            
            String location = params.get("location").toString().trim();
            if (StringUtils.isBlank(location)) {
                return ApiResponse.error(400, "位置信息不能为空");
            }

            // 查找该用户今天的未签退记录
            QueryWrapper<AttendanceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("status", 0); // 未签退
            queryWrapper.apply("DATE(create_time) = CURDATE()");
            
            AttendanceEntity attendance = attendanceService.getOne(queryWrapper);
            if (attendance == null) {
                return ApiResponse.error(400, "未找到今日签到记录，请先签到");
            }

            attendance.setSignOutTime(new Date());
            attendance.setSignOutLocation(location);
            attendance.setStatus(1); // 已签退

            attendanceService.updateById(attendance);

            return ApiResponse.success("签退成功", attendance);
        } catch (Exception e) {
            return ApiResponse.error(500, "签退失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日考勤状态
     */
    @GetMapping("/today")
    public ApiResponse<AttendanceEntity> getTodayAttendance(@RequestParam Long userId) {
        try {
            // 参数校验
            if (userId == null) {
                return ApiResponse.error(400, "用户ID不能为空");
            }
            
            // 查询用户今天的考勤记录
            QueryWrapper<AttendanceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.apply("DATE(create_time) = CURDATE()");
            
            AttendanceEntity attendance = attendanceService.getOne(queryWrapper);
            
            // 当没有考勤记录时，返回null数据而不是错误
            return ApiResponse.success("查询成功", attendance);
        } catch (Exception e) {
            return ApiResponse.error(500, "查询失败: " + e.getMessage());
        }
    }
}