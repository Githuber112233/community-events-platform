<template>
  <div class="container-custom py-8">
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-16">
      <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-500">加载个人资料...</p>
    </div>

    <!-- Not Logged In (when trying to view own profile without login) -->
    <div v-else-if="!isLoggedIn && !viewUserId" class="py-16 text-center">
      <h2 class="text-2xl font-bold text-gray-900 mb-4">请先登录</h2>
      <RouterLink to="/login" class="btn-primary inline-flex items-center">
        去登录
      </RouterLink>
    </div>

    <!-- User Not Found -->
    <div v-else-if="!loading && !user" class="py-16 text-center">
      <h2 class="text-2xl font-bold text-gray-900 mb-4">用户不存在</h2>
      <RouterLink to="/" class="btn-primary inline-flex items-center">
        返回首页
      </RouterLink>
    </div>

    <template v-else>
      <!-- Profile Header -->
      <div class="card p-6 mb-8">
        <div class="flex flex-col md:flex-row items-start md:items-center space-y-4 md:space-y-0 md:space-x-6">
          <div class="relative">
            <img
              :src="user?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user?.id}`"
              :alt="user?.nickname || user?.username"
              class="w-32 h-32 rounded-full border-4 border-primary-200"
            />
            <!-- 编辑按钮：仅在看自己的主页时显示 -->
            <button
              v-if="!viewUserId"
              @click="isEditing = !isEditing"
              class="absolute bottom-0 right-0 bg-primary-600 text-white p-2 rounded-full hover:bg-primary-700 transition-colors"
            >
              <Edit class="w-4 h-4" />
            </button>
          </div>

          <div class="flex-1">
            <div class="flex items-start md:items-center justify-between">
              <div>
                <h1 class="text-2xl font-bold text-gray-900 mb-1">{{ user?.nickname || user?.username }}</h1>
                <p class="text-gray-600 mb-2">@{{ user?.username }}</p>
              </div>
              <div class="flex items-center gap-2">
                <!-- 关注按钮：仅在看他人主页时显示 -->
                <button
                  v-if="viewUserId && isLoggedIn"
                  class="follow-btn"
                  :class="{ following: isFollowing }"
                  :disabled="loadingFollow"
                  @click="handleFollow"
                >
                  <span v-if="loadingFollow" class="loading-spinner"></span>
                  <template v-else>
                    {{ isFollowing ? '已关注' : '关注' }}
                  </template>
                </button>
                <!-- 设置按钮：仅在看自己的主页时显示 -->
                <button v-if="!viewUserId" class="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-full transition-colors">
                  <Settings class="w-6 h-6" />
                </button>
              </div>
            </div>

            <div class="flex flex-wrap gap-4 text-sm text-gray-600 mb-3">
              <div v-if="user?.location" class="flex items-center space-x-1">
                <MapPin class="w-4 h-4" />
                <span>{{ user.location }}</span>
              </div>
              <!-- 邮箱：仅在看自己的主页时显示 -->
              <div v-if="!viewUserId && user?.email" class="flex items-center space-x-1">
                <Mail class="w-4 h-4" />
                <span>{{ user.email }}</span>
              </div>
              <div class="flex items-center space-x-1">
                <Calendar class="w-4 h-4" />
                <span>加入于 {{ formatDate(user?.createdAt) }}</span>
              </div>
            </div>

            <textarea
              v-if="isEditing"
              v-model="editBio"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none resize-none"
              rows="3"
              placeholder="介绍一下你自己..."
            />
            <p v-else-if="user?.bio" class="text-gray-700">{{ user.bio }}</p>
            <p v-else class="text-gray-400 italic">暂无个人简介</p>

            <div v-if="isEditing" class="mt-3 flex gap-2">
              <button @click="saveProfile" class="btn-primary text-sm py-2 px-4">保存</button>
              <button @click="isEditing = false" class="btn-secondary text-sm py-2 px-4">取消</button>
            </div>
          </div>
        </div>

        <!-- Stats -->
        <div class="grid grid-cols-2 md:grid-cols-6 gap-4 mt-6 pt-6 border-t border-gray-200">
          <div class="text-center cursor-pointer hover:bg-gray-50 rounded-lg p-2" @click="activeTab = 'my-events'">
            <p class="text-2xl font-bold text-gray-900">{{ myEvents.length }}</p>
            <p class="text-sm text-gray-600">发布活动</p>
          </div>
          <div class="text-center cursor-pointer hover:bg-gray-50 rounded-lg p-2" @click="activeTab = 'participating'">
            <p class="text-2xl font-bold text-gray-900">{{ participatingEvents.length }}</p>
            <p class="text-sm text-gray-600">参加活动</p>
          </div>
          <div class="text-center cursor-pointer hover:bg-gray-50 rounded-lg p-2" @click="activeTab = 'liked'">
            <p class="text-2xl font-bold text-gray-900">{{ likedEvents.length }}</p>
            <p class="text-sm text-gray-600">收藏活动</p>
          </div>
          <div class="text-center cursor-pointer hover:bg-gray-50 rounded-lg p-2" @click="goToFriendsList('followers')">
            <p class="text-2xl font-bold text-gray-900">{{ followersCount }}</p>
            <p class="text-sm text-gray-600 flex items-center justify-center gap-1">
              <Users class="w-3 h-3" /> 粉丝
            </p>
          </div>
          <div class="text-center cursor-pointer hover:bg-gray-50 rounded-lg p-2" @click="goToFriendsList('following')">
            <p class="text-2xl font-bold text-gray-900">{{ followingCount }}</p>
            <p class="text-sm text-gray-600 flex items-center justify-center gap-1">
              <Users class="w-3 h-3" /> 关注
            </p>
          </div>
          <div class="text-center">
            <p class="text-2xl font-bold text-gray-900">{{ totalLikes }}</p>
            <p class="text-sm text-gray-600">获得点赞</p>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex space-x-2 mb-6 overflow-x-auto">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id; loadEvents(tab.id)"
          :class="[
            'flex items-center space-x-2 px-6 py-3 rounded-lg font-medium transition-all whitespace-nowrap',
            activeTab === tab.id
              ? 'bg-primary-600 text-white'
              : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'
          ]"
        >
          <component :is="tab.icon" class="w-4 h-4" />
          <span>{{ tab.label }}</span>
        </button>
      </div>

      <!-- Events Loading -->
      <div v-if="loadingEvents" class="text-center py-8">
        <div class="w-8 h-8 border-2 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto"></div>
        <p class="text-gray-500 mt-2">加载中...</p>
      </div>

      <!-- Events Grid -->
      <div v-else-if="currentEvents.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <EventCard v-for="event in currentEvents" :key="event.id" :event="transformEvent(event)" />
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-16">
        <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <Calendar class="w-12 h-12 text-gray-400" />
        </div>
        <h3 class="text-xl font-bold text-gray-900 mb-2">暂无活动</h3>
        <p class="text-gray-600 mb-4">
          <template v-if="activeTab === 'my-events'">还没有发布任何活动，去发布一个吧！</template>
          <template v-else-if="activeTab === 'participating'">还没有参加任何活动，去发现有趣的活动吧！</template>
          <template v-else-if="activeTab === 'liked'">还没有收藏任何活动，去浏览一下吧！</template>
        </p>
        <RouterLink v-if="activeTab !== 'liked'" to="/create-event" class="btn-primary inline-flex items-center">
          发布活动
        </RouterLink>
        <RouterLink v-else to="/events" class="btn-primary inline-flex items-center">
          发现活动
        </RouterLink>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Calendar, MapPin, Mail, Edit, Settings, CalendarDays, Star, Users } from 'lucide-vue-next'
import { activityApi, userApi, followApi, type Activity, type User } from '../utils/mockData'
import EventCard from '../components/EventCard.vue'

const route = useRoute()
const router = useRouter()
// viewUserId 不为空表示查看他人主页
const viewUserId = computed(() => route.params.id ? Number(route.params.id) : null)

const user = ref<User | null>(null)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const loading = ref(true)
const isEditing = ref(false)
const editBio = ref('')

// 粉丝/关注
const followersCount = ref(0)
const followingCount = ref(0)
const isFollowing = ref(false)
const loadingFollow = ref(false)
const currentUserId = computed(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      return JSON.parse(userStr).id
    } catch {}
  }
  return null
})

const activeTab = ref('my-events')
const loadingEvents = ref(false)

const myEvents = ref<Activity[]>([])
const participatingEvents = ref<Activity[]>([])
const likedEvents = ref<Activity[]>([])

const tabs = [
  { id: 'my-events', label: '我发布的', icon: Calendar },
  { id: 'participating', label: '我参加的', icon: CalendarDays },
  { id: 'liked', label: '收藏的', icon: Star },
]

const totalLikes = computed(() => {
  return myEvents.value.reduce((sum, e) => sum + (e.likeCount || 0), 0)
})

const currentEvents = computed(() => {
  switch (activeTab.value) {
    case 'my-events': return myEvents.value
    case 'participating': return participatingEvents.value
    case 'liked': return likedEvents.value
    default: return []
  }
})

// 根据兴趣分类生成默认封面图
const getDefaultCoverImage = (interestName?: string): string => {
  const covers: Record<string, string> = {
    '体育': 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800',
    '篮球': 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800',
    '足球': 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800',
    '跑步': 'https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=800',
    '健身': 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=800',
    '文化艺术': 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800',
    '绘画': 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800',
    '读书': 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800',
    '科技': 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800',
    '编程': 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800',
    'Python': 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=800',
    '社交': 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800',
    '聚会': 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800',
    '咖啡': 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800',
    '户外': 'https://images.unsplash.com/photo-1551632811-561732d1e306?w=800',
    '登山': 'https://images.unsplash.com/photo-1551632811-561732d1e306?w=800',
    '徒步': 'https://images.unsplash.com/photo-1551632811-561732d1e306?w=800',
    '志愿': 'https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=800',
    '公益': 'https://images.unsplash.com/photo-1559027615-cd4628902d4a?w=800',
    '桌游': 'https://images.unsplash.com/photo-1610890716171-6b1c9f2bd40c?w=800',
    '摄影': 'https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=800',
    '音乐': 'https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=800',
    '电影': 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800',
    '旅行': 'https://images.unsplash.com/photo-1488085061387-422e29b40080?w=800',
  }
  if (!interestName) return 'https://picsum.photos/seed/' + Math.random() + '/400/300'
  for (const [key, url] of Object.entries(covers)) {
    if (interestName.includes(key)) return url
  }
  return 'https://picsum.photos/seed/' + Math.random() + '/400/300'
}

const transformEvent = (event: Activity) => {
  const firstInterest = event.interests?.[0]
  return {
    ...event,
    participants: event.currentParticipants,
    category: firstInterest?.name || '综合',
    images: event.coverImage ? [event.coverImage] : [getDefaultCoverImage(firstInterest?.name)],
    date: event.startTime ? event.startTime.split('T')[0] : '',
    time: event.startTime ? event.startTime.split('T')[1]?.substring(0, 5) : '',
    location: event.address || `${event.city || ''}${event.district || ''}`,
    organizer: event.creator ? {
      name: event.creator.nickname || event.creator.username,
      avatar: event.creator.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${event.creator.id}`
    } : { name: '未知', avatar: '' }
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const loadUserInfo = async () => {
  // 如果有 viewUserId，获取该用户的公开信息
  if (viewUserId.value) {
    try {
      const res = await userApi.getUserInfo(viewUserId.value)
      if (res.code === 200) {
        user.value = res.data
      } else {
        user.value = null
      }
    } catch {
      user.value = null
    }
  } else {
    // 获取当前登录用户的信息
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        user.value = JSON.parse(userStr)
        editBio.value = user.value?.bio || ''
      } catch {}
    }
  }
  loading.value = false
}

const loadEvents = async (type: string) => {
  if (!user.value) return

  loadingEvents.value = true
  // 转换前端 tab id 到后端 type
  const typeMap: Record<string, string> = {
    'my-events': 'created',
    'participating': 'participating',
    'liked': 'liked'
  }
  const backendType = typeMap[type] || 'created'

  try {
    const res = await userApi.getUserActivities(backendType as any)
    if (res.code === 200) {
      switch (type) {
        case 'my-events':
          myEvents.value = res.data || []
          break
        case 'participating':
          participatingEvents.value = res.data || []
          break
        case 'liked':
          likedEvents.value = res.data || []
          break
      }
    }
  } catch (e) {
    console.error('加载活动失败:', e)
  } finally {
    loadingEvents.value = false
  }
}

const saveProfile = async () => {
  if (!user.value) return

  try {
    const res = await userApi.updateUserInfo({ bio: editBio.value })
    if (res.code === 200) {
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      isEditing.value = false
    }
  } catch (e) {
    console.error('保存失败:', e)
  }
}

// 检查关注状态
const checkFollowStatus = async () => {
  if (!user.value || !currentUserId.value || user.value.id === currentUserId.value) return

  try {
    const res = await followApi.getFollowing(currentUserId.value)
    if (res.code === 200 && res.data) {
      isFollowing.value = res.data.some((u: User) => u.id === user.value?.id)
    }
  } catch (e) {
    console.error('检查关注状态失败:', e)
  }
}

// 获取粉丝和关注数量
const loadFollowCounts = async () => {
  if (!user.value) return

  try {
    const [followersRes, followingRes] = await Promise.all([
      followApi.getFollowers(user.value.id),
      followApi.getFollowing(user.value.id)
    ])
    if (followersRes.code === 200) {
      followersCount.value = followersRes.data?.length || 0
    }
    if (followingRes.code === 200) {
      followingCount.value = followingRes.data?.length || 0
    }
  } catch (e) {
    console.error('加载关注数量失败:', e)
  }
}

// 关注/取消关注
const handleFollow = async () => {
  if (!user.value || !isLoggedIn.value) {
    router.push('/login')
    return
  }

  loadingFollow.value = true
  try {
    if (isFollowing.value) {
      await followApi.unfollowUser(user.value.id)
      isFollowing.value = false
      followersCount.value--
    } else {
      await followApi.followUser(user.value.id)
      isFollowing.value = true
      followersCount.value++
    }
  } catch (e) {
    console.error('操作失败:', e)
  } finally {
    loadingFollow.value = false
  }
}

// 跳转到好友列表
const goToFriendsList = (type: 'followers' | 'following') => {
  if (user.value) {
    router.push(`/friends/${type}/${user.value.id}`)
  }
}

onMounted(async () => {
  await loadUserInfo()
  if (user.value) {
    await Promise.all([
      loadEvents(activeTab.value),
      loadFollowCounts(),
      checkFollowStatus()
    ])
  }
})
</script>

<style scoped>
/* 关注按钮样式 */
.follow-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 999px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 2px solid #111;
  background: #111;
  color: #fff;
  min-width: 100px;
}

.follow-btn:hover:not(:disabled) {
  background: #333;
  border-color: #333;
  transform: translateY(-1px);
}

.follow-btn.following {
  background: transparent;
  color: #111;
}

.follow-btn.following:hover:not(:disabled) {
  background: rgba(255, 75, 75, 0.1);
  border-color: #ff4b4b;
  color: #ff4b4b;
}

.follow-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.follow-btn.following .loading-spinner {
  border-color: rgba(255, 75, 75, 0.3);
  border-top-color: #ff4b4b;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 640px) {
  .follow-btn {
    padding: 8px 16px;
    font-size: 0.85rem;
    min-width: 80px;
  }
}
</style>
