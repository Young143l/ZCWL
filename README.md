# ZCWL Frontend - 智能文档与代码工作平台

![React](https://img.shields.io/badge/React-19.2.0-61DAFB?logo=react&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9.3-3178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-7.2.4-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.1.18-06B6D4?logo=tailwind-css&logoColor=white)
![Ant Design](https://img.shields.io/badge/Ant_Design-6.2.1-0170FE?logo=ant-design&logoColor=white)

ZCWL (智能文档与代码工作平台) 是一个集文档管理、代码编辑和AI智能对话于一体的综合性Web应用平台。基于现代化Web技术栈构建，为用户提供直观的项目管理界面、高效的文档协作环境以及强大的AI辅助编程和文档问答功能。

## 🌟 核心功能

### 文档管理系统
- **文档组织**: 支持层级化的文档结构，包含文档目录和章节内容
- **Markdown编辑**: 基于Markdown的富文本编辑，支持实时预览
- **文档浏览**: 便捷的文档列表展示和目录导航功能
- **文档信息卡**: 显示文档摘要、封面图等元信息

### 智能代码工作区
- **Simple Frontend项目**: AI驱动的前端项目生成和编辑
- **代码协作**: 支持多文件代码项目的创建、编辑和管理
- **AI代码助手**: 基于上下文的代码生成和修改建议
- **项目列表**: 用户个人代码项目的集中管理

### AI智能对话助手
- **文档问答**: 针对特定文档内容的智能问答
- **代码咨询**: 编程相关问题的技术解答
- **流式响应**: 实时流式输出，提供更好的交互体验
- **对话历史**: 完整的对话记录保存和管理
- **对话限制**: 单次对话最多100轮，确保系统稳定性

### 用户认证系统
- **安全登录**: 基于Token的用户认证机制
- **用户头像**: 支持个性化用户头像显示
- **会话管理**: 持久化用户会话状态
- **权限控制**: 不同功能模块的访问权限管理

### 现代化UI/UX
- **响应式设计**: 完美适配桌面和移动设备
- **Ant Design组件**: 企业级UI设计语言，提供专业外观
- **Tailwind CSS**: 实用优先的CSS框架，快速构建自定义样式
- **浮动工具栏**: 便捷的快速操作入口
- **主题定制**: 基于Ant Design的主题颜色配置（青色主色调）

## 🚀 快速开始

### 系统要求
- Node.js (v18 或更高版本)
- npm (v8 或更高版本) 或 yarn
- Git

### 安装步骤

1. **克隆代码仓库**
   ```bash
   git clone git@github.com:Young143l/ZCWL_front_end.git
   cd ZCWL_front_end
   ```

2. **安装依赖包**
   ```bash
   npm install
   # 或使用 yarn
   yarn install
   ```

3. **配置环境变量**
   - 复制模板文件：
     ```bash
     copy .env.template .env
     ```
   - 编辑 `.env` 文件，设置后端API地址：
     ```env
     VITE_BACK_END='http://your-backend-url'
     ```

4. **启动开发服务器**
   ```bash
   npm run dev
   # 或使用 yarn
   yarn dev
   ```

5. **访问应用**
   打开浏览器访问 `http://localhost:5173`（或终端中显示的实际端口）

### 可用脚本命令

| 命令 | 描述 |
|------|------|
| `npm run dev` | 启动开发服务器，支持热重载 |
| `npm run build` | 构建生产版本应用 |
| `npm run preview` | 本地预览生产构建版本 |
| `npm run lint` | 运行ESLint检查代码质量 |

## 📁 项目架构

```
ZCWL_front_end/
├── public/                 # 静态资源文件
│   ├── favicon.svg         # 网站图标
│   ├── logo.svg            # 应用Logo
│   ├── qwen.svg            # Qwen相关图标
│   └── sf.svg              # Simple Frontend图标
├── src/
│   ├── api/               # API服务层
│   │   ├── AIChatDoc_api.ts    # AI对话API接口
│   │   ├── Code_api.ts         # 代码项目API接口
│   │   ├── Doc_api.ts          # 文档管理API接口
│   │   ├── Login_api.ts        # 用户认证API接口
│   │   └── Project_api.ts      # 项目管理API接口（待实现）
│   ├── components/        # 可复用UI组件
│   │   ├── AIChatBody_components.tsx   # AI对话消息展示
│   │   ├── AIChatDoc_components.tsx    # AI对话主界面
│   │   ├── Chapter_components.tsx      # 文档章节组件
│   │   ├── CodeCard_components.tsx     # 代码项目卡片
│   │   ├── DocBreadcrumb_components.tsx # 文档面包屑导航
│   │   ├── DocCard_components.tsx      # 文档卡片列表
│   │   ├── DocInfoCard_components.tsx  # 文档信息卡片
│   │   ├── FloatTools_components.tsx   # 浮动工具栏
│   │   ├── Header_components.tsx       # 应用头部导航
│   │   ├── Footer_components.tsx       # 应用底部
│   │   ├── Hello_Sum_components.tsx    # 欢迎摘要组件
│   │   ├── LoadingWindow_components.tsx # 加载窗口
│   │   ├── SF_Ask_components.tsx       # Simple Frontend提问组件
│   │   ├── SF_Editor_components.tsx    # Simple Frontend编辑器
│   │   ├── SF_View_componetns.tsx      # Simple Frontend视图组件
│   │   └── UserAvatar_components.tsx   # 用户头像组件
│   ├── layout/            # 布局组件
│   │   └── Main_layout.tsx             # 主应用布局
│   ├── pages/             # 页面组件
│   │   ├── Home.tsx                    # 首页
│   │   ├── Login.tsx                   # 登录页面
│   │   ├── NotFound.tsx                # 404错误页面
│   │   ├── Template_Page.tsx           # 模板页面
│   │   ├── Code/                       # 代码相关页面
│   │   │   ├── Code.tsx                # 代码项目列表页面
│   │   │   └── CodeSF.tsx              # Simple Frontend编辑页面
│   │   ├── Document/                   # 文档相关页面
│   │   │   ├── DocumentContent.tsx     # 文档内容展示页面
│   │   │   ├── DocumentDirectory.tsx   # 文档目录页面
│   │   │   └── DocumentList.tsx        # 文档列表页面
│   │   └── Project/                    # 项目相关页面
│   │       └── ProjectList.tsx         # 项目列表页面
│   ├── status/            # 状态管理（Zustand Store）
│   │   ├── AIChatDoc_status.ts         # AI对话状态管理
│   │   └── Login_status.ts             # 用户登录状态管理
│   ├── index.css          # 全局样式文件
│   └── main.tsx           # 应用入口文件
├── .env.template          # 环境变量模板
├── vite.config.ts         # Vite构建配置
├── tsconfig.json          # TypeScript配置
├── package.json           # 项目依赖和脚本配置
└── README.md              # 项目说明文档
```

## 🗺️ 路由结构

- `/` - 首页
- `/login` - 登录页面
- `/document` - 文档列表页面
- `/document/:d_id` - 文档目录页面
- `/document/:d_id/:c_id` - 文档内容页面
- `/project` - 项目列表页面
- `/code` - 代码项目列表页面
- `/code/sf/:sf_id` - Simple Frontend项目编辑页面
- `/*` - 404错误页面

## ⚙️ 环境变量配置

应用需要以下环境变量：

| 变量 | 描述 | 示例 |
|------|------|------|
| `VITE_BACK_END` | 后端API基础URL | `http://localhost:3000` 或 `https://api.yourdomain.com` |

**注意**: 以 `VITE_` 开头的环境变量会被Vite暴露给客户端代码。

## 🛠️ 技术栈详情

### 核心框架
- **React 19**: 现代化JavaScript库，用于构建用户界面
- **TypeScript**: JavaScript的类型超集，提升代码质量和开发体验
- **Vite 7**: 下一代前端构建工具，提供即时热更新

### UI组件库
- **Ant Design 6**: 企业级UI设计语言和React组件库
- **Tailwind CSS 4**: 实用优先的CSS框架，快速构建自定义设计

### 功能库
- **@monaco-editor/react**: Monaco编辑器的React封装（VS Code编辑器内核）
- **react-router-dom**: React应用的声明式路由
- **zustand**: 轻量级状态管理解决方案
- **react-markdown**: React的Markdown渲染器
- **github-markdown-css**: GitHub风格的Markdown样式
- **remark-gfm**: GitHub Flavored Markdown扩展支持
- **console-feed**: 控制台日志展示组件

### 开发工具
- **ESLint**: JavaScript/TypeScript代码质量检查
- **Prettier**: 代码格式化（通过ESLint集成）
- **babel-plugin-react-compiler**: React编译器插件（实验性）

## 🔒 安全特性

- **Token认证**: 所有API请求都需要有效的Bearer Token
- **输入验证**: 前端表单输入长度限制（如AI对话输入限制2000字符）
- **错误处理**: 完善的错误捕获和用户友好的错误提示
- **HTTPS支持**: 生产环境推荐使用HTTPS协议

## 🧪 开发与测试

项目包含ESLint配置用于代码质量保证。运行以下命令检查代码规范：

```bash
npm run lint
```

## 📦 部署指南

### 生产构建
```bash
npm run build
```

### 部署选项
构建输出位于 `dist/` 目录，可以通过以下方式部署：

1. **Vite预览服务器**:
   ```bash
   npm run preview
   ```

2. **静态托管服务**:
   - Netlify
   - Vercel  
   - GitHub Pages
   - AWS S3 + CloudFront
   - 其他支持静态文件托管的服务

### 后端依赖
前端应用需要配合相应的后端服务，后端API需实现以下接口：
- 用户认证 (`/users/login`)
- 文档管理 (`/doc`, `/doc/:d_id`, `/doc/:d_id/:c_id`)
- AI对话 (`/ai/chat/new`, `/ai/chat/ask`)
- 代码项目 (`/code`, `/code/sf`, `/code/sf/:sf_id`)

## 📄 许可证

本项目仅供学习和研究使用。

---
*ZCWL - 智能文档与代码工作平台，让编程和文档协作更智能、更高效！*