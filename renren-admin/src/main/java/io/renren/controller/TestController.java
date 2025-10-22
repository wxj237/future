package io.renren.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/simple")
    public Map<String, Object> simpleTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "主包测试成功！");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @GetMapping("/attendance")
    public Map<String, Object> testAttendance() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "考勤模块主包测试成功！");
        result.put("data", "这说明主包下的Controller可以被扫描到");
        return result;
    }
}