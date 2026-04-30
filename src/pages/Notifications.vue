<template>
  <div class="container-custom py-8">
    <div class="max-w-3xl mx-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">消息通知</h1>
          <p class="text-sm text-gray-500 mt-1">
            <template v-if="unreadCount > 0">
              你有 <strong class="text-primary-600">{{ unreadCount }}</strong> 条未读通知
            </template>
            <template v-else>
              暂无未读通知
            </template>
          </p>
        </div>
        <button
          v-if="notifications.length > 0 && unreadCount > 0"
          @click="handleMarkAllRead"
          class="text-sm text-primary-600 hover:text-primary-700 font-medium"
        >
          全部标为已读
        </button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center py-16">
        <div class="w-10 h-10 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-500">加载通知...</p>
      </div>

      <!-- Empty -->
      <div v-else-if="notifications.length === 0" class="text-center py-16">
        <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <Bell class="w-10 h-10 text-gray-400" />
        </div>
        <h3 class="text-lg font-bold text-gray-900 mb-2">暂无通知</h3>
        <p class="text-gray-500 text-sm">当你有新的活动通知时，会显示在这里</p>
      </div>

      <!-- Notification List -->
      <div v-else class="space-y-2">
        <div
          v-for="notif in notifications"
          :key="notif.id"
          @click="handleClickNotification(notif)"
          class="card p-4 cursor-pointer transition-colors"
          :class="notif.isRead ? 'hover:bg-gray-50' : 'bg-primary-50/30 border-l-4 border-primary-500 hover:bg-primary-50/50'"
        >
          <div class="flex items-start gap-3">
            <!-- Icon -->
            <div
              class="w-10 h-10 rounded-full flex items-center justify-center shrink-0 mt-0.5"
              :class="iconClass(notif.type)"
            >
              <component :is="iconComponent(notif.type)" class="w-5 h-5" />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <h4 class="font-semibold text-sm" :class="notif.isRead ? 'text-gray-700' : 'text-gray-900'">
                  {{ notif.title }}
                </h4>
                <span v-if="!notif.isRead" class="w-2 h-2 bg-primary-500 rounded-full shrink-0"></span>
              </div>
              <p class="text-sm text-gray-600 line-clamp-2 mb-1">{{ notif.content }}</p>
              <div class="flex items-center gap-3 text-xs text-gray-400">
                <span>{{ formatTime(notif.createdAt) }}</span>
                <span v-if="notif.senderName" class="truncate">来自 {{ notif.senderName }}</span>
                <span v-if="notif.activityTitle" class="text-primary-600 truncate">{{ notif.activityTitle }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Load More -->
      <div v-if="hasMore" class="text-center mt-6">
        <button
          @click="loadMore"
          :disabled="loadingMore"
          class="btn-secondary text-sm py-2 px-6"
        >
          <span v-if="loadingMore" class="spinner-sm"></span>
          <span v-else>加载更多</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, BellRing, Calendar, Info, AlertTriangle } from 'lucide-vue-next'
import { notificationApi } from '../utils/api'

const router = useRouter()
const notifications = ref<any[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const page = ref(0)
const hasMore = ref(true)
const unreadCount = ref(0)

const fetchNotifications = async (reset = false) => {
  if (reset) { page.value = 0; notifications.value = []; loading.value = true }
  try {
    const res = await notificationApi.getNotifications(page.value, 20)
    if (res.code === 200 && res.data) {
      const items = res.data.content || []
      if (reset) {
        notifications.value = items
      } else {
        notifications.value = [...notifications.value, ...items]
      }
      hasMore.value = items.length >= 20
    }
  } catch (e) {
    console.error('获取通知失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  try {
    const res = await notificationApi.getUnreadCount()
    if (res.code === 200 && res.data) {
      unreadCount.value = res.data.unreadCount
    }
  } catch (e) {
    console.error('获取未读计数失败:', e)
  }
}

const handleMarkAllRead = async () => {
  try {
    const res = await notificationApi.markAllAsRead()
    if (res.code === 200) {
      notifications.value.forEach(n => n.isRead = true)
      unreadCount.value = 0
    }
  } catch (e) {
    console.error('标记已读失败:', e)
  }
}

const handleClickNotification = async (notif: any) => {
  // 标记已读
  if (!notif.isRead) {
    try {
      await notificationApi.markAsRead(notif.id)
      notif.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch {}
  }
  // 跳转到活动详情
  if (notif.activityId) {
    router.push(`/events/${notif.activityId}`)
  }
}

const loadMore = async () => {
  loadingMore.value = true
  page.value++
  try {
    const res = await notificationApi.getNotifications(page.value, 20)
    if (res.code === 200 && res.data) {
      const items = res.data.content || []
      notifications.value = [...notifications.value, ...items]
      hasMore.value = items.length >= 20
    } else {
      hasMore.value = false
    }
  } catch {
    hasMore.value = false
  } finally {
    loadingMore.value = false
  }
}

const iconClass = (type: string) => {
  switch (type) {
    case 'ACTIVITY_REMINDER': return 'bg-blue-100 text-blue-600'
    case 'ACTIVITY_CANCEL': return 'bg-orange-100 text-orange-600'
    case 'ACTIVITY_UPDATE': return 'bg-green-100 text-green-600'
    default: return 'bg-gray-100 text-gray-600'
  }
}

const iconComponent = (type: string) => {
  switch (type) {
    case 'ACTIVITY_REMINDER': return Bell
    case 'ACTIVITY_CANCEL': return AlertTriangle
    case 'ACTIVITY_UPDATE': return Calendar
    default: return Info
  }
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

onMounted(async () => {
  await Promise.all([fetchNotifications(true), fetchUnreadCount()])
})
</script>

<style scoped>
.spinner-sm {
  display: inline-block;
  width: 16px; height: 16px;
  border: 2px solid #ddd;
  border-top-color: #555;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
