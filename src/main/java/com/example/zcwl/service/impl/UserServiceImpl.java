package com.example.zcwl.service.impl;

import com.example.zcwl.entity.User;
import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.repository.UserRepository;
import com.example.zcwl.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务实现类
 * 实现了UserService接口定义的业务逻辑方法
 */
@Service  // 声明这是一个服务类，用于业务逻辑实现
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * 构造函数注入
     * @param userRepository 用户数据访问接口实例
     */
    @Autowired  // 自动注入UserRepository依赖，用于数据库操作
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 创建用户
     * @param userDTO 用户数据传输对象，包含用户信息
     * @return 创建成功的用户实体
     */
    @Override
    public User createUser(UserDTO userDTO) {
        // 创建User实体对象
        User user = new User();
        // 使用BeanUtils复制属性，将userDTO的属性值复制到user对象
        BeanUtils.copyProperties(userDTO, user);
        // 调用Repository的save方法保存用户实体
        return userRepository.save(user);
    }

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 包含用户实体的Optional对象，如果不存在则返回Optional.empty()
     */
    @Override
    public Optional<User> getUserById(String id) {
        // 调用Repository的findById方法根据ID查询
        return userRepository.findById(id);
    }

    /**
     * 查询所有用户（分页）
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页后的用户列表
     */
    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        // 调用Repository的findAll方法，带分页参数
        return userRepository.findAll(pageable);
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
        // 先根据ID查询现有用户
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            // 如果存在，更新字段值
            User user = optionalUser.get();
            // 使用BeanUtils复制属性，将userDTO的属性值复制到user对象
            BeanUtils.copyProperties(userDTO, user);
            // 保存更新后的实体
            return userRepository.save(user);
        }
        // 如果不存在，抛出运行时异常
        throw new RuntimeException("User not found with id: " + id);
    }

    /**
     * 删除用户
     * @param id 用户ID
     */
    @Override
    public void deleteUser(String id) {
        // 调用Repository的deleteById方法根据ID删除
        userRepository.deleteById(id);
    }

}