package com.community.activityplatform.service;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.ActivityParticipant.ParticipantStatus;
import com.community.activityplatform.entity.Interest;
import com.community.activityplatform.entity.UserInterest;
import com.community.activityplatform.repository.ActivityLikeRepository;
import com.community.activityplatform.repository.ActivityParticipantRepository;
import com.community.activityplatform.repository.ActivityRepository;
import com.community.activityplatform.repository.ActivityViewRepository;
import com.community.activityplatform.repository.InterestRepository;
import com.community.activityplatform.repository.UserInterestRepository;
import com.community.activityplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 混合推荐服务
 * 核心算法: Content-Based + Collaborative Filtering + Time Decay
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final ActivityParticipantRepository participantRepository;
    private final ActivityLikeRepository likeRepository;
    private final ActivityViewRepository viewRepository;
    private final InterestRepository interestRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${recommendation.cf-enable-threshold:20}")
    private Integer cfEnableThreshold;

    @Value("${recommendation.top-k-users:30}")
    private Integer topKUsers;

    @Value("${recommendation.top-n-activities:10}")
    private Integer topNActivities;

    @Value("${recommendation.time-decay-factor:0.005}")
    private Double timeDecayFactor;

    @Value("${recommendation.cache-ttl:3600}")
    private Long cacheTtl;

    @Value("${recommendation.cb-tag-weight:0.75}")
    private Double cbTagWeight;

    @Value("${recommendation.cb-quality-weight:0.25}")
    private Double cbQualityWeight;

    // 用户行为反馈权重（用于兴趣标签动态更新）
    private static final double FEEDBACK_PARTICIPATE = 0.1;
    private static final double FEEDBACK_LIKE = 0.05;
    private static final double FEEDBACK_VIEW = 0.01;

    // User-CF 采样上限（用户量大时随机采样避免O(n²)爆炸）
    private static final int USER_SAMPLE_SIZE = 100;

    /**
     * 为用户推荐活动
     */
    @Transactional
    public Result<Page<ActivityDTO>> recommendActivities(Long userId, Integer page, Integer size) {
        log.info("开始获取推荐活动，用户ID: {}, page: {}, size: {}", userId, page, size);
        String cacheKey = "recommend:user:" + userId + ":page:" + page;

        // 尝试从缓存获取
        try {
            @SuppressWarnings("unchecked")
            List<ActivityDTO> cachedResult = (List<ActivityDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                log.info("从缓存获取推荐成功，数量: {}", cachedResult.size());
                Pageable pageable = PageRequest.of(page, size);
                return Result.success(new PageImpl<>(cachedResult, pageable, cachedResult.size()));
            }
        } catch (Exception e) {
            log.warn("Redis缓存获取失败，继续计算推荐: {}", e.getMessage());
        }

        // 获取用户已排除的活动ID（已参与、已点赞等）
        Set<Long> excludedActivityIds = getExcludedActivityIds(userId);
        log.info("用户已排除的活动ID数量: {}", excludedActivityIds.size());

        // 使用混合推荐算法：Content-Based + User-CF
        log.info("开始执行混合推荐算法");
        List<Activity> recommendedActivities;
        try {
            recommendedActivities = hybridRecommendation(userId, excludedActivityIds);
            log.info("混合推荐算法返回 {} 个活动", recommendedActivities.size());
        } catch (Exception e) {
            log.error("混合推荐算法执行失败: {}", e.getMessage(), e);
            // 兜底：返回热门活动
            log.info("使用热门活动作为兜底");
            recommendedActivities = activityRepository
                    .findByStatusAndStartTimeAfterOrderByViewCountDescLikeCountDesc(
                            Activity.ActivityStatus.RECRUITING, LocalDateTime.now(), PageRequest.of(0, topNActivities))
                    .getContent();
            if (recommendedActivities.isEmpty()) {
                recommendedActivities = activityRepository
                        .findByStatusAndStartTimeAfter(
                                Activity.ActivityStatus.RECRUITING, LocalDateTime.now(), PageRequest.of(0, topNActivities))
                        .getContent();
            }
        }

        // 过滤掉已过期的活动
        recommendedActivities = recommendedActivities.stream()
                .filter(a -> a.getStartTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        log.info("过滤后有 {} 个推荐活动", recommendedActivities.size());

        // 转换为DTO
        log.info("开始转换DTO");
        List<ActivityDTO> activityDTOs = recommendedActivities.stream()
                .limit(topNActivities)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("DTO转换完成，数量: {}", activityDTOs.size());

        // 缓存结果（失败不影响返回）
        try {
            redisTemplate.opsForValue().set(cacheKey, activityDTOs, cacheTtl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis缓存设置失败: {}", e.getMessage());
        }

        // 分页
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + size, activityDTOs.size());
        List<ActivityDTO> pageContent = start < activityDTOs.size() ? activityDTOs.subList(start, end) : Collections.emptyList();
        Page<ActivityDTO> resultPage = new PageImpl<>(pageContent, pageable, activityDTOs.size());

        return Result.success(resultPage);
    }

    /**
     * 基于内容的推荐(Content-Based)
     * 综合标签相似度 + 活动质量分 + 时间衰减
     */
    private List<Activity> contentBasedRecommendation(Long userId, Set<Long> excludedActivityIds) {
        // 获取用户兴趣标签
        List<UserInterest> userInterests = userInterestRepository.findByUserId(userId);
        if (userInterests.isEmpty()) {
            // 如果没有兴趣标签,返回正在招募的热门活动
            List<Activity> popularActivities = activityRepository
                    .findByStatusAndStartTimeAfterOrderByViewCountDescLikeCountDesc(
                            Activity.ActivityStatus.RECRUITING, LocalDateTime.now(), PageRequest.of(0, topNActivities))
                    .getContent();
            if (popularActivities.isEmpty()) {
                popularActivities = activityRepository
                        .findByStatusAndStartTimeAfter(
                                Activity.ActivityStatus.RECRUITING, LocalDateTime.now(), PageRequest.of(0, topNActivities))
                        .getContent();
            }
            return popularActivities;
        }

        // 构建用户兴趣向量
        Map<Long, Double> userInterestVector = new HashMap<>();
        for (UserInterest ui : userInterests) {
            double weight = (ui.getClickCount() * 0.3 + ui.getParticipateCount() * 0.7) * ui.getWeight();
            userInterestVector.put(ui.getInterest().getId(), weight);
        }

        // 获取所有正在招募的活动
        List<Activity> allActivities = activityRepository.findByStatusAndStartTimeAfter(
                Activity.ActivityStatus.RECRUITING, LocalDateTime.now(), PageRequest.of(0, 1000))
                .getContent();

        // 计算全局最大值（用于归一化质量分）
        double maxLikeCount = allActivities.stream()
                .mapToDouble(a -> a.getLikeCount() != null ? a.getLikeCount() : 0)
                .max().orElse(1.0);
        double maxCommentCount = allActivities.stream()
                .mapToDouble(a -> a.getCommentCount() != null ? a.getCommentCount() : 0)
                .max().orElse(1.0);
        double maxViewCount = allActivities.stream()
                .mapToDouble(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .max().orElse(1.0);

        // 计算每个活动的综合评分
        Map<Activity, Double> activityScores = new HashMap<>();
        for (Activity activity : allActivities) {
            if (excludedActivityIds.contains(activity.getId())) {
                continue;
            }

            // ① 标签相似度
            Map<Long, Double> activityInterestVector = new HashMap<>();
            for (Interest interest : activity.getInterests()) {
                activityInterestVector.put(interest.getId(), 1.0);
            }
            double tagScore = calculateCosineSimilarity(userInterestVector, activityInterestVector);

            // ② 活动质量分（归一化到[0,1]）
            double likeNorm = (activity.getLikeCount() != null ? activity.getLikeCount() : 0) / maxLikeCount;
            double commentNorm = (activity.getCommentCount() != null ? activity.getCommentCount() : 0) / maxCommentCount;
            double viewNorm = (activity.getViewCount() != null ? activity.getViewCount() : 0) / maxViewCount;
            double qualityScore = likeNorm * 0.5 + commentNorm * 0.3 + viewNorm * 0.2;

            // ③ 综合CB评分 = α * 标签分 + (1-α) * 质量分
            double combinedCB = cbTagWeight * tagScore + cbQualityWeight * qualityScore;

            // ④ 时间衰减
            double timeDecay = calculateTimeDecay(activity.getStartTime());

            // 最终评分 = CB综合分 × 时间衰减
            double finalScore = combinedCB * timeDecay;
            activityScores.put(activity, finalScore);
        }

        return activityScores.entrySet().stream()
                .sorted(Map.Entry.<Activity, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 混合推荐(Content-Based + User-CF)
     * 使用置信度加权融合，避免数据稀疏时CF引入噪声
     */
    private List<Activity> hybridRecommendation(Long userId, Set<Long> excludedActivityIds) {
        // 1. 基于内容的推荐分数（归一化到[0,1]）
        Map<Activity, Double> contentScores = new HashMap<>();
        List<Activity> contentActivities = contentBasedRecommendation(userId, excludedActivityIds);
        for (int i = 0; i < contentActivities.size(); i++) {
            double score = 1.0 - (double) i / Math.max(contentActivities.size(), 1);
            contentScores.put(contentActivities.get(i), score);
        }

        // 2. User-CF 推荐分数
        Map<Activity, Double> cfScores = userCollaborativeFiltering(userId, excludedActivityIds);
        // 归一化 CF 分数
        double maxCfScore = cfScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        if (maxCfScore > 0) {
            cfScores.replaceAll((k, v) -> v / maxCfScore);
        }

        // 3. 置信度加权融合
        // CF置信度：行为数据越多，CF越可靠；最低权重0，最高0.6
        int userBehaviorCount = getUserBehaviorCount(userId);
        double cfConfidence = Math.min(1.0, (double) userBehaviorCount / (cfEnableThreshold * 5.0));
        double cbWeight = 1.0 - cfConfidence * 0.6;   // CB最低占40%
        double cfWeight = cfConfidence * 0.6;          // CF最高占60%

        log.info("混合推荐权重 - CB: {:.2f}, CF: {:.2f} (置信度: {:.2f}, 行为数: {})",
                cbWeight, cfWeight, cfConfidence, userBehaviorCount);

        Map<Activity, Double> finalScores = new HashMap<>();

        for (Map.Entry<Activity, Double> entry : contentScores.entrySet()) {
            finalScores.put(entry.getKey(), entry.getValue() * cbWeight);
        }
        for (Map.Entry<Activity, Double> entry : cfScores.entrySet()) {
            Activity activity = entry.getKey();
            double score = entry.getValue() * cfWeight;
            finalScores.put(activity, finalScores.getOrDefault(activity, 0.0) + score);
        }

        return finalScores.entrySet().stream()
                .sorted(Map.Entry.<Activity, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 基于用户的协同过滤(User-CF)
     * 使用杰卡德系数计算用户相似度，支持采样优化
     */
    private Map<Activity, Double> userCollaborativeFiltering(Long userId, Set<Long> excludedActivityIds) {
        // 获取目标用户参与的活动ID集合
        List<ActivityParticipant> userParticipations = participantRepository.findByUserId(userId, PageRequest.of(0, 200))
                .getContent();
        Set<Long> userActivityIds = userParticipations.stream()
                .map(p -> p.getActivity().getId())
                .collect(Collectors.toSet());

        // 获取所有用户（带采样优化）
        List<com.community.activityplatform.entity.User> allUsers = new ArrayList<>(userRepository.findAll());

        // 采样优化：用户量超过阈值时随机采样
        if (allUsers.size() > USER_SAMPLE_SIZE) {
            Collections.shuffle(allUsers);
            allUsers = new ArrayList<>(allUsers.subList(0, USER_SAMPLE_SIZE));
        }

        // 计算用户相似度（杰卡德系数：共同参与活动数 / 并集活动数）
        Map<Long, Double> userSimilarities = new HashMap<>();
        for (com.community.activityplatform.entity.User otherUser : allUsers) {
            if (otherUser.getId().equals(userId)) {
                continue;
            }
            double similarity = calculateJaccardSimilarity(userId, otherUser.getId());
            if (similarity > 0) {
                userSimilarities.put(otherUser.getId(), similarity);
            }
        }

        // 找到Top-K相似用户
        List<Long> similarUserIds = userSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topKUsers)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 获取相似用户参与的活动，计算推荐分
        Map<Activity, Double> recommendedScores = new HashMap<>();
        for (Long similarUserId : similarUserIds) {
            List<ActivityParticipant> similarParticipations = participantRepository
                    .findByUserId(similarUserId, PageRequest.of(0, 100))
                    .getContent();

            for (ActivityParticipant participation : similarParticipations) {
                Activity activity = participation.getActivity();

                if (userActivityIds.contains(activity.getId()) || excludedActivityIds.contains(activity.getId())) {
                    continue;
                }
                if (activity.getStartTime().isBefore(LocalDateTime.now())) {
                    continue;
                }

                double statusWeight = participation.getStatus() == ParticipantStatus.CHECKED_IN ? 1.2 : 1.0;
                double score = userSimilarities.get(similarUserId) * statusWeight;
                recommendedScores.merge(activity, score, Double::sum);
            }
        }

        return recommendedScores;
    }

    /**
     * 计算用户杰卡德相似度（基于参与活动的重叠度）
     * Jaccard = |A ∩ B| / |A ∪ B|
     */
    private double calculateJaccardSimilarity(Long userId1, Long userId2) {
        Set<Long> acts1 = participantRepository.findByUserId(userId1, PageRequest.of(0, 200)).getContent()
                .stream().map(p -> p.getActivity().getId()).collect(Collectors.toSet());
        Set<Long> acts2 = participantRepository.findByUserId(userId2, PageRequest.of(0, 200)).getContent()
                .stream().map(p -> p.getActivity().getId()).collect(Collectors.toSet());

        if (acts1.isEmpty() && acts2.isEmpty()) {
            // 若均无数数据，基于兴趣标签计算杰卡德相似度作为兜底
            return calculateInterestJaccardSimilarity(userId1, userId2);
        }

        Set<Long> intersection = acts1.stream()
                .filter(acts2::contains)
                .collect(Collectors.toSet());

        Set<Long> union = new HashSet<>(acts1);
        union.addAll(acts2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 基于兴趣标签的杰卡德相似度（备用方案）
     */
    private double calculateInterestJaccardSimilarity(Long userId1, Long userId2) {
        Set<Long> ints1 = userInterestRepository.findByUserId(userId1).stream()
                .map(ui -> ui.getInterest().getId()).collect(Collectors.toSet());
        Set<Long> ints2 = userInterestRepository.findByUserId(userId2).stream()
                .map(ui -> ui.getInterest().getId()).collect(Collectors.toSet());

        Set<Long> intersection = ints1.stream().filter(ints2::contains).collect(Collectors.toSet());
        Set<Long> union = new HashSet<>(ints1);
        union.addAll(ints2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(Map<Long, Double> vector1, Map<Long, Double> vector2) {
        Set<Long> allKeys = new HashSet<>();
        allKeys.addAll(vector1.keySet());
        allKeys.addAll(vector2.keySet());

        if (allKeys.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long key : allKeys) {
            double v1 = vector1.getOrDefault(key, 0.0);
            double v2 = vector2.getOrDefault(key, 0.0);

            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算时间衰减因子
     * 活动越近,权重越大
     */
    private double calculateTimeDecay(LocalDateTime activityTime) {
        long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), activityTime);

        if (daysDifference < 0) {
            return 0.0; // 已过去的活动
        }

        // 指数衰减: e^(-decay_factor * days)
        return Math.exp(-timeDecayFactor * daysDifference);
    }

    /**
     * 获取用户已排除的活动ID(已参与、已浏览等)
     */
    private Set<Long> getExcludedActivityIds(Long userId) {
        Set<Long> excludedIds = new HashSet<>();

        if (userId == null) {
            return excludedIds;
        }

        // 已参与的活动
        List<ActivityParticipant> participations = participantRepository.findByUserId(userId, PageRequest.of(0, 1000))
                .getContent();
        excludedIds.addAll(participations.stream().map(p -> p.getActivity().getId()).collect(Collectors.toSet()));

        // 已点赞的活动
        likeRepository.findByUserId(userId, PageRequest.of(0, 1000))
                .getContent()
                .forEach(like -> excludedIds.add(like.getActivity().getId()));

        return excludedIds;
    }

    /**
     * 获取用户行为数量(参与 + 浏览)
     */
    private int getUserBehaviorCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        int participateCount = (int) participantRepository.findByUserId(userId, PageRequest.of(0, 1000))
                .getTotalElements();

        int likeCount = (int) likeRepository.findByUserId(userId, PageRequest.of(0, 1000))
                .getTotalElements();

        return participateCount + likeCount;
    }

    /**
     * 清除推荐缓存
     */
    public void clearRecommendationCache(Long userId) {
        String pattern = "recommend:user:" + userId + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
    }

    /**
     * 用户行为反馈：动态更新兴趣标签权重
     * @param userId    用户ID
     * @param activityId 活动ID
     * @param actionType 行为类型：participate/like/view
     */
    public void updateInterestFeedback(Long userId, Long activityId, String actionType) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null || activity.getInterests() == null) {
            return;
        }

        double feedbackWeight;
        switch (actionType.toLowerCase()) {
            case "participate":
                feedbackWeight = FEEDBACK_PARTICIPATE;
                break;
            case "like":
                feedbackWeight = FEEDBACK_LIKE;
                break;
            case "view":
                feedbackWeight = FEEDBACK_VIEW;
                break;
            default:
                return;
        }

        for (Interest interest : activity.getInterests()) {
            UserInterest userInterest = userInterestRepository
                    .findByUserIdAndInterestId(userId, interest.getId())
                    .orElse(null);

            if (userInterest != null) {
                // 动态增加权重（参与/点赞/浏览对不同兴趣标签的贡献）
                int baseCount = "participate".equals(actionType.toLowerCase())
                        ? userInterest.getParticipateCount()
                        : userInterest.getClickCount();
                userInterest.setWeight(userInterest.getWeight() + (int) (feedbackWeight * (baseCount + 1)));
                userInterestRepository.save(userInterest);
            } else {
                // 首次接触该兴趣标签，创建记录
                UserInterest newUi = UserInterest.builder()
                        .user(userRepository.findById(userId).orElse(null))
                        .interest(interest)
                        .weight(1)
                        .clickCount(actionType.equalsIgnoreCase("participate") ? 0 : 1)
                        .participateCount(actionType.equalsIgnoreCase("participate") ? 1 : 0)
                        .build();
                userInterestRepository.save(newUi);
            }
        }

        // 清除推荐缓存，使用户下次获取推荐时反映最新偏好
        clearRecommendationCache(userId);
    }

    /**
     * Activity转DTO
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
                .currentParticipants(activity.getCurrentParticipants())
                .status(activity.getStatus())
                .viewCount(activity.getViewCount())
                .likeCount(activity.getLikeCount())
                .commentCount(activity.getCommentCount())
                .fee(activity.getFee())
                .requirements(activity.getRequirements())
                .contactPhone(activity.getContactPhone())
                .createdAt(activity.getCreatedAt());

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
