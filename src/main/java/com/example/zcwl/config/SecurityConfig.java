package com.example.zcwl.config;

import com.example.zcwl.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;

/**
 * Spring Security配置类
 * 用于配置安全规则和过滤器链
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数
     * @param userDetailsService 用户详情服务
     * @param jwtAuthenticationFilter JWT认证过滤器
     */
    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置认证提供者
     * @return 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(plainTextPasswordEncoder());
        return authProvider;
    }

    /**
     * 配置HTTP安全规则
     * @param http HTTP安全
     * @return 安全过滤器链
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护
                .csrf().disable()
                // 启用CORS
                .cors().and()
                // 配置授权规则
                .authorizeHttpRequests()
                // 允许所有用户访问的路径
                .antMatchers("/").permitAll()
                .antMatchers("/validate-token").permitAll()
                .antMatchers("/register").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/users/login").permitAll()
                .antMatchers(HttpMethod.POST, "/users").permitAll()
                // GET /users/:id 需要认证
                // DELETE /users/** 需要认证
                .antMatchers("/doc/**").permitAll()
                .antMatchers("/api/doc-contents/**").permitAll()
                // 允许Swagger相关路径
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                // 其他路径需要认证
                .anyRequest().authenticated()
                .and()
                // 配置会话管理策略为无状态
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // 配置认证提供者
                .and()
                .authenticationProvider(authenticationProvider());

        // 添加JWT认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置源
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源模式（使用allowedOriginPatterns替代allowedOrigins，避免与allowCredentials冲突）
        configuration.addAllowedOriginPattern("*");
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的HTTP头
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        // 允许暴露的HTTP头
        configuration.setExposedHeaders(Arrays.asList("Content-Length"));
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 注册CORS配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 明文密码编码器
     * 使用NoOpPasswordEncoder，直接比较明文密码，便于前期维护
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder plainTextPasswordEncoder() {
        // 使用NoOpPasswordEncoder，直接比较明文密码
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    /**
     * 认证管理器
     * @param config 认证配置
     * @return 认证管理器
     * @throws Exception 异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}