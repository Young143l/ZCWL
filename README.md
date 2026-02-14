# ZCWL AI RAG 文档处理系统

这是一个用于处理文档、进行AI语义切片和向量化的RAG（Retrieval Augmented Generation）系统。该系统可以从后端API获取文档，使用AI进行语义切片，然后将切片内容向量化并存储到PostgreSQL数据库中。

## 功能特性

- 📄 **文档获取**: 从后端API获取文档列表、文档详情和章节内容
- ✂️ **AI语义切片**: 使用大模型（Qwen）对文档内容进行智能语义切片
- 🧠 **文本向量化**: 使用阿里云百炼API将文本转换为1536维向量
- 💾 **数据库存储**: 将切片和向量存储到PostgreSQL的`doc_rag`表中
- 🔧 **模块化设计**: 代码按功能拆分为多个模块，易于维护和扩展

## 系统架构

```
src/
├── config.ts          # 配置管理
├── types.ts           # TypeScript接口定义
├── apiClient.ts       # API客户端（后端API + AI切片）
├── embeddingService.ts # 向量化服务
├── databaseService.ts # 数据库服务
├── docProcessor.ts    # 主处理器逻辑
└── index.ts           # 应用入口
```

## 环境要求

- Node.js >= 18.x
- npm >= 8.x
- PostgreSQL >= 12.x (需要安装pgvector扩展)
- 后端API服务（提供文档数据）

## 安装与配置

### 1. 克隆项目

```bash
git clone <repository-url>
cd ZCWL_ai_rag
```

### 2. 安装依赖

```bash
npm install
```

### 3. 配置环境变量

复制示例配置文件：

```bash
cp .env.example .env
```

编辑 `.env` 文件，配置以下参数：

```env
# 后端API基础URL
BACKEND_API_BASE=http://localhost:8080

# 七牛云API配置（用于AI切片）
QINIU_API_KEY=your_qiniu_api_key_here
QINIU_API_URL=https://api.qnaigc.com/v1
QINIU_MODEL=qwen-max-2025-01-25

# 阿里云百炼API配置（用于向量化）
DASHSCOPE_API_KEY=your_dashscope_api_key_here
DASHSCOPE_EMBEDDING_MODEL=text-embedding-v4

# 切片配置
MAX_CHUNK_SIZE=1000

# PostgreSQL数据库配置
PG_HOST=localhost
PG_PORT=5432
PG_DATABASE=your_database_name
PG_USER=your_username
PG_PASSWORD=your_password
```

### 4. 数据库准备

确保PostgreSQL数据库中存在以下表结构：

```sql
-- 需要先安装pgvector扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 创建文档RAG表
CREATE TABLE doc_rag (
    id SERIAL PRIMARY KEY,
    d_id INTEGER NOT NULL,
    c_id INTEGER NOT NULL,
    chunk TEXT NOT NULL,
    vector VECTOR(1536) NOT NULL
);

-- 设置表所有者
ALTER TABLE doc_rag OWNER TO zcwl;
```

## 使用方法

### 开发模式

```bash
# 编译并运行
npm run dev

# 或者分别执行
npm run build
npm start
```

### 直接运行

```bash
# 编译TypeScript
npx tsc

# 运行应用
node dist/index.js
```

## API接口说明

系统会调用以下后端API接口：

### GET /doc
获取文档列表
- **响应**: `{ "docList": [{ "id": "1", "name": "文档名", "summary": "摘要", "img": "图片URL" }] }`

### GET /doc/:d_id
获取文档详细信息
- **响应**: `{ "docInfo": { ... }, "docDir": [{ "id": "1", "name": "章节名" }] }`

### GET /doc/:d_id/:c_id
获取章节内容
- **响应**: `{ "d_id": "1", "c_id": "1", "title": "标题", "content": "内容" }`

## 错误处理

系统包含完善的错误处理机制：

- **网络错误**: 自动重试和详细的错误日志
- **AI切片失败**: 自动降级到规则-based切片方法
- **向量化失败**: 支持备用的模拟向量化方法
- **数据库错误**: 事务回滚和错误报告

## 技术栈

- **语言**: TypeScript
- **HTTP客户端**: Axios
- **数据库**: PostgreSQL + pgvector
- **AI服务**: 
  - 七牛云API (Qwen模型) - 用于语义切片
  - 阿里云百炼API - 用于文本向量化
- **配置管理**: dotenv

## 项目结构说明

- **config.ts**: 集中管理所有配置参数
- **types.ts**: 定义所有TypeScript接口类型
- **apiClient.ts**: 封装所有API调用，包括后端文档API和AI切片API
- **embeddingService.ts**: 专门处理文本向量化，支持维度适配
- **databaseService.ts**: 数据库操作封装，支持事务处理
- **docProcessor.ts**: 核心业务逻辑，协调各个服务完成文档处理
- **index.ts**: 应用入口点

## 注意事项

1. **向量维度**: 数据库表定义为1536维向量，如果使用的embedding模型返回不同维度，系统会自动进行填充或截断
2. **API密钥安全**: 请勿将API密钥提交到版本控制系统
3. **数据库权限**: 确保数据库用户有适当的表操作权限
4. **内存使用**: 处理大量文档时注意内存使用情况

## 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用 ISC 许可证 - 详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题，请联系项目维护者。