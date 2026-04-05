<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user/user'
import {
  Collection,
  User,
  Setting,
  Bell,
  Wallet,
  Search,
  CaretBottom,
  SwitchButton,
  Headset,
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

onMounted(async () => {
  if (isLogin.value) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      console.error('Account sync failed')
    }
  }
})
</script>

<template>
  <div class="app-shell">
    <nav class="top-navbar">
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
            :class="['nav-link', { active: route.path.startsWith('/consult') }]"
            @click="handleNavigation('/consult')"
          >
            咨询预约
          </div>
          <div
            :class="['nav-link', { active: route.path === '/history' }]"
            @click="handleNavigation('/history')"
          >
            历史
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
            <div class="nav-asset-entry" @click="handleNavigation('/wallet')">
              <div class="balance-visual">
                <div class="icon-orbit">
                  <el-icon><Wallet /></el-icon>
                </div>
                <span class="amount-display">{{ formattedBalance }}</span>
              </div>
              <div class="ui-divider"></div>
            </div>

            <el-dropdown trigger="click" popper-class="nav-dropdown" placement="bottom-end">
              <div class="user-profile-trigger">
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
                <el-dropdown-menu class="custom-menu">
                  <div class="menu-group">
                    <div class="group-title">个人中心</div>
                    <el-dropdown-item @click="handleNavigation('/profile')">
                      <el-icon><User /></el-icon>我的主页
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleNavigation('/wallet')">
                      <el-icon><Wallet /></el-icon>钱包账户
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleNavigation('/notifications')">
                      <el-icon><Bell /></el-icon>消息通知
                    </el-dropdown-item>
                  </div>

                  <template v-if="isCreator">
                    <div class="menu-group border-top">
                      <div class="group-title">创作者工具</div>
                      <el-dropdown-item @click="handleNavigation('/creator/audio')">
                        <el-icon><Collection /></el-icon>内容管理
                      </el-dropdown-item>
                    </div>
                  </template>

                  <div class="menu-group border-top">
                    <div class="group-title">系统</div>
                    <div class="menu-item-wrapper">
                      <el-dropdown-item @click="handleNavigation('/settings')">
                        <el-icon><Setting /></el-icon>账号设置
                      </el-dropdown-item>
                    </div>
                  </div>

                  <div class="menu-group border-top logout-section">
                    <el-dropdown-item @click="handleLogout" class="logout-item">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </div>
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

<style>
.nav-dropdown.el-dropdown__popper {
  border: 1px solid rgba(232, 230, 223, 0.5) !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08) !important;
  border-radius: 16px !important;
  margin-top: 12px !important;
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
}
.nav-dropdown .custom-menu {
  padding: 6px !important;
  background-color: #ffffff;
  min-width: 180px;
}
.nav-dropdown .group-title {
  padding: 10px 14px 6px;
  font-size: 10px;
  font-weight: 800;
  color: #c4c2ba;
  text-transform: uppercase;
  letter-spacing: 1.2px;
}

.nav-dropdown .el-dropdown-menu__item {
  margin: 2px 4px !important;
  padding: 10px 12px !important;
  border-radius: 8px !important;
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  font-size: 13.5px !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
}

.nav-dropdown .el-dropdown-menu__item:hover {
  background-color: #fcfbf7 !important;
  color: #1a1a1a !important;
  transform: translateX(4px) !important;
}

.nav-dropdown .el-popper__arrow::before {
  display: none !important;
}
</style>

<style scoped>
.app-shell {
  min-height: 100vh;
  background-color: #fcfbf7;
}
.top-navbar {
  height: 72px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e8e6df;
  position: sticky;
  top: 0;
  z-index: 1000;
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
}

.nav-asset-entry {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s;
}
.nav-asset-entry:hover {
  transform: translateY(-1px);
}
.balance-visual {
  display: flex;
  align-items: center;
  gap: 12px;
}
.icon-orbit {
  width: 38px;
  height: 38px;
  background: #fff;
  border: 1px solid #e8e6df;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.nav-asset-entry:hover .icon-orbit {
  border-color: #1a1a1a;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.amount-display {
  font-size: 14px;
  font-weight: 700;
  color: #1a1a1a;
}
.ui-divider {
  width: 1px;
  height: 24px;
  background: #e8e6df;
  margin: 0 15px;
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
</style>
