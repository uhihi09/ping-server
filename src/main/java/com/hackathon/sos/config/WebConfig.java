package com.hackathon.sos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // ✅ "*" 대신 명시적으로 origin 지정
                .allowedOrigins(
                    "http://localhost:5173",      // Vite 개발 서버
                    "http://localhost:3000",      // 추가 개발 서버
                    "http://127.0.0.1:5173"       // localhost 대신 IP
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 쿠키/인증 허용
                .maxAge(3600);
    }
}