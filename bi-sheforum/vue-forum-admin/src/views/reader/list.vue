<template>
  <div class="user-management-container">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <el-input
          v-model="search"
          placeholder="搜索用户..."
          clearable
          style="width: 300px"
          @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <div class="action-buttons">
        <el-button type="primary" @click="handleAddUser">
          <el-icon><Plus /></el-icon>
          <span>添加用户</span>
        </el-button>
        <el-button type="info" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <el-card shadow="never" class="user-table-card">
      <el-table
          :data="filterTableData"
          style="width: 100%"
          :border="true"
          stripe
          v-loading="loading"
          @sort-change="handleSortChange"
      >
        <!-- 头像列 -->
        <el-table-column label="头像" width="100" align="center">
          <template #default="scope">
            <el-popover
                placement="right"
                trigger="hover"
                :width="200"
            >
              <template #reference>
                <el-avatar
                    :size="50"
                    :src="scope.row.head ? `${VERCODE_URL}/upload/${scope.row.head}` : defaultAvatar"
                    :class="{'avatar-active': scope.row.status === 0}"
                />
              </template>
              <div class="avatar-preview">
                <img
                    :src="scope.row.head ? `${VERCODE_URL}/upload/${scope.row.head}` : defaultAvatar"
                    class="preview-image"
                />
              </div>
            </el-popover>
          </template>
        </el-table-column>

        <!-- 基本信息列 -->
        <el-table-column
            label="账户"
            prop="username"
            width="150"
            sortable
        />
        <el-table-column label="昵称" prop="nickname" width="150" />
        <el-table-column label="真实姓名" prop="realname" width="150" />

        <!-- 密码列（安全处理） -->
        <el-table-column label="密码" width="180">
          <template #default="scope">
            <div class="password-cell">
              <span v-if="showPassword[scope.row.id]">{{ scope.row.password }}</span>
              <span v-else>••••••••</span>
              <el-icon
                  class="eye-icon"
                  @click="togglePasswordVisibility(scope.row.id)"
              >
                <View v-if="showPassword[scope.row.id]" />
                <Hide v-else />
              </el-icon>
            </div>
          </template>
        </el-table-column>

        <!-- 其他信息列 -->
        <el-table-column label="性别" prop="sex" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.sex === '男' ? 'primary' : 'danger'" effect="plain">
              {{ scope.row.sex || '未知' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="电话" prop="phone" width="150" />

        <!-- 等级列 -->
        <el-table-column label="等级" width="80" fixed="right">
          <template #default="scope">
            <el-tag type="warning">Lv.{{ scope.row.level || 1 }}</el-tag>
          </template>
        </el-table-column>
        
        <!-- 经验列 -->
        <el-table-column label="经验" width="100" fixed="right">
          <template #default="scope">
            <span>{{ scope.row.experience || 0 }}</span>
          </template>
        </el-table-column>
        
        <!-- 积分列 -->
        <el-table-column label="积分" width="80" fixed="right">
          <template #default="scope">
            <span>{{ scope.row.points || 0 }}</span>
          </template>
        </el-table-column>
        
        <!-- 发帖数 -->
        <el-table-column label="发帖" width="70" fixed="right">
          <template #default="scope">
            <el-tag type="success" size="small">{{ scope.row.threadCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        
        <!-- 评论数 -->
        <el-table-column label="评论" width="70" fixed="right">
          <template #default="scope">
            <el-tag type="info" size="small">{{ scope.row.commentCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        
        <!-- 状态列 -->
        <el-table-column label="状态" width="120" fixed="right">
          <template #default="scope">
            <el-switch
                v-model="scope.row.status"
                :active-value="0"
                :inactive-value="1"
                active-text="启用"
                inactive-text="禁用"
                @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleEdit(scope.row)"
                :icon="Edit"
                plain
            >
              修改
            </el-button>
            <el-button
                type="success"
                size="small"
                @click="handleDetail(scope.row)"
                :icon="View"
                plain
            >
              详情
            </el-button>
            <el-dropdown trigger="click">
              <el-button type="info" size="small" :icon="More" plain />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleResetPassword(scope.row)">
                    <el-icon><RefreshLeft /></el-icon>重置密码
                  </el-dropdown-item>
                  <el-dropdown-item
                      @click="handleDelete(scope.row)"
                      divided
                  >
                    <el-icon><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20, 30, 50]"
            :background="true"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="currentUser ? '编辑用户' : '添加用户'"
        width="600px"
    >
      <el-form :model="form" label-width="100px" :rules="rules" ref="userForm">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!currentUser">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realname">
          <el-input v-model="form.realname" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio label="男" />
            <el-radio label="女" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="头像" prop="head">
          <el-upload
              class="avatar-uploader"
              :action="VERCODE_URL+'/upload'"
              :show-file-list="false"
              accept=".jpeg,.png,.jpg"
              :data="extra"
              :on-success="handleAvatarUploadSuccess"
              :before-upload="handleBeforeUpload"
          >
            <img v-if="form.head" :src="VERCODE_URL+'/upload/' + form.head" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
              v-model="form.status"
              :active-value="0"
              :inactive-value="1"
              active-text="启用"
              inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Refresh, View, Hide,
  Edit, More, Delete, RefreshLeft
} from '@element-plus/icons-vue'
import { VERCODE_URL } from '@/plugins/config.js';
import axios from 'axios';

const router = useRouter();
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 数据状态
const userData = ref([]);
const loading = ref(false);
const search = ref('');
const showPassword = reactive({});
const dialogVisible = ref(false);
const currentUser = ref(null);
const userForm = ref(null);

// 表单数据
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  realname: '',
  sex: '男',
  phone: '',
  head: '',
  status: 0
});

// 表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在6到20个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
});

// 分页相关
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 初始化加载数据
onMounted(() => {
  loadList();
});

// 加载用户列表
async function loadList() {
  loading.value = true;
  try {
    const res = await axios.get('/api/users/page', {
      params: {
        pageSize: pageSize.value,
        pageIndex: currentPage.value
      }
    });
    userData.value = res.data.list;
    total.value = res.data.total;
  } catch (error) {
    console.error('加载用户列表失败:', error);
    ElMessage.error('加载用户列表失败');
  } finally {
    loading.value = false;
  }
}

// 搜索功能
const filterTableData = computed(() => {
  const keyword = search.value.toLowerCase();
  return userData.value.filter(user =>
      !keyword ||
      user.username.toLowerCase().includes(keyword) ||
      (user.nickname && user.nickname.toLowerCase().includes(keyword)) ||
      (user.realname && user.realname.toLowerCase().includes(keyword))
  );
});

function handleSearch() {
  currentPage.value = 1;
}

// 分页处理
function handleSizeChange(size) {
  pageSize.value = size;
  loadList();
}

function handleCurrentChange(page) {
  currentPage.value = page;
  loadList();
}

// 密码显示切换
function togglePasswordVisibility(userId) {
  showPassword[userId] = !showPassword[userId];
}

// 状态切换
async function handleStatusChange(user) {
  try {
    await axios.post('/api/users', {
      id: user.id,
      status: user.status
    });
    ElMessage.success(`用户已${user.status === 0 ? '启用' : '禁用'}`);
  } catch (error) {
    console.error('状态更新失败:', error);
    ElMessage.error('状态更新失败');
    // 恢复原状态
    user.status = user.status === 0 ? 1 : 0;
  }
}

// 用户操作
function handleEdit(user) {
  currentUser.value = user;
  Object.assign(form, {
    id: user.id,
    username: user.username,
    password: '',
    nickname: user.nickname,
    realname: user.realname,
    sex: user.sex,
    phone: user.phone,
    head: user.head,
    status: user.status
  });
  dialogVisible.value = true;
}

function handleAddUser() {
  currentUser.value = null;
  resetForm();
  dialogVisible.value = true;
}

function handleDetail(user) {
  router.push(`/reader/detail?id=${user.id}`);
}

function resetForm() {
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    realname: '',
    sex: '男',
    phone: '',
    head: '',
    status: 0
  });
}

async function handleResetPassword(user) {
  try {
    await ElMessageBox.confirm(
        `确定要重置用户 ${user.username} 的密码吗?`,
        '提示',
        { type: 'warning' }
    );

    const res = await axios.post('/api/users/reset-password', {
      userId: user.id
    });

    ElMessage.success(`密码已重置为: ${res.data.newPassword}`);
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error);
      ElMessage.error('重置密码失败');
    }
  }
}

async function handleDelete(user) {
  try {
    await ElMessageBox.confirm(
        `确定要删除用户 ${user.username} 吗?`,
        '警告',
        { type: 'error' }
    );

    await axios.delete(`/api/users/${user.id}`);
    ElMessage.success('用户删除成功');
    loadList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error);
      ElMessage.error('删除用户失败');
    }
  }
}

// 对话框提交
async function submitForm() {
  try {
    await userForm.value.validate();

    if (currentUser.value) {
      // 更新用户
      await updateUser();
    } else {
      // 添加用户
      await addUser();
    }

    dialogVisible.value = false;
    loadList();
    ElMessage.success(currentUser.value ? '用户信息更新成功' : '用户添加成功');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error);
    }
  }
}

async function updateUser() {
  const { id, username, nickname, realname, sex, phone, head, status } = form;
  await axios.put('/api/users', {
    id,
    username,
    nickname,
    realname,
    sex,
    phone,
    head,
    status
  });
}

async function addUser() {
  const { username, password, nickname, realname, sex, phone, head, status } = form;
  await axios.post('/api/users/add', {
    username,
    password,
    nickname,
    realname,
    sex,
    phone,
    head,
    status
  });
}

// 刷新数据
function refreshData() {
  loadList();
}

// 排序处理
function handleSortChange({ column, prop, order }) {
  // 这里可以添加排序逻辑
  console.log('排序:', prop, order);
}

// 图片上传处理
const extra = { type: 'common' };

const handleAvatarUploadSuccess = ({ status, msg, data }) => {
  if (status) {
    form.head = data;
    ElMessage.success(msg);
  } else {
    ElMessage.error(msg);
  }
}

const handleBeforeUpload = (rawFile) => {
  const isValidType = /^image\/(jpeg|png|jpg)$/.test(rawFile.type);
  const isValidSize = rawFile.size / 1024 / 1024 <= 2;

  if (!isValidType) {
    ElMessage.error('封面图片必须是JPG/PNG格式!');
    return false;
  }
  if (!isValidSize) {
    ElMessage.error('封面图片大小不能超过2MB!');
    return false;
  }

  return true;
}
</script>

<style scoped>
.user-management-container {
  padding: 20px;
  background-color: #f5f7fa;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.user-table-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.avatar-active {
  box-shadow: 0 0 0 2px var(--el-color-primary);
}

.avatar-preview {
  text-align: center;
}

.preview-image {
  max-width: 180px;
  max-height: 180px;
  border-radius: 4px;
}

.password-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.eye-icon {
  cursor: pointer;
  color: var(--el-color-primary);
  transition: color 0.3s;
}

.eye-icon:hover {
  color: var(--el-color-primary-light-3);
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  margin-top: 20px;
  border-top: 1px solid var(--el-border-color-light);
}

/* 表格行悬停效果 */
:deep(.el-table__body tr:hover>td) {
  background-color: var(--el-color-primary-light-9) !important;
}

/* 表格单元格样式 */
:deep(.el-table td) {
  padding: 12px 0;
}

/* 表头样式 */
:deep(.el-table th) {
  background-color: #f8fafc;
  font-weight: 600;
  color: #333;
}

/* 按钮组样式 */
.action-buttons {
  display: flex;
  gap: 10px;
}

/* 对话框中的头像上传样式 */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-uploader :deep(.el-icon) {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>