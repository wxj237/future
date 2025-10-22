package io.renren.modules.job;

import io.renren.modules.sys.service.InboxRemindService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyRemindJob {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private InboxRemindService inboxRemindService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void createDailyRemind() {
        logger.info("开始创建每日提醒");
        try {
            inboxRemindService.createDailyRemind();
            logger.info("每日提醒创建完成");
        } catch (Exception e) {
            logger.error("创建每日提醒失败", e);
        }
    }
}