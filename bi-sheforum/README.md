# 毕设Campus Forum

#### 介绍

这是我的毕业设计，一个校内论坛管理系统。该系统旨在为青岛大学的师生提供一个交流与互动的平台，包含发帖、评论、点赞、举报、论坛管理员管理等功能，力求提供一个安全、易用、功能丰富的校园论坛。

#### 软件架构

本系统采用 Spring Boot 作为后端框架，前端使用 Thymeleaf 进行模板渲染，数据库采用 MySQL 存储数据。系统架构采用常见的 MVC 模式，前后端分离，并结合 Redis 提升性能，使用 Spring Security 进行用户认证与权限管理，Kafka 用于异步消息处理，确保系统高并发情况下的稳定性。

#### 安装教程

1.  克隆项目：git clone https://gitee.com/yourusername/campus-forum.git
2.  导入项目到 IDE（如 IntelliJ IDEA 或 Eclipse），并通过 Maven 或 Gradle 下载依赖。
3.  配置数据库，创建数据库 forum 并运行项目中的 SQL 脚本文件以初始化数据库。
4.  配置 application.yml 中的相关参数，特别是数据库连接配置、Redis 配置等。
5.  启动 Spring Boot 应用

#### 使用说明

1.  启动应用后，在浏览器中访问 http://localhost:4154。
2.  注册一个新用户，登录后即可使用论坛的基本功能，如发布贴子、评论、点赞等。
3.  论坛管理员可以通过后台管理页面（如 /admin）进行用户管理、贴子审核等操作。

#### 参与贡献

1.  Fork 项目到自己的 GitHub/Gitee 账户。
2.  在分支上开发新功能或修复 bug。
3.  提交代码并创建 Pull Request，合并请求时描述清楚修改内容。


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
