package com.community.activityplatform.repository;

import com.community.activityplatform.entity.ActivityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 活动评论数据访问层
 */
@Repository
public interface ActivityCommentRepository extends JpaRepository<ActivityComment, Long> {

    @Query("SELECT ac FROM ActivityComment ac WHERE ac.activity.id = :activityId AND ac.parent IS NULL ORDER BY ac.createdAt DESC")
    Page<ActivityComment> findRootCommentsByActivityId(@Param("activityId") Long activityId, Pageable pageable);

    @Query("SELECT ac FROM ActivityComment ac WHERE ac.parent.id = :parentId ORDER BY ac.createdAt ASC")
    List<ActivityComment> findRepliesByParentId(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(ac) FROM ActivityComment ac WHERE ac.activity.id = :activityId")
    Long countByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT ac FROM ActivityComment ac WHERE ac.user.id = :userId ORDER BY ac.createdAt DESC")
    Page<ActivityComment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(ac) FROM ActivityComment ac WHERE ac.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
