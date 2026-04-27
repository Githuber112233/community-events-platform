package com.community.activityplatform.controller;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.service.ActivityService;
import com.community.activityplatform.service.RecommendationService;
import com.community.activityplatform.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final RecommendationService recommendationService;
    private final JwtUtil jwtUtil;

    /**
     * 创建活动
     */
    @PostMapping
    public Result<ActivityDTO> createActivity(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ActivityDTO activityDTO) {
        Long userId = extractUserIdFromToken(token);
        return activityService.createActivity(userId, activityDTO);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{activityId}")
    public Result<ActivityDTO> getActivityDetail(
            @PathVariable Long activityId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = token != null ? extractUserIdFromToken(token) : null;
        return activityService.getActivityDetail(activityId, userId);
    }

    /**
     * 获取活动列表
     * status=all 或 status=null 表示所有状态
     * sort=date（最新发布）或 sort=participants（最多参与）
     */
    @GetMapping
    public Result<Page<ActivityDTO>> getActivityList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "date") String sort,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // "all" 或空值表示查所有状态，传 null 给 service
        Activity.ActivityStatus activityStatus = null;
        if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
            activityStatus = Activity.ActivityStatus.valueOf(status);
        }
        // sort=date 按创建时间排序，sort=participants 按参与人数排序
        Pageable pageable = PageRequest.of(page, size,
            sort.equalsIgnoreCase("participants")
                ? org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "currentParticipants")
                : org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Long userId = token != null ? extractUserIdFromToken(token) : null;
        return activityService.getActivityList(activityStatus, categoryId, keyword, pageable, userId);
    }

    /**
     * 获取附近活动
     */
    @GetMapping("/nearby")
    public Result<List<ActivityDTO>> getNearbyActivities(
            @RequestParam String city,
            @RequestParam String district,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = token != null ? extractUserIdFromToken(token) : null;
        return activityService.getNearbyActivities(city, district, userId);
    }

    /**
     * 获取热门活动
     */
    @GetMapping("/popular")
    public Result<Page<ActivityDTO>> getPopularActivities(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = token != null ? extractUserIdFromToken(token) : null;
        return activityService.getPopularActivities(pageable, userId);
    }

    /**
     * 参与活动
     */
    @PostMapping("/{activityId}/participate")
    public Result<Void> participateActivity(
            @RequestHeader("Authorization") String token,
            @PathVariable Long activityId,
            @RequestBody(required = false) Map<String, String> message) {
        Long userId = extractUserIdFromToken(token);
        String msg = message != null ? message.get("message") : null;
        return activityService.participateActivity(userId, activityId, msg);
    }

    /**
     * 取消参与活动
     */
    @DeleteMapping("/{activityId}/participate")
    public Result<Void> cancelParticipation(
            @RequestHeader("Authorization") String token,
            @PathVariable Long activityId) {
        Long userId = extractUserIdFromToken(token);
        return activityService.cancelParticipation(userId, activityId);
    }

    /**
     * 点赞活动
     */
    @PostMapping("/{activityId}/like")
    public Result<Void> likeActivity(
            @RequestHeader("Authorization") String token,
            @PathVariable Long activityId) {
        Long userId = extractUserIdFromToken(token);
        return activityService.likeActivity(userId, activityId);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{activityId}/like")
    public Result<Void> unlikeActivity(
            @RequestHeader("Authorization") String token,
            @PathVariable Long activityId) {
        Long userId = extractUserIdFromToken(token);
        return activityService.unlikeActivity(userId, activityId);
    }

    /**
     * 获取推荐活动
     */
    @GetMapping("/recommended")
    public Result<Page<ActivityDTO>> getRecommendedActivities(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 直接返回热门活动作为推荐（临时方案）
        Pageable pageable = PageRequest.of(page, size);
        return activityService.getPopularActivities(pageable, null);
    }

    /**
     * 获取活动参与者列表
     */
    @GetMapping("/{activityId}/participants")
    public Result<List<Map<String, Object>>> getActivityParticipants(
            @PathVariable Long activityId,
            @RequestParam(required = false, defaultValue = "APPROVED") String status) {
        return activityService.getActivityParticipants(activityId, status);
    }

    /**
     * 从 Token 中提取用户 ID
     */
    private Long extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
