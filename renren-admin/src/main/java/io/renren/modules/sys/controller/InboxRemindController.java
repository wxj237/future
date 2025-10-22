package io.renren.modules.sys.controller;

import io.renren.modules.sys.entity.InboxRemindEntity;
import io.renren.modules.sys.service.InboxRemindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/inboxRemind")
public class InboxRemindController {

    @Autowired
    private InboxRemindService inboxRemindService;

    @GetMapping("/userReminds")
    public Map<String, Object> getUserReminds(@RequestParam Long userId) {
        List<InboxRemindEntity> reminds = inboxRemindService.getUserReminds(userId);
        return Map.of("code", 0, "msg", "success", "reminds", reminds);
    }

    @GetMapping("/unreadCount")
    public Map<String, Object> getUnreadRemindCount(@RequestParam Long userId) {
        int count = inboxRemindService.getUnreadRemindCount(userId);
        return Map.of("code", 0, "msg", "success", "count", count);
    }

    @PostMapping("/markAsRead/{id}")
    public Map<String, Object> markRemindAsRead(@PathVariable("id") Long id) {
        inboxRemindService.markRemindAsRead(id);
        return Map.of("code", 0, "msg", "success");
    }

    @PostMapping("/markAllAsRead")
    public Map<String, Object> markAllRemindsAsRead(@RequestParam Long userId) {
        inboxRemindService.markAllRemindsAsRead(userId);
        return Map.of("code", 0, "msg", "success");
    }
}