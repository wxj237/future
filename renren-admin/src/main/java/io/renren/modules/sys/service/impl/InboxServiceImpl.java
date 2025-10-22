package io.renren.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.sys.dao.InboxDao;
import io.renren.modules.sys.entity.InboxEntity;
import io.renren.modules.sys.service.InboxService;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service("inboxService")
public class InboxServiceImpl extends ServiceImpl<InboxDao, InboxEntity> implements InboxService {

    @Override
    public void sendToGroup(InboxEntity inbox) {
        this.save(inbox);
    }

    @Override
    public void sendToUsers(InboxEntity inbox) {
        this.save(inbox);
    }

    @Override
    public void sendToAllUsers(InboxEntity inbox) {
        this.save(inbox);
    }

    @Override
    public void markAsRead(Long inboxId) {
        InboxEntity inbox = this.getById(inboxId);
        if (inbox != null) {
            inbox.setReadStatus(1);
            this.updateById(inbox);
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        return baseMapper.getUnreadCount(userId);
    }
}