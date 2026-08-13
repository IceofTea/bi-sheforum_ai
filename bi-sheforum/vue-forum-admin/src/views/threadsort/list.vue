<template>
  <div class="card">
    <el-table :data="pagedTableData"
              style="min-height: 480px;width: 100%; padding: 0 10px; border: 1px solid #ebeef5; border-radius: 10px; background-color: #fff; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);">

      <el-table-column label="id" prop="id" width="150" />
      <el-table-column label="名称" prop="name" width="250" />
      <el-table-column label="父级名称" prop="parentcontent.name" />
      <el-table-column label="祖级名称" prop="parentcontent.parentcontent.name" />

      <!-- 状态列 -->
      <el-table-column label="状态">
        <template #default="scope">
          <el-tag v-if="scope.row.status == 0" class="status-tag" type="success" effect="dark">可用</el-tag>
          <el-tag v-if="scope.row.status == 1" class="status-tag" type="danger" effect="dark">禁用</el-tag>
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column width="300">
        <template #header>
          <el-input v-model="search" placeholder="搜索" class="search-input" />
        </template>
        <template #default="scope">
          <el-button class="action-btn" type="primary" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
          <el-button class="action-btn" type="success" @click="handleAdded(scope.$index, scope.row)">启用</el-button>
          <el-button class="action-btn" type="danger" @click="handleDelete(scope.$index, scope.row)">禁用</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="sortData.length"
        layout="total, prev, pager, next, jumper"
        background
    ></el-pagination>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const sortData = ref([{}]);
const currentPage = ref(1);  // 当前页
const pageSize = ref(15);  // 每页显示15条数据

// 加载列表数据
async function loadList() {
  try {
    const res = await axios.get('/api/sort', {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });
    console.log("API 数据:", res.data); // 调试：查看返回的接口数据
    sortData.value = res.data;
  } catch (error) {
    console.error("加载数据失败:", error);
  }
}

// 操作按钮
const handleEdit = (index, row) => {
  router.push(`edit?id=${row.id}`);
}

// 启用操作
const handleAdded = (index, row) => {
  axios.post('/api/users', { id: row.id, status: 0 })
      .then((res) => {
        loadList();
      });
}

// 禁用操作
const handleDelete = (index, row) => {
  axios.post('/api/users', { id: row.id, status: 1 })
      .then((res) => {
        loadList();
      });
}

const search = ref('');

// 计算属性：过滤数据
const filterTableData = computed(() => {
  return sortData.value.filter((data) => {
    return !search.value || data.name.includes(search.value);
  });
});

// 分页计算：当前页展示的数据
const pagedTableData = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return filterTableData.value.slice(startIndex, endIndex);
});

// 处理分页页码变化
const handlePageChange = (page) => {
  currentPage.value = page;
};

loadList(); // 初始加载数据
</script>

<style scoped>
.card {
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #ebeef5;
  background-color: #fff;
  color: #303133;
  transition: box-shadow 0.3s ease;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.card:hover {
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
}

.el-table {
  border-radius: 8px;
  background-color: #f9f9f9;
}

.el-input {
  border-radius: 12px;
  transition: all 0.3s;
}

.el-input:focus {
  border-color: #409eff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.search-input {
  margin-bottom: 15px;
  width: 300px;
}

/* 半透明方形按钮样式 */
.action-btn {
  border-radius: 4px; /* 方形按钮 */
  margin: 0 5px;
  padding: 8px 20px;
  background-color: rgba(64, 158, 255, 0.1); /* 半透明背景 */
  color: #409eff;
  border: 1px solid #409eff;
  transition: transform 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px); /* 悬浮时略微提升 */
  background-color: rgba(64, 158, 255, 0.3); /* 悬浮时加深透明度 */
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2); /* 悬浮时增加阴影 */
}

.el-button--primary {
  background-color: rgba(64, 158, 255, 0.1);
  border-color: #409eff;
  color: #409eff;
}

.el-button--primary:hover {
  background-color: rgba(64, 158, 255, 0.3);
}

.el-button--success {
  background-color: rgba(103, 194, 58, 0.1);
  border-color: #67c23a;
  color: #67c23a;
}

.el-button--success:hover {
  background-color: rgba(103, 194, 58, 0.3);
}

.el-button--danger {
  background-color: rgba(245, 108, 108, 0.1);
  border-color: #f56c6c;
  color: #f56c6c;
}

.el-button--danger:hover {
  background-color: rgba(245, 108, 108, 0.3);
}

/* 状态标签 */
.status-tag {
  margin-left: 10px;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: bold;
}

/* 提升按钮的动效 */
.action-btn {
  border-radius: 4px;
  margin: 0 5px;
  padding: 8px 20px;
  transition: transform 0.3s ease, background-color 0.3s ease, box-shadow 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
}

/* 分页样式 */
.el-pagination {
  padding-top: 20px;
  display: flex;
  justify-content: center;
  background-color: #fff;
}
</style>
