/**
 * AI 代码补全 Hook — Copilot 风格
 *
 * 工作流程：
 * 1. 按快捷键（Ctrl+Enter / Alt+\）触发补全
 * 2. 光标后出现灰色幽灵文本 "✨AI 生成中..."
 * 3. LLM 异步返回后 → 幽灵文本更新为实际补全代码
 * 4. Tab → 接受补全（写入编辑器）
 * 5. Esc → 取消补全（隐藏幽灵文本）
 * 6. 继续打字 → 自动打断生成 + 隐藏幽灵文本
 *
 * 核心机制：
 * - 第一次 trigger → provider 看到 currentAbortController 有值 → 返回 "✨AI 生成中..."
 * - LLM 返回后 → hide + setTimeout(trigger) → provider 看到 currentCompletionResult 有值 → 返回实际补全
 * - setTimeout 确保 hide 先执行完毕再 trigger
 */

import type { editor, languages, IDisposable } from 'monaco-editor';
import type { Monaco } from '@monaco-editor/react';
import { getCodeCompletion } from '../api/Code_api';
import completionConfig from '../config/completion';

// 当前正在进行的请求
let currentAbortController: AbortController | null = null;

/**
 * 取消当前正在进行的补全请求
 */
function cancelCurrentCompletion() {
  if (currentAbortController) {
    currentAbortController.abort();
    currentAbortController = null;
  }
}

/**
 * 获取补全上下文字符串
 */
function getContextInfo(
  model: editor.ITextModel,
  position: { lineNumber: number; column: number },
) {
  const fullCode = model.getValue();
  const lines = model.getLinesContent();
  const currentLine = lines[position.lineNumber - 1] || '';
  const offset = model.getOffsetAt({
    lineNumber: position.lineNumber,
    column: position.column,
  });
  const prefixCode = fullCode.substring(0, offset);
  const suffixCode = fullCode.substring(offset);
  return {
    language: model.getLanguageId(),
    prefixCode: prefixCode.slice(-completionConfig.maxPrefixChars),
    suffixCode: suffixCode.slice(0, completionConfig.maxSuffixChars),
    currentLineContent: currentLine,
    fullCode,
  };
}

/**
 * 在 Monaco Editor 上注册 Copilot 风格的 AI 内联补全
 *
 * @param monaco Monaco 实例
 * @param editor Monaco Editor 实例
 * @param token 用户认证 token
 * @param getLanguage 获取当前语言的函数
 * @param projectId 项目 ID (cpId / sfId)
 * @param isSF 是否是 SF（三件套）编辑器
 * @returns disposable 清理函数
 */
export function registerInlineCompletion(
  monaco: Monaco,
  editor: editor.IStandaloneCodeEditor,
  token: string,
  getLanguage: () => string,
  projectId?: string,
  isSF?: boolean,
): IDisposable {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const m = monaco as any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const ed = editor as any;

  // 注册 InlineCompletionsProvider
  const providerDisposable = m.languages.registerInlineCompletionsProvider(
    ['python', 'html', 'css', 'javascript'],
    {
      provideInlineCompletions: (
        model: editor.ITextModel,
        position: { lineNumber: number; column: number },
        context: languages.InlineCompletionContext,
      ) => {
        // 只响应手动触发（快捷键），不自动弹出
        if (context.triggerKind !== 1) return { items: [] };

        const language = getLanguage();
        if (model.getLanguageId() !== language) return { items: [] };

        // 优先级1：异步请求已完成，返回实际补全内容
        if (currentCompletionResult) {
          const result = currentCompletionResult;
          currentCompletionResult = null;
          const range = new m.Range(
            position.lineNumber,
            position.column,
            position.lineNumber,
            position.column,
          );
          return {
            items: [
              {
                insertText: result,
                range,
              },
            ],
          };
        }

        // 优先级2：有活跃的请求，显示 "✨AI 生成中..."
        if (currentAbortController) {
          const range = new m.Range(
            position.lineNumber,
            position.column,
            position.lineNumber,
            position.column,
          );
          return {
            items: [
              {
                insertText: '✨AI 生成中...',
                range,
              },
            ],
          };
        }

        return { items: [] };
      },
      freeInlineCompletions: () => {
        // 清理资源
      },
    },
  );

  /**
   * 刷新内联补全显示
   * 先隐藏当前 widget，再重新触发，确保 provider 被重新调用
   */
  const refreshInlineSuggestion = () => {
    ed.trigger('keyboard', 'editor.action.inlineSuggest.hide', {});
    // 使用 setTimeout 确保 hide 执行完毕后重新触发
    setTimeout(() => {
      ed.trigger('keyboard', 'editor.action.inlineSuggest.trigger', {
        triggerKind: 1,
      });
    }, 0);
  };

  // 补全触发函数（异步获取 LLM 结果）
  const triggerCompletion = async () => {
    const position = editor.getPosition();
    const model = editor.getModel();
    if (!position || !model) return;

    const language = getLanguage();
    if (model.getLanguageId() !== language) return;

    // 取消之前的请求
    cancelCurrentCompletion();

    // 创建新的 AbortController
    currentAbortController = new AbortController();

    // 第一步：触发内联补全显示 "✨AI 生成中..."
    refreshInlineSuggestion();

    // 获取上下文
    const originLineNumber = position.lineNumber;
    const originColumn = position.column;
    const context = getContextInfo(model, position);

    try {
      const result = await getCodeCompletion(
        token,
        {
          ...context,
          language,
          cpId: isSF ? undefined : projectId,
          sfId: isSF ? projectId : undefined,
        },
        isSF,
      );

      if (currentAbortController.signal.aborted) return;

      if (result.ok && 'completion' in result && result.completion) {
        // 保存补全结果
        currentCompletionResult = result.completion;
        const pos = editor.getPosition();
        // 光标位置没变 → 刷新 ghost text 为实际补全内容
        if (pos && pos.lineNumber === originLineNumber && pos.column === originColumn) {
          refreshInlineSuggestion();
        } else {
          // 光标位置变了，丢弃结果
          currentCompletionResult = null;
        }
      } else {
        // API 返回失败或无内容 → 隐藏 ghost text
        ed.trigger('keyboard', 'editor.action.inlineSuggest.hide', {});
      }
    } catch {
      // 请求失败，隐藏 ghost text
      ed.trigger('keyboard', 'editor.action.inlineSuggest.hide', {});
    } finally {
      if (currentAbortController && !currentAbortController.signal.aborted) {
        currentAbortController = null;
      }
    }
  };

  // 键盘事件监听
  const editorDomNode = editor.getDomNode();
  if (!editorDomNode) {
    return {
      dispose: () => {
        providerDisposable.dispose();
      },
    };
  }

  const handleKeyDown = (e: KeyboardEvent) => {
    // ---- Esc 取消 ----
    // 无论有无活跃请求，按 Esc 都尝试隐藏 ghost text
    // 覆盖两种场景：生成中的 "✨AI 生成中..." 和生成完成后的实际补全内容
    if (e.key === 'Escape') {
      e.preventDefault();
      cancelCurrentCompletion();
      ed.trigger('keyboard', 'editor.action.inlineSuggest.hide', {});
      return;
    }

    // ---- 打断逻辑 ----
    if (currentAbortController) {
      // 用户输入字符 → 打断生成并隐藏 ghost text
      // 注意：不能 preventDefault，否则用户无法输入
      if (!e.ctrlKey && !e.metaKey && !e.altKey && e.key.length === 1) {
        cancelCurrentCompletion();
        ed.trigger('keyboard', 'editor.action.inlineSuggest.hide', {});
        return;
      }
    }

    // ---- 触发逻辑 ----
    // Ctrl+Enter
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      e.preventDefault();
      e.stopPropagation();
      triggerCompletion();
      return;
    }

    // Alt+\
    if (e.altKey && e.key === '\\') {
      e.preventDefault();
      e.stopPropagation();
      triggerCompletion();
      return;
    }
  };

  editorDomNode.addEventListener('keydown', handleKeyDown, true);

  return {
    dispose: () => {
      cancelCurrentCompletion();
      providerDisposable.dispose();
      editorDomNode.removeEventListener('keydown', handleKeyDown, true);
    },
  };
}

// 存储异步请求完成后的补全结果（用于 refresh → provider 返回实际补全）
let currentCompletionResult: string | null = null;
