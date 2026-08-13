<template>
  <div class="enhanced-form-container">
    <el-card shadow="hover" class="form-card">
      <template #header>
        <div class="form-header">
          <h3>作者信息编辑</h3>
          <el-button type="info" plain @click="router.push('/writer/list')" :icon="ArrowLeft">
            返回列表
          </el-button>
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
            保存修改
          </el-button>
          <el-button
              @click="resetForm(ruleFormRef)"
              :icon="Refresh"
          >
            重置表单
          </el-button>
          <el-button
              type="danger"
              @click="showDeleteConfirm"
              :icon="Delete"
          >
            删除作者
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
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, User, Male, Female, Location,
  Plus, ZoomIn, Check, Refresh, Delete
} from '@element-plus/icons-vue'

// 确保正确导入VERCODE_URL
import { VERCODE_URL } from '@/plugins/config.js'

const route = useRoute()
const router = useRouter()
const ruleFormRef = ref()
const submitting = ref(false)

// 上传相关配置
const extra = { type: 'common' }
const uploadAction = computed(() => `${VERCODE_URL}/upload`)

// 表单数据
const ruleForm = reactive({
  id: null,
  name: '',
  birthday: '',
  sex: '',
  address: '',
  introduction: '',
  head: ''
})

// 获取完整头像URL
const getAvatarUrl = (head) => {
  return head ? `${VERCODE_URL}/upload/${head}` : ''
}

// 上传成功处理
const handleAvatarSuccess = ({ status, msg, data }, uploadFile) => {
  if (status) {
    ruleForm.head = data
    ElMessage.success(msg)
  } else {
    ElMessage.error(msg)
  }
}

// 上传失败处理
const handleUploadError = (error) => {
  try {
    const { msg } = JSON.parse(error.message)
    ElMessage.error(msg || '上传失败')
  } catch {
    ElMessage.error('上传失败')
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

// 加载数据
onMounted(() => {
  if (route.query.id) {
    loadData(route.query.id)
  }
})

async function loadData(id) {
  try {
    const response = await axios.get(`/api/writer/one`, { params: { id } })
    Object.assign(ruleForm, response.data)
    console.log('Loaded data:', response.data) // 调试用
  } catch (error) {
    ElMessage.error('加载作者数据失败')
    console.error(error)
  }
}

// 表单提交
const submitForm = async (formEl) => {
  if (!formEl) return

  try {
    await formEl.validate()
    submitting.value = true

    const response = await axios.post('/api/writer/update', ruleForm)

    ElMessage.success('作者信息更新成功')
    router.push('/writer/list')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.response?.data?.message || '更新失败')
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
    loadData(route.query.id) // 重新加载原始数据
  }).catch(() => {
    // 取消操作
  })
}

// 删除确认
const showDeleteConfirm = () => {
  ElMessageBox.confirm('确定要删除该作者吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
    center: true
  }).then(async () => {
    try {
      await axios.post('/api/writer/delete', { id: ruleForm.id })
      ElMessage.success('作者删除成功')
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
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-container {
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
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
  color: #8c939d;
}

.upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  margin-bottom: 4px;
}

.upload-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.avatar-tips {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  text-align: center;
  margin-top: 8px;
}
</style>