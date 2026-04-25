# 🎯 部署步骤可视化清单

## 📋 前置检查

- [ ] 电脑已安装 Windows 10/11
- [ ] 网络连接正常
- [ ] 有管理员权限

---

## 🗂️ 第一阶段:软件安装(30-60分钟)

### 1.1 安装MySQL
```
□ 下载 MySQL Installer
□ 运行安装程序
□ 选择 "Developer Default"
□ 设置 Root 密码: 123456
□ 完成安装
□ 验证安装: mysql --version
```

**验证命令:**
```bash
mysql -u root -p
# 输入密码: 123456
# 看到 "Welcome to the MySQL monitor" 表示成功
```

### 1.2 安装Redis
```
□ 下载 Redis-x64-5.0.14.1.zip
□ 解压到 C:\redis
□ 双击 redis-server.exe
□ 看到端口6379监听信息
□ 验证安装: redis-cli ping
```

**验证命令:**
```bash
cd C:\redis
redis-cli.exe ping
# 看到 "PONG" 表示成功
```

### 1.3 安装JDK 17
```
□ 下载 JDK 17 Windows x64
□ 运行安装程序
□ 配置环境变量 JAVA_HOME
□ 配置 PATH 变量
□ 验证安装: java -version
```

**验证命令:**
```bash
java -version
# 看到 "java version 17.x.x" 表示成功
```

### 1.4 安装Maven
```
□ 下载 Maven 3.9.x zip
□ 解压到 C:\Program Files\Apache\Maven
□ 配置环境变量 MAVEN_HOME
□ 配置 PATH 变量
□ 验证安装: mvn -version
```

**验证命令:**
```bash
mvn -version
# 看到 "Apache Maven 3.x.x" 表示成功
```

### 1.5 安装VS Code
```
□ 下载 VS Code
□ 运行安装程序
□ 安装 Java 插件包
□ 安装 Spring Boot 插件包
□ 安装 MySQL 插件
□ 安装 Redis 插件
```

---

## 🗄️ 第二阶段:数据库配置(10-15分钟)

### 2.1 创建数据库
```
□ 打开命令提示符
□ 登录 MySQL: mysql -u root -p
□ 输入密码: 123456
□ 创建数据库
□ 验证数据库创建成功
□ 退出 MySQL
```

**SQL命令:**
```sql
CREATE DATABASE IF NOT EXISTS community_activity
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

SHOW DATABASES;
-- 应该看到 community_activity
```

### 2.2 执行初始化脚本
```
□ 打开 VS Code
□ 打开 init-data.sql 文件
□ 点击 "Execute SQL Script"
□ 输入连接信息
  - Host: localhost
  - Port: 3306
  - User: root
  - Password: 123456
  - Database: community_activity
□ 点击 Connect
□ 等待执行完成
□ 查看统计信息
```

**预期结果:**
```
✓ 用户数量: 5
✓ 兴趣标签数量: 50
✓ 活动数量: 10
✓ 活动参与数量: 12
✓ 活动点赞数量: 11
✓ 活动评论数量: 8
✓ 用户关注数量: 10
✓ 活动浏览数量: 10
```

### 2.3 验证数据
```
□ 在 MySQL 中执行查询
□ 确认用户数据存在
□ 确认兴趣标签存在
□ 确认活动数据存在
□ 退出 MySQL
```

---

## ⚙️ 第三阶段:项目配置(5-10分钟)

### 3.1 打开项目
```
□ 打开 VS Code
□ 文件 -> 打开文件夹
□ 选择: c:/Users/17964/WorkBuddy/Claw/backend
□ 点击 "选择文件夹"
□ 等待项目加载
```

### 3.2 修改配置文件
```
□ 打开 application.yml
□ 检查数据库配置
□ 修改 MySQL 密码(如果需要)
□ 检查 Redis 配置
□ 检查 JWT 配置
□ 保存文件(Ctrl+S)
```

**关键配置:**
```yaml
spring:
  datasource:
    username: root
    password: 123456  # 修改为你的密码
```

---

## 🚀 第四阶段:运行项目(5-10分钟)

### 4.1 启动服务
```
□ 启动 MySQL: net start mysql80
□ 启动 Redis: C:\redis\redis-server.exe
□ 等待两个服务都启动
```

**验证服务:**
```bash
# 验证 MySQL
mysql -u root -p -e "SELECT 1;"

# 验证 Redis
redis-cli ping
```

### 4.2 启动后端项目
```
□ 打开 Spring Boot Dashboard
□ 找到 CommunityActivityPlatformApplication
□ 点击 Play 按钮 ▶
□ 观察启动日志
□ 看到 "Started CommunityActivityPlatformApplication"
□ 看到 "社区活动平台后端服务启动成功!"
```

**启动成功的标志:**
```
========================================
社区活动平台后端服务启动成功!
API地址: http://localhost:8080/api
========================================
```

### 4.3 测试API
```
□ 打开浏览器
□ 访问: http://localhost:8080/api/interests
□ 看到 JSON 数据响应
□ 验证数据格式正确
```

**预期响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "篮球",
      "description": "篮球运动及相关活动",
      "category": "运动健身",
      "popularity": 50,
      "active": true
    },
    ...
  ]
}
```

---

## 🧪 第五阶段:功能测试(15-20分钟)

### 5.1 用户登录测试
```
□ 打开 Postman
□ 创建新请求
□ 方法: POST
□ URL: http://localhost:8080/api/auth/login
□ Headers: Content-Type: application/json
□ Body:
  {
    "username": "user1",
    "password": "123456"
  }
□ 点击 Send
□ 复制返回的 token
```

### 5.2 获取推荐活动测试
```
□ 创建新请求
□ 方法: GET
□ URL: http://localhost:8080/api/activities/recommended?page=0&size=5
□ Headers: Authorization: Bearer <刚才复制的token>
□ 点击 Send
□ 查看返回的活动列表
```

### 5.3 创建活动测试
```
□ 创建新请求
□ 方法: POST
□ URL: http://localhost:8080/api/activities
□ Headers:
  - Authorization: Bearer <token>
  - Content-Type: application/json
□ Body:
  {
    "title": "我的第一个活动",
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
□ 点击 Send
□ 确认活动创建成功
```

### 5.4 参与活动测试
```
□ 创建新请求
□ 方法: POST
□ URL: http://localhost:8080/api/activities/1/participate
□ Headers:
  - Authorization: Bearer <token>
  - Content-Type: application/json
□ Body:
  {
    "message": "我想参加这个活动!"
  }
□ 点击 Send
□ 确认参与成功
```

### 5.5 点赞活动测试
```
□ 创建新请求
□ 方法: POST
□ URL: http://localhost:8080/api/activities/1/like
□ Headers: Authorization: Bearer <token>
□ 点击 Send
□ 确认点赞成功
```

### 5.6 发表评论测试
```
□ 创建新请求
□ 方法: POST
□ URL: http://localhost:8080/api/comments
□ Headers:
  - Authorization: Bearer <token>
  - Content-Type: application/json
□ Body:
  {
    "activityId": 1,
    "content": "这个活动看起来很棒!"
  }
□ 点击 Send
□ 确认评论成功
```

---

## ✅ 第六阶段:最终检查(5分钟)

### 6.1 环境检查
```
□ MySQL 服务正在运行
□ Redis 服务正在运行
□ 后端项目正在运行
□ 浏览器可以访问 API
```

### 6.2 功能检查
```
□ 可以登录获取 Token
□ 可以获取推荐活动
□ 可以创建新活动
□ 可以参与活动
□ 可以点赞活动
□ 可以发表评论
```

### 6.3 数据检查
```
□ 打开 MySQL
□ 查询 users 表: 应该有5个用户
□ 查询 activities 表: 应该有10+个活动
□ 查询 activity_participants 表: 应该有12+条记录
□ 退出 MySQL
```

---

## 🎊 完成标志

当你完成以下所有检查项,恭喜你!部署成功! ✅

```
✓ 所有软件安装成功
✓ 数据库创建并初始化成功
✓ 项目配置正确
✓ 所有服务正常运行
✓ API 测试通过
✓ 所有核心功能可用
```

---

## 📞 遇到问题?

1. **详细指南**: 查看 `DEPLOYMENT_GUIDE.md`
2. **快速参考**: 查看 `QUICK_REFERENCE.md`
3. **实现总结**: 查看 `IMPLEMENTATION_SUMMARY.md`
4. **项目文档**: 查看 `README.md`

---

## 🎯 预计时间

- **新手(第一次部署)**: 2-3小时
- **有经验用户**: 1-1.5小时
- **熟练用户**: 30-60分钟

---

## 💡 提示

1. **按顺序执行**: 不要跳过步骤
2. **每步验证**: 完成每一步都要验证
3. **保存进度**: 遇到问题可以随时暂停
4. **记录问题**: 记录遇到的问题和解决方案
5. **多做练习**: 多次练习会更熟练

---

**祝你部署顺利!🚀**
