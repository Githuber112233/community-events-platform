/**
 * API 服务层
 * 对接后端 Spring Boot API
 */

// 开发环境使用相对路径，通过 Vite 代理转发
const API_BASE_URL = '/api'

// 获取Token
function getToken(): string | null {
  return localStorage.getItem('token')
}

// 获取请求头
function getHeaders(): HeadersInit {
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  }
  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

// 通用请求封装
export async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers: {
      ...getHeaders(),
      ...options.headers,
    },
  })

  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  return response.json()
}

// ============ 认证相关 ============

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  code: number
  message?: string
  data: {
    user: User
    token: string
  }
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  nickname: string
  avatar?: string
}

export const authApi = {
  login: (data: LoginRequest) =>
    request<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  register: (data: RegisterRequest) =>
    request<{ code: number; message: string; data: UserDTO }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getCurrentUser: () =>
    request<{ code: number; message?: string; data: User }>('/auth/me'),
}

// ============ 用户相关 ============

export interface User {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone?: string
  bio?: string
  location?: string
  interests?: Interest[]
  createdAt: string
  activitiesCount?: number
}

export interface UserDTO {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone?: string
  bio?: string
  location?: string
  interests?: Interest[]
  createdAt: string
  followingCount?: number
  followersCount?: number
  credits?: number
  status?: string
  role?: string
}

export interface Interest {
  id: number
  name: string
  icon: string
  category: string
  popularity: number
}

export const userApi = {
  getUserInfo: (userId: number) =>
    request<{ code: number; message?: string; data: User }>(`/users/${userId}`),

  updateUserInfo: (data: Partial<User>) =>
    request<{ code: number; message?: string; data: User }>('/users/me', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  getUserActivities: (type: 'created' | 'participating' | 'liked') =>
    request<{ code: number; message?: string; data: Activity[] }>(`/users/me/activities?type=${type}`),

  getInterests: () =>
    request<{ code: number; message?: string; data: Interest[] }>('/interests'),
}

// ============ 管理员相关 ============
export interface AdminLoginRequest {
  username: string
  password: string
}

export interface AdminLoginResponse {
  code: number
  message?: string
  data: {
    user: User
    token: string
  }
}

export const adminApi = {
  login: (data: AdminLoginRequest) =>
    request<AdminLoginResponse>('/admin/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
}

// ============ 活动相关 ============

export interface Activity {
  id: number
  title: string
  description: string
  content: string
  coverImage: string
  province: string
  city: string
  district: string
  address: string
  latitude?: number
  longitude?: number
  startTime: string
  endTime: string
  registrationDeadline: string
  maxParticipants: number
  currentParticipants: number
  status: 'RECRUITING' | 'IN_PROGRESS' | 'ENDED' | 'CANCELLED'
  viewCount: number
  likeCount: number
  commentCount: number
  fee: number
  requirements: string
  contactPhone: string
  creator: User
  interests: Interest[]
  createdAt: string
  // 前端兼容字段
  category?: string
  images?: string[]
  date?: string
  time?: string
  location?: string
  organizer?: { name: string; avatar: string }
  isLiked?: boolean
  isParticipated?: boolean
}

export interface PageResponse<T> {
  code: number
  message?: string
  data: {
    content: T[]
    totalElements: number
    totalPages: number
    number: number
    size: number
    first: boolean
    last: boolean
  }
}

export interface ApiResult {
  code: number
  message?: string
}

export const activityApi = {
  // 获取活动列表
  getActivities: (params: { page?: number; size?: number; status?: string; categoryId?: number; keyword?: string; sort?: string } = {}) => {
    let url = `/activities?page=${params.page || 0}&size=${params.size || 10}`
    // status 为 undefined/null 时查所有状态（不传status参数）
    if (params.status) {
      url += `&status=${params.status}`
    }
    if (params.categoryId && params.categoryId > 0) {
      url += `&categoryId=${params.categoryId}`
    }
    if (params.keyword && params.keyword.trim()) {
      url += `&keyword=${encodeURIComponent(params.keyword.trim())}`
    }
    if (params.sort) {
      url += `&sort=${params.sort}`
    }
    return request<PageResponse<Activity>>(url)
  },

  // 获取活动详情
  getActivityDetail: (activityId: number) =>
    request<{ code: number; message?: string; data: Activity }>(`/activities/${activityId}`),

  // 获取活动参与者列表
  getActivityParticipants: (activityId: number, status = 'APPROVED') =>
    request<{ code: number; message?: string; data: any[] }>(`/activities/${activityId}/participants?status=${status}`),

  // 获取活动所有参与者详细信息（仅活动创建者）
  getAllParticipantsWithDetails: (activityId: number) =>
    request<{ code: number; message?: string; data: any[] }>(`/activities/${activityId}/participants/all`),

  // 活动签到（仅活动创建者）
  checkInParticipant: (activityId: number, userId: number) =>
    request<{ code: number; message?: string; data: any }>(`/activities/${activityId}/check-in/${userId}`, {
      method: 'POST',
    }),

  // 取消签到（仅活动创建者）
  cancelCheckIn: (activityId: number, userId: number) =>
    request<ApiResult>(`/activities/${activityId}/check-in/${userId}`, {
      method: 'DELETE',
    }),

  // 获取热门活动
  getPopularActivities: (page = 0, size = 10) =>
    request<PageResponse<Activity>>(`/activities/popular?page=${page}&size=${size}`),

  // 获取附近活动
  getNearbyActivities: (city: string, district: string) =>
    request<{ code: number; message?: string; data: Activity[] }>(`/activities/nearby?city=${city}&district=${district}`),

  // 获取推荐活动
  getRecommendedActivities: (page = 0, size = 10) =>
    request<PageResponse<Activity>>(`/activities/recommended?page=${page}&size=${size}`),

  // 刷新推荐活动
  refreshRecommendedActivities: () =>
    request<{ code: number; message?: string; data: Activity[] }>(`/activities/recommended/refresh`, {
      method: 'POST'
    }),

  // 创建活动
  createActivity: (data: Partial<Activity>) =>
    request<{ code: number; message?: string; data: Activity }>('/activities', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  // 更新活动
  updateActivity: (activityId: number, data: Partial<Activity>) =>
    request<{ code: number; message?: string; data: Activity }>(`/activities/${activityId}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  // 删除活动
  deleteActivity: (activityId: number) =>
    request<ApiResult>(`/activities/${activityId}`, {
      method: 'DELETE',
    }),

  // 参与活动
  participateActivity: (activityId: number, message?: string) =>
    request<ApiResult>(`/activities/${activityId}/participate`, {
      method: 'POST',
      body: JSON.stringify({ message }),
    }),

  // 取消参与
  cancelParticipation: (activityId: number) =>
    request<ApiResult>(`/activities/${activityId}/participate`, {
      method: 'DELETE',
    }),

  // 点赞活动
  likeActivity: (activityId: number) =>
    request<ApiResult>(`/activities/${activityId}/like`, {
      method: 'POST',
    }),

  // 取消点赞
  unlikeActivity: (activityId: number) =>
    request<ApiResult>(`/activities/${activityId}/like`, {
      method: 'DELETE',
    }),
}

// ============ 评论相关 ============

export interface Comment {
  id: number
  content: string
  user: User
  activityId?: number
  parentId?: number
  replies?: Comment[]
  likeCount: number
  createdAt: string
  liked?: boolean
}

export const commentApi = {
  getComments: (activityId: number, page = 0, size = 20) =>
    request<{ code: number; message?: string; data: PageResponse<Comment> }>(`/comments/activities/${activityId}?page=${page}&size=${size}`),

  addComment: (activityId: number, content: string, parentId?: number) =>
    request<{ code: number; message?: string; data: Comment }>(`/comments`, {
      method: 'POST',
      body: JSON.stringify({ activityId, content, parentId }),
    }),

  deleteComment: (commentId: number) =>
    request<ApiResult>(`/comments/${commentId}`, {
      method: 'DELETE',
    }),

  likeComment: (commentId: number) =>
    request<{ code: number; message?: string; data: { likeCount: number } }>(`/comments/${commentId}/like`, {
      method: 'POST',
    }),

  unlikeComment: (commentId: number) =>
    request<{ code: number; message?: string; data: { likeCount: number } }>(`/comments/${commentId}/like`, {
      method: 'DELETE',
    }),
}

// ============ 关注相关 ============

export const followApi = {
  followUser: (userId: number) =>
    request<ApiResult>(`/users/${userId}/follow`, {
      method: 'POST',
    }),

  unfollowUser: (userId: number) =>
    request<ApiResult>(`/users/${userId}/follow`, {
      method: 'DELETE',
    }),

  getFollowers: (userId: number) =>
    request<{ code: number; message?: string; data: User[] }>(`/users/${userId}/followers`),

  getFollowing: (userId: number) =>
    request<{ code: number; message?: string; data: User[] }>(`/users/${userId}/following`),
}

// ============ 通知相关 ============

export const notificationApi = {
  // 获取通知列表
  getNotifications: (page = 0, size = 20) =>
    request<{ code: number; message?: string; data: { content: any[]; totalElements: number; totalPages: number } }>(`/notifications?page=${page}&size=${size}`),

  // 获取未读数量
  getUnreadCount: () =>
    request<{ code: number; message?: string; data: { unreadCount: number } }>('/notifications/unread-count'),

  // 标记单条已读
  markAsRead: (notificationId: number) =>
    request<ApiResult>(`/notifications/${notificationId}/read`, { method: 'PUT' }),

  // 标记全部已读
  markAllAsRead: () =>
    request<ApiResult>('/notifications/read-all', { method: 'PUT' }),

  // 一键通知活动参与者
  notifyParticipants: (activityId: number, title?: string, content?: string) =>
    request<ApiResult>(`/notifications/activity/${activityId}`, {
      method: 'POST',
      body: JSON.stringify({ title, content }),
    }),
}

// 导出所有API
export default {
  auth: authApi,
  user: userApi,
  activity: activityApi,
  comment: commentApi,
  follow: followApi,
  notification: notificationApi,
}
