package io.renren.modules.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.attendance.entity.AttendanceEntity;
import java.util.List;
import java.util.Map;

public interface AttendanceService extends IService<AttendanceEntity> {
    /**
     * 统计用户考勤情况
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤统计结果
     */
    Map<String, Object> getAttendanceStatistics(Long userId, String startDate, String endDate);
    
    /**
     * 获取用户考勤记录列表
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<AttendanceEntity> getAttendanceList(Long userId, String startDate, String endDate);
}