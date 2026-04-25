<template>
  <div class="container-custom py-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">🏛️ 管理后台</h1>
      <p class="text-gray-600">管理平台活动、用户和审核内容</p>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'users'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">总用户数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.totalUsers }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Users class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'all'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">总活动数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.totalActivities }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <Calendar class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'pending'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待审核</p>
            <p class="text-2xl font-bold text-orange-600">{{ stats.pendingActivities }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <AlertCircle class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'all'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">招募中</p>
            <p class="text-2xl font-bold text-primary-600">{{ stats.recruitingActivities }}</p>
          </div>
          <div class="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
            <CheckCircle class="w-6 h-6 text-primary-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex space-x-4 mb-6 border-b border-gray-200">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="activeTab = tab.id"
        :class="[
          'px-4 py-2 font-medium border-b-2 -mb-px transition-colors',
          activeTab === tab.id
            ? 'border-primary-600 text-primary-600'
            : 'border-transparent text-gray-500 hover:text-gray-700'
        ]"
      >
        {{ tab.label }}
        <span
          v-if="tab.id === 'pending' && stats.pendingActivities > 0"
          class="ml-2 px-2 py-0.5 bg-orange-500 text-white text-xs rounded-full"
        >
          {{ stats.pendingActivities }}
        </span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-16">
      <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-500">加载中...</p>
    </div>

    <!-- Activities List -->
    <div v-else-if="activeTab !== 'users'" class="space-y-4">
      <div v-if="activities.length === 0" class="text-center py-16 bg-gray-50 rounded-xl">
        <Calendar class="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p class="text-gray-500">暂无数据</p>
      </div>

      <div
        v-for="activity in activities"
        :key="activity.id"
        class="card p-6"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <h3 class="text-lg font-bold text-gray-900">{{ activity.title }}</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs font-medium rounded-full',
                  activity.status === 'PENDING' ? 'bg-orange-100 text-orange-700' :
                  activity.status === 'RECRUITING' ? 'bg-green-100 text-green-700' :
                  activity.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                  'bg-gray-100 text-gray-700'
                ]"
              >
                {{ statusText[activity.status] || activity.status }}
              </span>
            </div>
            <p class="text-sm text-gray-600 mb-2">{{ activity.description?.substring(0, 100) }}...</p>
            <div class="flex items-center gap-4 text-xs text-gray-500">
              <span>👤 {{ activity.creator?.nickname || activity.creator?.username }}</span>
              <span>📍 {{ activity.city }} {{ activity.district }}</span>
              <span>📅 {{ formatDate(activity.startTime) }}</span>
              <span>👥 {{ activity.currentParticipants }}/{{ activity.maxParticipants }}</span>
            </div>
          </div>
          <div v-if="activeTab === 'pending'" class="flex gap-2 ml-4">
            <button
              @click="approveActivity(activity.id)"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm font-medium"
            >
              通过
            </button>
            <button
              @click="rejectActivity(activity.id)"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-medium"
            >
              拒绝
            </button>
          </div>
          <div v-else class="flex gap-2 ml-4">
            <button
              @click="deleteActivity(activity.id)"
              class="px-4 py-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 text-sm font-medium"
            >
              删除
            </button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-6">
        <button
          v-for="page in totalPages"
          :key="page"
          @click="fetchActivities(page - 1)"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium',
            currentPage === page - 1
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          {{ page }}
        </button>
      </div>
    </div>

    <!-- Users List -->
    <div v-else class="space-y-4">
      <div v-if="users.length === 0" class="text-center py-16 bg-gray-50 rounded-xl">
        <Users class="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p class="text-gray-500">暂无用户</p>
      </div>

      <div
        v-for="user in users"
        :key="user.id"
        class="card p-6"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <img
              :src="user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}`"
              class="w-12 h-12 rounded-full bg-gray-100"
            />
            <div>
              <div class="flex items-center gap-2">
                <h3 class="font-bold text-gray-900">{{ user.nickname || user.username }}</h3>
                <span
                  :class="[
                    'px-2 py-0.5 text-xs font-medium rounded-full',
                    user.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                    user.role === 'ORGANIZER' ? 'bg-blue-100 text-blue-700' :
                    'bg-gray-100 text-gray-700'
                  ]"
                >
                  {{ roleText[user.role] || user.role }}
                </span>
              </div>
              <p class="text-sm text-gray-500">{{ user.email || '未设置邮箱' }}</p>
            </div>
          </div>
          <div class="flex items-center gap-4">
            <div class="text-right text-sm text-gray-500">
              <p>注册于 {{ formatDate(user.createdAt) }}</p>
              <p>积分: {{ user.credits || 0 }}</p>
            </div>
            <button
              @click="viewUserProfile(user.id)"
              class="px-4 py-2 bg-primary-100 text-primary-600 rounded-lg hover:bg-primary-200 text-sm font-medium"
            >
              查看画像
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- User Profile Modal -->
  <div v-if="showProfileModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showProfileModal = false">
    <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto m-4">
      <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
        <h2 class="text-xl font-bold text-gray-900">👤 用户画像</h2>
        <button @click="showProfileModal = false" class="p-2 hover:bg-gray-100 rounded-lg">
          <span class="text-2xl">×</span>
        </button>
      </div>

      <div v-if="userProfile" class="p-6 space-y-6">
        <!-- Basic Info -->
        <div class="flex items-start gap-6">
          <img
            :src="userProfile.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${userProfile.id}`"
            class="w-20 h-20 rounded-full bg-gray-100"
          />
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <h3 class="text-xl font-bold text-gray-900">{{ userProfile.nickname || userProfile.username }}</h3>
              <span
                :class="[
                  'px-2 py-0.5 text-xs font-medium rounded-full',
                  userProfile.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                  userProfile.role === 'ORGANIZER' ? 'bg-blue-100 text-blue-700' :
                  'bg-gray-100 text-gray-700'
                ]"
              >
                {{ roleText[userProfile.role] || userProfile.role }}
              </span>
              <span
                :class="[
                  'px-2 py-0.5 text-xs font-medium rounded-full',
                  userProfile.status === 'ACTIVE' ? 'bg-green-100 text-green-700' :
                  userProfile.status === 'BANNED' ? 'bg-red-100 text-red-700' :
                  'bg-gray-100 text-gray-700'
                ]"
              >
                {{ statusText[userProfile.status] || userProfile.status }}
              </span>
            </div>
            <p class="text-gray-600 mb-2">{{ userProfile.bio || '暂无个人简介' }}</p>
            <div class="flex flex-wrap gap-4 text-sm text-gray-500">
              <span v-if="userProfile.email">📧 {{ userProfile.email }}</span>
              <span v-if="userProfile.phone">📱 {{ userProfile.phone }}</span>
              <span v-if="userProfile.gender">⚧ {{ userProfile.gender }}</span>
              <span v-if="userProfile.city">{{ userProfile.province }} {{ userProfile.city }}</span>
            </div>
          </div>
        </div>

        <!-- Statistics -->
        <div class="bg-gray-50 rounded-xl p-4">
          <h4 class="font-bold text-gray-900 mb-4">📊 数据统计</h4>
          <div class="grid grid-cols-4 gap-4">
            <div class="text-center">
              <p class="text-2xl font-bold text-primary-600">{{ userProfile.statistics?.createdActivities || 0 }}</p>
              <p class="text-xs text-gray-500">创建活动</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-green-600">{{ userProfile.statistics?.participatingActivities || 0 }}</p>
              <p class="text-xs text-gray-500">参与活动</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-blue-600">{{ userProfile.statistics?.likedActivities || 0 }}</p>
              <p class="text-xs text-gray-500">点赞活动</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-orange-600">{{ userProfile.statistics?.totalComments || 0 }}</p>
              <p class="text-xs text-gray-500">发表评论</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-red-600">{{ userProfile.statistics?.followers || 0 }}</p>
              <p class="text-xs text-gray-500">粉丝</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-purple-600">{{ userProfile.statistics?.following || 0 }}</p>
              <p class="text-xs text-gray-500">关注</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-pink-600">{{ userProfile.statistics?.totalLikes || 0 }}</p>
              <p class="text-xs text-gray-500">获赞总数</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-yellow-600">{{ userProfile.credits || 0 }}</p>
              <p class="text-xs text-gray-500">积分</p>
            </div>
          </div>
        </div>

        <!-- Interests -->
        <div v-if="userProfile.interests && userProfile.interests.length > 0">
          <h4 class="font-bold text-gray-900 mb-3">🏷️ 兴趣标签</h4>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="interest in userProfile.interests"
              :key="interest.id"
              class="px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-sm"
            >
              {{ interest.name }}
              <span class="text-xs text-primary-500 ml-1">×{{ interest.weight || 1 }}</span>
            </span>
          </div>
        </div>

        <!-- Recent Activities -->
        <div v-if="userProfile.recentActivities && userProfile.recentActivities.length > 0">
          <h4 class="font-bold text-gray-900 mb-3">📝 最近创建的活动</h4>
          <div class="space-y-3">
            <div
              v-for="activity in userProfile.recentActivities"
              :key="activity.id"
              class="border border-gray-200 rounded-lg p-3"
            >
              <div class="flex items-center justify-between">
                <h5 class="font-medium text-gray-900">{{ activity.title }}</h5>
                <span
                  :class="[
                    'px-2 py-0.5 text-xs font-medium rounded-full',
                    activity.status === 'PENDING' ? 'bg-orange-100 text-orange-700' :
                    activity.status === 'RECRUITING' ? 'bg-green-100 text-green-700' :
                    'bg-gray-100 text-gray-700'
                  ]"
                >
                  {{ statusText[activity.status] || activity.status }}
                </span>
              </div>
              <p class="text-sm text-gray-500 mt-1">
                {{ activity.city }} {{ activity.district }} · {{ formatDate(activity.startTime) }}
              </p>
            </div>
          </div>
        </div>

        <!-- Account Info -->
        <div class="text-sm text-gray-500">
          <p>注册时间: {{ formatDate(userProfile.createdAt) }}</p>
          <p>最后活跃: {{ formatDate(userProfile.lastActiveAt) }}</p>
        </div>
      </div>

      <div v-else-if="profileLoading" class="text-center py-16">
        <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-500">加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Users, Calendar, AlertCircle, CheckCircle } from 'lucide-vue-next'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('pending')
const activities = ref<any[]>([])
const users = ref<any[]>([])
const currentPage = ref(0)
const totalPages = ref(0)

// User profile modal
const showProfileModal = ref(false)
const profileLoading = ref(false)
const userProfile = ref<any>(null)

const stats = reactive({
  totalUsers: 0,
  totalActivities: 0,
  pendingActivities: 0,
  recruitingActivities: 0
})

const tabs = [
  { id: 'pending', label: '待审核' },
  { id: 'all', label: '所有活动' },
  { id: 'users', label: '用户管理' },
]

const statusText: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  RECRUITING: '招募中',
  FULL: '已满员',
  ONGOING: '进行中',
  COMPLETED: '已结束',
  CANCELLED: '已取消',
  ACTIVE: '正常',
  INACTIVE: '未激活',
  BANNED: '已封禁'
}

const roleText: Record<string, string> = {
  ADMIN: '管理员',
  ORGANIZER: '组织者',
  USER: '普通用户'
}

const getToken = () => localStorage.getItem('token')

const fetchStatistics = async () => {
  try {
    const res = await fetch('/api/admin/statistics', {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      stats.totalUsers = data.data.totalUsers || 0
      stats.totalActivities = data.data.totalActivities || 0
      stats.pendingActivities = data.data.pendingActivities || 0
      stats.recruitingActivities = data.data.recruitingActivities || 0
    }
  } catch (e) {
    console.error('获取统计失败:', e)
  }
}

const fetchActivities = async (page = 0) => {
  loading.value = true
  try {
    let url = `/api/admin/activities?page=${page}&size=10`
    if (activeTab.value === 'pending') {
      url = `/api/admin/activities/pending?page=${page}&size=10`
    } else if (activeTab.value !== 'users') {
      url = `/api/admin/activities?page=${page}&size=10`
    }
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      activities.value = data.data.content || []
      totalPages.value = data.data.totalPages || 0
      currentPage.value = page
    } else {
      console.error('获取活动失败:', data.message)
      activities.value = []
    }
  } catch (e) {
    console.error('获取活动失败:', e)
    activities.value = []
  } finally {
    loading.value = false
  }
}

const fetchUsers = async (page = 0) => {
  loading.value = true
  try {
    const res = await fetch(`/api/admin/users?page=${page}&size=10`, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      users.value = data.data.content || []
      totalPages.value = data.data.totalPages || 0
    } else {
      console.error('获取用户失败:', data.message)
      users.value = []
    }
  } catch (e) {
    console.error('获取用户失败:', e)
    users.value = []
  } finally {
    loading.value = false
  }
}

const approveActivity = async (id: number) => {
  try {
    const res = await fetch(`/api/admin/activities/${id}/approve`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('活动已通过审核')
      fetchActivities()
      fetchStatistics()
    } else {
      alert(data.message || '操作失败')
    }
  } catch (e) {
    alert('操作失败')
  }
}

const rejectActivity = async (id: number) => {
  if (!confirm('确定要拒绝该活动吗？')) return
  try {
    const res = await fetch(`/api/admin/activities/${id}/reject`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getToken()}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('活动已拒绝')
      fetchActivities()
      fetchStatistics()
    } else {
      alert(data.message || '操作失败')
    }
  } catch (e) {
    alert('操作失败')
  }
}

const deleteActivity = async (id: number) => {
  if (!confirm('确定要删除该活动吗？此操作不可恢复！')) return
  try {
    const res = await fetch(`/api/admin/activities/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('活动已删除')
      fetchActivities()
      fetchStatistics()
    } else {
      alert(data.message || '删除失败')
    }
  } catch (e) {
    alert('删除失败')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const viewUserProfile = async (userId: number) => {
  showProfileModal.value = true
  profileLoading.value = true
  userProfile.value = null
  try {
    const res = await fetch(`/api/admin/users/${userId}/profile`, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      userProfile.value = data.data
    } else {
      alert(data.message || '获取用户画像失败')
      showProfileModal.value = false
    }
  } catch (e) {
    console.error('获取用户画像失败:', e)
    alert('获取用户画像失败')
    showProfileModal.value = false
  } finally {
    profileLoading.value = false
  }
}

// 监听标签切换，重新加载数据
watch(activeTab, (tab) => {
  if (tab === 'users') {
    fetchUsers()
  } else {
    fetchActivities()
  }
})

onMounted(() => {
  // 检查是否是管理员
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    if (user.role !== 'ADMIN') {
      alert('您没有管理员权限')
      router.push('/')
      return
    }
  }

  fetchStatistics()
  fetchActivities()
})
</script>