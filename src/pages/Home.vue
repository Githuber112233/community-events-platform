<template>
  <div class="page-home">
    <!-- Hero Section -->
    <section class="hero-section relative overflow-hidden">
      <!-- 背景装饰 -->
      <div class="hero-bg-orb orb-1"></div>
      <div class="hero-bg-orb orb-2"></div>

      <div class="container-custom hero-content">
        <div class="hero-text-block">
          <div class="hero-tag reveal-up">社区兴趣活动平台</div>
          <h1 class="hero-title reveal-up" style="transition-delay: 0.1s">
            与志同道合的人<br />共享生活里的兴趣
          </h1>
          <p class="hero-desc reveal-up" style="transition-delay: 0.2s">
            在这里发布或参与活动，无论是晨跑、读书、桌游还是摄影，总有人与你同行。
          </p>
          <div class="hero-actions reveal-up" style="transition-delay: 0.3s">
            <RouterLink to="/events" class="btn-primary-gold">探索活动</RouterLink>
            <RouterLink to="/create-event" class="btn-ghost-dark">发布活动</RouterLink>
          </div>
        </div>

        <!-- 右侧浮动卡片 -->
        <div class="hero-float-cards reveal-right" style="transition-delay: 0.2s">
          <div class="float-card card-main">
            <div class="float-card-img">
              <img src="https://picsum.photos/seed/hiking/400/240" alt="活动封面" />
            </div>
            <div class="float-card-body">
              <span class="float-card-tag">户外</span>
              <h4>周末香山徒步</h4>
              <p>明天 · 北京香山</p>
              <div class="float-card-avatars">
                <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=a" />
                <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=b" />
                <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=c" />
                <span>+12 人报名</span>
              </div>
            </div>
          </div>

          <div class="float-card card-small">
            <span class="float-card-tag">读书</span>
            <h5>每周读书会</h5>
            <p>周四晚 · 线上</p>
          </div>

          <div class="float-card card-stat">
            <div class="stat-number">2,847</div>
            <div class="stat-label">本月活跃参与者</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 轮盘推荐（仅登录用户可见） -->
    <section class="recommendation-section container-custom">
      <RecommendationWheel />
    </section>

    <!-- Categories Section -->
    <section class="categories-section container-custom">
      <div class="section-header reveal-up">
        <h2>按兴趣探索</h2>
        <RouterLink to="/events" class="see-all-link">
          全部分类
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </RouterLink>
      </div>
      <div class="categories-grid">
        <CategoryCard
          v-for="(category, i) in categories"
          :key="category.id"
          :category="category"
          class="reveal-up"
          :style="`transition-delay: ${i * 0.06}s`"
        />
      </div>
    </section>

    <!-- Hot Events Section -->
    <section class="events-section container-custom">
      <div class="section-header reveal-up">
        <h2>近期热门</h2>
        <RouterLink to="/events" class="see-all-link">
          查看全部
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </RouterLink>
      </div>

      <div v-if="loadingHot" class="loading-placeholder">
        <div class="skeleton-card" v-for="n in 3" :key="n"></div>
      </div>
      <div v-else-if="hotEvents.length > 0" class="events-grid">
        <EventCard
          v-for="event in hotEvents"
          :key="event.id"
          :event="transformEvent(event)"
        />
      </div>
      <div v-else class="empty-tip">暂无热门活动</div>
    </section>

    <!-- Recommended Section（仅登录用户显示） -->
    <section v-if="recommendedEvents.length > 0" class="events-section container-custom">
      <div class="section-header reveal-up">
        <h2>为你推荐</h2>
        <RouterLink to="/events" class="see-all-link">
          查看全部
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </RouterLink>
      </div>
      <div v-if="loadingRecommended" class="loading-placeholder">
        <div class="skeleton-card" v-for="n in 3" :key="n"></div>
      </div>
      <div v-else class="events-grid">
        <EventCard
          v-for="event in recommendedEvents"
          :key="event.id"
          :event="transformEvent(event)"
        />
      </div>
    </section>

    <!-- New Events Section -->
    <section class="events-section container-custom">
      <div class="section-header reveal-up">
        <h2>最新发布</h2>
        <RouterLink to="/events" class="see-all-link">
          查看全部
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
        </RouterLink>
      </div>
      <div v-if="loadingNew" class="loading-placeholder">
        <div class="skeleton-card" v-for="n in 3" :key="n"></div>
      </div>
      <div v-else-if="newEvents.length > 0" class="events-grid">
        <EventCard
          v-for="event in newEvents"
          :key="event.id"
          :event="transformEvent(event)"
        />
      </div>
      <div v-else class="empty-tip">暂无最新活动</div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section reveal-up">
      <div class="cta-inner container-custom">
        <div class="cta-left">
          <h2>想办一场属于自己的活动？</h2>
          <p>无论规模大小，在这里都能找到志同道合的参与者。</p>
        </div>
        <RouterLink to="/create-event" class="btn-primary-gold">立即发布</RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { activityApi, categories, type Activity } from '../utils/mockData'
import CategoryCard from '../components/CategoryCard.vue'
import EventCard from '../components/EventCard.vue'
import RecommendationWheel from '../components/RecommendationWheel.vue'

const hotEvents = ref<Activity[]>([])
const newEvents = ref<Activity[]>([])
const recommendedEvents = ref<Activity[]>([])
const loadingHot = ref(true)
const loadingNew = ref(true)
const loadingRecommended = ref(false)

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

// 滚动入场动效
const initReveal = () => {
  const els = document.querySelectorAll('.reveal-up, .reveal-right')
  const io = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible')
        io.unobserve(entry.target)
      }
    })
  }, { threshold: 0.12 })
  els.forEach(el => io.observe(el))
}

onMounted(async () => {
  initReveal()

  // 获取热门活动
  try {
    const res = await activityApi.getPopularActivities(0, 6)
    if (res.code === 200 && res.data.content) {
      hotEvents.value = res.data.content
    }
  } catch (e) {
    console.error('获取热门活动失败:', e)
  }
  loadingHot.value = false

  // 获取最新活动
  try {
    const res = await activityApi.getActivities({ page: 0, size: 6 })
    if (res.code === 200 && res.data.content) {
      newEvents.value = res.data.content
    }
  } catch (e) {
    console.error('获取最新活动失败:', e)
  }
  loadingNew.value = false

  // 获取推荐活动（仅登录用户）
  const token = localStorage.getItem('token')
  if (token) {
    try {
      loadingRecommended.value = true
      const res = await activityApi.getRecommendedActivities(0, 6)
      if (res.code === 200 && res.data.content) {
        recommendedEvents.value = res.data.content
      }
    } catch (e) {
      console.error('获取推荐活动失败:', e)
    }
    loadingRecommended.value = false
  }
})
</script>

<style scoped>
/* ─── Hero ─────────────────────────────────── */
.hero-section {
  min-height: 86vh;
  display: flex;
  align-items: center;
  background: #fff;
  padding: 120px 0 80px;
  position: relative;
}

.hero-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  pointer-events: none;
  z-index: 0;
}
.orb-1 {
  width: 480px; height: 480px;
  background: rgba(212,175,55,0.08);
  top: -100px; right: 5%;
}
.orb-2 {
  width: 300px; height: 300px;
  background: rgba(0,0,0,0.03);
  bottom: -60px; left: 10%;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
}

@media (max-width: 900px) {
  .hero-content { grid-template-columns: 1fr; gap: 48px; }
  .hero-float-cards { display: none; }
}

.hero-tag {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #d4af37;
  border: 1px solid rgba(212,175,55,0.4);
  padding: 6px 14px;
  border-radius: 999px;
  margin-bottom: 24px;
}

.hero-title {
  font-size: clamp(2rem, 4vw, 3.2rem);
  font-weight: 700;
  line-height: 1.18;
  color: #111;
  letter-spacing: -0.03em;
  margin-bottom: 20px;
}

.hero-desc {
  font-size: 1.05rem;
  color: #555;
  line-height: 1.75;
  max-width: 480px;
  margin-bottom: 36px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.btn-primary-gold {
  background: #111;
  color: #fff;
  padding: 14px 32px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.95rem;
  letter-spacing: 0.02em;
  transition: all 0.25s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1.5px solid #111;
}
.btn-primary-gold:hover {
  background: #d4af37;
  border-color: #d4af37;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(212,175,55,0.3);
}

.btn-ghost-dark {
  background: transparent;
  color: #111;
  padding: 14px 32px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 0.95rem;
  letter-spacing: 0.02em;
  transition: all 0.25s ease;
  text-decoration: none;
  border: 1.5px solid #ddd;
}
.btn-ghost-dark:hover {
  background: #f5f5f5;
  border-color: #ccc;
  transform: translateY(-1px);
}

/* ─── Hero Float Cards ───────────────────────── */
.hero-float-cards {
  position: relative;
  height: 400px;
}

.float-card {
  position: absolute;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.08);
  overflow: hidden;
  transition: transform 0.4s ease;
}
.float-card:hover { transform: translateY(-4px) !important; }

.card-main {
  width: 260px;
  top: 0; left: 20px;
  animation: floatY 5s ease-in-out infinite;
}
.card-main .float-card-img { height: 140px; }
.card-main .float-card-img img { width: 100%; height: 100%; object-fit: cover; }
.card-main .float-card-body { padding: 16px; }
.card-main .float-card-body h4 { font-size: 0.95rem; font-weight: 700; color: #111; margin: 6px 0 4px; }
.card-main .float-card-body p { font-size: 0.8rem; color: #888; }

.float-card-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  color: #d4af37;
  background: rgba(212,175,55,0.1);
  padding: 3px 10px;
  border-radius: 999px;
}

.float-card-avatars {
  display: flex;
  align-items: center;
  margin-top: 12px;
  gap: 4px;
}
.float-card-avatars img {
  width: 28px; height: 28px;
  border-radius: 50%;
  border: 2px solid #fff;
  margin-left: -6px;
}
.float-card-avatars img:first-child { margin-left: 0; }
.float-card-avatars span {
  font-size: 11px; color: #888; margin-left: 6px;
}

.card-small {
  width: 160px;
  padding: 16px;
  right: 0; top: 60px;
  animation: floatY 6s ease-in-out infinite reverse;
}
.card-small h5 { font-size: 0.88rem; font-weight: 700; color: #111; margin: 8px 0 4px; }
.card-small p { font-size: 0.78rem; color: #888; }

.card-stat {
  width: 150px;
  padding: 20px 16px;
  bottom: 0; left: 60px;
  text-align: center;
  animation: floatY 7s ease-in-out infinite;
  border: 1px solid #f0f0f0;
}
.stat-number { font-size: 1.6rem; font-weight: 800; color: #111; letter-spacing: -0.03em; }
.stat-label { font-size: 0.78rem; color: #888; margin-top: 4px; }

@keyframes floatY {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

/* ─── 轮盘推荐 ─────────────────────────── */
.recommendation-section {
  padding: 40px 0;
  background: #fafafa;
}

/* ─── Section 通用 ────────────────────────── */
.categories-section,
.events-section {
  padding: 80px 0;
  border-top: 1px solid #f2f2f2;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 40px;
}
.section-header h2 {
  font-size: 1.7rem;
  font-weight: 700;
  color: #111;
  letter-spacing: -0.02em;
}
.see-all-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.88rem;
  font-weight: 500;
  color: #888;
  text-decoration: none;
  transition: color 0.2s;
}
.see-all-link:hover { color: #d4af37; }

/* ─── Categories Grid ─────────────────────── */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}
@media (max-width: 1024px) { .categories-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 640px) { .categories-grid { grid-template-columns: repeat(2, 1fr); } }

/* ─── Events Grid ─────────────────────────── */
.events-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
@media (max-width: 1024px) { .events-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .events-grid { grid-template-columns: 1fr; } }

/* ─── Loading Skeleton ──────────────────── */
.loading-placeholder {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.skeleton-card {
  height: 280px;
  background: linear-gradient(90deg, #f5f5f5 25%, #ebebeb 50%, #f5f5f5 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 16px;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ─── CTA ─────────────────────────────────── */
.cta-section {
  background: #111;
  padding: 80px 0;
  margin-top: 0;
}
.cta-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
  flex-wrap: wrap;
}
.cta-left h2 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  margin-bottom: 8px;
}
.cta-left p {
  color: #888;
  font-size: 1rem;
}
.cta-section .btn-primary-gold {
  background: #d4af37;
  border-color: #d4af37;
  color: #111;
  white-space: nowrap;
}
.cta-section .btn-primary-gold:hover {
  background: #c9a227;
  border-color: #c9a227;
  color: #111;
}

/* ─── 滚动入场动效 ─────────────────────── */
.reveal-up {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}
.reveal-right {
  opacity: 0;
  transform: translateX(40px);
  transition: opacity 0.7s ease, transform 0.7s ease;
}
.reveal-up.is-visible,
.reveal-right.is-visible {
  opacity: 1;
  transform: none;
}

/* ─── 空状态 ──────────────────────────── */
.empty-tip {
  text-align: center;
  padding: 48px;
  color: #aaa;
  font-size: 0.95rem;
}
</style>
