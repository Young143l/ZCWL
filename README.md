# ZCWL 前端

![React](https://img.shields.io/badge/React-19.2.0-61DAFB?logo=react&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9.3-3178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-7.2.4-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.1.18-06B6D4?logo=tailwind-css&logoColor=white)
![Ant Design](https://img.shields.io/badge/Ant_Design-6.2.1-0170FE?logo=ant-design&logoColor=white)

智能文档与代码工作平台前端，基于 React + TypeScript + Vite 构建。

## 技术栈

React 19 / TypeScript 5 / Vite 7 / Tailwind CSS 4 / Ant Design 6 / Zustand

## 快速开始

```bash
git clone git@github.com:Young143l/ZCWL_front_end.git
cd ZCWL_front_end
pnpm install
cp .env.template .env
pnpm run dev
```

## 环境变量

| 变量 | 说明 |
|------|------|
| VITE_BACK_END | 后端 API 地址 |
| VITE_MCP_SERVER | MCP API 地址 |

## 命令

| 命令 | 说明 |
|------|------|
| pnpm run dev | 开发服务器 |
| pnpm run build | 构建生产版本 |
| pnpm run preview | 预览构建结果 |

## 目录结构

```
src/
  api/        接口
  components/ 组件
  layout/     布局
  pages/      页面
  status/     状态
  utils/      工具函数
```

## 许可证

仅供学习研究使用。