package com.example.zcwl.repository;

import com.example.zcwl.entity.DocRag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * DocRagRepository接口
 * 用于操作doc_rag表，提供向量搜索功能
 */
public interface DocRagRepository extends JpaRepository<DocRag, Integer> {

    /**
     * 向量搜索方法
     * 使用PostgresSQL的向量相似度查询
     * @param vector 查询向量
     * @param limit 返回结果数量限制
     * @return 包含id、chunk、d_id、c_id和distance的Map列表
     */
    @Query(value = "SELECT id, chunk, d_id, c_id, vector <=> CAST(:vector AS vector) AS distance " +
            "FROM doc_rag " +
            "ORDER BY vector <=> CAST(:vector AS vector) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Map<String, Object>> searchSimilar(@Param("vector") String vector, @Param("limit") int limit);

    /**
     * 统计doc_rag表中的记录数
     * @return 记录数
     */
    long count();

    /**
     * 获取前N条记录，用于测试
     * @return DocRag列表
     */
    List<DocRag> findTop5ByIdNotNull();
}
