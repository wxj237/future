// io/renren/modules/attendance/dao/WeeklyReportDao.java
package io.renren.modules.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.attendance.entity.WeeklyReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

@Mapper
public interface WeeklyReportDao extends BaseMapper<WeeklyReportEntity> {
    /**
     * 根据用户ID和日期范围查询周报
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 周报列表
     */
    List<WeeklyReportEntity> selectByUserIdAndDateRange(@Param("userId") Long userId, 
                                                       @Param("startDate") Date startDate, 
                                                       @Param("endDate") Date endDate);
    
    /**
     * 根据用户ID和日期范围查询周报
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 周报实体
     */
    WeeklyReportEntity selectByUserIdAndDate(@Param("userId") Long userId, 
                                            @Param("startDate") Date startDate, 
                                            @Param("endDate") Date endDate);
}