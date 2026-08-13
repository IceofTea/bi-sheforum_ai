package com.henry.forum.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * springmvc的配置文件
 * @author 14435
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer{
    @Value("${spring.servlet.multipart.location}")
    private String path;
    //静态资源的路径映射
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + path);
    }
}
