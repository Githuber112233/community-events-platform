package com.community.activityplatform.service;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityComment;
import com.community.activityplatform.entity.ActivityLike;
import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.ActivityView;
import com.community.activityplatform.entity.Interest;
import com.community.activityplatform.entity.User;
import com.community.activityplatform.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动服务
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final ActivityLikeRepository likeRepository;
    private final ActivityCommentRepository commentRepository;
    private final ActivityViewRepository viewRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;

    /**
     * 创建活动
     */
    @Transactional
    public Result<ActivityDTO> createActivity(Long creatorId, ActivityDTO activityDTO) {
        // 获取创建者，判断是否为管理员
        User creator = userRepository.findById(creatorId).orElse(null);
        Activity.ActivityStatus initialStatus =
                (creator != null && creator.getRole() == User.UserRole.ADMIN)
                        ? Activity.ActivityStatus.RECRUITING   // 管理员直接通过
                        : Activity.ActivityStatus.PENDING;   // 普通用户需审核

        Activity activity = Activity.builder()
                .title(activityDTO.getTitle())
                .description(activityDTO.getDescription())
                .content(activityDTO.getContent())
                .coverImage(activityDTO.getCoverImage())
                .creator(creator)
                .province(activityDTO.getProvince())
                .city(activityDTO.getCity())
                .district(activityDTO.getDistrict())
                .address(activityDTO.getAddress())
                .latitude(activityDTO.getLatitude())
                .longitude(activityDTO.getLongitude())
                .startTime(activityDTO.getStartTime())
                .endTime(activityDTO.getEndTime())
                .registrationDeadline(activityDTO.getRegistrationDeadline())
                .maxParticipants(activityDTO.getMaxParticipants() != null ? activityDTO.getMaxParticipants() : 100)
                .currentParticipants(0)
                .status(initialStatus)
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .fee(activityDTO.getFee())
                .requirements(activityDTO.getRequirements())
                .contactPhone(activityDTO.getContactPhone())
                .build();

        // 设置兴趣标签
        if (activityDTO.getInterests() != null) {
            List<Long> interestIds = activityDTO.getInterests().stream()
                    .map(InterestDTO::getId)
                    .collect(Collectors.toList());
            List<Interest> interests = interestRepository.findAllById(interestIds);
            activity.setInterests(interests);
        }

        activityRepository.save(activity);
        return Result.success(convertToDTO(activity));
    }

    /**
     * 获取活动详情
     */
    public Result<ActivityDTO> getActivityDetail(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        ActivityDTO activityDTO = convertToDTO(activity);

        // 检查是否已点赞
        if (userId != null) {
            activityDTO.setIsLiked(likeRepository.existsByUser_IdAndActivity_Id(userId, activityId));

            // 检查是否已参与
            Optional<ActivityParticipant> participant = participantRepository.findByUser_IdAndActivity_Id(userId, activityId);
            activityDTO.setIsParticipated(participant.isPresent());
        }

        // 增加浏览次数
        activity.setViewCount(activity.getViewCount() + 1);
        activityRepository.save(activity);

        // 记录浏览
        if (userId != null) {
            ActivityView view = ActivityView.builder()
                    .activity(activity)
                    .viewDuration(0)
                    .createdAt(LocalDateTime.now())
                    .build();
            viewRepository.save(view);
        }

        return Result.success(activityDTO);
    }

    /**
     * 获取活动列表(分页)
     * @param status null表示所有状态
     */
    public Result<Page<ActivityDTO>> getActivityList(Activity.ActivityStatus status, Long categoryId, String keyword, Pageable pageable, Long userId) {
        Page<Activity> activities;
        boolean allStatuses = (status == null);

        if (categoryId != null && categoryId > 0) {
            // 按分类筛选
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 分类 + 关键词
                activities = allStatuses
                        ? activityRepository.findByInterestIdAndKeyword(categoryId, keyword.trim(), pageable)
                        : activityRepository.findByInterestIdAndStatus(categoryId, status, pageable);
            } else {
                // 仅分类
                activities = allStatuses
                        ? activityRepository.findByInterestId(categoryId, pageable)
                        : activityRepository.findByInterestIdAndStatus(categoryId, status, pageable);
            }
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 仅关键词
            activities = allStatuses
                    ? activityRepository.findByKeywordAllStatuses(keyword.trim(), pageable)
                    : activityRepository.findByTitleContainingIgnoreCaseAndStatus(keyword.trim(), status, pageable);
        } else {
            // 无筛选条件
            activities = allStatuses
                    ? activityRepository.findAllActivities(pageable)
                    : activityRepository.findByStatus(status, pageable);
        }

        Page<ActivityDTO> activityDTOs = activities.map(activity -> {
            ActivityDTO dto = convertToDTO(activity);
            if (userId != null) {
                dto.setIsLiked(likeRepository.existsByUser_IdAndActivity_Id(userId, activity.getId()));
                Optional<ActivityParticipant> participant = participantRepository.findByUser_IdAndActivity_Id(userId, activity.getId());
                dto.setIsParticipated(participant.isPresent());
            }
            return dto;
        });

        return Result.success(activityDTOs);
    }

    /**
     * 获取活动参与者列表
     */
    public Result<List<Map<String, Object>>> getActivityParticipants(Long activityId, String status) {
        ActivityParticipant.ParticipantStatus participantStatus =
                ActivityParticipant.ParticipantStatus.valueOf(status);

        List<ActivityParticipant> participants = participantRepository
                .findByActivityIdAndStatus(activityId, participantStatus);

        List<Map<String, Object>> result = participants.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getUser().getId());
                    map.put("username", p.getUser().getUsername());
                    map.put("nickname", p.getUser().getNickname());
                    map.put("avatar", p.getUser().getAvatar());
                    map.put("status", p.getStatus().name());
                    map.put("message", p.getMessage());
                    map.put("createdAt", p.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 获取附近活动
     */
    public Result<List<ActivityDTO>> getNearbyActivities(String city, String district, Long userId) {
        List<Activity> activities = activityRepository.findNearbyActivities(
                city, district, LocalDateTime.now(), Activity.ActivityStatus.RECRUITING);

        List<ActivityDTO> activityDTOs = activities.stream()
                .map(activity -> {
                    ActivityDTO dto = convertToDTO(activity);
                    if (userId != null) {
                        dto.setIsLiked(likeRepository.existsByUser_IdAndActivity_Id(userId, activity.getId()));
                        Optional<ActivityParticipant> participant = participantRepository.findByUser_IdAndActivity_Id(userId, activity.getId());
                        dto.setIsParticipated(participant.isPresent());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return Result.success(activityDTOs);
    }

    /**
     * 获取热门活动
     */
    public Result<Page<ActivityDTO>> getPopularActivities(Pageable pageable, Long userId) {
        Page<Activity> activities = activityRepository.findPopularActivities(pageable);

        Page<ActivityDTO> activityDTOs = activities.map(activity -> {
            ActivityDTO dto = convertToDTO(activity);
            if (userId != null) {
                dto.setIsLiked(likeRepository.existsByUser_IdAndActivity_Id(userId, activity.getId()));
                Optional<ActivityParticipant> participant = participantRepository.findByUser_IdAndActivity_Id(userId, activity.getId());
                dto.setIsParticipated(participant.isPresent());
            }
            return dto;
        });

        return Result.success(activityDTOs);
    }

    /**
     * 参与活动
     */
    @Transactional
    public Result<Void> participateActivity(Long userId, Long activityId, String message) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            return Result.error("活动已满员");
        }

        if (activity.getRegistrationDeadline().isBefore(LocalDateTime.now())) {
            return Result.error("报名已截止");
        }

        // 检查是否存在有效的参与记录（不包括已取消的）
        Optional<ActivityParticipant> existingParticipant = participantRepository.findByUser_IdAndActivity_Id(userId, activityId);
        if (existingParticipant.isPresent()) {
            ActivityParticipant.ParticipantStatus status = existingParticipant.get().getStatus();
            if (status == ActivityParticipant.ParticipantStatus.APPROVED) {
                return Result.error("已经参与过该活动");
            } else if (status == ActivityParticipant.ParticipantStatus.PENDING) {
                return Result.error("报名正在审核中，请等待");
            }
            // 如果是 CANCELLED 状态，允许重新报名（更新现有记录）
        }

        ActivityParticipant participant;
        if (existingParticipant.isPresent()) {
            // 重新报名：更新已有记录的状态
            participant = existingParticipant.get();
            participant.setStatus(ActivityParticipant.ParticipantStatus.APPROVED);
            participant.setMessage(message);
            participant.setCreatedAt(LocalDateTime.now());
        } else {
            // 新报名：创建新记录
            participant = ActivityParticipant.builder()
                    .user(userRepository.findById(userId).orElse(null))
                    .activity(activity)
                    .status(ActivityParticipant.ParticipantStatus.APPROVED)
                    .message(message)
                    .build();
        }

        participantRepository.save(participant);

        // 更新活动参与人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activityRepository.save(activity);

        return Result.success();
    }

    /**
     * 取消参与活动
     */
    @Transactional
    public Result<Void> cancelParticipation(Long userId, Long activityId) {
        Optional<ActivityParticipant> participant = participantRepository.findByUser_IdAndActivity_Id(userId, activityId);
        if (!participant.isPresent()) {
            return Result.error("未参与该活动");
        }

        ActivityParticipant p = participant.get();
        // 允许取消 PENDING、APPROVED 状态的参与记录
        if (p.getStatus() != ActivityParticipant.ParticipantStatus.PENDING
                && p.getStatus() != ActivityParticipant.ParticipantStatus.APPROVED) {
            return Result.error("无法取消该参与记录");
        }

        // 如果是 APPROVED 状态，需要更新活动参与人数
        if (p.getStatus() == ActivityParticipant.ParticipantStatus.APPROVED) {
            Activity activity = activityRepository.findById(activityId).orElse(null);
            if (activity != null) {
                activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
                activityRepository.save(activity);
            }
        }

        // 将状态改为 CANCELLED，而不是删除记录（保留历史）
        p.setStatus(ActivityParticipant.ParticipantStatus.CANCELLED);
        participantRepository.save(p);

        return Result.success();
    }

    /**
     * 点赞活动
     */
    @Transactional
    public Result<Void> likeActivity(Long userId, Long activityId) {
        if (likeRepository.existsByUser_IdAndActivity_Id(userId, activityId)) {
            return Result.error("已经点赞过该活动");
        }

        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        ActivityLike like = ActivityLike.builder()
                .user(userRepository.findById(userId).orElse(null))
                .activity(activity)
                .build();

        likeRepository.save(like);

        // 更新点赞数
        activity.setLikeCount(activity.getLikeCount() + 1);
        activityRepository.save(activity);

        return Result.success();
    }

    /**
     * 取消点赞
     */
    @Transactional
    public Result<Void> unlikeActivity(Long userId, Long activityId) {
        likeRepository.findByUser_IdAndActivity_Id(userId, activityId)
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    Activity activity = activityRepository.findById(activityId).orElse(null);
                    if (activity != null) {
                        activity.setLikeCount(Math.max(0, activity.getLikeCount() - 1));
                        activityRepository.save(activity);
                    }
                });

        return Result.success();
    }

    /**
     * Entity转DTO
     */
    private ActivityDTO convertToDTO(Activity activity) {
        ActivityDTO.ActivityDTOBuilder builder = ActivityDTO.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .content(activity.getContent())
                .coverImage(activity.getCoverImage())
                .province(activity.getProvince())
                .city(activity.getCity())
                .district(activity.getDistrict())
                .address(activity.getAddress())
                .latitude(activity.getLatitude())
                .longitude(activity.getLongitude())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .registrationDeadline(activity.getRegistrationDeadline())
                .maxParticipants(activity.getMaxParticipants())
                .currentParticipants(participantRepository.countByActivityIdAndStatus(activity.getId(),
                        ActivityParticipant.ParticipantStatus.APPROVED).intValue())
                .status(activity.getStatus())
                .viewCount(activity.getViewCount())
                .likeCount(activity.getLikeCount())
                .commentCount(activity.getCommentCount())
                .fee(activity.getFee())
                .requirements(activity.getRequirements())
                .contactPhone(activity.getContactPhone())
                .createdAt(activity.getCreatedAt())
                .isLiked(false)
                .isParticipated(false);

        // 设置创建者
        if (activity.getCreator() != null) {
            builder.creator(com.community.activityplatform.dto.UserDTO.builder()
                    .id(activity.getCreator().getId())
                    .username(activity.getCreator().getUsername())
                    .nickname(activity.getCreator().getNickname())
                    .avatar(activity.getCreator().getAvatar())
                    .build());
        }

        // 设置兴趣标签
        if (activity.getInterests() != null) {
            List<InterestDTO> interestDTOs = activity.getInterests().stream()
                    .map(interest -> InterestDTO.builder()
                            .id(interest.getId())
                            .name(interest.getName())
                            .description(interest.getDescription())
                            .category(interest.getCategory())
                            .popularity(interest.getPopularity())
                            .active(interest.getActive())
                            .build())
                    .collect(Collectors.toList());
            builder.interests(interestDTOs);
        }

        return builder.build();
    }
}
