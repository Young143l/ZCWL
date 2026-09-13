package com.example.zcwl.controller;

import com.example.zcwl.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI智能测验控制器
 * 提供基于文档内容的智能测验生成和批改API
 */
@RestController
@RequestMapping("/quiz")
public class QuizController {

    private static final Logger logger = LoggerFactory.getLogger(QuizController.class);

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * 生成测验题目
     * POST /quiz/generate
     * 
     * @param request 请求体，包含docId, chapterId(可选), questionCount(可选)
     * @return 题目列表
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQuiz(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            Integer docId = ((Number) request.get("docId")).intValue();
            Integer chapterId = request.get("chapterId") != null ? 
                ((Number) request.get("chapterId")).intValue() : null;
            Integer questionCount = request.get("questionCount") != null ? 
                ((Number) request.get("questionCount")).intValue() : 5;

            logger.info("用户 {} 为文档 {} 章节 {} 生成测验", userId, docId, chapterId);

            List<Map<String, Object>> questions = quizService.generateQuiz(docId, chapterId, questionCount);

            Map<String, Object> response = new HashMap<>();
            if (questions.isEmpty()) {
                response.put("ok", false);
                response.put("message", "生成测验失败，请确保文档内容不为空");
            } else {
                response.put("ok", true);
                response.put("questions", questions);
                response.put("count", questions.size());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("生成测验失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 批改测验答案
     * POST /quiz/grade
     * 
     * @param request 请求体，包含docId, chapterId, questions, answers
     * @return 批改结果
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/grade")
    public ResponseEntity<Map<String, Object>> gradeQuiz(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            Integer docId = ((Number) request.get("docId")).intValue();
            Integer chapterId = request.get("chapterId") != null ? 
                ((Number) request.get("chapterId")).intValue() : null;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) request.get("questions");
            @SuppressWarnings("unchecked")
            List<String> answers = (List<String>) request.get("answers");

            logger.info("用户 {} 提交测验答案，文档 {} 章节 {}，共 {} 题", 
                userId, docId, chapterId, questions != null ? questions.size() : 0);

            Map<String, Object> gradeResult = quizService.gradeQuiz(docId, chapterId, questions, answers);

            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.putAll(gradeResult);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批改测验失败: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("ok", false);
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取测验历史
     * GET /quiz/history/{docId}
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/history/{docId}")
    public ResponseEntity<Map<String, Object>> getQuizHistory(
            @PathVariable Integer docId,
            Authentication authentication) {
        String userId = authentication.getName();
        List<Map<String, Object>> history = quizService.getQuizHistory(userId, docId);

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("history", history);
        return ResponseEntity.ok(response);
    }
}
