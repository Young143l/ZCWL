# ZCWL Frontend

![React](https://img.shields.io/badge/React-19.2.0-61DAFB?logo=react&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9.3-3178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-7.2.4-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.1.18-06B6D4?logo=tailwind-css&logoColor=white)
![Ant Design](https://img.shields.io/badge/Ant_Design-6.2.1-0170FE?logo=ant-design&logoColor=white)

ZCWL (智能文档与代码工作平台) is a comprehensive web application that combines document management, code editing, and AI-powered chat functionality into a unified workspace. Built with modern web technologies, it provides an intuitive interface for managing projects, collaborating on documents, and getting AI assistance for coding and documentation tasks.

## 🌟 Features

### Document Management
- **Document Organization**: Hierarchical document structure with directories and files
- **Rich Text Editing**: Markdown-based document editing with preview support
- **Document Browsing**: Easy navigation through document lists and directories
- **Project Integration**: Documents organized within project contexts

### Code Editing
- **Integrated Code Editor**: Monaco editor integration for syntax highlighting and code completion
- **Multiple Language Support**: Support for various programming languages
- **Code Collaboration**: Share and collaborate on code snippets

### AI Chat Assistant
- **Intelligent Q&A**: AI-powered responses to technical and documentation questions
- **Context-Aware**: Understands project context and provides relevant answers
- **Streaming Responses**: Real-time response streaming for better user experience
- **Conversation History**: Maintains chat history for ongoing conversations

### User Management
- **Authentication**: Secure login system with token-based authentication
- **User Profiles**: Personalized user experience with avatar support
- **Session Management**: Persistent sessions with proper logout handling

### UI/UX Features
- **Responsive Design**: Works seamlessly across desktop and mobile devices
- **Modern Interface**: Clean, professional design using Ant Design components
- **Floating Tools**: Accessible floating toolbar for quick actions
- **Custom Themes**: Configurable theme support with Tailwind CSS

## 🚀 Quick Start

### Prerequisites
- Node.js (v18 or higher)
- npm (v8 or higher) or yarn
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone git@github.com:Young143l/ZCWL_front_end.git
   cd ZCWL_front_end
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Configure environment variables**
   - Copy the template file:
     ```bash
     cp .env.template .env
     ```
   - Edit the `.env` file and set your backend URL:
     ```env
     VITE_BACK_END='http://your-backend-url'
     ```

4. **Start the development server**
   ```bash
   npm run dev
   # or
   yarn dev
   ```

5. **Open your browser**
   Visit `http://localhost:5173` (or the port shown in the terminal)

### Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Starts the development server with hot reloading |
| `npm run build` | Builds the application for production |
| `npm run preview` | Locally previews the production build |
| `npm run lint` | Runs ESLint to check for code quality issues |

## 📁 Project Structure

```
ZCWL_front_end/
├── public/                 # Static assets
├── src/
│   ├── api/               # API service layer
│   │   ├── AIChatDoc_api.ts    # AI chat API endpoints
│   │   ├── Doc_api.ts          # Document management API
│   │   ├── Login_api.ts        # Authentication API
│   │   └── Project_api.ts      # Project management API
│   ├── components/        # Reusable UI components
│   │   ├── AIChatBody_components.tsx   # AI chat message display
│   │   ├── AIChatDoc_components.tsx    # AI chat interface
│   │   ├── Chapter_components.tsx      # Document chapter components
│   │   ├── DocBreadcrumb_components.tsx # Document navigation breadcrumbs
│   │   ├── DocCard_components.tsx      # Document cards for listing
│   │   ├── FloatTools_components.tsx   # Floating toolbar
│   │   ├── Header_components.tsx       # Application header
│   │   ├── Footer_components.tsx       # Application footer
│   │   ├── SF_Ask_components.tsx       # Ask/Send functionality
│   │   ├── SF_Editor_components.tsx    # Code editor components
│   │   └── SF_View_componetns.tsx      # View/Display components
│   ├── layout/            # Layout components
│   │   └── Main_layout.tsx             # Main application layout
│   ├── pages/             # Page components
│   │   ├── Home.tsx                    # Home page
│   │   ├── Login.tsx                   # Login page
│   │   ├── NotFound.tsx                # 404 page
│   │   ├── Code/                       # Code-related pages
│   │   │   └── CodeSF.tsx              # Code editor page
│   │   ├── Document/                   # Document-related pages
│   │   │   ├── DocumentContent.tsx     # Document content view
│   │   │   ├── DocumentDirectory.tsx   # Document directory view
│   │   │   └── DocumentList.tsx        # Document list view
│   │   └── Project/                    # Project-related pages
│   │       └── ProjectList.tsx         # Project list view
│   ├── status/            # State management (Zustand stores)
│   │   ├── AIChatDoc_status.ts         # AI chat state
│   │   └── Login_status.ts             # Authentication state
│   ├── index.css          # Global styles
│   └── main.tsx           # Application entry point
├── .env.template          # Environment variables template
├── vite.config.ts         # Vite configuration
├── tsconfig.json          # TypeScript configuration
├── package.json           # Project dependencies and scripts
└── README.md              # This file
```

## ⚙️ Environment Variables

The application requires the following environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_BACK_END` | Backend API base URL | `http://localhost:3000` or `https://api.yourdomain.com` |

**Note**: Environment variables prefixed with `VITE_` are exposed to the client-side code by Vite.

## 🛠️ Technologies Used

### Core Frameworks
- **React 19**: Modern JavaScript library for building user interfaces
- **TypeScript**: Typed superset of JavaScript for better code quality
- **Vite**: Next-generation frontend build tool with instant HMR

### UI Libraries
- **Ant Design**: Enterprise-level UI design language and React components
- **Tailwind CSS**: Utility-first CSS framework for rapid UI development

### Additional Libraries
- **@monaco-editor/react**: React wrapper for Monaco Editor (VS Code's editor)
- **react-router-dom**: Declarative routing for React applications
- **zustand**: Lightweight state management solution
- **react-markdown**: Markdown renderer for React
- **github-markdown-css**: GitHub-style markdown styling

### Development Tools
- **ESLint**: JavaScript/TypeScript linter for code quality
- **Prettier**: Code formatter (configured via ESLint)

## 🧪 Testing

The project includes ESLint configuration for code quality assurance. Run the following command to check for linting errors:

```bash
npm run lint
```

## 📦 Deployment

To deploy the application:

1. **Build for production**
   ```bash
   npm run build
   ```

2. **Serve the built files**
   The build output will be in the `dist/` directory. You can serve it using any static file server:

   ```bash
   # Using Vite's preview server
   npm run preview

   # Or deploy to any static hosting service (Netlify, Vercel, GitHub Pages, etc.)
   ```
