package com.community.activityplatform.controller;

import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.User;
import com.community.activityplatform.repository.UserRepository;
import com.community.activityplatform.service.AdminService;
import com.community.activityplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    /**
     * 获取待审核活动列表
     */
    @GetMapping("/activities/pending")
    public Result<?> getPendingActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.getPendingActivities(page, size);
    }

    /**
     * 获取所有活动（管理员用）
     */
    @GetMapping("/activities")
    public Result<?> getAllActivities(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.getAllActivities(status, page, size);
    }

    /**
     * 审核活动通过
     */
    @PostMapping("/activities/{activityId}/approve")
    public Result<?> approveActivity(
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.approveActivity(activityId);
    }

    /**
     * 审核活动拒绝
     */
    @PostMapping("/activities/{activityId}/reject")
    public Result<?> rejectActivity(
            @PathVariable Long activityId,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        String reason = body != null ? body.get("reason") : null;
        return adminService.rejectActivity(activityId, reason);
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/activities/{activityId}")
    public Result<?> deleteActivity(
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.deleteActivity(activityId);
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public Result<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.getAllUsers(page, size);
    }

    /**
     * 获取用户画像（管理员专用）
     */
    @GetMapping("/users/{userId}/profile")
    public Result<?> getUserProfile(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.getUserProfile(userId);
    }

    /**
     * 获取平台统计数据
     */
    @GetMapping("/statistics")
    public Result<?> getStatistics(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.getStatistics();
    }

    /**
     * 获取所有用户的兴趣权重汇总数据
     */
    @GetMapping("/users/interests")
    public Result<?> getAllUsersInterestWeights(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.getAllUsersInterestWeights();
    }

    /**
     * 回填用户兴趣点击/参与计数（从历史数据反推）
     */
    @PostMapping("/users/interests/backfill")
    public Result<?> backfillInterestCounts(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.backfillInterestCounts();
    }

    /**
     * 获取 User-CF 协同过滤算法可视化数据
     */
    @GetMapping("/recommendation/user-cf")
    public Result<?> getUserCFVisualization(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限访问");
        }
        return adminService.getUserCFVisualization();
    }

    /**
     * 判断是否为管理员（检查用户角色）
     */
    private boolean isAdmin(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }
}