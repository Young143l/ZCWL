package com.example.zcwl.service.impl;

import com.example.zcwl.entity.User;
import com.example.zcwl.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 用户详情服务实现类
 * 用于从数据库中加载用户信息，供Spring Security使用
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 构造函数
     * @param userRepository 用户数据访问接口
     */
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户名或用户ID加载用户详情
     * @param username 用户名或用户ID
     * @return 用户详情
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 先尝试根据用户名查询用户
        User user = userRepository.findByName(username);
        
        // 如果没有找到，再尝试根据用户ID查询用户
        if (user == null) {
            user = userRepository.findByuId(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found with username or userId: " + username);
            }
        }

        // 创建UserDetails对象，用于Spring Security认证
        return new org.springframework.security.core.userdetails.User(
                user.getUId(),
                user.getPassword(),
                new ArrayList<>() // 权限列表，暂时为空
        );
    }

    /**
     * 根据用户名获取用户实体
     * @param username 用户名
     * @return 用户实体
     */
    public User getUserByUsername(String username) {
        return userRepository.findByName(username);
    }

    /**
     * 根据用户ID获取用户实体
     * @param userId 用户ID
     * @return 用户实体
     */
    public User getUserByUserId(String userId) {
        return userRepository.findByuId(userId);
    }

    /**
     * 根据用户ID加载用户详情
     * @param userId 用户ID
     * @return 用户详情
     * @throws UsernameNotFoundException 如果用户不存在
     */
    public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
        // 根据用户ID查询用户
        User user = userRepository.findByuId(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }

        // 创建UserDetails对象，用于Spring Security认证
        return new org.springframework.security.core.userdetails.User(
                user.getUId(),
                user.getPassword(),
                new ArrayList<>() // 权限列表，暂时为空
        );
    }
}
