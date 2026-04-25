package com.community.activityplatform.repository;

import com.community.activityplatform.entity.ActivityLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 活动点赞记录数据访问层
 */
@Repository
public interface ActivityLikeRepository extends JpaRepository<ActivityLike, Long> {

    Optional<ActivityLike> findByUser_IdAndActivity_Id(Long userId, Long activityId);

    @Query("SELECT al FROM ActivityLike al WHERE al.user.id = :userId")
    Page<ActivityLike> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(al) FROM ActivityLike al WHERE al.activity.id = :activityId")
    Long countByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT COUNT(al) FROM ActivityLike al WHERE al.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    boolean existsByUser_IdAndActivity_Id(Long userId, Long activityId);
}
