<template>
  <div class="container-custom py-8">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold text-gray-900 mb-2">管理后台</h1>
      <p class="text-gray-600">管理平台活动、用户和审核内容</p>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'users'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">总用户数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.totalUsers }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Users class="w-6 h-6 text-blue-600" />
          </div>
        </div>
      </div>
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'all'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">总活动数</p>
            <p class="text-2xl font-bold text-gray-900">{{ stats.totalActivities }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <Calendar class="w-6 h-6 text-green-600" />
          </div>
        </div>
      </div>
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'pending'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">待审核</p>
            <p class="text-2xl font-bold text-orange-600">{{ stats.pendingActivities }}</p>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
            <AlertCircle class="w-6 h-6 text-orange-600" />
          </div>
        </div>
      </div>
      <div class="card p-6 cursor-pointer hover:shadow-md transition-shadow" @click="activeTab = 'all'">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-gray-500">招募中</p>
            <p class="text-2xl font-bold text-primary-600">{{ stats.recruitingActivities }}</p>
          </div>
          <div class="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
            <CheckCircle class="w-6 h-6 text-primary-600" />
          </div>
        </div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex space-x-4 mb-6 border-b border-gray-200 overflow-x-auto">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="activeTab = tab.id"
        :class="[
          'px-4 py-2 font-medium border-b-2 -mb-px transition-colors whitespace-nowrap',
          activeTab === tab.id
            ? 'border-primary-600 text-primary-600'
            : 'border-transparent text-gray-500 hover:text-gray-700'
        ]"
      >
        {{ tab.label }}
        <span
          v-if="tab.id === 'pending' && stats.pendingActivities > 0"
          class="ml-2 px-2 py-0.5 bg-orange-500 text-white text-xs rounded-full"
        >
          {{ stats.pendingActivities }}
        </span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-16">
      <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-500">加载中...</p>
    </div>

    <!-- Activities List -->
    <div v-else-if="activeTab === 'pending' || activeTab === 'all'" class="space-y-4">
      <div v-if="activities.length === 0" class="text-center py-16 bg-gray-50 rounded-xl">
        <Calendar class="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p class="text-gray-500">暂无数据</p>
      </div>

      <div
        v-for="activity in activities"
        :key="activity.id"
        class="card p-6"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <h3 class="text-lg font-bold text-gray-900">{{ activity.title }}</h3>
              <span
                :class="[
                  'px-2 py-1 text-xs font-medium rounded-full',
                  activity.status === 'PENDING' ? 'bg-orange-100 text-orange-700' :
                  activity.status === 'RECRUITING' ? 'bg-green-100 text-green-700' :
                  activity.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                  'bg-gray-100 text-gray-700'
                ]"
              >
                {{ statusText[activity.status] || activity.status }}
              </span>
            </div>
            <p class="text-sm text-gray-600 mb-2">{{ activity.description?.substring(0, 100) }}...</p>
            <div class="flex items-center gap-4 text-xs text-gray-500">
              <span>{{ activity.creator?.nickname || activity.creator?.username }}</span>
              <span>{{ activity.city }} {{ activity.district }}</span>
              <span>{{ formatDate(activity.startTime) }}</span>
              <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }}</span>
            </div>
          </div>
          <div v-if="activeTab === 'pending'" class="flex gap-2 ml-4">
            <button
              @click="approveActivity(activity.id)"
              class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm font-medium"
            >
              通过
            </button>
            <button
              @click="rejectActivity(activity.id)"
              class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-medium"
            >
              拒绝
            </button>
          </div>
          <div v-else class="flex gap-2 ml-4">
            <button
              @click="deleteActivity(activity.id)"
              class="px-4 py-2 bg-red-100 text-red-600 rounded-lg hover:bg-red-200 text-sm font-medium"
            >
              删除
            </button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex justify-center gap-2 mt-6">
        <button
          v-for="page in totalPages"
          :key="page"
          @click="fetchActivities(page - 1)"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium',
            currentPage === page - 1
              ? 'bg-primary-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          {{ page }}
        </button>
      </div>
    </div>

    <!-- Users List -->
    <div v-else-if="activeTab === 'users'" class="space-y-4">
      <div v-if="users.length === 0" class="text-center py-16 bg-gray-50 rounded-xl">
        <Users class="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p class="text-gray-500">暂无用户</p>
      </div>

      <div
        v-for="user in users"
        :key="user.id"
        class="card p-6"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <img
              :src="user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}`"
              class="w-12 h-12 rounded-full bg-gray-100"
            />
            <div>
              <div class="flex items-center gap-2">
                <h3 class="font-bold text-gray-900">{{ user.nickname || user.username }}</h3>
                <span
                  :class="[
                    'px-2 py-0.5 text-xs font-medium rounded-full',
                    user.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                    user.role === 'ORGANIZER' ? 'bg-blue-100 text-blue-700' :
                    'bg-gray-100 text-gray-700'
                  ]"
                >
                  {{ roleText[user.role] || user.role }}
                </span>
              </div>
              <p class="text-sm text-gray-500">{{ user.email || '未设置邮箱' }}</p>
            </div>
          </div>
          <div class="flex items-center gap-4">
            <div class="text-right text-sm text-gray-500">
              <p>注册于 {{ formatDate(user.createdAt) }}</p>
              <p>积分: {{ user.credits || 0 }}</p>
            </div>
            <button
              @click="viewUserProfile(user.id)"
              class="px-4 py-2 bg-primary-100 text-primary-600 rounded-lg hover:bg-primary-200 text-sm font-medium"
            >
              查看画像
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Interest Weights Tab -->
    <div v-else-if="activeTab === 'interests'" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="card p-4">
          <p class="text-sm text-gray-500">总用户数</p>
          <p class="text-xl font-bold text-gray-900">{{ interestData.totalUsers || 0 }}</p>
        </div>
        <div class="card p-4">
          <p class="text-sm text-gray-500">兴趣标签数</p>
          <p class="text-xl font-bold text-primary-600">{{ interestData.totalInterests || 0 }}</p>
        </div>
        <div class="card p-4">
          <p class="text-sm text-gray-500">最大权重值</p>
          <p class="text-xl font-bold text-orange-600">{{ interestData.maxWeight || 1 }}</p>
        </div>
      </div>

      <div class="card p-4">
        <div class="flex items-center gap-4 text-sm text-gray-600">
          <span class="font-medium">权重色阶：</span>
          <div class="flex items-center gap-1">
            <div class="w-8 h-4 rounded-sm" style="background-color: #e5e7eb"></div>
            <span>无行为</span>
          </div>
          <div class="flex items-center gap-1">
            <div class="w-8 h-4 rounded-sm" style="background-color: #fef3c7"></div>
            <span>低</span>
          </div>
          <div class="flex items-center gap-1">
            <div class="w-8 h-4 rounded-sm" style="background-color: #f59e0b"></div>
            <span>中</span>
          </div>
          <div class="flex items-center gap-1">
            <div class="w-8 h-4 rounded-sm" style="background-color: #d97706"></div>
            <span>高</span>
          </div>
          <div class="flex items-center gap-1">
            <div class="w-8 h-4 rounded-sm" style="background-color: #92400e"></div>
            <span>极高</span>
          </div>
        </div>
      </div>

      <div v-if="interestData.users && interestData.users.length > 0">
        <div
          v-for="user in interestData.users"
          :key="user.id"
          class="card p-5"
        >
          <div class="flex items-center gap-3 mb-4">
            <img
              :src="user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}`"
              class="w-10 h-10 rounded-full bg-gray-100"
            />
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <h3 class="font-bold text-gray-900">{{ user.nickname }}</h3>
                <span
                  :class="[
                    'px-2 py-0.5 text-xs font-medium rounded-full',
                    user.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                    user.role === 'ORGANIZER' ? 'bg-blue-100 text-blue-700' :
                    'bg-gray-100 text-gray-700'
                  ]"
                >
                  {{ roleText[user.role] || user.role }}
                </span>
              </div>
              <p class="text-xs text-gray-500">{{ user.interestCount }} 个兴趣标签</p>
            </div>
          </div>

          <div v-if="user.interests && user.interests.length > 0" class="space-y-2">
            <div
              v-for="interest in user.interests"
              :key="interest.interestId"
              class="flex items-center gap-3"
            >
              <span class="w-20 text-sm text-gray-700 truncate font-medium text-right">{{ interest.interestName }}</span>
              <div class="flex-1 h-6 bg-gray-100 rounded-full overflow-hidden relative">
                <div
                  class="h-full rounded-full transition-all duration-300 ease-out"
                  :style="{
                    width: getWeightBarWidth(interest.weight) + '%',
                    backgroundColor: getWeightColor(interest.weight)
                  }"
                ></div>
                <span class="absolute inset-0 flex items-center justify-center text-xs font-medium" :class="interest.weight <= 1 ? 'text-gray-400' : 'text-gray-700'">
                  {{ interest.weight }}
                </span>
              </div>
              <div class="flex items-center gap-2 text-xs w-36 shrink-0">
                <span
                  class="px-1.5 py-0.5 rounded text-xs font-medium"
                  :class="{
                    'bg-gray-100 text-gray-400': getWeightLevel(interest.weight) === '无',
                    'bg-amber-50 text-amber-600': getWeightLevel(interest.weight) === '低',
                    'bg-amber-100 text-amber-700': getWeightLevel(interest.weight) === '中',
                    'bg-orange-100 text-orange-700': getWeightLevel(interest.weight) === '高',
                    'bg-orange-200 text-orange-800': getWeightLevel(interest.weight) === '极高',
                  }"
                >{{ getWeightLevel(interest.weight) }}</span>
                <span class="text-gray-400">{{ interest.clickCount }}点</span>
                <span class="text-gray-400">{{ interest.participateCount }}参</span>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-3 text-sm text-gray-400">
            该用户暂未选择兴趣标签
          </div>
        </div>
      </div>

      <div v-else class="text-center py-16 bg-gray-50 rounded-xl">
        <BarChart3 class="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <p class="text-gray-500">暂无用户兴趣数据</p>
      </div>
    </div>

    <!-- ========== User-CF 可视化 Tab ========== -->
    <div v-else-if="activeTab === 'recommendation'" class="space-y-6">
      <!-- CF Loading State -->
      <div v-if="cfLoading" class="text-center py-16">
        <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-500">正在计算 User-CF 算法数据...</p>
      </div>

      <template v-else>
      <!-- CF Overview Indicator Cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="card p-4 border-l-4 border-l-blue-500">
          <p class="text-xs text-gray-500 uppercase tracking-wide">用户总数</p>
          <p class="text-2xl font-bold text-gray-900 mt-1">{{ cfData.overview?.totalUsers || 0 }}</p>
        </div>
        <div class="card p-4 border-l-4 border-l-amber-500">
          <p class="text-xs text-gray-500 uppercase tracking-wide">相似度连线</p>
          <p class="text-2xl font-bold text-amber-600 mt-1">{{ cfData.overview?.totalSimilarityEdges || 0 }}</p>
        </div>
        <div class="card p-4 border-l-4 border-l-green-500">
          <p class="text-xs text-gray-500 uppercase tracking-wide">CF 活跃用户</p>
          <p class="text-2xl font-bold text-green-600 mt-1">{{ cfData.overview?.cfActiveUsers || 0 }}</p>
          <p class="text-xs text-gray-400 mt-0.5">置信度 &gt; 0</p>
        </div>
        <div class="card p-4 border-l-4 border-l-purple-500">
          <p class="text-xs text-gray-500 uppercase tracking-wide">平均 CF 置信度</p>
          <p class="text-2xl font-bold text-purple-600 mt-1">{{ ((cfData.overview?.avgCfConfidence || 0) * 100).toFixed(1) }}%</p>
          <p class="text-xs text-gray-400 mt-0.5">最高60%</p>
        </div>
      </div>

      <!-- Algorithm Description -->
      <div class="card p-4 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100">
        <div class="flex items-start gap-3">
          <div class="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center shrink-0 mt-0.5">
            <Brain class="w-4 h-4 text-blue-600" />
          </div>
          <div class="text-sm">
            <p class="font-semibold text-blue-900 mb-1">User-CF 协同过滤算法</p>
            <p class="text-blue-700/70">
              使用杰卡德系数(Jaccard Similarity)计算用户间相似度，基于共同参与活动的重叠度。
              无活动数据时自动降级为兴趣标签杰卡德兜底。采用置信度加权融合策略：
              <code class="bg-blue-100 px-1 rounded text-xs">CB权重 = 1 - cfConfidence × 0.6</code>，
              <code class="bg-blue-100 px-1 rounded text-xs">CF权重 = cfConfidence × 0.6</code>。
              CF 置信度 = min(1, 行为数 / (阈值×5))，保证数据稀疏时 CB 至少占 40%。
            </p>
          </div>
        </div>
      </div>

      <!-- Charts Grid -->
      <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <!-- Heatmap: User Similarity Matrix -->
        <div class="card p-5">
          <h3 class="text-lg font-bold text-gray-900 mb-1">用户相似度热力图</h3>
          <p class="text-xs text-gray-500 mb-4">颜色越深表示两用户越相似（杰卡德系数越高）</p>
          <div ref="heatmapRef" style="width: 100%; height: 400px;"></div>
        </div>

        <!-- Network Graph: User Relationship -->
        <div class="card p-5">
          <h3 class="text-lg font-bold text-gray-900 mb-1">用户关系网络图</h3>
          <p class="text-xs text-gray-500 mb-4">节点=用户，连线=相似关系，连线粗细=相似度</p>
          <div ref="networkRef" style="width: 100%; height: 400px;"></div>
        </div>

        <!-- Weight Metrics Bar Chart -->
        <div class="card p-5">
          <h3 class="text-lg font-bold text-gray-900 mb-1">融合权重分布</h3>
          <p class="text-xs text-gray-500 mb-4">每个用户的 CB/CF 权重占比（行为越多CF权重越高）</p>
          <div ref="weightBarRef" style="width: 100%; height: 400px;"></div>
        </div>

        <!-- Confidence Scatter Plot -->
        <div class="card p-5">
          <h3 class="text-lg font-bold text-gray-900 mb-1">CF 置信度分析</h3>
          <p class="text-xs text-gray-500 mb-4">用户行为数 vs CF 置信度（越多行为越可信）</p>
          <div ref="confidenceRef" style="width: 100%; height: 400px;"></div>
        </div>
      </div>

      <!-- Recommendation Path Table -->
      <div class="card p-5">
        <h3 class="text-lg font-bold text-gray-900 mb-1">推荐路径明细</h3>
        <p class="text-xs text-gray-500 mb-4">展示"我 → 相似用户 → 推荐活动"的完整推荐链路</p>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-gray-200">
                <th class="text-left py-2 px-3 text-gray-500 font-medium">目标用户</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium">相似用户</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium">杰卡德系数</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium">推荐活动ID</th>
                <th class="text-left py-2 px-3 text-gray-500 font-medium">推荐评分</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="path in cfData.recommendationPaths" :key="path.userId">
                <tr
                  v-for="(step, idx) in path.steps"
                  :key="`${path.userId}-${idx}`"
                  class="border-b border-gray-100 hover:bg-gray-50 transition-colors"
                >
                  <td class="py-2 px-3 font-medium" v-if="idx === 0" :rowspan="path.steps.length">
                    <div class="flex items-center gap-2">
                      <div class="w-6 h-6 bg-primary-100 rounded-full flex items-center justify-center">
                        <span class="text-xs font-bold text-primary-600">{{ path.userName?.charAt(0) }}</span>
                      </div>
                      {{ path.userName }}
                      <span class="text-xs text-gray-400">({{ path.recommendedActivityCount }} 推荐)</span>
                    </div>
                  </td>
                  <td class="py-2 px-3">
                    <span class="px-2 py-0.5 bg-blue-50 text-blue-700 rounded text-xs">{{ step.similarUserName }}</span>
                  </td>
                  <td class="py-2 px-3">
                    <span
                      class="px-2 py-0.5 rounded-full text-xs font-medium"
                      :class="step.similarity >= 0.5 ? 'bg-green-100 text-green-700' : step.similarity >= 0.2 ? 'bg-amber-100 text-amber-700' : 'bg-gray-100 text-gray-600'"
                    >{{ (step.similarity * 100).toFixed(1) }}%</span>
                  </td>
                  <td class="py-2 px-3 font-mono text-xs text-gray-600">#{{ step.activityId }}</td>
                  <td class="py-2 px-3">
                    <div class="flex items-center gap-2">
                      <div class="w-16 h-2 bg-gray-100 rounded-full overflow-hidden">
                        <div
                          class="h-full bg-primary-500 rounded-full"
                          :style="{ width: Math.min(100, step.score * 100) + '%' }"
                        ></div>
                      </div>
                      <span class="text-xs text-gray-600">{{ step.score.toFixed(3) }}</span>
                    </div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
          <div v-if="!cfData.recommendationPaths || cfData.recommendationPaths.length === 0" class="text-center py-8 text-gray-400">
            暂无推荐路径数据（需要用户间有相似度 > 0 的关系）
          </div>
        </div>
      </div>
      </template>
    </div>
  </div>

  <!-- User Profile Modal -->
  <div v-if="showProfileModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="showProfileModal = false">
    <div class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto m-4">
      <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
        <h2 class="text-xl font-bold text-gray-900">用户画像</h2>
        <button @click="showProfileModal = false" class="p-2 hover:bg-gray-100 rounded-lg">
          <span class="text-2xl">&times;</span>
        </button>
      </div>

      <div v-if="userProfile" class="p-6 space-y-6">
        <div class="flex items-start gap-6">
          <img
            :src="userProfile.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${userProfile.id}`"
            class="w-20 h-20 rounded-full bg-gray-100"
          />
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-2">
              <h3 class="text-xl font-bold text-gray-900">{{ userProfile.nickname || userProfile.username }}</h3>
              <span
                :class="[
                  'px-2 py-0.5 text-xs font-medium rounded-full',
                  userProfile.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' :
                  userProfile.role === 'ORGANIZER' ? 'bg-blue-100 text-blue-700' :
                  'bg-gray-100 text-gray-700'
                ]"
              >
                {{ roleText[userProfile.role] || userProfile.role }}
              </span>
              <span
                :class="[
                  'px-2 py-0.5 text-xs font-medium rounded-full',
                  userProfile.status === 'ACTIVE' ? 'bg-green-100 text-green-700' :
                  userProfile.status === 'BANNED' ? 'bg-red-100 text-red-700' :
                  'bg-gray-100 text-gray-700'
                ]"
              >
                {{ statusText[userProfile.status] || userProfile.status }}
              </span>
            </div>
            <p class="text-gray-600 mb-2">{{ userProfile.bio || '暂无个人简介' }}</p>
            <div class="flex flex-wrap gap-4 text-sm text-gray-500">
              <span v-if="userProfile.email">{{ userProfile.email }}</span>
              <span v-if="userProfile.phone">{{ userProfile.phone }}</span>
              <span v-if="userProfile.gender">{{ userProfile.gender }}</span>
              <span v-if="userProfile.city">{{ userProfile.province }} {{ userProfile.city }}</span>
            </div>
          </div>
        </div>

        <div class="bg-gray-50 rounded-xl p-4">
          <h4 class="font-bold text-gray-900 mb-4">数据统计</h4>
          <div class="grid grid-cols-4 gap-4">
            <div class="text-center">
              <p class="text-2xl font-bold text-primary-600">{{ userProfile.statistics?.createdActivities || 0 }}</p>
              <p class="text-xs text-gray-500">创建活动</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-green-600">{{ userProfile.statistics?.participatingActivities || 0 }}</p>
              <p class="text-xs text-gray-500">参与活动</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-blue-600">{{ userProfile.statistics?.likedActivities || 0 }}</p>
              <p class="text-xs text-gray-500">点赞活动</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-orange-600">{{ userProfile.statistics?.totalComments || 0 }}</p>
              <p class="text-xs text-gray-500">发表评论</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-red-600">{{ userProfile.statistics?.followers || 0 }}</p>
              <p class="text-xs text-gray-500">粉丝</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-purple-600">{{ userProfile.statistics?.following || 0 }}</p>
              <p class="text-xs text-gray-500">关注</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-pink-600">{{ userProfile.statistics?.totalLikes || 0 }}</p>
              <p class="text-xs text-gray-500">获赞总数</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-bold text-yellow-600">{{ userProfile.credits || 0 }}</p>
              <p class="text-xs text-gray-500">积分</p>
            </div>
          </div>
        </div>

        <div v-if="userProfile.interests && userProfile.interests.length > 0">
          <h4 class="font-bold text-gray-900 mb-3">兴趣标签</h4>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="interest in userProfile.interests"
              :key="interest.id"
              class="px-3 py-1 bg-primary-100 text-primary-700 rounded-full text-sm"
            >
              {{ interest.name }}
              <span class="text-xs text-primary-500 ml-1">x{{ interest.weight || 1 }}</span>
            </span>
          </div>
        </div>

        <div v-if="userProfile.recentActivities && userProfile.recentActivities.length > 0">
          <h4 class="font-bold text-gray-900 mb-3">最近创建的活动</h4>
          <div class="space-y-3">
            <div
              v-for="activity in userProfile.recentActivities"
              :key="activity.id"
              class="border border-gray-200 rounded-lg p-3"
            >
              <div class="flex items-center justify-between">
                <h5 class="font-medium text-gray-900">{{ activity.title }}</h5>
                <span
                  :class="[
                    'px-2 py-0.5 text-xs font-medium rounded-full',
                    activity.status === 'PENDING' ? 'bg-orange-100 text-orange-700' :
                    activity.status === 'RECRUITING' ? 'bg-green-100 text-green-700' :
                    'bg-gray-100 text-gray-700'
                  ]"
                >
                  {{ statusText[activity.status] || activity.status }}
                </span>
              </div>
              <p class="text-sm text-gray-500 mt-1">
                {{ activity.city }} {{ activity.district }} · {{ formatDate(activity.startTime) }}
              </p>
            </div>
          </div>
        </div>

        <div class="text-sm text-gray-500">
          <p>注册时间: {{ formatDate(userProfile.createdAt) }}</p>
          <p>最后活跃: {{ formatDate(userProfile.lastActiveAt) }}</p>
        </div>
      </div>

      <div v-else-if="profileLoading" class="text-center py-16">
        <div class="w-12 h-12 border-3 border-primary-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        <p class="text-gray-500">加载中...</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Users, Calendar, AlertCircle, CheckCircle, BarChart3, Brain } from 'lucide-vue-next'
import * as echarts from 'echarts'

const router = useRouter()
const loading = ref(false)
const activeTab = ref('pending')
const activities = ref<any[]>([])
const users = ref<any[]>([])
const currentPage = ref(0)
const totalPages = ref(0)

// User profile modal
const showProfileModal = ref(false)
const profileLoading = ref(false)
const userProfile = ref<any>(null)

// Interest weight data
const interestData = ref<any>({
  users: [],
  interests: [],
  maxWeight: 1,
  totalUsers: 0,
  totalInterests: 0
})

// User-CF visualization data
const cfData = ref<any>({
  overview: {},
  userNodes: [],
  similarityEdges: [],
  recommendationPaths: [],
  weightMetrics: []
})

// Chart DOM refs
const heatmapRef = ref<HTMLElement | null>(null)
const networkRef = ref<HTMLElement | null>(null)
const weightBarRef = ref<HTMLElement | null>(null)
const confidenceRef = ref<HTMLElement | null>(null)

// Store chart instances for cleanup
let heatmapChart: echarts.ECharts | null = null
let networkChart: echarts.ECharts | null = null
let weightBarChart: echarts.ECharts | null = null
let confidenceChart: echarts.ECharts | null = null

const stats = reactive({
  totalUsers: 0,
  totalActivities: 0,
  pendingActivities: 0,
  recruitingActivities: 0
})

const tabs = [
  { id: 'pending', label: '待审核' },
  { id: 'all', label: '所有活动' },
  { id: 'users', label: '用户管理' },
  { id: 'interests', label: '兴趣权重' },
  { id: 'recommendation', label: '推荐算法' },
]

const statusText: Record<string, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  RECRUITING: '招募中',
  FULL: '已满员',
  ONGOING: '进行中',
  COMPLETED: '已结束',
  CANCELLED: '已取消',
  ACTIVE: '正常',
  INACTIVE: '未激活',
  BANNED: '已封禁'
}

const roleText: Record<string, string> = {
  ADMIN: '管理员',
  ORGANIZER: '组织者',
  USER: '普通用户'
}

const getToken = () => localStorage.getItem('token')

// ===== Weight visualization helpers =====

const getWeightBarWidth = (weight: number): number => {
  if (weight <= 1) return 3
  const maxW = Math.max(interestData.value.maxWeight || 1, 2)
  const ratio = Math.log(1 + weight) / Math.log(1 + maxW)
  return Math.min(100, Math.max(5, ratio * 100))
}

const getWeightColor = (weight: number): string => {
  if (weight <= 1) return '#e5e7eb'
  const maxW = Math.max(interestData.value.maxWeight || 1, 2)
  const ratio = Math.min(1, Math.log(1 + weight) / Math.log(1 + maxW))
  if (ratio < 0.25) return '#fef3c7'
  if (ratio < 0.5) return '#fbbf24'
  if (ratio < 0.75) return '#f59e0b'
  return '#d97706'
}

const getWeightLevel = (weight: number): string => {
  if (weight <= 1) return '无'
  if (weight <= 10) return '低'
  if (weight <= 30) return '中'
  if (weight <= 60) return '高'
  return '极高'
}

// ===== ECharts Chart Builders =====

const buildHeatmap = () => {
  if (!heatmapRef.value || !cfData.value.userNodes || cfData.value.userNodes.length === 0) return

  heatmapChart = echarts.init(heatmapRef.value)
  const nodes = cfData.value.userNodes
  const edges = cfData.value.similarityEdges || []

  // Build similarity matrix
  const n = nodes.length
  const labels = nodes.map((u: any) => u.nickname?.length > 4 ? u.nickname.substring(0, 4) : u.nickname)
  const matrixData: [number, number, number][] = []

  // Build a lookup map
  const simMap = new Map<string, number>()
  edges.forEach((e: any) => {
    const key = `${e.source}-${e.target}`
    simMap.set(key, e.similarity)
    simMap.set(`${e.target}-${e.source}`, e.similarity)
  })

  for (let i = 0; i < n; i++) {
    for (let j = 0; j < n; j++) {
      if (i === j) {
        matrixData.push([j, n - 1 - i, 1]) // self = 1 (diagonal)
      } else {
        const key = `${nodes[i].id}-${nodes[j].id}`
        matrixData.push([j, n - 1 - i, simMap.get(key) || 0])
      }
    }
  }

  const maxSim = cfData.value.overview?.maxSimilarity || 1

  heatmapChart.setOption({
    tooltip: {
      formatter: (params: any) => {
        const val = params.value[2]
        if (params.value[0] === params.value[1]) return `${labels[params.value[0]]} (自身)`
        return `${labels[params.value[0]]} ↔ ${labels[n - 1 - params.value[1]]}<br/>相似度: <b>${(val * 100).toFixed(1)}%</b>`
      }
    },
    grid: { left: 100, right: 40, top: 10, bottom: 80 },
    xAxis: {
      type: 'category',
      data: labels,
      splitArea: { show: true },
      axisLabel: { rotate: 45, fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: [...labels].reverse(),
      splitArea: { show: true },
      axisLabel: { fontSize: 11 }
    },
    visualMap: {
      min: 0,
      max: maxSim,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 0,
      inRange: {
        color: ['#f0f0f0', '#fff7bc', '#fec44f', '#d95f0e', '#993404']
      },
      text: ['低相似', '高相似'],
      textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'heatmap',
      data: matrixData,
      label: {
        show: n <= 10,
        formatter: (params: any) => params.value[2] > 0 ? (params.value[2] * 100).toFixed(0) + '%' : ''
      },
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.3)' }
      }
    }]
  })
}

const buildNetworkGraph = () => {
  if (!networkRef.value || !cfData.value.userNodes || cfData.value.userNodes.length === 0) return

  networkChart = echarts.init(networkRef.value)
  const nodes = cfData.value.userNodes
  const edges = cfData.value.similarityEdges || []

  const graphNodes = nodes.map((u: any) => ({
    id: String(u.id),
    name: u.nickname?.length > 6 ? u.nickname.substring(0, 6) + '..' : u.nickname,
    symbolSize: Math.max(20, u.activityCount * 8 + 15),
    category: u.role === 'ADMIN' ? 2 : u.activityCount >= 3 ? 1 : 0,
    label: { show: true, fontSize: 11 },
    itemStyle: {
      color: u.role === 'ADMIN' ? '#8b5cf6' : u.activityCount >= 3 ? '#3b82f6' : '#6b7280'
    }
  }))

  const graphEdges = edges.map((e: any) => ({
    source: String(e.source),
    target: String(e.target),
    lineStyle: {
      width: Math.max(1, e.similarity * 10),
      color: e.similarity >= 0.5 ? '#10b981' : e.similarity >= 0.2 ? '#f59e0b' : '#d1d5db',
      opacity: Math.max(0.3, e.similarity)
    },
    value: e.similarity
  }))

  networkChart.setOption({
    tooltip: {
      formatter: (params: any) => {
        if (params.dataType === 'edge') {
          return `${params.data.source} ↔ ${params.data.target}<br/>相似度: ${(params.data.value * 100).toFixed(1)}%`
        }
        return params.name
      }
    },
    legend: {
      data: ['低活跃用户', '活跃用户', '管理员'],
      bottom: 0,
      textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      data: graphNodes,
      links: graphEdges,
      categories: [
        { name: '低活跃用户' },
        { name: '活跃用户' },
        { name: '管理员' }
      ],
      roam: true,
      draggable: true,
      force: {
        repulsion: 200,
        edgeLength: [80, 200],
        gravity: 0.1
      },
      lineStyle: { curveness: 0.2 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 4 }
      }
    }]
  })
}

const buildWeightBarChart = () => {
  if (!weightBarRef.value || !cfData.value.weightMetrics || cfData.value.weightMetrics.length === 0) return

  weightBarChart = echarts.init(weightBarRef.value)
  const metrics = cfData.value.weightMetrics
  const labels = metrics.map((m: any) => m.userName?.length > 5 ? m.userName.substring(0, 5) : m.userName)

  weightBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const m = metrics[params[0].dataIndex]
        return `${m.userName}<br/>CB权重: ${(m.cbWeight * 100).toFixed(1)}%<br/>CF权重: ${(m.cfWeight * 100).toFixed(1)}%<br/>CF置信度: ${(m.cfConfidence * 100).toFixed(1)}%<br/>行为数: ${m.behaviorCount}`
      }
    },
    legend: { data: ['CB 权重', 'CF 权重'], bottom: 0 },
    grid: { left: 80, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%', fontSize: 11 }
    },
    yAxis: {
      type: 'category',
      data: labels,
      axisLabel: { fontSize: 11 }
    },
    series: [
      {
        name: 'CB 权重',
        type: 'bar',
        stack: 'weight',
        data: metrics.map((m: any) => Math.round(m.cbWeight * 1000) / 10),
        itemStyle: { color: '#6366f1' },
        label: { show: metrics.length <= 10, position: 'inside', formatter: '{c}%', fontSize: 10, color: '#fff' }
      },
      {
        name: 'CF 权重',
        type: 'bar',
        stack: 'weight',
        data: metrics.map((m: any) => Math.round(m.cfWeight * 1000) / 10),
        itemStyle: { color: '#f59e0b' },
        label: { show: metrics.length <= 10, position: 'inside', formatter: '{c}%', fontSize: 10, color: '#fff' }
      }
    ]
  })
}

const buildConfidenceScatter = () => {
  if (!confidenceRef.value || !cfData.value.weightMetrics || cfData.value.weightMetrics.length === 0) return

  confidenceChart = echarts.init(confidenceRef.value)
  const metrics = cfData.value.weightMetrics

  const scatterData = metrics.map((m: any) => ({
    value: [m.behaviorCount, Math.round(m.cfConfidence * 10000) / 100, m.userName],
    userName: m.userName
  }))

  // Group by confidence level
  const groups = [
    { name: '低 (0-25%)', color: '#ef4444', min: 0, max: 0.25 },
    { name: '中 (25-50%)', color: '#f59e0b', min: 0.25, max: 0.5 },
    { name: '高 (50-100%)', color: '#10b981', min: 0.5, max: 1.01 }
  ]

  confidenceChart.setOption({
    tooltip: {
      formatter: (params: any) => {
        const d = params.data
        return `${d.userName}<br/>行为数: ${d.value[0]}<br/>CF置信度: ${d.value[1]}%`
      }
    },
    legend: { data: groups.map(g => g.name), bottom: 0 },
    grid: { left: 60, right: 30, top: 30, bottom: 50 },
    xAxis: {
      name: '行为数 (参与+点赞)',
      nameLocation: 'middle',
      nameGap: 30,
      axisLabel: { fontSize: 11 }
    },
    yAxis: {
      name: 'CF 置信度 (%)',
      max: 100,
      axisLabel: { formatter: '{value}%', fontSize: 11 }
    },
    series: groups.map(g => ({
      name: g.name,
      type: 'scatter',
      data: scatterData.filter((d: any) => d.value[1] / 100 >= g.min && d.value[1] / 100 < g.max),
      symbolSize: 16,
      itemStyle: { color: g.color },
      label: {
        show: true,
        formatter: (params: any) => params.data.userName?.charAt(0) || '',
        position: 'inside',
        fontSize: 10,
        color: '#fff',
        fontWeight: 'bold'
      }
    })),
    // Reference line at threshold
    graphic: [{
      type: 'text',
      left: '70%',
      top: '15%',
      style: {
        text: `阈值: ${metrics[0]?.behaviorCount || 20} 次行为`,
        fill: '#9ca3af',
        fontSize: 11
      }
    }]
  })
}

const renderAllCharts = () => {
  // Dispose existing charts first
  heatmapChart?.dispose()
  networkChart?.dispose()
  weightBarChart?.dispose()
  confidenceChart?.dispose()
  heatmapChart = networkChart = weightBarChart = confidenceChart = null

  nextTick(() => {
    buildHeatmap()
    buildNetworkGraph()
    buildWeightBarChart()
    buildConfidenceScatter()
  })
}

// ===== API calls =====

const fetchStatistics = async () => {
  try {
    const res = await fetch('/api/admin/statistics', {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      stats.totalUsers = data.data.totalUsers || 0
      stats.totalActivities = data.data.totalActivities || 0
      stats.pendingActivities = data.data.pendingActivities || 0
      stats.recruitingActivities = data.data.recruitingActivities || 0
    }
  } catch (e) {
    console.error('获取统计失败:', e)
  }
}

const fetchActivities = async (page = 0) => {
  loading.value = true
  try {
    let url = `/api/admin/activities?page=${page}&size=10`
    if (activeTab.value === 'pending') {
      url = `/api/admin/activities/pending?page=${page}&size=10`
    }
    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      activities.value = data.data.content || []
      totalPages.value = data.data.totalPages || 0
      currentPage.value = page
    } else {
      console.error('获取活动失败:', data.message)
      activities.value = []
    }
  } catch (e) {
    console.error('获取活动失败:', e)
    activities.value = []
  } finally {
    loading.value = false
  }
}

const fetchUsers = async (page = 0) => {
  loading.value = true
  try {
    const res = await fetch(`/api/admin/users?page=${page}&size=10`, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      users.value = data.data.content || []
      totalPages.value = data.data.totalPages || 0
    } else {
      console.error('获取用户失败:', data.message)
      users.value = []
    }
  } catch (e) {
    console.error('获取用户失败:', e)
    users.value = []
  } finally {
    loading.value = false
  }
}

const fetchInterestWeights = async () => {
  loading.value = true
  try {
    const res = await fetch('/api/admin/users/interests', {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      interestData.value = data.data
    } else {
      console.error('获取兴趣权重失败:', data.message)
    }
  } catch (e) {
    console.error('获取兴趣权重失败:', e)
  } finally {
    loading.value = false
  }
}

const cfLoading = ref(false)

const fetchUserCFData = async () => {
  cfLoading.value = true
  try {
    const res = await fetch('/api/admin/recommendation/user-cf', {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      cfData.value = data.data
    } else {
      console.error('获取User-CF数据失败:', data.message)
    }
  } catch (e) {
    console.error('获取User-CF数据失败:', e)
  } finally {
    // 先让 cfLoading=false 使 DOM 渲染出图表容器 div，再初始化图表
    cfLoading.value = false
    nextTick(() => {
      // 用 requestAnimationFrame 确保浏览器已布局完成
      requestAnimationFrame(() => renderAllCharts())
    })
  }
}

const approveActivity = async (id: number) => {
  try {
    const res = await fetch(`/api/admin/activities/${id}/approve`, {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('活动已通过审核')
      fetchActivities()
      fetchStatistics()
    } else {
      alert(data.message || '操作失败')
    }
  } catch (e) {
    alert('操作失败')
  }
}

const rejectActivity = async (id: number) => {
  if (!confirm('确定要拒绝该活动吗？')) return
  try {
    const res = await fetch(`/api/admin/activities/${id}/reject`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getToken()}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('活动已拒绝')
      fetchActivities()
      fetchStatistics()
    } else {
      alert(data.message || '操作失败')
    }
  } catch (e) {
    alert('操作失败')
  }
}

const deleteActivity = async (id: number) => {
  if (!confirm('确定要删除该活动吗？此操作不可恢复！')) return
  try {
    const res = await fetch(`/api/admin/activities/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200) {
      alert('活动已删除')
      fetchActivities()
      fetchStatistics()
    } else {
      alert(data.message || '删除失败')
    }
  } catch (e) {
    alert('删除失败')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const viewUserProfile = async (userId: number) => {
  showProfileModal.value = true
  profileLoading.value = true
  userProfile.value = null
  try {
    const res = await fetch(`/api/admin/users/${userId}/profile`, {
      headers: { 'Authorization': `Bearer ${getToken()}` }
    })
    const data = await res.json()
    if (data.code === 200 && data.data) {
      userProfile.value = data.data
    } else {
      alert(data.message || '获取用户画像失败')
      showProfileModal.value = false
    }
  } catch (e) {
    console.error('获取用户画像失败:', e)
    alert('获取用户画像失败')
    showProfileModal.value = false
  } finally {
    profileLoading.value = false
  }
}

// Tab switch
watch(activeTab, (tab) => {
  if (tab === 'users') {
    fetchUsers()
  } else if (tab === 'interests') {
    fetchInterestWeights()
  } else if (tab === 'recommendation') {
    fetchUserCFData()
  } else {
    fetchActivities()
  }
})

// Handle resize
const handleResize = () => {
  heatmapChart?.resize()
  networkChart?.resize()
  weightBarChart?.resize()
  confidenceChart?.resize()
}

onMounted(() => {
  // Check admin role
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    if (user.role !== 'ADMIN') {
      alert('您没有管理员权限')
      router.push('/')
      return
    }
  }

  fetchStatistics()
  fetchActivities()
  window.addEventListener('resize', handleResize)
})

// Cleanup charts on unmount
import { onUnmounted } from 'vue'
onUnmounted(() => {
  heatmapChart?.dispose()
  networkChart?.dispose()
  weightBarChart?.dispose()
  confidenceChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>
