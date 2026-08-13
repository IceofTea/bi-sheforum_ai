<template>
  <div class="tieba-post-page">
    <!-- 顶部导航 -->
    <header class="tieba-header">
      <div class="header-inner">
        <div class="logo" @click="goHome">校园论坛</div>
        <div class="header-actions">
          <el-button plain @click="goBack">返回</el-button>
          <div class="user-info" @click="gotoUser">
            <img :src="userAvatar" class="avatar">
            <span class="nickname">{{ userNickname }}</span>
          </div>
        </div>
      </div>
    </header>

    <div class="page-container">
      <!-- 发帖卡片 -->
      <div class="post-card">
        <div class="card-header">
          <h2><i class="el-icon-edit"></i> 发布新帖</h2>
          <p>分享你的想法，与同学们交流</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="post-form">
          <el-form-item label="帖子标题" prop="name">
            <el-input 
              v-model="form.name" 
              placeholder="请输入有意义的标题..."
              maxlength="50"
              show-word-limit
              size="large"
            />
          </el-form-item>

          <el-form-item label="选择分类" prop="category">
            <el-cascader
              v-model="form.category"
              :options="categoryOptions"
              :props="{ checkStrictly: true, value: 'id', label: 'name' }"
              placeholder="选择帖子分类"
              @change="handleCategoryChange"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="帖子内容" prop="introduction">
            <el-input
              type="textarea"
              :rows="6"
              v-model="form.introduction"
              placeholder="详细描述你的内容..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="封面图片（可选）" prop="picture">
            <div class="cover-upload">
              <el-upload
                class="cover-uploader"
                :action="UPLOAD_URL"
                :show-file-list="false"
                :on-success="handleCoverSuccess"
                :before-upload="beforeUpload"
                accept=".jpg,.jpeg,.png,.gif"
              >
                <img v-if="form.picture" :src="UPLOAD_URL + '/' + form.picture" class="cover-preview">
                <div v-else class="upload-trigger">
                  <i class="el-icon-plus"></i>
                  <span>上传封面</span>
                </div>
              </el-upload>
              <div v-if="form.picture" class="remove-cover" @click="form.picture = ''">
                <i class="el-icon-close"></i>
              </div>
            </div>
          </el-form-item>

          <div class="form-actions">
            <el-button 
              type="primary" 
              size="large"
              :loading="isSubmitting"
              @click="submitForm"
            >
              <i class="el-icon-upload2"></i> 发布帖子
            </el-button>
            <el-button size="large" @click="resetForm">重置</el-button>
          </div>
        </el-form>
      </div>

      <!-- 发帖提示 -->
      <div class="post-tips">
        <div class="tip-title">发帖须知</div>
        <ul>
          <li>请勿发布违规内容</li>
          <li>标题需简洁明了</li>
          <li>请选择正确的分类</li>
          <li>封面图片不超过2MB</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VERCODE_URL } from '@/plugins/config.js'

const router = useRouter()
const formRef = ref()
const UPLOAD_URL = '/upload'

const form = reactive({
  name: '',
  category: [],
  sortId: '',
  introduction: '',
  picture: ''
})

const rules = {
  name: [{ required: true, message: '请输入帖子标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  introduction: [{ required: true, message: '请输入帖子内容', trigger: 'blur' }]
}

const categoryOptions = ref([])
const isSubmitting = ref(false)
const userNickname = ref('游客')
let userAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0MCIgZmlsbD0iI2UzZTNlMyIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjQwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjZmZmIj5XPC90ZXh0Pjwvc3ZnPg==';

onMounted(async () => {
  await loadCategories();
  await loadUserInfo();
});

const loadCategories = async () => {
  const res = await axios.get('/api/sort');
  categoryOptions.value = buildCascader(res.data || []);
};

const loadUserInfo = async () => {
  if (sessionStorage.id) {
    try {
      const res = await axios.get('/api/users/one', { params: { id: sessionStorage.id } });
      const userData = res.data?.data || res.data;
      if (userData) {
        userNickname.value = userData.nickname || '用户';
        if (userData.head) {
          userAvatar = VERCODE_URL + '/upload/' + userData.head;
        }
      }
    } catch (e) {
      console.error('获取用户信息失败:', e);
    }
  }
};

const buildCascader = (data) => {
  return data
    .filter(item => item.parent === null || item.parent === 0)
    .map(item => {
      const children = data
        .filter(c => c.parent === item.id)
        .map(c => ({ id: c.id, name: c.name }));
      return {
        id: item.id,
        name: item.name,
        children: children.length > 0 ? children : undefined
      };
    });
};

const handleCategoryChange = (value) => {
  if (value && value.length > 0) {
    form.sortId = value[value.length - 1];
  }
};

const handleCoverSuccess = (res) => {
  if (res.status) {
    form.picture = res.data;
    ElMessage.success('封面上传成功');
  } else {
    ElMessage.error(res.msg || '上传失败');
  }
};

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type);
  const isSizeValid = file.size / 1024 / 1024 < 2;
  if (!isImage) ElMessage.error('封面必须是 JPG/PNG 图片');
  if (!isSizeValid) ElMessage.error('封面不能大于 2MB');
  return isImage && isSizeValid;
};

const submitForm = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    await formRef.value.validate();

    const userRes = await axios.get('/api/users/one', {
      params: { id: sessionStorage.id }
    });
    const userData = userRes.data?.data || userRes.data;
    console.log('用户数据:', userData);

    if (!userData?.id) {
      ElMessage.error('获取用户信息失败');
      return;
    }

    const postData = {
      name: form.name,
      introduction: form.introduction,
      picture: form.picture,
      threadsSortId: form.sortId,
      writer: userData.nickname,
      writerId: userData.id
    };
    console.log('发帖数据:', postData);

    const res = await axios.post('/api/threads', null, {
      params: postData
    });
    console.log('发帖响应:', res);

    if (res.data?.status === 200) {
      ElMessage.success('发帖成功！');
      router.push('/allthread');
    } else {
      ElMessage.error(res.data?.msg || '发帖失败');
    }
  } catch (error) {
    console.error('发帖失败:', error);
    ElMessage.error('发帖失败，请检查内容');
  } finally {
    isSubmitting.value = false;
  }
};

const resetForm = () => {
  formRef.value.resetFields();
  form.picture = '';
};

function goHome() {
  router.push('/');
}

function goBack() {
  router.back();
}

function gotoUser() {
  router.push('/user/index');
}
</script>

<style lang="less" scoped>
.tieba-post-page {
  min-height: 100vh;
  background: #f5f8fc;
}

.tieba-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  .header-inner {
    max-width: 1000px;
    margin: 0 auto;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
  }
  
  .logo {
    font-size: 22px;
    font-weight: bold;
    color: #fff;
    cursor: pointer;
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .el-button {
      background: rgba(255,255,255,0.25);
      border: none;
      color: #fff;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
      }
      
      .nickname {
        color: #fff;
        font-size: 14px;
      }
    }
  }
}

.page-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 20px;
  display: flex;
  gap: 20px;
  flex: 1;
}

.post-card {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  .card-header {
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      color: #333;
      display: flex;
      align-items: center;
      gap: 10px;

      i {
        color: #667eea;
      }
    }

    p {
      margin: 0;
      color: #999;
      font-size: 14px;
    }
  }

  .post-form {
    .el-form-item {
      margin-bottom: 24px;

      .el-form-item__label {
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }
    }

    .cover-upload {
      position: relative;

      .cover-uploader {
        width: 200px;
        height: 140px;
        border: 2px dashed #ddd;
        border-radius: 12px;
        cursor: pointer;
        overflow: hidden;
        transition: all 0.3s;

        &:hover {
          border-color: #667eea;
        }

        .cover-preview {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .upload-trigger {
          width: 100%;
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #aaa;

          i {
            font-size: 32px;
            margin-bottom: 8px;
          }

          span {
            font-size: 13px;
          }
        }
      }

      .remove-cover {
        position: absolute;
        top: -10px;
        right: -10px;
        width: 24px;
        height: 24px;
        background: #f56c6c;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: #fff;
        font-size: 12px;
      }
    }

    .form-actions {
      display: flex;
      gap: 15px;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid #eee;

      .el-button--primary {
        background: linear-gradient(135deg, #667eea, #764ba2);
        border: none;
        padding: 20px 40px;
      }

      .el-button {
        padding: 20px 30px;
      }
    }
  }
}

.post-tips {
  width: 200px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  height: fit-content;
  position: sticky;
  top: 100px;

  .tip-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
    margin-bottom: 12px;
  }

  ul {
    margin: 0;
    padding: 0;

    li {
      font-size: 13px;
      color: #888;
      padding: 8px 0;
      list-style: none;

      &::before {
        content: '•';
        color: #667eea;
        margin-right: 8px;
      }
    }
  }
}

@media (max-width: 800px) {
  .page-container {
    flex-direction: column;
  }

  .post-tips {
    width: 100%;
    position: static;
  }
}
</style>