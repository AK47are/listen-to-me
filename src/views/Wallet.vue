<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { balanceApi } from '@/api/user/balance'
import { orderApi } from '@/api/user/order'

const router = useRouter()

const balance = ref(0)
const orderList = ref([])
const loading = ref(false)
const showRechargeDialog = ref(false)
const rechargeForm = ref({
  amount: 100,
  paymentMethod: 'ALIPAY',
})

const getBalance = async () => {
  try {
    const res = await balanceApi.getBalance()
    balance.value = res.balance
  } catch (error) {
    ElMessage.error('获取余额失败')
  }
}

const getOrderList = async () => {
  loading.value = true
  try {
    const res = await orderApi.getAudioOrderPage({
      pageNum: 1,
      pageSize: 20,
    })
    orderList.value = res.records || []
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleRecharge = async () => {
  try {
    const res = await balanceApi.recharge(rechargeForm.value)
    window.open(res.payUrl, '_blank')
    ElMessage.success('充值链接已打开，请在支付宝中完成支付')
    showRechargeDialog.value = false
  } catch (error) {
    ElMessage.error('充值失败')
  }
}

onMounted(() => {
  getBalance()
  getOrderList()
})
</script>

<template>
  <div class="wallet-container">
    <div class="balance-card">
      <h2>我的钱包</h2>
      <div class="balance-info">
        <div class="balance-amount">
          <span class="label">当前余额</span>
          <span class="amount">{{ balance }}</span>
          <span class="unit">虚拟币</span>
        </div>
        <el-button type="primary" size="large" @click="showRechargeDialog = true">
          <el-icon><Wallet /></el-icon> 立即充值
        </el-button>
      </div>
    </div>

    <div class="order-section">
      <h2>订单记录</h2>
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        加载中...
      </div>
      <div v-else-if="orderList.length === 0" class="empty">
        <el-empty description="暂无订单记录" />
      </div>
      <div v-else class="order-list">
        <div v-for="order in orderList" :key="order.orderSn" class="order-item">
          <div class="order-info">
            <div class="order-header">
              <span class="order-type">{{ order.orderType === 'PURCHASE' ? '购买' : '充值' }}</span>
              <span class="order-sn">{{ order.orderSn }}</span>
            </div>
            <div class="order-detail">
              <span v-if="order.audioTitle">{{ order.audioTitle }}</span>
              <span class="order-amount">{{ order.payAmount }} 虚拟币</span>
            </div>
            <div class="order-time">{{ new Date(order.createTime).toLocaleString() }}</div>
          </div>
          <div class="order-status" :class="order.status.toLowerCase()">
            {{
              order.status === 'SUCCESS' ? '成功' : order.status === 'PENDING' ? '待支付' : '失败'
            }}
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showRechargeDialog" title="充值" width="400px">
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeForm.amount" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio label="ALIPAY">支付宝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRechargeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.wallet-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.balance-card h2 {
  margin: 0 0 30px 0;
  font-size: 24px;
}

.balance-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-amount {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.balance-amount .label {
  font-size: 16px;
  opacity: 0.9;
}

.balance-amount .amount {
  font-size: 48px;
  font-weight: bold;
}

.balance-amount .unit {
  font-size: 16px;
  opacity: 0.9;
}

.order-section {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-section h2 {
  margin-bottom: 20px;
  color: #333;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  gap: 10px;
  color: #999;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s;
}

.order-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.order-info {
  flex: 1;
}

.order-header {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
}

.order-type {
  padding: 4px 12px;
  border-radius: 4px;
  background: #f0f0f0;
  font-size: 12px;
  font-weight: bold;
}

.order-sn {
  color: #999;
  font-size: 14px;
}

.order-detail {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 16px;
  color: #333;
}

.order-amount {
  font-weight: bold;
  color: #f56c6c;
}

.order-time {
  color: #999;
  font-size: 12px;
}

.order-status {
  padding: 6px 16px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
}

.order-status.success {
  background: #f0f9ff;
  color: #67c23a;
}

.order-status.pending {
  background: #fff7e6;
  color: #e6a23c;
}

.order-status.failed {
  background: #fef0f0;
  color: #f56c6c;
}
</style>
