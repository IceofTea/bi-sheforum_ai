<template>
  <div class="tieba-read-history">
    <!-- 顶部导航 -->
    <header class="tieba-header">
      <div class="header-inner">
        <div class="logo" @click="goHome">校园论坛</div>
        <div class="nav-search">
          <el-input
            v-model="search"
            placeholder="搜索浏览记录..."
            prefix-icon="el-icon-search"
            @keyup.enter="handleSearch"
            clearable
          ></el-input>
        </div>
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
          <h2><i class="el-icon-time"></i> 浏览记录</h2>
          <span class="count">共 {{ filteredData.length }} 条记录</span>
        </div>
        <div class="header-right">
          <el-input
            v-model="search"
            placeholder="搜索帖子标题..."
            prefix-icon="el-icon-search"
            clearable
          ></el-input>
        </div>
      </div>

      <!-- 浏览记录列表 -->
      <div class="history-list">
        <div 
          v-for="item in displayData" 
          :key="item.id" 
          class="history-card"
          @click="gotoThread(item.threadInfo?.id)"
        >
          <div class="card-left">
            <div class="card-icon">
              <i class="el-icon-document"></i>
            </div>
          </div>
          <div class="card-body">
            <div class="card-header">
              <el-tag size="small" type="primary">{{ item.threadInfo?.threadsSort?.name || '默认' }}</el-tag>
              <h3 class="card-title">{{ item.threadInfo?.name || '未知帖子' }}</h3>
            </div>
            <div class="card-summary">{{ item.threadInfo?.introduction || '暂无内容' }}</div>
            <div class="card-footer">
              <span class="author"><i class="el-icon-user"></i> {{ item.threadInfo?.writer || '未知作者' }}</span>
              <span class="time"><i class="el-icon-time"></i> {{ formatTime(item.time) }}</span>
            </div>
          </div>
        </div>

        <div class="empty-state" v-if="displayData.length === 0">
          <i class="el-icon-reading"></i>
          <p>暂无浏览记录</p>
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
import { formatRelativeTime } from '@/utils/timeFormat.js';
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const threadlists = ref([])
const search = ref('')
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
    const res = await axios.get('/api/userread/one', { params: { id: sessionStorage.id } })
    threadlists.value = res.data || []
  } catch (e) {
    console.error('加载浏览记录失败', e)
  }
}

const formatTime = (time) => formatRelativeTime(time);

const handleSearch = () => {
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const filteredData = computed(() => {
  if (!search.value) return threadlists.value
  const kw = search.value.toLowerCase()
  return threadlists.value.filter(item => 
    item.threadInfo?.name?.toLowerCase().includes(kw) ||
    item.threadInfo?.writer?.toLowerCase().includes(kw)
  )
})

const displayData = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredData.value.slice(start, start + pageSize)
})

const totalPages = computed(() => Math.ceil(filteredData.value.length / pageSize))

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
.tieba-read-history {
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
    gap: 20px;
    padding: 0 20px;
  }
  
  .logo {
    font-size: 22px;
    font-weight: bold;
    color: #fff;
    cursor: pointer;
  }
  
  .nav-search {
    flex: 1;
    max-width: 300px;
    
    .el-input__wrapper {
      border-radius: 20px;
      box-shadow: none;
      background: rgba(255,255,255,0.2);
      
      input {
        background: transparent;
        color: #fff;
        
        &::placeholder {
          color: rgba(255,255,255,0.7);
        }
      }
    }
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
        color: #667eea;
      }
    }
    
    .count {
      font-size: 14px;
      color: #999;
    }
  }

  .header-right {
    .el-input {
      width: 250px;
    }
  }
}

.history-list {
  .history-card {
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
      box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
      border-left-color: #667eea;

      .card-title {
        color: #667eea;
      }
    }
    
    .card-left {
      .card-icon {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        
        i {
          font-size: 22px;
          color: #fff;
        }
      }
    }
    
    .card-body {
      flex: 1;
      min-width: 0;
      
      .card-header {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 8px;

        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          transition: color 0.3s;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      
      .card-summary {
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
        margin-bottom: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .card-footer {
        display: flex;
        gap: 20px;
        font-size: 12px;
        color: #909399;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
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