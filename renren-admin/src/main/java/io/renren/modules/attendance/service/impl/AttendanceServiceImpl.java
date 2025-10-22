package io.renren.modules.attendance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.attendance.dao.AttendanceDao;
import io.renren.modules.attendance.entity.AttendanceEntity;
import io.renren.modules.attendance.service.AttendanceService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceDao, AttendanceEntity> implements AttendanceService {
    
    @Override
    public Map<String, Object> getAttendanceStatistics(Long userId, String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        QueryWrapper<AttendanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("create_time", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("create_time", endDate);
        }
        
        List<AttendanceEntity> records = this.list(queryWrapper);
        
        int totalDays = records.size();
        int signedInDays = 0;
        int signedOutDays = 0;
        
        for (AttendanceEntity record : records) {
            if (record.getSignInTime() != null) {
                signedInDays++;
            }
            if (record.getSignOutTime() != null) {
                signedOutDays++;
            }
        }
        
        statistics.put("totalDays", totalDays);
        statistics.put("signedInDays", signedInDays);
        statistics.put("signedOutDays", signedOutDays);
        statistics.put("absentDays", totalDays - signedInDays);
        
        return statistics;
    }
    
    @Override
    public List<AttendanceEntity> getAttendanceList(Long userId, String startDate, String endDate) {
        QueryWrapper<AttendanceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (startDate != null && !startDate.isEmpty()) {
            queryWrapper.ge("create_time", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            queryWrapper.le("create_time", endDate);
        }
        queryWrapper.orderByDesc("create_time");
        
        return this.list(queryWrapper);
    }
}