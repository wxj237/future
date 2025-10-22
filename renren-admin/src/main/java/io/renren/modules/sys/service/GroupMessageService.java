// src/main/java/io/renren/modules/sys/service/GroupMessageService.java
package io.renren.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.sys.entity.GroupMessageEntity;

import java.util.List;
import java.util.Map;

public interface GroupMessageService extends IService<GroupMessageEntity> {

    List<GroupMessageEntity> getGroupMessages(Long groupId);

    void sendMessage(GroupMessageEntity message);
}