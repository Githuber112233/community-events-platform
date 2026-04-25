<template>
  <RouterLink :to="`/events?category=${category.id}`" class="cat-card-link">
    <div class="cat-card">
      <div class="cat-icon">{{ category.icon }}</div>
      <div class="cat-name">{{ category.name }}</div>
      <div class="cat-arrow">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12h14M12 5l7 7-7 7"/>
        </svg>
      </div>
    </div>
  </RouterLink>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Category } from '../types'
defineProps<{ category: Category }>()
</script>

<style scoped>
.cat-card-link { text-decoration: none; display: block; }

.cat-card {
  background: #fff;
  border: 1.5px solid #f0f0f0;
  border-radius: 16px;
  padding: 20px 16px 18px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}
.cat-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 0%, rgba(212,175,55,0.08) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease;
}
.cat-card:hover {
  border-color: rgba(212,175,55,0.3);
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.07);
}
.cat-card:hover::after { opacity: 1; }

.cat-icon {
  font-size: 2rem;
  margin-bottom: 10px;
  display: block;
  transition: transform 0.3s ease;
}
.cat-card:hover .cat-icon { transform: scale(1.12); }

.cat-name {
  font-size: 0.83rem;
  font-weight: 700;
  color: #333;
  letter-spacing: 0.01em;
  transition: color 0.2s;
}
.cat-card:hover .cat-name { color: #111; }

.cat-arrow {
  margin-top: 8px;
  color: #ccc;
  display: flex;
  justify-content: center;
  transition: all 0.2s ease;
  opacity: 0;
  transform: translateX(-4px);
}
.cat-card:hover .cat-arrow {
  opacity: 1;
  color: #d4af37;
  transform: none;
}
</style>
