package io.renren.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.sys.entity.InboxRemindEntity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface InboxRemindDao extends BaseMapper<InboxRemindEntity> {
    List<InboxRemindEntity> getUserReminds(Long userId);
    int getUnreadRemindCount(Long userId);
    void markAsRead(Long remindId);
    void markAllAsRead(Long userId);
}