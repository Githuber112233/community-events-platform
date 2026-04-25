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

    @Value("${recommendation.cf-enable-threshold:5}")
    private Integer cfEnableThreshold;

    @Value("${recommendation.top-k-users:20}")
    private Integer topKUsers;

    @Value("${recommendation.top-n-activities:10}")
    private Integer topNActivities;

    @Value("${recommendation.time-decay-factor:0.01}")
    private Double timeDecayFactor;

    @Value("${recommendation.cache-ttl:1800}")
    private Long cacheTtl;

    /**
     * 为用户推荐活动
     */
    public Result<Page<ActivityDTO>> recommendActivities(Long userId, Integer page, Integer size) {
        String cacheKey = "recommend:user:" + userId + ":page:" + page;

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ActivityDTO> cachedResult = (List<ActivityDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            Pageable pageable = PageRequest.of(page, size);
            return Result.success(new PageImpl<>(cachedResult, pageable, cachedResult.size()));
        }

        // 获取用户已参与和已浏览的活动
        Set<Long> excludedActivityIds = getExcludedActivityIds(userId);

        // 获取用户行为数据,决定使用何种推荐策略
        int userBehaviorCount = getUserBehaviorCount(userId);

        List<Activity> recommendedActivities;

        if (userBehaviorCount < cfEnableThreshold) {
            // 行为数据少,使用基于内容的推荐
            log.info("用户 {} 行为数据不足,使用基于内容的推荐", userId);
            recommendedActivities = contentBasedRecommendation(userId, excludedActivityIds);
        } else {
            // 行为数据充足,使用混合推荐
            log.info("用户 {} 行为数据充足,使用混合推荐", userId);
            recommendedActivities = hybridRecommendation(userId, excludedActivityIds);
        }

        // 转换为DTO
        List<ActivityDTO> activityDTOs = recommendedActivities.stream()
                .limit(topNActivities)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, activityDTOs, cacheTtl, TimeUnit.SECONDS);

        // 分页
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + size, activityDTOs.size());
        List<ActivityDTO> pageContent = activityDTOs.subList(start, end);
        Page<ActivityDTO> resultPage = new PageImpl<>(pageContent, pageable, activityDTOs.size());

        return Result.success(resultPage);
    }

    /**
     * 基于内容的推荐(Content-Based)
     * 根据用户兴趣标签推荐相似活动
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
            // 如果热门招募活动为空，返回所有正在招募的活动
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
            // 权重 = (点击数 * 0.3 + 参与数 * 0.7) * 标签权重
            double weight = (ui.getClickCount() * 0.3 + ui.getParticipateCount() * 0.7) * ui.getWeight();
            userInterestVector.put(ui.getInterest().getId(), weight);
        }

        // 获取所有正在招募的活动
        List<Activity> allActivities = activityRepository.findByStatusAndStartTimeAfter(
                Activity.ActivityStatus.RECRUITING, LocalDateTime.now(), PageRequest.of(0, 1000))
                .getContent();

        // 计算每个活动与用户的相似度
        Map<Activity, Double> activityScores = new HashMap<>();
        for (Activity activity : allActivities) {
            if (excludedActivityIds.contains(activity.getId())) {
                continue;
            }

            // 构建活动兴趣向量
            Map<Long, Double> activityInterestVector = new HashMap<>();
            for (Interest interest : activity.getInterests()) {
                activityInterestVector.put(interest.getId(), 1.0);
            }

            // 计算余弦相似度
            double similarity = calculateCosineSimilarity(userInterestVector, activityInterestVector);

            // 考虑时间衰减因子
            double timeDecay = calculateTimeDecay(activity.getStartTime());

            // 综合评分 = 相似度 * 时间衰减
            double finalScore = similarity * timeDecay;
            activityScores.put(activity, finalScore);
        }

        // 按评分排序返回
        return activityScores.entrySet().stream()
                .sorted(Map.Entry.<Activity, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 混合推荐(Content-Based + User-CF)
     */
    private List<Activity> hybridRecommendation(Long userId, Set<Long> excludedActivityIds) {
        // 1. 基于内容的推荐分数
        Map<Activity, Double> contentScores = new HashMap<>();
        List<Activity> contentActivities = contentBasedRecommendation(userId, excludedActivityIds);
        for (int i = 0; i < contentActivities.size(); i++) {
            // 归一化分数 (排名越高分数越高)
            double score = 1.0 - (double) i / contentActivities.size();
            contentScores.put(contentActivities.get(i), score);
        }

        // 2. 基于用户的协同过滤推荐分数
        Map<Activity, Double> cfScores = userCollaborativeFiltering(userId, excludedActivityIds);

        // 3. 动态融合两种推荐算法
        // 根据用户行为数据量动态调整权重
        int userBehaviorCount = getUserBehaviorCount(userId);
        double cfWeight = Math.min(1.0, (double) userBehaviorCount / (cfEnableThreshold * 2));
        double contentWeight = 1.0 - cfWeight;

        Map<Activity, Double> finalScores = new HashMap<>();

        // 融合Content-Based结果
        for (Map.Entry<Activity, Double> entry : contentScores.entrySet()) {
            finalScores.put(entry.getKey(), entry.getValue() * contentWeight);
        }

        // 融合User-CF结果
        for (Map.Entry<Activity, Double> entry : cfScores.entrySet()) {
            Activity activity = entry.getKey();
            double score = entry.getValue() * cfWeight;
            finalScores.put(activity, finalScores.getOrDefault(activity, 0.0) + score);
        }

        // 按最终分数排序
        return finalScores.entrySet().stream()
                .sorted(Map.Entry.<Activity, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 基于用户的协同过滤(User-CF)
     * 找到相似用户,推荐他们参与的活动
     */
    private Map<Activity, Double> userCollaborativeFiltering(Long userId, Set<Long> excludedActivityIds) {
        // 获取目标用户参与的活动
        List<ActivityParticipant> userParticipations = participantRepository.findByUserId(userId, PageRequest.of(0, 100))
                .getContent();
        Set<Long> userActivityIds = userParticipations.stream()
                .map(p -> p.getActivity().getId())
                .collect(Collectors.toSet());

        // 获取所有用户
        List<com.community.activityplatform.entity.User> allUsers = userRepository.findAll();

        // 计算用户相似度
        Map<Long, Double> userSimilarities = new HashMap<>();
        for (com.community.activityplatform.entity.User otherUser : allUsers) {
            if (otherUser.getId().equals(userId)) {
                continue;
            }

            double similarity = calculateUserSimilarity(userId, otherUser.getId());
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

        // 获取相似用户参与的活动
        Map<Activity, Double> recommendedScores = new HashMap<>();
        for (Long similarUserId : similarUserIds) {
            List<ActivityParticipant> similarUserParticipations = participantRepository
                    .findByUserId(similarUserId, PageRequest.of(0, 100))
                    .getContent();

            for (ActivityParticipant participation : similarUserParticipations) {
                Activity activity = participation.getActivity();

                // 排除已参与的活动
                if (userActivityIds.contains(activity.getId()) || excludedActivityIds.contains(activity.getId())) {
                    continue;
                }

                // 排除已结束的活动
                if (activity.getStartTime().isBefore(LocalDateTime.now())) {
                    continue;
                }

                // 评分 = 用户相似度 * 参与状态权重
                double statusWeight = 1.0;
                if (participation.getStatus() == ParticipantStatus.APPROVED) {
                    statusWeight = 1.0;
                } else if (participation.getStatus() == ParticipantStatus.CHECKED_IN) {
                    statusWeight = 1.2;
                }

                double score = userSimilarities.get(similarUserId) * statusWeight;
                recommendedScores.merge(activity, score, Double::sum);
            }
        }

        return recommendedScores;
    }

    /**
     * 计算用户相似度(基于参与活动的兴趣标签重叠度)
     */
    private double calculateUserSimilarity(Long userId1, Long userId2) {
        List<UserInterest> interests1 = userInterestRepository.findByUserId(userId1);
        List<UserInterest> interests2 = userInterestRepository.findByUserId(userId2);

        if (interests1.isEmpty() || interests2.isEmpty()) {
            return 0.0;
        }

        // 构建兴趣向量
        Map<Long, Double> vector1 = new HashMap<>();
        Map<Long, Double> vector2 = new HashMap<>();

        for (UserInterest ui : interests1) {
            vector1.put(ui.getInterest().getId(), (double) ui.getParticipateCount());
        }

        for (UserInterest ui : interests2) {
            vector2.put(ui.getInterest().getId(), (double) ui.getParticipateCount());
        }

        return calculateCosineSimilarity(vector1, vector2);
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
