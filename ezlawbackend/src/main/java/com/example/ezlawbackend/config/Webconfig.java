package com.example.ezlawbackend.config;

import com.example.ezlawbackend.interceptor.SessionAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;

@Configuration
public class Webconfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000"); // Your React app's URL
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionAuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/signup",
                        "/api/lawyerauth/login",
                        "/api/lawyerauth/signup",
                        "/api/webhook/stripe"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/webhook/stripe")
                .allowedOrigins("https://879b-2405-9800-b651-e1c3-f8b7-d67b-ba24-31b1.ngrok-free.app")
                .allowedOrigins(" http://127.0.0.1:8080")
                .allowedOrigins(" http://localhos:3000")
                .allowedMethods(HttpMethod.POST.name())
                .allowCredentials(true);
    }
}