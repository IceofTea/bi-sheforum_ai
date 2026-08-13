<template>
  <div class="tieba-sort-page">
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
            <img :src="userAvatar" class="avatar" />
            <span class="nickname">{{ userNickname }}</span>
          </div>
        </div>
      </div>
    </header>

    <div class="page-container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentSortName }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 标题区 -->
      <div class="page-header">
        <div class="header-left">
          <div class="sort-cover">
            <img :src="sortCover" alt="">
          </div>
          <div class="sort-info">
            <h2>{{ currentSortName }}</h2>
            <div class="sort-meta">
              <span class="count">{{ filteredData.length }} 篇帖子</span>
              <span class="followers" v-if="isFollowed">已关注</span>
            </div>
            <div class="sort-actions">
              <el-button
                v-if="userId"
                :type="isFollowed ? 'info' : 'primary'"
                :icon="isFollowed ? 'el-icon-check' : 'el-icon-plus'"
                size="small"
                @click="handleFollowSort"
              >
                {{ isFollowed ? '已关注' : '关注' }}
              </el-button>
              <el-button
                v-else
                type="primary"
                size="small"
                @click="$router.push('/login')"
              >
                登录关注
              </el-button>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-input
            v-model="search"
            placeholder="搜索帖子..."
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
          <p>该分区暂无帖子</p>
          <el-button type="primary" @click="gotoupload">发布帖子</el-button>
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
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { formatRelativeTime } from '@/utils/timeFormat.js';

const router = useRouter();
const route = useRoute();
const threadlists = ref([]);
const search = ref('');
const filterType = ref('all');
const currentPage = ref(1);
const pageSize = 20;
const currentSortName = ref('话题分区');
const currentSortId = ref(null);
const isFollowed = ref(false);
const sortFollowers = ref(0);
const userNickname = ref('游客');
const userAvatar = ref('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==');

const sortCover = computed(() => {
  const colors = ['FF6B6B', '4ECDC4', '45B7D1', '96CEB4', 'FFEAA7', 'DDA0DD', '98D8C8', 'F7DC6F'];
  const color = colors[currentSortId.value % colors.length];
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200"><rect width="200" height="200" fill="#${color}"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-family="Arial" font-size="60" fill="white" font-weight="bold">${encodeURIComponent('话题')}</text></svg>`;
  return 'data:image/svg+xml;base64,' + btoa(svg);
});

const userId = computed(() => sessionStorage.id);

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
          userAvatar.value = VERCODE_URL + '/upload/' + res.data.head;
        }
      }
    } catch (e) {}
  }
};

const loadlist = async () => {
  const sortId = route.query.id;
  currentSortId.value = sortId ? parseInt(sortId) : null;

  const res = await axios.get('/api/threads/bysort', { params: route.query });
  const data = res.data || [];
  threadlists.value = data.filter(e => e.isupload === 0);

  if (data.length > 0) {
    currentSortName.value = data[0].threadsSort?.name || '话题分区';
  } else {
    const sortRes = await axios.get('/api/sort/one', { params: { id: sortId } });
    if (sortRes.data) {
      currentSortName.value = sortRes.data.name;
    }
  }

  if (sortId && sessionStorage.id) {
    try {
      const followRes = await axios.get('/api/usersort/isFollowed', {
        params: { userId: sessionStorage.id, sortId }
      });
      isFollowed.value = followRes.data === true;
    } catch (e) {}
  }

  if (sortId) {
    try {
      const followedRes = await axios.get('/api/usersort/followed', {
        params: { userId: sessionStorage.id }
      });
      sortFollowers.value = followedRes.data?.length || 0;
    } catch (e) {}
  }
};

const handleFollowSort = async () => {
  const userId = sessionStorage.id;
  if (!userId) {
    router.push('/login');
    return;
  }
  const sortId = currentSortId.value;
  if (!sortId) return;

  try {
    if (isFollowed.value) {
      await axios.get('/api/usersort/unfollow', { params: { userId, sortId } });
    } else {
      await axios.get('/api/usersort/follow', { params: { userId, sortId } });
    }
    isFollowed.value = !isFollowed.value;
    localStorage.setItem('sortFollowChanged', Date.now().toString());
  } catch (e) {}
};

const handlePageChange = (page) => {
  currentPage.value = page;
};

const handleSearch = () => {
  currentPage.value = 1;
};

const formatTime = (timestamp) => formatRelativeTime(timestamp);

const handleImageError = (event) => {
  event.target.style.display = 'none';
  event.target.parentElement.classList.add('placeholder');
  event.target.parentElement.innerHTML = '<i class="el-icon-document"></i>';
};

const filteredData = computed(() => {
  if (!search.value) return threadlists.value;
  const kw = search.value.toLowerCase();
  return threadlists.value.filter(t => 
    t.name.toLowerCase().includes(kw) ||
    (t.writer && t.writer.toLowerCase().includes(kw))
  );
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
.tieba-sort-page {
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

.breadcrumb {
  margin-bottom: 15px;
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
    gap: 20px;

    .sort-cover {
      width: 100px;
      height: 100px;
      border-radius: 12px;
      overflow: hidden;
      flex-shrink: 0;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .sort-info {
      h2 {
        margin: 0 0 8px 0;
        font-size: 22px;
        color: #333;
      }

      .sort-meta {
        display: flex;
        gap: 15px;
        margin-bottom: 10px;
        font-size: 13px;
        color: #666;

        .count {
          color: #999;
        }

        .followers {
          color: #409eff;
        }
      }

      .sort-actions {
        .el-button {
          min-width: 90px;
        }
      }
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