// src/main/java/io/renren/modules/sys/dao/GroupDao.java
package io.renren.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.sys.entity.GroupEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupDao extends BaseMapper<GroupEntity> {
}