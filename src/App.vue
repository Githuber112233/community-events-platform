<template>
  <div class="min-h-screen flex flex-col">
    <Navbar />
    <main class="flex-1 pt-16 pb-20 md:pb-0">
      <RouterView />
    </main>
    <Footer />

    <!-- 兴趣选择弹窗 -->
    <InterestSelectModal
      :visible="showInterestModal"
      @close="showInterestModal = false"
      @confirmed="handleInterestConfirmed"
      @skipped="handleInterestSkipped"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'
import InterestSelectModal from './components/InterestSelectModal.vue'

const showInterestModal = ref(false)

watch(() => localStorage.getItem('token'), (newToken, oldToken) => {
  if (newToken && !oldToken) {
    // 新登录用户，检查是否选择过兴趣标签
    const hasSelectedInterests = localStorage.getItem('hasSelectedInterests')
    if (!hasSelectedInterests) {
      // 延迟一下显示，等页面加载完成
      setTimeout(() => {
        showInterestModal.value = true
      }, 1000)
    }
  }
}, { immediate: true })

const handleInterestConfirmed = (ids: number[]) => {
  showInterestModal.value = false
  localStorage.setItem('hasSelectedInterests', 'true')
  console.log('用户选择了兴趣:', ids)
}

const handleInterestSkipped = () => {
  showInterestModal.value = false
  // 标记已选择，下次不再弹出（或设置为永久跳过）
  localStorage.setItem('hasSelectedInterests', 'true')
}
</script>