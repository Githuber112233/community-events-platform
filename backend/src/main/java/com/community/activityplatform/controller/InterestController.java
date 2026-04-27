package com.community.activityplatform.controller;

import java.util.Map;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.service.InterestService;
import com.community.activityplatform.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 兴趣标签控制器
 */
@RestController
@RequestMapping("/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;
    private final RecommendationService recommendationService;

    /**
     * 获取所有兴趣标签
     */
    @GetMapping
    public Result<List<InterestDTO>> getAllInterests() {
        return interestService.getAllInterests();
    }

    /**
     * 按分类获取兴趣标签
     */
    @GetMapping("/category/{category}")
    public Result<List<InterestDTO>> getInterestsByCategory(@PathVariable String category) {
        return interestService.getInterestsByCategory(category);
    }

    /**
     * 创建兴趣标签(仅管理员)
     */
    @PostMapping
    public Result<InterestDTO> createInterest(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        String category = request.get("category");
        return interestService.createInterest(name, description, category);
    }

    /**
     * 用户行为反馈：更新兴趣标签权重
     * @param request 包含 userId, activityId, action(participate/like/view)
     */
    @PostMapping("/feedback")
    public Result<Void> updateInterestFeedback(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Long activityId = Long.valueOf(request.get("activityId").toString());
        String action = request.get("action").toString();
        recommendationService.updateInterestFeedback(userId, activityId, action);
        return Result.success(null);
    }
}
