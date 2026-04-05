<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElDialog } from 'element-plus'
import { userApi } from '@/api/user'
import { authApi } from '@/api/user'
import { useUserStore } from '@/stores/user/user'

const userStore = useUserStore()

const profileForm = reactive({
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  verifyCode: '',
  oldPassword: '',
  newPassword: '',
})
const profileInfo = reactive({
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  username: '',
  balance: '',
  frozenBalance: '',
  isCreator: 0,
  id: 0,
})

const verifyCodeForm = reactive({
  target: '',
  uuid: '',
  imageCode: '',
})


const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const isEditMode = ref(false)
const captchaUrl = ref('')

// 创作者申请相关
const showApplyDialog = ref(false)
const showStatusDialog = ref(false)
const applyForm = reactive({
  realName: '',
  phone: '',
  intro: '',
  attachment: ''
})
const applyStatus = ref(null)
const checkingStatus = ref(false)
const submittingApply = ref(false)

const getProfile = async () => {
  try {
    const res = await userApi.getProfile()
    Object.assign(profileInfo, res.data)
    Object.assign(profileForm, res.data)
  } catch (error) {
    console.error(error)
  }
}
const getCaptcha = async () => {
  try {
    const res = await authApi.getImageCaptcha()
    captchaUrl.value = res.data.img
    verifyCodeForm.uuid = res.data.uuid
  } catch (error) {
    console.error(error)
  }
}

const sendVerifyCode = async () => {
  if (!profileForm.phone && !profileForm.email) {
    ElMessage.warning('请输入手机号或邮箱')
    return
  }
  verifyCodeForm.target = profileForm.phone || profileForm.email

  sendingCode.value = true
  try {
    await authApi.sendVerifyCode(verifyCodeForm)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value === 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error(error)
  } finally {
    sendingCode.value = false
  }
}

const handleFileChange = async (file) => {
  if (file.status === 'ready') {
    try {
      loading.value = true
      const response = await userApi.uploadAvatar(file.raw)
      if (response && response.data) {
        profileForm.avatar = response.data
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.warning('头像上传失败')
      }
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }
}

const beforeAvatarUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt20M = file.size / 1024 / 1024 < 20

  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG/PNG 图片')
    return false
  }
  if (!isLt20M) {
    ElMessage.error('图片大小不能超过 20MB')
    return false
  }
  return true
}

const handleUpdateProfile = async () => {
  loading.value = true
  try {
    const data = {
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
    }
    data.phone = profileForm.phone
    data.email = profileForm.email

    if ((profileInfo.phone !== profileForm.phone) || (profileInfo.email !== profileForm.email)) {
      if (!profileForm.verifyCode) {
        ElMessage.warning('修改手机号或邮箱需要验证码')
        loading.value = false
        return
      }
      data.verifyCode = profileForm.verifyCode
    }

    if (profileForm.newPassword) {
      if (!profileForm.oldPassword) {
        ElMessage.warning('修改密码需要输入原密码')
        loading.value = false
        return
      }
      data.oldPassword = profileForm.oldPassword
      data.newPassword = profileForm.newPassword
    }

    await userApi.updateProfile(data)
    ElMessage.success('修改成功')
    await getProfile()
    profileForm.verifyCode = ''
    profileForm.oldPassword = ''
    profileForm.newPassword = ''
    isEditMode.value = false
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
}

// 打开申请对话框
const openApplyDialog = () => {
  showApplyDialog.value = true
}

// 关闭申请对话框
const closeApplyDialog = () => {
  showApplyDialog.value = false
  // 重置表单
  applyForm.realName = ''
  applyForm.phone = ''
  applyForm.intro = ''
  applyForm.attachment = ''
}

// 提交创作者申请
const submitCreatorApply = async () => {
  // 验证表单
  if (!applyForm.realName || !applyForm.phone || !applyForm.intro) {
    ElMessage.warning('请填写完整的申请信息')
    return
  }

  submittingApply.value = true
  try {
    await userApi.applyCreator(applyForm)
    ElMessage.success('申请提交成功')
    closeApplyDialog()
    // 刷新个人资料
    getProfile()
  } catch (error) {
    console.error(error)
  } finally {
    submittingApply.value = false
  }
}

// 查询申请状态
const checkApplyStatus = async () => {
  checkingStatus.value = true
  try {
    const res = await userApi.getCreatorApplyStatus()
    applyStatus.value = res.data
    showStatusDialog.value = true
  } catch (error) {
    console.error(error)
  } finally {
    checkingStatus.value = false
  }
}

onMounted(() => {
  getProfile()
  getCaptcha()
})
</script>

<template>
  <div class="profile-container">
    <!-- 个人资料展示 -->
    <div class="profile-box" v-if="!isEditMode">
      <div class="profile-header">
        <h2>个人资料</h2>
        <el-button type="primary" @click="toggleEditMode">
          <el-icon><Edit /></el-icon> 修改个人信息
        </el-button>
      </div>
      
      <div class="profile-info">
        <div class="avatar-section">
          <img :src="profileInfo.avatar" alt="头像" class="avatar" />
          <h3>{{ profileInfo.nickname }}</h3>
          <p class="username">{{ profileInfo.username }}</p>
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <span class="label">手机号</span>
            <span class="value">{{ profileInfo.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ profileInfo.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">账户余额</span>
            <span class="value balance">{{ profileInfo.balance }} 虚拟币</span>
          </div>
          <div class="info-item">
            <span class="label">冻结余额</span>
            <span class="value">{{ profileInfo.frozenBalance }} 虚拟币</span>
          </div>
          <div class="info-item">
          <span class="label">创作者身份</span>
          <span class="value">
            <el-tag :type="profileInfo.isCreator ? 'success' : 'info'">
              {{ profileInfo.isCreator ? '是' : '否' }}
            </el-tag>
            <el-button
              v-if="!profileInfo.isCreator"
              type="primary"
              size="small"
              style="margin-left: 10px"
              @click="openApplyDialog"
            >
              申请成为创作者
            </el-button>
            <el-button
              type="info"
              size="small"
              style="margin-left: 10px"
              @click="checkApplyStatus"
            >
              查看申请状态
            </el-button>
          </span>
        </div>
        </div>
      </div>
    </div>
    
    <!-- 编辑个人信息 -->
    <div class="profile-box" v-else>
      <div class="profile-header">
        <h2>修改个人信息</h2>
        <el-button @click="toggleEditMode">
          <el-icon><Close /></el-icon> 取消
        </el-button>
      </div>
      
      <el-form :model="profileForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload">
            <img :src="profileForm.avatar" alt="头像预览" class="avatar-preview" />
            <el-upload
              class="avatar-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              :before-upload="beforeAvatarUpload"
              style="margin-top: 10px"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon> 上传头像
              </el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="图形验证码">
          <div class="captcha-box">
            <el-input
              v-model="verifyCodeForm.imageCode"
              placeholder="请输入图形验证码"
              size="large"
              prefix-icon="Key"
            />
            <img :src="captchaUrl" alt="验证码" class="captcha-img" @click="getCaptcha" />
          </div>
        </el-form-item>
        <el-form-item label="验证码">
          <div class="input-with-btn">
            <el-input v-model="profileForm.verifyCode" placeholder="请输入验证码" />
            <el-button
              type="primary"
              :disabled="countdown > 0 || sendingCode"
              @click="sendVerifyCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="原密码">
          <el-input
            v-model="profileForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="profileForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleUpdateProfile">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 创作者申请对话框 -->
    <el-dialog
      v-model="showApplyDialog"
      title="申请成为创作者"
      width="500px"
    >
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="真实姓名" required>
          <el-input v-model="applyForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号码" required>
          <el-input v-model="applyForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="个人简介" required>
          <el-input
            v-model="applyForm.intro"
            type="textarea"
            :rows="4"
            placeholder="请简要介绍您的专业背景和擅长领域"
          />
        </el-form-item>
        <el-form-item label="附件链接">
          <el-input v-model="applyForm.attachment" placeholder="请输入作品集或个人网站链接（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeApplyDialog">取消</el-button>
          <el-button type="primary" :loading="submittingApply" @click="submitCreatorApply">
            提交申请
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 申请状态对话框 -->
    <el-dialog
      v-model="showStatusDialog"
      title="申请状态"
      width="400px"
    >
      <div v-if="checkingStatus" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      <div v-else class="status-content">
        <div class="status-item">
          <span class="label">状态：</span>
          <span class="value">
            <el-tag :type="applyStatus.status === 'PENDING' ? 'warning' : applyStatus.status === 'APPROVED' ? 'success' : 'danger'">
              {{ applyStatus.status === 'PENDING' ? '审核中' : applyStatus.status === 'APPROVED' ? '已通过' : '已拒绝' }}
            </el-tag>
          </span>
        </div>
        <div class="status-item" v-if="applyStatus.reason">
          <span class="label">原因：</span>
          <span class="value">{{ applyStatus.reason }}</span>
        </div>
        <div class="status-item">
          <span class="label">申请时间：</span>
          <span class="value">{{ applyStatus.applyTime }}</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showStatusDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
@import '@/resource/css/profile.css';

</style>