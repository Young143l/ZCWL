# ZCWL 智能文档与代码工作平台

ZCWL 是一个集 AI 对话、文档管理、代码生成、RAG（检索增强生成）于一体的智能工作平台。

## 仓库结构

本仓库为 ZCWL 项目 monorepo，包含全部子模块与项目资料：

| 目录 | 说明 | 技术栈 |
|------|------|--------|
| [`backend/`](backend/) | 后端服务：AI 对话、文档管理、代码生成、RAG | Java 23 · Spring Boot 4 · PostgreSQL · Redis |
| [`frontend/`](frontend/) | 前端：智能文档与代码工作平台界面 | React 19 · TypeScript · Vite · Ant Design |
| [`ai-rag/`](ai-rag/) | RAG 文档处理：AI 语义切片与向量化 | TypeScript · Bun · 阿里云百炼 · PostgreSQL |
| [`mcp-server/`](mcp-server/) | MCP Server：项目问答、代码分析与文档生成 | Python · FastAPI · LangChain · 七牛云 Kodo |
| [`doc-manager/`](doc-manager/) | 文档管理工具 | TypeScript · Bun |
| [`docs/`](docs/) | 项目资料：开发文档、申报书、教程、演示图片等 | — |

各子目录内有独立的 README，包含该模块的详细说明与启动方式。

## 快速开始

```bash
# 后端（详见 backend/README.md）
cd backend && mvn spring-boot:run

# 前端（详见 frontend/README.md）
cd frontend && bun install && bun run dev

# RAG 文档处理（详见 ai-rag/README.md）
cd ai-rag && bun install && bun run src/index.ts

# MCP Server（详见 mcp-server/README.md）
cd mcp-server && uv run run.py
```

## 说明

本项目已完结归档。各子模块原本为独立仓库，已于合并时通过 `git subtree` 保留完整提交历史。
