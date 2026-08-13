<template>
  <div class="finish-page">
    <div class="success-card">
      <div class="success-icon">
        <el-icon :size="64" color="#67c23a"><CircleCheckFilled /></el-icon>
      </div>
      <h1>发布成功！</h1>
      <p>您的帖子已成功发布，等待其他同学浏览和评论</p>
      <div class="stats">
        <div class="stat-item">
          <span class="num">{{ threadId ? '#' + threadId : '--' }}</span>
          <span class="label">帖子编号</span>
        </div>
        <div class="stat-item">
          <span class="num"><el-icon><Clock /></el-icon></span>
          <span class="label">{{ currentTime }}</span>
        </div>
      </div>
      <div class="actions">
        <el-button type="primary" size="large" @click="viewThread">查看帖子</el-button>
        <el-button size="large" @click="$router.push('/uploadthread')">继续发帖</el-button>
        <el-button size="large" @click="$router.push('/')">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CircleCheckFilled, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const threadId = ref(route.query.id || '')
const currentTime = ref('')

onMounted(() => {
  const now = new Date()
  currentTime.value = `${now.getFullYear()}-${(now.getMonth()+1).toString().padStart(2,'0')}-${now.getDate().toString().padStart(2,'0')} ${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}`
})

function viewThread() {
  if (route.query.id) {
    router.push(`/thread/read?id=${route.query.id}`)
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.finish-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.success-card {
  background: #fff;
  border-radius: 20px;
  padding: 48px;
  text-align: center;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  max-width: 480px;
  width: 100%;
}

.success-icon {
  margin-bottom: 20px;
}

h1 {
  font-size: 28px;
  color: #303133;
  margin: 0 0 12px;
}

p {
  color: #909399;
  margin: 0 0 32px;
  font-size: 15px;
}

.stats {
  display: flex;
  gap: 32px;
  justify-content: center;
  margin-bottom: 32px;

  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;

    .num {
      font-size: 20px;
      font-weight: 700;
      color: #667eea;
    }

    .label {
      font-size: 13px;
      color: #909399;
    }
  }
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}
</style>
