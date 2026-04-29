package com.community.activityplatform.service;

import com.community.activityplatform.dto.*;
import com.community.activityplatform.entity.*;
import com.community.activityplatform.repository.*;
import com.community.activityplatform.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserInterestRepository userInterestRepository;
    @Mock private InterestRepository interestRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private ActivityParticipantRepository participantRepository;
    @Mock private ActivityLikeRepository likeRepository;
    @Mock private UserFollowRepository followRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User targetUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("testuser").nickname("测试用户")
                .password("encoded_password")
                .role(User.UserRole.USER).status(User.UserStatus.ACTIVE)
                .credits(0)
                .build();

        targetUser = User.builder()
                .id(2L).username("target").nickname("目标用户")
                .role(User.UserRole.USER).status(User.UserStatus.ACTIVE)
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser").password("password123")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser").password("password123")
                .nickname("新用户").email("new@test.com")
                .phone("13800138000")
                .build();
    }

    // ===== 登录 =====

    @Test
    @DisplayName("登录 - 用户不存在")
    void login_userNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        Result<Map<String, Object>> result = userService.login(loginRequest);

        assertEquals(500, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("登录 - 密码错误")
    void login_wrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(false);

        Result<Map<String, Object>> result = userService.login(loginRequest);

        assertEquals(500, result.getCode());
        assertEquals("密码错误", result.getMessage());
    }

    @Test
    @DisplayName("登录 - 账号未激活")
    void login_inactive() {
        testUser.setStatus(User.UserStatus.INACTIVE);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        Result<Map<String, Object>> result = userService.login(loginRequest);

        assertEquals(500, result.getCode());
        assertEquals("账号未激活", result.getMessage());
    }

    @Test
    @DisplayName("登录 - 账号被封禁")
    void login_banned() {
        testUser.setStatus(User.UserStatus.BANNED);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        Result<Map<String, Object>> result = userService.login(loginRequest);

        assertEquals(500, result.getCode());
        assertEquals("账号已被封禁", result.getMessage());
    }

    @Test
    @DisplayName("登录 - 成功返回token和用户信息")
    void login_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(1L, "testuser")).thenReturn("refresh_token");
        when(userInterestRepository.findByUserId(1L)).thenReturn(new ArrayList<>());
        when(followRepository.countFollowingByUserId(1L)).thenReturn(0L);
        when(followRepository.countFollowersByUserId(1L)).thenReturn(0L);

        Result<Map<String, Object>> result = userService.login(loginRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("access_token", result.getData().get("token"));
        assertEquals("refresh_token", result.getData().get("refreshToken"));
        assertNotNull(result.getData().get("user"));
    }

    // ===== 注册 =====

    @Test
    @DisplayName("注册 - 用户名已存在")
    void register_usernameExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        Result<UserDTO> result = userService.register(registerRequest);

        assertEquals(500, result.getCode());
        assertEquals("用户名已存在", result.getMessage());
    }

    @Test
    @DisplayName("注册 - 邮箱已被注册")
    void register_emailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(true);

        Result<UserDTO> result = userService.register(registerRequest);

        assertEquals(500, result.getCode());
        assertEquals("邮箱已被注册", result.getMessage());
    }

    @Test
    @DisplayName("注册 - 手机号已被注册")
    void register_phoneExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByPhone("13800138000")).thenReturn(true);

        Result<UserDTO> result = userService.register(registerRequest);

        assertEquals(500, result.getCode());
        assertEquals("手机号已被注册", result.getMessage());
    }

    @Test
    @DisplayName("注册 - 成功")
    void register_success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByPhone("13800138000")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_new_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(userInterestRepository.findByUserId(anyLong())).thenReturn(new ArrayList<>());
        when(followRepository.countFollowingByUserId(anyLong())).thenReturn(0L);
        when(followRepository.countFollowersByUserId(anyLong())).thenReturn(0L);

        Result<UserDTO> result = userService.register(registerRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(userRepository).save(argThat(user ->
                "encoded_new_password".equals(user.getPassword()) &&
                User.UserStatus.ACTIVE == user.getStatus() &&
                User.UserRole.USER == user.getRole() &&
                0 == user.getCredits()
        ));
    }

    // ===== 获取用户信息 =====

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    void getUserInfo_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Result<UserDTO> result = userService.getUserInfo(999L);

        assertEquals(500, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    void getUserInfo_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userInterestRepository.findByUserId(1L)).thenReturn(new ArrayList<>());
        when(followRepository.countFollowingByUserId(1L)).thenReturn(5L);
        when(followRepository.countFollowersByUserId(1L)).thenReturn(10L);

        Result<UserDTO> result = userService.getUserInfo(1L);

        assertEquals(200, result.getCode());
        assertEquals("testuser", result.getData().getUsername());
        assertEquals(5L, result.getData().getFollowingCount());
        assertEquals(10L, result.getData().getFollowersCount());
    }

    // ===== 更新用户信息 =====

    @Test
    @DisplayName("更新用户信息 - 用户不存在")
    void updateUserInfo_notFound() {
        UserDTO dto = UserDTO.builder().nickname("新昵称").build();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Result<UserDTO> result = userService.updateUserInfo(999L, dto);

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void updateUserInfo_success() {
        UserDTO dto = UserDTO.builder()
                .nickname("新昵称").email("new@test.com").phone("13800138000")
                .gender("男").bio("新简介").province("广东").city("深圳").district("南山")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userInterestRepository.findByUserId(1L)).thenReturn(new ArrayList<>());
        when(followRepository.countFollowingByUserId(1L)).thenReturn(0L);
        when(followRepository.countFollowersByUserId(1L)).thenReturn(0L);

        Result<UserDTO> result = userService.updateUserInfo(1L, dto);

        assertEquals(200, result.getCode());
        verify(userRepository).save(argThat(user ->
                "新昵称".equals(user.getNickname()) &&
                "男".equals(user.getGender()) &&
                "新简介".equals(user.getBio())
        ));
    }

    // ===== 兴趣标签 =====

    @Test
    @DisplayName("更新兴趣标签 - 用户不存在")
    void updateUserInterests_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Void> result = userService.updateUserInterests(999L, List.of(1L, 2L));

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("更新兴趣标签 - 成功删除旧的并添加新的")
    void updateUserInterests_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(interestRepository.findById(1L)).thenReturn(Optional.of(
                Interest.builder().id(1L).name("运动").build()));
        when(interestRepository.findById(2L)).thenReturn(Optional.of(
                Interest.builder().id(2L).name("音乐").build()));

        Result<Void> result = userService.updateUserInterests(1L, List.of(1L, 2L));

        assertEquals(200, result.getCode());
        verify(userInterestRepository).deleteByUserId(1L);
        verify(userInterestRepository).saveAll(argThat(list ->
                ((List<UserInterest>) list).size() == 2
        ));
    }

    @Test
    @DisplayName("获取用户兴趣标签 - 成功")
    void getUserInterests_success() {
        Interest interest = Interest.builder().id(1L).name("运动")
                .category("运动健身").popularity(100).active(true).build();
        UserInterest userInterest = UserInterest.builder()
                .user(testUser).interest(interest).weight(2).clickCount(10).participateCount(5)
                .build();
        when(userInterestRepository.findByUserId(1L)).thenReturn(List.of(userInterest));

        Result<List<InterestDTO>> result = userService.getUserInterests(1L);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("运动", result.getData().get(0).getName());
        assertEquals(2, result.getData().get(0).getWeight());
    }

    // ===== 关注/取关 =====

    @Test
    @DisplayName("关注用户 - 不能关注自己")
    void followUser_self() {
        Result<Void> result = userService.followUser(1L, 1L);

        assertEquals(500, result.getCode());
        assertEquals("不能关注自己", result.getMessage());
    }

    @Test
    @DisplayName("关注用户 - 用户不存在")
    void followUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Result<Void> result = userService.followUser(1L, 999L);

        assertEquals(500, result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("关注用户 - 已经关注过")
    void followUser_alreadyFollowed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.existsByFollowerIdAndFollowedId(1L, 2L)).thenReturn(true);

        Result<Void> result = userService.followUser(1L, 2L);

        assertEquals(500, result.getCode());
        assertEquals("已经关注过该用户", result.getMessage());
    }

    @Test
    @DisplayName("关注用户 - 成功")
    void followUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.existsByFollowerIdAndFollowedId(1L, 2L)).thenReturn(false);

        Result<Void> result = userService.followUser(1L, 2L);

        assertEquals(200, result.getCode());
        verify(followRepository).save(any(UserFollow.class));
    }

    @Test
    @DisplayName("取消关注 - 成功")
    void unfollowUser_success() {
        UserFollow follow = UserFollow.builder().id(1L).build();
        when(followRepository.findByFollowerIdAndFollowedId(1L, 2L))
                .thenReturn(Optional.of(follow));

        Result<Void> result = userService.unfollowUser(1L, 2L);

        assertEquals(200, result.getCode());
        verify(followRepository).delete(follow);
    }

    @Test
    @DisplayName("取消关注 - 未关注过（静默成功）")
    void unfollowUser_notFollowed() {
        when(followRepository.findByFollowerIdAndFollowedId(1L, 2L))
                .thenReturn(Optional.empty());

        Result<Void> result = userService.unfollowUser(1L, 2L);

        assertEquals(200, result.getCode());
    }

    // ===== 粉丝/关注列表 =====

    @Test
    @DisplayName("获取粉丝列表 - 用户不存在")
    void getFollowers_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Result<List<UserDTO>> result = userService.getFollowers(999L);

        assertEquals(500, result.getCode());
    }

    @Test
    @DisplayName("获取粉丝列表 - 成功")
    void getFollowers_success() {
        User follower = User.builder().id(3L).username("follower")
                .role(User.UserRole.USER).status(User.UserStatus.ACTIVE).build();
        UserFollow follow = UserFollow.builder()
                .follower(follower).followed(testUser).build();
        Page<UserFollow> page = new PageImpl<>(List.of(follow));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(followRepository.findFollowersByUserId(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Result<List<UserDTO>> result = userService.getFollowers(1L);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    // ===== 用户活动列表 =====

    @Test
    @DisplayName("获取用户活动列表 - 创建的活动")
    void getUserActivities_created() {
        Page<Activity> page = new PageImpl<>(List.of(
                Activity.builder().id(1L).title("活动1").creator(testUser).build()
        ));
        when(activityRepository.findByCreatorId(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(participantRepository.countByActivityIdAndStatus(anyLong(), any())).thenReturn(0L);

        Result<List<ActivityDTO>> result = userService.getUserActivities(
                1L, "created", Pageable.unpaged());

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("获取用户活动列表 - 不支持的类型")
    void getUserActivities_unsupportedType() {
        Result<List<ActivityDTO>> result = userService.getUserActivities(
                1L, "invalid_type", Pageable.unpaged());

        assertEquals(500, result.getCode());
        assertEquals("不支持的活动类型", result.getMessage());
    }
}
