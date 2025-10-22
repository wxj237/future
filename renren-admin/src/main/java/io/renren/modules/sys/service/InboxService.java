package io.renren.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.sys.entity.InboxEntity;
import java.util.Map;

public interface InboxService extends IService<InboxEntity> {

    void sendToGroup(InboxEntity inbox);
    void sendToUsers(InboxEntity inbox);
    void sendToAllUsers(InboxEntity inbox);

    void markAsRead(Long inboxId);
    int getUnreadCount(Long userId);
}