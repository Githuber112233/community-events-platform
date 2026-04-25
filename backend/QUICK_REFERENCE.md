# 快速参考卡片 - 常用命令速查

## 🚀 快速启动(假设环境已配置好)

### 1. 启动MySQL
```bash
net start mysql80
# 或
net start mysql
```

### 2. 启动Redis
```bash
cd C:\redis
redis-server.exe
```

### 3. 启动后端项目(VS Code中)
- 打开Spring Boot Dashboard
- 点击 `CommunityActivityPlatformApplication` 的 Play 按钮

### 4. 测试API
```
浏览器访问: http://localhost:8080/api/interests
```

---

## 🔧 常用命令

### MySQL相关

#### 登录MySQL
```bash
mysql -u root -p
```

#### 创建数据库
```sql
CREATE DATABASE community_activity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 使用数据库
```sql
USE community_activity;
```

#### 查看表
```sql
SHOW TABLES;
```

#### 查看数据
```sql
SELECT * FROM users LIMIT 5;
SELECT * FROM interests LIMIT 5;
SELECT * FROM activities LIMIT 5;
```

#### 执行初始化脚本
```bash
mysql -u root -p community_activity < src/main/resources/init-data.sql
```

#### 退出MySQL
```bash
exit;
```

---

### Redis相关

#### 启动Redis
```bash
redis-server.exe
```

#### 连接Redis
```bash
redis-cli.exe
```

#### 测试连接
```bash
ping
# 返回: PONG
```

#### 查看所有键
```bash
KEYS *
```

#### 删除所有键
```bash
FLUSHDB
```

#### 退出Redis
```bash
exit
```

---

### Maven相关

#### 编译项目
```bash
mvn clean compile
```

#### 运行项目
```bash
mvn spring-boot:run
```

#### 打包项目
```bash
mvn clean package
```

#### 运行jar包
```bash
java -jar target/activity-platform-1.0.0.jar
```

#### 跳过测试
```bash
mvn clean package -DskipTests
```

#### 清理项目
```bash
mvn clean
```

---

### VS Code相关

#### 打开终端
```
Ctrl + ~
```

#### 保存文件
```
Ctrl + S
```

#### 打开命令面板
```
Ctrl + Shift + P
```

#### 打开Spring Boot Dashboard
```
Ctrl + Shift + P -> 输入 "Spring Boot Dashboard"
```

---

## 📝 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | ADMIN | 管理员 |
| user1 | 123456 | USER | 张三(运动爱好者) |
| user2 | 123456 | USER | 李四(文艺青年) |
| user3 | 123456 | USER | 王五(摄影爱好者) |
| user4 | 123456 | USER | 赵六(喜欢阅读) |

---

## 🔑 常用API端点

### 公开接口(无需登录)
```
GET    /api/interests                                    # 获取所有兴趣标签
GET    /api/interests/category/{category}                # 按分类获取兴趣标签
GET    /api/activities/nearby                           # 获取附近活动
GET    /api/activities/popular                          # 获取热门活动
GET    /api/activities/{activityId}                     # 获取活动详情
GET    /api/comments/activities/{activityId}            # 获取活动评论
POST   /api/auth/login                                  # 用户登录
POST   /api/auth/register                               # 用户注册
```

### 需要登录的接口(需要Token)
```
GET    /api/users/me                                    # 获取当前用户信息
GET    /api/users/{userId}                              # 获取用户信息
PUT    /api/users/me                                    # 更新用户信息
PUT    /api/users/me/interests                          # 更新兴趣标签
GET    /api/users/me/interests                          # 获取兴趣标签
POST   /api/users/me/following/{userId}                 # 关注用户
DELETE /api/users/me/following/{userId}                 # 取消关注
GET    /api/users/me/activities                         # 获取参与的活动

POST   /api/activities                                  # 创建活动
GET    /api/activities                                  # 获取活动列表
GET    /api/activities/recommended                      # 获取推荐活动 ⭐
POST   /api/activities/{activityId}/participate        # 参与活动
DELETE /api/activities/{activityId}/participate        # 取消参与
POST   /api/activities/{activityId}/like                # 点赞活动
DELETE /api/activities/{activityId}/like                # 取消点赞

POST   /api/comments                                    # 发表评论
DELETE /api/comments/{commentId}                        # 删除评论
```

---

## 🎯 快速测试流程

### 1. 登录获取Token
```bash
# 使用Postman或curl
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "123456"
}
```

### 2. 获取推荐活动(需要Token)
```bash
GET http://localhost:8080/api/activities/recommended?page=0&size=5
Authorization: Bearer <你的Token>
```

### 3. 创建活动(需要Token)
```bash
POST http://localhost:8080/api/activities
Authorization: Bearer <你的Token>
Content-Type: application/json

{
  "title": "测试活动",
  "description": "这是一个测试活动",
  "city": "上海市",
  "district": "浦东新区",
  "address": "测试地址",
  "startTime": "2024-03-25 09:00:00",
  "endTime": "2024-03-25 17:00:00",
  "registrationDeadline": "2024-03-24 18:00:00",
  "maxParticipants": 20,
  "fee": "免费"
}
```

### 4. 参与活动(需要Token)
```bash
POST http://localhost:8080/api/activities/1/participate
Authorization: Bearer <你的Token>
Content-Type: application/json

{
  "message": "我想参加"
}
```

---

## 🛠️ 常见问题快速修复

### 问题:端口被占用
```bash
# 查看占用8080端口的进程
netstat -ano | findstr 8080

# 结束进程
taskkill /PID <进程ID> /F

# 或修改application.yml中的端口
server.port: 8081
```

### 问题:数据库连接失败
```bash
# 重启MySQL
net stop mysql80
net start mysql80

# 测试连接
mysql -u root -p
```

### 问题:Redis连接失败
```bash
# 重启Redis
redis-server.exe

# 测试连接
redis-cli.exe ping
```

### 问题:项目编译失败
```bash
# 清理并重新编译
mvn clean install -U

# 跳过测试
mvn clean package -DskipTests
```

---

## 📊 数据库统计查询

```sql
-- 查看所有统计
SELECT
  (SELECT COUNT(*) FROM users) AS '用户数',
  (SELECT COUNT(*) FROM interests) AS '兴趣标签数',
  (SELECT COUNT(*) FROM activities) AS '活动数',
  (SELECT COUNT(*) FROM activity_participants) AS '参与数',
  (SELECT COUNT(*) FROM activity_likes) AS '点赞数',
  (SELECT COUNT(*) FROM activity_comments) AS '评论数',
  (SELECT COUNT(*) FROM user_follows) AS '关注数';

-- 查看用户参与的活动
SELECT u.username, a.title, ap.status
FROM activity_participants ap
JOIN users u ON ap.user_id = u.id
JOIN activities a ON ap.activity_id = a.id;

-- 查看活动统计
SELECT
  id,
  title,
  current_participants AS '参与人数',
  like_count AS '点赞数',
  comment_count AS '评论数',
  view_count AS '浏览数'
FROM activities;
```

---

## 🔍 日志查看

### VS Code输出日志
- 查看"输出"窗口
- 选择对应的日志通道(一般会自动显示)

### 查看Spring Boot日志
- 项目启动日志
- 请求处理日志
- 错误日志

---

## 💡 提示

1. **启动顺序**: MySQL → Redis → 后端项目
2. **端口**: 后端默认8080端口
3. **Token**: 有效期24小时,过期需重新登录
4. **缓存**: 推荐结果缓存30分钟
5. **数据库**: 密码默认123456,生产环境需修改

---

**祝你使用愉快! 🎉**
