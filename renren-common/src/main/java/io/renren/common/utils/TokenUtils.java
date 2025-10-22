package io.renren.common.utils;

import io.renren.common.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Token工具类
 */
@Component
public class TokenUtils {
    
    @Resource
    private RedisUtils redisUtils;
    
    /**
     * 刷新Token
     * @param oldToken 旧Token
     * @return 新Token
     */
    public String refreshToken(String oldToken) {
        // 这里应该实现具体的Token刷新逻辑
        // 例如生成新的Token并更新数据库或Redis中的记录
        return oldToken; // 简化处理，实际应该生成新的Token
    }
    
    /**
     * 检查Token是否即将过期
     * @param expireDate 过期时间
     * @return 是否即将过期
     */
    public boolean isAboutToExpire(Date expireDate) {
        if (expireDate == null) {
            return true;
        }
        // 30分钟 = 30 * 60 * 1000 毫秒
        return (expireDate.getTime() - System.currentTimeMillis()) < (30 * 60 * 1000);
    }
}