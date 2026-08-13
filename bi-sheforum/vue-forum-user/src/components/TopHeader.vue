<template>
  <header class="top-header">
    <div class="header-inner">
      <div class="logo" @click="goHome">校园论坛</div>
      <div class="nav-search">
        <el-input
          v-model="searchKeyword"
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
          <el-tag v-if="props.userdata.level" type="warning" size="small" effect="dark">Lv.{{ props.userdata.level }}</el-tag>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { VERCODE_URL } from '@/plugins/config.js';

const router = useRouter();
const searchKeyword = ref('');

const props = defineProps({
  userdata: {
    type: Object,
    default: () => ({})
  }
});

const userAvatar = computed(() => {
  if (props.userdata.head) {
    return VERCODE_URL + '/upload/' + props.userdata.head;
  }
  return 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==';
});

const userNickname = computed(() => props.userdata.nickname || '未登录');

const goHome = () => router.push('/');

const handleSearch = () => {
  if (searchKeyword.value) {
    router.push({ path: '/allthread', query: { search: searchKeyword.value } });
  }
};

const gotoupload = () => router.push('/uploadthread');

const gotoUser = () => router.push('/user/index');
</script>

<style lang="less" scoped>
.top-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .header-inner {
    width: 100%;
    height: 64px;
    display: flex;
    align-items: center;
    gap: 40px;
    padding: 0 60px;
  }
  
  .logo {
    font-size: 24px;
    font-weight: bold;
    color: #fff;
    cursor: pointer;
    text-shadow: 0 2px 4px rgba(0,0,0,0.2);
    min-width: 120px;
  }
  
  .nav-search {
    flex: 1;
    max-width: 500px;
    margin: 0 auto;
    
    .el-input__wrapper {
      border-radius: 20px;
      box-shadow: none;
      background: rgba(255,255,255,0.2);
      backdrop-filter: blur(10px);
      
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
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-left: auto;
    
    .el-button {
      padding: 10px 20px;
      font-size: 14px;
      background: rgba(255,255,255,0.25);
      border: none;
      color: #fff;
      backdrop-filter: blur(10px);
      
      &:hover {
        background: rgba(255,255,255,0.35);
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 5px 10px;
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
      
      .nickname {
        color: #fff;
        font-size: 14px;
        max-width: 80px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>