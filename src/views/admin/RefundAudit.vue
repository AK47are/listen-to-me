<template>
  <div class="admin-dashboard">
    <h1>退款审核</h1>

    <div class="refund-section">
      <table class="data-table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>订单ID</th>
            <th>日期</th>
            <th>时间</th>
            <th>金额</th>
            <th>用户</th>
            <th>创作者</th>
            <th>原因</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="apply in refundApplyList.records" :key="apply.id">
            <td>{{ apply.id }}</td>
            <td>{{ apply.orderId }}</td>
            <td>{{ apply.applyTime }}</td>
            <td>{{ apply.startTime }}-{{ apply.endTime }}</td>
            <td>¥{{ apply.price }}</td>
            <td>{{ apply.userNickname }}</td>
            <td>{{ apply.creatorName }}</td>
            <td>{{ apply.reason }}</td>
            <td>
              <button class="btn approve" @click="auditRefund(apply.id, true)">通过</button>
              <button class="btn reject" @click="auditRefund(apply.id, false)">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="refundApplyList.total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[1, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="refundApplyList.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { refundApi } from '@/api/admin/refund'
import { ElMessage, ElMessageBox } from 'element-plus'

const refundApplyList = ref({
  records: [],
  total: 0,
})

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
    refundApplyList.value = response.data
  } catch (error) {
    console.error('Failed to load refund apply list:', error)
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

const auditRefund = async (applyId, approved) => {
  try {
    let rejectReason = null
    if (!approved) {
      ElMessageBox.prompt('请输入拒绝原因', '拒绝审核', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入拒绝原因',
        inputValidator: (value) => {
          if (!value || value.trim() === '') {
            return '拒绝原因不能为空'
          }
          return true
        },
      })
        .then(async (result) => {
          rejectReason = result.value

          await refundApi.auditRefund({
            applyId,
            approved,
            rejectReason,
          })
          await loadRefundApplyList()
          ElMessage.success('审核操作已完成')
        })
        .catch(() => {
          // 用户取消输入
          ElMessage.info('审核操作已取消')
        })
    } else {
      await refundApi.auditRefund({
        applyId,
        approved,
        rejectReason,
      })
      await loadRefundApplyList()
      ElMessage.success('审核操作已完成')
    }
  } catch (error) {
    console.error('Failed to audit refund:', error)
  }
}
onMounted(() => {
  loadRefundApplyList()
})
</script>

<style scoped>
@import '@/resource/css/adminDash.css';
</style>
