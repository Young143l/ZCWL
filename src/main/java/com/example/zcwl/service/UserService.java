package com.example.zcwl.service;

import com.example.zcwl.entity.User;
import com.example.zcwl.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 用户服务接口
 * 提供用户相关的业务逻辑操作
 */
public interface UserService {

    /**
     * 创建用户
     * @param userDTO 用户数据传输对象，包含用户的基本信息
     * @return 创建成功的用户实体对象
     */
    User createUser(UserDTO userDTO);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 包含用户实体的Optional对象，如果未找到则返回Optional.empty()
     */
    Optional<User> getUserById(String id);

    /**
     * 查询所有用户（分页）
     * @param pageable 分页参数，包含页码、每页大小等信息
     * @return 包含用户实体的分页结果
     */
    Page<User> getAllUsers(Pageable pageable);

    /**
     * 更新用户
     * @param id 用户ID
     * @param userDTO 包含更新信息的用户数据传输对象
     * @return 更新后的用户实体对象
     */
    User updateUser(String id, UserDTO userDTO);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void deleteUser(String id);

}