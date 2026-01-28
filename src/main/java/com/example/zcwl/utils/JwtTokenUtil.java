package com.example.zcwl.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT token工具类
 * 用于生成、验证和解析JWT token
 */
@Component
public class JwtTokenUtil {

    // 密钥，用于签名和验证token
    private SecretKey secretKey;

    // token过期时间（毫秒）
    @Value("${jwt.expire-time:86400000}")
    private long expireTime;

    // 从配置文件读取密钥
    @Value("${jwt.secret-key:your-secret-key-for-jwt-token-generation-and-validation}")
    private String jwtSecretKey;

    /**
     * 构造函数
     */
    public JwtTokenUtil() {
        // 构造函数为空，密钥初始化在@PostConstruct中执行
    }

    /**
     * 在依赖注入完成后初始化密钥
     * 使用@PostConstruct确保@Value注解的值已经注入
     */
    @javax.annotation.PostConstruct
    public void init() {
        // 从配置文件读取密钥，配置文件中已使用环境变量占位符
        // 这样Spring Boot会自动从环境变量中读取值，环境变量不存在时使用默认值
        byte[] keyBytes = this.jwtSecretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从token中提取用户名
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中提取过期时间
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中提取指定的声明
     * @param token JWT token
     * @param claimsResolver 声明解析器
     * @param <T> 声明类型
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中提取所有声明
     * @param token JWT token
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        // 确保密钥已初始化
        if (secretKey == null) {
            init();
        }
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查token是否过期
     * @param token JWT token
     * @return 是否过期
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 生成token
     * @param userDetails 用户详情
     * @return JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * 生成token
     * @param claims 自定义声明
     * @param subject 主题（通常是用户名）
     * @return JWT token
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expireTime);

        // 确保密钥已初始化
        if (secretKey == null) {
            init();
        }

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(createdDate)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证token
     * @param token JWT token
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 验证token
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 解析token并获取声明
            getAllClaimsFromToken(token);
            // 检查token是否过期
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
