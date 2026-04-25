-- ============================================
-- 社区活动平台数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS community_activity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE community_activity;
-- ============================================
-- 创建表结构 (必须在 INSERT 之前执行)
-- ============================================

-- 1. 创建兴趣标签表 (interests)
CREATE TABLE IF NOT EXISTS interests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '兴趣名称',
    description VARCHAR(255) COMMENT '兴趣描述',
    category VARCHAR(50) COMMENT '分类',
    popularity INT DEFAULT 0 COMMENT '热度',
    active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兴趣标签表';

-- 2. 创建用户表 (users) - 因为后面有插入 users 数据
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    gender ENUM('男', '女', '未知') DEFAULT '未知' COMMENT '性别',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '区县',
    avatar VARCHAR(255) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '状态',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER' COMMENT '角色',
    credits INT DEFAULT 0 COMMENT '积分',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 创建用户兴趣关联表 (user_interests)
CREATE TABLE IF NOT EXISTS user_interests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    interest_id INT NOT NULL COMMENT '兴趣ID',
    weight INT DEFAULT 1 COMMENT '权重',
    click_count INT DEFAULT 0 COMMENT '点击次数',
    participate_count INT DEFAULT 0 COMMENT '参与次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_interest (user_id, interest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户兴趣关联表';

-- 4. 创建活动表 (activities)
CREATE TABLE IF NOT EXISTS activities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    description VARCHAR(255) COMMENT '活动简述',
    content TEXT COMMENT '活动内容详情',
    creator_id INT NOT NULL COMMENT '创建者ID',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '区县',
    address VARCHAR(255) COMMENT '详细地址',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    longitude DECIMAL(10, 7) COMMENT '经度',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    registration_deadline DATETIME COMMENT '报名截止时间',
    max_participants INT DEFAULT 0 COMMENT '最大人数',
    current_participants INT DEFAULT 0 COMMENT '当前人数',
    status ENUM('RECRUITING', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'RECRUITING' COMMENT '状态',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    fee VARCHAR(50) DEFAULT '免费' COMMENT '费用',
    requirements TEXT COMMENT '参与要求',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 5. 创建活动兴趣关联表 (activity_interests)
CREATE TABLE IF NOT EXISTS activity_interests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    activity_id INT NOT NULL,
    interest_id INT NOT NULL,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE,
    UNIQUE KEY uk_activity_interest (activity_id, interest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 创建活动参与者表 (activity_participants)
CREATE TABLE IF NOT EXISTS activity_participants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    activity_id INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CHECKED_IN') DEFAULT 'PENDING',
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_activity (user_id, activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 创建活动点赞表 (activity_likes)
CREATE TABLE IF NOT EXISTS activity_likes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    activity_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_activity_like (user_id, activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 创建活动评论表 (activity_comments)
CREATE TABLE IF NOT EXISTS activity_comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    activity_id INT NOT NULL,
    parent_id INT DEFAULT NULL COMMENT '父评论ID，用于回复',
    content TEXT NOT NULL,
    like_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES activity_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 创建用户关注表 (user_follows)
CREATE TABLE IF NOT EXISTS user_follows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT NOT NULL COMMENT '关注者ID',
    followed_id INT NOT NULL COMMENT '被关注者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (followed_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_follower_followed (follower_id, followed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 创建活动浏览表 (activity_views)
CREATE TABLE IF NOT EXISTS activity_views (
    id INT AUTO_INCREMENT PRIMARY KEY,
    activity_id INT NOT NULL,
    view_duration INT DEFAULT 0 COMMENT '浏览时长(秒)',
    device_type VARCHAR(20) COMMENT '设备类型',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 表结构创建完成，接下来可以插入数据了
-- ============================================
-- ============================================
-- 兴趣标签数据
-- ============================================
INSERT INTO interests (name, description, category, popularity, active) VALUES

-- 运动健身类

('篮球', '篮球运动及相关活动', '运动健身', 50, TRUE),
('足球', '足球运动及相关活动', '运动健身', 45, TRUE),
('羽毛球', '羽毛球运动及相关活动', '运动健身', 40, TRUE),
('跑步', '跑步健身活动', '运动健身', 60, TRUE),
('游泳', '游泳运动及相关活动', '运动健身', 35, TRUE),
('健身', '健身房健身活动', '运动健身', 55, TRUE),
('瑜伽', '瑜伽健身活动', '运动健身', 42, TRUE),
('骑行', '骑行运动及相关活动', '运动健身', 38, TRUE),
('爬山', '爬山徒步活动', '运动健身', 48, TRUE),
('乒乓球', '乒乓球运动及相关活动', '运动健身', 30, TRUE),

-- 文化艺术类
('阅读', '读书分享会等活动', '文化艺术', 52, TRUE),
('书法', '书法练习及交流活动', '文化艺术', 25, TRUE),
('绘画', '绘画艺术活动', '文化艺术', 32, TRUE),
('摄影', '摄影采风及作品分享', '文化艺术', 58, TRUE),
('音乐', '音乐欣赏及演奏活动', '文化艺术', 45, TRUE),
('舞蹈', '舞蹈学习及表演活动', '文化艺术', 40, TRUE),
('戏剧', '戏剧欣赏及表演活动', '文化艺术', 22, TRUE),
('电影', '电影观赏及讨论活动', '文化艺术', 65, TRUE),
('写作', '写作练习及分享活动', '文化艺术', 28, TRUE),
('收藏', '收藏品鉴赏及交流活动', '文化艺术', 20, TRUE),

-- 社交联谊类
('交友', '单身交友活动', '社交联谊', 70, TRUE),
('聚会', '各类主题聚会活动', '社交联谊', 55, TRUE),
('桌游', '桌游娱乐活动', '社交联谊', 48, TRUE),
('烧烤', '户外烧烤活动', '社交联谊', 42, TRUE),
('KTV', 'KTV娱乐活动', '社交联谊', 50, TRUE),
('聚餐', '美食聚餐活动', '社交联谊', 60, TRUE),
('旅游', '短途旅游活动', '社交联谊', 68, TRUE),
('徒步', '户外徒步活动', '社交联谊', 35, TRUE),
('露营', '户外露营活动', '社交联谊', 40, TRUE),
('相亲', '相亲交友活动', '社交联谊', 45, TRUE),

-- 学习培训类
('英语', '英语学习交流活动', '学习培训', 38, TRUE),
('编程', '编程技术交流', '学习培训', 55, TRUE),
('创业', '创业经验分享', '学习培训', 42, TRUE),
('理财', '理财知识讲座', '学习培训', 48, TRUE),
('讲座', '各类主题讲座', '学习培训', 50, TRUE),
('工作坊', '实践技能工作坊', '学习培训', 35, TRUE),
('培训', '专业培训课程', '学习培训', 40, TRUE),
('考证', '考试学习交流', '学习培训', 30, TRUE),
('考研', '考研学习交流', '学习培训', 28, TRUE),
('职业发展', '职业规划及发展交流', '学习培训', 45, TRUE),

-- 公益志愿类
('环保', '环保公益活动', '公益志愿', 32, TRUE),
('扶贫', '扶贫帮困活动', '公益志愿', 25, TRUE),
('支教', '教育支教活动', '公益志愿', 22, TRUE),
('敬老', '敬老爱老活动', '公益志愿', 35, TRUE),
('助残', '助残帮扶活动', '公益志愿', 20, TRUE),
('献血', '无偿献血活动', '公益志愿', 30, TRUE),
('慈善', '慈善募捐活动', '公益志愿', 28, TRUE),
('义卖', '义卖募捐活动', '公益志愿', 25, TRUE),
('环保宣传', '环保宣传教育活动', '公益志愿', 18, TRUE),
('社区服务', '社区志愿服务', '公益志愿', 40, TRUE),

-- 亲子活动类
('亲子游戏', '亲子互动游戏活动', '亲子活动', 45, TRUE),
('亲子阅读', '亲子共读活动', '亲子活动', 38, TRUE),
('亲子手工', '亲子手工制作活动', '亲子活动', 42, TRUE),
('亲子运动', '亲子体育运动活动', '亲子活动', 40, TRUE),
('亲子旅游', '亲子短途旅游活动', '亲子活动', 35, TRUE),
('育儿交流', '育儿经验分享交流', '亲子活动', 48, TRUE),
('亲子演出', '亲子观看演出活动', '亲子活动', 32, TRUE),
('亲子烘焙', '亲子烘焙制作活动', '亲子活动', 30, TRUE),
('亲子科技', '亲子科技体验活动', '亲子活动', 25, TRUE),
('亲子自然', '亲子自然探索活动', '亲子活动', 28, TRUE);

-- ============================================
-- 测试用户数据
-- ============================================
INSERT INTO users (username, password, nickname, email, phone, gender, province, city, district, avatar, bio, status, role, credits, created_at, updated_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin@example.com', '13800138001', '男', '北京市', '北京市', '朝阳区', 'https://example.com/avatar/admin.jpg', '社区活动平台管理员', 'ACTIVE', 'ADMIN', 1000, NOW(), NOW()),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 'zhangsan@example.com', '13800138002', '男', '上海市', '上海市', '浦东新区', 'https://example.com/avatar/user1.jpg', '热爱运动的程序员', 'ACTIVE', 'USER', 500, NOW(), NOW()),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 'lisi@example.com', '13800138003', '女', '上海市', '上海市', '徐汇区', 'https://example.com/avatar/user2.jpg', '文艺青年', 'ACTIVE', 'USER', 300, NOW(), NOW()),
('user3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', 'wangwu@example.com', '13800138004', '男', '北京市', '北京市', '海淀区', 'https://example.com/avatar/user3.jpg', '摄影爱好者', 'ACTIVE', 'USER', 450, NOW(), NOW()),
('user4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', 'zhaoliu@example.com', '13800138005', '女', '广州市', '广州市', '天河区', 'https://example.com/avatar/user4.jpg', '喜欢阅读', 'ACTIVE', 'USER', 280, NOW(), NOW());

-- ============================================
-- 用户兴趣标签数据
-- ============================================
INSERT INTO user_interests (user_id, interest_id, weight, click_count, participate_count) VALUES
-- 管理员的兴趣
(1, 1, 1, 10, 5),
(1, 2, 1, 8, 4),
(1, 3, 1, 6, 3),

-- 张三的兴趣(运动类)
(2, 1, 3, 15, 8),
(2, 2, 2, 10, 5),
(2, 4, 3, 20, 10),
(2, 6, 2, 12, 6),
(2, 7, 1, 8, 3),

-- 李四的兴趣(文艺类)
(3, 11, 3, 18, 9),
(3, 12, 2, 10, 4),
(3, 13, 2, 12, 5),
(3, 15, 3, 15, 7),
(3, 16, 1, 8, 3),

-- 王五的兴趣(摄影、社交类)
(4, 14, 4, 25, 12),
(4, 21, 2, 12, 5),
(4, 22, 2, 10, 4),
(4, 23, 1, 8, 3),

-- 赵六的兴趣(阅读、学习类)
(5, 11, 4, 30, 15),
(5, 31, 2, 10, 4),
(5, 32, 2, 12, 5),
(5, 35, 1, 8, 3);

-- ============================================
-- 活动数据
-- ============================================
INSERT INTO activities (title, description, content, creator_id, province, city, district, address, latitude, longitude, start_time, end_time, registration_deadline, max_participants, current_participants, status, view_count, like_count, comment_count, fee, requirements, contact_phone, created_at, updated_at) VALUES
-- 运动类活动
('周末篮球友谊赛', '周末篮球友谊赛,欢迎篮球爱好者参加', '本周六下午2点,在XX体育馆举行篮球友谊赛,欢迎各位篮球爱好者前来参加,增进友谊,切磋球技。', 2, '上海市', '上海市', '浦东新区', 'XX体育馆', '31.230416', '121.473701', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 20, 15, 'RECRUITING', 120, 35, 8, '免费', '自带篮球,穿着运动装备', '13800138002', NOW(), NOW()),

('城市夜跑活动', '每周一次的城市夜跑活动', '每周二、四晚上8点,在XX公园集合进行夜跑活动,路线约5公里,欢迎跑步爱好者参加。', 2, '上海市', '上海市', '浦东新区', 'XX公园', '31.220416', '121.463701', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 50, 38, 'RECRUITING', 200, 65, 12, '免费', '穿着运动装备,自备饮用水', '13800138002', NOW(), NOW()),

-- 文化艺术类活动
('读书分享会', '每月一次的读书分享会', '本月读书分享会主题为"经典文学",欢迎各位书友分享自己喜欢的文学作品和阅读心得。', 5, '广州市', '广州市', '天河区', 'XX书店', '23.129110', '113.264385', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 30, 22, 'RECRUITING', 150, 45, 15, '免费', '携带自己喜欢的书籍', '13800138005', NOW(), NOW()),

('摄影采风活动', '周末摄影采风活动', '本周六上午,组织摄影爱好者前往XX公园进行摄影采风活动,欢迎摄影爱好者参加。', 4, '北京市', '北京市', '海淀区', 'XX公园', '39.904989', '116.405285', DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 25, 18, 'RECRUITING', 180, 55, 10, '50元', '自带相机,有一定的摄影基础', '13800138004', NOW(), NOW()),

-- 社交联谊类活动
('单身交友派对', '周末单身交友派对', '本周六晚上7点,在XX酒吧举行单身交友派对,欢迎单身青年参加,拓展社交圈。', 2, '上海市', '上海市', '浦东新区', 'XX酒吧', '31.240416', '121.483701', DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 40, 30, 'RECRUITING', 250, 80, 20, '100元', '年龄22-35岁,穿着得体', '13800138002', NOW(), NOW()),

('户外烧烤活动', '周末户外烧烤活动', '本周日,组织户外烧烤活动,地点在XX公园,欢迎朋友们参加,一起享受美食和阳光。', 3, '上海市', '上海市', '徐汇区', 'XX公园', '31.210416', '121.443701', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 35, 28, 'RECRUITING', 220, 70, 18, '80元', '费用AA制', '13800138003', NOW(), NOW()),

-- 学习培训类活动
('Python编程入门', 'Python编程入门课程', '为期4周的Python编程入门课程,适合零基础的学员参加,学习Python基础语法和编程思维。', 4, '北京市', '北京市', '海淀区', 'XX培训中心', '39.914989', '116.415285', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 38 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), 20, 12, 'RECRUITING', 300, 90, 25, '500元', '自带笔记本电脑', '13800138004', NOW(), NOW()),

('理财知识讲座', '个人理财知识讲座', '邀请专业理财师讲解个人理财知识,包括基金、股票、保险等方面的投资建议。', 5, '广州市', '广州市', '天河区', 'XX会议室', '23.139110', '113.274385', DATE_ADD(NOW(), INTERVAL 8 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 50, 40, 'RECRUITING', 280, 75, 30, '免费', '对理财有兴趣的朋友', '13800138005', NOW(), NOW()),

-- 公益志愿类活动
('环保公益活动', '周末环保公益活动', '本周六,组织志愿者到XX公园进行环保公益活动,包括垃圾清理、环保宣传等。', 3, '上海市', '上海市', '徐汇区', 'XX公园', '31.220416', '121.453701', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 30, 25, 'RECRUITING', 160, 50, 12, '免费', '愿意参与公益活动', '13800138003', NOW(), NOW()),

('敬老爱老活动', '敬老院志愿服务', '本周日,组织志愿者到XX敬老院进行志愿服务,为老人们送去温暖和关怀。', 4, '北京市', '北京市', '海淀区', 'XX敬老院', '39.924989', '116.425285', DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 20, 15, 'RECRUITING', 140, 42, 8, '免费', '有爱心,有耐心', '13800138004', NOW(), NOW());

-- ============================================
-- 活动兴趣标签关联数据
-- ============================================
INSERT INTO activity_interests (activity_id, interest_id) VALUES
-- 篮球友谊赛
(1, 1),
-- 城市夜跑
(2, 4),
-- 读书分享会
(3, 11),
-- 摄影采风
(4, 14),
(4, 21),
-- 单身交友派对
(5, 21),
(5, 24),
-- 户外烧烤
(6, 22),
(6, 24),
-- Python编程
(7, 32),
-- 理财讲座
(8, 34),
-- 环保公益
(9, 41),
-- 敬老活动
(10, 44);

-- ============================================
-- 活动参与数据
-- ============================================
INSERT INTO activity_participants (user_id, activity_id, status, message, created_at) VALUES
(3, 1, 'APPROVED', '我擅长篮球,希望参加', NOW()),
(4, 1, 'PENDING', '想参加学习', NOW()),
(5, 1, 'APPROVED', '我也喜欢篮球', NOW()),
(2, 2, 'CHECKED_IN', '每周都来', NOW()),
(3, 2, 'APPROVED', '加入夜跑', NOW()),
(4, 3, 'APPROVED', '喜欢阅读', NOW()),
(5, 3, 'PENDING', '学习交流', NOW()),
(2, 5, 'PENDING', '想认识新朋友', NOW()),
(3, 5, 'APPROVED', '单身交友', NOW()),
(4, 5, 'PENDING', '期待参加', NOW()),
(2, 6, 'APPROVED', '参加烧烤', NOW()),
(3, 6, 'CHECKED_IN', '喜欢户外活动', NOW());

-- ============================================
-- 活动点赞数据
-- ============================================
INSERT INTO activity_likes (user_id, activity_id, created_at) VALUES
(2, 3, NOW()),
(3, 4, NOW()),
(4, 2, NOW()),
(5, 1, NOW()),
(2, 5, NOW()),
(3, 5, NOW()),
(4, 6, NOW()),
(5, 7, NOW()),
(2, 8, NOW()),
(3, 9, NOW()),
(4, 10, NOW());

-- ============================================
-- 活动评论数据
-- ============================================
INSERT INTO activity_comments (user_id, activity_id, parent_id, content, like_count, created_at, updated_at) VALUES
(3, 1, NULL, '这个活动看起来不错,期待参加!', 5, NOW(), NOW()),
(4, 1, NULL, '想报名参加,什么时候截止?', 3, NOW(), NOW()),
(2, 1, 2, '报名截止时间是本周三', 2, NOW(), NOW()),
(5, 3, NULL, '读书分享会很有意义', 6, NOW(), NOW()),
(2, 3, 4, '分享哪本经典文学?', 1, NOW(), NOW()),
(4, 5, NULL, '期待认识新朋友', 4, NOW(), NOW()),
(3, 6, NULL, '户外烧烤太棒了!', 5, NOW(), NOW()),
(4, 7, NULL, 'Python课程很实用', 3, NOW(), NOW());

-- ============================================
-- 用户关注数据
-- ============================================
INSERT INTO user_follows (follower_id, followed_id, created_at) VALUES
(2, 3, NOW()),
(2, 4, NOW()),
(2, 5, NOW()),
(3, 2, NOW()),
(3, 4, NOW()),
(3, 5, NOW()),
(4, 2, NOW()),
(4, 3, NOW()),
(5, 2, NOW()),
(5, 3, NOW());

-- ============================================
-- 活动浏览数据
-- ============================================
INSERT INTO activity_views (activity_id, view_duration, device_type, ip_address, created_at) VALUES
(1, 120, 'mobile', '192.168.1.100', NOW()),
(1, 85, 'desktop', '192.168.1.101', NOW()),
(2, 90, 'mobile', '192.168.1.102', NOW()),
(3, 150, 'desktop', '192.168.1.103', NOW()),
(4, 200, 'mobile', '192.168.1.104', NOW()),
(5, 75, 'desktop', '192.168.1.105', NOW()),
(6, 110, 'mobile', '192.168.1.106', NOW()),
(7, 180, 'desktop', '192.168.1.107', NOW()),
(8, 95, 'mobile', '192.168.1.108', NOW()),
(9, 130, 'desktop', '192.168.1.109', NOW());

-- ============================================
-- 数据插入完成
-- ============================================
SELECT '数据初始化完成!' AS message;
SELECT COUNT(*) AS '用户数量' FROM users;
SELECT COUNT(*) AS '兴趣标签数量' FROM interests;
SELECT COUNT(*) AS '活动数量' FROM activities;
SELECT COUNT(*) AS '活动参与数量' FROM activity_participants;
SELECT COUNT(*) AS '活动点赞数量' FROM activity_likes;
SELECT COUNT(*) AS '活动评论数量' FROM activity_comments;
SELECT COUNT(*) AS '用户关注数量' FROM user_follows;
SELECT COUNT(*) AS '活动浏览数量' FROM activity_views;
