<template>
  <div class="layout-wrapper" :class="{ dark: isDark }">
    <NavBar class="fixed"></NavBar>
    <HiddenSidebar :userId="userId" />
    <RouterView></RouterView>
    <Footer></Footer>
    <el-button class="dark-toggle" :icon="isDark ? 'el-icon-sunny' : 'el-icon-moon'" circle @click="toggleDark"></el-button>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import NavBar from './Nav.vue';
import Footer from './Footer.vue';
import HiddenSidebar from './HiddenSidebar.vue';

const isDark = ref(false)
const userId = ref(null)

onMounted(() => {
  isDark.value = localStorage.getItem('dark-mode') === 'true'
  const id = sessionStorage.getItem('id')
  userId.value = id ? parseInt(id) : null
})

function toggleDark() {
  isDark.value = !isDark.value
  localStorage.setItem('dark-mode', isDark.value)
  document.documentElement.classList.toggle('dark', isDark.value)
}
</script>
<style lang="less" scoped>
.fixed{
    position: fixed;
}
.dark-toggle {
  position: fixed;
  bottom: 90px;
  right: 24px;
  z-index: 999;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  font-size: 20px;
}
</style>
