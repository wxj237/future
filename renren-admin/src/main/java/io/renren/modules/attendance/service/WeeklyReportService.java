package io.renren.modules.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.attendance.dto.WeeklyReportDTO;
import io.renren.modules.attendance.entity.WeeklyReportEntity;
import java.util.List;
import java.util.Map;

public interface WeeklyReportService extends IService<WeeklyReportEntity> {

    /** 用户 + ISO 周（如 2025-W41）查单条 */
    WeeklyReportEntity getByUserIdAndWeek(Long userId, String weekIso);

    /** 分页返回：list + total */
    Map<String, Object> getList(Map<String, Object> params);

    /** 导出列表（不分页） */
    List<WeeklyReportDTO> list(Map<String, Object> params);

    /** 保存或更新（返回最新实体） */
    WeeklyReportEntity saveOrUpdateReturning(WeeklyReportEntity req);

    /** 以 userId + weekStartDate/weekEndDate 幂等 upsert */
    WeeklyReportEntity saveOrUpsertForUserAndWeek(WeeklyReportEntity req);

    /** 直接SQL删除方法（物理删除，绕过逻辑删除/插件） */
    boolean directDelete(Long id);
}
