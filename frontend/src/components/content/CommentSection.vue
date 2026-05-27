<template>
  <div class="comment-section">
    <h4>评论 ({{ comments.length }})</h4>

    <div class="comment-input">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="2"
        :placeholder="replyTarget ? `回复 @${replyTarget.userId}` : '写下你的评论...'"
        maxlength="2000"
        show-word-limit
      />
      <div class="input-actions">
        <span v-if="replyTarget" class="reply-hint">
          回复 @{{ replyTarget.userId }}
          <el-button size="small" text @click="cancelReply">取消</el-button>
        </span>
        <el-button type="primary" size="small" :loading="sending" @click="handleSubmit">
          发布
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-for="comment in comments" :key="comment.id" class="comment-item">
      <div class="comment-avatar">{{ comment.userId }}</div>
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-user">用户{{ comment.userId }}</span>
          <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
        </div>
        <div v-if="comment.replyToUserId" class="reply-to">
          回复 @{{ comment.replyToUserId }}
        </div>
        <p class="comment-text">{{ comment.body }}</p>
        <div class="comment-actions">
          <el-button size="small" text @click="handleLike(comment)">
            <i class="ri-thumb-up-line"></i> {{ comment.likeCount || 0 }}
          </el-button>
          <el-button size="small" text @click="startReply(comment)">回复</el-button>
          <el-button
            v-if="comment.userId === currentUserId"
            size="small" text type="danger"
            @click="handleDelete(comment)"
          >删除</el-button>
        </div>

        <!-- Replies -->
        <div v-if="comment.replies && comment.replies.length" class="replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
            <span class="reply-user">用户{{ reply.userId }}</span>
            <span v-if="reply.replyToUserId"> 回复 @{{ reply.replyToUserId }}</span>
            ：{{ reply.body }}
            <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
            <el-button size="small" text @click="handleLike(reply)" style="margin-left:8px">
              <i class="ri-thumb-up-line"></i> {{ reply.likeCount || 0 }}
            </el-button>
            <el-button
              v-if="reply.userId === currentUserId"
              size="small" text type="danger"
              @click="handleDelete(reply)"
            >删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && comments.length === 0" description="暂无评论，来发表第一条吧" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getComments, getReplies, createComment, likeComment, unlikeComment, deleteComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  contentId: { type: [String, Number], required: true }
})

const userStore = useUserStore()
const currentUserId = userStore.info?.id

const comments = ref([])
const loading = ref(false)
const newComment = ref('')
const sending = ref(false)
const replyTarget = ref(null)

async function fetchComments() {
  loading.value = true
  try {
    const res = await getComments(props.contentId)
    const list = res.data || []
    for (const c of list) {
      try {
        const replyRes = await getReplies(c.id, props.contentId)
        c.replies = replyRes.data || []
      } catch { c.replies = [] }
    }
    comments.value = list
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!newComment.value.trim()) return
  sending.value = true
  try {
    const data = {
      body: newComment.value,
      parentId: replyTarget.value?.id || null,
      replyToId: replyTarget.value?.id || null,
      replyToUserId: replyTarget.value?.userId || null
    }
    await createComment(props.contentId, data)
    newComment.value = ''
    replyTarget.value = null
    ElMessage.success('评论已发布')
    await fetchComments()
  } catch { /* handled by interceptor */ }
  finally { sending.value = false }
}

function startReply(comment) {
  replyTarget.value = comment
  newComment.value = ''
}

function cancelReply() {
  replyTarget.value = null
  newComment.value = ''
}

async function handleLike(comment) {
  try {
    // Simple toggle: if already liked by client state, unlike
    if (comment._liked) {
      await unlikeComment(comment.id)
      comment.likeCount = Math.max(0, (comment.likeCount || 1) - 1)
      comment._liked = false
    } else {
      await likeComment(comment.id)
      comment.likeCount = (comment.likeCount || 0) + 1
      comment._liked = true
    }
  } catch { /* handled */ }
}

async function handleDelete(comment) {
  try {
    await deleteComment(comment.id)
    ElMessage.success('评论已删除')
    await fetchComments()
  } catch { /* handled */ }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

onMounted(fetchComments)
</script>

<style scoped>
.comment-section { margin-top: 24px; }
.comment-section h4 { margin: 0 0 16px; font-size: 16px; }
.comment-input { margin-bottom: 20px; }
.input-actions { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.reply-hint { font-size: 12px; color: #909399; }
.comment-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.comment-avatar { width: 36px; height: 36px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; flex-shrink: 0; }
.comment-body { flex: 1; min-width: 0; }
.comment-header { display: flex; gap: 10px; align-items: center; margin-bottom: 4px; }
.comment-user { font-size: 13px; font-weight: 600; color: #303133; }
.comment-time { font-size: 12px; color: #c0c4cc; }
.reply-to { font-size: 12px; color: #909399; margin-bottom: 4px; }
.comment-text { margin: 0; font-size: 14px; line-height: 1.6; color: #303133; }
.comment-actions { margin-top: 6px; }
.replies { margin-top: 10px; padding-left: 16px; border-left: 2px solid #f0f0f0; }
.reply-item { padding: 6px 0; font-size: 13px; line-height: 1.6; }
.reply-user { font-weight: 600; color: #409eff; }
.reply-time { font-size: 11px; color: #c0c4cc; margin-left: 8px; }
.loading-wrapper { padding: 20px 0; }
</style>
