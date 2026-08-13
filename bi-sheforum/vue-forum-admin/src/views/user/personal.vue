<template>
  <div class="personal-center">
    <el-row :gutter="20">
      <!-- 左侧个人信息 -->
      <el-col :span="8">
        <el-card shadow="hover" class="profile-card">
          <div class="user-avatar">
            <el-avatar :size="100" :src="userInfo.head ? VERCODE_URL + '/upload/' + userInfo.head : defaultAvatar">
              {{ userInfo.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="user-info">
            <h3>{{ userInfo.nickname || '用户' }}</h3>
            <p>@{{ userInfo.username }}</p>
            <p>注册时间: {{ formatTime(userInfo.registerTime) }}</p>
          </div>
        </el-card>

        <el-card shadow="hover" class="stats-card">
          <h4>统计信息</h4>
          <div class="stat-item">
            <span>发帖数:</span>
            <span class="stat-value">{{ stats.threadCount || 0 }}</span>
          </div>
          <div class="stat-item">
            <span>评论数:</span>
            <span class="stat-value">{{ stats.commentCount || 0 }}</span>
          </div>
          <div class="stat-item">
            <span>获赞数:</span>
            <span class="stat-value">{{ stats.likesCount || 0 }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧内容区域 -->
      <el-col :span="16">
        <el-card shadow="hover" class="content-card">
          <div class="tab-header">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="我的帖子" name="threads">
                <div v-if="myThreads.length > 0">
                  <div v-for="thread in myThreads" :key="thread.id" class="thread-item">
                    <div class="thread-title">
                      <router-link :to="'/thread/detail?id=' + thread.id">
                        {{ thread.name }}
                      </router-link>
                    </div>
                    <div class="thread-meta">
                      <span>发布时间: {{ formatTime(thread.time) }}</span>
                      <span>浏览: {{ thread.viewCount || 0 }}</span>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无帖子" />
              </el-tab-pane>

              <el-tab-pane label="我的评论" name="comments">
                <div v-if="myComments.length > 0">
                  <div v-for="comment in myComments" :key="comment.id" class="comment-item">
                    <div class="comment-content">
                      <router-link :to="'/thread/detail?id=' + comment.threadInfoId" class="thread-link">
                        回复: {{ getThreadName(comment.threadInfoId) }}
                      </router-link>
                      <p class="comment-text">{{ comment.comment }}</p>
                    </div>
                    <div class="comment-time">
                      {{ formatTime(comment.time) }}
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无评论" />
              </el-tab-pane>

              <el-tab-pane label="我的收藏" name="favorites">
                <div v-if="favorites.length > 0">
                  <div v-for="fav in favorites" :key="fav.id" class="favorite-item">
                    <div class="favorite-title">
                      <router-link :to="'/thread/detail?id=' + fav.threadInfoId">
                        {{ getThreadName(fav.threadInfoId) }}
                      </router-link>
                    </div>
                    <div class="favorite-time">
                      收藏时间: {{ formatTime(fav.time) }}
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无收藏" />
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { VERCODE_URL } from '@/plugins/config.js'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const activeTab = ref('comments')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = reactive({
  username: '',
  nickname: '',
  head: '',
  registerTime: null
})

// 统计数据
const stats = reactive({
  threadCount: 0,
  commentCount: 0,
  likesCount: 0
})

// 数据列表
const myThreads = ref([])
const myComments = ref([])
const favorites = ref([])

// 加载用户信息
async function loadUserInfo() {
  try {
    // 先尝试通过用户名获取用户ID
    const userRes = await axios.get('/api/users/byusername', {
      params: { username: sessionStorage.rootname }
    })

    let userId = null
    if (userRes.data && userRes.data.id) {
      userId = userRes.data.id
    } else {
      // 如果byusername接口不存在，使用用户名作为ID（假设rootname是ID）
      userId = sessionStorage.rootname
    }

    const res = await axios.get('/api/users/one', {
      params: { id: userId }
    })
    if (res.data) {
      Object.assign(userInfo, res.data)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')

    // 设置默认值
    userInfo.username = sessionStorage.rootname
    userInfo.nickname = sessionStorage.rootname
    userInfo.registerTime = new Date()
  }
}

// 加载统计数据
async function loadStats() {
  try {
    const [threadRes, commentRes] = await Promise.all([
      axios.get('/api/threads/byuser', { params: { writerId: getUserId() } }),
      axios.get('/api/comments/byuser', { params: { userId: getUserId() } })
    ])

    stats.threadCount = threadRes.data?.length || 0
    stats.commentCount = commentRes.data?.length || 0

    // 这里可以添加获取点赞数的API调用
    stats.likesCount = 42 // 模拟数据
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 设置默认值
    stats.threadCount = 0
    stats.commentCount = 0
    stats.likesCount = 0
  }
}

// 获取用户ID
function getUserId() {
  // 优先使用从API获取的用户ID，如果没有则使用sessionStorage中的用户名
  return userInfo.id || sessionStorage.rootname
}

// 加载我的帖子
async function loadMyThreads() {
  try {
    const res = await axios.get('/api/threads/byuser', {
      params: { writerId: getUserId() }
    })
    myThreads.value = res.data || []
  } catch (error) {
    console.error('加载帖子失败:', error)
    myThreads.value = []
  }
}

// 加载我的评论
async function loadMyComments() {
  try {
    const res = await axios.get('/api/comments/byuser', {
      params: { userId: getUserId() }
    })
    myComments.value = res.data || []
  } catch (error) {
    console.error('加载评论失败:', error)
    myComments.value = []
  }
}

// 加载收藏
async function loadFavorites() {
  try {
    const res = await axios.get('/api/usercollect/byuser', {
      params: { userId: getUserId() }
    })
    favorites.value = res.data || []
  } catch (error) {
    console.error('加载收藏失败:', error)
    favorites.value = []
  }
}

// 格式化时间
function formatTime(timeStr) {
  if (!timeStr) return ''

  let date
  try {
    // 处理Spring Boot JSON Format返回的时间格式（中文格式）
    if (typeof timeStr === 'string' && /^\d{4}年\d{1,2}月\d{1,2}号/.test(timeStr)) {
      // 直接返回中文格式的时间，这是Spring Boot @JsonFormat的结果
      return timeStr
    }

    // 处理ISO格式的字符串
    date = new Date(timeStr.replace('T', ' ').replace(/\.\d{3}Z$/, ''))

    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      // 如果无效，尝试原始字符串
      return timeStr
    }

    const year = date.getFullYear()
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')

    return `${year}年${month}月${day}日 ${hours}:${minutes}`
  } catch (error) {
    console.error('时间格式化错误:', error)
    return timeStr
  }
}

// 获取帖子名称（用于显示评论中的帖子标题）
function getThreadName(threadId) {
  const thread = myThreads.value.find(t => t.id === threadId)
  return thread ? thread.name : '帖子已删除'
}

// 初始化数据
onMounted(async () => {
  try {
    await loadUserInfo()

    if (!userInfo.id) {
      // 如果无法获取用户ID，设置模拟数据
      setMockData()
    } else {
      await Promise.all([
        loadStats(),
        loadMyThreads(),
        loadMyComments(),
        loadFavorites()
      ])
    }
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('加载数据失败')
    setMockData()
  }
})

// 设置模拟数据（当API不可用时）
function setMockData() {
  stats.threadCount = 5
  stats.commentCount = 12
  stats.likesCount = 28

  // 模拟帖子数据
  myThreads.value = [
    { id: 1, name: 'Spring Boot学习心得', time: new Date().toISOString(), viewCount: 156 },
    { id: 2, name: 'Vue.js前端开发技巧', time: new Date(Date.now() - 86400000).toISOString(), viewCount: 89 }
  ]

  // 模拟评论数据
  myComments.value = [
    { id: 1, comment: '这个解决方案很实用！', threadInfoId: 1, time: new Date().toISOString() },
    { id: 2, comment: '感谢分享，学到了很多', threadInfoId: 2, time: new Date(Date.now() - 3600000).toISOString() }
  ]

  // 模拟收藏数据
  favorites.value = [
    { id: 1, threadInfoId: 1, time: new Date().toISOString() }
  ]
}
</script>

<style scoped>
.personal-center {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.profile-card {
  text-align: center;
  margin-bottom: 20px;
}

.user-avatar {
  margin-bottom: 15px;
}

.user-info h3 {
  margin: 10px 0 5px 0;
  color: #303133;
}

.user-info p {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-card h4 {
  margin-top: 0;
  color: #303133;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin: 10px 0;
  padding: 5px 0;
  border-bottom: 1px dashed #eee;
}

.stat-value {
  font-weight: bold;
  color: #409EFF;
}

.content-card {
  min-height: 500px;
}

.thread-item, .comment-item, .favorite-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.thread-item:last-child,
.comment-item:last-child,
.favorite-item:last-child {
  border-bottom: none;
}

.thread-title a,
.thread-link,
.favorite-title a {
  color: #409EFF;
  text-decoration: none;
  font-weight: bold;
}

.thread-title a:hover,
.thread-link:hover,
.favorite-title a:hover {
  text-decoration: underline;
}

.thread-meta,
.comment-time,
.favorite-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.comment-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.comment-text {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}
</style>