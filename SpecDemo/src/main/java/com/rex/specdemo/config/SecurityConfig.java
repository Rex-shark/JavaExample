package com.rex.specdemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 安全過濾鏈配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 停用 CSRF（REST API 不需要）
                .csrf(AbstractHttpConfigurer::disable)
                // 無狀態 Session 管理（使用 JWT）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置路徑權限
                .authorizeHttpRequests(auth -> auth
                        // 公開路徑
                        .requestMatchers(
                                "/api/auth/**", // 認證相關 API
                                "/swagger-ui/**", // Swagger UI
                                "/swagger-ui.html", // Swagger UI HTML
                                "/v3/api-docs/**", // OpenAPI 規格
                                "/swagger-resources/**", // Swagger 資源
                                "/webjars/**" // WebJars 資源
                        ).permitAll()
                        // 其他路徑需要認證
                        .anyRequest().authenticated())
                // 加入 JWT 過濾器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密碼加密器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
