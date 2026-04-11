<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  Calendar,
  Clock,
  Coin,
  Location,
  Loading,
  Search,
  CopyDocument,
} from '@element-plus/icons-vue'
import { slotApi } from '@/api/creator/slot'

const slotList = ref([])
const loading = ref(false)
const submitting = ref(false)
const batchExpanded = ref(true)

const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0,
})

const searchParams = reactive({
  startTime: '',
  endTime: '',
})

const batchForm = reactive({
  slots: [
    {
      startTime: '',
      endTime: '',
      price: 50,
      address: '',
    },
  ],
})

const getSlotList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      startTime: searchParams.startTime ? formatDateTime(searchParams.startTime) : '',
      endTime: searchParams.endTime ? formatDateTime(searchParams.endTime) : '',
    }
    const res = await slotApi.getSlotPage(params)
    slotList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取时间槽列表失败')
  } finally {
    loading.value = false
  }
}

const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const formatDisplayDate = (dateStr) => {
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${month}月${day}日 ${weekdays[date.getDay()]}`
}

const formatDisplayTime = (dateStr) => {
  const date = new Date(dateStr)
  return date.toTimeString().slice(0, 5)
}

const copyAddress = (address) => {
  if (!address) return
  navigator.clipboard
    ?.writeText(address)
    .then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

const handleBatchCreate = async () => {
  const hasEmpty = batchForm.slots.some(
    (slot) => !slot.startTime || !slot.endTime || !slot.price || !slot.address.trim(),
  )
  if (hasEmpty) {
    ElMessage.warning('请填写完整的时间槽信息')
    return
  }

  const formattedSlots = batchForm.slots.map((slot) => ({
    ...slot,
    startTime: formatDateTime(slot.startTime),
    endTime: formatDateTime(slot.endTime),
  }))

  submitting.value = true
  try {
    await slotApi.saveSlotBatch(formattedSlots)
    ElMessage.success('时间槽创建成功')
    batchForm.slots = [{ startTime: '', endTime: '', price: 50, address: '' }]
    getSlotList()
  } catch (error) {
    ElMessage.error('创建时间槽失败')
  } finally {
    submitting.value = false
  }
}

const addSlotRow = () => {
  batchForm.slots.push({
    startTime: '',
    endTime: '',
    price: 50,
    address: '',
  })
}

const removeSlotRow = (index) => {
  if (batchForm.slots.length > 1) {
    batchForm.slots.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个时间槽')
  }
}

const updateSlotStatus = async (slotId, status) => {
  try {
    await slotApi.updateSlot(slotId, status)
    ElMessage.success(status === 'AVAILABLE' ? '已重新开放' : '已临时关闭')
    getSlotList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteSlot = async (slotId) => {
  try {
    await ElMessageBox.confirm('删除后将无法恢复，确定要永久删除这个时间槽吗？', '删除确认', {
      confirmButtonText: '永久删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger',
    })
    await slotApi.deleteSlot(slotId)
    ElMessage.success('删除成功')
    getSlotList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  getSlotList()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  getSlotList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  getSlotList()
}

const getStatusConfig = (status) => {
  const config = {
    AVAILABLE: { text: '可预约', class: 'available' },
    BOOKED: { text: '已约满', class: 'booked' },
    EXPIRED: { text: '已过期', class: 'expired' },
    CANCELLED: { text: '已关闭', class: 'cancelled' },
  }
  return config[status] || { text: status, class: '' }
}

onMounted(() => {
  getSlotList()
})
</script>

<template>
  <div class="slot-manager">
    <div class="manager-header">
      <div class="title-section">
        <h1 class="page-title">时间槽管理</h1>
        <p class="page-subtitle">设置可预约时段，管理你的日程</p>
      </div>
      <div class="header-actions">
        <button class="toggle-batch-btn" @click="batchExpanded = !batchExpanded">
          <el-icon><Plus /></el-icon>
          <span>{{ batchExpanded ? '收起' : '快速排期' }}</span>
        </button>
      </div>
    </div>

    <transition name="slide-fade">
      <div v-show="batchExpanded" class="batch-section">
        <div class="batch-header">
          <h3>批量创建时间槽</h3>
          <span class="batch-hint">一次创建多个时段，提高排期效率</span>
        </div>

        <div class="batch-slots">
          <div v-for="(slot, index) in batchForm.slots" :key="index" class="batch-row">
            <div class="row-fields">
              <div class="field-group">
                <label
                  ><el-icon><Calendar /></el-icon> 开始</label
                >
                <el-date-picker
                  v-model="slot.startTime"
                  type="datetime"
                  placeholder="选择日期时间"
                  class="custom-datepicker"
                />
              </div>
              <div class="field-group">
                <label
                  ><el-icon><Clock /></el-icon> 结束</label
                >
                <el-date-picker
                  v-model="slot.endTime"
                  type="datetime"
                  placeholder="选择日期时间"
                  class="custom-datepicker"
                />
              </div>
              <div class="field-group price-group">
                <label
                  ><el-icon><Coin /></el-icon> 价格</label
                >
                <el-input-number
                  v-model="slot.price"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  class="custom-number"
                />
              </div>
              <div class="field-group address-group">
                <label
                  ><el-icon><Location /></el-icon> 地址</label
                >
                <el-input
                  v-model="slot.address"
                  placeholder="会议链接或地点"
                  class="custom-input"
                />
              </div>
            </div>
            <button
              class="remove-row-btn"
              :class="{ disabled: batchForm.slots.length <= 1 }"
              @click="removeSlotRow(index)"
            >
              <el-icon><Delete /></el-icon>
            </button>
          </div>
        </div>

        <div class="batch-footer">
          <button class="add-row-btn" @click="addSlotRow">
            <el-icon><Plus /></el-icon>
            添加时段
          </button>
          <button class="submit-batch-btn" :disabled="submitting" @click="handleBatchCreate">
            <el-icon v-if="!submitting"><Calendar /></el-icon>
            <el-icon v-else class="is-loading"><Loading /></el-icon>
            <span>{{ submitting ? '创建中' : '批量创建' }}</span>
          </button>
        </div>
      </div>
    </transition>

    <div class="list-section">
      <div class="list-header">
        <h3>已有时间槽</h3>
        <div class="filter-bar">
          <el-date-picker
            v-model="searchParams.startTime"
            type="datetime"
            placeholder="开始时间"
            class="filter-datepicker"
          />
          <span class="date-separator">—</span>
          <el-date-picker
            v-model="searchParams.endTime"
            type="datetime"
            placeholder="结束时间"
            class="filter-datepicker"
          />
          <button class="search-btn" @click="handleSearch">
            <el-icon><Search /></el-icon>
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading-grid">
        <div v-for="i in 6" :key="i" class="slot-card-skeleton">
          <el-skeleton animated>
            <template #template>
              <div class="skeleton-header">
                <el-skeleton-item variant="text" style="width: 40%" />
                <el-skeleton-item variant="text" style="width: 30%" />
              </div>
              <el-skeleton-item variant="text" style="width: 60%; margin: 12px 0" />
              <el-skeleton-item variant="text" style="width: 80%" />
              <div class="skeleton-footer">
                <el-skeleton-item variant="button" style="width: 45%" />
                <el-skeleton-item variant="button" style="width: 45%" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <div v-else-if="slotList.length === 0" class="empty-list">
        <div class="empty-icon">📅</div>
        <h3>暂无时间槽</h3>
        <p>点击上方「快速排期」创建你的第一个可预约时段</p>
      </div>

      <div v-else class="slot-grid">
        <div v-for="slot in slotList" :key="slot.id" class="slot-card">
          <div class="card-status" :class="getStatusConfig(slot.status).class"></div>
          <div class="card-content">
            <div class="card-header">
              <span class="slot-date">{{ formatDisplayDate(slot.startTime) }}</span>
              <span class="slot-status" :class="getStatusConfig(slot.status).class">
                {{ getStatusConfig(slot.status).text }}
              </span>
            </div>
            <div class="slot-time-range">
              <el-icon><Clock /></el-icon>
              <span
                >{{ formatDisplayTime(slot.startTime) }} —
                {{ formatDisplayTime(slot.endTime) }}</span
              >
            </div>
            <div class="slot-meta">
              <div class="meta-item">
                <el-icon><Coin /></el-icon>
                <span class="price">¥{{ slot.price }}</span>
              </div>
              <div class="meta-item address-row">
                <el-icon><Location /></el-icon>
                <el-tooltip :content="slot.address || '未设置'" placement="top" :show-after="500">
                  <span class="address-text">{{ slot.address || '未设置' }}</span>
                </el-tooltip>
                <button
                  v-if="slot.address"
                  class="copy-address-btn"
                  @click.stop="copyAddress(slot.address)"
                >
                  <el-icon><CopyDocument /></el-icon>
                </button>
              </div>
            </div>
          </div>
          <div class="card-actions">
            <template v-if="slot.status === 'AVAILABLE'">
              <el-tooltip content="临时关闭后用户将不可预约，后续可重新开放" placement="top">
                <button
                  class="action-btn close-btn"
                  @click="updateSlotStatus(slot.id, 'CANCELLED')"
                >
                  关闭
                </button>
              </el-tooltip>
            </template>
            <template v-else-if="slot.status === 'CANCELLED'">
              <el-tooltip content="重新开放后用户可再次预约" placement="top">
                <button class="action-btn open-btn" @click="updateSlotStatus(slot.id, 'AVAILABLE')">
                  开放
                </button>
              </el-tooltip>
            </template>
            <template v-else>
              <button class="action-btn disabled-btn" disabled>不可操作</button>
            </template>
            <el-tooltip content="永久删除此时间槽，不可恢复" placement="top">
              <button
                v-if="slot.status !== 'BOOKED'"
                class="action-btn delete-btn"
                @click="deleteSlot(slot.id)"
              >
                <el-icon><Delete /></el-icon>
              </button>
            </el-tooltip>
          </div>
        </div>
      </div>

      <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[12, 24, 48]"
          layout="sizes, prev, pager, next"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.slot-manager {
  min-height: 100vh;
  background: #fcfbf7;
  padding: 40px 30px 60px;
}

.manager-header {
  max-width: 1400px;
  margin: 0 auto 32px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.title-section {
  text-align: left;
}

.page-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 2.2rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 0.95rem;
  color: #8e8c84;
  letter-spacing: 0.5px;
  margin: 0;
}

.toggle-batch-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 12px 28px;
  font-weight: 600;
  font-size: 0.95rem;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.toggle-batch-btn:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

.batch-section {
  max-width: 1400px;
  margin: 0 auto 40px;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 28px;
  padding: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.batch-header {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 24px;
}

.batch-header h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.3rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.batch-hint {
  font-size: 0.85rem;
  color: #8e8c84;
}

.batch-slots {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.batch-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  background: #fcfbf7;
  border-radius: 20px;
  padding: 16px 20px;
  border: 1px solid #efeee8;
}

.row-fields {
  flex: 1;
  display: grid;
  grid-template-columns: 1.2fr 1.2fr 0.8fr 1.5fr;
  gap: 16px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-group label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #5a5953;
}

.custom-datepicker,
.custom-input {
  width: 100%;
}

.custom-datepicker :deep(.el-input__wrapper),
.custom-input :deep(.el-input__wrapper) {
  background: #ffffff;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  box-shadow: none;
  padding: 6px 16px;
}

.custom-number {
  width: 100%;
}

.custom-number :deep(.el-input__wrapper) {
  background: #ffffff;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  box-shadow: none;
  padding: 6px 16px;
}

.remove-row-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid #e8e6df;
  background: #ffffff;
  color: #c74242;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  margin-top: 28px;
}

.remove-row-btn:hover:not(.disabled) {
  background: #fdeded;
  border-color: #f5c2c2;
}

.remove-row-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.batch-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.add-row-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: 1px dashed #dcdbd3;
  border-radius: 40px;
  padding: 10px 20px;
  font-weight: 500;
  color: #1a1a1a;
  cursor: pointer;
  transition: all 0.2s;
}

.add-row-btn:hover {
  border-color: #1a1a1a;
  background: #f7f6f2;
}

.submit-batch-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #1a1a1a;
  border: none;
  border-radius: 40px;
  padding: 12px 32px;
  font-weight: 600;
  font-size: 0.95rem;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-batch-btn:hover:not(:disabled) {
  background: #2c2c2c;
  transform: translateY(-1px);
}

.submit-batch-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.list-section {
  max-width: 1400px;
  margin: 0 auto;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.list-header h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #ffffff;
  padding: 6px 6px 6px 16px;
  border-radius: 60px;
  border: 1px solid #efeee8;
}

.filter-datepicker {
  width: 170px;
}

.filter-datepicker :deep(.el-input__wrapper) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
}

.date-separator {
  color: #b0aea3;
  font-size: 0.9rem;
}

.search-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: #1a1a1a;
  color: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.search-btn:hover {
  background: #2c2c2c;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.slot-card-skeleton {
  background: #ffffff;
  border-radius: 24px;
  padding: 20px;
  border: 1px solid #efeee8;
}

.skeleton-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.skeleton-footer {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.empty-list {
  text-align: center;
  padding: 80px 20px;
  background: #ffffff;
  border-radius: 28px;
  border: 1px solid #efeee8;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-list h3 {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.3rem;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.empty-list p {
  color: #8e8c84;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.slot-card {
  position: relative;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.slot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.04);
  border-color: #dcdbd3;
}

.card-status {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.card-status.available {
  background: #67c23a;
}
.card-status.booked {
  background: #e6a23c;
}
.card-status.expired {
  background: #b0aea3;
}
.card-status.cancelled {
  background: #f56c6c;
}

.card-content {
  padding: 24px 20px 20px;
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.slot-date {
  font-weight: 700;
  font-size: 1.1rem;
  color: #1a1a1a;
}

.slot-status {
  padding: 2px 10px;
  border-radius: 30px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.slot-status.available {
  background: #e8f3e8;
  color: #2e6b2e;
}
.slot-status.booked {
  background: #fff3e0;
  color: #b85e00;
}
.slot-status.expired {
  background: #f0efe9;
  color: #5a5953;
}
.slot-status.cancelled {
  background: #fdeded;
  color: #c74242;
}

.slot-time-range {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #5a5953;
  font-size: 0.95rem;
}

.slot-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #7a7972;
}

.meta-item .price {
  font-weight: 700;
  color: #1a1a1a;
  font-size: 1.2rem;
}

.address-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.address-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.9rem;
  color: #7a7972;
  min-width: 0;
}

.copy-address-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #e8e6df;
  background: transparent;
  color: #5a5953;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  padding: 0;
}

.copy-address-btn:hover {
  background: #f0efe9;
  border-color: #1a1a1a;
  color: #1a1a1a;
}

.card-actions {
  display: flex;
  gap: 12px;
  padding: 16px 20px 20px;
  border-top: 1px solid #efeee8;
}

.action-btn {
  flex: 1;
  padding: 10px 0;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: transparent;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.close-btn:hover {
  background: #fff3e0;
  border-color: #f5c2c2;
  color: #b85e00;
}

.open-btn:hover {
  background: #e8f3e8;
  border-color: #b3d9b3;
  color: #2e6b2e;
}

.delete-btn {
  flex: 0.4;
}

.delete-btn:hover {
  background: #fdeded;
  border-color: #f5c2c2;
  color: #c74242;
}

.disabled-btn {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f0efe9;
}

.pagination-wrapper {
  margin-top: 48px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: #ffffff;
  --el-pagination-border-color: #efeee8;
  --el-pagination-border-radius: 40px;
  --el-pagination-button-bg-color: #ffffff;
}

:deep(.el-pagination .el-pager li) {
  border-radius: 12px;
  border: 1px solid #efeee8;
  background: #ffffff;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

@media (max-width: 768px) {
  .slot-manager {
    padding: 20px 16px;
  }
  .manager-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  .row-fields {
    grid-template-columns: 1fr;
  }
  .batch-row {
    flex-direction: column;
  }
  .remove-row-btn {
    align-self: flex-end;
    margin-top: 0;
  }
  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .filter-bar {
    width: 100%;
    justify-content: space-between;
  }
  .filter-datepicker {
    flex: 1;
  }
  .slot-grid {
    grid-template-columns: 1fr;
  }
  .address-text {
    white-space: normal;
    word-break: break-all;
  }
}
</style>
