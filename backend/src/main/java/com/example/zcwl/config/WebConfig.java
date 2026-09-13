package com.example.zcwl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Web 配置类
 * 配置全局的 RestTemplate Bean
 */
@Configuration
public class WebConfig {

    /**
     * 配置全局 RestTemplate Bean
     * 统一设置超时、消息转换器等
     * @return 配置好的 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        // 创建 SimpleClientHttpRequestFactory
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000); // 30 秒连接超时
        requestFactory.setReadTimeout(300000); // 5 分钟读取超时，以支持长时间运行的流式请求

        // RestTemplate 默认已经包含了 Jackson 消息转换器，不需要显式添加
        // 如需自定义 ObjectMapper 配置，可以在这里进行

        return new RestTemplate(requestFactory);
    }
}
