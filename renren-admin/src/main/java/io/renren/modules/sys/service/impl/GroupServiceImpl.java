// src/main/java/io/renren/modules/sys/service/impl/GroupServiceImpl.java
package io.renren.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.sys.dao.GroupDao;
import io.renren.modules.sys.entity.GroupEntity;
import io.renren.modules.sys.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("groupService")
public class GroupServiceImpl extends ServiceImpl<GroupDao, GroupEntity> implements GroupService {

    @Override
    public List<GroupEntity> queryList(Map<String, Object> params) {
        return baseMapper.selectList(new QueryWrapper<GroupEntity>().orderByAsc("sort"));
    }

    @Override
    public void saveGroup(GroupEntity group) {
        this.save(group);
    }

    @Override
    public void updateGroup(GroupEntity group) {
        this.updateById(group);
    }

    @Override
    public void deleteBatch(Long[] groupIds) {
        for(Long groupId : groupIds) {
            this.removeById(groupId);
        }
    }
}