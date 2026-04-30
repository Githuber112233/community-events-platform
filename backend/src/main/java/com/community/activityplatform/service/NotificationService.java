package com.community.activityplatform.service;

import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.Notification;
import com.community.activityplatform.entity.User;
import com.community.activityplatform.repository.ActivityParticipantRepository;
import com.community.activityplatform.repository.ActivityRepository;
import com.community.activityplatform.repository.NotificationRepository;
import com.community.activityplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;

    /**
     * 获取用户的通知列表
     */
    public Result<Page<Map<String, Object>>> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);

        Page<Map<String, Object>> result = notifications.map(n -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", n.getId());
            map.put("type", n.getType().name());
            map.put("title", n.getTitle());
            map.put("content", n.getContent());
            map.put("isRead", n.getIsRead());
            map.put("createdAt", n.getCreatedAt());
            if (n.getSender() != null) {
                map.put("senderId", n.getSender().getId());
                map.put("senderName", n.getSender().getNickname() != null ? n.getSender().getNickname() : n.getSender().getUsername());
                map.put("senderAvatar", n.getSender().getAvatar());
            }
            if (n.getActivity() != null) {
                map.put("activityId", n.getActivity().getId());
                map.put("activityTitle", n.getActivity().getTitle());
            }
            return map;
        });

        return Result.success(result);
    }

    /**
     * 获取未读通知数量
     */
    public Result<Map<String, Object>> getUnreadCount(Long userId) {
        Long count = notificationRepository.countUnreadByUserId(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unreadCount", count);
        return Result.success(result);
    }

    /**
     * 标记单条通知为已读
     */
    @Transactional
    public Result<Void> markAsRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(notificationId, userId);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public Result<Void> markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
        return Result.success();
    }

    /**
     * 一键通知活动参与者（活动创建者调用）
     */
    @Transactional
    public Result<Map<String, Object>> notifyActivityParticipants(Long senderId, Long activityId, String title, String content) {
        // 验证活动存在
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) {
            return Result.error("活动不存在");
        }

        // 验证发送者是活动创建者
        if (!activity.getCreator().getId().equals(senderId)) {
            User sender = userRepository.findById(senderId).orElse(null);
            if (sender == null || sender.getRole() != User.UserRole.ADMIN) {
                return Result.error("只有活动创建者可以发送通知");
            }
        }

        // 获取已通过的参与者
        List<ActivityParticipant> participants = participantRepository
                .findByActivityIdAndStatus(activityId, ActivityParticipant.ParticipantStatus.APPROVED);

        if (participants.isEmpty()) {
            return Result.error("暂无参与者可通知");
        }

        User sender = userRepository.findById(senderId).orElse(null);

        // 为每个参与者创建通知
        for (ActivityParticipant p : participants) {
            Notification notification = Notification.builder()
                    .user(p.getUser())
                    .sender(sender)
                    .activity(activity)
                    .type(Notification.NotificationType.ACTIVITY_REMINDER)
                    .title(title != null && !title.trim().isEmpty() ? title.trim() : "活动提醒")
                    .content(content != null && !content.trim().isEmpty() ? content.trim() :
                            "活动「" + activity.getTitle() + "」有新消息，请查看详情。")
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notifiedCount", participants.size());
        result.put("message", "已成功通知 " + participants.size() + " 位参与者");
        return Result.success(result);
    }

    /**
     * 取消报名时通知创建者（内部调用）
     */
    @Transactional
    public void notifyCreatorOnCancel(Long participantId, Long activityId) {
        Activity activity = activityRepository.findById(activityId).orElse(null);
        if (activity == null) return;

        User participant = userRepository.findById(participantId).orElse(null);
        if (participant == null) return;

        String participantName = participant.getNickname() != null ? participant.getNickname() : participant.getUsername();

        Notification notification = Notification.builder()
                .user(activity.getCreator())
                .sender(participant)
                .activity(activity)
                .type(Notification.NotificationType.ACTIVITY_CANCEL)
                .title("取消报名通知")
                .content(participantName + " 取消了活动「" + activity.getTitle() + "」的报名。")
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
}
