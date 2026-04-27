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
        // 构建目标设为 es2021，减少不必要的转译和 polyfill
        target: "es2021",
        // 启用 CSS 代码分割
        cssCodeSplit: true,
        // CSS 压缩使用 lightningcss（Vite 内置支持，速度更快、压缩率更高）
        cssMinify: "lightningcss",
        // 启用源码映射（生产环境建议关闭以提升加载速度）
        sourcemap: false,
        // 压缩配置（使用 esbuild，速度更快）
        minify: "esbuild",
        // 小于 4KB 的资源内联为 base64，减少 HTTP 请求
        assetsInlineLimit: 4096,
        // 模块预加载 polyfill，确保动态导入兼容性
        modulePreload: {
            polyfill: true,
        },
        // 代码分割配置
        rollupOptions: {
            // 启用 rollup 缓存以加速二次构建
            cache: true,
            output: {
                // 手动代码分割策略
                manualChunks: {
                    // UI 组件库单独打包
                    "ui-vendor": ["antd"],
                    // antd 图标单独打包（体积较大）
                    "icon-vendor": ["@ant-design/icons"],
                    // 编辑器相关库单独打包
                    "editor-vendor": ["@monaco-editor/react", "monaco-editor"],
                    // 路由和状态管理
                    "router-state": [
                        "react-router-dom",
                        "zustand",
                        "react-device-detect",
                    ],
                    // Markdown 相关
                    "markdown-vendor": [
                        "react-markdown",
                        "remark-gfm",
                        "github-markdown-css",
                    ],
                    // 控制台日志与工具
                    "util-vendor": ["console-feed", "ts-md5", "nprogress"],
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
        // 设置 chunk 大小警告阈值（monaco-editor ~3.8MB，antd ~1MB，调高避免无意义误报）
        chunkSizeWarningLimit: 4000,
        // 启用压缩后大小报告
        reportCompressedSize: true,
    },
    // 优化依赖预构建
    optimizeDeps: {
        // 需要预构建的依赖
        include: [
            "react",
            "react-dom",
            "antd",
            "@ant-design/icons",
            "@monaco-editor/react",
            "monaco-editor",
            "react-router-dom",
            "zustand",
            "react-markdown",
            "remark-gfm",
            "console-feed",
        ],
        // 排除某些依赖（如果有问题可以取消注释）
        // exclude: [],
    },
    // 性能优化配置
    esbuild: {
        // 删除 console 和 debugger
        drop: ["console", "debugger"],
        // 启用 tree shaking
        treeShaking: true,
        // 设置字符集为 utf8，减小输出体积
        charset: "utf8",
    },
});
