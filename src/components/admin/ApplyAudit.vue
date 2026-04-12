<template>
  <div class="admin-dashboard">
    <h1>申请审核</h1>

    <div class="audit-section">
      <table class="data-table">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>真实姓名</th>
            <th>电话</th>
            <th>简介</th>
            <th>附件链接</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="apply in auditApplyList.records" :key="apply.id">
            <td>{{ apply.id }}</td>
            <td>{{ apply.realName }}</td>
            <td>{{ apply.phone }}</td>
            <td>{{ apply.intro }}</td>
            <td>
              <a :href="apply.attachment" target="_blank">{{ apply.attachment }}</a>
            </td>
            <td>{{ formatDate(apply.applyTime) }}</td>
            <td>
              <button class="btn approve" @click="auditApply(apply.id, 'APPROVED')">通过</button>
              <button class="btn reject" @click="auditApply(apply.id, 'REJECTED')">拒绝</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="auditApplyList.total > 0">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[1, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="auditApplyList.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

import { auditApi } from '@/api/admin/audit'
import { ElMessage, ElMessageBox } from 'element-plus'

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

const auditApply = async (applyId, status) => {
  try {
    let rejectReason = null
    if (status === 'REJECTED') {
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

          await auditApi.auditApply({
            applyId,
            status,
            rejectReason,
          })
          await loadAuditApplyList()
          ElMessage.success('审核操作已完成')
        })
        .catch(() => {
          // 用户取消输入
        })
    } else {
      await auditApi.auditApply({
        applyId,
        status,
        rejectReason,
      })
      await loadAuditApplyList()
      ElMessage.success('审核操作已完成')
    }
  } catch (error) {
    console.error('Failed to audit apply:', error)
  }
}
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadAuditApplyList()
})
</script>

<style scoped>
@import '@/resource/css/adminDash.css';
</style>
