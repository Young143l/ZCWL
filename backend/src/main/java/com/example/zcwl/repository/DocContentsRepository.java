package com.example.zcwl.repository;

import com.example.zcwl.entity.DocContents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档内容数据访问接口
 * 提供文档内容相关的数据库操作
 */
@Repository  // 声明这是一个数据访问层组件，用于数据库操作
public interface DocContentsRepository extends JpaRepository<DocContents, DocContents.DocContentsId> {
    // JpaRepository<实体类, 复合主键类型> 提供了基本的CRUD操作和分页排序功能

    /**
     * 根据文档ID查询所有章节内容
     * @param docId 文档ID
     * @return 该文档下所有章节内容的列表
     */
    @Query("SELECT dc FROM DocContents dc WHERE dc.id.docId = ?1")
    // @Query：使用JPQL查询语句，根据文档ID查询所有章节内容
    List<DocContents> findByIdDocId(Integer docId);

}