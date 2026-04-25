package com.community.activityplatform.repository;

import com.community.activityplatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.city = :city AND u.district = :district")
    List<User> findByLocation(@Param("city") String city, @Param("district") String district);

    @Query("SELECT u FROM User u WHERE u.city = :city")
    List<User> findByCity(@Param("city") String city);

    @Query("SELECT u FROM User u JOIN u.interests ui WHERE ui.interest.id = :interestId")
    List<User> findByInterestId(@Param("interestId") Long interestId);

    @Query("SELECT COUNT(u) FROM User u")
    Long countAllUsers();
}
