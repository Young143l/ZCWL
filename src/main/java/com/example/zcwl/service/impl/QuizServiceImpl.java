package com.example.zcwl.service.impl;

import com.example.zcwl.entity.DocContents;
import com.example.zcwl.repository.DocContentsRepository;
import com.example.zcwl.service.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI智能测验服务实现类
 * 调用AI API根据文档内容生成测验题目并批改答案
 */
@Service
public class QuizServiceImpl implements QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final DocContentsRepository docContentsRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @Autowired
    public QuizServiceImpl(DocContentsRepository docContentsRepository, RestTemplate restTemplate) {
        this.docContentsRepository = docContentsRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Map<String, Object>> generateQuiz(Integer docId, Integer chapterId, Integer questionCount) {
        // 获取文档内容
        String content = getDocumentContent(docId, chapterId);
        if (content == null || content.isBlank()) {
            logger.warn("文档内容为空，无法生成测验，docId: {}, chapterId: {}", docId, chapterId);
            return Collections.emptyList();
        }

        // 限制内容长度，避免token超限
        String limitedContent = content.length() > 6000 ? content.substring(0, 6000) + "..." : content;

        int count = (questionCount != null && questionCount > 0 && questionCount <= 10) ? questionCount : 5;

        // 构建AI请求
        Map<String, Object> requestBody = buildQuizRequest(limitedContent, count);

        try {
            // 调用AI API
            ResponseEntity<Map<String, Object>> responseEntity = callOpenAIApi(requestBody);

            if (responseEntity.getBody() != null) {
                Object choicesObj = responseEntity.getBody().get("choices");
                if (choicesObj instanceof List<?> choicesList && !choicesList.isEmpty()) {
                    Object choiceObj = choicesList.getFirst();
                    if (choiceObj instanceof Map<?, ?> choiceMap) {
                        Object messageObj = choiceMap.get("message");
                        if (messageObj instanceof Map<?, ?> messageMap) {
                            Object contentObj = messageMap.get("content");
                            if (contentObj instanceof String answer) {
                                // 解析AI返回的题目
                                return parseQuizQuestions(answer);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("生成测验题目失败: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> gradeQuiz(Integer docId, Integer chapterId,
                                          List<Map<String, Object>> questions,
                                          List<String> answers) {
        Map<String, Object> result = new HashMap<>();
        
        if (questions == null || answers == null || questions.isEmpty() || answers.isEmpty()) {
            result.put("error", "题目或答案不能为空");
            return result;
        }

        // 构建批改请求
        Map<String, Object> requestBody = buildGradeRequest(questions, answers);

        try {
            ResponseEntity<Map<String, Object>> responseEntity = callOpenAIApi(requestBody);

            if (responseEntity.getBody() != null) {
                Object choicesObj = responseEntity.getBody().get("choices");
                if (choicesObj instanceof List<?> choicesList && !choicesList.isEmpty()) {
                    Object choiceObj = choicesList.getFirst();
                    if (choiceObj instanceof Map<?, ?> choiceMap) {
                        Object messageObj = choiceMap.get("message");
                        if (messageObj instanceof Map<?, ?> messageMap) {
                            Object contentObj = messageMap.get("content");
                            if (contentObj instanceof String gradeResult) {
                                return parseGradeResult(gradeResult, questions, answers);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("批改答案失败: {}", e.getMessage(), e);
            result.put("error", "批改失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getQuizHistory(String userId, Integer docId) {
        // 简版：返回空列表，后续可以扩展数据库存储
        return new ArrayList<>();
    }

    /**
     * 获取文档内容
     */
    private String getDocumentContent(Integer docId, Integer chapterId) {
        StringBuilder content = new StringBuilder();

        if (chapterId != null) {
            // 获取指定章节内容
            Optional<DocContents> opt = docContentsRepository.findById(
                new DocContents.DocContentsId(docId, chapterId));
            if (opt.isPresent()) {
                content.append("章节名称：").append(opt.get().getName()).append("\n\n");
                content.append(opt.get().getContent());
            }
        } else {
            // 获取整个文档所有章节内容
            List<DocContents> chapters = docContentsRepository.findByIdDocId(docId);
            if (chapters != null && !chapters.isEmpty()) {
                for (DocContents dc : chapters) {
                    content.append("## ").append(dc.getName()).append("\n\n");
                    content.append(dc.getContent()).append("\n\n");
                }
            }
        }

        return content.toString();
    }

    /**
     * 构建生成测验的AI请求
     */
    private Map<String, Object> buildQuizRequest(String content, int count) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);

        List<Map<String, Object>> messages = new ArrayList<>();

        // 系统提示词
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", String.format(
            "你是一个专业的AI教育测验出题助手。请根据提供的文档内容，生成%d道选择题。" +
            "要求：\n" +
            "1. 每道题有4个选项（A、B、C、D）\n" +
            "2. 题目必须基于文档内容，考察关键知识点\n" +
            "3. 选项要有一定的迷惑性，不能太简单\n" +
            "4. 必须标注正确答案\n" +
            "5. 为每道题提供简要的知识点解析\n\n" +
            "请严格按照以下JSON格式返回（不要添加任何其他文字）：\n" +
            "{\n" +
            "  \"questions\": [\n" +
            "    {\n" +
            "      \"question\": \"题目内容\",\n" +
            "      \"options\": {\"A\": \"选项A\", \"B\": \"选项B\", \"C\": \"选项C\", \"D\": \"选项D\"},\n" +
            "      \"answer\": \"A\",\n" +
            "      \"explanation\": \"解析：为什么选这个答案\"\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            count));
        messages.add(systemMessage);

        // 用户消息 - 文档内容
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "请基于以下文档内容生成" + count + "道选择题：\n\n" + content);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("max_tokens", 4096);

        return requestBody;
    }

    /**
     * 构建批改答案的AI请求
     */
    private Map<String, Object> buildGradeRequest(List<Map<String, Object>> questions, List<String> answers) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.3);

        List<Map<String, Object>> messages = new ArrayList<>();

        // 构建题目和答案的文本
        StringBuilder content = new StringBuilder();
        content.append("请批改以下测验答案，逐题判断对错并给出解析：\n\n");

        for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> q = questions.get(i);
            content.append("第").append(i + 1).append("题：").append(q.get("question")).append("\n");
            content.append("正确答案：").append(q.get("answer")).append("\n");
            @SuppressWarnings("unchecked")
            Map<String, String> options = (Map<String, String>) q.get("options");
            if (options != null) {
                content.append("选项：").append(options).append("\n");
            }
            String userAnswer = i < answers.size() ? answers.get(i) : "未作答";
            content.append("用户答案：").append(userAnswer).append("\n\n");
        }

        // 系统提示词
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", 
            "你是一个AI教育批改助手。请根据题目和用户答案进行批改，返回JSON格式结果。\n" +
            "请严格按照以下JSON格式返回（不要添加任何其他文字）：\n" +
            "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"questionIndex\": 0,\n" +
            "      \"correct\": true,\n" +
            "      \"userAnswer\": \"A\",\n" +
            "      \"correctAnswer\": \"A\",\n" +
            "      \"feedback\": \"回答正确！解析：...\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"score\": 4,\n" +
            "  \"total\": 5,\n" +
            "  \"summary\": \"总体评价和建议\"\n" +
            "}");
        messages.add(systemMessage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", content.toString());
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("stream", false);
        requestBody.put("max_tokens", 4096);

        return requestBody;
    }

    /**
     * 解析AI返回的题目JSON
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseQuizQuestions(String aiResponse) {
        try {
            // 尝试提取JSON部分
            String jsonStr = extractJsonFromResponse(aiResponse);
            if (jsonStr == null) {
                logger.error("无法从AI响应中提取JSON: {}", aiResponse);
                return Collections.emptyList();
            }

            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);
            Object questionsObj = parsed.get("questions");
            if (questionsObj instanceof List<?> questionsList) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Object q : questionsList) {
                    if (q instanceof Map<?, ?> qMap) {
                        Map<String, Object> question = new HashMap<>();
                        question.put("question", qMap.get("question"));
                        question.put("options", qMap.get("options"));
                        question.put("answer", qMap.get("answer"));
                        question.put("explanation", qMap.get("explanation"));
                        result.add(question);
                    }
                }
                return result;
            }
        } catch (Exception e) {
            logger.error("解析题目JSON失败: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * 解析AI返回的批改结果JSON
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGradeResult(String aiResponse, 
                                                   List<Map<String, Object>> questions,
                                                   List<String> answers) {
        Map<String, Object> result = new HashMap<>();
        try {
            String jsonStr = extractJsonFromResponse(aiResponse);
            if (jsonStr == null) {
                result.put("error", "无法解析批改结果");
                return result;
            }

            Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);

            // 提取评分信息
            Object scoreObj = parsed.get("score");
            Object totalObj = parsed.get("total");
            Object summaryObj = parsed.get("summary");
            Object resultsObj = parsed.get("results");

            int score = scoreObj instanceof Number ? ((Number) scoreObj).intValue() : 0;
            int total = totalObj instanceof Number ? ((Number) totalObj).intValue() : questions.size();
            String summary = summaryObj instanceof String ? (String) summaryObj : "";

            // 构建每题的详细反馈
            List<Map<String, Object>> details = new ArrayList<>();
            if (resultsObj instanceof List<?> resultsList) {
                for (Object r : resultsList) {
                    if (r instanceof Map<?, ?> rMap) {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("correct", rMap.get("correct"));
                        detail.put("userAnswer", rMap.get("userAnswer"));
                        detail.put("correctAnswer", rMap.get("correctAnswer"));
                        detail.put("feedback", rMap.get("feedback"));
                        details.add(detail);
                    }
                }
            }

            result.put("score", score);
            result.put("total", total);
            result.put("summary", summary);
            result.put("details", details);

        } catch (Exception e) {
            logger.error("解析批改结果失败: {}", e.getMessage(), e);
            result.put("error", "解析批改结果失败");
        }

        return result;
    }

    /**
     * 从AI响应中提取JSON部分
     * 处理AI可能在JSON前后添加额外文本的情况
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }

        // 尝试找到第一个 { 和最后一个 }
        int startIdx = response.indexOf('{');
        int endIdx = response.lastIndexOf('}');

        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }

        // 如果没找到标准JSON，尝试找 [
        startIdx = response.indexOf('[');
        endIdx = response.lastIndexOf(']');
        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }

        return null;
    }

    /**
     * 调用OpenAI API
     */
    private ResponseEntity<Map<String, Object>> callOpenAIApi(Map<String, Object> requestBody) {
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                .post(url)
                .headers(headers)
                .body(requestBody);

        return restTemplate.exchange(
                requestEntity,
                new org.springframework.core.ParameterizedTypeReference<>() {}
        );
    }
}
