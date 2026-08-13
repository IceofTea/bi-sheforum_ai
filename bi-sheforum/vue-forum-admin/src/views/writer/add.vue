<template>
  <div class="enhanced-form-container">
    <el-card shadow="hover" class="form-card">
      <template #header>
        <div class="form-header">
          <h3>作者信息{{ ruleForm.id ? '编辑' : '新增' }}</h3>
          <div class="header-actions">
            <el-button type="info" plain @click="$router.back()" :icon="ArrowLeft">返回</el-button>
          </div>
        </div>
      </template>

      <el-form
          ref="ruleFormRef"
          :model="ruleForm"
          :rules="rules"
          label-width="120px"
          label-position="top"
          status-icon
          class="demo-ruleForm"
      >
        <div class="form-columns">
          <div class="form-main">
            <el-form-item label="姓名" prop="name" class="enhanced-form-item">
              <el-input
                  v-model="ruleForm.name"
                  placeholder="请输入作者姓名"
                  clearable
                  @blur="handleNameBlur"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="出生日期" prop="birthday" class="enhanced-form-item">
              <el-date-picker
                  v-model="ruleForm.birthday"
                  type="date"
                  placeholder="选择出生日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="性别" prop="sex" class="enhanced-form-item">
              <el-radio-group v-model="ruleForm.sex">
                <el-radio-button label="男">
                  <el-icon><Male /></el-icon> 男
                </el-radio-button>
                <el-radio-button label="女">
                  <el-icon><Female /></el-icon> 女
                </el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="地址" prop="address" class="enhanced-form-item">
              <el-input
                  v-model="ruleForm.address"
                  placeholder="请输入详细地址"
                  clearable
              >
                <template #prefix>
                  <el-icon><Location /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="个人简介" prop="introduction" class="enhanced-form-item">
              <el-input
                  v-model="ruleForm.introduction"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入作者简介"
                  maxlength="500"
                  show-word-limit
                  resize="none"
              />
            </el-form-item>
          </div>

          <div class="form-side">
            <el-form-item label="头像上传" class="avatar-upload-item">
              <el-upload
                  class="avatar-uploader"
                  :action="uploadAction"
                  :show-file-list="false"
                  accept=".jpeg,.png,.jpg"
                  :data="extra"
                  :headers="uploadHeaders"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleUploadError"
                  :before-upload="handleBeforeUpload"
              >
                <div class="avatar-container">
                  <img
                      v-if="ruleForm.head"
                      :src="getAvatarUrl(ruleForm.head)"
                      class="avatar-image"
                  />
                  <div v-else class="avatar-upload-placeholder">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <div class="upload-text">点击上传头像</div>
                    <div class="upload-hint">支持 JPG/PNG 格式，小于2MB</div>
                  </div>
                  <div class="avatar-mask">
                    <el-icon><ZoomIn /></el-icon>
                    <span>更换头像</span>
                  </div>
                </div>
              </el-upload>
              <div class="avatar-tips" v-if="!ruleForm.head">建议尺寸：200×200像素</div>
            </el-form-item>
          </div>
        </div>

        <el-form-item class="form-actions">
          <el-button
              type="primary"
              @click="submitForm(ruleFormRef)"
              :loading="submitting"
              :icon="Check"
          >
            提交
          </el-button>
          <el-button
              @click="resetForm(ruleFormRef)"
              :icon="Refresh"
          >
            重置
          </el-button>
          <el-button
              v-if="ruleForm.id"
              type="danger"
              @click="showDeleteConfirm"
              :icon="Delete"
          >
            删除
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import {
  ElMessage,
  ElMessageBox
} from 'element-plus'
import {
  ArrowLeft, User, Male, Female, Location,
  Plus, ZoomIn, Check, Refresh, Delete
} from '@element-plus/icons-vue'

// 使用 computed 确保 URL 响应式
const VERCODE_URL = ref('http://your-api-base-url') // 替换为实际API基础URL
const uploadAction = computed(() => `${VERCODE_URL.value}/upload`)
const uploadHeaders = ref({
  'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
})

const route = useRoute()
const router = useRouter()
const ruleFormRef = ref()
const submitting = ref(false)

// 表单数据 - 确保所有字段都有初始值
const ruleForm = reactive({
  id: null,
  name: '',
  birthday: '',
  sex: '男', // 设置默认值
  address: '',
  introduction: '',
  head: ''
})

// 表单验证规则
const rules = reactive({
  name: [
    { required: true, message: '请输入作者姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  birthday: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入地址', trigger: 'blur' },
    { min: 5, max: 100, message: '长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  introduction: [
    { required: true, message: '请输入简介', trigger: 'blur' },
    { min: 10, message: '至少输入 10 个字符', trigger: 'blur' }
  ]
})

// 加载数据
onMounted(() => {
  if (route.query.id) {
    loadData(route.query.id)
  }
})

async function loadData(id) {
  try {
    const response = await axios.get(`/api/writer/${id}`)
    // 确保响应数据填充到表单前所有字段都存在
    Object.assign(ruleForm, {
      id: response.data.id || null,
      name: response.data.name || '',
      birthday: response.data.birthday || '',
      sex: response.data.sex || '男',
      address: response.data.address || '',
      introduction: response.data.introduction || '',
      head: response.data.head || ''
    })
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error(error)
  }
}

// 上传相关配置
const extra = { type: 'common' }

// 获取完整头像URL
const getAvatarUrl = (head) => {
  if (!head) return ''
  return head.startsWith('http') ? head : `${VERCODE_URL.value}/upload/${head}`
}

// 上传成功处理
const handleAvatarSuccess = (response, uploadFile) => {
  try {
    if (response.status) {
      ruleForm.head = response.data
      ElMessage.success(response.msg || '上传成功')
    } else {
      ElMessage.error(response.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传处理错误:', error)
    ElMessage.error('上传处理失败')
  }
}

// 上传失败处理
const handleUploadError = (error) => {
  try {
    const errorData = error.response?.data || JSON.parse(error.message)
    ElMessage.error(errorData.msg || errorData.message || '上传失败')
  } catch (e) {
    ElMessage.error(error.message || '上传失败')
  }
}

// 上传前验证
const handleBeforeUpload = (rawFile) => {
  const isValidType = /^image\/(jpeg|png|jpg)$/.test(rawFile.type)
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isValidType) {
    ElMessage.error('头像必须是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 表单提交
const submitForm = async (formEl) => {
  if (!formEl) return

  try {
    await formEl.validate()
    submitting.value = true

    // 确保 ruleForm.id 存在判断
    const apiUrl = ruleForm.id ? `/api/writer/${ruleForm.id}` : '/api/writer'
    const method = ruleForm.id ? 'put' : 'post'

    const response = await axios({
      method,
      url: apiUrl,
      data: ruleForm
    })

    ElMessage.success(ruleForm.id ? '更新成功' : '新增成功')
    router.push('/writer/list')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.response?.data?.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = (formEl) => {
  if (!formEl) return

  ElMessageBox.confirm('确定要重置表单吗？所有修改将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formEl.resetFields()
    if (!ruleForm.id) {
      // 重置所有字段
      Object.assign(ruleForm, {
        name: '',
        birthday: '',
        sex: '男',
        address: '',
        introduction: '',
        head: ''
      })
    }
  }).catch(() => {
    // 取消操作
  })
}

// 删除确认 - 添加 ruleForm.id 存在检查
const showDeleteConfirm = () => {
  if (!ruleForm.id) {
    ElMessage.error('无效的作者ID')
    return
  }

  ElMessageBox.confirm('确定要删除该作者吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
    center: true
  }).then(async () => {
    try {
      await axios.delete(`/api/writer/${ruleForm.id}`)
      ElMessage.success('删除成功')
      router.push('/writer/list')
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {
    // 取消操作
  })
}

// 姓名输入框失去焦点时处理
const handleNameBlur = () => {
  if (ruleForm.name && !ruleForm.introduction) {
    ruleForm.introduction = `${ruleForm.name}，${ruleForm.sex === '男' ? '他' : '她'}是一位...`
  }
}
</script>

<style scoped>
.enhanced-form-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.form-card {
  border-radius: 8px;
  max-width: 1200px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.form-columns {
  display: flex;
  gap: 30px;
}

.form-main {
  flex: 1;
  min-width: 0;
}

.form-side {
  width: 300px;
}

.enhanced-form-item {
  margin-bottom: 22px;
}

.avatar-upload-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-uploader {
  width: 100%;
  margin-bottom: 10px;
}

.avatar-container {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px dashed var(--el-border-color);
  transition: all 0.3s;
  cursor: pointer;
}

.avatar-container:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--el-text-color-secondary);
  background-color: var(--el-fill-color-light);
}

.upload-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 14px;
  margin-bottom: 5px;
}

.upload-hint {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.avatar-mask {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 36px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-container:hover .avatar-mask {
  opacity: 1;
}

.avatar-mask .el-icon {
  margin-right: 5px;
}

.avatar-tips {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .form-columns {
    flex-direction: column;
  }

  .form-side {
    width: 100%;
    order: -1;
    margin-bottom: 20px;
  }

  .avatar-container {
    width: 150px;
    height: 150px;
    margin: 0 auto;
  }
}
</style>