// src/main/java/io/renren/modules/sys/controller/GroupController.java
package io.renren.modules.sys.controller;

import io.renren.common.utils.Result;
import io.renren.modules.sys.entity.GroupEntity;
import io.renren.modules.sys.service.GroupService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组管理
 */
@RestController
@RequestMapping("/sys/group")
public class GroupController {
    @Autowired
    private GroupService groupService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:group:list")
    public Result<Map<String, Object>> list(@RequestParam Map<String, Object> params){
        List<GroupEntity> groupList = groupService.queryList(params);
        Map<String, Object> result = new HashMap<>();
        result.put("list", groupList);
        return new Result<Map<String, Object>>().ok(result);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{groupId}")
    @RequiresPermissions("sys:group:info")
    public Result<GroupEntity> info(@PathVariable("groupId") Long groupId){
        GroupEntity group = groupService.getById(groupId);
        return new Result<GroupEntity>().ok(group);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("sys:group:save")
    public Result<String> save(@RequestBody GroupEntity group){
        groupService.saveGroup(group);
        return new Result<String>().ok("操作成功");
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @RequiresPermissions("sys:group:update")
    public Result<String> update(@RequestBody GroupEntity group){
        groupService.updateGroup(group);
        return new Result<String>().ok("操作成功");
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @RequiresPermissions("sys:group:delete")
    public Result<String> delete(@RequestBody Long[] groupIds){
        groupService.deleteBatch(groupIds);
        return new Result<String>().ok("操作成功");
    }
}