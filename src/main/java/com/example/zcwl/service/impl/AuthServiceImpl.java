package com.example.zcwl.service.impl;

import com.example.zcwl.dto.LoginRequestDTO;
import com.example.zcwl.dto.LoginResponseDTO;
import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.entity.User;
import com.example.zcwl.repository.UserRepository;
import com.example.zcwl.service.AuthService;
import com.example.zcwl.service.UserDetailsServiceImpl;
import com.example.zcwl.utils.JwtTokenUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 * 实现登录和注册逻辑
 */
@Service
public class AuthServiceImpl implements AuthService {

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
            // 直接根据用户ID查询用户
            User user = userDetailsService.getUserByUserId(loginRequestDTO.getUserId());
            
            // 检查用户是否存在
            if (user == null) {
                System.out.println("User not found for userId: " + loginRequestDTO.getUserId());
                throw new RuntimeException("Invalid userId or password");
            }
            
            // 验证密码（直接比较明文密码，由于数据库表结构限制）
            if (!loginRequestDTO.getPassword().equals(user.getPassword())) {
                System.out.println("Password mismatch for userId: " + loginRequestDTO.getUserId());
                throw new RuntimeException("Invalid userId or password");
            }
            
            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUserId(loginRequestDTO.getUserId());

            // 生成token
            String token = jwtTokenUtil.generateToken(userDetails);

            // 创建登录响应
            return new LoginResponseDTO(token, user.getName(), user.getUId());
        } catch (Exception e) {
            // 认证失败，打印简洁的错误信息以便调试
            System.out.println("Login failed for userId: " + loginRequestDTO.getUserId());
            System.out.println("Exception: " + e.getMessage());
            // 认证失败，抛出运行时异常
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
        // 检查用户名是否已存在
        if (userRepository.findByName(userDTO.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(userDTO.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        // 创建用户实体
        User user = new User();
        
        // 手动设置User对象的属性，避免复制phone字段
        // 设置用户ID（这里简单使用用户名作为ID）
        user.setUId(userDTO.getUsername());
        // 设置用户名
        user.setName(userDTO.getUsername());
        // 设置邮箱
        user.setEmail(userDTO.getEmail());
        // 使用明文密码存储（由于数据库表结构限制）
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
