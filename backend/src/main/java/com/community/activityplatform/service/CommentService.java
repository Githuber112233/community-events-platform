package com.community.activityplatform.service;

import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityComment;
import com.community.activityplatform.repository.ActivityCommentRepository;
import com.community.activityplatform.repository.ActivityRepository;
import com.community.activityplatform.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论服务
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final ActivityCommentRepository commentRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    /**
     * 发表评论
     */
    @Transactional
    public Result<ActivityComment> addComment(Long userId, Long activityId, Long parentId, String content) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        ActivityComment comment = ActivityComment.builder()
                .user(userRepository.findById(userId).orElse(null))
                .activity(activity)
                .content(content)
                .likeCount(0)
                .build();

        // 如果是回复评论,设置父评论
        if (parentId != null) {
            ActivityComment parentComment = commentRepository.findById(parentId).orElse(null);
            if (parentComment != null) {
                comment.setParent(parentComment);
            }
        }

        commentRepository.save(comment);

        // 更新活动评论数
        activity.setCommentCount(activity.getCommentCount() + 1);
        activityRepository.save(activity);

        return Result.success(comment);
    }

    /**
     * 删除评论
     */
    @Transactional
    public Result<Void> deleteComment(Long userId, Long commentId) {
        ActivityComment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        if (!comment.getUser().getId().equals(userId)) {
            return Result.error("无权删除该评论");
        }

        // 更新活动评论数
        Activity activity = comment.getActivity();
        activity.setCommentCount(Math.max(0, activity.getCommentCount() - 1));
        activityRepository.save(activity);

        commentRepository.delete(comment);
        return Result.success();
    }

    /**
     * 获取活动评论列表
     */
    public Result<Page<ActivityComment>> getActivityComments(Long activityId, Pageable pageable) {
        Page<ActivityComment> comments = commentRepository.findRootCommentsByActivityId(activityId, pageable);

        // 加载每条评论的回复
        comments.getContent().forEach(comment -> {
            List<ActivityComment> replies = commentRepository.findRepliesByParentId(comment.getId());
            comment.setReplies(replies);
        });

        return Result.success(comments);
    }
}
