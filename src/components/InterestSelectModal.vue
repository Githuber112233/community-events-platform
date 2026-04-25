<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="handleClose">
        <div class="modal-container">
          <div class="modal-header">
            <h2 class="modal-title">选择你的兴趣标签</h2>
            <p class="modal-subtitle">选择你感兴趣的标签，我们为你推荐更精准的活动</p>
            <button class="close-btn" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div class="interests-grid">
              <button
                v-for="interest in allInterests"
                :key="interest.id"
                class="interest-tag"
                :class="{ selected: selectedIds.includes(interest.id) }"
                @click="toggleInterest(interest.id)"
              >
                <span class="tag-name">{{ interest.name }}</span>
                <svg v-if="selectedIds.includes(interest.id)" class="check-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </button>
            </div>
            <p class="selection-hint">
              已选择 <span class="count">{{ selectedIds.length }}</span> 个标签（至少选择3个）
            </p>
          </div>
          <div class="modal-footer">
            <button class="skip-btn" @click="handleSkip">先跳过</button>
            <button
              class="confirm-btn"
              :disabled="selectedIds.length < 3"
              @click="handleConfirm"
            >
              完成选择 ({{ selectedIds.length }})
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi } from '../utils/mockData'

interface Interest {
  id: number
  name: string
  category: string
}

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'confirmed', ids: number[]): void
  (e: 'skipped'): void
}>()

const allInterests = ref<Interest[]>([])
const selectedIds = ref<number[]>([])

const loadInterests = async () => {
  try {
    const res = await userApi.getInterests()
    if (res.code === 200 && res.data) {
      allInterests.value = res.data
    }
  } catch (e) {
    console.error('加载兴趣标签失败:', e)
  }
}

const toggleInterest = (id: number) => {
  const index = selectedIds.value.indexOf(id)
  if (index === -1) {
    selectedIds.value.push(id)
  } else {
    selectedIds.value.splice(index, 1)
  }
}

const handleClose = () => {
  emit('close')
}

const handleSkip = () => {
  emit('skipped')
}

const handleConfirm = async () => {
  if (selectedIds.value.length < 3) return
  try {
    const response = await fetch('/api/users/me/interests', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(selectedIds.value)
    })
    if (!response.ok) {
      throw new Error('保存失败')
    }
  } catch (e) {
    console.error('保存兴趣失败:', e)
  }
  emit('confirmed', selectedIds.value)
}

onMounted(() => {
  loadInterests()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal-container {
  background: #fff;
  border-radius: 20px;
  width: 100%;
  max-width: 520px;
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.modal-header {
  padding: 24px 24px 16px;
  position: relative;
  border-bottom: 1px solid #f0f0f0;
}
.modal-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #111;
  margin: 0 0 6px 0;
}
.modal-subtitle {
  font-size: 0.9rem;
  color: #888;
  margin: 0;
}
.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.close-btn svg { width: 16px; height: 16px; color: #666; }
.close-btn:hover { background: #eee; }
.modal-body {
  padding: 20px 24px;
  flex: 1;
  overflow-y: auto;
}
.interests-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.interest-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 25px;
  font-size: 0.9rem;
  color: #333;
  cursor: pointer;
  transition: all 0.25s ease;
}
.interest-tag:hover { background: #eee; }
.interest-tag.selected {
  background: rgba(212, 175, 55, 0.15);
  border-color: #d4af37;
  color: #111;
}
.tag-name { font-weight: 500; }
.check-icon { width: 14px; height: 14px; color: #d4af37; }
.selection-hint {
  margin-top: 16px;
  font-size: 0.85rem;
  color: #888;
  text-align: center;
}
.selection-hint .count { color: #d4af37; font-weight: 700; }
.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
}
.skip-btn {
  padding: 12px 24px;
  background: transparent;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 0.9rem;
  color: #888;
  cursor: pointer;
  transition: all 0.2s;
}
.skip-btn:hover { background: #f5f5f5; color: #333; }
.confirm-btn {
  padding: 12px 28px;
  background: linear-gradient(135deg, #d4af37, #c9a227);
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 600;
  color: #111;
  cursor: pointer;
  transition: all 0.25s ease;
}
.confirm-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(212, 175, 55, 0.4);
}
.confirm-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-enter-active, .modal-leave-active { transition: all 0.3s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal-container, .modal-leave-to .modal-container {
  transform: scale(0.9) translateY(20px);
}
</style>