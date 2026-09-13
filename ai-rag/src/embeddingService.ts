import OpenAI from "openai";
import { DASHSCOPE_API_KEY, DASHSCOPE_EMBEDDING_MODEL } from "./config";

export class EmbeddingService {
    private openai: OpenAI;

    constructor() {
        this.openai = new OpenAI({
            apiKey: DASHSCOPE_API_KEY,
            baseURL: "https://dashscope.aliyuncs.com/compatible-mode/v1",
        });
    }

    async getEmbedding(text: string): Promise<number[]> {
        console.log("获取文本向量...");
        try {
            // 使用阿里云百炼API进行向量化
            const completion = await this.openai.embeddings.create({
                model: DASHSCOPE_EMBEDDING_MODEL,
                input: text,
            });

            // 从响应中提取向量
            let vector = completion.data[0].embedding;
            console.log(`✅ 文本向量化成功，维度: ${vector.length}`);

            return vector;
        } catch (error) {
            console.error("获取文本向量失败:", error);
            throw error;
        }
    }

    // 模拟向量化方法 - 基于文本内容生成确定性的向量（备用方案）
    generateMockVector(text: string): number[] {
        // 简单的哈希向量化（仅用于演示）
        const vector: number[] = new Array(1536).fill(0);

        // 将文本转换为字符码并分配到向量中
        for (let i = 0; i < text.length; i++) {
            const charCode = text.charCodeAt(i);
            const index = i % 1536;
            vector[index] += charCode;
        }

        // 归一化向量
        const magnitude = Math.sqrt(
            vector.reduce((sum, val) => sum + val * val, 0),
        );
        if (magnitude > 0) {
            for (let i = 0; i < vector.length; i++) {
                vector[i] = vector[i] / magnitude;
            }
        }

        return vector;
    }
}
