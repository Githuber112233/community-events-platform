<template>
  <div class="container-custom py-8">
    <!-- Loading State -->
    <div v-if="loading" class="text-center py-16">
      <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-500">加载活动详情...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="py-16 text-center">
      <h2 class="text-2xl font-bold text-gray-900 mb-4">{{ error }}</h2>
      <RouterLink to="/events" class="btn-primary inline-flex items-center">
        <ChevronLeft class="w-4 h-4 mr-1" />
        返回活动列表
      </RouterLink>
    </div>

    <!-- Not Found -->
    <div v-else-if="!event" class="py-16 text-center">
      <h2 class="text-2xl font-bold text-gray-900 mb-4">活动未找到</h2>
      <RouterLink to="/events" class="btn-primary inline-flex items-center">
        <ChevronLeft class="w-4 h-4 mr-1" />
        返回活动列表
      </RouterLink>
    </div>

    <template v-else>
      <!-- Back Button -->
      <RouterLink
        to="/events"
        class="inline-flex items-center text-gray-600 hover:text-gray-900 mb-6"
      >
        <ChevronLeft class="w-4 h-4 mr-1" />
        返回活动列表
      </RouterLink>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Content -->
        <div class="lg:col-span-2 space-y-6">
          <!-- Hero Image -->
          <div class="card">
            <div class="relative h-80 overflow-hidden">
              <img
                :src="displayEvent.images[0] || 'https://picsum.photos/800/400'"
                :alt="event.title"
                class="w-full h-full object-cover"
              />
              <div class="absolute top-4 left-4">
                <span class="bg-white/90 backdrop-blur-sm px-4 py-2 rounded-full font-medium text-gray-700">
                  {{ displayEvent.category }}
                </span>
              </div>
            </div>
          </div>

          <!-- Event Info -->
          <div class="card p-6">
            <div class="flex items-start justify-between mb-4">
              <h1 class="text-3xl font-bold text-gray-900 flex-1">
                {{ event.title }}
              </h1>
              <button
                @click="handleLike"
                :class="[
                  'p-2 rounded-full transition-colors',
                  isLiked ? 'bg-red-100 text-red-500' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                ]"
              >
                <Heart :class="['w-6 h-6', isLiked ? 'fill-current' : '']" />
              </button>
            </div>

            <p class="text-gray-600 mb-6 leading-relaxed">
              {{ event.description }}
            </p>

            <!-- Event Details -->
            <div class="space-y-4 mb-6">
              <div class="flex items-start space-x-3">
                <Calendar class="w-5 h-5 text-primary-600 mt-0.5 flex-shrink-0" />
                <div>
                  <p class="font-medium text-gray-900">日期</p>
                  <p class="text-gray-600">{{ displayEvent.date }}</p>
                </div>
              </div>

              <div class="flex items-start space-x-3">
                <Clock class="w-5 h-5 text-primary-600 mt-0.5 flex-shrink-0" />
                <div>
                  <p class="font-medium text-gray-900">时间</p>
                  <p class="text-gray-600">{{ displayEvent.time }}</p>
                </div>
              </div>

              <div class="flex items-start space-x-3">
                <MapPin class="w-5 h-5 text-primary-600 mt-0.5 flex-shrink-0" />
                <div>
                  <p class="font-medium text-gray-900">地点</p>
                  <p class="text-gray-600">{{ displayEvent.location }}</p>
                </div>
              </div>

              <div class="flex items-start space-x-3">
                <Users class="w-5 h-5 text-primary-600 mt-0.5 flex-shrink-0" />
                <div>
                  <p class="font-medium text-gray-900">参与人数</p>
                  <p class="text-gray-600">
                    {{ event.currentParticipants }} / {{ event.maxParticipants }} 人
                  </p>
                  <div class="mt-2">
                    <div class="w-full h-2 bg-gray-200 rounded-full overflow-hidden">
                      <div
                        :class="[
                          'h-full transition-all',
                          participantsPercentage >= 90
                            ? 'bg-red-500'
                            : participantsPercentage >= 70
                            ? 'bg-yellow-500'
                            : 'bg-green-500'
                        ]"
                        :style="{ width: `${participantsPercentage}%` }"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex flex-col sm:flex-row gap-4">
              <button
                @click="handleParticipate"
                :disabled="participating || !isLoggedIn"
                class="btn-primary flex-1 flex items-center justify-center space-x-2"
              >
                <span v-if="participating">已报名 ✓</span>
                <span v-else-if="!isLoggedIn">登录后报名</span>
                <span v-else-if="event.status !== 'RECRUITING'">报名已截止</span>
                <span v-else>立即报名</span>
              </button>
              <button class="btn-secondary flex items-center justify-center space-x-2">
                <Share2 class="w-4 h-4" />
                <span>分享活动</span>
              </button>
            </div>
          </div>

          <!-- Comments Section -->
          <div class="card p-6">
            <h3 class="text-xl font-bold text-gray-900 mb-4">评论互动 ({{ comments.length }})</h3>
            <div class="space-y-4">
              <!-- Comment Input -->
              <div class="flex space-x-3">
                <img
                  :src="currentUserAvatar"
                  alt="用户头像"
                  class="w-10 h-10 rounded-full"
                />
                <div class="flex-1">
                  <textarea
                    v-model="newComment"
                    placeholder="写下你的评论..."
                    class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none resize-none"
                    rows="3"
                  />
                  <div class="flex justify-end mt-2">
                    <button
                      @click="handleSubmitComment"
                      :disabled="!newComment.trim() || submittingComment"
                      class="btn-primary text-sm py-2 px-4"
                    >
                      {{ submittingComment ? '发表中...' : '发表评论' }}
                    </button>
                  </div>
                </div>
              </div>

              <!-- Comments List -->
              <div v-if="comments.length > 0" class="border-t border-gray-200 pt-4 space-y-4">
                <div v-for="comment in comments" :key="comment.id" class="flex space-x-3">
                  <img
                    :src="comment.user?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${comment.user?.id}`"
                    :alt="comment.user?.nickname || '用户'"
                    class="w-10 h-10 rounded-full"
                  />
                  <div class="flex-1">
                    <div class="flex items-center space-x-2 mb-1">
                      <span class="font-medium text-gray-900">{{ comment.user?.nickname || comment.user?.username }}</span>
                      <span v-if="comment.user?.id === event.creator?.id" class="text-xs text-primary-600 bg-primary-50 px-2 py-0.5 rounded-full">组织者</span>
                    </div>
                    <p class="text-gray-600 text-sm mb-2">
                      {{ comment.content }}
                    </p>
                    <div class="flex items-center space-x-4 text-xs text-gray-500">
                      <span>{{ formatTime(comment.createdAt) }}</span>
                      <button @click="handleLikeComment(comment)" class="flex items-center space-x-1 hover:text-red-500">
                        <Heart class="w-4 h-4" :class="comment.liked ? 'fill-current text-red-500' : ''" />
                        <span>{{ comment.likeCount || 0 }}</span>
                      </button>
                      <button class="hover:text-primary-600">回复</button>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-4 text-gray-500 text-sm">
                暂无评论，快来发表第一条评论吧
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="space-y-6">
          <!-- Organizer Card -->
          <div class="card p-6">
            <h3 class="font-bold text-gray-900 mb-4">活动组织者</h3>
            <div class="flex items-center space-x-4 mb-4">
              <img
                :src="displayEvent.organizer?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=organizer'"
                :alt="displayEvent.organizer?.name"
                class="w-16 h-16 rounded-full"
              />
              <div>
                <div class="flex items-center space-x-2">
                  <h4 class="font-bold text-gray-900">{{ displayEvent.organizer?.name }}</h4>
                </div>
                <p class="text-sm text-gray-600">已组织 {{ event.creator?.activitiesCount || 0 }} 个活动</p>
              </div>
            </div>
            <button @click="visitOrganizerProfile" class="w-full btn-secondary">
              查看组织者主页
            </button>
          </div>

          <!-- Participants Card -->
          <div class="card p-6">
            <h3 class="font-bold text-gray-900 mb-4">已参与成员 ({{ participants.length }})</h3>
            <div v-if="participants.length > 0" class="space-y-3">
              <div v-for="p in participants.slice(0, 10)" :key="p.id" class="flex items-center space-x-3">
                <img
                  :src="p.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${p.id}`"
                  :alt="p.nickname || p.username"
                  class="w-10 h-10 rounded-full"
                />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 truncate">{{ p.nickname || p.username }}</p>
                </div>
              </div>
              <p v-if="participants.length > 10" class="text-sm text-gray-500 text-center">
                还有 {{ participants.length - 10 }} 人参与...
              </p>
            </div>
            <div v-else class="text-sm text-gray-500 text-center py-4">
              暂无参与成员
            </div>
          </div>

          <!-- Similar Events -->
          <div class="card p-6">
            <h3 class="font-bold text-gray-900 mb-4">相似活动</h3>
            <div class="space-y-4">
              <RouterLink
                v-for="similar in similarEvents"
                :key="similar.id"
                :to="`/events/${similar.id}`"
                class="flex space-x-3 group"
              >
                <img
                  :src="similar.coverImage || 'https://picsum.photos/200/200'"
                  :alt="similar.title"
                  class="w-20 h-20 rounded-lg object-cover flex-shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <h4 class="font-medium text-gray-900 group-hover:text-primary-600 transition-colors line-clamp-2 mb-1">
                    {{ similar.title }}
                  </h4>
                  <p class="text-sm text-gray-600 line-clamp-1">{{ similar.startTime?.split('T')[0] }}</p>
                  <p class="text-sm text-gray-600 line-clamp-1">{{ similar.currentParticipants }} 人参与</p>
                </div>
              </RouterLink>
              <div v-if="similarEvents.length === 0" class="text-sm text-gray-500 text-center py-4">
                暂无相似活动
              </div>
            </div>
          </div>

          <!-- Location Map Placeholder -->
          <div class="card p-6">
            <h3 class="font-bold text-gray-900 mb-4">活动地点</h3>
            <div class="w-full h-48 bg-gray-100 rounded-lg flex items-center justify-center">
              <div class="text-center">
                <MapPin class="w-8 h-8 text-gray-400 mx-auto mb-2" />
                <p class="text-sm text-gray-600">{{ displayEvent.location }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Calendar, MapPin, Users, Clock, Share2, Heart, ChevronLeft } from 'lucide-vue-next'
import { activityApi, commentApi, type Activity, type Comment } from '../utils/mockData'

const route = useRoute()
const router = useRouter()

const event = ref<Activity | null>(null)
const comments = ref<Comment[]>([])
const participants = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const isLiked = ref(false)
const participating = ref(false)
const newComment = ref('')
const submittingComment = ref(false)
const similarEvents = ref<Activity[]>([])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const currentUserAvatar = computed(() => {
  const user = localStorage.getItem('user')
  if (user) {
    try {
      const userData = JSON.parse(user)
      return userData.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${userData.id}`
    } catch {}
  }
  return 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
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
  if (!interestName) return 'https://picsum.photos/seed/' + Math.random() + '/800/400'
  for (const [key, url] of Object.entries(covers)) {
    if (interestName.includes(key)) return url
  }
  return 'https://picsum.photos/seed/' + Math.random() + '/800/400'
}

const displayEvent = computed<any>(() => {
  if (!event.value) return {}
  const firstInterest = event.value.interests?.[0]
  return {
    ...event.value,
    category: firstInterest?.name || '综合',
    images: event.value.coverImage ? [event.value.coverImage] : [getDefaultCoverImage(firstInterest?.name)],
    date: event.value.startTime ? event.value.startTime.split('T')[0] : '',
    time: event.value.startTime ? event.value.startTime.split('T')[1]?.substring(0, 5) : '',
    location: event.value.address || `${event.value.city || ''}${event.value.district || ''}`,
    organizer: event.value.creator ? {
      name: event.value.creator.nickname || event.value.creator.username,
      avatar: event.value.creator.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${event.value.creator.id}`
    } : { name: '未知', avatar: '' }
  }
})

const participantsPercentage = computed(() => {
  if (!event.value || !event.value.maxParticipants) return 0
  return (event.value.currentParticipants / event.value.maxParticipants) * 100
})

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const fetchEventDetail = async () => {
  loading.value = true
  error.value = ''
  try {
    const activityId = Number(route.params.id)
    const res = await activityApi.getActivityDetail(activityId)
    if (res.code === 200) {
      event.value = res.data
      isLiked.value = res.data.isLiked || false
      participating.value = res.data.isParticipated || false
    } else {
      error.value = '活动不存在'
    }
  } catch (e: any) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const activityId = Number(route.params.id)
    const res = await commentApi.getComments(activityId)
    if (res.code === 200 && res.data) {
      comments.value = res.data.content || []
    } else {
      console.error('获取评论失败:', res.message)
      comments.value = []
    }
  } catch (e) {
    console.error('获取评论失败:', e)
    comments.value = []
  }
}

const fetchSimilarEvents = async () => {
  if (!event.value) return
  try {
    const res = await activityApi.getActivities({ page: 0, size: 3 })
    if (res.code === 200) {
      similarEvents.value = res.data.content
        .filter(e => e.id !== event.value!.id)
        .slice(0, 3)
    }
  } catch (e) {
    console.error('获取相似活动失败:', e)
  }
}

const fetchParticipants = async () => {
  if (!event.value) return
  try {
    const res = await activityApi.getActivityParticipants(event.value.id)
    if (res.code === 200) {
      participants.value = res.data || []
    }
  } catch (e) {
    console.error('获取参与者失败:', e)
  }
}

const visitOrganizerProfile = () => {
  if (event.value?.creator?.id) {
    router.push(`/profile/${event.value.creator.id}`)
  }
}

const handleLike = async () => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  if (!event.value) return

  try {
    if (isLiked.value) {
      const res = await activityApi.unlikeActivity(event.value.id)
      if (res.code === 200) {
        event.value.likeCount = Math.max(0, (event.value.likeCount || 1) - 1)
        isLiked.value = false
      }
    } else {
      const res = await activityApi.likeActivity(event.value.id)
      if (res.code === 200) {
        event.value.likeCount = (event.value.likeCount || 0) + 1
        isLiked.value = true
      }
    }
  } catch (e) {
    console.error('点赞操作失败:', e)
    alert('操作失败，请稍后重试')
  }
}

const handleParticipate = async () => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  if (!event.value) return

  try {
    if (participating.value) {
      const res = await activityApi.cancelParticipation(event.value.id)
      if (res.code === 200) {
        participating.value = false
        alert('已取消报名')
        fetchParticipants()
        fetchEventDetail()
      } else {
        alert(res.message || '取消失败')
      }
    } else {
      const res = await activityApi.participateActivity(event.value.id)
      if (res.code === 200) {
        participating.value = true
        alert('报名成功！')
        fetchParticipants()
        fetchEventDetail()
      } else {
        alert(res.message || '报名失败')
      }
    }
  } catch (e) {
    console.error('报名操作失败:', e)
    alert('操作失败，请稍后重试')
  }
}

const handleSubmitComment = async () => {
  if (!newComment.value.trim() || !event.value) return
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  submittingComment.value = true
  try {
    const res = await commentApi.addComment(event.value.id, newComment.value)
    if (res.code === 200) {
      comments.value.unshift(res.data)
      event.value.commentCount = (event.value.commentCount || 0) + 1
      newComment.value = ''
      alert('评论发表成功！')
    } else {
      alert(res.message || '评论失败')
    }
  } catch (e) {
    console.error('发表评论失败:', e)
    alert('评论失败，请稍后重试')
  } finally {
    submittingComment.value = false
  }
}

const handleLikeComment = async (comment: Comment) => {
  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }
  try {
    const res = await commentApi.likeComment(comment.id)
    if (res.code === 200) {
      comment.likeCount = (comment.likeCount || 0) + 1
      comment.liked = true
    }
  } catch (e) {
    console.error('评论点赞失败:', e)
  }
}

onMounted(async () => {
  await fetchEventDetail()
  if (event.value) {
    await Promise.all([fetchComments(), fetchSimilarEvents(), fetchParticipants()])
  }
})
</script>
