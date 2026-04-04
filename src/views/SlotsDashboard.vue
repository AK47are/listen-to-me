<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { creatorApi } from '@/api/creator'

// 时间槽列表
const slotList = ref([])
const loading = ref(false)
const submitting = ref(false)

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 查询参数
const searchParams = reactive({
  startTime: '',
  endTime: ''
})

// 批量创建时间槽表单
const batchForm = reactive({
  slots: [
    {
      startTime: '',
      endTime: '',
      price: 50,
      address: '腾讯会议链接'
    }
  ]
})

// 获取时间槽列表
const getSlotList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      startTime: searchParams.startTime ? formatDateTime(searchParams.startTime) : '',
      endTime: searchParams.endTime ? formatDateTime(searchParams.endTime) : ''
    }
    const res = await creatorApi.getSlotPage(params)
    slotList.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取时间槽列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间为后端期望的格式
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

// 批量创建时间槽
const handleBatchCreate = async () => {
  // 验证表单
  const hasEmpty = batchForm.slots.some(slot => 
    !slot.startTime || !slot.endTime || !slot.price || !slot.address
  )
  if (hasEmpty) {
    ElMessage.warning('请填写完整的时间槽信息')
    return
  }

  // 格式化时间槽数据
  const formattedSlots = batchForm.slots.map(slot => ({
    ...slot,
    startTime: formatDateTime(slot.startTime),
    endTime: formatDateTime(slot.endTime)
  }))

  submitting.value = true
  try {
    await creatorApi.saveSlotBatch(formattedSlots)
    ElMessage.success('时间槽创建成功')
    // 重置表单
    batchForm.slots = [
      {
        startTime: '',
        endTime: '',
        price: 50,
        address: '腾讯会议链接'
      }
    ]
    // 刷新列表
    getSlotList()
  } catch (error) {
    ElMessage.error('创建时间槽失败')
  } finally {
    submitting.value = false
  }
}

// 添加时间槽行
const addSlotRow = () => {
  batchForm.slots.push({
    startTime: '',
    endTime: '',
    price: 50,
    address: '腾讯会议链接'
  })
}

// 删除时间槽行
const removeSlotRow = (index) => {
  if (batchForm.slots.length > 1) {
    batchForm.slots.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个时间槽')
  }
}

// 更新时间槽状态
const updateSlotStatus = async (slotId, status) => {
  try {
    await creatorApi.updateSlot(slotId, status)
    ElMessage.success('状态更新成功')
    getSlotList()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 删除时间槽
const deleteSlot = async (slotId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个时间槽吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await creatorApi.deleteSlot(slotId)
    ElMessage.success('删除成功')
    getSlotList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 搜索时间槽
const handleSearch = () => {
  pagination.pageNum = 1
  getSlotList()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.pageNum = page
  getSlotList()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  getSlotList()
}

// 格式化状态文本
const formatStatusText = (status) => {
  const statusMap = {
    'AVAILABLE': '可用',
    'BOOKED': '已预约',
    'EXPIRED': '已过期',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 格式化状态类型
const formatStatusType = (status) => {
  const typeMap = {
    'AVAILABLE': 'success',
    'BOOKED': 'warning',
    'EXPIRED': 'info',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  getSlotList()
})
</script>

<template>
  <div class="slots-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>
        <el-icon><Calendar /></el-icon>
        时间槽管理
      </h2>
    </div>

    <!-- 批量创建时间槽 -->
    <div class="section-card">
      <div class="section-header">
        <h3>
          <el-icon><Plus /></el-icon>
          批量创建时间槽
        </h3>
      </div>
      <div class="section-body">
        <div v-for="(slot, index) in batchForm.slots" :key="index" class="slot-form-row">
          <el-form :model="slot" label-width="100px" class="slot-form">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="开始时间" required>
                  <el-date-picker
                    v-model="slot.startTime"
                    type="datetime"
                    placeholder="选择开始时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="结束时间" required>
                  <el-date-picker
                    v-model="slot.endTime"
                    type="datetime"
                    placeholder="选择结束时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item label="价格" required>
                  <el-input
                    v-model.number="slot.price"
                    type="number"
                    min="0"
                    placeholder="请输入价格"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="预约地址" required>
                  <el-input
                    v-model="slot.address"
                    placeholder="请输入预约地址（如腾讯会议链接）"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="4" class="form-actions">
                <el-button
                  type="danger"
                  @click="removeSlotRow(index)"
                  :disabled="batchForm.slots.length <= 1"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div class="form-footer">
          <el-button type="primary" @click="addSlotRow">
            <el-icon><Plus /></el-icon>
            添加时间槽
          </el-button>
          <el-button
            type="success"
            :loading="submitting"
            @click="handleBatchCreate"
          >
            <el-icon><Check /></el-icon>
            批量创建
          </el-button>
        </div>
      </div>
    </div>

    <!-- 时间槽列表 -->
    <div class="section-card">
      <div class="section-header">
        <h3>
          <el-icon><List /></el-icon>
          时间槽列表
        </h3>
        <div class="search-bar">
          <el-date-picker
            v-model="searchParams.startTime"
            type="datetime"
            placeholder="开始时间"
            style="width: 200px; margin-right: 10px"
          />
          <el-date-picker
            v-model="searchParams.endTime"
            type="datetime"
            placeholder="结束时间"
            style="width: 200px; margin-right: 10px"
          />
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </div>
      </div>
      <div class="section-body">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="slotList.length === 0" class="empty-state">
          <el-empty description="暂无时间槽">
            <el-button type="primary" @click="addSlotRow">创建时间槽</el-button>
          </el-empty>
        </div>
        <div v-else class="slot-list">
          <el-table :data="slotList" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="时间" width="300">
              <template #default="scope">
                <div>{{ scope.row.startTime }} - {{ scope.row.endTime }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100" />
            <el-table-column prop="address" label="预约地址" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="formatStatusType(scope.row.status)">
                  {{ formatStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300">
              <template #default="scope">
                <el-button
                  v-if="scope.row.status === 'AVAILABLE'"
                  type="danger"
                  size="small"
                  @click="updateSlotStatus(scope.row.id, 'CANCELLED')"
                >
                  取消
                </el-button>
                <el-button
                  v-else-if="scope.row.status === 'CANCELLED'"
                  type="success"
                  size="small"
                  @click="updateSlotStatus(scope.row.id, 'AVAILABLE')"
                >
                  启用
                </el-button>
                <span v-else>-</span>
                <el-button
                  v-if="scope.row.status === 'AVAILABLE' || scope.row.status === 'CANCELLED'"
                  type="danger"
                  size="small"
                  @click="deleteSlot(scope.row.id)"
                  style="margin-left: 10px"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div v-if="pagination.total > 0" class="pagination">
          <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            :total="pagination.total"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import '@/resource/css/slotsDashboard.css';
</style>