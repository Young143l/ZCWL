package com.example.zcwl.service;

import com.example.zcwl.dto.LoginRequestDTO;
import com.example.zcwl.dto.LoginResponseDTO;
import com.example.zcwl.dto.UserDTO;
import com.example.zcwl.entity.User;

/**
 * 认证服务接口
 * 定义登录和注册方法
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequestDTO 登录请求数据
     * @return 登录响应数据，包含token和用户信息
     */
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    /**
     * 用户注册
     * @param userDTO 用户注册数据
     * @return 注册成功的用户信息
     */
    User register(UserDTO userDTO);

    /**
     * 验证token
     * @param token JWT token
     * @return 是否有效
     */
    boolean validateToken(String token);
}
