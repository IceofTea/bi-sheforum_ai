<template>
  <div class="action-list">
    <h2>用户行为记录</h2>
    <el-alert title="功能说明" type="info" :closable="false" show-icon>
      <template #default>
        记录用户的关键操作行为：登录、发帖、评论、签到、收藏等。
      </template>
    </el-alert>
    <el-table :data="actions" border stripe v-loading="loading" style="margin-top: 20px">
      <el-table-column prop="time" label="时间" width="180" />
      <el-table-column prop="user" label="用户" width="120" />
      <el-table-column prop="action" label="行为" width="120" />
      <el-table-column prop="detail" label="详情" />
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const loading = ref(false)
const actions = ref([])
</script>
