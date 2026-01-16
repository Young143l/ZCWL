package com.example.zcwl.repository;

import com.example.zcwl.entity.UserTagPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户标签偏好数据访问接口
 * 提供用户标签偏好相关的数据库操作
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface UserTagPreferencesRepository extends JpaRepository<UserTagPreferences, UserTagPreferences.UserTagPreferencesId> {
    // JpaRepository<实体类, 复合主键类型> 提供了基本的CRUD操作和分页排序功能

    /**
     * 根据用户ID查询所有标签偏好
     * @param uId 用户ID
     * @return 该用户对所有标签的偏好列表
     */
    @Query("SELECT utp FROM UserTagPreferences utp WHERE utp.id.uId = ?1")
    // @Query：使用JPQL查询语句，根据用户ID查询所有标签偏好
    List<UserTagPreferences> findByIdUId(String uId);

    /**
     * 根据标签ID查询所有用户偏好
     * @param tId 标签ID
     * @return 所有用户对该标签的偏好列表
     */
    @Query("SELECT utp FROM UserTagPreferences utp WHERE utp.id.tId = ?1")
    // @Query：使用JPQL查询语句，根据标签ID查询所有用户偏好
    List<UserTagPreferences> findByIdTId(Integer tId);

}