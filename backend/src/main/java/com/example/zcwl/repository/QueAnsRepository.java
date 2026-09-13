package com.example.zcwl.repository;

import com.example.zcwl.entity.QueAns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 问答数据访问接口
 * 继承JpaRepository，提供问答实体的CRUD操作和自定义查询
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface QueAnsRepository extends JpaRepository<QueAns, QueAns.QueAnsId> {
    // JpaRepository<QueAns, QueAns.QueAnsId>：
    // - QueAns：实体类类型
    // - QueAns.QueAnsId：实体类的主键类型（复合主键）

    /**
     * 根据对话ID查询所有问答
     * @param dId 对话ID
     * @return 该对话下的所有问答列表
     */
    @Query("SELECT qa FROM QueAns qa WHERE qa.id.dId = ?1")
    // @Query：使用JPQL查询语句，?1表示第一个参数
    // SELECT qa FROM QueAns qa：查询QueAns实体，别名qa
    // WHERE qa.id.dId = ?1：条件是qa实体的id复合主键中的dId字段等于第一个参数
    List<QueAns> findByIdDId(Integer dId);

}