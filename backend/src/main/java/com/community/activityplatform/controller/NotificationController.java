package com.community.activityplatform.controller;

import com.community.activityplatform.dto.Result;
import com.community.activityplatform.service.NotificationService;
import com.community.activityplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    /**
     * 获取通知列表
     */
    @GetMapping
    public Result<Page<Map<String, Object>>> getNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = extractUserId(token);
        return notificationService.getNotifications(userId, page, size);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(@RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return notificationService.getUnreadCount(userId);
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public Result<Void> markAsRead(
            @RequestHeader("Authorization") String token,
            @PathVariable Long notificationId) {
        Long userId = extractUserId(token);
        return notificationService.markAsRead(userId, notificationId);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return notificationService.markAllAsRead(userId);
    }

    /**
     * 一键通知活动参与者
     */
    @PostMapping("/activity/{activityId}")
    public Result<Map<String, Object>> notifyParticipants(
            @RequestHeader("Authorization") String token,
            @PathVariable Long activityId,
            @RequestBody(required = false) Map<String, String> body) {
        Long userId = extractUserId(token);
        String title = body != null ? body.get("title") : null;
        String content = body != null ? body.get("content") : null;
        return notificationService.notifyActivityParticipants(userId, activityId, title, content);
    }

    private Long extractUserId(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
