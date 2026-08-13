<template>
  <div class="thread-management-container">
    <el-card shadow="hover" class="management-card">
      <div class="search-container">
        <el-input
            v-model="search"
            placeholder="搜索帖子标题或发帖人..."
            clearable
            @clear="handleSearchClear"
            @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
      </div>

      <el-table
          :data="filterTableData"
          style="width: 100%"
          :row-class-name="tableRowClassName"
          @row-click="handleRowClick"
          v-loading="loading"
      >
        <el-table-column label="封面" width="180">
          <template #default="scope">
            <div class="cover-container">
              <el-image
                  v-if="scope.row.picture"
                  :src="VERCODE_URL+'/upload/' + scope.row.picture"
                  :preview-src-list="[VERCODE_URL+'/upload/' + scope.row.picture]"
                  fit="cover"
                  class="cover-image"
                  :class="{'has-cover': scope.row.picture}"
              >
                <template #error>
                  <div class="no-cover">无封面</div>
                </template>
              </el-image>
              <div v-else class="no-cover">无封面</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="标题" prop="name" min-width="180">
          <template #default="scope">
            <el-tooltip :content="scope.row.name" placement="top">
              <span class="title-text">{{ scope.row.name }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="发帖人" prop="writer" width="120" />

        <el-table-column label="简介" prop="introduction" min-width="200">
          <template #default="scope">
            <el-tooltip :content="scope.row.introduction" placement="top">
              <span class="intro-text">{{ truncateIntro(scope.row.introduction) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="收藏量" sortable prop="collectnum" width="100">
          <template #default="scope">
            <el-tag effect="plain" type="info">
              <el-icon><star /></el-icon> {{ scope.row.collectnum }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="类型" prop="threadsSort.name" width="120" />

        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-popover
                placement="top-start"
                :width="200"
                trigger="hover"
                :content="getStatusDescription(scope.row)"
            >
              <template #reference>
                <el-tag
                    :type="getStatusTagType(scope.row)"
                    effect="light"
                    class="status-tag"
                >
                  {{ getStatusText(scope.row) }}
                </el-tag>
              </template>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                  type="primary"
                  size="small"
                  plain
                  @click.stop="handleEdit(scope.row)"
                  class="action-btn"
              >
                <el-icon><edit /></el-icon> 编辑
              </el-button>
              <el-dropdown
                  trigger="click"
                  @command="handleCommand"
                  @click.stop
              >
                <el-button
                    type="success"
                    size="small"
                    plain
                    class="action-btn"
                >
                  <el-icon><more /></el-icon> 更多
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                        :command="{ action: 'publish', row: scope.row }"
                        :disabled="scope.row.isupload === 0"
                    >
                      <el-icon><circle-check /></el-icon> 公开
                    </el-dropdown-item>
                    <el-dropdown-item
                        :command="{ action: 'ban', row: scope.row }"
                        :disabled="scope.row.isupload === 1"
                    >
                      <el-icon><warning /></el-icon> 封禁
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            background
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { useThreadsStore } from '@/stores/threads';
import { ElMessage } from 'element-plus';
import { Search, Edit, Star, More, CircleCheck, Warning } from '@element-plus/icons-vue';

const router = useRouter();
const threadStore = useThreadsStore();

// 数据状态
const threadData = ref([]);
const loading = ref(false);
const search = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 加载列表数据
const loadList = async () => {
  loading.value = true;
  try {
    const res = await axios({
      method: 'get',
      url: '/api/threads',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      params: { pageSize: pageSize.value, pageIndex: currentPage.value }
    });

    res.data.list.forEach(item => {
      item.threadsSortname = item.threadsSort.name;
    });

    threadData.value = res.data.list;
    total.value = res.data.total;
  } catch (error) {
    console.error('加载数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 初始化加载
onMounted(() => {
  loadList();
});

// 过滤表格数据
const filterTableData = computed(() => {
  return threadData.value.filter(data => {
    return !search.value ||
        data.name.toLowerCase().includes(search.value.toLowerCase()) ||
        data.writer.toLowerCase().includes(search.value.toLowerCase());
  });
});

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val;
  loadList();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadList();
};

// 操作处理
const handleEdit = (row) => {
  router.push(`/thread/edit?id=${row.id}`);
};

const handleCommand = async (command) => {
  try {
    const { action, row } = command;
    const updateData = { id: row.id };

    if (action === 'publish') {
      updateData.isupload = 0;
    } else if (action === 'ban') {
      updateData.isupload = 1;
    }

    await axios({
      method: 'post',
      url: '/api/threads/update',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      data: updateData
    });

    ElMessage.success('操作成功');
    loadList();
  } catch (error) {
    ElMessage.error('操作失败');
    console.error(error);
  }
};

// 辅助函数
const truncateIntro = (text) => {
  return text?.length > 30 ? text.substring(0, 30) + '...' : text;
};

const getStatusText = (row) => {
  if (row.isupload === 1) return '已封禁';
  return row.status === 0 ? '坟贴' : '更新中';
};

const getStatusTagType = (row) => {
  if (row.isupload === 1) return 'danger';
  return row.status === 0 ? 'success' : 'primary';
};

const getStatusDescription = (row) => {
  if (row.isupload === 1) return '该帖子已被管理员封禁';
  return row.status === 0 ? '该帖子已停止更新' : '该帖子正在持续更新中';
};

const tableRowClassName = ({ row }) => {
  return row.isupload === 1 ? 'disabled-row' : '';
};

const handleRowClick = (row) => {
  if (row.isupload !== 1) {
    // 可以添加点击行的处理逻辑
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  loadList();
};

const handleSearchClear = () => {
  search.value = '';
  handleSearch();
};
</script>

<style scoped lang="scss">
.thread-management-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 40px);
}

.management-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  }
}

.search-container {
  padding: 15px 20px;
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;

  .el-input {
    max-width: 400px;
  }
}

.cover-container {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;

  .cover-image {
    width: 100%;
    height: 100%;
    border-radius: 4px;
    transition: transform 0.3s ease;

    &:hover {
      transform: scale(1.02);
    }

    &.has-cover {
      border: 1px solid #ebeef5;
    }
  }

  .no-cover {
    color: #909399;
    font-size: 14px;
    height: 100%; /* 设置固定高度 */
    display: flex;
    justify-content: center; /* 水平居中 */
    align-items: center;     /* 垂直居中 */
    background: #ffffff;
    border-radius: 5px;
    border: 1px dashed #dcdfe6;
  }


  .image-error {
    color: #f56c6c;
    font-size: 14px;
    padding: 10px;
    text-align: center;
  }
}

.title-text {
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s;

  &:hover {
    color: var(--el-color-primary);
  }
}

.intro-text {
  color: #606266;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-tag {
  cursor: default;
  user-select: none;
}

.action-buttons {
  display: flex;
  gap: 8px;

  .action-btn {
    transition: all 0.2s;

    &:hover {
      transform: translateY(-1px);
    }
  }
}

.pagination-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
}

:deep(.el-table) {
  .disabled-row {
    opacity: 0.7;
    background-color: #fafafa;

    td {
      color: #c0c4cc !important;
    }
  }

  .el-table__row {
    transition: all 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    }
  }
}
</style>