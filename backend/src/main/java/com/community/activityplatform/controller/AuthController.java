package com.community.activityplatform.controller;

import com.community.activityplatform.dto.LoginRequest;
import com.community.activityplatform.dto.RegisterRequest;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.dto.UserDTO;
import com.community.activityplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录（普通用户 / 管理员 通用）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    /**
     * 管理员登录（与普通登录共用同一接口，区别在于前端使用管理员账号登录）
     * 若需要区分，可改用 /admin/login 专用端点
     */
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody LoginRequest request) {
        Result<Map<String, Object>> result = userService.login(request);
        if (result.getCode() == 200) {
            Map<String, Object> data = result.getData();
            UserDTO user = (UserDTO) data.get("user");
            if (!"ADMIN".equals(user.getRole())) {
                return Result.error(403, "非管理员账号，无权访问后台");
            }
        }
        return result;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }
}
