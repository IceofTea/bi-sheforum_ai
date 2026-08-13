<template>
    <el-form ref="ruleFormRef" :model="ruleForm" :rules="rules" label-width="120px" class="demo-ruleForm" :size="formSize"
        status-icon>
        <el-form-item label="用户名 " prop="rootname">
            <el-input v-model="ruleForm.rootname" disabled/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
            <el-input v-model="ruleForm.password" />
        </el-form-item>
        <el-form-item label="真实姓名 " prop="realname">
            <el-input v-model="ruleForm.realname"/>
        </el-form-item>
        <el-form-item label="头像上传">
            <el-upload class="avatar-uploader" :action="VERCODE_URL+'/upload'" :show-file-list="false"
                accept=".jpeg,.png,.jpg" :data="extra" :on-success="handleAvatarSuccess" :on-error="handleUploadError"
                :before-upload="handleBeforeUpload">
                <img v-if="ruleForm.head" :src="VERCODE_URL+'/upload/' + ruleForm.head" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon">
                    <Plus />
                </el-icon>
            </el-upload>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" @click="submitForm(ruleFormRef)">
                提交
            </el-button>
            <el-button @click="resetForm(ruleFormRef)">重置</el-button>
        </el-form-item>
    </el-form>
</template>
<script setup>
import { VERCODE_URL } from '@/plugins/config.js';
import axios from 'axios';
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from "vue-router"
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
let route=useRoute();
let router=useRouter();
const formSize = ref('default')
const ruleFormRef = ref()
const ruleForm = ref({

})
// 加载列表
onMounted(() => {
  loadList();
});

async function loadList() {
  try {
    const res = await axios.get('/api/roots/one', { params: { name: sessionStorage.rootname } })
    if (res.data) {
      ruleForm.value = res.data
      console.log('用户信息加载成功:', ruleForm.value)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}
// 附加参数
let extra = { type: 'common' };
// 上传成功
const handleAvatarSuccess = ({ status, msg, data }, uploadFile) => {
    //如果上传成功
    if (status) {
        // 生成图片的地址
        ruleForm.value.head = data;
        // form.avatar = URL.createObjectURL(uploadFile.raw);
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
    // 判断图片格式/^imge\/(jpeg|png|jpg)$/
    let isValid = /^image\/(jpeg|png|jpg)$/.test(rawFile.type);
    if (!isValid) {
        ElMessage.error('头像必须是jpg/png格式!')
        return false
    }
    //图片大于2MB
    if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error('头像体积不能大于2MB!')
        return false
    }
    return true
}

const rules = reactive({
    password: [
        {
            required: false,
            message: '请输入新密码（可选）',
            trigger: 'blur',
        },
    ],
    realname: [
        {
            required: true,
            message: '请输入真实姓名',
            trigger: 'blur',
        },
    ],
})

const submitForm = async (formEl) => {
    if (!formEl) return
    await formEl.validate(async (valid, fields) => {
        if (valid) {
            try {
                const res = await axios.put('/api/roots', { ...ruleForm.value })
                console.log('更新成功:', res.data)
                ElMessage.success('用户信息更新成功')
                // 可选：重定向到其他地方
                // router.push("/user/edit")
            } catch (error) {
                console.error('更新失败:', error)
                ElMessage.error('更新失败，请稍后重试')
            }
        } else {
            console.log('表单验证失败:', fields)
            ElMessage.error('请检查表单内容')
        }
    })
}

const resetForm = (formEl) => {
    if (!formEl) return
    formEl.resetFields()
}

</script>
<style>
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

.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
}
</style>