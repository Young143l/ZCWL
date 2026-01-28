import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestAIAPI {
    public static void main(String[] args) {
        try {
            // 配置
            String apiKey = "sk-5c14837e2eabcd567f88db15f374f68717fe3c3d8a15532672b21e1bbd192436";
            String baseUrl = "https://api.qnaigc.com/v1";
            String model = "xiaomi/mimo-v2-flash";
            Double temperature = 0.7;

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", temperature);
            requestBody.put("stream", false);

            List<Map<String, String>> messages = new ArrayList<>();
            // 系统消息
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一个智能助手，需要根据用户的问题提供准确、有用的回答。");
            messages.add(systemMessage);

            // 用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "你好，你是谁？");
            messages.add(userMessage);

            requestBody.put("messages", messages);

            System.out.println("AI API Request Body: " + requestBody);
            System.out.println("AI API Base URL: " + baseUrl);
            System.out.println("AI API Key: " + apiKey.substring(0, 10) + "...");

            // 构建请求URL
            String url = baseUrl + "/chat/completions";
            System.out.println("Calling AI API at: " + url);

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求实体
            RequestEntity<Map<String, Object>> requestEntity = RequestEntity
                    .post(url)
                    .headers(headers)
                    .body(requestBody);

            // 初始化RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // 调用API
            System.out.println("Sending request to AI API...");
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    requestEntity,
                    Map.class
            );

            // 记录响应状态
            System.out.println("AI API Response Status: " + responseEntity.getStatusCode());
            System.out.println("AI API Response Headers: " + responseEntity.getHeaders());

            // 获取响应体
            Map<String, Object> response = responseEntity.getBody();
            System.out.println("AI API Response Body: " + response);

            // 提取回答
            if (response != null) {
                // 检查是否有错误字段
                if (response.containsKey("error")) {
                    System.out.println("AI API returned error: " + response.get("error"));
                } else {
                    Object choicesObj = response.get("choices");
                    if (choicesObj instanceof List<?> choicesList) {
                        System.out.println("AI API Choices List Size: " + choicesList.size());
                        if (!choicesList.isEmpty()) {
                            Object choiceObj = choicesList.get(0);
                            if (choiceObj instanceof Map<?, ?> choiceMap) {
                                Object messageObj = choiceMap.get("message");
                                if (messageObj instanceof Map<?, ?> messageMap) {
                                    Object contentObj = messageMap.get("content");
                                    if (contentObj instanceof String) {
                                        String answer = (String) contentObj;
                                        System.out.println("AI API Answer: " + answer);
                                    } else {
                                        System.out.println("AI API Content is not a string: " + contentObj);
                                    }
                                } else {
                                    System.out.println("AI API Message is not a map: " + messageObj);
                                }
                            } else {
                                System.out.println("AI API Choice is not a map: " + choiceObj);
                            }
                        } else {
                            System.out.println("AI API Choices list is empty");
                        }
                    } else {
                        System.out.println("AI API Choices is not a list: " + choicesObj);
                    }
                }
            } else {
                System.out.println("AI API Response is null");
            }

        } catch (Exception e) {
            System.out.println("Error calling AI API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
