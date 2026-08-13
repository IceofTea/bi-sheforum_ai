<template>
  <el-menu
      class="el-menu-vertical-enhanced"
      default-active="2"
      router
      @open="handleOpen"
      @close="handleClose"
  >
    <!-- 首页 -->
    <el-menu-item index="/index">
      <el-icon><User /></el-icon>
      <span class="menu-label">首页</span>
    </el-menu-item>

    <!-- 贴子管理 -->
    <el-sub-menu index="1">
      <template #title>
        <el-icon><Reading /></el-icon>
        <span class="menu-label">贴子管理</span>
      </template>
      <el-menu-item index="/thread/list">贴子列表</el-menu-item>
      <el-menu-item index="/thread/amount">访问数据</el-menu-item>
      <el-menu-item index="/thread/add">新增贴子</el-menu-item>
    </el-sub-menu>

    <!-- 话题管理 -->
    <el-sub-menu index="2">
      <template #title>
        <el-icon><Discount /></el-icon>
        <span class="menu-label">话题管理</span>
      </template>
      <el-menu-item index="/sort/list">话题列表</el-menu-item>
      <el-menu-item index="/sort/add">话题添加</el-menu-item>
    </el-sub-menu>

    <!-- 用户管理 -->
    <el-sub-menu index="3">
      <template #title>
        <el-icon><User /></el-icon>
        <span class="menu-label">用户管理</span>
      </template>
      <el-menu-item index="/reader/list">用户列表</el-menu-item>
    </el-sub-menu>

    <!-- 个人中心 -->
    <el-menu-item index="/user/personal">
      <el-icon><UserFilled /></el-icon>
      <span class="menu-label">个人中心</span>
    </el-menu-item>

    <!-- 作者管理 -->
    <el-sub-menu index="4">
      <template #title>
        <el-icon><EditPen /></el-icon>
        <span class="menu-label">作者管理</span>
      </template>
      <el-menu-item index="/writer/list">作者列表</el-menu-item>
      <el-menu-item index="/writer/add">添加作者</el-menu-item>
    </el-sub-menu>

    <!-- 评论管理 -->
    <el-menu-item index="/comment/list">
      <el-icon><ChatLineSquare /></el-icon>
      <span class="menu-label">评论管理</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import { User, Reading, Discount, EditPen, ChatLineSquare, UserFilled } from '@element-plus/icons-vue'

const handleOpen = (key, keyPath) => {
  console.log('打开菜单：', key, keyPath);
};

const handleClose = (key, keyPath) => {
  console.log('关闭菜单：', key, keyPath);
};
</script>

<style scoped lang="less">
.el-menu-vertical-enhanced {
  width: 240px;
  min-height: 100vh;
  background-color: #fff;
  border-right: 1px solid #f0f0f0;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.03);
  transition: all 0.3s ease;
  overflow-y: auto;
  scrollbar-width: none;
}

.el-menu-vertical-enhanced .el-menu-item,
.el-menu-vertical-enhanced .el-sub-menu__title {
  height: 50px;
  font-size: 16px;
  color: #444;
  padding-left: 20px;
  transition: all 0.25s ease;
  border-left: 3px solid transparent;
}

.el-menu-vertical-enhanced .el-menu-item:hover,
.el-menu-vertical-enhanced .el-sub-menu__title:hover {
  background-color: #f0faff;
  color: #00bcd4;
  border-left: 3px solid #00bcd4;
}

.el-menu-vertical-enhanced .el-menu-item:hover .el-icon,
.el-menu-vertical-enhanced .el-sub-menu__title:hover .el-icon {
  transform: scale(1.1) rotate(4deg);
}

.el-menu-vertical-enhanced .el-menu-item.is-active,
.el-menu-vertical-enhanced .el-sub-menu.is-opened > .el-sub-menu__title {
  background-color: #e6f7ff;
  color: #00bcd4;
  font-weight: bold;
  border-left: 3px solid #00bcd4;
}

.el-menu-vertical-enhanced .el-sub-menu .el-menu-item {
  padding-left: 42px;
  font-size: 15px;
  position: relative;
}

.el-menu-vertical-enhanced .el-sub-menu .el-menu-item::before {
  content: '';
  position: absolute;
  left: 30px;
  top: 50%;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: transparent;
  transform: translateY(-50%);
  transition: background-color 0.3s;
}

.el-menu-vertical-enhanced .el-sub-menu .el-menu-item:hover::before {
  background-color: #00bcd4;
}

.el-menu-vertical-enhanced .el-menu-item.is-active::after {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background-color: #00bcd4;
  animation: activeLine 0.3s ease-in-out;
}

.menu-label {
  margin-left: 8px;
  transition: color 0.3s;
}

@keyframes activeLine {
  from {
    height: 0;
  }
  to {
    height: 100%;
  }
}
</style>
