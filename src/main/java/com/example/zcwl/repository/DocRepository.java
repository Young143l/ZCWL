package com.example.zcwl.repository;

import com.example.zcwl.entity.Doc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 文档数据访问接口
 * 提供文档相关的数据库操作
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface DocRepository extends JpaRepository<Doc, Integer> {
    // JpaRepository<实体类, 主键类型> 提供了基本的CRUD操作和分页排序功能

    /**
     * 根据文档名称查询文档
     * @param docName 文档名称
     * @return 匹配的文档实体，如果未找到则返回null
     */
    Doc findByDocName(String docName);

}