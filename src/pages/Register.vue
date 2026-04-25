<template>
  <div class="min-h-screen bg-gradient-to-br from-primary-50 to-secondary-50 flex items-center justify-center px-4 py-12">
    <div class="max-w-md w-full">
      <!-- Back Button -->
      <button
        @click="router.go(-1)"
        class="mb-6 flex items-center text-gray-600 hover:text-gray-900 transition-colors"
      >
        <ArrowLeft class="w-5 h-5 mr-2" />
        返回
      </button>

      <!-- Card -->
      <div class="bg-white rounded-2xl shadow-xl p-8">
        <!-- Header -->
        <div class="text-center mb-8">
          <div class="w-16 h-16 bg-gradient-to-br from-primary-500 to-secondary-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <User class="w-8 h-8 text-white" />
          </div>
          <h1 class="text-2xl font-bold text-gray-900 mb-2">创建账号</h1>
          <p class="text-gray-500">加入社区活动，发现精彩活动</p>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
          {{ error }}
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="space-y-5">
          <!-- Username -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">用户名</label>
            <div class="relative">
              <User class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="formData.username"
                type="text"
                required
                placeholder="请输入用户名"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
              />
            </div>
          </div>

          <!-- Email -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">邮箱地址</label>
            <div class="relative">
              <Mail class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="formData.email"
                type="email"
                required
                placeholder="请输入邮箱"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
              />
            </div>
          </div>

          <!-- Password -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">密码</label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="formData.password"
                :type="showPassword ? 'text' : 'password'"
                required
                :minlength="6"
                placeholder="至少6位密码"
                class="w-full pl-10 pr-12 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                <EyeOff v-if="showPassword" class="w-5 h-5" />
                <Eye v-else class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- Confirm Password -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">确认密码</label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="formData.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                required
                placeholder="请再次输入密码"
                class="w-full pl-10 pr-12 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent outline-none transition-all"
              />
              <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                <EyeOff v-if="showConfirmPassword" class="w-5 h-5" />
                <Eye v-else class="w-5 h-5" />
              </button>
            </div>
          </div>

          <!-- Terms Checkbox -->
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input
                v-model="agreeTerms"
                type="checkbox"
                class="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
            </div>
            <div class="ml-3 text-sm">
              <span class="text-gray-600">我已阅读并同意</span>
              <RouterLink to="/terms" class="text-primary-600 hover:text-primary-700 font-medium">用户协议</RouterLink>
              <span class="text-gray-600">和</span>
              <RouterLink to="/privacy" class="text-primary-600 hover:text-primary-700 font-medium">隐私政策</RouterLink>
            </div>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="isLoading"
            class="w-full py-3 px-4 bg-gradient-to-r from-primary-600 to-secondary-600 text-white font-medium rounded-lg hover:from-primary-700 hover:to-secondary-700 focus:ring-4 focus:ring-primary-200 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
          >
            <div v-if="isLoading" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            <template v-else>
              <CheckCircle class="w-5 h-5 mr-2" />
              注册
            </template>
          </button>
        </form>

        <!-- Divider -->
        <div class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-2 bg-white text-gray-500">或使用以下方式注册</span>
          </div>
        </div>

        <!-- Social Register -->
        <div class="grid grid-cols-2 gap-4">
          <button class="flex items-center justify-center px-4 py-2 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <svg class="w-5 h-5" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            <span class="ml-2 text-sm text-gray-600">Google</span>
          </button>
          <button class="flex items-center justify-center px-4 py-2 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors">
            <svg class="w-5 h-5" viewBox="0 0 24 24" fill="#07C160">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348z"/>
            </svg>
            <span class="ml-2 text-sm text-gray-600">微信</span>
          </button>
        </div>

        <!-- Login Link -->
        <p class="mt-6 text-center text-sm text-gray-600">
          已有账号？
          <RouterLink to="/login" class="ml-1 text-primary-600 hover:text-primary-700 font-medium">
            立即登录
          </RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { Mail, Lock, User, Eye, EyeOff, ArrowLeft, CheckCircle } from 'lucide-vue-next'
import { authApi } from '../utils/api'

const router = useRouter()

const pixelAvatars = [
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Gizmo',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Mochi',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Bongo',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Zippy',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Waffle',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Noodle',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Pepper',
  'https://api.dicebear.com/7.x/pixel-art/svg?seed=Cookie',
]

const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isLoading = ref(false)
const error = ref('')
const agreeTerms = ref(false)

const validateForm = () => {
  if (formData.password !== formData.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return false
  }
  if (formData.password.length < 6) {
    error.value = '密码长度至少为6位'
    return false
  }
  if (!agreeTerms.value) {
    error.value = '请同意用户协议和隐私政策'
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return

  isLoading.value = true
  error.value = ''

  try {
    const randomAvatar = pixelAvatars[Math.floor(Math.random() * pixelAvatars.length)]
    const response = await authApi.register({
      username: formData.username,
      email: formData.email,
      password: formData.password,
      nickname: formData.username, // 使用用户名作为昵称
      avatar: randomAvatar,
    })

    // 注册成功后跳转到登录页
    router.push('/login')
  } catch (err: any) {
    error.value = err.message || '网络错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}
</script>
