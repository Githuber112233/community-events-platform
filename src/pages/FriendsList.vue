<template>
  <div class="friends-page">
    <div class="container-custom">
      <div class="page-header">
        <button class="back-btn" @click="router.back()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        <h1 class="page-title">{{ pageTitle }}</h1>
      </div>

      <div class="tab-bar">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'followers' }"
          @click="activeTab = 'followers'"
        >
          粉丝 <span class="count">{{ followers.length }}</span>
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'following' }"
          @click="activeTab = 'following'"
        >
          关注 <span class="count">{{ following.length }}</span>
        </button>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="skeleton-item" v-for="n in 5" :key="n"></div>
      </div>

      <div v-else-if="currentList.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
        </svg>
        <p>{{ activeTab === 'followers' ? '还没有粉丝' : '还没有关注任何人' }}</p>
      </div>

      <div v-else class="friends-list">
        <div
          v-for="friend in currentList"
          :key="friend.id"
          class="friend-card"
          @click="goToProfile(friend.id)"
        >
          <img
            :src="friend.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${friend.id}`"
            :alt="friend.nickname || friend.username"
            class="friend-avatar"
          />
          <div class="friend-info">
            <h4 class="friend-name">{{ friend.nickname || friend.username }}</h4>
            <p class="friend-bio">{{ friend.bio || '暂无简介' }}</p>
          </div>
          <button
            v-if="friend.id !== currentUserId"
            class="action-btn"
            :class="{ following: isFollowing(friend.id) }"
            @click.stop="toggleFollow(friend)"
          >
            {{ isFollowing(friend.id) ? '已关注' : '关注' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { followApi } from '../utils/mockData'

const route = useRoute()
const router = useRouter()

const activeTab = ref<'followers' | 'following'>('followers')
const followers = ref<any[]>([])
const following = ref<any[]>([])
const loading = ref(true)
const currentUserId = ref<number | null>(null)
const followingIds = ref<Set<number>>(new Set())

const pageTitle = computed(() =>
  route.params.type === 'followers' ? '我的粉丝' : '我的关注'
)

const currentList = computed(() =>
  activeTab.value === 'followers' ? followers.value : following.value
)

const isFollowing = (userId: number) => followingIds.value.has(userId)

const loadData = async () => {
  loading.value = true
  const userId = route.params.userId || currentUserId.value

  try {
    const [followersRes, followingRes] = await Promise.all([
      followApi.getFollowers(Number(userId)),
      followApi.getFollowing(Number(userId))
    ])

    if (followersRes.code === 200) {
      followers.value = followersRes.data || []
    }
    if (followingRes.code === 200) {
      following.value = followingRes.data || []
      followingIds.value = new Set(followingRes.data?.map((u: any) => u.id) || [])
    }
  } catch (e) {
    console.error('加载好友列表失败:', e)
  } finally {
    loading.value = false
  }
}

const toggleFollow = async (user: any) => {
  if (isFollowing(user.id)) {
    await followApi.unfollowUser(user.id)
    followingIds.value.delete(user.id)
  } else {
    await followApi.followUser(user.id)
    followingIds.value.add(user.id)
  }
}

const goToProfile = (userId: number) => {
  router.push(`/profile/${userId}`)
}

onMounted(async () => {
  try {
    const userData = localStorage.getItem('user')
    if (userData) {
      currentUserId.value = JSON.parse(userData).id
    }
  } catch (e) {
    console.error('获取当前用户失败:', e)
  }

  await loadData()
})
</script>

<style scoped>
.friends-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 40px;
}

.container-custom {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 16px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  background: #fff;
  position: sticky;
  top: 64px;
  z-index: 10;
}

.back-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn svg { width: 18px; height: 18px; }

.page-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #111;
  margin: 0;
}

.tab-bar {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 4px;
  margin: 16px 0;
  gap: 4px;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-btn.active {
  background: #111;
  color: #fff;
}

.tab-btn .count {
  font-size: 0.8rem;
  opacity: 0.7;
}

.friends-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.friend-card:hover { background: #f9f9f9; }

.friend-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 0.95rem;
  font-weight: 600;
  color: #111;
  margin: 0 0 4px 0;
}

.friend-bio {
  font-size: 0.8rem;
  color: #888;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-btn {
  padding: 8px 16px;
  border: 1px solid #d4af37;
  background: transparent;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  color: #d4af37;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn.following {
  background: #d4af37;
  color: #111;
}

.action-btn:hover {
  background: rgba(212, 175, 55, 0.1);
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-item {
  height: 82px;
  background: linear-gradient(90deg, #f5f5f5 25%, #ebebeb 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 12px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #aaa;
}

.empty-state svg {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 0.95rem;
}
</style>