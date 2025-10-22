package io.renren.modules.sys.controller;

import io.renren.modules.sys.entity.InboxEntity;
import io.renren.modules.sys.service.InboxService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/sys/inbox")
public class InboxController {

    @Autowired
    private InboxService inboxService;

    @PostMapping("/sendToGroup")
    @RequiresPermissions("sys:inbox:send")
    public Map<String, Object> sendToGroup(@RequestBody InboxEntity inbox) {
        inboxService.sendToGroup(inbox);
        return Map.of("code", 0, "msg", "success");
    }

    @PostMapping("/sendToUsers")
    @RequiresPermissions("sys:inbox:send")
    public Map<String, Object> sendToUsers(@RequestBody InboxEntity inbox) {
        inboxService.sendToUsers(inbox);
        return Map.of("code", 0, "msg", "success");
    }

    @PostMapping("/sendToAll")
    @RequiresPermissions("sys:inbox:send")
    public Map<String, Object> sendToAll(@RequestBody InboxEntity inbox) {
        inboxService.sendToAllUsers(inbox);
        return Map.of("code", 0, "msg", "success");
    }

    @PostMapping("/markAsRead/{id}")
    public Map<String, Object> markAsRead(@PathVariable("id") Long id) {
        inboxService.markAsRead(id);
        return Map.of("code", 0, "msg", "success");
    }

    @GetMapping("/unreadCount")
    public Map<String, Object> getUnreadCount(@RequestParam Long userId) {
        int count = inboxService.getUnreadCount(userId);
        return Map.of("code", 0, "msg", "success", "count", count);
    }
}