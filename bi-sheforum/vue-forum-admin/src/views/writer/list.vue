<template>
  <div class="enhanced-table-container">
    <el-card shadow="hover" class="table-card">
      <!-- 增强的表格头部 -->
      <div class="table-header">
        <h3 class="table-title">版主/达人列表</h3>
        <div class="header-actions">
          <el-input
              v-model="search"
              placeholder="搜索名字、地址、简介..."
              clearable
              style="width: 300px"
              @clear="handleSearchClear"
              @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="loadList" :icon="Refresh">刷新</el-button>
          <el-button type="success" @click="handleAddNew" :icon="Plus">新增版主</el-button>
        </div>
      </div>

      <el-table
          :data="filteredData"
          style="width: 100%"
          v-loading="loading"
          :row-class-name="tableRowClassName"
          stripe
          border
      >
        <!-- 头像列 - 修改为与第一个表格相同的显示方式 -->
        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-avatar
                :size="60"
                :src="getAvatarUrl(scope.row.head)"
                @error="handleAvatarError"
            >
              <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png" />
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column label="姓名" prop="name" sortable min-width="120">
          <template #default="scope">
            <div class="name-cell">
              <span>{{ scope.row.name || '-' }}</span>
              <el-tooltip v-if="scope.row.introduction" effect="light" placement="top">
                <template #content>
                  <div style="max-width: 300px">{{ scope.row.introduction }}</div>
                </template>
                <el-icon class="info-icon"><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <!-- 出生日期列 - 增强日期显示 -->
        <el-table-column label="出生日期" prop="birthday" sortable width="140">
          <template #default="scope">
            <el-tag v-if="scope.row.birthday" type="info">
              {{ formatBirthday(scope.row.birthday) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="性别" prop="sex" width="80" align="center">
          <template #default="scope">
            <el-icon v-if="scope.row.sex === '男'" color="#409EFF"><Male /></el-icon>
            <el-icon v-else-if="scope.row.sex === '女'" color="#F56C6C"><Female /></el-icon>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="地址" prop="address" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.address || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="简介" prop="introduction" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ scope.row.introduction || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag
                :type="scope.row.status === 0 ? 'success' : 'danger'"
                effect="dark"
                class="status-tag"
            >
              {{ scope.row.status === 0 ? '可用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <el-button-group>
              <el-tooltip content="编辑" placement="top">
                <el-button
                    type="primary"
                    size="small"
                    @click="handleEdit(scope.$index, scope.row)"
                    :icon="Edit"
                    circle
                />
              </el-tooltip>

              <el-tooltip :content="scope.row.status === 0 ? '已公开' : '设为公开'" placement="top">
                <el-button
                    :type="scope.row.status === 0 ? 'success' : 'default'"
                    size="small"
                    @click="scope.row.status === 1 && handleStatusChange(scope.row, 0)"
                    :disabled="scope.row.status === 0"
                    :icon="View"
                    circle
                />
              </el-tooltip>

              <el-tooltip :content="scope.row.status === 1 ? '已隐藏' : '设为隐藏'" placement="top">
                <el-button
                    :type="scope.row.status === 1 ? 'danger' : 'default'"
                    size="small"
                    @click="scope.row.status === 0 && handleStatusChange(scope.row, 1)"
                    :disabled="scope.row.status === 1"
                    :icon="Hide"
                    circle
                />
              </el-tooltip>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 增强的分页器 -->
      <div class="table-footer">
        <div class="footer-info">
          显示 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, total) }} 条，共 {{ total }} 条
        </div>
        <el-pagination
            background
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="smart-pagination"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, Plus, Edit, View, Hide,
  InfoFilled, Male, Female
} from '@element-plus/icons-vue'

// 确保正确导入VERCODE_URL
import { VERCODE_URL } from '@/plugins/config.js'

const defaultAvatar = 'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'
const router = useRouter()
const writerData = ref([])
const loading = ref(false)
const search = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取完整头像URL
const getAvatarUrl = (head) => {
  if (!head) return defaultAvatar
  return head.startsWith('http') ? head : `${VERCODE_URL}/upload/${head}`
}

// 处理头像加载错误
const handleAvatarError = () => {
  return true
}

// 增强的日期格式化函数
const formatBirthday = (dateStr) => {
  if (!dateStr) return ''

  // 处理"1989年1月"这种格式
  if (dateStr.includes('年')) {
    return dateStr
  }

  // 处理标准日期格式
  try {
    const date = new Date(dateStr)
    if (isNaN(date.getTime())) return dateStr

    const year = date.getFullYear()
    const month = date.getMonth() + 1
    return `${year}年${month}月`
  } catch {
    return dateStr
  }
}

// 加载数据
onMounted(() => {
  loadList()
})

async function loadList() {
  try {
    loading.value = true
    const response = await axios.get('/api/writer', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })

    // 处理数据确保head字段存在
    writerData.value = response.data.map(item => ({
      ...item,
      head: item.head || null
    }))

    total.value = response.data.total || response.data.length
  } catch (error) {
    loading.value = false
    ElMessage.error('加载数据失败')
    console.error(error)

    // 模拟数据 - 包含各种日期格式
    writerData.value = [
      {
        id: 1,
        name: "张三",
        birthday: "1990年5月",  // 中文格式
        sex: "男",
        address: "北京市朝阳区建国路88号",
        introduction: "资深作家，代表作《编程艺术》",
        head: "avatar1.jpg",
        status: 0
      },
      {
        id: 2,
        name: "李四",
        birthday: "1985-11-22",  // 标准日期格式
        sex: "女",
        address: "上海市浦东新区张江高科技园区",
        introduction: "新生代作家，擅长科幻小说创作",
        head: null,  // 无头像
        status: 1
      }
    ]
    total.value = writerData.value.length
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  let date

  // 处理"1989年1月"这种格式
  if (dateStr.includes('年')) {
    return dateStr
  }

  // 处理标准日期格式
  try {
    date = new Date(dateStr.replace('T', ' ').replace(/\.\d{3}Z$/, ''))
    if (isNaN(date.getTime())) return dateStr

    const year = date.getFullYear()
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${year}年${month}月${day}日`
  } catch {
    return dateStr
  }
}

// 表格行类名
const tableRowClassName = ({ row }) => {
  return row.status === 1 ? 'disabled-row' : ''
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
}

const handleSearchClear = () => {
  search.value = ''
  handleSearch()
}

// 计算过滤后的数据
const filteredData = computed(() => {
  let data = writerData.value

  // 搜索筛选
  if (search.value) {
    const keyword = search.value.toLowerCase()
    data = data.filter(item =>
        (item.name && item.name.toLowerCase().includes(keyword)) ||
        (item.address && item.address.toLowerCase().includes(keyword)) ||
        (item.introduction && item.introduction.toLowerCase().includes(keyword))
    )
  }

  // 分页处理
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return data.slice(start, end)
})

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadList()
}

// 操作按钮
const handleEdit = (index, row) => {
  router.push(`/writer/edit?id=${row.id}`)
}

const handleAddNew = () => {
  router.push('/writer/add')
}

const handleStatusChange = async (row, newStatus) => {
  try {
    await axios.post('/api/users', { id: row.id, status: newStatus })
    ElMessage.success('状态更新成功')
    loadList()
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error(error)
  }
}
</script>

<style scoped>
.enhanced-table-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.table-card {
  border-radius: 8px;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
  padding: 0 20px;
  padding-top: 20px;
}

.table-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-hover {
  transition: transform 0.3s;
  cursor: pointer;
}

.avatar-hover:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-preview {
  padding: 10px;
  text-align: center;
}

.name-cell {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.info-icon {
  color: #909399;
  cursor: help;
  margin-left: 5px;
}

.status-tag {
  font-weight: bold;
  letter-spacing: 1px;
}

.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 15px 20px;
  border-top: 1px solid #ebeef5;
}

.footer-info {
  color: #909399;
  font-size: 14px;
}

.smart-pagination {
  margin: 0;
}

/* 禁用行的样式 */
:deep(.disabled-row) {
  opacity: 0.7;
  background-color: #fafafa;
}

:deep(.disabled-row:hover > td) {
  background-color: #f5f5f5 !important;
}

/* 表格悬停效果 */
:deep(.el-table__body tr:hover > td) {
  background-color: #f0f7ff !important;
}

/* 按钮组样式 */
.el-button-group {
  display: flex;
  gap: 5px;
}

.el-button.is-circle {
  width: 32px;
  height: 32px;
  padding: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .table-footer {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  .el-avatar {
    background-color: #f5f7fa;
  }
}
</style>