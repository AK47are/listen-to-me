<template>
  <div class="user-consult">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Collection /></el-icon>
        我的预约
      </h2>
      <p class="page-subtitle">查看和管理您的咨询预约记录</p>
    </div>

    <!-- 我的预约列表 -->
    <div class="my-consults-section">
      <div class="section-header">
        <h3>
          <el-icon><Collection /></el-icon>
          我的预约记录
        </h3>
        <div class="filter-bar">
          <el-select
            v-model="searchParams.status"
            placeholder="全部状态"
            clearable
            @change="getMyConsultOrders"
            style="min-width: 120px"
          >
            <el-option label="全部状态" value="" />
            <el-option label="待确认" value="PENDING_CONFIRM" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退款中" value="REFUND_PENDING" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
          <el-button type="primary" @click="getMyConsultOrders" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <!-- 预约列表 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else-if="consultOrders.length === 0" class="empty-state">
        <el-empty description="暂无预约记录" />
      </div>
      <div v-else class="consult-list">
        <el-card v-for="order in consultOrders" :key="order.id" class="consult-card" shadow="hover">
          <div class="consult-card-content">
            <div class="consult-info">
              <div class="time-badge">
                <el-icon><Clock /></el-icon>
                <span>{{ order.date }} {{ order.startTime }} - {{ order.endTime }}</span>
              </div>
              <div class="creator-row">
                <el-avatar :size="32" :src="order.creatorAvatar" />
                <span class="creator-name">{{ order.creatorName }}</span>
              </div>
              <div class="message-preview" v-if="order.message">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ order.message }}</span>
              </div>
            </div>
            <div class="consult-meta">
              <div class="price-tag">
                <el-icon><Coin /></el-icon>
                <span>{{ order.price }} 币</span>
              </div>
              <el-tag :type="getStatusType(order.status)" effect="dark">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>
          <div class="consult-card-footer">
            <div class="address-info" v-if="order.address">
              <el-icon><Location /></el-icon>
              <span>预约地址：{{ order.address }}</span>
            </div>
            <div class="actions">
              <el-button
                v-if="order.status === 'PENDING_CONFIRM'"
                type="danger"
                size="small"
                @click="cancelConsult(order.id)"
              >
                取消预约
              </el-button>
              <el-button
                v-if="order.status === 'CONFIRMED' || order.status === 'COMPLETED'"
                type="warning"
                size="small"
                @click="showRefundDialog(order.id)"
              >
                申请退款
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[1, 5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleConsultSizeChange"
          @current-change="handleConsultPageChange"
        />
      </div>
    </div>

    <!-- 退款申请弹窗 -->
    <el-dialog v-model="showRefundModal" title="申请退款" width="500px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="退款原因" required>
          <el-input
            v-model="refundReason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明退款原因..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRefundModal = false">取消</el-button>
        <el-button
          type="primary"
          @click="applyRefund"
          :loading="submitting"
          :disabled="!refundReason.trim()"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Collection, Refresh, ChatDotRound, Clock, Coin, Location } from '@element-plus/icons-vue'
import { consultApi } from '@/api/user/consult'

const consultOrders = ref([])
const total = ref(0)
const searchParams = ref({
  status: '',
})

const currentPage = ref(1)
const pageSize = ref(8)

const showRefundModal = ref(false)
const currentOrderId = ref(null)
const refundReason = ref('')

const loading = ref(false)
const submitting = ref(false)

const getMyConsultOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: searchParams.value.status,
    }
    const response = await consultApi.getMyConsultPage(params)
    consultOrders.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('获取预约订单列表失败:', error)
    ElMessage.error('获取预约订单列表失败')
  } finally {
    loading.value = false
  }
}

const cancelConsult = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？取消后将全额退款。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await consultApi.cancelConsult(id)
    ElMessage.success('预约取消成功')
    getMyConsultOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败')
    }
  }
}

const showRefundDialog = (id) => {
  currentOrderId.value = id
  refundReason.value = ''
  showRefundModal.value = true
}

const applyRefund = async () => {
  if (!refundReason.value.trim()) {
    ElMessage.warning('请输入退款原因')
    return
  }

  submitting.value = true
  try {
    await consultApi.applyRefund(currentOrderId.value, { reason: refundReason.value })
    ElMessage.success('退款申请提交成功')
    showRefundModal.value = false
    getMyConsultOrders()
  } catch (error) {
    console.error('申请退款失败:', error)
    ElMessage.error('申请退款失败')
  } finally {
    submitting.value = false
  }
}

const handleConsultSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getMyConsultOrders()
}

const handleConsultPageChange = (page) => {
  currentPage.value = page
  getMyConsultOrders()
}

const getStatusType = (status) => {
  const types = {
    PENDING_CONFIRM: 'warning',
    CONFIRMED: 'success',
    COMPLETED: 'info',
    CANCELLED: 'danger',
    REFUND_PENDING: 'warning',
    REFUNDED: 'info',
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    PENDING_CONFIRM: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUND_PENDING: '退款中',
    REFUNDED: '已退款',
  }
  return texts[status] || status
}

onMounted(() => {
  getMyConsultOrders()
})
</script>

<style scoped>
@import '@/resource/css/userConsult.css';
</style>
