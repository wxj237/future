// DailyPlanService.java
package io.renren.modules.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.attendance.entity.DailyPlanEntity;
import io.renren.modules.attendance.dto.DailyPlanDTO;

import java.util.List;
import java.util.Map;

/**
 * 每日工作计划服务接口
 */
public interface DailyPlanService extends IService<DailyPlanEntity> {
    /**
     * 根据用户ID和日期获取工作计划
     * @param userId 用户ID
     * @param planDate 计划日期
     * @return 工作计划实体
     */
    DailyPlanEntity getByUserIdAndDate(Long userId, String planDate);

    /**
     * 获取工作计划列表
     * @param params 查询参数
     * @return 工作计划列表
     */
    Map<String, Object> getList(Map<String, Object> params);

    /**
     * 获取日报列表（用于导出）
     * @param params 查询参数
     * @return 日报DTO列表
     */
    List<DailyPlanDTO> list(Map<String, Object> params);
}