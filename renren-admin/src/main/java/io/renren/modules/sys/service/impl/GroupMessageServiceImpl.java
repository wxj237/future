// src/main/java/io/renren/modules/sys/service/impl/GroupMessageServiceImpl.java
package io.renren.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.sys.dao.GroupMessageDao;
import io.renren.modules.sys.entity.GroupMessageEntity;
import io.renren.modules.sys.service.GroupMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("groupMessageService")
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageDao, GroupMessageEntity> implements GroupMessageService {

    @Override
    public List<GroupMessageEntity> getGroupMessages(Long groupId) {
        return baseMapper.selectList(new QueryWrapper<GroupMessageEntity>()
                .eq("group_id", groupId)
                .orderByAsc("create_time"));
    }

    @Override
    public void sendMessage(GroupMessageEntity message) {
        this.save(message);
    }
}