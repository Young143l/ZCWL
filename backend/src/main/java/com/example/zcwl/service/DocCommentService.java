package com.example.zcwl.service;

import com.example.zcwl.entity.DocComment;

import java.util.List;
import java.util.Map;

/**
 * 文档评论服务接口
 */
public interface DocCommentService {

    /**
     * 获取指定文档章节的评论（包含二级评论）
     * @param dId 文档ID
     * @param cId 章节ID
     * @return 评论列表，包含children字段
     */
    List<Map<String, Object>> getComments(Long dId, Long cId);

    /**
     * 保存评论
     * @param comment 评论信息
     * @return 保存后的评论
     */
    DocComment saveComment(DocComment comment);

    /**
     * 验证评论层级是否合法（最多两层）
     * @param fa 父评论ID
     * @return 是否合法
     */
    boolean validateCommentLevel(Long fa);
}
