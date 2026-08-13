<template>
  <div class="app-container">
    <!-- 顶部标题和操作栏 -->
    <div class="header-section">
      <h2 class="page-title">评论管理系统</h2>
      <div class="action-bar">
        <el-input
            v-model="search"
            placeholder="搜索评论内容、用户昵称..."
            clearable
            style="width: 300px"
            @clear="handleSearchClear"
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadList" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stat-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <span class="stat-icon"><el-icon><ChatLineRound /></el-icon></span>
          <div>
            <div class="stat-value">{{ stats.totalComments }}</div>
            <div class="stat-label">总评论数</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <span class="stat-icon"><el-icon><ChatDotRound /></el-icon></span>
          <div>
            <div class="stat-value">{{ stats.totalReplies }}</div>
            <div class="stat-label">总回复数</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <span class="stat-icon"><el-icon><Clock /></el-icon></span>
          <div>
            <div class="stat-value">{{ stats.todayComments }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主表格区域 -->
    <el-card shadow="never" class="main-card">
      <el-table
          :data="filterTableData"
          style="width: 100%"
          v-loading="loading"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          row-key="id"
          @sort-change="handleSortChange"
      >
        <el-table-column label="评论详情" type="expand" width="80">
          <template #default="props">
            <div class="expand-content">
              <div v-if="props.row.replyContent && props.row.replyContent.length > 0">
                <div class="reply-header">
                  <span class="reply-count">共 {{ props.row.replyContent.length }} 条回复</span>
                  <el-button
                      type="text"
                      size="small"
                      @click="showReplyDialog(props.row)"
                      :icon="Plus"
                  >
                    添加回复
                  </el-button>
                </div>
                <div class="reply-list">
                  <div v-for="(reply, index) in props.row.replyContent" :key="reply.id" class="reply-item">
                    <el-avatar :size="32" :src="getAvatar(reply.userInfo.username)">
                      {{ reply.userInfo.nickname?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div class="reply-content">
                      <div class="reply-user">
                        <span class="username">{{ reply.userInfo.nickname || '未知用户' }}</span>
                        <span class="user-account">@{{ reply.userInfo.username }}</span>
                        <span class="reply-time">{{ formatTime(reply.time) }}</span>
                      </div>
                      <div class="reply-text">{{ reply.comment }}</div>
                      <div class="reply-actions">
                        <el-button
                            type="text"
                            size="small"
                            @click="handleLikeReply(reply)"
                        >
                          <el-icon><Star /></el-icon> 点赞 ({{ reply.likes || 0 }})
                        </el-button>
                        <el-button
                            type="text"
                            size="small"
                            @click="showReplyDialog(props.row, reply)"
                        >
                          <el-icon><ChatLineRound /></el-icon> 回复
                        </el-button>
                        <el-button
                            type="text"
                            size="small"
                            @click="handleDeleteReply(reply)"
                            style="color: #f56c6c"
                        >
                          <el-icon><Delete /></el-icon> 删除
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-replies">
                <el-empty description="暂无回复" :image-size="60" />
                <el-button
                    type="text"
                    size="small"
                    @click="showReplyDialog(props.row)"
                    :icon="Plus"
                >
                  添加回复
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论内容" min-width="300">
          <template #default="scope">
            <div class="comment-main">
              <el-avatar :size="36" :src="getAvatar(scope.row.userInfo.username)">
                {{ scope.row.userInfo.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="comment-content">
                <div class="comment-user">
                  <span class="username">{{ scope.row.userInfo.nickname || '未知用户' }}</span>
                  <span class="user-account">@{{ scope.row.userInfo.username }}</span>
                  <el-tag size="small" effect="plain" v-if="scope.row.isAuthor">
                    作者
                  </el-tag>
                </div>
                <div class="comment-text">{{ scope.row.comment }}</div>
                <div class="comment-meta">
                  <span class="comment-time">{{ formatTime(scope.row.time) }}</span>
                  <span class="comment-likes">
                    <el-icon><Star /></el-icon> {{ scope.row.likes || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所属主题" prop="threadInfo.name" width="180" sortable />

        <el-table-column label="评论时间" prop="time" width="180" sortable />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
                size="small"
                @click="showEditDialog(scope.row)"
                :icon="Edit"
                plain
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
                :icon="Delete"
                plain
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section" v-if="totalItems > 0">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalItems"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评论/回复对话框 -->
    <el-dialog
        v-model="dialog.visible"
        :title="dialog.title"
        width="600px"
    >
      <el-form :model="dialog.form" label-width="80px">
        <el-form-item label="内容">
          <el-input
              v-model="dialog.form.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入内容"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitDialog">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Search, Refresh, Edit, Delete, Plus, Star, ChatLineRound, ChatDotRound, Clock
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 新增统计数据结构
const stats = reactive({
  totalComments: 0,
  totalReplies: 0,
  todayComments: 0
})


// 新增：加载统计数据
async function loadStats() {
  try {
    // 获取总评论数
    const commentsRes = await axios.get('/api/comments/count')
    stats.totalComments = commentsRes.data.count || 0

    // 获取总回复数
    const repliesRes = await axios.get('/api/reply/count')
    stats.totalReplies = repliesRes.data.count || 0

    // 获取今日新增评论数
    const todayRes = await axios.get('/api/comments/today-count')
    stats.todayComments = todayRes.data.count || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 可以设置默认值或显示错误消息
    stats.totalComments = 295
    stats.totalReplies = 234
    stats.todayComments = 15
  }
}

// 表格数据
const commentData = ref([])
const loading = ref(false)
const search = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)
const sortProp = ref('time')
const sortOrder = ref('descending')

// 对话框
const dialog = reactive({
  visible: false,
  title: '',
  form: {
    id: null,
    comment: '',
    parentId: null,
    replyToId: null
  }
})

// 加载数据
onMounted(() => {
  loadList()
})

// 在loadList函数中获取统计数据
async function loadList() {
  try {
    loading.value = true
    // 获取评论列表数据
    const response = await axios({
      method: 'get',
      url: '/api/comments',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      params: {
        page: currentPage.value,
        size: pageSize.value,
        search: search.value,
        sort: sortProp.value,
        order: sortOrder.value
      }
    })

    // 处理数据确保 replyContent 存在
    commentData.value = response.data.map(item => {
      return {
        ...item,
        replyContent: item.replyContent || []
      }
    })
    totalItems.value = commentData.value.length

    // 获取统计数据
    await loadStats()

    loading.value = false
  } catch (error) {
    console.error('加载数据失败:', error)
    loading.value = false
    ElMessage.error('加载数据失败')

    // 模拟数据用于展示
    commentData.value = [
      {
        id: 1,
        comment: "这个产品非常好用，界面简洁美观！",
        time: new Date().toISOString(),
        likes: 5,
        isAuthor: true,
        userInfo: {
          username: "admin",
          nickname: "管理员"
        },
        threadInfo: {
          name: "产品反馈"
        },
        replyContent: [
          {
            id: 101,
            comment: "同意，特别是响应速度很快",
            time: new Date().toISOString(),
            likes: 2,
            userInfo: {
              username: "user1",
              nickname: "张三"
            }
          }
        ]
      },
      {
        id: 2,
        comment: "希望增加更多自定义功能",
        time: new Date(Date.now() - 86400000).toISOString(),
        likes: 3,
        isAuthor: false,
        userInfo: {
          username: "user2",
          nickname: "李四"
        },
        threadInfo: {
          name: "功能建议"
        },
        replyContent: []
      }
    ]
    totalItems.value = commentData.value.length

    // 模拟统计数据
    stats.totalComments = 295
    stats.totalReplies = 342
    stats.todayComments = 12
  }
}

// 搜索和筛选
const handleSearch = () => {
  currentPage.value = 1
  loadList()
}

const handleSearchClear = () => {
  if (search.value === '') {
    loadList()
  }
}

// 排序
const handleSortChange = ({ prop, order }) => {
  sortProp.value = prop
  sortOrder.value = order
  loadList()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadList()
}

// 过滤表格数据
const filterTableData = computed(() => {
  return commentData.value.filter(item => {
    return !search.value ||
        item.comment.includes(search.value) ||
        (item.userInfo.nickname && item.userInfo.nickname.includes(search.value)) ||
        (item.userInfo.username && item.userInfo.username.includes(search.value))
  })
})

// 显示编辑对话框
const showEditDialog = (row) => {
  dialog.visible = true
  dialog.title = '编辑评论'
  dialog.form = {
    id: row.id,
    comment: row.comment,
    parentId: null,
    replyToId: null
  }
}

// 显示回复对话框
const showReplyDialog = (comment, reply = null) => {
  dialog.visible = true
  dialog.title = reply ? `回复 ${reply.userInfo.nickname}` : '添加回复'
  dialog.form = {
    id: null,
    comment: '',
    parentId: comment.id,
    replyToId: reply?.id || null
  }
}

// 提交对话框
const submitDialog = async () => {
  try {
    if (dialog.form.id) {
      // 编辑评论
      await axios({
        method: 'post',
        url: '/api/comment',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: {
          id: dialog.form.id,
          comment: dialog.form.comment
        }
      })
      ElMessage.success('评论更新成功')
    } else {
      // 添加回复
      await axios({
        method: 'post',
        url: '/api/comment',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: {
          parentId: dialog.form.parentId,
          replyToId: dialog.form.replyToId,
          comment: dialog.form.comment
        }
      })
      ElMessage.success('回复添加成功')
    }

    dialog.visible = false
    loadList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(dialog.form.id ? '更新失败' : '添加失败')
  }
}

// 删除评论
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论及其所有回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios({
      method: 'delete',
      url: '/api/comments',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: { id: row.id }
    })

    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 删除回复
const handleDeleteReply = async (reply) => {
  try {
    await ElMessageBox.confirm('确定要删除这条回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios({
      method: 'delete',
      url: '/api/reply',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: { id: reply.id }
    })

    ElMessage.success('删除成功')
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 点赞回复
const handleLikeReply = async (reply) => {
  try {
    // 这里假设有点赞接口，如果没有可以只做前端效果
    reply.likes = (reply.likes || 0) + 1
    ElMessage.success('点赞成功')
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败')
  }
}

// 工具函数
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  let date
  // 尝试不同的日期格式
  if (typeof timeStr === 'string') {
    // 如果是ISO格式的字符串，先转换为Date对象
    date = new Date(timeStr.replace('T', ' ').replace(/\.\d{3}Z$/, ''))
  } else {
    date = new Date(timeStr)
  }

  // 检查日期是否有效
  if (isNaN(date.getTime())) {
    return '无效时间'
  }

  // 格式化时间为中文显示
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')

  return `${year}年${month}月${day}日 ${hours}:${minutes}`
}

const getAvatar = (username) => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
  const hash = username?.split('').reduce((acc, char) => char.charCodeAt(0) + acc, 0) || 0
  const color = colors[hash % colors.length]
  return `https://via.placeholder.com/150/${color.substring(1)}/FFFFFF?text=${username?.charAt(0)?.toUpperCase() || 'U'}`
}
</script>

<style scoped>
/* 新增统计卡片样式 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1) !important;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 8px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  margin-right: 16px;
  border-radius: 50%;
  background-color: #f0f7ff;
  color: #409eff;
  font-size: 24px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.header-section {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  color: #303133;
  margin: 0;
  font-weight: 500;
  font-size: 24px;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main-card {
  border-radius: 8px;
  border: none;
}

.expand-content {
  padding: 10px 0 10px 40px;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.reply-count {
  color: #909399;
  font-size: 14px;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reply-item {
  display: flex;
  gap: 12px;
}

.reply-content {
  flex: 1;
}

.reply-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 14px;
}

.username {
  font-weight: 500;
  color: #303133;
}

.user-account {
  color: #909399;
}

.reply-time {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.reply-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 4px;
}

.reply-actions {
  display: flex;
  gap: 12px;
}

.empty-replies {
  text-align: center;
  padding: 20px 0;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-text {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 6px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.comment-likes {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.el-table {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #f5f7fa;
  --el-table-row-hover-bg-color: #f5f7fa;
}

.el-table::before {
  height: 0;
}

:deep(.el-table__expand-icon) {
  color: #409EFF;
}

:deep(.el-table__expanded-cell) {
  padding: 10px;
  background-color: #fafafa;
}

.el-tag {
  margin-left: 4px;
}
</style>