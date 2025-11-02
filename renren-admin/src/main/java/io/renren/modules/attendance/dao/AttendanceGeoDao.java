package io.renren.modules.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.attendance.entity.AttendanceGeoEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendanceGeoDao extends BaseMapper<AttendanceGeoEntity> { }
