package com.example.zcwl.service;

import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI 代码补全服务接口
 */
public interface CompletionService {

    /**
     * 获取代码补全建议（非流式）
     *
     * @param language        语言类型
     * @param prefixCode      光标前代码
     * @param suffixCode      光标后代码
     * @param currentLineContent 当前行内容
     * @param fullCode        完整代码
     * @return 补全建议列表
     */
    Map<String, Object> getCompletion(
            String language,
            String prefixCode,
            String suffixCode,
            String currentLineContent,
            String fullCode
    );

    /**
     * 获取代码补全建议（流式）
     *
     * @param language        语言类型
     * @param prefixCode      光标前代码
     * @param suffixCode      光标后代码
     * @param currentLineContent 当前行内容
     * @param fullCode        完整代码
     * @return 流式补全结果
     */
    Flux<String> getCompletionStream(
            String language,
            String prefixCode,
            String suffixCode,
            String currentLineContent,
            String fullCode
    );
}