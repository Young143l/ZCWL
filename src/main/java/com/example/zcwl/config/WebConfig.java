package com.example.zcwl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Web 配置类
 * 配置全局的 RestTemplate 和 WebClient Bean
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
        // 创建请求工厂，设置超时
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000); // 30秒连接超时
        requestFactory.setReadTimeout(60000); // 60秒读取超时

        // RestTemplate 默认已经包含了 Jackson 消息转换器，不需要显式添加
        // 如需自定义 ObjectMapper 配置，可以在这里进行
        
        return new RestTemplate(requestFactory);
    }

    /**
     * 配置全局 WebClient Bean
     * 统一设置超时、拦截器等
     * @return 配置好的 WebClient
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB 最大内存
                .build();
    }
}
