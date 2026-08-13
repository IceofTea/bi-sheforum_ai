<template>
  <div class="form-container">
    <div class="form-header">
      <h2>内容发布</h2>
      <p>填写完整信息以便更好地展示您的内容</p>
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

      <!-- 发帖人选择 -->
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
              <el-avatar :size="24" :src="item.avatar || defaultAvatar" />
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
            drag
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
          {{ isSubmitting ? '提交中...' : '发布内容' }}
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
import { useRouter } from 'vue-router';
import {
  Plus, Upload, Edit, Refresh, Clock,
  InfoFilled, Promotion, RefreshLeft
} from '@element-plus/icons-vue';

const router = useRouter();
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
  parentlist();
  writers();
});

// 获取作者列表
const writers = () => {
  axios({
    method: 'get',
    url: '/api/writer',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
  })
      .then((res) => {
        writerlist.value = res.data;
      });
}

// 获取分类列表
const parentlist = () => {
  axios({
    method: 'get',
    url: '/api/sort',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
  })
      .then((res) => {
        allsort.value = res.data;
        // 只取没有父级的分类作为一级分类
        parentslist.value = res.data.filter(item => !item.parentcontent);
      });
}

// 切换二级分类
const switchlist = (name) => {
  sortlist.value = [];
  allsort.value.forEach((item) => {
    if (item.parentcontent != null && item.parentcontent.name == name) {
      sortlist.value.push(item);
    }
  });
}

// 分类选择可视化
const handleCategoryHover = (isHover) => {
  if (parentslist.value.length > 0) {
    categoryHover.value = isHover;
  }
};

// 文件上传处理
const handleRemove = (file, uploadFiles) => {
  axios({
    method: 'delete',
    url: '/upload',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    params: { file: uploadfile.value }
  }).then((res) => {
    console.log(res);
  });
}

const beforeRemove = (uploadFile, uploadFiles) => {
  return ElMessageBox.confirm(
      `确定移除 ${uploadFile.name} 吗？`
  ).then(
      () => true,
      () => false
  );
}

const handleSuccess = (response, files, uploadFiles) => {
  ruleForm.text = response.data;
  uploadfile.value = response.data;
}

// 图片上传处理
let extra = { type: 'common' };

// 上传成功
const handleAvatarSuccess = ({ status, msg, data }, uploadFile) => {
  if (status) {
    ruleForm.picture = data;
    ElMessage.success(msg);
  } else {
    ElMessage.error(msg);
  }
}

// 上传失败
let handleUploadError = (error, uploadFile) => {
  let { status, msg } = JSON.parse(error.message);
  ElMessage.error(msg);
}

const handleBeforeUpload = (rawFile) => {
  let isValid = /^image\/(jpeg|png|jpg)$/.test(rawFile.type);
  if (!isValid) {
    ElMessage.error('头像必须是jpg/png格式!');
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像体积不能大于2MB!');
    return false;
  }
  return true;
}

// 表单验证规则
const rules = reactive({
  name: [
    {
      required: true,
      message: '请输入标题',
      trigger: 'change',
    },
  ],
  writer: [
    {
      required: true,
      message: '请输入作者',
      trigger: 'change',
    },
  ],
  parent: [
    {
      required: true,
      message: '请选择父级',
      trigger: 'change',
    },
  ],
  threadsSortId: [
    {
      required: true,
      message: '请选择二级分类',
      trigger: 'change',
    },
  ],
  status: [
    {
      required: true,
      message: '请选择状态',
      trigger: 'change',
    },
  ],
  introduction: [
    {
      required: true,
      message: '请输入简介',
      trigger: 'change',
    },
  ],
})

// 表单提交
const submitForm = async (formEl) => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  if (!formEl) return;

  await formEl.validate((valid, fields) => {
    if (valid) {
      // 将选中的分类id替换到表单数据
      allsort.value.forEach((item) => {
        if (item.name === ruleForm.threadsSortId) {
          ruleForm.threadsSortId = item.id;
        }
      });
      writerlist.value.forEach((item) => {
        if (item.name === ruleForm.writer) {
          ruleForm.writerId = item.id;
        }
      });

      // 发送表单数据
      axios({
        method: 'post',
        url: '/api/threads',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        params: {...ruleForm}
      })
          .then((res) => {
            console.log(res);
            // 成功后跳转
            router.push('/thread/list');
          })
          .catch((error) => {
            console.error(error);
            ElMessage.error('提交失败！');
          })
          .finally(() => {
            isSubmitting.value = false;
          });
    } else {
      console.log('error submit!', fields);
      isSubmitting.value = false;
    }
  });
};

// 表单重置
const resetForm = (formEl) => {
  if (!formEl) return;
  formEl.resetFields();
  fileList.value = [];
  ruleForm.picture = '';
}
</script>

<style scoped>
/* 保持与之前相同的样式 */
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