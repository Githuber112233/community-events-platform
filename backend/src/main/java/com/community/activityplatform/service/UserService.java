package com.community.activityplatform.service;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.LoginRequest;
import com.community.activityplatform.dto.RegisterRequest;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.dto.UserDTO;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityLike;
import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.Interest;
import com.community.activityplatform.entity.User;
import com.community.activityplatform.entity.UserFollow;
import com.community.activityplatform.entity.UserInterest;
import com.community.activityplatform.repository.*;
import com.community.activityplatform.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final InterestRepository interestRepository;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final ActivityLikeRepository likeRepository;
    private final UserFollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public Result<Map<String, Object>> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        if (user.getStatus() == User.UserStatus.INACTIVE) {
            return Result.error("账号未激活");
        }

        if (user.getStatus() == User.UserStatus.BANNED) {
            return Result.error("账号已被封禁");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("refreshToken", refreshToken);
        data.put("user", convertToDTO(user));

        return Result.success(data);
    }

    /**
     * 用户注册
     */
    @Transactional
    public Result<UserDTO> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return Result.error("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return Result.error("邮箱已被注册");
        }

        // 检查手机号是否已存在
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            return Result.error("手机号已被注册");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .avatar(request.getAvatar())
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .credits(0)
                .build();

        userRepository.save(user);
        return Result.success(convertToDTO(user));
    }

    /**
     * 获取用户信息
     */
    public Result<UserDTO> getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return Result.error("用户不存在");
        }

        return Result.success(convertToDTO(user));
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public Result<UserDTO> updateUserInfo(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return Result.error("用户不存在");
        }

        if (userDTO.getNickname() != null) {
            user.setNickname(userDTO.getNickname());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getGender() != null) {
            user.setGender(userDTO.getGender());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }
        if (userDTO.getProvince() != null) {
            user.setProvince(userDTO.getProvince());
        }
        if (userDTO.getCity() != null) {
            user.setCity(userDTO.getCity());
        }
        if (userDTO.getDistrict() != null) {
            user.setDistrict(userDTO.getDistrict());
        }
        if (userDTO.getAvatar() != null) {
            user.setAvatar(userDTO.getAvatar());
        }

        userRepository.save(user);
        return Result.success(convertToDTO(user));
    }

    /**
     * 更新用户兴趣标签
     */
    @Transactional
    public Result<Void> updateUserInterests(Long userId, List<Long> interestIds) {
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return Result.error("用户不存在");
        }

        // 删除旧的兴趣标签
        userInterestRepository.deleteByUserId(userId);

        // 添加新的兴趣标签
        List<UserInterest> userInterests = new ArrayList<>();
        for (Long interestId : interestIds) {
            Interest interest = interestRepository.findById(interestId).orElse(null);
            if (interest != null) {
                UserInterest userInterest = UserInterest.builder()
                        .user(user)
                        .interest(interest)
                        .weight(1)
                        .clickCount(0)
                        .participateCount(0)
                        .build();
                userInterests.add(userInterest);
            }
        }

        userInterestRepository.saveAll(userInterests);
        return Result.success();
    }

    /**
     * 获取用户兴趣标签
     */
    public Result<List<InterestDTO>> getUserInterests(Long userId) {
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

        return Result.success(interestDTOs);
    }

    /**
     * 关注用户
     */
    @Transactional
    public Result<Void> followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            return Result.error("不能关注自己");
        }

        User follower = userRepository.findById(followerId).orElse(null);
        User followed = userRepository.findById(followedId).orElse(null);

        if (follower == null || followed == null) {
            return Result.error("用户不存在");
        }

        if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            return Result.error("已经关注过该用户");
        }

        UserFollow follow =
                UserFollow.builder()
                        .follower(follower)
                        .followed(followed)
                        .build();

        followRepository.save(follow);
        return Result.success();
    }

    /**
     * 取消关注用户
     */
    @Transactional
    public Result<Void> unfollowUser(Long followerId, Long followedId) {
        followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .ifPresent(followRepository::delete);
        return Result.success();
    }

    /**
     * 获取用户粉丝列表
     */
    public Result<List<UserDTO>> getFollowers(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Page<UserFollow> followersPage = followRepository.findFollowersByUserId(userId, Pageable.unpaged());
        List<UserDTO> followerDTOs = followersPage.getContent().stream()
                .map(follow -> convertToDTO(follow.getFollower()))
                .collect(Collectors.toList());
        return Result.success(followerDTOs);
    }

    /**
     * 获取用户关注列表
     */
    public Result<List<UserDTO>> getFollowing(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Page<UserFollow> followingPage = followRepository.findFollowingByUserId(userId, Pageable.unpaged());
        List<UserDTO> followingDTOs = followingPage.getContent().stream()
                .map(follow -> convertToDTO(follow.getFollowed()))
                .collect(Collectors.toList());
        return Result.success(followingDTOs);
    }

    /**
     * 获取用户活动列表（创建/参与/收藏）
     */
    public Result<List<ActivityDTO>> getUserActivities(Long userId, String type, Pageable pageable) {
        switch (type) {
            case "created":
                Page<Activity> createdActivities = activityRepository.findByCreatorId(userId, pageable);
                List<ActivityDTO> createdDTOs = createdActivities.getContent().stream()
                        .map(this::convertActivityToDTO)
                        .collect(Collectors.toList());
                return Result.success(createdDTOs);
            case "participating":
                Page<ActivityParticipant> participants = participantRepository.findByUserId(userId, pageable);
                List<ActivityDTO> participatingDTOs = participants.getContent().stream()
                        .map(p -> convertActivityToDTO(p.getActivity()))
                        .collect(Collectors.toList());
                return Result.success(participatingDTOs);
            case "liked":
                Page<ActivityLike> likes = likeRepository.findByUserId(userId, pageable);
                List<ActivityDTO> likedDTOs = likes.getContent().stream()
                        .map(l -> convertActivityToDTO(l.getActivity()))
                        .collect(Collectors.toList());
                return Result.success(likedDTOs);
            default:
                return Result.error("不支持的活动类型");
        }
    }

    private ActivityDTO convertActivityToDTO(Activity activity) {
        ActivityDTO.ActivityDTOBuilder builder = ActivityDTO.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .content(activity.getContent())
                .coverImage(activity.getCoverImage())
                .city(activity.getCity())
                .district(activity.getDistrict())
                .address(activity.getAddress())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .status(activity.getStatus())
                .currentParticipants(participantRepository.countByActivityIdAndStatus(activity.getId(),
                        ActivityParticipant.ParticipantStatus.APPROVED).intValue())
                .maxParticipants(activity.getMaxParticipants())
                .viewCount(activity.getViewCount())
                .likeCount(activity.getLikeCount())
                .commentCount(activity.getCommentCount())
                .fee(activity.getFee())
                .createdAt(activity.getCreatedAt());

        if (activity.getCreator() != null) {
            builder.creator(com.community.activityplatform.dto.UserDTO.builder()
                    .id(activity.getCreator().getId())
                    .username(activity.getCreator().getUsername())
                    .nickname(activity.getCreator().getNickname())
                    .avatar(activity.getCreator().getAvatar())
                    .build());
        }
        return builder.build();
    }

    /**
     * Entity转DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
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
                .credits(user.getCredits())
                .status(user.getStatus().name())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt());

        // 获取用户兴趣
        List<UserInterest> userInterests = userInterestRepository.findByUserId(user.getId());
        List<InterestDTO> interestDTOs = userInterests.stream()
                .map(ui -> InterestDTO.builder()
                        .id(ui.getInterest().getId())
                        .name(ui.getInterest().getName())
                        .description(ui.getInterest().getDescription())
                        .category(ui.getInterest().getCategory())
                        .popularity(ui.getInterest().getPopularity())
                        .active(ui.getInterest().getActive())
                        .build())
                .collect(Collectors.toList());
        builder.interests(interestDTOs);

        // 获取关注数和粉丝数
        Long followingCount = followRepository.countFollowingByUserId(user.getId());
        Long followersCount = followRepository.countFollowersByUserId(user.getId());
        builder.followingCount(followingCount);
        builder.followersCount(followersCount);

        return builder.build();
    }
}
