package com.community.activityplatform.service;

import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.*;
import com.community.activityplatform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 单元测试")
class CommentServiceTest {

    @Mock private ActivityCommentRepository commentRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentLikeRepository commentLikeRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private User otherUser;
    private Activity testActivity;
    private ActivityComment testComment;
    private ActivityComment parentComment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").nickname("测试用户")
                .build();

        otherUser = User.builder()
                .id(2L).username("other").nickname("其他用户")
                .build();

        testActivity = Activity.builder()
                .id(1L).title("测试活动").commentCount(5)
                .build();

        testComment = ActivityComment.builder()
                .id(1L).user(testUser).activity(testActivity)
                .content("这是一条评论").likeCount(3)
                .build();

        parentComment = ActivityComment.builder()
                .id(2L).user(otherUser).activity(testActivity)
                .content("父评论").likeCount(1)
                .build();
    }

    // ===== 发表评论 =====

    @Test
    @DisplayName("发表评论 - 活动不存在")
    void addComment_activityNotFound() {
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        Result<ActivityComment> result = commentService.addComment(1L, 999L, null, "评论内容");

        assertEquals(500, result.getCode());
        assertEquals("活动不存在", result.getMessage());
    }

    @Test
    @DisplayName("发表评论 - 成功")
    void addComment_success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            ActivityComment c = inv.getArgument(0);
            c.setId(3L);
            return c;
        });
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityComment> result = commentService.addComment(1L, 1L, null, "新评论");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("新评论", result.getData().getContent());
        assertEquals(0, result.getData().getLikeCount());
        assertNull(result.getData().getParent()); // 非回复评论
        verify(activityRepository).save(argThat(a -> a.getCommentCount() == 6));
    }

    @Test
    @DisplayName("发表回复评论 - 设置父评论")
    void addComment_withParent() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any())).thenAnswer(inv -> {
            ActivityComment c = inv.getArgument(0);
            c.setId(3L);
            return c;
        });
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityComment> result = commentService.addComment(1L, 1L, 2L, "回复内容");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getParent());
        assertEquals(2L, result.getData().getParent().getId());
    }

    @Test
    @DisplayName("发表回复评论 - 父评论不存在时忽略parentId")
    void addComment_parentNotFound() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());
        when(commentRepository.save(any())).thenAnswer(inv -> {
            ActivityComment c = inv.getArgument(0);
            c.setId(3L);
            return c;
        });
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityComment> result = commentService.addComment(1L, 1L, 999L, "回复不存在父评论");

        assertEquals(200, result.getCode());
        assertNull(result.getData().getParent());
    }

    // ===== 删除评论 =====

    @Test
    @DisplayName("删除评论 - 评论不存在")
    void deleteComment_notFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Void> result = commentService.deleteComment(1L, 999L);

        assertEquals(500, result.getCode());
        assertEquals("评论不存在", result.getMessage());
    }

    @Test
    @DisplayName("删除评论 - 无权删除他人评论")
    void deleteComment_notOwner() {
        testComment.setUser(otherUser); // 评论是otherUser发的
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        Result<Void> result = commentService.deleteComment(1L, 1L); // testUser尝试删除

        assertEquals(500, result.getCode());
        assertEquals("无权删除该评论", result.getMessage());
    }

    @Test
    @DisplayName("删除评论 - 成功，评论数减1")
    void deleteComment_success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Void> result = commentService.deleteComment(1L, 1L);

        assertEquals(200, result.getCode());
        verify(commentRepository).delete(testComment);
        verify(activityRepository).save(argThat(a -> a.getCommentCount() == 4));
    }

    @Test
    @DisplayName("删除评论 - 评论数为0时不会变为负数")
    void deleteComment_zeroCount() {
        testActivity.setCommentCount(0);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        commentService.deleteComment(1L, 1L);

        verify(activityRepository).save(argThat(a -> a.getCommentCount() == 0));
    }

    // ===== 获取评论列表 =====

    @Test
    @DisplayName("获取评论列表 - 成功并加载回复")
    void getActivityComments_success() {
        Page<ActivityComment> page = new PageImpl<>(List.of(testComment));
        when(commentRepository.findRootCommentsByActivityId(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(commentRepository.findRepliesByParentId(1L))
                .thenReturn(List.of(ActivityComment.builder().id(10L).content("回复1").build()));

        Result<Page<ActivityComment>> result = commentService.getActivityComments(
                1L, PageRequest.of(0, 10));

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getContent().size());
        assertNotNull(result.getData().getContent().get(0).getReplies());
        assertEquals(1, result.getData().getContent().get(0).getReplies().size());
    }

    // ===== 点赞评论 =====

    @Test
    @DisplayName("点赞评论 - 评论不存在")
    void likeComment_notFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Map<String, Object>> result = commentService.likeComment(1L, 999L);

        assertEquals(500, result.getCode());
        assertEquals("评论不存在", result.getMessage());
    }

    @Test
    @DisplayName("点赞评论 - 已经点过赞")
    void likeComment_alreadyLiked() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.findByUserIdAndCommentId(1L, 1L))
                .thenReturn(Optional.of(CommentLike.builder().build()));

        Result<Map<String, Object>> result = commentService.likeComment(1L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("已经点过赞了", result.getMessage());
    }

    @Test
    @DisplayName("点赞评论 - 成功，点赞数加1")
    void likeComment_success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.findByUserIdAndCommentId(1L, 1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentLikeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Map<String, Object>> result = commentService.likeComment(1L, 1L);

        assertEquals(200, result.getCode());
        assertEquals(4, result.getData().get("likeCount")); // 3 + 1
        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    @DisplayName("取消点赞评论 - 评论不存在")
    void unlikeComment_notFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Map<String, Object>> result = commentService.unlikeComment(1L, 999L);

        assertEquals(500, result.getCode());
        assertEquals("评论不存在", result.getMessage());
    }

    @Test
    @DisplayName("取消点赞评论 - 成功，点赞数减1")
    void unlikeComment_success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Map<String, Object>> result = commentService.unlikeComment(1L, 1L);

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().get("likeCount")); // 3 - 1
        verify(commentLikeRepository).deleteByUserIdAndCommentId(1L, 1L);
    }

    @Test
    @DisplayName("取消点赞评论 - 点赞数为0时不会变为负数")
    void unlikeComment_zeroCount() {
        testComment.setLikeCount(0);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Map<String, Object>> result = commentService.unlikeComment(1L, 1L);

        assertEquals(200, result.getCode());
        assertEquals(0, result.getData().get("likeCount"));
    }

    // ===== 检查是否点赞 =====

    @Test
    @DisplayName("检查评论点赞 - 已点赞")
    void isCommentLiked_true() {
        when(commentLikeRepository.findByUserIdAndCommentId(1L, 1L))
                .thenReturn(Optional.of(CommentLike.builder().build()));

        assertTrue(commentService.isCommentLiked(1L, 1L));
    }

    @Test
    @DisplayName("检查评论点赞 - 未点赞")
    void isCommentLiked_false() {
        when(commentLikeRepository.findByUserIdAndCommentId(1L, 1L))
                .thenReturn(Optional.empty());

        assertFalse(commentService.isCommentLiked(1L, 1L));
    }
}
