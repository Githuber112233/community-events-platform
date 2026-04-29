# 邻里 - 社区活动平台 (Community Events Platform)

一个现代化的社区活动管理和参与平台，帮助社区成员发现、创建和参与各种兴趣活动。

## 功能特性

### 核心功能
- 🏠 **首页展示** - 热门活动、3D轮盘推荐、分类导航
- 📋 **活动列表** - 支持搜索、筛选、排序的活动浏览
- 📄 **活动详情** - 完整的活动信息展示、评论互动、报名签到
- 👤 **个人中心** - 用户资料管理、活动记录查看、关注/粉丝
- ✍️ **活动发布** - 直观的活动创建和编辑界面
- 🎯 **智能推荐** - 基于内容 + 用户协同过滤的混合推荐算法

### 管理后台
- 📊 **数据统计** - 用户/活动/参与等核心指标
- ✅ **活动审核** - 管理员审核用户发布的活动
- 👥 **用户管理** - 用户封禁/解封、用户画像查看
- 🏷️ **兴趣标签管理** - 6大活动分类标签

### 用户体验
- 📱 **响应式设计** - 完美适配桌面端和移动端
- 🎨 **现代化UI** - 基于 Tailwind CSS 的精美界面
- ⚡ **快速交互** - Vite 5 提供极速开发体验
- 🔍 **智能搜索** - 支持标题和描述的关键词搜索
- 🏷️ **分类筛选** - 运动健身/文化艺术/科技创新/社交聚会/美食探店/户外探险

## 技术栈

### 前端
- **框架**: Vue 3 + TypeScript + Composition API
- **路由**: Vue Router 4 (History 模式)
- **样式**: Tailwind CSS (自定义 primary/secondary 色系)
- **构建工具**: Vite 5
- **图标**: Lucide Vue Next

### 后端
- **框架**: Spring Boot 3.2.3
- **安全**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **缓存**: Redis
- **数据库**: MySQL 8
- **构建工具**: Maven

## 项目结构

```
Claw/
├── src/                          # 前端源码 (Vue 3)
│   ├── components/               # 可复用组件
│   │   ├── Navbar.vue            # 顶部导航栏
│   │   ├── Footer.vue            # 页脚
│   │   ├── Layout.vue            # 布局容器
│   │   ├── EventCard.vue         # 活动卡片
│   │   ├── CategoryCard.vue      # 分类卡片
│   │   ├── RecommendationWheel.vue # 3D轮盘推荐
│   │   └── InterestSelectModal.vue  # 兴趣选择弹窗
│   ├── pages/                    # 页面组件
│   │   ├── Home.vue              # 首页
│   │   ├── Events.vue            # 活动列表
│   │   ├── EventDetail.vue       # 活动详情
│   │   ├── Profile.vue           # 个人中心
│   │   ├── CreateEvent.vue       # 发布活动
│   │   ├── EditEvent.vue         # 编辑活动
│   │   ├── Login.vue             # 登录
│   │   ├── Register.vue          # 注册
│   │   ├── Admin.vue             # 管理后台
│   │   └── FriendsList.vue       # 好友列表
│   ├── types/
│   │   └── index.ts              # TypeScript 类型定义
│   ├── utils/
│   │   ├── api.ts                # API 请求封装
│   │   └── mockData.ts           # 模拟数据
│   ├── App.vue                   # 根组件
│   ├── main.ts                   # 应用入口 & 路由配置
│   └── index.css                 # 全局样式
├── backend/                      # 后端源码 (Spring Boot)
│   └── src/main/java/com/community/activityplatform/
│       ├── controller/           # 控制层 (6个)
│       │   ├── AuthController.java
│       │   ├── ActivityController.java
│       │   ├── CommentController.java
│       │   ├── UserController.java
│       │   ├── AdminController.java
│       │   └── InterestController.java
│       ├── service/              # 服务层 (6个)
│       │   ├── UserService.java
│       │   ├── ActivityService.java
│       │   ├── CommentService.java
│       │   ├── RecommendationService.java  # 混合推荐算法
│       │   ├── AdminService.java
│       │   └── InterestService.java
│       ├── entity/               # 实体类 (10张表)
│       ├── dto/                  # 数据传输对象
│       ├── repository/           # 数据访问层
│       ├── security/             # JWT安全配置
│       ├── config/               # 配置类
│       └── util/                 # 工具类
├── index.html                    # HTML 模板
├── package.json                  # 前端依赖配置
├── pom.xml                       # 后端依赖配置
└── vite.config.ts                # Vite 配置
```

## 数据库设计

| 表名 | 用途 |
|------|------|
| users | 用户信息（含角色/状态/积分） |
| activities | 活动信息（8种状态枚举） |
| activity_participants | 参与记录（含签到） |
| activity_likes | 活动点赞 |
| activity_comments | 评论（树形回复） |
| comment_likes | 评论点赞 |
| interests | 兴趣标签（6大分类） |
| user_interests | 用户兴趣关联 |
| user_follows | 关注关系 |
| activity_views | 浏览记录 |

## 推荐算法

采用 **Content-Based (CB) + User-CF 混合推荐算法**：

- **CB模块**：标签余弦相似度 + 活动质量分 + 时间衰减
- **User-CF模块**：杰卡德相似度 + Top-30相似用户协同
- **融合策略**：行为数据少→CB主导，行为数据多→CF权重提升（最高60%）
- **缓存**：Redis 缓存推荐结果（1小时过期）

## 快速开始

### 前端

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

访问 `http://localhost:5173` 查看应用。

### 后端

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean package -DskipTests

# 启动服务
java -jar target/activity-platform-1.0.0.jar
```

后端服务运行在 `http://localhost:8080`。

### 数据库

1. 创建 MySQL 数据库：`CREATE DATABASE activity_platform;`
2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息
3. 启动后端服务，JPA 会自动建表
4. DataInitializer 会自动初始化测试数据

#### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | zhangsan | 123456 |
| 普通用户 | lisi | 123456 |
| 普通用户 | wangwu | 123456 |

## 页面说明

### 首页 (`/`)
- Hero 区域展示平台特色
- 3D 轮盘推荐活动（支持左右滑动 + 换一换）
- 活动分类导航（6大分类）
- 热门活动 + 最新活动

### 活动列表 (`/events`)
- 全部活动展示
- 关键词搜索 + 分类筛选 + 排序
- 分页加载

### 活动详情 (`/events/:id`)
- 完整活动信息
- 组织者信息
- 参与人数进度
- 评论互动（支持回复）
- 签到功能（仅创建者）

### 个人中心 (`/profile`)
- 用户资料展示与编辑
- 关注/粉丝列表
- 发布/参与/收藏的活动记录

### 管理后台 (`/admin`)
- 数据统计概览
- 活动审核
- 用户管理（封禁/解封）
- 用户画像查看

## 浏览器支持

- Chrome (最新版)
- Firefox (最新版)
- Safari (最新版)
- Edge (最新版)

## License

MIT

## 联系方式

如有问题或建议，欢迎提交 Issue。
