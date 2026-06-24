package com.battery.controller;

import com.battery.config.AuthTokenStore;
import com.battery.entity.LoginRequest;
import com.battery.entity.LoginResponse;
import com.battery.entity.Result;
import com.battery.entity.SysUser;
import com.battery.service.SysUserService;
import com.battery.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return Result.error(400, "用户名和密码不能为空");
        }

        SysUser user = sysUserService.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());
        LoginResponse response = new LoginResponse(token, user.getUsername(), user.getNickname());
        AuthTokenStore.put(token, response);

        log.info("用户 {} 登录成功", request.getUsername());
        return Result.success(response);
    }

    @GetMapping("/info")
    public Result<LoginResponse> getUserInfo(@RequestHeader("Authorization") String token) {
        LoginResponse user = AuthTokenStore.get(token);
        if (user == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        AuthTokenStore.remove(token);
        return Result.success(null);
    }
}
