package io.renren.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.sys.entity.InboxEntity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface InboxDao extends BaseMapper<InboxEntity> {
    List<InboxEntity> queryList(Map<String, Object> params);
    int queryTotal(Map<String, Object> params);

    List<InboxEntity> getUserInbox(Long userId);
    int getUnreadCount(Long userId);
}