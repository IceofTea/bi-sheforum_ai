<template>
  <div class="not-found">
    <div class="not-found-inner">
      <div class="error-code">404</div>
      <h1>页面不存在</h1>
      <p>抱歉，您访问的页面不存在或已被删除</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="$router.push('/')">返回首页</el-button>
        <el-button size="large" @click="$router.back()">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f8fc;
}

.not-found-inner {
  text-align: center;
  padding: 40px;

  .error-code {
    font-size: 120px;
    font-weight: 800;
    background: linear-gradient(135deg, #667eea, #764ba2);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    line-height: 1;
    margin-bottom: 16px;
  }

  h1 {
    font-size: 28px;
    color: #303133;
    margin: 0 0 12px;
  }

  p {
    font-size: 16px;
    color: #909399;
    margin: 0 0 32px;
  }

  .actions {
    display: flex;
    gap: 16px;
    justify-content: center;
  }
}
</style>
