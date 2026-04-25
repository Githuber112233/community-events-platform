package com.community.activityplatform.dto;

import com.community.activityplatform.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
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
    private Integer credits;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private List<InterestDTO> interests;
    private Long followingCount;
    private Long followersCount;
}
