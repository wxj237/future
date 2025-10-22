package io.renren.modules.attendance.controller;

import io.renren.modules.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/attendance/statistics")
public class AttendanceStatisticsController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 获取考勤统计信息
     */
    @GetMapping("/summary")
    public Map<String, Object> getAttendanceStatistics(@RequestParam Long userId,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> statistics = attendanceService.getAttendanceStatistics(userId, startDate, endDate);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", statistics);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取考勤记录列表
     */
    @GetMapping("/list")
    public Map<String, Object> getAttendanceList(@RequestParam Long userId,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var attendanceList = attendanceService.getAttendanceList(userId, startDate, endDate);
            
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", attendanceList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        
        return result;
    }
}