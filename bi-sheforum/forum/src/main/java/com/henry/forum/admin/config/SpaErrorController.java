package com.henry.forum.admin.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 单页应用（SPA）history 路由回退。
 *
 * 前端构建产物由 CI 复制到 classpath 静态资源目录：
 *   用户端 -> static/index.html（根路径）
 *   管理端 -> static/admin/index.html（/admin 子路径）
 *
 * 对未匹配任何后端接口 / 静态资源的 404 请求，按路径转发到对应入口页，
 * 使刷新 / 直接访问深层路由时返回正确的 HTML。API 类路径保持 404 语义。
 */
@Controller
public class SpaErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = statusAttr == null ? 500 : Integer.parseInt(statusAttr.toString());
        if (code != 404) {
            return null;
        }
        String uri = String.valueOf(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        // 后端接口 / 上传 / 验证码 / 静态文件（带扩展名）：保留 404 语义
        if (uri.startsWith("/api/") || uri.equals("/api")
                || uri.startsWith("/upload/") || uri.equals("/upload")
                || uri.startsWith("/vercode") || uri.contains(".")) {
            return null;
        }
        // SPA 深层路由：返回对应前端入口页（重置为 200）
        response.setStatus(HttpServletResponse.SC_OK);
        if (uri.startsWith("/admin")) {
            return "forward:/admin/index.html";
        }
        return "forward:/index.html";
    }
}