<template>
  <div class="creator-slots-page">
    <div class="page-header">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/consult' }">咨询预约</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/creator/${creatorId}` }">
          {{ creatorInfo?.nickname || '创作者主age' }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>选择时间</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="title-row">
        <h1 class="page-title">选择咨询时间</h1>
        <div class="creator-info-right" v-if="creatorInfo">
          <img :src="creatorInfo.avatar || defaultAvatar" class="creator-avatar-right" />
          <div class="creator-details-right">
            <span class="creator-name-right">{{ creatorInfo.nickname || '创作者' }}</span>
            <div class="creator-stats-right">
              <span
                ><el-icon><Headset /></el-icon> {{ creatorInfo.audioCount || 0 }}</span
              >
              <span
                ><el-icon><Star /></el-icon> {{ creatorInfo.fansCount || 0 }}</span
              >
              <span
                ><el-icon><ChatDotRound /></el-icon> {{ creatorInfo.consultCount || 0 }}</span
              >
            </div>
          </div>
        </div>
        <div v-else class="creator-placeholder-right">
          <el-skeleton :rows="1" animated style="width: 160px" />
        </div>
      </div>
    </div>

    <div class="slots-section">
      <div class="section-header">
        <h3 class="section-title">
          <el-icon><Clock /></el-icon>
          可预约时间
        </h3>
        <div class="date-range-picker">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateRangeChange"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </div>
        <span class="slot-count">共 {{ total }} 个时间段</span>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="error" class="error-state">
        <el-empty description="加载失败，请稍后重试">
          <el-button type="primary" @click="reload">重新加载</el-button>
        </el-empty>
      </div>

      <div v-else-if="slotList.length === 0" class="empty-state">
        <el-empty description="暂无可用时间槽">
          <el-button type="primary" @click="router.back()">返回创作者列表</el-button>
        </el-empty>
      </div>

      <div v-else class="slots-grid">
        <div
          v-for="slot in slotList"
          :key="slot.id"
          class="slot-card"
          @click="handleSlotSelect(slot)"
        >
          <div class="slot-date">{{ formatDate(slot.startTime) }}</div>
          <div class="slot-time">
            {{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}
          </div>
          <div class="slot-price">¥{{ slot.price }}</div>
          <div class="slot-duration">{{ getSlotDuration(slot) }}分钟</div>
        </div>
      </div>

      <div v-if="total > 0 && !loading && !error" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          :total="total"
          :page-size="10"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog
      v-model="showBookingDialog"
      title="确认预约"
      width="540px"
      :close-on-click-modal="false"
      class="booking-dialog"
      append-to-body
    >
      <div class="booking-dialog-content">
        <div class="dialog-creator-mini" v-if="creatorInfo">
          <img :src="creatorInfo.avatar || defaultAvatar" class="mini-avatar" />
          <span class="mini-name">{{ creatorInfo.nickname }}</span>
        </div>

        <div class="info-card">
          <div class="info-header">
            <el-icon><Calendar /></el-icon>
            <span>预约时段</span>
          </div>
          <div class="info-body">
            <div class="info-item">
              <span class="label">日期</span>
              <span class="value">{{
                selectedSlot ? formatDate(selectedSlot.startTime) : ''
              }}</span>
            </div>
            <div class="info-item">
              <span class="label">时间</span>
              <span class="value">{{
                selectedSlot
                  ? formatTime(selectedSlot.startTime) + ' — ' + formatTime(selectedSlot.endTime)
                  : ''
              }}</span>
            </div>
            <div class="info-item">
              <span class="label">时长</span>
              <span class="value">{{
                selectedSlot ? getSlotDuration(selectedSlot) + ' 分钟' : ''
              }}</span>
            </div>
            <div class="info-item highlight">
              <span class="label">费用</span>
              <span class="value price">¥{{ selectedSlot ? selectedSlot.price : '' }}</span>
            </div>
            <div class="info-item" v-if="selectedSlot?.address">
              <span class="label">联系地址</span>
              <span class="value">{{ selectedSlot.address }}</span>
            </div>
            <div class="info-item" v-else>
              <span class="label">联系地址</span>
              <span class="value muted">
                <el-icon><Lock /></el-icon> 预约成功后可见
              </span>
            </div>
          </div>
        </div>

        <div class="message-area">
          <div class="message-header">
            <el-icon><Edit /></el-icon>
            <span>咨询问题</span>
          </div>
          <el-input
            v-model="bookingMessage"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您想咨询的问题，以便创作者更好地为您解答..."
            maxlength="500"
            show-word-limit
            class="message-input"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelBooking" size="large" plain>取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleBookingSubmit" size="large">
            确认预约
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Clock,
  Timer,
  Location,
  Calendar,
  Headset,
  Star,
  ChatDotRound,
  Edit,
  Lock,
  InfoFilled,
} from '@element-plus/icons-vue'
import { creatorApi } from '@/api/user/creator'
import { consultApi } from '@/api/user/consult'

const route = useRoute()
const router = useRouter()

const creatorId = ref(Number(route.params.id))
const creatorInfo = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const loading = ref(false)
const error = ref(false)
const slotList = ref([])
const selectedSlot = ref(null)
const showBookingDialog = ref(false)
const bookingMessage = ref('')
const submitting = ref(false)

const dateRange = ref([])
const pagination = ref({ pageNum: 1 })
const total = ref(0)

const formatDateForBackend = (dateStr) => {
  if (!dateStr) return undefined
  const date = new Date(dateStr)
  return date.toISOString().slice(0, 19)
}

const fetchCreatorInfo = async () => {
  try {
    const res = await creatorApi.getCreatorDetail(creatorId.value)
    creatorInfo.value = res.data
  } catch (err) {
    console.error('获取创作者信息失败', err)
    creatorInfo.value = null
  }
}

const getSlotList = async () => {
  loading.value = true
  error.value = false
  try {
    const params = {
      creatorId: creatorId.value,
      pageNum: pagination.value.pageNum,
      pageSize: 10,
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = formatDateForBackend(dateRange.value[0])
      params.endTime = formatDateForBackend(dateRange.value[1])
    }
    const res = await creatorApi.getCreatorSlots(params)
    const data = res.data || res
    slotList.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (err) {
    console.error('获取时间槽失败:', err)
    error.value = true
    ElMessage.error('获取时间槽列表失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

const reload = () => {
  pagination.value.pageNum = 1
  getSlotList()
}

const handleDateRangeChange = () => {
  pagination.value.pageNum = 1
  getSlotList()
}

const handleSlotSelect = (slot) => {
  selectedSlot.value = slot
  showBookingDialog.value = true
  bookingMessage.value = ''
}

const handleBookingSubmit = async () => {
  if (!bookingMessage.value.trim()) {
    ElMessage.warning('请填写咨询问题')
    return
  }
  submitting.value = true
  try {
    await consultApi.saveConsult({
      slotId: selectedSlot.value.id,
      message: bookingMessage.value,
    })
    ElMessage.success('预约成功')
    showBookingDialog.value = false
    await getSlotList()
    selectedSlot.value = null
  } catch (error) {
    ElMessage.error('预约失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleCancelBooking = () => {
  showBookingDialog.value = false
  selectedSlot.value = null
  bookingMessage.value = ''
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getSlotList()
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[date.getDay()]
  return `${year}年${month}月${day}日 ${weekDay}`
}

const formatTime = (dateStr) => {
  const date = new Date(dateStr)
  return date.toTimeString().slice(0, 5)
}

const getSlotDuration = (slot) => {
  return Math.round((new Date(slot.endTime) - new Date(slot.startTime)) / 60000)
}

onMounted(() => {
  fetchCreatorInfo()
  getSlotList()
})
</script>

<style scoped>
.creator-slots-page {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 0 60px;
}
.page-header {
  max-width: 1400px;
  margin: 0 auto 40px;
  padding: 0 30px;
}
.breadcrumb {
  margin-bottom: 16px;
  font-size: 14px;
}
.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #8e8c84;
  font-weight: 400;
  font-size: 14px;
}
.breadcrumb :deep(.el-breadcrumb__inner a) {
  color: #8e8c84;
  text-decoration: none;
  font-size: 14px;
}
.breadcrumb :deep(.el-breadcrumb__inner a:hover) {
  color: #1a1a1a;
}
.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1a1a1a;
  font-weight: 600;
  font-size: 14px;
}
.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 32px;
}
.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0;
}
.creator-info-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.creator-avatar-right {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}
.creator-details-right {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}
.creator-name-right {
  font-weight: 600;
  font-size: 0.9rem;
  color: #1a1a1a;
}
.creator-stats-right {
  display: flex;
  gap: 12px;
  font-size: 0.7rem;
  color: #8e8c84;
}
.creator-stats-right span {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.creator-placeholder-right {
  width: 160px;
}
.slots-section {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}
.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  gap: 8px;
}
.date-range-picker :deep(.el-input__wrapper) {
  background: #ffffff;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  box-shadow: none;
  padding: 6px 16px;
}
.slot-count {
  font-size: 0.85rem;
  color: #b0aea3;
  margin-left: auto;
}
.loading-state,
.empty-state,
.error-state {
  padding: 60px 0;
  text-align: center;
}
.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.slot-card {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.slot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.04);
  border-color: #e2e0d7;
}
.slot-date {
  font-weight: 600;
  font-size: 1rem;
  color: #1a1a1a;
}
.slot-time {
  font-size: 0.9rem;
  color: #8e8c84;
  display: flex;
  align-items: center;
  gap: 4px;
}
.slot-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-top: 4px;
}
.slot-duration {
  font-size: 0.75rem;
  color: #b0aea3;
}
.pagination-wrapper {
  margin-top: 48px;
  display: flex;
  justify-content: center;
}
@media (max-width: 768px) {
  .slots-grid {
    grid-template-columns: 1fr;
  }
}

/* 对话框严格垂直居中 + 内容滚动 */
.booking-dialog :deep(.el-dialog) {
  border-radius: 32px;
  background: #ffffff;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  margin: 0 auto !important;
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  width: 540px;
  max-height: 85vh;
}
.booking-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
  max-height: calc(85vh - 120px);
  overflow-y: auto;
}
.booking-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 12px 24px;
  border-bottom: 1px solid #f0efeb;
  margin-right: 0;
  position: relative;
}
.booking-dialog :deep(.el-dialog__title) {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
}
.booking-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  transition: all 0.2s;
}
.booking-dialog :deep(.el-dialog__headerbtn:hover) {
  background: rgba(0, 0, 0, 0.1);
  transform: scale(1.05);
}
.booking-dialog :deep(.el-dialog__close) {
  font-size: 18px;
  font-weight: 600;
  color: #8e8c84;
}
.booking-dialog :deep(.el-dialog__close:hover) {
  color: #1a1a1a;
}
.booking-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 24px;
  border-top: 1px solid #f0efeb;
}

.dialog-creator-mini {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e8e6df;
}
.mini-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.mini-name {
  font-weight: 600;
  font-size: 1rem;
  color: #1a1a1a;
}

.info-card {
  background: #fcfbf7;
  border-radius: 20px;
  padding: 4px 0;
  margin-bottom: 24px;
  border: 1px solid #efeee8;
}
.info-header,
.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #1a1a1a;
  border-bottom: 1px solid #efeee8;
}
.info-header .el-icon,
.message-header .el-icon {
  font-size: 18px;
  color: #8e8c84;
}
.info-body {
  padding: 8px 20px 16px;
}
.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 0.9rem;
  border-bottom: 1px solid #f7f6f2;
}
.info-item:last-child {
  border-bottom: none;
}
.info-item .label {
  color: #b0aea3;
}
.info-item .value {
  color: #1a1a1a;
  font-weight: 500;
}
.info-item.highlight .value {
  font-size: 1.2rem;
  font-weight: 700;
  color: #1a1a1a;
}
.info-item .value.muted {
  color: #b0aea3;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.message-area {
  background: #fcfbf7;
  border-radius: 20px;
  border: 1px solid #efeee8;
}
.message-input :deep(.el-textarea__inner) {
  background: #ffffff;
  border: none;
  border-radius: 0 0 20px 20px;
  font-size: 14px;
  color: #1a1a1a;
  padding: 12px 20px;
}
.message-input :deep(.el-textarea__inner:focus) {
  box-shadow: none;
  border: none;
}
.info-tip {
  margin-top: 16px;
  padding: 8px 12px;
  background: #f7f6f2;
  border-radius: 12px;
  font-size: 12px;
  color: #8e8c84;
  display: flex;
  align-items: center;
  gap: 8px;
}
.info-tip .el-icon {
  font-size: 14px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}
.dialog-footer .el-button {
  border-radius: 40px;
  padding: 10px 28px;
  font-weight: 500;
}
.dialog-footer .el-button--primary {
  background: #1a1a1a;
  border: none;
}
.dialog-footer .el-button--primary:hover {
  background: #2c2c2c;
  transform: scale(0.98);
}
.dialog-footer .el-button--default {
  border-color: #e8e6df;
  color: #1a1a1a;
}
.dialog-footer .el-button--default:hover {
  background: #f7f6f2;
  border-color: #dcdbd3;
}
</style>
