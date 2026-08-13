<template>
  <div class="tieba-home">
    <TopHeader :userdata="userdata" />
    
    <div class="tieba-container">
      <!-- 左侧边栏 -->
      <aside class="tieba-sidebar">
        <div class="side-section">
          <div class="section-title">
            <span><i class="el-icon-s-grid"></i> 版块分类</span>
            <span class="sort-count">{{ sortlist.length }}个</span>
          </div>
          <div class="sort-list" v-if="sortlist.length > 0">
            <div class="sort-grid">
              <div 
                v-for="(item, index) in pagedSorts" 
                :key="item.id" 
                class="sort-item"
                @click="gotoSortthread(item.id)"
              >
                <div class="sort-icon" :style="{ background: iconColors[(sortPage - 1 + index) % iconColors.length] }">
                  <i class="el-icon-folder-opened"></i>
                  <span class="icon-shine"></span>
                </div>
                <div class="sort-info">
                  <span class="sort-name">{{ item.name }}</span>
                  <span class="sort-desc">{{ getSortDesc(item) }}</span>
                </div>
                <div class="sort-badge" v-if="getSortThreadCount(item.id) > 0">
                  {{ getSortThreadCount(item.id) > 99 ? '99+' : getSortThreadCount(item.id) }}
                </div>
                <div class="sort-arrow"><i class="el-icon-arrow-right"></i></div>
              </div>
            </div>
            <div class="sort-pagination" v-if="sortTotalPages > 1">
              <span class="page-btn" @click="prevSortPage" :class="{ disabled: sortPage <= 1 }">
                <i class="el-icon-arrow-left"></i>
              </span>
              <div class="page-numbers">
                <template v-for="p in visiblePages" :key="p">
                  <span v-if="p === '...'" class="page-ellipsis">···</span>
                  <span v-else 
                    class="page-num" 
                    :class="{ active: p === sortPage }"
                    @click="sortPage = p"
                  >{{ p }}</span>
                </template>
              </div>
              <span class="page-btn" @click="nextSortPage" :class="{ disabled: sortPage >= sortTotalPages }">
                <i class="el-icon-arrow-right"></i>
              </span>
            </div>
          </div>
          <div class="empty-sort" v-else>
            <i class="el-icon-folder-opened"></i>
            <span>暂无版块</span>
          </div>
        </div>
        <div class="side-section quick-section">
          <div class="section-title">
            <i class="el-icon-menu"></i>
            <span>快捷服务</span>
          </div>
          <div class="quick-btns">
            <el-button icon="el-icon-edit" class="quick-btn main-btn" @click="gotoupload()">发布帖子</el-button>
            <el-button icon="el-icon-document" class="quick-btn" @click="gotoallthread()">浏览帖子</el-button>
            <el-button icon="el-icon-hot" class="quick-btn" @click="gotothreadlist()">热门排行</el-button>
            <el-button icon="el-icon-user" class="quick-btn" @click="gotoUser()">个人中心</el-button>
            <el-button icon="el-icon-star-on" class="quick-btn" @click="gotoUserCollect()">我的收藏</el-button>
            <el-button icon="el-icon-time" class="quick-btn" @click="gotoUserHistory()">阅读历史</el-button>
          </div>
        </div>
      </aside>

      <!-- 中间内容 -->
      <main class="tieba-main">
        <!-- 筛选标签 -->
        <div class="filter-tabs">
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

        <!-- 帖子列表 -->
        <div class="thread-feed">
          <div 
            v-for="item in displayThreads" 
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
                <span class="time">{{ formatRelativeTime(item.createTime) }}</span>
                <span class="stats-item"><i class="el-icon-view"></i> <span class="stat-label">阅读</span><span class="stat-value">{{ item.views || 0 }}</span></span>
                <span class="stats-item"><i class="el-icon-chat-line-round"></i> <span class="stat-label">评论</span><span class="stat-value">{{ item.commentCount || 0 }}</span></span>
              </div>
            </div>
          </div>

          <!-- 加载更多 -->
          <div class="load-more" v-if="displayThreads.length < filteredThreads.length">
            <el-button size="small" @click="loadMore">加载更多</el-button>
          </div>

          <!-- 空状态 -->
          <div class="empty-state" v-if="displayThreads.length === 0">
            <i class="el-icon-folder-opened"></i>
            <p>暂无帖子，快来发帖吧</p>
          </div>
        </div>
      </main>

      <!-- 右侧边栏 -->
      <aside class="tieba-right">
        <!-- 用户信息卡 -->
        <div class="user-card" v-if="userdata.id">
          <div class="card-inner">
            <img :src="userdata.head ? VERCODE_URL + '/upload/' + userdata.head : defaultAvatar" class="avatar-lg">
            <div class="user-name">{{ userdata.nickname }}</div>
            <div class="user-level">
              <el-tag type="warning" size="small">Lv.{{ userdata.level || 1 }}</el-tag>
              <span class="exp-text">{{ userdata.experience || 0 }} 经验</span>
            </div>
            <div class="user-stats">
              <div class="stat-item">
                <span class="num">{{ userStats.threadCount }}</span>
                <span class="label">发帖</span>
              </div>
              <div class="stat-item">
                <span class="num">{{ userStats.readCount }}</span>
                <span class="label">浏览</span>
              </div>
              <div class="stat-item">
                <span class="num">{{ userStats.collectCount }}</span>
                <span class="label">收藏</span>
              </div>
            </div>
            <el-button type="primary" size="small" @click="gotoUser">进入个人中心</el-button>
          </div>
        </div>

        <!-- 热门话题 -->
        <div class="hot-card">
          <div class="card-title"><i class="el-icon-hot"></i> 热门话题</div>
          <div class="hot-tabs">
            <span :class="['hot-tab', { active: hotFilterType === 'hot' }]" @click="hotFilterType = 'hot'">热门</span>
            <span :class="['hot-tab', { active: hotFilterType === 'comment' }]" @click="hotFilterType = 'comment'">热评</span>
            <span :class="['hot-tab', { active: hotFilterType === 'new' }]" @click="hotFilterType = 'new'">最新</span>
          </div>
          <div class="hot-list">
            <div 
              v-for="(item, index) in hotThreads" 
              :key="item.id" 
              class="hot-item"
              @click="toThreadDetail(item.id)"
            >
              <span class="hot-num" :class="'top' + (index + 1)">{{ index + 1 }}</span>
              <div class="hot-content">
                <div class="hot-title">{{ item.name }}</div>
                <div class="hot-meta">
                  <el-tag size="small" type="primary" v-if="item.threadsSort?.name">{{ item.threadsSort?.name }}</el-tag>
                  <span class="hot-author" v-if="item.writer"><i class="el-icon-user"></i> {{ item.writer }}</span>
                  <span class="hot-stat"><i class="el-icon-view"></i> {{ item.views || 0 }}</span>
                  <span class="hot-stat"><i class="el-icon-chat-line-round"></i> {{ item.commentCount || 0 }}</span>
                  <span class="hot-time" v-if="item.createTime"><i class="el-icon-time"></i> {{ formatHotTime(item.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部 -->
        <div class="footer-tip">
          <div class="footer-links">
            <span @click="gotoIndex">返回首页</span>
            <span @click="gotoallthread">全部帖子</span>
            <span @click="gotothreadlist">热门排行</span>
            <span @click="gotoupload">发帖</span>
          </div>
          <div class="footer-copyright">校园论坛 - 连接你我</div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import TopHeader from '@/components/TopHeader.vue';

import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios';
import { formatRelativeTime } from '@/utils/timeFormat.js';

const router = useRouter();
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==';

const sortlist = ref([]);
const allThreads = ref([]);
const hotFilterType = ref('hot');
const userdata = ref({});
const filterType = ref('all');
const searchKeyword = ref('');
const pageSize = 15;
const currentPage = ref(1);
const userStats = ref({ collectCount: 0, threadCount: 0, readCount: 0 });
const sortPage = ref(1);
const sortPageSize = 4;

const iconColors = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a8edea, #fed6e3)',
  'linear-gradient(135deg, #ff9a9e, #fecfef)',
  'linear-gradient(135deg, #ffecd2, #fcb69f)'
];

const getSortDesc = (item) => {
  if (!item.introduction) return '探索更多精彩内容';
  return item.introduction.length > 15 ? item.introduction.substring(0, 15) + '...' : item.introduction;
};

const getSortThreadCount = (sortId) => {
  return allThreads.value.filter(t => t.threadsSortId === sortId && t.isupload === 0).length;
};

const sortTotalPages = computed(() => Math.ceil(sortlist.value.length / sortPageSize));

const visiblePages = computed(() => {
  const total = sortTotalPages.value;
  const current = sortPage.value;
  const pages = [];
  
  if (total <= 5) {
    for (let i = 1; i <= total; i++) pages.push(i);
  } else {
    if (current <= 3) {
      pages.push(1, 2, 3, 4, '...', total);
    } else if (current >= total - 2) {
      pages.push(1, '...', total - 3, total - 2, total - 1, total);
    } else {
      pages.push(1, '...', current - 1, current, current + 1, '...', total);
    }
  }
  return pages;
});

const pagedSorts = computed(() => {
  const start = (sortPage.value - 1) * sortPageSize;
  return sortlist.value.slice(start, start + sortPageSize);
});

const prevSortPage = () => {
  if (sortPage.value > 1) sortPage.value--;
};

const nextSortPage = () => {
  if (sortPage.value < sortTotalPages.value) sortPage.value++;
};

const displaySorts = computed(() => {
  return sortlist.value;
});

onMounted(() => {
  loadData();
});

const loadData = async () => {
  try {
    const [sortRes, threadRes, userRes] = await Promise.all([
      axios.get('/api/sort').catch(() => ({ data: [] })),
      axios.get('/api/threads/all').catch(() => ({ data: [] })),
      sessionStorage.id ? axios.get('/api/users/one', { params: { id: sessionStorage.id } }).catch(() => ({ data: {} })) : Promise.resolve({ data: {} })
    ]);
    
    sortlist.value = sortRes.data || [];
    allThreads.value = threadRes.data || [];
    userdata.value = userRes.data?.data || userRes.data || {};

    console.log('userdata:', userdata.value);

    if (userdata.value.id) {
      loadUserStats();
      localStorage.setItem('userLoggedIn', Date.now().toString());
    }
  } catch (error) {
    console.error('加载数据失败:', error);
  }
};

const loadUserStats = async () => {
  try {
    const userId = sessionStorage.id;
    if (!userId) return;

    const userRes = await axios.get('/api/threads/byuser', { params: { writerId: userId } });
    const threads = userRes.data || [];
    
    userStats.value.threadCount = threads.length;
    console.log('用户帖子数:', userStats.value.threadCount);

    // 调用/api/userread/one获取浏览记录，然后统计去重后的不同帖子数量
    const readRes = await axios.get('/api/userread/one', { params: { id: userId } });
    const readList = readRes.data || [];
    const uniqueThreadIds = new Set(readList.map(item => item.threadInfo?.id).filter(Boolean));
    userStats.value.readCount = uniqueThreadIds.size;
    console.log('���览记录数:', readList.length, '去重后:', userStats.value.readCount);

    // 调用/api/usercollect/one获取收藏记录
    const collectRes = await axios.get('/api/usercollect/one', { params: { id: userId } });
    userStats.value.collectCount = collectRes.data?.length || 0;

    console.log('用户统计:', userStats.value);
  } catch (error) {
    console.error('加载用户统计失败:', error);
  }
};

const filteredThreads = computed(() => {
  let result = allThreads.value.filter(t => t.isupload === 0);
  
  if (filterType.value === 'hot') {
    result = [...result].sort((a, b) => (b.views || 0) - (a.views || 0));
  } else if (filterType.value === 'new') {
    result = [...result].sort((a, b) => {
      const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
      const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
      return timeB - timeA;
    });
  } else {
    result = [...result].sort((a, b) => {
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
  
  return result;
});

const displayThreads = computed(() => {
  return filteredThreads.value.slice(0, currentPage.value * pageSize);
});

const hotThreads = computed(() => {
  const threads = allThreads.value.filter(t => t.isupload === 0);
  let sorted;
  if (hotFilterType.value === 'comment') {
    sorted = [...threads].sort((a, b) => (b.commentCount || 0) - (a.commentCount || 0));
  } else if (hotFilterType.value === 'new') {
    sorted = [...threads].sort((a, b) => {
      const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
      const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
      return timeB - timeA;
    });
  } else {
    sorted = [...threads].sort((a, b) => (b.views || 0) - (a.views || 0));
  }
  return sorted.slice(0, 5);
});

const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const now = new Date();
  const diff = now - d;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));  
  if (days === 0) return '今天';
  if (days === 1) return '昨天';
  if (days < 7) return `${days}天前`;  
  return `${d.getMonth() + 1}-${d.getDate()}`;
}

const formatHotTime = (timestamp) => formatRelativeTime(timestamp);

const handleAvatarError = (event) => {
  event.target.src = defaultAvatar;
};

const loadMore = () => {
  currentPage.value++;
};

const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({ path: '/allthread', query: { search: searchKeyword.value } });
  }
};

function goHome() {
  router.push('/')
}

function gotoUser() {
  router.push('/user/index')
}

function gotoUserCollect() {
  router.push('/user/mycollect')
}

function gotoUserHistory() {
  router.push('/user/readhistory')
}

function gotoLogin() {
  router.push('/login')
}

function gotoSortthread(id) {
  router.push(`/sortthread?id=${id}`);
}

function gotoallthread() {
  router.push('/allthread')
}

function gotothreadlist() {
  router.push('/threadlist')
}

function gotoupload() {
  router.push('/uploadthread')
}

function toThreadDetail(id) {
  router.push(`/thread/read?id=${id}`)
}
</script>

<style lang="less" scoped>
.tieba-home {
  min-height: 100vh;
  background: #f5f8fc;
}

.tieba-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 20px;
  flex: 1;
}

.tieba-sidebar {
  width: 200px;
  flex-shrink: 0;
  
  .side-section {
    background: #fff;
    border-radius: 12px;
    padding: 15px;
    margin-bottom: 15px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    
    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      padding-bottom: 12px;
      border-bottom: 1px solid #eee;
      margin-bottom: 12px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .sort-count {
        font-size: 12px;
        color: #999;
        font-weight: normal;
      }
    }
    
    .sort-list {
      .sort-grid {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      
      .sort-pagination {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px solid #eee;
        
        .page-btn {
          width: 28px;
          height: 28px;
          border-radius: 6px;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          background: #f5f8fc;
          color: #667eea;
          transition: all 0.2s;
          font-size: 12px;
          
          &:hover:not(.disabled) {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: #fff;
            transform: scale(1.1);
          }
          
          &.disabled {
            color: #ccc;
            cursor: not-allowed;
          }
        }
        
        .page-numbers {
          display: flex;
          align-items: center;
          gap: 4px;
          
          .page-num {
            min-width: 28px;
            height: 28px;
            padding: 0 8px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            font-size: 12px;
            color: #666;
            transition: all 0.2s;
            background: #f5f8fc;
            
            &:hover:not(.active) {
              background: #e8ecf3;
              color: #667eea;
            }
            
            &.active {
              background: linear-gradient(135deg, #667eea, #764ba2);
              color: #fff;
              font-weight: 600;
            }
          }
          
          .page-ellipsis {
            color: #999;
            font-size: 12px;
            padding: 0 4px;
          }
        }
      }
      
      .sort-item {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        border-radius: 10px;
        cursor: pointer;
        font-size: 13px;
        color: #555;
        transition: all 0.3s;
        background: #f8f9fc;
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 3px;
          background: linear-gradient(135deg, #667eea, #764ba2);
          opacity: 0;
          transition: opacity 0.3s;
        }
        
        &:hover {
          background: linear-gradient(135deg, #667eea08, #764ba208);
          transform: translateX(4px);
          
          &::before {
            opacity: 1;
          }
          
          .sort-arrow {
            opacity: 1;
            transform: translateX(0);
          }
          
          .sort-name {
            color: #667eea;
          }
          
          .sort-badge {
            transform: scale(1.1);
          }
          
          .sort-icon .icon-shine {
            animation-duration: 1s;
          }
        }
        
        .sort-icon {
          width: 36px;
          height: 36px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          position: relative;
          overflow: hidden;
          
          i {
            color: #fff;
            font-size: 16px;
            position: relative;
            z-index: 1;
          }
          
          .icon-shine {
            position: absolute;
            top: -50%;
            left: -50%;
            width: 200%;
            height: 200%;
            background: linear-gradient(
              45deg,
              transparent 40%,
              rgba(255,255,255,0.3) 50%,
              transparent 60%
            );
            animation: shine 3s ease-in-out infinite;
          }
        }
        
        @keyframes shine {
          0%, 100% { transform: translateX(-100%) rotate(45deg); }
          50% { transform: translateX(100%) rotate(45deg); }
        }
        
        .sort-info {
          flex: 1;
          min-width: 0;
          display: flex;
          flex-direction: column;
          gap: 2px;
          
          .sort-name {
            font-weight: 500;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            transition: color 0.3s;
          }
          
          .sort-desc {
            font-size: 11px;
            color: #999;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        
        .sort-badge {
          position: absolute;
          top: 6px;
          right: 6px;
          min-width: 18px;
          height: 18px;
          padding: 0 5px;
          background: linear-gradient(135deg, #f56c6c, #e6a23c);
          color: #fff;
          font-size: 10px;
          font-weight: 600;
          border-radius: 9px;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 6px rgba(245, 108, 108, 0.4);
        }
        
        .sort-arrow {
          opacity: 0;
          transform: translateX(-8px);
          transition: all 0.3s;
          color: #667eea;
        }
      }
      
      .empty-sort {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 20px;
        color: #999;
        font-size: 13px;
        
        i {
          font-size: 28px;
        }
      }
    }
  }
}

.side-section.quick-section {
  .section-title {
    justify-content: center !important;
    gap: 6px;
  }
  
  .quick-btns {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .quick-btn {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      font-size: 13px;
      border: 1px solid #e8ecf3;
      background: #fff;
      color: #666;
      padding: 8px 12px;
      
      &:hover {
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-color: #667eea;
        color: #fff;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.25);
      }
      
      &.main-btn {
        background: linear-gradient(135deg, #667eea, #764ba2);
        border-color: #667eea;
        color: #fff;
        
        &:hover {
          background: linear-gradient(135deg, #5a6fd6, #6a4190);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }
      }
      
      i {
        margin-right: 6px;
      }
    }
  }
}

.tieba-main {
  flex: 1;
  min-width: 0;
  
  .filter-tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
    padding: 12px 15px;
    background: #fff;
    border-radius: 12px;
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
      
      i {
        margin-right: 4px;
      }
    }
  }
  
  .thread-feed {
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
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
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
  }
}

.tieba-right {
  width: 240px;
  flex-shrink: 0;
  
  .user-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 15px;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
    
    .card-inner {
      text-align: center;
    }
    
    .avatar-lg {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      border: 3px solid rgba(255,255,255,0.5);
      margin-bottom: 12px;
    }
    
    .user-name {
      color: #fff;
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 8px;
    }
    
    .user-level {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-bottom: 12px;
      
      .exp-text {
        color: rgba(255,255,255,0.8);
        font-size: 12px;
      }
    }
    
    .user-stats {
      display: flex;
      justify-content: center;
      gap: 30px;
      margin-bottom: 15px;
      
      .stat-item {
        text-align: center;
        
        .num {
          display: block;
          color: #fff;
          font-size: 20px;
          font-weight: bold;
        }
        
        .label {
          color: rgba(255,255,255,0.8);
          font-size: 12px;
        }
      }
    }
    
    .el-button {
      background: rgba(255,255,255,0.25);
      border: none;
      color: #fff;
      
      &:hover {
        background: rgba(255,255,255,0.35);
      }
    }
  }
  
  .hot-card {
    background: #fff;
    border-radius: 12px;
    padding: 15px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);  
    .card-title {
      font-size: 14px;
      font-weight: 600;
      color: #333;
      padding-bottom: 12px;
      border-bottom: 1px solid #eee;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 8px;      
      i {
        color: #f56c6c;
      }
    }  
    .hot-tabs {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;      
      .hot-tab {
        padding: 4px 12px;
        border-radius: 16px;
        font-size: 12px;
        color: #666;
        cursor: pointer;
        transition: all 0.3s;
        background: #f5f8fc;        
        &:hover {
          background: #e8ecf3;
        }        
        &.active {
          background: linear-gradient(135deg, #667eea, #764ba2);
          color: #fff;
        }
      }
    }  
    .hot-list {
      .hot-item {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        padding: 10px 0;
        border-bottom: 1px solid #f5f5f5;
        cursor: pointer;
        transition: all 0.2s;        
        &:last-child {
          border-bottom: none;
        }        
        &:hover {
          background: #f9f9f9;
          border-radius: 6px;
          padding-left: 4px;          
          .hot-title {
            color: #667eea;
          }
        }        
        .hot-num {
          width: 20px;
          height: 20px;
          border-radius: 4px;
          font-size: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          font-weight: 600;          
          &.top1 { background: #f56c6c; color: #fff; }
          &.top2 { background: #e6a23c; color: #fff; }
          &.top3 { background: #67c23a; color: #fff; }
          &.top4, &.top5 { background: #eee; color: #888; }
        }        
        .hot-content {
          flex: 1;
          min-width: 0;          
          .hot-title {
            font-size: 13px;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            transition: color 0.3s;
            margin-bottom: 6px;
            font-weight: 500;
          }          
          .hot-meta {
            display: flex;
            align-items: center;
            gap: 8px;
            flex-wrap: wrap;
            font-size: 12px;
            color: #999;            
            .hot-author {
              display: flex;
              align-items: center;
              gap: 2px;
            }            
            .hot-stat {
              display: flex;
              align-items: center;
              gap: 2px;
            }            
            .hot-time {
              display: flex;
              align-items: center;
              gap: 2px;
            }            
            .el-tag {
              padding: 0 6px;
              height: 18px;
              line-height: 18px;
            }
          }
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
}

@media (max-width: 900px) {
  .tieba-container {
    flex-direction: column;
    padding-left: 20px;
  }
  
  .tieba-sidebar, .tieba-right {
    width: 100%;
  }
}
</style>