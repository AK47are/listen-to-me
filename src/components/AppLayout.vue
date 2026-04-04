<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user/user'

const router = useRouter()
const userStore = useUserStore()

const isLogin = computed(() => userStore.isLogin)
const userInfo = computed(() => userStore.userInfo)
const isCreator = computed(() => userStore.userInfo.isCreator)
const isAdmin = computed(() => userStore.userInfo.username === 'admin')

const handleNavigation = (path) => {
  router.push(path)
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="app-layout">
    <div class="header">
      <div class="container">
        <div class="logo" @click="handleNavigation('/')">
          <el-icon><Headset /></el-icon>
          <span>听我</span>
        </div>
        <div class="nav">
          <div :class="['nav-item', { active: $route.path === '/' }]" @click="handleNavigation('/')">
            首页
          </div>
          <div :class="['nav-item', { active: $route.path.startsWith('/consult') }]" @click="handleNavigation('/consult')">
            咨询预约
          </div>
          <div :class="['nav-item', { active: $route.path === '/favorite' }]" @click="handleNavigation('/favorite')">
            收藏
          </div>
          <div :class="['nav-item', { active: $route.path === '/like' }]" @click="handleNavigation('/like')">
            喜欢
          </div>
          <div :class="['nav-item', { active: $route.path === '/history' }]" @click="handleNavigation('/history')">
            历史
          </div>
        </div>
        <div class="user-actions">
          <div v-if="isLogin" class="user-info">
            <el-dropdown>
              <div class="user-avatar">
                <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" />
                <el-icon v-else><User /></el-icon>
                <span>{{ userInfo.nickname || userInfo.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleNavigation('/profile')">
                    <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleNavigation('/wallet')">
                    <el-icon><Wallet /></el-icon> 我的钱包
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleNavigation('/user/consult')">
                    <el-icon><Calendar /></el-icon> 我的预约
                  </el-dropdown-item>
                  <template v-if="isCreator">
                    <el-dropdown-item @click="handleNavigation('/creator/audio')">
                      <el-icon><Collection /></el-icon> 我的作品
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleNavigation('/creator/create')">
                      <el-icon><Plus /></el-icon> 创建作品
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleNavigation('/creator/slots')">
                      <el-icon><Timer /></el-icon> 时间槽管理
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleNavigation('/creator/consult')">
                      <el-icon><Message /></el-icon> 预约订单管理
                    </el-dropdown-item>
                  </template>
                  <template v-if="isAdmin">
                    <el-dropdown-item @click="handleNavigation('/admin')">
                      <el-icon><Setting /></el-icon> 管理面板
                    </el-dropdown-item>
                  </template>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div v-else class="auth-buttons">
            <el-button @click="handleNavigation('/login')">登录</el-button>
            <el-button type="primary" @click="handleNavigation('/register')">注册</el-button>
          </div>
        </div>
      </div>
    </div>
    <div class="main">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
@import '@/resource/css/appLayout.css';
</style>
