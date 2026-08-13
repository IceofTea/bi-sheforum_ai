<template>
  <div class="form-container">
    <div class="form-header">
      <h2>编辑内容</h2>
      <p>修改内容信息</p>
    </div>

    <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="rules"
        label-width="120px"
        class="advanced-form"
        :size="formSize"
        status-icon
        label-position="top"
    >
      <!-- 标题 -->
      <el-form-item label="标题" prop="name">
        <el-input
            v-model="ruleForm.name"
            placeholder="请输入内容标题"
            clearable
            @focus="showTitleTip = true"
            @blur="showTitleTip = false"
        />
        <transition name="fade">
          <div v-if="showTitleTip" class="input-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>建议使用简洁明了的标题，不超过30个字</span>
          </div>
        </transition>
      </el-form-item>

<!-- 发帖人 -->
      <el-form-item label="发帖人" prop="writer">
        <el-select
            v-model="ruleForm.writer"
            placeholder="请选择发帖人"
            filterable
        >
          <el-option
              v-for="item in writerlist"
              :key="item.id"
              :label="item.name"
              :value="item.name"
          >
            <div class="writer-option">
              <el-avatar :size="24" :src="item.head ? getAvatarUrl(item.head) : defaultAvatar" />
              <span>{{ item.name }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 分类选择 -->
      <div class="form-row">
        <el-form-item label="一级分类" prop="parent" class="category-item">
          <el-select
              v-model="ruleForm.parent"
              placeholder="请选择一级分类"
              @change="switchlist(ruleForm.parent)"
              @visible-change="handleCategoryHover(true)"
              @mouseleave="handleCategoryHover(false)"
          >
            <el-option
                v-for="item in parentslist"
                :key="item.id"
                :label="item.name"
                :value="item.name"
            />
          </el-select>
          <div v-if="categoryHover" class="category-visual">
            <div
                v-for="item in parentslist"
                :key="item.id"
                :class="{active: ruleForm.parent === item.name}"
                @click="ruleForm.parent = item.name; switchlist(item.name)"
            >
              {{ item.name }}
            </div>
          </div>
        </el-form-item>

        <el-form-item label="二级分类" prop="threadsSortId" class="category-item">
          <el-select
              v-model="ruleForm.threadsSortId"
              placeholder="请选择二级分类"
              :disabled="!ruleForm.parent"
          >
            <el-option
                v-for="item in sortlist"
                :key="item.id"
                :label="item.name"
                :value="item.name"
            />
          </el-select>
        </el-form-item>
      </div>

      <!-- 状态选择 -->
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="ruleForm.status" class="status-radio">
          <el-radio-button label="1">
            <el-icon><Refresh /></el-icon>
            <span>更新中</span>
          </el-radio-button>
          <el-radio-button label="0">
            <el-icon><Clock /></el-icon>
            <span>坟贴</span>
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- 简介 -->
      <el-form-item label="简介" prop="introduction">
        <el-input
            v-model="ruleForm.introduction"
            type="textarea"
            :rows="4"
            placeholder="请输入内容简介"
            show-word-limit
            maxlength="300"
        />
        <div class="word-count">{{ ruleForm.introduction.length }}/300</div>
      </el-form-item>

      <!-- 文件上传 -->
      <el-form-item label="内容文件">
        <el-upload
            class="upload-card"
            :action="VERCODE_URL+'/upload'"
            multiple
            :on-remove="handleRemove"
            :limit="3"
            :on-success="handleSuccess"
            :before-remove="beforeRemove"
            :file-list="fileList"
        >
          <el-icon class="upload-icon"><Upload /></el-icon>
          <div class="upload-text">
            <p>点击或拖拽文件到此处上传</p>
            <p class="upload-hint">支持.txt格式，单个文件不超过10MB</p>
          </div>
        </el-upload>
      </el-form-item>

      <!-- 封面图片 -->
      <el-form-item label="封面图片">
        <div class="cover-uploader">
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
            <div v-if="ruleForm.picture" class="cover-preview">
              <img :src="VERCODE_URL+'/upload/' + ruleForm.picture" class="avatar" />
              <div class="cover-hover">
                <el-icon><Edit /></el-icon>
                <span>更换封面</span>
              </div>
            </div>
            <div v-else class="cover-empty">
              <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
              <p>点击上传封面</p>
              <p class="cover-hint">建议尺寸：800×400像素，不超过2MB</p>
            </div>
          </el-upload>
        </div>
      </el-form-item>

      <!-- 表单操作 -->
      <el-form-item class="form-actions">
        <el-button
            type="primary"
            @click="submitForm(ruleFormRef)"
            :loading="isSubmitting"
            :icon="Promotion"
            round
        >
          {{ isSubmitting ? '提交中...' : '保存修改' }}
        </el-button>
        <el-button
            @click="resetForm(ruleFormRef)"
            :icon="RefreshLeft"
            round
        >
          重置表单
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import axios from 'axios';
import { reactive, ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Plus, Upload, Edit, Refresh, Clock,
  InfoFilled, Promotion, RefreshLeft
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');

// 表单数据
const ruleFormRef = ref();
const ruleForm = reactive({
  name: '',
  writer: '',
  parent: '',
  threadsSortId: '',
  status: '1',
  introduction: '',
  picture: '',
  text: ''
});

// UI状态
const formSize = ref('default');
const isSubmitting = ref(false);
const showTitleTip = ref(false);
const categoryHover = ref(false);
const fileList = ref([]);

// 数据列表
const parentslist = ref([]);
const sortlist = ref([]);
const allsort = ref([]);
const writerlist = ref([]);
const uploadfile = ref('');

// 初始化数据
onMounted(() => {
  loadList();
  parentlist();
  writers();
});

// 加载编辑数据
const loadList = () => {
  axios.get('/api/threads/one', { params: route.query })
      .then((res) => {
        Object.assign(ruleForm, res.data);
        ruleForm.parent = res.data.threadsSort?.parentcontent?.name || '';
        ruleForm.threadsSortId = res.data.threadsSort?.name || '';
        ruleForm.status = res.data.status || '1';
      });
};

// 获取作者列表
const writers = () => {
  axios.get('/api/writer')
      .then((res) => {
        writerlist.value = res.data;
      });
};

// 获取分类列表
const parentlist = () => {
  axios.get('/api/sort')
      .then((res) => {
        allsort.value = res.data;
        parentslist.value = res.data.filter(item => item.parent == 1);
      });
};

// 切换二级分类
const switchlist = (name) => {
  sortlist.value = [];
  allsort.value.forEach((item) => {
    if (item.parentcontent != null && item.parentcontent.name == name) {
      sortlist.value.push(item);
    }
  });
};

// 分类选择可视化
const handleCategoryHover = (isHover) => {
  if (parentslist.value.length > 0) {
    categoryHover.value = isHover;
  }
};

// 文件上传处理
const handleRemove = (file, uploadFiles) => {
  axios.delete('/upload', { params: { file: uploadfile.value } })
      .then(res => console.log(res))
      .catch(err => console.error(err));
};

const beforeRemove = (uploadFile) => {
  return ElMessageBox.confirm(`确定移除 ${uploadFile.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  });
};

const handleSuccess = (response) => {
  ruleForm.text = response.data;
  uploadfile.value = response.data;
};

// 图片上传处理
const extra = { type: 'common' };

const handleAvatarSuccess = ({ status, msg, data }) => {
  if (status) {
    ruleForm.picture = data;
    ElMessage.success(msg);
  } else {
    ElMessage.error(msg);
  }
};

const handleUploadError = (error) => {
  let { status, msg } = JSON.parse(error.message);
  ElMessage.error(msg);
};

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
};

// 表单验证规则
const rules = reactive({
  name: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在2到30个字符', trigger: 'blur' }
  ],
  writer: [
    { required: true, message: '请选择作者', trigger: 'change' }
  ],
  parent: [
    { required: true, message: '请选择一级分类', trigger: 'change' }
  ],
  threadsSortId: [
    { required: true, message: '请选择二级分类', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  introduction: [
    { required: true, message: '请输入简介', trigger: 'blur' },
    { min: 10, message: '简介至少10个字符', trigger: 'blur' }
  ]
});

// 表单提交
const submitForm = async (formEl) => {
  if (isSubmitting.value) return;

  try {
    isSubmitting.value = true;
    await formEl.validate();

    // 处理分类和作者ID
    const selectedSort = allsort.value.find(item => item.name === ruleForm.threadsSortId);
    const selectedWriter = writerlist.value.find(item => item.name === ruleForm.writer);

    if (selectedSort) ruleForm.threadsSortId = selectedSort.id;
    if (selectedWriter) ruleForm.writerId = selectedWriter.id;

    // 提交数据
    await axios.post('/api/threads/update', ruleForm);
    ElMessage.success('修改成功！');
    router.push('/thread/list');
  } catch (error) {
    console.error('提交失败:', error);
    ElMessage.error(error.response?.data?.message || '提交失败');
  } finally {
    isSubmitting.value = false;
  }
};

// 表单重置
const resetForm = (formEl) => {
  if (!formEl) return;
  formEl.resetFields();
  fileList.value = [];
  loadList(); // 重新加载原始数据
};
</script>

<style scoped>
/* 保持与发布页面相同的样式 */
.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.form-header {
  margin-bottom: 30px;
  text-align: center;
}

.form-header h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: #909399;
}

.advanced-form {
  padding: 20px;
}

.input-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  align-items: center;
}

.input-tip .el-icon {
  margin-right: 4px;
  font-size: 14px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.writer-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.category-item {
  flex: 1;
  position: relative;
}

.category-visual {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  z-index: 10;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
}

.category-visual div {
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  text-align: center;
  background: #f5f7fa;
  transition: all 0.3s;
}

.category-visual div:hover,
.category-visual div.active {
  background: var(--el-color-primary);
  color: #fff;
}

.status-radio {
  display: flex;
  gap: 10px;
}

.status-radio .el-radio-button {
  flex: 1;
}

.status-radio .el-radio-button :deep(.el-radio-button__inner) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 80px;
}

.status-radio .el-icon {
  font-size: 24px;
}

.word-count {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.upload-card {
  width: 100%;
}

.upload-card :deep(.el-upload-dragger) {
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.upload-icon {
  font-size: 48px;
  color: var(--el-color-primary);
}

.upload-text {
  text-align: center;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.cover-uploader {
  width: 100%;
  max-width: 400px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100%;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.cover-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  width: 100%;
  height: 200px;
  gap: 8px;
}

.cover-empty .el-icon {
  font-size: 40px;
}

.cover-hint {
  font-size: 12px;
  color: #c0c4cc;
}

.cover-preview {
  position: relative;
  width: 100%;
  height: 200px;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-hover {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-hover {
  opacity: 1;
}

.cover-hover .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
  padding: 12px 24px;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>