<template>
  <div class="legal-page">
    <div class="container-custom">
      <h1 class="page-title">帮助中心</h1>
      <p class="page-subtitle">快速找到你需要的答案</p>

      <!-- 搜索框 -->
      <div class="search-box">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input type="text" v-model="searchQuery" placeholder="搜索常见问题..." />
      </div>

      <!-- FAQ 分类 -->
      <div class="faq-section" v-for="section in filteredSections" :key="section.title">
        <h2 class="section-title">{{ section.title }}</h2>
        <div class="faq-list">
          <div
            v-for="(item, idx) in section.items"
            :key="idx"
            class="faq-item"
            :class="{ open: openItems.has(`${section.title}-${idx}`) }"
            @click="toggleItem(`${section.title}-${idx}`)"
          >
            <div class="faq-question">
              <span>{{ item.q }}</span>
              <svg class="faq-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
            </div>
            <div class="faq-answer" v-show="openItems.has(`${section.title}-${idx}`)">
              <p v-html="item.a"></p>
            </div>
          </div>
        </div>
      </div>

      <!-- 联系我们 -->
      <div class="contact-card">
        <h2>没找到答案？</h2>
        <p>如果以上内容未能解决你的问题，欢迎通过以下方式联系我们：</p>
        <div class="contact-methods">
          <div class="contact-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
            <div>
              <strong>电子邮件</strong>
              <a href="mailto:liuhb@qq.com">liuhb@qq.com</a>
            </div>
          </div>
          <div class="contact-item">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <div>
              <strong>响应时间</strong>
              <span>通常在 24 小时内回复</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const searchQuery = ref('')
const openItems = ref(new Set<string>())

const toggleItem = (key: string) => {
  const s = new Set(openItems.value)
  s.has(key) ? s.delete(key) : s.add(key)
  openItems.value = s
}

const faqSections = [
  {
    title: '账号与登录',
    items: [
      { q: '如何注册邻聚账号？', a: '点击页面右上角「注册」按钮，填写手机号、用户名和密码即可完成注册。注册成功后系统会自动分配一个头像，你可以在个人主页中修改。' },
      { q: '忘记密码怎么办？', a: '目前暂不支持在线找回密码，请发送邮件至 <a href="mailto:liuhb@qq.com">liuhb@qq.com</a>，提供你的注册手机号或用户名，我们会在核实后帮你重置密码。' },
      { q: '如何修改个人信息？', a: '登录后点击右上角头像进入「个人主页」，可以修改昵称、头像、个人简介等信息。' },
    ]
  },
  {
    title: '活动参与',
    items: [
      { q: '如何报名参加活动？', a: '在活动详情页点击「立即报名」按钮即可。部分活动需要等待组织者审核通过，审核结果会通过活动详情页通知你。' },
      { q: '报名后可以取消吗？', a: '可以。进入「个人主页」→「我的活动」→「已报名」，找到对应活动点击「取消报名」即可。注意：距活动开始不足 24 小时时可能无法取消。' },
      { q: '如何发布活动？', a: '登录后点击导航栏「发布活动」按钮，按页面提示填写活动标题、时间、地点、人数上限、费用说明等信息，提交后即可发布。' },
    ]
  },
  {
    title: '推荐与兴趣',
    items: [
      { q: '推荐活动是怎么生成的？', a: '邻聚采用基于内容 + 用户协同过滤的混合推荐算法，根据你的兴趣标签、历史参与和浏览行为智能推荐你可能感兴趣的活动。' },
      { q: '如何设置兴趣偏好？', a: '首次登录时会弹出兴趣选择弹窗，至少选择 3 个感兴趣的标签。后续可以在个人主页中修改兴趣偏好，系统会据此优化推荐结果。' },
    ]
  },
  {
    title: '社交功能',
    items: [
      { q: '如何关注其他用户？', a: '访问其他用户的个人主页，点击「关注」按钮即可。关注后你可以在「我的关注」列表中查看对方的最新动态。' },
      { q: '如何查看粉丝和关注列表？', a: '进入个人主页，点击粉丝数或关注数即可进入对应的列表页面。' },
    ]
  },
  {
    title: '其他问题',
    items: [
      { q: '邻聚支持哪些平台？', a: '目前邻聚以 Web 端为主，支持主流浏览器（Chrome、Firefox、Safari、Edge），同时适配手机端浏览器访问。' },
      { q: '遇到 Bug 或有功能建议怎么办？', a: '欢迎通过邮件 <a href="mailto:liuhb@qq.com">liuhb@qq.com</a> 反馈问题或提出建议，我们会认真对待每一条反馈。' },
    ]
  }
]

const filteredSections = computed(() => {
  if (!searchQuery.value.trim()) return faqSections
  const kw = searchQuery.value.trim().toLowerCase()
  return faqSections
    .map(s => ({
      ...s,
      items: s.items.filter(item => item.q.toLowerCase().includes(kw) || item.a.toLowerCase().includes(kw))
    }))
    .filter(s => s.items.length > 0)
})
</script>

<style scoped>
.legal-page {
  min-height: 70vh;
  padding: 60px 0 80px;
  background: #fafafa;
}
.page-title {
  font-size: 2rem;
  font-weight: 800;
  color: #111;
  margin: 0 0 8px 0;
  letter-spacing: -0.02em;
}
.page-subtitle {
  font-size: 1rem;
  color: #999;
  margin: 0 0 32px 0;
}
.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 500px;
  padding: 14px 20px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  margin-bottom: 48px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: border-color 0.2s;
}
.search-box:focus-within { border-color: #d4af37; }
.search-box svg { width: 18px; height: 18px; color: #bbb; flex-shrink: 0; }
.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 0.95rem;
  color: #333;
  background: transparent;
}
.search-box input::placeholder { color: #ccc; }

.faq-section { margin-bottom: 40px; }
.section-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #222;
  margin: 0 0 16px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}
.faq-list { display: flex; flex-direction: column; gap: 8px; }
.faq-item {
  background: #fff;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.faq-item:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.faq-item.open { border-color: #d4af37; }
.faq-question {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  font-size: 0.93rem;
  font-weight: 600;
  color: #333;
  user-select: none;
}
.faq-arrow {
  width: 16px;
  height: 16px;
  color: #bbb;
  transition: transform 0.3s;
  flex-shrink: 0;
}
.faq-item.open .faq-arrow { transform: rotate(180deg); color: #d4af37; }
.faq-answer {
  padding: 0 20px 16px;
  font-size: 0.88rem;
  color: #666;
  line-height: 1.75;
}
.faq-answer a { color: #d4af37; text-decoration: none; }
.faq-answer a:hover { text-decoration: underline; }

.contact-card {
  margin-top: 48px;
  padding: 32px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px;
  color: #fff;
}
.contact-card h2 { font-size: 1.2rem; margin: 0 0 8px 0; }
.contact-card > p { font-size: 0.88rem; color: rgba(255,255,255,0.6); margin: 0 0 24px 0; }
.contact-methods { display: flex; gap: 32px; flex-wrap: wrap; }
.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.contact-item svg { width: 20px; height: 20px; color: #d4af37; flex-shrink: 0; }
.contact-item strong { display: block; font-size: 0.82rem; color: rgba(255,255,255,0.7); }
.contact-item a, .contact-item span { font-size: 0.95rem; color: #fff; text-decoration: none; }
.contact-item a:hover { color: #d4af37; }

@media (max-width: 640px) {
  .legal-page { padding: 40px 0 60px; }
  .page-title { font-size: 1.6rem; }
  .contact-methods { flex-direction: column; gap: 20px; }
}
</style>
