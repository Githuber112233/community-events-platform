package com.community.activityplatform.dto;

import com.community.activityplatform.entity.Activity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String coverImage;
    private UserDTO creator;
    private List<InterestDTO> interests;
    private String province;
    private String city;
    private String district;
    private String address;
    private String latitude;
    private String longitude;

    /**
     * 处理前端传入的 number 类型坐标，转换为 String 存储
     */
    public void setLatitude(Object lat) {
        if (lat == null) {
            this.latitude = null;
        } else if (lat instanceof Number) {
            this.latitude = lat.toString();
        } else {
            this.latitude = lat.toString();
        }
    }

    public void setLongitude(Object lng) {
        if (lng == null) {
            this.longitude = null;
        } else if (lng instanceof Number) {
            this.longitude = lng.toString();
        } else {
            this.longitude = lng.toString();
        }
    }
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime registrationDeadline;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Activity.ActivityStatus status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String fee;
    private String requirements;
    private String contactPhone;
    private LocalDateTime createdAt;
    private Boolean isLiked;
    private Boolean isParticipated;
}
