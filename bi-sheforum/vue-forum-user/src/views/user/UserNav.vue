<template>
  <div class="user-nav">
    <ul class="nav-inner">
      <li class="logo" @click="gotoIndex">
        <i class="el-icon-back"></i>
        <span>返回首页</span>
      </li>
      <li @click="gotomyIndex" :class="{ active: $route.path === '/user/index' }">
        <i class="el-icon-user"></i>
        <span>我的首页</span>
      </li>
      <li @click="gotocollect" :class="{ active: $route.path.includes('mycollect') }">
        <i class="el-icon-star-on"></i>
        <span>我的收藏</span>
      </li>
      <li @click="gotoread" :class="{ active: $route.path.includes('read') }">
        <i class="el-icon-time"></i>
        <span>历史记录</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function gotoIndex() {
  router.push('/')
}
function gotomyIndex() {
  router.push('/user/index')
}
function gotocollect() {
  router.push('/user/mycollect')
}
function gotoread() {
  router.push('/user/readhistory')
}
</script>

<style lang="less" scoped>
.user-nav {
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .nav-inner {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 0 20px;
    max-width: 1200px;
    margin: 0 auto;
    height: 50px;
    color: #fff;

    li {
      list-style: none;
      padding: 8px 14px;
      font-size: 14px;
      color: rgba(255,255,255,0.85);
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.3s;
      display: flex;
      align-items: center;
      gap: 6px;

      &:hover {
        background: rgba(255,255,255,0.2);
        color: #fff;
      }

      &.active {
        background: rgba(255,255,255,0.25);
        color: #fff;
        font-weight: 500;
      }

      &.logo {
        margin-right: 15px;
        font-weight: 600;
        background: rgba(255,255,255,0.15);
        
        i {
          font-size: 16px;
        }
      }
    }
  }
}
</style>