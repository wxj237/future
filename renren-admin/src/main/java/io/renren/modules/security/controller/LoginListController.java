package io.renren.modules.security.controller;

import io.renren.common.utils.Result;
import io.renren.modules.security.service.LoginListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/loginList")
public class LoginListController {
    @Autowired
    private LoginListService loginListService;

    @PostMapping("/import")
    public Result importLoginList(@RequestBody List<Map<String, String>> userList) {
        loginListService.batchImportLoginList(userList);
        return new Result();
    }

    @GetMapping("/list")
    public Result<List<Map<String, String>>> getLoginList() {
        List<Map<String, String>> list = loginListService.getAllLoginList();
        return new Result<List<Map<String, String>>>().ok(list);
    }
}