package com.github.listen_to_me.common.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.github.listen_to_me.common.Result;
import com.github.listen_to_me.common.filter.JwtAuthenticationFilter;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启注解权限管理，允许在 Controller 方法上添加 @PreAuthorize 限定权限
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 使用 BCrypt 强哈希算法，用户登录时会调用并与数据库密码比对
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 作为认证逻辑的入口，供 Controller 显式调用以触发认证流程
    // 只要容器中存在 UserDetailsService 和 PasswordEncoder 的 Bean，框架会自动将它们组合成一个默认的
    // DaoAuthenticationProvider 并注入到 AuthenticationManager 中
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 安全过滤链配置
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable) // 前后端分离的项目不需要 CSRF
                // 不通过 Session 记忆用户
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/common/auth/refresh").authenticated()
                        .requestMatchers("/api/common/auth/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint((req, res, ex) -> {
                            renderJson(res, HttpStatus.HTTP_UNAUTHORIZED, Result.fail(401, "认证失败，请重新登录"));
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            renderJson(res, HttpStatus.HTTP_FORBIDDEN, Result.fail(403, "权限不足，拒绝访问"));
                        }))
                .build();
    }

    private void renderJson(HttpServletResponse response, int status, Result<?> result) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(result));
    }

}
