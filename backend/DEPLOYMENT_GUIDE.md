# 社区活动平台后端 - 详细部署运行指南

## 📋 目录
1. [环境准备](#环境准备)
2. [MySQL数据库安装与配置](#mysql数据库安装与配置)
3. [Redis安装与配置](#redis安装与配置)
4. [Java和Maven安装](#java和maven安装)
5. [VS Code环境配置](#vs-code环境配置)
6. [数据库初始化](#数据库初始化)
7. [后端项目配置](#后端项目配置)
8. [运行后端项目](#运行后端项目)
9. [测试API接口](#测试api接口)
10. [常见问题解决](#常见问题解决)

---

## 环境准备

### 需要安装的软件

在开始之前,请确保你的电脑上已经安装了以下软件:

1. **MySQL 8.0+** (数据库)
2. **Redis 6.0+** (缓存)
3. **JDK 17+** (Java开发环境)
4. **Maven 3.6+** (项目构建工具)
5. **VS Code** (代码编辑器)

---

## MySQL数据库安装与配置

### 第1步:检查MySQL是否已安装

打开命令提示符(CMD)或PowerShell,输入以下命令:

```bash
mysql --version
```

**如果看到类似以下输出,说明MySQL已安装:**
```
mysql  Ver 8.0.33 for Win64 on x86_64 (MySQL Community Server - GPL)
```

**如果提示"mysql不是内部或外部命令",则需要安装MySQL。**

### 第2步:下载安装MySQL(如果未安装)

#### Windows系统:

1. 访问MySQL官网下载页面: https://dev.mysql.com/downloads/mysql/

2. 点击"Download"下载Windows版本

3. 运行下载的安装程序

4. 安装步骤:
   - 选择安装类型: "Developer Default"
   - 点击"Next"
   - 点击"Execute"安装必需组件
   - 配置MySQL Server:
     - 设置Root密码: 建议使用 `123456` (方便测试)
     - 其他选项保持默认
   - 点击"Next"完成安装

5. 安装完成后,点击"Finish"

### 第3步:启动MySQL服务

**方法1: 通过服务管理器**
1. 按 `Win + R`,输入 `services.msc`
2. 找到 `MySQL80` 或 `MySQL` 服务
3. 右键点击,选择"启动"

**方法2: 通过命令行**
```bash
# 以管理员身份运行CMD或PowerShell
net start mysql80
# 或者
net start mysql
```

### 第4步:登录MySQL测试

打开命令提示符,输入:

```bash
mysql -u root -p
```

输入你设置的密码(例如: `123456`),如果成功登录会看到:

```
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 8
Server version: 8.0.33 MySQL Community Server - GPL

Copyright (c) 2000, 2024, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
```

输入 `exit` 退出MySQL。

**✅ MySQL配置完成!**

---

## Redis安装与配置

### 第1步:检查Redis是否已安装

在命令提示符中输入:

```bash
redis-cli --version
```

**如果看到版本号,说明Redis已安装。**

**如果提示命令不存在,则需要安装Redis。**

### 第2步:下载安装Redis(Windows)

#### Windows系统安装步骤:

**方法1: 使用Chocolatey(推荐,如果你已安装)**

```bash
# 以管理员身份运行PowerShell
choco install redis-64
```

**方法2: 手动下载安装**

1. 访问GitHub下载页面: https://github.com/tporadowski/redis/releases

2. 下载最新版本的 `.zip` 文件,例如: `Redis-x64-5.0.14.1.zip`

3. 解压到某个目录,例如: `C:\redis`

4. 打开解压后的文件夹,找到 `redis-server.exe`

### 第3步:启动Redis服务

**方法1: 手动启动(开发环境推荐)**

1. 打开Redis安装目录,例如: `C:\redis`
2. 双击运行 `redis-server.exe`
3. 会看到一个黑色窗口,显示Redis已启动:

```
[12345] 01 Jan 00:00:00.000 # Server started
[12345] 01 Jan 00:00:00.000 * The server is now ready to accept connections on port 6379
```

**方法2: 通过命令行启动**

```bash
# 进入Redis目录
cd C:\redis

# 启动Redis
redis-server.exe
```

**方法3: 安装为Windows服务**

```bash
# 以管理员身份运行CMD
cd C:\redis

# 安装为服务
redis-server.exe --service-install

# 启动服务
redis-server.exe --service-start

# 停止服务
redis-server.exe --service-stop

# 卸载服务
redis-server.exe --service-uninstall
```

### 第4步:测试Redis连接

打开新的命令提示符窗口,输入:

```bash
# 进入Redis目录
cd C:\redis

# 连接Redis客户端
redis-cli.exe
```

连接成功后会看到:

```
127.0.0.1:6379>
```

输入测试命令:

```bash
# 设置键值
set test "hello world"

# 获取值
get test

# 退出
exit
```

如果显示 `hello world`,说明Redis运行正常。

**✅ Redis配置完成!**

---

## Java和Maven安装

### 第1步:检查Java是否已安装

在命令提示符中输入:

```bash
java -version
```

**推荐看到类似以下输出(JDK 17或更高版本):**
```
java version "17.0.10" 2024-01-16 LTS
Java(TM) SE Runtime Environment (build 17.0.10+8-LTS-263)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.10+8-LTS-263, mixed mode)
```

**如果版本低于17或未安装,需要安装JDK 17。**

### 第2步:下载安装JDK 17(如果未安装)

1. 访问Oracle JDK下载页面: https://www.oracle.com/java/technologies/downloads/#java17

2. 选择Windows x64 Installer,点击下载

3. 运行安装程序,按照提示完成安装

4. 安装完成后,配置环境变量:
   - 右键"此电脑" -> "属性" -> "高级系统设置" -> "环境变量"
   - 在"系统变量"中新建:
     - 变量名: `JAVA_HOME`
     - 变量值: `C:\Program Files\Java\jdk-17` (根据实际安装路径调整)
   - 在"系统变量"中找到"Path",点击"编辑"
   - 点击"新建",添加: `%JAVA_HOME%\bin`
   - 点击"确定"保存

5. 重新打开命令提示符,输入 `java -version` 验证

### 第3步:检查Maven是否已安装

在命令提示符中输入:

```bash
mvn -version
```

**推荐看到类似以下输出:**
```
Apache Maven 3.9.6 (586e050571849e585819e22c9080999e04b0062a)
Maven home: C:\Program Files\Apache\Maven\apache-maven-3.9.6
Java version: 17.0.10, vendor: Oracle Corporation, ...
Default locale: zh_CN, platform encoding: GBK
OS name: "Windows 10", version: "10.0", arch: "amd64"
```

**如果提示命令不存在,需要安装Maven。**

### 第4步:下载安装Maven(如果未安装)

1. 访问Maven官网下载页面: https://maven.apache.org/download.cgi

2. 下载 `apache-maven-3.9.6-bin.zip` (或最新版本)

3. 解压到某个目录,例如: `C:\Program Files\Apache\Maven`

4. 配置环境变量:
   - 右键"此电脑" -> "属性" -> "高级系统设置" -> "环境变量"
   - 在"系统变量"中新建:
     - 变量名: `MAVEN_HOME`
     - 变量值: `C:\Program Files\Apache\Maven\apache-maven-3.9.6`
   - 在"系统变量"中找到"Path",点击"编辑"
   - 点击"新建",添加: `%MAVEN_HOME%\bin`
   - 点击"确定"保存

5. 重新打开命令提示符,输入 `mvn -version` 验证

**✅ Java和Maven配置完成!**

---

## VS Code环境配置

### 第1步:安装VS Code

如果还没有安装,访问: https://code.visualstudio.com/

点击下载,运行安装程序,按照提示完成安装。

### 第2步:安装VS Code插件

打开VS Code,点击左侧的扩展图标(或按 `Ctrl+Shift+X`),搜索并安装以下插件:

1. **Extension Pack for Java** (Java开发必备)
   - 搜索: `Extension Pack for Java`
   - 点击"Install"

2. **Spring Boot Extension Pack** (Spring Boot开发)
   - 搜索: `Spring Boot Extension Pack`
   - 点击"Install"

3. **Spring Boot Dashboard** (Spring Boot项目管理)
   - 搜索: `Spring Boot Dashboard`
   - 点击"Install"

4. **MySQL** (数据库管理)
   - 搜索: `MySQL`
   - 点击"Install"

5. **Redis for VS Code** (Redis管理)
   - 搜索: `Redis for VS Code`
   - 点击"Install"

### 第3步:打开项目

1. 打开VS Code

2. 点击"文件" -> "打开文件夹"

3. 选择你的项目目录: `c:/Users/17964/WorkBuddy/Claw/backend`

4. 点击"选择文件夹"

### 第4步:配置VS Code

VS Code会自动识别Java项目并开始构建。如果提示"工作区被信任",点击"信任工作区"。

**✅ VS Code环境配置完成!**

---

## 数据库初始化

### 第1步:打开MySQL命令行

打开命令提示符(CMD),输入:

```bash
mysql -u root -p
```

输入你的MySQL密码(例如: `123456`)

### 第2步:创建数据库

在MySQL命令行中输入以下SQL命令(一行一行执行):

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS community_activity CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE community_activity;

-- 查看数据库
SHOW DATABASES;
```

**你应该能看到 `community_activity` 数据库已创建。**

### 第3步:执行初始化脚本

有三种方法执行初始化脚本:

#### 方法1:在VS Code中执行(推荐)

1. 在VS Code中打开文件: `backend/src/main/resources/init-data.sql`

2. 确保已安装MySQL插件

3. 点击SQL文件右上角的"Execute SQL Script"图标(或右键点击文件,选择"Execute SQL Script")

4. 在弹出的对话框中,输入MySQL连接信息:
   - Host: `localhost`
   - Port: `3306`
   - User: `root`
   - Password: `123456` (你的MySQL密码)
   - Database: `community_activity`

5. 点击"Connect"执行脚本

6. 查看输出结果,确认所有数据插入成功

#### 方法2:通过命令行执行

```bash
# 进入后端项目目录
cd c:/Users/17964/WorkBuddy/Claw/backend

# 执行初始化脚本
mysql -u root -p community_activity < src/main/resources/init-data.sql
```

输入MySQL密码后,脚本会自动执行。

#### 方法3:在MySQL命令行中执行

```sql
-- 在MySQL命令行中执行
source c:/Users/17964/WorkBuddy/Claw/backend/src/main/resources/init-data.sql;
```

### 第4步:验证数据是否插入成功

在MySQL命令行中输入:

```sql
USE community_activity;

-- 查看用户表
SELECT COUNT(*) AS '用户数量' FROM users;

-- 查看兴趣标签
SELECT COUNT(*) AS '兴趣标签数量' FROM interests;

-- 查看活动
SELECT COUNT(*) AS '活动数量' FROM activities;

-- 查看活动参与
SELECT COUNT(*) AS '活动参与数量' FROM activity_participants;

-- 查看活动点赞
SELECT COUNT(*) AS '活动点赞数量' FROM activity_likes;

-- 查看活动评论
SELECT COUNT(*) AS '活动评论数量' FROM activity_comments;

-- 查看用户关注
SELECT COUNT(*) AS '用户关注数量' FROM user_follows;

-- 查看活动浏览
SELECT COUNT(*) AS '活动浏览数量' FROM activity_views;

-- 查看所有表
SHOW TABLES;
```

**你应该看到类似以下的统计结果:**
```
+----------+
| 用户数量 |
+----------+
|        5 |
+----------+

+--------------+
| 兴趣标签数量 |
+--------------+
|           50 |
+--------------+

+----------+
| 活动数量 |
+----------+
|       10 |
+----------+

... (其他表的统计数据)
```

### 第5步:查看示例数据

```sql
-- 查看所有用户
SELECT id, username, nickname, email, city, district FROM users;

-- 查看所有兴趣标签
SELECT id, name, category FROM interests LIMIT 10;

-- 查看所有活动
SELECT id, title, city, district, status, start_time FROM activities;

-- 查看用户兴趣标签关联
SELECT u.username, i.name, ui.weight, ui.click_count, ui.participate_count
FROM users u
JOIN user_interests ui ON u.id = ui.user_id
JOIN interests i ON ui.interest_id = i.id
LIMIT 10;

-- 查看活动参与记录
SELECT u.username, a.title, ap.status, ap.created_at
FROM activity_participants ap
JOIN users u ON ap.user_id = u.id
JOIN activities a ON ap.activity_id = a.id;

-- 查看活动点赞记录
SELECT u.username, a.title, al.created_at
FROM activity_likes al
JOIN users u ON al.user_id = u.id
JOIN activities a ON al.activity_id = a.id;

-- 查看活动评论
SELECT u.username, a.title, ac.content, ac.created_at
FROM activity_comments ac
JOIN users u ON ac.user_id = u.id
JOIN activities a ON ac.activity_id = a.id;

-- 查看用户关注关系
SELECT f.username AS '关注者', fd.username AS '被关注者', uf.created_at
FROM user_follows uf
JOIN users f ON uf.follower_id = f.id
JOIN users fd ON uf.followed_id = fd.id;
```

输入 `exit` 退出MySQL。

**✅ 数据库初始化完成!**

---

## 后端项目配置

### 第1步:打开配置文件

在VS Code中打开文件: `backend/src/main/resources/application.yml`

### 第2步:检查数据库配置

找到以下配置部分,确认或修改:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_activity?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456  # 修改为你的MySQL密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**说明:**
- `url`: 数据库连接地址
- `username`: MySQL用户名(通常是root)
- `password`: MySQL密码(需要修改为你自己的密码)

### 第3步:检查Redis配置

找到以下配置部分:

```yaml
  data:
    redis:
      host: localhost
      port: 6379
      password:    # 如果Redis没有设置密码,留空即可
      database: 0
```

**说明:**
- `host`: Redis服务器地址(本地是localhost)
- `port`: Redis端口(默认6379)
- `password`: Redis密码(如果有的话)

### 第4步:检查JWT配置

找到以下配置部分:

```yaml
jwt:
  secret: community-activity-platform-secret-key-2024-very-long-and-secure
  expiration: 86400000      # 24小时(毫秒)
  refresh-expiration: 604800000  # 7天
```

**说明:**
- `secret`: JWT密钥(生产环境需要修改为更复杂的字符串)
- `expiration`: Token有效期(毫秒)
- `refresh-expiration`: 刷新Token有效期

### 第5步:检查推荐算法配置

找到以下配置部分:

```yaml
recommendation:
  cf-enable-threshold: 5   # 行为数量超过此值才使用CF
  top-k-users: 20          # 相似用户数量
  top-n-activities: 10     # 推荐活动数量
  time-decay-factor: 0.01  # 时间衰减系数
  cache-ttl: 1800         # 缓存有效期(秒)
```

**说明:**
- 这些参数会影响推荐算法的效果,可以根据需要调整

### 第6步:保存配置文件

按 `Ctrl + S` 保存配置文件。

**✅ 后端项目配置完成!**

---

## 运行后端项目

有几种方法可以运行后端项目,推荐使用VS Code直接运行。

### 方法1:在VS Code中运行(推荐)

#### 第1步:确保所有服务已启动

1. 确认MySQL服务正在运行:
   ```bash
   # 在命令行中检查
   mysql -u root -p -e "SELECT 1;"
   ```

2. 确认Redis服务正在运行:
   ```bash
   # 在命令行中检查
   redis-cli ping
   # 应该返回: PONG
   ```

#### 第2步:在VS Code中打开Spring Boot Dashboard

1. 在VS Code左侧栏,找到"Spring Boot Dashboard"图标(通常是一个小叶子图标)

2. 如果没有看到,按 `Ctrl+Shift+P`,输入"Spring Boot Dashboard",点击打开

#### 第3步:启动项目

1. 在Spring Boot Dashboard中,找到 `CommunityActivityPlatformApplication`

2. 点击右侧的"Play"按钮(▶)启动项目

3. 或者右键点击项目,选择"Run"

#### 第4步:查看运行日志

在VS Code底部的"输出"窗口中,你会看到类似以下的日志:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.3)

2024-03-18 10:00:00.000  INFO 12345 --- [main] c.c.a.CommunityActivityPlatformApplication : Starting CommunityActivityPlatformApplication using Java 17.0.10
2024-03-18 10:00:00.123  INFO 12345 --- [main] c.c.a.CommunityActivityPlatformApplication : The following profiles are active: default
2024-03-18 10:00:01.456  INFO 12345 --- [main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
...
2024-03-18 10:00:05.789  INFO 12345 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path '/api'
2024-03-18 10:00:05.790  INFO 12345 --- [main] c.c.a.CommunityActivityPlatformApplication : Started CommunityActivityPlatformApplication in 6.123 seconds (JVM running for 7.456)

========================================
社区活动平台后端服务启动成功!
API地址: http://localhost:8080/api
========================================
```

**✅ 项目启动成功!**

### 方法2:使用Maven命令行运行

#### 第1步:打开终端

在VS Code中,按 `Ctrl + ~` 打开终端,或点击菜单"终端" -> "新建终端"

#### 第2步:进入后端目录

```bash
cd c:/Users/17964/WorkBuddy/Claw/backend
```

#### 第3步:编译项目

```bash
mvn clean compile
```

这个过程会下载所有依赖包,第一次运行可能需要几分钟。看到以下信息表示编译成功:

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

#### 第4步:运行项目

```bash
mvn spring-boot:run
```

你会看到与VS Code中相同的启动日志。

### 方法3:打包后运行

#### 第1步:编译打包

```bash
mvn clean package
```

#### 第2步:运行jar包

```bash
cd target
java -jar activity-platform-1.0.0.jar
```

### 停止项目

**在VS Code中停止:**
1. 在Spring Boot Dashboard中,找到 `CommunityActivityPlatformApplication`
2. 点击右侧的"Stop"按钮(⏹)
3. 或者右键点击项目,选择"Stop"

**在命令行中停止:**
- 按 `Ctrl + C` 停止运行

**✅ 后端项目运行配置完成!**

---

## 测试API接口

项目启动成功后,我们可以测试一些API接口。

### 方法1:使用浏览器测试(GET请求)

#### 1. 获取所有兴趣标签

在浏览器中打开:

```
http://localhost:8080/api/interests
```

**你应该看到类似以下的JSON响应:**

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
    {
      "id": 2,
      "name": "足球",
      "description": "足球运动及相关活动",
      "category": "运动健身",
      "popularity": 45,
      "active": true
    },
    ...
  ]
}
```

#### 2. 获取热门活动

```
http://localhost:8080/api/activities/popular?page=0&size=5
```

#### 3. 获取活动详情

```
http://localhost:8080/api/activities/1
```

### 方法2:使用Postman测试(所有请求)

#### 1. 用户登录

- **方法**: POST
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**:
  ```
  Content-Type: application/json
  ```
- **Body**(选择raw,JSON格式):
  ```json
  {
    "username": "user1",
    "password": "123456"
  }
  ```

- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "user": {
        "id": 2,
        "username": "user1",
        "nickname": "张三",
        "email": "zhangsan@example.com",
        ...
      }
    }
  }
  ```

- **复制Token** (后续需要用到)

#### 2. 获取当前用户信息

- **方法**: GET
- **URL**: `http://localhost:8080/api/users/me`
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```

#### 3. 获取推荐活动

- **方法**: GET
- **URL**: `http://localhost:8080/api/activities/recommended?page=0&size=5`
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```

#### 4. 创建活动

- **方法**: POST
- **URL**: `http://localhost:8080/api/activities`
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json
  ```
- **Body**:
  ```json
  {
    "title": "周末徒步活动",
    "description": "周末组织户外徒步活动",
    "city": "上海市",
    "district": "浦东新区",
    "address": "世纪公园",
    "startTime": "2024-03-25 09:00:00",
    "endTime": "2024-03-25 17:00:00",
    "registrationDeadline": "2024-03-24 18:00:00",
    "maxParticipants": 30,
    "fee": "免费",
    "contactPhone": "13800138002"
  }
  ```

#### 5. 参与活动

- **方法**: POST
- **URL**: `http://localhost:8080/api/activities/1/participate`
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json
  ```
- **Body**:
  ```json
  {
    "message": "我非常想参加这个活动!"
  }
  ```

#### 6. 点赞活动

- **方法**: POST
- **URL**: `http://localhost:8080/api/activities/1/like`
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  ```

#### 7. 发表评论

- **方法**: POST
- **URL**: `http://localhost:8080/api/comments`
- **Headers**:
  ```
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/json
  ```
- **Body**:
  ```json
  {
    "activityId": 1,
    "content": "这个活动看起来很棒,期待参加!"
  }
  ```

### 方法3:使用VS Code REST Client插件测试

#### 1.安装REST Client插件

在VS Code中搜索并安装 `REST Client` 插件

#### 2.创建测试文件

在项目根目录创建 `test.http` 文件:

```http
### 用户登录
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "123456"
}

### 获取兴趣标签
GET http://localhost:8080/api/interests

### 获取热门活动
GET http://localhost:8080/api/activities/popular?page=0&size=5

### 获取活动详情
GET http://localhost:8080/api/activities/1
```

#### 3.执行测试

在每个请求上方的 "Send Request" 链接上点击,即可发送请求。

**✅ API测试完成!**

---

## 常见问题解决

### 问题1:连接MySQL失败

**错误信息:**
```
Communications link failure
```

**解决方法:**

1. 确认MySQL服务是否启动:
   ```bash
   # Windows
   net start mysql80
   ```

2. 确认MySQL端口是否正确:
   ```bash
   # 查看MySQL监听的端口
   netstat -an | findstr 3306
   ```

3. 确认用户名和密码是否正确:
   ```bash
   mysql -u root -p
   ```

4. 检查防火墙设置

### 问题2:连接Redis失败

**错误信息:**
```
Unable to connect to Redis
```

**解决方法:**

1. 确认Redis服务是否启动:
   ```bash
   redis-cli ping
   ```

2. 如果提示连接失败,启动Redis:
   ```bash
   redis-server
   ```

3. 确认Redis端口是否正确:
   ```bash
   netstat -an | findstr 6379
   ```

### 问题3:端口被占用

**错误信息:**
```
Web server failed to start. Port 8080 was already in use.
```

**解决方法:**

**方法1:修改端口**

在 `application.yml` 中修改:
```yaml
server:
  port: 8081  # 改为其他端口
```

**方法2:停止占用端口的程序**

```bash
# 查看占用8080端口的进程
netstat -ano | findstr 8080

# 找到PID,结束进程
taskkill /PID <进程ID> /F
```

### 问题4:编译失败

**错误信息:**
```
BUILD FAILURE
```

**解决方法:**

1. 清理Maven缓存:
   ```bash
   mvn clean
   ```

2. 删除 `.m2` 目录下的缓存:
   ```bash
   # Windows
   rmdir /s /q %USERPROFILE%\.m2\repository
   ```

3. 重新编译:
   ```bash
   mvn clean install -U
   ```

### 问题5:依赖下载失败

**错误信息:**
```
Could not resolve dependencies
```

**解决方法:**

1. 检查网络连接

2. 配置Maven镜像源(阿里云镜像):

   打开 `%USERPROFILE%\.m2\settings.xml` (如果没有则创建),添加:

   ```xml
   <mirrors>
     <mirror>
       <id>aliyunmaven</id>
       <mirrorOf>*</mirrorOf>
       <name>阿里云公共仓库</name>
       <url>https://maven.aliyun.com/repository/public</url>
     </mirror>
   </mirrors>
   ```

3. 重新下载依赖:
   ```bash
   mvn clean install -U
   ```

### 问题6:Java版本不匹配

**错误信息:**
```
java.lang.UnsupportedClassVersionError: class file has wrong version
```

**解决方法:**

1. 检查Java版本:
   ```bash
   java -version
   ```

2. 确保是JDK 17或更高版本

3. 如果不是,下载安装JDK 17:
   https://www.oracle.com/java/technologies/downloads/#java17

4. 配置JAVA_HOME环境变量

### 问题7:数据库表不存在

**错误信息:**
```
Table 'community_activity.users' doesn't exist
```

**解决方法:**

1. 确认数据库已创建:
   ```sql
   SHOW DATABASES;
   ```

2. 确认已执行初始化脚本:
   ```bash
   mysql -u root -p community_activity < src/main/resources/init-data.sql
   ```

3. 或者修改 `application.yml` 中的配置,让JPA自动创建表:
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: update  # 自动更新表结构
   ```

### 问题8:JWT Token验证失败

**错误信息:**
```
未授权,请先登录
```

**解决方法:**

1. 确认Token格式正确:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

   注意 `Bearer` 后面有个空格

2. 确认Token未过期:
   - 默认有效期24小时
   - 过期后需要重新登录

3. 检查JWT配置:
   ```yaml
   jwt:
     secret: community-activity-platform-secret-key-2024-very-long-and-secure
     expiration: 86400000  # 24小时
   ```

---

## 🎉 完成检查清单

在完成所有步骤后,你可以对照以下清单检查:

### 环境检查
- [ ] MySQL已安装并运行
- [ ] Redis已安装并运行
- [ ] JDK 17已安装
- [ ] Maven已安装
- [ ] VS Code已安装并配置好插件

### 数据库检查
- [ ] community_activity数据库已创建
- [ ] init-data.sql已执行
- [ ] 用户表有5条记录
- [ ] 兴趣标签表有50条记录
- [ ] 活动表有10条记录
- [ ] 其他表有示例数据

### 项目检查
- [ ] application.yml配置正确
- [ ] MySQL连接信息正确
- [ ] Redis连接信息正确
- [ ] 项目可以成功编译
- [ ] 项目可以成功启动

### 功能检查
- [ ] 可以通过浏览器访问 http://localhost:8080/api/interests
- [ ] 可以通过Postman登录获取Token
- [ ] 可以使用Token访问需要认证的接口
- [ ] 可以创建活动
- [ ] 可以参与活动
- [ ] 可以点赞活动
- [ ] 可以发表评论

---

## 📞 需要帮助?

如果在部署过程中遇到任何问题,可以:

1. 查看《IMPLEMENTATION_SUMMARY.md》了解技术细节
2. 查看《README.md》了解项目功能
3. 检查VS Code的"输出"窗口中的错误日志
4. 检查MySQL和Redis的日志
5. 在线搜索相关错误信息

---

**祝你部署顺利!🚀**
