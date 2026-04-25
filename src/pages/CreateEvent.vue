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
      <h1 class="text-3xl font-bold text-gray-900 mb-2">发布新活动</h1>
      <p class="text-gray-600">填写以下信息来创建一个新活动</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <!-- Main Form -->
      <div class="lg:col-span-2">
        <!-- Error Message -->
        <div v-if="error" class="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-600">
          {{ error }}
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
                  活动分类 <span class="text-red-500">*</span>
                </label>
                <select
                  v-model="formData.interestId"
                  class="input-field"
                  required
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
              {{ submitting ? '发布中...' : '发布活动' }}
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
          <h3 class="font-bold text-gray-900 mb-4">💡 发布小贴士</h3>
          <ul class="space-y-3 text-sm text-gray-700">
            <li>• 选择清晰的活动标题，让人一眼了解活动内容</li>
            <li>• 详细描述活动流程和注意事项，帮助参与者做好准备</li>
            <li>• 选择合适的分类，方便用户快速找到你的活动</li>
            <li>• 上传高质量的活动图片，提升活动吸引力</li>
            <li>• 设置合理的参与人数上限，确保活动质量</li>
          </ul>
        </div>

        <div class="card p-6">
          <h3 class="font-bold text-gray-900 mb-4">📋 发布规则</h3>
          <ul class="space-y-3 text-sm text-gray-700">
            <li>• 活动内容需真实有效</li>
            <li>• 禁止发布违规或违法内容</li>
            <li>• 请选择适合的地点和时间</li>
            <li>• 活动开始前可随时编辑信息</li>
            <li>• 活动结束后可申请退款处理</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, MapPin, Users, ChevronLeft } from 'lucide-vue-next'
import { activityApi, categories as defaultInterests } from '../utils/mockData'

const router = useRouter()
const submitting = ref(false)
const error = ref('')
const interests = ref(defaultInterests)

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
})

const handleSubmit = async () => {
  // 检查登录
  if (!localStorage.getItem('token')) {
    router.push('/login')
    return
  }

  // 表单验证
  if (!formData.title || !formData.description || !formData.address) {
    error.value = '请填写必填项（标题、描述、地址）'
    return
  }

  // 验证时间
  if (!formData.startTime || !formData.endTime || !formData.registrationDeadline) {
    error.value = '请填写活动时间（开始时间、结束时间、报名截止时间）'
    return
  }

  const startTimeDate = new Date(formData.startTime)
  const endTimeDate = new Date(formData.endTime)
  const deadlineDate = new Date(formData.registrationDeadline)

  if (isNaN(startTimeDate.getTime()) || isNaN(endTimeDate.getTime()) || isNaN(deadlineDate.getTime())) {
    error.value = '时间格式不正确，请重新选择'
    return
  }

  if (startTimeDate >= endTimeDate) {
    error.value = '结束时间必须晚于开始时间'
    return
  }

  if (deadlineDate >= startTimeDate) {
    error.value = '报名截止时间必须早于开始时间'
    return
  }

  submitting.value = true
  error.value = ''

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
      // 后端暂时不支持 interests，先不传
    }

    console.log('提交数据:', submitData)
    const res = await activityApi.createActivity(submitData)
    console.log('返回结果:', res)

    if (res.code === 200 || res.code === 0) {
      alert('活动发布成功！请等待管理员审核')
      router.push('/events')
    } else {
      error.value = res.message || '发布失败，请检查网络'
    }
  } catch (e: any) {
    console.error('发布活动错误:', e)
    error.value = e.message || '发布失败，请检查后端是否运行'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 检查登录状态
  if (!localStorage.getItem('token')) {
    router.push('/login')
  }
})
</script>
