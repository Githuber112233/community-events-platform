package com.community.activityplatform.repository;

import com.community.activityplatform.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 兴趣标签数据访问层
 */
@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findByName(String name);

    List<Interest> findByCategory(String category);

    List<Interest> findByActiveTrueOrderByPopularityDesc();

    List<Interest> findByCategoryAndActiveTrueOrderByPopularityDesc(String category);
}
