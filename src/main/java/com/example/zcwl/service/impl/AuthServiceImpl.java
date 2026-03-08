package com.example.zcwl.service.impl;

import com.example.zcwl.dto.LoginRequestDTO;
import com.example.zcwl.dto.LoginResponseDTO;
import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.entity.User;
import com.example.zcwl.repository.UserRepository;
import com.example.zcwl.service.AuthService;
import com.example.zcwl.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 * 实现登录和注册逻辑
 */
@Service
public class AuthServiceImpl implements AuthService {

    static {
        LoggerFactory.getLogger(AuthServiceImpl.class);
    }

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数
     * @param userDetailsService 用户详情服务
     * @param jwtTokenUtil JWT token工具
     * @param userRepository 用户数据访问接口
     * @param passwordEncoder 密码编码器
     */
    @Autowired
    public AuthServiceImpl(
            UserDetailsServiceImpl userDetailsService,
            JwtTokenUtil jwtTokenUtil,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录
     * @param loginRequestDTO 登录请求数据
     * @return 登录响应数据，包含username、userId和token
     * @throws RuntimeException 如果认证失败
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        try {
            System.out.println("AuthServiceImpl.login called: userId=" + loginRequestDTO.getUserId() + ", password=" + loginRequestDTO.getPassword());
            
            // 直接从数据库查找用户
            User user = userRepository.findByuId(loginRequestDTO.getUserId());
            System.out.println("User found by uId: " + user);
            
            if (user == null) {
                user = userRepository.findByName(loginRequestDTO.getUserId());
                System.out.println("User found by name: " + user);
            }
            
            if (user == null) {
                System.out.println("User not found: " + loginRequestDTO.getUserId());
                throw new RuntimeException("Invalid userId or password");
            }
            
            System.out.println("Found user: name=" + user.getName() + ", uId=" + user.getUId() + ", password=" + user.getPassword());
            
            // 验证密码
            if (!user.getPassword().equals(loginRequestDTO.getPassword())) {
                System.out.println("Password mismatch for user: " + user.getName());
                System.out.println("Expected password: " + user.getPassword() + ", Actual password: " + loginRequestDTO.getPassword());
                throw new RuntimeException("Invalid userId or password");
            }
            
            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getName());
            System.out.println("User details loaded successfully: " + userDetails.getUsername());
            
            // 生成JWT token
            System.out.println("Generating token with JwtTokenUtil...");
            String token = jwtTokenUtil.generateToken(userDetails);
            System.out.println("Generated token: " + token);
            
            // 测试token验证
            System.out.println("Testing token validation...");
            boolean isValid = jwtTokenUtil.validateToken(token, userDetails);
            System.out.println("Token validation result: " + isValid);
            
            // 返回登录响应
            LoginResponseDTO response = new LoginResponseDTO(token, user.getName(), user.getUId(), null);
            System.out.println("Login response: " + response);
            return response;
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            throw new RuntimeException("Invalid userId or password");
        }
    }

    /**
     * 用户注册
     * @param userDTO 用户注册数据
     * @return 注册成功的用户信息
     */
    @Override
    public User register(UserDTO userDTO) {
        // 验证用户名长度
        if (userDTO.getUserName() == null || userDTO.getUserName().length() < 6 || userDTO.getUserName().length() > 20) {
            throw new RuntimeException("用户名长度必须在6-20个字符之间");
        }
        
        // 验证邮箱长度
        if (userDTO.getEmail() == null || userDTO.getEmail().length() > 20) {
            throw new RuntimeException("邮箱长度不能超过20个字符");
        }
        
        // 验证密码长度
        if (userDTO.getPassword() == null || userDTO.getPassword().length() < 8 || userDTO.getPassword().length() > 20) {
            throw new RuntimeException("密码长度必须在8-20个字符之间");
        }

        // 检查密码是否与用户名相同
        if (userDTO.getPassword().equals(userDTO.getUserName())) {
            throw new RuntimeException("密码不能与用户名相同");
        }

        // 检查用户名是否已存在
        if (userRepository.findByName(userDTO.getUserName()) != null) {
            throw new RuntimeException("Username already exists");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(userDTO.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        // 检查用户ID是否已存在
        if (userRepository.findByuId(userDTO.getUserName()) != null) {
            throw new RuntimeException("用户ID已存在");
        }

        // 创建用户实体
        User user = new User();
        
        // 手动设置User对象的属性，避免复制phone字段
        // 设置用户ID（这里简单使用用户名作为ID）
        user.setUId(userDTO.getUserName());
        // 设置用户名
        user.setName(userDTO.getUserName());
        // 设置邮箱
        user.setEmail(userDTO.getEmail());
        // 直接存储明文密码，便于前期维护
        user.setPassword(userDTO.getPassword());

        // 保存用户
        return userRepository.save(user);
    }

    /**
     * 验证token
     * @param token JWT token
     * @return 是否有效
     */
    @Override
    public boolean validateToken(String token) {
        return jwtTokenUtil.validateToken(token);
    }
}
