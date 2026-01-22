# ZCWL_front_end

智创未来（ZCWL）前端项目是一个基于 React 和 Vite 构建的现代化前端应用，专注于提供轻量级、高性能的用户界面体验。

## 技术栈

- **React**: ^19.2.0 - 用于构建用户界面的 JavaScript 库
- **Vite**: ^7.2.4 - 下一代前端构建工具，提供快速的热更新和冷启动
- **TypeScript**: ~5.9.3 - 为 JavaScript 添加静态类型检查
- **Ant Design**: ^6.2.1 - 企业级 UI 设计语言和组件库
- **Tailwind CSS**: ^4.1.18 - 实用优先的 CSS 框架
- **React Router DOM**: ^7.12.0 - React 应用的声明式路由
- **Zustand**: ^5.0.10 - 轻量级状态管理解决方案

## 项目结构

```
ZCWL_front_end/
├── src/
│   ├── api/                    # API 请求模块
│   │   ├── Doc_api.ts          # 文档相关API
│   │   └── Login_api.ts        # 登录相关API
│   ├── components/             # 可复用的UI组件
│   │   ├── DocCard_components.tsx # 文档卡片组件
│   │   ├── Footer_components.tsx  # 页脚组件
│   │   ├── Header_components.tsx  # 页眉组件
│   │   ├── Hello_Sum_components.tsx # 汇总组件
│   │   └── ToTop_components.tsx     # 返回顶部组件
│   ├── layout/                 # 页面布局组件
│   │   └── Main_layout.tsx     # 主布局
│   ├── pages/                  # 页面组件
│   │   ├── DocumentContent.tsx # 文档内容页
│   │   ├── DocumentDirectory.tsx # 文档目录页
│   │   ├── DocumentList.tsx    # 文档列表页
│   │   ├── Home.tsx            # 首页
│   │   ├── Login.tsx           # 登录页
│   │   └── NotFound.tsx        # 404页面
│   ├── status/                 # 状态管理
│   │   └── Login_status.tsx    # 登录状态管理
│   ├── index.css               # 全局样式
│   └── main.tsx                # 应用入口文件
├── public/                     # 静态资源
├── .env.template               # 环境变量模板
├── .gitignore                  # Git 忽略配置
├── eslint.config.js            # ESLint 配置
├── index.html                  # HTML 模板
├── package.json                # 项目依赖和脚本
├── tsconfig.json               # TypeScript 配置
├── vite.config.ts              # Vite 配置
└── README.md                   # 项目说明文档
```

## 安装与运行

### 环境要求

- Node.js (建议 v18 或更高版本)
- npm 包管理器

### 安装步骤

1. 克隆项目到本地：
   ```bash
   git clone <repository-url>
   cd ZCWL_front_end
   ```

2. 安装项目依赖：
   ```bash
   npm install
   ```

3. 复制环境变量模板：
   ```bash
   cp .env.template .env
   ```

4. 启动开发服务器：
   ```bash
   npm run dev
   ```
   
5. 在浏览器中打开 [http://localhost:5173](http://localhost:5173) 查看应用

## 可用脚本

- `npm run dev` - 启动开发服务器（带有热重载）
- `npm run build` - 构建生产版本到 `dist` 目录
- `npm run preview` - 预览生产构建
- `npm run lint` - 运行 ESLint 进行代码检查


## 部署

要部署此应用，请执行以下步骤：

1. 构建生产版本：
   ```bash
   npm run build
   ```

2. 将 `dist/` 目录中的文件部署到静态服务器或 CDN 上
