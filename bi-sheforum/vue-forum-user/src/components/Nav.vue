<template>
  <!-- 顶部导航栏 -->
  <header class="nav-container">
    <div class="nav-inner">
      <div class="logo" @click="gotoIndex">校园论坛</div>
      <nav class="nav-bar">
        <ul>
          <li @click="gotoIndex()" :class="['nav-item', { active: $route.path === '/' || $route.path === '/index' }]">首页</li>
          <li @click="gotoallthread()" :class="['nav-item', { active: $route.path === '/allthread' }]">全部帖子</li>
          <li @click="gotothreadlist()" :class="['nav-item', { active: $route.path === '/threadlist' }]">热门排行</li>
        </ul>
      </nav>
      <div class="nav-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索帖子..."
          prefix-icon="el-icon-search"
          size="small"
          @keyup.enter="handleSearch"
        ></el-input>
      </div>
      <div class="nav-actions">
        <el-button type="primary" size="small" icon="el-icon-edit" @click="gotoupload">发帖</el-button>
        <div class="user-info" v-if="userdata.id">
          <img :src="userdata.head ? VERCODE_URL + '/upload/' + userdata.head : defaultAvatar" class="avatar" @error="handleAvatarError">
          <span class="username" @click="gotoUser">{{ userdata.nickname || '游客' }}</span>
          <el-tag type="warning" size="small">Lv.{{ userdata.level || 1 }}</el-tag>
          <span class="logout" @click="logout">退出</span>
        </div>
        <el-button plain size="small" v-if="!userdata.id" @click="gotoLogin">登录</el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from "axios";

const router = useRouter();
const route = useRoute();
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==';

const userdata = ref({});
const searchKeyword = ref('');

onMounted(() => {
  loadUser();
});

const loadUser = async () => {
  if (sessionStorage.id) {
    try {
      const res = await axios.get('/api/users/one', { params: { id: sessionStorage.id } }).catch(() => ({ data: {} }));
      userdata.value = res.data || {};
    } catch (error) {
      console.error('加载用户失败:', error);
    }
  }
};

const handleAvatarError = (event) => {
  event.target.src = defaultAvatar;
};

const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({ path: '/allthread', query: { search: searchKeyword.value } });
  }
};

const gotoIndex = () => router.push('/');
const gotoUser = () => router.push('/user/index');
const gotoLogin = () => router.push('/login');
const gotoallthread = () => router.push('/allthread');
const gotothreadlist = () => router.push('/threadlist');
const gotoupload = () => router.push('/uploadthread');
const logout = () => {
  sessionStorage.clear();
  userdata.value = {};
  router.push('/login');
};
</script>

<style lang="less" scoped>
.nav-container {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .nav-inner {
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
    text-shadow: 0 2px 4px rgba(0,0,0,0.2);
  }

  .nav-bar {
    ul {
      display: flex;
      gap: 5px;
    }
    
    .nav-item {
      list-style: none;
      padding: 8px 16px;
      font-size: 15px;
      color: rgba(255,255,255,0.85);
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.3s;
      
      &:hover {
        background: rgba(255,255,255,0.2);
        color: #fff;
      }
      
      &.active {
        background: rgba(255,255,255,0.25);
        color: #fff;
        font-weight: 500;
      }
    }
  }

  .nav-search {
    flex: 1;
    max-width: 280px;
    
    .el-input__wrapper {
      border-radius: 20px;
      box-shadow: none;
      background: rgba(255,255,255,0.2);
      
      &:hover, &:focus-within {
        background: rgba(255,255,255,0.3);
      }
      
      input {
        background: transparent;
        color: #fff;
        
        &::placeholder {
          color: rgba(255,255,255,0.7);
        }
      }
      
      .el-input__prefix {
        color: #fff;
      }
    }
  }

  .nav-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-left: auto;
    
    .el-button {
      background: rgba(255,255,255,0.25);
      border: none;
      color: #fff;
      
      &:hover {
        background: rgba(255,255,255,0.35);
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 10px;
      border-radius: 20px;
      background: rgba(255,255,255,0.2);
      transition: all 0.3s;
      
      &:hover {
        background: rgba(255,255,255,0.3);
      }
      
      .avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        object-fit: cover;
        border: 2px solid rgba(255,255,255,0.5);
      }
      
      .username {
        color: #fff;
        font-size: 14px;
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .el-tag {
        flex-shrink: 0;
      }
      
      .logout {
        color: rgba(255,255,255,0.7);
        font-size: 13px;
        padding: 4px 8px;
        border-radius: 4px;
        transition: all 0.2s;
        
        &:hover {
          background: rgba(255,255,255,0.2);
          color: #fff;
        }
      }
    }
  }

  @media (max-width: 900px) {
    .nav-inner {
      flex-wrap: wrap;
      height: auto;
      padding: 10px;
    }
    
    .nav-bar {
      order: 3;
      width: 100%;
      
      ul {
        justify-content: center;
        flex-wrap: wrap;
        gap: 3px;
        padding-top: 10px;
        border-top: 1px solid rgba(255,255,255,0.2);
        margin-top: 10px;
      }
    }
  }
}
</style>