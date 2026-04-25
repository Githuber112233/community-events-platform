package com.community.activityplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 社区活动平台主应用类
 */
@SpringBootApplication
public class CommunityActivityPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityActivityPlatformApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("社区活动平台后端服务启动成功!");
        System.out.println("API地址: http://localhost:8080/api");
        System.out.println("========================================\n");
    }
}

