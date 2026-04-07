<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">个人主页</h1>
      <p class="page-subtitle">管理你的个人信息</p>
    </div>

    <div class="profile-content">
      <div class="profile-card">
        <div class="card-header">
          <h2>个人资料</h2>
          <el-button class="edit-btn" @click="openEditDialog">
            <el-icon><Edit /></el-icon> 修改信息
          </el-button>
        </div>

        <div class="profile-body">
          <div class="avatar-section">
            <img :src="profileInfo.avatar" alt="头像" class="avatar" />
            <h3>{{ profileInfo.nickname }}</h3>
            <p class="username">@{{ profileInfo.username }}</p>
          </div>

          <div class="info-grid">
            <div class="info-item">
              <span class="label">手机号</span>
              <span class="value">{{ maskPhone(profileInfo.phone) || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ maskEmail(profileInfo.email) || '未设置' }}</span>
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
                <el-tag
                  :type="profileInfo.isCreator ? 'success' : 'info'"
                  size="small"
                  :class="{ 'clickable-tag': profileInfo.isCreator }"
                  @click="profileInfo.isCreator && checkApplyStatus()"
                >
                  {{ profileInfo.isCreator ? '已认证' : '普通用户' }}
                </el-tag>
                <el-button
                  v-if="!profileInfo.isCreator"
                  type="primary"
                  size="small"
                  plain
                  @click="openApplyDialog"
                >
                  申请成为创作者
                </el-button>
                <el-button
                  v-if="!profileInfo.isCreator"
                  type="info"
                  size="small"
                  plain
                  @click="checkApplyStatus"
                >
                  查看申请状态
                </el-button>
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="two-columns">
        <div class="info-card">
          <div class="card-header">
            <h3>最近播放</h3>
            <el-button text class="more-btn" @click="goToHistory">查看更多</el-button>
          </div>
          <div class="recent-list" v-if="recentHistory.length > 0">
            <div
              v-for="item in recentHistory"
              :key="item.id"
              class="recent-item"
              @click="goToAudio(item.id)"
            >
              <img :src="item.coverUrl" class="recent-cover" />
              <div class="recent-info">
                <div class="recent-title">{{ item.title }}</div>
                <div class="recent-creator">{{ item.creatorName }}</div>
              </div>
              <div class="recent-progress" v-if="item.progress">
                <el-progress
                  :percentage="item.progressPercent"
                  :show-text="false"
                  :stroke-width="3"
                />
              </div>
            </div>
          </div>
          <div v-else class="empty-tip">暂无播放记录</div>
        </div>

        <div class="info-card">
          <div class="card-header">
            <h3>我关注的创作者</h3>
            <el-button text class="more-btn" @click="openFollowDialog">查看更多</el-button>
          </div>
          <div class="follow-list" v-if="followList.length > 0">
            <div
              v-for="creator in followList"
              :key="creator.creatorId"
              class="follow-item"
              @click="goToCreatorProfile(creator.creatorId)"
            >
              <img :src="creator.avatar" class="follow-avatar" />
              <div class="follow-name">{{ creator.nickname }}</div>
            </div>
          </div>
          <div v-else class="empty-tip">暂未关注创作者</div>
        </div>
      </div>
    </div>

    <!-- 编辑个人信息对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="修改个人信息"
      width="560px"
      class="custom-dialog"
      destroy-on-close
    >
      <el-form :model="profileForm" label-width="100px" class="edit-form">
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
              class="uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              :before-upload="beforeAvatarUpload"
            >
              <el-button plain>
                <el-icon><Upload /></el-icon> 上传头像
              </el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="手机号">
          <div class="field-with-verify">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            <el-button
              v-if="profileForm.phone && profileForm.phone !== profileInfo.phone"
              class="verify-btn"
              :disabled="phoneCodeCountdown > 0 || sendingPhoneCode"
              @click="sendPhoneVerifyCode"
            >
              {{ phoneCodeCountdown > 0 ? `${phoneCodeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item
          v-if="profileForm.phone && profileForm.phone !== profileInfo.phone"
          label="手机验证码"
        >
          <el-input v-model="profileForm.phoneVerifyCode" placeholder="请输入手机验证码" />
        </el-form-item>

        <el-form-item label="邮箱">
          <div class="field-with-verify">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            <el-button
              v-if="profileForm.email && profileForm.email !== profileInfo.email"
              class="verify-btn"
              :disabled="emailCodeCountdown > 0 || sendingEmailCode"
              @click="sendEmailVerifyCode"
            >
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item
          v-if="profileForm.email && profileForm.email !== profileInfo.email"
          label="邮箱验证码"
        >
          <el-input v-model="profileForm.emailVerifyCode" placeholder="请输入邮箱验证码" />
        </el-form-item>

        <el-form-item label="图形验证码" v-if="needCaptcha">
          <div class="captcha-group">
            <el-input v-model="verifyCodeForm.imageCode" placeholder="请输入图形验证码" />
            <img :src="captchaUrl" alt="验证码" class="captcha-img" @click="getCaptcha" />
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
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="cancel-btn" @click="closeEditDialog">取消</el-button>
          <el-button class="confirm-btn" :loading="loading" @click="handleUpdateProfile"
            >保存修改</el-button
          >
        </div>
      </template>
    </el-dialog>

    <!-- 创作者申请对话框 -->
    <el-dialog v-model="showApplyDialog" title="申请成为创作者" width="500px" class="custom-dialog">
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
          <el-input
            v-model="applyForm.attachment"
            placeholder="请输入作品集或个人网站链接（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="cancel-btn" @click="closeApplyDialog">取消</el-button>
          <el-button class="confirm-btn" :loading="submittingApply" @click="submitCreatorApply">
            提交申请
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 申请状态对话框 -->
    <el-dialog v-model="showStatusDialog" title="申请状态" width="400px" class="custom-dialog">
      <div v-if="checkingStatus" class="status-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else class="status-content">
        <div class="status-row">
          <span class="status-label">状态</span>
          <span class="status-value">
            <el-tag
              :type="
                applyStatus.status === 'PENDING'
                  ? 'warning'
                  : applyStatus.status === 'APPROVED'
                    ? 'success'
                    : 'danger'
              "
              size="small"
            >
              {{
                applyStatus.status === 'PENDING'
                  ? '审核中'
                  : applyStatus.status === 'APPROVED'
                    ? '已通过'
                    : '已拒绝'
              }}
            </el-tag>
          </span>
        </div>
        <div class="status-row" v-if="applyStatus.reason">
          <span class="status-label">拒绝原因</span>
          <span class="status-value reject-reason">{{ applyStatus.reason }}</span>
        </div>
        <div class="status-row">
          <span class="status-label">申请时间</span>
          <span class="status-value">{{ applyStatus.applyTime }}</span>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="confirm-btn" @click="showStatusDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 我的关注弹窗 -->
    <el-dialog v-model="showFollowDialog" title="我的关注" width="500px" class="follow-dialog">
      <div v-if="followLoading" class="status-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="followListFull.length === 0" class="empty-tip">暂未关注创作者</div>
      <div v-else class="follow-dialog-list">
        <div v-for="creator in followListFull" :key="creator.creatorId" class="follow-dialog-item">
          <div class="follow-dialog-info" @click="goToCreatorProfile(creator.creatorId)">
            <img :src="creator.avatar" class="follow-dialog-avatar" />
            <div class="follow-dialog-name">{{ creator.nickname }}</div>
          </div>
          <el-popconfirm
            title="确定要取消关注吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="handleUnfollow(creator.creatorId)"
          >
            <template #reference>
              <el-button size="small" plain class="unfollow-btn">取消关注</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { authApi } from '@/api/common/auth'
import { applyApi } from '@/api/user/apply'
import { profileApi } from '@/api/user/profile'
import { historyApi } from '@/api/user/history'
import { followApi } from '@/api/user/follow'
import { ElMessage } from 'element-plus'
import { Edit, Upload, Loading } from '@element-plus/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const profileForm = reactive({
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  phoneVerifyCode: '',
  emailVerifyCode: '',
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
const sendingPhoneCode = ref(false)
const sendingEmailCode = ref(false)
const phoneCodeCountdown = ref(0)
const emailCodeCountdown = ref(0)
const captchaUrl = ref('')

const showEditDialog = ref(false)
const showApplyDialog = ref(false)
const showStatusDialog = ref(false)
const showFollowDialog = ref(false)
const applyForm = reactive({
  realName: '',
  phone: '',
  intro: '',
  attachment: '',
})
const applyStatus = ref(null)
const checkingStatus = ref(false)
const submittingApply = ref(false)

const recentHistory = ref([])
const followList = ref([])
const followListFull = ref([])
const followLoading = ref(false)

const needCaptcha = computed(() => {
  return (
    profileForm.phone !== profileInfo.phone ||
    profileForm.email !== profileInfo.email ||
    profileForm.newPassword
  )
})

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const maskEmail = (email) => {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (name.length <= 2) return email
  return name.slice(0, 2) + '***@' + domain
}

const getProfile = async () => {
  try {
    const res = await profileApi.getProfile()
    Object.assign(profileInfo, res.data)
    Object.assign(profileForm, res.data)
    profileForm.phoneVerifyCode = ''
    profileForm.emailVerifyCode = ''
  } catch (error) {
    console.error(error)
  }
}

const getRecentHistory = async () => {
  try {
    const res = await historyApi.getHistoryPage({
      pageNum: 1,
      pageSize: 5,
    })
    const records = res.data.records || []
    recentHistory.value = records.map((item) => ({
      ...item,
      progressPercent: item.progress ? Math.round((item.progress / item.duration) * 100) : 0,
    }))
  } catch (error) {
    console.error('获取最近播放失败', error)
  }
}

const getFollowList = async () => {
  try {
    const res = await followApi.getFollowPage({
      pageNum: 1,
      pageSize: 6,
    })
    followList.value = res.data.records || []
  } catch (error) {
    console.error('获取关注列表失败', error)
  }
}

const getFullFollowList = async () => {
  followLoading.value = true
  try {
    const res = await followApi.getFollowPage({
      pageNum: 1,
      pageSize: 100,
    })
    followListFull.value = res.data.records || []
  } catch (error) {
    console.error('获取关注列表失败', error)
    ElMessage.error('获取关注列表失败')
  } finally {
    followLoading.value = false
  }
}

const openEditDialog = () => {
  Object.assign(profileForm, profileInfo)
  profileForm.phoneVerifyCode = ''
  profileForm.emailVerifyCode = ''
  showEditDialog.value = true
  getCaptcha()
}

const closeEditDialog = () => {
  showEditDialog.value = false
  phoneCodeCountdown.value = 0
  emailCodeCountdown.value = 0
}

const openFollowDialog = async () => {
  await getFullFollowList()
  showFollowDialog.value = true
}

const handleUnfollow = async (creatorId) => {
  try {
    await followApi.unfollowCreator(creatorId)
    ElMessage.success('已取消关注')
    await getFullFollowList()
    await getFollowList()
  } catch (error) {
    console.error('取消关注失败', error)
    ElMessage.error('取消关注失败')
  }
}

const goToHistory = () => router.push('/history')
const goToAudio = (audioId) => router.push(`/audio/${audioId}`)
const goToCreatorProfile = (creatorId) => router.push(`/creator/${creatorId}`)

const getCaptcha = async () => {
  try {
    const res = await authApi.getImageCaptcha()
    captchaUrl.value = res.data.img
    verifyCodeForm.uuid = res.data.uuid
  } catch (error) {
    console.error(error)
  }
}

const sendPhoneVerifyCode = async () => {
  if (!profileForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!verifyCodeForm.imageCode) {
    ElMessage.warning('请先输入图形验证码')
    return
  }

  sendingPhoneCode.value = true
  try {
    verifyCodeForm.target = profileForm.phone
    await authApi.sendVerifyCode(verifyCodeForm)
    ElMessage.success('验证码已发送')
    phoneCodeCountdown.value = 60
    const timer = setInterval(() => {
      phoneCodeCountdown.value--
      if (phoneCodeCountdown.value === 0) clearInterval(timer)
    }, 1000)
    getCaptcha()
    verifyCodeForm.imageCode = ''
  } catch (error) {
    console.error(error)
    ElMessage.error('发送失败，请重试')
    getCaptcha()
  } finally {
    sendingPhoneCode.value = false
  }
}

const sendEmailVerifyCode = async () => {
  if (!profileForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  if (!verifyCodeForm.imageCode) {
    ElMessage.warning('请先输入图形验证码')
    return
  }

  sendingEmailCode.value = true
  try {
    verifyCodeForm.target = profileForm.email
    await authApi.sendVerifyCode(verifyCodeForm)
    ElMessage.success('验证码已发送')
    emailCodeCountdown.value = 60
    const timer = setInterval(() => {
      emailCodeCountdown.value--
      if (emailCodeCountdown.value === 0) clearInterval(timer)
    }, 1000)
    getCaptcha()
    verifyCodeForm.imageCode = ''
  } catch (error) {
    console.error(error)
    ElMessage.error('发送失败，请重试')
    getCaptcha()
  } finally {
    sendingEmailCode.value = false
  }
}

const handleFileChange = async (file) => {
  if (file.status === 'ready') {
    try {
      loading.value = true
      const response = await profileApi.uploadAvatar(file.raw)
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

    if (profileForm.phone !== profileInfo.phone) {
      if (!profileForm.phoneVerifyCode) {
        ElMessage.warning('修改手机号需要输入验证码')
        loading.value = false
        return
      }
      data.phone = profileForm.phone
      data.verifyCode = profileForm.phoneVerifyCode
    }

    if (profileForm.email !== profileInfo.email) {
      if (!profileForm.emailVerifyCode) {
        ElMessage.warning('修改邮箱需要输入验证码')
        loading.value = false
        return
      }
      data.email = profileForm.email
      data.verifyCode = profileForm.emailVerifyCode
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

    await profileApi.updateProfile(data)
    ElMessage.success('修改成功')
    await getProfile()
    closeEditDialog()
  } catch (error) {
    console.error(error)
    ElMessage.error('修改失败，请重试')
  } finally {
    loading.value = false
  }
}

const openApplyDialog = () => {
  showApplyDialog.value = true
}

const closeApplyDialog = () => {
  showApplyDialog.value = false
  applyForm.realName = ''
  applyForm.phone = ''
  applyForm.intro = ''
  applyForm.attachment = ''
}

const submitCreatorApply = async () => {
  if (!applyForm.realName || !applyForm.phone || !applyForm.intro) {
    ElMessage.warning('请填写完整的申请信息')
    return
  }

  submittingApply.value = true
  try {
    await applyApi.applyCreator(applyForm)
    ElMessage.success('申请提交成功')
    closeApplyDialog()
    getProfile()
  } catch (error) {
    console.error(error)
  } finally {
    submittingApply.value = false
  }
}

const checkApplyStatus = async () => {
  checkingStatus.value = true
  try {
    const res = await applyApi.getApplyStatus()
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
  getRecentHistory()
  getFollowList()
})
</script>

<style scoped>
@import '@/resource/css/profile.css';
</style>
