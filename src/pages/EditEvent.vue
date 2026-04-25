<template>
  <div class="container-custom py-8">
    <!-- Header -->
    <div class="mb-8">
      <button
        @click="router.go(-1)"
        class="flex items-center text-gray-600 hover:text-gray-900 mb-4"
      >
        <ChevronLeft class="w-4 h-4 mr-1" />
        返回
      </button>
      <h1 class="text-3xl font-bold text-gray-900 mb-2">编辑活动</h1>
      <p class="text-gray-600">修改活动信息后提交更新</p>
    </div>

    <div v-if="loading" class="text-center py-16">
      <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-500">加载活动数据中...</p>
    </div>

    <div v-else-if="error && !loading" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600">
      {{ error }}
      <button @click="loadActivity" class="ml-4 underline">重试</button>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Main Form -->
      <div class="lg:col-span-2">
        <!-- Error Message -->
        <div v-if="submitError" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600">
          {{ submitError }}
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Basic Info Card -->
          <div class="card p-6">
            <h2 class="text-xl font-bold text-gray-900 mb-6">基本信息</h2>
            <div class="space-y-4">
              <!-- Title -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  活动标题 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="formData.title"
                  type="text"
                  placeholder="给活动起个吸引人的标题"
                  class="input-field"
                  required
                />
              </div>

              <!-- Description -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  活动描述 <span class="text-red-500">*</span>
                </label>
                <textarea
                  v-model="formData.description"
                  placeholder="详细描述活动内容、流程和注意事项..."
                  class="input-field resize-none"
                  rows="6"
                  required
                />
                <p class="text-xs text-gray-500 mt-1">至少 20 个字符</p>
              </div>

              <!-- Category -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  活动分类
                </label>
                <select
                  v-model="formData.interestId"
                  class="input-field"
                >
                  <option value="">选择分类</option>
                  <option v-for="interest in interests" :key="interest.id" :value="interest.id">
                    {{ interest.icon }} {{ interest.name }}
                  </option>
                </select>
              </div>

              <!-- Fee -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  费用（元）
                </label>
                <input
                  v-model.number="formData.fee"
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0 表示免费"
                  class="input-field"
                />
              </div>
            </div>
          </div>

          <!-- Time & Location Card -->
          <div class="card p-6">
            <h2 class="text-xl font-bold text-gray-900 mb-6">时间和地点</h2>
            <div class="space-y-4">
              <!-- Start Time -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  开始时间 <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <Calendar class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    v-model="formData.startTime"
                    type="datetime-local"
                    class="input-field pl-12"
                    required
                  />
                </div>
              </div>

              <!-- End Time -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  结束时间 <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <Calendar class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    v-model="formData.endTime"
                    type="datetime-local"
                    class="input-field pl-12"
                    required
                  />
                </div>
              </div>

              <!-- Registration Deadline -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  报名截止时间 <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <Calendar class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    v-model="formData.registrationDeadline"
                    type="datetime-local"
                    class="input-field pl-12"
                    required
                  />
                </div>
              </div>

              <!-- Province/City/District -->
              <div class="grid grid-cols-3 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    省份 <span class="text-red-500">*</span>
                  </label>
                  <input
                    v-model="formData.province"
                    type="text"
                    placeholder="如：北京市"
                    class="input-field"
                    required
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    城市 <span class="text-red-500">*</span>
                  </label>
                  <input
                    v-model="formData.city"
                    type="text"
                    placeholder="如：北京市"
                    class="input-field"
                    required
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-2">
                    区县
                  </label>
                  <input
                    v-model="formData.district"
                    type="text"
                    placeholder="如：朝阳区"
                    class="input-field"
                  />
                </div>
              </div>

              <!-- Address -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  详细地址 <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <MapPin class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    v-model="formData.address"
                    type="text"
                    placeholder="详细地址，如：XX公园南门"
                    class="input-field pl-12"
                    required
                  />
                </div>
              </div>

              <!-- Max Participants -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  最大参与人数 <span class="text-red-500">*</span>
                </label>
                <div class="relative">
                  <Users class="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    v-model.number="formData.maxParticipants"
                    type="number"
                    placeholder="如：20"
                    min="1"
                    max="10000"
                    class="input-field pl-12"
                    required
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Additional Info Card -->
          <div class="card p-6">
            <h2 class="text-xl font-bold text-gray-900 mb-6">补充信息</h2>
            <div class="space-y-4">
              <!-- Requirements -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  参与要求
                </label>
                <textarea
                  v-model="formData.requirements"
                  placeholder="如：需要自带运动装备..."
                  class="input-field resize-none"
                  rows="3"
                />
              </div>

              <!-- Contact Phone -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  联系电话 <span class="text-red-500">*</span>
                </label>
                <input
                  v-model="formData.contactPhone"
                  type="tel"
                  placeholder="活动组织者联系电话"
                  class="input-field"
                  required
                />
              </div>

              <!-- Cover Image -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                  封面图片 URL
                </label>
                <input
                  v-model="formData.coverImage"
                  type="url"
                  placeholder="https://..."
                  class="input-field"
                />
                <div v-if="formData.coverImage" class="mt-2">
                  <img :src="formData.coverImage" alt="封面预览" class="w-full h-48 object-cover rounded-lg" />
                </div>
              </div>
            </div>
          </div>

          <!-- Submit Buttons -->
          <div class="flex flex-col sm:flex-row gap-4">
            <button
              type="submit"
              :disabled="submitting"
              class="btn-primary flex-1 flex items-center justify-center"
            >
              <div v-if="submitting" class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin mr-2"></div>
              {{ submitting ? '保存中...' : '保存修改' }}
            </button>
            <button
              type="button"
              @click="router.go(-1)"
              class="btn-secondary"
            >
              取消
            </button>
          </div>
        </form>
      </div>

      <!-- Sidebar Tips -->
      <div class="space-y-6">
        <div class="card p-6 bg-primary-50 border-primary-200">
          <h3 class="font-bold text-gray-900 mb-4">💡 编辑小贴士</h3>
          <ul class="space-y-3 text-sm text-gray-700">
            <li>• 活动开始前可随时编辑信息</li>
            <li>• 修改后需要重新审核</li>
            <li>• 详细描述有助于吸引更多参与者</li>
            <li>• 保持联系方式畅通</li>
          </ul>
        </div>

        <div class="card p-6">
          <h3 class="font-bold text-gray-900 mb-4">📋 活动状态</h3>
          <div class="space-y-2">
            <span
              :class="[
                'px-3 py-1 text-sm font-medium rounded-full',
                statusClass
              ]"
            >
              {{ statusText }}
            </span>
            <p class="text-xs text-gray-500 mt-2">
              当前报名: {{ formData.currentParticipants || 0 }} / {{ formData.maxParticipants || 0 }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Calendar, MapPin, Users, ChevronLeft } from 'lucide-vue-next'
import { activityApi, categories as defaultInterests } from '../utils/mockData'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const error = ref('')
const submitError = ref('')
const submitting = ref(false)
const interests = ref(defaultInterests)

const activityId = computed(() => Number(route.params.id))

const formData = reactive({
  title: '',
  description: '',
  content: '',
  coverImage: '',
  province: '',
  city: '',
  district: '',
  address: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  maxParticipants: 20,
  fee: 0,
  requirements: '',
  contactPhone: '',
  interestId: '' as number | '',
  status: '',
  currentParticipants: 0,
})

const statusClass = computed(() => {
  switch (formData.status) {
    case 'RECRUITING': return 'bg-green-100 text-green-700'
    case 'PENDING': return 'bg-orange-100 text-orange-700'
    case 'ONGOING': return 'bg-blue-100 text-blue-700'
    case 'COMPLETED': return 'bg-gray-100 text-gray-700'
    case 'CANCELLED': return 'bg-red-100 text-red-700'
    default: return 'bg-gray-100 text-gray-700'
  }
})

const statusText = computed(() => {
  switch (formData.status) {
    case 'RECRUITING': return '招募中'
    case 'PENDING': return '待审核'
    case 'ONGOING': return '进行中'
    case 'COMPLETED': return '已结束'
    case 'CANCELLED': return '已取消'
    default: return formData.status || '未知'
  }
})

const loadActivity = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await activityApi.getActivityDetail(activityId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      formData.title = data.title || ''
      formData.description = data.description || ''
      formData.content = data.content || ''
      formData.coverImage = data.coverImage || ''
      formData.province = data.province || ''
      formData.city = data.city || ''
      formData.district = data.district || ''
      formData.address = data.address || ''
      formData.maxParticipants = data.maxParticipants || 20
      formData.fee = data.fee || 0
      formData.requirements = data.requirements || ''
      formData.contactPhone = data.contactPhone || ''
      formData.status = data.status || ''
      formData.currentParticipants = data.currentParticipants || 0

      // 转换时间格式
      if (data.startTime) {
        formData.startTime = data.startTime.slice(0, 16)
      }
      if (data.endTime) {
        formData.endTime = data.endTime.slice(0, 16)
      }
      if (data.registrationDeadline) {
        formData.registrationDeadline = data.registrationDeadline.slice(0, 16)
      }

      // 设置分类（如果有的话）
      if (data.interests && data.interests.length > 0) {
        formData.interestId = data.interests[0].id
      }
    } else {
      error.value = res.message || '获取活动详情失败'
    }
  } catch (e: any) {
    error.value = e.message || '获取活动详情失败，请检查网络'
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  // 检查登录
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }

  // 表单验证
  if (!formData.title || !formData.description || !formData.address) {
    submitError.value = '请填写必填项（标题、描述、地址）'
    return
  }

  // 验证时间
  if (!formData.startTime || !formData.endTime || !formData.registrationDeadline) {
    submitError.value = '请填写活动时间（开始时间、结束时间、报名截止时间）'
    return
  }

  const startTimeDate = new Date(formData.startTime)
  const endTimeDate = new Date(formData.endTime)
  const deadlineDate = new Date(formData.registrationDeadline)

  if (isNaN(startTimeDate.getTime()) || isNaN(endTimeDate.getTime()) || isNaN(deadlineDate.getTime())) {
    submitError.value = '时间格式不正确，请重新选择'
    return
  }

  if (startTimeDate >= endTimeDate) {
    submitError.value = '结束时间必须晚于开始时间'
    return
  }

  if (deadlineDate >= startTimeDate) {
    submitError.value = '报名截止时间必须早于开始时间'
    return
  }

  submitting.value = true
  submitError.value = ''

  try {
    // 转换时间格式
    const startTime = startTimeDate.toISOString()
    const endTime = endTimeDate.toISOString()
    const registrationDeadline = deadlineDate.toISOString()

    const submitData = {
      title: formData.title,
      description: formData.description,
      content: formData.content || formData.description,
      coverImage: formData.coverImage || '',
      province: formData.province || '北京市',
      city: formData.city || '北京市',
      district: formData.district || '朝阳区',
      address: formData.address,
      latitude: 0,
      longitude: 0,
      startTime,
      endTime,
      registrationDeadline,
      maxParticipants: formData.maxParticipants,
      fee: Number(formData.fee || 0),
      requirements: formData.requirements || '',
      contactPhone: formData.contactPhone || '',
    }

    console.log('提交数据:', submitData)
    const res = await activityApi.updateActivity(activityId.value, submitData)
    console.log('返回结果:', res)

    if (res.code === 200 || res.code === 0) {
      alert('活动更新成功！')
      router.push('/events')
    } else {
      submitError.value = res.message || '更新失败，请检查网络'
    }
  } catch (e: any) {
    console.error('更新活动错误:', e)
    submitError.value = e.message || '更新失败，请检查后端是否运行'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 检查登录状态
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }

  // 检查活动ID
  if (!activityId.value) {
    error.value = '活动ID无效'
    loading.value = false
    return
  }

  loadActivity()
})
</script>