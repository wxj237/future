/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.controller;

import io.renren.common.utils.Result;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.impl.SysUserBatchImportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 批量导入用户接口
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/sys/user/import")
@Tag(name = "批量导入用户")
@AllArgsConstructor
public class SysUserBatchImportController {
    
    private final SysUserBatchImportServiceImpl sysUserBatchImportService;

    @PostMapping
    @Operation(summary = "批量导入用户")
    public Result batchImportUsers(@RequestBody List<SysUserEntity> userList) {
        try {
            sysUserBatchImportService.batchImportUsers(userList);
            return new Result().ok("用户导入成功");
        } catch (Exception e) {
            return new Result().error("用户导入失败: " + e.getMessage());
        }
    }
}