// src/main/java/io/renren/modules/sys/service/GroupService.java
package io.renren.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.sys.entity.GroupEntity;

import java.util.List;
import java.util.Map;

public interface GroupService extends IService<GroupEntity> {

    List<GroupEntity> queryList(Map<String, Object> params);

    void saveGroup(GroupEntity group);

    void updateGroup(GroupEntity group);

    void deleteBatch(Long[] groupIds);
}