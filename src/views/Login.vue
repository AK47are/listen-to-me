<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/user'
import { useUserStore } from '@/stores/user/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: '',
})

const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await authApi.login(loginForm)
    // 适配后端返回结构
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.userInfo)
    ElMessage.success('欢迎回来')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-root">
    <div class="visual-side">
      <div class="glass-overlay"></div>
      <div class="content-box">
        <h1 class="logo-text">听 · 我说</h1>
        <p class="tagline">每一个专业的见解，都值得被更深地倾听。</p>
      </div>
    </div>

    <div class="action-side">
      <div class="card-inner">
        <header class="card-head">
          <h2 class="main-title">欢迎登录</h2>
        </header>

        <el-form :model="loginForm" class="fast-form">
          <el-form-item class="f-item">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              spellcheck="false"
            />
          </el-form-item>

          <el-form-item class="f-item">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
            />
          </el-form-item>

          <el-button type="primary" class="go-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form>

        <div class="bottom-link">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 复用高性能响应式布局结构 */
.page-root {
  display: flex;
  min-height: 100vh;
  background: #fff;
  contain: layout;
}

.visual-side {
  flex: 1.2;
  position: relative;
  overflow: hidden;
  background: #1a1a1a url('https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=1000')
    center/cover;
}
.glass-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
}
.content-box {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 80px;
  color: #fff;
}
.logo-text {
  font-family: 'Noto Serif SC', serif;
  font-size: 4rem;
  margin: 0;
  letter-spacing: 4px;
}
.tagline {
  font-size: 1.2rem;
  opacity: 0.7;
  margin-top: 20px;
  letter-spacing: 2px;
}

.action-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fcfbf7;
}
.card-inner {
  width: 100%;
  max-width: 440px;
  padding: 40px;
}

.card-head {
  text-align: center;
  margin-bottom: 50px;
}
.main-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.4rem;
  font-weight: 700;
  color: #1a1a1a;
}

.fast-form {
  display: flex;
  flex-direction: column;
  gap: 28px;
}
.f-item {
  margin-bottom: 0 !important;
}

/* 线框化输入框及字号提档至 16px */
:deep(.el-input) {
  --el-input-font-size: 16px;
}
:deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border-bottom: 1px solid #e2e0d7 !important;
  border-radius: 0 !important;
  padding: 0 !important;
}
:deep(.el-input__inner::placeholder) {
  color: #b0aea3;
}

.go-btn {
  width: 100%;
  height: 56px;
  background: #1a1a1a !important;
  border: none !important;
  margin-top: 20px;
  font-weight: 700;
  font-size: 1.1rem;
  letter-spacing: 12px;
}

.bottom-link {
  text-align: center;
  margin-top: 35px;
  font-size: 1rem;
  color: #a19f96;
}
.bottom-link a {
  color: #1a1a1a;
  font-weight: 700;
  text-decoration: none;
  border-bottom: 1px solid #1a1a1a;
  margin-left: 8px;
}
</style>
