package com.example.zcwl.service;

import java.util.List;
import java.util.Map;

/**
 * AI智能测验服务接口
 * 基于文档内容自动生成测验题目并批改答案
 */
public interface QuizService {

    /**
     * 根据文档内容生成测验题目
     * @param docId 文档ID
     * @param chapterId 章节ID（可为空，为空则基于整个文档）
     * @param questionCount 题目数量（默认5题）
     * @return 包含题目的Map列表
     */
    List<Map<String, Object>> generateQuiz(Integer docId, Integer chapterId, Integer questionCount);

    /**
     * 批改用户答案
     * @param docId 文档ID
     * @param chapterId 章节ID
     * @param questions 题目列表（含正确答案）
     * @param answers 用户答案列表
     * @return 批改结果，包含得分和每题的反馈
     */
    Map<String, Object> gradeQuiz(Integer docId, Integer chapterId, 
                                   List<Map<String, Object>> questions, 
                                   List<String> answers);

    /**
     * 获取用户的测验历史记录
     * @param userId 用户ID
     * @param docId 文档ID
     * @return 测验记录列表
     */
    List<Map<String, Object>> getQuizHistory(String userId, Integer docId);
}
