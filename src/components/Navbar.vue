<template>
  <nav
    class="fixed top-0 left-0 right-0 z-50 transition-all duration-300"
    :class="[
      scrolled
        ? 'glass shadow-nav border-b border-black/[0.08]'
        : 'bg-transparent'
    ]"
  >
    <div class="container-custom">
      <div class="flex items-center justify-between h-14">

        <!-- Logo -->
        <RouterLink to="/" class="flex items-center gap-2.5 group" aria-label="邻里首页">
          <!-- SVG Logo -->
          <div class="w-8 h-8 flex items-center justify-center">
            <svg width="28" height="28" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg"
                 class="transition-transform duration-300 group-hover:scale-110">
              <circle cx="20" cy="20" r="18" stroke="#1d1d1f" stroke-width="1.5"/>
              <circle cx="20" cy="14" r="3.5" fill="#1d1d1f"/>
              <circle cx="11" cy="22.5" r="2.8" fill="#1d1d1f" opacity="0.5"/>
              <circle cx="29" cy="22.5" r="2.8" fill="#1d1d1f" opacity="0.5"/>
              <path d="M14 22.5 Q20 18.5 26 22.5" stroke="#1d1d1f" stroke-width="1.5" fill="none" stroke-linecap="round"/>
              <path d="M10 30 Q20 26.5 30 30" stroke="#1d1d1f" stroke-width="1.2" fill="none" stroke-linecap="round" opacity="0.35"/>
            </svg>
          </div>
          <span class="text-[17px] font-semibold tracking-tight text-primary-900">
            邻聚
          </span>
        </RouterLink>

        <!-- Center Nav Links -->
        <div class="hidden md:flex items-center gap-1">
          <RouterLink
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link-item"
            :class="isActive(item.path) ? 'text-primary-900 font-medium' : 'text-primary-500'"
          >
            {{ item.label }}
          </RouterLink>
        </div>

        <!-- Right Actions -->
        <div class="flex items-center gap-3">

          <!-- Search (桌面端) -->
          <div
            class="hidden lg:flex items-center gap-2 px-3 py-2 rounded-xl bg-primary-100 hover:bg-primary-200 transition-colors duration-200 cursor-pointer"
            @click="openSearch"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="text-primary-500">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/>
            </svg>
            <span class="text-sm text-primary-400 w-28">搜索活动...</span>
            <kbd class="text-[10px] text-primary-400 border border-primary-300 rounded px-1">⌘K</kbd>
          </div>

          <template v-if="isAuthenticated">
            <!-- 通知铃铛 -->
            <RouterLink
              to="/notifications"
              class="relative p-2 rounded-xl hover:bg-primary-100 transition-colors duration-200"
            >
              <Bell class="w-5 h-5 text-primary-500" />
              <span
                v-if="unreadCount > 0"
                class="absolute -top-0.5 -right-0.5 w-4.5 h-4.5 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center min-w-[18px] min-h-[18px]"
              >
                {{ unreadCount > 99 ? '99+' : unreadCount }}
              </span>
            </RouterLink>

            <!-- 发布活动 -->
            <RouterLink to="/create-event" class="btn-primary text-sm py-2 px-4">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M12 5v14M5 12h14"/>
              </svg>
              发布
            </RouterLink>

            <!-- 用户菜单 -->
            <div class="relative" ref="userMenuRef">
              <button
                @click="showUserMenu = !showUserMenu"
                class="flex items-center gap-2 px-2 py-1.5 rounded-xl hover:bg-primary-100 transition-colors duration-200 group"
              >
                <div class="w-7 h-7 rounded-full bg-primary-900 flex items-center justify-center text-white text-xs font-semibold ring-2 ring-transparent group-hover:ring-primary-200 transition-all">
                  {{ userInitial }}
                </div>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     class="text-primary-400 transition-transform duration-200"
                     :class="showUserMenu ? 'rotate-180' : ''">
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </button>

              <!-- Dropdown -->
              <Transition name="dropdown">
                <div
                  v-if="showUserMenu"
                  class="absolute right-0 mt-2 w-52 bg-white rounded-2xl shadow-modal border border-primary-100 py-2 z-50 overflow-hidden"
                >
                  <!-- 用户信息头部 -->
                  <div class="px-4 py-3 border-b border-primary-100">
                    <p class="text-sm font-semibold text-primary-900">{{ currentUser?.nickname || currentUser?.username }}</p>
                    <p class="text-xs text-primary-400 mt-0.5">社区成员</p>
                  </div>

                  <div class="py-1">
                    <RouterLink
                      to="/profile"
                      @click="showUserMenu = false"
                      class="dropdown-item"
                    >
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                      </svg>
                      个人中心
                    </RouterLink>
                    <RouterLink
                      v-if="isAdmin"
                      to="/admin"
                      @click="showUserMenu = false"
                      class="dropdown-item text-accent-600"
                    >
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                      </svg>
                      管理后台
                    </RouterLink>
                  </div>

                  <div class="border-t border-primary-100 pt-1">
                    <button
                      @click="handleLogout"
                      class="dropdown-item w-full text-left text-red-500 hover:bg-red-50"
                    >
                      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
                      </svg>
                      退出登录
                    </button>
                  </div>
                </div>
              </Transition>
            </div>
          </template>

          <template v-else>
            <RouterLink to="/login" class="btn-ghost text-sm py-2 px-4">
              登录
            </RouterLink>
            <RouterLink to="/register" class="btn-primary text-sm py-2 px-4">
              注册
            </RouterLink>
          </template>
        </div>
      </div>
    </div>

    <!-- 移动端底部导航 -->
    <div class="md:hidden fixed bottom-0 left-0 right-0 glass border-t border-black/[0.08] safe-area-bottom">
      <div class="flex justify-around px-2 py-2">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex flex-col items-center gap-0.5 px-4 py-2 rounded-xl transition-colors duration-200"
          :class="isActive(item.path) ? 'text-primary-900' : 'text-primary-400'"
        >
          <component :is="item.icon" class="w-5 h-5" />
          <span class="text-[10px] font-medium">{{ item.label }}</span>
        </RouterLink>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { Home, Calendar, User, Bell } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const showUserMenu = ref(false)
const userMenuRef = ref<HTMLElement | null>(null)
const scrolled = ref(false)
const unreadCount = ref(0)

// 获取通知未读数量
const fetchUnreadCount = async () => {
  const token = localStorage.getItem('token')
  if (!token) { unreadCount.value = 0; return }
  try {
    const res = await fetch('/api/notifications/unread-count', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      unreadCount.value = data.data.unreadCount || 0
    }
  } catch {}
}

// 登录状态
const token = ref(localStorage.getItem('token'))
const userStr = ref(localStorage.getItem('user'))

const isAuthenticated = computed(() => !!token.value)

const currentUser = computed(() => {
  if (!userStr.value) return null
  try { return JSON.parse(userStr.value) } catch { return null }
})

const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

const userInitial = computed(() => {
  const user = currentUser.value
  if (!user) return 'U'
  return (user.nickname || user.username || 'U').charAt(0).toUpperCase()
})

const navItems = [
  { path: '/', label: '首页', icon: Home },
  { path: '/events', label: '活动', icon: Calendar },
  { path: '/profile', label: '我的', icon: User },
]

// 判断当前路由是否活跃
const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const openSearch = () => {
  router.push('/events')
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  token.value = null
  userStr.value = null
  showUserMenu.value = false
  window.dispatchEvent(new CustomEvent('auth-change'))
  router.push('/login')
}

// 滚动监听 - 控制毛玻璃效果
const handleScroll = () => {
  scrolled.value = window.scrollY > 20
}

// 点击外部关闭菜单
const handleClickOutside = (event: MouseEvent) => {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target as Node)) {
    showUserMenu.value = false
  }
}

// 监听路由变化
watch(() => route.path, () => {
  token.value = localStorage.getItem('token')
  userStr.value = localStorage.getItem('user')
  showUserMenu.value = false
})

const handleAuthChange = () => {
  token.value = localStorage.getItem('token')
  userStr.value = localStorage.getItem('user')
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('auth-change', handleAuthChange)
  handleScroll()
  handleAuthChange()
  fetchUnreadCount()
  // 每30秒刷新未读数量
  setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('auth-change', handleAuthChange)
})
</script>

<style scoped>
.nav-link-item {
  @apply px-4 py-2 rounded-xl text-sm transition-all duration-200 hover:text-primary-900 hover:bg-primary-100;
}

.dropdown-item {
  @apply flex items-center gap-2.5 px-4 py-2.5 text-sm text-primary-700
         hover:bg-primary-50 hover:text-primary-900 transition-colors duration-150
         cursor-pointer;
}

.dropdown-enter-active {
  animation: slideDown 0.2s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}
.dropdown-leave-active {
  animation: slideDown 0.15s ease reverse both;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.safe-area-bottom {
  padding-bottom: env(safe-area-inset-bottom, 0);
}
</style>
