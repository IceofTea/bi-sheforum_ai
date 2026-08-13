<template>
  <div class="register-container">
    <div class="register-form">
      <h2>注册新账号</h2>

      <div class="form-group">
        <label for="username">用户名</label>
        <input
            type="text"
            id="username"
            v-model="form.username"
            placeholder="请输入用户名"
            @blur="validateUsername"
        >
        <span class="error-msg" v-if="errors.username">{{ errors.username }}</span>
      </div>

      <div class="form-group">
        <label for="password">密码</label>
        <input
            type="password"
            id="password"
            v-model="form.password"
            placeholder="请输入密码"
            @blur="validatePassword"
        >
        <span class="error-msg" v-if="errors.password">{{ errors.password }}</span>
      </div>

      <div class="form-group">
        <label for="confirmPassword">确认密码</label>
        <input
            type="password"
            id="confirmPassword"
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            @blur="validateConfirmPassword"
        >
        <span class="error-msg" v-if="errors.confirmPassword">{{ errors.confirmPassword }}</span>
      </div>

      <div class="form-group">
        <label for="email">邮箱</label>
        <input
            type="email"
            id="email"
            v-model="form.email"
            placeholder="请输入邮箱"
            @blur="validateEmail"
        >
        <span class="error-msg" v-if="errors.email">{{ errors.email }}</span>
      </div>

      <div class="form-group">
        <label for="verificationCode">验证码</label>
        <div class="code-input">
          <input
              type="text"
              id="verificationCode"
              v-model="form.verificationCode"
              placeholder="请输入验证码"
          >
          <button
              class="send-code-btn"
              :disabled="isSendingCode"
              @click="sendVerificationCode"
          >
            {{ isSendingCode ? `${countdown}秒后重试` : '获取验证码' }}
          </button>
        </div>
        <span class="error-msg" v-if="errors.verificationCode">{{ errors.verificationCode }}</span>
      </div>

      <button class="register-btn" @click="handleRegister" :disabled="isRegistering">
        {{ isRegistering ? '注册中...' : '立即注册' }}
      </button>

      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        verificationCode: ''
      },
      errors: {
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        verificationCode: ''
      },
      isSendingCode: false,
      countdown: 60,
      isRegistering: false
    }
  },
  methods: {
    validateUsername() {
      if (!this.form.username) {
        this.errors.username = '请输入用户名'
      } else if (this.form.username.length < 4 || this.form.username.length > 16) {
        this.errors.username = '用户名长度应为4-16个字符'
      } else {
        this.errors.username = ''
      }
    },
    validatePassword() {
      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,16}$/
      if (!this.form.password) {
        this.errors.password = '请输入密码'
      } else if (!passwordRegex.test(this.form.password)) {
        this.errors.password = '密码需包含大小写字母和数字，长度8-16位'
      } else {
        this.errors.password = ''
      }
    },
    validateConfirmPassword() {
      if (!this.form.confirmPassword) {
        this.errors.confirmPassword = '请确认密码'
      } else if (this.form.password !== this.form.confirmPassword) {
        this.errors.confirmPassword = '两次输入的密码不一致'
      } else {
        this.errors.confirmPassword = ''
      }
    },
    validateEmail() {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!this.form.email) {
        this.errors.email = '请输入邮箱'
      } else if (!emailRegex.test(this.form.email)) {
        this.errors.email = '请输入有效的邮箱地址'
      } else {
        this.errors.email = ''
      }
    },
    async sendVerificationCode() {
      this.validateEmail()
      if (this.errors.email) return

      this.isSendingCode = true

      try {
        // 调用发送验证码API
        const response = await this.$axios.post('/api/auth/send-verification-code', {
          email: this.form.email
        })

        if (response.data.success) {
          this.startCountdown()
          this.$message.success('验证码已发送')
        } else {
          this.$message.error(response.data.message || '发送验证码失败')
          this.isSendingCode = false
        }
      } catch (error) {
        console.error('发送验证码失败:', error)
        this.$message.error('发送验证码失败，请重试')
        this.isSendingCode = false
      }
    },
    startCountdown() {
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
          this.isSendingCode = false
          this.countdown = 60
        }
      }, 1000)
    },
    async handleRegister() {
      // 验证所有字段
      this.validateUsername()
      this.validatePassword()
      this.validateConfirmPassword()
      this.validateEmail()

      // 检查是否有错误
      if (Object.values(this.errors).some(error => error)) {
        return
      }

      this.isRegistering = true

      try {
        // 调用注册API
        const response = await this.$axios.post('/api/auth/register', {
          username: this.form.username,
          password: this.form.password,
          email: this.form.email,
          verificationCode: this.form.verificationCode
        })

        if (response.data.success) {
          this.$message.success('注册成功')
          // 注册成功后跳转到登录页面
          this.$router.push('/login')
        } else {
          this.$message.error(response.data.message || '注册失败')
        }
      } catch (error) {
        console.error('注册失败:', error)
        this.$message.error(error.response?.data?.message || '注册失败，请重试')
      } finally {
        this.isRegistering = false
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.register-form {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #409eff;
  outline: none;
}

.error-msg {
  display: block;
  margin-top: 6px;
  color: #f56c6c;
  font-size: 12px;
}

.code-input {
  display: flex;
  gap: 10px;
}

.send-code-btn {
  flex-shrink: 0;
  padding: 0 12px;
  background: #f0f2f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.send-code-btn:hover {
  background: #e4e6e9;
}

.send-code-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.register-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.register-btn:hover {
  background-color: #66b1ff;
}

.register-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>