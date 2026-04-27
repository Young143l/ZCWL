# ZCWL MCP Server

基于 MCP 协议的智能项目代码助手，集成七牛云存储与 LangChain Agent，提供项目问答、代码分析和文档生成服务。

## 技术栈

FastAPI + LangChain + MCP + Pydantic + 七牛云 Kodo + DeepSeek V3.2

## 快速开始

### 环境要求

- Python >= 3.10
- uv (提供 uvx 命令)

### 安装与配置

```bash
pip install -r requirements.txt
cp src/config_tample.py src/config.py   # 编辑 config.py 填写 api_key
```

### 启动

```bash
python run.py               # 启动服务
python run.py --check       # 检查 MCP 连接
```

服务启动后访问 http://localhost:8001/docs 查看 API 文档。

## API

- **POST** `/ask/{id}` - 项目问答，支持代码片段分析
- **GET** `/doc/{id}` - 生成项目学习文档
- **GET** `/` - 服务状态

## 核心模块

- `src/mcp_tool.py` - 七牛云 MCP 客户端封装
- `src/agent_service.py` - LangChain Agent 管理
- `src/config.py` - 全局配置（已加入 .gitignore，不会被提交）

## 许可证

MIT License