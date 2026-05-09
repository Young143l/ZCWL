import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import svgr from "vite-plugin-svgr";
import babel from '@rolldown/plugin-babel'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        react(),
        babel({ presets: [reactCompilerPreset()] }),
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
        // 压缩配置（使用 oxc，速度更快）
        minify: "oxc",
        // 小于 4KB 的资源内联为 base64，减少 HTTP 请求
        assetsInlineLimit: 4096,
        // 模块预加载 polyfill，确保动态导入兼容性
        modulePreload: {
            polyfill: true,
        },
        // 代码分割配置
        rollupOptions: {
            output: {
                // 手动代码分割策略
                manualChunks(id: string) {
                    if (id.includes("node_modules")) {
                        if (id.includes("antd") && !id.includes("@ant-design/icons")) {
                            return "ui-vendor";
                        }
                        if (id.includes("@ant-design/icons")) {
                            return "icon-vendor";
                        }
                        if (id.includes("monaco-editor")) {
                            return "editor-vendor";
                        }
                        if (id.includes("react-router-dom") || id.includes("zustand") || id.includes("react-device-detect")) {
                            return "router-state";
                        }
                        if (id.includes("react-markdown") || id.includes("remark-gfm") || id.includes("github-markdown-css")) {
                            return "markdown-vendor";
                        }
                        if (id.includes("console-feed") || id.includes("ts-md5") || id.includes("nprogress")) {
                            return "util-vendor";
                        }
                    }
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
});
