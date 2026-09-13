package com.example.zcwl.repository;

import com.example.zcwl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户数据访问接口
 * 提供用户相关的数据库操作
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface UserRepository extends JpaRepository<User, String> {
    // JpaRepository<实体类, 主键类型> 提供了基本的CRUD操作和分页排序功能

    /**
     * 根据用户名查询用户
     * @param name 用户名
     * @return 匹配的用户实体，如果未找到则返回null
     */
    User findByName(String name);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 匹配的用户实体，如果未找到则返回null
     */
    User findByEmail(String email);

    /**
     * 根据用户ID查询用户
     * @param uId 用户ID
     * @return 匹配的用户实体，如果未找到则返回null
     */
    User findByuId(String uId);

}