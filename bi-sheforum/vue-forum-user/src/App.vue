<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'

onMounted(() => {
  const saved = localStorage.getItem('dark-mode')
  if (saved === 'true') {
    document.documentElement.classList.add('dark')
  }
})
</script>

<template>
  <RouterView />
</template>

<style>
:root {
  --bg-primary: #f5f8fc;
  --bg-card: #ffffff;
  --bg-nav: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --text-primary: #303133;
  --text-secondary: #909399;
  --border-color: #e4e7ed;
}

html.dark {
  --bg-primary: #1a1a2e;
  --bg-card: #16213e;
  --bg-nav: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  --text-primary: #e0e0e0;
  --text-secondary: #a0a0a0;
  --border-color: #2a2a4a;
}

body {
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: background 0.3s, color 0.3s;
  margin: 0;
}
</style>
