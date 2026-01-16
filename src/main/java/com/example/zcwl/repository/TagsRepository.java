package com.example.zcwl.repository;

import com.example.zcwl.entity.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 标签数据访问接口
 * 提供标签相关的数据库操作
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface TagsRepository extends JpaRepository<Tags, Integer> {
    // JpaRepository<实体类, 主键类型> 提供了基本的CRUD操作和分页排序功能

    /**
     * 根据标签名称查询标签
     * @param name 标签名称
     * @return 匹配的标签实体，如果未找到则返回null
     */
    Tags findByName(String name);

}