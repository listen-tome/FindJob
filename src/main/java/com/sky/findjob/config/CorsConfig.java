package com.sky.findjob.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许你的前端域名
                .allowedOrigins("https://findjob.aicaipu.top")
                // 允许全部请求方式
                .allowedMethods("*")
                // 允许全部请求头
                .allowedHeaders("*")
                // 支持 cookie / 凭证
                .allowCredentials(true);
    }
}
