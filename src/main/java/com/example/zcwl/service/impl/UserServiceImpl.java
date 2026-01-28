package com.example.zcwl.service.impl;

import com.example.zcwl.entity.User;
import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.repository.UserRepository;
import com.example.zcwl.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务实现类
 * 实现了UserService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数注入
     * @param userRepository 用户数据访问接口实例
     * @param passwordEncoder 密码编码器实例
     */
    @Autowired  // 自动注入依赖
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建用户
     * @param userDTO 用户数据传输对象，包含用户信息
     * @return 创建成功的用户实体
     */
    @Override
    public User createUser(UserDTO userDTO) {
        logger.info("Creating user: {}", userDTO.getUsername());

        // 显式验证用户名长度，确保不超过数据库字段限制
        if (userDTO.getUsername() == null || userDTO.getUsername().length() < 6 || userDTO.getUsername().length() > 20) {
            logger.warn("Username length invalid: {}", userDTO.getUsername());
            throw new RuntimeException("用户名长度必须在6-20个字符之间");
        }
        
        // 显式验证邮箱长度，确保不超过数据库字段限制
        if (userDTO.getEmail() == null || userDTO.getEmail().length() > 20) {
            logger.warn("Email length invalid: {}", userDTO.getEmail());
            throw new RuntimeException("邮箱长度不能超过20个字符");
        }

        // 检查密码是否与用户名相同
        if (userDTO.getPassword().equals(userDTO.getUsername())) {
            logger.warn("Password cannot be the same as username for user: {}", userDTO.getUsername());
            throw new RuntimeException("密码不能与用户名相同");
        }

        // 验证密码强度
        validatePasswordStrength(userDTO.getPassword());

        // 检查用户名是否已存在
        if (userRepository.findByName(userDTO.getUsername()) != null) {
            logger.warn("Username already exists: {}", userDTO.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(userDTO.getEmail()) != null) {
            logger.warn("Email already exists: {}", userDTO.getEmail());
            throw new RuntimeException("Email already exists");
        }

        // 检查用户ID是否已存在
        if (userRepository.findByuId(userDTO.getUsername()) != null) {
            logger.warn("User ID already exists: {}", userDTO.getUsername());
            throw new RuntimeException("用户ID已存在");
        }

        // 创建User实体对象
        User user = new User();
        // 设置用户ID
        user.setUId(userDTO.getUsername());
        // 设置用户名
        user.setName(userDTO.getUsername());
        // 设置邮箱
        user.setEmail(userDTO.getEmail());
        // 使用明文存储密码，便于前期维护
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        // 调用Repository的save方法保存用户实体
        User savedUser = userRepository.save(user);
        logger.info("User created successfully: {}", savedUser.getUId());
        return savedUser;
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 包含用户实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<User> getUserById(String id) {
        logger.debug("Getting user by id: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.debug("Found user: {}", user.get().getUId());
        } else {
            logger.debug("User not found: {}", id);
        }
        return user;
    }

    /**
     * 查询所有用户（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的用户列表
     */
    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        logger.debug("Getting all users with pageable: {}", pageable);
        Page<User> users = userRepository.findAll(pageable);
        logger.debug("Found {} users", users.getTotalElements());
        return users;
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param userDTO 更新后的用户信息
     * @return 更新成功的用户实体
     * @throws RuntimeException 如果用户不存在
     */
    @Override
    public User updateUser(String id, UserDTO userDTO) {
        logger.info("Updating user: {}", id);
        // 先根据ID查询现有用户
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            // 如果存在，更新字段值
            User user = optionalUser.get();
            
            // 手动设置各个字段，确保密码被正确加密
            user.setName(userDTO.getUsername());
            user.setEmail(userDTO.getEmail());
            
            // 如果提供了密码，则使用明文存储
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                // 验证密码强度
                validatePasswordStrength(userDTO.getPassword());
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
                logger.debug("Updated password for user: {}", id);
            }
            
            // 保存更新后的实体
            User updatedUser = userRepository.save(user);
            logger.info("User updated successfully: {}", updatedUser.getUId());
            return updatedUser;
        }
        // 如果不存在，抛出运行时异常
        logger.warn("User not found for update: {}", id);
        throw new RuntimeException("User not found with id: " + id);
    }

    /**
     * 删除用户
     * @param id 用户ID
     */
    @Override
    public void deleteUser(String id) {
        logger.info("Deleting user: {}", id);
        // 调用Repository的deleteById方法根据ID删除
        userRepository.deleteById(id);
        logger.info("User deleted successfully: {}", id);
    }

    /**
     * 验证密码强度
     * @param password 密码
     * @throws RuntimeException 如果密码强度不足
     */
    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new RuntimeException("密码长度不能少于8个字符");
        }
        if (password.length() > 20) {
            throw new RuntimeException("密码长度不能超过20个字符");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("密码必须包含至少一个大写字母");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("密码必须包含至少一个小写字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("密码必须包含至少一个数字");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>/?].*")) {
            throw new RuntimeException("密码必须包含至少一个特殊字符");
        }
    }
}