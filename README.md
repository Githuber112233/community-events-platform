# 社区活动平台 (Community Events Platform)

一个现代化的社区活动管理和参与平台，帮助社区成员发现、创建和参与各种活动。

## 功能特性

### 核心功能
- 🏠 **首页展示** - 热门活动、最新活动、分类导航
- 📋 **活动列表** - 支持搜索、筛选、排序的活动浏览
- 📄 **活动详情** - 完整的活动信息展示和互动功能
- 👤 **个人中心** - 用户资料管理、活动记录查看
- ✍️ **活动发布** - 直观的活动创建和编辑界面

### 用户体验
- 📱 **响应式设计** - 完美适配桌面端和移动端
- 🎨 **现代化UI** - 基于 Tailwind CSS 的精美界面
- ⚡ **快速交互** - 使用 Vite 提供的快速开发体验
- 🔍 **智能搜索** - 支持标题和描述的关键词搜索
- 🏷️ **分类筛选** - 6大活动分类快速筛选

## 技术栈

- **框架**: React 18 + TypeScript
- **路由**: React Router v6
- **样式**: Tailwind CSS
- **构建工具**: Vite
- **图标**: Lucide React
- **开发工具**: ESLint

## 项目结构

```
Claw/
├── src/
│   ├── components/        # 可复用组件
│   │   ├── Navbar.tsx    # 导航栏
│   │   ├── Footer.tsx    # 页脚
│   │   ├── Layout.tsx    # 布局组件
│   │   ├── EventCard.tsx # 活动卡片
│   │   └── CategoryCard.tsx # 分类卡片
│   ├── pages/            # 页面组件
│   │   ├── Home.tsx      # 首页
│   │   ├── Events.tsx    # 活动列表
│   │   ├── EventDetail.tsx # 活动详情
│   │   ├── Profile.tsx   # 个人中心
│   │   └── CreateEvent.tsx # 发布活动
│   ├── types/            # TypeScript 类型定义
│   │   └── index.ts
│   ├── utils/            # 工具函数
│   │   ├── cn.ts         # 类名合并工具
│   │   └── mockData.ts   # 模拟数据
│   ├── App.tsx           # 主应用组件
│   ├── main.tsx          # 应用入口
│   └── index.css         # 全局样式
├── public/               # 静态资源
├── index.html            # HTML 模板
├── package.json          # 项目配置
├── tsconfig.json         # TypeScript 配置
├── tailwind.config.js    # Tailwind CSS 配置
└── vite.config.ts        # Vite 配置
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 `http://localhost:5173` 查看应用。

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 页面说明

### 首页 (/)
- 展示平台特色和核心价值
- 活动分类导航
- 热门活动推荐
- 最新活动展示

### 活动列表 (/events)
- 全部活动展示
- 关键词搜索
- 分类筛选
- 时间/参与人数排序

### 活动详情 (/events/:id)
- 完整活动信息
- 组织者信息
- 参与人数进度
- 评论互动功能
- 相似活动推荐

### 个人中心 (/profile)
- 用户资料展示
- 发布的活动
- 参加的活动
- 收藏的活动

### 发布活动 (/create-event)
- 活动基本信息表单
- 时间和地点设置
- 参与人数限制
- 图片上传功能

## 自定义配置

### 修改主题颜色

编辑 `tailwind.config.js` 中的 `theme.extend.colors` 部分：

```javascript
theme: {
  extend: {
    colors: {
      primary: {
        // 自定义主色调
      },
      secondary: {
        // 自定义辅助色
      }
    }
  }
}
```

### 修改模拟数据

编辑 `src/utils/mockData.ts` 文件来自定义活动和用户数据。

## 后续开发建议

### 后端集成
- 接入真实的后端 API
- 实现用户认证和授权
- 添加活动报名功能
- 实现评论和点赞功能

### 功能增强
- 添加活动收藏功能
- 实现活动提醒和通知
- 添加活动评价系统
- 支持活动图片上传到云存储

### 性能优化
- 实现图片懒加载
- 添加虚拟滚动
- 优化首屏加载速度

## 浏览器支持

- Chrome (最新版)
- Firefox (最新版)
- Safari (最新版)
- Edge (最新版)

## License

MIT

## 联系方式

如有问题或建议，欢迎提交 Issue。
