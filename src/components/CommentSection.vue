<template>
  <div class="comment-section">
    <h3>评论 ({{ commentTotal }})</h3>

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
        <el-button type="primary" :loading="submitting" @click="handleSubmitComment"
          >发表评论</el-button
        >
      </div>
    </div>

    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon> 加载中...
    </div>
    <div v-else-if="commentList.length === 0" class="empty">
      <el-empty description="暂无评论，快来抢沙发吧！" />
    </div>
    <div v-else class="comment-list">
      <div v-for="comment in commentList" :key="comment.id" class="comment-item">
        <div class="comment-header">
          <img :src="comment.avatar || defaultAvatar" alt="头像" class="avatar" />
          <div class="user-info">
            <span class="nickname">{{ comment.nickname }}</span>
            <span class="time">{{ formatTime(comment.createTime) }}</span>
          </div>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-actions">
          <el-button class="action-btn" @click="handleLikeComment(comment)">
            <el-icon><Pointer /></el-icon>
            <span class="like-count" :class="{ liked: comment.likedByCurrentUser }">{{
              comment.likeCount || 0
            }}</span>
          </el-button>
          <el-button class="action-btn" @click="toggleReplyForm(comment)">
            <el-icon><ChatDotRound /></el-icon> 回复
          </el-button>
        </div>

        <!-- 内联回复表单 -->
        <div v-if="activeReplyId === comment.id" class="inline-reply-form">
          <el-input
            v-model="replyContent"
            type="textarea"
            placeholder="输入回复内容..."
            :rows="2"
            maxlength="500"
            show-word-limit
          />
          <div class="inline-reply-actions">
            <el-button type="primary" :loading="submittingReply" @click="submitInlineReply">
              发表回复
            </el-button>
          </div>
        </div>

        <!-- 回复列表（所有回复扁平化显示在顶级评论下，按时间升序） -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <div class="reply-header">
              <img :src="reply.avatar || defaultAvatar" alt="头像" class="avatar" />
              <div class="user-info">
                <span class="nickname">{{ reply.nickname }}</span>
                <span class="time">{{ formatTime(reply.createTime) }}</span>
              </div>
            </div>
            <div class="reply-content">
              <span class="reply-to" v-if="reply.replyToNickname"
                >@{{ reply.replyToNickname }}</span
              >
              {{ reply.content }}
            </div>
            <div class="reply-actions">
              <el-button class="action-btn" @click="handleLikeComment(reply)">
                <el-icon><Pointer /></el-icon>
                <span class="like-count" :class="{ liked: reply.likedByCurrentUser }">{{
                  reply.likeCount || 0
                }}</span>
              </el-button>
              <el-button class="action-btn" @click="toggleReplyForm(reply)">
                <el-icon><ChatDotRound /></el-icon> 回复
              </el-button>
            </div>

            <!-- 回复二级评论时的内联表单 -->
            <div v-if="activeReplyId === reply.id" class="inline-reply-form">
              <el-input
                v-model="replyContent"
                type="textarea"
                placeholder="输入回复内容..."
                :rows="2"
                maxlength="500"
                show-word-limit
              />
              <div class="inline-reply-actions">
                <el-button type="primary" :loading="submittingReply" @click="submitInlineReply">
                  发表回复
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hasMore" class="load-more">
      <el-button link :loading="loadingMore" @click="loadMore">加载更多评论</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Pointer, ChatDotRound } from '@element-plus/icons-vue'
import { commentApi } from '@/api/user/comment'

const props = defineProps({
  audioId: {
    type: Number,
    required: true,
  },
})

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const commentList = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const submitting = ref(false)
const submittingReply = ref(false)
const commentTotal = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const activeReplyId = ref(null)
const replyContent = ref('')
const replyTarget = ref(null)

const hasMore = computed(() => commentList.value.length < commentTotal.value)

const commentForm = ref({
  content: '',
})

const flattenAndSortReplies = (replies) => {
  const flat = []
  const collect = (items) => {
    for (const item of items) {
      flat.push(item)
      if (item.replyList && item.replyList.length) {
        collect(item.replyList)
      }
    }
  }
  collect(replies)
  flat.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
  return flat
}

const getCommentList = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const res = await commentApi.getCommentPage({
      audioId: props.audioId,
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    })

    const comments = res.data.records || []
    commentTotal.value = res.data.total || 0

    const structuredComments = []
    comments.forEach((comment) => {
      const topLevelComment = {
        ...comment,
        replies: [],
      }
      if (comment.replyList && comment.replyList.length > 0) {
        topLevelComment.replies = flattenAndSortReplies(comment.replyList)
      }
      structuredComments.push(topLevelComment)
    })

    if (isLoadMore) {
      commentList.value.push(...structuredComments)
    } else {
      commentList.value = structuredComments
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  currentPage.value++
  getCommentList(true)
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

const handleSubmitComment = async () => {
  if (!commentForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  const content = commentForm.value.content

  submitting.value = true

  const tempComment = {
    id: Date.now(),
    content: content,
    nickname: '我',
    avatar: null,
    likeCount: 0,
    likedByCurrentUser: false,
    createTime: new Date().toISOString(),
    parentId: 0,
    replies: [],
  }

  commentList.value.unshift(tempComment)
  commentTotal.value++

  try {
    await commentApi.saveComment({
      audioId: props.audioId,
      content: content,
      parentId: 0,
    })
    ElMessage.success('评论成功')
    commentForm.value.content = ''
    currentPage.value = 1
    await getCommentList()
  } catch (error) {
    commentList.value = commentList.value.filter((c) => c.id !== tempComment.id)
    commentTotal.value--
    console.error('评论失败:', error)
    ElMessage.error(error.response?.data?.msg || '评论失败，请重试')
  } finally {
    submitting.value = false
  }
}

const toggleReplyForm = (comment) => {
  if (activeReplyId.value === comment.id) {
    activeReplyId.value = null
    replyContent.value = ''
    replyTarget.value = null
  } else {
    activeReplyId.value = comment.id
    replyTarget.value = comment
    // 根据是否为顶级评论决定是否添加 @前缀
    if (comment.parentId === 0) {
      replyContent.value = ''
    } else {
      replyContent.value = `@${comment.nickname} `
    }
  }
}

const submitInlineReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  if (!replyTarget.value) return

  const targetComment = replyTarget.value
  const content = replyContent.value
  const parentId = targetComment.id

  submittingReply.value = true

  let topLevelComment = null
  for (const comment of commentList.value) {
    if (comment.id === targetComment.id) {
      topLevelComment = comment
      break
    }
    if (comment.replies && comment.replies.find((r) => r.id === targetComment.id)) {
      topLevelComment = comment
      break
    }
  }

  const tempReply = {
    id: Date.now(),
    content: content,
    nickname: '我',
    avatar: null,
    likeCount: 0,
    likedByCurrentUser: false,
    createTime: new Date().toISOString(),
    parentId: parentId,
    replyToNickname: targetComment.nickname,
  }

  if (topLevelComment) {
    if (!topLevelComment.replies) topLevelComment.replies = []
    const index = topLevelComment.replies.findIndex(
      (r) => new Date(r.createTime) > new Date(tempReply.createTime),
    )
    if (index === -1) {
      topLevelComment.replies.push(tempReply)
    } else {
      topLevelComment.replies.splice(index, 0, tempReply)
    }
    commentTotal.value++
  }

  try {
    await commentApi.saveComment({
      audioId: props.audioId,
      content: content,
      parentId: parentId,
    })
    ElMessage.success('回复成功')
    activeReplyId.value = null
    replyContent.value = ''
    replyTarget.value = null
    currentPage.value = 1
    await getCommentList()
  } catch (error) {
    if (topLevelComment) {
      topLevelComment.replies = topLevelComment.replies.filter((r) => r.id !== tempReply.id)
      commentTotal.value--
    }
    console.error('回复失败:', error)
    ElMessage.error(error.response?.data?.msg || '回复失败，请重试')
  } finally {
    submittingReply.value = false
  }
}

const handleLikeComment = async (comment) => {
  const originalLiked = comment.likedByCurrentUser
  const originalCount = comment.likeCount

  comment.likedByCurrentUser = !originalLiked
  comment.likeCount = originalLiked ? originalCount - 1 : originalCount + 1

  try {
    await commentApi.likeComment({
      commentId: comment.id,
      action: originalLiked ? 'UNLIKE' : 'LIKE',
    })
  } catch (error) {
    comment.likedByCurrentUser = originalLiked
    comment.likeCount = originalCount
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  getCommentList()
})
</script>

<style scoped>
.comment-section {
  margin-top: 40px;
  padding: 28px;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 28px;
}

.comment-section h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
}

.comment-form {
  margin-bottom: 28px;
  padding: 20px;
  background: #fcfbf7;
  border-radius: 20px;
}

.comment-form :deep(.el-textarea__inner) {
  background: #ffffff;
  border: 1px solid #e8e6df;
  border-radius: 20px;
  font-size: 0.9rem;
  color: #1a1a1a;
}

.comment-form :deep(.el-textarea__inner:focus) {
  border-color: #1a1a1a;
  box-shadow: 0 0 0 2px rgba(26, 26, 26, 0.05);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.form-actions .el-button {
  border-radius: 40px;
  padding: 10px 28px;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-actions .el-button--primary {
  background: #1a1a1a;
  border: none;
}

.form-actions .el-button--primary:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

.loading,
.empty {
  padding: 60px 0;
  text-align: center;
  color: #b0aea3;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  padding: 20px;
  background: #fcfbf7;
  border: 1px solid #efeee8;
  border-radius: 24px;
}

.comment-header,
.reply-header {
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
  gap: 2px;
}

.nickname {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1a1a1a;
}

.time {
  font-size: 0.7rem;
  color: #b0aea3;
}

.comment-content,
.reply-content {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #6b6a62;
  margin-bottom: 12px;
  padding-left: 52px;
}

.reply-content .reply-to {
  color: #1a1a1a;
  font-weight: 500;
  margin-right: 4px;
}

.comment-actions,
.reply-actions {
  display: flex;
  gap: 20px;
  padding-left: 52px;
  margin-top: 8px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 0.85rem;
  font-weight: 500;
  color: #8e8c84;
  background: transparent;
  border: none;
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f7f6f2;
  color: #1a1a1a;
}

.like-count {
  font-weight: 600;
}

.like-count.liked {
  color: #e5484d;
}

.inline-reply-form {
  margin-top: 16px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
}

.inline-reply-form :deep(.el-textarea__inner) {
  background: #fcfbf7;
  border: 1px solid #e8e6df;
  border-radius: 16px;
}

.inline-reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.inline-reply-actions .el-button {
  border-radius: 40px;
  padding: 8px 24px;
  font-size: 0.85rem;
  font-weight: 500;
  background: #1a1a1a;
  border: none;
  color: #ffffff;
}

.inline-reply-actions .el-button:hover {
  background: #2c2c2c;
  transform: translateY(-1px);
}

.replies {
  margin-top: 16px;
  margin-left: 32px;
  padding-left: 20px;
  border-left: 2px solid #e8e6df;
}

.reply-item {
  padding: 16px;
  margin-bottom: 12px;
  background: #ffffff;
  border: 1px solid #efeee8;
  border-radius: 20px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-header .avatar {
  width: 32px;
  height: 32px;
}

.reply-content {
  padding-left: 44px;
}

.reply-actions {
  padding-left: 44px;
}

.load-more {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0efeb;
}

.load-more .el-button {
  color: #b0aea3;
  font-size: 0.85rem;
}

.load-more .el-button:hover {
  color: #1a1a1a;
}

@media (max-width: 768px) {
  .comment-section {
    padding: 20px;
  }
  .comment-content,
  .reply-content {
    padding-left: 0;
  }
  .comment-actions,
  .reply-actions {
    padding-left: 0;
  }
  .replies {
    margin-left: 12px;
    padding-left: 12px;
  }
  .reply-header .avatar {
    width: 28px;
    height: 28px;
  }
  .action-btn {
    padding: 4px 10px;
    font-size: 0.75rem;
  }
}
</style>
