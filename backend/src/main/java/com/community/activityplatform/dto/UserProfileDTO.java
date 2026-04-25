package com.community.activityplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户画像数据传输对象（管理员专用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    // ==================== 基本信息 ====================
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String gender;
    private String province;
    private String city;
    private String district;
    private String avatar;
    private String bio;
    private String status;
    private String role;
    private Integer credits;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;

    // ==================== 统计数据 ====================
    private Statistics statistics;

    // ==================== 兴趣标签 ====================
    private List<InterestDTO> interests;

    // ==================== 最近创建的活动 ====================
    private List<ActivityDTO> recentActivities;

    // ==================== 统计数据内部类 ====================
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private long createdActivities;      // 创建的活动数
        private long participatingActivities; // 参与的活动数
        private long likedActivities;         // 点赞的活动数
        private long commentedActivities;     // 评论的活动数
        private long totalComments;           // 总评论数
        private long totalLikes;              // 总点赞数（收到的）
        private long followers;              // 粉丝数
        private long following;              // 关注数
    }
}