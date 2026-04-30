package com.community.activityplatform.repository;

import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.Activity.ActivityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 活动数据访问层
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    @Query("SELECT a FROM Activity a WHERE a.status = :status AND a.startTime > :now ORDER BY a.createdAt DESC")
    Page<Activity> findByStatusAndStartTimeAfter(@Param("status") ActivityStatus status,
                                                   @Param("now") LocalDateTime now,
                                                   Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE a.status = :status AND a.startTime > :now ORDER BY a.viewCount DESC, a.likeCount DESC")
    Page<Activity> findByStatusAndStartTimeAfterOrderByViewCountDescLikeCountDesc(
            @Param("status") ActivityStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    Page<Activity> findByStatus(ActivityStatus status, Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE a.city = :city AND a.district = :district AND a.startTime > :now AND a.status = :status")
    List<Activity> findNearbyActivities(@Param("city") String city,
                                        @Param("district") String district,
                                        @Param("now") LocalDateTime now,
                                        @Param("status") ActivityStatus status);

    @Query("SELECT a FROM Activity a WHERE a.startTime BETWEEN :start AND :end")
    List<Activity> findActivitiesByDateRange(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Activity a JOIN a.interests i WHERE i.id = :interestId AND a.status = :status AND a.startTime > :now")
    List<Activity> findByInterestId(@Param("interestId") Long interestId,
                                    @Param("status") ActivityStatus status,
                                    @Param("now") LocalDateTime now);

    // 按分类筛选（分页）- 不检查开始时间，保持与findByStatus一致
    @Query("SELECT a FROM Activity a JOIN a.interests i WHERE i.id = :interestId AND a.status = :status ORDER BY a.createdAt DESC")
    Page<Activity> findByInterestIdAndStatus(@Param("interestId") Long interestId,
                                              @Param("status") ActivityStatus status,
                                              Pageable pageable);

    // 按分类筛选（所有状态）
    @Query("SELECT a FROM Activity a JOIN a.interests i WHERE i.id = :interestId ORDER BY a.createdAt DESC")
    Page<Activity> findByInterestId(@Param("interestId") Long interestId, Pageable pageable);

    // 所有状态分页查询
    @Query("SELECT a FROM Activity a ORDER BY a.createdAt DESC")
    Page<Activity> findAllActivities(Pageable pageable);

    // 按关键词搜索（分页）
    Page<Activity> findByTitleContainingIgnoreCaseAndStatus(String title, ActivityStatus status, Pageable pageable);

    // 按关键词搜索（所有状态）
    @Query("SELECT a FROM Activity a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.createdAt DESC")
    Page<Activity> findByKeywordAllStatuses(@Param("keyword") String keyword, Pageable pageable);

    // 按分类+关键词（所有状态）
    @Query("SELECT a FROM Activity a JOIN a.interests i WHERE i.id = :interestId AND LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.createdAt DESC")
    Page<Activity> findByInterestIdAndKeyword(@Param("interestId") Long interestId, @Param("keyword") String keyword, Pageable pageable);

    // 按分类+关键词+状态+未过期（报名中筛选用）
    @Query("SELECT a FROM Activity a JOIN a.interests i WHERE i.id = :interestId AND LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.status = :status AND a.startTime > :now ORDER BY a.createdAt DESC")
    Page<Activity> findByInterestIdAndStatusAndStartTimeAfter(@Param("interestId") Long interestId, @Param("keyword") String keyword, Pageable pageable);

    // 按分类+状态+未过期
    @Query("SELECT a FROM Activity a JOIN a.interests i WHERE i.id = :interestId AND a.status = :status AND a.startTime > :now ORDER BY a.createdAt DESC")
    Page<Activity> findByInterestIdAndStatusAndStartTimeAfter(@Param("interestId") Long interestId, @Param("status") ActivityStatus status, @Param("now") LocalDateTime now, Pageable pageable);

    // 按关键词+状态+未过期
    @Query("SELECT a FROM Activity a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND a.status = :status AND a.startTime > :now ORDER BY a.createdAt DESC")
    Page<Activity> findByTitleContainingIgnoreCaseAndStatusAndStartTimeAfter(@Param("keyword") String keyword, @Param("status") ActivityStatus status, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT a FROM Activity a WHERE a.creator.id = :creatorId ORDER BY a.createdAt DESC")
    Page<Activity> findByCreatorId(@Param("creatorId") Long creatorId, Pageable pageable);

    Long countByCreatorId(Long creatorId);

    @Query("SELECT a FROM Activity a ORDER BY a.viewCount DESC, a.likeCount DESC")
    Page<Activity> findPopularActivities(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Activity a WHERE a.status = :status")
    Long countByStatus(@Param("status") ActivityStatus status);
}
