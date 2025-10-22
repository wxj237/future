package io.renren.modules.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.attendance.entity.DailyPlanEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyPlanDao extends BaseMapper<DailyPlanEntity> {
    // MyBatis-Plus已经提供了基本的CRUD操作，
    // 如果需要自定义查询，可以在这里添加方法
}