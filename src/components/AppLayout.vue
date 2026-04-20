<template>
  <div class="app-shell">
    <nav class="top-navbar" :class="{ 'navbar-hidden': !isNavbarVisible }">
      <div class="nav-container">
        <div class="logo-area" @click="handleNavigation('/')">
          <el-icon class="logo-icon"><Headset /></el-icon>
          <span class="logo-brand">听 · 我说</span>
        </div>

        <div class="main-nav">
          <div :class="['nav-link', { active: route.path === '/' }]" @click="handleNavigation('/')">
            首页
          </div>
          <div
            :class="['nav-link', { active: route.path === '/hot' }]"
            @click="handleNavigation('/hot')"
          >
            全站热榜
          </div>
          <div
            :class="['nav-link', { active: route.path.startsWith('/consult') }]"
            @click="handleNavigation('/consult')"
          >
            咨询预约
          </div>
        </div>

        <div class="search-wrapper">
          <div class="search-capsule">
            <el-icon class="search-prefix"><Search /></el-icon>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索灵感、创作者..."
              class="inner-input"
              @keyup.enter="handleSearch"
            />
          </div>
        </div>

        <div class="action-area">
          <template v-if="isLogin">
            <!-- 创作中心下拉菜单（仅创作者可见） -->
            <el-dropdown
              v-if="isCreator"
              trigger="hover"
              popper-class="creator-dropdown-enhanced"
              placement="bottom-start"
            >
              <div class="creator-center-btn-enhanced">
                <el-icon><Collection /></el-icon>
                <span>创作中心</span>
                <el-icon class="arrow-icon"><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="creator-menu-enhanced">
                  <div class="creator-menu-header">内容管理</div>
                  <el-dropdown-item @click="handleNavigation('/creator/audio')">
                    <el-icon><Collection /></el-icon>我的稿件
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleNavigation('/creator/create')">
                    <el-icon><Edit /></el-icon>发布音频
                  </el-dropdown-item>
                  <div class="creator-menu-divider"></div>
                  <div class="creator-menu-header">预约管理</div>
                  <el-dropdown-item @click="handleNavigation('/creator/slots')">
                    <el-icon><Clock /></el-icon>时间槽管理
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleNavigation('/creator/consult')">
                    <el-icon><ChatDotRound /></el-icon>预约订单
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <div class="balance-hover" @click="handleNavigation('/wallet')">
              <div class="balance-wrapper">
                <el-icon><Wallet /></el-icon>
                <span class="balance-amount">{{ formattedBalance }}</span>
              </div>
              <div class="balance-tooltip">点击查看钱包详情</div>
            </div>

            <el-dropdown
              trigger="hover"
              popper-class="user-dropdown-enhanced"
              placement="bottom-end"
            >
              <div class="user-profile-trigger" @click="handleNavigation('/profile')">
                <div class="avatar-wrapper">
                  <img
                    v-if="userInfo?.avatar"
                    :src="userInfo.avatar"
                    class="avatar-circle"
                    @error="
                      (e) =>
                        (e.target.src =
                          'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
                    "
                  />
                  <div v-else class="avatar-circle placeholder">
                    <el-icon><User /></el-icon>
                  </div>
                </div>
                <div class="user-info-text">
                  <span class="nickname">{{
                    userInfo?.nickname || userInfo?.username || '用户'
                  }}</span>
                  <el-icon class="dropdown-arrow"><CaretBottom /></el-icon>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-menu-enhanced">
                  <div class="user-menu-header">个人中心</div>
                  <el-dropdown-item @click="handleNavigation('/user/consult')">
                    <el-icon><ChatDotRound /></el-icon>我的预约
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleNavigation('/favorite')">
                    <el-icon><Star /></el-icon>我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleNavigation('/like')">
                    <el-icon><Collection /></el-icon>我喜欢
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleNavigation('/history')">
                    <el-icon><Clock /></el-icon>播放历史
                  </el-dropdown-item>
                  <div class="user-menu-divider"></div>
                  <el-dropdown-item @click="handleLogout" class="logout-item">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <div v-else class="auth-box">
            <span class="login-link" @click="handleNavigation('/login')">登录</span>
            <el-button class="join-btn" @click="handleNavigation('/register')">加入</el-button>
          </div>
        </div>
      </div>
    </nav>

    <main class="main-viewport">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user/user'
import {
  Collection,
  User,
  Wallet,
  Search,
  CaretBottom,
  SwitchButton,
  Headset,
  Clock,
  Star,
  ChatDotRound,
  Edit,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLogin = computed(() => userStore.isLogin)
const userInfo = computed(() => userStore.userInfo)
const isCreator = computed(() => userInfo.value?.isCreator === true)

const formattedBalance = computed(() => {
  const balance = userInfo.value?.balance || 0
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2,
  }).format(balance)
})

const searchQuery = ref('')
const handleSearch = () => {
  const keyword = searchQuery.value.trim()
  if (!keyword) return
  router.push({ path: '/search', query: { q: keyword } })
}

const handleNavigation = (path) => router.push(path)
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const isNavbarVisible = ref(true)
let lastScrollTop = 0
let ticking = false
let scrollBasedVisible = true
let isHoveringTop = false

const updateScrollBasedVisibility = () => {
  const currentScroll = window.scrollY || document.documentElement.scrollTop
  const delta = currentScroll - lastScrollTop
  const minDelta = 8

  if (Math.abs(delta) < minDelta) {
    lastScrollTop = currentScroll
    return
  }

  if (currentScroll <= 10) {
    scrollBasedVisible = true
  } else if (delta > 0) {
    scrollBasedVisible = false
  } else if (delta < 0) {
    scrollBasedVisible = true
  }

  lastScrollTop = currentScroll

  if (!isHoveringTop) {
    isNavbarVisible.value = scrollBasedVisible
  }
}

const handleScroll = () => {
  if (!ticking) {
    requestAnimationFrame(() => {
      updateScrollBasedVisibility()
      ticking = false
    })
    ticking = true
  }
}

let hoverTimer = null
const handleMouseMove = (e) => {
  const isTop = e.clientY <= 50
  if (isTop && !isHoveringTop) {
    isHoveringTop = true
    isNavbarVisible.value = true
    if (hoverTimer) clearTimeout(hoverTimer)
  } else if (!isTop && isHoveringTop) {
    if (hoverTimer) clearTimeout(hoverTimer)
    hoverTimer = setTimeout(() => {
      isHoveringTop = false
      isNavbarVisible.value = scrollBasedVisible
    }, 100)
  }
}

// --- 定时刷新用户信息（头像临时链接）---
let refreshTimer = null
const REFRESH_INTERVAL = 20 * 60 * 1000 // 20分钟

const startRefreshTimer = () => {
  if (refreshTimer) clearInterval(refreshTimer)
  refreshTimer = setInterval(async () => {
    if (isLogin.value) {
      try {
        await userStore.refreshUserInfo()
      } catch (error) {
        // 如果失败（比如网络问题或 token 恰好在这期间过期），静默处理
        // token 过期后用户下次操作会触发 401 并登出
        console.debug('定时刷新用户信息失败')
      }
    }
  }, REFRESH_INTERVAL)
}

const handleVisibilityChange = () => {
  if (document.visibilityState === 'visible' && isLogin.value) {
    userStore.refreshUserInfo().catch(() => {})
  }
}
// --- 定时刷新结束 ---

onMounted(async () => {
  if (isLogin.value) {
    try {
      // 获取初始用户信息
      await userStore.refreshUserInfo()
    } catch (error) {
      console.error('Account sync failed')
    }
    startRefreshTimer()
    document.addEventListener('visibilitychange', handleVisibilityChange)
  }
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('mousemove', handleMouseMove)
  updateScrollBasedVisibility()
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('mousemove', handleMouseMove)
  if (hoverTimer) clearTimeout(hoverTimer)
})
</script>

<style>
/* 创作中心下拉菜单增强 */
.creator-dropdown-enhanced.el-dropdown__popper {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  margin-top: 12px !important;
}
.creator-menu-enhanced {
  background: #ffffff;
  border-radius: 24px;
  padding: 12px 8px;
  min-width: 180px;
  box-shadow: 0 20px 35px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e8e6df;
}
.creator-menu-header {
  font-size: 11px;
  font-weight: 800;
  color: #c4c2ba;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 8px 16px 4px;
}
.creator-menu-divider {
  height: 1px;
  background: #efeee8;
  margin: 8px 12px;
}
.creator-menu-enhanced .el-dropdown-menu__item {
  margin: 4px 8px;
  padding: 10px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  transition: all 0.2s;
}
.creator-menu-enhanced .el-dropdown-menu__item:hover {
  background: #fcfbf7;
  transform: translateX(4px);
}
.creator-menu-enhanced .el-dropdown-menu__item .el-icon {
  margin-right: 12px;
  font-size: 16px;
  color: #8e8c84;
}

/* 用户下拉菜单增强 */
.user-dropdown-enhanced.el-dropdown__popper {
  border: none !important;
  background: transparent !important;
  margin-top: 12px !important;
}
.user-menu-enhanced {
  background: #ffffff;
  border-radius: 24px;
  padding: 12px 8px;
  min-width: 200px;
  box-shadow: 0 20px 35px -12px rgba(0, 0, 0, 0.15);
  border: 1px solid #e8e6df;
}
.user-menu-header {
  font-size: 11px;
  font-weight: 800;
  color: #c4c2ba;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 8px 16px 4px;
}
.user-menu-divider {
  height: 1px;
  background: #efeee8;
  margin: 8px 12px;
}
.user-menu-enhanced .el-dropdown-menu__item {
  margin: 4px 8px;
  padding: 10px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  transition: all 0.2s;
}
.user-menu-enhanced .el-dropdown-menu__item:hover {
  background: #fcfbf7;
  transform: translateX(4px);
}
.user-menu-enhanced .el-dropdown-menu__item .el-icon {
  margin-right: 12px;
  font-size: 16px;
  color: #8e8c84;
}
.user-menu-enhanced .logout-item {
  color: #e5484d;
}
.user-menu-enhanced .logout-item .el-icon {
  color: #e5484d;
}

.creator-dropdown-enhanced .el-popper__arrow,
.user-dropdown-enhanced .el-popper__arrow {
  display: none;
}
</style>

<style scoped>
.app-shell {
  min-height: 100vh;
  background-color: #fcfbf7;
}
.top-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 72px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e8e6df;
  z-index: 1000;
  transition: transform 0.25s ease-in-out;
  transform: translateY(0);
}
.top-navbar.navbar-hidden {
  transform: translateY(-100%);
}
.main-viewport {
  min-height: 100vh;
  background-color: #fcfbf7;
  padding-top: 72px;
}
.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 30px;
}
.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 40px;
  transition: opacity 0.2s;
}
.logo-area:hover {
  opacity: 0.8;
}
.logo-brand {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
}
.main-nav {
  display: flex;
  gap: 28px;
}
.nav-link {
  font-size: 0.95rem;
  color: #8e8c84;
  cursor: pointer;
  transition: all 0.2s ease;
}
.nav-link:hover {
  color: #1a1a1a;
}
.nav-link.active {
  color: #1a1a1a;
  font-weight: 600;
}
.search-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 0 60px;
}
.search-capsule {
  width: 100%;
  max-width: 420px;
  height: 40px;
  background: #f7f6f2;
  border: 1px solid #efeee8;
  border-radius: 20px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.search-capsule:hover {
  border-color: #e8e6df;
  background: #f0efeb;
}
.search-capsule:focus-within {
  background: #ffffff;
  border-color: #dcdbd3;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  max-width: 440px;
}
.search-prefix {
  font-size: 16px;
  color: #b0aea3;
  margin-right: 10px;
  transition: color 0.2s;
}
.search-capsule:focus-within .search-prefix {
  color: #1a1a1a;
}
.inner-input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  font-size: 14px;
  color: #1a1a1a;
  height: 100%;
}
.inner-input::placeholder {
  color: #c4c2ba;
}
.action-area {
  display: flex;
  align-items: center;
  gap: 20px;
}
.creator-center-btn-enhanced {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px;
  background: #1a1a1a;
  border-radius: 48px;
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  letter-spacing: 0.5px;
}
.creator-center-btn-enhanced:hover {
  background: #2c2c2c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.creator-center-btn-enhanced .el-icon {
  font-size: 18px;
}
.creator-center-btn-enhanced .arrow-icon {
  font-size: 12px;
  margin-left: 4px;
}
.balance-hover {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 32px;
  transition: background 0.2s;
}
.balance-hover:hover {
  background: #f0efeb;
}
.balance-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1a1a1a;
}
.balance-amount {
  font-size: 0.9rem;
}
.balance-tooltip {
  position: absolute;
  bottom: -36px;
  left: 50%;
  transform: translateX(-50%);
  background: #1a1a1a;
  color: white;
  font-size: 0.7rem;
  padding: 4px 12px;
  border-radius: 20px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
  z-index: 1001;
}
.balance-hover:hover .balance-tooltip {
  opacity: 1;
}
.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.user-profile-trigger:hover {
  opacity: 0.85;
}
.avatar-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #f0efeb;
  transition: border-color 0.2s;
}
.user-profile-trigger:hover .avatar-circle {
  border-color: #dcdbd3;
}
.nickname {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1a1a1a;
}
.dropdown-arrow {
  font-size: 12px;
  color: #b0aea3;
  transition: transform 0.2s;
}
.user-profile-trigger:active .dropdown-arrow {
  transform: scale(0.9);
}
.auth-box {
  display: flex;
  gap: 20px;
  align-items: center;
}
.login-link {
  font-size: 14px;
  color: #8e8c84;
  cursor: pointer;
  transition: color 0.2s;
}
.login-link:hover {
  color: #1a1a1a;
}
.join-btn {
  background: #1a1a1a !important;
  color: #fff !important;
  border-radius: 20px;
  padding: 8px 20px;
  transition: transform 0.2s !important;
}
.join-btn:hover {
  transform: scale(1.02);
}
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.2s ease;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
@media (max-width: 768px) {
  .creator-center-btn-enhanced span {
    display: none;
  }
  .creator-center-btn-enhanced {
    padding: 8px 12px;
  }
  .balance-amount {
    display: none;
  }
}
</style>
