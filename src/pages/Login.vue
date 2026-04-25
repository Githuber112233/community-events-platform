<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="login-bg-orb orb-1"></div>
    <div class="login-bg-orb orb-2"></div>

    <div class="login-card">
      <!-- Logo + 标题 -->
      <div class="login-header">
        <div class="login-logo" @click="router.push('/')" title="回到首页">
          <svg width="36" height="36" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="24" r="7" fill="#111"/>
            <circle cx="36" cy="12" r="5" fill="#d4af37"/>
            <circle cx="36" cy="36" r="4" fill="#111" opacity="0.4"/>
            <line x1="12" y1="24" x2="36" y2="12" stroke="#d4af37" stroke-width="1.5" stroke-dasharray="3 2"/>
            <line x1="12" y1="24" x2="36" y2="36" stroke="#333" stroke-width="1.5" stroke-dasharray="3 2"/>
            <line x1="36" y1="12" x2="36" y2="36" stroke="#d4af37" stroke-width="1" stroke-dasharray="2 2" opacity="0.5"/>
          </svg>
        </div>
        <h1>欢迎回来</h1>
        <p>登录继续参与社区活动</p>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="login-error">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        {{ error }}
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="field-group">
          <label>用户名</label>
          <div class="field-input-wrap">
            <svg class="field-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            <input
              v-model="formData.username"
              type="text"
              required
              placeholder="输入用户名"
              autocomplete="username"
            />
          </div>
        </div>

        <div class="field-group">
          <label>密码</label>
          <div class="field-input-wrap">
            <svg class="field-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/>
            </svg>
            <input
              v-model="formData.password"
              :type="showPassword ? 'text' : 'password'"
              required
              placeholder="输入密码"
              autocomplete="current-password"
            />
            <button type="button" class="toggle-pw" @click="showPassword = !showPassword" tabindex="-1">
              <svg v-if="showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="field-extras">
          <label class="remember-label">
            <input type="checkbox" v-model="rememberMe" />
            <span>记住我</span>
          </label>
          <RouterLink to="/forgot-password" class="forgot-link">忘记密码</RouterLink>
        </div>

        <button type="submit" :disabled="isLoading" class="btn-submit">
          <span v-if="isLoading" class="spinner"></span>
          <span v-else>登录</span>
        </button>
      </form>

      <!-- 分割线 -->
      <div class="divider"><span>或</span></div>

      <!-- 第三方登录 -->
      <div class="social-login">
        <button class="social-btn">
          <svg width="18" height="18" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          <span>Google 登录</span>
        </button>
        <button class="social-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#07C160">
            <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348z"/>
          </svg>
          <span>微信登录</span>
        </button>
      </div>

      <!-- 注册跳转 -->
      <p class="register-tip">
        没有账号？
        <RouterLink to="/register">立即注册</RouterLink>
      </p>

      <!-- 返回首页 -->
      <button class="back-btn" @click="router.go(-1)">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { authApi } from '../utils/api'

const router = useRouter()

const formData = reactive({ username: '', password: '' })
const showPassword = ref(false)
const rememberMe = ref(false)
const isLoading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  isLoading.value = true
  error.value = ''
  try {
    const res = await authApi.login(formData)
    const { token, user } = res.data
    localStorage.setItem('token', token)
    localStorage.setItem('user', JSON.stringify(user))
    window.location.href = '/'
  } catch (err: any) {
    error.value = err.message || '网络错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  padding: 24px;
  position: relative;
  overflow: hidden;
}

.login-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}
.orb-1 {
  width: 400px; height: 400px;
  background: rgba(212,175,55,0.07);
  top: -100px; right: -50px;
}
.orb-2 {
  width: 300px; height: 300px;
  background: rgba(0,0,0,0.03);
  bottom: -80px; left: -40px;
}

/* ─── 卡片 ──────────────── */
.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 24px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 24px 64px rgba(0,0,0,0.08);
  padding: 48px 44px;
  position: relative;
  z-index: 1;
  animation: cardIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
  to   { opacity: 1; transform: none; }
}

/* ─── Header ────────────── */
.login-header {
  text-align: center;
  margin-bottom: 36px;
}
.login-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.login-logo:hover { transform: scale(1.05); }

.login-header h1 {
  font-size: 1.6rem;
  font-weight: 700;
  color: #111;
  letter-spacing: -0.02em;
  margin-bottom: 6px;
}
.login-header p {
  font-size: 0.9rem;
  color: #999;
}

/* ─── 错误提示 ──────────── */
.login-error {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fef2f2;
  border: 1px solid #fee2e2;
  color: #dc2626;
  font-size: 0.85rem;
  padding: 12px 14px;
  border-radius: 10px;
  margin-bottom: 20px;
}

/* ─── 表单 ───────────────── */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.field-group label {
  font-size: 0.83rem;
  font-weight: 600;
  color: #444;
  letter-spacing: 0.01em;
}

.field-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.field-icon {
  position: absolute;
  left: 14px;
  width: 16px;
  height: 16px;
  color: #ccc;
  pointer-events: none;
}

.field-input-wrap input {
  width: 100%;
  padding: 12px 44px 12px 42px;
  border: 1.5px solid #eee;
  border-radius: 12px;
  font-size: 0.93rem;
  color: #111;
  background: #fafafa;
  outline: none;
  transition: all 0.2s ease;
  font-family: inherit;
}
.field-input-wrap input::placeholder { color: #ccc; }
.field-input-wrap input:focus {
  border-color: #d4af37;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(212,175,55,0.12);
}

.toggle-pw {
  position: absolute;
  right: 14px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  color: #bbb;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}
.toggle-pw:hover { color: #666; }

/* ─── 额外选项 ──────────── */
.field-extras {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: -4px;
}

.remember-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.83rem;
  color: #666;
  cursor: pointer;
}
.remember-label input[type="checkbox"] {
  width: 15px;
  height: 15px;
  accent-color: #d4af37;
  cursor: pointer;
}

.forgot-link {
  font-size: 0.83rem;
  color: #999;
  text-decoration: none;
  transition: color 0.2s;
}
.forgot-link:hover { color: #d4af37; }

/* ─── 提交按钮 ──────────── */
.btn-submit {
  width: 100%;
  padding: 14px;
  background: #111;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: inherit;
  margin-top: 4px;
}
.btn-submit:hover:not(:disabled) {
  background: #d4af37;
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(212,175,55,0.3);
}
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.spinner {
  width: 18px; height: 18px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ─── 分割线 ─────────────── */
.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 24px 0 16px;
  color: #ddd;
  font-size: 0.82rem;
}
.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #f0f0f0;
}
.divider span { color: #bbb; white-space: nowrap; }

/* ─── 第三方登录 ────────── */
.social-login {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 11px 8px;
  border: 1.5px solid #eee;
  border-radius: 10px;
  background: #fff;
  font-size: 0.83rem;
  color: #555;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}
.social-btn:hover {
  background: #fafafa;
  border-color: #ddd;
  transform: translateY(-1px);
}

/* ─── 注册链接 ─────────── */
.register-tip {
  text-align: center;
  font-size: 0.85rem;
  color: #999;
  margin-top: 24px;
}
.register-tip a {
  color: #111;
  font-weight: 600;
  text-decoration: none;
  border-bottom: 1px solid #d4af37;
  padding-bottom: 1px;
  transition: color 0.2s;
}
.register-tip a:hover { color: #d4af37; }

/* ─── 返回按钮 ──────────── */
.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  background: none;
  border: none;
  color: #ccc;
  font-size: 0.82rem;
  cursor: pointer;
  padding: 12px 0 0;
  transition: color 0.2s;
  font-family: inherit;
}
.back-btn:hover { color: #888; }
</style>
