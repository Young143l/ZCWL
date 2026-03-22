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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;
import java.util.List;

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
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(plainTextPasswordEncoder());
        return authProvider;
    }

    /**
     * 配置HTTP安全规则
     * @param http HTTP安全
     * @return 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                // 禁用CSRF保护
                .csrf(AbstractHttpConfigurer::disable)
                // 启用CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许所有用户访问的路径
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/validate-token").permitAll()
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        // 允许 code 相关路径
                        .requestMatchers("/code/**").permitAll()
                        // 允许 project 相关路径
                        .requestMatchers("/project/**").permitAll()
                        // GET /users/:id 需要认证
                        // DELETE /users/** 需要认证
                        .requestMatchers("/doc/**").permitAll()
                        .requestMatchers("/api/doc-contents/**").permitAll()
                        // 允许 AI 聊天文档路径
                        .requestMatchers("/aichatdoc/**").permitAll()
                        // 允许 RAG 相关 API 路径（包括内部调用）
                        .requestMatchers("/api/rag/**").permitAll()
                        .requestMatchers("/rag/**").permitAll()
                        // 允许静态资源
                        .requestMatchers("/*.html", "/*.css", "/*.js", "/static/**", "/public/**").permitAll()
                        // 允许所有 OPTIONS 请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 允许 Swagger 相关路径
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        // 其他路径需要认证
                        .anyRequest().authenticated())
                // 配置会话管理策略为无状态
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置认证提供者
                .authenticationProvider(authenticationProvider())
                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
        configuration.setExposedHeaders(List.of("Content-Length"));
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 注册CORS配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 明文密码编码器
     * 自定义实现，直接比较明文密码，便于前期维护
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder plainTextPasswordEncoder() {
        // 自定义PasswordEncoder实现，直接比较明文密码
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                // 直接返回明文密码
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // 直接比较明文密码
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    /**
     * 认证管理器
     * @param config 认证配置
     * @return 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}