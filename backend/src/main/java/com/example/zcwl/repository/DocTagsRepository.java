package com.example.zcwl.repository;

import com.example.zcwl.entity.DocTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档标签关联数据访问接口
 * 提供文档标签关联相关的数据库操作
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface DocTagsRepository extends JpaRepository<DocTags, DocTags.DocTagsId> {
    // JpaRepository<实体类, 复合主键类型> 提供了基本的CRUD操作和分页排序功能

    /**
     * 根据文档ID查询所有关联的标签
     * @param docId 文档ID
     * @return 该文档下所有标签关联关系的列表
     */
    @Query("SELECT dt FROM DocTags dt WHERE dt.id.docId = ?1")
    // @Query：使用JPQL查询语句，根据文档ID查询所有关联的标签
    List<DocTags> findByIdDocId(Integer docId);

    /**
     * 根据标签ID查询所有关联的文档
     * @param tId 标签ID
     * @return 该标签下所有文档关联关系的列表
     */
    @Query("SELECT dt FROM DocTags dt WHERE dt.id.tId = ?1")
    // @Query：使用JPQL查询语句，根据标签ID查询所有关联的文档
    List<DocTags> findByIdTId(Integer tId);

}