# 毕设Campus Forum

#### Description

This is my graduation project, a campus forum management system. The system aims to provide a platform for communication and interaction for teachers and students at Qingdao University, with features such as posting, commenting, liking, reporting, and forum administrator management. The goal is to provide a safe, user-friendly, and feature-rich campus forum.

#### Software Architecture

This system uses Spring Boot as the backend framework, with Thymeleaf for frontend template rendering and MySQL as the database. The architecture follows the common MVC pattern, with frontend and backend separated. It integrates Redis to improve performance, Spring Security for user authentication and authorization management, and Kafka for asynchronous message processing to ensure stability under high concurrent usage.

#### Installation

1.  Clone the project：git clone https://gitee.com/yourusername/campus-forum.git
2.  Import the project into an IDE (such as IntelliJ IDEA or Eclipse), and download dependencies using Maven or Gradle.
3.  Configure the database, create a database named forum, and run the SQL script provided in the project to initialize the database.
4.  Configure the relevant parameters in application.yml, especially the database connection configuration and Redis settings.
5.  Start the Spring Boot application

#### Instructions

1.  After starting the application, visit http://localhost:4154 in your browser.
2.  Register a new user, and after logging in, you can use the basic features of the forum, such as posting, commenting, and liking.
3.  Forum administrators can manage users, review posts, and perform other administrative tasks via the backend management page (e.g., /admin).

#### Contribution

1.  Fork the project to your own GitHub/Gitee account.
2.  Develop new features or fix bugs on a branch.
3.  Submit your code and create a Pull Request. Be sure to describe the changes in the merge request.


#### Gitee Feature

1.  You can use Readme\_XXX.md to support different languages, such as Readme\_en.md, Readme\_zh.md
2.  Gitee blog [blog.gitee.com](https://blog.gitee.com)
3.  Explore open source project [https://gitee.com/explore](https://gitee.com/explore)
4.  The most valuable open source project [GVP](https://gitee.com/gvp)
5.  The manual of Gitee [https://gitee.com/help](https://gitee.com/help)
6.  The most popular members  [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
