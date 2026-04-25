package com.community.activityplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户兴趣标签实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_interests", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "interest_id"})
})
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer clickCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer participateCount = 0;
}
