package com.community.activityplatform.repository;

import com.community.activityplatform.entity.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户兴趣标签数据访问层
 */
@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    Optional<UserInterest> findByUserIdAndInterestId(Long userId, Long interestId);

    List<UserInterest> findByUserId(Long userId);

    @Query("SELECT ui FROM UserInterest ui WHERE ui.interest.id = :interestId")
    List<UserInterest> findByInterestId(@Param("interestId") Long interestId);

    void deleteByUserId(Long userId);
}
