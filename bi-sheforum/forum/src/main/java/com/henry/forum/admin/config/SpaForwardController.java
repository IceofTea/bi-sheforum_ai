package com.henry.forum.admin.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 单页应用（Vue Router history 模式）回退处理。
 *
 * 前端构建产物被复制到后端 classpath 静态资源目录：
 *   用户端 -> static/index.html（根路径）
 *   管理端 -> static/admin/index.html（/admin 子路径）
 *
 * 对非 API / 非静态资源的深层路径统一转发到对应入口页，
 * 使刷新 / 直接访问深层路由时返回正确的 HTML，而不是 404。
 */
@Controller
public class SpaForwardController {

    /** 用户端 history 路由回退（排除后端 API、上传、验证码与静态资源前缀） */
    @RequestMapping(value = {
            "/{path:^(?!api|upload|vercode|admin|assets)[^\\.]*}",
            "/{path:^(?!api|upload|vercode|admin|assets).*$}/**"
    })
    public String forwardUser() {
        return "forward:/index.html";
    }

    /** 管理端 history 路由回退（/admin 子路径，排除其静态资源前缀） */
    @RequestMapping(value = {
            "/admin",
            "/admin/",
            "/admin/{path:^(?!assets).*$}/**"
    })
    public String forwardAdmin() {
        return "forward:/admin/index.html";
    }
}