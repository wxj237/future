package io.renren.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.modules.sys.entity.InboxRemindEntity;
import java.util.List;

public interface InboxRemindService extends IService<InboxRemindEntity> {
    void createDailyRemind();
    List<InboxRemindEntity> getUserReminds(Long userId);
    int getUnreadRemindCount(Long userId);
    void markRemindAsRead(Long remindId);
    void markAllRemindsAsRead(Long userId);
}