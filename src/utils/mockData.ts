// 重新导出 api.ts 中的类型，供页面组件使用
export { authApi, activityApi, commentApi, userApi, followApi } from './api'
export type { LoginRequest, LoginResponse, RegisterRequest, User, Interest, Activity, Comment, PageResponse } from './api'

import { ref } from 'vue'
import { Event, Category, User } from '../types';
import { request } from './api';

// 动态加载的分类（从后端获取）
export const categories = ref<Category[]>([])
export const categoriesLoaded = ref(false)

// 从后端加载分类数据
export const loadCategories = async () => {
  if (categoriesLoaded.value) return
  try {
    const res = await request<{ code: number; data: any[] }>('/interests')
    if (res.code === 200 && res.data) {
      // 按category分组，取每个组的第一个interest作为代表
      const categoryMap = new Map<string, { id: string; name: string }>()
      res.data.forEach((interest: any) => {
        if (interest.category && !categoryMap.has(interest.category)) {
          categoryMap.set(interest.category, {
            id: String(interest.id),
            name: interest.category // 使用category作为名称
          })
        }
      })
      categories.value = Array.from(categoryMap.values()).map(c => ({ ...c, icon: '', color: '' }))
      // 确保"体育健身"等主要分类在前
      const priorityCategories = ['体育健身', '文化艺术', '科技学习', '社交聚会', '户外探险', '志愿服务']
      categories.value.sort((a, b) => {
        const aIdx = priorityCategories.indexOf(a.name)
        const bIdx = priorityCategories.indexOf(b.name)
        if (aIdx >= 0 && bIdx >= 0) return aIdx - bIdx
        if (aIdx >= 0) return -1
        if (bIdx >= 0) return 1
        return a.name.localeCompare(b.name)
      })
    }
  } catch (e) {
    console.error('加载分类失败', e)
  } finally {
    categoriesLoaded.value = true
  }
}

export const mockEvents: Event[] = [
  {
    id: 1,
    title: '周末篮球友谊赛',
    description: '欢迎所有篮球爱好者参加！无论你是新手还是高手，都能在这里找到乐趣。我们将提供饮水和基本装备。',
    category: '体育健身',
    date: '2026-03-15',
    time: '14:00',
    location: '朝阳公园篮球场',
    organizer: {
      name: '张伟',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
      id: 1
    },
    participants: 8,
    maxParticipants: 12,
    images: ['https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800'],
    isHot: true
  },
  {
    id: 2,
    title: '社区绘画工作坊',
    description: '专业美术老师带领大家一起创作油画作品，材料免费提供，适合零基础学员。',
    category: '文化艺术',
    date: '2026-03-16',
    time: '10:00',
    location: '社区文化中心',
    organizer: {
      name: '李娜',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Lily',
      id: 2
    },
    participants: 15,
    maxParticipants: 20,
    images: ['https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800'],
    isNew: true
  },
  {
    id: 3,
    title: 'Python编程入门分享会',
    description: '邀请资深工程师分享Python编程入门知识，包括环境搭建、基础语法和实际项目案例。',
    category: '科技学习',
    date: '2026-03-18',
    time: '19:00',
    location: '线上 Zoom',
    organizer: {
      name: '王强',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Max',
      id: 3
    },
    participants: 45,
    maxParticipants: 100,
    images: ['https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800'],
    isHot: true
  },
  {
    id: 4,
    title: '春季登山踏青',
    description: '春天的第一个周末，一起去香山踏青！全程约3公里，适合全家参与。',
    category: '户外探险',
    date: '2026-03-20',
    time: '08:00',
    location: '香山公园东门',
    organizer: {
      name: '赵明',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Mike',
      id: 4
    },
    participants: 22,
    maxParticipants: 30,
    images: ['https://images.unsplash.com/photo-1551632811-561732d1e306?w=800'],
    isNew: true
  },
  {
    id: 5,
    title: '社区老人关怀活动',
    description: '为社区独居老人提供陪伴服务，包括聊天、帮忙购物等。欢迎有时间的朋友报名。',
    category: '志愿服务',
    date: '2026-03-21',
    time: '09:00',
    location: '阳光社区服务站',
    organizer: {
      name: '刘芳',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah',
      id: 5
    },
    participants: 12,
    maxParticipants: 20,
    images: ['https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=800']
  },
  {
    id: 6,
    title: '周末咖啡品鉴会',
    description: '一起品尝来自世界各地的精品咖啡，了解咖啡文化，结交志同道合的朋友。',
    category: '社交聚会',
    date: '2026-03-22',
    time: '15:00',
    location: '星巴克（中关村店）',
    organizer: {
      name: '陈静',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Jane',
      id: 6
    },
    participants: 18,
    maxParticipants: 25,
    images: ['https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800']
  }
];

export const mockUser: User = {
  id: '1',
  name: '张三',
  email: 'zhangsan@example.com',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alex',
  bio: '热爱生活，喜欢参加各种社区活动',
  location: '北京市朝阳区',
  joinedDate: '2025-06-15'
};
