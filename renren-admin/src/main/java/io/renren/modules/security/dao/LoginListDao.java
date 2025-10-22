package io.renren.modules.security.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.security.entity.LoginListEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginListDao extends BaseMapper<LoginListEntity> {
    LoginListEntity getByStudentId(String studentId);
}
