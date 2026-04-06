<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/common/auth'

const router = useRouter()
const activeMethod = ref('phone')
const loading = ref(false)
const countdown = ref(0)
const captchaUrl = ref('')

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  verifyCode: '',
})

const verifyCodeForm = reactive({ target: '', uuid: '', imageCode: '' })

const getCaptcha = async () => {
  try {
    const { data } = await authApi.getImageCaptcha()
    captchaUrl.value = data.img
    verifyCodeForm.uuid = data.uuid
  } catch (e) {
    console.error('Captcha Error')
  }
}

getCaptcha()

const sendVerifyCode = async () => {
  const target = activeMethod.value === 'phone' ? registerForm.phone : registerForm.email
  if (!target || !verifyCodeForm.imageCode) return ElMessage.warning('请检查输入')
  verifyCodeForm.target = target
  try {
    await authApi.sendVerifyCode(verifyCodeForm)
    ElMessage.success('发送成功')
    countdown.value = 60
    const timer = setInterval(() => {
      if (--countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    ElMessage.error('发送失败')
  }
}

const handleRegister = async () => {
  if (registerForm.password !== registerForm.confirmPassword) return ElMessage.warning('密码不匹配')
  loading.value = true
  try {
    await authApi.register(registerForm)
    router.push('/login')
  } catch (e) {
    getCaptcha()
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
          <h2 class="main-title">账号注册</h2>
        </header>

        <nav class="tab-nav">
          <span :class="{ active: activeMethod === 'phone' }" @click="activeMethod = 'phone'"
            >手机号注册</span
          >
          <span :class="{ active: activeMethod === 'email' }" @click="activeMethod = 'email'"
            >邮箱号注册</span
          >
        </nav>

        <el-form :model="registerForm" class="fast-form">
          <el-form-item class="f-item">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" />
          </el-form-item>

          <el-form-item v-show="activeMethod === 'phone'" class="f-item">
            <el-input v-model="registerForm.phone" placeholder="手机号码" size="large" />
          </el-form-item>
          <el-form-item v-show="activeMethod === 'email'" class="f-item">
            <el-input v-model="registerForm.email" placeholder="电子邮箱" size="large" />
          </el-form-item>

          <div class="f-row">
            <el-form-item class="f-item flex-1">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="设置密码"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item class="f-item flex-1">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                size="large"
                show-password
              />
            </el-form-item>
          </div>

          <div class="f-row align-bottom b-line">
            <el-form-item class="f-item flex-1 no-b">
              <el-input v-model="verifyCodeForm.imageCode" placeholder="图形验证码" size="large" />
            </el-form-item>
            <div class="captcha-box" @click="getCaptcha">
              <img v-if="captchaUrl" :src="captchaUrl" class="captcha-img" />
            </div>
          </div>

          <div class="f-row align-bottom b-line">
            <el-form-item class="f-item flex-1 no-b">
              <el-input v-model="registerForm.verifyCode" placeholder="六位动态码" size="large" />
            </el-form-item>
            <el-button class="quick-btn" :disabled="countdown > 0" @click="sendVerifyCode">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>

          <el-button type="primary" class="go-btn" :loading="loading" @click="handleRegister">
            注 册 账 号
          </el-button>
        </el-form>

        <div class="bottom-link">已有账号？<router-link to="/login">立即登录</router-link></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-root {
  display: flex;
  min-height: 100vh;
  background: #fff;
}

.visual-side {
  flex: 1.2;
  position: relative;
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
  margin-bottom: 45px;
}
.main-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.4rem;
  font-weight: 700;
  color: #1a1a1a;
}

/* Tab：增大字号至 16px */
.tab-nav {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 40px;
}
.tab-nav span {
  font-size: 1rem;
  color: #8e8c84;
  cursor: pointer;
  padding-bottom: 8px;
  border-bottom: 2px solid transparent;
  transition: 0.3s;
}
.tab-nav span.active {
  color: #1a1a1a;
  font-weight: 700;
  border-bottom-color: #1a1a1a;
}

.fast-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.f-item {
  margin-bottom: 0 !important;
}
.f-row {
  display: flex;
  gap: 20px;
  width: 100%;
}
.flex-1 {
  flex: 1;
}
.align-bottom {
  align-items: flex-end;
}
.b-line {
  border-bottom: 1px solid #e2e0d7;
  padding-bottom: 6px;
}
.no-b :deep(.el-input__wrapper) {
  border-bottom: none !important;
}

/* 输入框文字增大至 16px */
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

.captcha-box {
  width: 110px;
  height: 40px;
  cursor: pointer;
  display: flex;
  justify-content: flex-end;
}
.captcha-img {
  height: 100%;
  object-fit: contain;
  filter: grayscale(1) contrast(1.2);
}

.quick-btn {
  border: none !important;
  background: transparent !important;
  color: #1a1a1a !important;
  font-weight: 700;
  font-size: 0.95rem;
  padding: 0;
  height: 40px;
}
.go-btn {
  width: 100%;
  height: 56px;
  background: #1a1a1a !important;
  border: none !important;
  margin-top: 20px;
  font-weight: 700;
  font-size: 1.1rem;
  letter-spacing: 8px;
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
