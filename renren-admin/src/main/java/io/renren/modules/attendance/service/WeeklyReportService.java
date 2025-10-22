package io.renren.modules.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.attendance.entity.WeeklyReportEntity;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

public interface WeeklyReportService extends IService<WeeklyReportEntity> {
    /**
     * 根据用户ID和周日期获取周报
     */
    WeeklyReportEntity getByUserIdAndWeek(Long userId, String weekStartDate);

    /**
     * 获取周报列表
     */
    Map<String, Object> getList(Map<String, Object> params);

    /**
     * 导出周报数据
     * @param response HTTP响应
     * @param params 查询参数
     */
    void exportWeeklyReport(HttpServletResponse response, Map<String, Object> params) throws Exception;
}