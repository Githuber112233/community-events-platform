package com.community.activityplatform.repository;

import com.community.activityplatform.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户关注关系数据访问层
 */
@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :followerId")
    Page<UserFollow> findFollowingByUserId(@Param("followerId") Long followerId, Pageable pageable);

    @Query("SELECT uf FROM UserFollow uf WHERE uf.followed.id = :followedId")
    Page<UserFollow> findFollowersByUserId(@Param("followedId") Long followedId, Pageable pageable);

    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.id = :userId")
    Long countFollowingByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.followed.id = :userId")
    Long countFollowersByUserId(@Param("userId") Long userId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
}
