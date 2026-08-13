<template>
  <div class="info-statistics">
    <h2>用户信息统计</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const stats = ref([
  { label: '总用户数', value: '--' },
  { label: '今日注册', value: '--' },
  { label: '总发帖数', value: '--' },
  { label: '总评论数', value: '--' }
])
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 20px;
  .stat-value { font-size: 32px; font-weight: 700; color: #409eff; }
  .stat-label { font-size: 14px; color: #909399; margin-top: 8px; }
}
</style>
