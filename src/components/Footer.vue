<template>
  <footer class="site-footer">
    <div class="container-custom footer-inner">
      <div class="footer-brand">
        <!-- Logo -->
        <div class="footer-logo">
          <svg width="28" height="28" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="24" r="7" fill="#111"/>
            <circle cx="36" cy="12" r="5" fill="#d4af37"/>
            <circle cx="36" cy="36" r="4" fill="#111" opacity="0.4"/>
            <line x1="12" y1="24" x2="36" y2="12" stroke="#d4af37" stroke-width="1.5" stroke-dasharray="3 2"/>
            <line x1="12" y1="24" x2="36" y2="36" stroke="#333" stroke-width="1.5" stroke-dasharray="3 2"/>
            <line x1="36" y1="12" x2="36" y2="36" stroke="#d4af37" stroke-width="1" stroke-dasharray="2 2" opacity="0.5"/>
          </svg>
          <span>邻聚</span>
        </div>
        <p class="footer-slogan">连接你身边的兴趣与人</p>
      </div>

      <div class="footer-links">
        <div class="footer-col">
          <h5>产品</h5>
          <a href="#">发现活动</a>
          <a href="#">发布活动</a>
          <a href="#">兴趣分类</a>
        </div>
        <div class="footer-col">
          <h5>支持</h5>
          <a href="#">帮助中心</a>
          <a href="#">隐私政策</a>
          <a href="#">使用条款</a>
        </div>
        <div class="footer-col">
          <h5>联系</h5>
          <a href="mailto:support@linju.app">support@linju.app</a>
        </div>
      </div>
    </div>

    <div class="footer-bottom container-custom">
      <span>© 2026 邻聚. 保留所有权利.</span>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
  margin-top: auto;
}

.footer-inner {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 48px;
  padding-top: 48px;
  padding-bottom: 40px;
  flex-wrap: wrap;
}

.footer-brand { max-width: 200px; }

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.footer-logo span {
  font-size: 1.1rem;
  font-weight: 800;
  color: #111;
  letter-spacing: -0.03em;
}

.footer-slogan {
  font-size: 0.83rem;
  color: #aaa;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  gap: 60px;
  flex-wrap: wrap;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.footer-col h5 {
  font-size: 0.78rem;
  font-weight: 700;
  color: #333;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 4px;
}
.footer-col a {
  font-size: 0.85rem;
  color: #999;
  text-decoration: none;
  transition: color 0.2s;
}
.footer-col a:hover { color: #d4af37; }

.footer-bottom {
  border-top: 1px solid #f0f0f0;
  padding-top: 20px;
  padding-bottom: 20px;
  font-size: 0.8rem;
  color: #ccc;
}
</style>
