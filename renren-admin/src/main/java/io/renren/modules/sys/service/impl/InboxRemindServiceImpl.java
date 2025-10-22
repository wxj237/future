package io.renren.modules.sys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.sys.dao.InboxRemindDao;
import io.renren.modules.sys.entity.InboxRemindEntity;
import io.renren.modules.sys.service.InboxRemindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service("inboxRemindService")
public class InboxRemindServiceImpl extends ServiceImpl<InboxRemindDao, InboxRemindEntity> implements InboxRemindService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void createDailyRemind() {
        // 简化版本：先记录日志，后续再完善用户获取逻辑
        logger.info("创建每日提醒任务开始");

        // 这里暂时不创建提醒，等用户相关服务可用后再实现
        // 或者可以通过其他方式获取当前登录用户ID
        logger.info("每日提醒功能待完善");
    }

    @Override
    public List<InboxRemindEntity> getUserReminds(Long userId) {
        return baseMapper.getUserReminds(userId);
    }

    @Override
    public int getUnreadRemindCount(Long userId) {
        return baseMapper.getUnreadRemindCount(userId);
    }

    @Override
    public void markRemindAsRead(Long remindId) {
        baseMapper.markAsRead(remindId);
    }

    @Override
    public void markAllRemindsAsRead(Long userId) {
        baseMapper.markAllAsRead(userId);
    }
}