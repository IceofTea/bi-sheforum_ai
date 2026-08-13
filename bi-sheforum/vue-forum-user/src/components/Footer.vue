<template>
  <div class="forum-footer">
    <div class="footer-main">
      <div class="footer-content">
        <div class="footer-section">
          <div class="section-title">
            <i class="el-icon-link"></i>
            <span>校园论坛</span>
          </div>
          <p class="section-desc">一个专注于校园交流的社区平台</p>
        </div>
        
        <div class="footer-section">
          <div class="section-title">快捷链接</div>
          <ul class="link-list">
            <li><a href="#">关于我们</a></li>
            <li><a href="#">联系我们</a></li>
            <li><a href="#">帮助中心</a></li>
            <li><a href="#">用户协议</a></li>
          </ul>
        </div>
        
        <div class="footer-section">
          <div class="section-title">技术栈</div>
          <ul class="link-list">
            <li><span class="tag">Vue 3</span></li>
            <li><span class="tag">Spring Boot</span></li>
            <li><span class="tag">MyBatis</span></li>
            <li><span class="tag">Element Plus</span></li>
          </ul>
        </div>
        
        <div class="footer-section">
          <div class="section-title">联系我们</div>
          <ul class="link-list contact">
            <li><i class="el-icon-message"></i> forum@example.edu.cn</li>
            <li><i class="el-icon-phone"></i> 0532-88888888</li>
            <li><i class="el-icon-location"></i> 山东省青岛市</li>
          </ul>
        </div>
      </div>
    </div>
    
    <div class="footer-bottom">
      <div class="bottom-content">
        <div class="copyright">
          <span>&copy; 2024 校园论坛论坛</span>
          <span class="divider">|</span>
          <span>版权所有</span>
        </div>
        <div class="statement">
          本网站内容仅供学习交流使用，如有问题请联系管理员
        </div>
      </div>
    </div>
  </div>
</template>

<script setup></script>

<style lang="less" scoped>
.forum-footer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  width: 100%;
}

.footer-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 30px;
}

.footer-section {
  flex: 1;
  min-width: 180px;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    
    i {
      font-size: 18px;
    }
  }
  
  .section-desc {
    font-size: 14px;
    opacity: 0.8;
    line-height: 1.6;
  }
  
  .link-list {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      margin-bottom: 10px;
      font-size: 14px;
      opacity: 0.9;
      
      a {
        color: #fff;
        text-decoration: none;
        transition: opacity 0.3s;
        
        &:hover {
          opacity: 0.8;
          text-decoration: underline;
        }
      }
      
      .tag {
        display: inline-block;
        padding: 2px 8px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 4px;
        font-size: 12px;
      }
      
      &.contact {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        i {
          margin-right: 8px;
        }
      }
    }
  }
}

.footer-bottom {
  background: rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.bottom-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.copyright {
  font-size: 14px;
  margin-bottom: 8px;
  
  .divider {
    margin: 0 10px;
    opacity: 0.5;
  }
}

.statement {
  font-size: 12px;
  opacity: 0.7;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 24px;
  }
  
  .footer-section {
    min-width: 100%;
  }
}
</style>