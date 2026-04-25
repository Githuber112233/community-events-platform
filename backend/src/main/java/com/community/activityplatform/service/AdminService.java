package com.community.activityplatform.service;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.PageDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.dto.UserDTO;
import com.community.activityplatform.dto.UserProfileDTO;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.User;
import com.community.activityplatform.entity.UserInterest;
import com.community.activityplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository participantRepository;
    private final ActivityCommentRepository commentRepository;
    private final ActivityLikeRepository likeRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserFollowRepository userFollowRepository;

    /**
     * 获取待审核活动列表
     */
    public Result<PageDTO<ActivityDTO>> getPendingActivities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Activity> activities = activityRepository.findByStatus(Activity.ActivityStatus.PENDING, pageable);
        Page<ActivityDTO> dtos = activities.map(this::convertToDTO);
        return Result.success(convertToPageDTO(dtos));
    }

    /**
     * 获取所有活动（支持筛选状态）
     */
    public Result<PageDTO<ActivityDTO>> getAllActivities(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Activity> activities;
        if (status != null && !status.isEmpty()) {
            Activity.ActivityStatus activityStatus = Activity.ActivityStatus.valueOf(status);
            activities = activityRepository.findByStatus(activityStatus, pageable);
        } else {
            activities = activityRepository.findAll(pageable);
        }
        Page<ActivityDTO> dtos = activities.map(this::convertToDTO);
        return Result.success(convertToPageDTO(dtos));
    }

    /**
     * 审核活动通过
     */
    @Transactional
    public Result<Void> approveActivity(Long activityId) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (activity.getStatus() != Activity.ActivityStatus.PENDING) {
            return Result.error("只能审核待审核状态的活动");
        }
        activity.setStatus(Activity.ActivityStatus.RECRUITING);
        activityRepository.save(activity);
        return Result.success("活动审核通过");
    }

    /**
     * 审核活动拒绝
     */
    @Transactional
    public Result<Void> rejectActivity(Long activityId, String reason) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        if (activity.getStatus() != Activity.ActivityStatus.PENDING) {
            return Result.error("只能审核待审核状态的活动");
        }
        activity.setStatus(Activity.ActivityStatus.REJECTED);
        activityRepository.save(activity);
        return Result.success("活动已拒绝");
    }

    /**
     * 删除活动
     */
    @Transactional
    public Result<Void> deleteActivity(Long activityId) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        activityRepository.delete(activity);
        return Result.success("活动已删除");
    }

    /**
     * 获取所有用户列表
     */
    public Result<PageDTO<UserDTO>> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAll(pageable);
        Page<UserDTO> dtos = users.map(this::convertToUserDTO);
        return Result.success(convertToPageDTO(dtos));
    }

    /**
     * 获取用户画像（管理员专用）
     */
    public Result<UserProfileDTO> getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 构建用户画像
        UserProfileDTO profile = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .province(user.getProvince())
                .city(user.getCity())
                .district(user.getDistrict())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .status(user.getStatus().name())
                .role(user.getRole().name())
                .credits(user.getCredits())
                .createdAt(user.getCreatedAt())
                .lastActiveAt(user.getUpdatedAt())
                .build();

        // 统计数据
        long createdActivities = activityRepository.countByCreatorId(userId);
        long participations = participantRepository.countByUserId(userId);
        long likedActivities = likeRepository.countByUserId(userId);
        long totalComments = commentRepository.countByUserId(userId);
        long followers = userFollowRepository.countFollowersByUserId(userId);
        long following = userFollowRepository.countFollowingByUserId(userId);

        // 获取用户评论过的活动数（去重）
        long commentedActivities = commentRepository.findByUserId(userId, Pageable.unpaged())
                .getContent().stream().map(c -> c.getActivity().getId()).distinct().count();

        // 获取用户收到的点赞总数
        long totalLikesReceived = activityRepository.findByCreatorId(userId, Pageable.unpaged())
                .getContent().stream().mapToLong(a -> a.getLikeCount()).sum();

        UserProfileDTO.Statistics statistics = UserProfileDTO.Statistics.builder()
                .createdActivities(createdActivities)
                .participatingActivities(participations)
                .likedActivities(likedActivities)
                .commentedActivities(commentedActivities)
                .totalComments(totalComments)
                .totalLikes(totalLikesReceived)
                .followers(followers)
                .following(following)
                .build();
        profile.setStatistics(statistics);

        // 兴趣标签
        List<UserInterest> userInterests = userInterestRepository.findByUserId(userId);
        List<InterestDTO> interestDTOs = userInterests.stream()
                .map(ui -> InterestDTO.builder()
                        .id(ui.getInterest().getId())
                        .name(ui.getInterest().getName())
                        .description(ui.getInterest().getDescription())
                        .category(ui.getInterest().getCategory())
                        .popularity(ui.getInterest().getPopularity())
                        .active(ui.getInterest().getActive())
                        .weight(ui.getWeight())
                        .clickCount(ui.getClickCount())
                        .participateCount(ui.getParticipateCount())
                        .build())
                .collect(Collectors.toList());
        profile.setInterests(interestDTOs);

        // 最近创建的活动（最多5条）
        Page<Activity> recentActivitiesPage = activityRepository.findByCreatorId(userId,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<ActivityDTO> recentActivities = recentActivitiesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        profile.setRecentActivities(recentActivities);

        return Result.success(profile);
    }

    /**
     * 获取平台统计数据
     */
    public Result<Object> getStatistics() {
        long totalUsers = userRepository.count();
        long totalActivities = activityRepository.count();
        long pendingActivities = activityRepository.countByStatus(Activity.ActivityStatus.PENDING);
        long recruitingActivities = activityRepository.countByStatus(Activity.ActivityStatus.RECRUITING);

        return Result.success(new Statistics(
                totalUsers,
                totalActivities,
                pendingActivities,
                recruitingActivities
        ));
    }

    // ============ 私有转换方法 ============

    private ActivityDTO convertToDTO(Activity activity) {
        return ActivityDTO.builder()
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
                .currentParticipants(activity.getCurrentParticipants())
                .status(activity.getStatus())
                .viewCount(activity.getViewCount())
                .likeCount(activity.getLikeCount())
                .commentCount(activity.getCommentCount())
                .fee(activity.getFee())
                .requirements(activity.getRequirements())
                .contactPhone(activity.getContactPhone())
                .createdAt(activity.getCreatedAt())
                .creator(activity.getCreator() != null ? UserDTO.builder()
                        .id(activity.getCreator().getId())
                        .username(activity.getCreator().getUsername())
                        .nickname(activity.getCreator().getNickname())
                        .avatar(activity.getCreator().getAvatar())
                        .build() : null)
                .build();
    }

    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .status(user.getStatus().name())
                .role(user.getRole().name())
                .credits(user.getCredits())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private <T> PageDTO<T> convertToPageDTO(Page<T> page) {
        return PageDTO.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * 统计数据内部类
     */
    private record Statistics(
            long totalUsers,
            long totalActivities,
            long pendingActivities,
            long recruitingActivities
    ) {}
}