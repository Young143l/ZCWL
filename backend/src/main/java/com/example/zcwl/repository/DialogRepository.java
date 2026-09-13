package com.example.zcwl.repository;

import com.example.zcwl.entity.Dialog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 对话数据访问接口
 * 提供对话相关的数据库操作
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface DialogRepository extends JpaRepository<Dialog, Integer> {
    // JpaRepository<实体类, 主键类型> 提供了基本的CRUD操作和分页排序功能

    /**
     * 根据用户ID查询所有对话
     * @param uId 用户ID
     * @return 该用户的所有对话列表
     */
    @Query("SELECT d FROM Dialog d WHERE d.user.uId = ?1")
    // @Query：使用JPQL查询语句，根据用户ID查询所有对话
    List<Dialog> findByUserUId(String uId);

    /**
     * 根据对话ID查询对话，并同时加载关联的用户对象
     * 使用join fetch避免懒加载错误
     * @param id 对话ID
     * @return 包含用户对象的对话
     */
    @Query("SELECT d FROM Dialog d JOIN FETCH d.user WHERE d.dId = ?1")
    Optional<Dialog> findByIdWithUser(Integer id);

}