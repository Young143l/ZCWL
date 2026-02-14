import { ApiClient } from "./apiClient";
import { EmbeddingService } from "./embeddingService";
import { DatabaseService } from "./databaseService";
import { DocItem, DocDirItem, ChunkVector } from "./types";
import { MAX_CHUNK_SIZE } from "./config";

export class DocProcessor {
    private apiClient: ApiClient;
    private embeddingService: EmbeddingService;
    private databaseService: DatabaseService;

    constructor() {
        this.apiClient = new ApiClient();
        this.embeddingService = new EmbeddingService();
        this.databaseService = new DatabaseService();
    }

    async processDocument(
        docId: string,
        chapterId: string,
        content: string,
    ): Promise<ChunkVector[]> {
        console.log(`处理文档 ${docId} 章节 ${chapterId}...`);

        // 语义切片
        const chunks = await this.apiClient.aiSemanticChunking(
            content,
            MAX_CHUNK_SIZE,
        );
        console.log(`切分为 ${chunks.length} 个片段`);

        // 向量化
        const chunkVectors: ChunkVector[] = [];
        for (let i = 0; i < chunks.length; i++) {
            console.log(`处理片段 ${i + 1}/${chunks.length}...`);
            const vector = await this.embeddingService.getEmbedding(chunks[i]);
            chunkVectors.push({
                d_id: parseInt(docId),
                c_id: parseInt(chapterId),
                chunk: chunks[i],
                vector: vector,
            });
        }

        return chunkVectors;
    }

    async processAll(): Promise<void> {
        try {
            // 清空数据库
            await this.databaseService.clearDatabase();

            // 获取文档列表
            const docList = await this.apiClient.getDocList();
            console.log(`找到 ${docList.length} 个文档`);

            const allChunks: ChunkVector[] = [];

            // 处理每个文档
            for (const doc of docList) {
                console.log(`\n处理文档: ${doc.name} (ID: ${doc.id})`);

                // 获取文档详细信息和目录
                const docInfo = await this.apiClient.getDocInfo(doc.id);
                console.log(`文档有 ${docInfo.docDir.length} 个章节`);

                // 处理每个章节
                for (const chapter of docInfo.docDir) {
                    try {
                        const content = await this.apiClient.getDocContent(
                            doc.id,
                            chapter.id,
                        );
                        const chunks = await this.processDocument(
                            doc.id,
                            chapter.id,
                            content.content,
                        );
                        allChunks.push(...chunks);
                    } catch (error) {
                        console.error(
                            `处理章节 ${chapter.name} (ID: ${chapter.id}) 失败:`,
                            error,
                        );
                        continue;
                    }
                }
            }

            // 保存到数据库
            if (allChunks.length > 0) {
                await this.databaseService.saveToDatabase(allChunks);
                console.log(`\n总共处理了 ${allChunks.length} 个切片`);
            } else {
                console.log("\n没有找到需要处理的内容");
            }
        } catch (error) {
            console.error("处理过程中发生错误:", error);
            throw error;
        }
    }

    async close(): Promise<void> {
        await this.databaseService.close();
    }
}
