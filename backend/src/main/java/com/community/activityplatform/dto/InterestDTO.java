package com.community.activityplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 兴趣标签数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Integer popularity;
    private Boolean active;
    // 用户与该兴趣标签的关联数据（仅在用户兴趣接口中返回）
    private Integer weight;
    private Integer clickCount;
    private Integer participateCount;
}

