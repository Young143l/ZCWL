# ZCWL 前端

![React](https://img.shields.io/badge/React-19.2.0-61DAFB?logo=react&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9.3-3178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-7.2.4-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.1.18-06B6D4?logo=tailwind-css&logoColor=white)
![Ant Design](https://img.shields.io/badge/Ant_Design-6.2.1-0170FE?logo=ant-design&logoColor=white)
![Zustand](https://img.shields.io/badge/Zustand-5.0.10-000000?logo=redux&logoColor=white)
![Monaco Editor](https://img.shields.io/badge/Monaco_Editor-0.55.1-F50057?logo=monaco-editor&logoColor=white)

智能文档与代码工作平台前端，基于 React + TypeScript + Vite 构建。

## 技术栈

- **核心框架**: React 19 (含 React Compiler)
- **类型系统**: TypeScript 5 (strict mode, verbatimModuleSyntax)
- **构建工具**: Vite 7
- **样式方案**: Tailwind CSS 4
- **UI 组件**: Ant Design 6
- **状态管理**: Zustand 5
- **代码编辑器**: Monaco Editor
- **路由管理**: React Router DOM 7

## 快速开始

```bash
git clone git@github.com:Young143l/ZCWL_front_end.git
cd ZCWL_front_end
bun install
cp .env.template .env
bun run dev
```

## 环境变量

| 变量 | 说明 |
|------|------|
| VITE_BACK_END | 后端 API 地址 |
| VITE_MCP_SERVER | MCP API 地址 |

## 命令

| 命令 | 说明 |
|------|------|
| bun run dev | 开发服务器 |
| bun run build | 构建生产版本 (先进行类型检查) |
| bun run lint | ESLint 代码检查 |
| bun run preview | 预览构建结果 |

## 功能特性

- 📄 智能文档管理
- 💻 代码片段管理
- 🤖 AI 辅助对话
- 🎨 Markdown 富文本编辑
- 🧠 思维导图支持

## 目录结构

```
src/
  api/        接口定义
  components/ 通用组件
  config/    配置文件
  hooks/     自定义 Hooks
  layout/    页面布局
  pages/     页面组件
  status/    状态管理
  stores/    Zustand Store
  utils/     工具函数
```

## 许可证

仅供学习研究使用。