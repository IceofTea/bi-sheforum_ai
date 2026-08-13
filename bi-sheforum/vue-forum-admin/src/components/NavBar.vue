<template>
  <el-menu
      :default-active="activeIndex"
      router
      class="custom-menu"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
  >
    <!-- 图片 Logo -->
    <el-menu-item index="0" class="logo-img">
      <img src="./img/Logo.png" alt="LOGO" class="logo-image" />
    </el-menu-item>

    <div class="flex-grow" />

    <!-- 用户信息 -->
    <el-sub-menu index="/user" class="user-info">
      <template #title>
        <div class="user-content">
          <div class="avatar-container">
            <img :src="VERCODE_URL + '/upload/' + root.head" alt="头像" class="avatar" />
          </div>
          <span class="username">{{ root.rootname }}</span>
        </div>
      </template>
      <el-menu-item index="/user/personal">个人中心</el-menu-item>
      <el-menu-item index="/user/edit">编辑资料</el-menu-item>
    </el-sub-menu>

    <!-- 退出按钮 -->
    <el-menu-item index="2" class="logout-btn">退出</el-menu-item>
  </el-menu>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import { ref, onMounted } from 'vue';
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const root = ref({});
const activeIndex = ref('1');

const handleSelect = async (key, keyPath) => {
  console.log(key, keyPath);

  // 处理退出登录
  if (key === '2' || keyPath.includes('logout')) {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      sessionStorage.clear()
      window.location.href = '/user/login'
    } catch {
      // 用户取消了退出
    }
  }
};

const loadList = async () => {
  try {
    const res = await axios.get('/api/roots/one', { params: { name: sessionStorage.rootname } })
    if (res.data) {
      root.value = res.data
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
};

onMounted(() => {
  loadList();
});
</script>

<style scoped lang="less">
.custom-menu {
  display: flex;
  align-items: center;
  padding: 0 5%;
  background: #ffffff;
  border-bottom: 1px solid #eee;
  height: 64px;

  :deep(.el-menu-item) {
    display: flex;
    align-items: center;
    height: 64px;
    padding: 0 20px;
    font-size: 18px;
    color: #333;
    transition: all 0.3s ease;

    &:hover {
      color: #00bcd4;
      background-color: transparent;
    }
  }
}

.flex-grow {
  flex-grow: 1;
}

/* 图片 Logo */
.logo-img {
  padding: 0;
  margin-left: -80px;
  .logo-image {
    height: 60px;
    width: auto;
    display: block;
  }
}

/* 用户信息模块 */
.user-info {
  .user-content {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .avatar-container {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid #00bcd4;
    transition: transform 0.3s ease, box-shadow 0.3s;

    &:hover {
      transform: scale(1.1);
      box-shadow: 0 0 10px #00bcd4;
    }
  }

  .avatar {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .username {
    font-size: 16px;
    color: #333;
    background: #ffffff;
    padding: 4px 10px;
    border-radius: 5px;
    cursor: default;
    transition: none;

    &:hover {
      background: #ffffff;
    }
  }
}

/* 退出按钮 */
.logout-btn {
  font-size: 16px;
  color: #00bcd4;
  border: 1px solid #00bcd4;
  border-radius: 5px;
  padding: 6px 15px;
  transition: all 0.3s;

  &:hover {
    background-color: #00bcd4;
    color: white;
    box-shadow: 0 0 10px #00bcd4;
  }
}
</style>
