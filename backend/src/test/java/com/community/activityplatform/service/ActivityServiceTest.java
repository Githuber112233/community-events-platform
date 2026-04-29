package com.community.activityplatform.service;

import com.community.activityplatform.dto.ActivityDTO;
import com.community.activityplatform.dto.InterestDTO;
import com.community.activityplatform.dto.Result;
import com.community.activityplatform.entity.*;
import com.community.activityplatform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityService 单元测试")
class ActivityServiceTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private ActivityParticipantRepository participantRepository;
    @Mock private ActivityLikeRepository likeRepository;
    @Mock private ActivityCommentRepository commentRepository;
    @Mock private ActivityViewRepository viewRepository;
    @Mock private UserRepository userRepository;
    @Mock private InterestRepository interestRepository;

    @InjectMocks
    private ActivityService activityService;

    private User testUser;
    private User adminUser;
    private Activity testActivity;
    private ActivityDTO testActivityDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").nickname("测试用户")
                .role(User.UserRole.USER).status(User.UserStatus.ACTIVE)
                .build();

        adminUser = User.builder()
                .id(2L).username("admin").nickname("管理员")
                .role(User.UserRole.ADMIN).status(User.UserStatus.ACTIVE)
                .build();

        testActivity = Activity.builder()
                .id(1L).title("测试活动").description("测试描述")
                .province("广东省").city("深圳市").district("南山区")
                .address("科技园")
                .latitude("22.5").longitude("113.9")
                .startTime(LocalDateTime.now().plusDays(7))
                .endTime(LocalDateTime.now().plusDays(8))
                .registrationDeadline(LocalDateTime.now().plusDays(5))
                .maxParticipants(50).currentParticipants(10)
                .status(Activity.ActivityStatus.RECRUITING)
                .viewCount(100).likeCount(20).commentCount(5)
                .creator(testUser)
                .build();

        InterestDTO interestDTO = InterestDTO.builder().id(1L).name("运动").category("运动健身").build();
        testActivityDTO = ActivityDTO.builder()
                .id(1L).title("测试活动").description("测试描述")
                .province("广东省").city("深圳市").district("南山区")
                .maxParticipants(50).currentParticipants(10)
                .status(Activity.ActivityStatus.RECRUITING)
                .viewCount(100).likeCount(20).commentCount(5)
                .interests(List.of(interestDTO))
                .build();
    }

    // ===== 创建活动 =====

    @Test
    @DisplayName("普通用户创建活动 - 状态应为PENDING")
    void createActivity_normalUser_statusPending() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityDTO> result = activityService.createActivity(1L, testActivityDTO);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(Activity.ActivityStatus.PENDING, result.getData().getStatus());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    @DisplayName("管理员创建活动 - 状态应为RECRUITING")
    void createActivity_admin_statusRecruiting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityDTO> result = activityService.createActivity(2L, testActivityDTO);

        assertEquals(200, result.getCode());
        assertEquals(Activity.ActivityStatus.RECRUITING, result.getData().getStatus());
    }

    @Test
    @DisplayName("创建活动 - 带兴趣标签")
    void createActivity_withInterests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interestRepository.findAllById(anyList())).thenReturn(List.of(
                Interest.builder().id(1L).name("运动").build()
        ));
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityDTO> result = activityService.createActivity(1L, testActivityDTO);

        assertEquals(200, result.getCode());
        verify(interestRepository).findAllById(anyList());
    }

    @Test
    @DisplayName("创建活动 - 未设置maxParticipants时默认100")
    void createActivity_defaultMaxParticipants() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            assertEquals(100, a.getMaxParticipants());
            return a;
        });

        testActivityDTO.setMaxParticipants(null);
        activityService.createActivity(1L, testActivityDTO);
    }

    // ===== 获取活动详情 =====

    @Test
    @DisplayName("获取活动详情 - 活动不存在")
    void getActivityDetail_notFound() {
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        Result<ActivityDTO> result = activityService.getActivityDetail(999L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("活动不存在", result.getMessage());
    }

    @Test
    @DisplayName("获取活动详情 - 正常获取并增加浏览量")
    void getActivityDetail_success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(likeRepository.existsByUser_IdAndActivity_Id(1L, 1L)).thenReturn(false);
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L)).thenReturn(Optional.empty());
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(viewRepository.save(any(ActivityView.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityDTO> result = activityService.getActivityDetail(1L, 1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("测试活动", result.getData().getTitle());
        assertFalse(result.getData().getIsLiked());
        assertFalse(result.getData().getIsParticipated());
        verify(viewRepository).save(any(ActivityView.class));
    }

    @Test
    @DisplayName("获取活动详情 - 未登录用户不检查点赞/参与状态")
    void getActivityDetail_notLoggedIn() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(inv -> inv.getArgument(0));

        Result<ActivityDTO> result = activityService.getActivityDetail(1L, null);

        assertEquals(200, result.getCode());
        verify(likeRepository, never()).existsByUser_IdAndActivity_Id(anyLong(), anyLong());
        verify(viewRepository, never()).save(any());
    }

    // ===== 参与活动 =====

    @Test
    @DisplayName("参与活动 - 活动不存在")
    void participateActivity_notFound() {
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Void> result = activityService.participateActivity(1L, 999L, "我想参加");

        assertEquals(500, result.getCode());
        assertEquals("活动不存在", result.getMessage());
    }

    @Test
    @DisplayName("参与活动 - 已满员")
    void participateActivity_full() {
        testActivity.setCurrentParticipants(50);
        testActivity.setMaxParticipants(50);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        Result<Void> result = activityService.participateActivity(1L, 1L, "我想参加");

        assertEquals(500, result.getCode());
        assertEquals("活动已满员", result.getMessage());
    }

    @Test
    @DisplayName("参与活动 - 报名已截止")
    void participateActivity_deadlinePassed() {
        testActivity.setRegistrationDeadline(LocalDateTime.now().minusDays(1));
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        Result<Void> result = activityService.participateActivity(1L, 1L, "我想参加");

        assertEquals(500, result.getCode());
        assertEquals("报名已截止", result.getMessage());
    }

    @Test
    @DisplayName("参与活动 - 已经参与过")
    void participateActivity_alreadyParticipated() {
        ActivityParticipant participant = ActivityParticipant.builder()
                .id(1L).status(ActivityParticipant.ParticipantStatus.APPROVED)
                .build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.of(participant));

        Result<Void> result = activityService.participateActivity(1L, 1L, "我想参加");

        assertEquals(500, result.getCode());
        assertEquals("已经参与过该活动", result.getMessage());
    }

    @Test
    @DisplayName("参与活动 - 报名正在审核中")
    void participateActivity_pending() {
        ActivityParticipant participant = ActivityParticipant.builder()
                .id(1L).status(ActivityParticipant.ParticipantStatus.PENDING)
                .build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.of(participant));

        Result<Void> result = activityService.participateActivity(1L, 1L, "我想参加");

        assertEquals(500, result.getCode());
        assertEquals("报名正在审核中，请等待", result.getMessage());
    }

    @Test
    @DisplayName("参与活动 - 首次报名成功")
    void participateActivity_success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Void> result = activityService.participateActivity(1L, 1L, "我想参加");

        assertEquals(200, result.getCode());
        verify(participantRepository).save(argThat(p ->
                p.getStatus() == ActivityParticipant.ParticipantStatus.APPROVED &&
                "我想参加".equals(p.getMessage())
        ));
    }

    @Test
    @DisplayName("参与活动 - 取消后重新报名成功")
    void participateActivity_reregisterAfterCancel() {
        ActivityParticipant cancelledParticipant = ActivityParticipant.builder()
                .id(1L).status(ActivityParticipant.ParticipantStatus.CANCELLED)
                .message("旧消息")
                .build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.of(cancelledParticipant));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Void> result = activityService.participateActivity(1L, 1L, "新报名消息");

        assertEquals(200, result.getCode());
        verify(participantRepository).save(argThat(p ->
                p.getStatus() == ActivityParticipant.ParticipantStatus.APPROVED &&
                "新报名消息".equals(p.getMessage())
        ));
    }

    // ===== 取消参与 =====

    @Test
    @DisplayName("取消参与 - 未参与过")
    void cancelParticipation_notParticipated() {
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.empty());

        Result<Void> result = activityService.cancelParticipation(1L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("未参与该活动", result.getMessage());
    }

    @Test
    @DisplayName("取消参与 - APPROVED状态取消成功，参与人数减1")
    void cancelParticipation_approvedSuccess() {
        ActivityParticipant participant = ActivityParticipant.builder()
                .id(1L).status(ActivityParticipant.ParticipantStatus.APPROVED).build();
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.of(participant));
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Void> result = activityService.cancelParticipation(1L, 1L);

        assertEquals(200, result.getCode());
        verify(participantRepository).save(argThat(p ->
                p.getStatus() == ActivityParticipant.ParticipantStatus.CANCELLED
        ));
    }

    @Test
    @DisplayName("取消参与 - CANCELLED状态无法再取消")
    void cancelParticipation_alreadyCancelled() {
        ActivityParticipant participant = ActivityParticipant.builder()
                .id(1L).status(ActivityParticipant.ParticipantStatus.CANCELLED).build();
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.of(participant));

        Result<Void> result = activityService.cancelParticipation(1L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("无法取消该参与记录", result.getMessage());
    }

    // ===== 点赞/取消点赞 =====

    @Test
    @DisplayName("点赞活动 - 已经点赞过")
    void likeActivity_alreadyLiked() {
        when(likeRepository.existsByUser_IdAndActivity_Id(1L, 1L)).thenReturn(true);

        Result<Void> result = activityService.likeActivity(1L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("已经点赞过该活动", result.getMessage());
    }

    @Test
    @DisplayName("点赞活动 - 活动不存在")
    void likeActivity_notFound() {
        when(likeRepository.existsByUser_IdAndActivity_Id(1L, 999L)).thenReturn(false);
        when(activityRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Void> result = activityService.likeActivity(1L, 999L);

        assertEquals(500, result.getCode());
        assertEquals("活动不存在", result.getMessage());
    }

    @Test
    @DisplayName("点赞活动 - 成功")
    void likeActivity_success() {
        when(likeRepository.existsByUser_IdAndActivity_Id(1L, 1L)).thenReturn(false);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(likeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Void> result = activityService.likeActivity(1L, 1L);

        assertEquals(200, result.getCode());
        verify(likeRepository).save(any(ActivityLike.class));
    }

    @Test
    @DisplayName("取消点赞 - 成功")
    void unlikeActivity_success() {
        ActivityLike like = ActivityLike.builder().id(1L).build();
        when(likeRepository.findByUser_IdAndActivity_Id(1L, 1L)).thenReturn(Optional.of(like));
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Void> result = activityService.unlikeActivity(1L, 1L);

        assertEquals(200, result.getCode());
        verify(likeRepository).delete(like);
    }

    // ===== 签到 =====

    @Test
    @DisplayName("签到 - 只有创建者可以操作")
    void checkInParticipant_notCreator() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));

        Result<Map<String, Object>> result = activityService.checkInParticipant(2L, 1L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("只有活动创建者可以执行签到操作", result.getMessage());
    }

    @Test
    @DisplayName("签到 - 用户未报名")
    void checkInParticipant_notRegistered() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.findByUser_IdAndActivity_Id(3L, 1L))
                .thenReturn(Optional.empty());

        Result<Map<String, Object>> result = activityService.checkInParticipant(1L, 1L, 3L);

        assertEquals(500, result.getCode());
        assertEquals("该用户未报名此活动", result.getMessage());
    }

    @Test
    @DisplayName("签到 - 已签到过")
    void checkInParticipant_alreadyCheckedIn() {
        ActivityParticipant participant = ActivityParticipant.builder()
                .id(1L).status(ActivityParticipant.ParticipantStatus.CHECKED_IN).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.of(participant));

        Result<Map<String, Object>> result = activityService.checkInParticipant(1L, 1L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("该用户已签到", result.getMessage());
    }

    @Test
    @DisplayName("签到 - 成功")
    void checkInParticipant_success() {
        ActivityParticipant participant = ActivityParticipant.builder()
                .id(1L).status(ActivityParticipant.ParticipantStatus.APPROVED).build();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(participantRepository.findByUser_IdAndActivity_Id(1L, 1L))
                .thenReturn(Optional.of(participant));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Result<Map<String, Object>> result = activityService.checkInParticipant(1L, 1L, 1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().get("checkedAt"));
        verify(participantRepository).save(argThat(p ->
                p.getStatus() == ActivityParticipant.ParticipantStatus.CHECKED_IN &&
                p.getCheckedAt() != null
        ));
    }

    // ===== 获取参与者详情 =====

    @Test
    @DisplayName("获取参与者详情 - 非创建者/管理员无权查看")
    void getAllParticipantsWithDetails_notAuthorized() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(userRepository.findById(3L)).thenReturn(Optional.of(testUser)); // testUser is USER role

        Result<List<Map<String, Object>>> result =
                activityService.getAllParticipantsWithDetails(3L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("只有活动创建者或管理员可以查看报名信息", result.getMessage());
    }

    @Test
    @DisplayName("获取参与者详情 - 管理员可以查看")
    void getAllParticipantsWithDetails_adminSuccess() {
        User anotherUser = User.builder().id(3L).username("other").nickname("其他")
                .phone("13800138000").email("other@test.com").build();
        ActivityParticipant participant = ActivityParticipant.builder()
                .id(1L).user(anotherUser).status(ActivityParticipant.ParticipantStatus.APPROVED)
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(participantRepository.findByActivityIdAndStatusNot(eq(1L), any()))
                .thenReturn(List.of(participant));

        Result<List<Map<String, Object>>> result =
                activityService.getAllParticipantsWithDetails(2L, 1L);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("13800138000", result.getData().get(0).get("phone"));
    }

    // ===== 活动列表 =====

    @Test
    @DisplayName("获取活动列表 - 无筛选条件")
    void getActivityList_noFilters() {
        Page<Activity> page = new PageImpl<>(List.of(testActivity));
        when(activityRepository.findAllActivities(any(Pageable.class))).thenReturn(page);
        when(participantRepository.countByActivityIdAndStatus(anyLong(), any()))
                .thenReturn(10L);

        Result<Page<ActivityDTO>> result = activityService.getActivityList(
                null, null, null, PageRequest.of(0, 10), null);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getTotalElements());
    }
}
