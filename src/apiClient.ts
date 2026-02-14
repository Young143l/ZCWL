import { BACKEND_API_BASE, openaiClient, QINIU_MODEL } from "./config";
import {
    DocItem,
    DocResponse,
    DocInfoResponse,
    DocContentResponse,
    DocDirItem,
} from "./types";

export class ApiClient {
    private backendBaseUrl: string;
    private openaiModel: string;

    constructor() {
        this.backendBaseUrl = BACKEND_API_BASE;
        this.openaiModel = QINIU_MODEL;
    }

    async getDocList(): Promise<DocItem[]> {
        console.log("获取文档列表...");
        try {
            const response = await fetch(`${this.backendBaseUrl}/doc`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = (await response.json()) as DocResponse;
            return data.docList;
        } catch (error) {
            console.error("获取文档列表失败:", error);
            throw error;
        }
    }

    async getDocInfo(
        docId: string,
    ): Promise<{ docInfo: DocItem; docDir: DocDirItem[] }> {
        console.log(`获取文档 ${docId} 详细信息...`);
        try {
            const response = await fetch(
                `${this.backendBaseUrl}/doc/${docId}`,
                {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                    },
                },
            );
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = (await response.json()) as DocInfoResponse;
            return data;
        } catch (error) {
            console.error(`获取文档 ${docId} 详细信息失败:`, error);
            throw error;
        }
    }

    async getDocContent(
        docId: string,
        chapterId: string,
    ): Promise<DocContentResponse> {
        console.log(`获取文档 ${docId} 章节 ${chapterId} 内容...`);
        try {
            const response = await fetch(
                `${this.backendBaseUrl}/doc/${docId}/${chapterId}`,
                {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                    },
                },
            );
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = (await response.json()) as DocContentResponse;
            return data;
        } catch (error) {
            console.error(
                `获取文档 ${docId} 章节 ${chapterId} 内容失败:`,
                error,
            );
            throw error;
        }
    }

    async aiSemanticChunking(
        text: string,
        maxChunkSize: number,
    ): Promise<string[]> {
        if (text.length <= maxChunkSize) {
            return [text];
        }

        console.log("调用AI进行语义切片...");

        try {
            // 构建AI提示词，要求AI按语义进行切片
            const prompt = `请将以下文本按语义完整性进行切片，每个切片不超过${maxChunkSize}个字符，保持语义完整性和上下文连贯性。返回JSON格式的数组，只包含切片内容，不要包含其他信息。

文本内容：
${text}

请严格按照以下JSON格式返回：
{"chunks": ["切片1", "切片2", "切片3", ...]}`;

            const completion = await openaiClient.chat.completions.create({
                model: this.openaiModel,
                messages: [
                    {
                        role: "system",
                        content:
                            "你是一个专业的文本处理助手，专门负责将长文本按语义完整性进行切片。",
                    },
                    {
                        role: "user",
                        content: prompt,
                    },
                ],
            });

            // 解析AI响应
            const aiResponse = completion.choices[0].message.content || "";

            // 尝试解析JSON
            try {
                const result = JSON.parse(aiResponse);
                if (Array.isArray(result.chunks)) {
                    return result.chunks.filter(
                        (chunk: string) => chunk.trim().length > 0,
                    );
                } else {
                    console.warn("AI返回的格式不符合预期，使用备用切片方法");
                    return this.fallbackChunking(text, maxChunkSize);
                }
            } catch (parseError) {
                console.warn(
                    "AI响应无法解析为JSON，使用备用切片方法:",
                    parseError,
                );
                // 如果AI返回的不是JSON格式，尝试从文本中提取切片
                return this.extractChunksFromText(aiResponse, maxChunkSize);
            }
        } catch (error) {
            console.error("AI语义切片失败:", error);
            console.log("使用备用切片方法...");
            return this.fallbackChunking(text, maxChunkSize);
        }
    }

    // 备用切片方法（原始的规则-based切片）
    fallbackChunking(text: string, maxChunkSize: number = 1000): string[] {
        if (text.length <= maxChunkSize) {
            return [text];
        }

        // 按段落切分
        const paragraphs = text
            .split("\n\n")
            .filter((p) => p.trim().length > 0);
        const chunks: string[] = [];
        let currentChunk = "";

        for (const paragraph of paragraphs) {
            if (currentChunk.length + paragraph.length <= maxChunkSize) {
                currentChunk += (currentChunk ? "\n\n" : "") + paragraph;
            } else {
                if (currentChunk) {
                    chunks.push(currentChunk);
                    currentChunk = paragraph;
                } else {
                    // 单个段落超过最大长度，按句子切分
                    const sentences = paragraph
                        .split(/[。！？.!?]/)
                        .filter((s) => s.trim().length > 0);
                    let sentenceChunk = "";
                    for (const sentence of sentences) {
                        if (
                            sentenceChunk.length + sentence.length <=
                            maxChunkSize
                        ) {
                            sentenceChunk +=
                                (sentenceChunk ? "。" : "") + sentence;
                        } else {
                            if (sentenceChunk) {
                                chunks.push(sentenceChunk + "。");
                                sentenceChunk = sentence;
                            } else {
                                // 单个句子也超过长度，强制切分
                                chunks.push(
                                    sentence.substring(0, maxChunkSize),
                                );
                                sentenceChunk =
                                    sentence.substring(maxChunkSize);
                            }
                        }
                    }
                    if (sentenceChunk) {
                        chunks.push(sentenceChunk + "。");
                    }
                }
            }
        }

        if (currentChunk) {
            chunks.push(currentChunk);
        }

        return chunks.filter((chunk) => chunk.trim().length > 0);
    }

    // 从AI文本响应中提取切片
    extractChunksFromText(aiResponse: string, maxChunkSize: number): string[] {
        // 尝试从文本中提取类似JSON的结构
        const jsonMatch = aiResponse.match(/\{[^{}]*"chunks"[^{}]*\}/);
        if (jsonMatch) {
            try {
                const result = JSON.parse(jsonMatch[0]);
                if (Array.isArray(result.chunks)) {
                    return result.chunks.filter(
                        (chunk: string) => chunk.trim().length > 0,
                    );
                }
            } catch (e) {
                // 忽略解析错误
            }
        }

        // 如果无法提取JSON，尝试按行分割
        const lines = aiResponse
            .split("\n")
            .filter((line) => line.trim().length > 0);
        const chunks: string[] = [];

        for (const line of lines) {
            // 移除可能的编号前缀
            const cleanLine = line.replace(/^\d+\.\s*|\-\s*|\*\s*/, "").trim();
            if (cleanLine.length > 0 && cleanLine.length <= maxChunkSize) {
                chunks.push(cleanLine);
            } else if (cleanLine.length > maxChunkSize) {
                // 如果单行太长，使用备用方法切分
                const subChunks = this.fallbackChunking(
                    cleanLine,
                    maxChunkSize,
                );
                chunks.push(...subChunks);
            }
        }

        return chunks.length > 0
            ? chunks
            : this.fallbackChunking(aiResponse, maxChunkSize);
    }
}
