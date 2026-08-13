<template>
  <div class="tieba-user-center">
    <!-- 顶部导航 -->
    <header class="tieba-header">
      <div class="header-inner">
        <div class="logo" @click="goHome">校园论坛</div>
        <div class="header-actions">
          <el-button type="primary" icon="el-icon-edit" @click="gotoupload">发帖</el-button>
          <div class="user-info">
            <img :src="userAvatar" class="avatar" @error="handleAvatarError">
            <span class="nickname">{{ userInfo.nickname || '用户' }}</span>
          </div>
        </div>
      </div>
    </header>

    <div class="page-container">
      <!-- 用户信息卡片 -->
      <div class="user-profile">
        <div class="profile-cover">
          <div class="cover-pattern"></div>
        </div>
        <div class="profile-content">
          <div class="profile-avatar">
            <img :src="userAvatar" @error="handleAvatarError">
            <el-button class="edit-avatar-btn" size="small" icon="el-icon-camera" @click="openEditDialog"></el-button>
            <div class="avatar-ring"></div>
          </div>
          <div class="profile-info">
            <h1>{{ userInfo.nickname || '用户' }}</h1>
            <div class="profile-badges">
              <el-tag type="warning" size="small" effect="dark">
                <i class="el-icon-medal"></i> Lv.{{ userInfo.level || 1 }}
              </el-tag>
              <el-tag :type="userInfo.sex === '男' ? 'primary' : 'danger'" size="small" effect="plain">
                <i class="el-icon-male" v-if="userInfo.sex === '男'"></i>
                <i class="el-icon-female" v-else></i>
                {{ userInfo.sex || '未设置' }}
              </el-tag>
              <el-tag type="success" size="small" effect="plain">
                <i class="el-icon-coordinate"></i> 注册用户
              </el-tag>
            </div>
            <div class="profile-meta">
              <span><i class="el-icon-calendar"></i> 注册于 {{ formatRegisterTime(userInfo.registerTime) }}</span>
            </div>
            <div class="profile-follow">
              <span class="follow-stat" @click="showFollowers = true">
                <strong>{{ followingCount }}</strong> 关注
              </span>
              <span class="follow-stat" @click="showFollowers = true">
                <strong>{{ followerCount }}</strong> 粉丝
              </span>
            </div>
          </div>
          <div class="profile-actions">
            <el-button :type="todaySigned ? 'info' : 'warning'" :icon="todaySigned ? 'el-icon-check' : 'el-icon-calendar'" @click="handleSign" :loading="signing">
              {{ todaySigned ? '已签到' : '签到' }}
            </el-button>
            <el-button type="primary" icon="el-icon-edit" @click="openEditDialog">编辑资料</el-button>
          </div>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="stats-grid">
        <div class="stat-card" @click="goTo('/user/myread')">
          <div class="stat-icon reading">
            <i class="el-icon-reading"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ readCount }}</span>
            <span class="stat-label">浏览记录</span>
          </div>
        </div>
        <div class="stat-card" @click="goTo('/user/mycollect')">
          <div class="stat-icon collect">
            <i class="el-icon-star-on"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ collectCount }}</span>
            <span class="stat-label">我的收藏</span>
          </div>
        </div>
        <div class="stat-card" @click="goToMyThreads">
          <div class="stat-icon threads">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ threadCount }}</span>
            <span class="stat-label">发帖数</span>
          </div>
        </div>
        <div class="stat-card" @click="goToMyComments">
          <div class="stat-icon comments">
            <i class="el-icon-chat-line-round"></i>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ myCommentCount }}</span>
            <span class="stat-label">评论数</span>
          </div>
        </div>
      </div>

      <!-- 经验值进度条 -->
      <div class="exp-progress-card" v-if="userInfo.level">
        <div class="exp-header">
          <span class="exp-title">经验值</span>
          <div class="exp-stats">
            <span class="exp-points"><i class="el-icon-coin"></i> 积分: {{ userInfo.points || 0 }}</span>
            <span class="exp-value">{{ userInfo.experience || 0 }} / {{ getNextLevelExp() }}</span>
          </div>
        </div>
        <el-progress 
          :percentage="getExpPercentage()" 
          :stroke-width="10"
          color="linear-gradient(90deg, #667eea, #764ba2)"
          :show-text="false"
        ></el-progress>
        <div class="exp-hint">再获取 {{ Math.max(0, getNextLevelExp() - (userInfo.experience || 0)) }} 经验值升级到 Lv.{{ (userInfo.level || 1) + 1 }}</div>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-section">
        <h2><i class="el-icon-menu"></i> 功能菜单</h2>
        <div class="menu-grid">
          <div class="menu-card" @click="goTo('/user/myread')">
            <div class="menu-icon">
              <i class="el-icon-time"></i>
            </div>
            <span class="menu-title">浏览记录</span>
            <span class="menu-desc">查看历史浏览</span>
          </div>
          <div class="menu-card" @click="goTo('/user/mycollect')">
            <div class="menu-icon">
              <i class="el-icon-star-on"></i>
            </div>
            <span class="menu-title">我的收藏</span>
            <span class="menu-desc">管理收藏帖子</span>
          </div>
          <div class="menu-card" @click="openEditDialog">
            <div class="menu-icon">
              <i class="el-icon-user"></i>
            </div>
            <span class="menu-title">修改资料</span>
            <span class="menu-desc">编辑个人信息</span>
          </div>
          <div class="menu-card" @click="gotoupload">
            <div class="menu-icon">
              <i class="el-icon-edit"></i>
            </div>
            <span class="menu-title">发布帖子</span>
            <span class="menu-desc">发表新内容</span>
          </div>
        </div>
      </div>

      <!-- 我的评论预览 -->
      <div class="recent-section" v-if="commentList.length > 0">
        <div class="section-header">
          <h2><i class="el-icon-chat-line-round"></i> 我的评论</h2>
          <el-button type="text" @click="goToMyComments">查看更多 <i class="el-icon-arrow-right"></i></el-button>
        </div>
        <div class="comment-list">
          <div class="comment-item" v-for="item in commentList.slice(0, 3)" :key="item.id" @click="goToThread(item.threadInfo?.id)">
            <div class="comment-avatar">
              <img :src="userAvatar" @error="handleAvatarError">
            </div>
            <div class="comment-content">
              <div class="comment-thread">评论了: {{ item.threadInfo?.name || '未知帖子' }}</div>
              <div class="comment-text">{{ item.comment || '' }}</div>
              <div class="comment-time">{{ formatTime(item.time) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近浏览预览 -->
      <div class="recent-section" v-if="readList.length > 0">
        <div class="section-header">
          <h2><i class="el-icon-reading"></i> 最近浏览</h2>
          <el-button type="text" @click="goTo('/user/myread')">查看更多 <i class="el-icon-arrow-right"></i></el-button>
        </div>
        <div class="recent-list">
          <div class="recent-item" v-for="item in readList.slice(0, 4)" :key="item.id" @click="goToThread(item.threadInfo?.id)">
            <div class="item-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="item-content">
              <span class="item-title">{{ item.threadInfo?.name || '未知帖子' }}</span>
              <span class="item-time">{{ formatTime(item.time) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 成就展示 -->
      <div class="achievements-section">
        <h2><i class="el-icon-trophy"></i> 成就徽章 <span class="badge-count">{{ earnedBadgeCount }}/{{ allBadgeTypes.length }}</span></h2>
        <div class="achievements-grid" v-if="allBadgeTypes.length > 0">
          <div
            class="achievement-item"
            :class="{ earned: isBadgeEarned(badge.id), locked: !isBadgeEarned(badge.id) }"
            v-for="badge in allBadgeTypes"
            :key="badge.id"
          >
            <div class="badge-icon-wrapper">
              <i :class="badge.icon || 'el-icon-medal'"></i>
            </div>
            <span class="badge-name">{{ badge.name }}</span>
            <span class="achieve-desc">{{ badge.description }}</span>
            <span class="badge-reward" v-if="badge.expReward > 0 || badge.pointsReward > 0">
              +{{ badge.expReward }}经验 +{{ badge.pointsReward }}积分
            </span>
          </div>
        </div>
        <div class="achievements-grid" v-else>
          <div class="achievement-item locked">
            <div class="badge-icon-wrapper">
              <i class="el-icon-star-off"></i>
            </div>
            <span class="badge-name">暂无徽章</span>
            <span class="achieve-desc">快去发帖、评论、签到吧！</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 关注/粉丝弹窗 -->
    <el-dialog title="关注与粉丝" v-model="showFollowers" width="420px">
      <el-tabs v-model="followTab">
        <el-tab-pane label="关注" name="following">
          <div class="follow-list" v-if="followingList.length > 0">
            <div class="follow-user" v-for="u in followingList" :key="u.id">
              <span>{{ u.nickname || u.username }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无关注" />
        </el-tab-pane>
        <el-tab-pane label="粉丝" name="follower">
          <div class="follow-list" v-if="followerList.length > 0">
            <div class="follow-user" v-for="u in followerList" :key="u.id">
              <span>{{ u.nickname || u.username }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无粉丝" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 编辑资料对话框 -->
    <el-dialog title="修改资料" v-model="editDialogVisible" width="500px" class="edit-dialog" @close="handleDialogClose">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-upload
              class="avatar-uploader"
              :action="VERCODE_URL + '/upload'"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :data="uploadParams"
              accept=".jpg,.jpeg,.png"
            >
              <img v-if="editForm.head" :src="VERCODE_URL + '/upload/' + editForm.head" class="avatar-preview">
              <div v-else class="upload-placeholder">
                <i class="el-icon-plus"></i>
                <span>上传头像</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUserInfo" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js'
import { formatRelativeTime } from '@/utils/timeFormat.js';
import { ref, reactive, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg=='

const userInfo = ref({})
const userAvatar = ref(defaultAvatar)
const readList = ref([])
const collectList = ref([])
const commentList = ref([])
const editDialogVisible = ref(false)
const saving = ref(false)
const todaySigned = ref(false)
const signing = ref(false)
const editForm = reactive({
  nickname: '',
  sex: '男',
  head: ''
})
const uploadParams = { type: 'common' }
const followingCount = ref(0)
const followerCount = ref(0)
const showFollowers = ref(false)
const followTab = ref('following')
const followingList = ref([])
const followerList = ref([])

onMounted(() => {
  loadUserData()
})

onActivated(() => {
  loadUserData()
})

const handleSign = async () => {
  if (todaySigned.value) {
    ElMessage.info('今日已签到')
    return
  }
  signing.value = true
  try {
    const res = await axios.post('/api/sign', { userId: parseInt(sessionStorage.id) })
    if (res.data && res.data.status === 200) {
      todaySigned.value = true
      const data = res.data.data || {}
      ElMessage.success(data.msg || '签到成功！')
      loadUserData()
    } else {
      ElMessage.error((res.data && res.data.msg) || '签到失败')
    }
  } catch (e) {
    ElMessage.error('签到失败，请稍后重试')
  } finally {
    signing.value = false
  }
}

const openEditDialog = () => {
  editForm.nickname = userInfo.value.nickname || ''
  editForm.sex = userInfo.value.sex || '男'
  editForm.head = userInfo.value.head || ''
  editDialogVisible.value = true
}

const handleDialogClose = () => {
  editDialogVisible.value = false
}

const loadUserData = async () => {
  if (!sessionStorage.id) {
    router.push('/login')
    return
  }
  
  try {
    const userRes = await axios.get('/api/users/one', { params: { id: sessionStorage.id } }).catch(() => ({ data: null }))
    if (userRes.data) {
      userInfo.value = userRes.data.data || userRes.data
      if (userInfo.value.head) {
        userAvatar.value = VERCODE_URL + '/upload/' + userInfo.value.head
      }
    }
  } catch (e) {
    console.error('加载用户失败:', e)
  }
  
  try {
    const readRes = await axios.get('/api/userread/one', { params: { id: sessionStorage.id } }).catch(() => ({ data: [] }))
    if (readRes.data) {
      readList.value = readRes.data
    }
  } catch (e) {}
  
  try {
    const collectRes = await axios.get('/api/usercollect/one', { params: { id: sessionStorage.id } }).catch(() => ({ data: [] }))
    if (collectRes.data) {
      collectList.value = collectRes.data
    }
  } catch (e) {}

  try {
    const commentRes = await axios.get('/api/comments/byuser', { params: { id: sessionStorage.id } }).catch(() => ({ data: [] }))
    if (commentRes.data) {
      commentList.value = commentRes.data
    }
  } catch (e) {
    console.error('加载评论失败:', e)
  }

  try {
    const threadRes = await axios.get('/api/threads/byuser', { params: { writerId: sessionStorage.id } })
    threadCount.value = (threadRes.data || []).length
    console.log('帖子数:', threadCount.value)
  } catch (e) {
    console.error('加载帖子数失败:', e)
  }

  try {
    const signRes = await axios.get('/api/sign/status', { params: { userId: sessionStorage.id } })
    todaySigned.value = signRes.data && signRes.data.data === true
  } catch (e) {}

  loadBadges()
  loadFollowStats()
}

const loadFollowStats = async () => {
  try {
    const uid = sessionStorage.id
    const [fingRes, ferRes] = await Promise.all([
      axios.get('/api/follow/followinglist/' + uid).catch(() => ({ data: [] })),
      axios.get('/api/follow/followerlist/' + uid).catch(() => ({ data: [] }))
    ])
    followingList.value = fingRes.data || []
    followerList.value = ferRes.data || []
    followingCount.value = followingList.value.length
    followerCount.value = followerList.value.length
  } catch (e) {}
}

const readCount = computed(() => readList.value.length)
const collectCount = computed(() => {
  const uniqueIds = new Set(collectList.value.map(e => e.threadInfo?.id).filter(Boolean))
  return uniqueIds.size
})

const threadCount = ref(0)
const myCommentCount = computed(() => commentList.value.length || 0)
const badgeList = ref([])
const allBadgeTypes = ref([])
const earnedBadgeIds = ref(new Set())

const earnedBadgeCount = computed(() => earnedBadgeIds.value.size)

const isBadgeEarned = (badgeTypeId) => earnedBadgeIds.value.has(badgeTypeId)

const loadBadges = async () => {
  try {
    const badgeRes = await axios.get('/api/badge/user/' + sessionStorage.id)
    if (badgeRes.data && badgeRes.data.data) {
      badgeList.value = badgeRes.data.data
      earnedBadgeIds.value = new Set(badgeRes.data.data.map(b => b.badgeTypeId))
    }
  } catch (e) {
    console.error('加载徽章失败:', e)
  }

  try {
    const allRes = await axios.get('/api/badge/all')
    if (allRes.data && allRes.data.data) {
      allBadgeTypes.value = allRes.data.data
    }
  } catch (e) {
    console.error('加载徽章类型失败:', e)
  }
}

const loadData = async () => {
  try {
    const userId = sessionStorage.id
    if (!userId) return

    const threadRes = await axios.get('/api/threads/byuser', { params: { writerId: userId } })
    threadCount.value = (threadRes.data || []).length
  } catch (e) {
    console.error('加载帖子数失败:', e)
  }
}

const getNextLevelExp = () => {
  const level = userInfo.value.level || 1
  return level * 100
}

const getExpPercentage = () => {
  const exp = userInfo.value.experience || 0
  const maxExp = getNextLevelExp()
  return Math.min(100, Math.round((exp / maxExp) * 100))
}

const handleAvatarError = (e) => {
  e.target.src = defaultAvatar
}

const handleUploadSuccess = (res) => {
  if (res.status === 200 || res.status === true) {
    editForm.head = res.data
    userAvatar.value = VERCODE_URL + '/upload/' + res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const saveUserInfo = async () => {
  if (!editForm.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  
  saving.value = true
  try {
    const updateData = {
      id: parseInt(sessionStorage.id),
      nickname: editForm.nickname,
      sex: editForm.sex,
      head: editForm.head || ''
    }
    const res = await axios.post('/api/users/update', updateData, {
      headers: { 'Content-Type': 'application/json' }
    })
    if (res.data && (res.data.status === 200 || res.data.status === true)) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      loadUserData()
    } else {
      ElMessage.error(res.data?.msg || '保存失败')
    }
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const formatTime = (time) => formatRelativeTime(time);

const formatRegisterTime = (time) => {
  if (!time) return '未知'
  try {
    const date = new Date(time)
    if (isNaN(date.getTime())) return '未知'
    return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  } catch (e) {
    return '未知'
  }
}

function goTo(path) {
  router.push(path)
}

function goToThread(id) {
  if (id) router.push(`/thread/read?id=${id}`)
}

function goHome() {
  router.push('/')
}

function gotoupload() {
  router.push('/uploadthread')
}

function goToMyThreads() {
  router.push('/user/mythreads')
}

function goToMyComments() {
  router.push('/user/myscomments')
}
</script>

<style lang="less" scoped>
.tieba-user-center {
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
}

.user-profile {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;

  .profile-cover {
    height: 140px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    position: relative;
    overflow: hidden;
    
    .cover-pattern {
      position: absolute;
      width: 100%;
      height: 100%;
      background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
      opacity: 0.5;
    }
  }

  .profile-content {
    display: flex;
    align-items: flex-end;
    padding: 0 30px 25px;
    margin-top: -60px;
    position: relative;

    .profile-avatar {
      position: relative;
      margin-right: 25px;

      img {
        width: 110px;
        height: 110px;
        border-radius: 50%;
        border: 5px solid #fff;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        object-fit: cover;
      }

      .edit-avatar-btn {
        position: absolute;
        bottom: 5px;
        right: 5px;
        width: 32px;
        height: 32px;
        border-radius: 50%;
        padding: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #667eea, #764ba2);
        border: 2px solid #fff;
        color: #fff;
      }
      
      .avatar-ring {
        position: absolute;
        top: -5px;
        left: -5px;
        right: -5px;
        bottom: -5px;
        border-radius: 50%;
        border: 3px solid #667eea;
        opacity: 0.3;
      }
    }

    .profile-info {
      flex: 1;
      padding-top: 15px;

      h1 {
        margin: 0 0 12px;
        font-size: 26px;
        color: #303133;
      }

      .profile-badges {
        display: flex;
        gap: 8px;
        margin-bottom: 8px;
      }
      
      .profile-meta {
        font-size: 13px;
        color: #909399;
        
        i {
          margin-right: 4px;
        }
      }

      .profile-follow {
        display: flex;
        gap: 20px;
        margin-top: 8px;

        .follow-stat {
          font-size: 13px;
          color: #909399;
          cursor: pointer;
          transition: color 0.2s;

          &:hover {
            color: #667eea;
          }

          strong {
            font-size: 16px;
            color: #303133;
            margin-right: 2px;
          }
        }
      }
    }
    
    .profile-actions {
      .el-button {
        background: linear-gradient(135deg, #667eea, #764ba2);
        border: none;
      }
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 20px;

  .stat-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 15px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 24px rgba(102, 126, 234, 0.2);
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      i {
        font-size: 26px;
        color: #fff;
      }
      
      &.reading { background: linear-gradient(135deg, #667eea, #764ba2); }
      &.collect { background: linear-gradient(135deg, #e6a23c, #f56c6c); }
      &.threads { background: linear-gradient(135deg, #67c23a, #85ce61); }
      &.comments { background: linear-gradient(135deg, #409eff, #66b1ff); }
    }

    .stat-info {
      display: flex;
      flex-direction: column;

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
      }

      .stat-label {
        font-size: 13px;
        color: #909399;
      }
    }
  }
}

.exp-progress-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .exp-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    
    .exp-title {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }

    .exp-stats {
      display: flex;
      align-items: center;
      gap: 16px;

      .exp-points {
        font-size: 13px;
        color: #e6a23c;
        font-weight: 500;

        i {
          margin-right: 2px;
        }
      }

      .exp-value {
        font-size: 14px;
        color: #667eea;
        font-weight: 600;
      }
    }
  }
  
  .exp-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }
}

.menu-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  h2 {
    margin: 0 0 20px;
    font-size: 18px;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      color: #667eea;
    }
  }

  .menu-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 15px;

    .menu-card {
      padding: 20px;
      background: #f5f8fc;
      border-radius: 12px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
        transform: translateY(-3px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.15);

        .menu-icon {
          transform: scale(1.1);
        }
      }

      .menu-icon {
        width: 52px;
        height: 52px;
        margin: 0 auto 14px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-radius: 14px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: transform 0.3s;

        i {
          font-size: 24px;
          color: #fff;
        }
      }

      .menu-title {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 5px;
      }

      .menu-desc {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.recent-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    h2 {
      margin: 0;
      font-size: 18px;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        color: #667eea;
      }
    }
  }

  .recent-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .recent-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px;
      background: #f5f8fc;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #e8ecf1;
      }

      .item-icon {
        width: 40px;
        height: 40px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;

        i {
          font-size: 18px;
          color: #fff;
        }
      }

      .item-content {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;

        .item-title {
          font-size: 14px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .item-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .comment-list {
    .comment-item {
      display: flex;
      gap: 12px;
      padding: 14px;
      background: #f5f8fc;
      border-radius: 10px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #e8ecf1;
      }
      
      &:last-child {
        margin-bottom: 0;
      }

      .comment-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        overflow: hidden;
        flex-shrink: 0;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .comment-content {
        flex: 1;
        min-width: 0;

        .comment-thread {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
        }

        .comment-text {
          font-size: 14px;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin-bottom: 4px;
        }
        
        .comment-time {
          font-size: 12px;
          color: #c0c4cc;
        }
      }
    }
  }
}

.achievements-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  h2 {
    margin: 0 0 20px;
    font-size: 18px;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      color: #e6a23c;
    }

    .badge-count {
      font-size: 13px;
      font-weight: 400;
      color: #909399;
      margin-left: 4px;
    }
  }

  .achievements-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 12px;

    .achievement-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 14px 10px;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
      border-radius: 12px;
      text-align: center;
      transition: all 0.3s;
      position: relative;

      &.earned {
        background: linear-gradient(135deg, rgba(230, 162, 60, 0.12), rgba(245, 108, 108, 0.12));
        border: 1px solid rgba(230, 162, 60, 0.3);

        .badge-icon-wrapper i {
          color: #e6a23c;
        }
      }

      &.locked {
        opacity: 0.45;

        .badge-icon-wrapper {
          background: #f0f2f5;

          i {
            color: #c0c4cc;
          }
        }

        .badge-name {
          color: #c0c4cc;
        }
      }

      .badge-icon-wrapper {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 8px;
        background: linear-gradient(135deg, rgba(230, 162, 60, 0.15), rgba(245, 108, 108, 0.15));

        i {
          font-size: 22px;
          color: #e6a23c;
        }
      }

      .badge-name {
        font-size: 12px;
        color: #303133;
        font-weight: 600;
        margin-bottom: 2px;
      }

      .achieve-desc {
        font-size: 10px;
        color: #909399;
        line-height: 1.3;
      }

      .badge-reward {
        font-size: 10px;
        color: #67c23a;
        margin-top: 4px;
        font-weight: 500;
      }
    }
  }
}

.avatar-upload {
  .avatar-uploader {
    width: 100px;
    height: 100px;
    border: 2px dashed #ddd;
    border-radius: 50%;
    cursor: pointer;
    overflow: hidden;

    &:hover {
      border-color: #667eea;
    }

    .avatar-preview {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .upload-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;

      i {
        font-size: 24px;
        margin-bottom: 4px;
      }

      span {
        font-size: 12px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.follow-list {
  max-height: 400px;
  overflow-y: auto;

  .follow-user {
    padding: 12px 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--border-color, #f0f0f0);
    font-size: 14px;
    color: var(--text-primary, #303133);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .menu-grid {
    grid-template-columns: repeat(2, 1fr) !important;
  }

  .recent-list {
    grid-template-columns: 1fr !important;
  }
  
  .achievements-grid {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  
  .user-profile .profile-content {
    flex-direction: column;
    align-items: center;
    text-align: center;
    margin-top: -40px;
    padding: 0 20px 20px;
    
    .profile-avatar {
      margin-right: 0;
      margin-bottom: 10px;
    }
    
    .profile-actions {
      margin-top: 15px;
    }
  }
}
</style>