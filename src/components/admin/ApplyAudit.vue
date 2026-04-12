<template>
  <div class="apply-audit">
    <div class="audit-header">
      <h3 class="audit-title">创作者申请审核</h3>
      <span class="audit-count">共 {{ auditApplyList.total }} 条申请</span>
    </div>

    <div class="table-wrapper">
      <table class="audit-table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>真实姓名</th>
            <th>联系电话</th>
            <th>个人简介</th>
            <th>附件</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="apply in auditApplyList.records" :key="apply.id">
            <td class="id-cell">{{ apply.id }}</td>
            <td class="name-cell">{{ apply.realName }}</td>
            <td>{{ apply.phone }}</td>
            <td class="intro-cell">
              <el-tooltip :content="apply.intro" placement="top" :show-after="500">
                <span>{{ truncateText(apply.intro, 20) }}</span>
              </el-tooltip>
            </td>
            <td>
              <a :href="apply.attachment" target="_blank" class="attachment-link">查看</a>
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

    <div v-if="auditApplyList.records.length === 0" class="empty-state">暂无待审核申请</div>

    <div v-if="auditApplyList.total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="sizes, prev, pager, next"
        :total="auditApplyList.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditApi } from '@/api/admin/audit'

const emit = defineEmits(['update:pendingCount'])

const auditApplyList = ref({
  records: [],
  total: 0,
})

const pagination = ref({
  pageNum: 1,
  pageSize: 10,
})

const loadAuditApplyList = async () => {
  try {
    const response = await auditApi.getAuditApplyPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    auditApplyList.value = response.data
    emit('update:pendingCount', auditApplyList.value.total)
  } catch (error) {
    console.error('Failed to load audit apply list:', error)
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  loadAuditApplyList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  loadAuditApplyList()
}

const handleApprove = async (apply) => {
  try {
    await ElMessageBox.confirm(`确定通过「${apply.realName}」的创作者申请吗？`, '审核通过', {
      confirmButtonText: '确定通过',
      cancelButtonText: '取消',
      type: 'success',
    })
    await auditApi.auditApply({
      applyId: apply.id,
      status: 'APPROVED',
      rejectReason: null,
    })
    ElMessage.success('已通过申请')
    loadAuditApplyList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = async (apply) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
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
    await auditApi.auditApply({
      applyId: apply.id,
      status: 'REJECTED',
      rejectReason: reason,
    })
    ElMessage.success('已拒绝申请')
    loadAuditApplyList()
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

const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '…' : text
}

defineExpose({
  reload: loadAuditApplyList,
})

onMounted(() => {
  loadAuditApplyList()
})
</script>

<style scoped>
.apply-audit {
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
  min-width: 900px;
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

.name-cell {
  font-weight: 500;
}

.intro-cell {
  max-width: 200px;
}

.intro-cell span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: default;
}

.attachment-link {
  color: #1a1a1a;
  text-decoration: none;
  border-bottom: 1px dashed #b0aea3;
  padding-bottom: 2px;
  transition: all 0.2s;
}

.attachment-link:hover {
  color: #000000;
  border-bottom-color: #1a1a1a;
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
  .apply-audit {
    padding: 16px;
  }
}
</style>
