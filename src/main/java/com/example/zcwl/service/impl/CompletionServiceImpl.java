package com.example.zcwl.service.impl;

import com.example.zcwl.service.CompletionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AI 代码补全服务实现类
 * 提供 Copilot 风格的内联代码补全功能
 */
@Service
public class CompletionServiceImpl implements CompletionService {

    private static final Logger logger = LoggerFactory.getLogger(CompletionServiceImpl.class);

    private final RestTemplate restTemplate;

    // 速率限制：最多每 3 秒调用一次 AI API
    private final AtomicLong lastApiCallTime = new AtomicLong(0);
    private static final long MIN_INTERVAL_MS = 3000;

    // 重试配置：最多重试 3 次
    private static final int MAX_RETRIES = 3;
    private static final long BASE_RETRY_DELAY_MS = 2000;

    // AI服务配置 - 代码生成模块
    @Value("${spring.ai.code.api-key}")
    private String codeApiKey;

    @Value("${spring.ai.code.base-url}")
    private String codeBaseUrl;

    @Value("${spring.ai.code.chat.options.model}")
    private String codeModel;

    @Value("${spring.ai.code.chat.options.temperature}")
    private Double codeTemperature;

    @Autowired
    public CompletionServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, Object> getCompletion(
            String language,
            String prefixCode,
            String suffixCode,
            String currentLineContent,
            String fullCode) {

        logger.debug("获取代码补全, 语言: {}", language);

        try {
            // 构建提示词
            String prompt = buildCompletionPrompt(language, prefixCode, suffixCode, currentLineContent, fullCode);

            // 速率限制等待
            throttleIfNeeded();

            // 调用 AI 服务（带重试）
            String aiResponse = callAIServiceWithRetry(prompt);

            // 解析补全结果
            String completion = parseCompletionResponse(aiResponse);

            Map<String, Object> result = new HashMap<>();
            result.put("completion", completion);
            return result;
        } catch (Exception e) {
            logger.error("获取代码补全失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("completion", "");
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Flux<String> getCompletionStream(
            String language,
            String prefixCode,
            String suffixCode,
            String currentLineContent,
            String fullCode) {

        logger.debug("获取流式代码补全, 语言: {}", language);

        return Flux.create(sink -> {
            CompletableFuture.runAsync(() -> {
                try {
                    // 构建提示词
                    String prompt = buildCompletionPrompt(language, prefixCode, suffixCode, currentLineContent, fullCode);

                    // 构建请求体，设置 stream: true
                Map<String, Object> requestBody = new HashMap<>();
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "user", "content", prompt));
                requestBody.put("messages", messages);
                requestBody.put("model", codeModel);
                requestBody.put("temperature", codeTemperature != null ? codeTemperature : 0.3);
                requestBody.put("reasoning_effort", "low");
                    requestBody.put("stream", true);

                    String url = codeBaseUrl + "/chat/completions";

                    try (java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                            .connectTimeout(java.time.Duration.ofSeconds(30))
                            .build()) {

                        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(url))
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + codeApiKey)
                                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                                        new ObjectMapper().writeValueAsString(requestBody)))
                                .build();

                        java.net.http.HttpResponse<java.io.InputStream> response = httpClient.send(
                                request,
                                java.net.http.HttpResponse.BodyHandlers.ofInputStream());

                        if (response.statusCode() == 200) {
                            try (java.io.InputStream inputStream = response.body();
                                 BufferedReader reader = new BufferedReader(
                                         new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8),
                                         131072)) {
                                String line;
                                StringBuilder fullCompletion = new StringBuilder();
                                while ((line = reader.readLine()) != null) {
                                    if (sink.isCancelled()) {
                                        break;
                                    }
                                    if (line.startsWith("data: ")) {
                                        String data = line.substring(6);
                                        if ("[DONE]".equals(data)) {
                                            break;
                                        }
                                        try {
                                            Map<String, Object> chunk = new ObjectMapper().readValue(data,
                                                    new com.fasterxml.jackson.core.type.TypeReference<>() {});
                                            List<?> choices = (List<?>) chunk.get("choices");
                                            if (choices != null && !choices.isEmpty()) {
                                                Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                                                Map<?, ?> delta = (Map<?, ?>) choice.get("delta");
                                                if (delta != null && delta.containsKey("content")) {
                                                    String text = delta.get("content").toString();
                                                    fullCompletion.append(text);
                                                    // 发送 SSE 格式
                                                    sink.next("data: {\"type\": \"delta\", \"text\": \"" +
                                                            escapeJson(text) + "\"}\n\n");
                                                }
                                            }
                                        } catch (Exception e) {
                                            // 跳过解析失败的 chunk
                                        }
                                    }
                                }
                                // 发送完成信号
                                sink.next("data: {\"type\": \"done\"}\n\n");
                            }
                        } else {
                            sink.error(new RuntimeException("AI服务返回错误状态码: " + response.statusCode()));
                        }
                    }
                } catch (Exception e) {
                    logger.error("流式补全失败", e);
                    sink.error(e);
                }
                sink.complete();
            });
        });
    }

    /**
     * 构建代码补全提示词
     */
    private String buildCompletionPrompt(
            String language,
            String prefixCode,
            String suffixCode,
            String currentLineContent,
            String fullCode) {

        return String.format("""
                你是一个代码补全助手。根据给定的代码上下文，补全光标位置（<CURSOR>标记处）之后的代码。
                
                ## 规则
                1. **必须返回代码补全内容，不能返回空**。如果不知道补全什么，至少返回一个合理的语法补全（如分号、括号、换行等）。
                2. 只返回补全的代码，不要任何额外解释，不要思考过程。
                3. 保持与原代码一致的缩进风格。
                4. 补全内容要自然、符合该语言的语法和常见模式。
                5. **最多补全 5 行代码**，尽量简短。
                6. 如果是单行补全，不要换行。
                
                ## 代码语言
                %s
                
                ## 当前行内容
                %s
                
                ## 光标前代码
                %s<CURSOR>
                
                ## 光标后代码
                %s
                """,
                language,
                currentLineContent != null ? currentLineContent : "",
                prefixCode != null ? prefixCode : "",
                suffixCode != null ? suffixCode : "");
    }

    /**
     * 速率限制：确保两次 API 调用之间至少间隔 MIN_INTERVAL_MS
     */
    private void throttleIfNeeded() {
        long now = System.currentTimeMillis();
        long lastCall = lastApiCallTime.get();
        long elapsed = now - lastCall;
        if (elapsed < MIN_INTERVAL_MS) {
            long waitTime = MIN_INTERVAL_MS - elapsed;
            logger.debug("速率限制等待 {}ms", waitTime);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 调用 AI 服务（带重试和指数退避）
     */
    private String callAIServiceWithRetry(String prompt) {
        Exception lastException = null;
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                // 更新最后调用时间戳
                lastApiCallTime.set(System.currentTimeMillis());

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);
            requestBody.put("model", codeModel);
            requestBody.put("temperature", codeTemperature != null ? codeTemperature : 0.3);
            requestBody.put("reasoning_effort", "low");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + codeApiKey);

                RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                        .post(URI.create(codeBaseUrl + "/chat/completions"))
                        .headers(headers)
                        .body(requestBody);

                ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                        requestEntity,
                        new org.springframework.core.ParameterizedTypeReference<>() {});

                Map<String, Object> response = responseEntity.getBody();
                if (response != null && response.containsKey("choices")) {
                    List<?> choicesList = (List<?>) response.get("choices");
                    if (!choicesList.isEmpty()) {
                        Map<?, ?> choiceMap = (Map<?, ?>) choicesList.get(0);
                        if (choiceMap.containsKey("message")) {
                            Map<?, ?> messageMap = (Map<?, ?>) choiceMap.get("message");
                            if (messageMap.containsKey("content")) {
                                String content = messageMap.get("content").toString();
                                // 检测空内容，空内容也重试
                                if (content == null || content.trim().isEmpty()) {
                                    throw new RuntimeException("AI返回内容为空");
                                }
                                return content;
                            }
                        }
                    }
                }
                throw new RuntimeException("AI服务响应格式异常");
            } catch (Exception e) {
                lastException = e;
                String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                boolean isRateLimited = errorMsg.contains("429") || errorMsg.contains("rate limit");
                boolean isEmptyResponse = errorMsg.contains("AI返回内容为空") || errorMsg.contains("响应格式异常");
                
                if (attempt < MAX_RETRIES - 1) {
                    // 指数退避
                    long delayMs = BASE_RETRY_DELAY_MS * (long) Math.pow(2, attempt);
                    logger.warn("API调用失败 (attempt {}/{})，等待 {}ms 后重试, 原因: {}", 
                            attempt + 1, MAX_RETRIES, delayMs, errorMsg.length() > 100 ? errorMsg.substring(0, 100) : errorMsg);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    logger.error("调用AI服务失败，已耗尽所有重试 (attempt {}/{})", attempt + 1, MAX_RETRIES, e);
                    throw new RuntimeException("调用AI服务失败: " + e.getMessage());
                }
            }
        }
        throw new RuntimeException("调用AI服务失败，已重试" + MAX_RETRIES + "次: " +
                (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    /**
     * 调用 AI 服务（原始方法，保留供流式调用使用）
     */
    private String callAIService(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);
            requestBody.put("model", codeModel);
            requestBody.put("temperature", codeTemperature != null ? codeTemperature : 0.3);
            requestBody.put("reasoning_effort", "low");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + codeApiKey);

            RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                    .post(URI.create(codeBaseUrl + "/chat/completions"))
                    .headers(headers)
                    .body(requestBody);

            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    requestEntity,
                    new org.springframework.core.ParameterizedTypeReference<>() {});

            Map<String, Object> response = responseEntity.getBody();
            if (response != null && response.containsKey("choices")) {
                List<?> choicesList = (List<?>) response.get("choices");
                if (!choicesList.isEmpty()) {
                    Map<?, ?> choiceMap = (Map<?, ?>) choicesList.get(0);
                    if (choiceMap.containsKey("message")) {
                        Map<?, ?> messageMap = (Map<?, ?>) choiceMap.get("message");
                        if (messageMap.containsKey("content")) {
                            return messageMap.get("content").toString();
                        }
                    }
                }
            }
            throw new RuntimeException("AI服务响应格式异常");
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            throw new RuntimeException("调用AI服务失败: " + e.getMessage());
        }
    }

    /**
     * 解析补全响应
     */
    private String parseCompletionResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "";
        }
        // 清理可能的 markdown 代码块标记
        String cleaned = aiResponse.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            int lastBackticks = cleaned.lastIndexOf("```");
            if (lastBackticks > 0) {
                cleaned = cleaned.substring(0, lastBackticks);
            }
        }
        return cleaned.trim();
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String language, String prefixCode, String suffixCode) {
        return language + "_" +
                (prefixCode != null ? prefixCode.hashCode() : "") + "_" +
                (suffixCode != null ? suffixCode.hashCode() : "");
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final String completion;
        final long expiryTime;

        CacheEntry(String completion, long expiryTime) {
            this.completion = completion;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}