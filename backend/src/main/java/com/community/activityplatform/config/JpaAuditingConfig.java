package com.community.activityplatform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA审计配置
 * 自动填充createdAt和updatedAt字段
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
