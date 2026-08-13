<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { ElConfigProvider } from 'element-plus' ;
</script>

<template>
  <el-config-provider>
    <RouterView />
  </el-config-provider>
</template>

