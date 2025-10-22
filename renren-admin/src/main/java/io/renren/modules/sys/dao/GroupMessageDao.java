// src/main/java/io/renren/modules/sys/dao/GroupMessageDao.java
package io.renren.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.sys.entity.GroupMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMessageDao extends BaseMapper<GroupMessageEntity> {
}