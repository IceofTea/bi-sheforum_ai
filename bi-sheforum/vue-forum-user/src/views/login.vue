<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-container">
      <div class="login-card">
        <div class="card-header">
          <h1>校园论坛</h1>
          <p>欢迎回来，请登录您的账号</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="login-form" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item prop="vercode">
            <div class="vercode-row">
              <el-input
                v-model="form.vercode"
                placeholder="验证码"
                :prefix-icon="Key"
                size="large"
                maxlength="4"
                class="vercode-input"
              />
              <img :src="vercodeUrl" class="vercode-img" @click="refreshVercode" alt="验证码" />
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin" round>
              登 录
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span>还没有账号？</span>
            <el-button type="primary" link @click="goRegister">立即注册</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import axios from 'axios'
import { VERCODE_URL } from '@/plugins/config.js'

const http = axios.create({ baseURL: '' })

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)
const vercodeUrl = ref('')

const form = reactive({
  username: '',
  password: '',
  vercode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  vercode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

onMounted(() => {
  refreshVercode()
})

function refreshVercode() {
  vercodeUrl.value = VERCODE_URL + '/vercode?' + Date.now()
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await http.post('/api/users/login', {
      username: form.username,
      password: form.password
    })

    if (res && res.status === 200) {
      const user = res.data
      const token = (user && user.token) || res.data?.msg || ''
      sessionStorage.setItem('token', token)
      sessionStorage.setItem('id', user?.id)
      sessionStorage.setItem('username', user?.username)
      sessionStorage.setItem('nickname', user?.nickname || '')
      ElMessage.success('登录成功')

      const redirect = route.query.redirect
      router.replace(redirect || '/')
    } else {
      ElMessage.error(res?.msg || '登录失败')
      refreshVercode()
    }
  } catch (e) {
    ElMessage.error('请求失败，请检查网络')
    refreshVercode()
  } finally {
    loading.value = false
  }
}

function goRegister() {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 40px 32px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    font-size: 28px;
    color: #303133;
    margin: 0 0 8px;
    font-weight: 700;
  }

  p {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.login-form {
  .vercode-row {
    display: flex;
    gap: 12px;
    align-items: center;

    .vercode-input {
      flex: 1;
    }

    .vercode-img {
      width: 110px;
      height: 40px;
      border-radius: 8px;
      cursor: pointer;
      flex-shrink: 0;
      object-fit: cover;
    }
  }

  .submit-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    border: none;
  }

  .form-footer {
    text-align: center;
    font-size: 14px;
    color: #909399;

    .el-button {
      font-size: 14px;
    }
  }
}
</style>
