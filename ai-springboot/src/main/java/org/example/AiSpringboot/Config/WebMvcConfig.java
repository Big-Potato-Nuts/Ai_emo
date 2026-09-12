package org.example.AiSpringboot.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 职责：把 /uploads/** 的静态资源请求映射到服务器磁盘上的 uploads 目录
 * 说明：
 * 文件上传接口把图片保存在程序运行目录下的 uploads 文件夹中（见 FileService），
 * 浏览器访问 /uploads/xxx.png 时，Spring MVC 默认不会去磁盘找文件，
 * 需要这里手动注册资源映射，否则前端拼接的图片地址会 404。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 计算上传目录的磁盘绝对路径（程序运行目录 + uploads）
        String uploadPath = System.getProperty("user.dir") + java.io.File.separator + "uploads";
        // 2. 注册资源映射：URL 前缀 /uploads/** 对应磁盘目录 file:{uploadPath}/
        //    注意：Windows 路径需以 file: 开头，并保证路径末尾带斜杠
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + java.io.File.separator);
    }
}
