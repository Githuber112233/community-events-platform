package com.community.activityplatform.controller;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.dto.UserDTO;
import com.community.activityplatform.service.UserService;
import com.community.activityplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return userService.getUserInfo(userId);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserDTO> getUserById(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 更新用户信息（前端: PUT /users/profile → 修正为 /users/me）
     */
    @PutMapping("/me")
    public Result<UserDTO> updateCurrentUser(
            @RequestHeader("Authorization") String token,
            @RequestBody UserDTO userDTO) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return userService.updateUserInfo(userId, userDTO);
    }

    /**
     * 更新用户兴趣标签
     */
    @PutMapping("/me/interests")
    public Result<Void> updateUserInterests(
            @RequestHeader("Authorization") String token,
            @RequestBody List<Long> interestIds) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return userService.updateUserInterests(userId, interestIds);
    }

    /**
     * 获取用户兴趣标签
     */
    @GetMapping("/me/interests")
    public Result<List<InterestDTO>> getUserInterests(
            @RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return userService.getUserInterests(userId);
    }

    // ========== 前端 followApi 需要的端点 ==========

    /**
     * 关注用户（前端: POST /users/{userId}/follow）
     */
    @PostMapping("/{userId}/follow")
    public Result<Void> followUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        Long followerId = extractUserIdFromToken(token);
        if (followerId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return userService.followUser(followerId, userId);
    }

    /**
     * 取消关注用户（前端: DELETE /users/{userId}/follow）
     */
    @DeleteMapping("/{userId}/follow")
    public Result<Void> unfollowUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        Long followerId = extractUserIdFromToken(token);
        if (followerId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return userService.unfollowUser(followerId, userId);
    }

    /**
     * 获取用户粉丝列表（前端: GET /users/{userId}/followers）
     */
    @GetMapping("/{userId}/followers")
    public Result<List<UserDTO>> getFollowers(@PathVariable Long userId) {
        return userService.getFollowers(userId);
    }

    /**
     * 获取用户关注列表（前端: GET /users/{userId}/following）
     */
    @GetMapping("/{userId}/following")
    public Result<List<UserDTO>> getFollowing(@PathVariable Long userId) {
        return userService.getFollowing(userId);
    }

    /**
     * 获取用户活动列表（创建/参与/收藏）
     */
    @GetMapping("/me/activities")
    public Result<?> getUserActivities(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "created") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        Pageable pageable = PageRequest.of(page, size);
        return userService.getUserActivities(userId, type, pageable);
    }

    /**
     * 从 Token 中提取用户 ID（通过 JWT 解析）
     */
    private Long extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
