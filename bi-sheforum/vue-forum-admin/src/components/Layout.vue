<template>
	<div class="common-layout">
	  <el-container>
		<el-header>
			<NavBar></NavBar>
		</el-header>
		<el-container>
		  <el-aside width="auto">
			<SiderBar></SiderBar>
		</el-aside>
		  <el-container>
			<el-main>
				<RouterView></RouterView>
			</el-main>
		  </el-container>
		</el-container>
	  </el-container>
	</div>
  </template>
<script setup>
import NavBar from './NavBar.vue';
import SiderBar from './SiderBar.vue';
</script>
<style lang="less" scoped>
.el-header {
	padding: 0;
	background-color: #fff
}

.el-aside {
	min-height: calc(100vh - 60px);
}
.el-main{
	background-color: #F3F4F6;
}
</style>
