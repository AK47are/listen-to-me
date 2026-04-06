<template>
  <!-- 用户管理 -->
  <div class="refund-section">
    <h1>用户管理</h1>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="userSearchQuery"
        placeholder="输入用户名查询"
        clearable
        style="width: 300px"
        @keyup.enter="handleUserSearch"
      >
        <template #append>
          <el-button type="primary" @click="handleUserSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 用户列表 -->
    <table class="data-table">
      <thead>
        <tr>
          <th>用户ID</th>
          <th>用户名</th>
          <th>昵称</th>
          <th>创作者身份</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in userList.records" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.nickname }}</td>
          <td>
            <el-tag :type="user.isCreator ? 'success' : 'info'">
              {{ user.isCreator ? '是' : '否' }}
            </el-tag>
          </td>
          <td>
            <button class="btn reject" @click="banUser(user.id)">封禁</button>
            <button class="btn approve" @click="unbanUser(user.id)">解封</button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 分页 -->
    <div class="pagination" v-if="userList.total > 0">
      <el-pagination
        v-model:current-page="userPagination.pageNum"
        v-model:page-size="userPagination.pageSize"
        :page-sizes="[1, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="userList.total"
        @size-change="handleUserSizeChange"
        @current-change="handleUserPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminUserApi } from '@/api/admin/user'
import { ElMessage, ElIcon } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const userList = ref({
  records: [],
  total: 0,
})
const userSearchQuery = ref('')
const userPagination = ref({
  pageNum: 1,
  pageSize: 10,
})

// 加载用户列表
const loadUserList = async () => {
  try {
    const response = await adminUserApi.getUserPage({
      pageNum: userPagination.value.pageNum,
      pageSize: userPagination.value.pageSize,
      username: userSearchQuery.value,
    })
    userList.value = response.data
  } catch (error) {
    console.error('Failed to load user list:', error)
  }
}

// 封禁用户
const banUser = async (userId) => {
  try {
    await adminUserApi.banUser(userId)
    ElMessage.success('用户已封禁')
    loadUserList()
  } catch (error) {
    console.error('Failed to ban user:', error)
  }
}

// 解封用户
const unbanUser = async (userId) => {
  try {
    await adminUserApi.unbanUser(userId)
    ElMessage.success('用户已解封')
    loadUserList()
  } catch (error) {
    console.error('Failed to unban user:', error)
  }
}

// 用户列表分页处理
const handleUserPageChange = (page) => {
  userPagination.value.pageNum = page
  loadUserList()
}

const handleUserSizeChange = (size) => {
  userPagination.value.pageSize = size
  userPagination.value.pageNum = 1
  loadUserList()
}

const handleUserSearch = () => {
  userPagination.value.pageNum = 1
  loadUserList()
}
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
@import '@/resource/css/adminDash.css';
</style>
