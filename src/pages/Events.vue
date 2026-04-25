<template>
  <div class="events-page container-custom">
    <!-- 页头 -->
    <header class="events-header">
      <h1>发现活动</h1>
      <p>找到你感兴趣的社区活动，加入其中</p>
    </header>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrap">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索活动名称..."
          @input="handleSearch"
        />
        <button v-if="searchQuery" class="clear-btn" @click="searchQuery=''; handleSearch()">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
    </div>

    <!-- 状态筛选 + 排序 -->
    <div class="filter-row">
      <!-- 状态筛选 - 移动端折叠 -->
      <div class="status-filter w-full">
        <div class="status-filter-label">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
          </svg>
          <span class="hidden sm:inline">活动状态</span>
        </div>
        <div class="status-segments flex-1 justify-center sm:justify-start">
          <button
            v-for="item in statusOptions"
            :key="item.value"
            :class="['segment-btn', selectedStatus === item.value ? 'segment-active' : '']"
            @click="selectedStatus = item.value; handleSearch()"
          >
            <span v-if="item.dot" class="segment-dot" :style="{ background: item.dot }"></span>
            {{ item.label }}
          </button>
        </div>
      </div>

      <!-- 排序 - 移动端简化为按钮 -->
      <div class="sort-wrap">
        <svg class="sort-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>
        </svg>
        <select v-model="sortBy" @change="handleSearch" class="sort-select">
          <option value="date">最新</option>
          <option value="participants">最热</option>
        </select>
        <svg class="chevron" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-pills">
      <button
        @click="selectedCategory = 'all'; handleSearch()"
        :class="['pill', selectedCategory === 'all' ? 'pill-active' : '']"
      >全部</button>
      <button
        v-for="cat in categories"
        :key="cat.id"
        @click="selectedCategory = cat.id; handleSearch()"
        :class="['pill', selectedCategory === cat.id ? 'pill-active' : '']"
      >
        {{ cat.name }}
      </button>
    </div>

    <!-- 结果统计 + 加载更多 -->
    <div class="results-bar">
      <span class="results-count">共 <strong>{{ totalElements }}</strong> 个活动</span>
    </div>

    <!-- 加载中 -->
    <div v-if="loading && events.length === 0" class="skeleton-grid">
      <div class="skeleton-card" v-for="n in 9" :key="n"></div>
    </div>

    <!-- 活动列表 -->
    <div v-else-if="events.length > 0" class="events-grid">
      <EventCard
        v-for="(event, i) in events"
        :key="event.id"
        :event="transformEvent(event)"
      />
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1">
          <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
        </svg>
      </div>
      <h3>没有找到匹配的活动</h3>
      <p>试试清除筛选条件，或者换个关键词</p>
      <button class="btn-clear" @click="clearFilters">清除筛选</button>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && events.length > 0" class="load-more">
      <button
        @click="loadMore"
        :disabled="loading"
        class="btn-load-more"
      >
        <span v-if="loading" class="spinner-sm"></span>
        <span v-else>加载更多</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { activityApi, categories, loadCategories, type Activity } from '../utils/mockData'
import EventCard from '../components/EventCard.vue'

const searchQuery = ref('')
const selectedCategory = ref('all')
const selectedStatus = ref('all') // all | recruiting | ongoing | ended
const sortBy = ref('date')
const events = ref<Activity[]>([])
const loading = ref(true)
const page = ref(0)
const size = ref(12)
const totalElements = ref(0)
const hasMore = ref(true)

// 状态筛选选项
const statusOptions = [
  { value: 'all',      label: '全部',       dot: null },
  { value: 'recruiting', label: '报名中',   dot: '#22c55e' },
  { value: 'ongoing',  label: '进行中',     dot: '#3b82f6' },
  { value: 'ended',    label: '已结束',     dot: '#f59e0b' },
]

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
      avatar: event.creator.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + event.creator.id
    } : { name: '未知', avatar: '' }
  }
}

const fetchEvents = async (reset = false) => {
  if (reset) { page.value = 0; events.value = [] }
  loading.value = true
  try {
    const categoryId = selectedCategory.value === 'all' ? undefined : Number(selectedCategory.value)
    const keyword = searchQuery.value.trim() || undefined

    // 根据状态筛选构造请求参数
    let status: string | undefined = undefined
    if (selectedStatus.value === 'recruiting') {
      status = 'RECRUITING'
    } else if (selectedStatus.value === 'ongoing') {
      status = 'ONGOING'
    } else if (selectedStatus.value === 'ended') {
      status = 'COMPLETED'
    }
    // 'all' 时不传 status，由后端返回所有状态

    const res = await activityApi.getActivities({
      page: page.value,
      size: size.value,
      status,
      categoryId,
      keyword,
      sort: sortBy.value
    })
    if (res.code === 200) {
      events.value = reset ? res.data.content : [...events.value, ...res.data.content]
      totalElements.value = res.data.totalElements
      hasMore.value = !res.data.last
    }
  } catch (e) {
    console.error('获取活动列表失败:', e)
  }
  finally { loading.value = false }
}

const handleSearch = () => fetchEvents(true)
const loadMore = () => { if (!loading.value && hasMore.value) { page.value++; fetchEvents(false) } }
const clearFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = 'all'
  selectedStatus.value = 'all'
  sortBy.value = 'date'
  fetchEvents(true)
}

onMounted(async () => {
  await loadCategories()
  fetchEvents(true)
})
</script>

<style scoped>
.events-page { padding-top: 48px; padding-bottom: 80px; }

/* ─── 页头 ─────────────── */
.events-header { margin-bottom: 40px; }
.events-header h1 {
  font-size: 2rem; font-weight: 700; color: #111;
  letter-spacing: -0.03em; margin-bottom: 8px;
}
.events-header p { font-size: 0.95rem; color: #888; }

/* ─── 搜索栏 ─────────────── */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input-wrap {
  flex: 1;
  min-width: 240px;
  position: relative;
  display: flex;
  align-items: center;
}
.search-icon {
  position: absolute; left: 14px;
  width: 16px; height: 16px; color: #bbb;
  pointer-events: none;
}
.search-input-wrap input {
  width: 100%;
  padding: 12px 40px 12px 44px;
  border: 1.5px solid #eee;
  border-radius: 12px;
  font-size: 0.93rem;
  color: #111;
  background: #fafafa;
  outline: none;
  transition: all 0.2s;
  font-family: inherit;
}
.search-input-wrap input::placeholder { color: #ccc; }
.search-input-wrap input:focus {
  border-color: #d4af37;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(212,175,55,0.1);
}
.clear-btn {
  position: absolute; right: 12px;
  background: none; border: none; cursor: pointer;
  color: #bbb; padding: 4px; display: flex; align-items: center;
  transition: color 0.2s;
}
.clear-btn:hover { color: #666; }

.sort-wrap {
  position: relative;
  display: flex;
  align-items: center;
  background: #fafafa;
  border: 1.5px solid #eee;
  border-radius: 10px;
  padding: 0 12px;
  gap: 6px;
  min-width: 100px;
  transition: border-color 0.2s;
  flex-shrink: 0;
}
.sort-wrap:focus-within {
  border-color: #d4af37;
  box-shadow: 0 0 0 3px rgba(212,175,55,0.1);
}
.sort-icon { width: 12px; height: 12px; color: #bbb; flex-shrink: 0; }
.sort-wrap select {
  background: none; border: none; outline: none;
  font-size: 0.8rem; color: #555; cursor: pointer;
  font-family: inherit; padding: 8px 0; flex: 1;
  appearance: none;
  min-width: 60px;
}
.sort-select {
  background: none; border: none; outline: none;
  font-size: 0.8rem; color: #555; cursor: pointer;
  font-family: inherit; padding: 8px 0; flex: 1;
  appearance: none;
}
.chevron { color: #bbb; flex-shrink: 0; pointer-events: none; }

/* ─── 筛选行 ─────────────── */
.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

/* ─── 状态筛选器 ─────────── */
.status-filter {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}
.status-filter-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #999;
  white-space: nowrap;
  letter-spacing: 0.04em;
}
.status-filter-label svg { color: #bbb; }

.status-segments {
  display: flex;
  align-items: center;
  background: #f4f4f4;
  border-radius: 10px;
  padding: 3px;
  gap: 2px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}
.status-segments::-webkit-scrollbar {
  display: none;
}

.segment-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 7px;
  background: transparent;
  font-size: 0.8rem;
  font-weight: 500;
  color: #777;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  white-space: nowrap;
  flex-shrink: 0;
}
.segment-btn:hover:not(.segment-active) {
  color: #333;
  background: rgba(255,255,255,0.7);
}
.segment-active {
  background: #fff;
  color: #111;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.segment-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* ─── 分类标签 ─────────── */
.category-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 32px;
}
.pill {
  padding: 7px 18px;
  border-radius: 999px;
  border: 1.5px solid #eee;
  background: #fff;
  font-size: 0.83rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}
.pill:hover { border-color: #d4af37; color: #d4af37; }
.pill-active {
  background: #111;
  border-color: #111;
  color: #fff;
}
.pill-active:hover { background: #d4af37; border-color: #d4af37; color: #fff; }

/* ─── 结果统计 ────────── */
.results-bar {
  margin-bottom: 24px;
}
.results-count { font-size: 0.85rem; color: #aaa; }
.results-count strong { color: #333; }

/* ─── 活动网格 ────────── */
.events-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
@media (max-width: 1024px) { .events-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .events-grid { grid-template-columns: 1fr; } }

/* ─── 骨架屏 ────────────── */
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.skeleton-card {
  height: 280px;
  background: linear-gradient(90deg, #f5f5f5 25%, #ebebeb 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 18px;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* ─── 空状态 ─────────── */
.empty-state {
  text-align: center;
  padding: 80px 24px;
}
.empty-icon {
  width: 88px; height: 88px;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 20px;
}
.empty-state h3 { font-size: 1.1rem; font-weight: 700; color: #333; margin-bottom: 8px; }
.empty-state p { font-size: 0.9rem; color: #999; margin-bottom: 24px; }
.btn-clear {
  padding: 10px 28px;
  border: 1.5px solid #111;
  border-radius: 10px;
  background: transparent;
  font-size: 0.88rem;
  font-weight: 600;
  color: #111;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.btn-clear:hover { background: #111; color: #fff; }

/* ─── 加载更多 ────────── */
.load-more {
  display: flex;
  justify-content: center;
  margin-top: 48px;
}
.btn-load-more {
  padding: 12px 40px;
  border: 1.5px solid #ddd;
  border-radius: 12px;
  background: #fff;
  font-size: 0.9rem;
  font-weight: 600;
  color: #555;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: inherit;
}
.btn-load-more:hover:not(:disabled) { border-color: #111; color: #111; transform: translateY(-1px); }
.btn-load-more:disabled { opacity: 0.5; cursor: not-allowed; }
.spinner-sm {
  width: 16px; height: 16px;
  border: 2px solid #ddd;
  border-top-color: #555;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ─── 滚动入场 ────────── */
.reveal-up {
  opacity: 0; transform: translateY(20px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}
.reveal-up.is-visible { opacity: 1; transform: none; }
</style>
