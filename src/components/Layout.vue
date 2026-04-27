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
      :mandatory="isNewUser"
      @close="showInterestModal = false"
      @confirmed="handleInterestConfirmed"
      @skipped="handleInterestSkipped"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import Navbar from './Navbar.vue'
import Footer from './Footer.vue'
import InterestSelectModal from './InterestSelectModal.vue'

const showInterestModal = ref(false)
const isNewUser = ref(false)
const isCheckingInterests = ref(true)

// 检查用户是否已选择兴趣（从后端获取）
const checkUserInterests = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    isCheckingInterests.value = false
    return
  }

  try {
    const res = await fetch('/api/users/me/interests', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const data = await res.json()
    // 如果用户没有任何兴趣标签，强制弹出选择弹窗
    if (data.code === 200 && (!data.data || data.data.length === 0)) {
      isNewUser.value = true
      showInterestModal.value = true
    }
  } catch (e) {
    console.error('检查用户兴趣失败:', e)
  } finally {
    isCheckingInterests.value = false
  }
}

// 新用户必须选择至少3个兴趣才能继续
const handleInterestConfirmed = (ids: number[]) => {
  showInterestModal.value = false
  isNewUser.value = false
  console.log('用户选择了兴趣:', ids)
}

// 新用户不允许跳过
const handleInterestSkipped = () => {
  // 新用户不允许跳过，重新显示弹窗
  if (isNewUser.value) {
    showInterestModal.value = true
    return
  }
  showInterestModal.value = false
}

onMounted(() => {
  checkUserInterests()
})
</script>
