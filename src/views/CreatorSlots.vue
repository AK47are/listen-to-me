<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { consultApi } from '@/api/consult'

const route = useRoute()
const router = useRouter()

const creatorId = ref(Number(route.params.id))
const creatorInfo = ref(null)
const loading = ref(false)
const slotList = ref([])
const selectedSlot = ref(null)
const showBookingDialog = ref(false)
const bookingMessage = ref('')
const submitting = ref(false)

const dateRange = ref([])
const pagination = ref({
  pageNum: 1,
  pageSize: 10
})
const total = ref(0)


const formatDateForBackend = (dateStr) => {
  if (!dateStr) return undefined
  const date = new Date(dateStr)
  // Format as YYYY-MM-DDTHH:mm:ss without timezone
  return date.toISOString().slice(0, 19)
}

const getSlotList = async () => {
  loading.value = true
  try {
    const res = await consultApi.getCreatorSlots({
      creatorId: creatorId.value,
      startTime: formatDateForBackend(dateRange.value[0]),
      endTime: formatDateForBackend(dateRange.value[1]),
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize
    })
    slotList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取时间槽列表失败')
  } finally {
    loading.value = false
  }
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
      message: bookingMessage.value
    })
    ElMessage.success('预约成功')
    showBookingDialog.value = false
    getSlotList() // Refresh slot list to remove the booked slot
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

const handleBack = () => {
  router.back()
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  getSlotList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
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
  getSlotList()
})
</script>

<template>
  <div class="creator-slots-page">
    <!-- 页面头部 --> 
    <div class="page-header">
      <div class="header-content">
        <el-button class="back-btn" link @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回创作者列表
        </el-button>
        <h1 class="page-title">选择咨询时间</h1>
      </div>
    </div>

    <!-- 时间槽选择区域 -->
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

      <div v-else-if="slotList.length === 0" class="empty-state">
        <el-empty description="暂无可用时间槽">
          <el-button type="primary" @click="handleBack">返回创作者列表</el-button>
        </el-empty>
      </div>

      <div v-else class="slots-list">
        <div
          v-for="slot in slotList"
          :key="slot.id"
          :class="['slot-item', { selected: selectedSlot?.id === slot.id }]"
          @click="handleSlotSelect(slot)"
        >
          <div class="slot-checkbox">
            <el-radio :model-value="selectedSlot?.id === slot.id" :label="true">
              <span class="radio-label"></span>
            </el-radio>
          </div>
          <div class="slot-info">
            <div class="slot-date">{{ formatDate(slot.startTime) }}</div>
            <div class="slot-time">
              <el-icon><Timer /></el-icon>
              {{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}
            </div>
            <div class="slot-address" v-if="slot.address">
              <el-icon><Location /></el-icon>
              {{ slot.address }}
            </div>
          </div>
          <div class="slot-price">
            <span class="price-label">咨询费用</span>
            <span class="price-value">¥{{ slot.price }}</span>
          </div>
          <div class="slot-duration">
            <el-tag size="small" type="info">
              {{ Math.round((new Date(slot.endTime) - new Date(slot.startTime)) / 60000) }} 分钟
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[1,10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div v-if="slotList.length > 0" class="action-bar">
      <div class="action-content">
        <div class="selected-info" v-if="selectedSlot">
          <span class="label">已选择：</span>
          <span class="value">{{ formatDate(selectedSlot.startTime) }} {{ formatTime(selectedSlot.startTime) }} - {{ formatTime(selectedSlot.endTime) }}</span>
        </div>
        <div class="action-buttons">
          <el-button size="large" @click="handleBack">返回</el-button>
          <el-button
            type="primary"
            size="large"
            :disabled="!selectedSlot"
            @click="showBookingDialog = true"
          >
            立即预约
            <el-icon><Calendar /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 预约对话框 -->
    <el-dialog
      v-model="showBookingDialog"
      title="确认预约"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="booking-dialog-content">
        <!-- 时间槽信息 -->
        <div class="slot-info-section">
          <h4 class="section-title">时间槽信息</h4>
          <div class="info-row">
            <span class="info-label">日期：</span>
            <span class="info-value">{{ selectedSlot ? formatDate(selectedSlot.startTime) : '' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">时间：</span>
            <span class="info-value">{{ selectedSlot ? formatTime(selectedSlot.startTime) + ' - ' + formatTime(selectedSlot.endTime) : '' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">时长：</span>
            <span class="info-value">{{ selectedSlot ? getSlotDuration(selectedSlot) + ' 分钟' : '' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">费用：</span>
            <span class="info-value price">¥{{ selectedSlot ? selectedSlot.price : '' }}</span>
          </div>
          <div class="info-row" v-if="selectedSlot?.address">
            <span class="info-label">地址：</span>
            <span class="info-value">{{ selectedSlot.address }}</span>
          </div>
        </div>

        <!-- 咨询问题 -->
        <div class="message-section">
          <h4 class="section-title">咨询问题</h4>
          <el-input
            v-model="bookingMessage"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您想咨询的问题，以便创作者更好地为您解答..."
            maxlength="500"
            show-word-limit
          />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelBooking">取消</el-button>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleBookingSubmit"
          >
            确认预约
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
@import '@/resource/css/creatorSlots.css';
</style>