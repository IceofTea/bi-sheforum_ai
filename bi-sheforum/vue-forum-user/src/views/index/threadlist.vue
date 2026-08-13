<template>
  <div class="threadlist-page">
    <header class="tieba-header">
      <div class="header-inner">
        <div class="logo" @click="goHome">校园论坛</div>
        <div class="header-actions">
          <el-button type="primary" icon="el-icon-edit" @click="gotoupload">发帖</el-button>
          <div class="user-info" v-if="userdata.id">
            <img :src="userdata.head ? VERCODE_URL + '/upload/' + userdata.head : defaultAvatar" class="avatar">
            <span class="nickname" @click="gotoUser">{{ userdata.nickname || '游客' }}</span>
            <el-tag type="warning" size="small">Lv.{{ userdata.level || 1 }}</el-tag>
          </div>
          <el-button plain v-if="!userdata.id" @click="gotoLogin">登录</el-button>
        </div>
      </div>
    </header>

    <main class="tieba-main">
      <div class="page-title">
        <i class="el-icon-hot"></i>
        热门排行
      </div>

      <div class="ranking-container">
        <div class="ranking-section">
          <div class="section-header">
            <i class="el-icon-view"></i>
            <span>浏览榜</span>
          </div>
          <div class="ranking-list">
            <div 
              v-for="(item, index) in readRanked" 
              :key="'read-' + item.id" 
              class="rank-item"
              @click="gotoThread(item.id)"
            >
              <span class="rank-num" :class="'top' + (index + 1)">{{ index + 1 }}</span>
              <div class="rank-content">
                <div class="rank-title">{{ item.name }}</div>
                <div class="rank-meta">
                  <span class="author">{{ item.writer }}</span>
                  <span class="time">{{ formatRelativeTime(item.createTime) }}</span>
                  <span class="sort">{{ item.threadsSort?.name }}</span>
                </div>
              </div>
              <div class="rank-stats">
                <span><i class="el-icon-view"></i> {{ EveryThreadReadData[item.name] || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="ranking-section">
          <div class="section-header">
            <i class="el-icon-star-on"></i>
            <span>收藏榜</span>
          </div>
          <div class="ranking-list">
            <div 
              v-for="(item, index) in collectRanked" 
              :key="'collect-' + item.id" 
              class="rank-item"
              @click="gotoThread(item.id)"
            >
              <span class="rank-num" :class="'top' + (index + 1)">{{ index + 1 }}</span>
              <div class="rank-content">
                <div class="rank-title">{{ item.name }}</div>
                <div class="rank-meta">
                  <span class="author">{{ item.writer }}</span>
                  <span class="time">{{ formatRelativeTime(item.createTime) }}</span>
                  <span class="sort">{{ item.threadsSort?.name }}</span>
                </div>
              </div>
              <div class="rank-stats">
                <span><i class="el-icon-star-on"></i> {{ EveryThreadCollectData[item.name] || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="footer-tip">
        <div class="footer-links">
          <span @click="goHome">返回首页</span>
          <span @click="gotoallthread">全部帖子</span>
          <span @click="gotothreadlist">热门排行</span>
          <span @click="gotoupload">发帖</span>
        </div>
        <div class="footer-copyright">校园论坛 - 连接你我</div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import { formatRelativeTime } from '@/utils/timeFormat.js';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==';

let readRanked = ref([]);
let collectRanked = ref([]);
let userdata = ref({});
let EveryThreadReadData = ref({});
let EveryThreadCollectData = ref({});

onMounted(() => {
  loadData();
});

const loadData = async () => {
  try {
    const [threadRes, userRes, userReadRes, userCollectRes] = await Promise.all([
      axios.get('api/threads/all'),
      sessionStorage.id ? axios.get('/api/users/one', { params: { id: sessionStorage.id } }) : Promise.resolve({ data: {} }),
      axios.get('/api/userread'),
      axios.get('/api/usercollect')
    ]);

    userdata.value = userRes.data || {};

    const threads = threadRes.data || [];
    threads.forEach(item => {
      EveryThreadReadData.value[item.name] = 0;
      EveryThreadCollectData.value[item.name] = 0;
    });

    (userReadRes.data || []).forEach(item => {
      if (item.threadInfo?.name) {
        EveryThreadReadData.value[item.threadInfo.name] = (EveryThreadReadData.value[item.threadInfo.name] || 0) + 1;
      }
    });

    (userCollectRes.data || []).forEach(item => {
      if (item.threadInfo?.name) {
        EveryThreadCollectData.value[item.threadInfo.name] = (EveryThreadCollectData.value[item.threadInfo.name] || 0) + 1;
      }
    });

    const readWithCount = threads.map(item => ({
      ...item,
      num: EveryThreadReadData.value[item.name] || 0
    }));
    const collectWithCount = threads.map(item => ({
      ...item,
      num:EveryThreadCollectData.value[item.name] || 0
    }));

    readRanked.value = readWithCount.sort((a, b) => b.num - a.num).slice(0, 10);
    collectRanked.value = collectWithCount.sort((a, b) => b.num - a.num).slice(0, 10);
  } catch (error) {
    console.error('加载数据失败:', error);
  }
};

const goHome = () => router.push('/');
const gotoUser = () => router.push('/user/index');
const gotoLogin = () => router.push('/login');
const gotoupload = () => router.push('/uploadthread');
const gotoallthread = () => router.push('/allthread');
const gotothreadlist = () => router.push('/threadlist');
const gotoThread = (id) => router.push(`/thread/read?id=${id}`);
</script>

<style lang="less" scoped>
.threadlist-page {
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
      padding: 5px 10px;
      border-radius: 20px;
      background: rgba(255,255,255,0.2);

      .avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: 2px solid rgba(255,255,255,0.5);
      }

      .nickname {
        color: #fff;
        font-size: 14px;
      }
    }
  }
}

.tieba-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;

  i {
    color: #f56c6c;
  }
}

.ranking-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.ranking-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .section-header {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      color: #667eea;
    }
  }

  .ranking-list {
    .rank-item {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      margin-bottom: 8px;

      &:hover {
        background: linear-gradient(135deg, #667eea10, #764ba210);

        .rank-title {
          color: #667eea;
        }
      }

      .rank-num {
        width: 28px;
        height: 28px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        font-weight: bold;
        flex-shrink: 0;

        &.top1 { background: #f56c6c; color: #fff; }
        &.top2 { background: #e6a23c; color: #fff; }
        &.top3 { background: #67c23a; color: #fff; }
        &.top4, &.top5, &.top6, &.top7, &.top8, &.top9, &.top10 { background: #eee; color: #888; }
      }

      .rank-content {
        flex: 1;
        min-width: 0;

        .rank-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          transition: color 0.3s;
        }

        .rank-meta {
          font-size: 12px;
          color: #909399;

          .author {
            margin-right: 10px;
          }

          .sort {
            color: #667eea;
          }
        }
      }

      .rank-stats {
        font-size: 14px;
        color: #909399;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

.footer-tip {
  text-align: center;
  padding: 25px 20px;
  
  .footer-links {
    display: flex;
    justify-content: center;
    gap: 25px;
    margin-bottom: 12px;
    
    span {
      font-size: 13px;
      color: #888;
      cursor: pointer;
      transition: color 0.3s;
      
      &:hover {
        color: #667eea;
      }
    }
  }
  
  .footer-copyright {
    color: #aaa;
    font-size: 12px;
  }
}

@media (max-width: 768px) {
  .ranking-container {
    grid-template-columns: 1fr;
  }
}
</style>