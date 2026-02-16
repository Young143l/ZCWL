package com.example.zcwl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Web 配置类
 * 配置全局的 RestTemplate、WebClient Bean 和 CORS 过滤器
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
        requestFactory.setConnectTimeout(30000); // 30秒连接超时
        requestFactory.setReadTimeout(300000); // 5分钟读取超时，以支持长时间运行的流式请求

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
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024); // 16MB 最大内存
                })
                .build();
    }

    /**
     * 配置 CORS 过滤器
     * 允许跨域请求，支持前端从不同域访问 API
     * @return CORS 过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建 CORS 配置
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("*"); // 允许所有来源，生产环境应该设置具体的域名
        corsConfig.addAllowedMethod("*"); // 允许所有 HTTP 方法
        corsConfig.addAllowedHeader("*"); // 允许所有 HTTP 头
        corsConfig.setAllowCredentials(true); // 允许携带凭证
        corsConfig.setMaxAge(3600L); // 预检请求的缓存时间（秒）

        // 创建 URL 源配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // 对所有路径应用 CORS 配置

        // 创建并返回 CORS 过滤器
        return new CorsFilter(source);
    }
}
