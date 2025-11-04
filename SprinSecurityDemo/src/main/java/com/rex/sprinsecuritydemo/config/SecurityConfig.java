package com.rex.sprinsecuritydemo.config;

import com.rex.sprinsecuritydemo.filter.BearerAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST API 通常關掉 CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll() // 這裡開放某些API
                        .anyRequest().authenticated()              // 其他都要驗證
                )
                .addFilterBefore(new BearerAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}