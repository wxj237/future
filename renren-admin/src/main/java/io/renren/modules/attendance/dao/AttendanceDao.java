// io/renren/modules/attendance/dao/AttendanceDao.java
package io.renren.modules.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.attendance.entity.AttendanceEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendanceDao extends BaseMapper<AttendanceEntity> {
}
