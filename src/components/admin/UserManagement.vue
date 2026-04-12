<template>
  <div class="user-management">
    <div class="management-header">
      <h3 class="management-title">用户管理</h3>
      <span class="management-count">共 {{ total }} 位用户</span>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="输入用户名搜索"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <button class="search-btn" @click="handleSearch">搜索</button>
      <button class="reset-btn" @click="handleReset">重置</button>
    </div>

    <div class="table-wrapper">
      <table class="user-table">
        <thead>
          <tr>
            <th>用户ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>手机号</th>
            <th>创作者身份</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in records" :key="user.id">
            <td class="id-cell">{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname || '—' }}</td>
            <td>{{ user.phone || '—' }}</td>
            <td>
              <span :class="['role-badge', user.isCreator ? 'creator' : 'listener']">
                {{ user.isCreator ? '创作者' : '听众' }}
              </span>
            </td>
            <td>
              <span :class="['status-badge', user.status === 'NORMAL' ? 'normal' : 'banned']">
                {{ user.status === 'NORMAL' ? '正常' : '已封禁' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td class="actions-cell">
              <button
                v-if="user.status === 'NORMAL'"
                class="action-btn ban-btn"
                @click="handleBan(user)"
              >
                封禁
              </button>
              <button v-else class="action-btn unban-btn" @click="handleUnban(user)">解封</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="records.length === 0" class="empty-state">暂无用户数据</div>

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
import { Search } from '@element-plus/icons-vue'
import { adminUserApi } from '@/api/admin/user'

const records = ref([])
const total = ref(0)
const searchKeyword = ref('')

const pagination = ref({
  pageNum: 1,
  pageSize: 10,
})

const loadUserList = async () => {
  try {
    const response = await adminUserApi.getUserPage({
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      username: searchKeyword.value || undefined,
    })

    if (response.records) {
      records.value = response.records
      total.value = response.total
    } else if (response.data && response.data.records) {
      records.value = response.data.records
      total.value = response.data.total
    }
  } catch (error) {
    console.error('Failed to load user list:', error)
    ElMessage.error('加载用户列表失败')
  }
}

const handlePageChange = (page) => {
  pagination.value.pageNum = page
  loadUserList()
}

const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.pageNum = 1
  loadUserList()
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadUserList()
}

const handleReset = () => {
  searchKeyword.value = ''
  pagination.value.pageNum = 1
  loadUserList()
}

const handleBan = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要封禁用户「${user.nickname || user.username}」吗？封禁后该用户将无法登录和使用平台。`,
      '封禁用户',
      {
        confirmButtonText: '确定封禁',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    await adminUserApi.banUser(user.id)
    ElMessage.success('用户已封禁')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleUnban = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要解封用户「${user.nickname || user.username}」吗？解封后该用户将恢复正常使用。`,
      '解封用户',
      {
        confirmButtonText: '确定解封',
        cancelButtonText: '取消',
        type: 'info',
      },
    )
    await adminUserApi.unbanUser(user.id)
    ElMessage.success('用户已解封')
    loadUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return '—'
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

defineExpose({
  reload: loadUserList,
})

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-management {
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
  padding: 24px;
}

.management-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20px;
}

.management-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.management-count {
  font-size: 0.85rem;
  color: #8e8c84;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.search-input {
  flex: 1;
  max-width: 320px;
}

.search-input :deep(.el-input__wrapper) {
  background: #fcfbf7;
  border: 1px solid #e8e6df;
  border-radius: 40px;
  box-shadow: none;
  padding: 6px 16px;
}

.search-btn,
.reset-btn {
  padding: 8px 20px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: #ffffff;
  color: #1a1a1a;
}

.search-btn {
  background: #1a1a1a;
  border-color: #1a1a1a;
  color: #ffffff;
}

.search-btn:hover {
  background: #2c2c2c;
}

.reset-btn:hover {
  background: #f7f6f2;
}

.table-wrapper {
  overflow-x: auto;
  border-radius: 16px;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
}

.user-table th {
  text-align: left;
  padding: 14px 12px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #8e8c84;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  border-bottom: 1px solid #efeee8;
}

.user-table td {
  padding: 16px 12px;
  font-size: 0.9rem;
  color: #1a1a1a;
  border-bottom: 1px solid #f0efe9;
}

.user-table tbody tr:hover {
  background: #fcfbf7;
}

.user-table tbody tr:last-child td {
  border-bottom: none;
}

.id-cell {
  font-weight: 600;
  color: #5a5953;
}

.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 0.75rem;
  font-weight: 600;
}

.role-badge.creator {
  background: #e8f3e8;
  color: #2e6b2e;
}

.role-badge.listener {
  background: #f0efe9;
  color: #5a5953;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 30px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-badge.normal {
  background: #e8f3e8;
  color: #2e6b2e;
}

.status-badge.banned {
  background: #fdeded;
  color: #c74242;
}

.actions-cell {
  white-space: nowrap;
}

.action-btn {
  padding: 6px 20px;
  border-radius: 40px;
  font-weight: 500;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #e8e6df;
  background: transparent;
  color: #1a1a1a;
}

.ban-btn:hover {
  background: #fdeded;
  border-color: #f5c2c2;
  color: #c74242;
}

.unban-btn:hover {
  background: #e8f3e8;
  border-color: #b3d9b3;
  color: #2e6b2e;
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
  .user-management {
    padding: 16px;
  }

  .search-bar {
    flex-wrap: wrap;
  }

  .search-input {
    max-width: 100%;
  }
}
</style>
