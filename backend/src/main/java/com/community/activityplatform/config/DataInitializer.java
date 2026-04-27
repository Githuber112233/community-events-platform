package com.community.activityplatform.config;

import com.community.activityplatform.entity.Activity;
import com.community.activityplatform.entity.ActivityParticipant;
import com.community.activityplatform.entity.Interest;
import com.community.activityplatform.entity.User;
import com.community.activityplatform.entity.UserInterest;
import com.community.activityplatform.repository.ActivityParticipantRepository;
import com.community.activityplatform.repository.ActivityRepository;
import com.community.activityplatform.repository.InterestRepository;
import com.community.activityplatform.repository.UserInterestRepository;
import com.community.activityplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试数据初始化器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 修复已有参与者记录：将 PENDING 状态改为 APPROVED（因为系统设计为报名即通过）
        fixPendingParticipants();

        // 检查是否已有数据
        if (userRepository.count() > 0) {
            log.info("数据库已有数据，跳过初始化");
            return;
        }

        log.info("开始初始化测试数据...");

        // 初始化兴趣标签（6大分类）
        Interest sports = createInterestIfNotExists("体育健身", "体育", "⚽");
        Interest culture = createInterestIfNotExists("文化艺术", "文化", "🎨");
        Interest tech = createInterestIfNotExists("科技学习", "科技", "💻");
        Interest social = createInterestIfNotExists("社交聚会", "社交", "🎉");
        Interest outdoor = createInterestIfNotExists("户外探险", "户外", "🏔️");
        Interest volunteer = createInterestIfNotExists("志愿服务", "志愿", "🤝");

        // 创建用户
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nickname("平台管理员")
                .email("admin@example.com")
                .avatar("https://api.dicebear.com/7.x/pixel-art/svg?seed=admin")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.ADMIN)
                .credits(1000)
                .build();
        admin = userRepository.save(admin);

        User user1 = User.builder()
                .username("zhangsan")
                .password(passwordEncoder.encode("123456"))
                .nickname("张三")
                .email("zhangsan@example.com")
                .avatar("https://api.dicebear.com/7.x/pixel-art/svg?seed=zhangsan")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .credits(100)
                .build();
        user1 = userRepository.save(user1);

        User user2 = User.builder()
                .username("lisi")
                .password(passwordEncoder.encode("123456"))
                .nickname("李四")
                .email("lisi@example.com")
                .avatar("https://api.dicebear.com/7.x/pixel-art/svg?seed=lisi")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .credits(200)
                .build();
        user2 = userRepository.save(user2);

        User user3 = User.builder()
                .username("wangwu")
                .password(passwordEncoder.encode("123456"))
                .nickname("王五")
                .email("wangwu@example.com")
                .avatar("https://api.dicebear.com/7.x/pixel-art/svg?seed=wangwu")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .credits(150)
                .build();
        user3 = userRepository.save(user3);

        User user4 = User.builder()
                .username("zhaoliu")
                .password(passwordEncoder.encode("123456"))
                .nickname("赵六")
                .email("zhaoliu@example.com")
                .avatar("https://api.dicebear.com/7.x/pixel-art/svg?seed=zhaoliu")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .credits(180)
                .build();
        user4 = userRepository.save(user4);

        User user5 = User.builder()
                .username("sunqi")
                .password(passwordEncoder.encode("123456"))
                .nickname("孙七")
                .email("sunqi@example.com")
                .avatar("https://api.dicebear.com/7.x/pixel-art/svg?seed=sunqi")
                .status(User.UserStatus.ACTIVE)
                .role(User.UserRole.USER)
                .credits(220)
                .build();
        user5 = userRepository.save(user5);

        // 基准时间：2026-04-24（当前日期）
        LocalDateTime now = LocalDateTime.of(2026, 4, 24, 14, 0);

        // ============================================================
        // 活动1-5: 体育健身类 RECRUITING
        // ============================================================
        Activity a1 = buildActivity("周末篮球友谊赛", "体育健身",
                "每周一次的社区篮球友谊赛，新老球友均可参加，提供饮水和基础装备。",
                "https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800",
                "北京市", "北京市", "朝阳区", "朝阳公园篮球场", "39.9288", "116.4469",
                now.plusDays(2).withHour(14).withMinute(0), now.plusDays(2).withHour(17).withMinute(0),
                now.plusDays(1).withHour(22).withMinute(0),
                20, 6, Activity.ActivityStatus.RECRUITING, "0", "身体健康，适合运动", "13800138001",
                user1, sports, 156, 28, 9);
        a1 = activityRepository.save(a1);

        Activity a2 = buildActivity("青少年羽毛球公开课", "体育健身",
                "专业羽毛球教练亲自指导，报名即送羽毛球拍一只，适合8-16岁青少年。",
                "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=800",
                "北京市", "北京市", "海淀区", "北京大学体育馆", "39.9867", "116.3073",
                now.plusDays(5).withHour(9).withMinute(0), now.plusDays(5).withHour(11).withMinute(0),
                now.plusDays(4).withHour(20).withMinute(0),
                30, 12, Activity.ActivityStatus.RECRUITING, "29.9", "穿运动服，自带羽毛球拍", "13800138002",
                user2, sports, 234, 45, 18);
        a2 = activityRepository.save(a2);

        Activity a3 = buildActivity("夜跑团-奥森5公里", "体育健身",
                "每晚7点相约奥林匹克森林公园，一起夜跑锻炼，氛围轻松愉快。",
                "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=800",
                "北京市", "北京市", "朝阳区", "奥林匹克森林公园南园", "40.0121", "116.4043",
                now.plusDays(1).withHour(19).withMinute(0), now.plusDays(1).withHour(21).withMinute(0),
                now.plusDays(1).withHour(12).withMinute(0),
                40, 18, Activity.ActivityStatus.RECRUITING, "0", "穿跑步鞋，携带手机和耳机", "13800138003",
                user3, sports, 312, 67, 24);
        a3 = activityRepository.save(a3);

        Activity a4 = buildActivity("健身私教体验课", "体育健身",
                "国家职业教练一对一指导，体验先进器械，定制个人训练计划。",
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800",
                "北京市", "北京市", "海淀区", "中关村 fitness", "39.9845", "116.3110",
                now.plusDays(3).withHour(10).withMinute(0), now.plusDays(3).withHour(11).withMinute(0),
                now.plusDays(2).withHour(22).withMinute(0),
                10, 3, Activity.ActivityStatus.RECRUITING, "68", "穿运动服，自带水杯", "13800138004",
                user4, sports, 89, 22, 7);
        a4 = activityRepository.save(a4);

        Activity a5 = buildActivity("社区足球联赛开幕式", "体育健身",
                "2026年度社区足球联赛开幕战，8支社区代表队角逐，精彩不容错过。",
                "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800",
                "北京市", "北京市", "朝阳区", "工人体育场", "39.9322", "116.3970",
                now.plusDays(7).withHour(15).withMinute(0), now.plusDays(7).withHour(18).withMinute(0),
                now.plusDays(6).withHour(18).withMinute(0),
                200, 145, Activity.ActivityStatus.RECRUITING, "0", "无需特殊装备", "13800138005",
                user5, sports, 567, 123, 42);
        a5 = activityRepository.save(a5);

        // ============================================================
        // 活动6-10: 文化艺术类 RECRUITING
        // ============================================================
        Activity a6 = buildActivity("社区绘画工作坊", "文化艺术",
                "专业美术老师带领大家一起创作油画作品，材料全部免费提供，适合零基础学员。",
                "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800",
                "北京市", "北京市", "海淀区", "海淀区文化馆", "39.9818", "116.3074",
                now.plusDays(4).withHour(10).withMinute(0), now.plusDays(4).withHour(16).withMinute(0),
                now.plusDays(3).withHour(20).withMinute(0),
                25, 10, Activity.ActivityStatus.RECRUITING, "0", "无特殊要求", "13800138006",
                user1, culture, 178, 38, 14);
        a6 = activityRepository.save(a6);

        Activity a7 = buildActivity("古典音乐欣赏沙龙", "文化艺术",
                "中央音乐学院学生现场演奏，带你走进古典音乐的世界，配有专业导赏讲解。",
                "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=800",
                "北京市", "北京市", "东城区", "国家大剧院", "39.9405", "116.4088",
                now.plusDays(6).withHour(19).withMinute(30), now.plusDays(6).withHour(21).withMinute(30),
                now.plusDays(5).withHour(22).withMinute(0),
                80, 52, Activity.ActivityStatus.RECRUITING, "99", "请提前15分钟入场", "13800138007",
                user2, culture, 345, 76, 28);
        a7 = activityRepository.save(a7);

        Activity a8 = buildActivity("书法入门公开课", "文化艺术",
                "中国书法家协会会员亲授，从握笔到楷书基础，一堂课带你领略书法之美。",
                "https://images.unsplash.com/photo-1516961642265-531546e84af2?w=800",
                "北京市", "北京市", "西城区", "西城区文化中心", "39.9256", "116.3731",
                now.plusDays(3).withHour(14).withMinute(0), now.plusDays(3).withHour(16).withMinute(0),
                now.plusDays(2).withHour(20).withMinute(0),
                20, 7, Activity.ActivityStatus.RECRUITING, "0", "自带毛笔（如有），现场也提供", "13800138008",
                user3, culture, 98, 25, 11);
        a8 = activityRepository.save(a8);

        Activity a9 = buildActivity("摄影基础实战班", "文化艺术",
                "专业摄影师带队外拍，学习构图与光影运用，理论与实践结合，轻松拍出好照片。",
                "https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=800",
                "北京市", "北京市", "朝阳区", "798艺术区", "39.9835", "116.4975",
                now.plusDays(8).withHour(9).withMinute(0), now.plusDays(8).withHour(15).withMinute(0),
                now.plusDays(7).withHour(20).withMinute(0),
                15, 5, Activity.ActivityStatus.RECRUITING, "88", "需自带相机或手机", "13800138009",
                user4, culture, 267, 54, 19);
        a9 = activityRepository.save(a9);

        Activity a10 = buildActivity("陶艺体验工坊", "文化艺术",
                "零基础也能做的陶艺体验，亲手拉坯制作杯子或碗，成品可带回家留念。",
                "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?w=800",
                "北京市", "北京市", "丰台区", "北京陶瓷艺术馆", "39.8671", "116.4125",
                now.plusDays(5).withHour(13).withMinute(0), now.plusDays(5).withHour(17).withMinute(0),
                now.plusDays(4).withHour(18).withMinute(0),
                12, 4, Activity.ActivityStatus.RECRUITING, "128", "穿深色衣服，做好弄脏的准备", "13800138010",
                user5, culture, 189, 41, 16);
        a10 = activityRepository.save(a10);

        // ============================================================
        // 活动11-15: 科技学习类 RECRUITING
        // ============================================================
        Activity a11 = buildActivity("Python编程入门分享会", "科技学习",
                "资深工程师带你从零搭建Python环境，讲授基础语法，并完成一个小项目实战。",
                "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800",
                "北京市", "北京市", "海淀区", "中关村创业大厦", "39.9831", "116.3063",
                now.plusDays(4).withHour(19).withMinute(0), now.plusDays(4).withHour(21).withMinute(0),
                now.plusDays(3).withHour(22).withMinute(0),
                100, 68, Activity.ActivityStatus.RECRUITING, "0", "自带笔记本电脑", "13800138011",
                user1, tech, 445, 98, 37);
        a11 = activityRepository.save(a11);

        Activity a12 = buildActivity("AI大模型应用工作坊", "科技学习",
                "Hands-on体验ChatGPT、Claude等主流大模型，学习如何用AI提升工作效率。",
                "https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800",
                "北京市", "北京市", "海淀区", "清华科技园会议中心", "40.0097", "116.3222",
                now.plusDays(6).withHour(14).withMinute(0), now.plusDays(6).withHour(17).withMinute(0),
                now.plusDays(5).withHour(20).withMinute(0),
                60, 41, Activity.ActivityStatus.RECRUITING, "49", "无特殊要求", "13800138012",
                user2, tech, 523, 112, 45);
        a12 = activityRepository.save(a12);

        Activity a13 = buildActivity("简历面试技巧训练营", "科技学习",
                "HR专家一对一模拟面试，深度点评简历问题，助你斩获心仪offer。",
                "https://images.unsplash.com/photo-1521737711867-e3b97375f902?w=800",
                "北京市", "北京市", "朝阳区", "望京SOHO", "39.9985", "116.4703",
                now.plusDays(2).withHour(10).withMinute(0), now.plusDays(2).withHour(12).withMinute(0),
                now.plusDays(1).withHour(18).withMinute(0),
                30, 18, Activity.ActivityStatus.RECRUITING, "0", "请携带简历", "13800138013",
                user3, tech, 289, 67, 23);
        a13 = activityRepository.save(a13);

        Activity a14 = buildActivity("无人机航拍体验日", "科技学习",
                "专业飞手带你解锁航拍技能，体验最新款消费级无人机，作品现场点评。",
                "https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=800",
                "北京市", "北京市", "昌平区", "回龙观体育公园", "40.0653", "116.3272",
                now.plusDays(9).withHour(8).withMinute(0), now.plusDays(9).withHour(12).withMinute(0),
                now.plusDays(8).withHour(20).withMinute(0),
                20, 8, Activity.ActivityStatus.RECRUITING, "199", "无需飞行经验，现场提供设备", "13800138014",
                user4, tech, 334, 73, 29);
        a14 = activityRepository.save(a14);

        Activity a15 = buildActivity("数据分析实战营", "科技学习",
                "使用真实商业数据集，从数据清洗到可视化，用Excel和Python完成数据分析报告。",
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800",
                "北京市", "北京市", "海淀区", "北京大学光华管理学院", "39.9877", "116.3126",
                now.plusDays(7).withHour(13).withMinute(0), now.plusDays(7).withHour(18).withMinute(0),
                now.plusDays(6).withHour(20).withMinute(0),
                50, 33, Activity.ActivityStatus.RECRUITING, "0", "自备笔记本电脑，安装好Python", "13800138015",
                user5, tech, 412, 89, 34);
        a15 = activityRepository.save(a15);

        // ============================================================
        // 活动16-20: 社交聚会类 RECRUITING
        // ============================================================
        Activity a16 = buildActivity("周末咖啡品鉴会", "社交聚会",
                "一起品尝来自埃塞俄比亚、哥伦比亚等产区的精品咖啡，深入了解咖啡文化。",
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800",
                "北京市", "北京市", "海淀区", "五道口咖啡空间", "39.9900", "116.3200",
                now.plusDays(2).withHour(15).withMinute(0), now.plusDays(2).withHour(17).withMinute(0),
                now.plusDays(1).withHour(22).withMinute(0),
                20, 14, Activity.ActivityStatus.RECRUITING, "58", "无特殊要求", "13800138016",
                user1, social, 223, 52, 21);
        a16 = activityRepository.save(a16);

        Activity a17 = buildActivity("桌游爱好者聚会", "社交聚会",
                "狼人杀、三国杀、宝石商人...多种桌游任你选，新手老手都能玩得开心。",
                "https://images.unsplash.com/photo-1610890716171-6b1c9f2bd40c?w=800",
                "北京市", "北京市", "朝阳区", "三里屯桌游吧", "39.9387", "116.4521",
                now.plusDays(3).withHour(18).withMinute(0), now.plusDays(3).withHour(22).withMinute(0),
                now.plusDays(2).withHour(22).withMinute(0),
                16, 9, Activity.ActivityStatus.RECRUITING, "35", "无需桌游经验", "13800138017",
                user2, social, 198, 44, 17);
        a17 = activityRepository.save(a17);

        Activity a18 = buildActivity("单身青年联谊派对", "社交聚会",
                "精心设计的互动游戏，帮助单身青年拓宽社交圈，结识志趣相投的新朋友。",
                "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=800",
                "北京市", "北京市", "东城区", "东直门南大街PartyRoom", "39.9419", "116.4279",
                now.plusDays(5).withHour(19).withMinute(0), now.plusDays(5).withHour(22).withMinute(0),
                now.plusDays(4).withHour(20).withMinute(0),
                50, 38, Activity.ActivityStatus.RECRUITING, "66", "仅限单身人士参加", "13800138018",
                user3, social, 478, 102, 53);
        a18 = activityRepository.save(a18);

        Activity a19 = buildActivity("周末英语角", "社交聚会",
                "外教主持，话题涵盖文化、旅行、科技等领域，轻松氛围中提升英语口语。",
                "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800",
                "北京市", "北京市", "海淀区", "五道口英语吧", "39.9905", "116.3195",
                now.plusDays(4).withHour(19).withMinute(0), now.plusDays(4).withHour(21).withMinute(0),
                now.plusDays(3).withHour(22).withMinute(0),
                25, 16, Activity.ActivityStatus.RECRUITING, "0", "CET-4及以上水平", "13800138019",
                user4, social, 312, 68, 27);
        a19 = activityRepository.save(a19);

        Activity a20 = buildActivity("同城交换书籍活动", "社交聚会",
                "带一本你读过的书，换一本别人推荐的书，分享阅读心得，结交爱读书的朋友。",
                "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800",
                "北京市", "北京市", "朝阳区", "朝阳区图书馆多功能厅", "39.9280", "116.4400",
                now.plusDays(6).withHour(14).withMinute(0), now.plusDays(6).withHour(17).withMinute(0),
                now.plusDays(5).withHour(20).withMinute(0),
                40, 27, Activity.ActivityStatus.RECRUITING, "0", "所带书籍八成新以上", "13800138020",
                user5, social, 187, 43, 18);
        a20 = activityRepository.save(a20);

        // ============================================================
        // 活动21-25: 户外探险类 RECRUITING
        // ============================================================
        Activity a21 = buildActivity("春季登山踏青", "户外探险",
                "春天的第一次户外远足，去香山欣赏春色，全程约5公里，强度适中，适合全家参与。",
                "https://images.unsplash.com/photo-1551632811-561732d1e306?w=800",
                "北京市", "北京市", "海淀区", "香山公园东门集合", "39.9970", "116.1930",
                now.plusDays(4).withHour(8).withMinute(0), now.plusDays(4).withHour(14).withMinute(0),
                now.plusDays(3).withHour(20).withMinute(0),
                30, 19, Activity.ActivityStatus.RECRUITING, "10", "穿登山鞋，自备水和干粮", "13800138021",
                user1, outdoor, 267, 58, 22);
        a21 = activityRepository.save(a21);

        Activity a22 = buildActivity("城市骑行挑战赛", "户外探险",
                "从朝阳公园出发，绕北京二环一圈，全程约30公里，欢迎骑行爱好者报名。",
                "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=800",
                "北京市", "北京市", "朝阳区", "朝阳公园西门", "39.9330", "116.4570",
                now.plusDays(7).withHour(7).withMinute(0), now.plusDays(7).withHour(11).withMinute(0),
                now.plusDays(6).withHour(20).withMinute(0),
                50, 31, Activity.ActivityStatus.RECRUITING, "0", "需自带自行车和头盔", "13800138022",
                user2, outdoor, 389, 84, 31);
        a22 = activityRepository.save(a22);

        Activity a23 = buildActivity("密云水库露营体验", "户外探险",
                "两天一夜户外露营，烧烤、篝火、观星，远离城市喧嚣，回归自然怀抱。",
                "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800",
                "北京市", "北京市", "密云区", "密云水库北岸营地", "40.5123", "116.8012",
                now.plusDays(10).withHour(8).withMinute(0), now.plusDays(11).withHour(18).withMinute(0),
                now.plusDays(9).withHour(20).withMinute(0),
                25, 11, Activity.ActivityStatus.RECRUITING, "168", "需自带睡袋，营地提供帐篷", "13800138023",
                user3, outdoor, 456, 97, 38);
        a23 = activityRepository.save(a23);

        Activity a24 = buildActivity("奥森公园亲子自然课", "户外探险",
                "专业自然导师带队，观察鸟类和植物，制作植物标本，适合6-12岁儿童和家长。",
                "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800",
                "北京市", "北京市", "朝阳区", "奥林匹克森林公园北园", "40.0130", "116.4030",
                now.plusDays(3).withHour(9).withMinute(0), now.plusDays(3).withHour(12).withMinute(0),
                now.plusDays(2).withHour(18).withMinute(0),
                20, 12, Activity.ActivityStatus.RECRUITING, "58", "适合6-12岁儿童，需家长陪同", "13800138024",
                user4, outdoor, 198, 46, 19);
        a24 = activityRepository.save(a24);

        Activity a25 = buildActivity("野溪垂钓休闲日", "户外探险",
                "北京周边优质野钓点，体验溪流钓鱼的乐趣，免费提供基础钓具和鱼饵。",
                "https://images.unsplash.com/photo-1500634245200-e524e5f97444?w=800",
                "北京市", "北京市", "怀柔区", "怀柔区野溪钓场", "40.3923", "116.6512",
                now.plusDays(5).withHour(6).withMinute(0), now.plusDays(5).withHour(14).withMinute(0),
                now.plusDays(4).withHour(18).withMinute(0),
                15, 6, Activity.ActivityStatus.RECRUITING, "88", "钓具由主办方提供，请勿放生鱼", "13800138025",
                user5, outdoor, 145, 33, 12);
        a25 = activityRepository.save(a25);

        // ============================================================
        // 活动26: 志愿服务类 RECRUITING
        // ============================================================
        Activity a26 = buildActivity("社区老人关怀行动", "志愿服务",
                "探访社区独居老人，陪伴聊天、帮忙购物，共建温暖社区，志愿者可获得服务时长证明。",
                "https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=800",
                "北京市", "北京市", "朝阳区", "劲松社区服务中心", "39.8785", "116.4355",
                now.plusDays(2).withHour(9).withMinute(0), now.plusDays(2).withHour(12).withMinute(0),
                now.plusDays(1).withHour(18).withMinute(0),
                20, 13, Activity.ActivityStatus.RECRUITING, "0", "有爱心，耐心，诚实可靠", "13800138026",
                user1, volunteer, 134, 38, 15);
        a26 = activityRepository.save(a26);

        Activity a27 = buildActivity("流浪动物救助站志愿", "志愿服务",
                "到流浪动物救助站帮忙照顾猫狗，打扫笼舍、喂食、遛狗，献出一份爱心。",
                "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=800",
                "北京市", "北京市", "通州区", "北京爱心动物救助站", "39.9012", "116.6512",
                now.plusDays(4).withHour(10).withMinute(0), now.plusDays(4).withHour(15).withMinute(0),
                now.plusDays(3).withHour(18).withMinute(0),
                15, 7, Activity.ActivityStatus.RECRUITING, "0", "不怕动物，有耐心", "13800138027",
                user2, volunteer, 212, 56, 21);
        a27 = activityRepository.save(a27);

        Activity a28 = buildActivity("城市环保清洁行动", "志愿服务",
                "沿河岸和公园捡拾垃圾，宣传环保知识，用行动守护北京蓝天白云。",
                "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=800",
                "北京市", "北京市", "海淀区", "昆玉河畔西三环段", "39.9545", "116.3123",
                now.plusDays(6).withHour(8).withMinute(0), now.plusDays(6).withHour(11).withMinute(0),
                now.plusDays(5).withHour(20).withMinute(0),
                30, 22, Activity.ActivityStatus.RECRUITING, "0", "主办方提供手套和垃圾袋", "13800138028",
                user3, volunteer, 289, 72, 28);
        a28 = activityRepository.save(a28);

        // ============================================================
        // 活动29-30: ONGOING 进行中
        // ============================================================
        // 进行中活动：已开始但未结束
        Activity a29 = buildActivity("社区读书会第23期", "文化艺术",
                "本期共读《人类简史》，一起讨论认知革命带给人类的深刻变革。",
                "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800",
                "北京市", "北京市", "海淀区", "北京大学图书馆研讨室A", "39.9877", "116.3126",
                now.minusHours(2), now.plusHours(2), now.minusDays(1),
                30, 24, Activity.ActivityStatus.ONGOING, "0", "请提前阅读第一章", "13800138029",
                user4, culture, 156, 34, 22);
        a29 = activityRepository.save(a29);

        Activity a30 = buildActivity("春季马拉松训练营", "体育健身",
                "专业教练带队，系统训练备战2026北京春季马拉松，内容包括体能测试、配速训练。",
                "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=800",
                "北京市", "北京市", "朝阳区", "朝阳公园跑道", "39.9330", "116.4570",
                now.minusHours(1), now.plusHours(3), now.minusDays(2),
                40, 35, Activity.ActivityStatus.ONGOING, "0", "有一定跑步基础", "13800138030",
                user5, sports, 423, 89, 36);
        a30 = activityRepository.save(a30);

        // ============================================================
        // 初始化用户兴趣标签（模拟用户选择兴趣的过程）
        // ============================================================
        initUserInterests(user1, user2, user3, user4, user5, admin,
                sports, culture, tech, social, outdoor, volunteer);

        // ============================================================
        // 初始化参与者记录（每个用户参与10-15个活动，供User-CF使用）
        // ============================================================
        initParticipations(user1, user2, user3, user4, user5, admin, sports, culture, tech, social, outdoor, volunteer, now);

        log.info("测试数据初始化完成！共创建 {} 个用户和 {} 个活动", userRepository.count(), activityRepository.count());
        log.info("- 管理员账号: admin / admin123");
        log.info("- 普通用户: zhangsan / 123456, lisi / 123456, wangwu / 123456");
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /**
     * 初始化用户兴趣标签
     * 每个用户分配2-3个兴趣标签，用户间有重叠以便User-CF找到相似用户
     */
    private void initUserInterests(User u1, User u2, User u3, User u4, User u5, User admin,
                                   Interest sports, Interest culture, Interest tech,
                                   Interest social, Interest outdoor, Interest volunteer) {
        // 避免重复初始化
        if (userInterestRepository.count() > 0) {
            return;
        }

        // 用户兴趣配置：(用户, 兴趣标签列表, 参与次数权重, 点击次数权重)
        // 权重设计：主要兴趣 participateCount=8, clickCount=15；次要兴趣 participateCount=3, clickCount=8
        record UserInterestConfig(User user, Interest primary, Interest secondary, Interest tertiary) {}

        List<UserInterestConfig> configs = List.of(
            // zhangsan：体育健身(主) + 户外探险(主) + 社交聚会(次)
            new UserInterestConfig(u1, sports, outdoor, social),
            // lisi：文化艺术(主) + 科技学习(主) + 社交聚会(次)
            new UserInterestConfig(u2, culture, tech, social),
            // wangwu：体育健身(主) + 志愿服务(主) + 社交聚会(次)
            new UserInterestConfig(u3, sports, volunteer, social),
            // zhaoliu：科技学习(主) + 文化艺术(主) + 户外探险(次)
            new UserInterestConfig(u4, tech, culture, outdoor),
            // sunqi：户外探险(主) + 志愿服务(主) + 文化艺术(次)
            new UserInterestConfig(u5, outdoor, volunteer, culture)
        );

        for (UserInterestConfig cfg : configs) {
            // 主要兴趣1
            saveUserInterest(cfg.user(), cfg.primary(), 8, 15);
            // 主要兴趣2
            saveUserInterest(cfg.user(), cfg.secondary(), 8, 15);
            // 次要兴趣（可能为null）
            if (cfg.tertiary() != null) {
                saveUserInterest(cfg.user(), cfg.tertiary(), 3, 8);
            }
        }

        log.info("用户兴趣标签初始化完成，共 {} 条记录", userInterestRepository.count());
    }

    private void saveUserInterest(User user, Interest interest, int participateCount, int clickCount) {
        if (userInterestRepository.findByUserIdAndInterestId(user.getId(), interest.getId()).isEmpty()) {
            UserInterest ui = UserInterest.builder()
                    .user(user)
                    .interest(interest)
                    .weight(1)
                    .participateCount(participateCount)
                    .clickCount(clickCount)
                    .build();
            userInterestRepository.save(ui);
        }
    }

    /**
     * 初始化参与者记录
     * 每个用户参与10-15个活动，且用户间有活动重叠（以便User-CF计算相似度）
     */
    private void initParticipations(User u1, User u2, User u3, User u4, User u5, User admin,
                                    Interest sports, Interest culture, Interest tech,
                                    Interest social, Interest outdoor, Interest volunteer,
                                    LocalDateTime now) {
        // 避免重复初始化
        if (participantRepository.count() > 0) {
            return;
        }

        // 按兴趣分类活动（根据DataInitializer中创建顺序）
        // a1-a5: sports, a6-a10: culture, a11-a15: tech
        // a16-a20: social, a21-a25: outdoor, a26-a28: volunteer
        Long[] sportsIds    = {1L, 2L, 3L, 4L, 5L};
        Long[] cultureIds   = {6L, 7L, 8L, 9L, 10L};
        Long[] techIds      = {11L, 12L, 13L, 14L, 15L};
        Long[] socialIds    = {16L, 17L, 18L, 19L, 20L};
        Long[] outdoorIds   = {21L, 22L, 23L, 24L, 25L};
        Long[] volunteerIds = {26L, 27L, 28L};

        // 用户参与活动映射（每个用户10-15个，且用户间有明显重叠）
        // zhangsan: 体育5 + 户外5 + 社交3 = 13个
        saveParticipations(u1, concat(sportsIds, outdoorIds, socialIds), now);
        // lisi: 文化5 + 科技5 + 社交4 = 14个
        saveParticipations(u2, concat(cultureIds, techIds, socialIds), now);
        // wangwu: 体育5 + 志愿3 + 社交4 = 12个
        saveParticipations(u3, concat(sportsIds, volunteerIds, socialIds), now);
        // zhaoliu: 科技5 + 文化5 + 户外3 = 13个
        saveParticipations(u4, concat(techIds, cultureIds, outdoorIds), now);
        // sunqi: 户外5 + 志愿3 + 文化3 = 11个
        saveParticipations(u5, concat(outdoorIds, volunteerIds, cultureIds), now);

        // 管理员少量参与（体现平台活跃）
        saveParticipations(admin, new Long[]{1L, 6L, 11L, 16L, 21L, 26L}, now);

        log.info("参与者记录初始化完成，共 {} 条记录", participantRepository.count());
    }

    private void saveParticipations(User user, Long[] activityIds, LocalDateTime now) {
        for (int i = 0; i < activityIds.length; i++) {
            Long actId = activityIds[i];
            if (participantRepository.existsByUser_IdAndActivity_Id(user.getId(), actId)) {
                continue;
            }
            Activity activity = activityRepository.findById(actId).orElse(null);
            if (activity == null) continue;

            ActivityParticipant p = ActivityParticipant.builder()
                    .user(user)
                    .activity(activity)
                    .status(ActivityParticipant.ParticipantStatus.APPROVED)
                    .createdAt(now.minusDays(i % 7 + 1))
                    .build();
            participantRepository.save(p);
        }
    }

    private Long[] concat(Long[] a, Long[] b, Long[] c) {
        Long[] result = new Long[a.length + b.length + c.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length + b.length, c.length);
        return result;
    }

    private Activity buildActivity(String title, String category, String description,
                                   String coverImage, String province, String city, String district,
                                   String address, String lat, String lng,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   LocalDateTime registrationDeadline,
                                   int maxParticipants, int currentParticipants,
                                   Activity.ActivityStatus status, String fee,
                                   String requirements, String contactPhone,
                                   User creator, Interest interest,
                                   int viewCount, int likeCount, int commentCount) {
        Activity activity = Activity.builder()
                .title(title)
                .description(description)
                .content("<p>" + description + "</p>")
                .coverImage(coverImage)
                .province(province)
                .city(city)
                .district(district)
                .address(address)
                .latitude(lat)
                .longitude(lng)
                .startTime(startTime)
                .endTime(endTime)
                .registrationDeadline(registrationDeadline)
                .maxParticipants(maxParticipants)
                .currentParticipants(currentParticipants)
                .status(status)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .fee(fee)
                .requirements(requirements)
                .contactPhone(contactPhone)
                .creator(creator)
                .build();
        activity.setInterests(new ArrayList<>());
        activity.getInterests().add(interest);
        return activity;
    }

    private Interest createInterestIfNotExists(String name, String category, String icon) {
        return interestRepository.findByName(name).orElseGet(() -> {
            Interest interest = Interest.builder()
                    .name(name)
                    .category(category)
                    .description(name)
                    .popularity(0)
                    .active(true)
                    .build();
            return interestRepository.save(interest);
        });
    }

    /**
     * 修复已有参与者记录：将 PENDING 状态改为 APPROVED
     */
    private void fixPendingParticipants() {
        try {
            var pendingParticipants = participantRepository.findByStatus(ActivityParticipant.ParticipantStatus.PENDING);
            if (!pendingParticipants.isEmpty()) {
                log.info("发现 {} 条 PENDING 状态的参与记录，正在修复为 APPROVED...", pendingParticipants.size());
                for (var p : pendingParticipants) {
                    p.setStatus(ActivityParticipant.ParticipantStatus.APPROVED);
                }
                participantRepository.saveAll(pendingParticipants);
                log.info("已修复 {} 条参与记录", pendingParticipants.size());
            }
        } catch (Exception e) {
            log.warn("修复 PENDING 参与记录时出错: {}", e.getMessage());
        }
    }
}
