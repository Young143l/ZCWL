# 七牛云 AI 助手 (ZCWL MCP Server)

基于 MCP (Model Context Protocol) 的智能项目代码助手，集成七牛云存储与 LangChain Agent，提供项目问答、代码分析和文档生成服务。

## ✨ 功能特性

- **🔍 智能项目问答** - 针对项目代码进行自然语言问答
- **📄 代码片段分析** - 支持指定代码行范围进行深度分析
- **📚 文档自动生成** - 自动生成项目学习分析文档
- **☁️ 七牛云集成** - 无缝对接七牛云对象存储服务
- **🤖 AI Agent** - 基于 LangChain 的智能代理系统
- **⚡ FastAPI** - 高性能异步 API 服务

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| Web 框架 | FastAPI + Uvicorn |
| AI 框架 | LangChain + LangChain-OpenAI |
| MCP SDK | mcp >= 1.1.2 |
| 数据验证 | Pydantic v2 |
| 云存储 | 七牛云 Kodo |
| LLM | DeepSeek V3.2 (via API) |

## 📁 项目结构

```
ZCWL_mcp_server/
├── src/
│   ├── __init__.py          # 包初始化
│   ├── main.py              # FastAPI 主程序
│   ├── config.py            # 全局配置
│   ├── mcp_tool.py          # MCP 客户端封装
│   └── agent_service.py     # LangChain Agent 服务
├── test/
│   └── index.html           # 前端测试页面
├── run.py                   # 服务启动入口
├── requirements.txt         # 依赖列表
└── README.md               # 项目说明
```

## 🚀 快速开始

### 环境要求

- Python >= 3.10
- [uv](https://github.com/astral-sh/uv) 或 uvx (用于运行 MCP Server)

### 安装依赖

```bash
pip install -r requirements.txt
```

### 配置

编辑 `src/config.py` 配置以下信息：

```python
# 七牛云配置
ak = "your-qiniu-access-key"
sk = "your-qiniu-secret-key"
qiniu_kado_url = "your-bucket.domain.com"

# AI API 配置
api_key = "your-openai-api-key"
model_name = "deepseek/deepseek-v3.2-251201"
base_url = "https://api.qnaigc.com/v1"

# 服务配置
host = "0.0.0.0"
port = 8001
```

### 启动服务

```bash
# 启动 API 服务
python run.py

# 检查 MCP 连接
python run.py --check
```

服务启动后访问：
- API 文档: http://localhost:8001/docs
- 前端页面: http://localhost:8001/index.html

## 📡 API 接口

### 1. 项目问答

**POST** `/ask/{id}`

询问项目相关问题，支持代码片段分析。

**请求参数：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | path | 项目ID（七牛云存储路径） |
| ask | body | 用户问题 |
| codeSnap | body | 代码片段列表（可选） |

**请求示例：**

```json
{
  "ask": "这个函数的作用是什么？",
  "codeSnap": [
    {
      "fileName": "/src/main.py",
      "lineStart": 10,
      "lineEnd": 25
    }
  ]
}
```

**响应：**

```json
{
  "ans": "这个函数用于处理用户请求..."
}
```

### 2. 生成项目文档

**GET** `/doc/{id}`

生成项目的学习分析文档。

**响应：**

```json
{
  "doc": "# 项目学习文档\n\n## 1. 项目概述..."
}
```

### 3. 服务状态

**GET** `/`

获取服务基本信息。

## ⚙️ 核心模块说明

### MCP 客户端 (`src/mcp_tool.py`)

封装七牛云 MCP Server，提供：
- 与七牛云存储的连接管理
- 工具发现和调用
- LangChain 工具包装器

### Agent 服务 (`src/agent_service.py`)

管理 LangChain Agent：
- Agent 初始化和生命周期管理
- 工具调用编排
- 流式/非流式响应支持

### 配置管理 (`src/config.py`)

集中管理所有配置项，包括：
- 七牛云认证信息
- AI API 配置
- 服务运行参数

## 📝 使用示例

### 代码问答

```python
import requests

response = requests.post(
    "http://localhost:8001/ask/my-project",
    json={
        "ask": "解释 main 函数的逻辑",
        "codeSnap": [
            {"fileName": "main.py", "lineStart": 1, "lineEnd": 50}
        ]
    }
)
print(response.json()["ans"])
```

### 生成文档

```python
response = requests.get("http://localhost:8001/doc/my-project")
print(response.json()["doc"])
```

## 🔧 开发说明

### 添加新的 MCP 工具

MCP 工具会自动从七牛云 MCP Server 发现，无需手动注册。

### 自定义 Agent 提示词

修改 `src/agent_service.py` 中的 `ChatPromptTemplate`：

```python
prompt = ChatPromptTemplate.from_messages([
    ("system", "你的自定义提示词..."),
    ("human", "{input}"),
    ("placeholder", "{agent_scratchpad}")
])
```

## ⚠️ 注意事项

1. **安全性**：生产环境请使用环境变量管理敏感配置，不要硬编码在代码中
2. **存储空间**：服务仅操作 `zcwl-project` 存储空间
3. **依赖**：确保已安装 `uv` 或 `uvx` 命令
4. **网络**：需要能够访问七牛云和 AI API 服务

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**ZCWL Team** 🚀
