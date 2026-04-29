package com.community.activityplatform.repository;

import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.ActivityParticipant.ParticipantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 活动参与记录数据访问层
 */
@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {

    Optional<ActivityParticipant> findByUser_IdAndActivity_Id(Long userId, Long activityId);

    boolean existsByUser_IdAndActivity_Id(Long userId, Long activityId);

    @Query("SELECT ap FROM ActivityParticipant ap WHERE ap.user.id = :userId")
    Page<ActivityParticipant> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ap FROM ActivityParticipant ap WHERE ap.activity.id = :activityId AND ap.status = :status")
    List<ActivityParticipant> findByActivityIdAndStatus(@Param("activityId") Long activityId,
                                                          @Param("status") ParticipantStatus status);

    @Query("SELECT ap FROM ActivityParticipant ap WHERE ap.activity.id = :activityId AND ap.status != :status")
    List<ActivityParticipant> findByActivityIdAndStatusNot(@Param("activityId") Long activityId,
                                                            @Param("status") ParticipantStatus status);

    @Query("SELECT COUNT(ap) FROM ActivityParticipant ap WHERE ap.activity.id = :activityId AND ap.status = :status")
    Long countByActivityIdAndStatus(@Param("activityId") Long activityId,
                                      @Param("status") ParticipantStatus status);

    @Query("SELECT ap FROM ActivityParticipant ap WHERE ap.activity.creator.id = :creatorId")
    Page<ActivityParticipant> findByActivityCreatorId(@Param("creatorId") Long creatorId, Pageable pageable);

    @Query("SELECT COUNT(ap) FROM ActivityParticipant ap WHERE ap.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    List<ActivityParticipant> findByStatus(ParticipantStatus status);
}
