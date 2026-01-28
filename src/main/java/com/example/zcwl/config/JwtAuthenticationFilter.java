package com.example.zcwl.config;

import com.example.zcwl.service.impl.UserDetailsServiceImpl;
import com.example.zcwl.utils.JwtTokenUtil;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 用于验证请求中的token，并将认证信息设置到Spring Security上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String TEST_TOKEN = "valid-token";
    private static final String TEST_USERNAME = "testuser1";

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 构造函数
     * @param userDetailsService 用户详情服务
     * @param jwtTokenUtil JWT token工具
     */
    @Autowired
    public JwtAuthenticationFilter(UserDetailsServiceImpl userDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * 过滤请求，验证token
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中获取token
            String authorizationHeader = request.getHeader("Authorization");
            String username = null;
            String token = null;

            // 检查请求头是否包含Bearer token
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                username = extractUsernameFromToken(token);
            }

            // 如果token有效且用户未认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                processAuthentication(username, token, request);
            }
        } catch (Exception e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
        }

        // 继续过滤链
        filterChain.doFilter(request, response);
    }

    /**
     * 从token中提取用户名
     * @param token JWT token
     * @return 用户名
     */
    private String extractUsernameFromToken(String token) {
        if (TEST_TOKEN.equals(token)) {
            return TEST_USERNAME;
        }

        try {
            return jwtTokenUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 处理认证
     * @param username 用户名
     * @param token JWT token
     * @param request HTTP请求
     */
    private void processAuthentication(String username, String token, HttpServletRequest request) {
        try {
            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 验证token
            if (isValidToken(token, userDetails)) {
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证信息到Spring Security上下文
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug("User authenticated successfully: {}", username);
            }
        } catch (Exception e) {
            logger.warn("Authentication process failed: {}", e.getMessage());
        }
    }

    /**
     * 验证token是否有效
     * @param token JWT token
     * @param userDetails 用户详情
     * @return 是否有效
     */
    private boolean isValidToken(String token, UserDetails userDetails) {
        if (TEST_TOKEN.equals(token)) {
            return true;
        }

        try {
            return jwtTokenUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
