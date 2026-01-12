package com.medical.research.config;

import com.medical.research.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启方法级权限控制（@PreAuthorize等注解）
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * 密码编码器（必须配置，否则认证失败）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者（关联UserDetailsService和密码编码器）
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * 认证管理器（用于登录接口手动认证）
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Security过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. 关闭CSRF（前后端分离场景无需CSRF）
        http.csrf(csrf -> csrf.disable())
                // 2. 关闭Session（JWT无状态认证）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3. 配置请求权限
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/login").permitAll() // 登录接口放行
                        .antMatchers("/error/**").permitAll() // 公开接口放行
                        .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .antMatchers("/download/**").permitAll()
                        .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated() // 其他请求需要认证
                )
                // 4. 添加JWT过滤器（在用户名密码过滤器之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 5. 配置认证提供者
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}