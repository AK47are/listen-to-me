<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { commentApi } from '@/api/user/comment'

const props = defineProps({
  audioId: {
    type: Number,
    required: true,
  },
})

const commentList = ref([])
const loading = ref(false)
const submitting = ref(false)
const commentForm = ref({
  content: '',
  parentId: null,
})
const commentQuery = ref({
  audioId: props.audioId,
  pageNum: 1,
  pageSize: 100,
})

const getCommentList = async () => {
  loading.value = true
  try {
    // 使用 getCommentPage 替代 getCommentList
    const res = await commentApi.getCommentPage(commentQuery.value)

    const comments = res.data.records || []
    const structuredComments = []

    comments.forEach((comment) => {
      const topLevelComment = {
        ...comment,
        replies: [],
      }
      structuredComments.push(topLevelComment)

      if (comment.replyList && comment.replyList.length > 0) {
        collectReplies(comment.replyList, topLevelComment.replies)
      }
    })

    commentList.value = structuredComments
  } catch (error) {
    ElMessage.error('获取评论列表失败')
    console.error('获取评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

const collectReplies = (replies, replyList) => {
  replies.forEach((reply) => {
    replyList.push(reply)
    if (reply.replyList && reply.replyList.length > 0) {
      collectReplies(reply.replyList, replyList)
    }
  })
}

const handleSubmitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const parentId = commentForm.value.parentId || 0
    await commentApi.saveComment({
      audioId: props.audioId,
      content: commentForm.value.content,
      parentId,
    })
    ElMessage.success('评论成功')
    commentForm.value.content = ''
    commentForm.value.parentId = null
    await getCommentList()
  } catch (error) {
    console.error('评论失败:', error)
  } finally {
    submitting.value = false
  }
}

const handleReply = (comment) => {
  commentForm.value.parentId = comment.id
  commentForm.value.content = `@${comment.nickname} `
}

const handleLikeComment = async (comment) => {
  try {
    // 使用 likeComment 替代 saveCommentLike
    await commentApi.likeComment({
      commentId: comment.id,
      action: comment.likedByCurrentUser ? 'UNLIKE' : 'LIKE',
    })
    comment.likedByCurrentUser = !comment.likedByCurrentUser
    comment.likeCount += comment.likedByCurrentUser ? 1 : -1
  } catch (error) {
    console.error('操作失败:', error)
  }
}

onMounted(() => {
  getCommentList()
})
</script>

<template>
  <div class="comment-section">
    <h3>评论 ({{ commentList.length }})</h3>

    <div class="comment-form">
      <el-input
        v-model="commentForm.content"
        type="textarea"
        placeholder="发表你的评论..."
        :rows="3"
        maxlength="500"
        show-word-limit
      />
      <div class="form-actions">
        <el-button v-if="commentForm.parentId" @click="commentForm.parentId = null">
          取消回复
        </el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitComment">
          发表评论
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      加载中...
    </div>
    <div v-else-if="commentList.length === 0" class="empty">
      <el-empty description="暂无评论，快来抢沙发吧！" />
    </div>
    <div v-else class="comment-list">
      <!-- 顶级评论 -->
      <div v-for="comment in commentList" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <img :src="comment.avatar" alt="头像" class="avatar" />
          <div class="user-info">
            <span class="nickname">{{ comment.nickname }}</span>
            <span class="time">{{ new Date(comment.createTime).toLocaleString() }}</span>
          </div>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-actions">
          <el-button
            link
            :type="comment.likedByCurrentUser ? 'danger' : 'default'"
            @click="handleLikeComment(comment)"
          >
            <el-icon><Pointer /></el-icon>
            {{ comment.likeCount || 0 }}
          </el-button>
          <el-button link @click="handleReply(comment)">
            <el-icon><ChatDotRound /></el-icon>
            回复
          </el-button>
        </div>

        <!-- 回复评论，挂在顶级评论下面 -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <div class="reply-header">
              <img :src="reply.avatar" alt="头像" class="avatar" />
              <div class="user-info">
                <span class="nickname">{{ reply.nickname }}</span>
                <span class="time">{{ new Date(reply.createTime).toLocaleString() }}</span>
              </div>
            </div>
            <div class="reply-content">
              {{ reply.content }}
            </div>
            <div class="reply-actions">
              <el-button
                link
                :type="reply.likedByCurrentUser ? 'danger' : 'default'"
                @click="handleLikeComment(reply)"
              >
                <el-icon><Pointer /></el-icon>
                {{ reply.likeCount || 0 }}
              </el-button>
              <el-button link @click="handleReply(reply)">
                <el-icon><ChatDotRound /></el-icon>
                回复
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-section {
  margin-top: 40px;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.comment-section h3 {
  margin-bottom: 20px;
  color: #333;
}

.comment-form {
  margin-bottom: 30px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  gap: 10px;
  color: #999;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
}

.comment-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nickname {
  font-weight: bold;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-actions {
  display: flex;
  gap: 20px;
}

/* 回复区域样式 */
.replies {
  margin-top: 20px;
  padding-left: 20px;
  border-left: 2px solid #e0e0e0;
}

.reply-item {
  padding: 15px;
  margin-bottom: 15px;
  background: #f9f9f9;
  border-radius: 6px;
}

.reply-header {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.reply-header .avatar {
  width: 32px;
  height: 32px;
}

.reply-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}

.reply-actions {
  display: flex;
  gap: 15px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-section {
    padding: 20px;
  }

  .comment-actions,
  .reply-actions {
    flex-wrap: wrap;
  }

  .comment-item {
    padding: 15px;
  }

  .replies {
    padding-left: 10px;
  }

  .reply-item {
    padding: 12px;
  }
}
</style>
