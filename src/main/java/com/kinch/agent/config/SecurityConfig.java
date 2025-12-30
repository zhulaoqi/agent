package com.kinch.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置 - 禁用默认登录，使用自定义登录
 *
 * @author kinch
 * @date 2025-12-29
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf(AbstractHttpConfigurer::disable)
            
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable)
            
            // 禁用HTTP Basic
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // 禁用登出
            .logout(AbstractHttpConfigurer::disable)
            
            // 允许所有请求
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        
        return http.build();
    }
}

