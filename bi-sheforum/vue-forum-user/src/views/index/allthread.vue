<template>
  <div class="tieba-list-page">
    <!-- 顶部导航 -->
    <header class="tieba-header">
      <div class="header-inner">
        <div class="logo" @click="goHome">校园论坛</div>
        <div class="nav-search">
          <el-input
            v-model="search"
            placeholder="搜索帖子..."
            prefix-icon="el-icon-search"
            @keyup.enter="handleSearch"
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
      <!-- 标题区 -->
      <div class="page-header">
        <div class="header-left">
          <h2>全部帖子</h2>
          <span class="count">共 {{ filteredData.length }} 篇</span>
        </div>
        <div class="header-right">
          <el-input
            v-model="search"
            placeholder="搜索标题、作者..."
            prefix-icon="el-icon-search"
            clearable
            @input="currentPage = 1"
          ></el-input>
        </div>
      </div>

      <!-- 筛选 -->
      <div class="filter-bar">
        <span 
          :class="['tab', { active: filterType === 'all' }]" 
          @click="filterType = 'all'"
        ><i class="el-icon-magic-stick"></i>综合推荐</span>
        <span 
          :class="['tab', { active: filterType === 'hot' }]" 
          @click="filterType = 'hot'"
        ><i class="el-icon-hot"></i>热门</span>
        <span 
          :class="['tab', { active: filterType === 'new' }]" 
          @click="filterType = 'new'"
        ><i class="el-icon-time"></i>最新</span>
      </div>

      <!-- 帖子流 -->
      <div class="thread-stream">
        <div 
          v-for="item in displayData" 
          :key="item.id" 
          class="thread-card"
          @click="toThreadDetail(item.id)"
        >
          <div class="card-body">
            <div class="card-header">
              <el-tag size="small" type="primary">{{ item.threadsSort?.name || '默认' }}</el-tag>
              <h3 class="card-title">{{ item.name }}</h3>
            </div>
            <div class="card-summary">{{ item.introduction || '暂无内容' }}</div>
            <div class="card-footer">
              <span class="author"><i class="el-icon-user"></i> {{ item.writer }}</span>
              <span class="time">{{ formatTime(item.createTime) }}</span>
              <span class="stats-item"><i class="el-icon-view"></i> <span class="stat-label">阅读</span><span class="stat-value">{{ item.views || 0 }}</span></span>
              <span class="stats-item"><i class="el-icon-chat-line-round"></i> <span class="stat-label">评论</span><span class="stat-value">{{ item.commentCount || 0 }}</span></span>
            </div>
          </div>
        </div>

        <div class="empty-state" v-if="displayData.length === 0">
          <i class="el-icon-folder-opened"></i>
          <p>暂无帖子</p>
          <el-button type="primary" @click="gotoupload">发布第一个帖子</el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="totalPages > 1">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="20"
          :total="filteredData.length"
          @current-change="handlePageChange"
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { formatRelativeTime } from '@/utils/timeFormat.js';

const router = useRouter();
const threadlists = ref([]);
const search = ref('');
const filterType = ref('all');
const currentPage = ref(1);
const pageSize = 20;
const userNickname = ref('游客');
let userAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==';

onMounted(() => {
  loadlist();
  loadUserInfo();
});

const loadUserInfo = async () => {
  if (sessionStorage.id) {
    try {
      const res = await axios.get('/api/users/one', { params: { id: sessionStorage.id } });
      if (res.data) {
        userNickname.value = res.data.nickname || '用户';
        if (res.data.head) {
          userAvatar = VERCODE_URL + '/upload/' + res.data.head;
        }
      }
    } catch (e) {}
  }
};

const loadlist = async () => {
  const res = await axios.get('/api/threads/all');
  threadlists.value = res.data || [];
};

const handleSearch = () => {
  currentPage.value = 1;
};

const handlePageChange = (page) => {
  currentPage.value = page;
};

const formatTime = (timestamp) => formatRelativeTime(timestamp);

const handleImageError = (event) => {
  event.target.style.display = 'none';
  event.target.parentElement.classList.add('placeholder');
  event.target.parentElement.innerHTML = '<i class="el-icon-document"></i>';
};

const filteredData = computed(() => {
  let result = threadlists.value.filter(t => t.isupload === 0);
  
  if (search.value) {
    const kw = search.value.toLowerCase();
    result = result.filter(t => 
      t.name.toLowerCase().includes(kw) ||
      (t.writer && t.writer.toLowerCase().includes(kw))
    );
  }
  
  return result;
});

const sortedData = computed(() => {
  const data = [...filteredData.value];
  if (filterType.value === 'hot') {
    return data.sort((a, b) => (b.views || 0) - (a.views || 0));
  } else if (filterType.value === 'new') {
    return data.sort((a, b) => {
      const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
      const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
      return timeB - timeA;
    });
  } else {
    return data.sort((a, b) => {
      const getScore = (item) => {
        const views = item.views || 0;
        const comments = item.commentCount || 0;
        let timeDecay = 1.0;
        if (item.createTime) {
          const hours = (Date.now() - new Date(item.createTime).getTime()) / (1000 * 60 * 60);
          timeDecay = 1.0 / (1.0 + hours / 168);
        }
        return (views * 0.3 + comments * 2.0) * timeDecay;
      };
      return getScore(b) - getScore(a);
    });
  }
});

const displayData = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return sortedData.value.slice(start, start + pageSize);
});

const totalPages = computed(() => Math.ceil(sortedData.value.length / pageSize));

function goHome() {
  router.push('/')
}

function gotoUser() {
  router.push('/user/index')
}

function gotoupload() {
  router.push('/uploadthread')
}

function toThreadDetail(id) {
  router.push(`/thread/read?id=${id}`)
}
</script>

<style lang="less" scoped>
.tieba-list-page {
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
    max-width: 400px;
    
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
    h2 {
      margin: 0;
      font-size: 20px;
      color: #333;
      display: inline-block;
    }
    
    .count {
      font-size: 14px;
      color: #999;
      margin-left: 10px;
    }
  }

  .header-right {
    .el-input {
      width: 250px;
    }
  }
}

.filter-bar {
  display: flex;
  gap: 10px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .tab {
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      background: #f5f8fc;
    }

    &.active {
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: #fff;
    }
  }
}

.thread-stream {
  .thread-card {
    display: flex;
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
        gap: 16px;
        font-size: 12px;
        color: #909399;
        flex-wrap: wrap;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        
        .stats-item {
          display: flex;
          align-items: center;
          gap: 4px;
          padding: 2px 8px;
          background: #f5f7fa;
          border-radius: 10px;
          
          .stat-label {
            color: #909399;
          }
          
          .stat-value {
            color: #667eea;
            font-weight: 600;
          }
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