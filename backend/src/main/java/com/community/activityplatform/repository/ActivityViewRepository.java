package com.community.activityplatform.repository;

import com.community.activityplatform.entity.ActivityView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动浏览记录数据访问层
 */
@Repository
public interface ActivityViewRepository extends JpaRepository<ActivityView, Long> {

    @Query("SELECT COUNT(av) FROM ActivityView av WHERE av.activity.id = :activityId")
    Long countByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT av FROM ActivityView av WHERE av.activity.id = :activityId")
    List<ActivityView> findByActivityId(@Param("activityId") Long activityId);

    @Query("SELECT av FROM ActivityView av WHERE av.createdAt BETWEEN :start AND :end")
    List<ActivityView> findByCreatedAtBetween(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(av) FROM ActivityView av WHERE av.activity.id = :activityId AND av.createdAt >= :date")
    Long countByActivityIdAndDateAfter(@Param("activityId") Long activityId,
                                        @Param("date") LocalDateTime date);
}
