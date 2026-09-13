import { config } from "dotenv";
import OpenAI from "openai";

// 加载环境变量
config();

// API配置
export const BACKEND_API_BASE =
    process.env.BACKEND_API_BASE as string
export const QINIU_API_KEY =
    process.env.QINIU_API_KEY as string
export const QINIU_API_URL =
    process.env.QINIU_API_URL as string
export const QINIU_MODEL =
    process.env.QINIU_MODEL as string
export const DASHSCOPE_API_KEY =
    process.env.DASHSCOPE_API_KEY as string
export const DASHSCOPE_EMBEDDING_MODEL =
    process.env.DASHSCOPE_EMBEDDING_MODEL as string
export const MAX_CHUNK_SIZE = parseInt(process.env.MAX_CHUNK_SIZE as string);

// 创建OpenAI客户端实例（用于七牛云兼容API）
export const openaiClient = new OpenAI({
    apiKey: QINIU_API_KEY as string,
    baseURL: QINIU_API_URL as string,
});

// PostgreSQL配置
export const PG_CONFIG = {
    host: process.env.PG_HOST as string,
    port: parseInt(process.env.PG_PORT as string),
    database: process.env.PG_DATABASE as string,
    user: process.env.PG_USER as string,
    password: process.env.PG_PASSWORD as string,
};
