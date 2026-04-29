<template>
  <div class="wheel-container">
    <div class="wheel-header">
      <h3 class="wheel-title">
        <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
        </svg>
        为你推荐
      </h3>
      <button v-if="isLoggedIn" class="refresh-btn" @click="handleRefresh" :class="{ 'is-spinning': isLoading }">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M23 4v6h-6M1 20v-6h6"/>
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
        </svg>
        换一换
      </button>
    </div>

    <div class="wheel-wrapper" :class="{ 'is-loading': isLoading }">
      <div class="wheel-carousel">
        <div class="wheel-track" :style="trackStyle">
          <!-- 左卡片 -->
          <div class="wheel-card wheel-card-left" @click="rotateToPrev">
            <template v-if="activities.length >= 2">
              <div class="card-image-wrap">
                <img :src="activities[prevIndex]?.coverImage || defaultImage" :alt="activities[prevIndex]?.title" />
                <div class="card-overlay"></div>
              </div>
              <div class="card-content">
                <span class="card-tag">{{ activities[prevIndex]?.interests?.[0]?.name || '综合' }}</span>
                <h4 class="card-title">{{ activities[prevIndex]?.title }}</h4>
                <p class="card-meta">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  {{ activities[prevIndex]?.city }}{{ activities[prevIndex]?.district }}
                </p>
              </div>
            </template>
          </div>

          <!-- 中间卡片 -->
          <div class="wheel-card wheel-card-center" @click="goToDetail">
            <template v-if="activities.length > 0">
              <div class="card-image-wrap">
                <img :src="activities[currentIndex]?.coverImage || defaultImage" :alt="activities[currentIndex]?.title" />
                <div class="card-overlay"></div>
                <span class="card-hot-badge">
                  <svg viewBox="0 0 24 24" fill="currentColor" width="10" height="10"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93z"/></svg>
                  推荐
                </span>
              </div>
              <div class="card-content">
                <span class="card-tag">{{ activities[currentIndex]?.interests?.[0]?.name || '综合' }}</span>
                <h4 class="card-title">{{ activities[currentIndex]?.title }}</h4>
                <div class="card-info-row">
                  <span class="card-date">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    {{ formatDate(activities[currentIndex]?.startTime) }}
                  </span>
                  <span class="card-participants">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg>
                    {{ activities[currentIndex]?.currentParticipants || 0 }}/{{ activities[currentIndex]?.maxParticipants }}
                  </span>
                </div>
                <p class="card-location">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  {{ activities[currentIndex]?.address || '' }}{{ activities[currentIndex]?.district }}
                </p>
              </div>
            </template>
            <div v-else class="empty-wheel">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              <p>暂无推荐</p>
            </div>
          </div>

          <!-- 右卡片 -->
          <div class="wheel-card wheel-card-right" @click="rotateToNext">
            <template v-if="activities.length >= 3">
              <div class="card-image-wrap">
                <img :src="activities[nextIndex]?.coverImage || defaultImage" :alt="activities[nextIndex]?.title" />
                <div class="card-overlay"></div>
              </div>
              <div class="card-content">
                <span class="card-tag">{{ activities[nextIndex]?.interests?.[0]?.name || '综合' }}</span>
                <h4 class="card-title">{{ activities[nextIndex]?.title }}</h4>
                <p class="card-meta">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                  {{ activities[nextIndex]?.city }}{{ activities[nextIndex]?.district }}
                </p>
              </div>
            </template>
          </div>
        </div>

        <div class="wheel-indicators">
          <button v-for="(_, index) in Math.min(activities.length, 5)" :key="index" class="indicator" :class="{ active: index === currentIndex % 5 }" @click.stop="currentIndex = index"></button>
        </div>
      </div>
    </div>

    <div v-if="!isLoggedIn" class="login-prompt">
      <p>登录后获取个性化推荐</p>
      <RouterLink to="/login" class="login-link">立即登录</RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { activityApi } from '../utils/mockData'
import type { Activity } from '../utils/api'

const router = useRouter()
const activities = ref<Activity[]>([])
const currentIndex = ref(0)
const isLoading = ref(false)
const isLoggedIn = ref(!!localStorage.getItem('token'))
const defaultImage = 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800'

const prevIndex = computed(() => {
  if (activities.value.length < 2) return 0
  return (currentIndex.value - 1 + activities.value.length) % activities.value.length
})

const nextIndex = computed(() => {
  if (activities.value.length < 3) return currentIndex.value
  return (currentIndex.value + 1) % activities.value.length
})

const trackStyle = computed(() => ({
  transform: `translateX(-${currentIndex.value * 33.33}%)`,
  transition: 'transform 0.5s cubic-bezier(0.4, 0, 0.2, 1)'
}))

const loadRecommendations = async () => {
  if (!isLoggedIn.value) return
  isLoading.value = true
  try {
    const res = await activityApi.getRecommendedActivities(0, 10)
    if (res.code === 200 && res.data.content) {
      activities.value = res.data.content
      currentIndex.value = 0
    }
  } catch (e) {
    console.error('获取推荐失败:', e)
  } finally {
    isLoading.value = false
  }
}

const handleRefresh = async () => {
  if (isLoading.value) return
  isLoading.value = true
  try {
    // 调用刷新接口，清除缓存重新计算推荐
    const res = await activityApi.refreshRecommendedActivities()
    console.log('刷新推荐响应:', res)
    if (res.code === 200 && res.data) {
      activities.value = res.data
      currentIndex.value = 0
      console.log('推荐已更新，数量:', activities.value.length)
    } else {
      console.warn('刷新推荐返回异常:', res)
    }
  } catch (e) {
    console.error('刷新推荐失败:', e)
    // 失败时回退到普通加载
    await loadRecommendations()
  } finally {
    isLoading.value = false
  }
}

const rotateToPrev = () => { currentIndex.value = prevIndex.value }
const rotateToNext = () => { currentIndex.value = (currentIndex.value + 1) % Math.max(activities.value.length, 1) }

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '待定'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const goToDetail = () => {
  if (activities.value.length > 0 && activities[currentIndex.value]) {
    router.push(`/events/${activities[currentIndex.value].id}`)
  }
}

watch(() => localStorage.getItem('token'), (newToken) => {
  isLoggedIn.value = !!newToken
  if (isLoggedIn.value && activities.value.length === 0) loadRecommendations()
})

onMounted(() => {
  if (isLoggedIn.value && activities.value.length === 0) loadRecommendations()
})
</script>

<style scoped>
.wheel-container {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 20px;
  padding: 24px;
  position: relative;
  overflow: hidden;
}
.wheel-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 30%, rgba(212, 175, 55, 0.1) 0%, transparent 50%);
  pointer-events: none;
}
.wheel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}
.wheel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.1rem;
  font-weight: 700;
  color: #fff;
  margin: 0;
}
.title-icon {
  width: 20px;
  height: 20px;
  color: #d4af37;
  fill: rgba(212, 175, 55, 0.2);
}
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  color: #a0a0a0;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
}
.refresh-btn svg { width: 14px; height: 14px; }
.refresh-btn:hover {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.4);
  color: #d4af37;
}
.refresh-btn.is-spinning svg { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.wheel-wrapper { position: relative; z-index: 1; perspective: 1000px; }
.wheel-wrapper.is-loading .wheel-track { opacity: 0.5; pointer-events: none; }
.wheel-carousel { position: relative; height: 280px; overflow: hidden; }
.wheel-track { display: flex; align-items: center; height: 100%; }
.wheel-card {
  position: absolute;
  width: 200px;
  height: 260px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}
.wheel-card-left { left: 0; transform: translateX(-10px) scale(0.85); opacity: 0.6; z-index: 1; }
.wheel-card-center { left: 50%; transform: translateX(-50%) scale(1); opacity: 1; z-index: 3; width: 220px; height: 280px; }
.wheel-card-right { right: 0; transform: translateX(10px) scale(0.85); opacity: 0.6; z-index: 1; }
.wheel-card:hover { transform: translateX(-50%) scale(1.05) !important; z-index: 10; }
.wheel-card-left:hover { transform: translateX(0) scale(0.9) !important; }
.wheel-card-right:hover { transform: translateX(0) scale(0.9) !important; }
.card-image-wrap { position: relative; height: 140px; overflow: hidden; }
.wheel-card-center .card-image-wrap { height: 160px; }
.card-image-wrap img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s ease; }
.wheel-card:hover .card-image-wrap img { transform: scale(1.1); }
.card-overlay { position: absolute; inset: 0; background: linear-gradient(to bottom, transparent 40%, rgba(0, 0, 0, 0.6) 100%); }
.card-hot-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
  color: #fff;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}
.card-content { padding: 12px; }
.card-tag {
  display: inline-block;
  font-size: 0.7rem;
  font-weight: 600;
  color: #d4af37;
  background: rgba(212, 175, 55, 0.15);
  padding: 2px 8px;
  border-radius: 8px;
  margin-bottom: 6px;
}
.card-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: #111;
  margin: 0 0 6px 0;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.wheel-card-center .card-title { font-size: 1rem; }
.card-meta, .card-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: #888;
  margin: 4px 0;
}
.card-meta svg, .card-location svg, .card-date svg, .card-participants svg {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}
.card-info-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 4px; }
.card-date, .card-participants { display: flex; align-items: center; gap: 4px; font-size: 0.75rem; color: #666; }
.empty-wheel { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #888; }
.empty-wheel svg { width: 48px; height: 48px; margin-bottom: 12px; opacity: 0.5; }
.empty-wheel p { font-size: 0.9rem; }
.wheel-indicators { display: flex; justify-content: center; gap: 8px; margin-top: 16px; }
.indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}
.indicator.active { width: 24px; border-radius: 4px; background: #d4af37; }
.login-prompt {
  margin-top: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  text-align: center;
  position: relative;
  z-index: 1;
}
.login-prompt p { color: #888; font-size: 0.85rem; margin: 0 0 8px 0; }
.login-link { color: #d4af37; font-weight: 600; text-decoration: none; font-size: 0.9rem; transition: color 0.2s; }
.login-link:hover { color: #fff; }
@media (max-width: 640px) {
  .wheel-container { padding: 16px; }
  .wheel-card { width: 160px; height: 220px; }
  .wheel-card-center { width: 180px; height: 240px; }
  .wheel-carousel { height: 240px; }
}
</style>