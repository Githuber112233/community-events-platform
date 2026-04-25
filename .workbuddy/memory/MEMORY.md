# 项目长期记忆

## 社区活动平台 (Claw)

### 项目技术栈（当前版本 - Vue）
- **前端框架**：Vue 3 + TypeScript + Vite 5
- **样式**：Tailwind CSS（primary/secondary 自定义色系）
- **路由**：Vue Router 4（history模式）
- **图标**：lucide-vue-next
- **后端**：Spring Boot 3 + JPA + JWT + Redis
- **数据库**：MySQL 8

### 前端文件结构
```
src/
  main.ts          # Vue应用入口，路由在此集中定义
  App.vue          # 根组件
  components/
    Layout.vue     # 整体布局（含Navbar/Footer）
    Navbar.vue     # 顶部导航栏
    Footer.vue     # 底部
    EventCard.vue  # 活动卡片
    CategoryCard.vue # 分类卡片
    RecommendationWheel.vue # 首页3D轮盘推荐组件
    InterestSelectModal.vue   # 兴趣选择弹窗组件
  pages/
    Home.vue / Events.vue / EventDetail.vue
    Profile.vue / CreateEvent.vue / FriendsList.vue
    Login.vue / Register.vue
  types/index.ts   # TypeScript类型（Event, User, Category）
  utils/mockData.ts # 模拟数据
```

### 新增功能（2026-04-25）
1. **首页轮盘推荐** - RecommendationWheel.vue，3D旋转轮盘展示推荐活动，支持左右滑动和"换一换"按钮刷新
2. **兴趣标签选择** - InterestSelectModal.vue，新用户首次登录弹出兴趣选择（至少选3个）
3. **好友互关功能** - FriendsList.vue（粉丝/关注列表页面），Profile.vue 添加关注按钮和粉丝/关注统计入口
4. **路由** - `/friends/:type/:userId` 好友列表页面路由
5. **管理员用户画像** - UserProfileDTO.java，完整用户画像数据结构；AdminController `/admin/users/{userId}/profile` 接口
6. **活动编辑页面** - EditEvent.vue，支持编辑已有活动
7. **后端启动脚本** - `backend/start.bat`，自动终止占用8080端口的旧Java进程

### 修复内容（2026-04-25）
1. **活动列表筛选** - 修复sort参数未传给后端的问题；ActivityController添加sort参数支持
2. **移动端适配** - 优化Events.vue筛选器和排序组件的移动端显示

### 迁移历史
- 2026-03-29：前端从 React 18 + React Router 完整迁移到 Vue 3 + Vue Router 4，保持原有 Tailwind CSS 样式完全不变

### 开发服务器
- 端口：5173（若占用则自动切换5174）
- 启动命令：`npm.cmd run dev --cache C:\Users\17964\AppData\Local\npm-cache`（注意npm缓存目录权限问题，需用用户目录）

### 数据库设计（6张核心表）
user / activity / activity_registration / activity_like / comment / category

### 最近修复（2026-04-22）
1. **头像功能**：注册时随机分配 DiceBear Pixel Art 头像
2. **管理员界面**：修复数据获取和loading状态
3. **首页热门/最新**：修复API调用逻辑，添加错误处理
4. **评论点赞**：修复API调用，添加响应处理和用户提示
5. **测试数据**：创建 DataInitializer.java 自动初始化测试数据
   - 管理员：admin/admin123
   - 测试用户：zhangsan/123456, lisi/123456, wangwu/123456
   - 8个测试活动（7个RECRUITING + 1个PENDING）
6. **后端编译错误**：DataInitializer.java 中 fee 字段是 String 类型，但传入 int；Interest 实体无 icon 字段（均已修复）

### API路径
- 评论列表：GET /api/comments/activities/{activityId}
- 发表评论：POST /api/comments
- 点赞评论：POST /api/comments/{commentId}/like
- 热门活动：GET /api/activities/popular
- 用户活动：GET /api/users/me/activities?type=created|participating|liked

### 毕设文档
- 2026-03-29：已生成中期检查评价表 Word 文档
  - 输出路径：`D:\desktop\工作实习\毕设\本科毕业论文中期检查评价表_柳鸿博.docx`
  - 包含：基本信息、进度表、已完成工作、论文进度、存在问题与计划、截图说明、指导教师评语签字栏
  - 截图需手动补充（①数据库表结构 ②Postman接口测试 ③前端系统截图 ④Network联调截图 ⑤代码量/Git记录）
