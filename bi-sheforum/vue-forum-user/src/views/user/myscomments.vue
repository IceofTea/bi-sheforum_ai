<template>
  <div class="tieba-my-comments">
    <!-- 顶部导航 -->
    <header class="tieba-header">
      <div class="header-inner">
        <div class="logo" @click="goHome">校园论坛</div>
        <div class="header-actions">
          <el-button type="primary" icon="el-icon-edit" @click="gotoupload">发帖</el-button>
          <div class="user-info" @click="gotoUser">
            <img :src="userAvatar" class="avatar">
            <span class="nickname">{{ userNickname }}</span>
          </div>
        </div>
      </div>
    </header>

    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-left">
          <h2><i class="el-icon-chat-line-round"></i> 我的评论</h2>
          <span class="count">共 {{ filteredData.length }} 条评论</span>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <div 
          v-for="item in displayData" 
          :key="item.id" 
          class="comment-card"
          @click="gotoThread(item.threadInfo?.id)"
        >
          <div class="comment-avatar">
            <img :src="userAvatar">
          </div>
          <div class="comment-body">
            <div class="comment-header">
              <span class="username">我</span>
              <span class="time">{{ formatDateTime(item.time) }}</span>
            </div>
            <div class="comment-thread-info">
              评论了: <span class="thread-title">{{ item.threadInfo?.name || '未知帖子' }}</span>
            </div>
            <div class="comment-text">{{ item.comment || '' }}</div>
          </div>
          <el-popconfirm title="确定删除此评论?" confirm-button-text="删除" cancel-button-text="取消" @confirm.stop="deleteComment(item.id, $event)">
            <template #reference>
              <el-button size="small" type="danger" icon="el-icon-delete" circle @click.stop class="delete-comment-btn"></el-button>
            </template>
          </el-popconfirm>
        </div>

        <div class="empty-state" v-if="displayData.length === 0">
          <i class="el-icon-chat-line-round"></i>
          <p>还没有评论过</p>
          <el-button type="primary" @click="goHome">去首页逛逛</el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="10"
          :total="filteredData.length"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js'
import { formatRelativeTime, formatDateTime } from '@/utils/timeFormat.js';
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const commentList = ref([])
const currentPage = ref(1)
const pageSize = 10
const userNickname = ref('游客')
const userAvatar = ref('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==')

onMounted(() => {
  loadlist()
  loadUserInfo()
})

const loadUserInfo = async () => {
  if (sessionStorage.id) {
    try {
      const res = await axios.get('/api/users/one', { params: { id: sessionStorage.id } })
      if (res.data) {
        userNickname.value = res.data.nickname || '用户'
        if (res.data.head) {
          userAvatar.value = VERCODE_URL + '/upload/' + res.data.head
        }
      }
    } catch (e) {}
  }
}

const loadlist = async () => {
  try {
    const userId = parseInt(sessionStorage.id)
    console.log('请求评论列表, userId:', userId)
    const res = await axios.get('/api/comments/byuser', { params: { id: userId } })
    console.log('评论响应:', res.data)
    commentList.value = res.data || []
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

const formatTime = (time) => formatRelativeTime(time);

const handlePageChange = (page) => {
  currentPage.value = page
}

const filteredData = computed(() => {
  return commentList.value
})

const displayData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredData.value.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.ceil(filteredData.value.length / pageSize))

async function deleteComment(id, event) {
  event?.stopPropagation()
  try {
    const res = await axios.delete('/api/comments', { params: { id } })
    if (res.status === 200) {
      ElMessage.success('删除成功')
      loadlist()
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function gotoThread(id) {
  if (id) router.push(`/thread/read?id=${id}`)
}

function goHome() {
  router.push('/')
}

function gotoupload() {
  router.push('/uploadthread')
}

function gotoUser() {
  router.push('/user/index')
}
</script>

<style lang="less" scoped>
.tieba-my-comments {
  min-height: 100vh;
  background: #f5f8fc;
}

.tieba-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .header-inner {
    max-width: 1200px;
    margin: 0 auto;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
  }
  
  .logo {
    font-size: 22px;
    font-weight: bold;
    color: #fff;
    cursor: pointer;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .el-button {
      background: rgba(255,255,255,0.25);
      border: none;
      color: #fff;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
      }
      
      .nickname {
        color: #fff;
        font-size: 14px;
      }
    }
  }
}

.page-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  flex: 1;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    h2 {
      margin: 0;
      font-size: 20px;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;
      
      i {
        color: #409eff;
      }
    }
    
    .count {
      font-size: 14px;
      color: #999;
    }
  }
}

.comment-list {
  .comment-card {
    display: flex;
    gap: 16px;
    padding: 16px;
    background: #fff;
    border-radius: 12px;
    margin-bottom: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    cursor: pointer;
    transition: all 0.3s;
    border-left: 4px solid transparent;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(64, 158, 255, 0.15);
      border-left-color: #409eff;
    }

    .delete-comment-btn {
      position: absolute;
      right: 16px;
      top: 16px;
      opacity: 0;
      transition: opacity 0.2s;
    }

    &:hover .delete-comment-btn {
      opacity: 1;
    }
    
    .comment-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      overflow: hidden;
      flex-shrink: 0;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .comment-body {
      flex: 1;
      min-width: 0;
      
      .comment-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .username {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
        }
        
        .time {
          font-size: 12px;
          color: #c0c4cc;
        }
      }
      
      .comment-thread-info {
        font-size: 13px;
        color: #606266;
        margin-bottom: 8px;
        
        .thread-title {
          color: #409eff;
          font-weight: 500;
        }
      }
      
      .comment-text {
        font-size: 14px;
        color: #303133;
        line-height: 1.6;
        background: #f5f8fc;
        padding: 12px;
        border-radius: 8px;
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 60px 20px;
    background: #fff;
    border-radius: 12px;

    i {
      font-size: 60px;
      color: #ddd;
      margin-bottom: 15px;
    }

    p {
      color: #999;
      margin-bottom: 20px;
    }
  }
}

.pagination {
  text-align: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  margin-top: 15px;
}
</style>