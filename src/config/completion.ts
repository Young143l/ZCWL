/**
 * AI 代码补全配置
 * 
 * 控制 Copilot 风格内联补全的行为和 UI 样式
 */
export interface CompletionConfig {
  /** 是否启用 AI 补全 */
  enabled: boolean;
  
  /** 去抖延迟（毫秒），输入停止后等待多久再请求补全 */
  debounceDelay: number;
  
  /** 上下文缓存时间（毫秒），相同上下文的补全结果缓存时间 */
  cacheTTL: number;
  
  /** 发送给 LLM 的光标前最大字符数 */
  maxPrefixChars: number;
  
  /** 发送给 LLM 的光标后最大字符数 */
  maxSuffixChars: number;
  
  /** 是否启用流式补全 */
  enableStream: boolean;
  
  /** 多语言支持列表 */
  supportedLanguages: string[];
}

const defaultConfig: CompletionConfig = {
  enabled: true,
  debounceDelay: 500,
  cacheTTL: 60000,
  maxPrefixChars: 500,
  maxSuffixChars: 200,
  enableStream: false,
  supportedLanguages: ['python', 'html', 'css', 'javascript', 'typescript'],
};

export default defaultConfig;