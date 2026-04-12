<template>
  <div class="refund-audit">
    <div class="audit-header">
      <h3 class="audit-title">退款审核</h3>
      <span class="audit-count">共 {{ total }} 条待处理申请</span>
    </div>

    <div class="table-wrapper">
      <table class="audit-table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>订单ID</th>
            <th>预约时间</th>
            <th>金额</th>
            <th>申请用户</th>
            <th>创作者</th>
            <th>退款原因</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="apply in records" :key="apply.id">
            <td class="id-cell">{{ apply.id }}</td>
            <td>{{ apply.orderId }}</td>
            <td>
              <div>{{ formatDate(apply.startTime) }}</div>
              <div class="time-range">
                {{ formatTime(apply.startTime) }} — {{ formatTime(apply.endTime) }}
              </div>
            </td>
            <td class="price-cell">¥{{ apply.price }}</td>
            <td>
              <div class="user-info">
                <img :src="apply.userAvatar || defaultAvatar" class="user-avatar" />
                <span>{{ apply.userNickname }}</span>
              </div>
            </td>
            <td>{{ apply.creatorName }}</td>
            <td class="reason-cell">
              <el-tooltip :content="apply.reason" placement="top" :show-after="500">
                <span>{{ truncateText(apply.reason, 15) }}</span>
              </el-tooltip>
            </td>
            <td>{{ formatDate(apply.applyTime) }}</td>
            <td class="actions-cell">
              <button class="action-btn approve-btn" @click="handleApprove(apply)">通过</button>
              <button class="action-btn reject-btn" @click="handleReject(apply)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="records.length === 0" class="empty-state">暂无待处理退款申请</div>

    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { refundApi } from '@/api/admin/refund'

const emit = defineEmits(['update:pendingCount'])

const records = ref([])
const total = ref(0)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const pagination = ref({
  pageNum: 1,
  pageSize: 10,
})

const loadRefundApplyList = async () => {
  try {
    const response = await refundApi.getRefundApplyPage({
      status: 'PENDING',
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })

    if (response.records) {
      records.value = response.records
      total.value = response.total
    } else if (response.data && response.data.records) {
      records.value = response.data.records
      total.value = response.data.total
    }

    emit('update:pendingCount', total.value)
  } catch (error) {
    console.error('Failed to load refund apply list:', error)
    ElMessage.error('加载退款申请列表失败')
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  loadRefundApplyList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  loadRefundApplyList()
}

const handleApprove = async (apply) => {
  try {
    await ElMessageBox.confirm(
      `确定通过「${apply.userNickname}」的退款申请吗？通过后费用将退还给用户。`,
      '审核通过',
      {
        confirmButtonText: '确定通过',
        cancelButtonText: '取消',
        type: 'success',
      },
    )
    await refundApi.auditRefund({
      applyId: apply.id,
      approved: true,
      rejectReason: null,
    })
    ElMessage.success('已通过退款申请')
    loadRefundApplyList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (apply) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝退款', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '请填写拒绝原因',
      inputValidator: (value) => {
        if (!value || value.trim() === '') {
          return '拒绝原因不能为空'
        }
        return true
      },
    })
    await refundApi.auditRefund({
      applyId: apply.id,
      approved: false,
      rejectReason: reason,
    })
    ElMessage.success('已拒绝退款申请')
    loadRefundApplyList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '…' : text
}

defineExpose({
  reload: loadRefundApplyList,
})

onMounted(() => {
  loadRefundApplyList()
})
</script>

<style scoped>
.refund-audit {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 24px;
}

.audit-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20px;
}

.audit-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.audit-count {
  font-size: 0.85rem;
  color: #8e8c84;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 16px;
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1100px;
}

.audit-table th {
  text-align: left;
  padding: 14px 12px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #8e8c84;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  border-bottom: 1px solid #efeee8;
}

.audit-table td {
  padding: 16px 12px;
  font-size: 0.9rem;
  color: #1a1a1a;
  border-bottom: 1px solid #f0efe9;
}

.audit-table tbody tr:hover {
  background: #fcfbf7;
}

.audit-table tbody tr:last-child td {
  border-bottom: none;
}

.id-cell {
  font-weight: 600;
  color: #5a5953;
}

.price-cell {
  font-weight: 600;
  color: #1a1a1a;
}

.time-range {
  font-size: 0.8rem;
  color: #8e8c84;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.reason-cell {
  max-width: 150px;
}

.reason-cell span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: default;
}

.actions-cell {
  white-space: nowrap;
}

.action-btn {
  padding: 6px 16px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: transparent;
  color: #1a1a1a;
  margin-right: 8px;
}

.action-btn:last-child {
  margin-right: 0;
}

.approve-btn:hover {
  background: #e8f3e8;
  border-color: #b3d9b3;
  color: #2e6b2e;
}

.reject-btn:hover {
  background: #fdeded;
  border-color: #f5c2c2;
  color: #c74242;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #b0aea3;
  font-size: 0.9rem;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: #ffffff;
  --el-pagination-border-color: #efeee8;
  --el-pagination-border-radius: 40px;
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
  .refund-audit {
    padding: 16px;
  }
}
</style>
