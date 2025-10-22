// src/main/java/io/renren/modules/sys/controller/GroupMessageController.java
package io.renren.modules.sys.controller;

import io.renren.common.utils.Result;
import io.renren.modules.sys.entity.GroupMessageEntity;
import io.renren.modules.sys.service.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组消息管理
 */
@RestController
@RequestMapping("/sys/groupmessage")
public class GroupMessageController {
    @Autowired
    private GroupMessageService groupMessageService;

    /**
     * 获取群组消息
     */
    @GetMapping("/messages/{groupId}")
    public Result<Map<String, Object>> getMessages(@PathVariable("groupId") Long groupId){
        List<GroupMessageEntity> messages = groupMessageService.getGroupMessages(groupId);
        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        return new Result<Map<String, Object>>().ok(result);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody GroupMessageEntity message){
        groupMessageService.sendMessage(message);
        return new Result<String>().ok("消息发送成功");
    }
}