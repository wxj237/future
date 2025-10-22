package io.renren.modules.security.controller;

import io.renren.common.exception.ErrorCode;
import io.renren.common.exception.RenException;
import io.renren.common.utils.IpUtils;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.modules.log.entity.SysLogLoginEntity;
import io.renren.modules.log.enums.LoginOperationEnum;
import io.renren.modules.log.enums.LoginStatusEnum;
import io.renren.modules.log.service.SysLogLoginService;
import io.renren.modules.security.password.PasswordUtils;
import io.renren.modules.security.service.LoginListService;
import io.renren.modules.security.service.SysUserTokenService;
import io.renren.modules.sys.dto.SysUserDTO;
import io.renren.modules.sys.enums.UserStatusEnum;
import io.renren.modules.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/simplifiedLogin")
public class SimplifiedLoginController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserTokenService sysUserTokenService;

    @Autowired
    private SysLogLoginService sysLogLoginService;

    @Autowired
    private LoginListService loginListService;

    @PostMapping("")
    public Result simplifiedLogin(HttpServletRequest request, @RequestBody Map<String, String> payload) {
        String studentId = payload.get("studentId");

        // 学号不能为空
        AssertUtils.isBlank(studentId, "学号不能为空");

        // 检查学号是否在登录名单中
        if (!loginListService.isStudentInLoginList(studentId)) {
            throw new RenException(ErrorCode.ACCOUNT_PASSWORD_ERROR);
        }

        // 用户信息
        SysUserDTO user = sysUserService.getByUsername(studentId);

        // 如果用户不存在，自动创建用户
        if (user == null) {
            SysUserDTO newUser = new SysUserDTO();
            newUser.setUsername(studentId);
            newUser.setRealName(loginListService.getStudentInfo(studentId).get("name").toString());
            newUser.setStatus(1); // 正常状态

            // 使用固定密码"admin"
            String password = PasswordUtils.encode("admin");
            newUser.setPassword(password);

            // 保存用户
            sysUserService.save(newUser);

            // 获取新创建的用户信息
            user = sysUserService.getByUsername(studentId);
        }

        // 创建日志实体
        SysLogLoginEntity log = new SysLogLoginEntity();
        log.setOperation(LoginOperationEnum.LOGIN.value());
        log.setIp(IpUtils.getIpAddr(request));
        log.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));

        // 使用固定密码"admin"验证
        if (!PasswordUtils.matches("admin", user.getPassword())) {
            log.setStatus(LoginStatusEnum.FAIL.value());
            log.setCreator(user.getId());
            log.setCreatorName(user.getUsername());
            sysLogLoginService.save(log);

            throw new RenException(ErrorCode.ACCOUNT_PASSWORD_ERROR);
        }

        // 登录成功
        log.setStatus(LoginStatusEnum.SUCCESS.value());
        log.setCreator(user.getId());
        log.setCreatorName(user.getUsername());
        sysLogLoginService.save(log);

        return sysUserTokenService.createToken(user.getId());
    }
}