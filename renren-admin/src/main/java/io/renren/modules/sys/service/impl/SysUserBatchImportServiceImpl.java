/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import io.renren.modules.security.password.PasswordUtils;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.enums.SuperAdminEnum;
import io.renren.modules.sys.service.SysUserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 批量导入用户服务
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
@AllArgsConstructor
public class SysUserBatchImportServiceImpl {
    
    private final SysUserDao sysUserDao;
    private final SysUserService sysUserService;

    /**
     * 批量导入用户
     * @param userList 用户列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchImportUsers(List<SysUserEntity> userList) {
        for (SysUserEntity user : userList) {
            // 设置默认密码为"admin"
            String password = PasswordUtils.encode("admin");
            user.setPassword(password);
            
            // 设置为非超级管理员
            user.setSuperAdmin(SuperAdminEnum.NO.value());
            
            // 设置默认状态为正常
            if (user.getStatus() == null) {
                user.setStatus(1); // 1表示正常状态
            }
            
            // 保存用户
            sysUserDao.insert(user);
        }
    }
}