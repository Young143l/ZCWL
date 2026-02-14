import { config } from 'dotenv';

// 加载环境变量
config();

// API配置
export const BACKEND_API_BASE = process.env.BACKEND_API_BASE || 'http://localhost:3000';
export const QINIU_API_KEY = process.env.QINIU_API_KEY || 'sk-86173eb2595dc53f7b9f7b8fe88c363e49f7aa1f429c74d32d5f0fa5633d65b5';
export const QINIU_API_URL = process.env.QINIU_API_URL || 'https://api.qnaigc.com/v1';
export const QINIU_MODEL = process.env.QINIU_MODEL || 'qwen-max-2025-01-25';
export const DASHSCOPE_API_KEY = process.env.DASHSCOPE_API_KEY || 'your_dashscope_api_key_here';
export const DASHSCOPE_EMBEDDING_MODEL = process.env.DASHSCOPE_EMBEDDING_MODEL || 'text-embedding-v4';
export const MAX_CHUNK_SIZE = parseInt(process.env.MAX_CHUNK_SIZE || '1000');

// PostgreSQL配置
export const PG_CONFIG = {
  host: process.env.PG_HOST || 'localhost',
  port: parseInt(process.env.PG_PORT || '5432'),
  database: process.env.PG_DATABASE || 'zcwl_db',
  user: process.env.PG_USER || 'zcwl',
  password: process.env.PG_PASSWORD || 'password',
};