package com.community.activityplatform.service;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.PageDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.dto.UserDTO;
import com.community.activityplatform.dto.UserProfileDTO;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityLike;
import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.Interest;
import com.community.activityplatform.entity.User;
import com.community.activityplatform.entity.UserInterest;
import com.community.activityplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.community.activityplatform.entity.ActivityParticipant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository participantRepository;
    private final ActivityCommentRepository commentRepository;
    private final ActivityLikeRepository likeRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserFollowRepository userFollowRepository;
    private final InterestRepository interestRepository;

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
     * 获取所有用户的兴趣权重汇总数据
     * 返回结构：{
     *   users: [{id, nickname, avatar, interests: [{interestId, interestName, weight, clickCount, participateCount}]}],
     *   interests: [{id, name, category}],  // 所有兴趣标签（列头）
     *   maxWeight: number  // 最大权重值（用于前端归一化）
     * }
     */
    public Result<Map<String, Object>> getAllUsersInterestWeights() {
        // 1. 获取所有用户
        List<User> allUsers = userRepository.findAll();

        // 2. 获取所有兴趣标签（排序保持一致）
        List<Interest> allInterests = interestRepository.findByActiveTrueOrderByPopularityDesc();

        // 3. 查询所有 UserInterest 记录
        List<UserInterest> allUserInterests = userInterestRepository.findAll();

        // 按 userId 分组
        Map<Long, List<UserInterest>> userInterestsMap = allUserInterests.stream()
                .collect(Collectors.groupingBy(ui -> ui.getUser().getId()));

        // 4. 构建返回数据
        int maxWeight = 1;
        List<Map<String, Object>> usersData = new ArrayList<>();

        for (User user : allUsers) {
            List<UserInterest> userInterests = userInterestsMap.getOrDefault(user.getId(), Collections.emptyList());

            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", user.getId());
            userMap.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
            userMap.put("avatar", user.getAvatar());
            userMap.put("role", user.getRole().name());

            List<Map<String, Object>> interestsList = new ArrayList<>();
            for (UserInterest ui : userInterests) {
                Map<String, Object> interestMap = new LinkedHashMap<>();
                interestMap.put("interestId", ui.getInterest().getId());
                interestMap.put("interestName", ui.getInterest().getName());
                interestMap.put("category", ui.getInterest().getCategory());
                interestMap.put("weight", ui.getWeight());
                interestMap.put("clickCount", ui.getClickCount());
                interestMap.put("participateCount", ui.getParticipateCount());
                interestsList.add(interestMap);

                if (ui.getWeight() != null && ui.getWeight() > maxWeight) {
                    maxWeight = ui.getWeight();
                }
            }

            // 按权重降序排列
            interestsList.sort((a, b) -> Integer.compare(
                    (Integer) b.getOrDefault("weight", 0),
                    (Integer) a.getOrDefault("weight", 0)));

            userMap.put("interests", interestsList);
            userMap.put("interestCount", interestsList.size());
            usersData.add(userMap);
        }

        // 5. 按用户兴趣数量降序排列
        usersData.sort((a, b) -> Integer.compare(
                (Integer) b.getOrDefault("interestCount", 0),
                (Integer) a.getOrDefault("interestCount", 0)));

        // 6. 构建兴趣标签列表（列头信息）
        List<Map<String, Object>> interestsInfo = allInterests.stream()
                .map(interest -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", interest.getId());
                    m.put("name", interest.getName());
                    m.put("category", interest.getCategory());
                    m.put("popularity", interest.getPopularity());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("users", usersData);
        result.put("interests", interestsInfo);
        result.put("maxWeight", maxWeight);
        result.put("totalUsers", allUsers.size());
        result.put("totalInterests", allInterests.size());

        return Result.success(result);
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

    /**
     * 回填用户兴趣的 clickCount、participateCount 和 weight
     * 根据历史 ActivityLike 和 ActivityParticipant 记录反推
     *
     * 权重计算公式（参考小红书/豆瓣/Meetup 的行为分层加权策略）：
     *   interestScore = log(1 + clickCount) × W_click
     *                  + log(1 + participateCount) × W_participate
     *   其中 W_click=1, W_participate=5（参与行为权重是点击的5倍）
     *   weight = max(1, ceil(interestScore × 10))
     *   用 log 平滑避免线性膨胀，保留区分度
     */
    @Transactional
    public Result<Map<String, Object>> backfillInterestCounts() {
        // 1. 预加载所有活动（缓存），避免 N+1 查询
        Map<Long, Activity> activityCache = activityRepository.findAll().stream()
                .collect(Collectors.toMap(Activity::getId, a -> a));

        // 用于批量保存
        Map<String, UserInterest> toSave = new HashMap<>(); // key = "userId-interestId"

        // 2. 从 ActivityLike 回填 clickCount（点赞算一次点击）
        List<ActivityLike> allLikes = likeRepository.findAll();
        log.info("回填兴趣计数：共 {} 条点赞记录", allLikes.size());
        for (ActivityLike like : allLikes) {
            Long userId = like.getUser().getId();
            Long activityId = like.getActivity().getId();
            Activity activity = activityCache.get(activityId);
            if (activity == null) continue;

            for (Interest interest : activity.getInterests()) {
                String key = userId + "-" + interest.getId();
                UserInterest ui = toSave.get(key);
                if (ui == null) {
                    ui = userInterestRepository.findByUserIdAndInterestId(userId, interest.getId()).orElse(null);
                    if (ui == null) {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user == null) continue;
                        ui = UserInterest.builder()
                                .user(user)
                                .interest(interest)
                                .weight(1)
                                .clickCount(0)
                                .participateCount(0)
                                .build();
                    }
                }
                ui.setClickCount(ui.getClickCount() + 1);
                toSave.put(key, ui);
            }
        }

        // 3. 从 ActivityParticipant（APPROVED/CHECKED_IN）回填 participateCount
        List<ActivityParticipant> allParticipants = participantRepository.findAll();
        long approvedCount = allParticipants.stream()
                .filter(p -> p.getStatus() == ActivityParticipant.ParticipantStatus.APPROVED
                        || p.getStatus() == ActivityParticipant.ParticipantStatus.CHECKED_IN)
                .count();
        log.info("回填兴趣计数：共 {} 条参与记录，其中 {} 条有效", allParticipants.size(), approvedCount);

        for (ActivityParticipant participant : allParticipants) {
            if (participant.getStatus() != ActivityParticipant.ParticipantStatus.APPROVED
                    && participant.getStatus() != ActivityParticipant.ParticipantStatus.CHECKED_IN) {
                continue;
            }
            Long userId = participant.getUser().getId();
            Long activityId = participant.getActivity().getId();
            Activity activity = activityCache.get(activityId);
            if (activity == null) continue;

            for (Interest interest : activity.getInterests()) {
                String key = userId + "-" + interest.getId();
                UserInterest ui = toSave.get(key);
                if (ui == null) {
                    ui = userInterestRepository.findByUserIdAndInterestId(userId, interest.getId()).orElse(null);
                    if (ui == null) {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user == null) continue;
                        ui = UserInterest.builder()
                                .user(user)
                                .interest(interest)
                                .weight(1)
                                .clickCount(0)
                                .participateCount(0)
                                .build();
                    }
                }
                ui.setParticipateCount(ui.getParticipateCount() + 1);
                toSave.put(key, ui);
            }
        }

        // 4. 根据行为数据重新计算 weight
        // 公式：weight = max(1, ceil((log(1+clickCount)*1 + log(1+participateCount)*5) * 10))
        for (UserInterest ui : toSave.values()) {
            int clicks = Math.max(0, ui.getClickCount());
            int participates = Math.max(0, ui.getParticipateCount());
            double score = Math.log(1 + clicks) * 1.0 + Math.log(1 + participates) * 5.0;
            int newWeight = Math.max(1, (int) Math.ceil(score * 10));
            ui.setWeight(newWeight);
        }

        // 5. 批量保存
        List<UserInterest> saved = userInterestRepository.saveAll(toSave.values());
        log.info("回填完成：更新了 {} 条 UserInterest 记录", saved.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalLikesProcessed", allLikes.size());
        result.put("totalParticipantsProcessed", allParticipants.size());
        result.put("userInterestsUpdated", saved.size());
        return Result.success(result);
    }

    /**
     * 获取 User-CF 协同过滤算法可视化数据
     * 包含：用户节点列表、相似度矩阵、推荐路径、融合权重指标
     */
    public Result<Map<String, Object>> getUserCFVisualization() {
        List<User> allUsers = userRepository.findAll();

        // 1. 构建用户节点列表（含活动ID集合）
        List<Map<String, Object>> userNodes = new ArrayList<>();
        Map<Long, Set<Long>> userActivityMap = new HashMap<>();    // userId -> 参与的活动ID集合
        Map<Long, Set<Long>> userInterestMap = new HashMap<>();   // userId -> 兴趣标签ID集合
        Map<Long, String> userNameMap = new HashMap<>();          // userId -> 昵称
        Map<Long, String> userAvatarMap = new HashMap<>();        // userId -> 头像
        Map<Long, Long> userBehaviorCountMap = new HashMap<>();   // userId -> 行为数

        for (User user : allUsers) {
            userNameMap.put(user.getId(), user.getNickname() != null ? user.getNickname() : user.getUsername());
            userAvatarMap.put(user.getId(), user.getAvatar());

            // 参与活动
            Set<Long> activityIds = participantRepository.findByUserId(user.getId(), PageRequest.of(0, 200))
                    .getContent().stream()
                    .filter(p -> p.getStatus() == ActivityParticipant.ParticipantStatus.APPROVED
                            || p.getStatus() == ActivityParticipant.ParticipantStatus.CHECKED_IN)
                    .map(p -> p.getActivity().getId())
                    .collect(Collectors.toSet());
            userActivityMap.put(user.getId(), activityIds);

            // 兴趣标签
            Set<Long> interestIds = userInterestRepository.findByUserId(user.getId()).stream()
                    .map(ui -> ui.getInterest().getId())
                    .collect(Collectors.toSet());
            userInterestMap.put(user.getId(), interestIds);

            // 行为计数
            long participateCount = participantRepository.countByUserId(user.getId());
            long likeCount = likeRepository.countByUserId(user.getId());
            userBehaviorCountMap.put(user.getId(), participateCount + likeCount);

            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", user.getId());
            node.put("nickname", userNameMap.get(user.getId()));
            node.put("avatar", user.getAvatar());
            node.put("role", user.getRole().name());
            node.put("activityCount", activityIds.size());
            node.put("interestCount", interestIds.size());
            node.put("behaviorCount", participateCount + likeCount);
            userNodes.add(node);
        }

        // 2. 计算用户间相似度矩阵（杰卡德系数）
        List<Map<String, Object>> similarityEdges = new ArrayList<>();
        Map<String, Double> similarityMatrix = new LinkedHashMap<>(); // "userId1-userId2" -> similarity
        double maxSimilarity = 0.0;

        for (int i = 0; i < allUsers.size(); i++) {
            for (int j = i + 1; j < allUsers.size(); j++) {
                Long idA = allUsers.get(i).getId();
                Long idB = allUsers.get(j).getId();
                Set<Long> actsA = userActivityMap.get(idA);
                Set<Long> actsB = userActivityMap.get(idB);

                double similarity = 0.0;
                // 基于参与活动的杰卡德
                if (!actsA.isEmpty() || !actsB.isEmpty()) {
                    Set<Long> intersection = new HashSet<>(actsA);
                    intersection.retainAll(actsB);
                    Set<Long> union = new HashSet<>(actsA);
                    union.addAll(actsB);
                    similarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
                }
                // 兜底：基于兴趣标签的杰卡德
                if (similarity == 0.0) {
                    Set<Long> intsA = userInterestMap.get(idA);
                    Set<Long> intsB = userInterestMap.get(idB);
                    if (!intsA.isEmpty() || !intsB.isEmpty()) {
                        Set<Long> inter = new HashSet<>(intsA);
                        inter.retainAll(intsB);
                        Set<Long> uni = new HashSet<>(intsA);
                        uni.addAll(intsB);
                        similarity = uni.isEmpty() ? 0.0 : (double) inter.size() / uni.size();
                    }
                }

                if (similarity > 0) {
                    String key = Math.min(idA, idB) + "-" + Math.max(idA, idB);
                    similarityMatrix.put(key, similarity);
                    if (similarity > maxSimilarity) maxSimilarity = similarity;

                    Map<String, Object> edge = new LinkedHashMap<>();
                    edge.put("source", idA);
                    edge.put("sourceName", userNameMap.get(idA));
                    edge.put("target", idB);
                    edge.put("targetName", userNameMap.get(idB));
                    edge.put("similarity", Math.round(similarity * 10000.0) / 10000.0);
                    similarityEdges.add(edge);
                }
            }
        }

        // 3. 为每个用户生成推荐路径（我 → 相似用户 → 他们参与的活动）
        List<Map<String, Object>> recommendationPaths = new ArrayList<>();
        for (User user : allUsers) {
            Long userId = user.getId();
            Set<Long> myActivities = userActivityMap.get(userId);

            // 找到与该用户相似度最高的 Top-5 用户
            List<Map<String, Object>> mySimilarUsers = similarityEdges.stream()
                    .filter(e -> e.get("source").equals(userId) || e.get("target").equals(userId))
                    .sorted((a, b) -> Double.compare((Double) b.get("similarity"), (Double) a.get("similarity")))
                    .limit(5)
                    .collect(Collectors.toList());

            Set<Long> recommendedActivityIds = new LinkedHashSet<>();
            List<Map<String, Object>> pathSteps = new ArrayList<>();

            for (Map<String, Object> edge : mySimilarUsers) {
                Long similarId = edge.get("source").equals(userId) ? (Long) edge.get("target") : (Long) edge.get("source");
                Double sim = (Double) edge.get("similarity");
                Set<Long> similarActivities = userActivityMap.get(similarId);

                if (similarActivities != null) {
                    Set<Long> newActivities = new HashSet<>(similarActivities);
                    newActivities.removeAll(myActivities);
                    recommendedActivityIds.addAll(newActivities);

                    for (Long actId : newActivities) {
                        Map<String, Object> step = new LinkedHashMap<>();
                        step.put("userId", userId);
                        step.put("similarUserId", similarId);
                        step.put("similarUserName", userNameMap.get(similarId));
                        step.put("similarity", sim);
                        step.put("activityId", actId);
                        step.put("score", sim);
                        pathSteps.add(step);
                    }
                }
            }

            if (!pathSteps.isEmpty()) {
                Map<String, Object> path = new LinkedHashMap<>();
                path.put("userId", userId);
                path.put("userName", userNameMap.get(userId));
                path.put("similarUserCount", mySimilarUsers.size());
                path.put("recommendedActivityCount", recommendedActivityIds.size());
                path.put("steps", pathSteps.stream().limit(20).collect(Collectors.toList())); // 限制展示条数
                recommendationPaths.add(path);
            }
        }

        // 4. 融合权重指标（对每个用户计算）
        int cfEnableThreshold = 20; // 与 RecommendationService 默认值一致
        List<Map<String, Object>> weightMetrics = new ArrayList<>();

        int totalCfActive = 0; // CF 置信度 > 0 的用户数
        double totalCfConfidence = 0.0;

        for (User user : allUsers) {
            Long userId = user.getId();
            long behaviorCount = userBehaviorCountMap.getOrDefault(userId, 0L);
            double cfConfidence = Math.min(1.0, (double) behaviorCount / (cfEnableThreshold * 5.0));
            double cbWeight = 1.0 - cfConfidence * 0.6;
            double cfWeight = cfConfidence * 0.6;

            if (cfConfidence > 0) totalCfActive++;
            totalCfConfidence += cfConfidence;

            Map<String, Object> metric = new LinkedHashMap<>();
            metric.put("userId", userId);
            metric.put("userName", userNameMap.get(userId));
            metric.put("behaviorCount", behaviorCount);
            metric.put("cfConfidence", Math.round(cfConfidence * 10000.0) / 10000.0);
            metric.put("cbWeight", Math.round(cbWeight * 10000.0) / 10000.0);
            metric.put("cfWeight", Math.round(cfWeight * 10000.0) / 10000.0);
            weightMetrics.add(metric);
        }

        // 5. 汇总统计
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalUsers", allUsers.size());
        overview.put("totalSimilarityEdges", similarityEdges.size());
        overview.put("maxSimilarity", Math.round(maxSimilarity * 10000.0) / 10000.0);
        overview.put("avgSimilarity", similarityEdges.isEmpty() ? 0.0 :
                Math.round(similarityEdges.stream().mapToDouble(e -> (Double) e.get("similarity")).average().orElse(0.0) * 10000.0) / 10000.0);
        overview.put("cfActiveUsers", totalCfActive);
        overview.put("avgCfConfidence", allUsers.isEmpty() ? 0.0 :
                Math.round((totalCfConfidence / allUsers.size()) * 10000.0) / 10000.0);
        overview.put("algorithm", "User-CF (Jaccard + Content-Based Hybrid)");

        // 组装返回数据
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", overview);
        result.put("userNodes", userNodes);
        result.put("similarityEdges", similarityEdges);
        result.put("recommendationPaths", recommendationPaths);
        result.put("weightMetrics", weightMetrics);

        return Result.success(result);
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