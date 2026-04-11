import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import svgr from "vite-plugin-svgr";

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        react({
            babel: {
                plugins: [["babel-plugin-react-compiler"]],
            },
        }),
        tailwindcss(),
        svgr(),
    ],
    server: {
        proxy: {
            "/api": {
                target: "http://localhost:8080",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ""),
            },
            "/mcp": {
                target: "http://localhost:8001",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/mcp/, ""),
            },
            "/ws": {
                target: "ws://localhost:8080",
                changeOrigin: true,
                ws: true,
            },
        },
    },
    build: {
        // 启用 CSS 代码分割
        cssCodeSplit: true,
        // 启用源码映射（生产环境建议关闭以提升加载速度）
        sourcemap: false,
        // 压缩配置（使用 esbuild，速度更快）
        minify: "esbuild",
        // 代码分割配置
        rollupOptions: {
            output: {
                // 手动代码分割策略
                manualChunks: {
                    // React 核心库单独打包
                    "react-vendor": ["react", "react-dom"],
                    // UI 组件库单独打包
                    "ui-vendor": ["antd"],
                    // 编辑器相关库单独打包
                    "editor-vendor": ["@monaco-editor/react", "monaco-editor"],
                    // 路由和状态管理
                    "router-state": ["react-router-dom", "zustand"],
                    // Markdown 相关
                    "markdown-vendor": [
                        "react-markdown",
                        "remark-gfm",
                        "github-markdown-css",
                    ],
                },
                // 资源文件命名规则
                chunkFileNames: "assets/js/[name]-[hash].js",
                entryFileNames: "assets/js/[name]-[hash].js",
                assetFileNames: (assetInfo) => {
                    const name = assetInfo.name ?? "";
                    if (/\.(png|jpe?g|gif|svg|webp|ico)$/i.test(name)) {
                        return "assets/images/[name]-[hash][extname]";
                    }
                    if (/\.(css)$/i.test(name)) {
                        return "assets/css/[name]-[hash][extname]";
                    }
                    if (/\.(woff2?|ttf|otf|eot)$/i.test(name)) {
                        return "assets/fonts/[name]-[hash][extname]";
                    }
                    return "assets/[name]-[hash][extname]";
                },
            },
        },
        // 设置 chunk 大小警告阈值
        chunkSizeWarningLimit: 1000,
        // 启用 brotli 压缩（需要服务器支持）
        reportCompressedSize: true,
    },
    // 优化依赖预构建
    optimizeDeps: {
        // 需要预构建的依赖
        include: [
            "react",
            "react-dom",
            "antd",
            "@monaco-editor/react",
            "monaco-editor",
            "react-router-dom",
            "zustand",
            "react-markdown",
            "remark-gfm",
        ],
        // 排除某些依赖（如果有问题可以取消注释）
        // exclude: [],
    },
    // 性能优化配置
    esbuild: {
        // 删除 console 和 debugger（与 terser 配合）
        drop: ["console", "debugger"],
        // 启用 tree shaking
        treeShaking: true,
    },
});
