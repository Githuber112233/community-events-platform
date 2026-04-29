package com.community.activityplatform.repository;

import com.community.activityplatform.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 评论点赞数据访问层
 */
@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Query("SELECT cl FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.id = :commentId")
    Optional<CommentLike> findByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    Long countByCommentId(@Param("commentId") Long commentId);

    void deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);
}