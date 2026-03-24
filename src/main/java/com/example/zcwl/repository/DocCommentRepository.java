package com.example.zcwl.repository;

import com.example.zcwl.entity.DocComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 文档评论仓库接口
 */
public interface DocCommentRepository extends JpaRepository<DocComment, Long> {

    /**
     * 根据文档ID和章节ID查询评论，按创建时间排序
     * @param dId 文档ID
     * @param cId 章节ID
     * @return 评论列表
     */
    @Query("SELECT c FROM DocComment c WHERE c.dId = :dId AND c.cId = :cId ORDER BY c.createdAt ASC")
    List<DocComment> findByDidAndCid(@Param("dId") Long dId, @Param("cId") Long cId);

    /**
     * 根据父评论ID查询子评论，按创建时间排序
     * @param fa 父评论ID
     * @return 子评论列表
     */
    @Query("SELECT c FROM DocComment c WHERE c.fa = :fa ORDER BY c.createdAt ASC")
    List<DocComment> findByFa(@Param("fa") Long fa);

    /**
     * 根据文档ID和章节ID查询一级评论（fa = -1），按创建时间排序
     * @param dId 文档ID
     * @param cId 章节ID
     * @return 一级评论列表
     */
    @Query("SELECT c FROM DocComment c WHERE c.dId = :dId AND c.cId = :cId AND c.fa = -1 ORDER BY c.createdAt ASC")
    List<DocComment> findFirstLevelComments(@Param("dId") Long dId, @Param("cId") Long cId);
}
