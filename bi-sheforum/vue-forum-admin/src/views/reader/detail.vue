<template>
  <div class="user-settings-container">
    <!-- 基本设置区域 -->
    <el-card shadow="never" class="settings-card">
      <div class="settings-header">
        <h2 class="settings-title">
          <el-icon><User /></el-icon>
          <span>基本设置</span>
        </h2>
        <el-button
            type="primary"
            @click="submitForm(ruleFormRef)"
            :loading="saving"
            :icon="Check"
        >
          保存设置
        </el-button>
      </div>

      <el-form
          ref="ruleFormRef"
          :model="ruleForm"
          :rules="rules"
          label-width="120px"
          label-position="top"
          class="settings-form"
      >
        <div class="form-grid">
          <!-- 左侧表单 -->
          <div class="form-left">
            <el-form-item label="用户名" prop="username">
              <el-input
                  v-model="ruleForm.username"
                  disabled
                  placeholder="系统用户名"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                  v-model="ruleForm.password"
                  type="password"
                  show-password
                  placeholder="输入新密码"
                  clearable
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
              <div class="form-tip">密码长度8-20位，包含字母和数字</div>
            </el-form-item>

            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="ruleForm.sex">
                <el-radio-button label="男">
                  <el-icon><Male /></el-icon>
                  <span>男</span>
                </el-radio-button>
                <el-radio-button label="女">
                  <el-icon><Female /></el-icon>
                  <span>女</span>
                </el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input
                  v-model="ruleForm.nickname"
                  placeholder="输入您的昵称"
                  clearable
              >
                <template #prefix>
                  <el-icon><EditPen /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="真实姓名" prop="realname">
              <el-input
                  v-model="ruleForm.realname"
                  placeholder="输入真实姓名"
                  clearable
              >
                <template #prefix>
                  <el-icon><Document /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>

          <!-- 右侧头像上传 -->
          <div class="form-right">
            <el-form-item label="头像上传">
              <div class="avatar-upload-container">
                <div class="upload-instructions">
                  <el-alert
                      title="上传说明"
                      type="info"
                      :closable="false"
                  >
                    <ul class="instruction-list">
                      <li>支持 JPG/PNG 格式图片</li>
                      <li>文件大小不超过 5MB</li>
                      <li>图片尺寸建议 200×200 像素以上</li>
                      <li>新头像需审核后生效</li>
                    </ul>
                  </el-alert>
                </div>

                <el-upload
                    class="avatar-uploader"
                    :action="VERCODE_URL+'/upload'"
                    :show-file-list="false"
                    accept=".jpeg,.png,.jpg"
                    :data="extra"
                    :on-success="handleAvatarSuccess"
                    :on-error="handleUploadError"
                    :before-upload="handleBeforeUpload"
                >
                  <div class="avatar-preview">
                    <div v-if="ruleForm.head" class="avatar-wrapper">
                      <img
                          :src="VERCODE_URL+'/upload/' + ruleForm.head"
                          class="avatar-image"
                      />
                      <div class="avatar-overlay">
                        <el-icon><Camera /></el-icon>
                        <span>更换头像</span>
                      </div>
                    </div>
                    <div v-else class="avatar-empty">
                      <el-icon><Plus /></el-icon>
                      <span>上传头像</span>
                    </div>
                  </div>
                </el-upload>
              </div>
            </el-form-item>
          </div>
        </div>
      </el-form>
    </el-card>

    <!-- 用户文件区域 -->
    <el-card shadow="never" class="files-card">
      <div class="files-header">
        <h2 class="files-title">
          <el-icon><Folder /></el-icon>
          <span>我的贴子</span>
        </h2>
        <el-button
            type="primary"
            plain
            @click="refreshFiles"
            :icon="Refresh"
        >
          刷新列表
        </el-button>
      </div>

      <div v-if="uploadlist.length > 0" class="file-grid">
        <div
            v-for="item in uploadlist"
            :key="item.id"
            class="file-card"
            @click="previewFile(item)"
        >
          <div class="file-thumbnail">
            <img
                v-if="isImage(item.name)"
                :src="getFileThumbnail(item)"
                class="thumbnail-image"
            />
            <div v-else class="file-icon">
              <el-icon v-if="isDocument(item.name)"><Document /></el-icon>
              <el-icon v-else><Files /></el-icon>
            </div>
            <div class="file-actions">
              <el-button
                  circle
                  type="danger"
                  :icon="Delete"
                  @click.stop="confirmDelete(item)"
              />
            </div>
          </div>
          <div class="file-info">
            <el-tooltip :content="item.name" placement="top">
              <div class="file-name">{{ truncateName(item.name) }}</div>
            </el-tooltip>
            <div class="file-date">{{ formatDate(item.createdAt) }}</div>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无上传文件" />
    </el-card>

    <!-- 删除确认对话框 -->
    <el-dialog
        v-model="deleteDialogVisible"
        title="确认删除"
        width="400px"
        align-center
    >
      <div class="delete-confirm">
        <el-icon color="#F56C6C" :size="60"><Warning /></el-icon>
        <p>确定要删除贴子 "{{ selectedFile?.name }}" 吗？</p>
        <p class="delete-warning">此操作不可撤销，请谨慎操作！</p>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button
            type="danger"
            @click="handleRemove"
            :loading="deleting"
        >
          确认删除
        </el-button>
      </template>
    </el-dialog>

    <!-- 贴子预览对话框 -->
    <el-dialog
        v-model="previewDialogVisible"
        :title="previewFileTitle"
        width="70%"
        fullscreen
        @close="closePreview"
    >
      <div v-if="previewLoading" class="preview-loading">
        <el-icon class="loading-icon" :size="50"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
      <div v-else class="preview-content">
        <img
            v-if="isImage(previewFileTitle)"
            :src="previewUrl"
            class="preview-image"
        />
        <iframe
            v-else-if="isDocument(previewFileTitle)"
            :src="previewUrl"
            class="preview-document"
        ></iframe>
        <div v-else class="preview-unsupported">
          <el-icon><Warning /></el-icon>
          <p>不支持预览此文件类型</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  User, Check, Lock, Male, Female, EditPen, Document,
  Folder, Refresh, Delete, Warning, Camera, Plus,
  Loading, Files
} from '@element-plus/icons-vue';
import { VERCODE_URL } from '@/plugins/config.js';
import axios from 'axios';
import dayjs from 'dayjs';

const router = useRouter();
const route = useRoute();

// 表单相关
const ruleFormRef = ref();
const ruleForm = ref({});
const saving = ref(false);
const formSize = ref('default');

// 文件相关
const uploadlist = ref([]);
const deleteDialogVisible = ref(false);
const deleting = ref(false);
const selectedFile = ref(null);
const previewDialogVisible = ref(false);
const previewLoading = ref(false);
const previewUrl = ref('');
const previewFileTitle = ref('');

// 表单验证规则
const rules = reactive({
  password: [
    { min: 8, max: 20, message: '密码长度8-20位', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: '需包含字母和数字', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 12, message: '长度2-12个字符', trigger: 'blur' }
  ],
  realname: [
    { pattern: /^[\u4e00-\u9fa5]{2,10}$/, message: '请输入有效中文姓名', trigger: 'blur' }
  ]
});

// 初始化加载数据
onMounted(() => {
  loadData();
});

async function loadData() {
  try {
    const [userRes, uploadsRes] = await Promise.all([
      axios.get('api/users/one', { params: { id: route.query.id } }),
      axios.get('api/useruploads', { params: { id: route.query.id } })
    ]);

    ruleForm.value = userRes.data;
    uploadlist.value = uploadsRes.data.map(item => ({
      ...item,
      name: item.upload.split("_")[1]
    }));
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败');
  }
}

// 头像上传处理
const extra = { type: 'common' };

const handleAvatarSuccess = ({ status, msg, data }) => {
  if (status) {
    ruleForm.value.head = data;
    ElMessage.success(msg);
  } else {
    ElMessage.error(msg);
  }
};

const handleUploadError = (error) => {
  const { status, msg } = JSON.parse(error.message);
  ElMessage.error(msg);
};

const handleBeforeUpload = (rawFile) => {
  const isValidType = /^image\/(jpeg|png|jpg)$/.test(rawFile.type);
  const isValidSize = rawFile.size / 1024 / 1024 <= 5;

  if (!isValidType) {
    ElMessage.error('仅支持JPG/PNG格式图片!');
    return false;
  }
  if (!isValidSize) {
    ElMessage.error('图片大小不能超过5MB!');
    return false;
  }

  return true;
};

// 表单提交
const submitForm = async (formEl) => {
  if (!formEl) return;

  try {
    saving.value = true;
    await formEl.validate();

    await axios.post('api/users/update', { ...ruleForm.value });
    ElMessage.success('设置保存成功');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('保存失败:', error);
      ElMessage.error('保存失败');
    }
  } finally {
    saving.value = false;
  }
};

// 文件操作
function confirmDelete(file) {
  selectedFile.value = file;
  deleteDialogVisible.value = true;
}

async function handleRemove() {
  try {
    deleting.value = true;
    await Promise.all([
      axios.delete('/upload', { params: { file: selectedFile.value.upload } }),
      axios.delete('api/useruploads', { params: { id: selectedFile.value.id } })
    ]);

    await loadData();
    ElMessage.success('贴子删除成功');
    deleteDialogVisible.value = false;
  } catch (error) {
    console.error('删除失败:', error);
    ElMessage.error('贴子删除失败');
  } finally {
    deleting.value = false;
  }
}

function previewFile(file) {
  previewFileTitle.value = file.name;
  previewLoading.value = true;
  previewDialogVisible.value = true;

  // 模拟异步加载
  setTimeout(() => {
    previewUrl.value = `${VERCODE_URL}/upload/${file.upload}`;
    previewLoading.value = false;
  }, 800);
}

function closePreview() {
  previewUrl.value = '';
}

function refreshFiles() {
  loadData();
}

// 工具函数
function isImage(filename) {
  return /\.(jpg|jpeg|png|gif|webp)$/i.test(filename);
}

function isDocument(filename) {
  return /\.(pdf|doc|docx|xls|xlsx|ppt|pptx)$/i.test(filename);
}

function getFileThumbnail(file) {
  if (isImage(file.name)) {
    return `${VERCODE_URL}/upload/${file.upload}`;
  }
  return './img/封面.jpg';
}

function truncateName(name, length = 15) {
  if (name.length <= length) return name;
  return name.substring(0, length) + '...';
}

function formatDate(dateString) {
  return dayjs(dateString).format('YYYY-MM-DD HH:mm');
}
</script>

<style scoped>
.user-settings-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.settings-card, .files-card {
  margin-bottom: 24px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.settings-header, .files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
}

.settings-title, .files-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.settings-title .el-icon, .files-title .el-icon {
  font-size: 20px;
}

.settings-form {
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

.form-left {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-right {
  display: flex;
  justify-content: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.avatar-upload-container {
  width: 100%;
  max-width: 320px;
}

.upload-instructions {
  margin-bottom: 20px;
}

.instruction-list {
  margin: 8px 0 0 20px;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.avatar-preview {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  border: 1px dashed #dcdfe6;
  transition: all 0.3s;
}

.avatar-preview:hover {
  border-color: var(--el-color-primary);
}

.avatar-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay, .avatar-empty {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-empty {
  background-color: #f5f7fa;
  color: #909399;
  opacity: 1;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon, .avatar-empty .el-icon {
  font-size: 30px;
  margin-bottom: 8px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  padding: 16px;
}

.file-card {
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  cursor: pointer;
}

.file-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.file-thumbnail {
  position: relative;
  width: 100%;
  height: 140px;
  background-color: #f5f7fa;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
}

.file-icon .el-icon {
  font-size: 40px;
}

.file-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.file-card:hover .file-actions {
  opacity: 1;
}

.file-info {
  padding: 12px;
}

.file-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-date {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.delete-confirm {
  text-align: center;
  padding: 20px 0;
}

.delete-confirm p {
  margin: 16px 0;
  font-size: 16px;
}

.delete-warning {
  color: #f56c6c;
  font-weight: bold;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
}

.loading-icon {
  animation: rotate 2s linear infinite;
}

.preview-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-document {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
}

.preview-unsupported .el-icon {
  font-size: 50px;
  margin-bottom: 16px;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-right {
    justify-content: flex-start;
  }

  .file-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}
</style>