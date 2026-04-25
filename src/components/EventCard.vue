<template>
  <RouterLink :to="`/events/${event.id}`" class="event-card-link">
    <article class="event-card">
      <!-- 封面图 -->
      <div class="card-image">
        <img
          :src="event.images[0]"
          :alt="event.title"
          loading="lazy"
        />
        <!-- 分类标签 -->
        <span class="card-category">{{ event.category }}</span>
        <!-- 热门徽章 -->
        <span v-if="event.isHot" class="card-badge badge-hot">热</span>
      </div>

      <!-- 内容区 -->
      <div class="card-body">
        <h3 class="card-title">{{ event.title }}</h3>

        <div class="card-meta">
          <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            <span>{{ event.date }}<template v-if="event.time"> · {{ event.time }}</template></span>
          </div>
          <div class="meta-item">
            <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>
            </svg>
            <span class="truncate">{{ event.location }}</span>
          </div>
        </div>

        <!-- 人数进度条 -->
        <div class="card-footer">
          <div class="participants-info">
            <div class="organizer">
              <img
                :src="event.organizer?.avatar"
                :alt="event.organizer?.name"
                class="organizer-avatar"
              />
              <span class="organizer-name">{{ event.organizer?.name }}</span>
            </div>
            <span class="participants-count">
              {{ event.participants }}<span class="text-dim">/{{ event.maxParticipants }}</span>
            </span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :class="{
                'progress-danger': participantsPercentage >= 90,
                'progress-warn': participantsPercentage >= 70 && participantsPercentage < 90,
                'progress-ok': participantsPercentage < 70
              }"
              :style="{ width: `${Math.min(participantsPercentage, 100)}%` }"
            />
          </div>
        </div>
      </div>

      <!-- hover 时展示的微光覆盖层 -->
      <div class="card-glow"></div>
    </article>
  </RouterLink>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import type { Event } from '../types'

const props = defineProps<{ event: Event }>()

const participantsPercentage = computed(
  () => (props.event.participants / props.event.maxParticipants) * 100
)
</script>

<style scoped>
.event-card-link {
  display: block;
  text-decoration: none;
}

.event-card {
  background: #fff;
  border-radius: 18px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  position: relative;
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.35s ease,
              border-color 0.25s ease;
  cursor: pointer;
}
.event-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 50px rgba(0,0,0,0.10);
  border-color: transparent;
}

/* ─── 封面图 ────────────── */
.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}
.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
  display: block;
}
.event-card:hover .card-image img {
  transform: scale(1.05);
}

.card-category {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(8px);
  font-size: 11px;
  font-weight: 600;
  color: #333;
  padding: 4px 12px;
  border-radius: 999px;
  letter-spacing: 0.03em;
}

.card-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}
.badge-hot {
  background: #111;
  color: #d4af37;
  letter-spacing: 0.04em;
}

/* ─── 内容区 ─────────────── */
.card-body {
  padding: 18px 20px 20px;
}

.card-title {
  font-size: 1rem;
  font-weight: 700;
  color: #111;
  line-height: 1.45;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}
.event-card:hover .card-title {
  color: #d4af37;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 7px;
  margin-bottom: 16px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 0.82rem;
  color: #888;
}
.meta-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: #bbb;
}
.truncate {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* ─── 底部进度条 ─────────── */
.card-footer { }

.participants-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.organizer {
  display: flex;
  align-items: center;
  gap: 7px;
}
.organizer-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  background: #f0f0f0;
}
.organizer-name {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
}

.participants-count {
  font-size: 0.82rem;
  font-weight: 600;
  color: #111;
}
.text-dim { color: #bbb; font-weight: 400; }

.progress-bar {
  height: 4px;
  background: #f0f0f0;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.6s ease;
}
.progress-ok { background: #111; }
.progress-warn { background: #d4af37; }
.progress-danger { background: #e74c3c; }

/* ─── 微光覆盖层 ─────────── */
.card-glow {
  position: absolute;
  inset: 0;
  border-radius: 18px;
  pointer-events: none;
  background: radial-gradient(circle at 50% 0%, rgba(212,175,55,0.06) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.35s ease;
}
.event-card:hover .card-glow {
  opacity: 1;
}
</style>
