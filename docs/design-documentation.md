# 社区活动平台 - 完整技术设计文档

## 目录

1. [设计思路](#1-设计思路)
2. [实现步骤](#2-实现步骤)
3. [实现原理](#3-实现原理)
4. [后端集成方案](#4-后端集成方案)
5. [推荐算法实现](#5-推荐算法实现)
6. [论文撰写要点](#6-论文撰写要点)

---

## 1. 设计思路

### 1.1 系统架构设计

#### 1.1.1 整体架构

社区活动平台采用**前后端分离**的架构模式，遵循**分层设计**原则：

```
┌─────────────────────────────────────────────────────────┐
│                    用户界面层 (Frontend)                  │
│  React + TypeScript + Tailwind CSS                      │
├─────────────────────────────────────────────────────────┤
│                    业务逻辑层 (Backend)                   │
│  Spring Boot / Node.js                                  │
├─────────────────────────────────────────────────────────┤
│                    数据访问层 (DAO)                       │
│  MyBatis / TypeORM / Prisma                              │
├─────────────────────────────────────────────────────────┤
│                    数据存储层 (Database)                  │
│  MySQL / PostgreSQL / MongoDB                           │
├─────────────────────────────────────────────────────────┤
│                    推荐引擎层 (Recommendation)            │
│  User-CF + CB 协同算法                                   │
└─────────────────────────────────────────────────────────┘
```

#### 1.1.2 前端架构设计

采用**组件化**和**模块化**的设计理念：

```
前端架构层次
│
├─ 视图层 (View Layer)
│  ├─ 页面组件 (Pages)
│  │  ├─ Home.tsx (首页)
│  │  ├─ Events.tsx (活动列表)
│  │  ├─ EventDetail.tsx (活动详情)
│  │  ├─ Profile.tsx (个人中心)
│  │  └─ CreateEvent.tsx (发布活动)
│  │
│  └─ 可复用组件 (Components)
│     ├─ Layout.tsx (布局)
│     ├─ Navbar.tsx (导航栏)
│     ├─ Footer.tsx (页脚)
│     ├─ EventCard.tsx (活动卡片)
│     └─ CategoryCard.tsx (分类卡片)
│
├─ 状态管理层 (State Management)
│  ├─ React Context API
│  ├─ Redux / Zustand (可选)
│  └─ 本地状态
│
├─ 数据层 (Data Layer)
│  ├─ API 调用层 (axios/fetch)
│  ├─ 数据缓存层 (React Query)
│  └─ 本地存储 (LocalStorage)
│
└─ 工具层 (Utils)
   ├─ cn.ts (类名合并)
   ├─ mockData.ts (模拟数据)
   └─ validators.ts (表单验证)
```

### 1.2 核心设计理念

#### 1.2.1 用户体验优先原则

**响应式设计策略：**
- **移动优先**：从小屏幕开始设计，逐步扩展到大屏幕
- **断点系统**：
  - sm: 640px (手机)
  - md: 768px (平板)
  - lg: 1024px (桌面)
  - xl: 1280px (大屏)

**交互设计原则：**
- **一致性**：统一的交互模式和视觉语言
- **反馈性**：所有操作都有明确的视觉反馈
- **可预测性**：符合用户预期，降低学习成本

#### 1.2.2 性能优化策略

**前端性能优化：**
```typescript
// 1. 代码分割
const EventDetail = lazy(() => import('./pages/EventDetail'));

// 2. 图片懒加载
<img loading="lazy" src={imageUrl} alt={altText} />

// 3. 虚拟滚动（列表优化）
import { FixedSizeList } from 'react-window';

// 4. 防抖和节流
const debouncedSearch = useDebounce(searchQuery, 300);
```

#### 1.2.3 可扩展性设计

**模块化设计模式：**
- **高内聚低耦合**：每个模块职责单一，模块间依赖最小化
- **插件化架构**：支持功能模块的动态加载
- **配置驱动**：通过配置文件控制功能开关

### 1.3 数据流设计

#### 1.3.1 单向数据流

```
用户操作
    ↓
触发 Action
    ↓
更新 State
    ↓
重新渲染 UI
    ↓
用户看到变化
```

#### 1.3.2 数据请求流程

```
用户触发操作
    ↓
调用 API 层
    ↓
发送 HTTP 请求
    ↓
后端处理请求
    ↓
返回响应数据
    ↓
更新前端状态
    ↓
渲染更新后的 UI
```

---

## 2. 实现步骤

### 2.1 项目初始化阶段

#### 步骤 1：环境搭建

```bash
# 1. 创建项目
npm create vite@latest community-events -- --template react-ts
cd community-events

# 2. 安装依赖
npm install react-router-dom lucide-react clsx tailwind-merge

# 3. 安装开发依赖
npm install -D tailwindcss postcss autoprefixer
npm install -D @types/node @types/react @types/react-dom

# 4. 初始化 Tailwind CSS
npx tailwindcss init -p
```

#### 步骤 2：项目结构规划

```bash
src/
├── assets/          # 静态资源
├── components/       # 可复用组件
├── pages/           # 页面组件
├── hooks/           # 自定义 Hooks
├── services/        # API 服务层
├── stores/          # 状态管理
├── types/           # TypeScript 类型
├── utils/           # 工具函数
├── App.tsx          # 根组件
└── main.tsx         # 入口文件
```

#### 步骤 3：配置开发环境

**TypeScript 配置 (tsconfig.json)：**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "jsx": "react-jsx",
    "strict": true,
    "baseUrl": "./src",
    "paths": {
      "@/*": ["./*"]
    }
  }
}
```

**Tailwind CSS 配置 (tailwind.config.js)：**
```javascript
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f9ff',
          // ... 其他颜色
        }
      }
    }
  },
  plugins: []
}
```

### 2.2 核心功能实现阶段

#### 步骤 4：类型系统设计

```typescript
// src/types/index.ts
export interface Event {
  id: string;
  title: string;
  description: string;
  category: string;
  date: string;
  time: string;
  location: string;
  organizer: Organizer;
  participants: number;
  maxParticipants: number;
  images: string[];
  isHot?: boolean;
  isNew?: boolean;
}

export interface User {
  id: string;
  name: string;
  email: string;
  avatar: string;
  bio?: string;
  location?: string;
  joinedDate: string;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
  color: string;
}
```

#### 步骤 5：基础组件开发

**导航栏组件 (Navbar.tsx)：**
```typescript
// 设计要点：
// 1. 使用 React Router 进行路由导航
// 2. 响应式设计：桌面端显示完整导航，移动端显示底部导航
// 3. 集成搜索功能
// 4. 活跃状态高亮显示

const Navbar = () => {
  const location = useLocation();
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (query: string) => {
    // 搜索逻辑实现
    navigate(`/events?search=${encodeURIComponent(query)}`);
  };

  return (
    <nav>
      {/* Logo */}
      {/* 搜索框 */}
      {/* 导航链接 */}
      {/* 移动端底部导航 */}
    </nav>
  );
};
```

#### 步骤 6：页面组件开发

**首页组件 (Home.tsx)：**
```typescript
// 实现要点：
// 1. 使用卡片式布局展示活动
// 2. 实现分类导航
// 3. 使用响应式网格布局
// 4. 加载状态处理

const Home = () => {
  const { data: hotEvents, loading } = useHotEvents();
  const { data: newEvents } = useNewEvents();

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <HeroSection />
      <CategoriesSection />
      <HotEventsSection events={hotEvents} />
      <NewEventsSection events={newEvents} />
      <CTASection />
    </div>
  );
};
```

#### 步骤 7：状态管理实现

**使用 Context API 管理全局状态：**
```typescript
// src/stores/AuthContext.tsx
interface AuthContextType {
  user: User | null;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  const login = async (credentials) => {
    const response = await api.login(credentials);
    setUser(response.user);
    localStorage.setItem('token', response.token);
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('token');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
```

### 2.3 后端集成阶段

#### 步骤 8：API 服务层设计

```typescript
// src/services/api.ts
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

// 请求拦截器
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // 处理未授权
      logout();
    }
    return Promise.reject(error);
  }
);

export const eventApi = {
  // 获取活动列表
  getEvents: (params: EventQueryParams) =>
    api.get('/events', { params }),

  // 获取活动详情
  getEventById: (id: string) =>
    api.get(`/events/${id}`),

  // 创建活动
  createEvent: (data: CreateEventData) =>
    api.post('/events', data),

  // 报名活动
  joinEvent: (eventId: string) =>
    api.post(`/events/${eventId}/join`),

  // 获取推荐活动
  getRecommendedEvents: (userId: string) =>
    api.get(`/events/recommended/${userId}`),
};

export const userApi = {
  // 获取用户信息
  getProfile: () => api.get('/users/profile'),

  // 更新用户信息
  updateProfile: (data: UpdateProfileData) =>
    api.patch('/users/profile', data),

  // 获取用户的活动
  getUserEvents: (type: 'created' | 'participated' | 'favorited') =>
    api.get(`/users/events/${type}`),
};
```

#### 步骤 9：自定义 Hooks 封装

```typescript
// src/hooks/useEvents.ts
export const useEvents = (params?: EventQueryParams) => {
  return useQuery({
    queryKey: ['events', params],
    queryFn: () => eventApi.getEvents(params),
    staleTime: 5 * 60 * 1000, // 5 分钟
  });
};

export const useEventDetail = (id: string) => {
  return useQuery({
    queryKey: ['event', id],
    queryFn: () => eventApi.getEventById(id),
    enabled: !!id,
  });
};

export const useRecommendedEvents = (userId: string) => {
  return useQuery({
    queryKey: ['recommended-events', userId],
    queryFn: () => eventApi.getRecommendedEvents(userId),
    enabled: !!userId,
  });
};
```

### 2.4 优化和完善阶段

#### 步骤 10：性能优化

**1. 代码分割和懒加载：**
```typescript
// src/App.tsx
import { lazy, Suspense } from 'react';

const EventDetail = lazy(() => import('./pages/EventDetail'));
const CreateEvent = lazy(() => import('./pages/CreateEvent'));
const Profile = lazy(() => import('./pages/Profile'));

function App() {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <Routes>
        <Route path="/events/:id" element={<EventDetail />} />
        <Route path="/create-event" element={<CreateEvent />} />
        <Route path="/profile" element={<Profile />} />
      </Routes>
    </Suspense>
  );
}
```

**2. 图片优化：**
```typescript
// src/components/OptimizedImage.tsx
import { useState } from 'react';

const OptimizedImage = ({ src, alt, ...props }) => {
  const [isLoading, setIsLoading] = useState(true);

  return (
    <div className="relative">
      {isLoading && (
        <div className="absolute inset-0 bg-gray-200 animate-pulse" />
      )}
      <img
        src={src}
        alt={alt}
        loading="lazy"
        onLoad={() => setIsLoading(false)}
        className={`transition-opacity duration-300 ${
          isLoading ? 'opacity-0' : 'opacity-100'
        }`}
        {...props}
      />
    </div>
  );
};
```

**3. 虚拟滚动：**
```typescript
import { FixedSizeList as List } from 'react-window';

const VirtualizedEventList = ({ events }) => {
  const Row = ({ index, style }) => (
    <div style={style}>
      <EventCard event={events[index]} />
    </div>
  );

  return (
    <List
      height={600}
      itemCount={events.length}
      itemSize={320}
      width="100%"
    >
      {Row}
    </List>
  );
};
```

#### 步骤 11：错误处理和边界情况

**错误边界组件：**
```typescript
// src/components/ErrorBoundary.tsx
class ErrorBoundary extends React.Component {
  state = { hasError: false, error: null };

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
    // 发送错误日志到服务器
    logErrorToService(error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return <ErrorFallback error={this.state.error} />;
    }

    return this.props.children;
  }
}
```

**全局错误处理：**
```typescript
// src/utils/errorHandler.ts
export const handleApiError = (error: any) => {
  if (axios.isAxiosError(error)) {
    switch (error.response?.status) {
      case 401:
        toast.error('登录已过期，请重新登录');
        // 跳转到登录页
        break;
      case 403:
        toast.error('没有权限访问此资源');
        break;
      case 404:
        toast.error('请求的资源不存在');
        break;
      case 500:
        toast.error('服务器错误，请稍后重试');
        break;
      default:
        toast.error(error.response?.data?.message || '请求失败');
    }
  } else {
    toast.error('网络错误，请检查网络连接');
  }
};
```

---

## 3. 实现原理

### 3.1 前端核心原理

#### 3.1.1 React 组件化原理

**组件生命周期：**
```typescript
// 函数组件 + Hooks 模拟生命周期

function MyComponent() {
  // 1. 挂载阶段
  useEffect(() => {
    console.log('组件挂载');
    return () => {
      console.log('组件卸载'); // 清理函数
    };
  }, []);

  // 2. 更新阶段
  useEffect(() => {
    console.log('依赖项变化，组件更新');
  }, [dependency]);

  // 3. 渲染阶段
  return <div>组件内容</div>;
}
```

**虚拟 DOM 原理：**
```
1. React 创建虚拟 DOM 树
2. 状态变化时，创建新的虚拟 DOM 树
3. 对比新旧虚拟 DOM 树（Diff 算法）
4. 计算出最小的变更集
5. 批量更新真实 DOM
```

#### 3.1.2 状态管理原理

**React Hooks 原理：**
```typescript
// useState 原理简化版
let hookIndex = 0;
let hooks = [];

function useState(initialValue) {
  const currentIndex = hookIndex;

  // 首次渲染初始化
  if (hooks[currentIndex] === undefined) {
    hooks[currentIndex] = initialValue;
  }

  // 设置状态的函数
  const setState = (newValue) => {
    hooks[currentIndex] = newValue;
    // 触发重新渲染
    render();
  };

  hookIndex++;
  return [hooks[currentIndex], setState];
}
```

**Context API 原理：**
```typescript
// Context 本质上是一个发布-订阅系统
const MyContext = createContext();

// Provider 组件
function Provider({ children, value }) {
  const contextValue = useMemo(() => value, [value]);
  return (
    <MyContext.Provider value={contextValue}>
      {children}
    </MyContext.Provider>
  );
}

// Consumer 组件
function Consumer() {
  const value = useContext(MyContext);
  return <div>{value}</div>;
}
```

#### 3.1.3 路由原理

**React Router 原理：**
```typescript
// 1. History API 监听 URL 变化
window.addEventListener('popstate', handlePopState);

// 2. 根据当前 URL 匹配对应的组件
const matchRoutes = (routes, pathname) => {
  return routes.find(route => {
    // 将路由模式转换为正则表达式
    const regex = pathToRegex(route.path);
    return regex.test(pathname);
  });
};

// 3. 渲染匹配的组件
const Router = ({ routes }) => {
  const location = useLocation();
  const matchedRoute = matchRoutes(routes, location.pathname);
  return matchedRoute ? <matchedRoute.component /> : <NotFound />;
};
```

### 3.2 样式系统原理

#### 3.2.1 Tailwind CSS 原理

**Utility-First 原理：**
```css
/* 原子类定义 */
.bg-primary-600 {
  background-color: #0284c7;
}

.p-4 {
  padding: 1rem;
}

.rounded-lg {
  border-radius: 0.5rem;
}

/* 使用时组合 */
<div class="bg-primary-600 p-4 rounded-lg">
  内容
</div>
```

**JIT (Just-In-Time) 编译：**
```
1. 扫描源代码中的类名
2. 动态生成对应的 CSS
3. 删除未使用的样式
4. 生成最终的 CSS 文件
```

#### 3.2.2 响应式设计原理

**媒体查询断点：**
```css
/* Tailwind 断点定义 */
@screen sm {
  /* min-width: 640px */
}

@screen md {
  /* min-width: 768px */
}

@screen lg {
  /* min-width: 1024px */
}

/* 使用方式 */
<div class="grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
  内容
</div>
```

### 3.3 数据交互原理

#### 3.3.1 HTTP 请求原理

**Axios 拦截器原理：**
```typescript
// 请求拦截器链
axios.interceptors.request.use(
  (config) => {
    // 添加认证信息
    config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器链
axios.interceptors.response.use(
  (response) => {
    // 统一处理响应数据
    return response.data;
  },
  (error) => {
    // 统一处理错误
    return Promise.reject(error);
  }
);
```

#### 3.3.2 React Query 原理

**数据缓存和同步机制：**
```typescript
// React Query 核心原理
class QueryCache {
  queries = new Map();

  set(key, data) {
    this.queries.set(key, {
      data,
      timestamp: Date.now(),
      staleTime: 5 * 60 * 1000
    });
  }

  get(key) {
    const query = this.queries.get(key);
    if (!query) return null;

    // 检查数据是否过期
    const isStale = Date.now() - query.timestamp > query.staleTime;
    return {
      data: query.data,
      isStale
    };
  }
}

// 使用示例
const { data, isLoading } = useQuery({
  queryKey: ['events'],
  queryFn: fetchEvents,
  staleTime: 5 * 60 * 1000 // 5 分钟内使用缓存
});
```

---

## 4. 后端集成方案

### 4.1 后端技术栈选型

#### 4.1.1 可选技术栈

**方案一：Java Spring Boot**
```
优势：
- 成熟稳定，企业级应用首选
- 强类型，适合大型项目
- 丰富的生态系统

技术栈：
- Spring Boot 3.x
- Spring Security（认证授权）
- Spring Data JPA（数据访问）
- MySQL / PostgreSQL（数据库）
- Redis（缓存）
```

**方案二：Node.js + Express**
```
优势：
- 前后端统一语言
- 开发效率高
- 异步非阻塞 I/O

技术栈：
- Node.js 20+
- Express / Fastify
- JWT（认证）
- TypeORM / Prisma（数据访问）
- PostgreSQL / MongoDB（数据库）
- Redis（缓存）
```

**方案三：Python + FastAPI**
```
优势：
- 开发速度快
- 机器学习集成方便
- 适合推荐算法实现

技术栈：
- Python 3.11+
- FastAPI
- Pydantic（数据验证）
- SQLAlchemy（ORM）
- PostgreSQL（数据库）
- Redis（缓存）
- Scikit-learn / TensorFlow（机器学习）
```

### 4.2 后端架构设计

#### 4.2.1 分层架构

```
┌─────────────────────────────────────┐
│    Controller Layer (控制层)        │
│    接收请求，参数验证，返回响应      │
├─────────────────────────────────────┤
│    Service Layer (业务逻辑层)        │
│    核心业务逻辑，事务管理           │
├─────────────────────────────────────┤
│    Repository Layer (数据访问层)    │
│    数据库操作，缓存管理             │
├─────────────────────────────────────┤
│    Model Layer (数据模型层)          │
│    实体定义，关系映射               │
└─────────────────────────────────────┘
```

#### 4.2.2 数据库设计

**ER 图：**
```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    User     │       │   Event     │       │  Category   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◄──────│ id (PK)     │◄──────│ id (PK)     │
│ name        │       │ title       │       │ name        │
│ email       │       │ description │       │ icon        │
│ password    │       │ date        │       │ color       │
│ avatar      │       │ time        │       └─────────────┘
│ location    │       │ location    │              ▲
│ bio         │       │ organizer_id│──────────────┘
│ created_at  │       │ category_id │
│ updated_at  │       │ max_people  │
└─────────────┘       │ status      │
                      │ created_at  │
                      └─────────────┘
                            ▲
                            │
                            │
┌─────────────┐       ┌─────────────┐
│ Participant │       │   Rating    │
├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │
│ user_id (FK)│◄──────│ user_id (FK)│
│ event_id(FK)│       │ event_id(FK)│
│ joined_at   │       │ score       │
│ status      │       │ comment     │
└─────────────┘       │ created_at  │
                      └─────────────┘
```

**数据库表设计：**

```sql
-- 用户表
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500),
    location VARCHAR(200),
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 活动分类表
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(10),
    color VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 活动表
CREATE TABLE events (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    time TIME NOT NULL,
    location VARCHAR(500) NOT NULL,
    organizer_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    max_participants INT NOT NULL,
    status ENUM('draft', 'published', 'ongoing', 'completed', 'cancelled') DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (organizer_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_organizer (organizer_id),
    INDEX idx_category (category_id),
    INDEX idx_date (date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 活动参与者表
CREATE TABLE participants (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    status ENUM('registered', 'attended', 'cancelled') DEFAULT 'registered',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    UNIQUE KEY uk_user_event (user_id, event_id),
    INDEX idx_user (user_id),
    INDEX idx_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户评分表
CREATE TABLE ratings (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    score INT NOT NULL CHECK (score >= 1 AND score <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    UNIQUE KEY uk_user_event (user_id, event_id),
    INDEX idx_user (user_id),
    INDEX idx_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户收藏表
CREATE TABLE favorites (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    UNIQUE KEY uk_user_event (user_id, event_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户行为日志表（用于推荐算法）
CREATE TABLE user_behaviors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    action_type ENUM('view', 'click', 'join', 'favorite', 'share') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id),
    INDEX idx_user_action (user_id, action_type),
    INDEX idx_event_action (event_id, action_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4.3 API 接口设计

#### 4.3.1 RESTful API 设计

**通用响应格式：**
```typescript
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 错误响应
{
  "code": 400,
  "message": "参数错误",
  "errors": [ ... ]
}
```

**API 接口列表：**

```typescript
// ==================== 认证接口 ====================

// 用户登录
POST /api/auth/login
Request: { email: string, password: string }
Response: {
  token: string,
  user: User
}

// 用户注册
POST /api/auth/register
Request: { name: string, email: string, password: string }
Response: {
  token: string,
  user: User
}

// 刷新 Token
POST /api/auth/refresh
Headers: { Authorization: 'Bearer <refresh_token>' }
Response: {
  token: string
}

// ==================== 用户接口 ====================

// 获取当前用户信息
GET /api/users/me
Headers: { Authorization: 'Bearer <token>' }
Response: { user: User }

// 更新用户信息
PATCH /api/users/me
Headers: { Authorization: 'Bearer <token>' }
Request: { name?: string, avatar?: string, bio?: string, location?: string }
Response: { user: User }

// 获取用户的活动
GET /api/users/me/events/:type
Headers: { Authorization: 'Bearer <token>' }
Params: type = 'created' | 'participated' | 'favorited'
Query: ?page=1&limit=10
Response: {
  events: Event[],
  pagination: { total: number, page: number, limit: number }
}

// ==================== 活动接口 ====================

// 获取活动列表
GET /api/events
Query: {
  page?: number,
  limit?: number,
  category?: string,
  search?: string,
  sort?: 'date' | 'participants' | 'created'
}
Response: {
  events: Event[],
  pagination: { total: number, page: number, limit: number }
}

// 获取活动详情
GET /api/events/:id
Response: { event: Event }

// 创建活动
POST /api/events
Headers: { Authorization: 'Bearer <token>' }
Request: {
  title: string,
  description: string,
  category_id: string,
  date: string,
  time: string,
  location: string,
  max_participants: number,
  images: string[]
}
Response: { event: Event }

// 更新活动
PATCH /api/events/:id
Headers: { Authorization: 'Bearer <token>' }
Request: { ...Partial<CreateEvent> }
Response: { event: Event }

// 删除活动
DELETE /api/events/:id
Headers: { Authorization: 'Bearer <token>' }
Response: { message: string }

// ==================== 活动互动接口 ====================

// 报名活动
POST /api/events/:id/join
Headers: { Authorization: 'Bearer <token>' }
Response: { message: string }

// 取消报名
DELETE /api/events/:id/join
Headers: { Authorization: 'Bearer <token>' }
Response: { message: string }

// 收藏活动
POST /api/events/:id/favorite
Headers: { Authorization: 'Bearer <token>' }
Response: { message: string }

// 取消收藏
DELETE /api/events/:id/favorite
Headers: { Authorization: 'Bearer <token>' }
Response: { message: string }

// 评分活动
POST /api/events/:id/rate
Headers: { Authorization: 'Bearer <token>' }
Request: { score: number, comment?: string }
Response: { rating: Rating }

// ==================== 推荐接口 ====================

// 获取推荐活动
GET /api/events/recommended
Headers: { Authorization: 'Bearer <token>' }
Query: {
  page?: number,
  limit?: number,
  algorithm?: 'cf' | 'cb' | 'hybrid'  // 协同过滤 | 内容推荐 | 混合算法
}
Response: {
  events: Event[],
  algorithm_used: string,
  explanation?: string
}

// 记录用户行为
POST /api/events/:id/behavior
Headers: { Authorization: 'Bearer <token>' }
Request: {
  action_type: 'view' | 'click' | 'join' | 'favorite' | 'share'
}
Response: { message: string }

// ==================== 分类接口 ====================

// 获取所有分类
GET /api/categories
Response: { categories: Category[] }

// ==================== 搜索接口 ====================

// 搜索活动
GET /api/search
Query: {
  q: string,
  type?: 'events' | 'users' | 'all',
  page?: number,
  limit?: number
}
Response: {
  results: {
    events: Event[],
    users: User[]
  },
  pagination: { ... }
}
```

#### 4.3.2 前后端对接实现

**API Service 层实现：**
```typescript
// src/services/api.ts
import axios, { AxiosInstance, AxiosError } from 'axios';

// API 客户端类
class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  // 设置拦截器
  private setupInterceptors() {
    // 请求拦截器
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('access_token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // 响应拦截器
    this.client.interceptors.response.use(
      (response) => {
        return response.data;
      },
      async (error: AxiosError) => {
        const originalRequest = error.config as any;

        // Token 过期，尝试刷新
        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;
          try {
            const refreshToken = localStorage.getItem('refresh_token');
            const { data } = await this.client.post('/auth/refresh', {}, {
              headers: { Authorization: `Bearer ${refreshToken}` }
            });

            localStorage.setItem('access_token', data.token);
            originalRequest.headers.Authorization = `Bearer ${data.token}`;
            return this.client(originalRequest);
          } catch (refreshError) {
            // 刷新失败，跳转登录
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        }

        // 其他错误处理
        this.handleError(error);
        return Promise.reject(error);
      }
    );
  }

  // 统一错误处理
  private handleError(error: AxiosError) {
    const message = error.response?.data?.message || error.message || '请求失败';
    console.error('API Error:', message);
    // 可以在这里添加 Toast 通知
  }

  // HTTP 方法封装
  get<T = any>(url: string, config?: any): Promise<T> {
    return this.client.get(url, config);
  }

  post<T = any>(url: string, data?: any, config?: any): Promise<T> {
    return this.client.post(url, data, config);
  }

  patch<T = any>(url: string, data?: any, config?: any): Promise<T> {
    return this.client.patch(url, data, config);
  }

  delete<T = any>(url: string, config?: any): Promise<T> {
    return this.client.delete(url, config);
  }
}

// 导出 API 客户端实例
export const api = new ApiClient();

// ==================== API 服务模块 ====================

// 认证服务
export const authService = {
  login: (credentials: LoginCredentials) =>
    api.post<{ token: string; refresh_token: string; user: User }>('/auth/login', credentials),

  register: (userData: RegisterData) =>
    api.post<{ token: string; refresh_token: string; user: User }>('/auth/register', userData),

  logout: () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  },

  refreshToken: () =>
    api.post<{ token: string }>('/auth/refresh', {}),
};

// 用户服务
export const userService = {
  getProfile: () => api.get<{ user: User }>('/users/me'),

  updateProfile: (data: Partial<User>) =>
    api.patch<{ user: User }>('/users/me', data),

  getUserEvents: (type: 'created' | 'participated' | 'favorited', params?: PaginationParams) =>
    api.get<{ events: Event[]; pagination: PaginationInfo }>(`/users/me/events/${type}`, { params }),
};

// 活动服务
export const eventService = {
  getEvents: (params?: EventQueryParams) =>
    api.get<{ events: Event[]; pagination: PaginationInfo }>('/events', { params }),

  getEventById: (id: string) =>
    api.get<{ event: Event }>(`/events/${id}`),

  createEvent: (data: CreateEventData) =>
    api.post<{ event: Event }>('/events', data),

  updateEvent: (id: string, data: Partial<CreateEventData>) =>
    api.patch<{ event: Event }>(`/events/${id}`, data),

  deleteEvent: (id: string) =>
    api.delete<{ message: string }>(`/events/${id}`),

  joinEvent: (id: string) =>
    api.post<{ message: string }>(`/events/${id}/join`),

  cancelJoin: (id: string) =>
    api.delete<{ message: string }>(`/events/${id}/join`),

  favoriteEvent: (id: string) =>
    api.post<{ message: string }>(`/events/${id}/favorite`),

  unfavoriteEvent: (id: string) =>
    api.delete<{ message: string }>(`/events/${id}/favorite`),

  rateEvent: (id: string, data: { score: number; comment?: string }) =>
    api.post<{ rating: Rating }>(`/events/${id}/rate`, data),
};

// 推荐服务
export const recommendationService = {
  getRecommendedEvents: (params?: {
    page?: number;
    limit?: number;
    algorithm?: 'cf' | 'cb' | 'hybrid';
  }) => api.get<{ events: Event[]; algorithm_used: string; explanation?: string }>('/events/recommended', { params }),

  recordBehavior: (eventId: string, actionType: BehaviorAction) =>
    api.post<{ message: string }>(`/events/${eventId}/behavior`, { action_type: actionType }),
};

// 分类服务
export const categoryService = {
  getCategories: () => api.get<{ categories: Category[] }>('/categories'),
};

// 搜索服务
export const searchService = {
  search: (query: string, params?: SearchParams) =>
    api.get<{ results: { events: Event[]; users: User[] }; pagination: PaginationInfo }>('/search', {
      params: { q: query, ...params },
    }),
};
```

---

## 5. 推荐算法实现

### 5.1 推荐系统架构

#### 5.1.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                  推荐系统架构                             │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐    ┌──────────────┐    ┌────────────┐ │
│  │   数据采集层   │    │   特征工程层   │    │  算法模型层  │ │
│  │              │    │              │    │            │ │
│  │ - 用户行为日志 │ -> │ - 用户画像     │ -> │ - User-CF  │ │
│  │ - 活动内容    │    │ - 活动特征     │    │ - Content  │ │
│  │ - 用户属性    │    │ - 交互矩阵     │    │ - Hybrid   │ │
│  └──────────────┘    └──────────────┘    └────────────┘ │
│         │                  │                  │         │
│         └──────────────────┴──────────────────┘         │
│                            │                            │
│                    ┌───────▼───────┐                    │
│                    │   召回层       │                    │
│                    │ - 候选集生成   │                    │
│                    │ - 快速过滤     │                    │
│                    └───────┬───────┘                    │
│                            │                            │
│                    ┌───────▼───────┐                    │
│                    │   排序层       │                    │
│                    │ - 精确打分     │                    │
│                    │ - 多目标优化   │                    │
│                    └───────┬───────┘                    │
│                            │                            │
│                    ┌───────▼───────┐                    │
│                    │   重排层       │                    │
│                    │ - 多样性处理   │                    │
│                    │ - 业务规则     │                    │
│                    └───────┬───────┘                    │
│                            │                            │
│                    ┌───────▼───────┐                    │
│                    │   输出层       │                    │
│                    │ - 推荐列表     │                    │
│                    │ - 解释说明     │                    │
│                    └───────────────┘                    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

#### 5.1.2 在项目中的位置

```
项目目录结构
Claw/
├── src/                           # 前端代码
│   ├── services/                  # API 服务层
│   │   ├── api.ts                 # API 调用封装
│   │   └── recommendation.ts      # 推荐服务调用
│   ├── hooks/                     # 自定义 Hooks
│   │   └── useRecommendation.ts   # 推荐数据 Hook
│   └── pages/
│       └── Home.tsx               # 首页（展示推荐）
│
└── backend/                       # 后端代码（需要创建）
    ├── controllers/               # 控制器
    │   └── recommendation.controller.ts  # 推荐控制器
    ├── services/                  # 业务逻辑层
    │   └── recommendation/        # 推荐服务 ⭐⭐⭐
    │       ├── data-collector.ts  # 数据采集
    │       ├── feature-engineer.ts # 特征工程
    │       ├── models/            # 算法模型 ⭐⭐⭐
    │       │   ├── user-cf.ts     # User-CF 算法
    │       │   ├── content-based.ts # Content-Based 算法
    │       │   └── hybrid.ts      # 混合算法
    │       ├── recall.ts          # 召回层
    │       ├── ranking.ts         # 排序层
    │       └── reranking.ts       # 重排层
    ├── repositories/              # 数据访问层
    │   └── behavior.repository.ts # 行为数据仓库
    └── utils/                     # 工具函数
        └── matrix.ts              # 矩阵运算工具
```

### 5.2 User-CF (User-based Collaborative Filtering) 实现

#### 5.2.1 算法原理

User-CF 基于用户相似性进行推荐：
1. 收集用户对活动的评分/行为
2. 计算用户之间的相似度
3. 找到与目标用户最相似的 K 个用户
4. 推荐这些相似用户喜欢的活动

**相似度计算公式：**

**余弦相似度：**
```
similarity(u, v) = (Σ rating[u][i] * rating[v][i]) /
                  (√(Σ rating[u][i]²) * √(Σ rating[v][i]²))
```

**皮尔逊相关系数：**
```
similarity(u, v) = Σ((rating[u][i] - avg[u]) * (rating[v][i] - avg[v])) /
                  (√(Σ(rating[u][i] - avg[u])²) * √(Σ(rating[v][i] - avg[v])²))
```

#### 5.2.2 代码实现

**位置：`backend/services/recommendation/models/user-cf.ts`**

```typescript
/**
 * User-based Collaborative Filtering 推荐算法
 *
 * 核心思想：根据用户之间的相似度来推荐活动
 * "喜欢相似活动的人也可能喜欢其他相似的活动"
 */

import { Injectable } from '@nestjs/common';
import { UserRepository } from '../../repositories/user.repository';
import { BehaviorRepository } from '../../repositories/behavior.repository';
import { EventRepository } from '../../repositories/event.repository';

interface UserRating {
  userId: string;
  eventId: string;
  rating: number; // 1-5 评分，或者基于行为的权重
}

interface UserSimilarity {
  userId1: string;
  userId2: string;
  similarity: number; // 0-1
}

@Injectable()
export class UserCFService {
  private readonly SIMILAR_USER_COUNT = 20; // 取最相似的 N 个用户
  private readonly MIN_INTERSECTION_SIZE = 3; // 最小共同活动数
  private readonly SIMILARITY_THRESHOLD = 0.1; // 相似度阈值

  constructor(
    private readonly userRepository: UserRepository,
    private readonly behaviorRepository: BehaviorRepository,
    private readonly eventRepository: EventRepository,
  ) {}

  /**
   * 获取用户协同过滤推荐
   * @param targetUserId 目标用户ID
   * @param topN 返回推荐数量
   */
  async getRecommendations(targetUserId: string, topN: number = 10): Promise<string[]> {
    // 1. 获取用户行为数据（评分矩阵）
    const userRatings = await this.getUserRatingsMatrix();

    // 2. 计算目标用户与其他用户的相似度
    const similarities = await this.calculateUserSimilarities(targetUserId, userRatings);

    // 3. 选择最相似的 K 个用户
    const similarUsers = this.selectTopSimilarUsers(similarities);

    if (similarUsers.length === 0) {
      // 没有相似用户，返回热门活动
      return this.getFallbackRecommendations(topN);
    }

    // 4. 生成推荐列表
    const recommendations = await this.generateRecommendations(
      targetUserId,
      similarUsers,
      userRatings,
      topN,
    );

    return recommendations;
  }

  /**
   * 获取用户评分矩阵
   * 将用户对活动的各种行为转换为评分
   */
  private async getUserRatingsMatrix(): Promise<Map<string, Map<string, number>>> {
    const behaviors = await this.behaviorRepository.getAllBehaviors();

    const ratingMatrix = new Map<string, Map<string, number>>();

    // 行为权重配置
    const actionWeights = {
      view: 1,        // 浏览活动
      click: 2,       // 点击活动
      favorite: 4,    // 收藏活动
      join: 5,        // 报名活动
      share: 3,       // 分享活动
    };

    for (const behavior of behaviors) {
      const { userId, eventId, actionType } = behavior;

      if (!ratingMatrix.has(userId)) {
        ratingMatrix.set(userId, new Map());
      }

      const userRatings = ratingMatrix.get(userId)!;
      const currentRating = userRatings.get(eventId) || 0;
      const weight = actionWeights[actionType] || 1;

      // 累加行为权重，最高不超过 5 分
      const newRating = Math.min(5, currentRating + weight);
      userRatings.set(eventId, newRating);
    }

    return ratingMatrix;
  }

  /**
   * 计算用户相似度（使用余弦相似度）
   */
  private async calculateUserSimilarities(
    targetUserId: string,
    ratingMatrix: Map<string, Map<string, number>>,
  ): Promise<UserSimilarity[]> {
    const targetUserRatings = ratingMatrix.get(targetUserId);
    if (!targetUserRatings || targetUserRatings.size === 0) {
      return [];
    }

    const similarities: UserSimilarity[] = [];

    for (const [userId, userRatings] of ratingMatrix.entries()) {
      if (userId === targetUserId) continue;

      // 计算两个用户的共同活动
      const intersection = this.getIntersection(targetUserRatings, userRatings);

      // 共同活动数量过少，跳过
      if (intersection.size < this.MIN_INTERSECTION_SIZE) continue;

      // 计算余弦相似度
      const similarity = this.calculateCosineSimilarity(targetUserRatings, userRatings, intersection);

      if (similarity > this.SIMILARITY_THRESHOLD) {
        similarities.push({
          userId1: targetUserId,
          userId2: userId,
          similarity,
        });
      }
    }

    // 按相似度降序排序
    return similarities.sort((a, b) => b.similarity - a.similarity);
  }

  /**
   * 计算两个用户评分向量的余弦相似度
   */
  private calculateCosineSimilarity(
    ratings1: Map<string, number>,
    ratings2: Map<string, number>,
    intersection: Set<string>,
  ): number {
    let dotProduct = 0;
    let norm1 = 0;
    let norm2 = 0;

    for (const eventId of intersection) {
      const rating1 = ratings1.get(eventId) || 0;
      const rating2 = ratings2.get(eventId) || 0;

      dotProduct += rating1 * rating2;
      norm1 += rating1 * rating1;
      norm2 += rating2 * rating2;
    }

    if (norm1 === 0 || norm2 === 0) return 0;

    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
  }

  /**
   * 获取两个用户的共同活动集合
   */
  private getIntersection(
    ratings1: Map<string, number>,
    ratings2: Map<string, number>,
  ): Set<string> {
    const intersection = new Set<string>();

    for (const eventId of ratings1.keys()) {
      if (ratings2.has(eventId)) {
        intersection.add(eventId);
      }
    }

    return intersection;
  }

  /**
   * 选择最相似的 K 个用户
   */
  private selectTopSimilarUsers(similarities: UserSimilarity[]): Array<{ userId: string; similarity: number }> {
    return similarities
      .slice(0, this.SIMILAR_USER_COUNT)
      .map(s => ({ userId: s.userId2, similarity: s.similarity }));
  }

  /**
   * 生成推荐列表
   */
  private async generateRecommendations(
    targetUserId: string,
    similarUsers: Array<{ userId: string; similarity: number }>,
    ratingMatrix: Map<string, Map<string, number>>,
    topN: number,
  ): Promise<string[]> {
    const targetUserRatings = ratingMatrix.get(targetUserId) || new Map();
    const eventScores = new Map<string, number>();

    // 计算每个候选活动的推荐分数
    for (const { userId, similarity } of similarUsers) {
      const userRatings = ratingMatrix.get(userId);
      if (!userRatings) continue;

      for (const [eventId, rating] of userRatings.entries()) {
        // 跳过用户已交互过的活动
        if (targetUserRatings.has(eventId)) continue;

        // 计算加权分数：相似度 * 评分
        const weightedScore = similarity * rating;

        eventScores.set(
          eventId,
          (eventScores.get(eventId) || 0) + weightedScore,
        );
      }
    }

    // 按分数降序排序
    const sortedEvents = Array.from(eventScores.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, topN);

    return sortedEvents.map(([eventId]) => eventId);
  }

  /**
   * 获取回退推荐（热门活动）
   */
  private async getFallbackRecommendations(topN: number): Promise<string[]> {
    const hotEvents = await this.eventRepository.getHotEvents(topN);
    return hotEvents.map(event => event.id);
  }

  /**
   * 实时更新用户相似度缓存
   * 当用户产生新行为时调用
   */
  async updateUserSimilarityCache(userId: string): Promise<void> {
    // 可以在这里实现缓存更新逻辑
    // 例如使用 Redis 缓存相似度矩阵
  }
}
```

### 5.3 Content-Based (内容推荐) 实现

#### 5.3.1 算法原理

Content-Based 基于用户历史偏好和物品内容特征进行推荐：
1. 分析用户历史交互的活动，提取偏好特征
2. 分析活动的内容特征（分类、地点、时间等）
3. 计算用户偏好与未交互活动的相似度
4. 推荐相似度最高的活动

**特征向量化：**

```
活动特征向量 = [
  category_weight,      // 分类权重
  location_similarity,   // 地点相似度
  time_preference,      // 时间偏好
  participants_score,    // 参与人数偏好
  ...
]
```

#### 5.3.2 代码实现

**位置：`backend/services/recommendation/models/content-based.ts`**

```typescript
/**
 * Content-Based 推荐算法
 *
 * 核心思想：根据活动的内容特征和用户的偏好进行推荐
 * "根据你喜欢的历史活动，推荐内容相似的活动"
 */

import { Injectable } from '@nestjs/common';
import { BehaviorRepository } from '../../repositories/behavior.repository';
import { EventRepository } from '../../repositories/event.repository';
import { CategoryRepository } from '../../repositories/category.repository';

interface UserProfile {
  userId: string;
  categoryPreferences: Map<string, number>; // 分类偏好权重
  locationPreference: { lat: number; lng: number; radius: number };
  timePreferences: { weekday: number[]; hour: number[] };
  participantPreference: { avg: number; std: number };
}

interface EventFeatures {
  eventId: string;
  category: string;
  location: { lat: number; lng: number };
  date: Date;
  time: string;
  maxParticipants: number;
  titleKeywords: string[];
}

@Injectable()
export class ContentBasedService {
  private readonly TOP_N = 10;

  constructor(
    private readonly behaviorRepository: BehaviorRepository,
    private readonly eventRepository: EventRepository,
    private readonly categoryRepository: CategoryRepository,
  ) {}

  /**
   * 获取基于内容的推荐
   */
  async getRecommendations(targetUserId: string, topN: number = this.TOP_N): Promise<string[]> {
    // 1. 构建用户画像
    const userProfile = await this.buildUserProfile(targetUserId);

    if (!this.hasValidProfile(userProfile)) {
      // 用户画像无效，返回热门活动
      return this.getFallbackRecommendations(topN);
    }

    // 2. 获取候选活动（排除用户已交互的）
    const candidateEvents = await this.getCandidateEvents(targetUserId);

    // 3. 计算每个候选活动的推荐分数
    const scoredEvents = await this.scoreEvents(userProfile, candidateEvents);

    // 4. 返回分数最高的 N 个活动
    return scoredEvents
      .sort((a, b) => b.score - a.score)
      .slice(0, topN)
      .map(item => item.eventId);
  }

  /**
   * 构建用户画像
   */
  private async buildUserProfile(userId: string): Promise<UserProfile> {
    const userBehaviors = await this.behaviorRepository.getUserBehaviors(userId);
    const interactedEventIds = userBehaviors.map(b => b.eventId);
    const events = await this.eventRepository.getEventsByIds(interactedEventIds);

    const categoryPreferences = new Map<string, number>();
    const locations: { lat: number; lng: number }[] = [];
    const weekdays: number[] = [];
    const hours: number[] = [];
    const participantCounts: number[] = [];

    // 分析用户历史交互的活动
    for (const event of events) {
      // 统计分类偏好
      const weight = this.getBehaviorWeight(event);
      categoryPreferences.set(
        event.categoryId,
        (categoryPreferences.get(event.categoryId) || 0) + weight,
      );

      // 统计地点偏好
      if (event.locationLat && event.locationLng) {
        locations.push({ lat: event.locationLat, lng: event.locationLng });
      }

      // 统计时间偏好
      const eventDate = new Date(event.date);
      weekdays.push(eventDate.getDay());
      hours.push(parseInt(event.time.split(':')[0]));

      // 统计参与人数偏好
      participantCounts.push(event.maxParticipants);
    }

    // 归一化分类偏好
    const totalWeight = Array.from(categoryPreferences.values()).reduce((a, b) => a + b, 0);
    for (const [category, weight] of categoryPreferences.entries()) {
      categoryPreferences.set(category, weight / totalWeight);
    }

    // 计算地点偏好中心
    const avgLocation = this.calculateAverageLocation(locations);

    // 计算时间偏好分布
    const timePreferences = this.calculateTimeDistribution(weekdays, hours);

    // 计算参与人数偏好
    const participantPreference = this.calculateParticipantPreference(participantCounts);

    return {
      userId,
      categoryPreferences,
      locationPreference: avgLocation,
      timePreferences,
      participantPreference,
    };
  }

  /**
   * 计算事件的行为权重
   */
  private getBehaviorWeight(event: any): number {
    // 根据用户对该事件的行为类型计算权重
    const weights = {
      view: 1,
      click: 2,
      favorite: 4,
      join: 5,
      share: 3,
    };

    // 这里简化处理，实际应该从用户行为记录中获取
    return weights.join; // 默认使用报名权重
  }

  /**
   * 计算平均位置
   */
  private calculateAverageLocation(locations: { lat: number; lng: number }[]): {
    lat: number;
    lng: number;
    radius: number;
  } {
    if (locations.length === 0) {
      return { lat: 0, lng: 0, radius: 50 }; // 默认值
    }

    const avgLat = locations.reduce((sum, loc) => sum + loc.lat, 0) / locations.length;
    const avgLng = locations.reduce((sum, loc) => sum + loc.lng, 0) / locations.length;

    // 计算标准差作为半径
    const distances = locations.map(loc =>
      this.calculateDistance(avgLat, avgLng, loc.lat, loc.lng),
    );
    const radius = distances.reduce((a, b) => a + b, 0) / distances.length;

    return { lat: avgLat, lng: avgLng, radius: radius * 1.5 };
  }

  /**
   * 计算两点之间的距离（Haversine 公式）
   */
  private calculateDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
    const R = 6371; // 地球半径（千米）
    const dLat = this.toRad(lat2 - lat1);
    const dLng = this.toRad(lng2 - lng1);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(lat1)) *
        Math.cos(this.toRad(lat2)) *
        Math.sin(dLng / 2) *
        Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  private toRad(degrees: number): number {
    return (degrees * Math.PI) / 180;
  }

  /**
   * 计算时间偏好分布
   */
  private calculateTimeDistribution(weekdays: number[], hours: number[]): {
    weekday: number[];
    hour: number[];
  } {
    // 统计星期几的偏好
    const weekdayCounts = new Array(7).fill(0);
    for (const day of weekdays) {
      weekdayCounts[day]++;
    }

    // 统计小时的偏好
    const hourCounts = new Array(24).fill(0);
    for (const hour of hours) {
      hourCounts[hour]++;
    }

    // 归一化
    const totalWeekdays = weekdays.length || 1;
    const totalHours = hours.length || 1;

    return {
      weekday: weekdayCounts.map(count => count / totalWeekdays),
      hour: hourCounts.map(count => count / totalHours),
    };
  }

  /**
   * 计算参与人数偏好
   */
  private calculateParticipantPreference(counts: number[]): {
    avg: number;
    std: number;
  } {
    if (counts.length === 0) {
      return { avg: 20, std: 10 }; // 默认值
    }

    const avg = counts.reduce((a, b) => a + b, 0) / counts.length;
    const variance = counts.reduce((sum, count) => sum + Math.pow(count - avg, 2), 0) / counts.length;
    const std = Math.sqrt(variance);

    return { avg, std };
  }

  /**
   * 检查用户画像是否有效
   */
  private hasValidProfile(profile: UserProfile): boolean {
    return profile.categoryPreferences.size > 0;
  }

  /**
   * 获取候选活动
   */
  private async getCandidateEvents(userId: string): Promise<EventFeatures[]> {
    // 获取用户未交互的活动
    const userBehaviors = await this.behaviorRepository.getUserBehaviors(userId);
    const interactedEventIds = new Set(userBehaviors.map(b => b.eventId));

    const allEvents = await this.eventRepository.getUpcomingEvents();
    const candidateEvents = allEvents
      .filter(event => !interactedEventIds.has(event.id))
      .map(event => this.extractEventFeatures(event));

    return candidateEvents;
  }

  /**
   * 提取活动特征
   */
  private extractEventFeatures(event: any): EventFeatures {
    return {
      eventId: event.id,
      category: event.categoryId,
      location: { lat: event.locationLat || 0, lng: event.locationLng || 0 },
      date: new Date(event.date),
      time: event.time,
      maxParticipants: event.maxParticipants,
      titleKeywords: this.extractKeywords(event.title),
    };
  }

  /**
   * 提取关键词（简化版）
   */
  private extractKeywords(title: string): string[] {
    // 实际应用中应该使用 TF-IDF 或 Word2Vec
    return title.split(' ').filter(word => word.length > 2);
  }

  /**
   * 计算每个活动的推荐分数
   */
  private async scoreEvents(
    userProfile: UserProfile,
    events: EventFeatures[],
  ): Promise<Array<{ eventId: string; score: number }>> {
    const scoredEvents: Array<{ eventId: string; score: number }> = [];

    for (const event of events) {
      const score = await this.calculateEventScore(userProfile, event);
      scoredEvents.push({ eventId: event.eventId, score });
    }

    return scoredEvents;
  }

  /**
   * 计算单个活动的推荐分数
   */
  private async calculateEventScore(
    userProfile: UserProfile,
    event: EventFeatures,
  ): Promise<number> {
    let totalScore = 0;

    // 1. 分类相似度 (权重: 0.3)
    const categoryScore = userProfile.categoryPreferences.get(event.category) || 0;
    totalScore += categoryScore * 0.3;

    // 2. 地点相似度 (权重: 0.25)
    const distance = this.calculateDistance(
      userProfile.locationPreference.lat,
      userProfile.locationPreference.lng,
      event.location.lat,
      event.location.lng,
    );
    const locationScore = Math.max(0, 1 - distance / userProfile.locationPreference.radius);
    totalScore += locationScore * 0.25;

    // 3. 时间偏好匹配度 (权重: 0.2)
    const eventDate = new Date(event.date);
    const weekday = eventDate.getDay();
    const hour = parseInt(event.time.split(':')[0]);
    const weekdayScore = userProfile.timePreferences.weekday[weekday] || 0;
    const hourScore = userProfile.timePreferences.hour[hour] || 0;
    totalScore += (weekdayScore + hourScore) / 2 * 0.2;

    // 4. 参与人数偏好匹配度 (权重: 0.15)
    const participantScore = this.calculateGaussianSimilarity(
      event.maxParticipants,
      userProfile.participantPreference.avg,
      userProfile.participantPreference.std,
    );
    totalScore += participantScore * 0.15;

    // 5. 其他因素 (权重: 0.1)
    // 可以加入活动热度、组织者评分等
    totalScore += 0.1;

    return totalScore;
  }

  /**
   * 计算高斯相似度
   */
  private calculateGaussianSimilarity(value: number, mean: number, std: number): number {
    return Math.exp(-Math.pow(value - mean, 2) / (2 * Math.pow(std, 2)));
  }

  /**
   * 获取回退推荐
   */
  private async getFallbackRecommendations(topN: number): Promise<string[]> {
    const hotEvents = await this.eventRepository.getHotEvents(topN);
    return hotEvents.map(event => event.id);
  }
}
```

### 5.4 混合推荐算法实现

#### 5.4.1 算法原理

混合推荐结合 User-CF 和 Content-Based 的优点：
- User-CF：解决冷启动问题，发现新兴趣
- Content-Based：提供个性化，推荐可解释性强

**加权融合策略：**
```
最终分数 = α * User-CF分数 + β * Content-Based分数
其中 α + β = 1
```

#### 5.4.2 代码实现

**位置：`backend/services/recommendation/models/hybrid.ts`**

```typescript
/**
 * 混合推荐算法
 *
 * 结合 User-CF 和 Content-Based 的优势
 * 使用加权融合策略
 */

import { Injectable } from '@nestjs/common';
import { UserCFService } from './user-cf';
import { ContentBasedService } from './content-based';
import { BehaviorRepository } from '../../repositories/behavior.repository';

interface RecommendationResult {
  eventId: string;
  score: number;
  sources: {
    cf: number;
    cb: number;
  };
}

@Injectable()
export class HybridRecommendationService {
  private readonly CF_WEIGHT = 0.6; // User-CF 权重
  private readonly CB_WEIGHT = 0.4; // Content-Based 权重
  private readonly DIVERSITY_THRESHOLD = 0.7; // 多样性阈值

  constructor(
    private readonly userCFService: UserCFService,
    private readonly contentBasedService: ContentBasedService,
    private readonly behaviorRepository: BehaviorRepository,
  ) {}

  /**
   * 获取混合推荐
   */
  async getRecommendations(userId: string, topN: number = 10): Promise<string[]> {
    // 1. 动态调整权重（根据用户活跃度）
    const weights = await this.calculateDynamicWeights(userId);

    // 2. 分别获取 CF 和 CB 的推荐结果
    const [cfRecommendations, cbRecommendations] = await Promise.all([
      this.userCFService.getRecommendations(userId, topN * 2),
      this.contentBasedService.getRecommendations(userId, topN * 2),
    ]);

    // 3. 融合两个推荐列表
    const mergedScores = this.mergeRecommendations(
      cfRecommendations,
      cbRecommendations,
      weights.cf,
      weights.cb,
    );

    // 4. 多样性处理（MMR 算法）
    const diversifiedScores = this.applyMMR(mergedScores);

    // 5. 返回 Top N
    return diversifiedScores.slice(0, topN).map(item => item.eventId);
  }

  /**
   * 动态计算权重
   * 根据用户历史行为数量动态调整 CF 和 CB 的权重
   */
  private async calculateDynamicWeights(userId: string): Promise<{ cf: number; cb: number }> {
    const userBehaviors = await this.behaviorRepository.getUserBehaviors(userId);
    const behaviorCount = userBehaviors.length;

    // 新用户：更依赖内容推荐
    if (behaviorCount < 10) {
      return { cf: 0.3, cb: 0.7 };
    }

    // 活跃用户：更依赖协同过滤
    if (behaviorCount > 50) {
      return { cf: 0.7, cb: 0.3 };
    }

    // 中等用户：使用默认权重
    return { cf: this.CF_WEIGHT, cb: this.CB_WEIGHT };
  }

  /**
   * 融合两个推荐列表
   */
  private mergeRecommendations(
    cfRecs: string[],
    cbRecs: string[],
    cfWeight: number,
    cbWeight: number,
  ): RecommendationResult[] {
    const scoreMap = new Map<string, RecommendationResult>();

    // 处理 CF 推荐
    for (let i = 0; i < cfRecs.length; i++) {
      const eventId = cfRecs[i];
      const score = (cfRecs.length - i) / cfRecs.length; // 归一化分数

      if (!scoreMap.has(eventId)) {
        scoreMap.set(eventId, {
          eventId,
          score: 0,
          sources: { cf: 0, cb: 0 },
        });
      }

      scoreMap.get(eventId)!.sources.cf = score;
    }

    // 处理 CB 推荐
    for (let i = 0; i < cbRecs.length; i++) {
      const eventId = cbRecs[i];
      const score = (cbRecs.length - i) / cbRecs.length; // 归一化分数

      if (!scoreMap.has(eventId)) {
        scoreMap.set(eventId, {
          eventId,
          score: 0,
          sources: { cf: 0, cb: 0 },
        });
      }

      scoreMap.get(eventId)!.sources.cb = score;
    }

    // 计算加权分数
    const results: RecommendationResult[] = [];
    for (const [eventId, data] of scoreMap.entries()) {
      // 归一化分数
      const cfScore = data.sources.cf;
      const cbScore = data.sources.cb;
      const combinedScore = cfWeight * cfScore + cbWeight * cbScore;

      results.push({
        eventId,
        score: combinedScore,
        sources: { cf: cfScore, cb: cbScore },
      });
    }

    return results.sort((a, b) => b.score - a.score);
  }

  /**
   * 应用 MMR (Maximal Marginal Relevance) 算法增加多样性
   */
  private applyMMR(
    recommendations: RecommendationResult[],
    lambda: number = 0.7,
  ): RecommendationResult[] {
    const selected: RecommendationResult[] = [];
    const remaining = [...recommendations];

    // 选择分数最高的作为第一个
    if (remaining.length > 0) {
      selected.push(remaining.shift()!);
    }

    while (remaining.length > 0 && selected.length < 10) {
      let bestItem: RecommendationResult | null = null;
      let bestScore = -Infinity;

      for (const candidate of remaining) {
        // 计算相关分数
        const relevanceScore = candidate.score;

        // 计算与已选择项目的最大相似度
        let maxSimilarity = 0;
        for (const selectedItem of selected) {
          const similarity = this.calculateSimilarity(candidate, selectedItem);
          maxSimilarity = Math.max(maxSimilarity, similarity);
        }

        // MMR 分数
        const mmrScore = lambda * relevanceScore - (1 - lambda) * maxSimilarity;

        if (mmrScore > bestScore) {
          bestScore = mmrScore;
          bestItem = candidate;
        }
      }

      if (bestItem) {
        selected.push(bestItem);
        remaining.splice(remaining.indexOf(bestItem), 1);
      } else {
        break;
      }
    }

    return selected;
  }

  /**
   * 计算两个活动之间的相似度
   */
  private calculateSimilarity(item1: RecommendationResult, item2: RecommendationResult): number {
    // 简化版：使用 Jaccard 相似度
    const set1 = new Set([item1.sources.cf, item1.sources.cb]);
    const set2 = new Set([item2.sources.cf, item2.sources.cb]);

    const intersection = new Set([...set1].filter(x => set2.has(x)));
    const union = new Set([...set1, ...set2]);

    return intersection.size / union.size;
  }
}
```

### 5.5 前端集成推荐功能

#### 5.5.1 API Service 调用

**位置：`src/services/recommendation.ts`**

```typescript
/**
 * 推荐服务 API 调用
 */

import { api } from './api';

export interface RecommendationResponse {
  events: Event[];
  algorithm_used: string;
  explanation?: string;
}

export const recommendationService = {
  /**
   * 获取推荐活动
   * @param algorithm 算法类型：'cf' | 'cb' | 'hybrid'
   * @param page 页码
   * @param limit 每页数量
   */
  getRecommendations: async (params: {
    algorithm?: 'cf' | 'cb' | 'hybrid';
    page?: number;
    limit?: number;
  } = {}): Promise<RecommendationResponse> => {
    return api.get('/events/recommended', { params });
  },

  /**
   * 记录用户行为（用于推荐算法优化）
   * @param eventId 活动ID
   * @param actionType 行为类型
   */
  recordBehavior: async (eventId: string, actionType: BehaviorAction): Promise<void> => {
    return api.post(`/events/${eventId}/behavior`, { action_type: actionType });
  },
};

// 行为类型定义
export type BehaviorAction = 'view' | 'click' | 'join' | 'favorite' | 'share';
```

#### 5.5.2 自定义 Hook

**位置：`src/hooks/useRecommendation.ts`**

```typescript
/**
 * 推荐数据 Hook
 */

import { useQuery } from '@tanstack/react-query';
import { recommendationService } from '../services/recommendation';

export const useRecommendations = (algorithm: 'cf' | 'cb' | 'hybrid' = 'hybrid') => {
  return useQuery({
    queryKey: ['recommendations', algorithm],
    queryFn: () => recommendationService.getRecommendations({ algorithm }),
    staleTime: 10 * 60 * 1000, // 10 分钟缓存
    retry: 1,
  });
};

export const useRecordBehavior = () => {
  const recordBehavior = async (eventId: string, actionType: BehaviorAction) => {
    try {
      await recommendationService.recordBehavior(eventId, actionType);
    } catch (error) {
      console.error('Failed to record behavior:', error);
    }
  };

  return { recordBehavior };
};
```

#### 5.5.3 在首页中使用推荐

**位置：`src/pages/Home.tsx`（修改版）**

```typescript
import { useRecommendations } from '../hooks/useRecommendation';
import { useRecordBehavior } from '../hooks/useRecommendation';
import { useEffect } from 'react';

const Home = () => {
  // 使用推荐算法获取活动
  const { data: recommendedData, isLoading } = useRecommendations('hybrid');
  const { recordBehavior } = useRecordBehavior();

  const recommendedEvents = recommendedData?.events || [];

  // 记录用户行为
  const handleEventClick = (eventId: string) => {
    recordBehavior(eventId, 'click');
  };

  const handleEventView = (eventId: string) => {
    recordBehavior(eventId, 'view');
  };

  // 页面加载时记录浏览行为
  useEffect(() => {
    recommendedEvents.forEach(event => {
      recordBehavior(event.id, 'view');
    });
  }, [recommendedEvents]);

  return (
    <div className="space-y-12">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-r from-primary-600 to-secondary-600">
        {/* ... */}
      </section>

      {/* 推荐活动区域 */}
      <section className="container-custom">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">
            为您推荐
            <span className="text-sm font-normal text-gray-500 ml-2">
              （基于 {recommendedData?.algorithm_used} 算法）
            </span>
          </h2>
          {recommendedData?.explanation && (
            <span className="text-sm text-gray-600">{recommendedData.explanation}</span>
          )}
        </div>

        {isLoading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {recommendedEvents.map((event) => (
              <EventCard
                key={event.id}
                event={event}
                onClick={() => handleEventClick(event.id)}
                onView={() => handleEventView(event.id)}
              />
            ))}
          </div>
        )}
      </section>

      {/* 其他区域 */}
      {/* ... */}
    </div>
  );
};
```

### 5.6 推荐系统评估

#### 5.6.1 评估指标

```typescript
/**
 * 推荐系统评估指标
 */

interface EvaluationMetrics {
  // 准确性指标
  precision: number;      // 精确率
  recall: number;         // 召回率
  f1: number;            // F1 分数
  ndcg: number;          // NDCG (归一化折损累计增益)

  // 多样性指标
  diversity: number;     // 推荐列表多样性

  // 新颖性指标
  novelty: number;       // 推荐新颖性

  // 覆盖率指标
  coverage: number;      // 物品覆盖率
}

export class RecommendationEvaluator {
  /**
   * 计算 Precision@K
   */
  static calculatePrecisionAtK(
    recommendations: string[],
    relevantItems: Set<string>,
    k: number,
  ): number {
    const topK = recommendations.slice(0, k);
    const relevantCount = topK.filter(item => relevantItems.has(item)).length;
    return relevantCount / k;
  }

  /**
   * 计算 Recall@K
   */
  static calculateRecallAtK(
    recommendations: string[],
    relevantItems: Set<string>,
    k: number,
  ): number {
    const topK = recommendations.slice(0, k);
    const relevantCount = topK.filter(item => relevantItems.has(item)).length;
    return relevantCount / relevantItems.size;
  }

  /**
   * 计算 NDCG@K
   */
  static calculateNDCGAtK(
    recommendations: string[],
    relevanceScores: Map<string, number>,
    k: number,
  ): number {
    const topK = recommendations.slice(0, k);

    // 计算 DCG
    let dcg = 0;
    for (let i = 0; i < Math.min(k, topK.length); i++) {
      const relevance = relevanceScores.get(topK[i]) || 0;
      dcg += relevance / Math.log2(i + 2);
    }

    // 计算 IDCG (理想情况下的 DCG)
    const sortedRelevances = Array.from(relevanceScores.values())
      .sort((a, b) => b - a)
      .slice(0, k);
    let idcg = 0;
    for (let i = 0; i < sortedRelevances.length; i++) {
      idcg += sortedRelevances[i] / Math.log2(i + 2);
    }

    return idcg > 0 ? dcg / idcg : 0;
  }

  /**
   * 计算多样性
   */
  static calculateDiversity(
    recommendations: string[],
    eventFeatures: Map<string, any>,
  ): number {
    let totalSimilarity = 0;
    let pairCount = 0;

    for (let i = 0; i < recommendations.length; i++) {
      for (let j = i + 1; j < recommendations.length; j++) {
        const similarity = this.calculateSimilarity(
          recommendations[i],
          recommendations[j],
          eventFeatures,
        );
        totalSimilarity += similarity;
        pairCount++;
      }
    }

    return 1 - (pairCount > 0 ? totalSimilarity / pairCount : 0);
  }

  /**
   * 计算两个活动的相似度
   */
  private static calculateSimilarity(
    eventId1: string,
    eventId2: string,
    eventFeatures: Map<string, any>,
  ): number {
    const features1 = eventFeatures.get(eventId1);
    const features2 = eventFeatures.get(eventId2);

    if (!features1 || !features2) return 0;

    // 计算类别相似度
    const categorySimilarity = features1.category === features2.category ? 1 : 0;

    // 计算位置相似度
    const distance = this.calculateDistance(
      features1.location,
      features2.location,
    );
    const locationSimilarity = Math.max(0, 1 - distance / 10); // 10km 内视为相似

    // 综合相似度
    return 0.6 * categorySimilarity + 0.4 * locationSimilarity;
  }
}
```

---

## 6. 论文撰写要点

### 6.1 论文结构建议

#### 6.1.1 标题
```
"基于混合推荐算法的社区活动平台设计与实现"
或
"融合协同过滤与内容推荐的社区活动智能推荐系统研究"
```

#### 6.1.2 摘要 (Abstract)
```
摘要应包含：
1. 研究背景：社区活动平台的兴起和推荐系统的重要性
2. 问题陈述：现有推荐系统的局限性（冷启动、单一算法）
3. 研究方法：提出基于 User-CF 和 Content-Based 的混合推荐算法
4. 主要贡献：系统设计、算法实现、实验评估
5. 实验结果：算法性能指标（Precision, Recall, NDCG 等）
6. 结论：系统的有效性和实用价值

建议字数：300-500 字
```

#### 6.1.3 关键词
```
关键词：社区活动平台、推荐系统、协同过滤、内容推荐、混合算法、用户体验
```

### 6.2 各章节内容建议

#### 第一章：引言
```
内容要点：
1.1 研究背景
   - 社区活动的重要性
   - 在线活动平台的发展
   - 信息过载问题

1.2 问题陈述
   - 用户难以找到感兴趣的活动
   - 现有平台推荐精度不足
   - 冷启动问题

1.3 研究目标
   - 设计高效的社区活动平台
   - 实现精准的推荐算法
   - 提升用户体验

1.4 论文结构
   - 简要介绍各章节内容
```

#### 第二章：相关工作
```
内容要点：
2.1 推荐系统概述
   - 推荐系统的定义
   - 推荐系统的分类

2.2 协同过滤算法
   - User-based CF
   - Item-based CF
   - 矩阵分解

2.3 基于内容的推荐
   - 内容特征提取
   - 相似度计算

2.4 混合推荐算法
   - 加权融合
   - 级联融合
   - 切换融合

2.5 现有研究不足
   - 指出当前研究的局限性
   - 引出本文的创新点
```

#### 第三章：系统设计
```
内容要点：
3.1 系统架构
   - 整体架构图
   - 前端架构
   - 后端架构
   - 推荐系统架构

3.2 数据库设计
   - ER 图
   - 数据表设计
   - 索引优化

3.3 推荐系统设计
   - 数据采集层
   - 特征工程层
   - 算法模型层
   - 召回排序层

3.4 技术选型
   - 前端技术栈
   - 后端技术栈
   - 推荐算法实现
```

#### 第四章：推荐算法实现
```
内容要点：
4.1 User-CF 算法
   - 算法原理
   - 相似度计算方法
   - 实现细节
   - 伪代码

4.2 Content-Based 算法
   - 用户画像构建
   - 特征提取
   - 相似度计算
   - 实现细节

4.3 混合算法
   - 融合策略
   - 权重动态调整
   - 多样性优化（MMR）
   - 算法流程图

4.4 用户行为分析
   - 行为类型定义
   - 行为权重分配
   - 数据采集机制
```

#### 第五章：系统实现
```
内容要点：
5.1 前端实现
   - 组件设计
   - 路由配置
   - 状态管理
   - API 集成

5.2 后端实现
   - 接口设计
   - 数据访问层
   - 业务逻辑层
   - 推荐服务

5.3 推荐系统集成
   - 前端调用
   - 后端响应
   - 数据流转
   - 实时更新
```

#### 第六章：实验与评估
```
内容要点：
6.1 数据集
   - 数据来源
   - 数据规模
   - 数据预处理

6.2 评估指标
   - 准确性指标（Precision, Recall, F1, NDCG）
   - 多样性指标
   - 新颖性指标

6.3 实验设计
   - 对照组设计
   - 参数设置
   - 实验环境

6.4 实验结果
   - 算法对比
   - 消融实验
   - 参数敏感性分析
   - 结果分析

6.5 用户研究
   - 问卷设计
   - 用户反馈
   - 可用性测试
```

#### 第七章：结论与展望
```
内容要点：
7.1 研究总结
   - 主要工作
   - 创新点
   - 研究成果

7.2 局限性
   - 当前系统的不足
   - 数据规模的限制
   - 算法的改进空间

7.3 未来工作
   - 引入深度学习模型
   - 多目标优化
   - 实时推荐优化
   - 个性化解释
```

### 6.3 论文创新点建议

#### 6.3.1 技术创新
```
1. 提出动态权重调整机制
   - 根据用户活跃度动态调整 User-CF 和 Content-Based 的权重
   - 新用户依赖内容推荐，活跃用户依赖协同过滤

2. 引入 MMR 算法增加多样性
   - 在混合推荐中应用 Maximal Marginal Relevance
   - 平衡准确性和多样性

3. 多维度用户行为分析
   - 综合考虑浏览、点击、收藏、报名等多种行为
   - 不同行为赋予不同权重

4. 可解释性推荐
   - 为每个推荐结果提供解释说明
   - 提升用户信任度和满意度
```

#### 6.3.2 应用创新
```
1. 社区活动场景的推荐系统应用
   - 针对社区活动的特点设计推荐算法
   - 考虑时间、地点、分类等因素

2. 实时推荐优化
   - 用户行为实时采集和处理
   - 推荐结果实时更新

3. 全栈系统实现
   - 从前端到后端的完整实现
   - 推荐算法与业务系统的深度集成
```

### 6.4 实验部分建议

#### 6.4.1 实验设计
```typescript
// 实验对比算法
const algorithms = [
  { name: 'User-CF', weight: { cf: 1, cb: 0 } },
  { name: 'Content-Based', weight: { cf: 0, cb: 1 } },
  { name: 'Hybrid (0.5:0.5)', weight: { cf: 0.5, cb: 0.5 } },
  { name: 'Hybrid (0.6:0.4)', weight: { cf: 0.6, cb: 0.4 } },
  { name: 'Hybrid (0.7:0.3)', weight: { cf: 0.7, cb: 0.3 } },
  { name: 'Dynamic Hybrid', weight: 'dynamic' }, // 动态权重
  { name: 'Hybrid + MMR', weight: { cf: 0.6, cb: 0.4 }, mmr: true },
];

// 评估指标
const metrics = {
  precision: [],   // Precision@K, K = 5, 10, 20
  recall: [],      // Recall@K, K = 5, 10, 20
  ndcg: [],        // NDCG@K, K = 5, 10, 20
  diversity: [],   // 推荐列表多样性
  novelty: [],     // 推荐新颖性
};
```

#### 6.4.2 结果展示建议

**表 1：算法性能对比**
```
算法                Precision@5    Recall@5    NDCG@5    Diversity
User-CF             0.42          0.28        0.52      0.35
Content-Based       0.38          0.32        0.48      0.42
Hybrid (0.5:0.5)     0.45          0.30        0.55      0.38
Hybrid (0.6:0.4)     0.48          0.31        0.58      0.36
Dynamic Hybrid      0.51          0.34        0.62      0.41
Hybrid + MMR        0.47          0.32        0.59      0.52
```

**图 1：不同 K 值下的 Precision 对比**
```
Precision
0.6 ┤              *
    │           *     *
0.5 ┤        *           *
    │     *                 *
0.4 ┤  *                       *
    │
0.3 ┤
    └─────────────────────────
      5   10  15  20  25
           K
```

### 6.5 写作注意事项

#### 6.5.1 学术规范
```
1. 引用规范
   - 所有引用都要标注出处
   - 使用标准的引用格式（APA, IEEE 等）
   - 避免抄袭

2. 术语规范
   - 首次出现时给出定义
   - 全文术语保持一致
   - 使用行业标准术语

3. 格式规范
   - 遵循期刊或会议的格式要求
   - 图表要有编号和标题
   - 公式要编号并解释
```

#### 6.5.2 写作技巧
```
1. 逻辑清晰
   - 段落之间要有过渡
   - 论点要有论据支持
   - 避免跳跃性思维

2. 语言准确
   - 使用客观、准确的语言
   - 避免模糊不清的表达
   - 适当使用专业术语

3. 图文并茂
   - 关键部分使用图表说明
   - 算法流程使用流程图
   - 系统架构使用架构图
```

### 6.6 可发表的会议/期刊建议

#### 6.6.1 国际会议
```
1. RecSys (ACM Conference on Recommender Systems)
   - 推荐系统顶级会议
   - 注重算法创新和实验验证

2. KDD (ACM SIGKDD Conference on Knowledge Discovery and Data Mining)
   - 数据挖掘顶级会议
   - 适合有创新性算法的工作

3. WWW (The Web Conference)
   - Web 技术会议
   - 适合 Web 应用的推荐系统

4. CIKM (International Conference on Information and Knowledge Management)
   - 信息管理会议
   - 适合推荐系统应用研究
```

#### 6.6.2 国内会议/期刊
```
1. CCF A 类期刊
   - 《软件学报》
   - 《计算机学报》
   - 《计算机研究与发展》

2. CCF B 类期刊
   - 《计算机科学》
   - 《小型微型计算机系统》
   - 《中文信息学报》

3. 会议
   - CCF 中国计算机大会
   - 全国推荐系统大会
```

---

## 附录：快速参考

### A. 项目启动命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码检查
npm run lint
```

### B. 主要文件位置

```
前端：
- src/services/recommendation.ts      # 推荐 API 调用
- src/hooks/useRecommendation.ts       # 推荐 Hook
- src/pages/Home.tsx                   # 首页（展示推荐）

后端（需创建）：
- backend/services/recommendation/
  - models/
    - user-cf.ts                       # User-CF 算法 ⭐
    - content-based.ts                 # Content-Based 算法 ⭐
    - hybrid.ts                        # 混合算法 ⭐
  - recall.ts                          # 召回层
  - ranking.ts                         # 排序层
  - reranking.ts                       # 重排层
```

### C. API 接口清单

```
推荐相关接口：
GET  /api/events/recommended          # 获取推荐活动
POST /api/events/:id/behavior          # 记录用户行为
```

---

## 总结

本文档详细介绍了社区活动平台的完整设计、实现、后端集成以及推荐算法的实现方案。主要内容包括：

1. **设计思路**：采用前后端分离架构，组件化设计，响应式布局
2. **实现步骤**：从项目初始化到功能实现的完整流程
3. **实现原理**：前端核心技术和数据处理原理
4. **后端集成**：完整的后端架构设计和 API 接口规范
5. **推荐算法**：User-CF、Content-Based、混合算法的完整实现
6. **论文撰写**：论文结构、创新点、实验设计等建议

推荐算法的核心代码位于：
- **前端**：`src/services/recommendation.ts` 和 `src/hooks/useRecommendation.ts`
- **后端**：`backend/services/recommendation/models/` 目录下的三个算法文件

通过本文档，你可以：
- 理解系统的完整架构和实现原理
- 实现完整的后端 API 和推荐算法
- 将推荐算法集成到现有系统中
- 撰写高质量的学术论文

祝你的论文撰写顺利！
