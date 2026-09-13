import { Pool } from "pg";
import { PG_CONFIG } from "./config";
import { ChunkVector } from "./types";

export class DatabaseService {
    private pgPool: Pool;

    constructor() {
        this.pgPool = new Pool(PG_CONFIG);
    }

    async clearDatabase(): Promise<void> {
        console.log("清空数据库表 doc_rag...");
        const client = await this.pgPool.connect();
        try {
            await client.query("DELETE FROM doc_rag");
            console.log("数据库表已清空");
        } finally {
            client.release();
        }
    }

    async saveToDatabase(chunks: ChunkVector[]): Promise<void> {
        console.log(`保存 ${chunks.length} 个切片到数据库...`);
        const client = await this.pgPool.connect();
        try {
            await client.query("BEGIN");
            for (const chunk of chunks) {
                await client.query(
                    "INSERT INTO doc_rag (d_id, c_id, chunk, vector) VALUES ($1, $2, $3, $4)",
                    [
                        chunk.d_id,
                        chunk.c_id,
                        chunk.chunk,
                        `[${chunk.vector.join(",")}]`,
                    ],
                );
            }
            await client.query("COMMIT");
            console.log("数据保存成功");
        } catch (error) {
            await client.query("ROLLBACK");
            console.error("保存数据到数据库失败:", error);
            throw error;
        } finally {
            client.release();
        }
    }

    async close(): Promise<void> {
        await this.pgPool.end();
    }
}
