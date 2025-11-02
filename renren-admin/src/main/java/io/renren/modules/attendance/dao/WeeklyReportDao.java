package io.renren.modules.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.attendance.entity.WeeklyReportEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
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

    // === 直连物理删除 ===
    /**
     * 直接通过ID删除周报
     * @param id 周报ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM weekly_report WHERE id = #{id}")
    int directDeleteById(@Param("id") Long id);
}
