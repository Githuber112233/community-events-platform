package com.community.activityplatform.controller;
import java.util.Map;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.ActivityComment;
import com.community.activityplatform.service.CommentService;
import com.community.activityplatform.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    /**
     * 发表评论
     */
    @PostMapping
    public Result<ActivityComment> addComment(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        Long activityId = ((Number) request.get("activityId")).longValue();
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
        String content = (String) request.get("content");
        return commentService.addComment(userId, activityId, parentId, content);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return commentService.deleteComment(userId, commentId);
    }

    /**
     * 获取活动评论列表
     */
    @GetMapping("/activities/{activityId}")
    public Result<Page<ActivityComment>> getActivityComments(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Pageable pageable = PageRequest.of(page, size);
        Result<Page<ActivityComment>> result = commentService.getActivityComments(activityId, pageable);
        
        // 如果用户已登录，添加点赞状态
        if (result.getData() != null && token != null) {
            Long userId = extractUserIdFromToken(token);
            if (userId != null) {
                result.getData().getContent().forEach(comment -> {
                    boolean liked = commentService.isCommentLiked(userId, comment.getId());
                    comment.setLiked(liked);
                });
            }
        }
        
        return result;
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{commentId}/like")
    public Result<Map<String, Object>> likeComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return commentService.likeComment(userId, commentId);
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{commentId}/like")
    public Result<Map<String, Object>> unlikeComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {
        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录或Token无效");
        }
        return commentService.unlikeComment(userId, commentId);
    }

    /**
     * 从Token中提取用户ID
     */
    private Long extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
