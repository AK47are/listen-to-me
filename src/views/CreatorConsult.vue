<template>
  <div class="creator-consult">
    <h2>预约订单管理</h2>

    <!-- 订单列表 -->
    <div class="consult-list">
      <div class="filter-bar">
        <select v-model="searchParams.status">
          <option value="">全部状态</option>
          <option value="PENDING_CONFIRM">待确认</option>
          <option value="CONFIRMED">已确认</option>
          <option value="COMPLETED">已完成</option>
          <option value="CANCELLED">已取消</option>
          <option value="REFUND_PENDING">退款中</option>
          <option value="REFUNDED">已退款</option>
        </select>
        <input type="date" v-model="searchParams.startDate" placeholder="开始日期" />
        <input type="date" v-model="searchParams.endDate" placeholder="结束日期" />
        <button @click="getConsultOrders" class="btn btn-secondary">查询</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>日期</th>
            <th>时间</th>
            <th>价格</th>
            <th>状态</th>
            <th>用户</th>
            <th>留言</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in consultOrders" :key="order.id">
            <td>{{ order.id }}</td>
            <td>{{ order.date }}</td>
            <td>{{ order.startTime }} - {{ order.endTime }}</td>
            <td>{{ order.price }}</td>
            <td>{{ order.status }}</td>
            <td>
              <div class="user-info">
                <img :src="order.userAvatar" alt="用户头像" class="avatar" />
                <span>{{ order.userNickname }}</span>
              </div>
            </td>
            <td>{{ order.message }}</td>
            <td>
              <button
                v-if="order.status === 'PENDING_CONFIRM'"
                @click="confirmConsult(order.id)"
                class="btn btn-success"
              >
                确认
              </button>
              <button
                v-if="order.status === 'PENDING_CONFIRM'"
                @click="rejectConsult(order.id)"
                class="btn btn-danger"
              >
                拒绝
              </button>
              <button
                v-if="order.status === 'CONFIRMED'"
                @click="completeConsult(order.id)"
                class="btn btn-primary"
              >
                标记完成
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button @click="changePage(1)" :disabled="currentPage === 1">首页</button>
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
        <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
        <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">
          下一页
        </button>
        <button @click="changePage(totalPages)" :disabled="currentPage === totalPages">末页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { creatorConsultApi } from '@/api/creator/consult'

const consultOrders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchParams = ref({
  status: '',
  startDate: '',
  endDate: '',
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const getConsultOrders = async () => {
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: searchParams.value.status,
      startDate: searchParams.value.startDate,
      endDate: searchParams.value.endDate,
    }
    const response = await creatorConsultApi.getConsultPage(params)
    consultOrders.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('获取预约订单列表失败:', error)
    ElMessage.error('获取预约订单列表失败')
  }
}

const confirmConsult = async (id) => {
  try {
    await creatorConsultApi.confirmConsult(id)
    ElMessage.success('已确认预约')
    getConsultOrders()
  } catch (error) {
    console.error('确认预约失败:', error)
    ElMessage.error('确认预约失败')
  }
}

const rejectConsult = async (id) => {
  try {
    await ElMessageBox.confirm('确定要拒绝这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await creatorConsultApi.rejectConsult(id)
    ElMessage.success('预约拒绝成功')
    getConsultOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝预约失败:', error)
      ElMessage.error('拒绝预约失败')
    }
  }
}

const completeConsult = async (id) => {
  try {
    await creatorConsultApi.completeConsult(id)
    ElMessage.success('已标记完成')
    getConsultOrders()
  } catch (error) {
    console.error('标记完成失败:', error)
    ElMessage.error('标记完成失败')
  }
}

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    getConsultOrders()
  }
}

onMounted(() => {
  getConsultOrders()
})
</script>

<style scoped>
.creator-consult {
  padding: 20px;
}

.consult-list {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-bar {
  margin-bottom: 20px;
}

.filter-bar select,
.filter-bar input {
  padding: 8px;
  margin-right: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-secondary {
  background-color: #2196f3;
  color: white;
}

.btn-success {
  background-color: #4caf50;
  color: white;
}

.btn-danger {
  background-color: #f44336;
  color: white;
}

.btn-primary {
  background-color: #2196f3;
  color: white;
}

.table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.table th,
.table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.table th {
  background-color: #f2f2f2;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.pagination button {
  padding: 5px 10px;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
